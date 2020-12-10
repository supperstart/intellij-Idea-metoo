package com.metoo.manage.seller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.EnoughFree;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.EnoughFreeQueryObject;
import com.metoo.foundation.service.IEnoughFreeService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 *
 * @Title EnoughFreeSellerAction.java
 * 
 * Description: 卖家满包邮控制器
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/php/seller/")
public class EnoughFreeSellerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IEnoughFreeService enoughFreeService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IStoreService storeService;
	
	@RequestMapping("/enoughfree/switch.json")
	public void open(HttpServletRequest request,
			HttpServletResponse response, @RequestParam(value = "user_id", required = true)String user_id,
			String type, String price){
		Result result = null;
		Map map = new HashMap();
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if(user != null){
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			if(store != null){
				if(type != null && !type.equals("") && type.equals("off")){
					 store.setEnough_free(0);
				}else{
					EnoughFree free = null;
					if(type != null && !type.equals("") && type.equals("on")){
						store.setEnough_free(1);
						 free = this.enoughFreeService.getObjById(CommUtil.null2Long(store.getEnough_free_id()));
						 if(free == null){
							 free = new EnoughFree();
							 free.setAddTime(new Date()); 
							 free.setEf_type(1);
							 free.setCondition_amount(CommUtil.null2BigDecimal(price));
							 free.setStore_id(CommUtil.null2String(store.getId()));
							 free.setStore_name(store.getStore_name());
							 this.enoughFreeService.save(free);
							 store.setEnough_free_id(CommUtil.null2String(free.getId()));
							 store.setEnough_free_price(CommUtil.null2BigDecimal(price));
						}else{
							 free.setCondition_amount(CommUtil.null2BigDecimal(price));
							 free.setEf_frequency(free.getEf_frequency() + 1);
							 this.enoughFreeService.update(free);
							 store.setEnough_free_price(CommUtil.null2BigDecimal(price));
						}
					}else{
						 free = this.enoughFreeService.getObjById(CommUtil.null2Long(store.getEnough_free_id()));
						 free.setCondition_amount(CommUtil.null2BigDecimal(price));
						 free.setEf_frequency(free.getEf_frequency() + 1);
						 this.enoughFreeService.update(free);
						 store.setEnough_free_price(CommUtil.null2BigDecimal(price));
					}
				}
				 this.storeService.update(store);
				 result = new Result(5200, "Successfully");
			}else{
				result = new Result(5400, "用户没有该店铺");
			}
		}else{
			result = new Result(5400, "用户信息错误");
		}
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		try {
			
			JSONObject obj = new JSONObject().fromObject(result);
			response.getWriter().print(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * 满包邮列表
	 * @param request
	 * @param response
	 * @param currentPage 
	 * @param title
	 * @param begin_time
	 * @param end_time
	 * @param status
	 * @param uid
	 */
	@RequestMapping(value= "enoughfree.json", method = RequestMethod.GET)
	public void get(HttpServletRequest request,
			HttpServletResponse response, String currentPage, 
			String title, String begin_time, String end_time, String status, @RequestParam(value = "uid", required = true) String uid){
		Result result = null;
		Map map = new HashMap();
		if(uid != null && !"".equals(uid)){
			User user = this.userService.getObjById(CommUtil.null2Long(uid));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			ModelAndView mv = new ModelAndView();
			EnoughFreeQueryObject qo = new EnoughFreeQueryObject(currentPage,
					mv,"addTime", "desc");
			qo.addQuery("obj.store_id", new SysMap("store_id", store.getId().toString()), "=");
			if(title != null && !"".equals(title)){
				qo.addQuery("obj.eftitle", new SysMap("eftitle", "%" + title + "%"), "like");
			}
			if (begin_time != null && !begin_time.equals("")
					&& end_time != null && !end_time.equals("")) {
				qo.addQuery("DATE_FORMAT(obj.efbegin_time,'%Y-%m-%d')", new SysMap(
						"efbegin_time", begin_time), ">=");
				qo.addQuery("DATE_FORMAT(obj.efend_time,'%Y-%m-%d')", new SysMap(
						"efend_time", end_time), "<=");
			}
			if (status != null && !"".equals(status)) {
				qo.addQuery("obj.efstatus",
						new SysMap("efstatus", CommUtil.null2Int(status)), "=");
			}
			IPageList pList = this.enoughFreeService.list(qo);
			List<EnoughFree> enoughFrees = pList.getResult();
			JSONArray json = JSONArray.fromObject(pList.getResult());
			/*List<Map> efList = new ArrayList<Map>();
			for(EnoughFree ef : enoughFrees){
				Map efMap = new HashMap();
				efMap.put("title", ef.getEftitle());
				efMap.put("begin_time", ef.getEfbegin_time());
				efMap.put("end_time", ef.getEfend_time());
				efMap.put("content", ef.getEfcontent());
				efMap.put("tag", ef.getEftag());
				efList.add(efMap);
			}*/
			map.put("ecoughRess", json);
			//map.put("enoughFress", efList);
			map.put("currentpage", pList.getCurrentPage());
			map.put("pageSize", pList.getPageSize());
			map.put("begin_time", begin_time);
			map.put("end_time", end_time);
			result = new Result(5200, "Successfully", map);
		}else{
			result = new Result(-100, "User information error");
		}
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		try {
			
			JSONObject obj = new JSONObject().fromObject(result);
			response.getWriter().print(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @description EnoughFree新增满包邮
	 * @param request
	 * @param response
	 * @param id 满包邮活动ID
	 * @param currentPage 当前页数
	 * @param uid
	 * @param price
	 */
	@RequestMapping(value = "enoughfree.json", method = RequestMethod.POST)
	public void POST(HttpServletRequest request,
			HttpServletResponse response, @RequestPart(value = "free_id", required = true)String free_id, String currentPage,
			String user_id, String price){
		Result result = null;
		Map map = new HashMap();
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		Map params = new HashMap();
		params.put("sid", store.getId().toString());
		List<EnoughFree> efoughFrees = this.enoughFreeService.query("select obj from EnoughFree obj where obj.store_id=:sid and (obj.efstatus=10 or obj.efstatus=5)", params, -1, -1);
		for(EnoughFree ef : efoughFrees){
			if(ef.getEfend_time().before(new Date())){
				ef.setDeleteStatus(20);
			}
			this.enoughFreeService.update(ef);
		}
		efoughFrees = this.enoughFreeService.query("select obj from EnoughFree obj where obj.store_id=:sid and (obj.efstatus=10 or obj.efstatus=5)", params, -1, -1);
		if(efoughFrees.size() > this.configService.getSysConfig().getEnoughfree_max_count()){
			msg = "您当前正在审核或正在进行的满包邮活动超过最大限制";
		}else{
			WebForm wf = new WebForm();
			EnoughFree enoughFree = null;
			if(CommUtil.null2String(free_id).equals("")){
				enoughFree = wf.toPo(request, EnoughFree.class);
				enoughFree.setAddTime(new Date());
				enoughFree.setEf_type(1);
			}else{
				EnoughFree obj = this.enoughFreeService.getObjById(CommUtil.null2Long(free_id));
				enoughFree = (EnoughFree) wf.toPo(request, obj);
			}
			enoughFree.setEfstatus(0);
			enoughFree.setEf_type(1);
			enoughFree.setEftag("Orders "+ price +" AED free shipping.");
			enoughFree.setStore_id("" +store.getId());
			enoughFree.setStore_name(store.getStore_name());
			enoughFree.setCondition_amount(CommUtil.null2BigDecimal(price));
			if(free_id.equals("")){
				this.enoughFreeService.save(enoughFree);
				result = new Result(5200, "Save Successfully");
			}else{
				this.enoughFreeService.update(enoughFree);
				result = new Result(5200, "Edit Successfully");
			}
			response.setContentType("text/plain");
			response.setHeader("Cache-Control", "no-cache");
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
	}
	
	/**
	 * 满包邮删除
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param uid
	 * @param currentPage
	 */
	@RequestMapping(value = "enoughfree.json", method = RequestMethod.DELETE)
	public void delete(HttpServletRequest request, HttpServletResponse response, String mulitId, String user_id, String currentPage){
		Result result = null;
		for(String id : mulitId.split(",")){
			if(!id.equals("")){
				EnoughFree enoughFree = this.enoughFreeService.getObjById(CommUtil.null2Long(id));
				User user = this.userService.getObjById(CommUtil.null2Long(user_id));
				user = user.getParent() == null ? user : user.getParent();
				Store store = user.getStore();
				if(enoughFree.getStore_id().equals(store.getId().toString())){
					String ids = enoughFree.getEfgoods_ids_json();
					if(ids != null && !ids.equals("")){
						List<String> goods_ids = (List) Json
								.fromJson(ids);
						for(String goods_id : goods_ids){
							Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
							if(goods.getOrder_enough_free_id().equals(id)){
								goods.setEnough_free(0);
								goods.setOrder_enough_free_id("");
								this.goodsService.update(goods);
							}
						}
					}
					this.enoughFreeService.delete(CommUtil.null2Long(id));
				}
			}
		}
		result = new Result(5200, "Delete Successfully");
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
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
	 * @description 添加活动商品
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "enoughfree/goods.json", method = RequestMethod.PUT)
	public void enoughFreeGoods(HttpServletRequest request,
			HttpServletResponse response, String goods_id, @RequestParam(value = "free_id", required = true)String free_id, String user_id){
		Result result = null;
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		EnoughFree enoughFree = this.enoughFreeService.getObjById(CommUtil.null2Long(free_id));
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		if(goods.getGoods_store().getId() == store.getId()){
			int flag = goods.getEnough_free();
			String ids = enoughFree.getEfgoods_ids_json();
			List<String> goods_id_list = new ArrayList<String>();
			if(ids != null && !goods_id.equals("")){
				goods_id_list = (List)Json.fromJson(ids);
			}
			if(flag == 0){
				if (goods.getCombin_status() == 0 && goods.getGroup_buy() == 0
						&& goods.getGoods_type() == 1
						&& goods.getActivity_status() == 0
						&& goods.getF_sale_type() == 0
						&& goods.getAdvance_sale_type() == 0
						&& goods.getOrder_enough_give_status() == 0
						&& goods.getEnough_reduce() == 0) {
					goods.setEnough_free(1);
					goods.setOrder_enough_free_id(free_id);
					goods_id_list.add(goods_id);
					enoughFree.setEfgoods_ids_json(Json.toJson(goods_id_list,
							JsonFormat.compact()));
				}
			}else{
				goods.setEnough_free(1);
				goods.setOrder_enough_free_id("");
				if(goods_id_list.contains(goods_id)){
					goods_id_list.remove(goods_id);
				}
				enoughFree.setEfgoods_ids_json(Json.toJson(goods_id_list,
						JsonFormat.compact()));
			}
			this.goodsService.update(goods);
			this.enoughFreeService.update(enoughFree);
			result = new Result(5200, "Add Successfully");
		}else{
			result = new Result(5400, "商品信息错误");
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
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
	 * @description 批量添加活动商品
	 * @param request
	 * @param response
	 * @param uid
	 * @param mulitid
	 * @param eid
	 * @param type
	 */
	@RequestMapping(value = "enoughfree/batch.json", method = RequestMethod.POST)
	public void mulitGoods(HttpServletRequest request,
			HttpServletResponse response, String user_id, String mulitid, String free_id, String type){
		Result result = null;
		EnoughFree enoughFree = this.enoughFreeService.getObjById(CommUtil.null2Long(free_id));
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		String json = enoughFree.getEfgoods_ids_json();
		List<String> goods_id_list = new ArrayList<String>();
		if(json != null && !json.equals("")){
			goods_id_list = (List)Json.fromJson(json);
		}
		String[] ids = mulitid.split(",");
		for(String id : ids){
			if(!id.equals("")){
				Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
				if(goods.getGoods_store().getId() == store.getId()){
					if(goods.getEnough_free() == 0 || goods.getOrder_enough_free_id().equals(free_id)){
						if(type.equals("add")){
							if(goods.getCombin_status() == 0
									&& goods.getGroup_buy() == 0
									&& goods.getGoods_type() == 1
									&& goods.getActivity_status() == 0
									&& goods.getF_sale_type() == 0
									&& goods.getAdvance_sale_type() == 0
									&& goods.getOrder_enough_give_status() == 0
									&& goods.getEnough_reduce() == 0) {
								goods_id_list.add(id);
								goods.setEnough_free(1);
								goods.setOrder_enough_free_id(free_id);
							}	
						}else{
							if(goods_id_list.contains(id)){
								goods_id_list.remove(id);
							}
							goods.setEnough_free(0);
							goods.setOrder_enough_free_id("");
						}
					}
					this.goodsService.update(goods);
				}
			}
		}
		enoughFree.setEfgoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
		this.enoughFreeService.update(enoughFree);
		result = new Result(5200, "Batch Add Successfully");
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
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
	 * 提交审核
	 * @param rquest
	 * @param response
	 * @param id
	 * @param uid
	 */
	@RequestMapping(value = "emoughfree/apply.json", method = RequestMethod.POST)
	public void apply(HttpServletRequest rquest,
			HttpServletResponse response, String id, String user_id){
		Result result = null;
		if(id != null && !id.equals("")){
			EnoughFree enoughFree = this.enoughFreeService.getObjById(CommUtil.null2Long(id));
			User user = this.userService.getObjById(CommUtil.null2Long(user_id));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			if(enoughFree != null && enoughFree.getStore_id().equals("" +store.getId())){
				Map params = new HashMap();
				params.put("sid", store.getId().toString());
				List<EnoughFree> enoughFrees = this.enoughFreeService.query("select obj from EnoughFree obj where obj.store_id=:sid and(obj.efstatus=10 or obj.efstatus=5)", params, -1, -1);
				if(enoughFrees.size() < this.configService
						.getSysConfig().getEnoughfree_max_count()){
					if(enoughFree.getEfstatus() == 0 || enoughFree.getEfstatus() == -10){
						enoughFree.setEfstatus(5);
						enoughFree.setFailed_reason("");
						this.enoughFreeService.update(enoughFree);
						result = new Result(5200, "Submit successfully");
					}
				}else{
					result = new Result(-1, "submit failure,You do not have this activity");
				}
			}else{
				result = new Result(-1, "submit failure,You do not have this activity");
			}
		}else{
			result = new Result(-1, "submit failure,You do not have this activity");
		}
		result = new Result(5200, "Apply Successfully");
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
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
}
