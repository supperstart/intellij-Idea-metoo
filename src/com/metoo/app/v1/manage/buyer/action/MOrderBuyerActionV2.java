package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;

import net.sf.json.JSONArray;

/**
 * <p>
 * 	Description: 用户订单管理类，多店铺合单
 * </p> 
 * @author 46075
 *
 */
@Controller
@RequestMapping("/app/v2/")
public class MOrderBuyerActionV2 {
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IStoreService storeService;
	
	
	@RequestMapping(value = "order.json", method = RequestMethod.POST)
	public void orderV2(HttpServletRequest request, HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String order_status, String token, String name, String language) {
		String orderTemp = this.orderFormService.orderMain(request, response, currentPage, order_id, beginTime, endTime,
				order_status, token, name, language);
		response.setContentType("application/json; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().println(orderTemp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "order_view.json")
	@ResponseBody
	public String getOrderView(HttpServletRequest request, HttpServletResponse response, String id, String token,
			String language) {
		int code = -1;
		String msg = "";
		Map map = new HashMap();
		Map main = new HashMap();
		List<Map> order = new ArrayList<Map>();
		if (!CommUtil.null2String(token).equals("")) {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user != null) {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				//List<Map> goodsList = this.orderFormTools.queryGoodsInfo(obj.getGoods_info());
				if (obj != null && obj.getUser_id().equals(user.getId().toString())) {
					String imgWebServer = this.configService.getSysConfig().getImageWebServer();
					if (obj.getChild_order_detail() != null && !obj.getChild_order_detail().equals("")) {
						order = this.orderFormTools.queryGoodsInfo(obj.getChild_order_detail());
						//main.put("childs", JSONArray.fromObject(childs));
						/*JSONArray array = this.orderFormTools.queryChildOrder(obj.getChild_order_detail());
						order.add(array);*/
					}
					main.put("order_goods_info", obj.getGoods_info());
					main.put("enough_free", obj.getEnough_free());
					main.put("order_id", obj.getId());
					main.put("order_status", obj.getOrder_status());
					main.put("enough_reduce_amount", obj.getEnough_reduce_amount());
					Map coupon_map = orderFormTools.queryCouponInfo(obj.getCoupon_info());
					main.put("coupon_amount", coupon_map.get("coupon_amount"));
					main.put("goods_amount", obj.getGoods_amount());
					main.put("ship_price", obj.getEnough_free() == 1 ? 0 : obj.getShip_price());
					main.put("totalPrice",  obj.getEnough_free() == 1 ? obj.getGoods_amount() : obj.getTotalPrice());
					order.add(main);
					map.put("order_number", obj.getOrder_id());
					map.put("order_status", obj.getOrder_status());
					map.put("platform_ship_price", obj.getPlatform_ship_price());
					map.put("discounts_amount", obj.getDiscounts_amount());
					map.put("integral", obj.getIntegral());
					/*map.put("integral_price",
							CommUtil.mul(obj.getIntegral(), this.configService.getSysConfig().getIntegralExchangeRate()));*/
					map.put("order_coupon_amount", obj.getCoupon_amount());
					map.put("order_goods_amount",
							CommUtil.subtract(obj.getPayment_amount(), obj.getPlatform_ship_price()));
					map.put("payment_amount", obj.getPayment_amount());
					map.put("imgWebServer", imgWebServer);
					//店铺
					Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
					Map storeMap = new HashMap();
					main.put("store_name", store.getStore_name());
					main.put("store_id", store.getId());
					main.put("store_enough_free", store.getEnough_free());
					main.put("store_enough_free_price", store.getEnough_free_price());
					main.put("store_logo",
							store.getStore_logo() == null ? ""
									: this.configService.getSysConfig().getImageWebServer() + "/"
											+ store.getStore_logo().getPath() + "/" + store.getStore_logo().getName());
					//main.put("store", storeMap);
					
					// 收货地址
					map.put("Transport", obj.getTransport());
					map.put("Receiver_Name", obj.getReceiver_Name());
					map.put("Receiver_area", obj.getReceiver_area());
					map.put("Receiver_area_info", obj.getReceiver_area_info());
					map.put("Receiver_zip", obj.getReceiver_zip());
					map.put("Receiver_telephone", obj.getReceiver_telephone());
					map.put("Receiver_mobile", obj.getReceiver_mobile());
					
					map.put("shipCode", obj.getShipCode());
					map.put("AddTime", obj.getAddTime());
					map.put("ShipTime", obj.getShipTime());
					map.put("ConfirmTime", obj.getConfirmTime());
					map.put("FinishTime", obj.getFinishTime());
					map.put("order", order);
					code = 4200;
					msg = "Successfully";
				} else {
					code = 4205;
					msg = "No information was found";
				}
			} else {
				code = -100;
				msg = "token Invalidation";
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		return Json.toJson(new Result(code, msg, map), JsonFormat.compact());
	}

}
