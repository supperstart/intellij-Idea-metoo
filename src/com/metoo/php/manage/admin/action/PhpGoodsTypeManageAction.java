package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsType;
import com.metoo.foundation.domain.GoodsTypeProperty;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.IGoodsTypeService;
import com.metoo.foundation.service.IUserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/php/admin/goodsType")
public class PhpGoodsTypeManageAction {
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsTypeService goodsTypeService;
	@Autowired
	private IGoodsBrandService brandService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private IGoodsClassService goodsClassService;

	@RequestMapping(value = "/save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String brand_ids,
			String property, String uid) {
		Result result = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		if (user != null) {
			user = user.getParent() == null ? user : user.getParent();
			WebForm wf = new WebForm();
			GoodsType goodsType = null;
			if (id.equals("")) {
				goodsType = wf.toPo(request, GoodsType.class);
				goodsType.setAddTime(new Date());
			} else {
				GoodsType obj = this.goodsTypeService.getObjById(CommUtil.null2Long(id));
				goodsType = (GoodsType) wf.toPo(request, obj);
			}

			if (brand_ids != null && !brand_ids.equals("")) {
				String[] ids = brand_ids.split(",");
				goodsType.getGbs().clear();
				for (String brandId : ids) {
					GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brandId));
					goodsType.getGbs().add(brand);
				}
			}
			if (id.equals("")) {
				this.goodsTypeService.save(goodsType);
			} else {
				this.goodsTypeService.update(goodsType);
			}
			this.genericProperty(property, goodsType);
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5422, "No such user");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	public void genericProperty(String property, GoodsType goodsType) {
		if (property != null && !property.equals("")) {
			JSONArray jsonArray = JSONArray.fromObject(property);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject json = (JSONObject) jsonArray.get(i);
				// String id = (String) json.get("id");
				String id = CommUtil.null2String(json.get("id"));
				int sequence = CommUtil.null2Int(json.get("sequence"));
				String name = (String) json.get("name");
				String value = (String) json.get("value");
				boolean display = CommUtil.null2Boolean(json.get("display"));
				String hc_value = (String) json.get("hc_value");
				if (!name.equals("") && !value.equals("")) {
					GoodsTypeProperty goodstypeProperty = null;
					if (id.equals("")) {
						goodstypeProperty = new GoodsTypeProperty();
						goodstypeProperty.setAddTime(new Date());
					} else {
						goodstypeProperty = this.goodsTypePropertyService.getObjById(CommUtil.null2Long(id));
					}
					goodstypeProperty.setName(name);
					goodstypeProperty.setSequence(sequence);
					goodstypeProperty.setValue(value);
					goodstypeProperty.setDisplay(display);
					goodstypeProperty.setHc_value(hc_value);
					goodstypeProperty.setGoodsType(goodsType);
					if (id.equals("")) {
						this.goodsTypePropertyService.save(goodstypeProperty);
					} else {
						this.goodsTypePropertyService.update(goodstypeProperty);
					}
				}
			}
		}
	}

	@RequestMapping(value = "/delete.json", method = RequestMethod.DELETE)
	public void goods_type_del(HttpServletRequest request, HttpServletResponse response, String mulitId) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				GoodsType goodsType = this.goodsTypeService.getObjById(Long.parseLong(id));
				goodsType.getGbs().clear();
				for (GoodsClass gc : goodsType.getGcs()) {
					gc.setGoodsType(null);
					this.goodsClassService.update(gc);
				}
				this.goodsTypeService.delete(Long.parseLong(id));
			}
		}
		Result result = new Result(5200, "Successfully");
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
}
