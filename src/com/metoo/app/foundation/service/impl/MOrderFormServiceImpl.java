package com.metoo.app.foundation.service.impl;

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
import com.metoo.app.foundation.service.MIGoodsService;
import com.metoo.app.foundation.service.MIOrderFormService;
import com.metoo.app.foundation.service.MIUserService;
import com.metoo.app.manage.admin.tool.MOrderFormTool;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.OrderFormQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsReturnService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IGroupInfoService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IPaymentService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IPredepositLogService;
import com.metoo.foundation.service.IReturnGoodsLogService;
import com.metoo.foundation.service.IStorePointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.kuaidi100.service.IExpressInfoService;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.buyer.tools.ShipTools;
import com.metoo.msg.MsgTools;
import com.metoo.view.web.tools.GoodsViewTools;

@Service
@Transactional
public class MOrderFormServiceImpl implements MIOrderFormService {

	@Resource(name = "orderFormMetooDAO")
	private IGenericDAO<OrderForm> orderFormMetooDao;

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private MIUserService userMetooService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private IStorePointService storePointService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IGroupInfoService groupinfoService;
	@Autowired
	private IGoodsReturnService goodsReturnService;
	@Autowired
	private IExpressCompanyService expressCompayService;
	@Autowired
	private IGroupGoodsService ggService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private MOrderFormTool orderFormMetooTools;
	@Autowired
	private IPayoffLogService payoffLogservice;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private MIGoodsService goodsMetooService;
	@Autowired
	private IReturnGoodsLogService returnGoodsLogService;
	@Autowired
	private ShipTools shipTools;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IExpressInfoService expressInfoService;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;

	@Override
	public OrderForm getObjById(Long id) {
		// TODO Auto-generated method stub
		OrderForm orderForm = this.orderFormMetooDao.get(id);
		if (orderForm != null) {
			return orderForm;
		}
		return null;
	}

	@Override
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(OrderForm.class, construct, query, params, this.orderFormMetooDao);
		if (properties != null) {
			PageObject pageObj = properties.getPageObj();
			if (pageObj != null)
				pList.doList(pageObj.getCurrentPage() == null ? 0 : pageObj.getCurrentPage(),
						pageObj.getPageSize() == null ? 0 : pageObj.getPageSize());
		} else
			pList.doList(0, -1);
		return pList;
	}

	public List<OrderForm> query(String query, Map params, int begin, int max) {
		return this.orderFormMetooDao.query(query, params, begin, max);

	}

	// 多条件查询
	public String order(HttpServletRequest request, HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String order_status, String token, String name, String language) {
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
				// ofqo.addQuery("obj.order_main", new SysMap("order_main", 1),
				// "=");// 只显示主订单,通过主订单完成子订单的加载[是否为主订单，1为主订单，主订单用在买家用户中心显示订单内容]
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
				IPageList pList = this.list(ofqo);
				List<OrderForm> orders = pList.getResult();
				List<Map<String, Object>> orderList = this.order(orders, name, language);
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
						List childOrderList = new ArrayList();
						for (Map childOrder : this.orderFormTools.queryGoodsInfo(order.getChild_order_detail())) {
							Map childMap = new HashMap();
							childMap.put("order_id", childOrder.get("order_id"));
							childMap.put("store_id", childOrder.get("store_id"));
							childMap.put("store_name", childOrder.get("store_name"));
							childMap.put("store_logo", childOrder.get("store_logo"));
							childMap.put("goods",
									this.orderFormTools.queryGoodsInfo(childOrder.get("order_goods_info").toString()));
							childOrderList.add(childMap);
						}
						orderMap.put("childOrder", childOrderList);
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

	public List<Map<String, Object>> order(List<OrderForm> orders, String name, String language) {
		List<Map<String, Object>> orderlist = new ArrayList<Map<String, Object>>();
		for (OrderForm order : orders) {
			Map<String, Object> ordermap = new HashMap<String, Object>();
			int main_count = 0;// 主订单商品总数
			int child_count = 0;// 子订单商品总数
			Map<String, Object> mianOrderMap = new HashMap<String, Object>();
			List<Map<String, Object>> goodsList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> main_goods_list = new ArrayList<Map<String, Object>>();
			List main_count_list = new ArrayList();
			List<Map> goods_info = this.orderFormTools.queryGoodsInfo(order.getGoods_info());
			boolean flag = true;
			if (name != null && !name.equals("")) {
				flag = this.orderFormMetooTools.indexof(goods_info, name);
			}
			if (flag) {
				for (Map obj : goods_info) {
					Map<String, Object> goodsMap = new HashMap<String, Object>();
					goodsMap.put("goods_id", obj.get("goods_id"));
					goodsMap.put("goods_name", obj.get("goods_name"));
					if ("1".equals(language)) {
						goodsMap.put("goods_name",
								obj.get("ksa_goods_name") != null && !"".equals(obj.get("ksa_goods_name").toString())
										? "^" + obj.get("ksa_goods_name") : obj.get("goods_name"));
					}
					goodsMap.put("goods_count", obj.get("goods_count"));
					goodsMap.put("goods_amount", obj.get("goods_amount"));
					goodsMap.put("goods_price", obj.get("goods_price"));
					goodsMap.put("goods_current_price", obj.get("goods_current_price"));
					goodsMap.put("goods_gsp_val", obj.get("goods_gsp_val"));
					goodsMap.put("goods_color", obj.get("goods_color"));
					Goods goods = this.goodsService.getObjById(CommUtil.null2Long(obj.get("goods_id")));
					goodsMap.put("free_gifts", goods.getPoint() == 1 && goods.getPoint_status() == 10 ? 1 : 0);
					goodsMap.put("evaluate", obj.get("evaluate"));
					goodsMap.put("goods_mainphoto_path", this.configService.getSysConfig().getImageWebServer() + "/"
							+ obj.get("goods_mainphoto_path"));
					main_count += Integer.parseInt(obj.get("goods_count").toString());
					goodsList.add(goodsMap);
					mianOrderMap.put("main_count", main_count);
				}
				if (goodsList.size() > 0) {
					mianOrderMap.put("order_id", order.getId());
					mianOrderMap.put("order_platform_ship_price", order.getPlatform_ship_price());
					mianOrderMap.put("order_num", order.getOrder_id());
					mianOrderMap.put("order_status", order.getOrder_status());
					mianOrderMap.put("order_cat", order.getOrder_cat());
					mianOrderMap.put("delivery_type", order.getDelivery_type());
					mianOrderMap.put("receiver_Name", order.getReceiver_Name());
					mianOrderMap.put("payType", order.getPayType());
					mianOrderMap.put("shipCode", order.getShipCode());
					mianOrderMap.put("store_id", order.getStore_id());
					mianOrderMap.put("order_add_time", order.getAddTime());
					mianOrderMap.put("order_time", CommUtil.formatNumDate(order.getAddTime()));
					mianOrderMap.put("whether_gift", order.getWhether_gift());
					mianOrderMap.put("total_price", order.getTotalPrice());
					mianOrderMap.put("shipCode", order.getShipCode());
					mianOrderMap.put("shipprice", order.getShip_price());
					mianOrderMap.put("integral_price", order.getIntegral_price());
					mianOrderMap.put("integral", CommUtil.div(order.getIntegral_price(),
							this.configService.getSysConfig().getIntegralExchangeRate()));
					if (order.getStore_id() == null) {
						mianOrderMap.put("store_name", "self");
					} else {
						Store store = this.storeService.getObjById(CommUtil.null2LongNew(order.getStore_id()));
						if (store != null) {
							mianOrderMap.put("store_name", store.getStore_name());
							mianOrderMap.put("store_logo",
									store.getStore_logo() == null ? ""
											: this.configService.getSysConfig().getImageWebServer() + "/"
													+ store.getStore_logo().getPath() + "/"
													+ store.getStore_logo().getName());
						}
					}
					mianOrderMap.put("goodsinfo", goodsList);
					main_goods_list.add(mianOrderMap);
					ordermap.put("main_order", main_goods_list);
				}
				ordermap.put("count", main_count);
				orderlist.add(ordermap);
			}
		}
		return orderlist;
	}
}
