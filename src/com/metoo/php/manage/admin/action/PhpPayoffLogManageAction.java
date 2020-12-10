package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;
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
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/php/admin/payoff")
public class PhpPayoffLogManageAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IPayoffLogService payoffLogService;
	@Autowired
	private IStoreService storeService;

	@RequestMapping("/save.json")
	public void sysConfig(HttpServletRequest request, HttpServletResponse response, String id, String name) {
		Result result = null;
		User user = this.userService.getObjByProperty(null, "userName", name);
		if (user != null && user.getUserRole().equals("ADMIN")) {
			SysConfig obj = this.configService.getSysConfig();
			WebForm wf = new WebForm();
			SysConfig sysConfig = null;
			if (id.equals("")) {
				sysConfig = wf.toPo(request, SysConfig.class);
				sysConfig.setAddTime(new Date());
			} else {
				sysConfig = (SysConfig) wf.toPo(request, obj);
			}
			Date now = new Date();
			int now_date = now.getDate();
			String select = getSelectedDate(CommUtil.null2Int(sysConfig.getPayoff_count()));
			String str[] = select.split(",");
			for (String payoff_date : str) {
				if (CommUtil.null2Int(payoff_date) >= now_date) {
					now.setDate(CommUtil.null2Int(payoff_date));
					now.setHours(0);
					now.setMinutes(00);
					now.setSeconds(01);
					break;
				}
			}
			sysConfig.setPayoff_date(now);
			if (id.equals("")) {
				this.configService.save(sysConfig);
			} else {
				this.configService.update(sysConfig);
			}
			result = new Result(5200, "No such user");
		} else {
			result = new Result(5422, "No such user");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	public String getSelectedDate(int count) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.roll(Calendar.DATE, -1);
		int allDate = cal.get(Calendar.DATE);
		String selected = "";
		switch (count) {
		case 1:
			selected = CommUtil.null2String(allDate);
			break;
		case 2:
			if (allDate == 31) {
				selected = "15,31";
			}
			if (allDate == 30) {
				selected = "15,30";
			}
			if (allDate == 29) {
				selected = "14,29";
			}
			if (allDate == 28) {
				selected = "14,28";
			}
			break;
		case 3:
			if (allDate == 31) {
				selected = "10,20,31";
			}
			if (allDate == 30) {
				selected = "10,20,30";
			}
			if (allDate == 29) {
				selected = "10,20,29";
			}
			if (allDate == 28) {
				selected = "10,20,28";
			}
			break;
		case 4:
			if (allDate == 31) {
				selected = "7,14,21,31";
			}
			if (allDate == 30) {
				selected = "7,14,21,30";
			}
			if (allDate == 29) {
				selected = "7,14,21,29";
			}
			if (allDate == 28) {
				selected = "7,14,21,28";
			}
			break;
		}
		return selected;
	}

	/**
	 * 结算保存
	 */
	@RequestMapping("/settle.json")
	public void payoff(HttpServletRequest request, HttpServletResponse response, String id, String uid) {
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		PayoffLog obj = this.payoffLogService.getObjById(CommUtil.null2Long(id));
		WebForm wf = new WebForm();
		obj = (PayoffLog) wf.toPo(request, obj);
		obj.setStatus(6);
		obj.setComplete_time(new Date());
		obj.setAdmin(user);
		this.payoffLogService.update(obj);
		Store store = obj.getSeller().getStore();
		store.setStore_sale_amount(
				BigDecimal.valueOf(CommUtil.subtract(store.getStore_sale_amount(), obj.getOrder_total_price())));// 减少店铺本次结算总销售金额
		store.setStore_commission_amount(
				BigDecimal.valueOf(CommUtil.subtract(store.getStore_commission_amount(), obj.getCommission_amount())));// 减少店铺本次结算总佣金
		store.setStore_payoff_amount(
				BigDecimal.valueOf(CommUtil.subtract(store.getStore_payoff_amount(), obj.getTotal_amount())));// 减少店铺本次结算金额
		this.storeService.update(store);
		SysConfig sc = this.configService.getSysConfig();
		sc.setPayoff_all_amount(BigDecimal.valueOf(CommUtil.add(obj.getTotal_amount(), sc.getPayoff_all_amount())));// 增加系统总结算（应结算）
		sc.setPayoff_all_amount_reality(
				BigDecimal.valueOf(CommUtil.add(obj.getReality_amount(), sc.getPayoff_all_amount_reality())));// 增加系统实际总结算
		this.configService.update(sc);
		Result result = new Result(0, "Successfully");
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
}
