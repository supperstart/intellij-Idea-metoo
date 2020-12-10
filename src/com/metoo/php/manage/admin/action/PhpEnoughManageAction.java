package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.app.buyer.domain.Result;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.service.IEnoughReduceService;

@Controller
@RequestMapping("/php/admin/enoughReduce")
public class PhpEnoughManageAction {

	@Autowired
	private IEnoughReduceService enoughReduceService;

	@RequestMapping("/apply.json")
	public void apply(HttpServletRequest request, HttpServletResponse response, String id, String erstatus,
			String failed_reason) {
		int code = 0;
		String msg = "";
		EnoughReduce enoughReduce = null;
		if (id != null && !id.equals("")) {
			enoughReduce = this.enoughReduceService.getObjById(CommUtil.null2Long(id));
			if (CommUtil.null2Int(erstatus) == -10) {
				enoughReduce.setErstatus(CommUtil.null2Int(erstatus));
				enoughReduce.setFailed_reason(failed_reason);
			}
			if (CommUtil.null2Int(erstatus) == 10) {
				enoughReduce.setErstatus(CommUtil.null2Int(erstatus));
				enoughReduce.setFailed_reason("");
			}
			this.enoughReduceService.update(enoughReduce);
			code = 5200;
			msg = "Successfully";
		} else {
			code = 5400;
			msg = "Id is empty";
		}
		Result result = new Result(code, msg);
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
