package com.metoo.php.manage.seller.action;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.metoo.app.buyer.domain.Http;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsFormat;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GoodsTypeProperty;
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.UserGoodsClass;
import com.metoo.foundation.domain.WaterMark;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsFormatService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsSpecificationService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserGoodsClassService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.IWaterMarkService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.LuceneVo;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.StoreTools;
import com.metoo.view.web.tools.StoreViewTools;

import net.sf.json.JSONObject;

@RequestMapping("/php/seller")
@Controller
public class PGoodsSellerAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private StoreTools storeTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IUserGoodsClassService userGoodsClassService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private IGoodsFormatService goodsFormatService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private ICGoodsService cGoodsService;
	@Autowired
	private IGoodsSpecPropertyService specPropertyService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IGoodsSpecificationService goodsSpecificationService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IWaterMarkService waterMarkService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IGoodsCartService cartService;
	@Autowired
	private IPointService pointService;
	@Autowired
	private MGoodsViewTools mgoodsViewTools;

	@SecurityMapping(title = "php发布商品", value = "/seller/add_goods_second.json", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/add_goods_second.json")
	public void addGoodsSecond(HttpServletRequest request, HttpServletResponse response, String user_id, String gc_id) {
		Result result = null;
		Map goods_info = new HashMap();
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		int store_status = user.getStore().getStore_status();
		if (store_status == 15) {
			GoodsClass gc = gc = this.goodsClassService.getObjById(CommUtil.null2Long(gc_id));
			List<Map> types = new ArrayList<Map>();
			if (gc.getGoodsType() != null) {
				for (GoodsTypeProperty obj : gc.getGoodsType().getProperties()) {
					Map goodsType_map = new HashMap();
					goodsType_map.put("type_id", obj.getId());
					goodsType_map.put("type_name", obj.getName());
					goodsType_map.put("type_property", CommUtil.splitByChar(obj.getValue(), ","));
					types.add(goodsType_map);
				}
				goods_info.put("goods_property", types);
			}
			if (gc.getLevel() == 2) {// 发布商品选择分类时选择三级分类,查询出所有与该三级分类关联的规格，即规格对应的详细商品分类
				Map spec_map = new HashMap();
				spec_map.put("spec_type", 0);
				List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService.query(
						"select obj from GoodsSpecification obj where obj.spec_type=:spec_type order by obj.sequence asc",
						spec_map, -1, -1);

				/*
				 * spec_map.put("store_id", user.getStore().getId());
				 * List<GoodsSpecification> goods_spec_list =
				 * this.goodsSpecificationService .query(
				 * "select obj from GoodsSpecification obj where obj.store.id=:store_id order by sequence asc"
				 * , spec_map, -1, -1);
				 */
				List<GoodsSpecification> spec_list = new ArrayList<GoodsSpecification>();
				for (GoodsSpecification gspec : goods_spec_list) {
					for (GoodsClass spec_goodsclass_detail : gspec.getSpec_goodsClass_detail()) {
						if (gc.getId().equals(spec_goodsclass_detail.getId())) {
							spec_list.add(gspec);
						}
					}
				}
				List<Map> gsfs = new ArrayList<Map>();
				for (GoodsSpecification gsf : spec_list) {
					Map gsf_map = new HashMap();
					gsf_map.put("gsf_id", gsf.getId());
					gsf_map.put("gsf_name", gsf.getName());
					List<Map> propertys = new ArrayList<Map>();
					for (GoodsSpecProperty sproperty : gsf.getProperties()) {
						Map sproperty_map = new HashMap();
						sproperty_map.put("property_id", sproperty.getId());
						sproperty_map.put("property_value", sproperty.getValue());
						propertys.add(sproperty_map);
					}
					gsf_map.put("propertys", propertys);
					gsfs.add(gsf_map);
				}
				goods_info.put("goods_spec", gsfs);
			} else if (gc.getLevel() == 1) {// 发布商品选择分类时选择二级分类,规格对应的主营商品分类
				/*
				 * Map spec_map = new HashMap(); spec_map.put("store_id",
				 * user.getStore().getId()); spec_map.put("gc_id", gc.getId());
				 * List<GoodsSpecification> goods_spec_list =
				 * this.goodsSpecificationService .query(
				 * "select obj from GoodsSpecification obj where obj.store.id=:store_id and obj.goodsclass.id=:gc_id order by sequence asc"
				 * , spec_map, -1, -1);
				 */
				Map spec_map = new HashMap();
				spec_map.put("spec_type", 0);
				List<GoodsSpecification> goods_spec_list = this.goodsSpecificationService.query(
						"select obj from GoodsSpecification obj where obj.spec_type=:spec_type order by obj.sequence asc",
						spec_map, -1, -1);
				List<Map> gsfs = new ArrayList<Map>();
				for (GoodsSpecification gsf : goods_spec_list) {
					Map gsf_map = new HashMap();
					gsf_map.put("gsf_id", gsf.getId());
					gsf_map.put("gsf_name", gsf.getName());
					List<Map> propertys = new ArrayList<Map>();
					for (GoodsSpecProperty sproperty : gsf.getProperties()) {
						Map sproperty_map = new HashMap();
						sproperty_map.put("property_id", sproperty.getId());
						sproperty_map.put("property_value", sproperty.getValue());
						propertys.add(sproperty_map);
					}
					gsf_map.put("propertys", propertys);
					gsfs.add(gsf_map);
				}
				goods_info.put("goods_spec", gsfs);
			}

			Map params = new HashMap();
			params.put("user_id", user.getId());
			params.put("display", true);
			List<UserGoodsClass> ugcs = this.userGoodsClassService.query(
					"select obj from UserGoodsClass obj where obj.user_id=:user_id and obj.display=:display and obj.parent.id is null order by obj.sequence asc",
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
			List<GoodsBrand> gbs = this.goodsBrandService.query(
					"select obj from GoodsBrand obj where obj.gc.id=:gc_id order by obj.sequence asc", params, -1, -1);
			List<Map> gb_list = new ArrayList<Map>();
			for (GoodsBrand gb : gbs) {
				Map gb_map = new HashMap();
				gb_map.put("brand_name", gb.getName());
				gb_map.put("brand_id", gb.getId());
				gb_list.add(gb_map);
			}
			goods_info.put("goods_brand", gb_list);

			List<Map> ugc_list = new ArrayList<Map>();
			for (UserGoodsClass ugc : ugcs) {
				Map ugc_map = new HashMap();
				ugc_map.put("ugc_name", ugc.getClassName());
				ugc_map.put("ugc_id", ugc.getId());
				ugc_list.add(ugc_map);
			}
			goods_info.put("ugc_list", ugc_list);
			goods_info.put("imageSuffix",
					this.storeViewTools.genericImageSuffix(this.configService.getSysConfig().getImageSuffix()));
			result = new Result(0, "success", goods_info);
		}
		if (store_status == 0) {
			result = new Result(1, "您尚未开通店铺，不能发布商品");
		}
		if (store_status == 10) {
			result = new Result(2, "您的店铺在审核中，不能发布商品");
		}
		if (store_status == 20) {
			result = new Result(3, "您的店铺已被关闭，不能发布商品");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	@SecurityMapping(title = "发布商品第三步", value = "/add_goods_finish.json*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/add_goods_finish.json")
	public void createGoodsFinish(HttpServletRequest request, HttpServletResponse response, String id,
			String goods_class_id, String image_ids, String goods_main_img_id, String user_class_ids,
			String goods_brand_id, String goods_spec_ids, String goods_properties, String intentory_details,
			String goods_session, String transport_type, String transport_id, String publish_goods_status,
			String publish_day, String publish_hour, String publish_min, String f_code_profix, String f_code_count,
			String advance_date, String goods_top_format_id, String goods_bottom_format_id, String delivery_area_id,
			String goods_price, String store_price, String user_id, String goods_transfee, String tag) {
		ModelAndView mv = null;
		Result result = null;
		User user = null;
		user = this.userService.getObjById(CommUtil.null2Long(user_id));
		Store store = user.getStore();
		if (user.getParent() != null) {
			store = user.getParent().getStore();
		}
		// 判断所发布的商品分类是否和店铺经营分类匹配
		GoodsClass gc = this.goodsClassService.getObjById(Long.parseLong(goods_class_id));
		boolean m = false;
		// [ 店铺经营类，[{"gc_list":[2, 8, 3, 6, 7,
		// 5],"m_id":1}]，当店铺申请店铺时选择了详细类目，商家发布商品时只可选择所有的详细类目]
		if (store.getGc_detail_info() == null || store.getGc_detail_info().equals("")) {
			// [主营类目Id，当店铺只经营主营类目时（商城1级分类），商家发布商品时可以选择该类目下的所有分类]
			if (gc.getParent().getId().equals(store.getGc_main_id())) {
				m = true;
			}
			if (gc.getParent().getParent() != null
					&& gc.getParent().getParent().getId().equals(store.getGc_main_id())) {
				m = true;
			}
		} else {
			Set<GoodsClass> store_gcs = this.storeTools.query_store_DetailGc(store.getGc_detail_info());
			m = store_gcs.contains(gc.getParent());
		}
		if (m) {
			WebForm wf = new WebForm();
			Goods goods = new Goods();
			String obj_status = null;
			Map temp_params = new HashMap();
			List<Long> temp_ids = new ArrayList<Long>();
			if (id == null || "".equals(id)) {
				goods = wf.toPo(request, Goods.class);
				goods.setAddTime(new Date());
				goods.setGoods_store(store);
			} else {
				Goods obj = this.goodsService.getObjById(Long.parseLong(id));
				goods = (Goods) wf.toPo(request, obj);
				BigDecimal old_price = obj.getGoods_current_price();
				obj_status = CommUtil.null2String(obj.getGoods_status());
				goods.setPrice_history(old_price);
			}

			if (goods.getCombin_status() == 2 || goods.getActivity_status() == 2) {
			} else {
				goods.setGoods_current_price(goods.getStore_price());
			}
			// 商品名称不可以带有任何html字样，进行过滤
			goods.setGoods_name(Jsoup.clean(goods.getGoods_name(), Whitelist.none()));
			// 商品详情不可以带有违规标签，如script等等
			goods.setGoods_details(goods.getGoods_details());

			goods.setGc(gc);
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
				String str = publish_day + " " + publish_hour + ":" + publish_min;
				Date date = CommUtil.formatDate(str, "yyyy-MM-dd HH:mm");
				goods.setGoods_seller_time(date);
			}
			goods.setPublish_goods_status(CommUtil.null2Int(publish_goods_status));// 发布审核后状态
			Accessory main_img = null;
			if (goods_main_img_id != null && !goods_main_img_id.equals("")) {
				main_img = this.accessoryService.getObjById(Long.parseLong(goods_main_img_id));
			}
			goods.setGoods_main_photo(main_img);
			goods.getGoods_ugcs().clear();

			if (user_class_ids != null && !user_class_ids.equals("")) {
				String[] ugc_ids = user_class_ids.split(",");
				temp_ids.clear();
				List u_class_list = new ArrayList();
				for (String ugc_id : ugc_ids) {
					if (!ugc_id.equals("")) {
						UserGoodsClass ugc = this.userGoodsClassService.getObjById(Long.parseLong(ugc_id));
						u_class_list.add(ugc);
					}
				}
				// [店铺中商品所在分类]
				goods.setGoods_ugcs(u_class_list);
			}

			if (image_ids != null && !image_ids.equals("")) {
				String[] img_ids = image_ids.split(",");
				goods.getGoods_photos().clear();
				temp_ids.clear();
				for (String img_id : img_ids) {
					if (!img_id.equals("")) {
						temp_ids.add(CommUtil.null2Long(img_id));
					}
				}

				if (!temp_ids.isEmpty()) {
					/*
					 * temp_params.clear(); temp_params.put("ids", temp_ids);
					 * List<Accessory> temp_list = this.accessoryService.query(
					 * "select new Accessory (id) from Accessory obj where obj.id in(:ids)"
					 * , temp_params, -1, -1);
					 */
					List<Accessory> temp_list = new ArrayList<Accessory>();
					for (Long img_id : temp_ids) {
						temp_list.add(this.accessoryService.getObjById(img_id));
					}
					for (Accessory as : temp_list) {
						System.err.println(as.getId());
					}
					goods.getGoods_photos().addAll(temp_list);
				}
			} else {
				goods.getGoods_photos().clear();
			}

			if (goods_brand_id != null && !goods_brand_id.equals("")) {
				GoodsBrand goods_brand = this.goodsBrandService.getObjById(Long.parseLong(goods_brand_id));
				goods.setGoods_brand(goods_brand);
			}
			goods.getGoods_specs().clear();
			if (goods_spec_ids != null) {
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
					List<GoodsSpecProperty> temp_list = this.specPropertyService.query(
							"select new GoodsSpecProperty(id) from GoodsSpecProperty obj where obj.id in(:ids)",
							temp_params, -1, -1);
					goods.getGoods_specs().addAll(temp_list);
				}
			}

			List<Map> maps = new ArrayList<Map>();
			if (goods_properties != null) {
				String[] properties = goods_properties.split(";");
				for (String property : properties) {
					if (!property.equals("")) {
						String[] list = property.split(",");
						Map map = new HashMap();
						map.put("id", list[0]);
						map.put("val", list[1]);
						map.put("name", this.goodsTypePropertyService.getObjById(Long.parseLong(list[0])).getName());
						maps.add(map);
					}
				}
				goods.setGoods_property(Json.toJson(maps, JsonFormat.compact()));
			}

			maps.clear();
			boolean warn_suppment = false;
			List<Map> specList = new ArrayList<Map>();
			String eids = null;
			if (intentory_details != null) {
				String[] inventory_list = intentory_details.split(";");
				for (String inventory : inventory_list) {
					if (!inventory.equals("")) {
						String[] list = inventory.split(",");
						Map map = new HashMap();
						try {
							map.put("id", list[0]);
							try {
								map.put("count", list[1]);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								map.put("count", "");
							}
							map.put("supp", list[2]);
							map.put("price", list[3]);
							map.put("img_id", list[4]);
							try {
								map.put("weight", list[5]);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								map.put("weight", "");
							}
							map.put("length", list[6]);
							map.put("width", list[7]);
							map.put("high", list[8]);
							map.put("goodsSerial", list[9]);
							map.put("goodsDisabled", list[10]);
							try {
								map.put("spec_color", list[11]);
							} catch (IndexOutOfBoundsException e) {
								// TODO Auto-generated catch block
								map.put("spec_color", "");
							}
							map.put("goods_tiered_price", list[12] == null ? "" : list[12]);
							map.put("goods_warn_inventory", 0);// list[12]
							if (list[3] != null && !list[3].equals("")) {
								map.put("goods_price", list[3]);
							}
							if (list[2] != null && !list[2].equals("")) {
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
							map.put("spec_color", "");
							map.put("goods_warn_inventory", "");
						}
						eids = store.getId() + list[9];
						map.put("eid", eids);
						specList.add(map);
						maps.add(map);
					}
				}
			}
			goods.setTransport(null);
			if (!maps.isEmpty()) {
				List list_discount_price = new ArrayList();
				List<BigDecimal> list_goods_price = new ArrayList<BigDecimal>();
				List<BigDecimal> list_goods_current_price = new ArrayList<BigDecimal>();
				List goods_inventory_list = new ArrayList();
				for (Map map : maps) {
					/*
					 * if (map.get("discount_price") != null &&
					 * !map.get("discount_price").equals("")) {
					 * list_discount_price.add(map.get("discount_price")); }
					 */
					if (map.get("goods_price") != null && !map.get("goods_price").equals("")) {
						list_goods_price.add(CommUtil.null2BigDecimal(map.get("goods_price")));
					}
					if (map.get("goods_current_price") != null && !map.get("goods_current_price").equals("")) {
						list_goods_current_price.add(CommUtil.null2BigDecimal(map.get("goods_current_price")));
					}
					if (map.get("count") != null) {
						goods_inventory_list.add(map.get("count"));
					}
				}
				/*
				 * goods.setGoods_discount_rate( list_discount_price.size() == 0
				 * ? 0 :
				 * CommUtil.null2Int(Collections.min(list_discount_price)));
				 */
				goods.setGoods_price(Collections.min(list_goods_price));

				if (goods_price == null || goods_price.equals("")) {
					goods_price = CommUtil.null2String(Collections.min(list_goods_price));
				}
				if (store_price == null || store_price.equals("")) {
					store_price = CommUtil.null2String(Collections.min(list_goods_current_price));
				}
				goods.setGoods_current_price(
						list_goods_current_price.size() == 0 ? null : Collections.min(list_goods_current_price));
				goods.setStore_price(
						list_goods_current_price.size() == 0 ? null : Collections.min(list_goods_current_price));
				goods.setGoods_inventory(goods_inventory_list.size() == 0 ? 0
						: CommUtil.null2Int(Collections.min(goods_inventory_list)));
				goods.setStore_deals_inventory(goods_inventory_list.size() == 0 ? 0
						: CommUtil.null2Int(Collections.min(goods_inventory_list)));
			}
			goods.setGoods_type(1);
			if (store.getGrade().getGradeName().equals("China")) {
				goods.setGoods_global(1);
			} else {
				goods.setGoods_global(2);
			}
			goods.setGoods_inventory_detail(Json.toJson(maps, JsonFormat.compact()));
			// 商品缺货状态操作
			try {
				if (goods.getInventory_type().equals("all")) {
					int inventory = goods.getGoods_inventory();
					if (CommUtil.null2Int(inventory) - goods.getGoods_warn_inventory() > 0) {
						goods.setWarn_inventory_status(0);// 预警状态恢复
					} else {
						goods.setWarn_inventory_status(-1);
					}
					goods.setStore_deals_inventory(inventory);
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 是否为F码商品，是则生成F码
			if (goods.getF_sale_type() == 1 && CommUtil.null2Int(f_code_count) > 0) {
				Set<String> set = new HashSet<String>();
				while (true) {
					if (set.size() == CommUtil.null2Int(f_code_count)) {
						break;
					}
					set.add((f_code_profix + CommUtil.randomString(12)).toUpperCase());
				}
				List<Map> f_code_maps = new ArrayList<Map>();
				if (!CommUtil.null2String(goods.getGoods_f_code()).equals("")) {
					f_code_maps = Json.fromJson(List.class, goods.getGoods_f_code());
				}

				for (String code : set) {
					Map f_code_map = new HashMap();
					f_code_map.put("code", code);
					f_code_map.put("status", 0);// 0表示该F码未使用，1为已经使用
					f_code_maps.add(f_code_map);
				}
				if (f_code_maps.size() > 0) {
					goods.setGoods_f_code(Json.toJson(f_code_maps, JsonFormat.compact()));
				}
			}
			// 是否为预售商品，是则加入发货时间
			if (goods.getAdvance_sale_type() == 1) {
				goods.setAdvance_date(CommUtil.formatDate(advance_date));
				goods.setGoods_status(-5);// 预售商品必须经过平台审核
			}
			// 添加商品版式
			goods.setGoods_top_format_id(CommUtil.null2Long(goods_top_format_id));
			GoodsFormat gf = this.goodsFormatService.getObjById(CommUtil.null2Long(goods_top_format_id));
			if (gf != null) {
				goods.setGoods_top_format_content(gf.getGf_content());
			} else {
				goods.setGoods_top_format_content(null);
			}
			goods.setGoods_bottom_format_id(CommUtil.null2Long(goods_bottom_format_id));
			gf = this.goodsFormatService.getObjById(CommUtil.null2Long(goods_bottom_format_id));
			if (gf != null) {
				goods.setGoods_bottom_format_content(gf.getGf_content());
			} else {
				goods.setGoods_bottom_format_content(null);
			}
			/*
			 * goods.setDelivery_area_id(CommUtil.null2Long(delivery_area_id));
			 * Area de_area = this.areaService.getObjById(CommUtil
			 * .null2Long(delivery_area_id)); String delivery_area =
			 * de_area.getParent().getParent() .getAreaName() +
			 * de_area.getParent().getAreaName() + de_area.getAreaName();
			 * goods.setDelivery_area(delivery_area);
			 */
			if (goods_price != null && store_price != null) {
				double subtract = CommUtil.subtract(goods_price, store_price);
				double div = CommUtil.div(subtract, goods_price);
				double mul = CommUtil.mul(div, 100);
				BigDecimal e = new BigDecimal(mul);
				goods.setGoods_discount_rate(e);

			}
			if (tag != null && !tag.equals("")) {
				goods.setIs_tag(CommUtil.null2Int(tag));
			}
			String featrues = request.getParameter("ksa_features");

			// 阿文产品亮点
			goods.setKsa_features(featrues);
			if (id == null || "".equals(id)) {
				this.goodsService.save(goods);
				this._cgoods(goods.getId(), specList);
				// 图片排序
				if (!temp_ids.isEmpty()) {
					for (Long pid : temp_ids) {

					}
				}
				// 生成商品二维码
				String uploadFilePath = this.configService.getSysConfig().getUploadFilePath();
				goods.setQr_img_path(CommUtil.getURL(request) + "/" + uploadFilePath + "/" + "goods_qr" + "/"
						+ goods.getId() + "_qr.jpg");
				/*
				 * this.goodsTools .createGoodsQR(request,
				 * CommUtil.null2String(goods .getId()), uploadFilePath, goods
				 * .getGoods_main_photo().getId());
				 */
				// 添加lucene索引
				/*
				 * String goods_lucene_path =
				 * System.getProperty("metoob2b2c.root") + File.separator +
				 * "luence" + File.separator + "goods"; File file = new
				 * File(goods_lucene_path); if (!file.exists()) {
				 * CommUtil.createFolder(goods_lucene_path); } LuceneVo vo =
				 * this.luceneVoTools.updateGoodsIndex(goods); SysConfig config
				 * = this.configService.getSysConfig(); LuceneUtil lucene =
				 * LuceneUtil.instance(); lucene.setConfig(config);
				 * lucene.setIndex_path(goods_lucene_path);
				 * lucene.writeIndex(vo);
				 */
			} else {
				// 更新lucene索引
				String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
						+ File.separator + "goods";
				if ("0".equals(obj_status) && "0".equals(publish_goods_status)) {// 编辑后直接上架
					// 更新lucene索引
					/*
					 * File file = new File(goods_lucene_path); if
					 * (!file.exists()) {
					 * CommUtil.createFolder(goods_lucene_path); } LuceneVo vo =
					 * this.luceneVoTools.updateGoodsIndex(goods); LuceneUtil
					 * lucene = LuceneUtil.instance();
					 * lucene.setIndex_path(goods_lucene_path);
					 * lucene.update(CommUtil.null2String(goods.getId()), vo);
					 */
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					lucene.delete_index(id);
				}
				if ("0".equals(obj_status) && ("1".equals(publish_goods_status) || "2".equals(publish_goods_status))) {// 编辑后放入仓库
					// 删除lucene索引
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					lucene.delete_index(id);
				}
				if ("-5".equals(obj_status) && "0".equals(publish_goods_status)) {// (未审核)在仓库中编辑后上架
					// 更新lucene索引
					/*
					 * File file = new File(goods_lucene_path); if
					 * (!file.exists()) {
					 * CommUtil.createFolder(goods_lucene_path); } LuceneVo vo =
					 * this.luceneVoTools .updateGoodsIndex(goods); LuceneUtil
					 * lucene = LuceneUtil.instance();
					 * lucene.setIndex_path(goods_lucene_path);
					 * lucene.update(CommUtil.null2String(goods.getId()), vo);
					 */
					// 删除lucene索引
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					lucene.delete_index(id);

				}
				if (("1".equals(obj_status) || "2".equals(obj_status)) && "0".equals(publish_goods_status)) {// 在仓库中编辑后上架
					// 添加lucene索引
					/*
					 * LuceneVo vo = this.luceneVoTools.updateGoodsIndex(goods);
					 * LuceneUtil lucene = LuceneUtil.instance();
					 * lucene.setIndex_path(goods_lucene_path);
					 * lucene.writeIndex(vo);
					 */
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					lucene.delete_index(id);
				}

				this.goodsService.update(goods);
				this._cgoods(CommUtil.null2Long(id), specList);
				/*
				 * if(specList.size() > 0){ double price =
				 * this._cgoods(CommUtil.null2Long(id), specList); BigDecimal e
				 * = new BigDecimal(price); goods.setGoods_discount_rate(e); }
				 */
			}
			String goods_view_url = CommUtil.getURL(request) + "/goods_" + goods.getId() + ".htm";
			if (this.configService.getSysConfig().isSecond_domain_open()
					&& goods.getGoods_store().getStore_second_domain() != "" && goods.getGoods_type() == 1) {
				String store_second_domain = "http://" + goods.getGoods_store().getStore_second_domain() + "."
						+ CommUtil.generic_domain(request);
				goods_view_url = store_second_domain + "/goods_" + goods.getId() + ".htm";
			}
			if (id == null || id.equals("")) {
				if (store.getGrade().getGoods_audit() == 0) {
					result = new Result(0, "商品发布成功,运营商会尽快为您审核！");
				}
				if (store.getGrade().getGoods_audit() == 1) {
					result = new Result(0, "商品发布成功！");
				}
			} else {
				if (store.getGrade().getGoods_audit() == 0) {
					result = new Result(0, "商品编辑成功,运营商会尽快为您审核");
				}
				if (store.getGrade().getGoods_audit() == 1) {
					result = new Result(0, "商品编辑成功");
				}
			}
		} else {
			result = new Result(1, "parameter error");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	public void _cgoods(Long goodsId, List<Map> specList) {
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		double goods_discount_rate = 0.0;
		double price = 0.0;
		if (obj.getCgoods().size() > 0) {
			List<CGoods> cgoods_list = obj.getCgoods();
			for (CGoods cgoods : cgoods_list) {
				boolean flag = this.cGoodsService.delete(cgoods.getId());
			}
			obj.getCgoods().clear();
		}
		for (Map spec : specList) {
			List<GoodsSpecProperty> goodsSpecPropertyList = new ArrayList<GoodsSpecProperty>();
			List<Accessory> accessory_list = new ArrayList<Accessory>();
			CGoods cgoods = new CGoods();
			cgoods.setAddTime(new Date());
			BigDecimal cgoods_price = CommUtil.null2BigDecimal(spec.get("price").equals("") ? 0 : spec.get("price"));
			BigDecimal cgoods_current_price = CommUtil
					.null2BigDecimal(spec.get("supp").equals("") ? 0 : spec.get("supp"));
			if (!cgoods_price.equals(cgoods_current_price)) {
				double subtract = CommUtil.subtract(cgoods_price, cgoods_current_price);
				double discount_price = CommUtil.div(subtract, cgoods_price) * 100;
				BigDecimal e = new BigDecimal(discount_price);
				cgoods.setCgoods_discount_rate(e);
				goods_discount_rate += discount_price;
			}
			cgoods.setGoods_price(cgoods_price);
			cgoods.setDiscount_price(cgoods_current_price);
			cgoods.setGoods_inventory(CommUtil.null2Int(spec.get("count")));
			cgoods.setCombination_id(CommUtil.null2String(spec.get("id")));
			cgoods.setGoods_tiered_price(CommUtil.null2String(spec.get("goods_tiered_price")));
			cgoods.setGoods(obj);
			String img = CommUtil.null2String(spec.get("img_id"));
			String[] img_id = img.split("_");

			for (String sid : img_id) {
				Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(sid));
				if (accessory != null) {
					accessory_list.add(accessory);
				}
			}
			if (accessory_list.size() > 0) {
				cgoods.setGoods_photos(accessory_list);
			}
			String spr_id = CommUtil.null2String(spec.get("id"));
			String[] spec_id = spr_id.split("_");
			for (String ids : spec_id) {
				GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(ids));
				if (goodsSpecProperty != null) {
					goodsSpecPropertyList.add(goodsSpecProperty);
				}
			}
			cgoods.setC_goods_spec(goodsSpecPropertyList);
			cgoods.setGoods_serial(CommUtil.null2String(spec.get("goodsSerial")));
			cgoods.setGoods_weight(CommUtil.null2Int(spec.get("weight")));
			cgoods.setGoods_length(CommUtil.null2Int(spec.get("length")));
			cgoods.setGoods_width(CommUtil.null2Int(spec.get("width")));
			cgoods.setGoods_high(CommUtil.null2Int(spec.get("high")));
			cgoods.setEid(CommUtil.null2String(spec.get("eid")));
			cgoods.setGoods_disabled(CommUtil.null2String(spec.get("goodsDisabled")));
			cgoods.setSpec_color(CommUtil.null2String(spec.get("spec_color")));
			if (CommUtil.null2Int(spec.get("goods_warn_inventory")) - CommUtil.null2Int(spec.get("count")) > 0) {
				cgoods.setWarn_inventory_status(-1);
			} else {
				cgoods.setWarn_inventory_status(0);
			}
			this.cGoodsService.save(cgoods);
		}

		if (specList.size() != 0) {
			// price = CommUtil.div(goods_discount_rate, specList.size());
			// BigDecimal e = new BigDecimal(price);
			// obj.setGoods_discount_rate(e);
			this.goodsService.update(obj);
		}

	}

	/**
	 * 商品图片上传
	 * 
	 * @param request
	 * @param response
	 * @param store_id
	 */
	@RequestMapping("/photo_save.json")
	public void swfUpload(HttpServletRequest request, HttpServletResponse response, String store_id) {
		Map json_map = new HashMap();
		Result result = null;
		User user = null;
		if (store_id != null && !store_id.equals("")) {
			Store store = this.storeService.getObjById(CommUtil.null2Long(store_id));
			user = store.getUser();
		}
		user = user.getParent() == null ? user : user.getParent();
		String path = this.storeTools.createUserFolder(request, user.getStore());
		String url = this.storeTools.createUserFolderURL(user.getStore());
		double csize = CommUtil.fileSize(new File(path));
		double img_remain_size = 0;
		if (user.getStore().getGrade().getSpaceSize() > 0) {
			img_remain_size = CommUtil.div(user.getStore().getGrade().getSpaceSize() * 1024 - csize, 1024);
			json_map.put("remainSpace", img_remain_size);
		}
		if (img_remain_size >= 0) {
			try {
				Map map = CommUtil.httpsaveFileToServer(request, "imgFile", url, null, null);
				Map params = new HashMap();
				params.put("store_id", CommUtil.null2Long(store_id));
				List<WaterMark> wms = this.waterMarkService
						.query("select obj from WaterMark obj where obj.store.id=:store_id", params, -1, -1);
				if (wms.size() > 0) {
					WaterMark mark = wms.get(0);
					if (mark.isWm_image_open()) {
						String pressImg = request.getSession().getServletContext().getRealPath("") + File.separator
								+ mark.getWm_image().getPath() + File.separator + mark.getWm_image().getName();
						String targetImg = path + File.separator + map.get("fileName");
						int pos = mark.getWm_image_pos();
						float alpha = mark.getWm_image_alpha();
						CommUtil.waterMarkWithImage(pressImg, targetImg, pos, alpha);
					}
					if (mark.isWm_text_open()) {
						String targetImg = path + File.separator + map.get("fileName");
						int pos = mark.getWm_text_pos();
						String text = mark.getWm_text();
						String markContentColor = mark.getWm_text_color();
						CommUtil.waterMarkWithText(targetImg, targetImg, text, markContentColor,
								new Font(mark.getWm_text_font(), Font.BOLD, mark.getWm_text_font_size()), pos, 100f);
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
				this.accessoryService.save(image);

				String urls = this.configService.getSysConfig().getImageWebServer() + "/" + url + "/" + image.getName();
				json_map.put("url", urls);
				json_map.put("id", image.getId());
				double csize2 = CommUtil.fileSize(new File(urls));
				double img_remain_size2 = 0;
				/*
				 * if (user.getStore().getGrade().getSpaceSize() > 0) {
				 * img_remain_size2 = CommUtil.div(user.getStore().getGrade()
				 * .getSpaceSize() 1024 - csize2, 1024);
				 * json_map.put("remainSpace", img_remain_size2); } else {
				 * json_map.put("remainSpace", "null"); }
				 */
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result = new Result(0, "success", json_map);
		} else {
			json_map.put("url", "");
			json_map.put("id", "");
			// json_map.put("remainSpace", -1);
			result = new Result(1, "error");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	@SecurityMapping(title = "商品图片删除", value = "/goods_image_del.htm*", rtype = "seller", rname = "发布新商品", rcode = "goods_seller", rgroup = "商品管理")
	@RequestMapping("/goods_image_del.json")
	public void goods_image_del(HttpServletRequest request, HttpServletResponse response, String image_id,
			String user_id) {
		User user = null;
		Result result = null;
		if (user_id == null || user_id.equals("")) {
			result = new Result(1, "用户信息错误");
		} else {
			user = this.userService.getObjById(CommUtil.null2Long(user_id));
			user = user.getParent() == null ? user : user.getParent();
			String path = this.storeTools.createUserFolder(request, user.getStore());
			Map map = new HashMap();
			Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(image_id));
			Boolean ret = false;
			int code = -1;
			if (accessory != null) {
				String url = this.configService.getSysConfig().getImageWebServer() + "/api/delete";
				String params = accessory.getPath() + "/" + accessory.getName();
				String res = Http.doPost(url, params);
				if (res != null || !res.equals("")) {
					JsonParser parse = new JsonParser(); // 创建json解析器
					JsonObject json = (JsonObject) parse.parse(res); // 创建jsonObject对象
					code = json.get("code").getAsInt();
				}
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
					for (CGoods cgoods : accessory.getC_goods_list()) {
						cgoods.getGoods_photos().remove(accessory);
						this.cGoodsService.update(cgoods);
					}
					ret = this.accessoryService.delete(accessory.getId());
					/*
					 * if (ret) { CommUtil.deleteFile(request.getSession().
					 * getServletContext().getRealPath("/") +
					 * accessory.getPath() + File.separator +
					 * accessory.getName() + "_middle." + accessory.getExt());
					 * CommUtil.del_acc(request, accessory); } double csize2 =
					 * CommUtil.fileSize(new File(path)); double
					 * img_remain_size2 = 0; if
					 * (user.getStore().getGrade().getSpaceSize() > 0) {
					 * img_remain_size2 =
					 * CommUtil.div(user.getStore().getGrade().getSpaceSize() *
					 * 1024 - csize2, 1024); map.put("remainSpace",
					 * img_remain_size2); } else { map.put("remainSpace",
					 * "null"); }
					 */
					result = new Result(0, "success");
				} else {
					result = new Result(1, "error");
				}
			}
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 商品上下架
	 * 
	 * @param request
	 * @param response
	 * @param mulitId商品id
	 * @param uid用户id
	 */
	@RequestMapping("/goods/sale.json")
	public void goodsSale(HttpServletRequest request, HttpServletResponse response, String mulitId, String uid) {
		Result result = null;
		int ret = -1;
		if (mulitId != null && !mulitId.equals("")) {
			if (uid != null && !uid.equals("")) {
				String[] ids = mulitId.split(",");
				for (String id : ids) {
					Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
					if (goods.getGoods_status() != -5) {
						User user = this.userService.getObjById(CommUtil.null2Long(uid));
						user = user.getParent() == null ? user : user.getParent();
						if (goods.getGoods_store().getUser().getId().equals(user.getId())) {
							int goods_status = goods.getGoods_status() == 0 ? 1 : 0;
							goods.setGoods_status(goods_status);
							this.goodsService.save(goods);
							String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
									+ File.separator + "goods";
							File file = new File(goods_lucene_path);
							if (!file.exists()) {
								CommUtil.createFolder(goods_lucene_path);
							}
							if (goods.getGoods_status() == 0) {
								// 更新lucene索引
								/*
								 * String goods_lucene_path =
								 * System.getProperty("user.dir") +
								 * File.separator + "luence" + File.separator +
								 * "goods";
								 */

								/*
								 * LuceneVo vo =
								 * this.luceneVoTools.updateGoodsIndex(goods);
								 * LuceneUtil lucene = LuceneUtil.instance();
								 * lucene.setIndex_path(goods_lucene_path);
								 * lucene.update(CommUtil.null2String(goods.
								 * getId()), vo);
								 */
								LuceneVo vo = this.luceneVoTools.updateGoodsIndex(goods);
								SysConfig config = this.configService.getSysConfig();
								LuceneUtil lucene = LuceneUtil.instance();
								lucene.setConfig(config);
								lucene.setIndex_path(goods_lucene_path);
								lucene.writeIndex(vo);
								ret = 0;
							} else {
								// 删除索引
								LuceneUtil lucenes = LuceneUtil.instance();
								lucenes.setIndex_path(goods_lucene_path);
								lucenes.delete_index(id);
								ret = 0;
							}
						} else {
							ret = -100;
						}
					} else {
						ret = -5;
					}
				}
			} else {
				ret = 1;
			}
		} else {
			ret = 1;
		}
		CommUtil.returnJson(Json.toJson(new Result(ret), JsonFormat.compact()), response);
	}

	/**
	 * @description 商品删除
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param uid
	 */
	@RequestMapping("/goods/delete.json")
	public void phpGoodsDelete(HttpServletRequest request, HttpServletResponse response, String mulitId, String uid) {
		Result result = null;
		String[] ids = mulitId.split(",");
		if (ids == null && "".equals(ids)) {
			result = new Result(1, "parameter error");
		} else {
			for (String id : ids) {
				Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
				if (goods == null) {
					result = new Result(1, "parameter error");
				} else {
					User user = this.userService.getObjById(CommUtil.null2Long(uid));
					user = user.getParent() == null ? user : user.getParent();
					if (goods.getGoods_store().getUser().getId().equals(user.getId())) {
						List<Evaluate> evaluates = goods.getEvaluates();
						for (Evaluate evaluate : evaluates) {
							this.evaluateService.delete(evaluate.getId());
						}
						Map params = new HashMap();
						params.put("gid", CommUtil.null2Long(id));
						params.clear();
						for (Accessory acc : goods.getGoods_photos()) {
							params.put("acid", acc.getId());
							List<Album> als = this.albumService.query(
									"select obj from Album obj where obj.album_cover.id = :acid", params, -1, -1);
							for (Album al : als) {
								al.setAlbum_cover(null);
								this.albumService.update(al);
							}
							params.clear();
						}
						if (goods.getGoods_main_photo() != null) {
							params.put("acid", goods.getGoods_main_photo().getId());
							List<Album> als = this.albumService.query(
									"select obj from Album obj where obj.album_cover.id = :acid", params, -1, -1);
							for (Album al : als) {
								al.setAlbum_cover(null);
								this.albumService.update(al);
							}
							CommUtil.del_acc(request, goods.getGoods_main_photo());
							goods.setGoods_main_photo(null);
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
						/*
						 * if (goods.getGoods_collect() == 0) {
						 * this.goodsService.delete(goods.getId()); } else {
						 */
						goods.setGoods_status(-3);
						this.goodsService.update(goods);
						// }
						// 删除索引
						String goods_lucene_path = System.getProperty("metoob2b2c.root") + "luence" + File.separator
								+ "goods";
						File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.delete_index(CommUtil.null2String(id));
						result = new Result(0, "Successfully");
					} else {
						result = new Result(-100, "token Invalidation");
					}
				}
			}
			CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
		}
	}

	/**
	 * @description 卖家批量修改海外仓库存
	 * @param request
	 * @param response
	 * @param goods_id
	 *            商品id
	 * @param sku_id
	 *            子商品id
	 * @param inventory
	 *            库存
	 */
	@RequestMapping("v1/goods/inventory.json")
	public void batchEditOverseaInventory(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "goods_id", required = true) String goods_id, String sku_id, String inventory) {
		int code = -1;
		String msg = "";
		int oversea_inventory = 0;
		if (null != inventory && CommUtil.null2Int(inventory) > oversea_inventory) {
			oversea_inventory = CommUtil.null2Int(inventory);
		}
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		if (null != obj) {
			CGoods coogds = this.cGoodsService.getObjById(CommUtil.null2Long(sku_id));
			if (null != coogds) {
				if (coogds.getGoods().getId().equals(obj.getId())) {
					coogds.setOversea_inventory(oversea_inventory);
					this.cGoodsService.update(coogds);
					code = 5200;
					msg = "Successfully";
				} else {
					code = 5213;
					msg = "sku不属于该商品";
				}
			} else {
				obj.setOversea_inventory(oversea_inventory);
				this.goodsService.update(obj);
				code = 5200;
				msg = "Successfully";
			}
		} else {
			code = 5405;
			msg = "资源为空";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	/**
	 * @description 批量修改价格或库存
	 * @param request
	 * @param response
	 * @param muiltId
	 *            商品id
	 * @param type
	 *            运费承担方式 0：买家 1：卖家
	 * @param goods_price
	 *            商品价格
	 * @param goods_current_price
	 *            商品原价
	 * @param inventory
	 *            商品库存
	 * @param inventory_detail
	 *            子商品库存
	 */
	@RequestMapping("v1/goods/property.json")
	public void batchGoodsTransfee(HttpServletRequest request, HttpServletResponse response, String muiltId,
			String type, String goods_price, String goods_current_price, String goods_inventory,
			String inventory_detail) {
		int code = 5200;
		String msg = "Successfulluy";
		BigDecimal price = new BigDecimal(0.00);
		BigDecimal current_price = new BigDecimal(0.00);
		String ids[] = muiltId.split(",");
		for (String id : ids) {
			Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
			if (type != null && !"".equals(type)) {
				obj.setGoods_transfee(Integer.parseInt(type));
			} else {
				if (inventory_detail != null && obj.getInventory_type().equals("spec")
						&& !"".equals(inventory_detail)) {
					Map<Object, Object> map = Json.fromJson(Map.class, inventory_detail);
					Map<BigDecimal, BigDecimal> minmumPriceMap = new HashMap<BigDecimal, BigDecimal>();
					for (Object cid : map.keySet()) {
						CGoods sku = this.cGoodsService.getObjById(CommUtil.null2Long(cid));
						if (null != sku) {
							Map<String, Object> childMap = JSONObject.fromObject(map.get(cid));
							sku.setGoods_price(CommUtil.null2BigDecimal(childMap.get("goods_price")));
							sku.setDiscount_price(CommUtil.null2BigDecimal(childMap.get("goods_current_price") == null
									? 0 : childMap.get("goods_current_price")));
							sku.setGoods_inventory(CommUtil.null2Int(childMap.get("goods_inventory")));
							BigDecimal value = minmumPriceMap
									.get(CommUtil.null2BigDecimal(childMap.get("goods_current_price")));
							if (value == null || CommUtil.subtract(value,
									CommUtil.null2BigDecimal(childMap.get("goods_price"))) < 0) {
								minmumPriceMap.put(CommUtil.null2BigDecimal(childMap.get("goods_current_price")),
										CommUtil.null2BigDecimal(childMap.get("goods_price")));
							}
							this.cGoodsService.update(sku);
						}
					}
					// 计算键最小折扣率
					// Multimap<String, String> map = TreeMultimap.create();
					current_price = this.mgoodsViewTools.getMinKey(minmumPriceMap);
					price = minmumPriceMap.get(current_price);
				} else {
					obj.setGoods_inventory(CommUtil.null2Int(goods_inventory));
					price = CommUtil.null2BigDecimal(goods_price);
					current_price = CommUtil.null2BigDecimal(goods_current_price);
				}
				obj.setGoods_price(price);
				obj.setStore_price(current_price);
				obj.setGoods_current_price(current_price);
				obj.setGoods_discount_rate(this.mgoodsViewTools.getGoodsRate(price, current_price));
				this.goodsService.update(obj);
			}
			// 更新商品索引 app排序 运费承担方式
			this.goodsService.update(obj);
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
				String rate = this.mgoodsViewTools.getRate(obj.getGoods_price(), obj.getGoods_current_price());
				vo.setVo_rate(rate);
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
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}
}
