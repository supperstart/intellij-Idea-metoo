package com.metoo.php.manage.seller.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IUserService;

@Controller
public class PEvaluateSellerAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private IEvaluateService evaluateService;

	@RequestMapping("/php/seller/evaluateSave.json")
	public void evaluate_reply_save(HttpServletRequest request, HttpServletResponse response, String id, String reply,
			String uid) {
		Result result = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		user = user.getParent() == null ? user : user.getParent();
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		if (evl != null && evl.getOf().getStore_id().equals(user.getStore().getId().toString())) {
			// evl.setReply(CommUtil.filterHTML(reply));
			evl.setReply(reply);
			evl.setReply_status(1);
			this.evaluateService.update(evl);
			result = new Result(0, "success");
		}
		CommUtil.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
}
