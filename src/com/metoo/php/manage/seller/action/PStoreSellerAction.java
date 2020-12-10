package com.metoo.php.manage.seller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.Transport;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITransportService;
import com.metoo.foundation.service.IUserService;

@Controller
public class PStoreSellerAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private ITransportService transportService;

	/**
	 *
	 * @param request
	 * @param response
	 * @param id
	 */
	@RequestMapping(value = "/php/store.json", method = RequestMethod.GET)
	public void query(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "id", required = true) String id) {
		Result result = null;
		if (id != null && !id.equals("")) {
			User user = this.userService.getObjById(CommUtil.null2Long(id));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			Map map = new HashMap();
			map.put("storeName", store.getStore_name());
			map.put("storeUserName", store.getUser().getUserName());
			map.put("trueName", store.getUser().getTrueName());
			map.put("storeOowerCard", store.getStore_ower_card());
			map.put("storeAddress", store.getStore_address());
			map.put("storeZip", store.getStore_zip());
			map.put("email", store.getUser().getEmail());
			map.put("companyName", store.getLicense_c_name());
			map.put("license_legal_name", store.getLicense_legal_name());
			map.put("license_legal_idCard", store.getLicense_legal_idCard());
			map.put("license_legal_idCard_image",
					store.getLicense_legal_idCard_image() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/"
									+ store.getLicense_legal_idCard_image().getPath() + "/"
									+ store.getLicense_legal_idCard_image().getName());

			map.put("storeLogo",
					store.getStore_logo() == null ? ""
							: configService.getSysConfig().getImageWebServer() + "/" + store.getStore_logo().getPath()
									+ "/" + store.getStore_logo().getName());
			map.put("license_legal_idCard_photo",
					store.getLicense_legal_idCard_photo() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/"
									+ store.getLicense_legal_idCard_photo().getPath() + "/"
									+ store.getLicense_legal_idCard_photo().getName());

			map.put("storeLogo",
					store.getStore_logo() == null ? ""
							: configService.getSysConfig().getImageWebServer() + "/" + store.getStore_logo().getPath()
									+ "/" + store.getStore_logo().getName());
			map.put("license_image",
					store.getLicense_image() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/"
									+ store.getLicense_image().getPath() + "/" + store.getLicense_image().getName());
			if (store.getLicense_area() != null) {
				map.put("license_area_name", store.getLicense_area().getAreaName());
				map.put("license_areaparent_name", store.getLicense_area().getParent().getAreaName());
				map.put("license_areaparentParent_name", store.getLicense_area().getParent().getParent().getAreaName());
			}

			map.put("organization_code", store.getOrganization_code());
			map.put("license_address", store.getLicense_address());
			map.put("license_establish_date", store.getLicense_establish_date());
			map.put("license_start_date", store.getLicense_start_date());
			map.put("license_end_date", store.getLicense_end_date());
			map.put("license_reg_capital", store.getLicense_reg_capital());
			if (store.getLicense_c_area() != null) {
				map.put("license_c_area", store.getLicense_c_area().getAreaName());
				map.put("license_cparent_area", store.getLicense_c_area().getParent().getAreaName());
				map.put("license_cparentParent_area", store.getLicense_c_area().getParent().getParent().getAreaName());
			}
			map.put("license_c_address", store.getLicense_c_address());
			map.put("license_reg_capital", store.getLicense_reg_capital());
			map.put("license_c_contact", store.getLicense_c_contact());
			map.put("license_c_mobile", store.getLicense_c_mobile());
			map.put("bank_account_name", store.getBank_account_name());
			map.put("bank_c_account", store.getBank_c_account());
			map.put("license_c_contact", store.getLicense_c_contact());
			map.put("license_c_contact", store.getLicense_c_contact());
			map.put("license_c_contact", store.getLicense_c_contact());

			map.put("gradeName", store.getGrade().getGradeName());
			map.put("gcName", this.goodsClassService.getObjById(store.getGc_main_id()) == null ? ""
					: this.goodsClassService.getObjById(store.getGc_main_id()).getClassName());
			map.put("telephone", store.getStore_telephone());
			List<Area> areas = this.areaService.query("select obj from Area obj where obj.parent.id is null", null, -1,
					-1);
			List<Map> areaList = new ArrayList<Map>();
			for (Area area : areas) {
				Map areaMap = new HashMap();
				areaMap.put("id", area.getId());
				areaMap.put("name", area.getAreaName());
				areaList.add(areaMap);
			}
			map.put("area", areaList);
			result = new Result(5200, "success", map);
		} else {
			result = new Result(-100, "User information error");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	@RequestMapping(value = "/php/seller/store.json", method = RequestMethod.POST)
	public void save(HttpServletRequest request, HttpServletResponse response, String uid, String areaId,
			String store_telephone, String transport_id) {
		Result result = new Result();
		if (uid != null && !uid.equals("")) {
			User user = this.userService.getObjById(CommUtil.null2Long(uid));
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			WebForm wf = new WebForm();
			wf.toPo(request, store);
			String uploadFilePath = this.configService.getSysConfig().getUploadFilePath();
			String saveFilePath = this.configService.getSysConfig().getUploadFilePath() + "/store_logo";
			Map map = new HashMap();
			String fileName = store.getStore_logo() == null ? "" : store.getStore_logo().getName();
			try {
				map = CommUtil.httpsaveFileToServer(request, "logo", saveFilePath, fileName, null);
				if (fileName.equals("")) {
					if (map.get("fileName") != "") {
						Accessory store_logo = new Accessory();
						store_logo.setName(CommUtil.null2String(map.get("fileName")));
						store_logo.setExt(CommUtil.null2String(map.get("mime")));
						store_logo.setSize(BigDecimal.valueOf(CommUtil.null2Double(map.get("fileSize"))));
						store_logo.setPath(uploadFilePath + "/store_logo");
						store_logo.setWidth(CommUtil.null2Int(map.get("width")));
						store_logo.setHeight(CommUtil.null2Int(map.get("height")));
						this.accessoryService.save(store_logo);
						store.setStore_logo(store_logo);
					}
				} else {
					Accessory store_logo = store.getStore_logo();
					store_logo.setName(CommUtil.null2String(map.get("fileName")));
					store_logo.setExt(CommUtil.null2String(map.get("mime")));
					store_logo.setSize(BigDecimal.valueOf(CommUtil.null2Double(map.get("fileSize"))));
					store_logo.setPath(uploadFilePath + "/store_logo");
					store_logo.setWidth(CommUtil.null2Int(map.get("width")));
					store_logo.setHeight(CommUtil.null2Int(map.get("height")));
					this.accessoryService.update(store_logo);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Area area = this.areaService.getObjById(CommUtil.null2Long(areaId));
			store.setArea(area);
			if (transport_id != null && !transport_id.equals("")) {
				Transport transport = this.transportService.getObjById(CommUtil.null2Long(transport_id));
				if (transport != null) {
					store.setTransport(transport);
				}
			}
			this.storeService.update(store);
			if (store_telephone != null && !store_telephone.equals("")) {
				user.setMobile(store_telephone);
				user.setTelephone(store_telephone);
				this.userService.update(user);
			}

			result = new Result(5200, "success");
		} else {
			result = new Result(-100, "User information error");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
}
