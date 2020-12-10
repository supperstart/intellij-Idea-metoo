package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.annotation.ApiVersion;

@Controller
@ApiVersion(2)
@RequestMapping("/hellowd/{version}")
public class VersionApi2 {

	@RequestMapping("/versiond")
	public void version2(HttpServletRequest request, HttpServletResponse response){
		try {
			response.getWriter().print("version2");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*	
	@RequestMapping("/version")
	@ApiVersion(3)
	public void version3(HttpServletRequest request, HttpServletResponse response){
		try {
			response.getWriter().print("version3");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
}

