package com.metoo.app.foundation.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
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
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.ip.IPSeeker;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.ActivityGoods;
import com.metoo.foundation.domain.BuyGift;
import com.metoo.foundation.domain.CombinPlan;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.FootPoint;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.Group;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.EvaluateQueryObject;
import com.metoo.foundation.service.IActivityGoodsService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IBuyGiftService;
import com.metoo.foundation.service.ICombinPlanService;
import com.metoo.foundation.service.IConsultSatisService;
import com.metoo.foundation.service.IConsultService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IFootPointService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreNavigationService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserGoodsClassService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.view.web.tools.ActivityViewTools;
import com.metoo.view.web.tools.AreaViewTools;
import com.metoo.view.web.tools.ConsultViewTools;
import com.metoo.view.web.tools.EvaluateViewTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.IntegralViewTools;
import com.metoo.view.web.tools.StoreViewTools;

@Service
@Transactional
public class MGoodsServiceImpl implements MIGoodsService{
	
	@Resource(name = "goodsMetooDAO")
	private IGenericDAO<Goods> goodsMetooDao;
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IUserGoodsClassService userGoodsClassService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IConsultService consultService;
	@Autowired
	private IGoodsBrandService brandService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private AreaViewTools areaViewTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private UserTools userTools;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private ConsultViewTools consultViewTools;
	@Autowired
	private EvaluateViewTools evaluateViewTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreNavigationService storenavigationService;
	@Autowired
	private IConsultSatisService consultsatisService;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IFootPointService footPointService;
	@Autowired
	private IActivityGoodsService actgoodsService;
	@Autowired
	private ActivityViewTools activityViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private ICombinPlanService combinplanService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IBuyGiftService buyGiftService;
	
	
	
	@Override
	public Goods getObjById(Long id) {
		// TODO Auto-generated method stub
		Goods goods = this.goodsMetooDao.get(id);
		if(goods != null){
			return goods;
		}
		return null;
	}
	
	public List<Goods> query(String query, Map params, int begin, int max) {
		return this.goodsMetooDao.query(query, params, begin, max);

	}
	
	public String goodsdetail(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = null;
		Result result = new Result();
		Map goodsMap = new HashMap();
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		if (obj != null) {
			if (this.configService.getSysConfig().isSecond_domain_open()) {// 如果系统开启了二级域名，则判断该商品是不是对应的二级域名下的，如果不是则返回错误页面
				String serverName = request.getServerName().toLowerCase();
				String secondDomain = CommUtil.null2String(serverName
						.substring(0, serverName.indexOf(".")));
				if (serverName.indexOf(".") == serverName.lastIndexOf(".")) {
					secondDomain = "www";
				}
				// System.out.println("已经开启二级域名，二级域名为：" + secondDomain);
				if (!secondDomain.equals("")) {
					//[商品类型，0为自营商品，1为第三方经销商]
					if (obj.getGoods_type() == 0) {// 自营商品禁止使用二级域名访问
						if (!secondDomain.equals("www")) {
							//[config:商城配置， userPath：自定义路径，和type配合使用 ，自定义路径，和type配合使用，type:视图类型 0为后台，1为前台 大于1为自定义路径]
						result = new Result(1,"参数错误，商品查看失败");
							
						}
						// System.out.println("已经开启二级域名，自营商品禁止二级域名访问");
					} else {
						if (!obj.getGoods_store().getStore_second_domain()//[获取店铺二级域名]
								.equals(secondDomain)) {
							// System.out.println("已经开启二级域名，非本店商品，二级域名错误");
							result = new Result(1,"参数错误，商品查看失败");
						}
					}
				} else {
					result = new Result(1,"参数错误，商品查看失败");
				}
			}
			// System.out.println("未开启二级域名");
			// 利用cookie添加浏览过的商品
			Cookie[] cookies = request.getCookies();
			Cookie goodscookie = null;
			int k = 0;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("goodscookie")) {
						String goods_ids = cookie.getValue();//goods_ids：获取当前Cookie值
						int m = 6;
						int n = goods_ids.split(",").length;
						if (m > n) {
							m = n + 1;
						}
						String[] new_goods_ids = goods_ids.split(",", m);
						for (int i = 0; i < new_goods_ids.length; i++) {
							if ("".equals(new_goods_ids[i])) {
								for (int j = i + 1; j < new_goods_ids.length; j++) {
									new_goods_ids[i] = new_goods_ids[j];
								}
							}
						}
						String[] new_ids = new String[6];
						for (int i = 0; i < m - 1; i++) {
							if (id.equals(new_goods_ids[i])) {
								k++;
							}
						}
						if (k == 0) {
							new_ids[0] = id;
							for (int j = 1; j < m; j++) {
								new_ids[j] = new_goods_ids[j - 1];
							}
							goods_ids = id + ",";
							if (m == 2) {
								for (int i = 1; i <= m - 1; i++) {
									goods_ids = goods_ids + new_ids[i] + ",";
								}
							} else {
								for (int i = 1; i < m; i++) {
									goods_ids = goods_ids + new_ids[i] + ",";
								}
							}
							goodscookie = new Cookie("goodscookie", goods_ids);
						} else {
							new_ids = new_goods_ids;
							goods_ids = "";
							for (int i = 0; i < m - 1; i++) {
								goods_ids += new_ids[i] + ",";
							}
							goodscookie = new Cookie("goodscookie", goods_ids);
						}
						goodscookie.setMaxAge(60 * 60 * 24 * 7);
						goodscookie.setDomain(CommUtil.generic_domain(request));
						response.addCookie(goodscookie);
						break;
					} else {
						goodscookie = new Cookie("goodscookie", id + ",");
						goodscookie.setMaxAge(60 * 60 * 24 * 7);
						goodscookie.setDomain(CommUtil.generic_domain(request));
						response.addCookie(goodscookie);
					}
				}
			} else {
				goodscookie = new Cookie("goodscookie", id + ",");
				goodscookie.setMaxAge(60 * 60 * 24 * 7);
				goodscookie.setDomain(CommUtil.generic_domain(request));
				response.addCookie(goodscookie);
			}
			User current_user = SecurityUserHolder.getCurrentUser();
			boolean admin_view = false;// 超级管理员可以查看未审核得到商品信息
			if (current_user != null) {
				// 登录用户记录浏览足迹信息
				Map params = new HashMap();
				params.put("fp_date", CommUtil.formatDate(CommUtil
						.formatShortDate(new Date())));
				params.put("fp_user_id", current_user.getId());//[JPQL查询]
				List<FootPoint> fps = this.footPointService
						.query("select obj from FootPoint obj where obj.fp_date=:fp_date and obj.fp_user_id=:fp_user_id",
								params, -1, -1);
				if (fps.size() == 0) {
					FootPoint fp = new FootPoint();
					fp.setAddTime(new Date());
					fp.setFp_date(new Date());
					fp.setFp_user_id(current_user.getId());
					fp.setFp_user_name(current_user.getUsername());
					fp.setFp_goods_count(1);
					Map map = new HashMap(); 
					map.put("goods_id", obj.getId());
					map.put("goods_name", obj.getGoods_name());
					map.put("goods_sale", obj.getGoods_salenum());
					map.put("goods_time", CommUtil.formatLongDate(new Date()));
					map.put("goods_img_path", obj.getGoods_main_photo() != null
							? CommUtil.getURL(request) + "/"
									+ obj.getGoods_main_photo().getPath() + "/"
									+ obj.getGoods_main_photo().getName()
							: CommUtil.getURL(request)
									+ "/"
									+ this.configService.getSysConfig()
											.getGoodsImage().getPath()
									+ "/"
									+ this.configService.getSysConfig()
											.getGoodsImage().getName());
					map.put("goods_price", obj.getGoods_current_price());
					map.put("goods_class_id",
							CommUtil.null2Long(obj.getGc().getId()));
					map.put("goods_class_name",
							CommUtil.null2String(obj.getGc().getClassName()));
					List<Map> list = new ArrayList<Map>();
					list.add(map);
					fp.setFp_goods_content(Json.toJson(list,
							JsonFormat.compact()));
					this.footPointService.save(fp);
				} else {
					FootPoint fp = fps.get(0);
					List<Map> list = Json.fromJson(List.class,
							fp.getFp_goods_content());
					boolean add = true;
					for (Map map : list) {// 排除重复的商品足迹
						if (CommUtil.null2Long(map.get("goods_id")).equals(
								obj.getId())) {
							add = false;
						}
					}
					if (add) {
						Map map = new HashMap();
						map.put("goods_id", obj.getId());
						map.put("goods_name", obj.getGoods_name());
						map.put("goods_sale", obj.getGoods_salenum());
						map.put("goods_time",
								CommUtil.formatLongDate(new Date()));
						map.put("goods_img_path",
								obj.getGoods_main_photo() != null
										? CommUtil.getURL(request)
												+ "/"
												+ obj.getGoods_main_photo()
														.getPath()
												+ "/"
												+ obj.getGoods_main_photo()
														.getName() : CommUtil
												.getURL(request)
												+ "/"
												+ this.configService
														.getSysConfig()
														.getGoodsImage()
														.getPath()
												+ "/"
												+ this.configService
														.getSysConfig()
														.getGoodsImage()
														.getName());
						map.put("goods_price", obj.getGoods_current_price());
						map.put("goods_class_id",
								CommUtil.null2Long(obj.getGc().getId()));
						map.put("goods_class_name", CommUtil.null2String(obj
								.getGc().getClassName()));
						list.add(0, map);// 后浏览的总是插入最前面
						fp.setFp_goods_count(list.size());
						fp.setFp_goods_content(Json.toJson(list,
								JsonFormat.compact()));
						this.footPointService.update(fp);
					}
				}
				current_user = this.userService
						.getObjById(current_user.getId());
				if (current_user.getUserRole().equals("ADMIN")) {
					admin_view = true;
				}
			}
			// 记录商品点击日志
			GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(obj
					.getId());
			todayGoodsLog.setGoods_click(todayGoodsLog.getGoods_click() + 1);
			String click_from_str = todayGoodsLog.getGoods_click_from();
			Map<String, Integer> clickmap = (click_from_str != null && !click_from_str
					.equals("")) ? (Map<String, Integer>) Json
					.fromJson(click_from_str) : new HashMap<String, Integer>();
			String from = clickfrom_to_chinese(CommUtil.null2String(request
					.getParameter("from")));
			if (from != null && !from.equals("")) {
				if (clickmap.containsKey(from)) {
					clickmap.put(from, clickmap.get(from) + 1);
				} else {
					clickmap.put(from, 1);
				}
			} else {
				if (clickmap.containsKey("unknow")) {
					clickmap.put("unknow", clickmap.get("unknow") + 1);
				} else {
					clickmap.put("unknow", 1);
				}
			}
			todayGoodsLog.setGoods_click_from(Json.toJson(clickmap,
					JsonFormat.compact()));
			this.goodsLogService.update(todayGoodsLog);
			//[商品当前状态，-5为平台未审核 0为上架，1为在仓库中，2为定时自动上架，3为店铺过期自动下架，-1为手动下架状态，-2为违规下架状态]
			if (obj.getGoods_status() == 0 || admin_view) {
				mv = new JModelAndView("default/store_goods.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				obj.setGoods_click(obj.getGoods_click() + 1);
				if (this.configService.getSysConfig().isZtc_status()
						&& obj.getZtc_status() == 2) {//直通车状态，1为开通申请待审核，2为审核通过,-1为审核失败,3为已经开通
					obj.setZtc_click_num(obj.getZtc_click_num() + 1);//
				}
				// 检测商品所有活动状态[活动状态，0为无活动，1为待审核，2为审核通过，3为活动已经过期活结束，审核未通过时状态为0]
				if (obj.getActivity_status() == 1
						|| obj.getActivity_status() == 2) {// 检查商城促销商品是否过期
					if (!CommUtil.null2String(obj.getActivity_goods_id())
							.equals("")) {
						ActivityGoods ag = this.actgoodsService.getObjById(obj
								.getActivity_goods_id());
						if (ag.getAct().getAc_end_time().before(new Date())) {
							ag.setAg_status(-2);
							this.actgoodsService.update(ag);
							obj.setActivity_status(0);
							obj.setActivity_goods_id(null);
						}
					}
				}
				if (obj.getGroup() != null && obj.getGroup_buy() == 2) {// 检查团购是否过期
					Group group = obj.getGroup();
					if (group.getEndTime().before(new Date())) {
						obj.setGroup(null);
						obj.setGroup_buy(0);
						obj.setGoods_current_price(obj.getStore_price());
					}
				}
				if (obj.getCombin_status() == 1) {// 检查组合是否过期
					Map params = new HashMap();
					params.put("endTime", new Date());
					params.put("main_goods_id", obj.getId());
					List<CombinPlan> combins = this.combinplanService
							.query("select obj from CombinPlan obj where obj.endTime<=:endTime and obj.main_goods_id=:main_goods_id",
									params, -1, -1);
					if (combins.size() > 0) {
						for (CombinPlan com : combins) {
							if (com.getCombin_type() == 0) {
								if (obj.getCombin_suit_id().equals(com.getId())) {
									obj.setCombin_suit_id(null);
								}
							} else {
								if (obj.getCombin_parts_id()
										.equals(com.getId())) {
									obj.setCombin_parts_id(null);
								}
							}
							obj.setCombin_status(0);
						}
					}
				}
				if (obj.getOrder_enough_give_status() == 1) {// 检查满就送是否过期
					BuyGift bg = this.buyGiftService.getObjById(obj
							.getBuyGift_id());
					if (bg != null && bg.getEndTime().before(new Date())) {
						bg.setGift_status(20);
						List<Map> maps = Json.fromJson(List.class,
								bg.getGift_info());
						maps.addAll(Json.fromJson(List.class,
								bg.getGoods_info()));
						for (Map map : maps) {
							Goods goods = this.goodsService.getObjById(CommUtil
									.null2Long(map.get("goods_id")));
							if (goods != null) {
								goods.setOrder_enough_give_status(0);
								goods.setOrder_enough_if_give(0);
								goods.setBuyGift_id(null);
							}
						}
						this.buyGiftService.update(bg);
					}
					if (bg != null && bg.getGift_status() == 10) {
						//mv.addObject("isGift", true);
						goodsMap.put("isGift", true);
					}
				}
				if (obj.getOrder_enough_if_give() == 1) {// 检查满就送赠品是否过期
					BuyGift bg = this.buyGiftService.getObjById(obj
							.getBuyGift_id());
					if (bg != null && bg.getGift_status() == 10) {
						//mv.addObject("isGive", true);
						goodsMap.put("isGift", true);
					}
				}
				if (obj.getEnough_reduce() == 1) {// 如果是满就减商品，未到活动时间不作处理，活动时间显示满减信息，已过期则删除满减信息
					EnoughReduce er = this.enoughReduceService
							.getObjById(CommUtil.null2Long(obj
									.getOrder_enough_reduce_id()));
					if (er.getErstatus() == 10
							&& er.getErbegin_time().before(new Date())
							&& er.getErend_time().after(new Date())) {// 正在进行
						mv.addObject("enoughreduce", er);
					} else if (er.getErend_time().before(new Date())) {// 已过期
						er.setErstatus(20);
						this.enoughReduceService.update(er);
						String goods_json = er.getErgoods_ids_json();
						List<String> goods_id_list = (List) Json
								.fromJson(goods_json);
						for (String goods_id : goods_id_list) {
							Goods ergood = this.goodsService
									.getObjById(CommUtil.null2Long(goods_id));
							ergood.setEnough_reduce(0);
							ergood.setOrder_enough_reduce_id("");
							this.goodsService.update(ergood);
						}
					}
				}
				this.goodsService.update(obj);
				/*mv.addObject("obj", obj);
				mv.addObject("goodsViewTools", goodsViewTools);
				mv.addObject("transportTools", transportTools);*/
				// 计算当期访问用户的IP地址，并计算对应的运费信息
				String current_ip = CommUtil.getIpAddr(request);// 获得本机IP
				System.out.println("当前IP："+current_ip);
				if (CommUtil.isIp(current_ip)) {
					IPSeeker ip = new IPSeeker(null, null);
					String current_city = ip.getIPLocation(current_ip)
							.getCountry();
					//mv.addObject("current_city", current_city);
					goodsMap.put("current_city", current_city);
				} else {
					goodsMap.put("current_city", "未知地区");
				}
				// 查询运费地区//[运费功能暂时不用]
				/*List<Area> areas = this.areaService
						.query("select obj from Area obj where obj.parent.id is null order by obj.sequence asc",
								null, -1, -1);
				mv.addObject("areas", areas);*/
				// 相关分类
				Map params = new HashMap();
				params.put("parent_id", obj.getGc().getParent().getId());
				params.put("display", true);
				List<GoodsClass> about_gcs = this.goodsClassService
						.query("select new GoodsClass(id,className) from GoodsClass obj where obj.parent.id=:parent_id and obj.display=:display order by sequence asc",
								params, -1, -1);
				//mv.addObject("about_gcs", about_gcs);
				List<Map> GoodsClass_List = new ArrayList<Map>();
				for(GoodsClass about_goodsClass:about_gcs){
					Map goodsClass_info = new HashMap();
					goodsClass_info.put("className", about_goodsClass.getClassName());
					GoodsClass_List.add(goodsClass_info);
					goodsMap.put("goodsClass_info", GoodsClass_List);
				}
				/*mv.addObject("userTools", userTools);
				mv.addObject("goodsViewTools", goodsViewTools);
				mv.addObject("activityViewTools", activityViewTools);
				*/
				//[获取店铺评分信息][后续完善]
				/*if (obj.getGoods_type() == 0) {// 平台自营商品 
				} else {// 商家商品
					this.generic_evaluate(obj.getGoods_store(), mv);//[引用评分信息方法]
					
					//mv.addObject("store", obj.getGoods_store());//[商家店铺]
					goodsMap.put("goods_store",obj.getGoods_store());
				}*/
				// 查詢评价第一页信息 [构造构造一个查询对象]
				EvaluateQueryObject qo = new EvaluateQueryObject("1", mv,
						"addTime", "desc");
				qo.addQuery("obj.evaluate_goods.id", new SysMap("goods_id",//[获取商品对应的评价表id]
						CommUtil.null2Long(id)), "=");
				qo.addQuery("obj.evaluate_type", new SysMap("evaluate_type",
						"goods"), "=");
				qo.addQuery("obj.evaluate_status", new SysMap(//[ 0为正常，1为禁止显示，2为取消评价]
						"evaluate_status", 0), "=");
				qo.setPageSize(10);
				IPageList eva_pList = this.evaluateService.list(qo);//[查询结果][转json-goods]
				String url = CommUtil.getURL(request) + "/goods_evaluation.htm";//存入要跳转的页面；
				//mv.addObject("eva_objs", eva_pList.getResult());//[转json-goods]
				goodsMap.put("evaluate_url", url);
				//[根据商品id查询评价信息]
				List<Evaluate>  evaluate_list = eva_pList.getResult();
				for(Evaluate evaluates:evaluate_list){
					Map evaluatesMap = new HashMap();
					Map orderFormMap = new HashMap();
					evaluatesMap.put("evaluate_info", evaluates.getEvaluate_info());//添加数据库信息，测试
					evaluatesMap.put("evaluates_num", evaluates.getGoods_num());
					orderFormMap.put("OrderForm", evaluates.getOf().getGoods_info());
					evaluatesMap.put("orderFormMap", orderFormMap);
					goodsMap.put("evaluatesMap", evaluatesMap);
				}
				/*String  eva_gotoPageAjaxHTML= CommUtil.showPageAjaxHtml(url, "",
						eva_pList.getCurrentPage(),
						eva_pList.getPages());
				goodsMap.put("eva_gotoPageAjaxHTML", eva_gotoPageAjaxHTML);*/
				/*mv.addObject("evaluateViewTools", this.evaluateViewTools);
				mv.addObject("orderFormTools", this.orderFormTools);*/
				// 查询成交记录第一页
				/*qo = new EvaluateQueryObject("1", mv, "addTime", "desc");
				qo.addQuery("obj.evaluate_goods.id", new SysMap("goods_id",
						CommUtil.null2Long(id)), "=");
				qo.setPageSize(10);
				IPageList order_eva_pList = this.evaluateService.list(qo);//[查询结果][转json-goods]
				
				
				url = CommUtil.getURL(request) + "/goods_order.htm";
				//mv.addObject("order_objs", order_eva_pList.getResult());
				List<Evaluate> evaluate = order_eva_pList.getResult();
				for(Evaluate evaluateRecord:evaluate){
					Map orderFormLogMap = new HashMap();
					Map userOrLog = new HashMap();
					orderFormLogMap.put("Log_info", evaluateRecord.get);
					userOrLog.put("user", orderlog.getLog_user().getUserName());
					goodsMap.put("orderFormLogMap", orderFormLogMap);
				}
				String order_gotoPageAjaxHTML = CommUtil.showPageAjaxHtml(url, "",order_eva_pList.getCurrentPage(),order_eva_pList.getPages());
				goodsMap.put("order_gotoPageAjaxHTML", order_gotoPageAjaxHTML);*/
				// 查询商品咨询第一页
				/*ConsultQueryObject cqo = new ConsultQueryObject("1", mv,
						"addTime", "desc");
				cqo.addQuery("obj.goods_id",
						new SysMap("goods_id", CommUtil.null2Long(id)), "=");
				cqo.setPageSize(10);
				IPageList pList = this.consultService.list(cqo);///[查询结果]//[转json-goods]
				url = CommUtil.getURL(request) + "/goods_consult.htm";
				List<Consult> consult_list = eva_pList.getResult();
				for(Consult consults:consult_list){
					Map consultMap = new HashMap();
					consultMap.put("consult_email",consults.getConsult_email());//添加数据库信息，测试
					consultMap.put("consult_type", consults.getConsult_type());
					consultMap.put("OrderForm", consults.getConsult_user_name());
					goodsMap.put("evaluatesMap", consultMap);
				}
				goodsMap.put("consult_gotoPageAjaxHTML",CommUtil.showPageAjaxHtml(url, "",pList.getCurrentPage(), pList.getPages()));*/
				/*mv.addObject(
						"consult_gotoPageAjaxHTML",
						CommUtil.showPageAjaxHtml(url, "",
								pList.getCurrentPage(), pList.getPages()));
				mv.addObject("consultViewTools", this.consultViewTools);*/
				
			/*	// 处理系统商品对比信息
				List<Goods> goods_compare_list = (List<Goods>) request
						.getSession(false).getAttribute("goods_compare_cart");
				int compare = 0;// 当前商品是否存在对比商品session中
				if (goods_compare_list != null) {
					for (Goods goods : goods_compare_list) {
						if (goods.getId().equals(obj.getId())) {
							compare = 1;
						}
					}
				} else {
					goods_compare_list = new ArrayList<Goods>();
				}
				// 计算商品对比中第一间商品的分类，只允许对比同一个分类的商品
				int compare_goods_flag = 0;// 默认允许对比商品，如果商品分类不一致曾不允许对比
				for (Goods compare_goods : goods_compare_list) {
					if (compare_goods != null) {
						compare_goods = this.goodsService
								.getObjById(compare_goods.getId());
						if (!compare_goods
								.getGc()
								.getParent()
								.getParent()
								.getId()
								.equals(obj.getGc().getParent().getParent()
										.getId())) {
							compare_goods_flag = 1;
						}
					}
				}
				mv.addObject("compare_goods_flag", compare_goods_flag);
				mv.addObject("goods_compare_list", goods_compare_list);
				mv.addObject("compare", compare);*/
				// 相关品牌
			} else {
				result = new Result(1,"参数错误，商品查看失败");
			}
		} else {
			result = new Result(1,"参数错误，商品查看失败");
		}
			//goodsMap.put("seo_Keywords", obj.getSeo_keywords());
			goodsMap.put("goodsName", obj.getGoods_name());
			goodsMap.put("seo_description", obj.getSeo_description());
			List<Map> accessoryList= new ArrayList<Map>();
			List<Accessory> accessorys = obj.getGoods_photos();	
			for (Accessory accessory : accessorys) {
				Map accessorymap = new HashMap();
				Map albumMap = new HashMap();
				accessorymap.put("id", accessory.getId());
				accessorymap.put("name", accessory.getName()); 
				accessorymap.put("path", accessory.getPath());
				albumMap.put("album_name",accessory.getAlbum().getAlbum_name());
				albumMap.put("album_sequence",accessory.getAlbum().getAlbum_sequence());
				accessorymap.put("albemMap", albumMap);
				accessoryList.add(accessorymap);
				goodsMap.put("accessoryMap",accessoryList);
			}
			if(!goodsMap.isEmpty()){
				result = new Result(0,"查询商品信息成功",goodsMap);
			}else{
				result = new Result(1,"商品信息为空");
			}
			String goodstemp = Json.toJson(result, JsonFormat.compact());
			return goodstemp;
			
		}

	
	/**
     * 修改来源提示语言
     * @param key
     * @return
     */
	public String clickfrom_to_chinese(String key) {
		String str = "其它";
		if (key.equals("search")) {
			str = "搜索";
		}
		if (key.equals("floor")) {
			str = "首页楼层";
		}
		if (key.equals("gcase")) {
			str = "橱窗";
		}

		return str;
	}

}
