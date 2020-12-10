package com.metoo.php.manage.seller.action;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.ExpressCompany;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IShipAddressService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.msg.MsgTools;

@Controller
public class POrderSellerAction {

	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IExpressCompanyService expressCompanyService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IShipAddressService shipAddressService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private OrderFormTools orderFormTools;

	/**
	 * 确认发货 物流单号由手动输入改为从ddu获取
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @param state_info
	 * @param ecc_id
	 * @param sa_id
	 * @param uid
	 * @throws Exception
	 */
	@RequestMapping("/php/order_shipping.json")
	public void orderShipping(HttpServletRequest request, HttpServletResponse response, String id, String state_info,
			String ecc_id, String sa_id, String uid, String shipCode) throws Exception {
		Result result = null;
		OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
		ExpressCompany ecc = this.expressCompanyService.getObjById(CommUtil.null2Long(ecc_id));
		Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		user = user.getParent() == null ? user : user.getParent();
		if (user.getStore().getId().equals(store.getId())) {
			obj.setOrder_status(30);
			obj.setUpdate_status(1);
			obj.setShipCode(shipCode);// 物流单
			obj.setShipTime(new Date());
			if (ecc != null) {
				Map json_map = new HashMap();
				json_map.put("express_company_id", ecc.getId());
				json_map.put("express_company_name", ecc.getCompany_name());
				json_map.put("express_company_mark", ecc.getCompany_mark());
				// json_map.put("express_company_type", ecc.getEcc_ec_type());
				obj.setExpress_info(Json.toJson(json_map));
			}
			String[] order_seller_intros = request.getParameterValues("order_seller_intro");
			String[] goods_ids = request.getParameterValues("goods_id");
			String[] goods_names = request.getParameterValues("goods_name");
			String[] goods_counts = request.getParameterValues("goods_count");
			/*
			 * ShipAddress shipAddress =
			 * this.shipAddressService.getObjById(CommUtil .null2Long(sa_id));
			 * if (shipAddress != null) {
			 * obj.setShip_addr_id(shipAddress.getId()); Area area =
			 * this.areaService.getObjById(shipAddress.getSa_area_id());
			 * obj.setShip_addr(area.getParent().getParent().getAreaName() +
			 * area.getParent().getAreaName() + area.getAreaName() +
			 * shipAddress.getSa_addr()); }
			 */
			this.orderFormService.update(obj);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info("确认发货");
			ofl.setState_info(state_info);
			ofl.setLog_user(user);
			ofl.setOf(obj);
			this.orderFormLogService.save(ofl);
			User buyer = this.userService.getObjById(CommUtil.null2Long(obj.getUser_id()));
			// DDU
			/*
			 * Map express_map = Json.fromJson(Map.class,
			 * obj.getExpress_info()); Long area_id =
			 * shipAddress.getSa_area_id(); List<Map> goods_map =
			 * this.orderFormTools.queryGoodsInfo( obj.getGoods_info()); Area
			 * area = areaService.getObjById(CommUtil.null2Long(area_id));
			 * DduTaskRequest ddutaskRequest = new DduTaskRequest();
			 * ddutaskRequest.setBatchNumber(obj.getOrder_id());
			 * ddutaskRequest.setFromCompany(shipAddress.getSa_company());
			 * ddutaskRequest.setFromAddress(area.getAreaName());
			 * ddutaskRequest.setFromLocation(shipAddress.getSa_addr());
			 * ddutaskRequest.setFromCountry(area.getParent().getParent().
			 * getAreaName());
			 * ddutaskRequest.setFromCperson(shipAddress.getSa_user_name());
			 * ddutaskRequest.setFromContactno(shipAddress.getSa_telephone());
			 * //[面单不显示] //ddutaskRequest.setFromMobileno(sa.getSa_telephone());
			 * //ddutaskRequest.setToCompany();
			 * ddutaskRequest.setToAddress(obj.getReceiver_area());
			 * ddutaskRequest.setToLocation(obj.getReceiver_area_info());
			 * //ddutaskRequest.setToCountry(sa.getSa_addr());
			 * ddutaskRequest.setToCountry(obj.getReceiver_area());
			 * ddutaskRequest.setToCperson(obj.getReceiver_Name());
			 * ddutaskRequest.setToContactno(obj.getReceiver_telephone());
			 * ddutaskRequest.setToMobileno(obj.getReceiver_telephone());
			 * ddutaskRequest.setReferenceNumber(obj.getReceiver_mobile());
			 * ddutaskRequest.setCompanyCode(CommUtil.null2String(express_map.
			 * get("express_company_mark"))); int pieces; int weight; int
			 * weightnum=0; int piecesnum=0; for(Map goods_maps:goods_map){
			 * pieces = CommUtil.null2Int(goods_maps.get("goods_count"));
			 * piecesnum += pieces; weight =
			 * CommUtil.null2Int(goods_maps.get("weight")); }
			 * ddutaskRequest.setWeight(weightnum);
			 * ddutaskRequest.setPieces(piecesnum);
			 * //ddutaskRequest.setPackageType("Document");
			 * ddutaskRequest.setCurrencyCode("AED");
			 * ddutaskRequest.setNcndAmount(obj.getTotalPrice());
			 * ddutaskRequest.setItemDescription("DESCRIPTION");
			 * ddutaskRequest.setSpecialInstruction("SPECIAL"); StringBuffer
			 * stringBuffer = SOAPUtils.webServiceTow(ddutaskRequest); String
			 * xmlResult = stringBuffer.toString().replace("<", "<"); String
			 * AWBNumber = SOAPUtils.getXmlMessageByName(xmlResult,
			 * "AWBNumber"); //报文返回状态码，0表示正常，3表示错误 String responseCode =
			 * SOAPUtils.getXmlMessageByName(xmlResult, "responseCode");
			 * if("1".equals(responseCode)){ obj.setShipCode(AWBNumber);
			 * this.orderFormService.update(obj); }
			 */
			Map map = new HashMap();
			map.put("buyer_id", buyer.getId().toString());
			map.put("seller_id", store.getUser().getId().toString());
			map.put("order_id", obj.getId());
			String json = Json.toJson(map);
			if (obj.getOrder_form() == 0) {
				this.msgTools.sendEmailCharge(CommUtil.getURL(request), "email_tobuyer_order_ship_notify",
						buyer.getEmail(), json, null, obj.getStore_id());
				this.msgTools.sendSmsCharge(CommUtil.getURL(request), "sms_tobuyer_order_ship_notify",
						buyer.getMobile(), json, null, obj.getStore_id());
			} else {
				this.msgTools.sendEmailFree(CommUtil.getURL(request), "email_tobuyer_order_ship_notify",
						buyer.getEmail(), json, null);
				this.msgTools.sendSmsFree(CommUtil.getURL(request), "sms_tobuyer_order_ship_notify", buyer.getMobile(),
						json, null);
			}
			result = new Result(0, "Successfully");
		} else {
			result = new Result(-100, "User information error");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 卖家取消订单
	 * @param request
	 * @param response
	 * @param id
	 * @param uid
	 * @param state_info
	 */
	@RequestMapping("/php/cancel.json")
	public void cancel(HttpServletRequest request, HttpServletResponse response, String id, String uid,
			String state_info) {
		Result result = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		user = user.getParent() == null ? user : user.getParent();
		if (user != null) {
			OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
			if (obj != null) {
				Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
				if (user.getStore().getId().equals(store.getId())) {
					obj.setOrder_status(0);
					this.orderFormService.update(obj);
					OrderFormLog objLog = new OrderFormLog();
					objLog.setAddTime(new Date());
					objLog.setLog_info("取消订单");
					objLog.setLog_user(user);
					objLog.setOf(obj);
					objLog.setState_info(state_info);
					this.orderFormLogService.save(objLog);
					this.orderFormTools.updateGoodsInventory(obj);
					result = new Result(0, "Order cancelled successfully");
				} else {
					result = new Result(1, "You do not have this order");
				}
			} else {
				result = new Result(1, "You do not have this order");
			}
		} else {
			result = new Result(-100, "User information error");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 卖家修改用户收获地址
	 * @param request
	 * @param response
	 * @param order_id
	 * @param area
	 * @param city
	 * @param area_info
	 * @param uid
	 */
	@RequestMapping("editAddress.json")
	public void editArea(HttpServletRequest request, HttpServletResponse response, String order_id, String area,
			String city, String area_info, String longitude_latitude, String uid) {
		int code = 0;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		Store store = user.getStore();
		OrderForm orderForm = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
		if (null != user && store.getStore_status() == 15) {
			if (CommUtil.null2Long(orderForm.getStore_id()).equals(store.getId())) {
				orderForm.setReceiver_area_info(area_info);
				//orderForm.setReceiver_area(area);
				orderForm.setReceiver_city(city);
				orderForm.setReceiver_longitude_latitude(longitude_latitude);
				this.orderFormService.update(orderForm);
				code = 5200;
				msg = "Successfully";
			} else {
				code = 5400;
				msg = "The order does not exist";
			}
		} else {
			code = 5422;
			msg = "The shop is closed.";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}
}
