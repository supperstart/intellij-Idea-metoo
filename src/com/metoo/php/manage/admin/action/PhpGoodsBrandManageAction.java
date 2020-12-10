package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.beans.BeanUtils;
import com.metoo.core.beans.BeanWrapper;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsBrandCategory;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsType;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsBrandCategoryService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/php/admin/goodsBrand")
public class PhpGoodsBrandManageAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsBrandCategoryService goodsBrandCategoryService;

	/**
	 * @description Brands add
	 * @param request
	 * @param response
	 * @param id
	 * @param gc_id
	 * @param uid
	 */
	@RequestMapping("/save.json")
	public void brand(HttpServletRequest request, HttpServletResponse response, String id, String gc_id, String uid,
			String brandLogo, String brandCredential) {
		Result result = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		if (user != null) {
			user = user.getParent() == null ? user : user.getParent();
			WebForm wf = new WebForm();
			GoodsBrand goodsBrand = null;
			user = user.getParent() == null ? user : user.getParent();
			if (id.equals("") || id == null) {
				goodsBrand = wf.toPo(request, GoodsBrand.class);
				goodsBrand.setAddTime(new Date());
				goodsBrand.setAudit(1);
				goodsBrand.setUserStatus(0);
			} else {
				GoodsBrand obj = this.goodsBrandService.getObjById(Long.parseLong(id));
				goodsBrand = (GoodsBrand) wf.toPo(request, obj);
			}
			// 品牌标识图片
			Accessory logo = this.accessoryService.getObjById(CommUtil.null2Long(brandLogo));
			goodsBrand.setBrandLogo(logo);
			// 品牌证书上传
			Accessory credential = this.accessoryService.getObjById(CommUtil.null2Long(brandCredential));
			goodsBrand.setBrandLogo(credential);
			GoodsClass gc = null;
			if (gc_id != null && !gc_id.equals("")) {
				gc = this.goodsClassService.getObjById(Long.valueOf(gc_id));
			} else {
				gc = this.goodsClassService.getObjById(user.getStore().getGc_main_id());
			}
			goodsBrand.setGc(gc);
			if (id.equals("")) {
				this.goodsBrandService.save(goodsBrand);
			} else
				this.goodsBrandService.update(goodsBrand);
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5422, "No such user");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/saudi.json", method = RequestMethod.PUT)
	public void saudi(HttpServletRequest request, HttpServletResponse response, String id, String name, String saudi) {
		Result result = null;
		User user = this.userService.getObjByProperty(null, "userName", name);
		if (user != null) {
			GoodsBrand brand = this.goodsBrandService.getObjById(CommUtil.null2Long(id));
			switch (saudi) {
			case "pass":
				brand.setAudit(1);
				break;
			case "refuse":
				brand.setAudit(-1);
				break;
			default:
				brand.setAudit(0);
			}
			this.goodsBrandService.update(brand);
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5422, "No such user");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/del.json")
	@ResponseBody
	public void del(HttpServletRequest Request, HttpServletResponse response, String mulitId) {
		Result result = null;
		if (mulitId != null && !mulitId.equals("")) {
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				if (!id.equals("")) {
					GoodsBrand goodsBrand = this.goodsBrandService.getObjById(CommUtil.null2Long(id));
					List<Goods> goodsList = goodsBrand.getGoods_list();
					for (Goods goods : goodsList) {
						goods.setGoods_brand(null);
						this.goodsService.update(goods);
					}
					List<GoodsType> goodsTypeList = goodsBrand.getTypes();
					for (GoodsType type : goodsTypeList) {
						type.getGbs().remove(goodsBrand);
					}
					if (goodsBrand.getCategory() != null) {
						GoodsBrandCategory category = goodsBrand.getCategory();
						if (category.getBrands().size() == 1) {
							goodsBrand.setCategory(null);
							this.goodsBrandService.update(goodsBrand);
							this.goodsBrandCategoryService.delete(category.getId());
						}
					}
					this.goodsBrandService.delete(Long.parseLong(id));
				}
			}
			result = new Result(5200, "SuccessFully");
		} else {
			result = new Result(5405, "Resources is empty");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/ajax.json")
	public void ajax(HttpServletRequest request, HttpServletResponse response, String id, String fieldName,
			String value) throws ClassNotFoundException {
		Result result = null;
		GoodsBrand obj = this.goodsBrandService.getObjById(CommUtil.null2Long(id));
		if (obj != null) {
			Field[] fields = GoodsBrand.class.getDeclaredFields();
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
						val = !CommUtil.null2Boolean(wrapper.getPropertyValue(fieldName));
					}
					wrapper.setPropertyValue(fieldName, val);
				}
			}
			this.goodsBrandService.update(obj);
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5405, "Resources is empty");
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
