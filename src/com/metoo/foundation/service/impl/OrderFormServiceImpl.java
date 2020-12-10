package com.metoo.foundation.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.OrderFormQueryObject;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;

@Service
@Transactional
public class OrderFormServiceImpl implements IOrderFormService {
	@Resource(name = "orderFormDAO")
	private IGenericDAO<OrderForm> orderFormDao;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IStoreService storeService;

	public boolean save(OrderForm orderForm) {
		/**
		 * init other field here
		 */
		try {
			this.orderFormDao.save(orderForm);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public OrderForm getObjById(Long id) {
		OrderForm orderForm = this.orderFormDao.get(id);
		if (orderForm != null) {
			return orderForm;
		}
		return null;
	}

	public boolean delete(Long id) {
		try {
			this.orderFormDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean batchDelete(List<Serializable> orderFormIds) {
		// TODO Auto-generated method stub
		for (Serializable id : orderFormIds) {
			delete((Long) id);
		}
		return true;
	}

	public IPageList list(IQueryObject properties) {
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(OrderForm.class,construct, query,
				params, this.orderFormDao);
		if (properties != null) {
			PageObject pageObj = properties.getPageObj();
			if (pageObj != null)
				pList.doList(pageObj.getCurrentPage() == null ? 0 : pageObj
						.getCurrentPage(), pageObj.getPageSize() == null ? 0
						: pageObj.getPageSize());
		} else
			pList.doList(0, -1);
		return pList;
	}

	public boolean update(OrderForm orderForm) {
		try {
			this.orderFormDao.update(orderForm);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<OrderForm> query(String query, Map params, int begin, int max) {
		return this.orderFormDao.query(query, params, begin, max);

	}

	@Override
	public List  queryFromOrderForm(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.orderFormDao.query(query, params, begin, max);
	}

	public String orderMain(HttpServletRequest request, HttpServletResponse response, String currentPage,
			String order_id, String beginTime, String endTime, String order_status, String token, String name,
			String language) {
		Map<String, Object> map = new HashMap<String, Object>();
		Result result = null;
		ModelAndView mv = new JModelAndView("user/default/usercenter/buyer_order.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, mv, "addTime", "desc");
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				ofqo.addQuery("obj.user_id", new SysMap("user_id", user.getId().toString()), "=");
				ofqo.addQuery("obj.order_main", new SysMap("order_main", 1), "=");// 只显示主订单,通过主订单完成子订单的加载[是否为主订单，1为主订单，主订单用在买家用户中心显示订单内容]
				// ofqo.addQuery("obj.order_cat", new SysMap("order_cat", 0),
				// "=");//[ 订单分类，0为购物订单，1为手机充值订单 2为生活类团购订单 3为商品类团购订单 4旅游报名订单]
				List<Integer> order_cart = new ArrayList<Integer>();
				order_cart.add(5);
				order_cart.add(0);
				ofqo.addQuery("obj.order_cat", new SysMap("order_cat", order_cart), "in");
				ofqo.setPageSize(20);// 设定分页查询，每页24件商品
				if (!CommUtil.null2String(order_id).equals("")) {
					ofqo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id + "%"), "like");
				}
				if (!CommUtil.null2String(beginTime).equals("")) {
					ofqo.addQuery("obj.addTime", new SysMap("beginTime", CommUtil.formatDate(beginTime)), ">=");
				}
				if (!CommUtil.null2String(endTime).equals("")) {
					String ends = endTime + " 23:59:59";
					ofqo.addQuery("obj.addTime",
							new SysMap("endTime", CommUtil.formatDate(ends, "yyyy-MM-dd hh:mm:ss")), "<=");
				}
				String or_status = null;
				if (!CommUtil.null2String(order_status).equals("")) {
					if (order_status.equals("order_submit")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 10), "=");
						or_status = "10";
					}
					if (order_status.equals("order_pay")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 20), "=");
						or_status = "20";
					}
					if (order_status.equals("order_shipping")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 30), "=");
						or_status = "30";
					}
					if (order_status.equals("payOnDelivery")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 16), "=");
						or_status = "16";
					}
					if (order_status.equals("order_receive")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 40), "=");
						or_status = "40";
					}
					if (order_status.equals("order_finish")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 50), "=");
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 65), "=");
						or_status = "50";
					}
					if (order_status.equals("order_cancel")) {
						ofqo.addQuery("obj.order_status", new SysMap("order_status", 0), "=");
						or_status = "0";
					}
				} else {
					ofqo.addQuery("obj.order_status", new SysMap("order_status", 90), "!=");
				}

				map.put("order_status", order_status);
				map.put("imageWebServer", this.configService.getSysConfig().getImageWebServer());
				IPageList pList = this.list(ofqo);
				List<OrderForm> orders = pList.getResult();
				List orderList = new ArrayList();
				for (OrderForm order : orders) {
					boolean flag = true;
					if (!CommUtil.null2String(name).equals("")) {
						flag = this.orderFormTools.verifyGoodsName(order, name);
					}
					if (flag) {
						Map orderMap = new HashMap();
						Store store = this.storeService.getObjById(CommUtil.null2Long(order.getStore_id()));
						orderMap.put("sotre_id", store.getId());
						orderMap.put("sotre_name", store.getStore_name());
						orderMap.put("sotre_logo", store.getStore_logo() != null
								? store.getStore_logo().getPath() + "/" + store.getStore_logo().getName() : "");
						orderMap.put("goods", this.orderFormTools.queryGoodsInfo(order.getGoods_info()));
						orderMap.put("order_id", order.getId());
						orderMap.put("order_number", order.getOrder_id());
						orderMap.put("order_status", order.getOrder_status());
						orderMap.put("payment_amount", order.getPayment_amount());
/*						List childOrderList = new ArrayList();
						for (Map childOrder : this.orderFormTools.queryGoodsInfo(order.getChild_order_detail())) {
							Map childMap = new HashMap();
							childMap.put("order_id", childOrder.get("order_id"));
							childMap.put("store_id", childOrder.get("store_id"));
							childMap.put("store_name", childOrder.get("store_name"));
							childMap.put("store_logo", childOrder.get("store_logo"));
							childMap.put("goods",
									this.orderFormTools.queryGoodsInfo(childOrder.get("order_goods_info").toString()));
							childOrderList.add(childMap);
						}*/
						orderMap.put("childOrder", this.orderFormTools.queryGoodsInfo(order.getChild_order_detail()));
						orderList.add(orderMap);
					}
				}
				map.put("orderlist", orderList);
				int[] status = new int[] { 0, 10, 16, 30, 40 };
				String[] string_status = new String[] { "order_cencel", "order_submit", "order_pending",
						"order_shipping", "order_finish" };
				Map<String, Object> orders_status = new LinkedHashMap<String, Object>();
				List<Map<String, Object>> statusmaps = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < status.length; i++) {
					Map statusmap = new LinkedHashMap();
					int size = this.query("select obj.id from OrderForm obj where obj.user_id="
							+ user.getId().toString() + " and obj.order_status =" + status[i] + "", null, -1, -1)
							.size();
					statusmap.put("order_size_" + status[i], size);
					statusmap.put(string_status[i], size);
					statusmaps.add(statusmap);
				}
				map.put("orderstatus", statusmaps);
				map.put("order_Pages", pList.getPages());
				result = new Result(0, "Successfully", map);
			}
		}
		return Json.toJson(result, JsonFormat.compact());
	}
}
