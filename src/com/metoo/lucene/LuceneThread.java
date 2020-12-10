package com.metoo.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
* <p>Title: LuceneThread.java</p>

* <p>Description: lucene搜索工具类，该类使用线程处理索引的建立，默认每天凌晨更新一次商城索引文件</p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 湖南创发科技有限公司 www.koala.com</p>

* @author erikzhang

* @date 2014-4-24

* @version koala_b2b2c v2.0 2015版 
 */
public class LuceneThread implements Runnable {
	private String path;
	private List<LuceneVo> vo_list = new ArrayList<LuceneVo>();
	private String type;

	public LuceneThread(String path, List<LuceneVo> vo_list) {
		super();
		this.path = path;
		this.vo_list = vo_list;
	}
	
	public LuceneThread(String path, List<LuceneVo> vo_list, String type) {
		super();
		this.path=path;
		this.vo_list=vo_list;
		this.type=type;
	}

	@Override
	public void run() {
		LuceneUtil lucene = LuceneUtil.instance();
		lucene.setIndex_path(this.path);
		//lucene.deleteAllIndex(true);
		//lucene.writeIndex(vo_list);
		if(this.type.equals("add")){
			try {
				lucene.writeIndex(vo_list);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			for(LuceneVo vo : vo_list){
				lucene.update(vo.getVo_id().toString(), vo);
			}
		}
	}
}
