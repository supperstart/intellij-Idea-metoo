package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.annotation.ApiVersion;

@Controller
@ApiVersion(1)
@RequestMapping("/hellow/{version}")
public class VersionApi1 {

	@RequestMapping("/version")
	public void version1(HttpServletRequest request, HttpServletResponse response){
		try {
			response.getWriter().print("version1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
