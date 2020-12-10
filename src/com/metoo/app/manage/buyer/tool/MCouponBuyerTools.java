package com.metoo.app.manage.buyer.tool;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;

@Component
public class MCouponBuyerTools {

	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;

	/**
	 * 
	 * @param coupon
	 * @param user
	 * @param visitor_id
	 * @return
	 * @发放优惠券到用户or游客
	 */
	public boolean IssueCoupon(Coupon coupon, User user, String tourists) {
		boolean flag = true;
		if (coupon != null) {
			Store store = coupon.getStore();
			CouponInfo couponInfo = null;
			boolean couponflag = true;
			if (coupon.getCoupon_count() > 0) {
				if (coupon.getCoupon_count() < coupon.getCouponinfos().size()) {
					flag = false;
				} else {
					couponInfo = new CouponInfo();
					couponInfo.setTourists(tourists);
					;
					couponInfo.setAddTime(new Date());
					couponInfo.setCoupon(coupon);
					couponInfo.setUser(user);
					couponInfo.setCoupon_sn(UUID.randomUUID().toString());
					couponInfo.setStore_id(store == null ? -1 : store.getId());
					if (this.couponInfoService.save(couponInfo) && coupon.getCoupon_count() > 0) {
						coupon.setCoupon_count(coupon.getCoupon_count() - 1);
						this.couponService.update(coupon);
					}
				}
			} else {
				couponInfo = new CouponInfo();
				couponInfo.setTourists(tourists);
				couponInfo.setAddTime(new Date());
				couponInfo.setCoupon(coupon);
				couponInfo.setUser(user);
				couponInfo.setCoupon_sn(UUID.randomUUID().toString());
				couponInfo.setStore_id(store == null ? -1 : store.getId());
				this.couponInfoService.save(couponInfo);
			}
		}
		return flag;
	}

	// 提取公共部分 使用工厂函数
	/*
	 * public void creatCouponInfo(){
	 * 
	 * }
	 */
}
