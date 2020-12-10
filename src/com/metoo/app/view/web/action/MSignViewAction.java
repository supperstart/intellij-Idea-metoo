package com.metoo.app.view.web.action;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.constant.Globals;
import com.metoo.core.service.IQueryService;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.Sign;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.ISignService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("app/sign/v1")
public class MSignViewAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private ISignService signService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private DatabaseTools databaseTools;

	@RequestMapping("/querySign.json")
	@ResponseBody
	public String querySign(HttpServletRequest request, HttpServletResponse response, String token) {
		int code = -1;
		String msg = "";
		Map map = new HashMap();
		if (!CommUtil.null2String(token).equals("")) {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user != null) {
				Sign sign = user.getSign();
				int WhetherSign = 0;// 0：漏签或第一次签到 1：已签到 -1：未签到
				String last_time = "";// 上一次签到时间
				if (sign == null) {
					sign = new Sign();
					sign.setAddTime(new Date());
					sign.setUser(user);
					sign.setCount(0);
					this.signService.save(sign);
				} else {
					Date update_time = sign.getUpdate_time();
					last_time = CommUtil.formatTime("yyyy-MM-dd HH:mm", update_time);
					//查询是否为本月第一天 
					Calendar c = Calendar.getInstance();
					int first_day = c.get(c.DAY_OF_MONTH);
					if(first_day ==1){
						sign.setContinueSign(0);
						this.signService.update(sign);
					}
					//查询连续签到次数
					if (sign.getContinueSign() > 0) {
						SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
						Date date = null;
						try {
							try {
								date = format.parse(last_time);
							} catch (java.text.ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}

						Calendar current = Calendar.getInstance();
						Calendar today = Calendar.getInstance(); // 今天
						today.set(Calendar.YEAR, current.get(Calendar.YEAR));
						today.set(Calendar.MONTH, current.get(Calendar.MONTH));
						today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
						// Calendar.HOUR——12小时制的小时数
						// Calendar.HOUR_OF_DAY——24小时制的小时数
						today.set(Calendar.HOUR_OF_DAY, 0);
						today.set(Calendar.MINUTE, 0);
						today.set(Calendar.SECOND, 0);

						Calendar yesterday = Calendar.getInstance(); // 昨天

						yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
						yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
						yesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
						yesterday.set(Calendar.HOUR_OF_DAY, 0);
						yesterday.set(Calendar.MINUTE, 0);
						yesterday.set(Calendar.SECOND, 0);

						current.setTime(date);
						// 上次签到时间
						if (current.after(today)) {
							//System.out.println("今天 " + last_time.split(" ")[1]);
							WhetherSign = 1;// 已签到
						} else if (current.before(today) && current.after(yesterday)) {
							//System.out.println("昨天 " + last_time.split(" ")[1]);
							WhetherSign = -1;// 未签到
						} else {
							//int index = last_time.indexOf("-") + 1;
							//System.out.println(last_time.substring(index, last_time.length()));
							// 漏签-清空用户连续记录
							WhetherSign = -1;// 未签到
							sign.setContinueSign(0);
							this.signService.update(sign);
						}
					}
				}
				int count = sign.getCount();
				map.put("last_time", last_time);// 上次签到时间
				map.put("count", sign.getCount());// 签到总次数
				map.put("integral", sign.getIntegral());// 签到总积分
				map.put("WhetherSign", WhetherSign);// 用户今日是否签到
				map.put("continueSign", sign.getContinueSign());// 用户页面连续签到显示
				// 查询本月总积分Sun Nov 01 18:06:00 CST 2020
				Date begin_time = this.getBeginDayOfMonth();
				Date end_time = this.getEndDayOfMonth();
				Map params = new HashMap();
				params.put("begin_time", begin_time);
				params.put("end_time", end_time);
				params.put("integral_from", "sign");
				params.put("integral_user", user.getId());
				// 本月积分查询
				List<IntegralLog> month_integralLogs = this.integralLogService.query(
						"select obj from IntegralLog obj where obj.addTime>=:begin_time and obj.addTime<=:end_time and obj.integral_from=:integral_from and obj.integral_user.id=:integral_user",
						params, -1, -1);
				
				int integral = this.databaseTools.queryNum("select SUM(integral) from " + Globals.DEFAULT_TABLE_SUFFIX
						+ "integrallog where integral_from='sign'" 
						+ " and addTime>=" + CommUtil.formatTime("yyyy-MM-dd", begin_time)
						+ " and addTime<=" + CommUtil.formatTime("yyyy-MM-dd", end_time)
						);
				map.put("current_month_count", month_integralLogs.size());// 本月签到总次数
				map.put("current_month_integral", integral);// 本月签到总积分
				// 本年签到积分查询
				params.clear();
				params.put("integral_user", user.getId());
				params.put("integral_from", "sign");
				List<IntegralLog> years_integralLogs = this.integralLogService.query(
						"select obj from IntegralLog obj where obj.integral_user.id=:integral_user and obj.integral_from=:integral_from order by addTime desc",
						params, -1, -1);
				List<Map> list = new ArrayList<Map>();
				for(IntegralLog integralLog : years_integralLogs){
					Map integralLogMap = new HashMap();
					integralLogMap.put("addTime", integralLog.getAddTime());
					integralLogMap.put("type", integralLog.getType());
					integralLogMap.put("from", integralLog.getIntegral_from());
					integralLogMap.put("integral", integralLog.getIntegral());
					list.add(integralLogMap);
				}
				map.put("years_integralLogs", list);
				code = 200;
				msg = "Successfullu";
			} else {
				code = -100;
				msg = "token Invalidation";

			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		return Json.toJson(new Result(code, msg, map), JsonFormat.compact());
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param token
	 * @return
	 * @description 签到
	 */
	@RequestMapping("/checkin.json")
	@ResponseBody
	public String checkin(HttpServletRequest request, HttpServletResponse response, String token) {
		int code = -1;
		String msg = "";
		if (!CommUtil.null2String(token).equals("")) {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user != null) {
				Sign sign = user.getSign();
				if (sign == null) {
					// 新用户签到
					sign = new Sign();
					sign.setAddTime(new Date());
					sign.setUpdate_time(new Date());
					sign.setContinueSign(1);
					sign.setCount(1);
					sign.setIntegral(1);
					sign.setUser(user);
					this.signService.save(sign);
					IntegralLog integralLog = new IntegralLog();
					integralLog.setAddTime(new Date());
					integralLog.setIntegral(1);
					integralLog.setIntegral_from("sign");
					integralLog.setIntegral_user(user);
					integralLog.setType("Add");
					integralLog.setContent("用户" + CommUtil.formatLongDate(new Date()) + "签到增加" + 1 + "分");
					this.integralLogService.save(integralLog);
				} else {
					//判断是否为本月的第一天 设置连续签到中断 重新开始计算连续签到天数
					Calendar c = Calendar.getInstance();
					int first_day = c.get(c.DAY_OF_MONTH);
					if(first_day ==1){//每月第一天连续签到重新计算
						sign.setContinueSign(0);
						this.signService.update(sign);
					}
					//查询昨天是否签到  设置连续签到中断
					Date update_time = sign.getUpdate_time();
					if(update_time != null){
						String last_time = CommUtil.formatTime("yyyy-MM-dd HH:mm", update_time);
						SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
						Date date = null;
						try {
							try {
								date = format.parse(last_time);
							} catch (java.text.ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}

						Calendar current = Calendar.getInstance();
						Calendar today = Calendar.getInstance(); // 今天
						today.set(Calendar.YEAR, current.get(Calendar.YEAR));
						today.set(Calendar.MONTH, current.get(Calendar.MONTH));
						today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
						// Calendar.HOUR——12小时制的小时数
						// Calendar.HOUR_OF_DAY——24小时制的小时数
						today.set(Calendar.HOUR_OF_DAY, 0);
						today.set(Calendar.MINUTE, 0);
						today.set(Calendar.SECOND, 0);

						Calendar yesterday = Calendar.getInstance(); // 昨天

						yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
						yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
						yesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
						yesterday.set(Calendar.HOUR_OF_DAY, 0);
						yesterday.set(Calendar.MINUTE, 0);
						yesterday.set(Calendar.SECOND, 0);

						current.setTime(date);
						if (current.before(today) && current.after(yesterday)) {
							//上次签到时间为昨天
						}else if(current.after(today)){
							//上次签到时间为今天
						}else{
							//漏签清除连续签到
							sign.setContinueSign(0);
							this.signService.update(sign);
						}
					}
					//查询今天是否已签到
					List<Sign> signs = this.signService.query("select obj from Sign obj where date(update_time) = curdate()", null, -1, -1);
					if(signs.size() == 0){
						sign.setUpdate_time(new Date());
						int integral = 0;
						if (sign.getContinueSign() >= 7) {
							integral = 7;
						}else{
							integral = sign.getContinueSign() + 1;
						}
						sign.setContinueSign(sign.getContinueSign() + 1 < 7 ? sign.getContinueSign() + 1 : 7);
						sign.setCount(sign.getCount() + 1);
						sign.setIntegral(sign.getIntegral() + integral);
						this.signService.update(sign);
						
						IntegralLog integralLog = new IntegralLog();
						integralLog.setAddTime(new Date());
						integralLog.setIntegral(integral);
						integralLog.setIntegral_from("sign");
						integralLog.setIntegral_user(user);
						integralLog.setType("Add");
						integralLog.setContent("用户" + CommUtil.formatLongDate(new Date()) + "签到增加" + integral + "分");
						this.integralLogService.save(integralLog);
						code = 4200;
						msg = "Successfuly";
					}else{
						code = 4401;
						msg = "You have signed in today, please do not double sign in";
					}
				}
			} else {
				code = -100;
				msg = "token Invalidation";
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		return Json.toJson(new Result(code, msg), JsonFormat.compact());
	}

	/**
	 * 根据日期获取该日期是该年的多少天
	 * 
	 * @param date
	 * @return
	 */

	@Test
	public void getDayNumForYear() {
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		System.out.println(ca.get(Calendar.DAY_OF_YEAR));

		Date begin_time = this.getBeginDayOfMonth();

		Date end_time = this.getEndDayOfMonth();

		System.out.println("本月开始时间：" + begin_time + "本月结束时间：" + end_time);
	}

	/**
	 * 格式化时间
	 * 
	 * @param time
	 * @return
	 */
	@Test
	public void formatDateTime() {
		String time = "2020-11-24 15:49";
		SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			try {
				date = format.parse(time);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar current = Calendar.getInstance();

		Calendar today = Calendar.getInstance(); // 今天

		today.set(Calendar.YEAR, current.get(Calendar.YEAR));
		today.set(Calendar.MONTH, current.get(Calendar.MONTH));
		today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
		// Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		Calendar yesterday = Calendar.getInstance(); // 昨天

		yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
		yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
		yesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
		yesterday.set(Calendar.HOUR_OF_DAY, 0);
		yesterday.set(Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND, 0);

		current.setTime(date);

		if (current.after(today)) {
			System.out.println("今天 " + time.split(" ")[1]);
			;
		} else if (current.before(today) && current.after(yesterday)) {
			System.out.println("昨天 " + time.split(" ")[1]);
		} else {
			int index = time.indexOf("-") + 1;
			System.out.println(time.substring(index, time.length()));
		}
	}

	// 获取本年的开始时间
	public static java.util.Date getBeginDayOfYear() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, getNowYear());
		// cal.set
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);

		return getDayStartTime(cal.getTime());
	}

	// 获取本年的结束时间
	public static java.util.Date getEndDayOfYear() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, getNowYear());
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DATE, 31);
		return getDayEndTime(cal.getTime());
	}

	// 获取本月的开始时间
	public static Date getBeginDayOfMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(getNowYear(), getNowMonth() - 1, 1);
		// return getDayStartTime(calendar.getTime());
		return calendar.getTime();
	}

	// 获取本月的结束时间
	public static Date getEndDayOfMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(getNowYear(), getNowMonth() - 1, 1);
		int day = calendar.getActualMaximum(5);
		calendar.set(getNowYear(), getNowMonth() - 1, day);
		// return getDayEndTime(calendar.getTime());
		return calendar.getTime();
	}

	// 获取今年是哪一年
	public static Integer getNowYear() {
		Date date = new Date();
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.setTime(date);
		return Integer.valueOf(gc.get(1));
	}

	// 获取本月是哪一月
	public static int getNowMonth() {
		Date date = new Date();
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.setTime(date);
		return gc.get(2) + 1;
	}

	// 获取某个日期的开始时间
	public static Timestamp getDayStartTime(Date d) {
		Calendar calendar = Calendar.getInstance();
		if (null != d)
			calendar.setTime(d);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0,
				0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return new Timestamp(calendar.getTimeInMillis());
	}

	// 获取某个日期的结束时间
	public static Timestamp getDayEndTime(Date d) {
		Calendar calendar = Calendar.getInstance();
		if (null != d)
			calendar.setTime(d);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23,
				59, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return new Timestamp(calendar.getTimeInMillis());
	}

}
