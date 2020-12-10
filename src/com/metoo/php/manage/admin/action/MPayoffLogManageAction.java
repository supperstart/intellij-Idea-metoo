package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
@RequestMapping("/admin/payofflog/")
public class MPayoffLogManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IPayoffLogService payofflogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreService storeService;

	@RequestMapping("save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String user_id) {
		User admin = this.userService.getObjById(Long.parseLong(user_id));
		Result result = null;
		PayoffLog obj = this.payofflogService.getObjById(Long.parseLong(id));
		WebForm wf = new WebForm();
		obj = (PayoffLog) wf.toPo(request, obj);
		obj.setStatus(6);
		obj.setComplete_time(new Date());
		obj.setAdmin(admin);
		this.payofflogService.update(obj);
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
		result = new Result(0, "success");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
