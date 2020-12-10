package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MAddressService;
import com.metoo.app.foundation.service.MIAreaService;
import com.metoo.core.annotation.ApiVersion;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.AddressQueryObject;
import com.metoo.foundation.service.IAddressService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/app/")
public class MAddressBuyerAction {
	@Autowired
	private MAddressService maddressService;
	@Autowired
	private MIAreaService metooAreaService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IUserService userService;

	/**
	 * Address列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "收货地址列表", value = "/buyer/address.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "v1/addressList.json", method = RequestMethod.POST)
	public void address(HttpServletRequest request, HttpServletResponse response, String currentPage, String token) {
		String result = this.maddressService.getAddress(request, response, currentPage, token);
		this.send_json(result, response);
	}

	@SecurityMapping(title = "新增收货地址", value = "/buyer/address_add.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "v1/addressAdd", method = RequestMethod.POST)
	public void addressAdd(HttpServletRequest request, HttpServletResponse response, String currentPage) {
		String result = this.metooAreaService.areaParent(request, response, currentPage);
		this.send_json(result, response);
	}

	@SecurityMapping(title = "编辑收货地址", value = "/buyer/address_edit.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	// @RequestMapping("/buyer_address_edit.json")
	@RequestMapping(value = "v1/addressEdit.json", method = RequestMethod.POST)
	public void addressEdit(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String token) {
		String result = this.maddressService.addressEdit(request, response, id, currentPage, token);
		this.send_json(result, response);
	}

	/**
	 * 根据父id加载下级区域，返回json格式数据，这里只返回id和areaName，根据需要可以修改返回数据 过滤掉有关联的属性关联对象
	 * 
	 * @param request
	 * @param response
	 * @param pid
	 */
	@RequestMapping(value = "v1/loadArea.json", method = RequestMethod.GET)
	public void loadArea(HttpServletRequest request, HttpServletResponse response, String pid) {
		Map<String, Object> params = new HashMap<String, Object>();
		Result result = null;
		params.put("pid", CommUtil.null2Long(pid));
		List<Area> areas = this.metooAreaService.query("select obj from Area obj where obj.parent.id=:pid", params, -1,
				-1);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Area area : areas) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", area.getId());
			map.put("areaName", area.getAreaName());
			list.add(map);
		}
		result = new Result(0, "Successfully", list);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            地址
	 * @param area_id
	 *            区域
	 * @param currentPage
	 *            当前页
	 * @return
	 */
	@SecurityMapping(title = "收货地址保存", value = "/buyer/address_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	// @RequestMapping("/buyer_address_save.json")
	@RequestMapping(value = "v1/addressSave.json", method = RequestMethod.POST)
	public void address_save(HttpServletRequest request, HttpServletResponse response, String id, String area_id,
			String flag, String currentPage, String token) {
		String result = this.maddressService.addressSave(request, response, id, area_id, flag, currentPage, token);
		this.send_json(result, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param currentPage
	 */
	@SecurityMapping(title = "收货地址删除", value = "/buyer/address_del.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	// @RequestMapping("/buyer_address_del.json")
	@RequestMapping(value = "v1/addressDelete.json", method = RequestMethod.POST)
	public void address_del(HttpServletRequest request, HttpServletResponse response, String mulitId,
			String currentPage, String token) {
		String result = this.maddressService.addressDelete(request, response, mulitId, currentPage, token);
		this.send_json(result, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param currentPage
	 */
	@SecurityMapping(title = "收货地址默认设置", value = "/buyer/address_default.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	// @RequestMapping("/buyer_address_default.json")
	@RequestMapping(value = "v1/buyer_address_default.json", method = RequestMethod.POST)
	public void address_default(HttpServletRequest request, HttpServletResponse response, String mulitId,
			String currentPage, String token) {
		String result = this.maddressService.address_metoo_default(request, response, mulitId, currentPage, token);
		this.send_json(result, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param currentPage
	 */
	@SecurityMapping(title = "收货地址默认取消", value = "/buyer/address_default_cancle.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	// @RequestMapping("/address_default_cancle.json")
	@RequestMapping(value = "v1/address_default_cancle.json", method = RequestMethod.POST)
	public void address__default_cancle(HttpServletRequest request, HttpServletResponse response, String mulitId,
			String currentPage, String token) {
		String result = this.maddressService.address_default_cancle(request, response, mulitId, currentPage, token);
		this.send_json(result, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param mulitId
	 *            地址id
	 * @param type
	 *            操作类型
	 * @param currentPage
	 *            当前页数
	 * @param token
	 *            用戶身份令牌
	 * @description 设置默认收货地址
	 */
	@RequestMapping(value = "v1/address.json", method = RequestMethod.PUT)
	public void addressDefaultCancle(HttpServletRequest request, HttpServletResponse response, String mulitId,
			String type, String currentPage, String token) {
		String result = this.maddressService.addressDefault(request, response, mulitId, type, currentPage, token);
		this.send_json(result, response);
	}

	private void send_json(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
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
