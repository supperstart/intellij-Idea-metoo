package com.metoo.php.manage.seller.action;

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

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsBrandCategoryService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.StoreTools;

@Controller
@RequestMapping("/php/seller/goodsBrand")
public class PGoodsBrandSellerAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private StoreTools storeTools;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsBrandCategoryService goodsBrandCategoryService;

	/**
	 * @author hk
	 * @methodsName add_goodsBrand
	 * @description 查询品牌关联分类
	 * @param request
	 * @param response
	 * @param uid
	 *            用户id
	 * @return json
	 */
	// @PostMapping("brand.json")
	// public void addGoodsBrand(HttpServletRequest request, HttpServletResponse
	// response, String uid){
	// Map map = new HashMap();
	// Result result = null;
	// if(uid != null && !uid.equals("")){
	// User user = this.userService.getObjById(CommUtil.null2Long(uid));
	// user = user.getParent() == null ? user : user.getParent();
	// List<GoodsClass> gcs = this.storeTools.query_store_detail_MainGc(user
	// .getStore().getGc_detail_info());
	// if(gcs.size() > 0){
	// List<Map> gc_maps = new ArrayList<Map>();
	// for(GoodsClass gc : gcs){
	// Map gc_map = new HashMap();
	// gc_map.put("id", gc.getId());
	// gc_map.put("name", gc.getClassName());
	// gc_maps.add(gc_map);
	// }
	// map.put("goodsClass", gc_maps);
	// }
	// result = new Result(0,"sueecss", map);
	// }else{
	// result = new Result(1,"未登录");
	// }
	// try {
	// response.getWriter().print(Json.toJson(result,JsonFormat.compact()));
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	@RequestMapping("save.json")
	public void goods_brand_save(HttpServletRequest request, HttpServletResponse response, String id, String gc_id,
			String uid, String brandLogo, String brandCredential) {
		Result result = null;
		WebForm wf = new WebForm();
		GoodsBrand goodsBrand = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		user = user.getParent() == null ? user : user.getParent();
		if (id == null || id.equals("")) {
			goodsBrand = wf.toPo(request, GoodsBrand.class);
			goodsBrand.setAddTime(new Date());
			goodsBrand.setAudit(0);
			goodsBrand.setUserStatus(1);
			goodsBrand.setStore_id(user.getStore().getId());
			goodsBrand.setUser(user);
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
		/*
		 * Map json = new HashMap(); json.put("ret", true); json.put("op_title",
		 * "品牌申请成功"); json.put("url", CommUtil.getURL(request) +
		 * "/seller/goods_brand_list.htm");
		 */
		result = new Result(0, "Successfully");
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
}
