package com.metoo.app.foundation.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MAddressService;
import com.metoo.app.foundation.service.MIAreaService;
import com.metoo.app.view.web.tool.MobileTools;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Address;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.AddressQueryObject;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
/**
 * @description 因为spring事务是基于aop的代理机制，当方法中调用this本身的方法时候即使在this的方法标明事务注解，但是事务注解会失效
 * @author Administrator
 *
 */
@Service
@Transactional
public class MAddressServiceImpl implements MAddressService{

	@Resource(name = "AddressMetooDao")
	private IGenericDAO<Address>  addressMetooDao;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private MIAreaService areaMetooService;
	@Autowired
	private IUserService userService;
	@Autowired
	private MobileTools mobileTools;
	
	public boolean save(Address address) {
		try {
			this.addressMetooDao.save(address);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
	   }	
	}
	
	
	public boolean update(Address address) {
		// TODO Auto-generated method stub
		try {
			this.addressMetooDao.update(address);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
	     try {
			this.addressMetooDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	public Address getObjById(Long id) {
		// TODO Auto-generated method stub
		Address address = this.addressMetooDao.get(id);
		if(address != null){
			return address;
		}
		return null;
	}

	public List<Address> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.addressMetooDao.query(query, params, begin, max);
	}

	public IPageList list(IQueryObject properties) {
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(Address.class, construct,
				query, params, this.addressMetooDao);
		if (properties != null) {
			PageObject pageObj = properties.getPageObj();
			if (pageObj != null)
				pList.doList(
						pageObj.getCurrentPage() == null ? 0 : pageObj
								.getCurrentPage(),
						pageObj.getPageSize() == null ? 0 : pageObj
								.getPageSize());
		} else
			pList.doList(0, -1);
		return pList;
	}
	
	public String getAddress(HttpServletRequest request, HttpServletResponse response,
			String currentPage, String token) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/address.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if(token.equals("")){
			result = new Result(-100, "token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");	
			}else{
				String url = this.configService.getSysConfig().getAddress();
				if (url == null || url.equals("")) {
					url = CommUtil.getURL(request);
				}
				AddressQueryObject qo = new AddressQueryObject(currentPage, mv,
						"default_val desc,obj.addTime", "asc");
				qo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");//SecurityUserHolder.getCurrentUser().getId()
				IPageList pList = this.list(qo);
				List<Map> addressList = CommUtil.saveIPageList2ModelAndView2(pList);
				map.put("addresList", addressList);
				List<Area> areas = this.areaMetooService.query(
						"select obj from Area obj where obj.parent.id is null", null,
						-1, -1);
				List<Map<String, Object>> areaList = new ArrayList<Map<String, Object>>();
				for (Area area : areas) {
					Map<String, Object> areaMap = new HashMap<String, Object>();
					areaMap.put("id", area.getId());
					areaMap.put("areaName", area.getAreaName());
					areaList.add(areaMap);
					map.put("areaMap", areaList);
				}
				result = new Result(0,"Successfully", map);
			}
		}
		return Json.toJson(result, JsonFormat.compact());
	}

	public String addressEdit(HttpServletRequest request, HttpServletResponse response,
			String id, String currentPage, String token) {
		Result result = null;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				List<Area> areas = this.areaMetooService.query(
						"select obj from Area obj where obj.parent.id is null", null,
						-1, -1);
				Map<String, Object> map = new HashMap<String, Object>();
				List<Map<String, Object>> areaList = new ArrayList<Map<String, Object>>();
				for(Area obj:areas){
					Map<String, Object> areaMap = new HashMap<String, Object>();
					areaMap.put("areaId", obj.getId());
					areaMap.put("areaName", obj.getAreaName());
					areaList.add(areaMap);
				}
				//国家：country 城市：city 地区： area
				map.put("areaMap", areaList);
				map.put("currentPage", currentPage);
				Address obj = this.getObjById(CommUtil.null2Long(id));
				Area area = obj.getArea();
				if(area.getLevel() == 2){
					map.put("country", obj.getArea().getParent().getParent().getAreaName());
					map.put("city", obj.getArea().getParent().getAreaName());
					map.put("area", obj.getArea().getAreaName());
				}else if(area.getLevel() == 1){
					map.put("country", obj.getArea().getParent().getAreaName());
					map.put("city", obj.getArea().getAreaName());
					map.put("area", "");
				}
				map.put("defaultVal", obj.getDefault_val());
				map.put("userName", obj.getTrueName());
				
				
				map.put("mobile", obj.getMobile());
				map.put("email", obj.getEmail());
				map.put("telephone", obj.getTelephone());
				map.put("AreaInfo", obj.getArea_info());
				map.put("AreaZip", obj.getZip());
				//比较当前用户id与地址对应得id是否相同
				if (obj.getUser().getId()
						.equals(user.getId())) {// 只允许修改自己的地址信息
				}
				 result = new Result(0, "Successfully", map);
			}
		}
		return JSON.toJSONString(result);
	}

	/**
	 * address保存管理
	 * 
	 * @param id
	 * @return
	 */
	
	public String addressSave(HttpServletRequest request, HttpServletResponse response,
			String id, String area_id, String flag, String currentPage, String token) {
		boolean saveboolean = false;
		Result result = new Result();
		WebForm wf = new WebForm();//封装添加表单对象
		Address address = null;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				Map<String, Object> params = new HashMap<String, Object>();
				String telephone = request.getParameter("telephone");
				if(this.mobileTools.verify(telephone)){
					if (id.equals("")) {
						address = wf.toPo(request, Address.class);
						address.setAddTime(new Date());
					} else {
						Address obj = this.getObjById(Long.parseLong(id));
						if (obj.getUser().getId()
								.equals(user.getId())) {
							address = (Address) wf.toPo(request, obj);
						}
					}
					address.setDefault_val(0);
					if(flag.equals("1")){
						params.put("user_id", user.getId());
						params.put("id", CommUtil.null2Long(id));
						params.put("default_val", 1);//[是否为默认收货地址，1为默认地址]
						List<Address> addresList = this
								.query("select obj from Address obj where obj.user.id=:user_id and obj.id!=:id and obj.default_val=:default_val",
										params, -1, -1);
						Map<String, String> currentPageMap = new HashMap<String, String>();
						currentPageMap.put("currentPage", currentPage);
						for (Address obj : addresList) {
							obj.setDefault_val(0);
							this.update(obj);
						}
						 address.setDefault_val(1);
					}
					params.clear();
					params.put("user_id", user.getId());
					params.put("id", CommUtil.null2Long(id));
					params.put("default_val", 1);//[是否为默认收货地址，1为默认地址]
					List<Address> defaultAddress = this
							.query("select obj from Address obj where obj.user.id=:user_id and obj.id!=:id and obj.default_val=:default_val",
									params, -1, -1);
					if(defaultAddress.size() == 0){
						address.setDefault_val(1);
					}
					Map<String, String> telephoneMap = this.mobileTools.mobile(telephone);
					address.setTelephone(telephoneMap.get("phoneNumber").toString());
					address.setMobile(telephoneMap.get("phoneNumber").toString());
					address.setUser(user);
					Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
					address.setArea(area);
					if (id.equals("")) {
						saveboolean= this.save(address);
						if(saveboolean) {
								List<Address> addresses =user.getAddrs();
								if(addresses.size() == 1){
									this.address_metoo_default(request, response, CommUtil.null2String(addresses.get(0).getId()),currentPage,token);
								}
							result = new Result(0, "Successfully");
						}else{
							result = new Result(1, "Error");
						}
					} else{
						if(this.update(address)) {
							result = new Result(0, "Successfully");
						}else{
							result = new Result(1, "Error");
						}
					}
				}else{
					result = new Result(4400 ,"手机号码格式不对");
				}
			}
		}
			return Json.toJson(result, JsonFormat.compact());
	}
	
	/**
	 * 删除用户地址(仅用户自己)
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param currentPage
	 * @return
	 */
	public String addressDelete(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage, String token) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		boolean del = false;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map<String, Object> params = new HashMap<String, Object>();
			boolean flag = false;
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				String[] ids = mulitId.split(",");
				for (String id : ids) {
					if (!id.equals("")) {
						Address address = this.getObjById(Long
								.parseLong(id));
						if (address.getUser().getId()
								.equals(user.getId())) {// 只允许删除自己的地址信息
							 del= this.delete(Long.parseLong(id));
							 if(address.getDefault_val() == 1){
								 flag = true;
							 }
						}
					}
				}
				if(del == true){
					if(flag){
						params.clear();
						params.put("uid", user.getId());
						List<Address> addres = this.query("select obj from Address obj where obj.user.id=:uid", params, -1, -1);
						if(user.getAddrs().size() > 0){
							Address obj = addres.get(0);
							obj.setDefault_val(1);
							this.update(obj);
						}
					}
					 result = new Result(0, "Successfully", map);
				}else{ 
					result = new Result(1, "Error");
				}
			}
			map.put("currentPage", currentPage);
		}
		return Json.toJson(result, JsonFormat.compact());
	}
	
	/**
	 * 设置地址默认设置
	 * @param request
	 * @param response
	 * @param mulitId
	 * @param currentPage
	 * @return
	 */
	public String address_metoo_default(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage, String token) {
		Result result = null;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map<String, Object> params = new HashMap<String, Object>();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				String[] ids = mulitId.split(",");
				for (String id : ids) {
					if (!id.equals("")) {
						Address address = this.getObjById(Long
								.parseLong(id));
						if (address.getUser().getId()
								.equals(user.getId())) {// 只允许修改自己的地址信息
							params.clear();
							params.put("user_id", user.getId());
							params.put("id", CommUtil.null2Long(id));
							params.put("default_val", 1);//[是否为默认收货地址，1为默认地址]
							List<Address> addresList = this
									.query("select obj from Address obj where obj.user.id=:user_id and obj.id!=:id and obj.default_val=:default_val",
											params, -1, -1);
							Map<String, String> map = new HashMap<String, String>();
							map.put("currentPage", currentPage);
							for (Address obj : addresList) {
								obj.setDefault_val(0);
								this.update(obj);
							}
						    address.setDefault_val(1);
							if(this.update(address)){
								result = new Result(0, "Successfully", map);
							}else{
								result = new Result(1, "Error");
							}
						}
					}
				}
			}
		}
		return Json.toJson(result, JsonFormat.compact());
	}
	
	public String address_default_cancle(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage,String token) {
		Result result = new Result();
		boolean flag = false;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				String[] ids = mulitId.split(",");
				for (String id : ids) {
					if (!id.equals("")) {
						Address address = this.getObjById(Long.parseLong(id));
						if (address.getUser().getId()
								.equals(user.getId())) {// 只允许修改自己的地址信息
							address.setDefault_val(0);
							flag = this.update(address);
						}
					}
				}
				Map<String, String> map = new HashMap<String, String>();
				map.put("currentPage", currentPage);
				if(flag){
					result = new Result(0, "Successfully", map);
				}else{
					result = new Result(1, "Error");
				}
			}
		}
		return Json.toJson(result, JsonFormat.compact());
	}


	@Override
	public String addressDefault(HttpServletRequest request, HttpServletResponse response, String mulitId, String type,
			String currentPage, String token) {
		Result result = null;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map<String, Object> params = new HashMap<String, Object>();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				String[] ids = mulitId.split(",");
				if(type.equals("add")){
					for (String id : ids) {
						if (!id.equals("")) {
							Address address = this.getObjById(Long
									.parseLong(id));
							if (address.getUser().getId()
									.equals(user.getId())) {// 只允许修改自己的地址信息
								params.clear();
								params.put("user_id", user.getId());
								params.put("id", CommUtil.null2Long(id));
								params.put("default_val", 1);//[是否为默认收货地址，1为默认地址]
								List<Address> addresList = this
										.query("select obj from Address obj where obj.user.id=:user_id and obj.id!=:id and obj.default_val=:default_val",
												params, -1, -1);
								Map<String, String> map = new HashMap<String, String>();
								map.put("currentPage", currentPage);
								for (Address obj : addresList) {
									obj.setDefault_val(0);
									this.update(obj);
								}
							    address.setDefault_val(1);
							    this.update(address);
							}
						}
					}
				}else{
					for (String id : ids) {
						if (!id.equals("")) {
							Address address = this.getObjById(Long.parseLong(id));
							if (address.getUser().getId()
									.equals(user.getId())) {// 只允许修改自己的地址信息
								address.setDefault_val(0);
								this.update(address);
							}
						}
					}
				}
			}
		}
		return Json.toJson(result, JsonFormat.compact());
	}
}
