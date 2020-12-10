package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.service.ILuckyDrawService;

@Controller
public class LuckyDrawManageAction {

	@Autowired
	private ILuckyDrawService service;
	@Autowired
	private DatabaseTools databaseTools;

	public void lucky(HttpServletRequest request, HttpServletResponse response, String id, String fieldName,
			String value) throws SQLException {
		Result result = null;
		String query = "select * from metoo_lucky_draw where switchs = 1";
		ResultSet res = this.databaseTools.selectIn(query);

		while (res.next()) {
			System.out.println(res.getString("order"));
		}
		result = new Result(5200, "Successfully");
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
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
