package com.metoo.manage.seller.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.constant.Globals;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.GoodsSpecificationQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsSpecificationService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.StoreTools;

/**
 * 
 * <p>
 * Title: GoodsSpecSellerAction.java
 * </p>
 * 
 * <p>
 * Description: 商家商品规格管理控制器，商家可以自行管理规格属性，发不商品时商家选择自己添加的规格属性，规格属性只在商品详细页显示并可以选择，
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-5-7
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Controller
public class GoodsSpecSellerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IGoodsSpecificationService goodsSpecService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private StoreTools shopTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsClassService goodsClassService;

	/**
	 * GoodsSpecification列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "商品规格列表", value = "/seller/goods_spec_list.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_list.htm")
	public ModelAndView list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_spec_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Map params = new HashMap();
		params.put("spec_type", 0);
		List<GoodsSpecification> specs_self = this.goodsSpecService.query("select obj from GoodsSpecification obj where obj.spec_type=:spec_type order by obj.sequence asc",
				params, -1, -1);
		params.clear();
		params.put("spec_type", 1);
		List<GoodsSpecification> specs_store = this.goodsSpecService.query("select obj from GoodsSpecification obj where obj.spec_type=:spec_type order by obj.sequence asc",
		params, -1, -1);
		
		GoodsSpecification goodsSpec = null;
		boolean flag = true;
		List<String> store_ids = new ArrayList<String>();
		for(GoodsSpecification goods_spec_store : specs_store){
			String store_id = CommUtil.null2String(goods_spec_store.getStore().getId());
			store_ids.add(store_id);
		}
		try {
			if(store_ids.contains(CommUtil.null2String(user.getStore().getId()))){
				flag = false;
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			flag = true;
		}
		if(flag){
			if(!specs_self.isEmpty()){
				for(GoodsSpecification spec : specs_self){
					goodsSpec = new GoodsSpecification();
					goodsSpec.setSpec_type(1);
					goodsSpec.setStore(user.getStore());
					goodsSpec.setAddTime(new Date());
					goodsSpec.setDeleteStatus(spec.getDeleteStatus());
					goodsSpec.setName(spec.getName());
					goodsSpec.setSequence(spec.getSequence());
					goodsSpec.setType(spec.getType());
					//goodsSpec.setParent(spec);
					if (CommUtil.null2String(spec.getGoodsclass().getId()) != null && !CommUtil.null2String(spec.getGoodsclass().getId()).equals("")) {
						GoodsClass gc_main = this.goodsClassService.getObjById(CommUtil
								.null2Long(CommUtil.null2String(spec.getGoodsclass().getId())));
						spec.setGoodsclass(gc_main);
					}
					//Set<GoodsClass> gcc = spec.getGoodsclass().getChilds(); -- 错-改goodsclass 使用的是编辑里中gc的子类
					List<GoodsClass> gc_child = spec.getSpec_goodsClass_detail();
					List<GoodsClass> gc_list = new ArrayList<GoodsClass>();
					for(GoodsClass gcc:gc_child){
						if (CommUtil.null2String(gcc.getId()) != null && !CommUtil.null2String(gcc.getId()).equals("")) {
							String ids[] = CommUtil.null2String(gcc.getId()).split(",");
							
							for (String c_id : ids) {
								GoodsClass gc_detail = this.goodsClassService
										.getObjById(CommUtil.null2Long(c_id));
								if (gc_detail != null) {
									gc_list.add(gc_detail);
								}
							}
							if (gc_list.size() > 0) {
								goodsSpec.setSpec_goodsClass_detail(gc_list);
							}
						goodsSpec.setGoodsclass(spec.getGoodsclass());
					}
				}		
					List<GoodsSpecProperty> properties = spec.getProperties();
					GoodsSpecProperty goodsSpecpro = null;
					List<GoodsSpecProperty> goodsSpecProList = new ArrayList<GoodsSpecProperty>();
					if(!properties.isEmpty()){
						for(GoodsSpecProperty gpt:properties){
							goodsSpecpro = new GoodsSpecProperty();
							goodsSpecpro.setAddTime(new Date());
							goodsSpecpro.setDeleteStatus(gpt.getDeleteStatus());
							goodsSpecpro.setSequence(gpt.getSequence());
							goodsSpecpro.setValue(gpt.getValue());
							if(CommUtil.null2String(gpt.getId()) != null && !CommUtil.null2String(gpt.getId()).equals("")){
								GoodsSpecProperty goodsty = this.goodsSpecPropertyService.getObjById(gpt.getId());
								goodsSpecpro.setSpec(goodsty.getSpec());
							}
							goodsSpecProList.add(goodsSpecpro);
						}
						//如果将list放到外面，因为之前在for循环内创建对象，这样每次都保存最后一个对象，所已list放到循环内部
						goodsSpec.setProperties(goodsSpecProList);
					}
						this.goodsSpecService.save(goodsSpec);
				}
			}	
		
			params.clear();
			params.put("store", user.getStore().getId());
			params.put("spec_type", 1);
			List<GoodsSpecification> spcfc_store = this.goodsSpecService.query("select obj from GoodsSpecification obj where obj.store.id=:store and obj.spec_type=:spec_type order by obj.sequence asc",
			params, -1, -1);
			GoodsSpecProperty goodsSpecproperty = null;
			List<GoodsSpecProperty> goodsSpecPropertyList = new ArrayList<GoodsSpecProperty>();
			if(!spcfc_store.isEmpty()){
				for(GoodsSpecification goodsSpecification : spcfc_store){
					goodsSpecPropertyList = goodsSpecification.getProperties();
				
					for(GoodsSpecProperty pro:goodsSpecPropertyList){
						goodsSpecproperty = new GoodsSpecProperty();
						goodsSpecproperty.setAddTime(new Date());
						goodsSpecproperty.setDeleteStatus(pro.getDeleteStatus());
						goodsSpecproperty.setSequence(pro.getSequence());
						goodsSpecproperty.setValue(pro.getValue());
						goodsSpecproperty.setSpec(goodsSpecification);
						this.goodsSpecPropertyService.save(goodsSpecproperty);
					}
				}
			}
		}	
		GoodsSpecificationQueryObject qo = new GoodsSpecificationQueryObject(
				currentPage, mv, orderBy, orderType);
		WebForm wf = new WebForm();
		wf.toQueryPo(request, qo, GoodsSpecification.class, mv);
		qo.addQuery("obj.store.id", new SysMap("store_id", user.getStore()
				.getId()), "=");
		qo.setOrderBy("sequence");
		qo.setOrderType("asc");
		IPageList pList = this.goodsSpecService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("shopTools", shopTools);
		return mv;
	}

/*
	@SecurityMapping(title = "商品规格列表", value = "/seller/goods_spec_list.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_list.htm")
	public ModelAndView list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_spec_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		GoodsSpecificationQueryObject qo = new GoodsSpecificationQueryObject(
				currentPage, mv, orderBy, orderType);
		WebForm wf = new WebForm();
		wf.toQueryPo(request, qo, GoodsSpecification.class, mv);
		qo.addQuery("obj.store.id", new SysMap("store_id", user.getStore()
				.getId()), "=");
		qo.setOrderBy("sequence");
		qo.setOrderType("asc");
		IPageList pList = this.goodsSpecService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("shopTools", shopTools);
		return mv;
	}
*/

	public static void saveIPageList2ModelAndView_spec(String url, String staticURL,
			String params, IPageList pList, ModelAndView mv) {
		if (pList != null) {
			mv.addObject("objs", pList.getResult());
			mv.addObject("totalPage", new Integer(pList.getPages()));//总页数
			mv.addObject("pageSize", pList.getPageSize());// 返回每页大小
			mv.addObject("rows", new Integer(pList.getRowCount()));// 返回查询总记录数
			mv.addObject("currentPage", new Integer(pList.getCurrentPage()));//返回有效的当前页
			
			mv.addObject("gotoPageHTML", CommUtil.showPageHtml(url, params,
					pList.getCurrentPage(), pList.getPages()));
			mv.addObject(
					"gotoPageFormHTML",
					CommUtil.showPageFormHtml(pList.getCurrentPage(),
						pList.getPages()));
			mv.addObject(
					"gotoPageStaticHTML",
					CommUtil.showPageStaticHtml(staticURL,
							pList.getCurrentPage(), pList.getPages()));
			mv.addObject(
					"gotoPageAjaxHTML",
					CommUtil.showPageAjaxHtml(url, params,
							pList.getCurrentPage(), pList.getPages()));
			
		}
	}
	
	/**
	 * goodsSpecification添加管理
	 * 
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@SecurityMapping(title = "商品规格添加", value = "/seller/goods_spec_add.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_add.htm")
	public ModelAndView add(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		mv.addObject("store_id", user.getStore().getId());
		Set<GoodsClass> gcs = this.shopTools.query_store_DetailGc(user
				.getStore().getGc_detail_info());// 解析所有详细类目
		GoodsClass main_gc = this.goodsClassService.getObjById(user.getStore()
				.getGc_main_id());
		if (gcs.size() > 0) {// 如果商家注册时选择了详细类目
			mv.addObject("gcs", gcs);
		} else {// 如果商家注册时没有选择详细类目，将查询主营类目的所有下级类目
			mv.addObject("gcs", main_gc.getChilds());
		}
		return mv;
	}

	/**
	 * goodsSpecification编辑管理
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@SecurityMapping(title = "商品规格编辑", value = "/seller/goods_spec_edit.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_edit.htm")
	public ModelAndView edit(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (id != null && !id.equals("")) {
			GoodsSpecification goodsSpecification = this.goodsSpecService
					.getObjById(Long.parseLong(id));
			if (goodsSpecification != null) {
				if (goodsSpecification.getStore().getUser().getId()
						.equals(user.getId())) {
					mv.addObject("obj", goodsSpecification);
					mv.addObject("currentPage", currentPage);
					mv.addObject("store_id", user.getStore().getId());
					mv.addObject("edit", true);
					Store store = user.getStore();
					Set<GoodsClass> gcs = this.shopTools
							.query_store_DetailGc(user.getStore()
									.getGc_detail_info());// 解析所有详细类目
					GoodsClass main_gc = this.goodsClassService
							.getObjById(store.getGc_main_id());
					if (gcs.size() > 0) {// 如果商家注册时选择了详细类目
						mv.addObject("gcs", gcs);
					} else {// 如果商家注册时没有选择详细类目，将查询主营类目的所有下级类目
						mv.addObject("gcs", main_gc.getChilds());
					}
					mv.addObject("gc_details", goodsSpecification
							.getGoodsclass().getChilds());
				} else {
					mv = new JModelAndView("error.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 1, request,
							response);
					mv.addObject("url", CommUtil.getURL(request)
							+ "/seller/goods_spec_list.htm?currentPage="
							+ currentPage);
					mv.addObject("op_title", "您所访问的地址不存在");
				}
			} else {
				mv = new JModelAndView("error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("url", CommUtil.getURL(request)
						+ "/seller/goods_spec_list.htm?currentPage="
						+ currentPage);
				mv.addObject("op_title", "您所访问的地址不存在");
			}
		}
		return mv;
	}

	/**
	 * goodsSpecification保存管理
	 * 
	 * @param id
	 * @return
	 */
	@SecurityMapping(title = "商品规保存", value = "/seller/goods_spec_save.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_save.htm")
	public String save(HttpServletRequest request,
			HttpServletResponse response, String id, String count,
			String currentPage, String gc_id, String goodsClass_detail_ids) {
		WebForm wf = new WebForm();
		GoodsSpecification goodsSpecification = null;
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (id.equals("")) {
			goodsSpecification = wf.toPo(request, GoodsSpecification.class);
			goodsSpecification.setAddTime(new Date());
		} else {
			GoodsSpecification obj = this.goodsSpecService.getObjById(Long
					.parseLong(id));
			goodsSpecification = (GoodsSpecification) wf.toPo(request, obj);
		}
		goodsSpecification.setSpec_type(1);
		goodsSpecification.setStore(user.getStore());
		if (gc_id != null && !gc_id.equals("")) {
			GoodsClass gc_main = this.goodsClassService.getObjById(CommUtil
					.null2Long(gc_id));
			goodsSpecification.setGoodsclass(gc_main);
		}
		if (goodsClass_detail_ids != null && !goodsClass_detail_ids.equals("")) {
			String ids[] = goodsClass_detail_ids.split(",");
			List<GoodsClass> gc_list = new ArrayList<GoodsClass>();
			for (String c_id : ids) {
				GoodsClass gc_detail = this.goodsClassService
						.getObjById(CommUtil.null2Long(c_id));
				if (gc_detail != null) {
					gc_list.add(gc_detail);
				}
			}
			if (gc_list.size() > 0) {
				goodsSpecification.setSpec_goodsClass_detail(gc_list);
			}
		} else {
			goodsSpecification.getSpec_goodsClass_detail().removeAll(
					goodsSpecification.getSpec_goodsClass_detail());
		}
		if (id.equals("")) {
			this.goodsSpecService.save(goodsSpecification);
		} else
			this.goodsSpecService.update(goodsSpecification);
		this.genericProperty(request, goodsSpecification, count);
		request.getSession(false).setAttribute("url",
				CommUtil.getURL(request) + "/seller/goods_spec_list.htm");
		request.getSession(false).setAttribute("op_title", "规格添加成功");
		return "redirect:/seller/success.htm";
	}

	private void clearProperty(HttpServletRequest request,
			GoodsSpecification spec) {
		for (GoodsSpecProperty property : spec.getProperties()) {
			Accessory img = property.getSpecImage();
			CommUtil.del_acc(request, img);
			for (Goods goods : property.getGoods_list()) {
				goods.getGoods_specs().remove(property);
			}
			for (GoodsCart gc : property.getCart_list()) {
				gc.getGsps().remove(property);
			}
			this.goodsSpecPropertyService.delete(property.getId());
		}
	}

	private void genericProperty(HttpServletRequest request,
			GoodsSpecification spec, String count) {
		for (int i = 1; i <= CommUtil.null2Int(count); i++) {
			Integer sequence = CommUtil.null2Int(request
					.getParameter("sequence_" + i));
			String value = CommUtil.null2String(request.getParameter("value_"
					+ i));
			if (sequence != null && !sequence.equals("") && value != null
					&& !value.equals("")) {
				String id = CommUtil.null2String(request
						.getParameter("id_" + i));
				GoodsSpecProperty property = null;
				if (id != null && !id.equals("")) {
					property = this.goodsSpecPropertyService.getObjById(Long
							.parseLong(id));
				} else
					property = new GoodsSpecProperty();
				property.setAddTime(new Date());
				property.setSequence(sequence);
				property.setSpec(spec);
				property.setValue(value);
				String uploadFilePath = this.configService.getSysConfig()
						.getUploadFilePath();
				String saveFilePathName = request.getSession()
						.getServletContext().getRealPath("/")
						+ uploadFilePath + File.separator + "spec";
				Map map = new HashMap();
				try {
					String fileName = property.getSpecImage() == null ? ""
							: property.getSpecImage().getName();
					map = CommUtil.saveFileToServer(request, "specImage_" + i,
							saveFilePathName, fileName, null);
					if (fileName.equals("")) {
						if (map.get("fileName") != "") {
							Accessory specImage = new Accessory();
							specImage.setName(CommUtil.null2String(map
									.get("fileName")));
							specImage.setExt(CommUtil.null2String(map
									.get("mime")));
							specImage.setSize(BigDecimal.valueOf(CommUtil
									.null2Double(map.get("fileSize"))));
							specImage.setPath(uploadFilePath + "/spec");
							specImage.setWidth(CommUtil.null2Int(map
									.get("width")));
							specImage.setHeight(CommUtil.null2Int(map
									.get("height")));
							specImage.setAddTime(new Date());
							this.accessoryService.save(specImage);
							property.setSpecImage(specImage);
						}
					} else {
						if (map.get("fileName") != "") {
							Accessory specImage = property.getSpecImage();
							specImage.setName(CommUtil.null2String(map
									.get("fileName")));
							specImage.setExt(CommUtil.null2String(map
									.get("mime")));
							specImage.setSize(BigDecimal.valueOf(CommUtil
									.null2Double(map.get("fileSize"))));
							specImage.setPath(uploadFilePath + "/spec");
							specImage.setWidth(CommUtil.null2Int(map
									.get("width")));
							specImage.setHeight(CommUtil.null2Int(map
									.get("height")));
							specImage.setAddTime(new Date());
							this.accessoryService.update(specImage);
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (id.equals("")) {
					this.goodsSpecPropertyService.save(property);
				} else {
					this.goodsSpecPropertyService.update(property);
				}
			}
		}
	}

	@SecurityMapping(title = "商品规格删除", value = "/seller/goods_spec_del.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_del.htm")
	public String delete(HttpServletRequest request, String mulitId,
			String currentPage) {
		String[] ids = mulitId.split(",");
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		for (String id : ids) {
			if (!id.equals("")) {
				GoodsSpecification obj = this.goodsSpecService.getObjById(Long
						.parseLong(id));
				if (obj != null) {
					if (obj.getStore().getUser().getId().equals(user.getId())) {
						this.clearProperty(request, obj);
						this.goodsSpecService.delete(Long.parseLong(id));
					}
				}
			}
		}
		return "redirect:goods_spec_list.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "商品规格Ajax删除", value = "/seller/goods_property_delete.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_property_delete.htm")
	public void goods_property_delete(HttpServletRequest request,
			HttpServletResponse response, String id) {
		boolean ret = true;
		if (!id.equals("")) {
			this.databaseTools.execute("delete from "
					+ Globals.DEFAULT_TABLE_SUFFIX
					+ "goods_spec where spec_id=" + id);
			this.databaseTools.execute("delete from "
					+ Globals.DEFAULT_TABLE_SUFFIX + "cart_gsp where gsp_id="
					+ id);
			GoodsSpecProperty property = this.goodsSpecPropertyService
					.getObjById(Long.parseLong(id));
			property.setSpec(null);
			Accessory img = property.getSpecImage();
			CommUtil.del_acc(request, img);
			property.setSpecImage(null);
			ret = this.goodsSpecPropertyService.delete(property.getId());
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

	@SecurityMapping(title = "规格名称验证", value = "/seller/goods_spec_verify.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_verify.htm")
	public void goods_spec_verify(HttpServletRequest request,
			HttpServletResponse response, String name, String id,
			String store_id) {
		boolean ret = true;
		Map params = new HashMap();
		params.put("name", name);
		params.put("store_id", CommUtil.null2Long(store_id));
		params.put("id", CommUtil.null2Long(id));
		List<GoodsSpecification> gss = this.goodsSpecService
				.query("select obj from GoodsSpecification obj where obj.name=:name and obj.id!=:id and obj.store.id=:store_id",
						params, -1, -1);
		if (gss != null && gss.size() > 0) {
			ret = false;
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

	@SecurityMapping(title = "规格新增分类加载", value = "/seller/spec_gc_load.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/spec_gc_load.htm")
	public ModelAndView spec_gc_load(HttpServletRequest request,
			HttpServletResponse response, String gc_id, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/spec_gc_load.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (gc_id != null && !gc_id.equals("")) {
			GoodsClass gc = this.goodsClassService.getObjById(CommUtil
					.null2Long(gc_id));
			mv.addObject("gcs", gc.getChilds());
		}
		if (id != null && !id.equals("")) {
			GoodsSpecification obj = this.goodsSpecService.getObjById(CommUtil
					.null2Long(id));
			mv.addObject("obj", obj);
		}
		return mv;
	}
	
	@SecurityMapping(title = "规格新增", value = "seller/spec_add.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/seller/spec_add.htm")
	public ModelAndView spec_add(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("spec_id", id);
		return mv;
	}

	@SecurityMapping(title = "规格新增保存", value = "/seller/spec_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/seller/spec_ajax_save.htm")
	public void spec_save(HttpServletRequest request,
			HttpServletResponse response, String id, String value) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		int code = 0;
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (id != null && !id.equals("")) {
			GoodsSpecification goodsSpecification = this.goodsSpecService.getObjById(Long.parseLong(id));
			if(goodsSpecification != null){
				GoodsSpecProperty goodsSpecProperty = new GoodsSpecProperty();
				if(goodsSpecification.getStore().getUser().getId().equals(user.getId())){
					goodsSpecProperty.setAddTime(new Date());
					goodsSpecProperty.setValue(value);
					goodsSpecProperty.setSpec(goodsSpecification);
					this.goodsSpecPropertyService.save(goodsSpecProperty);
				}else{
					code = 200;
				}
			}
		}
		try {
			response.getWriter().print(value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * goodsSpecification编辑管理
	 * 根据id查询规格
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@SecurityMapping(title = "新增商品查询规格值", value = "/seller/goods_spec_edit.htm*", rtype = "seller", rname = "规格管理", rcode = "spec_seller", rgroup = "商品管理")
	@RequestMapping("/seller/goods_spec_set.htm")
	public void goods_spec(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		String jsonList = "";
		
		List<Map> list = new ArrayList<Map>();
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/goods_spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (id != null && !id.equals("")) {
			GoodsSpecification goodsSpecification = this.goodsSpecService
					.getObjById(Long.parseLong(id));
			if (goodsSpecification != null) {
				if (goodsSpecification.getStore().getUser().getId()
						.equals(user.getId())) {
					for(GoodsSpecProperty gsp : goodsSpecification.getProperties()){
						Map map = new HashMap();
						map.put("id",gsp.getId());
						map.put("name",gsp.getValue());
						list.add(map);
					}
				}
			}
		}
		jsonList = Json.toJson(list, JsonFormat.compact());
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(jsonList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}