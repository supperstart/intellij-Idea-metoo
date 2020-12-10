package com.metoo.app.view.web.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.Md5Encrypt;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;

@Component
public class MAppLoginViewTools {
	
	@Autowired
	private ICouponInfoService couponInfoService;

	
	/**
	 * 当用户登录后生成verify返回给客户端保存，每次发送用户中心中请求时将verify放入到请求头中，
	 * 用来验证用户密码是否已经被更改，如已经更改，手机客户端提示用户重新登录
	 * 
	 * @param user
	 * @return
	 */
	public String create_appverify(User user) {
		String app_verify = user.getPassword() + user.getApp_login_token();
		app_verify = Md5Encrypt.md5(app_verify).toLowerCase();
		return app_verify;
	}
	
	/**
	 * 
	 * @param user
	 * @param tourists
	 * @descript 合并用户优惠券
	 */
	public void margeCoupon(User user, String tourists) {
		Map params = new HashMap();
		params.put("tourists", tourists);
		List<CouponInfo> couponInfos = this.couponInfoService
				.query("select obj from CouponInfo obj where obj.tourists=:tourists", params, -1, -1);
		params.clear();
		params.put("user_id", user.getId());
		List<CouponInfo> userCouponInofs = this.couponInfoService.query("select obj from CouponInfo obj where obj.user.id=:user_id", params, -1, -1);
		List<Long> list = new ArrayList<Long>();
		if(userCouponInofs.size() > 0){
			for(CouponInfo CouponInof : userCouponInofs){
				list.add(CouponInof.getCoupon().getId());
			}
		}
		for(CouponInfo couponInfo :  couponInfos){
			if(list.contains(couponInfo.getCoupon().getId())){
				this.couponInfoService.delete(couponInfo.getId());
			}else{
				couponInfo.setUser(user);
				couponInfo.setTourists(null);
				this.couponInfoService.update(couponInfo);
			}
		}
	}
	
	public void send_json(String json, HttpServletResponse response) {
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
