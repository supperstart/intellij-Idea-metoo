package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Role;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IRoleService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/admin/user/")
public class MUserManageAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;

	@RequestMapping("save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String userName,
			String password) {
		boolean flag = false;
		Result result = null;
		WebForm wf = new WebForm();
		User user = null;
		if (id.equals("")) {
			user = wf.toPo(request, User.class);
			user.setAddTime(new Date());
		} else {
			User u = this.userService.getObjById(CommUtil.null2Long(id));
			user = (User) wf.toPo(request, u);
		}
		if (!userName.equals("")) {
			user.setUserName(userName);
		}
		if (!password.equals("")) {
			user.setPassword(Md5Encrypt.md5(password).toLowerCase());
		}
		if (id.equals("")) {
			user.setUserRole("Buyer");
			user.getRoles().clear();
			Map params = new HashMap();
			params.put("type", "BUYER");
			List<Role> roles = this.roleService.query("select new Role(id) from Role obj where obj.type=:type", params,
					-1, -1);
			user.getRoles().addAll(roles);
			flag = this.userService.save(user);
		} else {
			flag = this.userService.save(user);
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			if (flag) {
				result = new Result(0, "Successfully");
				writer.print(Json.toJson(result, JsonFormat.compact()));
			} else {
				result = new Result(1, "The user already exists");
				writer.print(Json.toJson(result, JsonFormat.compact()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("delete.json")
	public void delete(HttpServletRequest request, HttpServletResponse response, String id) {
		Result result = new Result();
		if (id != null && !id.equals("")) {
			User parent = this.userService.getObjById(CommUtil.null2Long(id));
			if (parent.equals("admin")) {
				result = new Result(2, "admin不可删除");
			} else {
				for (User u : parent.getChilds()) {
					u.getRoles().clear();
					u.setDeleteStatus(-1);
					this.userService.update(u);
				}
				parent.setDeleteStatus(-1);
				this.userService.update(parent);
				result = new Result(0, "success");
			}
		} else {
			result = new Result(1, "parameter error");
		}
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
