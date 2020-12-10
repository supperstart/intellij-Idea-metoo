package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GoodsType;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecificationService;
import com.metoo.foundation.service.IGoodsTypeService;
import com.metoo.foundation.service.ISysConfigService;

@Controller
@RequestMapping("/php/admin/goodsClass")
public class PhpGoodsClassManageAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsTypeService goodsTypeService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsSpecificationService goodsSpecificationService;

	/**
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            类目ID
	 * @param pid
	 *            类目父ID
	 * @param type_link
	 *            商品类型是否关联到下级 true and false
	 * @param classTypeId
	 *            商品类型ID
	 * @param commission_link
	 *            佣金比例是否...
	 * @param commission_rate
	 *            佣金比例值
	 * @param guarantee_link
	 *            佣金是否...
	 * @param guarantee
	 *            佣金
	 * @param gbs_ids
	 *            品牌IDS
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String pid, String type_link,
			String classTypeId, String commission_link, String commission_rate, String guarantee_link, String guarantee,
			String gbs_ids, String class_icon) {
		Result reuslt = null;
		WebForm wf = new WebForm();
		GoodsClass goodsClass = null;
		if (id.equals("")) {
			goodsClass = wf.toPo(request, GoodsClass.class);
			goodsClass.setAddTime(new Date());
		} else {
			GoodsClass obj = this.goodsClassService.getObjById(CommUtil.null2Long(id));
			goodsClass = (GoodsClass) wf.toPo(request, obj);
		}
		GoodsClass parent = this.goodsClassService.getObjById(CommUtil.null2Long(pid));
		if (parent != null) {
			goodsClass.setParent(parent);
			goodsClass.setLevel(parent.getLevel() + 1);
		}

		Set<Long> ids = this.genericIds(goodsClass);
		// 将商品类型关联到下级商品分类
		GoodsType goodsType = this.goodsTypeService.getObjById(CommUtil.null2Long(classTypeId));
		if (goodsType != null) {
			goodsClass.setGoodsType(goodsType);
			if (CommUtil.null2Boolean(type_link)) {
				for (Long gcId : ids) {
					GoodsClass childGoodsClass = this.goodsClassService.getObjById(gcId);
					childGoodsClass.setGoodsType(goodsType);
					this.goodsClassService.update(childGoodsClass);
				}
			}
		}
		// 将佣金比例关联到下级商品分类
		if (CommUtil.null2Boolean(commission_link)) {
			for (Long gcId : ids) {
				GoodsClass childGoodsClass = this.goodsClassService.getObjById(gcId);
				if (childGoodsClass != null) {
					childGoodsClass.setCommission_rate(new BigDecimal(commission_rate));
					this.goodsClassService.update(childGoodsClass);
				}
			}
		}
		// 将保证金关联到下级商品分类
		if (CommUtil.null2Boolean(guarantee_link)) {
			for (Long gcId : ids) {
				GoodsClass childGoodsClass = this.goodsClassService.getObjById(gcId);
				if (childGoodsClass != null) {
					childGoodsClass.setGuarantee(new BigDecimal(guarantee));
					this.goodsClassService.update(childGoodsClass);
				}
			}
		}
		// 商品分类图标
		if (class_icon != null && !class_icon.equals("")) {
			Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(class_icon));
			if (accessory != null) {
				goodsClass.setIcon_acc(accessory);
			}
		}
		/*
		 * String uploadFilePath =
		 * this.configService.getSysConfig().getUploadFilePath(); String
		 * saveFilePathName = uploadFilePath + "/" + "class_icon"; Map map = new
		 * HashMap(); try { String fileName = goodsClass.getIcon_acc() == null ?
		 * "" : goodsClass.getIcon_acc().getName(); map =
		 * CommUtil.httpsaveFileToServer(request, "icon_acc", saveFilePathName,
		 * fileName, null); if (fileName.equals("")) { if (map.get("fileName")
		 * != "") { Accessory photo = new Accessory();
		 * photo.setName(CommUtil.null2String(map.get("fileName")));
		 * photo.setExt(CommUtil.null2String(map.get("mime")));
		 * photo.setSize(BigDecimal.valueOf(CommUtil.null2Double(map
		 * .get("fileSize")))); photo.setPath(uploadFilePath + "/class_icon");
		 * photo.setWidth(CommUtil.null2Int(map.get("width")));
		 * photo.setHeight(CommUtil.null2Int(map.get("height")));
		 * photo.setAddTime(new Date()); this.accessoryService.save(photo);
		 * goodsClass.setIcon_acc(photo); } } else { if (map.get("fileName") !=
		 * "") { Accessory photo = goodsClass.getIcon_acc();
		 * photo.setName(CommUtil.null2String(map.get("fileName")));
		 * photo.setExt(CommUtil.null2String(map.get("mime")));
		 * photo.setSize(BigDecimal.valueOf(CommUtil.null2Double(map
		 * .get("fileSize")))); photo.setPath(uploadFilePath + "/class_icon");
		 * photo.setWidth(CommUtil.null2Int(map.get("width")));
		 * photo.setHeight(CommUtil.null2Int(map.get("height")));
		 * photo.setAddTime(new Date()); this.accessoryService.update(photo); }
		 * }
		 * 
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		if (gbs_ids != null && !gbs_ids.equals("")) {
			String[] gbIds = gbs_ids.split(",");
			List list_temp = new ArrayList();
			for (String gb_id : gbs_ids.split(",")) {
				if (gb_id != null && !gb_id.equals("")) {
					GoodsBrand goodsBrand = this.goodsBrandService.getObjById(CommUtil.null2Long(gb_id));
					Map map_temp = new HashMap();
					map_temp.put("id", goodsBrand.getId());
					map_temp.put("name", goodsBrand.getName());
					map_temp.put("imgPath", goodsBrand.getBrandLogo().getPath());
					map_temp.put("imgName", goodsBrand.getBrandLogo().getName());
					list_temp.add(map_temp);
				}
			}
			goodsClass.setGb_info(Json.toJson(list_temp, JsonFormat.compact()));
		} else {
			goodsClass.setGb_info(null);
		}
		if (id.equals("")) {
			this.goodsClassService.save(goodsClass);
			reuslt = new Result(5200, "Save Successfully");
		} else {
			this.goodsClassService.update(goodsClass);
			reuslt = new Result(5200, "Update Successfully");
		}
		this.returnJson(Json.toJson(reuslt, JsonFormat.compact()), response);
	}

	/**
	 * @description 类目删除
	 * @param request
	 * @param response
	 * @param mulitId
	 * @ RequestMapping (value = "goodsClass.json", method =
	 * RequestMethod.DELETE)
	 */
	@RequestMapping(value = "/delete.json", method = RequestMethod.DELETE)
	public void delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "mulitId", required = true) String mulitId) {
		Result result = null;
		if (mulitId != null && !mulitId.equals("")) {
			for (String id : mulitId.split(",")) {
				Set<Long> list = this.genericIds(this.goodsClassService.getObjById(CommUtil.null2Long(id)));
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("ids", list);
				List<GoodsClass> gcs = this.goodsClassService.query(
						"select obj from GoodsClass obj where obj.id in (:ids) order by obj.level desc", params, -1,
						-1);
				for (GoodsClass gc : gcs) {
					for (Goods goods : gc.getGoods_list()) {
						goods.setGc(null);
						this.goodsService.update(goods);
					}
					GoodsType goodsType = gc.getGoodsType();
					if (goodsType != null) {
						goodsType.getGcs().remove(gc);
						this.goodsTypeService.update(goodsType);
					}
					for (GoodsSpecification gsp : gc.getSpec_detail()) {
						gsp.getSpec_goodsClass_detail().remove(gc);
						gsp.setGoodsclass(null);
						this.goodsSpecificationService.update(gsp);
					}
					gc.setChilds(null);
					gc.getSpec_detail().clear();
					this.goodsClassService.update(gc);
					this.goodsClassService.delete(gc.getId());
				}
			}
			result = new Result(5200, "Delete Successfully");
		}
		this.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	private Set<Long> genericIds(GoodsClass gc) {
		Set<Long> ids = new HashSet<Long>();
		ids.add(gc.getId());
		for (GoodsClass child : gc.getChilds()) {
			Set<Long> cids = genericIds(child);
			for (Long id : cids) {
				ids.add(id);
			}
			ids.add(child.getId());
		}
		return ids;
	}

	/**
	 * 处理响应头
	 * 
	 * @param json
	 * @param response
	 */
	public void returnJson(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
