package com.metoo.manage.seller.action;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.qrcode.QRCodeUtil;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.ComplaintGoods;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsFormat;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreGrade;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.UserGoodsClass;
import com.metoo.foundation.domain.WaterMark;
import com.metoo.foundation.domain.ZTCGoldLog;
import com.metoo.foundation.domain.query.AccessoryQueryObject;
import com.metoo.foundation.domain.query.AlbumQueryObject;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.domain.query.TransportQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.IComplaintGoodsService;
import com.metoo.foundation.service.IComplaintService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsFormatService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsSpecificationService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPaymentService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITransportService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserGoodsClassService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.IWaterMarkService;
import com.metoo.foundation.service.IZTCGoldLogService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.LuceneVo;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.GoodsTools;
import com.metoo.manage.admin.tools.ImageTools;
import com.metoo.manage.admin.tools.StoreTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.StoreViewTools;

/**
 * 
 * <p>
 * Title: GoodsSellerAction.java
 * </p>
 * 
 * <p>
 * Description:商家后台商品管理控制器
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.metoo.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-5-7
 * 
 * @version metoo_b2b2c v2.0 2015版
 */
@Controller
public class GoodsSellerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IUserGoodsClassService userGoodsClassService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private IGoodsSpecPropertyService specPropertyService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private IWaterMarkService waterMarkService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private ITransportService transportService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private StoreTools storeTools;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private IGoodsSpecificationService goodsSpecificationService;
	@Autowired
	private IComplaintService complaintService;
	@Autowired
	private IComplaintGoodsService complaintGoodsService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private IGoodsFormatService goodsFormatService;
	@Autowired
	private IZTCGoldLogService iztcGoldLogService;
	@Autowired
	private IGoodsCartService cartService;
	@Autowired
	private GoodsTools goodsTools;
	@Autowired
	private ImageTools ImageTools;
	@Autowired
	private ICGoodsService cGoodsService;

	@SecurityMapping(title = "发布商品第一步", value = "/seller/add_goods_first.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/add_goods_first.htm")
	public ModelAndView add_goods_first(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/seller_error.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Map params = new HashMap();
		request.getSession(false).removeAttribute("goods_class_info");
		int store_status = (user.getStore() == null ? 0 : user.getStore()
				.getStore_status());
		if (store_status == 15) {
			StoreGrade grade = user.getStore().getGrade();
			int user_goods_count = user.getStore().getGoods_list().size();
			System.out.println(grade.getGoodsCount());
			if (grade.getGoodsCount() == 0
					|| user_goods_count < grade.getGoodsCount()) {
				mv = new JModelAndView(
						"user/default/sellercenter/add_goods_first.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request,
						response);
				String json_staples = "";
				if (user.getStaple_gc() != null
						&& !user.getStaple_gc().equals("")) {
					json_staples = user.getStaple_gc();
				}
				List<Map> staples = Json.fromJson(List.class, json_staples);
				mv.addObject("staples", staples);
				if (user.getStore().getGc_detail_info() != null) {// 店铺详细类目
					List<GoodsClass> gcs = this.storeTools
							.query_store_detail_MainGc(user.getStore()
									.getGc_detail_info());
					mv.addObject("gcs", gcs);
				} else {
					GoodsClass parent = this.goodsClassService.getObjById(user
							.getStore().getGc_main_id());
					mv.addObject("gcs", parent.getChilds());
				}
				mv.addObject("id", CommUtil.null2String(id));
			} else {
				mv.addObject("op_title", "您的店铺等级只允许上传" + grade.getGoodsCount()
						+ "件商品!");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/seller/store_grade.htm");
			}
		}
		if (store_status == 0) {
			mv.addObject("op_title", "您尚未开通店铺，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		} else if (store_status == 10) {
			mv.addObject("op_title", "您的店铺在审核中，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		}
		if (store_status == 20) {
			mv.addObject("op_title", "您的店铺已被关闭，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		} else {
			mv.addObject("op_title", "店铺信息错误，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "商品运费模板分页显示", value = "/seller/goods_transport.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_transport.htm")
	public ModelAndView goods_transport(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String ajax) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_transport.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		/*if (CommUtil.null2Boolean(ajax)) {
			mv = new JModelAndView(
					"user/default/sellercenter/goods_transport_list.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
		}
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String params = "";
		TransportQueryObject qo = new TransportQueryObject(currentPage, mv,
				orderBy, orderType);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		qo.addQuery("obj.store.id", new SysMap("store_id", store.getId()), "=");
		qo.setPageSize(1);
		IPageList pList = this.transportService.list(qo);
		CommUtil.saveIPageList2ModelAndView(
				url + "/seller/goods_transport.htm", "", params, pList, mv);
		mv.addObject("transportTools", transportTools);
		return mv;*/

		if (CommUtil.null2Boolean(ajax)) {
			mv = new JModelAndView("user/default/sellercenter/goods_transport_list.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request, response);
		}
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String params = "";
		TransportQueryObject qo = new TransportQueryObject(currentPage, mv, orderBy, orderType);
		Store store = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId()).getStore();
		qo.addQuery("obj.trans_user", new SysMap("obj_trans_user", 0), "=");
		qo.setPageSize(20);
		IPageList pList = this.transportService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/seller/goods_transport.htm", "", params, pList, mv);
		mv.addObject("transportTools", transportTools);
		mv.addObject("store", store);
		return mv;
	}

	@SecurityMapping(title = "发布商品第二步", value = "/seller/add_goods_second.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/add_goods_second.htm")
	public ModelAndView add_goods_second(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/seller_error.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		int store_status = user.getStore().getStore_status();
		if (store_status == 15) {
			if (request.getSession(false).getAttribute("goods_class_info") != null) {
				mv = new JModelAndView(
						"user/default/sellercenter/add_goods_second.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request,
						response);
				GoodsClass gc = (GoodsClass) request.getSession(false)
						.getAttribute("goods_class_info");
				gc = this.goodsClassService.getObjById(gc.getId());
				String goods_class_info = this.generic_goods_class_info(gc);
				mv.addObject("goods_class",
						this.goodsClassService.getObjById(gc.getId()));
				mv.addObject("goods_class_info", goods_class_info.substring(0,
						goods_class_info.length() - 1));
				request.getSession(false).removeAttribute("goods_class_info");
				if (gc.getLevel() == 2) {// 发布商品选择分类时选择三级分类,查询出所有与该三级分类关联的规格，即规格对应的详细商品分类
					Map spec_map = new HashMap();
					spec_map.put("store_id", user.getStore().getId());
					List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService
							.query("select obj from GoodsSpecification obj where obj.store.id=:store_id order by sequence asc",
									spec_map, -1, -1);
					List<GoodsSpecification> spec_list = new ArrayList<GoodsSpecification>();
					for (GoodsSpecification gspec : goods_spec_list) {
						for (GoodsClass spec_goodsclass_detail : gspec
								.getSpec_goodsClass_detail()) {
							if (gc.getId().equals(
									spec_goodsclass_detail.getId())) {
								spec_list.add(gspec);
							}
						}
					}
					mv.addObject("goods_spec_list", spec_list);
				} else if (gc.getLevel() == 1) {// 发布商品选择分类时选择二级分类,规格对应的主营商品分类
					Map spec_map = new HashMap();
					spec_map.put("store_id", user.getStore().getId());
					spec_map.put("gc_id", gc.getId());
					List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService
							.query("select obj from GoodsSpecification obj where obj.store.id=:store_id and obj.goodsclass.id=:gc_id order by sequence asc",
									spec_map, -1, -1);
					mv.addObject("goods_spec_list", goods_spec_list);
				}
				String path = this.storeTools.createUserFolder(request,
						user.getStore());
				double csize = CommUtil.fileSize(new File(path));
				double img_remain_size = 0;
				if (user.getStore().getGrade().getSpaceSize() >0) {
					img_remain_size = CommUtil.div(user.getStore().getGrade()
							.getSpaceSize()
							* 1024 - csize, 1024);
					mv.addObject("img_remain_size", img_remain_size);
				}
				Map params = new HashMap();
				params.put("user_id", user.getId());
				params.put("display", true);
				List<UserGoodsClass> ugcs = this.userGoodsClassService
						.query("select obj from UserGoodsClass obj where obj.user_id=:user_id and obj.display=:display and obj.parent.id is null order by obj.sequence asc",
								params, -1, -1);
				params.clear();
				GoodsClass goods_class = null;
				if (gc.getLevel() == 2) {
					goods_class = gc.getParent().getParent();
				}
				if (gc.getLevel() == 1) {
					goods_class = gc.getParent();
				}
				params.put("gc_id", goods_class.getId());
				List<GoodsBrand> gbs = this.goodsBrandService
						.query("select obj from GoodsBrand obj where obj.gc.id=:gc_id order by obj.sequence asc",
								params, -1, -1);
				mv.addObject("gbs", gbs);
				mv.addObject("ugcs", ugcs);
				mv.addObject("imageSuffix", this.storeViewTools
						.genericImageSuffix(this.configService.getSysConfig()
								.getImageSuffix()));
				//处理上传格式
				String[] strs =  this.configService.getSysConfig().getImageSuffix().split("\\|");
				StringBuffer sb = new StringBuffer();
				for (String str : strs) {
					sb.append("."+str+",");
				}
				mv.addObject("imageSuffix1", sb);
				Date now = new Date();
				now.setDate(now.getDate() + 1);
				mv.addObject("default_publish_day",
						CommUtil.formatShortDate(now));
				String goods_session = CommUtil.randomString(32);
				mv.addObject("goods_session", goods_session);//放入当前作用于 ==    request.setAttribute("token",token);
				request.getSession(false).setAttribute("goods_session",
						goods_session);
				mv.addObject("store", user.getStore());
				// 查询商品版式信息
				params.clear();
				params.put("gf_store_id", user.getStore().getId());
				List gfs = this.goodsFormatService
						.query("select obj from GoodsFormat obj where obj.gf_store_id=:gf_store_id",
								params, -1, -1);
				mv.addObject("gfs", gfs);
				// 查询地址信息，前端需要商家选择发货地址
				List<Area> areas = this.areaService.query(
						"select obj from Area obj where obj.parent.id is null",
						null, -1, -1);
				mv.addObject("areas", areas);
			} else {
				mv.addObject("op_title", "Session信息丢失，请重新发布商品");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/seller/add_goods_first.htm");
			}
		}
		if (store_status == 0) {
			mv.addObject("op_title", "您尚未开通店铺，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		}
		if (store_status == 10) {
			mv.addObject("op_title", "您的店铺在审核中，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		}
		if (store_status == 20) {
			mv.addObject("op_title", "您的店铺已被关闭，不能发布商品");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/index.htm");
		}
		return mv;
	}
	
	/**
	 * @description 移除list集合重复项
	 * @param list
	 * @return
	 */
	public List<GoodsSpecification> removeDuplicates(List<GoodsSpecification> list) {    
	     Set set = new HashSet();    
	     List<GoodsSpecification> newList = new ArrayList<GoodsSpecification>();    
		 for (Iterator iter = list.iterator(); iter.hasNext();) {    
			 Object element = iter.next();    
		     if (set.add(element))    
		    	 newList.add((GoodsSpecification) element);    
		    }     
			 list.clear();    
			 list.addAll(newList);
		return list;       
	 }   
	

	@SecurityMapping(title = "产品规格显示", value = "/seller/goods_inventory.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_inventory.htm")
	public ModelAndView goods_inventory(HttpServletRequest request,
			HttpServletResponse response, String goods_spec_ids,
			String supplement) {
		ModelAndView mv = mv = new JModelAndView(
				"user/default/sellercenter/goods_inventory.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		String[] spec_ids = goods_spec_ids.split(",");
		List<GoodsSpecProperty> gsps = new ArrayList<GoodsSpecProperty>();
		for (String spec_id : spec_ids) {
			if (!spec_id.equals("")) {
				GoodsSpecProperty gsp = this.specPropertyService
						.getObjById(Long.parseLong(spec_id));
				gsps.add(gsp);
			}
		}
		List<GoodsSpecification> specs = new ArrayList<GoodsSpecification>();
		for (GoodsSpecProperty gsp : gsps) {
			specs.add(gsp.getSpec());
		}
		List<GoodsSpecification> list = removeDuplicates(specs);
		for (GoodsSpecification spec : list) {
			spec.getProperties().clear();
			for (GoodsSpecProperty gsp : gsps) {
				if (gsp.getSpec().getId().equals(spec.getId())) {
					spec.getProperties().add(gsp);
				}
			}
		}
		
		GoodsSpecification[] spec_list = specs
				.toArray(new GoodsSpecification[specs.size()]);
		Arrays.sort(spec_list, new Comparator() {
			@Override
			public int compare(Object obj1, Object obj2) {
				// TODO Auto-generated method stub
				GoodsSpecification a = (GoodsSpecification) obj1;
				GoodsSpecification b = (GoodsSpecification) obj2;
				if (a.getSequence() == b.getSequence()) {
					return 0;
				} else {
					return a.getSequence() > b.getSequence() ? 1 : -1;
				}
			}
		});
		//[Arrays.asList 将数组转化为list]
		mv.addObject("specs", Arrays.asList(spec_list));
		
		List<List<GoodsSpecProperty>> gsp_list = this
				.generic_spec_property(specs);
		
		mv.addObject("gsps", gsp_list);
		
		if (supplement != null && !supplement.equals("")) {
			mv.addObject("supplement", supplement);
		}
		return mv;
	}
	
	@RequestMapping("/seller/query_photo.htm")
	public void img(HttpServletRequest request,
			HttpServletResponse response, String img_id){
		Map Json_map = new HashMap();
		String[] img = img_id.split("_");
		List<Accessory> photos = new ArrayList<Accessory>();
		for(String imgs : img){
			Accessory photo = this.accessoryService.getObjById(CommUtil.null2Long(imgs));
			photos.add(photo);
		}

		List<Map> photo_list = new ArrayList<Map>();
		if(!photos.isEmpty()){
			for(Accessory acc: photos){
				Map photo_map = new HashMap();
				try {
					photo_map.put("img", this.configService.getSysConfig().getImageWebServer()+"/"+acc.getPath()+"/"+acc.getName());
					photo_map.put("id", acc.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
				if(!photo_map.isEmpty()){
					photo_list.add(photo_map);
				}
			}
		}
		String jsonList = Json.toJson(photo_list, JsonFormat.compact());
		try {
			response.getWriter().print(jsonList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * arraylist转化为二维数组
	 * 
	 * @param list
	 * @return
	 */
	public static GoodsSpecProperty[][] list2group(
			List<List<GoodsSpecProperty>> list) {
		GoodsSpecProperty[][] gps = new GoodsSpecProperty[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			gps[i] = list.get(i).toArray(
					new GoodsSpecProperty[list.get(i).size()]);
		}
		return gps;
	}

	/**
	 * 生成库存组合
	 * 
	 * @param specs
	 * @return
	 */
	private List<List<GoodsSpecProperty>> generic_spec_property(
			List<GoodsSpecification> specs) {
		List<List<GoodsSpecProperty>> result_list = new LinkedList<List<GoodsSpecProperty>>();
		List<List<GoodsSpecProperty>> list = new LinkedList<List<GoodsSpecProperty>>();
		int max = 1;
		for (GoodsSpecification spec : specs) {
			list.add(spec.getProperties());
		}
		// 将List<List<GoodsSpecProperty>> 转换为二维数组
		GoodsSpecProperty[][] gsps = this.list2group(list);
		for (int i = 0; i < gsps.length; i++) {
			max *= gsps[i].length;
		}
		for (int i = 0; i < max; i++) {
			List<GoodsSpecProperty> temp_list = new ArrayList<GoodsSpecProperty>();
			int temp = 1; // 注意这个temp的用法。
			for (int j = 0; j < gsps.length; j++) {
				temp *= gsps[j].length;
				temp_list.add(j, gsps[j][i / (max / temp) % gsps[j].length]);
			}
			GoodsSpecProperty[] temp_gsps = temp_list
					.toArray(new GoodsSpecProperty[temp_list.size()]);
			Arrays.sort(temp_gsps, new Comparator() {
				public int compare(Object obj1, Object obj2) {
					// TODO Auto-generated method stub
					GoodsSpecProperty a = (GoodsSpecProperty) obj1;
					GoodsSpecProperty b = (GoodsSpecProperty) obj2;
					if (a.getSpec().getSequence() == b.getSpec().getSequence()) {
						return 0;
					} else {
						return a.getSpec().getSequence() > b.getSpec()
								.getSequence() ? 1 : -1;
					}
				}
			});
			result_list.add(Arrays.asList(temp_gsps));
		}
		return result_list;
	}
	
	/**
	 * 商家规格图片上传
	 * @param request
	 * @param response
	 * @param album_id
	 * @param session_u_id
	 * @param marke
	 */
	@RequestMapping("/seller_spec_upload.htm")
	public void spec_upload(HttpServletRequest request,
			HttpServletResponse response, String album_id, String session_u_id, String marke){
		User user = null;
		Map json_map = new HashMap();
		if (SecurityUserHolder.getCurrentUser() != null) {
			user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
		} else {
			int len = session_u_id.length();
			session_u_id = session_u_id.substring(0, len - 5);
			session_u_id = session_u_id.substring(5, session_u_id.length());
			user = this.userService
					.getObjById(CommUtil.null2Long(session_u_id));
		}
	
		user = user.getParent() == null ? user : user.getParent();
		String path = this.storeTools
				.createUserFolder(request, user.getStore());
		String url = this.storeTools.createUserFolderURL(user.getStore()) + "/spec";
		double csize = CommUtil.fileSize(new File(path));
		double img_remain_size = 0;
		if (user.getStore().getGrade().getSpaceSize() > 0) {
			img_remain_size = CommUtil.div(user.getStore().getGrade()
					.getSpaceSize()
					* 1024 - csize, 1024);
			json_map.put("remainSpace", img_remain_size);
		}
		
			try {
				Map map = CommUtil.httpsaveFileToServer(request, "imgFile_"+marke, url,
						null, null);
				if (map.get("fileName") != "") {
					Accessory image = new Accessory();
					image.setAddTime(new Date());
					image.setExt((String) map.get("mime"));
					image.setPath(url);
					image.setWidth(CommUtil.null2Int(map.get("width")));
					image.setHeight(CommUtil.null2Int(map.get("height")));
					image.setName(CommUtil.null2String(map.get("fileName")));
					image.setUser(user);
					this.accessoryService.save(image);
					
					String urls = this.configService.getSysConfig().getImageWebServer() + "/" + url + "/" + image.getName();
					json_map.put("url", urls);
					json_map.put("id", image.getId());
					json_map.put("marke", marke);
				}	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "上传商品图片", value = "/seller/swf_upload.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/swf_upload.htm")
	public void swf_upload(HttpServletRequest request,
			HttpServletResponse response, String album_id, String session_u_id) {
		Map json_map = new HashMap();
		User user = null;
		if (SecurityUserHolder.getCurrentUser() != null) {
			user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
		} else {
			int len = session_u_id.length();
			session_u_id = session_u_id.substring(0, len - 5);
			session_u_id = session_u_id.substring(5, session_u_id.length());
			user = this.userService
					.getObjById(CommUtil.null2Long(session_u_id));
		}
		user = user.getParent() == null ? user : user.getParent();
		String path = this.storeTools
				.createUserFolder(request, user.getStore());
		String url = this.storeTools.createUserFolderURL(user.getStore());
		double csize = CommUtil.fileSize(new File(path));
		double img_remain_size = 0;
		if (user.getStore().getGrade().getSpaceSize() > 0) {
			img_remain_size = CommUtil.div(user.getStore().getGrade()
					.getSpaceSize()
					* 1024 - csize, 1024);
			json_map.put("remainSpace", img_remain_size);
		}
		if (img_remain_size >= 0) {
			try {
				Map map = CommUtil.httpsaveFileToServer(request, "imgFile", url,
						null, null);
				Map params = new HashMap();
				params.put("store_id", user.getStore().getId());
				List<WaterMark> wms = this.waterMarkService
						.query("select obj from WaterMark obj where obj.store.id=:store_id",
								params, -1, -1);
				if (wms.size() > 0) {
					WaterMark mark = wms.get(0);
					if (mark.isWm_image_open()) {
						String pressImg = request.getSession()
								.getServletContext().getRealPath("")
								+ File.separator
								+ mark.getWm_image().getPath()
								+ File.separator + mark.getWm_image().getName();
						String targetImg = path + File.separator
								+ map.get("fileName");
						int pos = mark.getWm_image_pos();
						float alpha = mark.getWm_image_alpha();
						CommUtil.waterMarkWithImage(pressImg, targetImg, pos,
								alpha);
					}
					if (mark.isWm_text_open()) {
						String targetImg = path + File.separator
								+ map.get("fileName");
						int pos = mark.getWm_text_pos();
						String text = mark.getWm_text();
						String markContentColor = mark.getWm_text_color();
						CommUtil.waterMarkWithText(targetImg, targetImg, text,
								markContentColor,
								new Font(mark.getWm_text_font(), Font.BOLD,
										mark.getWm_text_font_size()), pos, 100f);
					}
				}
				Accessory image = new Accessory();
				image.setAddTime(new Date());
				image.setExt((String) map.get("mime"));
				image.setPath(url);
				image.setWidth(CommUtil.null2Int(map.get("width")));
				image.setHeight(CommUtil.null2Int(map.get("height")));
				image.setName(CommUtil.null2String(map.get("fileName")));
				image.setUser(user);
				Album album = null;
				if (album_id != null && !album_id.equals("")) {
					album = this.albumService.getObjById(CommUtil
							.null2Long(album_id));
				} else {
					album = this.albumService.getDefaultAlbum(user.getId());
					if (album == null) {
						album = new Album();
						album.setAddTime(new Date());
						album.setAlbum_name("默认相册");
						album.setAlbum_sequence(-10000);
						album.setAlbum_default(true);
						this.albumService.save(album);
					}
				}
				image.setAlbum(album);
				this.accessoryService.save(image);
				
				String urls = this.configService.getSysConfig().getImageWebServer()+ "/" + url + "/"
						+ image.getName();
				json_map.put("url", urls);
				// 同步生成小图片
				/*String ext = image.getExt().indexOf(".") < 0 ? "."
						+ image.getExt() : image.getExt();
				String source = request.getSession().getServletContext()
						.getRealPath("/")
						+ image.getPath() + File.separator + image.getName();
				String target = source + "_small" + ext;
				CommUtil.createSmall(source, target, this.configService
						.getSysConfig().getSmallWidth(), this.configService
						.getSysConfig().getSmallHeight());*/
				// 同步生成中等图片
				/*String midext = image.getExt().indexOf(".") < 0 ? "."
						+ image.getExt() : image.getExt();
				String midtarget = source + "_middle" + ext;
				CommUtil.createSmall(source, midtarget, this.configService
						.getSysConfig().getMiddleWidth(), this.configService
						.getSysConfig().getMiddleHeight());
				json_map.put("url", CommUtil.getURL(request) + "/" + url + "/"
						+ image.getName());*/
				json_map.put("id", image.getId());
				double csize2 = CommUtil.fileSize(new File(urls));
				double img_remain_size2 = 0;
				if (user.getStore().getGrade().getSpaceSize() > 0) {
					img_remain_size2 = CommUtil.div(user.getStore().getGrade()
							.getSpaceSize()
							* 1024 - csize2, 1024);
					json_map.put("remainSpace", img_remain_size2);
				} else {
					json_map.put("remainSpace", "null");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			json_map.put("url", "");
			json_map.put("id", "");
			json_map.put("remainSpace", -1);
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "商品图片删除", value = "/seller/goods_image_del.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_image_del.htm")
	public void goods_image_del(HttpServletRequest request,
			HttpServletResponse response, String image_id) {
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		String path = this.storeTools
				.createUserFolder(request, user.getStore());
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			Map<String, Comparable> map = new HashMap();
			Accessory img = this.accessoryService.getObjById(CommUtil
					.null2Long(image_id));
			Boolean ret = false;
			if(img!=null){
				for (Goods goods : img.getGoods_main_list()) {
					goods.setGoods_main_photo(null);
					this.goodsService.update(goods);
				}
				for (Goods goods1 : img.getGoods_list()) {
					goods1.getGoods_photos().remove(img);
					if(goods1.getGoods_inventory_detail() != "" && !goods1.getGoods_inventory_detail().equals("")){
						String inventory_detail = goods1.getGoods_inventory_detail();
						
					}
					this.goodsService.update(goods1);
				}		
				for (CGoods goods2 : img.getC_goods_list()) {
					goods2.getGoods_photos().remove(img);
					this.cGoodsService.update(goods2);
				}		
				ret = this.accessoryService.delete(img.getId());
				if (ret) {
					CommUtil.deleteFile(request.getSession().getServletContext()
							.getRealPath("/")+img.getPath()+File.separator+ 
							img.getName()+"_middle."+img.getExt());
					CommUtil.del_acc(request, img);
				}
				double csize2 = CommUtil.fileSize(new File(path));
				double img_remain_size2 = 0;
				if (user.getStore().getGrade().getSpaceSize() > 0) {
					img_remain_size2 = CommUtil.div(user.getStore().getGrade()
							.getSpaceSize()
							* 1024 - csize2, 1024);
					map.put("remainSpace", img_remain_size2);
				} else {
					map.put("remainSpace", "null");
				}
			}
			map.put("result", ret);
			writer = response.getWriter();
			writer.print(Json.toJson(map));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "发布商品第三步", value = "/seller/add_goods_finish.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/add_goods_finish.htm")
	public ModelAndView add_goods_finish(HttpServletRequest request,
			HttpServletResponse response, String id, String goods_class_id,
			String image_ids, String goods_main_img_id, String user_class_ids,
			String goods_brand_id, String goods_spec_ids,
			String goods_properties, String intentory_details,
			String goods_session, String transport_type, String transport_id,
			String publish_goods_status, String publish_day,
			String publish_hour, String publish_min, String f_code_profix,
			String f_code_count, String advance_date,
			String goods_top_format_id, String goods_bottom_format_id,
			String delivery_area_id, String goods_price, String store_price, String goods_transfee) {
		ModelAndView mv = null;
		String goods_session1 = CommUtil.null2String(request.getSession(false)
				.getAttribute("goods_session"));
		if (goods_session1.equals("")) {
			mv = new JModelAndView(
					"user/default/sellercenter/seller_error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "禁止重复提交表单");
			mv.addObject("url", CommUtil.getURL(request) + "/seller/goods.htm");
		} else {
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			Store store = user.getStore();
			if (user.getParent() != null) {
				store = user.getParent().getStore();
			}
			GoodsClass goodsClass = this.goodsClassService.getObjById(Long// 判断所发布的商品分类是否和店铺经营分类匹配
					.parseLong(goods_class_id));
			boolean contains = false;
			if (store.getGc_detail_info() == null
					|| store.getGc_detail_info().equals("")) {//店铺经营类，[{"gc_list":[2, 8, 3, 6, 7, 5],"m_id":1}]，当店铺申请店铺时选择了详细类目，商家发布商品时只可选择所有的详细类目
				if (goodsClass.getParent().getId().equals(store.getGc_main_id())) {//主营类目Id，当店铺只经营主营类目时（商城1级分类），商家发布商品时可以选择该类目下的所有分类
					contains = true;
				}
				if (goodsClass.getParent().getParent() != null
						&& goodsClass.getParent().getParent().getId()
								.equals(store.getGc_main_id())) {
					contains = true;
				}
			} else {
				Set<GoodsClass> store_gcs = this.storeTools
						.query_store_DetailGc(store.getGc_detail_info());
				contains = store_gcs.contains(goodsClass.getParent());
			}
			if (goods_session1.equals(goods_session) && contains) {
				WebForm wf = new WebForm();
				Goods goods = null;
				String obj_status = null;
				Map temp_params = new HashMap();
				Set<Long> temp_ids = new HashSet<Long>();
				if (id.equals("")) {
					goods = wf.toPo(request, Goods.class);
					goods.setAddTime(new Date());
					goods.setGoods_store(store);
				} else {
					Goods obj = this.goodsService
							.getObjById(Long.parseLong(id));
					BigDecimal old_price = obj.getGoods_current_price();
					obj_status = CommUtil.null2String(obj.getGoods_status());
					goods = (Goods) wf.toPo(request, obj);
					goods.setPrice_history(old_price);
				}
				
				if (goods.getCombin_status() == 2
						|| goods.getActivity_status() == 2) {
				} else {
					goods.setGoods_current_price(goods.getStore_price());
				}
			
				goods.setGoods_name(Jsoup.clean(goods.getGoods_name(),
						Whitelist.none()));
				// 商品详情不可以带有违规标签，如script等等
				goods.setGoods_details(goods
						.getGoods_details());

				goods.setGc(goodsClass);
				if (store.getGrade().getGoods_audit() == 0) {// 根据店铺等级判断是否需要平台商审核
					goods.setGoods_status(-5);
				} else if (store.getGrade().getGoods_audit() == 1) {
					goods.setGoods_status(0);
				}
				// 商品定时发布
				if (publish_goods_status.equals("0")) {
					goods.setGoods_seller_time(new Date());
				}
				if (publish_goods_status.equals("2")) {
					String str = publish_day + " " + publish_hour + ":"
							+ publish_min;
					Date date = CommUtil.formatDate(str, "yyyy-MM-dd HH:mm");
					goods.setGoods_seller_time(date);
				}
				goods.setPublish_goods_status(CommUtil
						.null2Int(publish_goods_status));// 发布审核后状态
				Accessory main_img = null;
				if (goods_main_img_id != null && !goods_main_img_id.equals("")) {
					main_img = this.accessoryService.getObjById(Long
							.parseLong(goods_main_img_id));
				}
				goods.setGoods_main_photo(main_img);
				goods.getGoods_ugcs().clear();
				String[] ugc_ids = user_class_ids.split(",");
				temp_ids.clear();
				List u_class_list = new ArrayList();
				for (String ugc_id : ugc_ids) {
					if (!ugc_id.equals("")) {
						UserGoodsClass ugc = this.userGoodsClassService
								.getObjById(Long.parseLong(ugc_id));
						u_class_list.add(ugc);
					}
				}
				//[店铺中商品所在分类]
				goods.setGoods_ugcs(u_class_list);
				String[] img_ids = image_ids.split(",");
				goods.getGoods_photos().clear();
				temp_ids.clear();
				for (String img_id : img_ids) {
					if (!img_id.equals("")) {
						temp_ids.add(CommUtil.null2Long(img_id));
					}
				}
				if (!temp_ids.isEmpty()) {
					temp_params.clear();
					temp_params.put("ids", temp_ids);
					List<Accessory> temp_list = this.accessoryService
							.query("select new Accessory (id) from Accessory obj where obj.id in(:ids)",
									temp_params, -1, -1);
					goods.getGoods_photos().addAll(temp_list);
				}
				if (goods_brand_id != null && !goods_brand_id.equals("")) {
					GoodsBrand goods_brand = this.goodsBrandService
							.getObjById(Long.parseLong(goods_brand_id));
					goods.setGoods_brand(goods_brand);
				}
				goods.getGoods_specs().clear();
				String[] spec_ids = goods_spec_ids.split(",");
				temp_ids.clear();
				for (String spec_id : spec_ids) {
					if (!spec_id.equals("")) {
						temp_ids.add(CommUtil.null2Long(spec_id));
					}
				}
				if (!temp_ids.isEmpty()) {
					temp_params.clear();
					temp_params.put("ids", temp_ids);
					List<GoodsSpecProperty> temp_list = this.specPropertyService
							.query("select new GoodsSpecProperty(id) from GoodsSpecProperty obj where obj.id in(:ids)",
									temp_params, -1, -1);
					goods.getGoods_specs().addAll(temp_list);
				}
				List<Map> maps = new ArrayList<Map>();
				String[] properties = goods_properties.split(";");
				for (String property : properties) {
					if (!property.equals("")) {
						String[] list = property.split(",");
						Map map = new HashMap();
						map.put("id", list[0]);
						map.put("val", list[1]);
						map.put("name", this.goodsTypePropertyService
								.getObjById(Long.parseLong(list[0])).getName());
						maps.add(map);
					}
				}
				goods.setGoods_property(Json.toJson(maps, JsonFormat.compact()));
				maps.clear();
				boolean warn_suppment = false;
				String[] inventory_list = intentory_details.split(";");
				List<Map> map_list = new ArrayList<Map>();
				String eid = null;
				for (String inventory : inventory_list) {
					if (!inventory.equals("")) {
						String[] list = inventory.split(",");
						Map map = new HashMap();
						try {
							map.put("id", list[0]);
							map.put("count", list[1]);
							map.put("supp", list[2]);
							map.put("price", list[3]);
							map.put("img_id", list[4]);
							map.put("weight", list[5]);
							map.put("length", list[6]);
							map.put("width", list[7]);
							map.put("high", list[8]);
							map.put("goodsSerial", list[9]);
							map.put("goodsDisabled", list[10]);
							if(list[3] != null && !list[3].equals("")){
								double subtract = CommUtil.subtract(list[3], list[2]);
								double	price = CommUtil.div(subtract, list[3]) * 100;
								int current_price = (new Double(price)).intValue();
								map.put("discount_price", current_price);
							}
							if(list[3] != null && !list[3].equals("")){
								map.put("goods_price", list[3]);
							}
							if(list[2] != null && !list[2].equals("")){
								map.put("goods_current_price", list[2]);
							}
						} catch (NullPointerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							map.put("id", "");
							map.put("count", "");
							map.put("supp", "");
							map.put("price", "");
							map.put("img_id", "");
							map.put("weight", "");
							map.put("length", "");
							map.put("width", "");
							map.put("high", "");
							map.put("goodsSerial", "");
							map.put("goodsDisabled", "");
						}
						eid = store.getId()+list[9];
						map.put("eid", eid);
						map_list.add(map);
						maps.add(map);
						if (CommUtil.null2Int(list[2]) > CommUtil
								.null2Int(list[1])) {
							warn_suppment = true;
						}
					}
				}
				if(!maps.isEmpty()){
					List list_discount_price = new ArrayList();
					List list_goods_price = new ArrayList();
					List list_goods_current_price = new ArrayList();
					List goods_inventory = new ArrayList();
					for(Map map : maps){
						if(map.get("discount_price") != null && !map.get("discount_price").equals("")){
							list_discount_price.add(map.get("discount_price"));
						}
						if(map.get("goods_price") != null && !map.get("goods_price").equals("")){
							list_goods_price.add(map.get("goods_price"));
						}
						if(map.get("goods_current_price") != null && !map.get("goods_current_price").equals("")){
							list_goods_current_price.add(map.get("goods_current_price"));
						}
						if(map.get("count") != null){
							goods_inventory.add(map.get("count"));
						}
					}
					//goods.setGoods_discount_rate(list_discount_price.size() == 0 ? 0 : CommUtil.null2Int(Collections.min(list_discount_price)));
					goods.setGoods_price(CommUtil.null2BigDecimal(Collections.min(list_goods_price)));
					goods.setGoods_current_price(list_goods_current_price.size() == 0 ? null : CommUtil.null2BigDecimal(Collections.min(list_goods_current_price)));
					goods.setStore_price(list_goods_current_price.size() == 0 ? null : CommUtil.null2BigDecimal(Collections.min(list_goods_current_price)));
					goods.setGoods_inventory(goods_inventory.size() == 0 ? 0 : CommUtil.null2Int(Collections.min(goods_inventory)));
				}
				
				if (warn_suppment) {
					goods.setWarn_inventory_status(-1);
				} else {
					goods.setWarn_inventory_status(0);
				}
				goods.setGoods_type(1);// 商家发布商品
				if(store.getGrade().getGradeName().equals("China")){
					goods.setGoods_global(1);	
				}else{
					goods.setGoods_global(2);
				}
				/*if (CommUtil.null2Int(goods_transfee) == 0) {// 使用运费模板  //[买家承担运费，调用缺省模板]
					List<Transport> transPort = this.transportService.query("select obj from Transport obj", null, -1, -1);
					for(Transport obj : transPort){
						if(store.getGrade().getGradeName().equals("China") && obj.getTrans_name().equals("China")){
							goods.setTransport(obj);
						}else{
							goods.setTransport(obj);
						}
					}
					
				}*/
		/*		if (CommUtil.null2Int(goods_transfee) == 0) {// 使用运费模板  //[买家承担运费，调用缺省模板]0为买家承担，1为卖家承担
					List<Transport> transPort = this.transportService.query("select obj from Transport obj", null, -1, -1);
					for(Transport obj : transPort){
						if(store.getGrade().getGradeName().equals("China")){
							if(obj.getTrans_name().equals("China")){
								goods.setTransport(obj);
							}
						}else{
							if(store.getGrade().getGradeName().equals("Dubai")){
								if(obj.getTrans_name().equals("Dubai")){
									goods.setTransport(obj);
								}
							}
						}
					}
				}else{
					if(CommUtil.null2Int(goods_transfee) == 1){
						goods.setTransport(null);
					}
				}*/
				goods.setGoods_inventory_detail(Json.toJson(maps,
						JsonFormat.compact()));
				// 商品缺货状态操作
				try {
					if (goods.getInventory_type().equals("all")) {
						int inventory = goods.getGoods_inventory();
						if (CommUtil.null2Int(inventory)
								- goods.getGoods_warn_inventory() > 0) {
							goods.setWarn_inventory_status(0);// 预警状态恢复
						} else {
							goods.setWarn_inventory_status(-1);
						}
					}
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			/*	if (CommUtil.null2Int(transport_type) == 0) {// 使用运费模板
					Transport trans = this.transportService.getObjById(CommUtil
							.null2Long(transport_id));
					goods.setTransport(trans);
				}
				if (CommUtil.null2Int(transport_type) == 1) {// 使用固定运费
					goods.setTransport(null);
				}*/
				
				// 是否为F码商品，是则生成F码
				if (goods.getF_sale_type() == 1
						&& CommUtil.null2Int(f_code_count) > 0) {
					Set<String> set = new HashSet<String>();
					while (true) {
						if (set.size() == CommUtil.null2Int(f_code_count)) {
							break;
						}
						set.add((f_code_profix + CommUtil.randomString(12))
								.toUpperCase());
					}
					List<Map> f_code_maps = new ArrayList<Map>();
					if (!CommUtil.null2String(goods.getGoods_f_code()).equals(
							"")) {
						f_code_maps = Json.fromJson(List.class,
								goods.getGoods_f_code());
					}

					for (String code : set) {
						Map f_code_map = new HashMap();
						f_code_map.put("code", code);
						f_code_map.put("status", 0);// 0表示该F码未使用，1为已经使用
						f_code_maps.add(f_code_map);
					}
					if (f_code_maps.size() > 0) {
						// System.out.println(Json.toJson(f_code_maps,JsonFormat.compact()));
						goods.setGoods_f_code(Json.toJson(f_code_maps,
								JsonFormat.compact()));
					}
				}
				// 是否为预售商品，是则加入发货时间
				if (goods.getAdvance_sale_type() == 1) {
					goods.setAdvance_date(CommUtil.formatDate(advance_date));
					goods.setGoods_status(-5);
				}

				// 添加商品版式
				goods.setGoods_top_format_id(CommUtil
						.null2Long(goods_top_format_id));
				GoodsFormat gf = this.goodsFormatService.getObjById(CommUtil
						.null2Long(goods_top_format_id));
				if (gf != null) {
					goods.setGoods_top_format_content(gf.getGf_content());
				} else {
					goods.setGoods_top_format_content(null);
				}
				goods.setGoods_bottom_format_id(CommUtil
						.null2Long(goods_bottom_format_id));
				gf = this.goodsFormatService.getObjById(CommUtil
						.null2Long(goods_bottom_format_id));
				if (gf != null) {
					goods.setGoods_bottom_format_content(gf.getGf_content());
				} else {
					goods.setGoods_bottom_format_content(null);
				}
				/*goods.setDelivery_area_id(CommUtil.null2Long(delivery_area_id));
				Area de_area = this.areaService.getObjById(CommUtil
						.null2Long(delivery_area_id));
				String delivery_area = de_area.getParent().getParent()
						.getAreaName()
						+ de_area.getParent().getAreaName()
						+ de_area.getAreaName();
				goods.setDelivery_area(delivery_area);*/
				if (id.equals("")) {
					/*if(!goods_price.equals("") && !store_price.equals("") && !goods_price.equals(store_price)){
						double subtract = CommUtil.subtract(goods_price, store_price);
						int	discount_rate = new Double(CommUtil.div(subtract, goods_price)* 100).intValue();
						goods.setGoods_discount_rate(discount_rate);
					}*/
					this.goodsService.save(goods);
					Goods obj = this.goodsService.getObjById(goods.getId());
					for(Map map : map_list){
						if(map.get("goodsDisabled").equals("0")){
							List<GoodsSpecProperty> goodsSpecProperty_list = new ArrayList<GoodsSpecProperty>();
							List<Accessory> accessory_list = new ArrayList<Accessory>();
							CGoods cGoods = new CGoods();
							cGoods.setAddTime(new Date());
							BigDecimal cgoods_price = CommUtil.null2BigDecimal(map.get("price").equals("")? 0 : map.get("price"));
							BigDecimal cgoods_current_price = CommUtil.null2BigDecimal(map.get("supp").equals("") ? 0 : map.get("supp"));
							if(!cgoods_price.equals(cgoods_current_price)){
								double subtract = CommUtil.subtract(cgoods_price, cgoods_current_price);
								double	price = CommUtil.div(subtract, cgoods_price) * 100;
								int current_price = (new Double(price)).intValue();
								//cGoods.setCgoods_discount_rate(current_price);
							}
							cGoods.setGoods_price(cgoods_price);
							cGoods.setDiscount_price(cgoods_current_price);
							cGoods.setGoods_inventory(CommUtil.null2Int(map.get("count")));
							cGoods.setCombination_id(CommUtil.null2String(map.get("id")));
							cGoods.setGoods(obj);
							String img = CommUtil.null2String(map.get("img_id"));
							String[] img_id = img.split("_");
							
							for(String sid : img_id){
								Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(sid));
								if(accessory != null){
									accessory_list.add(accessory);
								}
							}
							if(accessory_list.size() > 0){
								cGoods.setGoods_photos(accessory_list);
							}
							String spr_id = CommUtil.null2String(map.get("id"));
							String[] spec_id = spr_id.split("_");
							for(String ids : spec_id){
								GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(ids));
								if(goodsSpecProperty != null){
									goodsSpecProperty_list.add(goodsSpecProperty);
								}
							}
							cGoods.setC_goods_spec(goodsSpecProperty_list);
							cGoods.setGoods_serial(CommUtil.null2String(map.get("goodsSerial")));
							cGoods.setGoods_weight(CommUtil.null2Int(map.get("weight")));
							cGoods.setGoods_length(CommUtil.null2Int(map.get("length")));
							cGoods.setGoods_width(CommUtil.null2Int(map.get("width")));
							cGoods.setGoods_high(CommUtil.null2Int(map.get("high")));
							cGoods.setEid(CommUtil.null2String(map.get("eid")));
							cGoods.setGoods_disabled(CommUtil.null2String(map.get("goodsDisabled")));
							this.cGoodsService.save(cGoods);	
						}
					}
					
					// 生成商品二维码
					/*String uploadFilePath = this.configService.getSysConfig()
							.getUploadFilePath();
					goods.setQr_img_path(CommUtil.getURL(request) + "/"
							+ uploadFilePath + "/" + "goods_qr" + "/"
							+ goods.getId() + "_qr.jpg");
					this.goodsTools
							.createGoodsQR(request, CommUtil.null2String(goods
									.getId()), uploadFilePath, goods
									.getGoods_main_photo().getId());*/
					// 添加lucene索引 添加商品去除商品索引添加，后台审核商品时添加商品索引 获取项目运行路径 
				/*	String goods_lucene_path = System.getProperty("metoob2b2c.root")
							+ File.separator + "luence" + File.separator
							+ "goods";
					File file = new File(goods_lucene_path);
					if (!file.exists()) {
						CommUtil.createFolder(goods_lucene_path);
					}
					LuceneVo vo = this.luceneVoTools.updateGoodsIndex(goods);
					
					SysConfig config = this.configService.getSysConfig();
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setConfig(config);
					lucene.setIndex_path(goods_lucene_path);
					lucene.writeIndex(vo);*/
				} else {
					// 更新lucene索引 更新索引会导致需要审核的店铺，发布商品等待平台审核时，可以被搜索
					String goods_lucene_path = System.getProperty("metoob2b2c.root")
							+ File.separator + "luence" + File.separator
							+ "goods";
					if ("0".equals(obj_status)
							&& "0".equals(publish_goods_status)) {// 编辑后直接上架
						// 更新lucene索引
					/*	File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneVo vo = this.luceneVoTools
								.updateGoodsIndex(goods);
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.update(CommUtil.null2String(goods.getId()), vo);*/
						// 删除lucene索引
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.delete_index(id);
						
					}
					if ("-5".equals(obj_status)
							&& "0".equals(publish_goods_status)) {// (未审核)在仓库中编辑后上架
						// 更新lucene索引
						/*File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneVo vo = this.luceneVoTools
								.updateGoodsIndex(goods);
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.update(CommUtil.null2String(goods.getId()), vo);*/
						// 删除lucene索引
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.delete_index(id);
						
					}
					if ("0".equals(obj_status)
							&& ("1".equals(publish_goods_status) || "2"
									.equals(publish_goods_status))) {// 编辑后放入仓库
						// 删除lucene索引
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.delete_index(id);
					}
					if (("1".equals(obj_status) || "2".equals(obj_status))
							&& "0".equals(publish_goods_status)) {// 在仓库中编辑后上架
						// 添加lucene索引
						LuceneVo vo = this.luceneVoTools
								.updateGoodsIndex(goods);
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.writeIndex(vo);
						
					}
					if(!goods_price.equals("") && !store_price.equals("") && !goods_price.equals("store_price")){
						double subtract = CommUtil.subtract(goods_price, store_price);
						double	price = CommUtil.div(subtract, goods_price) * 100;
						int current_price = (new Double(price)).intValue();
						///goods.setGoods_discount_rate(current_price);
					}
					this.goodsService.update(goods);

					//标记有效果在这
					List<CGoods> cgoods_list = goods.getCgoods();
					List<String> combination_ids = new ArrayList<String>();
					if(!cgoods_list.isEmpty()){
						for(CGoods cgoods_id : cgoods_list){
							//[spec的组合id]
							combination_ids.add(CommUtil.null2String(cgoods_id.getCombination_id()));
						}
					}
					
					// 商品编辑 如果没有子商品判断子商品为空时直接添加新增的所有子规格 例如：没有规格的商品增加规格后编辑第一次增加规格
					CGoods cGoods = null;
					if(cgoods_list.isEmpty()){
						for(Map map : map_list){
							if(map.get("goodsDisabled").equals("0")){
								List<GoodsSpecProperty> goodsSpecProperty_list = new ArrayList<GoodsSpecProperty>();
								List<Accessory> accessory_list = new ArrayList<Accessory>();
								cGoods = new CGoods();
								cGoods.setAddTime(new Date());
								BigDecimal cgoods_price = CommUtil.null2BigDecimal(map.get("price").equals("")? 0 : map.get("price"));
								BigDecimal cgoods_current_price = CommUtil.null2BigDecimal(map.get("supp").equals("") ? 0 : map.get("supp"));
								if(!cgoods_price.equals(cgoods_current_price)){
									double subtract = CommUtil.subtract(cgoods_price, cgoods_current_price);
									int	discount_rate = new Double(CommUtil.div(subtract, cgoods_price) * 100).intValue();
									//cGoods.setCgoods_discount_rate(discount_rate);
								}
								cGoods.setGoods_price(cgoods_price);
								cGoods.setDiscount_price(cgoods_current_price);
								cGoods.setGoods_inventory(CommUtil.null2Int(map.get("count")));
								cGoods.setCombination_id(CommUtil.null2String(map.get("id")));
								cGoods.setGoods(goods);
								String img = CommUtil.null2String(map.get("img_id"));
								String[] img_id = img.split("_");
								for(String sid : img_id){
									Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(sid));
									if(accessory != null){
										accessory_list.add(accessory);
									}
								}
								if(accessory_list.size() > 0){
									cGoods.setGoods_photos(accessory_list);
								}
								String spec_id = CommUtil.null2String(map.get("id"));
								String[] spec_ids2 = spec_id.split("_");
								for(String ids : spec_ids2){
									GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(ids));
									if(goodsSpecProperty != null){
										goodsSpecProperty_list.add(goodsSpecProperty);
									}
								}
								cGoods.setC_goods_spec(goodsSpecProperty_list);
								cGoods.setGoods_serial(CommUtil.null2String(map.get("goodsSerial")));
								cGoods.setGoods_weight(CommUtil.null2Int(map.get("weight")));
								cGoods.setGoods_length(CommUtil.null2Int(map.get("length")));
								cGoods.setGoods_width(CommUtil.null2Int(map.get("width")));
								cGoods.setGoods_high(CommUtil.null2Int(map.get("high")));
								cGoods.setGoods_disabled(CommUtil.null2String(map.get("goodsDisabled")));
								cGoods.setEid(CommUtil.null2String(map.get("eid")));
								this.cGoodsService.save(cGoods);
							}
						}
						// 如果有子商品，判断本次提交是否有新增子商品 有新增，编辑已有子商品并新增
					}else{
						// 编辑时本次提交的子商品中可能存在移除的规格，删除对应的子商品表中的数据
						if(goods != null){
							List<CGoods> cgoods = goods.getCgoods();
							List<HashMap> list = Json.fromJson(ArrayList.class,
									goods.getGoods_inventory_detail());
							List<String> spec_id = new ArrayList<String>();;
							for(Map map : list){
								String ids = CommUtil.null2String(map.get("id"));
								 spec_id.add(ids);
							}
							for(CGoods cobj : cgoods){
								if(!spec_id.contains(cobj.getCombination_id())){
									this.cGoodsService.delete(cobj.getId());
								}
							}
						}
						// 遍历子商品 获取combination_id 比较前端传来的id
						List<Map> cGoods_list = new ArrayList<Map>();
						List<String>  cGoods_ids= new ArrayList<String>();
							for(CGoods cgoods : cgoods_list){
								Map map = new HashMap();
								map.put("cid", cgoods.getCombination_id());
								map.put("gid", cgoods.getId());
								cGoods_ids.add(cgoods.getCombination_id());
								cGoods_list.add(map);
							}
							
							for(Map map : map_list){
								if(map.get("goodsDisabled").equals("0")){
									List<GoodsSpecProperty> goodsSpecPropertyList = new ArrayList<GoodsSpecProperty>();
									List<Accessory> accessory_list = new ArrayList<Accessory>();
									if(cGoods_ids.contains(CommUtil.null2String(map.get("id")))){ 
										Map params = new HashMap();
										params.put("combination_id", CommUtil.null2String(map.get("id")));
										Set<Long> ids = new HashSet<Long>();
										for(Map cgoods : cGoods_list){
											ids.add(CommUtil.null2Long(cgoods.get("gid")));
										}
										params.put("ids", ids);
										CGoods editCgoods = this.cGoodsService.query("select obj from CGoods obj where obj.combination_id=:combination_id and obj.id in(:ids)", params, -1, -1).get(0);
										long id1 = editCgoods.getId();
										if(editCgoods != null){
											if(map.get("price").equals("")){
												editCgoods.setGoods(goods);
												//保存子商品规格
												String spr_id = CommUtil.null2String(map.get("id"));
												String[] specs = spr_id.split("_");
												for(String spec_id : specs){
													GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(spec_id));
													if(goodsSpecProperty != null){
														goodsSpecPropertyList.add(goodsSpecProperty);
													}
												}
												editCgoods.setC_goods_spec(goodsSpecPropertyList);
												editCgoods.setEid(CommUtil.null2String(map.get("eid")));
												editCgoods.setGoods_disabled(CommUtil.null2String(map.get("goodsDisabled")));
												this.cGoodsService.update(editCgoods);
											}else{
												BigDecimal cgoods_price = CommUtil.null2BigDecimal(map.get("price").equals("")? 0 : map.get("price"));
												BigDecimal cgoods_current_price = CommUtil.null2BigDecimal(map.get("supp").equals("") ? 0 : map.get("supp"));
												if(!cgoods_price.equals(cgoods_current_price)){
													double subtract = CommUtil.subtract(cgoods_price, cgoods_current_price);
													int discount_rate = (new Double(CommUtil.div(subtract, cgoods_price) * 100)).intValue();
													//editCgoods.setCgoods_discount_rate(discount_rate);
												}
												editCgoods.setGoods_price(cgoods_price);
												editCgoods.setDiscount_price(cgoods_current_price);
												editCgoods.setGoods_inventory(CommUtil.null2Int(map.get("count")));
												editCgoods.setGoods(goods);
												//保存子商品规格
												String spr_id = CommUtil.null2String(map.get("id"));
												String[] specs = spr_id.split("_");
												for(String spec_id : specs){
													GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(spec_id));
													if(goodsSpecProperty != null){
														goodsSpecPropertyList.add(goodsSpecProperty);
													}
												}
													editCgoods.setC_goods_spec(goodsSpecPropertyList);
													
													//保存子商品图片
													String img = CommUtil.null2String(map.get("img_id"));
													String[] img_id = img.split("_");
													for(String sid : img_id){
														Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(sid));
														if(accessory != null){
															accessory_list.add(accessory);
														}
													}
													if(accessory_list.size() > 0){
														editCgoods.setGoods_photos(accessory_list);
													}
													editCgoods.setC_goods_spec(goodsSpecPropertyList);
													editCgoods.setGoods_serial(CommUtil.null2String(map.get("goodsSerial")));
													editCgoods.setGoods_weight(CommUtil.null2Int(map.get("weight")));
													editCgoods.setGoods_length(CommUtil.null2Int(map.get("length")));
													editCgoods.setGoods_width(CommUtil.null2Int(map.get("width")));
													editCgoods.setGoods_high(CommUtil.null2Int(map.get("high")));
													editCgoods.setEid(CommUtil.null2String(map.get("eid")));
													editCgoods.setGoods_disabled(CommUtil.null2String(map.get("goodsDisabled")));
											}
											
											this.cGoodsService.update(editCgoods);
										}
										
									}else{
										cGoods = new CGoods();
										cGoods.setAddTime(new Date());
										cGoods.setCombination_id(CommUtil.null2String(map.get("id")));
										BigDecimal cgoods_price = CommUtil.null2BigDecimal(map.get("price").equals("")? 0 : map.get("price"));
										BigDecimal cgoods_current_price = CommUtil.null2BigDecimal(map.get("supp").equals("") ? 0 : map.get("supp"));
										if(!cgoods_price.equals(cgoods_current_price)){
											double subtract = CommUtil.subtract(cgoods_price, cgoods_current_price);
											double	price = CommUtil.div(subtract, cgoods_price) * 100;
											int current_price = (new Double(price)).intValue();
											//cGoods.setCgoods_discount_rate(current_price);
										}
										cGoods.setGoods_price(cgoods_price);
										cGoods.setDiscount_price(cgoods_current_price);
										cGoods.setGoods_inventory(CommUtil.null2Int(map.get("count")));
										cGoods.setGoods(goods);
										String img = CommUtil.null2String(map.get("img_id"));
										String[] img_id = img.split("_");
										//保存子商品规格
										String spr_id = CommUtil.null2String(map.get("id"));
										String[] spec_id = spr_id.split("_");
										for(String ids : spec_id){
											GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(ids));
											if(goodsSpecProperty != null){
												goodsSpecPropertyList.add(goodsSpecProperty);
											}
										}
										cGoods.setC_goods_spec(goodsSpecPropertyList);
										//[保存子规格]
										for(String sid : img_id){
											Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(sid));
											if(accessory != null){
												accessory_list.add(accessory);
											}
										}
										if(accessory_list.size() > 0){
											cGoods.setGoods_photos(accessory_list);
										}
										cGoods.setC_goods_spec(goodsSpecPropertyList);
										cGoods.setGoods_serial(CommUtil.null2String(map.get("goodsSerial")));
										cGoods.setGoods_weight(CommUtil.null2Int(map.get("weight")));
										cGoods.setGoods_length(CommUtil.null2Int(map.get("length")));
										cGoods.setGoods_width(CommUtil.null2Int(map.get("width")));
										cGoods.setGoods_high(CommUtil.null2Int(map.get("high")));
										cGoods.setEid(CommUtil.null2String(map.get("eid")));
										cGoods.setGoods_disabled(CommUtil.null2String(map.get("goodsDisabled")));
										this.cGoodsService.save(cGoods);
									}
								}
							}
						}
				}
				String goods_view_url = CommUtil.getURL(request) + "/goods_"
						+ goods.getId() + ".htm";
				if (this.configService.getSysConfig().isSecond_domain_open()
						&& goods.getGoods_store().getStore_second_domain() != ""
						&& goods.getGoods_type() == 1) {
					String store_second_domain = "http://"
							+ goods.getGoods_store().getStore_second_domain()
							+ "." + CommUtil.generic_domain(request);
					goods_view_url = store_second_domain + "/goods_"
							+ goods.getId() + ".htm";
				}
				if (id == null || id.equals("")) {
					mv = new JModelAndView(
							"user/default/sellercenter/add_goods_finish.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 0, request,
							response);
					if (store.getGrade().getGoods_audit() == 0) {
						mv.addObject("op_title", "商品发布成功,运营商会尽快为您审核！");
					}
					if (store.getGrade().getGoods_audit() == 1) {
						mv.addObject("op_title", "商品发布成功！");
					}
				} else {
					mv = new JModelAndView(
							"user/default/sellercenter/seller_success.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 0, request,
							response);
					if (store.getGrade().getGoods_audit() == 0) {
						mv.addObject("op_title", "商品编辑成功,运营商会尽快为您审核");
						mv.addObject("url", CommUtil.getURL(request)
								+ "/seller/goods.htm");
					}
					if (store.getGrade().getGoods_audit() == 1) {
						mv.addObject("op_title", "商品编辑成功");
						mv.addObject("url", CommUtil.getURL(request)
								+ "/seller/goods.htm");
					}
				}
				mv.addObject("goods_view_url", goods_view_url);
				mv.addObject("goods_edit_url", CommUtil.getURL(request)
						+ "/seller/goods_edit.htm?id=" + goods.getId());
				request.getSession(false).removeAttribute("goods_session");
			} else {
				mv = new JModelAndView(
						"user/default/sellercenter/seller_error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request,
						response);
				mv.addObject("op_title", "参数错误");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/seller/goods.htm");
			}
		}
		return mv;
	}
	
	
	
	@SecurityMapping(title = "发布商品上传图片", value = "/seller/add_goods_image.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/add_goods_image.htm")
	public void add_goods_image(HttpServletRequest request,
			HttpServletResponse response) {
		Map json_map = new HashMap();
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		String path = this.storeTools
				.createUserFolder(request, user.getStore());
		String url = this.storeTools.createUserFolderURL(user.getStore());
		double csize = CommUtil.fileSize(new File(path));
		double img_remain_size = 0;
		if (user.getStore().getGrade().getSpaceSize() > 0) {
			img_remain_size = CommUtil.div(user.getStore().getGrade()
					.getSpaceSize()
					* 1024 - csize, 1024);
			json_map.put("remainSpace", img_remain_size);
		}
		if (img_remain_size >= 0) {
			try {
				Map map = CommUtil.saveFileToServer(request, "imgFile", path,
						null, null);
				Map params = new HashMap();
				params.put("store_id", user.getStore().getId());
				List<WaterMark> wms = this.waterMarkService
						.query("select obj from WaterMark obj where obj.store.id=:store_id",
								params, -1, -1);
				if (wms.size() > 0) {
					WaterMark mark = wms.get(0);
					if (mark.isWm_image_open()) {
						String pressImg = request.getSession()
								.getServletContext().getRealPath("")
								+ File.separator
								+ mark.getWm_image().getPath()
								+ File.separator + mark.getWm_image().getName();
						System.out.println(mark.getWm_image().getPath()
								.replace("//", File.separator));
						System.out.println(mark.getWm_image().getName());
						String targetImg = path + File.separator
								+ map.get("fileName");
						int pos = mark.getWm_image_pos();
						float alpha = mark.getWm_image_alpha();
						CommUtil.waterMarkWithImage(pressImg, targetImg, pos,
								alpha);
					}
					if (mark.isWm_text_open()) {
						String targetImg = path + File.separator
								+ map.get("fileName");
						int pos = mark.getWm_text_pos();
						String text = mark.getWm_text();
						String markContentColor = mark.getWm_text_color();
						CommUtil.waterMarkWithText(targetImg, targetImg, text,
								markContentColor,
								new Font(mark.getWm_text_font(), Font.BOLD,
										mark.getWm_text_font_size()), pos, 100f);
					}
				}
				Accessory image = new Accessory();
				image.setAddTime(new Date());
				image.setExt((String) map.get("mime"));
				image.setPath(url);
				image.setWidth(CommUtil.null2Int(map.get("width")));
				image.setHeight(CommUtil.null2Int(map.get("height")));
				image.setName(CommUtil.null2String(map.get("fileName")));
				image.setUser(user);
				Album album = this.albumService.getDefaultAlbum(user.getId());
				if (album == null) {
					album = new Album();
					album.setAddTime(new Date());
					album.setAlbum_name("默认相册");
					album.setAlbum_sequence(-10000);
					album.setAlbum_default(true);
					this.albumService.save(album);
				}
				image.setAlbum(album);
				this.accessoryService.save(image);
				// 同步生成小图片
				String ext = image.getExt().indexOf(".") < 0 ? "."
						+ image.getExt() : image.getExt();
				String source = request.getSession().getServletContext()
						.getRealPath("/")
						+ image.getPath() + File.separator + image.getName();
				String target = source + "_small" + ext;
				CommUtil.createSmall(source, target, this.configService
						.getSysConfig().getSmallWidth(), this.configService
						.getSysConfig().getSmallHeight());
				// 同步生成中等图片
				String midext = image.getExt().indexOf(".") < 0 ? "."
						+ image.getExt() : image.getExt();
				String midtarget = source + "_middle" + ext;
				CommUtil.createSmall(source, midtarget, this.configService
						.getSysConfig().getMiddleWidth(), this.configService
						.getSysConfig().getMiddleHeight());
				json_map.put("url", CommUtil.getURL(request) + "/" + url + "/"
						+ image.getName());
				json_map.put("id", image.getId());
				double csize2 = CommUtil.fileSize(new File(path));
				double img_remain_size2 = 0;
				if (user.getStore().getGrade().getSpaceSize() > 0) {
					img_remain_size2 = CommUtil.div(user.getStore().getGrade()
							.getSpaceSize()
							* 1024 - csize2, 1024);
					json_map.put("remainSpace", img_remain_size2);
				} else {
					json_map.put("remainSpace", "null");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			json_map.put("url", "");
			json_map.put("id", "");
			json_map.put("remainSpace", -1);
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * AJAX加载商品分类数据
	 * 
	 * @param request
	 * @param response
	 * @param pid
	 *            上级分类Id
	 * @param session
	 *            是否加载到session中
	 */
	@SecurityMapping(title = "加载商品分类", value = "/seller/load_goods_class.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/load_goods_class.htm")
	public void load_goods_class(HttpServletRequest request,
			HttpServletResponse response, String pid, String session) {
		GoodsClass obj = this.goodsClassService.getObjById(CommUtil
				.null2Long(pid));
		List<Map> list = new ArrayList<Map>();
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		if (obj != null) {
			if (obj.getLevel() == 0) {// 加载二级分类
				Map map = this.storeTools.query_MainGc_Map(
						CommUtil.null2String(obj.getId()),
						store.getGc_detail_info());
				if (map != null) {
					List<Integer> ls = (List) map.get("gc_list");
					if (ls != null && !ls.equals("")) {
						for (Integer l : ls) {
							Map map_gc = new HashMap();
							GoodsClass gc = this.goodsClassService
									.getObjById(CommUtil.null2Long(l));
							map_gc.put("id", gc.getId());
							map_gc.put("className", gc.getClassName());
							list.add(map_gc);
						}
					}
				}
			} else if (obj.getLevel() == 1) {// 加载三级分类
				for (GoodsClass child : obj.getChilds()) {
					Map map_gc = new HashMap();
					map_gc.put("id", child.getId());
					map_gc.put("className", child.getClassName());
					list.add(map_gc);
				}
			}
			if (CommUtil.null2Boolean(session)) {
				request.getSession(false).setAttribute("goods_class_info", obj);
			}
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(list));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "添加用户常用商品分类", value = "/seller/load_goods_class.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/add_goods_class_staple.htm")
	public void add_goods_class_staple(HttpServletRequest request,
			HttpServletResponse response) {
		String ret = "error";
		if (request.getSession(false).getAttribute("goods_class_info") != null) {
			GoodsClass session_gc = (GoodsClass) request.getSession(false)
					.getAttribute("goods_class_info");
			GoodsClass gc = this.goodsClassService.getObjById(session_gc
					.getId());
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			user = user.getParent() == null ? user : user.getParent();
			String json = "";
			List<Map> list_map = new ArrayList<Map>();
			if (user.getStaple_gc() != null && !user.getStaple_gc().equals("")) {
				json = user.getStaple_gc();
				list_map = Json.fromJson(List.class, json);
			}
			if (list_map.size() > 0) {
				boolean flag = true;
				for (Map staple : list_map) {
					if (gc.getId().toString()
							.equals(CommUtil.null2String(staple.get("id")))) {
						flag = false;
						break;
					}
				}
				if (flag) {
					Map map = new HashMap();
					map.put("name",
							gc.getParent().getParent().getClassName() + ">"
									+ gc.getParent().getClassName() + ">"
									+ gc.getClassName());
					map.put("id", gc.getId());
					list_map.add(map);
					json = Json.toJson(list_map, JsonFormat.compact());
					ret = Json.toJson(map);
				}
			} else {
				Map map = new HashMap();
				map.put("name",
						gc.getParent().getParent().getClassName() + ">"
								+ gc.getParent().getClassName() + ">"
								+ gc.getClassName());
				map.put("id", gc.getId());
				map.put("id", gc.getId());
				list_map.add(map);
				json = Json.toJson(list_map, JsonFormat.compact());
				ret = Json.toJson(map);
			}
			user.setStaple_gc(json);
			this.userService.update(user);
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "删除用户常用商品分类", value = "/seller/del_goods_class_staple.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/del_goods_class_staple.htm")
	public void del_goods_class_staple(HttpServletRequest request,
			HttpServletResponse response, String id) {
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		List<Map> list_map = Json.fromJson(List.class, user.getStaple_gc());
		boolean ret = false;
		for (Map map : list_map) {
			if (CommUtil.null2String(map.get("id")).equals(id)) {
				ret = list_map.remove(map);
			}
		}
		user.setStaple_gc(Json.toJson(list_map, JsonFormat.compact()));
		this.userService.update(user);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "根据用户常用商品分类加载分类信息", value = "/seller/load_goods_class_staple.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/load_goods_class_staple.htm")
	public void load_goods_class_staple(HttpServletRequest request,
			HttpServletResponse response, String id, String name) {
		GoodsClass obj = null;
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (id != null && !id.equals("")) {
			List<Map> list_map = Json.fromJson(List.class, user.getStaple_gc());
			for (Map map : list_map) {
				if (CommUtil.null2String(map.get("id")).equals(id)) {
					obj = this.goodsClassService.getObjById(CommUtil
							.null2Long(map.get("id")));
				}
			}
		}
		if (name != null && !name.equals(""))
			obj = this.goodsClassService.getObjByProperty(null, "className",
					name);
		List<List<Map>> list = new ArrayList<List<Map>>();
		if (obj != null) {
			// 该版本要求三级分类才能添加到常用分类
			request.getSession(false).setAttribute("goods_class_info", obj);
			Map params = new HashMap();
			List<Map> second_list = new ArrayList<Map>();
			List<Map> third_list = new ArrayList<Map>();
			List<Map> other_list = new ArrayList<Map>();

			if (obj.getLevel() == 2) {
				params.put("pid", obj.getParent().getParent().getId());
				List<GoodsClass> second_gcs = this.goodsClassService
						.query("select obj from GoodsClass obj where obj.parent.id=:pid order by obj.sequence asc",
								params, -1, -1);
				for (GoodsClass gc : second_gcs) {
					Map map = new HashMap();
					map.put("id", gc.getId());
					map.put("className", gc.getClassName());
					second_list.add(map);
				}
				params.clear();
				params.put("pid", obj.getParent().getId());
				List<GoodsClass> third_gcs = this.goodsClassService
						.query("select obj from GoodsClass obj where obj.parent.id=:pid order by obj.sequence asc",
								params, -1, -1);
				for (GoodsClass gc : third_gcs) {
					Map map = new HashMap();
					map.put("id", gc.getId());
					map.put("className", gc.getClassName());
					third_list.add(map);
				}
			}

			if (obj.getLevel() == 1) {
				params.clear();
				params.put("pid", obj.getParent().getId());
				List<GoodsClass> third_gcs = this.goodsClassService
						.query("select obj from GoodsClass obj where obj.parent.id=:pid order by obj.sequence asc",
								params, -1, -1);
				for (GoodsClass gc : third_gcs) {
					Map map = new HashMap();
					map.put("id", gc.getId());
					map.put("className", gc.getClassName());
					second_list.add(map);
				}
			}

			Map map = new HashMap();
			String staple_info = this.generic_goods_class_info(obj);
			map.put("staple_info",
					staple_info.substring(0, staple_info.length() - 1));
			other_list.add(map);

			list.add(second_list);
			list.add(third_list);
			list.add(other_list);
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(list, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String generic_goods_class_info(GoodsClass gc) {
		String goods_class_info = gc.getClassName() + ">";
		if (gc.getParent() != null) {
			String class_info = generic_goods_class_info(gc.getParent());
			goods_class_info = class_info + goods_class_info;
		}
		return goods_class_info;
	}

	@SecurityMapping(title = "出售中的商品列表", value = "/seller/goods.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods.htm")
	public ModelAndView goods(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String goods_name, String user_class_id, String goods_serial) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
			if(CommUtil.null2String(goods_serial).equals("")){
				User user = this.userService.getObjById(SecurityUserHolder
						.getCurrentUser().getId());
				user = user.getParent() == null ? user : user.getParent();
				GoodsQueryObject qo = new GoodsQueryObject(currentPage, mv, orderBy,
						orderType);
				qo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
				qo.addQuery("obj.goods_store.id", new SysMap("goods_store_id", user
						.getStore().getId()), "=");
				qo.setOrderBy("warn_inventory_status,addTime");
				qo.setOrderType("desc");
				if (goods_name != null && !goods_name.equals("")) {
					qo.addQuery("obj.goods_name", new SysMap("goods_name", "%"
							+ goods_name + "%"), "like");
				}
				if (user_class_id != null && !user_class_id.equals("")) {
					UserGoodsClass ugc = this.userGoodsClassService.getObjById(Long
							.parseLong(user_class_id));
					qo.addQuery("ugc", ugc, "obj.goods_ugcs", "member of");
				}
				IPageList pList = this.goodsService.list(qo);
				CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
				mv.addObject("flag_main","0");
			}else{
				Map params = new HashMap();
				List<Map> clist1 = new ArrayList<Map>();
				params.put("goods_serial", goods_serial);
				List<Goods> cgoodslists = this.goodsService.query("select obj from Goods obj where obj.goods_serial=:goods_serial", params, -1, -1);
				if(cgoodslists.size()>0){
					mv.addObject("objs", cgoodslists);
					mv.addObject("flag","0");
				}else{
					params.clear();
					params.put("goods_serial", goods_serial);
					List<Map> clist = new ArrayList<Map>();
					List<CGoods> cgoodslist = this.cGoodsService.query("select obj from CGoods obj where obj.goods_serial=:goods_serial", params, -1, -1);
					mv.addObject("objs", cgoodslist);
					mv.addObject("flag","1");
				}
			}

		mv.addObject("storeTools", storeTools);
		mv.addObject("goodsViewTools", goodsViewTools);
		mv.addObject("goods_name", goods_name);
		mv.addObject("goods_serial", goods_serial);
		mv.addObject("user_class_id", user_class_id);
		return mv;
	}

	@SecurityMapping(title = "仓库中的商品列表", value = "/seller/goods_storage.htm*", rtype = "seller", rname = "仓库中的商品", rcode = "goods_storage_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_storage.htm")
	public ModelAndView goods_storage(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String goods_name, String user_class_id, String goods_serial) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_storage.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if(CommUtil.null2String(goods_serial).equals("")){
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			user = user.getParent() == null ? user : user.getParent();
			GoodsQueryObject qo = new GoodsQueryObject(currentPage, mv, orderBy,
					orderType);
			Map prarms = new HashMap();
			Set ids = new TreeSet();
			ids.add(1);// 仓库中商品
			ids.add(-5);// 未审核商品
			prarms.put("ids", ids);
			qo.addQuery("obj.goods_status in (:ids)", prarms);
			qo.addQuery("obj.goods_store.id", new SysMap("goods_store_id", user
					.getStore().getId()), "=");
			qo.setOrderBy("goods_seller_time");
			qo.setOrderType("desc");
			if (goods_name != null && !goods_name.equals("")) {
				qo.addQuery("obj.goods_name", new SysMap("goods_name", "%"
						+ goods_name + "%"), "like");
			}
			if (user_class_id != null && !user_class_id.equals("")) {
				UserGoodsClass ugc = this.userGoodsClassService.getObjById(Long
						.parseLong(user_class_id));
				qo.addQuery("ugc", ugc, "obj.goods_ugcs", "member of");
			}
			IPageList pList = this.goodsService.list(qo);
			CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);	
			mv.addObject("flag_main","0");
		}else{
			Map params = new HashMap();
			List<Map> clist1 = new ArrayList<Map>();
			params.put("goods_serial", goods_serial);
			List<Goods> cgoodslists = this.goodsService.query("select obj from Goods obj where obj.goods_serial=:goods_serial", params, -1, -1);
			if(cgoodslists.size()>0){
				mv.addObject("objs", cgoodslists);
				mv.addObject("flag","0");
			}else{
				params.clear();
				params.put("goods_serial", goods_serial);
				List<Map> clist = new ArrayList<Map>();
				List<CGoods> cgoodslist = this.cGoodsService.query("select obj from CGoods obj where obj.goods_serial=:goods_serial", params, -1, -1);
				mv.addObject("objs", cgoodslist);
				mv.addObject("flag","1");
			}
		}
		mv.addObject("storeTools", storeTools);
		mv.addObject("goodsViewTools", goodsViewTools);
		mv.addObject("goods_name", goods_name);
		mv.addObject("goods_serial", goods_serial);
		mv.addObject("user_class_id", user_class_id);
		return mv;
	}

	@SecurityMapping(title = "违规下架商品", value = "/seller/goods_out.htm*", rtype = "seller", rname = "违规下架商品", rcode = "goods_out_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_out.htm")
	public ModelAndView goods_out(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String goods_name, String user_class_id, String goods_serial) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_out.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if(CommUtil.null2String(goods_serial).equals("")){
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			user = user.getParent() == null ? user : user.getParent();
			GoodsQueryObject qo = new GoodsQueryObject(currentPage, mv, orderBy,
					orderType);
			qo.addQuery("obj.goods_status", new SysMap("goods_status", -2), "=");
			qo.addQuery("obj.goods_store.id", new SysMap("goods_store_id", user
					.getStore().getId()), "=");
			qo.setOrderBy("goods_seller_time");
			qo.setOrderType("desc");
			if (goods_name != null && !goods_name.equals("")) {
				qo.addQuery("obj.goods_name", new SysMap("goods_name", "%"
						+ goods_name + "%"), "like");
			}
			if (user_class_id != null && !user_class_id.equals("")) {
				UserGoodsClass ugc = this.userGoodsClassService.getObjById(Long
						.parseLong(user_class_id));
				qo.addQuery("ugc", ugc, "obj.goods_ugcs", "member of");
			}
			IPageList pList = this.goodsService.list(qo);
			CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);	
			mv.addObject("flag_main","0");
		}else{
			Map params = new HashMap();
			List<Map> clist1 = new ArrayList<Map>();
			params.put("goods_serial", goods_serial);
			List<Goods> cgoodslists = this.goodsService.query("select obj from Goods obj where obj.goods_serial=:goods_serial", params, -1, -1);
			if(cgoodslists.size()>0){
				mv.addObject("objs", cgoodslists);
				mv.addObject("flag","0");
			}else{
				params.clear();
				params.put("goods_serial", goods_serial);
				List<Map> clist = new ArrayList<Map>();
				List<CGoods> cgoodslist = this.cGoodsService.query("select obj from CGoods obj where obj.goods_serial=:goods_serial", params, -1, -1);
				mv.addObject("objs", cgoodslist);
				mv.addObject("flag","1");
			}
		}
		mv.addObject("storeTools", storeTools);
		mv.addObject("goodsViewTools", goodsViewTools);
		mv.addObject("goods_name", goods_name);
		mv.addObject("user_class_id", user_class_id);
		return mv;
	}

	@SecurityMapping(title = "商品编辑", value = "/seller/goods_edit.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_edit.htm")
	public ModelAndView goods_edit(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/add_goods_second.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Goods obj = this.goodsService.getObjById(Long.parseLong(id));
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		String uploadFilePath = this.configService.getSysConfig()
				.getUploadFilePath();
		if (obj.getGoods_store().getUser().getId().equals(user.getId())) {
			String path = request.getSession().getServletContext()
					.getRealPath("/")
					+ File.separator
					+ uploadFilePath
					+ File.separator
					+ "store" + File.separator + user.getStore().getId();
			if(user.getStore().getGrade().getSpaceSize() > 0){
				double img_remain_size = CommUtil.div(user.getStore().getGrade()
						.getSpaceSize()
						* 1024 - CommUtil.fileSize(new File(path)), 1024);
				if (img_remain_size < 0) {
					img_remain_size = -1;
				}
				mv.addObject("img_remain_size", img_remain_size);
			}
			Map params = new HashMap();
			params.put("user_id", user.getId());
			params.put("display", true);
			List<UserGoodsClass> ugcs = this.userGoodsClassService
					.query("select obj from UserGoodsClass obj where obj.user_id=:user_id and obj.display=:display and obj.parent.id is null order by obj.sequence asc",
							params, -1, -1);
			AccessoryQueryObject aqo = new AccessoryQueryObject();
			aqo.setPageSize(8);
			aqo.addQuery("obj.user.id", new SysMap("user_id", user.getId()),
					"=");
			aqo.setOrderBy("addTime");
			aqo.setOrderType("desc");
			IPageList pList = this.accessoryService.list(aqo);
			String photo_url = CommUtil.getURL(request)
					+ "/seller/load_photo.htm";
			mv.addObject("photos", pList.getResult());
			mv.addObject(
					"gotoPageAjaxHTML",
					CommUtil.showPageAjaxHtml(photo_url, "",
							pList.getCurrentPage(), pList.getPages()));
			mv.addObject("ugcs", ugcs);
			mv.addObject("obj", obj);
			if (request.getSession(false).getAttribute("goods_class_info") != null) {
				GoodsClass session_gc = (GoodsClass) request.getSession(false)
						.getAttribute("goods_class_info");
				GoodsClass gc = this.goodsClassService.getObjById(session_gc
						.getId());
				mv.addObject("goods_class_info",
						this.storeTools.generic_goods_class_info(gc));
				mv.addObject("goods_class", gc);
				request.getSession(false).removeAttribute("goods_class_info");
				params.clear();
				GoodsClass goods_class = new GoodsClass();
				if (gc.getLevel() == 2) {
					goods_class = gc.getParent().getParent();
				}
				if (gc.getLevel() == 1) {
					goods_class = gc.getParent();
				}
				params.put("gc_id", goods_class.getId());
				List<GoodsBrand> gbs = this.goodsBrandService
						.query("select obj from GoodsBrand obj where obj.gc.id=:gc_id order by obj.sequence asc",
								params, -1, -1);
				mv.addObject("gbs", gbs);
				if (gc.getLevel() == 2) {// 发布商品选择分类时选择三级分类,查询出所有与该三级分类关联的规格，即规格对应的详细商品分类
					Map spec_map = new HashMap();
					spec_map.put("store_id", user.getStore().getId());
					List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService
							.query("select obj from GoodsSpecification obj where obj.store.id=:store_id order by sequence asc",
									spec_map, -1, -1);
					List<GoodsSpecification> spec_list = new ArrayList<GoodsSpecification>();
					for (GoodsSpecification gspec : goods_spec_list) {
						for (GoodsClass spec_goodsclass_detail : gspec
								.getSpec_goodsClass_detail()) {
							if (gc.getId().equals(
									spec_goodsclass_detail.getId())) {
								spec_list.add(gspec);
							}

						}
					}
					mv.addObject("goods_spec_list", spec_list);
				} else if (gc.getLevel() == 1) {// 发布商品选择分类时选择二级分类,规格对应的主营商品分类
					Map spec_map = new HashMap();
					spec_map.put("store_id", user.getStore().getId());
					spec_map.put("gc_id", gc.getId());
					List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService
							.query("select obj from GoodsSpecification obj where obj.store.id=:store_id and obj.goodsclass.id=:gc_id order by sequence asc",
									spec_map, -1, -1);
					mv.addObject("goods_spec_list", goods_spec_list);
				}
			} else {
				if (obj.getGc() != null) {
					mv.addObject("goods_class_info", this.storeTools
							.generic_goods_class_info(obj.getGc()));
					mv.addObject("goods_class", obj.getGc());
					Map spec_map = new HashMap();
					spec_map.put("store_id", user.getStore().getId());
					List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService
							.query("select obj from GoodsSpecification obj where obj.store.id=:store_id order by sequence asc",
									spec_map, -1, -1);
					List<GoodsSpecification> spec_list = new ArrayList<GoodsSpecification>();
					for (GoodsSpecification gspec : goods_spec_list) {
						for (GoodsClass spec_goodsclass_detail : gspec
								.getSpec_goodsClass_detail()) {
							if (obj.getGc().getId()
									.equals(spec_goodsclass_detail.getId())
									|| obj.getGc()
											.getParent()
											.getId()
											.equals(spec_goodsclass_detail
													.getId())) {
								spec_list.add(gspec);
							}
						}
						mv.addObject("goods_spec_list", spec_list);
					}
					GoodsClass goods_class = null;
					if (obj.getGc().getLevel() == 2) {
						goods_class = obj.getGc().getParent().getParent();
					}
					if (obj.getGc().getLevel() == 1) {
						goods_class = obj.getGc().getParent();
					}
					params.clear();
					params.put("gc_id", goods_class.getId());
					List<GoodsBrand> gbs = this.goodsBrandService
							.query("select obj from GoodsBrand obj where obj.gc.id=:gc_id order by obj.sequence asc",
									params, -1, -1);
					mv.addObject("gbs", gbs);
				}
			}
			String goods_session = CommUtil.randomString(32);
			mv.addObject("goods_session", goods_session);
			request.getSession(false).setAttribute("goods_session",
					goods_session);
			mv.addObject("imageSuffix", this.storeViewTools
					.genericImageSuffix(this.configService.getSysConfig()
							.getImageSuffix()));
			//处理上传格式
			String[] strs =  this.configService.getSysConfig().getImageSuffix().split("\\|");
			StringBuffer sb = new StringBuffer();
			for (String str : strs) {
				sb.append("."+str+",");
			}
			mv.addObject("imageSuffix1", sb);
			Date now = new Date();
			now.setDate(now.getDate() + 1);
			mv.addObject("default_publish_day", CommUtil.formatShortDate(now));
			mv.addObject("store", obj.getGoods_store());
			// 查询商品版式信息
			params.clear();
			params.put("gf_store_id", user.getStore().getId());
			List<GoodsFormat> gfs = this.goodsFormatService
					.query("select obj from GoodsFormat obj where obj.gf_store_id=:gf_store_id",
							params, -1, -1);
			mv.addObject("gfs", gfs);
			// 查询地址信息，前端需要商家选择发货地址
			List<Area> areas = this.areaService.query(
					"select obj from Area obj where obj.parent.id is null",
					null, -1, -1);
			mv.addObject("areas", areas);
			Area de_area = this.areaService.getObjById(obj
					.getDelivery_area_id());
			mv.addObject("de_area", de_area);
			mv.addObject("jsessionid", request.getSession().getId());
		} else {
			mv = new JModelAndView(
					"user/default/sellercenter/seller_error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有该商品信息！");
			mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "商品上下架", value = "/seller/goods_sale.htm*", rtype = "seller", rname = "违规下架商品", rcode = "goods_out_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_sale.htm")
	public String goods_sale(HttpServletRequest request,
			HttpServletResponse response, String mulitId) {
		String url = "/seller/goods.htm";
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Goods goods = this.goodsService.getObjById(Long.parseLong(id));
				if (goods.getGoods_status() != -5) {
					User user = this.userService.getObjById(SecurityUserHolder
							.getCurrentUser().getId());
					user = user.getParent() == null ? user : user.getParent();
					if (goods.getGoods_store().getUser().getId()
							.equals(user.getId())) {
						int goods_status = goods.getGoods_status() == 0 ? 1 : 0;
						goods.setGoods_status(goods_status);
						this.goodsService.update(goods);
						if (goods_status == 0) {
							url = "/seller/goods_storage.htm";
							// 添加lucene索引
							String goods_lucene_path = System
									.getProperty("user.dir")
									+ File.separator
									+ "luence" + File.separator + "goods";
							File file = new File(goods_lucene_path);
							if (!file.exists()) {
								CommUtil.createFolder(goods_lucene_path);
							}
							LuceneVo vo = this.luceneVoTools
									.updateGoodsIndex(goods);
							LuceneUtil lucene = LuceneUtil.instance();
							lucene.setIndex_path(goods_lucene_path);
							lucene.update(CommUtil.null2String(goods.getId()),
									vo);
						} else {
							// 删除索引
							String goods_lucene_path = System
									.getProperty("user.dir")
									+ File.separator
									+ "luence" + File.separator + "goods";
							File file = new File(goods_lucene_path);
							if (!file.exists()) {
								CommUtil.createFolder(goods_lucene_path);
							}
							LuceneUtil lucene = LuceneUtil.instance();
							lucene.setIndex_path(goods_lucene_path);
							lucene.delete_index(CommUtil.null2String(goods
									.getId()));
						}
					}
				}
			}
		}
		return "redirect:" + url;
	}

	@SecurityMapping(title = "商品删除", value = "/seller/goods_del.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_list_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_del.htm")
	public String goods_del(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String op) {
		String url = "/seller/goods.htm";
		if (CommUtil.null2String(op).equals("storage")) {
			url = "/seller/goods_storage.htm";
		}
		if (CommUtil.null2String(op).equals("out")) {
			url = "/seller/goods_out.htm";
		}
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Goods goods = this.goodsService.getObjById(CommUtil
						.null2Long(id));
				User user = this.userService.getObjById(SecurityUserHolder
						.getCurrentUser().getId());
				user = user.getParent() == null ? user : user.getParent();
				if (goods.getGoods_store().getUser().getId()
						.equals(user.getId())) {
					List<Evaluate> evaluates = goods.getEvaluates();
					for (Evaluate e : evaluates) {
						this.evaluateService.delete(e.getId());
					}
					Map params = new HashMap();
					params.put("gid", CommUtil.null2Long(id));
					List<ComplaintGoods> complaintGoodses = this.complaintGoodsService
							.query("select obj from ComplaintGoods obj where obj.goods.id=:gid",
									params, -1, -1);
					for (ComplaintGoods cg : complaintGoodses) {
						this.complaintGoodsService.delete(cg.getId());
					}
					List<GroupGoods> groupGoodses = this.groupGoodsService
							.query("select obj from GroupGoods obj where obj.gg_goods.id=:gid",
									params, -1, -1);
					for (GroupGoods gg : groupGoodses) {
						this.groupGoodsService.delete(gg.getId());
					}
					params.clear();
					for (Accessory acc : goods.getGoods_photos()) {
						params.put("acid", acc.getId());
						List<Album> als = this.albumService
								.query("select obj from Album obj where obj.album_cover.id = :acid",
										params, -1, -1);
						for (Album al : als) {
							al.setAlbum_cover(null);
							this.albumService.update(al);
						}
						params.clear();
					}
					if (goods.getGoods_main_photo() != null) {
						params.put("acid", goods.getGoods_main_photo().getId());
						List<Album> als = this.albumService
								.query("select obj from Album obj where obj.album_cover.id = :acid",
										params, -1, -1);
						for (Album al : als) {
							al.setAlbum_cover(null);
							this.albumService.update(al);
						}
						CommUtil.del_acc(request, goods.getGoods_main_photo());
						goods.setGoods_main_photo(null);
					}
					List<ZTCGoldLog> ztcGoldLogs = this.iztcGoldLogService
							.query("select obj from ZTCGoldLog obj where obj.zgl_goods_id="
									+ CommUtil.null2Long(id), null, -1, -1);
					if (ztcGoldLogs.size() > 0) {
						for (ZTCGoldLog ztcGoldLog : ztcGoldLogs) {
							this.iztcGoldLogService.delete(ztcGoldLog.getId());
						}
					}
					for (GoodsCart cart : goods.getCarts()) {
						this.cartService.delete(cart.getId());
					}
					goods.getCarts().clear();
					goods.getGoods_ugcs().clear();
					goods.getGoods_photos().clear();
					goods.getGoods_specs().clear();
					goods.getAg_goods_list().clear();
					goods.getEvaluates().clear();
					goods.getGoods_photos().clear();
					goods.getGroup_goods_list().clear();
					if(goods.getGoods_collect() == 0){
						this.goodsService.delete(goods.getId());
					}else{
						goods.setGoods_status(-3);
						this.goodsService.update(goods);
					}
					// 删除索引
					String goods_lucene_path = System.getProperty("metoob2b2c.root")
							+ "luence" + File.separator
							+ "goods";
					File file = new File(goods_lucene_path);
					if (!file.exists()) {
						CommUtil.createFolder(goods_lucene_path);
					}
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					lucene.delete_index(CommUtil.null2String(id));
				}
			}
		}
		return "redirect:" + url;
	}

	@SecurityMapping(title = "商家商品相册列表", value = "/seller/goods_album.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_album.htm")
	public ModelAndView goods_album(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String ajax_type) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_album.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		AlbumQueryObject aqo = new AlbumQueryObject();
		aqo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		aqo.setOrderBy("album_sequence");
		aqo.setOrderType("asc");
		aqo.setPageSize(8);
		IPageList pList = this.albumService.list(aqo);
		String album_url = CommUtil.getURL(request) + "/seller/goods_album.htm";
		mv.addObject("albums", pList.getResult());
		mv.addObject("gotoPageAjaxHTML", CommUtil.showPageAjaxHtml(album_url,
				"", pList.getCurrentPage(), pList.getPages()));
		mv.addObject("ajax_type", ajax_type);
		mv.addObject("ImageTools", ImageTools);
		return mv;
	}

	@SecurityMapping(title = "商家商品图片列表", value = "/seller/goods_img.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_img.htm")
	public ModelAndView goods_img(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String type,
			String album_id) {
		ModelAndView mv = new JModelAndView("user/default/sellercenter/" + type
				+ ".html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		AccessoryQueryObject aqo = new AccessoryQueryObject(currentPage, mv,
				"addTime", "desc");
		aqo.setPageSize(20);
		aqo.addQuery("obj.album.id",
				new SysMap("album_id", CommUtil.null2Long(album_id)), "=");
		aqo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
		aqo.setOrderBy("addTime");
		aqo.setOrderType("desc");
		IPageList pList = this.accessoryService.list(aqo);
		String photo_url = CommUtil.getURL(request) + "/seller/goods_img.htm";
		mv.addObject("photos", pList.getResult());
		mv.addObject("gotoPageAjaxHTML", CommUtil.showPageAjaxHtml(photo_url,
				"", pList.getCurrentPage(), pList.getPages()));
		mv.addObject("album_id", album_id);
		return mv;
	}

	@SecurityMapping(title = "商品二维码生成", value = "/seller/goods_qr.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_qr.htm")
	public String goods_qr(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage)
			throws ClassNotFoundException {
		String ids[] = mulitId.split(",");
		for (String id : ids) {
			if (id != null) {
				Goods obj = this.goodsService
						.getObjById(CommUtil.null2Long(id));
				User user = this.userService.getObjById(SecurityUserHolder
						.getCurrentUser().getId());
				if (obj.getGoods_store().getId()
						.equals(user.getStore().getId())) {// 生成自己店铺的商品二维码
					String uploadFilePath = this.configService.getSysConfig()
							.getUploadFilePath();

					String destPath = request.getSession().getServletContext()
							.getRealPath("/")
							+ uploadFilePath + File.separator + "goods_qr";
					if (!CommUtil.fileExist(destPath)) {
						CommUtil.createFolder(destPath);
					}
					destPath = destPath + File.separator + obj.getId()
							+ "_qr.jpg";
					// Map goods_qr = new HashMap();
					// goods_qr.put("id", CommUtil.null2String(obj.getId()));
					// goods_qr.put("type", "goods");
					String logoPath = "";
					if (obj.getGoods_main_photo() != null) {
						logoPath = request.getSession().getServletContext()
								.getRealPath("/")
								+ obj.getGoods_main_photo().getPath()
								+ File.separator
								+ obj.getGoods_main_photo().getName();
					} else {
						logoPath = request.getSession().getServletContext()
								.getRealPath("/")
								+ this.configService.getSysConfig()
										.getGoodsImage().getPath()
								+ File.separator
								+ File.separator
								+ this.configService.getSysConfig()
										.getGoodsImage().getName();
					}
					QRCodeUtil.encode(CommUtil.getURL(request) + "/goods_" + id
							+ ".htm", logoPath, destPath, true);
					obj.setQr_img_path(CommUtil.getURL(request) + "/"
							+ uploadFilePath + "/" + "goods_qr" + "/"
							+ obj.getId() + "_qr.jpg");
					goodsService.update(obj);
				}
			}
		}
		return "redirect:goods.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "打开关联商品版式", value = "/seller/goods_format.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_format.htm")
	public ModelAndView goods_format(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_format.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		// 查询商品版式信息
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		Map params = new HashMap();
		params.put("gf_store_id", user.getStore().getId());
		List gfs = this.goodsFormatService
				.query("select obj from GoodsFormat obj where obj.gf_store_id=:gf_store_id",
						params, -1, -1);
		mv.addObject("gfs", gfs);
		mv.addObject("mulitId", mulitId);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "批量保存关联商品版式", value = "/seller/goods_format_link.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_format_link.htm")
	public void goods_format_link(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage,
			String goods_top_format_id, String goods_bottom_format_id) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!CommUtil.null2String(id).equals("")) {
				Goods goods = this.goodsService.getObjById(CommUtil
						.null2Long(id));
				Store store = this.userService.getObjById(
						SecurityUserHolder.getCurrentUser().getId()).getStore();
				if (goods.getGoods_store().getId().equals(store.getId())) {
					goods.setGoods_top_format_id(CommUtil
							.null2Long(goods_top_format_id));
					GoodsFormat gf = this.goodsFormatService
							.getObjById(CommUtil.null2Long(goods_top_format_id));
					if (gf != null) {
						goods.setGoods_top_format_content(gf.getGf_content());
					} else {
						goods.setGoods_top_format_content(null);
					}
					goods.setGoods_bottom_format_id(CommUtil
							.null2Long(goods_bottom_format_id));
					gf = this.goodsFormatService.getObjById(CommUtil
							.null2Long(goods_bottom_format_id));
					if (gf != null) {
						goods.setGoods_bottom_format_content(gf.getGf_content());
					} else {
						goods.setGoods_bottom_format_content(null);
					}
					this.goodsService.update(goods);
				}
			}
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SecurityMapping(title = "商品F码Excel下载", value = "/seller/goods_self_f_code_download.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_self_f_code_download.htm")
	public void goods_self_f_code_download(HttpServletRequest request,
			HttpServletResponse response, String id) throws IOException {
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		String excel_url = "";
		Store store = this.userService.getObjById(
				SecurityUserHolder.getCurrentUser().getId()).getStore();
		if (obj.getF_sale_type() == 1 && obj.getGoods_type() == 1
				&& obj.getGoods_store().getId().equals(store.getId())
				&& !CommUtil.null2String(obj.getGoods_f_code()).equals("")) {
			List<Map> list = Json.fromJson(List.class, obj.getGoods_f_code());
			String name = CommUtil.null2String(UUID.randomUUID());
			String path = request.getSession().getServletContext()
					.getRealPath("/")
					+ File.separator + "excel" + File.separator + name + ".xls";
			this.exportList2Excel("F码列表", new String[] { "F码信息", "F码状态" },
					list, response, name);
		}
	}

	// supplement
	@SecurityMapping(title = "商品补货", value = "/seller/goods_supplement.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_supplement.htm")
	public ModelAndView goods_supplement(HttpServletRequest request,
			HttpServletResponse response, String id) throws IOException {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_supplement.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		String gsp_ids = "";
		for (GoodsSpecProperty gsp : obj.getGoods_specs()) {
			gsp_ids = gsp_ids + "," + gsp.getId();
		}
		if (obj != null
				&& obj.getGoods_store().getId().equals(user.getStore().getId())) {
			Map spec_map = new HashMap();
			spec_map.put("store_id", user.getStore().getId());
			List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService
					.query("select obj from GoodsSpecification obj where obj.store.id=:store_id order by sequence asc",
							spec_map, -1, -1);
			List<GoodsSpecification> spec_list = new ArrayList<GoodsSpecification>();
			for (GoodsSpecification gspec : goods_spec_list) {
				for (GoodsClass spec_goodsclass_detail : gspec
						.getSpec_goodsClass_detail()) {
					if (obj.getGc().getId()
							.equals(spec_goodsclass_detail.getId())
							|| obj.getGc().getParent().getId()
									.equals(spec_goodsclass_detail.getId())) {
						spec_list.add(gspec);
					}
				}
				mv.addObject("goods_spec_list", spec_list);
			}
			mv.addObject("gsp_ids", gsp_ids);
			mv.addObject("obj", obj);
		}
		return mv;
	}

	// supplement
	@SecurityMapping(title = "商品补货保存", value = "/seller/goods_supplement_save.htm*", rtype = "seller", rname = "出售中的商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_supplement_save.htm")
	public void goods_supplement_save(HttpServletRequest request,
			HttpServletResponse response, String id, String inventory,
			String intentory_details) throws IOException {
		int code = -100;
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Map json_map = new HashMap();
		boolean warn_suppment = true;
		if (obj != null
				&& obj.getGoods_store().getId().equals(user.getStore().getId())) {
			if (obj.getInventory_type().equals("all")) {
				obj.setGoods_inventory(CommUtil.null2Int(inventory));
				if (CommUtil.null2Int(inventory)
						- obj.getGoods_warn_inventory() > 0) {
					obj.setWarn_inventory_status(0);// 预警状态恢复
				}
				code = 100;
			}
			if (obj.getInventory_type().equals("spec")) {
				List<Map> maps = (List<Map>) Json.fromJson(obj
						.getGoods_inventory_detail());
				for (Map map : maps) {
					String[] inventory_list = intentory_details.split(";");
					for (String temp_inventory : inventory_list) {
						if (!temp_inventory.equals("")) {
							String[] list = temp_inventory.split(",");
							if (list[0].equals(CommUtil.null2String(map
									.get("id")))) {
								map.put("count", list[1]);
								if (CommUtil.null2Int(map.get("count")) <= CommUtil
										.null2Int(map.get("supp"))) {
									warn_suppment = false;
								}
							}
						}
					}
				}
				if (warn_suppment) {
					obj.setWarn_inventory_status(0);// 预警状态恢复
				}
				obj.setGoods_inventory_detail(Json.toJson(maps,
						JsonFormat.compact()));
				code = 100;
			}
			this.goodsService.update(obj);
		}
		json_map.put("code", code);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean exportList2Excel(String title, String[] headers,
			List<Map> list, HttpServletResponse response, String name) {
		boolean ret = true;
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet(title);
		// 设置表格默认宽度为15个字节
		sheet.setDefaultColumnWidth(20);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);

		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.VIOLET.index);
		font.setFontHeightInPoints((short) 14);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		// 把字体应用到当前的样式
		style.setFont(font);

		// 生成并设置另一个样式
		HSSFCellStyle style_ = workbook.createCellStyle();
		style_.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		style_.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style_.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style_.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style_.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style_.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style_.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style_.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		// 生成另一个字体
		HSSFFont font_ = workbook.createFont();
		// font.setColor(color);
		font_.setFontHeightInPoints((short) 14);
		// font_.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		style_.setFont(font_);
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			cell.setCellValue(new HSSFRichTextString(headers[i]));
		}
		// 遍历集合数据，产生数据行
		int index = 0;
		for (Map map : list) {
			index++;
			row = sheet.createRow(index);
			String value = CommUtil.null2String(map.get("code"));
			HSSFCell cell = row.createCell(0);
			cell.setCellStyle(style_);
			cell.setCellValue(value);
			value = CommUtil.null2Int(map.get("status")) == 0 ? "未使用" : "已使用";
			cell = row.createCell(1);
			cell.setCellStyle(style_);
			cell.setCellValue(value);
		}
		try {
			response.setContentType("application/x-download");
			response.addHeader("Content-Disposition", "attachment;filename="
					+ name + ".xls");
			OutputStream os = response.getOutputStream();
			workbook.write(os);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 商品ajax更新
	 * @param request
	 * @param response
	 * @param id
	 * @param fieldName
	 * @param value 1. Integer.parseInt([String])
 3 2.Integer.valueOf([String]).intValue();
	 * @throws ClassNotFoundException
	 */
	@SecurityMapping(title = "商品AJAX更新", value = "/seller/goods_self_ajax.htm*", rtype = "seller", rname = "商品管理", rcode = "goods_self", rgroup = "自营")
	@RequestMapping("/seller/goods_self_ajax.htm")
	public void goods_self_ajax(HttpServletRequest request, HttpServletResponse response, String fieldName, String id,String value) throws ClassNotFoundException {
		Goods obj = null;
		if(fieldName.equals("goods_current_price")){
			 obj = this.goodsService.getObjById(CommUtil.null2Long(id));
			obj.setGoods_current_price(new BigDecimal(value));
		}
		if(fieldName.equals("goods_inventory")){
			obj = this.goodsService.getObjById(CommUtil.null2Long(id));
			obj.setGoods_inventory(Integer.parseInt(value));
		}
		this.goodsService.save(obj);
		try {
			response.getWriter().print(value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/verify_goodsSerial.htm")
	public void verify_goodsSerial(HttpServletRequest request, HttpServletResponse response, String goods_serial, String id){
		boolean ret = true;
		Long goods_id = CommUtil.null2Long(id);
		Map params = new HashMap();
		params.put("goods_serial", goods_serial);
		List<Goods> cgoodslists = this.goodsService.query("select obj from Goods obj where obj.goods_serial=:goods_serial", params, -1, -1);
		List<CGoods> cgoodslist = this.cGoodsService.query("select obj from CGoods obj where obj.goods_serial=:goods_serial", params, -1, -1);
		if(!cgoodslists.isEmpty()){
			if(cgoodslists.get(0).getId().equals(goods_id)){
				ret = true;
			}else{
				if(cgoodslists.size()>0 || cgoodslist.size()>0){
					ret = false;
				}
			}
		}else{
			if(!cgoodslist.isEmpty() && cgoodslist.size() == 1){
				if(cgoodslist.get(0).getGoods().getId().equals(goods_id) && cgoodslist.size() == 1){
					ret = true;
				}else{
					if(cgoodslists.size()>0 || cgoodslist.size()>0){
						ret = false;
					}
				}
			}else{
				ret = true;
			}
		}
		
		if(cgoodslists.isEmpty() && cgoodslist.isEmpty()){
			ret = true;
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@RequestMapping("/verify_goodsSku.htm")
	public void verify_goodsSku(HttpServletRequest request, HttpServletResponse response, String goods_serial, String id){
		boolean ret = true;
		Long goods_id = CommUtil.null2Long(id);
		Map params = new HashMap();
		params.put("goods_serial", goods_serial);
		List<Goods> goodslists = this.goodsService.query("select obj from Goods obj where obj.goods_serial=:goods_serial", params, -1, -1);
		List<CGoods> cgs = goodslists.get(0).getCgoods();
		List<CGoods> cgoodslist = this.cGoodsService.query("select obj from CGoods obj where obj.goods_serial=:goods_serial", params, -1, -1);
		if(!goodslists.isEmpty()){
			if(goodslists.get(0).getId().equals(goods_id)){
				ret = true;
			}else{
				if(goodslists.size()>0 || cgoodslist.size()>0){
					ret = false;
				}
			}
		}	
		if(!cgoodslist.isEmpty()){
			if(cgoodslist.get(0).getId().equals(goods_id)){
				ret = true;
			}else{
				if(goodslists.size()>0 || cgoodslist.size()>0){
					ret = false;
				}
			}
		}	
		if(goodslists.isEmpty() && cgoodslist.isEmpty()){
			ret = true;
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
