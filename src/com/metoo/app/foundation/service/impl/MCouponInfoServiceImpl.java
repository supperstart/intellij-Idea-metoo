package com.metoo.app.foundation.service.impl;

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

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MICouponInfoService;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.CouponInfoQueryObject;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
@Service
@Transactional
public class MCouponInfoServiceImpl implements MICouponInfoService{
	@Resource(name = "CouponMetooDAO")
	private IGenericDAO<CouponInfo> couponMetooDao;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private ICouponInfoService couponInfoService;
	
	private static Result result = null;
	
	public IPageList list(IQueryObject properties) {
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(CouponInfo.class,
				construct, query, params, this.couponMetooDao);
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
	
	/**
	 * 优惠券列表
	 */
	public String coupon(HttpServletRequest request,
			HttpServletResponse response, String token, String currentPage) {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				Map<String, Object> map = new HashMap<String, Object>();
				ModelAndView mv = new JModelAndView(
						"",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request, response);
				CouponInfoQueryObject qo = new CouponInfoQueryObject(currentPage, mv,
						"store_id", "asc");
				qo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
				qo.addQuery("obj.status", new SysMap("status", 0), "=");
				qo.addQuery("obj.coupon.coupon_end_time", new SysMap("coupon_end_time", new Date()), ">=");
				IPageList pList = this.couponInfoService.list(qo);
				List<CouponInfo> infos = pList.getResult();
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for(CouponInfo couponInfo : infos){
					Coupon coupon = couponInfo.getCoupon();
					Map<String, Object> couponInfoMap = new HashMap<String, Object>();
					couponInfoMap.put("Coupon_id", couponInfo.getId());
					couponInfoMap.put("Coupon_sn", couponInfo.getCoupon_sn());
					couponInfoMap.put("Coupon_amount", coupon.getCoupon_amount());
					couponInfoMap.put("Coupon_order_amount", coupon.getCoupon_order_amount());
					couponInfoMap.put("Coupon_begin_time", CommUtil.formatTime("yyyy.MM.dd", 
															coupon.getCoupon_begin_time()));
					couponInfoMap.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", 
															coupon.getCoupon_end_time()));
					couponInfoMap.put("coupon_type", coupon.getCoupon_type());
					couponInfoMap.put("Status", couponInfo.getStatus());
					Store store = couponInfo.getCoupon().getStore();
					couponInfoMap.put("store_id", store == null ? "" : store.getId());
					couponInfoMap.put("store_name", coupon.getStore() == null ? "Soarmall" :  coupon.getStore().getStore_name());
					list.add(couponInfoMap);
				}
				map.put("couponList", list);
				map.put("poointNum", user.getPointNum());
				result = new Result(0, "Successfuly" ,map);
			}
		return Json.toJson(result, JsonFormat.compact());
	}
}
