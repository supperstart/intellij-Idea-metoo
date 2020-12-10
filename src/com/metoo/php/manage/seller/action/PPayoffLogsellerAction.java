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
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/php/seller")
public class PPayoffLogsellerAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IPayoffLogService payoffLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderFormService orderFormService;

	/**
	 * 账单结算
	 */
	@RequestMapping("/settle.json")
	public void payOffEdit(HttpServletRequest reques, HttpServletResponse response, String uid, String mulitId) {
		Result result = null;
		if (mulitId.split(",") != null) {
			for (String id : mulitId.split(",")) {
				if (id != null && !id.equals("")) {
					PayoffLog obj = this.payoffLogService.getObjById(CommUtil.null2Long(id));
					if (obj != null) {
						User user = userService.getObjById(CommUtil.null2Long(uid));
						user = user.getParent() == null ? user : user.getParent();
						if (user != null && user.getId().equals(obj.getSeller().getId()) && obj.getStatus() == 1) {
							OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(obj.getO_id()));
							if (order != null) {
								// boolean flag = this.validatePayoffDate();
								boolean flag = true;
								boolean goods = false;// 购物
								boolean group = false;// 团购
								if (order.getOrder_status() == 50 && flag || order.getOrder_status() == 65 && flag) {
									goods = true;
								}
								if (order.getOrder_cat() == 2) {
									if (order.getOrder_status() == 20 && flag) {// 团购消费码订单
										group = true;
									}
								}
								if (goods || group) {// 已经完成的订单，并且今天为结算日
									obj.setStatus(3);// 设置结算中
									obj.setApply_time(new Date());
									this.payoffLogService.update(obj);
									result = new Result(0, "success");
								}
							}
						}
					}
				}
			}
		} else {
			result = new Result(105, "没有该记录");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	private boolean validatePayoffDate() {
		boolean payoff = false;
		Date Payoff_data = this.configService.getSysConfig().getPayoff_date();
		Date now = new Date();
		now.setHours(0);
		now.setMinutes(0);
		now.setSeconds(0);
		Date next = new Date();
		next.setDate(next.getDate() + 1);
		next.setHours(0);
		next.setMinutes(0);
		next.setSeconds(0);
		if (Payoff_data.after(now) && Payoff_data.before(next)) {
			payoff = true;
		}
		return payoff;
	}
}
