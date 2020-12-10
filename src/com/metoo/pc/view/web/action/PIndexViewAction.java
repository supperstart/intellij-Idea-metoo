package com.metoo.pc.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.ip.IPSeeker;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsTypeProperty;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.pc.view.web.tool.PGoodsClassTools;

@Controller
@RequestMapping("/pc/")
public class PIndexViewAction {
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private PGoodsClassTools goodsClassTools;
	@Autowired
	private IGoodsBrandService brandService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private MGoodsViewTools metooGoodsViewTools;

	/**
	 * PC-类目列表
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@RequestMapping(value = "class.json", method = RequestMethod.GET)
	public void Class(HttpServletRequest request, HttpServletResponse response, String id) {
		Result result = null;
		Map map = new HashMap();
		if (id != null && !id.equals("")) {
			GoodsClass gc = this.goodsClassService.getObjById(CommUtil.null2Long(id));
			if (gc != null) {
				Set<GoodsClass> gcs = gc.getChilds();
				List transition = new ArrayList(gcs);
				List list = this.goodsClassTools.info(transition);
				map.put("goodsClass", list);
				result = new Result(200, "success", map);
			} else {
				result = new Result(3205, "资源为空");
			}
		} else {
			Map params = new HashMap();
			params.put("display", true);
			List<GoodsClass> gcs = this.goodsClassService.query(
					"select new GoodsClass(id,className) from GoodsClass obj where obj.parent.id is null and obj.display=:display order by obj.sequence asc ",
					params, -1, -1);
			List list = this.goodsClassTools.info(gcs);
			map.put("goodsClass", list);
			result = new Result(200, "success", map);
		}

		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("utf-8");
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
	 * PC-根据商城分类查看商品列表
	 * 
	 * @param request
	 * @param response
	 * @param gc_id
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @return
	 */
	@RequestMapping("class/goods.json")
	public void store_goods_list(HttpServletRequest request, HttpServletResponse response, String gc_id,
			String currentPage, String orderBy, String orderType, String brand_ids, String gs_ids, String properties,
			String all_property_status, String detail_property_status, String goods_type, String goods_inventory,
			String goods_transfee, String goods_cod, String gc_ids) {
		Result result = null;
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Map wap_map = new HashMap();
		ModelAndView mv = new JModelAndView("store_goods_list.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		GoodsClass gc = this.goodsClassService.getObjById(CommUtil.null2Long(gc_id));
		Set gc_list = new TreeSet();
		if (gc != null) {
			if (gc.getLevel() == 0) {
				gc_list = gc.getChilds();
			} else if (gc.getLevel() == 1) {
				gc_list = gc.getParent().getChilds();
			} else if (gc.getLevel() == 2) {
				gc_list = gc.getParent().getParent().getChilds();
			}

			List<Map> gc_list_map = new ArrayList<Map>();
			for (Iterator<GoodsClass> iterator = gc_list.iterator(); iterator.hasNext();) {
				GoodsClass goodsclass = iterator.next();
				Map gc_map = new HashMap();
				gc_map.put("gc_id", goodsclass.getId());
				gc_map.put("gc_name", goodsclass.getClassName());
				if (goodsclass.getChilds() != null && !goodsclass.getChilds().equals("")) {
					List<Map> gcc_list_map = new ArrayList<Map>();
					for (Iterator<GoodsClass> iteratorc = goodsclass.getChilds().iterator(); iterator.hasNext();) {
						GoodsClass goodsclassc = iterator.next();
						Map gcc_map = new HashMap();
						gcc_map.put("gcc_name", goodsclassc.getClassName());
						gcc_map.put("gcc_id", goodsclassc.getId());
						gcc_list_map.add(gcc_map);
					}
					gc_map.put("gcc_list_map", gcc_list_map);
					gc_list_map.add(gc_map);
				}
			}
		}

		if (orderBy == null || orderBy.equals("")) {
			orderBy = "weightiness";
		}
		if (orderType == null || orderType.equals("")) {
			orderType = "desc";
		}
		GoodsQueryObject gqo = new GoodsQueryObject(null, currentPage, mv, orderBy, orderType);
		Set<Long> ids = null;
		if (gc != null) {
			ids = this.genericIds(gc.getId());
		}
		if (ids != null && ids.size() > 0) {
			Map paras = new HashMap();
			paras.put("ids", ids);
			gqo.addQuery("obj.gc.id in (:ids)", paras);
		} else {
			Set<Long> goodsclass_id = null;
			goodsclass_id = this.genericGcIds(gc_ids);
			if (goodsclass_id != null && !goodsclass_id.equals("")) {
				Map paras = new HashMap();
				paras.put("ids", goodsclass_id);
				gqo.addQuery("obj.gc.id in (:ids)", paras);
			}
		}

		if (goods_cod != null && !goods_cod.equals("")) {
			gqo.addQuery("obj.goods_cod", new SysMap("goods_cod", 0), "=");
			mv.addObject("goods_cod", goods_cod);
		}
		if (goods_transfee != null && !goods_transfee.equals("")) {
			gqo.addQuery("obj.goods_transfee", new SysMap("goods_transfee", 1), "=");
			mv.addObject("goods_transfee", goods_transfee);
		}
		gqo.setPageSize(20);// 设定分页查询，每页30件商品
		gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");

		List<Map> goods_property = new ArrayList<Map>();

		if (!CommUtil.null2String(brand_ids).equals("")) {
			if (brand_ids.indexOf(",") < 0) {
				brand_ids = brand_ids + ",";
			}
			String[] brand_id_list = CommUtil.null2String(brand_ids).split(",");
			if (brand_id_list.length == 1) {
				String brand_id = brand_id_list[0];
				gqo.addQuery("obj.goods_brand.id", new SysMap("brand_id", CommUtil.null2Long(brand_id)), "=", "and");
				Map map = new HashMap();
				GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
				if (brand != null) {
					map.put("name", "品牌");
					map.put("value", brand.getName());
					map.put("type", "brand");
					map.put("id", brand.getId());
					goods_property.add(map);
				}
			} else {
				for (int i = 0; i < brand_id_list.length; i++) {
					String brand_id = brand_id_list[i];
					if (i == 0) {
						gqo.addQuery("and (obj.goods_brand.id=" + CommUtil.null2Long(brand_id), null);
						Map map = new HashMap();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					} else if (i == brand_id_list.length - 1) {
						gqo.addQuery("or obj.goods_brand.id=" + CommUtil.null2Long(brand_id) + ")", null);
						Map map = new HashMap();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					} else {
						gqo.addQuery("or obj.goods_brand.id=" + CommUtil.null2Long(brand_id), null);
						Map map = new HashMap();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					}
				}
			}
			if (brand_ids != null && !brand_ids.equals("")) {
				wap_map.put("brand_ids", brand_ids);
			}
		}

		if (!CommUtil.null2String(gs_ids).equals("")) {
			List<List<GoodsSpecProperty>> gsp_lists = this.generic_gsp(gs_ids);
			for (int j = 0; j < gsp_lists.size(); j++) {
				List<GoodsSpecProperty> gsp_list = gsp_lists.get(j);
				if (gsp_list.size() == 1) {
					GoodsSpecProperty gsp = gsp_list.get(0);
					gqo.addQuery("gsp" + j, gsp, "obj.goods_specs", "member of", "and");
					Map map = new HashMap();
					map.put("spec_name", gsp.getSpec().getName());
					map.put("value", gsp.getValue());
					map.put("type", "gs");
					map.put("id", gsp.getId());
					goods_property.add(map);
				} else {
					for (int i = 0; i < gsp_list.size(); i++) {
						if (i == 0) {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs", "member of", "and(");
							Map map = new HashMap();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						} else if (i == gsp_list.size() - 1) {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs)", "member of", "or");
							Map map = new HashMap();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						} else {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs", "member of", "or");
							Map map = new HashMap();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						}
					}
				}
			}

			wap_map.put("gs_ids", gs_ids);
		}

		List<Map> propertylist = null;
		if (!CommUtil.null2String(properties).equals("")) {
			String[] properties_list = properties.substring(1).split("\\|");
			for (int i = 0; i < properties_list.length; i++) {
				String property_info = CommUtil.null2String(properties_list[i]);
				String[] property_info_list = property_info.split(",");
				GoodsTypeProperty gtp = this.goodsTypePropertyService
						.getObjById(CommUtil.null2Long(property_info_list[0]));
				Map p_map = new HashMap();
				p_map.put("gtp_name" + i, "%" + gtp.getName().trim() + "%");
				p_map.put("gtp_value" + i, "%" + property_info_list[1].trim() + "%");
				gqo.addQuery("and (obj.goods_property like :gtp_name" + i + " and obj.goods_property like :gtp_value"
						+ i + ")", p_map);
				Map map = new HashMap();
				map.put("name", gtp.getName());
				map.put("value", property_info_list[1]);
				map.put("type", "properties");
				map.put("id", gtp.getId());
				goods_property.add(map);
			}
			wap_map.put("properties", properties);

			// 处理筛选类型互斥,|1,超短裙（小于75cm）|2,纯色
			List<GoodsTypeProperty> filter_properties = new ArrayList<GoodsTypeProperty>();
			List<String> hc_property_list = new ArrayList<String>();// 已经互斥处理过的属性值，在循环中不再处理
			if (gc.getGoodsType() != null) {
				for (GoodsTypeProperty gtp : gc.getGoodsType().getProperties()) {
					boolean flag = true;
					GoodsTypeProperty gtp1 = new GoodsTypeProperty();
					gtp1.setDisplay(gtp.isDisplay());
					gtp1.setGoodsType(gtp.getGoodsType());
					gtp1.setHc_value(gtp.getHc_value());
					gtp1.setId(gtp.getId());
					gtp1.setName(gtp.getName());
					gtp1.setSequence(gtp.getSequence());
					gtp1.setValue(gtp.getValue());
					for (String hc_property : hc_property_list) {
						String[] hc_list = hc_property.split(":");
						if (hc_list[0].equals(gtp.getName())) {
							String[] hc_temp_list = hc_list[1].split(",");
							String[] defalut_list_value = gtp1.getValue().split(",");
							ArrayList<String> defalut_list = new ArrayList<String>(Arrays.asList(defalut_list_value));
							for (String hc_temp : hc_temp_list) {
								defalut_list.remove(hc_temp);
							}
							String value = "";
							for (int i = defalut_list.size() - 1; i >= 0; i--) {
								value = defalut_list.get(i) + "," + value;
							}
							gtp1.setValue(value.substring(0, value.length() - 1));
							flag = false;
							break;
						}

					}
					if (flag) {
						if (!CommUtil.null2String(gtp.getHc_value()).equals("")) {// 取消互斥类型
							String[] list1 = gtp.getHc_value().split("#");
							for (int i = 0; i < properties_list.length; i++) {
								String property_info = CommUtil.null2String(properties_list[i]);
								String[] property_info_list = property_info.split(",");
								if (property_info_list[1].equals(list1[0])) {// 存在该互斥，则需要进行处理
									hc_property_list.add(list1[1]);
								}
							}

						}
						filter_properties.add(gtp);
					} else {
						filter_properties.add(gtp1);
					}
				}
				Map goodsTypemap = new HashMap();
				propertylist = new ArrayList<Map>();
				for (GoodsTypeProperty goodsType : filter_properties) {
					goodsTypemap.put("id", goodsType.getId());
					goodsTypemap.put("name", goodsType.getName());
					// [属性可选值]
					goodsTypemap.put("value", CommUtil.splitByChar(goodsType.getValue(), ","));
					propertylist.add(goodsTypemap);
				}
				wap_map.put("propertylist", propertylist);
			}
		} else {
			// 处理筛选类型互斥
			try {
				if (gc.getGoodsType() != null) {
					List<GoodsTypeProperty> goodstype_property = gc.getGoodsType().getProperties();

					List<Map> goodsTypeProperty_list = new ArrayList<Map>();

					for (GoodsTypeProperty goodsTypeProperty : goodstype_property) {
						Map goodsTypePropertyMap = new HashMap();
						goodsTypePropertyMap.put("Property_id", goodsTypeProperty.getId());
						goodsTypePropertyMap.put("Property_name", goodsTypeProperty.getName());

						List<Map> goodsTypeProperty_v_list = new ArrayList<Map>();
						for (String s : CommUtil.splitByChar(goodsTypeProperty.getValue(), ",")) {
							Map goodsTypeProperty_v_Map = new HashMap();
							goodsTypeProperty_v_Map.put("v_info", s);
							goodsTypeProperty_v_list.add(goodsTypeProperty_v_Map);
						}
						goodsTypePropertyMap.put("v_info_value", goodsTypeProperty_v_list);
						goodsTypeProperty_list.add(goodsTypePropertyMap);
					}
					wap_map.put("propertylist", goodsTypeProperty_list);
				} else {
					wap_map.put("propertylist", "");
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				wap_map.put("propertylist", "");
			}
		}

		try {
			if (gc.getGoodsType().getGbs().size() > 0) {
				List<Map> gb_list = new ArrayList<Map>();
				for (GoodsBrand gb : gc.getGoodsType().getGbs()) {
					Map gbmap = new HashMap();
					gbmap.put("gb_id", gb.getId());
					gbmap.put("gb_name", gb.getName());
					gbmap.put("gb_photo", this.configService.getSysConfig().getImageWebServer() + "/"
							+ gb.getBrandLogo().getPath() + "/" + gb.getBrandLogo().getName());
					gb_list.add(gbmap);
				}
				wap_map.put("gb_list", gb_list);
			}
		} catch (NullPointerException e1) {
			// TODO Auto-generated catch block
			wap_map.put("gb_list", "");
		}

		/*
		 * if (CommUtil.null2Int(goods_inventory) == 0) {// 查询库存大于0
		 * gqo.addQuery("obj.goods_inventory", new SysMap("goods_inventory", 0),
		 * ">"); }
		 */ if (!CommUtil.null2String(goods_type).equals("") && CommUtil.null2Int(goods_type) != -1) {// 查询自营或者第三方经销商商品
			gqo.addQuery("obj.goods_type", new SysMap("goods_type", CommUtil.null2Int(goods_type)), "=");
		}
		IPageList pList = this.goodsService.list(gqo);
		wap_map.put("goods_Pages", pList.getPages());
		// [商品信息]
		List<Goods> obj = pList.getResult();
		List<Map> goodslist = new ArrayList<Map>();
		Set<Map> goodsset = new HashSet<Map>();
		for (Goods goods : obj) {
			Map goodsmap = new HashMap();
			goodsmap.put("goodsid", goods.getId());
			goodsmap.put("goods_name", goods.getGoods_name());
			goodsmap.put("goods_price", goods.getGoods_price());
			goodsmap.put("goods_current_price", goods.getGoods_current_price());
			goodsmap.put("well_evaluate", goods.getWell_evaluate() == null ? 0 : goods.getWell_evaluate());
			goodsmap.put("goods_main_photo", goods.getGoods_main_photo() != null
					? imageWebServer + "/" + goods.getGoods_main_photo().getPath() + "/"
							+ goods.getGoods_main_photo().getName() + "_middle." + goods.getGoods_main_photo().getExt()
					: imageWebServer + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
							+ this.configService.getSysConfig().getGoodsImage().getName() + "_middle."
							+ this.configService.getSysConfig().getGoodsImage().getExt());
			goodsmap.put("goodsext", goods.getAccessory() != null ? goods.getAccessory().getExt() : null);
			List<Accessory> acc = goods.getGoods_photos();

			List<Map> acclist = new ArrayList<Map>();
			for (Accessory accessory : acc) {
				Map accmap = new HashMap();
				accmap.put("photos", configService.getSysConfig().getImageWebServer() + "/" + accessory.getPath() + "/"
						+ accessory.getName() + "_middle." + accessory.getExt());
				acclist.add(accmap);
			}

			goodsmap.put("goods_photo", acclist);
			if (goods.getGoods_store() != null && !goods.getGoods_store().equals("")) {
				try {
					goodsmap.put("store_logo",
							this.configService.getSysConfig().getImageWebServer() + "/"
									+ goods.getGoods_store().getStore_logo().getPath() + "/"
									+ goods.getGoods_store().getStore_logo().getName());
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					goodsmap.put("store_logo", "");
				}
			}
			goodslist.add(goodsmap);
		}
		wap_map.put("goodslist", goodslist);
		// [已选择商品属性]
		Map goods_property_map = new HashMap();
		List<Map> goods_property_list = new ArrayList<Map>();
		for (Map map : goods_property) {
			goods_property_map.put("name", map.get("name"));
			goods_property_map.put("value", map.get("value"));
			goods_property_map.put("id", map.get("id"));
			goods_property_map.put("type", map.get("type"));
			goods_property_list.add(goods_property_map);
		}
		wap_map.put("goods_property_list", goods_property_list);
		wap_map.put("orderBy", orderBy);
		wap_map.put("goods_property", goods_property);
		wap_map.put("allCount", pList.getRowCount());
		if (detail_property_status != null && !detail_property_status.equals("")) {
			mv.addObject("detail_property_status", detail_property_status);
			String temp_str[] = detail_property_status.split(",");
			Map pro_map = new HashMap();
			List pro_list = new ArrayList();
			for (String property_status : temp_str) {
				if (property_status != null && !property_status.equals("")) {
					String mark[] = property_status.split("_");
					pro_map.put(mark[0], mark[1]);

					pro_list.add(mark[0]);
				}
			}
			/*
			 * mv.addObject("pro_list", pro_list); mv.addObject("pro_map",
			 * pro_map);
			 */
		}
		wap_map.put("all_property_status", all_property_status);

		// 计算当期访问用户的IP地址，并计算对应的运费信息
		String current_ip = CommUtil.getIpAddr(request);// 获得本机IP
		if (CommUtil.isIp(current_ip)) {
			IPSeeker ip = new IPSeeker(null, null);
			String current_city = ip.getIPLocation(current_ip).getCountry();
			wap_map.put("current_city", current_city);
			// mv.addObject("current_city", current_city);
		} else {
			wap_map.put("current_city", "未知地区");
		}
		wap_map.put("goods_inventory", CommUtil.null2Int(goods_inventory));
		wap_map.put("goods_type", CommUtil.null2String(goods_type).equals("") ? -1 : CommUtil.null2Int(goods_type));
		result = new Result(3200, "查询成功", wap_map);
		String gctemp = Json.toJson(result, JsonFormat.compact());
		try {
			response.getWriter().print(gctemp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 查询类目子id
	 * 
	 * @param id
	 * @return
	 */
	private Set<Long> genericIds(Long id) {
		Set<Long> ids = new HashSet<Long>();
		if (id != null) {
			ids.add(id);
			Map params = new HashMap();
			params.put("pid", id);
			List id_list = this.goodsClassService.query("select obj.id from GoodsClass obj where obj.parent.id=:pid",
					params, -1, -1);
			ids.addAll(id_list);
			for (int i = 0; i < id_list.size(); i++) {
				Long cid = CommUtil.null2Long(id_list.get(i));
				Set<Long> cids = genericIds(cid);
				ids.add(cid);
				ids.addAll(cids);
			}
		}
		return ids;
	}

	/**
	 * 查询多个一级类目的子类目
	 * 
	 * @param gclass_ids
	 * @return
	 */
	private Set<Long> genericGcIds(String gclass_ids) {
		String[] gc_ids = gclass_ids.split(",");
		Set<Long> ids = new HashSet<Long>();
		for (String id : gc_ids) {
			if (id != null && !id.equals("")) {
				Long lid = Long.parseLong(id);
				ids.add(lid);
				Map params = new HashMap();
				params.put("pid", lid);
				List id_list = this.goodsClassService
						.query("select obj.id from GoodsClass obj where obj.parent.id=:pid", params, -1, -1);
				ids.addAll(id_list);
				for (int i = 0; i < id_list.size(); i++) {
					Long cid = CommUtil.null2Long(id_list.get(i));
					Set<Long> cids = genericIds(cid);
					ids.add(cid);
					ids.addAll(cids);
				}
			}
		}
		return ids;
	}

	private List<List<GoodsSpecProperty>> generic_gsp(String gs_ids) {
		List<List<GoodsSpecProperty>> list = new ArrayList<List<GoodsSpecProperty>>();
		String[] gs_id_list = gs_ids.substring(1).split("\\|");
		for (String gd_id_info : gs_id_list) {
			String[] gs_info_list = gd_id_info.split(",");
			GoodsSpecProperty gsp = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gs_info_list[0]));
			boolean create = true;
			for (List<GoodsSpecProperty> gsp_list : list) {
				for (GoodsSpecProperty gsp_temp : gsp_list) {
					if (gsp_temp.getSpec().getId().equals(gsp.getSpec().getId())) {
						gsp_list.add(gsp);
						create = false;
						break;
					}
				}
			}
			if (create) {
				List<GoodsSpecProperty> gsps = new ArrayList<GoodsSpecProperty>();
				gsps.add(gsp);
				list.add(gsps);
			}
		}
		return list;
	}

	/**
	 * 根据用户浏览记录 猜你喜欢的商品列表
	 * 
	 * @param request
	 * 
	 * @param response
	 */
	@RequestMapping("/you_likegoods.json")
	public void you_likegoods(HttpServletRequest request, HttpServletResponse response, String orderBy,
			String orderType, String currentPage) {
		Result result = null;
		Map resultMap = new HashMap();
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		if (orderBy == null && orderType.equals("")) {
			orderBy = "weightiness";
		}
		if (orderType == null && orderType.equals("")) {
			orderType = "desc";
		}
		GoodsQueryObject gqo = new GoodsQueryObject(null, currentPage, mv, orderBy, orderType);
		gqo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
		gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
		gqo.setPageSize(8);

		Long gc_id = null;
		Cookie[] cookies = request.getCookies();
		List<Goods> list = new ArrayList<Goods>();
		List<Goods> objList = new ArrayList<Goods>();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("goodscookie")) {
					String[] goods_ids = cookie.getValue().split(",", 2);
					Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_ids[0]));
					if (obj == null)
						break;
					gc_id = obj.getGc().getId();
					gqo.addQuery("obj.gc.id", new SysMap("gc_id", CommUtil.null2Long(gc_id)), "=", "and");
					gqo.addQuery("obj.id", new SysMap("id", obj.getId()), "is not");
					IPageList pList = this.goodsService.list(gqo);
					objList = pList.getResult();
					int gcs_size = objList.size();
					if (pList.getPages() >= Integer.parseInt(currentPage)) {
						if (gcs_size < 8) {
							List<Goods> like_goods = this.goodsService.query(
									"select obj from Goods obj where obj.goods_status=0 and obj.id is not "
											+ obj.getId()
											+ " and obj.goods_store.store_status=15 order by obj.weightiness desc",
									null, 0, 8 - gcs_size);
							for (int i = 0; i < like_goods.size(); i++) {
								int k = 0;
								for (int j = 0; j < gcs_size; j++) {
									if (like_goods.get(i).getId().equals(objList.get(j).getId())) {
										k++;
									}
								}
								if (k == 0) {
									objList.add(like_goods.get(i));
								}
							}
						}
						this.metooGoodsViewTools.goods(objList, resultMap);
						resultMap.put("currentPage", pList.getCurrentPage());
						resultMap.put("Pages", pList.getPages());
						break;
					} else {
						int page = Integer.parseInt(currentPage) - pList.getPages();
						GoodsQueryObject qo = new GoodsQueryObject(null, CommUtil.null2String(page), mv, "weightiness",
								"desc");
						qo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
						qo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
						qo.setPageSize(8);
						pList = this.goodsService.list(qo);
						objList = pList.getResult();
						this.metooGoodsViewTools.goods(objList, resultMap);
						resultMap.put("currentPage", pList.getCurrentPage());
						resultMap.put("Pages", pList.getPages());
						break;
					}
				} else {
					IPageList pList = this.goodsService.list(gqo);
					objList = pList.getResult();
					this.metooGoodsViewTools.goods(objList, resultMap);
					resultMap.put("currentPage", pList.getCurrentPage());
					resultMap.put("Pages", pList.getPages());
				}
			}
		} else {
			IPageList pList = this.goodsService.list(gqo);
			objList = pList.getResult();
			this.metooGoodsViewTools.goods(objList, resultMap);
			resultMap.put("Pages", pList.getPages());
		}
		result = new Result(0, "success", resultMap);
		response.setCharacterEncoding("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
