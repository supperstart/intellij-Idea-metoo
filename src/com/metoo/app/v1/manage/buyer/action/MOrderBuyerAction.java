package com.metoo.app.v1.manage.buyer.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MIOrderFormService;
import com.metoo.app.view.web.tool.MCartViewTools;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.ExpressCompany;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.domain.ReturnGoodsLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.StorePoint;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.OrderFormQueryObject;
import com.metoo.foundation.domain.query.ReturnGoodsLogQueryObject;
import com.metoo.foundation.domain.virtual.TransContent;
import com.metoo.foundation.domain.virtual.TransInfo;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IReturnGoodsLogService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStorePointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.kuaidi100.domain.ExpressInfo;
import com.metoo.kuaidi100.pojo.TaskRequest;
import com.metoo.kuaidi100.pojo.TaskResponse;
import com.metoo.kuaidi100.post.HttpRequest;
import com.metoo.kuaidi100.post.JacksonHelper;
import com.metoo.kuaidi100.service.IExpressInfoService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.seller.tools.StoreLogTools;
import com.metoo.msg.MsgTools;
import com.metoo.view.web.tools.GoodsViewTools;

@Controller
@RequestMapping("/app/v1/")
public class MOrderBuyerAction {
	@Autowired
	private MIOrderFormService orderFormMetooService;
	@Resource(name = "goodsDAO")
	private IGenericDAO<Goods> goodsDAO;
	@Resource(name = "evaluateDAO")
	private IGenericDAO<Evaluate> evaluateDAO;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IStorePointService storePointService;
	@Autowired
	private IExpressCompanyService expressCompayService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IPayoffLogService payoffLogservice;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IReturnGoodsLogService returnGoodsLogService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IExpressInfoService expressInfoService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private ICGoodsService cGoodsService;
	@Autowired
	private StoreLogTools storeLogTools;
	@Autowired
	private IStoreLogService storeLogService;

	@SecurityMapping(title = "买家订单列表", value = "/buyer_order.json*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order.json", method = RequestMethod.POST)
	public void order(HttpServletRequest request, HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String order_status, String token, String name, String language) {
		String orderTemp = orderFormMetooService.order(request, response, currentPage, order_id, beginTime, endTime,
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

	/**
	 * @methodsName orderView
	 * @description 买家订单详情
	 * @author hukai
	 * @Date 2019-1010
	 * @param request
	 * @param response
	 * @param id
	 *            订单编号
	 * @param token
	 */
	@SecurityMapping(title = "买家订单详情", value = "/buyer/order_view.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_view.json", method = RequestMethod.GET)
	public void orderView(HttpServletRequest request, HttpServletResponse response, String id, String token,
			String language) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		ModelAndView mv = new JModelAndView("user/default/usercenter/order_view.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (CommUtil.null2String(token).equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map<String, Object> params = new HashMap<String, Object>();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (obj != null && obj.getUser_id().equals(CommUtil.null2String(user.getId()))) {
					if (obj.getOrder_cat() == 1) {// 手机充值订单
						mv = new JModelAndView("user/default/usercenter/recharge_order_view.html",
								configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request,
								response);
					}
					boolean query_ship = false;// 是否查询物流
					if (!CommUtil.null2String(obj.getShipCode()).equals("")) {
						query_ship = true;
					}
					/*
					 * if (obj.getOrder_main() == 1 &&
					 * !CommUtil.null2String(obj.getChild_order_detail()).equals
					 * ("")) {// 子订单中有商家已经发货，也需要显示 List<Map> maps =
					 * this.orderFormTools.queryGoodsInfo(obj.
					 * getChild_order_detail()); for (Map child_map : maps) {
					 * OrderForm child_order = this.orderFormService
					 * .getObjById(CommUtil.null2Long(child_map.get("order_id"))
					 * ); if (child_order != null &&
					 * !CommUtil.null2String(child_order.getShipCode()).equals(
					 * "")) { query_ship = true; } } }
					 */
					map.put("order", obj.getId());
					map.put("Order_id", obj.getOrder_id());// 订单号
					map.put("Order_status", obj.getOrder_status());
					map.put("shipCode", obj.getShipCode());
					map.put("AddTime", obj.getAddTime());
					map.put("ShipTime", obj.getShipTime());
					map.put("ConfirmTime", obj.getConfirmTime());
					map.put("FinishTime", obj.getFinishTime());
					try {
						map.put("Payment_mark", obj.getPayment().getMark());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("Payment_mark", "");
					}
					map.put("PayType", obj.getPayType());
					map.put("Ship_price", obj.getShip_price());
					map.put("Goods_amount", obj.getGoods_amount());
					map.put("Enough_reduce_amount", obj.getEnough_reduce_amount());
					map.put("Transport", obj.getTransport());
					map.put("Receiver_Name", obj.getReceiver_Name());
					map.put("Receiver_area", obj.getReceiver_area());
					map.put("Receiver_area_info", obj.getReceiver_area_info());
					map.put("Receiver_zip", obj.getReceiver_zip());
					map.put("Receiver_telephone", obj.getReceiver_telephone());
					map.put("Receiver_mobile", obj.getReceiver_mobile());
					if (obj.getPayment() != null) {
						map.put("Payment_name", obj.getPayment().getName());
					} else {
						map.put("Payment_name", "");
					}
					map.put("PayTime", obj.getPayTime());
					map.put("Pay_msg", obj.getPay_msg());
					map.put("Delivery_time", obj.getDelivery_time());
					map.put("Invoice", obj.getInvoice());
					map.put("InvoiceType", obj.getInvoiceType());
					map.put("Msg", obj.getMsg());
					map.put("TotalPrice", obj.getTotalPrice());
					map.put("coupon_amount", obj.getCoupon_amount());
					map.put("integral", obj.getIntegral());
					map.put("integral_price",
							CommUtil.mul(obj.getIntegral(), this.configService.getSysConfig().getIntegralRate()));
					map.put("freight_amount", obj.getFreight_amount());
					map.put("discounts_amount", obj.getDiscounts_amount());
					map.put("payment_amount", obj.getPayment_amount());

					Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
					Map storeMap = new HashMap();
					storeMap.put("store_name", store.getStore_name());
					storeMap.put("store_enough_free", store.getEnough_free());
					storeMap.put("store_enough_free_price", store.getEnough_free_price());
					storeMap.put("store_logo",
							store.getStore_logo() == null ? ""
									: this.configService.getSysConfig().getImageWebServer() + "/"
											+ store.getStore_logo().getPath() + "/" + store.getStore_logo().getName());
					map.put("store", storeMap);
					// 店铺优惠券信息
					Map storeCoupon = orderFormTools.queryCouponInfo(obj.getCoupon_info());
					if (storeCoupon != null && !storeCoupon.equals("")) {
						map.put("store_coupon_price", storeCoupon.get("coupon_amount"));
					}
					// 平台优惠券信息
					Map generalCoupon = orderFormTools.queryCouponInfo(obj.getGeneral_coupon());
					if (generalCoupon != null && !generalCoupon.equals("")) {
						map.put("general_counpon_info", generalCoupon.get("coupon_amount"));
					}
					// 订单日志
					List<OrderFormLog> orderflg = obj.getOfls();
					for (OrderFormLog orderFormLog : orderflg) {
						Map<String, Object> orls = new HashMap<String, Object>();
						orls.put("userName",
								orderFormLog.getLog_user() == null ? "" : orderFormLog.getLog_user().getUsername());
						orls.put("state_info", orderFormLog.getState_info());
						map.put("orls", orls);
					}
					// 商品
					List<Map> goodsList = this.orderFormTools.queryGoodsInfo(obj.getGoods_info());
					List<Map<String, Object>> goodsInfo = new ArrayList<Map<String, Object>>();
					for (Map goods : goodsList) {
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						goodsMap.put("goods_id", goods.get("goods_id"));
						goodsMap.put("goods_name", goods.get("goods_name"));
						if ("1".equals(language)) {
							goodsMap.put("goods_name",
									goods.get("ksa_goods_name") != null
											&& !"".equals(goods.get("ksa_goods_name").toString())
													? "^" + goods.get("ksa_goods_name") : goods.get("goods_name"));
						}
						goodsMap.put("goods_count", goods.get("goods_count"));
						goodsMap.put("goods_price", goods.get("goods_price"));
						goodsMap.put("goods_current_price", goods.get("goods_current_price"));
						goodsMap.put("goods_type", goods.get("goods_type"));
						goodsMap.put("goods_gsp_val", goods.get("goods_gsp_val"));
						goodsMap.put("goods_color", goods.get("goods_color"));
						goodsMap.put("goods_all_price",
								CommUtil.mul(goods.get("goods_count"), goods.get("goods_price")));

						goodsMap.put("goods_mainphoto_path", this.configService.getSysConfig().getImageWebServer() + "/"
								+ goods.get("goods_mainphoto_path"));
						goodsInfo.add(goodsMap);
					}
					map.put("goodsinfo", goodsInfo);
					/*
					 * // 子订单商品信息 List<Map> childMap =
					 * this.orderFormTools.queryGoodsInfo(obj.
					 * getChild_order_detail()); if (childMap != null &&
					 * !childMap.equals("")) { List<Map> childOrderList = new
					 * ArrayList<Map>(); for (Map childOrder : childMap) { Map
					 * childOrderMap = new HashMap();
					 * childOrderMap.put("order_id",
					 * childOrder.get("order_id")); String goods_json =
					 * ((String)
					 * childOrder.get("order_goods_info")).replaceAll("////",
					 * ""); List<Map> chilld_goodsinfos =
					 * this.orderFormTools.queryGoodsInfo(goods_json); List<Map>
					 * childgoodsinfolist = new ArrayList<Map>(); for (Map
					 * child_goodsinfo : chilld_goodsinfos) { Map child_goodsmap
					 * = new HashMap(); child_goodsmap.put("goods_id",
					 * child_goodsinfo.get("goods_id"));
					 * child_goodsmap.put("goods_name",
					 * child_goodsinfo.get("goods_name"));
					 * child_goodsmap.put("goods_count",
					 * child_goodsinfo.get("goods_count"));
					 * child_goodsmap.put("goods_price",
					 * child_goodsinfo.get("goods_price")); String store_name =
					 * null; if(this.goodsService.getObjById(CommUtil.null2Long(
					 * child_goodsinfo.get("goods_id").toString())).
					 * getGoods_store() == null){ store_name = "self"; }else{
					 * store_name =
					 * this.goodsService.getObjById(CommUtil.null2Long(
					 * child_goodsinfo.get("goods_id").toString())).
					 * getGoods_store().getStore_name(); }
					 * child_goodsmap.put("store_name", store_name);
					 * child_goodsmap.put("goods_mainphoto_path",
					 * this.configService.getSysConfig().getImageWebServer() +
					 * "/" + child_goodsinfo.get("goods_mainphoto_path"));
					 * childgoodsinfolist.add(child_goodsmap); }
					 * childOrderMap.put("child_goodsinfo", childgoodsinfolist);
					 * childOrderList.add(childOrderMap); }
					 * orderMap.put("childOrderList", childOrderList); }
					 */
					params.clear();
					params.put("of_id", obj.getId());
					/*
					 * List<OrderFormLog> ofls = this.orderFormLogService
					 * .query(
					 * "select obj from OrderFormLog obj where obj.of.id=:of_id"
					 * , params, -1, -1);
					 */
					map.put("express_company_name",
							this.orderFormTools.queryExInfo(obj.getExpress_info(), "express_company_name"));
					result = new Result(0, "Successfully", map);
				} else {
					result = new Result(2, "The order does not exist");
				}
			}
		}
		response.setContentType("application/json; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @methodsName WebServiceTow
	 * @description 根据id查询子订单
	 * @param request
	 * @param response
	 * @param id
	 */
	@RequestMapping(value = "order_child.json", method = RequestMethod.GET)
	public void OrderDetail(HttpServletRequest request, HttpServletResponse response, String id) {
		Map<String, Object> main_map = new HashMap<String, Object>();
		List orderlist = new ArrayList();
		Result result = null;
		OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(id));
		main_map.put("Order_id", order.getOrder_id());
		main_map.put("Order_status", order.getOrder_status());
		main_map.put("order_cat", order.getOrder_cat());
		main_map.put("delivery_type", order.getDelivery_type());
		main_map.put("receiver_Name", order.getReceiver_Name());
		main_map.put("payType", order.getPayType());
		main_map.put("store_id", order.getStore_id());
		main_map.put("addtime", order.getAddTime());
		main_map.put("whether_gift", order.getWhether_gift());
		main_map.put("totalPrice", order.getTotalPrice());
		main_map.put("shipCode", order.getShipCode());
		List<Map> goods_list = this.orderFormTools.queryGoodsInfo(order.getGoods_info());
		List<Map> goodsDetail_list = new ArrayList<Map>();
		for (Map map : goods_list) {
			Map<String, Object> goods_map = new HashMap<String, Object>();
			goods_map.put("goods_id", map.get("goods_id"));
			goods_map.put("goods_name", map.get("goods_name"));
			goods_map.put("goods_count", map.get("goods_count"));
			goods_map.put("goods_price", map.get("goods_price"));
			String store_name = null;
			if (this.goodsService.getObjById(CommUtil.null2Long(map.get("goods_id").toString()))
					.getGoods_store() == null) {
				store_name = "self";
			} else {
				store_name = this.goodsService.getObjById(CommUtil.null2Long(map.get("goods_id").toString()))
						.getGoods_store().getStore_name();
			}
			goods_map.put("store_name", store_name);
			goods_map.put("goods_mainphoto_path",
					this.configService.getSysConfig().getImageWebServer() + "/" + map.get("goods_mainphoto_path"));
			goodsDetail_list.add(goods_map);
		}
		main_map.put("goodsinfo", goodsDetail_list);
		if (CommUtil.isNotNull(main_map)) {
			result = new Result(0, "查询订单成功", main_map);
		} else {
			result = new Result(1, "查询失败");
		}

		String order_temp = Json.toJson(result, JsonFormat.compact());
		try {
			response.getWriter().print(order_temp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "订单取消", value = "/buyer/order_cancel.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_cancel.json", method = RequestMethod.POST)
	public void order_cancel(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		Result result = null;
		if (CommUtil.null2String(token).equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (obj != null && obj.getUser_id().equals(CommUtil.null2String(user.getId()))) {
					map.put("id", obj.getId());
					map.put("order_id", obj.getOrder_id());
					result = new Result(0, "Successfully", map);
				} else {
					result = new Result(1, "The order does not exist or the user information is incorrect");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "订单取消确定", value = "/buyer/order_cancel_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_cancel_save.json", method = RequestMethod.POST)
	public void cancel_save(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String state_info, String other_state_info, String token) {
		Result result = null;
		List<OrderForm> objs = new ArrayList<OrderForm>();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				objs.add(obj);
				boolean all_verify = true;
				if (obj != null && obj.getUser_id().equals(user.getId().toString())) {
					/*
					 * if (obj.getOrder_main() == 1 &&
					 * obj.getChild_order_detail() != null) { List<Map> maps =
					 * (List<Map>) Json.fromJson(CommUtil
					 * .null2String(obj.getChild_order_detail())); if (maps !=
					 * null) { for (Map map : maps) { //
					 * System.out.println(map.get("order_id")); OrderForm
					 * child_order = this.orderFormService
					 * .getObjById(CommUtil.null2Long(map .get("order_id")));
					 * objs.add(child_order); } } }
					 */
					for (OrderForm of : objs) {
						if (of.getOrder_status() >= 20) {
							all_verify = false;
						}
					}
				}
				if (all_verify) {
					if (obj != null && obj.getUser_id().equals(user.getId().toString())) {
						/*
						 * if (obj.getOrder_main() == 1) { List<Map> maps =
						 * (List<Map>) Json.fromJson(CommUtil.null2String(obj.
						 * getChild_order_detail())); if (maps != null) { for
						 * (Map map : maps) { OrderForm child_order =
						 * this.orderFormService
						 * .getObjById(CommUtil.null2Long(map.get("order_id")));
						 * child_order.setOrder_status(0);
						 * this.orderFormService.update(child_order); } } }
						 */
						if (obj.getOrder_status() < 30) { // 未发货之前更新商品库存
							this.orderFormTools.updateGoodsInventory(obj);
						}
						obj.setOrder_status(0);
						this.orderFormService.update(obj);
						OrderFormLog ofl = new OrderFormLog();
						ofl.setAddTime(new Date());
						ofl.setLog_info("取消订单");
						ofl.setLog_user(user);
						ofl.setOf(obj);
						if (state_info.equals("other")) {
							ofl.setState_info(other_state_info);
						} else {
							ofl.setState_info(state_info);
						}
						this.orderFormLogService.save(ofl);
						Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
						Map<String, Object> map = new HashMap<String, Object>();
						if (store != null) {
							map.put("seller_id", store.getUser().getId().toString());
						}
						map.put("order_id", obj.getId().toString());
						String json = Json.toJson(map);
						/*
						 * if (obj.getOrder_form() == 0) {
						 * this.msgTools.sendEmailCharge(CommUtil.getURL(request
						 * ), "email_toseller_order_cancel_notify",
						 * store.getUser().getEmail(), json, null,
						 * CommUtil.null2String(store.getId()));
						 * this.msgTools.sendSmsCharge(CommUtil.getURL(request),
						 * "sms_toseller_order_cancel_notify",
						 * store.getUser().getMobile(), json, null,
						 * CommUtil.null2String(store.getId())); }
						 */

						// 取消订单删除下单赠送的冻结积分金额

					}
					result = new Result(0, "Successfully");
				} else {
					result = new Result(1, "The order is unpaid");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "收货确认", value = "/buyer/order_cofirm.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "buyer_order_cofirm.json", method = RequestMethod.GET)
	public void order_cofirm(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String token) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (obj != null && obj.getUser_id().equals((CommUtil.null2String(user.getId())))) {
					map.put("order_id", obj.getOrder_id());
					map.put("id", obj.getId());
					result = new Result(0, "Successfully", map);
				} else {
					result = new Result(1, "The order does not exist");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "收货确认保存", value = "/buyer/order_cofirm_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_cofirm_save.json", method = RequestMethod.POST)
	public void order_cofirm_save(HttpServletRequest request, HttpServletResponse response, String id,
			String currentPage, String token) throws Exception {
		Result result = null;
		if (CommUtil.null2String(token).equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (obj != null && obj.getUser_id().equals(CommUtil.null2String(user.getId()))) {
					obj.setOrder_status(40);
					Calendar ca = Calendar.getInstance();
					ca.add(ca.DATE, this.configService.getSysConfig().getAuto_order_return());
					SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String latertime = bartDateFormat.format(ca.getTime());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = sdf.parse(latertime);
					obj.setReturn_shipTime(date);
					obj.setConfirmTime(new Date());// 设置确认收货时间
					if (obj.getOrder_form() == 0) {
						Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
						Map<String, Object> json_map = new HashMap<String, Object>();
						json_map.put("seller_id", store.getUser().getId().toString());
						json_map.put("order_id", obj.getId().toString());
						String json = Json.toJson(json_map);
						/*
						 * //发送邮件
						 * this.msgTools.sendEmailCharge(CommUtil.getURL(request
						 * ), "email_toseller_order_receive_ok_notify",
						 * store.getUser().getEmail(), json, null,
						 * obj.getStore_id()); //发送短信
						 * this.msgTools.sendSmsCharge(CommUtil.getURL(request),
						 * "sms_toseller_order_receive_ok_notify",
						 * store.getUser().getEmail(), json, null,
						 * obj.getStore_id());
						 */
					}

					List<Map> orderGoods = orderFormTools.queryGoodsInfo(obj.getGoods_info());
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					for (Map map : orderGoods) {
						map.put("evaluate", 0);
						list.add(map);
					}
					obj.setGoods_info(Json.toJson(list, JsonFormat.compact()));
					boolean ret = this.orderFormService.update(obj);
					/*
					 * //更改为确认支付减少库存，卖家发货前用户取消订单则恢复商品库存 if (obj.getPayType() !=
					 * null && obj.getPayType().equals("COD")) {//
					 * 如果买家支付方式为货到付款，买家确认收货时更新商品库存
					 * this.updateGoodsInventory(obj);// 更新商品库存 }
					 */

					if (ret) {// 订单状态更新成功，更新相关信息 暂时不处理子订单
						/*
						 * if (obj.getOrder_main() == 1 &&
						 * !CommUtil.null2String(obj.getChild_order_detail())
						 * .equals("")) {// 更新子订单状态信息 List<Map> maps =
						 * this.orderFormTools.queryGoodsInfo(obj
						 * .getChild_order_detail()); for (Map map : maps) {
						 * OrderForm child_order = this.orderFormService
						 * .getObjById(CommUtil.null2Long(map
						 * .get("order_id"))); child_order.setOrder_status(40);
						 * child_order.setReturn_shipTime(date);
						 * child_order.setConfirmTime(new Date());// 设置确认收货时间
						 * this.orderFormService.update(child_order); if
						 * (obj.getPayment().getMark().equals("payafter")) {//
						 * 如果买家支付方式为货到付款，买家确认收货，子订单商品销量增加 List<Map> goods_map =
						 * this.orderFormTools
						 * .queryGoodsInfo(child_order.getGoods_info()); for
						 * (Map child_map : goods_map) { Goods goods =
						 * this.goodsService .getObjById(CommUtil
						 * .null2Long(child_map .get("goods_id")));
						 * goods.setGoods_salenum(goods.getGoods_salenum() +
						 * CommUtil.null2Int(child_map .get("goods_count")));//
						 * 增加商品销量 GoodsLog todayGoodsLog = this.goodsViewTools
						 * .getTodayGoodsLog(goods.getId());
						 * todayGoodsLog.setGoods_salenum(todayGoodsLog
						 * .getGoods_salenum() + CommUtil.null2Int(child_map
						 * .get("goods_count"))); Map<String, Integer>
						 * logordermap = (Map<String, Integer>) Json
						 * .fromJson(todayGoodsLog .getGoods_order_type());
						 * String ordertype = child_order.getOrder_type(); if
						 * (logordermap.containsKey(ordertype)) { logordermap
						 * .put(ordertype, logordermap.get(ordertype) + CommUtil
						 * .null2Int(child_map .get("goods_count"))); } else {
						 * logordermap.put(ordertype, CommUtil
						 * .null2Int(child_map .get("goods_count"))); }
						 * todayGoodsLog.setGoods_order_type(Json.toJson(
						 * logordermap, JsonFormat.compact()));
						 * 
						 * Map<String, Integer> logspecmap = (Map<String,
						 * Integer>) Json .fromJson(todayGoodsLog
						 * .getGoods_sale_info()); String spectype = child_map
						 * .get("goods_gsp_val").toString(); if
						 * (logspecmap.containsKey(spectype)) { logspecmap
						 * .put(spectype, logspecmap.get(spectype) + CommUtil
						 * .null2Int(child_map .get("goods_count"))); } else {
						 * logspecmap.put(spectype, CommUtil .null2Int(child_map
						 * .get("goods_count"))); }
						 * todayGoodsLog.setGoods_sale_info(Json.toJson(
						 * logspecmap, JsonFormat.compact()));
						 * 
						 * this.goodsLogService.update(todayGoodsLog);
						 * 
						 * goods.setGoods_inventory(goods .getGoods_inventory()
						 * - CommUtil.null2Int(child_map
						 * .get("goods_count")));// 库存减少 if
						 * (goods.getGroup_buy() == 2) {// 如果为团购商品，增加团购销量,减少团购库存
						 * for (GroupGoods gg : goods .getGroup_goods_list()) {
						 * if (gg.getGroup() .getId() .equals(goods.getGroup()
						 * .getId())) {
						 * gg.setGg_selled_count(CommUtil.null2Int(gg
						 * .getGg_selled_count() + CommUtil.null2Int(map
						 * .get("goods_count"))));// 增加团购销量 if
						 * (gg.getGg_count()// 减少团购库存 - CommUtil .null2Int(map
						 * .get("goods_count")) > 0) {
						 * gg.setGg_count(gg.getGg_count() -
						 * CommUtil.null2Int(map .get("goods_count"))); } else {
						 * gg.setGg_count(0); } this.ggService.update(gg); } } }
						 * this.goodsService.update(goods); } } //
						 * 向子订单商家发送提醒信息，同时生成结算日志，如果子订单为平台自营，则不发送短信和邮件, if
						 * (child_order.getOrder_form() == 0) { Store store =
						 * this.storeService.getObjById(CommUtil
						 * .null2Long(child_order.getStore_id())); Map json_map
						 * = new HashMap(); json_map.put("seller_id",
						 * store.getUser().getId() .toString());
						 * json_map.put("childorder_id", child_order.getId()
						 * .toString()); String json = Json.toJson(json_map); if
						 * (obj.getOrder_form() == 0) { this.msgTools
						 * .sendEmailCharge( CommUtil.getURL(request),
						 * "email_toseller_order_receive_ok_notify",
						 * store.getUser().getEmail(), json, null,
						 * obj.getStore_id()); this.msgTools.sendSmsCharge(
						 * CommUtil.getURL(request),
						 * "sms_toseller_order_receive_ok_notify",
						 * store.getUser().getEmail(), json, null,
						 * obj.getStore_id()); } // 订单生成商家结算日志 PayoffLog plog =
						 * new PayoffLog(); plog.setPl_sn("pl" +
						 * CommUtil.formatTime("yyyyMMddHHmmss", new Date()) +
						 * store.getUser().getId()); plog.setPl_info("确认收货");
						 * plog.setAddTime(new Date());
						 * plog.setSeller(store.getUser());
						 * plog.setO_id(CommUtil.null2String(child_order
						 * .getId()));
						 * plog.setOrder_id(child_order.getOrder_id()
						 * .toString()); plog.setCommission_amount(child_order
						 * .getCommission_amount());// 该订单总佣金费用
						 * plog.setGoods_info(child_order.getGoods_info());
						 * plog.setOrder_total_price(child_order
						 * .getGoods_amount());// 该订单总商品金额
						 * plog.setTotal_amount(BigDecimal.valueOf(CommUtil
						 * .subtract(child_order.getGoods_amount(),
						 * child_order.getCommission_amount())));//
						 * 该订单应结算金额：结算金额=订单总商品金额-总佣金费用
						 * this.payoffLogservice.save(plog);
						 * store.setStore_sale_amount(BigDecimal
						 * .valueOf(CommUtil.add( child_order.getGoods_amount(),
						 * store.getStore_sale_amount())));// 店铺本次结算总销售金额
						 * store.setStore_commission_amount(BigDecimal.valueOf(
						 * CommUtil .add(child_order.getCommission_amount(),
						 * store.getStore_commission_amount())));// 店铺本次结算总佣金
						 * store.setStore_payoff_amount(BigDecimal
						 * .valueOf(CommUtil.add( plog.getTotal_amount(),
						 * store.getStore_payoff_amount())));// 店铺本次结算总佣金
						 * this.storeService.update(store); // 增加系统总销售金额、总佣金
						 * SysConfig sc = this.configService.getSysConfig();
						 * sc.setPayoff_all_sale(BigDecimal.valueOf(CommUtil
						 * .add(child_order.getGoods_amount(),
						 * sc.getPayoff_all_sale())));
						 * sc.setPayoff_all_commission(BigDecimal
						 * .valueOf(CommUtil.add(
						 * child_order.getCommission_amount(),
						 * sc.getPayoff_all_commission())));
						 * this.configService.update(sc); } } }
						 */
						OrderFormLog ofl = new OrderFormLog();
						ofl.setAddTime(new Date());
						ofl.setLog_info("确认收货");
						ofl.setLog_user(user);
						ofl.setOf(obj);
						this.orderFormLogService.save(ofl);
						// 主订单生成商家结算日志
						if (obj.getOrder_form() == 0) {
							Store store = this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id()));
							PayoffLog plog = new PayoffLog();
							plog.setPl_sn(
									"pl" + CommUtil.formatTime("yyyyMMddHHmmss", new Date()) + store.getUser().getId());
							plog.setPl_info("确认收货");
							plog.setAddTime(new Date());
							plog.setSeller(store.getUser());
							plog.setO_id(CommUtil.null2String(obj.getId()));
							plog.setOrder_id(obj.getOrder_id().toString());
							plog.setOrder_goods_vat(obj.getGoods_vat());// 服务VAT
							plog.setOrder_commission_vat(obj.getCommission_vat());// 佣金VAT
							plog.setOrder_ship_price(obj.getShip_price());// 买家物流费
							plog.setOrder_store_price(obj.getStore_ship_price());// 卖家物流费
							plog.setOrder_transport_type(obj.getTransport_type());
							plog.setCommission_amount(obj.getCommission_amount());// 该订单总佣金费用
							plog.setOrder_total_price(obj.getGoods_amount());// 该订单总商品金额
							Map storemap = this.orderFormTools.queryCouponInfo(obj.getCoupon_info());
							plog.setStore_amount(CommUtil.null2BigDecimal(storemap.get("coupon_amount")));
							Map generalmap = this.orderFormTools.queryCouponInfo(obj.getGeneral_coupon());
							plog.setGeneral_amount(CommUtil.null2BigDecimal(generalmap.get("coupon_amount")));
							if (obj.getTransport_type().equals("0")) {
								double goods_price = CommUtil.subtract(obj.getGoods_amount(),
										obj.getCommission_amount());// 该订单应结算金额：结算金额=订单总商品金额-总佣金费用
																	// [结算金额=订单总商品金额-总佣金费用-服务VAT-VAT]
								double vat_price = CommUtil.add(obj.getGoods_vat(), obj.getCommission_vat());
								double price = CommUtil.subtract(goods_price, vat_price);
								double store_amount = CommUtil.subtract(price, storemap.get("coupon_amount"));
								// double general_amount =
								// CommUtil.add(store_amount,
								// generalmap.get("coupon_amount"));
								plog.setTotal_amount(BigDecimal.valueOf(store_amount));
							} else {
								double goods_price = CommUtil.subtract(obj.getGoods_amount(),
										obj.getCommission_amount());
								double vat_price = CommUtil.add(obj.getGoods_vat(), obj.getCommission_vat());
								double ship_price = CommUtil.add(vat_price, obj.getStore_ship_price());
								double price = CommUtil.subtract(goods_price, ship_price);
								double store_amount = CommUtil.subtract(price, storemap.get("coupon_amount"));
								// double general_amount =
								// CommUtil.add(store_amount,
								// generalmap.get("coupon_amount"));
								plog.setTotal_amount(BigDecimal.valueOf(store_amount));
							}
							plog.setGoods_info(obj.getGoods_info());
							this.payoffLogservice.save(plog);
							this.storeService.update(store);
							// 增加系统总销售金额、总佣金
							SysConfig sc = this.configService.getSysConfig();
							sc.setPayoff_all_sale(
									BigDecimal.valueOf(CommUtil.add(obj.getGoods_amount(), sc.getPayoff_all_sale())));
							sc.setPayoff_all_commission(BigDecimal
									.valueOf(CommUtil.add(obj.getCommission_amount(), sc.getPayoff_all_commission())));
							this.configService.update(sc);
							/*
							 * StoreLog storeLog =
							 * this.storeLogTools.getTodayStoreLog(CommUtil.
							 * null2Long(obj.getStore_id()));
							 * storeLog.setSignfor(storeLog.getSignfor() + 1);
							 * this.storeLogService.update(storeLog);
							 */
							// 记录积分明细
							if (this.configService.getSysConfig().isIntegral()) {
								int integral = CommUtil
										.null2Int(obj.getTotalPrice().setScale(0, BigDecimal.ROUND_HALF_UP));
								DecimalFormat df = new DecimalFormat("0");
								user.setIntegral(user.getIntegral() + integral);
								this.userService.update(user);
								IntegralLog integralLog = new IntegralLog();
								integralLog.setAddTime(new Date());
								integralLog.setIntegral_user(user);
								integralLog.setContent("确认收货增加" + integral + "积分");
								integralLog.setIntegral(integral);
								integralLog.setType("Add");
								integralLog.setIntegral_from("confirm_order");
								this.integralLogService.save(integralLog);
							}
						}
					}
					result = new Result(0, "Successfully");
				} else {
					result = new Result(1, "The order does not exist");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 买家评价
	 */
	@SecurityMapping(title = "买家评价", value = "/buyer_order_evaluate.json*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_evaluate.json", method = RequestMethod.GET)
	public void order_evaluate(HttpServletRequest request, HttpServletResponse response, String id, String goods_id,
			String token, String language) {
		Result result = null;
		String msg = null;
		int code = -1;
		Map<String, Object> map = new HashMap<String, Object>();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm orderForm = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (orderForm != null && orderForm.getUser_id().equals(CommUtil.null2String(user.getId()))) {
					Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
					if (obj != null) {
						map.put("order_id", orderForm.getId());
						String store_name = orderForm.getStore_name();
						List<Map> goodsList = orderFormTools.queryGoodsInfo(orderForm.getGoods_info());
						for (Map goods : goodsList) {
							if (goods_id.equals(goods.get("goods_id").toString())) {
								map.put("goods_id", goods.get("goods_id"));
								map.put("goods_name", goods.get("goods_name"));
								if ("1".equals(language)) {
									map.put("goods_name",
											goods.get("ksa_goods_name") != null
													&& !"".equals(goods.get("ksa_goods_name"))
															? "^" + goods.get("ksa_goods_name")
															: goods.get("goods_name"));
								}
								map.put("eva_goods_count", goods.get("goods_count"));
								map.put("eva_goods_price", goods.get("goods_price"));
								map.put("eva_goods_type", goods.get("goods_type"));
								map.put("eva_goods_gsp_val", goods.get("goods_gsp_val"));
								map.put("goods_color", goods.get("color"));
								map.put("goods_photo", this.configService.getSysConfig().getImageWebServer() + "/"
										+ goods.get("goods_mainphoto_path"));
								map.put("eva_goods_domainPath", goods.get("goods_domainPath"));
								map.put("eva_combin_suit_info", goods.get("combin_suit_info"));
								map.put("store_name", store_name);
							}
						}
						map.put("jsessionid", request.getSession().getId());
						String evaluate_session = CommUtil.randomString(32);
						request.getSession(false).setAttribute("evaluate_session", evaluate_session);
						map.put("evaluate_session", evaluate_session);
						code = 4200;
						msg = "Successfully";
						if (orderForm.getOrder_status() >= 50) {
							msg = "This order has been evaluated";
						}
					} else {
						code = 4205;
						msg = "The commodity does not exist";
					}

				} else {
					code = 4205;
					msg = "The order does not exist";
				}
				result = new Result(code, msg, map);
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "买家评价图片上传", value = "/buyer_upload_evaluate.json*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "upload_evaluate.json", method = RequestMethod.POST)
	public void upload_evaluate(HttpServletRequest request, HttpServletResponse response, String token) {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map<String, Object> params = new HashMap<String, Object>();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				Map<String, Object> json_map = new HashMap<String, Object>();
				SysConfig config = this.configService.getSysConfig();
				String path = "";
				String uploadFilePath = config.getUploadFilePath();
				response.setContentType("text/html;charset=UTF-8");
				response.setHeader("Pragma", "No-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
				params.clear();
				params.put("userid", user.getId());
				params.put("info", "eva_temp");
				List<Accessory> imglist = this.accessoryService.query(
						"select obj from Accessory obj where obj.user.id=:userid and obj.info=:info", params, -1, -1);
				if (imglist.size() > 40) {// 每个用户最多可以上传40张临时图片，半小时清理一次
					json_map.put("ret", false);
					json_map.put("msg", "您最近上传过多图片，请稍后重试");
				} else {
					json_map.put("ret", true);
					try {
						// <1>. 判断路径是否存在,若不存在则创建路径
						String goods_id = CommUtil.null2String(request.getParameter("goods_id"));
						/*
						 * String filePath =
						 * request.getSession().getServletContext()
						 * .getRealPath("/") + uploadFilePath + File.separator +
						 * "evaluate" + File.separator + "goods_" + goods_id;
						 * CommUtil.createFolder(filePath); path =
						 * uploadFilePath + "/evaluate/goods_" + goods_id;
						 */
						String filePath = uploadFilePath + "/" + "evaluate";
						Map map = CommUtil.httpsaveFileToServer(request, "evaluate_photos", filePath, null, null);
						if (map.get("fileName") == null) {
							result = new Result(1, "图片太小或参数错误");
						} else {
							Accessory image = new Accessory();
							image.setUser(user);
							image.setAddTime(new Date());
							image.setExt((String) map.get("mime"));
							image.setPath(filePath);
							image.setWidth(CommUtil.null2Int(map.get("width")));
							image.setHeight(CommUtil.null2Int(map.get("height")));
							image.setName(CommUtil.null2String(map.get("fileName")));
							image.setInfo("eva_temp");
							this.accessoryService.save(image);
							/*
							 * json_map.put("url", CommUtil.getURL(request) +
							 * "/" + filePath + "/" + image.getName());
							 */
							json_map.put("url", this.configService.getSysConfig().getImageWebServer() + "/" + filePath
									+ "/" + image.getName());
							json_map.put("img_id", image.getId());
							json_map.put("goods_id", goods_id);
							result = new Result(0, "Successfully", json_map);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			try {
				response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @param token
	 */
	@RequestMapping(value = "del_evaluate.json", method = RequestMethod.DELETE)
	public void delImg(HttpServletRequest request, HttpServletResponse response, String id, String token) {
		/*
		 * int code = -1; String msg = "";
		 */
		Result result = null;
		if (CommUtil.null2String(token).equals("")) {
			result = new Result(-100, "token Invalidation");
			/*
			 * code = -100; msg = "token Invalidation";
			 */
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
				/*
				 * code = -100; msg = "token Invalidation";
				 */
			} else {
				user = user.getParent() == null ? user : user.getParent();
				Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(id));
				if (accessory == null && accessory.getUser().getId().equals(user.getId())) {
					result = new Result(4404, "There is no such resource");
				} else {
					String param = accessory.getPath() + "/" + accessory.getName();
					int code = CommUtil.httpDelFileToServer(request, response, param);
					if (code == 0) {
						for (Goods goods : accessory.getGoods_main_list()) {
							goods.setGoods_main_photo(null);
							this.goodsService.update(goods);
						}
						for (Goods goods1 : accessory.getGoods_list()) {
							goods1.getGoods_photos().remove(accessory);
							if (goods1.getGoods_inventory_detail() != ""
									&& !goods1.getGoods_inventory_detail().equals("")) {
								String inventory_detail = goods1.getGoods_inventory_detail();

							}
							this.goodsService.update(goods1);
						}
						if (accessory.getC_goods_list().isEmpty()) {
							for (CGoods cgoods : accessory.getC_goods_list()) {
								cgoods.getGoods_photos().remove(accessory);
								this.cGoodsService.update(cgoods);
							}
						}

						this.accessoryService.delete(accessory.getId());
						result = new Result(code, "Successfully");
					} else {
						result = new Result(code, "Error");
					}
				}
			}

		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*@SecurityMapping(title = "买家评价保存", value = "/buyer_order_evaluate_save*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_evaluate_save.json", method = RequestMethod.POST)
	public void order_evaluate_save(HttpServletRequest request, HttpServletResponse response, String id,
			String evaluate_session, String goods_id, String token) throws Exception {
		Result result = null;
		int code = -1;
		String msg = "";
		if (CommUtil.null2String(token).equals("")) {
			code = -100;
			msg = "token Invalidation";
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				code = -100;
				msg = "token Invalidation";
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				String evaluate_session1 = (String) request.getSession(false).getAttribute("evaluate_session");
				if (evaluate_session1 != null && evaluate_session1.equals(evaluate_session)) {
					if (!CommUtil.null2String(request.getParameter("evaluate_info")).equals("")) {
						request.getSession(false).removeAttribute("evaluate_session");
						if (obj != null && obj.getUser_id().equals(CommUtil.null2String(user.getId()))) {
							int i = obj.getOrder_status();
							if (obj.getOrder_status() == 40) {
								List<Map> json = this.orderFormTools.queryGoodsInfo(obj.getGoods_info());
								boolean flag = false;
								int description = eva_rate(request.getParameter("description_evaluate"));
								int service = eva_rate(request.getParameter("service_evaluate"));
								int ship = eva_rate(request.getParameter("ship_evaluate"));
								int total = eva_rate(request.getParameter("evaluate_buyer_val"));
								if (description == 0 || service == 0 || ship == 0 || total == -5) {
									flag = true;
								}
								if (flag) {
									result = new Result(3, "参数错误，禁止评价");
									code = 4400;
									msg = "parameter error";
								} else {
									OrderFormLog ofl = new OrderFormLog();
									ofl.setAddTime(new Date());
									ofl.setLog_info("评价订单");
									ofl.setLog_user(user);
									ofl.setOf(obj);
									this.orderFormLogService.save(ofl);
									obj.setFinishTime(new Date());
									this.orderFormService.update(obj);
									for (Map map : json) {
										map.put("orderForm", obj.getId());
									}
									// 暂时不处理子订单 List<Map> child_order =
									// this.orderFormTools.queryGoodsInfo(obj.getChild_order_detail());
									// List<Map> child_goods = new
									// ArrayList<Map>();
									// for (Map c : child_order) {
									// List<Map> maps = this.orderFormTools
									// .queryGoodsInfo(c.get("order_goods_info").toString());
									// for (Map cmap : maps) {
									// cmap.put("orderForm", c.get("order_id"));
									// }
									// child_goods.addAll(maps);
									// }
									// if (child_goods.size() > 0) {
									// json.addAll(child_goods);
									// }
									for (Map map : json) {
										Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
										if (goods != null) {
											if (map.get("goods_id").toString().equals(goods_id)) {
												List<Map> map_list = this.orderFormTools.compare(obj.getGoods_info(),
														goods_id);
												obj.setGoods_info(Json.toJson(map_list, JsonFormat.compact()));
												int order_status = this.orderFormTools.finish(obj.getGoods_info(),
														goods_id);
												if (order_status == 1) {
													obj.setOrder_status(50);
													this.orderFormService.update(obj);
												}
												Evaluate eva = new Evaluate();
												goods.setEvaluate_count(goods.getEvaluate_count() + 1);
												eva.setAddTime(new Date());
												eva.setEvaluate_goods(goods);
												eva.setGoods_num(CommUtil.null2Int(map.get("goods_count")));
												eva.setGoods_price(map.get("goods_price").toString());
												eva.setGoods_spec(map.get("goods_gsp_val").toString());
												eva.setEvaluate_info(request.getParameter("evaluate_info"));
												eva.setEvaluate_photos(request.getParameter("evaluate_photos"));
												eva.setEvaluate_buyer_val(CommUtil.null2Int(
														eva_rate(request.getParameter("evaluate_buyer_val"))));
												eva.setDescription_evaluate(BigDecimal.valueOf(CommUtil.null2Double(
														eva_rate(request.getParameter("description_evaluate")))));
												eva.setService_evaluate(BigDecimal.valueOf(CommUtil.null2Double(
														eva_rate(request.getParameter("service_evaluate")))));
												eva.setShip_evaluate(BigDecimal.valueOf(CommUtil
														.null2Double(eva_rate(request.getParameter("ship_evaluate")))));
												eva.setEvaluate_type("goods");
												eva.setEvaluate_user(user);
												eva.setOf(this.orderFormService
														.getObjById(CommUtil.null2Long(map.get("orderForm"))));
												eva.setReply_status(0);
												this.evaluateService.save(eva);
												String im_str = request.getParameter("evaluate_photos");
												if (im_str != null && !im_str.equals("") && im_str.length() > 0) {
													for (String str : im_str.split(",")) {
														if (str != null && !str.equals("")) {
															Accessory image = this.accessoryService
																	.getObjById(CommUtil.null2Long(str));
															if (image.getInfo().equals("eva_temp")) {
																image.setInfo("eva_img");
																this.accessoryService.save(image);
															}
														}
													}
												}
												params.clear();
												if (goods.getGoods_type() == 1) {
													Store store = this.storeService.getObjById(
															CommUtil.null2Long(goods.getGoods_store().getId()));
													params.put("store_id", store.getId().toString());
													List<Evaluate> evas = this.evaluateService.query(
															"select obj from Evaluate obj where obj.of.store_id=:store_id",
															params, -1, -1);
													double store_evaluate = 0;
													double description_evaluate = 0;
													double description_evaluate_total = 0;
													double service_evaluate = 0;
													double service_evaluate_total = 0;
													double ship_evaluate = 0;
													double ship_evaluate_total = 0;
													DecimalFormat df = new DecimalFormat("0.0");
													for (Evaluate eva1 : evas) {
														description_evaluate_total = description_evaluate_total
																+ CommUtil.null2Double(eva1.getDescription_evaluate());
														service_evaluate_total = service_evaluate_total
																+ CommUtil.null2Double(eva1.getService_evaluate());
														ship_evaluate_total = ship_evaluate_total
																+ CommUtil.null2Double(eva1.getShip_evaluate());
													}
													description_evaluate = CommUtil.null2Double(
															df.format(description_evaluate_total / evas.size()));
													service_evaluate = CommUtil.null2Double(
															df.format(service_evaluate_total / evas.size()));
													ship_evaluate = CommUtil
															.null2Double(df.format(ship_evaluate_total / evas.size()));
													store_evaluate = (description_evaluate + service_evaluate
															+ ship_evaluate) / 3;// 综合评分=三项具体评分之和/3
													store.setStore_credit(
															store.getStore_credit() + eva.getEvaluate_buyer_val());
													this.storeService.update(store);
													params.clear();
													params.put("store_id", store.getId());
													List<StorePoint> sps = this.storePointService.query(
															"select obj from StorePoint obj where obj.store.id=:store_id",
															params, -1, -1);
													StorePoint point = null;
													if (sps.size() > 0) {
														point = sps.get(0);
													} else {
														point = new StorePoint();
													}
													point.setAddTime(new Date());
													point.setStore(store);
													point.setDescription_evaluate(BigDecimal.valueOf(
															description_evaluate > 5 ? 5 : description_evaluate));
													point.setService_evaluate(BigDecimal
															.valueOf(service_evaluate > 5 ? 5 : service_evaluate));
													point.setShip_evaluate(
															BigDecimal.valueOf(ship_evaluate > 5 ? 5 : ship_evaluate));
													point.setStore_evaluate(BigDecimal
															.valueOf(store_evaluate > 5 ? 5 : store_evaluate));
													if (sps.size() > 0) {
														this.storePointService.update(point);
													} else {
														this.storePointService.save(point);
													}
												} else {
													User sp_user = this.userService.getObjById(obj.getEva_user_id());
													params.put("user_id", user.getId().toString());
													List<Evaluate> evas = this.evaluateService.query(
															"select obj from Evaluate obj where obj.of.user_id=:user_id",
															params, -1, -1);
													double store_evaluate = 0;
													double description_evaluate = 0;
													double description_evaluate_total = 0;
													double service_evaluate = 0;
													double service_evaluate_total = 0;
													double ship_evaluate = 0;
													double ship_evaluate_total = 0;
													DecimalFormat df = new DecimalFormat("0.0");
													for (Evaluate eva1 : evas) {
														description_evaluate_total = description_evaluate_total
																+ CommUtil.null2Double(eva1.getDescription_evaluate());
														service_evaluate_total = service_evaluate_total
																+ CommUtil.null2Double(eva1.getService_evaluate());
														ship_evaluate_total = ship_evaluate_total
																+ CommUtil.null2Double(eva1.getShip_evaluate());
													}
													description_evaluate = CommUtil.null2Double(
															df.format(description_evaluate_total / evas.size()));
													service_evaluate = CommUtil.null2Double(
															df.format(service_evaluate_total / evas.size()));
													ship_evaluate = CommUtil
															.null2Double(df.format(ship_evaluate_total / evas.size()));
													store_evaluate = (description_evaluate + service_evaluate
															+ ship_evaluate) / 3;
													params.clear();
													params.put("user_id", obj.getEva_user_id());
													List<StorePoint> sps = this.storePointService.query(
															"select obj from StorePoint obj where obj.user.id=:user_id",
															params, -1, -1);
													StorePoint point = null;
													if (sps.size() > 0) {
														point = sps.get(0);
													} else {
														point = new StorePoint();
													}
													point.setAddTime(new Date());
													point.setUser(sp_user);
													point.setDescription_evaluate(BigDecimal.valueOf(
															description_evaluate > 5 ? 5 : description_evaluate));
													point.setService_evaluate(BigDecimal
															.valueOf(service_evaluate > 5 ? 5 : service_evaluate));
													point.setShip_evaluate(
															BigDecimal.valueOf(ship_evaluate > 5 ? 5 : ship_evaluate));
													point.setStore_evaluate(BigDecimal
															.valueOf(store_evaluate > 5 ? 5 : store_evaluate));
													if (sps.size() > 0) {
														this.storePointService.update(point);
													} else {
														this.storePointService.save(point);
													}
												}
												this.goodsService.update(goods);
												User usr = this.userService
														.getObjById(CommUtil.null2Long(obj.getUser_id()));
												usr.setIntegral(usr.getIntegral()
														+ this.configService.getSysConfig().getIndentComment());
												// 增加用户消费金额
												usr.setUser_goods_fee(BigDecimal.valueOf(
														CommUtil.add(usr.getUser_goods_fee(), obj.getTotalPrice())));

												this.userService.update(usr);
												// 记录积分明细
												if (this.configService.getSysConfig().isIntegral()) {
													IntegralLog log = new IntegralLog();
													log.setAddTime(new Date());
													log.setIntegral_user(usr);
													log.setType("Add");
													log.setIntegral_from("order");
													int integral = this.configService.getSysConfig()
															.getMemberCharEvaluate();
													if (null == request.getParameter("evaluate_photos")
															|| request.getParameter("evaluate_photos").equals("")) {
														log.setIntegral(integral);
													} else {
														String photo = request.getParameter("evaluate_photos");
														String[] str = photo.split(",");
														double pintegral = CommUtil.mul(str.length,
																this.configService.getSysConfig().getMemberPhotoChar());
														integral += new Double(pintegral).intValue();
														log.setIntegral(integral);
													}
													log.setContent("订单文字评价增加" + integral + "分");
													this.integralLogService.save(log);
												}
												code = 4200;
												msg = "Successfully";
											}
										} else {
											code = 4205;
											msg = "The commodity does not exist";
										}
										
										 * if
										 * (this.configService.getSysConfig().
										 * isEmailEnable()) { if
										 * (obj.getOrder_form() == 0) { Store
										 * store = this.storeService.getObjById(
										 * CommUtil.
										 * null2Long(obj.getStore_id())); Map
										 * map = new HashMap();
										 * map.put("seller_id",
										 * store.getUser().getId().toString());
										 * map.put("order_id",
										 * obj.getId().toString()); String
										 * jsonmap = Json.toJson(map);//发送邮件
										 * this.msgTools.sendEmailCharge(
										 * CommUtil. getURL(request),
										 * "email_toseller_evaluate_ok_notify",
										 * store.getUser().getEmail(), jsonmap,
										 * null, obj.getStore_id()); } }
										 
									}
								}
							} else {
								code = 2;
								msg = "This order is not evaluable";
							}
						}
					} else {
						code = 4400;
						msg = "parameter error";
					}
				} else {
					code = 1;
					msg = "No repeat evaluation";
				}
			}
		}
		result = new Result(code, msg);
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	/**
	 * @descript 对整单进行评价
	 * @param request
	 * @param response
	 * @param id
	 * @param evaluate_session
	 * @param goods_id
	 * @param token
	 * @throws Exception
	 */
	@SecurityMapping(title = "买家评价保存", value = "/buyer_order_evaluate_save*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_evaluate_save.json", method = RequestMethod.POST)
	public void orderEvaluate(HttpServletRequest request, HttpServletResponse response, String id,
			String evaluate_session, String goods_id, String token) throws Exception {
		Result result = null;
		int code = -1;
		String msg = "";
		if (CommUtil.null2String(token).equals("")) {
			code = -100;
			msg = "token Invalidation";
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				code = -100;
				msg = "token Invalidation";
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				String evaluate_session1 = (String) request.getSession(false).getAttribute("evaluate_session");
				if (evaluate_session1 != null && evaluate_session1.equals(evaluate_session)) {
					request.getSession(false).removeAttribute("evaluate_session");
					if (obj != null && obj.getUser_id().equals(CommUtil.null2String(user.getId()))) {
						if (obj.getOrder_status() == 40) {
							List<Map> json = this.orderFormTools.queryGoodsInfo(obj.getGoods_info());
							boolean flag = false;
							int description = eva_rate(request.getParameter("description_evaluate"));
							int service = eva_rate(request.getParameter("service_evaluate"));
							int ship = eva_rate(request.getParameter("ship_evaluate"));
							int total = eva_rate(request.getParameter("evaluate_buyer_val"));
							if (description == 0 || service == 0 || ship == 0 || total == -5) {
								flag = true;
							}
							if (flag) {
								result = new Result(3, "参数错误，禁止评价");
								code = 4400;
								msg = "parameter error";
							} else {
								OrderFormLog ofl = new OrderFormLog();
								ofl.setAddTime(new Date());
								ofl.setLog_info("评价订单");
								ofl.setLog_user(user);
								ofl.setOf(obj);
								this.orderFormLogService.save(ofl);
								obj.setFinishTime(new Date());
								this.orderFormService.update(obj);
								for (Map map : json) {
									map.put("orderForm", obj.getId());
								}
								List<Map> child_order = this.orderFormTools.queryGoodsInfo(obj.getChild_order_detail());
								List<Map> child_goods = new ArrayList<Map>();
								for (Map c : child_order) {
									List<Map> maps = this.orderFormTools
											.queryGoodsInfo(c.get("order_goods_info").toString());
									for (Map cmap : maps) {
										cmap.put("orderForm", c.get("order_id"));
									}
									child_goods.addAll(maps);
								}
								if (child_goods.size() > 0) {
									json.addAll(child_goods);
								}
								for (Map map : json) {
									Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
									if (goods != null) {
										if (map.get("goods_id").toString().equals(goods_id)) {
											List<Map> map_list = this.orderFormTools.compare(obj.getGoods_info(),
													goods_id);
											obj.setGoods_info(Json.toJson(map_list, JsonFormat.compact()));
											int order_status = this.orderFormTools.finish(obj.getGoods_info(),
													goods_id);
											if (order_status == 1) {
												obj.setOrder_status(50);
												this.orderFormService.update(obj);
											}
											Evaluate eva = new Evaluate();
											goods.setEvaluate_count(goods.getEvaluate_count() + 1);
											eva.setAddTime(new Date());
											eva.setEvaluate_goods(goods);
											eva.setGoods_num(CommUtil.null2Int(map.get("goods_count")));
											eva.setGoods_price(map.get("goods_price").toString());
											eva.setGoods_spec(map.get("goods_gsp_val").toString());
											eva.setEvaluate_info(request.getParameter("evaluate_info"));
											eva.setEvaluate_photos(request.getParameter("evaluate_photos"));
											eva.setEvaluate_buyer_val(CommUtil
													.null2Int(eva_rate(request.getParameter("evaluate_buyer_val"))));
											eva.setDescription_evaluate(BigDecimal.valueOf(CommUtil.null2Double(
													eva_rate(request.getParameter("description_evaluate")))));
											eva.setService_evaluate(BigDecimal.valueOf(CommUtil
													.null2Double(eva_rate(request.getParameter("service_evaluate")))));
											eva.setShip_evaluate(BigDecimal.valueOf(CommUtil
													.null2Double(eva_rate(request.getParameter("ship_evaluate")))));
											eva.setEvaluate_type("goods");
											eva.setEvaluate_user(user);
											eva.setOf(this.orderFormService
													.getObjById(CommUtil.null2Long(map.get("orderForm"))));
											eva.setReply_status(0);
											this.evaluateService.save(eva);
											String im_str = request.getParameter("evaluate_photos");
											if (im_str != null && !im_str.equals("") && im_str.length() > 0) {
												for (String str : im_str.split(",")) {
													if (str != null && !str.equals("")) {
														Accessory image = this.accessoryService
																.getObjById(CommUtil.null2Long(str));
														if (image.getInfo().equals("eva_temp")) {
															image.setInfo("eva_img");
															this.accessoryService.save(image);
														}
													}
												}
											}
											params.clear();
											if (goods.getGoods_type() == 1) {
												Store store = this.storeService
														.getObjById(CommUtil.null2Long(goods.getGoods_store().getId()));
												params.put("store_id", store.getId().toString());
												List<Evaluate> evas = this.evaluateService.query(
														"select obj from Evaluate obj where obj.of.store_id=:store_id",
														params, -1, -1);
												double store_evaluate = 0;
												double description_evaluate = 0;
												double description_evaluate_total = 0;
												double service_evaluate = 0;
												double service_evaluate_total = 0;
												double ship_evaluate = 0;
												double ship_evaluate_total = 0;
												DecimalFormat df = new DecimalFormat("0.0");
												for (Evaluate eva1 : evas) {
													description_evaluate_total = description_evaluate_total
															+ CommUtil.null2Double(eva1.getDescription_evaluate());
													service_evaluate_total = service_evaluate_total
															+ CommUtil.null2Double(eva1.getService_evaluate());
													ship_evaluate_total = ship_evaluate_total
															+ CommUtil.null2Double(eva1.getShip_evaluate());
												}
												description_evaluate = CommUtil.null2Double(
														df.format(description_evaluate_total / evas.size()));
												service_evaluate = CommUtil
														.null2Double(df.format(service_evaluate_total / evas.size()));
												ship_evaluate = CommUtil
														.null2Double(df.format(ship_evaluate_total / evas.size()));
												store_evaluate = (description_evaluate + service_evaluate
														+ ship_evaluate) / 3;// 综合评分=三项具体评分之和/3
												store.setStore_credit(
														store.getStore_credit() + eva.getEvaluate_buyer_val());
												this.storeService.update(store);
												params.clear();
												params.put("store_id", store.getId());
												List<StorePoint> sps = this.storePointService.query(
														"select obj from StorePoint obj where obj.store.id=:store_id",
														params, -1, -1);
												StorePoint point = null;
												if (sps.size() > 0) {
													point = sps.get(0);
												} else {
													point = new StorePoint();
												}
												point.setAddTime(new Date());
												point.setStore(store);
												point.setDescription_evaluate(BigDecimal
														.valueOf(description_evaluate > 5 ? 5 : description_evaluate));
												point.setService_evaluate(BigDecimal
														.valueOf(service_evaluate > 5 ? 5 : service_evaluate));
												point.setShip_evaluate(
														BigDecimal.valueOf(ship_evaluate > 5 ? 5 : ship_evaluate));
												point.setStore_evaluate(
														BigDecimal.valueOf(store_evaluate > 5 ? 5 : store_evaluate));
												if (sps.size() > 0) {
													this.storePointService.update(point);
												} else {
													this.storePointService.save(point);
												}
											} else {
												User sp_user = this.userService.getObjById(obj.getEva_user_id());
												params.put("user_id", user.getId().toString());
												List<Evaluate> evas = this.evaluateService.query(
														"select obj from Evaluate obj where obj.of.user_id=:user_id",
														params, -1, -1);
												double store_evaluate = 0;
												double description_evaluate = 0;
												double description_evaluate_total = 0;
												double service_evaluate = 0;
												double service_evaluate_total = 0;
												double ship_evaluate = 0;
												double ship_evaluate_total = 0;
												DecimalFormat df = new DecimalFormat("0.0");
												for (Evaluate eva1 : evas) {
													description_evaluate_total = description_evaluate_total
															+ CommUtil.null2Double(eva1.getDescription_evaluate());
													service_evaluate_total = service_evaluate_total
															+ CommUtil.null2Double(eva1.getService_evaluate());
													ship_evaluate_total = ship_evaluate_total
															+ CommUtil.null2Double(eva1.getShip_evaluate());
												}
												description_evaluate = CommUtil.null2Double(
														df.format(description_evaluate_total / evas.size()));
												service_evaluate = CommUtil
														.null2Double(df.format(service_evaluate_total / evas.size()));
												ship_evaluate = CommUtil
														.null2Double(df.format(ship_evaluate_total / evas.size()));
												store_evaluate = (description_evaluate + service_evaluate
														+ ship_evaluate) / 3;
												params.clear();
												params.put("user_id", obj.getEva_user_id());
												List<StorePoint> sps = this.storePointService.query(
														"select obj from StorePoint obj where obj.user.id=:user_id",
														params, -1, -1);
												StorePoint point = null;
												if (sps.size() > 0) {
													point = sps.get(0);
												} else {
													point = new StorePoint();
												}
												point.setAddTime(new Date());
												point.setUser(sp_user);
												point.setDescription_evaluate(BigDecimal
														.valueOf(description_evaluate > 5 ? 5 : description_evaluate));
												point.setService_evaluate(BigDecimal
														.valueOf(service_evaluate > 5 ? 5 : service_evaluate));
												point.setShip_evaluate(
														BigDecimal.valueOf(ship_evaluate > 5 ? 5 : ship_evaluate));
												point.setStore_evaluate(
														BigDecimal.valueOf(store_evaluate > 5 ? 5 : store_evaluate));
												if (sps.size() > 0) {
													this.storePointService.update(point);
												} else {
													this.storePointService.save(point);
												}
											}
											this.goodsService.update(goods);
											User usr = this.userService
													.getObjById(CommUtil.null2Long(obj.getUser_id()));
											// 增加评价积分
											usr.setIntegral(usr.getIntegral()
													+ this.configService.getSysConfig().getIndentComment());
											// 增加用户消费金额
											usr.setUser_goods_fee(BigDecimal.valueOf(
													CommUtil.add(usr.getUser_goods_fee(), obj.getTotalPrice())));

											this.userService.update(usr);
											// 记录积分明细
											if (this.configService.getSysConfig().isIntegral()) {
												IntegralLog log = new IntegralLog();
												log.setAddTime(new Date());
												log.setContent("订单评价增加"
														+ this.configService.getSysConfig().getIndentComment() + "分");
												log.setIntegral(this.configService.getSysConfig().getIndentComment());
												log.setIntegral_user(usr);
												log.setType("order");
												this.integralLogService.save(log);
											}
											code = 4200;
											msg = "Successfully";
										}
									} else {
										code = 4205;
										msg = "The commodity does not exist";
									}
									/*
									 * if (this.configService.getSysConfig().
									 * isEmailEnable()) { if
									 * (obj.getOrder_form() == 0) { Store store
									 * = this.storeService.getObjById(CommUtil.
									 * null2Long(obj.getStore_id())); Map map =
									 * new HashMap(); map.put("seller_id",
									 * store.getUser().getId().toString());
									 * map.put("order_id",
									 * obj.getId().toString()); String jsonmap =
									 * Json.toJson(map);//发送邮件
									 * this.msgTools.sendEmailCharge(CommUtil.
									 * getURL(request),
									 * "email_toseller_evaluate_ok_notify",
									 * store.getUser().getEmail(), jsonmap,
									 * null, obj.getStore_id()); } }
									 */
								}
							}
						} else {
							code = 2;
							msg = "This order is not evaluable";
						}
					}
				} else {
					code = 1;
					msg = "No repeat evaluation";
				}
			}
		}
		result = new Result(code, msg);
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 1. 只有已取消和(已完成，暂时只有已完成订单可删除)的订单可以删除；
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @param currentPage
	 * @param token
	 * @throws Exception
	 */
	@SecurityMapping(title = "移到回收站", value = "/buyer/order_delete.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_delete.json", method = RequestMethod.DELETE)
	public void order_delete(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String token) throws Exception {
		Result result = null;
		if (CommUtil.null2String(token).equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (obj != null && obj.getUser_id().equals(user.getId().toString())) {
					if (obj.getOrder_status() == 0) {
						/*
						 * if (obj.getOrder_main() == 1 && obj.getOrder_cat() ==
						 * 0) {// [订单分类，0为购物订单，1为手机充值订单 // 2为生活类团购订单 //
						 * 3为商品类团购订单 // 4旅游报名订单] if (obj.getChild_order_detail()
						 * != null && !obj.getChild_order_detail().equals("")) {
						 * List<Map> maps = (List<Map>)
						 * Json.fromJson(obj.getChild_order_detail()); for (Map
						 * map : maps) { OrderForm child_order =
						 * this.orderFormService
						 * .getObjById(CommUtil.null2Long(map.get("order_id")));
						 * child_order.setOrder_status(0);
						 * this.orderFormService.delete(child_order.getId()); }
						 * } }
						 */
						obj.setOrder_status(90);
						this.orderFormService.update(obj);
						result = new Result(0, "Successfully");
					} else {
						result = new Result(1, "Error: ");
					}
				} else {
					result = new Result(1, "The order does not exist");
				}
			}
		}
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "买家物流详情", value = "/buyer/ship_view.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "ship_view.json", method = RequestMethod.GET)
	public void order_ship_view(HttpServletRequest request, HttpServletResponse response, String id, String token) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(id));
				if (order != null && !order.equals("")) {
					if (order.getUser_id().equals(user.getId().toString())) {
						List<TransInfo> transInfo_list = new ArrayList<TransInfo>();
						TransInfo transInfo = this.query_ship_getData(id);
						transInfo.setExpress_company_name(
								this.orderFormTools.queryExInfo(order.getExpress_info(), "express_company_name"));
						transInfo.setExpress_ship_code(order.getShipCode());
						transInfo_list.add(transInfo);
						/*
						 * if (order.getOrder_main() == 1 &&
						 * !CommUtil.null2String(order.getChild_order_detail())
						 * .equals("")) {// 查询子订单的物流跟踪信息 List<Map> maps =
						 * this.orderFormTools.queryGoodsInfo(order
						 * .getChild_order_detail()); for (Map child_map : maps)
						 * { OrderForm child_order = this.orderFormService
						 * .getObjById(CommUtil.null2Long(child_map
						 * .get("order_id"))); if (child_order.getExpress_info()
						 * != null) { TransInfo transInfo1 = this
						 * .query_ship_getData(CommUtil
						 * .null2String(child_order.getId())); transInfo1
						 * .setExpress_company_name(this.orderFormTools
						 * .queryExInfo(child_order .getExpress_info(),
						 * "express_company_name"));
						 * transInfo1.setExpress_ship_code(child_order
						 * .getShipCode()); transInfo_list.add(transInfo1); } }
						 * 
						 * }
						 */

						List<TransInfo> transInfolist = new ArrayList<TransInfo>();
						List<Map<String, Object>> transinfomap = new ArrayList<Map<String, Object>>();
						for (TransInfo transinfo : transInfolist) {
							Map<String, Object> transmap = new HashMap<String, Object>();
							transmap.put("", transinfo.getExpress_company_name());
							transmap.put("", transinfo.getExpress_ship_code());
							int status = Integer.parseInt(transinfo.getStatus());
							if (status == 1) {
								List<TransContent> tranContent = transinfo.getData();
								List<Map> transContList = new ArrayList<Map>();
								for (TransContent transContent : tranContent) {
									Map tranconMap = new HashMap();
									tranconMap.put("time", transContent.getTime());
									tranconMap.put("content", transContent.getContext());
									transContList.add(tranconMap);
								}
								transmap.put("transContent", transContList);
							}
							transmap.put("", transinfo.getExpress_company_name());
							transinfomap.add(transmap);
						}
						map.put("transinfo", transinfomap);

						Map orderMap = new HashMap();
						try {
							orderMap.put("Receiver_Name", order.getReceiver_Name());
							orderMap.put("Receiver_area", order.getReceiver_area());
							orderMap.put("Receiver_area_info", order.getReceiver_area_info());
							orderMap.put("Receiver_zip", order.getReceiver_zip());
							orderMap.put("Receiver_telephone", order.getReceiver_telephone());
							orderMap.put("Receiver_mobile", order.getReceiver_mobile());
							orderMap.put("Transport", order.getTransport());
							orderMap.put("Msg", order.getMsg());
							orderMap.put("InvoiceType", order.getInvoiceType());
						} catch (NullPointerException e) {
							// TODO Auto-generated catch block
							orderMap.put("Msg", "");
							orderMap.put("InvoiceType", "");
						}
						map.put("orderinfo", orderMap);
						result = new Result(0, "Successfully", map);
					} else {
						result = new Result(1, "The order does not exist");
					}
				} else {
					result = new Result(1, "The order does not exist");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "物流跟踪查询", value = "/buyer/query_ship.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "query_ship.json", method = RequestMethod.GET)
	public void query_ship(HttpServletRequest request, HttpServletResponse response, String id) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		TransInfo info = this.query_ship_getData(id);
		map.put("status", info.getStatus());
		if (CommUtil.null2Int(info.getStatus()) == 0) {
			result = new Result(1, "暂无物流信息");
		}
		if (CommUtil.null2Int(info.getStatus()) == 2) {
			result = new Result(1, "快递公司网络异常，请稍后查询...");
		}
		if (CommUtil.null2Int(info.getStatus()) == 1) {
			List<Map<String, Object>> transinfoList = new ArrayList<Map<String, Object>>();
			for (TransContent transinfo : info.getData()) {
				Map<String, Object> transinfoMap = new HashMap<String, Object>();
				transinfoMap.put("time", transinfo.getTime());
				transinfoMap.put("conten", CommUtil.substring(transinfo.getContext(), 18));
				transinfoList.add(transinfoMap);
			}
			map.put("transinfo", transinfoList);
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int eva_rate(String rate) {
		int score = 0;
		if (rate.equals("a")) {
			score = 1;
		} else if (rate.equals("b")) {
			score = 2;
		} else if (rate.equals("c")) {
			score = 3;
		} else if (rate.equals("d")) {
			score = 4;
		} else if (rate.equals("e")) {
			score = 5;
		}
		return score;
	}

	private int eva_total_rate(String rate) {
		int score = 0;
		if (rate.equals("a")) {
			score = 1;
		} else if (rate.equals("b")) {
			score = 2;
		} else if (rate.equals("c")) {
			score = 3;
		} else if (rate.equals("d")) {
			score = 4;
		} else if (rate.equals("e")) {
			score = 5;
		}
		return score;
	}

	@SecurityMapping(title = "买家退货申请列表", value = "/buyer/order_return_list.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_return_list.json", method = RequestMethod.GET)
	public void order_return_list(HttpServletRequest request, HttpServletResponse response, String currentPage,
			String order_id, String token, String language) {
		ModelAndView mv = new JModelAndView("user/default/usercenter/order_return_list.html",
				configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request, response);
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				Map<String, Object> returm_map = new HashMap<String, Object>();
				OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, mv, "addTime", "desc");
				ofqo.addQuery("obj.user_id", new SysMap("user_id", user.getId().toString()), "=");
				ofqo.addQuery("obj.order_main", new SysMap("order_main", 1), "=");
				if (!CommUtil.null2String(order_id).equals("")) {
					ofqo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id + "%"), "like");
				}
				ofqo.addQuery("obj.order_cat", new SysMap("order_cat", 2), "!=");
				ofqo.addQuery("obj.order_status", new SysMap("order_status", 40), ">=");
				ofqo.addQuery("obj.return_shipTime", new SysMap("return_shipTime", new Date()), ">=");
				IPageList pList = this.orderFormService.list(ofqo);
				List<OrderForm> orderForms = pList.getResult();
				List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
				for (OrderForm orderForm : orderForms) {
					Map<String, Object> orderform = new HashMap<String, Object>();
					orderform.put("id", orderForm.getId());
					orderform.put("order_id", orderForm.getOrder_id());
					orderform.put("order_ship_price", orderForm.getShip_price());
					orderform.put("add_time", orderForm.getAddTime());
					orderform.put("order_status", orderForm.getOrder_status());
					orderform.put("order_goods_amount", orderForm.getGoods_amount());
					orderform.put("total_price", orderForm.getTotalPrice());
					orderform.put("ship_price", orderForm.getShip_price());
					Store obj = this.storeService.getObjById(CommUtil.null2Long(orderForm.getStore_id()));
					orderform.put("store_id", obj.getId());
					orderform.put("store_name", obj.getStore_name());
					orderform
							.put("store_logo",
									obj.getStore_logo() == null ? ""
											: this.configService.getSysConfig().getImageWebServer() + "/"
													+ obj.getStore_logo().getPath() + "/"
													+ obj.getStore_logo().getName());
					List<Map> goodsinfoList = orderFormTools.queryGoodsInfo(orderForm.getGoods_info());
					List<Map> goodsList = new ArrayList<Map>();
					for (Map goods : goodsinfoList) {
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						goodsMap.put("goods_id", goods.get("goods_id"));
						goodsMap.put("goods_name", goods.get("goods_name"));
						if ("1".equals(language)) {
							goodsMap.put("goods_name",
									goods.get("ksa_goods_name") != null
											&& !"".equals(goods.get("ksa_goods_name").toString())
													? goods.get("ksa_goods_name") : goods.get("goods_name"));
						}
						goodsMap.put("goods_count", goods.get("goods_count"));
						goodsMap.put("goods_price", goods.get("goods_price"));
						goodsMap.put("goods_all_price", goods.get("goods_all_price"));
						goodsMap.put("goods_mainphoto_path", this.configService.getSysConfig().getImageWebServer() + "/"
								+ goods.get("goods_mainphoto_path"));
						goodsMap.put("goods_return_status", goods.get("goods_return_status"));
						goodsMap.put("goods_choice_type", goods.get("goods_choice_type"));
						goodsMap.put("goods_gsp_ids", goods.get("goods_gsp_ids"));
						goodsList.add(goodsMap);
					}
					orderform.put("goodsinfo_list", goodsList);
					List<Map> childGoodsInfoList = this.orderFormTools
							.queryGoodsInfo(orderForm.getChild_order_detail());
					List<Map> chilGoodsList = new ArrayList<Map>();
					if (!childGoodsInfoList.isEmpty()) {
						for (Map childOrder : childGoodsInfoList) {
							Map childOrderMap = new HashMap();
							OrderForm childOrderForm = this.orderFormService
									.getObjById(CommUtil.null2Long(childOrder.get("order_id")));
							childOrderMap.put("order_id", childOrder.get("order_id"));
							childOrderMap.put("total_price", childOrderForm.getTotalPrice());
							Store store = this.storeService
									.getObjById(CommUtil.null2Long(childOrderForm.getStore_id()));
							childOrderMap.put("store_name", store == null ? "" : store.getStore_name());
							childOrderMap.put("store_id", store == null ? "" : store.getId());
							orderform.put("store_logo", this.configService.getSysConfig().getImageWebServer() + "/"
									+ store.getStore_logo().getPath() + "/" + store.getStore_logo().getName());
							String goods_json = ((String) childOrder.get("order_goods_info")).replaceAll("////", "");
							List<Map> chilld_goodsinfos = this.orderFormTools.queryGoodsInfo(goods_json);
							if (!chilld_goodsinfos.isEmpty()) {
								List<Map> childgoodsinfolist = new ArrayList<Map>();
								for (Map child_goodsinfo : chilld_goodsinfos) {
									Map child_goodsmap = new HashMap();
									child_goodsmap.put("goods_id", child_goodsinfo.get("goods_id"));
									child_goodsmap.put("goods_name", child_goodsinfo.get("goods_name"));
									child_goodsmap.put("goods_count", child_goodsinfo.get("goods_count"));
									child_goodsmap.put("goods_price", child_goodsinfo.get("goods_price"));
									child_goodsmap.put("goods_return_status",
											child_goodsinfo.get("goods_return_status"));
									child_goodsmap.put("goods_mainphoto_path",
											this.configService.getSysConfig().getImageWebServer() + "/"
													+ child_goodsinfo.get("goods_mainphoto_path"));
									childgoodsinfolist.add(child_goodsmap);
								}
								childOrderMap.put("child_goodsinfo", childgoodsinfolist);
								chilGoodsList.add(childOrderMap);
							}
						}
						orderform.put("childOrderList", chilGoodsList);
					}
					returnList.add(orderform);
				}
				returm_map.put("return_list", returnList);
				returm_map.put("pageSize", pList.getPageSize());
				result = new Result(0, "Successfully", returnList);
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "买家退货申请", value = "/buyer/order_return_apply.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_return_apply.json", method = RequestMethod.POST)
	public void order_return_apply(HttpServletRequest request, HttpServletResponse response, String id, String oid,
			String goods_gsp_ids, String token) {
		Map return_apply_map = new HashMap();
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				ModelAndView mv = new JModelAndView("user/default/usercenter/order_return_apply.html",
						configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request, response);
				OrderForm orderForm = this.orderFormService.getObjById(CommUtil.null2Long(oid));
				if (orderForm != null) {
					if (orderForm.getUser_id().equals(user.getId().toString())) {
						List<Map> maps = this.orderFormTools.queryGoodsInfo(orderForm.getGoods_info());
						Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
						if (obj != null) {
							for (Map goods : maps) {
								if (CommUtil.null2String(goods.get("goods_id")).equals(id)) {
									List<Map> count_list = new ArrayList<Map>();
									return_apply_map.put("return_count", goods.get("goods_count"));
									Map map = new HashMap();
									map.put("goods_id", obj.getId());
									map.put("goods_name", obj.getGoods_name());
									// goodsinfo_map.put("goods_name",
									// goods.getGoods_current_price());
									map.put("goods_price", obj.getGoods_current_price());
									map.put("goods_type", obj.getGoods_type());
									map.put("goods_photo",
											this.configService.getSysConfig().getImageWebServer() + "/"
													+ obj.getGoods_main_photo().getPath() + "/"
													+ obj.getGoods_main_photo().getName());
									return_apply_map.put("goodsinfo_map", map);
									if (CommUtil.null2String(goods.get("goods_return_status"))// [商品退货状态
																								// 当有此字段为“”时可退货状态
																								// 5为已申请退货，6商家已同意退货，7用户填写退货物流，8商家已确认，提交平台已退款
																								// -1已经超出可退货时间]
											.equals("5")) {
										return_apply_map.put("view", true);
										List<Map> returnMaps = this.orderFormTools
												.queryGoodsInfo(orderForm.getReturn_goods_info());
										for (Map returnMap : returnMaps) {
											if (CommUtil.null2String(returnMap.get("return_goods_id")).equals(id)) {
												return_apply_map.put("return_content",
														returnMap.get("return_goods_content"));
											}
										}
									}
								}
							}
							return_apply_map.put("order_id", oid);
							result = new Result(0, "Successfully", return_apply_map);
						} else {
							result = new Result(1, "The merchandise is off the shelves");
						}
					} else {
						result = new Result(1, "The order does not exist");
					}
				} else {
					result = new Result(1, "The order does not exist");
				}
				return_apply_map.put("goods_gsp_ids", goods_gsp_ids);
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "买家退货申请保存", value = "/buyer/order_return_apply_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_return_apply_save.json", method = RequestMethod.POST)
	public void order_return_apply_save(HttpServletRequest request, HttpServletResponse response, String id,
			String currentPage, String return_goods_content, String goods_id, String return_goods_count,
			String goods_gsp_ids, String token) throws Exception {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				List<Goods> goods_list = this.orderFormTools.queryOfGoods(obj.getId().toString());
				Goods goods = null;
				for (Goods g : goods_list) {
					if (g.getId().toString().equals(goods_id)) {
						goods = g;
					}
				}
				if (obj != null && obj.getUser_id().equals(user.getId().toString()) && goods != null) {
					List<Map> list = new ArrayList<Map>();
					Map json = new HashMap();
					json.put("return_goods_id", goods.getId());
					json.put("return_goods_content", CommUtil.filterHTML(return_goods_content));
					json.put("return_goods_count", return_goods_count);
					json.put("return_goods_price", goods.getStore_price());
					json.put("return_goods_commission_rate", goods.getGc().getCommission_rate());
					json.put("return_order_id", id);
					list.add(json);
					obj.setReturn_goods_info(Json.toJson(list, JsonFormat.compact()));
					List<Map> maps = this.orderFormTools.queryGoodsInfo(obj.getGoods_info());
					List<Map> new_maps = new ArrayList<Map>();
					Map gls = new HashMap();
					for (Map m : maps) {
						if (m.get("goods_id").toString().equals(goods_id)
								&& goods_gsp_ids.equals(m.get("goods_gsp_ids").toString())) {
							m.put("goods_return_status", 5);
							gls.putAll(m);
						}
					}
					obj.setGoods_info(Json.toJson(maps, JsonFormat.compact()));
					this.orderFormService.update(obj);

					// 生成退货日志
					ReturnGoodsLog rlog = new ReturnGoodsLog();
					rlog.setReturn_service_id("re" + user.getId() + CommUtil.formatTime("yyyyMMddHHmmss", new Date()));
					rlog.setUser_name(user.getUserName());
					rlog.setUser_id(user.getId());
					rlog.setReturn_content(CommUtil.filterHTML(return_goods_content));
					rlog.setGoods_all_price(gls.get("goods_all_price").toString());
					rlog.setGoods_count(gls.get("goods_count").toString());
					rlog.setGoods_id(CommUtil.null2Long(gls.get("goods_id").toString()));
					rlog.setGoods_mainphoto_path(gls.get("goods_mainphoto_path").toString());
					rlog.setGoods_commission_rate(
							BigDecimal.valueOf(CommUtil.null2Double(gls.get("goods_commission_rate"))));
					rlog.setGoods_name(gls.get("goods_name").toString());
					rlog.setGoods_price(gls.get("goods_price").toString());
					rlog.setGoods_return_status("5");
					rlog.setAddTime(new Date());
					rlog.setReturn_order_id(CommUtil.null2Long(id));
					rlog.setGoods_type(goods.getGoods_type());
					if (goods.getGoods_store() != null) {
						rlog.setStore_id(goods.getGoods_store().getId());
					}
					this.returnGoodsLogService.save(rlog);
					// 如果是收费接口，则通知快递100，建立订单物流查询推送
					if (this.configService.getSysConfig().getKuaidi_type() == 1) {
						TaskRequest req = new TaskRequest();
						Map express_map = Json.fromJson(Map.class, obj.getExpress_info());
						req.setCompany(CommUtil.null2String(express_map.get("express_company_mark")));
						String from_addr = "";
						req.setFrom(from_addr);
						req.setTo(obj.getReceiver_area());
						req.setNumber(obj.getShipCode());
						req.getParameters().put("callbackurl", CommUtil.getURL(request)
								+ "/kuaidi100_callback.htm?order_id=" + obj.getId() + "&orderType=1");
						req.getParameters().put("salt",
								Md5Encrypt.md5(CommUtil.null2String(obj.getId())).toLowerCase());
						req.setKey(this.configService.getSysConfig().getKuaidi_id2());

						HashMap<String, String> p = new HashMap<String, String>();
						p.put("schema", "json");
						p.put("param", JacksonHelper.toJSON(req));
						try {
							String ret = HttpRequest.postData("http://www.kuaidi100.com/poll", p, "UTF-8");
							TaskResponse resp = JacksonHelper.fromJSON(ret, TaskResponse.class);
							if (resp.getResult() == true) {
								System.out.println("订阅成功");
							} else {
								System.out.println("订阅失败");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (obj.getOrder_form() == 0) {
						User seller = this.userService.getObjById(
								this.storeService.getObjById(CommUtil.null2Long(obj.getStore_id())).getUser().getId());
						Map map = new HashMap();
						map.put("buyer_id", user.getId().toString());
						map.put("seller_id", seller.getId().toString());
						this.msgTools.sendEmailCharge(CommUtil.getURL(request),
								"email_toseller_order_return_apply_notify", seller.getEmail(), Json.toJson(map), null,
								obj.getStore_id());
						map.clear();
						map.put("buyer_id", user.getId().toString());
						map.put("seller_id", seller.getId().toString());
						this.msgTools.sendSmsCharge(CommUtil.getURL(request), "sms_toseller_order_return_apply_notify",
								seller.getMobile(), Json.toJson(map), null, obj.getStore_id());

					}
					result = new Result(0, "Successfully");
				} else {
					result = new Result(1, "The commodity does not exist");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "买家退货申请取消", value = "/buyer/order_return_apply_cancel.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_return_apply_cancel.json", method = RequestMethod.POST)
	public void order_return_apply_cancel(HttpServletRequest request, HttpServletResponse response, String id,
			String currentPage, String goods_id, String goods_gsp_ids, String token) {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
				List<Goods> goods_list = this.orderFormTools.queryOfGoods(obj.getId().toString());
				Goods goods = null;
				for (Goods g : goods_list) {
					if (g.getId().toString().equals(goods_id)) {
						goods = g;
					}
				}
				if (obj != null && obj.getUser_id().equals(user.getId().toString()) && goods != null) {
					obj.setReturn_goods_info("");
					List<Map> maps = this.orderFormTools.queryGoodsInfo(obj.getGoods_info());
					Map gls = new HashMap();
					for (Map m : maps) {
						if (m.get("goods_id").toString().equals(goods_id)
								&& goods_gsp_ids.equals(m.get("goods_gsp_ids").toString())) {
							m.put("goods_return_status", "");
							gls.putAll(m);
						}
					}
					obj.setGoods_info(Json.toJson(maps, JsonFormat.compact()));
					this.orderFormService.update(obj);
					Map map = new HashMap();
					map.put("goods_id", CommUtil.null2Long(gls.get("goods_id").toString()));
					map.put("return_order_id", CommUtil.null2Long(id));
					map.put("uid", user.getId());
					List<ReturnGoodsLog> objs = this.returnGoodsLogService.query(
							"select obj from ReturnGoodsLog obj where obj.goods_id=:goods_id and obj.return_order_id=:return_order_id and obj.user_id=:uid",
							map, -1, -1);
					for (ReturnGoodsLog rl : objs) {
						this.returnGoodsLogService.delete(rl.getId());
					}
				}
				result = new Result(0, "Successfully");
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ExpressCompany queryExpressCompany(String json) {
		ExpressCompany ec = null;
		if (json != null && !json.equals("")) {
			HashMap map = Json.fromJson(HashMap.class, json);
			ec = this.expressCompayService.getObjById(CommUtil.null2Long(map.get("express_company_id")));
		}
		return ec;
	}

	private TransInfo query_ship_getData(String id) {
		TransInfo info = new TransInfo();
		OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
		if (obj != null && !CommUtil.null2String(obj.getShipCode()).equals("")) {
			if (this.configService.getSysConfig().getKuaidi_type() == 0) {// 免费物流接口
				try {
					ExpressCompany ec = this.queryExpressCompany(obj.getExpress_info());
					String query_url = "http://api.kuaidi100.com/api?id="
							+ this.configService.getSysConfig().getKuaidi_id() + "&com="
							+ (ec != null ? ec.getCompany_mark() : "") + "&nu=" + obj.getShipCode()
							+ "&show=0&muti=1&order=asc";
					URL url = new URL(query_url);
					URLConnection con = url.openConnection();
					con.setAllowUserInteraction(false);
					InputStream urlStream = url.openStream();
					String type = con.guessContentTypeFromStream(urlStream);
					String charSet = null;
					if (type == null)
						type = con.getContentType();
					if (type == null || type.trim().length() == 0 || type.trim().indexOf("text/html") < 0)
						return info;
					if (type.indexOf("charset=") > 0)
						charSet = type.substring(type.indexOf("charset=") + 8);
					byte b[] = new byte[10000];
					int numRead = urlStream.read(b);
					String content = new String(b, 0, numRead, charSet);
					while (numRead != -1) {
						numRead = urlStream.read(b);
						if (numRead != -1) {
							// String newContent = new String(b, 0, numRead);
							String newContent = new String(b, 0, numRead, charSet);
							content += newContent;
						}
					}
					info = Json.fromJson(TransInfo.class, content);
					urlStream.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (this.configService.getSysConfig().getKuaidi_type() == 1) {// 收费物流接口
				ExpressInfo ei = this.expressInfoService.getObjByPropertyWithType("order_id", obj.getId(), 0);
				if (ei != null) {
					List<TransContent> data = (List<TransContent>) Json
							.fromJson(CommUtil.null2String(ei.getOrder_express_info()));
					info.setData(data);
					info.setStatus("1");
				}
			}
		}
		return info;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@SecurityMapping(title = "买家退货填写物流", value = "/buyer/order_returnlog_view.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_returnlog_view.json", method = RequestMethod.POST)
	public void order_returnlog_view(HttpServletRequest request, HttpServletResponse response, String id,
			String token) {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				Map map = new HashMap();
				ModelAndView mv = new JModelAndView("user/default/usercenter/order_returnlog_view.html",
						configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request, response);
				ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil.null2Long(id));
				if (obj.getUser_id().equals(user.getId())) {
					if (obj.getGoods_return_status().equals("6") || obj.getGoods_return_status().equals("7")) {
						params.clear();
						params.put("status", 0);
						List<ExpressCompany> expressCompanys = this.expressCompayService.query(
								"select obj from ExpressCompany obj where obj.company_status=:status order by company_sequence asc",
								params, -1, -1);

						map.put("order_id", obj.getId());
						map.put("order_return_service_id", obj.getReturn_service_id());
						map.put("order_goods_name", obj.getGoods_name());
						map.put("order_self_address", obj.getSelf_address());
						map.put("order_return_content", obj.getReturn_content());
						map.put("order_express_code", obj.getExpress_code());
						if (CommUtil.null2Int(obj.getGoods_return_status()) == 6) {
							List<Map> exp_list = new ArrayList<Map>();
							for (ExpressCompany expressCommpany : expressCompanys) {
								Map exp_map = new HashMap();
								exp_map.put("exp_id", expressCommpany.getId());
								exp_map.put("exp_name", expressCommpany.getCompany_name());
								exp_list.add(exp_map);
							}
							map.put("exp_list", exp_list);
						}

						OrderForm of = this.orderFormService.getObjById(obj.getReturn_order_id());
						map.put("order_return_shipTime",
								CommUtil.formatTime("MM/dd/yyyy HH:mm:ss", of.getReturn_shipTime()));

						Goods goods = this.goodsService.getObjById(obj.getGoods_id());
						if (goods.getGoods_type() == 1) {
							map.put("store_name", goods.getGoods_store().getStore_name());
							map.put("store_id", goods.getGoods_store().getId());
						} else {
							map.put("store_name", "平台商");
						}

						if (obj.getGoods_return_status().equals("7")) {
							Map goods_return_status_map = new HashMap();
							goods_return_status_map.put("return_order_id", obj.getReturn_order_id());

							TransInfo transInfo = this.query_Returnship_getData(CommUtil.null2String(obj.getId()));
							if (CommUtil.null2Int(transInfo.getStatus()) == 1) {
								List<Map> info_content_list = new ArrayList<Map>();
								for (TransContent info : transInfo.getData()) {
									Map info_content = new HashMap();
									if (transInfo.getData().size() > 0) {
										info_content.put("time", info.getTime());
										info_content.put("content", info.getContext());
										info_content_list.add(info_content);
									} else {
										info_content.put("time", info.getTime());
										info_content.put("content", info.getContext());
										info_content_list.add(info_content);
									}
								}
								goods_return_status_map.put("info_content_list", info_content_list);
							}

							if (CommUtil.null2Int(transInfo.getStatus()) == 0) {
								goods_return_status_map.put("message", transInfo.getMessage());

							}

							if (CommUtil.null2Int(transInfo.getStatus()) == 2) {
								goods_return_status_map.put("message", transInfo.getMessage());
							}
							Map express = Json.fromJson(HashMap.class, obj.getReturn_express_info());
							goods_return_status_map.put("express_company_name", express.get("express_company_name"));
							map.put("goods_return_status_map", goods_return_status_map);
						}
						result = new Result(0, "Successfully", map);
					} else {
						result = new Result(100, "当前状态无法对退货服务单进行操作");
					}
				} else {
					result = new Result(300, "error", "您没有订单号为" + obj.getReturn_service_id() + "的退货单号");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private TransInfo query_Returnship_getData(String id) {
		TransInfo info = new TransInfo();
		ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil.null2Long(id));
		if (this.configService.getSysConfig().getKuaidi_type() == 0) {// 免费物流接口
			try {
				ExpressCompany ec = this.queryExpressCompany(obj.getReturn_express_info());
				String query_url = "http://api.kuaidi100.com/api?id=" + this.configService.getSysConfig().getKuaidi_id()
						+ "&com=" + (ec != null ? ec.getCompany_mark() : "") + "&nu=" + obj.getExpress_code()
						+ "&show=0&muti=1&order=asc";
				URL url = new URL(query_url);
				URLConnection con = url.openConnection();
				con.setAllowUserInteraction(false);
				InputStream urlStream = url.openStream();
				String type = con.guessContentTypeFromStream(urlStream);
				String charSet = null;
				if (type == null)
					type = con.getContentType();
				if (type == null || type.trim().length() == 0 || type.trim().indexOf("text/html") < 0)
					return info;
				if (type.indexOf("charset=") > 0)
					charSet = type.substring(type.indexOf("charset=") + 8);
				byte b[] = new byte[10000];
				int numRead = urlStream.read(b);
				String content = new String(b, 0, numRead, charSet);
				while (numRead != -1) {
					numRead = urlStream.read(b);
					if (numRead != -1) {
						// String newContent = new String(b, 0, numRead);
						String newContent = new String(b, 0, numRead, charSet);
						content += newContent;
					}
				}
				info = Json.fromJson(TransInfo.class, content);
				urlStream.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (this.configService.getSysConfig().getKuaidi_type() == 1) {// 收费物流接口
			ExpressInfo ei = this.expressInfoService.getObjByPropertyWithType("order_id", obj.getId(), 1);
			if (ei != null) {
				List<TransContent> data = (List<TransContent>) Json.fromJson(ei.getOrder_express_info());
				info.setData(data);
				info.setStatus("1");
			}
		}
		return info;
	}

	@SecurityMapping(title = "买家退货申请列表记录", value = "/buyer/order_return_listlog.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_return_listlog.json", method = RequestMethod.GET)
	public void order_return_listlog(HttpServletRequest request, HttpServletResponse response, String currentPage,
			String token) {
		Map map = new HashMap();
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users = this.userService.query(
					"select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if (users.isEmpty()) {
				result = new Result(-100, "token Invalidation");
			} else {
				User user = users.get(0);
				String imageWebServer = this.configService.getSysConfig().getImageWebServer();
				ModelAndView mv = new JModelAndView("user/default/usercenter/order_return_listlog.html",
						configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request, response);
				ReturnGoodsLogQueryObject qo = new ReturnGoodsLogQueryObject(currentPage, mv, "addTime", "desc");
				qo.addQuery("obj.user_id", new SysMap("user_id", user.getId()), "=");
				qo.addQuery("obj.goods_return_status", new SysMap("goods_return_status", "1"), "!=");
				IPageList pList = this.returnGoodsLogService.list(qo);
				List<ReturnGoodsLog> returnGoods = pList.getResult();
				List<Map> maps = new ArrayList<Map>();
				if (returnGoods.size() > 0) {
					for (ReturnGoodsLog goods : returnGoods) {
						Map goodsMap = new HashMap();
						goodsMap.put("return_id", goods.getId());
						goodsMap.put("return_service_id", goods.getReturn_service_id());
						goodsMap.put("return_goods_count", goods.getGoods_count());
						goodsMap.put("return_goods_all_price", goods.getGoods_all_price());
						goodsMap.put("refund_status", goods.getRefund_status());
						goodsMap.put("goods_return_status", goods.getGoods_return_status());
						Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods.getGoods_id()));
						Map objMap = new HashMap();
						objMap.put("goods_id", obj.getId());
						objMap.put("goods_name", obj.getGoods_name());
						objMap.put("goods_photo",
								obj.getGoods_main_photo() == null ? ""
										: imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
												+ obj.getGoods_main_photo().getName());
						objMap.put("goods_price", obj.getGoods_price());
						objMap.put("goods_curren_price", obj.getGoods_current_price());
						if (obj.getGoods_store() != null) {
							Store store = this.storeService
									.getObjById(CommUtil.null2Long(obj.getGoods_store().getId()));
							objMap.put("store_id", store.getId());
							objMap.put("store_name", store.getStore_name());
							try {
								objMap.put("store_logo",
										store.getStore_logo() == null ? ""
												: imageWebServer + "/" + store.getStore_logo().getPath() + "/"
														+ store.getStore_logo().getName());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								objMap.put("store_logo", "");
							}
						} else {
							objMap.put("store_id", "");
							objMap.put("store_name", "");

						}
						goodsMap.put("goods_info", objMap);
						maps.add(goodsMap);
						map.put("returnGoods", maps);
						map.put("goods_num", returnGoods.size());
					}

					map.put("pages", pList.getPageSize());
				}
				result = new Result(0, "Successfully", map);
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "买家退货物流信息保存", value = "/buyer/order_return_ship_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "order_return_ship_save.json", method = RequestMethod.POST)
	public void order_return_ship_save(HttpServletRequest request, HttpServletResponse response, String id,
			String currentPage, String express_id, String express_code, String token) {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users = this.userService.query(
					"select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if (users.isEmpty()) {
				result = new Result(-100, "token Invalidation");
			} else {
				User user = users.get(0);
				ModelAndView mv = new JModelAndView("success.html", configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request, response);
				ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil.null2Long(id));
				if (obj != null && obj.getUser_id().equals(user.getId())) {
					ExpressCompany ec = this.expressCompayService.getObjById(CommUtil.null2Long(express_id));
					Map json_map = new HashMap();
					json_map.put("express_company_id", ec.getId());
					json_map.put("express_company_name", ec.getCompany_name());
					json_map.put("express_company_mark", ec.getCompany_mark());
					json_map.put("express_company_type", ec.getCompany_type());
					String express_json = Json.toJson(json_map);
					obj.setReturn_express_info(express_json);
					obj.setExpress_code(express_code);
					obj.setGoods_return_status("7");
					this.returnGoodsLogService.update(obj);

					/*
					 * mv.addObject("op_title", "保存退货物流成功！");
					 * mv.addObject("url", CommUtil.getURL(request) +
					 * "/buyer/order_return_listlog.htm");
					 */
					OrderForm return_of = this.orderFormService.getObjById(obj.getReturn_order_id());
					List<Map> maps = this.orderFormTools.queryGoodsInfo(return_of.getGoods_info());
					List<Map> new_maps = new ArrayList<Map>();
					Map gls = new HashMap();
					for (Map m : maps) {
						if (m.get("goods_id").toString().equals(CommUtil.null2String(obj.getGoods_id()))) {
							m.put("goods_return_status", 7);
							gls.putAll(m);
						}
						new_maps.add(m);
					}
					return_of.setGoods_info(Json.toJson(new_maps));
					this.orderFormService.update(return_of);
					result = new Result(0, "Successfully");
				} else {
					result = new Result(1, "您没有为" + obj.getReturn_service_id() + "的退货服务号");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param file
	 * @param order_status
	 * @param column
	 * @descript 订单状态修改
	 */
	@RequestMapping(value = "editOrderStatus.json", method = RequestMethod.POST)
	public void brushBatch(HttpServletRequest request, HttpServletResponse response, MultipartFile file,
			String order_status, @RequestParam(value = "column", required = true, defaultValue = "1") Integer column) {
		int code = -1;
		String msg = "";
		InputStream input = null;
		HSSFWorkbook wb = null;
		List<String> list = new ArrayList<String>();
		if (null != file) {
			try {
				input = file.getInputStream();
				wb = new HSSFWorkbook(input);
				HSSFSheet sheet1 = wb.getSheet("Sheet1");
				for (int i = 1; i < sheet1.getPhysicalNumberOfRows(); i++) {
					List list1 = new ArrayList();
					HSSFRow row = sheet1.getRow(i);
					for (int j = 0; j < row.getLastCellNum(); j++) {
						// map.put(sheet1.getRow(1).getCell(j), row.getCell(j));
						if (j == column) {
							list.add(CommUtil.null2String(row.getCell(j)));
							break;
						}
					}

				}
				Map params = new HashMap();
				params.put("order_id", list);
				List<OrderForm> orderForms = this.orderFormService
						.query("SELECT obj FROM OrderForm obj WHERE obj.order_id in (:order_id)", params, -1, -1);
				for (OrderForm order : orderForms) {
					order.setOrder_status(Integer.parseInt(order_status));
					this.orderFormService.update(order);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			code = 4403;
			msg = "The file is empty";
		}
		try {
			response.getWriter().print(list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
