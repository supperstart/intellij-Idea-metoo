package com.metoo.manage.admin.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.beans.BeanUtils;
import com.metoo.core.beans.BeanWrapper;
import com.metoo.core.constant.Globals;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.query.GoodsSpecificationQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsSpecificationService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.manage.admin.tools.StoreTools;

/**
 * 
 * <p>
 * Title: GoodsSpecificationSelfManageAction.java
 * </p>
 * 
 * <p>
 * Description: 自营商品规格管理控制器，平台自营及商家可以自行管理规格属性，规格属性只在商品详细页显示并可以选择，
 * 平台搜索列表显示的规格属性为平台商品类型中的新增属性
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
 * @author hezeng
 * 
 * @date 2014年4月25日
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class SelfGoodsSpecManageAction {
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
	private IGoodsClassService goodsclassService;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private StoreTools shopTools;
	@Autowired
	private IStoreService storeService;

	/**
	 * GoodsSpecification列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "商品规格列表", value = "/admin/goods_spec_list.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_list.htm")
	public ModelAndView list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType) {
		ModelAndView mv = new JModelAndView("admin/blue/goods_spec_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		GoodsSpecificationQueryObject qo = new GoodsSpecificationQueryObject(
				currentPage, mv, orderBy, orderType);
		WebForm wf = new WebForm();
		wf.toQueryPo(request, qo, GoodsSpecification.class, mv);
		qo.addQuery("obj.spec_type", new SysMap("spec_type", 0), "=");
		qo.setOrderBy("sequence");
		qo.setOrderType("asc");
		IPageList pList = this.goodsSpecService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("shopTools", shopTools);
		return mv;
	}

	/**
	 * goodsSpecification添加管理
	 * 
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@SecurityMapping(title = "商品规格添加", value = "/admin/goods_spec_add.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_add.htm")
	public ModelAndView add(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("admin/blue/goods_spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<GoodsClass> pgcs = this.goodsclassService.query(
				"select obj from GoodsClass obj where obj.parent.id is null ",
				null, -1, -1);
		mv.addObject("pgcs", pgcs);
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
	@SecurityMapping(title = "商品规格编辑", value = "/admin/goods_spec_edit.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_edit.htm")
	public ModelAndView edit(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/goods_spec_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (id != null && !id.equals("")) {
			GoodsSpecification goodsSpecification = this.goodsSpecService
					.getObjById(Long.parseLong(id));
			mv.addObject("obj", goodsSpecification);
			mv.addObject("currentPage", currentPage);
			List<GoodsClass> pgcs = this.goodsclassService
					.query("select obj from GoodsClass obj where obj.parent.id is null ",
							null, -1, -1);
			GoodsClass main_gc = goodsSpecification.getGoodsclass();
			if (main_gc != null
					&& goodsSpecification.getSpec_goodsClass_detail().size() > 0) {
				mv.addObject("gc_childs", main_gc.getChilds());
			}
			mv.addObject("gcs", main_gc.getParent().getChilds());
			mv.addObject("pgcs", pgcs);
			mv.addObject("edit", true);
		}
		return mv;
	}

	/**
	 * goodsSpecification保存管理
	 * 
	 * @param id
	 * @return
	 */
	@SecurityMapping(title = "商品规格保存", value = "/admin/goods_spec_save.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_save.htm")
	public ModelAndView save(HttpServletRequest request,
			HttpServletResponse response, String id, String cmd, String count,
			String add_url, String list_url, String currentPage, String gc_ids,
			String gc_id) {
		WebForm wf = new WebForm();
		GoodsSpecification goodsSpecification = null;
		if (id.equals("")) {
			goodsSpecification = wf.toPo(request, GoodsSpecification.class);
			goodsSpecification.setAddTime(new Date());
			goodsSpecification.setSpec_type(0);
		} else {
			GoodsSpecification obj = this.goodsSpecService.getObjById(Long
					.parseLong(id));
			goodsSpecification = (GoodsSpecification) wf.toPo(request, obj);
		}
		if (gc_ids != null && !gc_ids.equals("")) {
			String ids[] = gc_ids.split(",");
			List<GoodsClass> gc_list = new ArrayList<GoodsClass>();
			for (String c_id : ids) {
				GoodsClass gc_detail = this.goodsclassService
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
		if (gc_id != null && !gc_id.equals("")) {
			GoodsClass gc_main = this.goodsclassService.getObjById(CommUtil
					.null2Long(gc_id));
			goodsSpecification.setGoodsclass(gc_main);
		}
		if (id.equals("")) {
			this.goodsSpecService.save(goodsSpecification);
		} else
			this.goodsSpecService.update(goodsSpecification);
		this.genericProperty(request, goodsSpecification, count);
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", list_url + "?currentPage=" + currentPage);
		mv.addObject("op_title", "保存商品规格成功");
		if (add_url != null) {
			mv.addObject("add_url", add_url);
		}
		return mv;
	}
/*	@SecurityMapping(title = "商品规格保存", value = "/admin/goods_spec_save.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_save.htm")
	public ModelAndView save(HttpServletRequest request,
			HttpServletResponse response, String id, String cmd, String count,
			String add_url, String list_url, String currentPage, String gc_ids,
			String gc_id) {
		Map params = new HashMap(); 
		params.put("store_status", 15);
		List<Store> stores = this.storeService.query("select new Store(id) from Store obj where obj.store_status=:store_status", params, -1, -1);
		WebForm wf = new WebForm();
		GoodsSpecification goodsSpecification = null;
		if (id.equals("")) {
			goodsSpecification = wf.toPo(request, GoodsSpecification.class);
			goodsSpecification.setAddTime(new Date());
			goodsSpecification.setSpec_type(0);
		} else {
			GoodsSpecification obj = this.goodsSpecService.getObjById(Long
					.parseLong(id));
			
			goodsSpecification = (GoodsSpecification) wf.toPo(request, obj);
		}
		if (gc_ids != null && !gc_ids.equals("")) {
			String ids[] = gc_ids.split(",");
			List<GoodsClass> gc_list = new ArrayList<GoodsClass>();
			for (String c_id : ids) {
				GoodsClass gc_detail = this.goodsclassService
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
		if (gc_id != null && !gc_id.equals("")) {
			GoodsClass gc_main = this.goodsclassService.getObjById(CommUtil
					.null2Long(gc_id));
			goodsSpecification.setGoodsclass(gc_main);
		}
		if (id.equals("")) {
			this.goodsSpecService.save(goodsSpecification);
			GoodsSpecification obj = null;
			for(Store store : stores){
				obj = wf.toPo(request, GoodsSpecification.class);
				obj.setAddTime(new Date());
				obj.setSpec_type(1);
				obj.setParent(goodsSpecification);
				obj.setStore(store);
				obj.setGoodsclass(goodsSpecification.getGoodsclass());
				if (gc_ids != null && !gc_ids.equals("")) {
					String ids[] = gc_ids.split(",");
					List<GoodsClass> gc_list = new ArrayList<GoodsClass>();
					for (String c_id : ids) {
						GoodsClass gc_detail = this.goodsclassService
								.getObjById(CommUtil.null2Long(c_id));
						if (gc_detail != null) {
							gc_list.add(gc_detail);
						}
					}
					if (gc_list.size() > 0) {
						obj.setSpec_goodsClass_detail(gc_list);
					}
				} else {
					obj.getSpec_goodsClass_detail().removeAll(
							obj.getSpec_goodsClass_detail());
				}
				this.goodsSpecService.save(obj);
				this.genericProperty(request, obj, count);
			}
		} else
			this.goodsSpecService.update(goodsSpecification);
			this.genericProperty(request, goodsSpecification, count);
			Set<GoodsSpecification> goods_spec_set = goodsSpecification.getChilds();
			GoodsSpecification gsf = null;
			for(GoodsSpecification spec : goods_spec_set){
				gsf = spec;
				gsf.setGoodsclass(goodsSpecification.getGoodsclass());
				gsf.setName(goodsSpecification.getName());
				gsf.setSequence(goodsSpecification.getSequence());
				gsf.setSpec_type(1);
				System.out.println(spec.getId());
				GoodsSpecification gsf = null;
				GoodsSpecification obj = this.goodsSpecService.getObjById(spec.getId());
				gsf = (GoodsSpecification) wf.toPo(request, obj);
				System.out.println(gsf.getId());
				//BeanUtils.copyProperties(gsf, goods_gsf);
				if (gc_ids != null && !gc_ids.equals("")) {
					String ids[] = gc_ids.split(",");
					List<GoodsClass> gc_list = new ArrayList<GoodsClass>();
					for (String c_id : ids) {
						GoodsClass gc_detail = this.goodsclassService
								.getObjById(CommUtil.null2Long(c_id));
						if (gc_detail != null) {
							gc_list.add(gc_detail);
						}
					}
					if (gc_list.size() > 0) {
						gsf.setSpec_goodsClass_detail(gc_list);
					}
				} else {
					gsf.getSpec_goodsClass_detail().removeAll(
							gsf.getSpec_goodsClass_detail());
				}
				this.goodsSpecService.update(gsf);
				//this.genericProperty(request, gsf, count);
			}
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", list_url + "?currentPage=" + currentPage);
		mv.addObject("op_title", "保存商品规格成功");
		if (add_url != null) {
			mv.addObject("add_url", add_url);
		}
		return mv;
	}*/

	private void clearProperty(HttpServletRequest request,
			GoodsSpecification spec) {
		for (GoodsSpecProperty property : spec.getProperties()) {
			this.databaseTools.execute("delete from "
					+ Globals.DEFAULT_TABLE_SUFFIX
					+ "goods_spec where spec_id=" 
					+ property.getId());
			this.databaseTools.execute("delete from "
					+ Globals.DEFAULT_TABLE_SUFFIX + "cart_gsp where gsp_id="
					+ property.getId());
			this.databaseTools.execute("delete from " 
					+ Globals.DEFAULT_TABLE_SUFFIX + "c_goods_spec where c_spec_id="
					+ property.getId());
			property.setSpec(null);
			Accessory img = property.getSpecImage();
			CommUtil.del_acc(request, img);
			property.setSpecImage(null);
			property.getCgoods().removeAll(property.getCgoods());
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

	@SecurityMapping(title = "商品规格删除", value = "/admin/goods_spec_del.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_del.htm")
	public String delete(HttpServletRequest request, String mulitId,
			String currentPage) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				GoodsSpecification obj = this.goodsSpecService.getObjById(Long
						.parseLong(id));
				Set<GoodsSpecification> se = obj.getChilds();
				for(GoodsSpecification gsf : se){
					this.goodsSpecService.update(gsf);
					this.clearProperty(request, gsf);
					gsf.getSpec_goodsClass_detail().removeAll(
							gsf.getSpec_goodsClass_detail());
					this.goodsSpecService.delete(gsf.getId());
				}
				this.clearProperty(request, obj);
				obj.getSpec_goodsClass_detail().removeAll(
						obj.getSpec_goodsClass_detail());
				this.goodsSpecService.delete(Long.parseLong(id));
			}
		}
		return "redirect:goods_spec_list.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "商品规格属性AJAX删除", value = "/admin/goods_property_delete.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_property_delete.htm")
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

	@SecurityMapping(title = "商品规格AJAX更新", value = "/admin/goods_spec_ajax.htm*", rtype = "admin", rname = "规格管理", rcode = "goods_spec", rgroup = "自营")
	@RequestMapping("/admin/goods_spec_ajax.htm")
	public void ajax(HttpServletRequest request, HttpServletResponse response,
			String id, String fieldName, String value)
			throws ClassNotFoundException {
		GoodsSpecification obj = this.goodsSpecService.getObjById(Long
				.parseLong(id));
		Field[] fields = GoodsSpecification.class.getDeclaredFields();
		BeanWrapper wrapper = new BeanWrapper(obj);
		Object val = null;
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				Class clz = Class.forName("java.lang.String");
				if (field.getType().getName().equals("int")) {
					clz = Class.forName("java.lang.Integer");
				}
				if (field.getType().getName().equals("boolean")) {
					clz = Class.forName("java.lang.Boolean");
				}
				if (!value.equals("")) {
					val = BeanUtils.convertType(value, clz);
				} else {
					val = !CommUtil.null2Boolean(wrapper
							.getPropertyValue(fieldName));
				}
				wrapper.setPropertyValue(fieldName, val);
			}
		}
		this.goodsSpecService.update(obj);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(val.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/admin/goods_spec_verify.htm")
	public void goods_spec_verify(HttpServletRequest request,
			HttpServletResponse response, String name, String id) {
		boolean ret = true;
		Map params = new HashMap();
		params.put("name", name);
		params.put("spec_type", 0);
		params.put("id", CommUtil.null2Long(id));
		List<GoodsSpecification> gss = this.goodsSpecService
				.query("select obj from GoodsSpecification obj where obj.name=:name and obj.id!=:id and obj.spec_type=:spec_type",
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

	/**
	 * GoodsSpecification列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@RequestMapping("/admin/goods_spec_gc_load.htm")
	public ModelAndView spec_goodsclass_load(HttpServletRequest request,
			HttpServletResponse response, String gc_id, String mark, String id) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/goods_spec_gc_load.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (mark != null && !mark.equals("")) {
			if (mark.equals("pgc")) {
				mv = new JModelAndView("admin/blue/goods_spec_pgc_load.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request,
						response);
			}
		}
		GoodsClass gc = this.goodsclassService.getObjById(CommUtil
				.null2Long(gc_id));
		if (id != null && !id.equals("")) {
			GoodsSpecification gspec = this.goodsSpecService
					.getObjById(CommUtil.null2Long(id));
			mv.addObject("obj", gspec);
		}
		if (gc != null) {
			mv.addObject("gcs", gc.getChilds());
		}
		return mv;
	}
}