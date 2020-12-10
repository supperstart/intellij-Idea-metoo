package com.metoo.app.statistics.action;

import java.io.IOException;
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

import com.metoo.app.buyer.domain.Result;
import com.metoo.foundation.domain.StoreStat;
import com.metoo.foundation.domain.SystemTip;
import com.metoo.foundation.service.IStoreStatService;
import com.metoo.foundation.service.ISystemTipService;

@Controller
public class Statistics {
	@Autowired
	private IStoreStatService storeStatService;
	@Autowired
	private ISystemTipService systemTipService;
	
	@RequestMapping("/statistics.json")
	public void statistics(HttpServletRequest request,
			HttpServletResponse response) {
		List<StoreStat> stats = this.storeStatService.query(
				"select obj from StoreStat obj order by obj.addTime desc",
				null, -1, -1);
		Result result = null;
		Map map = new HashMap();
		Map params = new HashMap();
		params.put("st_status", 0);
		List<SystemTip> sts = this.systemTipService
				.query("select obj from SystemTip obj where obj.st_status=:st_status order by obj.st_level desc",
						params, -1, -1);
		StoreStat stat = null;
		if (stats.size() > 0) {
			stat = stats.get(0);
		} else {
			stat = new StoreStat();
		}
		if(stat != null){
			map.put("importantReminder", sts.size());
			map.put("week_user", stat.getWeek_user());
			map.put("week_goods", stat.getWeek_goods());
			map.put("week_store", stat.getWeek_store());
			map.put("week_order", stat.getWeek_order());
			map.put("week_complaint", stat.getWeek_complaint());
			map.put("week_live_user", stat.getWeek_live_user());
			map.put("week_ztc", stat.getWeek_ztc());
			map.put("all_user", stat.getAll_user());
			map.put("all_store", stat.getAll_store());
			map.put("all_goods", stat.getAll_goods());
			map.put("not_payoff_num", stat.getNot_payoff_num());
			map.put("goods_audit", stat.getGoods_audit());
			map.put("not_refund", stat.getNot_refund());
			map.put("not_grouplife_refund", stat.getNot_grouplife_refund());
			map.put("store_audit", stat.getStore_audit());
			result = new Result(0, "success",map);
		}
		String temp = Json.toJson(result, JsonFormat.compact());
		try {
			response.getWriter().print(temp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
