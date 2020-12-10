package com.metoo.php.manage.admin.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.beans.BeanUtils;
import com.metoo.core.beans.BeanWrapper;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.LuceneVo;
import com.metoo.lucene.tools.LuceneVoTools;

@Controller
@RequestMapping("/php/admin/goods/")
public class MGoodsManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IUserService userService;

	/**
	 * 商品审核
	 * 
	 * @param request
	 * @param response
	 * @param mulitId
	 */
	@RequestMapping("saudi.json")
	public void goodsSaudi(HttpServletRequest request, HttpServletResponse response, String mulitId, String msg) {
		Result result = null;
		if (mulitId != null && !mulitId.equals("")) {
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
				if (obj != null) {
					obj.setGoods_status(obj.getPublish_goods_status());// 设置商品发布审核后状态
					obj.setGoods_msg(null);
					this.goodsService.update(obj);
					String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
							+ File.separator + "goods";
					File file = new File(goods_lucene_path);
					if (!file.exists()) {
						CommUtil.createFolder(goods_lucene_path);
					}
					LuceneVo vo = this.luceneVoTools.updateGoodsIndex(obj);
					SysConfig config = this.configService.getSysConfig();
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setConfig(config);
					lucene.setIndex_path(goods_lucene_path);
					lucene.writeIndex(vo);
				}
			}
			result = new Result(5200, "Suucessfully");
		} else {
			result = new Result(1003, "parameter error");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 商品更新
	 * 
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param fieldName
	 * @param value
	 * @throws ClassNotFoundException
	 */
	@RequestMapping("ajax.json")
	public void goodsAjax(HttpServletRequest request, HttpServletResponse response, String mulitId, String fieldName,
			String value, String message) throws ClassNotFoundException {
		Result result = null;
		if (mulitId != null && !mulitId.equals("")) {
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
				Field[] fields = Goods.class.getDeclaredFields();
				BeanWrapper wrapper = new BeanWrapper(obj);
				Object val = null;
				Object msg = null;
				String goods_msg = "goods_msg";
				for (Field field : fields) {
					if (field.getName().equals(fieldName)) {
						Class clz = Class.forName("java.lang.String");
						if (field.getType().getName().equals("int")) {
							clz = Class.forName("java.lang.Integer");
						}
						if (field.getType().getName().equals("boolean")) {
							clz = Class.forName("java.lang.Boolean");
						}
						if (!value.equals("")) {
							val = BeanUtils.convertType(value, clz);
							msg = BeanUtils.convertType(message, Class.forName("java.lang.String"));
							wrapper.setPropertyValue(goods_msg, msg);
						} else {
							val = !CommUtil.null2Boolean(wrapper.getPropertyValue(fieldName));
						}
						wrapper.setPropertyValue(fieldName, val);
					}
				}
				if (fieldName.equals("store_recommend")) {
					if (obj.isStore_recommend()) {
						obj.setStore_recommend_time(new Date());
					} else
						obj.setStore_recommend_time(null);
				}
				if (fieldName.equals("store_creativity")) {
					if (obj.isStore_creativity()) {
						obj.setStore_creativity_time(new Date());
					} else
						obj.setStore_creativity_time(null);
				}
				if (fieldName.equals("store_deals")) {
					if (obj.isStore_deals()) {
						obj.setStore_deals_time(new Date());
						// 更新商品秒杀库存
						Random random = new Random();
						//以生成[10,20]随机数为例，首先生成0-20的随机数，
						//然后对(20-10+1)取模得到[0-10]之间的随机数，然后加上min=10，最后生成的是10-20的随机数
						int inventory = random.nextInt(40 - 18 + 1) + 18;
						obj.setStore_seckill_inventory(CommUtil.null2Int((new Double(inventory)).intValue()));
						obj.setStore_deals_inventory(50);
					} else
						obj.setStore_deals_time(null);
				}
				if (fieldName.equals("store_china")) {
					if (obj.isStore_china()) {
						obj.setStore_china_time(new Date());
					} else
						obj.setStore_china_time(null);
				}
				this.goodsService.update(obj);
				if (obj != null) {
					if (obj.getGoods_status() == 0) {
						// 更新lucene索引
						String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
								+ File.separator + "goods";
						// LuceneUtil lucene = LuceneUtil.instance();
						// lucene.setIndex_path(goods_lucene_path);
						// lucene.delete_index(id);
						File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneVo vo = new LuceneVo();
						vo.setVo_id(obj.getId());
						vo.setVo_title(obj.getGoods_name());
						vo.setVo_content(obj.getGoods_details());
						vo.setVo_type("goods");
						vo.setVo_store_price(CommUtil.null2Double(obj.getGoods_current_price()));
						vo.setVo_add_time(obj.getAddTime().getTime());
						vo.setVo_goods_salenum(obj.getGoods_salenum());
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setConfig(this.configService.getSysConfig());
						lucene.setIndex_path(goods_lucene_path);
						lucene.update(CommUtil.null2String(obj.getId()), vo);
					} else {
						String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
								+ File.separator + "goods";
						File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setConfig(this.configService.getSysConfig());
						lucene.setIndex_path(goods_lucene_path);
						lucene.delete_index(CommUtil.null2String(id));
					}
				}
			}
			result = new Result(0, "Successfully");
		} else {
			result = new Result(1003, "parameter error");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @description 设置商品的关联商品,指定推荐
	 * @param request
	 * @param response
	 * @param id
	 * @param goods_id
	 */
	@RequestMapping("correlation.json")
	public void correlation(HttpServletRequest request, HttpServletResponse response, String id, String goods_id) {
		Result result = null;
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		if (id != null && !id.equals("") && obj.getGoods_status() == 0
				&& obj.getGoods_store().getStore_status() == 15) {
			String[] ids = id.split(",");
			Map params = new HashMap();
			Set<Long> goods_ids = new HashSet<Long>();
			for (String str : ids) {
				goods_ids.add(Long.parseLong(str));
			}
			params.put("goods_ids", goods_ids);
			params.put("goods_status", 0);
			List<Goods> goodsList = this.goodsService.query(
					"select obj from Goods obj where id in (:goods_ids) and obj.goods_status=:goods_status", params, -1,
					-1);
			List<String> correlationId = new ArrayList<String>();// 优化集合转json字符串
			StringBuilder sb = new StringBuilder();
			String correlationIds = new String();
			if (goodsList.size() > 0) {
				for (Goods goods : goodsList) {
					if (goods.getGoods_status() == 0 && obj.getGoods_store().getStore_status() == 15) {
						correlationId.add(CommUtil.null2String(goods.getId()));
						sb.append(CommUtil.null2String(goods.getId()) + ",");
					}
				}
				// String join = String.join(",", correlationId);
				obj.setGoods_correlation(sb.toString());
				this.goodsService.update(obj);
			}
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5400, "Id is empty");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("utf8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
