package com.metoo.php.manage.seller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
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
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Message;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.ReturnGoodsLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IMessageService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IReturnGoodsLogService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;

@Controller
public class PReturnSellerAction {

	@Autowired
	private IReturnGoodsLogService returngoodslogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderFormService orderformService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IMessageService messageService;

	@RequestMapping("/php/return.json")
	public void returnSave(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String goods_return_status, String self_address) {
		Result result = null;
		if (id != null) {
			ReturnGoodsLog obj = this.returngoodslogService.getObjById(Long.parseLong(id));
			obj.setGoods_return_status(goods_return_status);
			obj.setSelf_address(self_address);
			this.returngoodslogService.update(obj);
			User user = userService.getObjById(obj.getUser_id());
			String msg_content = "订单号：" + obj.getReturn_service_id() + "退货申请审核未通过，请在'退货/退款'-'查看返修/退换记录'中提交退货物流信息。";
			if (goods_return_status.equals("6")) {
				msg_content = "订单号：" + obj.getReturn_service_id() + "退货申请审核通过，请在'退货/退款'-'查看返修/退换记录'中提交退货物流信息。";
				OrderForm return_of = this.orderformService.getObjById(obj.getReturn_order_id());
				List<Map> maps = this.orderFormTools.queryGoodsInfo(return_of.getGoods_info());
				List<Map> new_maps = new ArrayList<Map>();
				Map gls = new HashMap();
				for (Map m : maps) {
					if (m.get("goods_id").toString().equals(CommUtil.null2String(obj.getGoods_id()))) {
						m.put("goods_return_status", 6);
						gls.putAll(m);
					}
					new_maps.add(m);
				}
				return_of.setGoods_info(Json.toJson(new_maps));
				this.orderformService.update(return_of);
				result = new Result(0, "success");
				Message msg = new Message();
				msg.setAddTime(new Date());
				msg.setStatus(0);
				msg.setType(0);
				msg.setContent(msg_content);
				msg.setFromUser(SecurityUserHolder.getCurrentUser());
				msg.setToUser(user);
				this.messageService.save(msg);
			}
		} else {
			result = new Result(1, "parameter error");
		}
		this.return_json(result, response);
	}

	@RequestMapping("/php/returnAccomplish.json")
	public void returnAccomplish(HttpServletRequest request, HttpServletResponse response, String id) {
		Result result = null;
		ReturnGoodsLog obj = this.returngoodslogService.getObjById(Long.parseLong(id));
		if (obj != null) {
			obj.setGoods_return_status("10");
			this.returngoodslogService.update(obj);
			OrderForm return_of = this.orderformService.getObjById(obj.getReturn_order_id());
			List<Map> maps = this.orderFormTools.queryGoodsInfo(return_of.getGoods_info());
			List<Map> new_maps = new ArrayList<Map>();
			Map<String, Integer> gls = new HashMap();
			for (Map m : maps) {
				if (m.get("goods_id").toString().equals(CommUtil.null2String(obj.getGoods_id()))) {
					m.put("goods_return_status", 8);
					gls.putAll(m);
				}
				new_maps.add(m);
			}
			return_of.setGoods_info(Json.toJson(new_maps));
			this.orderformService.update(return_of);
			result = new Result(0, "success");
		} else {
			result = new Result(1, "parameter error");
		}
		this.return_json(result, response);
	}

	public void return_json(Result result, HttpServletResponse response) {
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
