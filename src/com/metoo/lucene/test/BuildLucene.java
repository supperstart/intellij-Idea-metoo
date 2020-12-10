package com.metoo.lucene.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.lucene.LuceneThread;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.LuceneVo;
import com.metoo.lucene.tools.LuceneVoTools;

/**
 * <p>
 * Title: com.metoo.lucene.test.BuildLucene
 * </p>
 * 
 * <p>
 * Description: 用户商品索引更新
 * </p>
 * 
 * 
 * 
 * <p>
 * Company: 湖南觅通科技
 * </p>
 * 
 * @author hk
 * 
 * @version metoo_b2b2c 2.0
 *
 */
@Controller
public class BuildLucene {
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IGoodsService goodsService;
	
	@RequestMapping("/build_lucene.json")
	public void build(HttpServletRequest request,
			HttpServletResponse response, String name){
		String msg = "0";
		Map params = new HashMap();
		List<Store> stores = new ArrayList<Store>();
		if(name == null || name.equals("")){
			params.put("store_status", 15);
			stores = this.storeService.query("select obj from Store obj where obj.store_status=:store_status", params, -1, -1);
		}else{
			params.put("store_status", 15);
			params.put("name", name);
			stores = this.storeService.query("select obj from Store obj where obj.store_status=:store_status and obj.store_name=:name", params, -1, -1);
		}
		String goods_lucene_path = System.getProperty("metoob2b2c.root")
				 + "luence" + File.separator
				+ "goods";
		File file = new File(goods_lucene_path);
		for(Store store : stores){
				params.clear();
				params.put("store_status", 15);
				params.put("goods_status", 0);
				params.put("store_id", store.getId());
				List<Goods> goodsList = 
						this.goodsService.query("select obj from Goods obj where 1=1 "
												+ "and obj.goods_store.store_status=:store_status "
												+ "and obj.goods_status=:goods_status "
												+ "and obj.goods_store.id=:store_id", params, -1, -1);
				if(goodsList.size() > 0){
					List<LuceneVo> goods_vo_list = new ArrayList<LuceneVo>();
						for(int i=0; i<goodsList.size(); i++){
							Goods obj = goodsList.get(i);
							if(obj.getGoods_status() == 0 
									&& obj.getGoods_store().getStore_status() == 15){
								if (!file.exists()) {CommUtil.createFolder(goods_lucene_path);}
								LuceneVo vo = this.luceneVoTools.updateGoodsIndex(obj);
								goods_vo_list.add(vo);
								/*SysConfig config = this.configService.getSysConfig();
								LuceneUtil lucene = LuceneUtil.instance();
								lucene.setConfig(config);
								lucene.setIndex_path(goods_lucene_path);
								lucene.writeIndex(vo);*/
							/*	if(i == 10){
									try {
										Thread.sleep(10000);
										System.out.println("睡眠10秒");
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										//设置中断状态，抛出异常后会清除中断状态标记位
										Thread.currentThread().interrupt();
										System.out.println("睡眠");
									}
								}*/
							}
						}
						LuceneThread goods_thread = new LuceneThread(goods_lucene_path,
								goods_vo_list, "add");
						goods_thread.run();
					msg = "success";
				}else{
					msg = "null";
				}
			}
		try {
			response.getWriter().print(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @description 更新单个商品索引
	 * @param request
	 * @param response
	 * @param id
	 * @param type
	 */
	@RequestMapping("/goodsLuence.json")
	public void goodsLuence(HttpServletRequest request,
			HttpServletResponse response, String id, String type){
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		String msg = "";
		if(obj.getGoods_status() == 0 
				&& obj.getGoods_store().getStore_status() == 15){
			String goods_lucene_path = System.getProperty("metoob2b2c.root")
					+ File.separator + "luence" + File.separator
					+ "goods";
			File file = new File(goods_lucene_path);
			if (!file.exists()) {
				CommUtil.createFolder(goods_lucene_path);
			}
			LuceneUtil lucene = LuceneUtil.instance();
			SysConfig config = this.configService.getSysConfig();
			switch(type){
				case "add":
					LuceneVo add = this.luceneVoTools.updateGoodsIndex(obj);
					lucene.setConfig(config);
					lucene.setIndex_path(goods_lucene_path);
					lucene.writeIndex(add);
					msg = "添加成功";
					break;
				case "update":
					LuceneVo vo = this.luceneVoTools.updateGoodsIndex(obj);
					lucene.setConfig(config);
					lucene.setIndex_path(goods_lucene_path);
					lucene.update(CommUtil.null2String(obj.getId()), vo);
					msg = "更新成功";
					break;
				case "delete":
					lucene.setIndex_path(goods_lucene_path);
					lucene.delete_index(id);
					msg = "删除成功";
					break;
				default:
					break;
			}
			try {
				response.getWriter().print(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@RequestMapping("/goodsUpdateLuence.json")
	public void goodsUpdateLuence(HttpServletRequest request,
			HttpServletResponse response, String id){
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
		if(goods.getGoods_status() == 0 
				&& goods.getGoods_store().getStore_status() == 15){
			String goods_lucene_path = System.getProperty("metoob2b2c.root")
					+ File.separator + "luence" + File.separator
					+ "goods";
			// 更新lucene索引
			File file = new File(goods_lucene_path);
			if (!file.exists()) {
				CommUtil.createFolder(goods_lucene_path);
			}
			LuceneVo vo = this.luceneVoTools
					.updateGoodsIndex(goods);
			LuceneUtil lucene = LuceneUtil.instance();
			lucene.setIndex_path(goods_lucene_path);
			lucene.update(CommUtil.null2String(goods.getId()), vo);
			try {
				response.getWriter().print("success");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	@Test
	public void StandardAnalyzer(){
		  try {  
	            // 要处理的文本  
	            // "lucene分析器使用分词器和过滤器构成一个“管道”，文本在流经这个管道后成为可以进入索引的最小单位，因此，一个标准的分析器有两个部分组成，一个是分词器tokenizer,它用于将文本按照规则切分为一个个可以进入索引的最小单位。另外一个是TokenFilter，它主要作用是对切出来的词进行进一步的处理（如去掉敏感词、英文大小写转换、单复数处理）等。lucene中的Tokenstram方法首先创建一个tokenizer对象处理Reader对象中的流式文本，然后利用TokenFilter对输出流进行过滤处理";  
	            String text = "The Lucene PMC is pleased to announce the release of the Apache Solr Reference Guide for Solr 4.4.";  
	  
	            // 自定义停用词  
	            String[] self_stop_words = { "Lucene", "release", "Apache" };  
	            CharArraySet cas = new CharArraySet(Version.LUCENE_44, 0, true);  
	            for (int i = 0; i < self_stop_words.length; i++) {  
	                cas.add(self_stop_words[i]);  
	            }  
	  
	            // 加入系统默认停用词  
	            Iterator<Object> itor = StandardAnalyzer.STOP_WORDS_SET.iterator();  
	            while (itor.hasNext()) {  
	                cas.add(itor.next());  
	            }  
	  
	            // 标准分词器(Lucene内置的标准分析器,会将语汇单元转成小写形式，并去除停用词及标点符号)  
	            StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_44, cas);  
	  
	            TokenStream ts = sa.tokenStream("field", text);  
	            CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);  
	  
	            ts.reset();  
	            while (ts.incrementToken()) {  
	                System.out.println(ch.toString());  
	            }  
	            ts.end();  
	            ts.close();  
	        } catch (Exception ex) {  
	            ex.printStackTrace();  
	        }  
	    }  
	
	@Test
	public void systemProperties(){
		Properties props = System.getProperties();
		System.out.println("项目真实路径：" + props.getProperty("user.dir"));
		System.out.println("JDK版本：" + props.getProperty("java.version"));
		System.out.println("JDK版本：" + props.getProperty("java.vendor"));
		System.out.println("java.home : "+System.getProperty("java.home"));
		 
        System.out.println("java.class.version : "+System.getProperty("java.class.version"));

        System.out.println("java.class.path : "+System.getProperty("java.class.path"));

        System.out.println("java.library.path : "+System.getProperty("java.library.path"));

        System.out.println("java.io.tmpdir : "+System.getProperty("java.io.tmpdir"));

        System.out.println("java.compiler : "+System.getProperty("java.compiler"));

        System.out.println("java.ext.dirs : "+System.getProperty("java.ext.dirs"));

        System.out.println("user.name : "+System.getProperty("user.name"));

        System.out.println("user.home : "+System.getProperty("user.home"));

        System.out.println("user.dir : "+System.getProperty("user.dir"));

        System.out.println("===================");

        System.out.println("package: "+Test.class.getPackage().getName());

        System.out.println("package: "+Test.class.getPackage().toString());

        System.out.println("=========================");

        String packName = Test.class.getPackage().getName();

        /*URL packurl = new URL(packName);
        System.out.println(packurl.getPath());*/

        URI packuri;
		try {
			packuri = new URI(packName);
	        System.out.println(packuri.getPath());
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        //System.out.println(packuri.toURL().getPath());

        System.out.println(packName.replaceAll("//.", "/"));

        System.out.println(System.getProperty("user.dir")+"/"+(Test.class.getPackage().getName()).replaceAll("//.", "/")+"/");

		//System.out.println(System.getProperty("metoob2b2c.root"));
		/*System.out.println(request.getSession().getServletContext());*/
	}
	
	/**
	 * 测试srtring indexof(返回下标) and split(String,limit)
	 */
	@Test
	public void cookie(){
		/*String cc = "82,486,233,1293,543,";
		for(String sp : cc.split(",", 2)){
			System.out.println(sp);
		}*/
		String indexof = "vnsdfsajipoj";
		System.out.println(indexof.indexOf("a"));
	}
	
	@Test
	public void ana(){
		try {
			// 要处理的文本
			// "lucene分析器使用分词器和过滤器构成一个“管道”，文本在流经这个管道后成为可以进入索引的最小单位，因此，一个标准的分析器有两个部分组成，一个是分词器tokenizer,它用于将文本按照规则切分为一个个可以进入索引的最小单位。另外一个是TokenFilter，它主要作用是对切出来的词进行进一步的处理（如去掉敏感词、英文大小写转换、单复数处理）等。lucene中的Tokenstram方法首先创建一个tokenizer对象处理Reader对象中的流式文本，然后利用TokenFilter对输出流进行过滤处理";
			String text = "The Lucene PMC is pleased to announce the release of the Apache Solr Reference Guide for Solr 4.4.";
 
			// 简单分词器(以非字母符来分割文本信息，并将语汇单元统一为小写形式，并去掉数字类型的字符)
			SimpleAnalyzer sa = new SimpleAnalyzer(Version.LUCENE_44);
 
			TokenStream ts = sa.tokenStream("field", text);
			CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);
 
			ts.reset();
			while (ts.incrementToken()) {
				System.out.println(ch.toString());
			}
			ts.end();
			ts.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Test
	public void str(){
		/*String msg = "asdf:qwer:fgh";
		msg.substring(0, msg.indexOf(":"));
		System.out.println(msg.substring(0, msg.indexOf(":")));*/
		
		String blank = "men ";
		StringTokenizer st = new StringTokenizer(blank," ");
		int numble = st.countTokens();
		while(st.hasMoreTokens())
		{
			String str = st.nextToken();
		}
		System.out.print(numble);
		
	}
}
