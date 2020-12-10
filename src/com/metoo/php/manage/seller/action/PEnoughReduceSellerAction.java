package com.metoo.php.manage.seller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/php/seller/enoughReduce")
public class PEnoughReduceSellerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IEnoughReduceService enoughreduceService;
	@Autowired
	private IGoodsService goodsService;

	/**
	 * @description php满就送保存
	 * @param request
	 * @param response
	 * @param id满就送id
	 * @param currentPage
	 * @param uid用户id
	 * @param jsonmap满就送金额json
	 */
	@RequestMapping("/save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String uid, String jsonmap) {
		Result result = null;
		Map map = new HashMap();
		String msg = "";
		Store store = null;
		if (uid == null && uid.equals("")) {
			result = new Result(-100, "User information error");
		} else {
			User user = this.userService.getObjById(CommUtil.null2Long(uid));
			user = user.getParent() == null ? user : user.getParent();
			store = user.getStore();
			Map params = new HashMap();
			params.put("sid", store.getId().toString());
			List<EnoughReduce> ers = this.enoughreduceService.query(
					"select obj from EnoughReduce obj where obj.store_id=:sid and (obj.erstatus=10 or obj.erstatus=5)",
					params, -1, -1);
			for (EnoughReduce enoughReduce : ers) {
				if (enoughReduce.getErend_time().before(new Date())) {
					enoughReduce.setErstatus(20);
				}
				this.enoughreduceService.update(enoughReduce);
			}
			ers = this.enoughreduceService.query(
					"select obj from EnoughReduce obj where obj.store_id=:sid and (obj.erstatus=10 or obj.erstatus=5)",
					params, -1, -1);
			if (ers.size() > this.configService.getSysConfig().getEnoughreduce_max_count()) {
				map.put("op_title", "您当前正在审核或进行的满就减超过了规定的最大值");
				msg = "您当前正在审核或进行的满就减超过了规定的最大值";
				result = new Result(1, msg);
			} else {
				WebForm wf = new WebForm();
				EnoughReduce enoughreduce = null;
				if (CommUtil.null2String(id).equals("")) {
					enoughreduce = wf.toPo(request, EnoughReduce.class);
					enoughreduce.setAddTime(new Date());
					enoughreduce.setEr_type(1);
				} else {
					EnoughReduce obj = this.enoughreduceService.getObjById(Long.parseLong(id));
					if (obj.getErstatus() > 0) {
						map.put("op_title", "该活动不可编辑");// 审核状态 默认为0待审核 10为 审核通过
														// -10为审核未通过
														// 20为已结束。5为提交审核，此时商家不能再修改
					}
					enoughreduce = (EnoughReduce) wf.toPo(request, obj);
				}

				enoughreduce.setEr_json(jsonmap);
				String ertag = "";
				TreeMap mapType = JSON.parseObject(jsonmap, TreeMap.class);
				Iterator it = mapType.keySet().iterator();
				while (it.hasNext()) {
					double key = CommUtil.null2Double(it.next());
					double value = CommUtil.null2Double(mapType.get("key"));
					ertag += "Full " + key + " minus " + value + ",";
					// ertag = "Enoughreduce" + key + "subtract" + value + ",";
				}
				ertag = ertag.substring(0, ertag.length() - 1);
				enoughreduce.setErtag(ertag);
				enoughreduce.setErstatus(0);
				enoughreduce.setEr_type(1);
				enoughreduce.setStore_id("" + store.getId());
				enoughreduce.setStore_name(store.getStore_name());
				enoughreduce.setErgoods_ids_json("[]");
				if (id.equals("")) {
					this.enoughreduceService.save(enoughreduce);
					msg = "保存满就减活动成功";
					result = new Result(5200, msg);
				} else {
					this.enoughreduceService.update(enoughreduce);
					msg = "保存满就减活动成功";
					result = new Result(5200, msg);
				}
			}
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @Description 满就送删除
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param currentPage
	 * @param uid
	 *            用户id
	 */
	@RequestMapping("/delete.json")
	public void enoughreduce_del(HttpServletRequest request, HttpServletResponse response, String mulitId,
			String currentPage, String uid) {
		Result result = null;
		for (String id : mulitId.split(",")) {
			if (!id.equals("")) {
				EnoughReduce enoughreduce = this.enoughreduceService.getObjById(Long.parseLong(id));
				User user = this.userService.getObjById(CommUtil.null2Long(uid));
				user = user.getParent() == null ? user : user.getParent();
				Store store = user.getStore();
				if (enoughreduce.getStore_id().equals(store.getId().toString())) {
					String goods_json = enoughreduce.getErgoods_ids_json();
					if (goods_json != null && !goods_json.equals("")) {
						List<String> goods_id_list = (List) Json.fromJson(goods_json);
						for (String goods_id : goods_id_list) {
							Goods ergood = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
							if (ergood.getOrder_enough_reduce_id().equals(id)) {
								ergood.setEnough_reduce(0);
								ergood.setOrder_enough_reduce_id("");
								this.goodsService.update(ergood);
							}
						}
					}
					this.enoughreduceService.delete(Long.parseLong(id));
				}
			}
		}
		result = new Result(5200, "success");
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 满就减活动添加商品
	 * @param request
	 * @param response
	 * @param id
	 * @param mulitId
	 */
	@RequestMapping("/ajax.json")
	public void ajax(HttpServletRequest request, HttpServletResponse response, String er_id, String mulitId,
			String type, String uid) {
		Result result = null;
		EnoughReduce enoughReduce = this.enoughreduceService.getObjById(Long.parseLong(er_id));
		if (enoughReduce != null) {
			User user = this.userService.getObjById(CommUtil.null2Long(uid));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			List<String> goods_id_list = new ArrayList<String>();
			String goods_json = enoughReduce.getErgoods_ids_json();
			if (goods_json != null && !goods_json.equals("") && goods_json.length() > 2) {
				goods_id_list = (List<String>) Json.fromJson(goods_json);
			}
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				if (enoughReduce.getStore_id().equals(CommUtil.null2String(store.getId()))) {
					Goods goods = this.goodsService.getObjById(Long.parseLong(id));
					if (goods.getEnough_reduce() == 0
							|| goods.getOrder_enough_reduce_id().equals(CommUtil.null2String(enoughReduce.getId()))) {
						if (type.equals("add")) {
							// 同一个商品只能参加一个活动 满包邮为店铺活动可与其他活动同时进行
							if (goods.getGoods_type() == 1 && goods.getActivity_status() == 0
									&& goods.getOrder_enough_give_status() == 0) {
								goods_id_list.add(id);
								goods.setEnough_reduce(1);
								goods.setOrder_enough_reduce_id(CommUtil.null2String(enoughReduce.getId()));
							}
						} else {
							if (goods_id_list.contains(id)) {
								goods_id_list.remove(id);
							}
							goods.setEnough_reduce(0);
							goods.setOrder_enough_reduce_id("");
						}
						this.goodsService.update(goods);
					}
				}
			}
			enoughReduce.setErgoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
			this.enoughreduceService.update(enoughReduce);
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5405, "资源为空");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param er_id
	 * @param mulitId
	 * @param type
	 * @param uid
	 * @descript 满减活动设置活动页展示商品
	 */
	@RequestMapping("/banner_goods.json")
	public void banner_goods(HttpServletRequest request, HttpServletResponse response, String er_id, String mulitId,
			String type, String uid) {
		Result result = null;
		EnoughReduce enoughReduce = this.enoughreduceService.getObjById(Long.parseLong(er_id));
		if (enoughReduce != null) {
			User user = this.userService.getObjById(CommUtil.null2Long(uid));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			List<String> goods_id_list = new ArrayList<String>();
			String goods_json = enoughReduce.getErbanner_goods_ids();
			if (goods_json != null && !goods_json.equals("") && goods_json.length() > 2) {
				goods_id_list = (List<String>) Json.fromJson(goods_json);
			}
			boolean flag = true;
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				if (enoughReduce.getStore_id().equals(CommUtil.null2String(store.getId()))) {
					Goods goods = this.goodsService.getObjById(Long.parseLong(id));
					if (goods.getEnough_reduce() == 0
							|| goods.getOrder_enough_reduce_id().equals(CommUtil.null2String(enoughReduce.getId()))) {
						if (type.equals("add")) {
							if(goods.getEnough_reduce() == 1){
								if (goods.getGoods_type() == 1 && goods.getActivity_status() == 0
										&& goods.getOrder_enough_give_status() == 0) {
									goods_id_list.add(id);
								}	
							}else{
								flag = false;
							}
							// 同一个商品只能参加一个活动 满包邮为店铺活动可与其他活动同时进行
						} else {
							if (goods_id_list.contains(id)) {
								goods_id_list.remove(id);
							}
						}
						this.goodsService.update(goods);
					}
				}
			}
			enoughReduce.setErbanner_goods_ids(Json.toJson(goods_id_list, JsonFormat.compact()));
			this.enoughreduceService.update(enoughReduce);
			if(flag){
				result = new Result(5200, "Successfully");
			}else{
				result = new Result(5400, "非活动商品");
			}
		} else {
			result = new Result(5405, "资源为空");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description卖家满包邮活动审核
	 * @param request
	 * @param response
	 * @param id
	 * @param uid
	 */
	@RequestMapping("/apply.json")
	public void apply(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "id", required = true) String id, String uid) {
		Result result = null;
		if (!id.equals("")) {
			EnoughReduce enoughReduce = this.enoughreduceService.getObjById(Long.parseLong(id));
			User user = this.userService.getObjById(Long.parseLong(uid));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			if (enoughReduce != null) {
				Map params = new HashMap();
				params.put("store_id", CommUtil.null2String(store.getId()));
				List<EnoughReduce> enoughReduces = this.enoughreduceService
						.query("select obj from EnoughReduce obj where obj.store_id=:store_id "
								+ "and (obj.erstatus=10 or obj.erstatus=5)", params, -1, -1);
				if (enoughReduces.size() > this.configService.getSysConfig().getEnoughreduce_max_count()) {
					result = new Result(5407, "当前正在审核或进行的满就减超过了规定的最大值");
				} else {
					if (enoughReduce.getErstatus() == 0 || enoughReduce.getErstatus() == -10) {
						enoughReduce.setErstatus(5);
						enoughReduce.setFailed_reason("");
						this.enoughreduceService.update(enoughReduce);
						result = new Result(5200, "Successfully");
					}
				}
			} else {
				result = new Result(5405, "资源为空");
			}
		} else {
			result = new Result(5400, "参数错误");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 满减活动开启关闭
	 * @param request
	 * @param response
	 * @param er_id
	 *            活动id
	 */
	@RequestMapping("/switch.json")
	public void openToClose(HttpServletRequest request, HttpServletResponse response, String er_id) {
		int code = -1;
		String msg = "";
		EnoughReduce enoughReduce = this.enoughreduceService.getObjById(CommUtil.null2Long(er_id));
		if (enoughReduce != null) {
			if (enoughReduce.getErbegin_time().before(new Date()) && enoughReduce.getErend_time().after(new Date())) {
				if(enoughReduce.getErstatus() == 10 || enoughReduce.getErstatus() == 30){
					int erStatus = enoughReduce.getErstatus() == 10 ? 30 : 10;
					enoughReduce.setErstatus(erStatus);
					this.enoughreduceService.update(enoughReduce);
					code = 5200;
					msg = "Successfully";
				}else{
					code = 5214;
					msg = "活动未开启";	
				}
			}else{
				code = 5215;
				msg = "活动已结束";
			}
		} else {
			code = 5405;
			msg = "资源为空";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}
}
