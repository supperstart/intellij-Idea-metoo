package com.metoo.manage.time.test;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.impl.SysConfigService;

@Controller
public class CalendarTest {

	@Autowired
	private ISysConfigService configService;

	@Test
	public void hk() {
		// 其日历字段已由当前日期和时间初始化：
		Calendar rightNow = Calendar.getInstance(); // 子类对象
		// 获取年
		int year = rightNow.get(Calendar.YEAR);
		// 获取月
		int month = rightNow.get(Calendar.MONTH);
		// 获取日
		int date = rightNow.get(Calendar.DATE);
		// 获取几点
		int hour = rightNow.get(Calendar.HOUR_OF_DAY);
		// 获取上午下午
		int moa = rightNow.get(Calendar.AM_PM);
		if (moa == 1)
			System.out.println("下午");
		else
			System.out.println("上午");

		System.out.println(year + "年" + (month + 1) + "月" + date + "日" + hour + "时");
		rightNow.add(Calendar.YEAR, 5);
		rightNow.add(Calendar.DATE, -10);
		int year1 = rightNow.get(Calendar.YEAR);
		int date1 = rightNow.get(Calendar.DATE);
		System.out.println(year1 + "年" + (month + 1) + "月" + date1 + "日" + hour + "时");
	}

	@RequestMapping("eva.json")
	public void tests(HttpServletRequest request, HttpServletResponse response) {
		SysConfig sc = this.configService.getSysConfig();
		int auto_order_evaluate = sc.getAuto_order_evaluate();
		Calendar cal = Calendar.getInstance();
		cal = Calendar.getInstance();
		System.out.println(Calendar.DAY_OF_YEAR);
		cal.add(Calendar.DAY_OF_YEAR, -auto_order_evaluate);
		
		System.out.println(cal.getTime());
	}

}
