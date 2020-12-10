package com.metoo.app.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.tools.CommUtil;


public class CalendarTest {

	 public static void main(String[] args) {
	        Calendar cal = Calendar.getInstance();
	        
	        int day = cal.get(Calendar.DATE);
	        
	        cal.set(Calendar.DATE, 1);
	        int day1 = cal.get(cal.DATE);
	        
	        cal.roll(Calendar.DATE, -3);
	        int day2 = cal.get(cal.DATE);
	        
	        int month = cal.get(Calendar.MONTH) + 1;
	        int year = cal.get(Calendar.YEAR);
	        int dow = cal.get(Calendar.DAY_OF_WEEK);
	        int dom = cal.get(Calendar.DAY_OF_MONTH);
	        int doy = cal.get(Calendar.DAY_OF_YEAR);
	 
	        System.out.println("当期时间: " + cal.getTime());
	        
	        System.out.println("日期: " + day);
	        System.out.println("日期1: " + day1);
	        System.out.println("日期2: " + day2);
	        
	        System.out.println("月份: " + month);
	        System.out.println("年份: " + year);
	        System.out.println("一周的第几天: " + dow);  // 星期日为一周的第一天输出为 1，星期一输出为 2，以此类推
	        System.out.println("一月中的第几天: " + dom);
	        System.out.println("一年的第几天: " + doy);
	    }

	 
	 @Test
	 public void calendar(){
		 Calendar cal = Calendar.getInstance();
		 int year = cal.get(Calendar.YEAR);//年
		 int month =  cal.get(Calendar.MONTH) + 1;//月（必须要+1）
		 int date = cal.get(Calendar.DATE);//日
		 int hour_of_day = cal.get(Calendar.HOUR_OF_DAY);//时
		 int minute = cal.get(Calendar.MINUTE);//分
		 int second = cal.get(Calendar.SECOND);//秒
		 System.out.println("当前时间： " + cal.getTime());
		 System.out.println("当前年份： " + year);
		 System.out.println("当前月份：" + month);
		 System.out.println("当前日期：" + date);
		 System.out.println("当前小时：" + hour_of_day);
		 System.out.println("当前分：" + minute);
		 System.out.println("当前秒：" + second);
		 
		 Date now = new Date();
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(now);
			cal1.set(Calendar.HOUR_OF_DAY, 0);
			cal1.set(Calendar.MINUTE, 0);
			cal1.set(Calendar.SECOND, 0);
			cal1.add(Calendar.DAY_OF_YEAR, 1);

			 int year1 = cal1.get(Calendar.YEAR);//年
			 int month1 =  cal1.get(Calendar.MONTH) + 1;//月（必须要+1）
			 int date1 = cal1.get(Calendar.DATE);//日
			 int hour_of_day1 = cal1.get(Calendar.HOUR_OF_DAY);//时
			 int minute1 = cal1.get(Calendar.MINUTE);//分
			 int second1 = cal1.get(Calendar.SECOND);//秒
			 System.out.println("当前时间1： " + cal1.getTime());
			 System.out.println("当前年份1： " + year1);
			 System.out.println("当前月份1：" + month1);
			 System.out.println("当前日期1：" + date1);
			 System.out.println("当前小时1：" + hour_of_day1);
			 System.out.println("当前分1：" + minute1);
			 System.out.println("当前秒1：" + second1);
			
	 }
	 
	 @Test
	 public void getSelectedDate(){
		 int count = 2;
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DATE, 1);
			cal.roll(Calendar.DATE, -1);
			int allDate = cal.get(Calendar.DATE);
			String selected = "";
			switch(count){
			case 1:
				selected = CommUtil.null2String(allDate);
				break;
			case 2:
				if (allDate == 31) {
					selected = "15,31";
				}
				if (allDate == 30) {
					selected = "15,30";
				}
				if (allDate == 29) {
					selected = "14,29";
				}
				if (allDate == 28) {
					selected = "14,28";
				}
				break;
			case 3:
				if (allDate == 31) {
					selected = "10,20,31";
				}
				if (allDate == 30) {
					selected = "10,20,30";
				}
				if (allDate == 29) {
					selected = "10,20,29";
				}
				if (allDate == 28) {
					selected = "10,20,28";
				}
				break;
			case 4:
				if (allDate == 31) {
					selected = "7,14,21,31";
				}
				if (allDate == 30) {
					selected = "7,14,21,30";
				}
				if (allDate == 29) {
					selected = "7,14,21,29";
				}
				if (allDate == 28) {
					selected = "7,14,21,28";
				}
				break;
			}
			System.out.println(selected);
		}
	 
	 @Test
	 public void date(){
		
		 String[] date = new String[]{"6", "7", "8"};
		 System.out.println("2020-" + date[new Random().nextInt(date.length)] + "-" + (int)(Math.random()*(29-1+1) + 1));
		 System.out.println("2020-" + date[new Random().nextInt(date.length)] + "-" + (int)(Math.random() * 29 + 1));
		System.out.println(CommUtil.formatDate("2020-" + date[new Random().nextInt(date.length)] + "-" + (int)Math.random() * 29 + 1,"yyyy-MM-dd"));
	 }
	 
	 /**
	  * 计算指定时间间隔
	  * @throws ParseException
	  */
	 @Test
	 public void point_time() throws ParseException{
		 Calendar cal = Calendar.getInstance();
		 cal.add(Calendar.DAY_OF_YEAR, -3);
		 System.out.println(cal.getTime());
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 Date dt = sdf.parse("2020-9-24 18:01:01");
		 
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(dt);
		 System.out.println(calendar.getTime());
		 
		 calendar.add(Calendar.DAY_OF_YEAR, 3);
		 System.out.println(calendar.getTime());
		 
		 Calendar calendar1 = Calendar.getInstance();
		 calendar1.setTime(new Date());
		 System.out.println(calendar1.getTime());
		 
		 Long l = calendar1.getTime().getTime() - calendar.getTime().getTime();//毫秒
		 long day=l/(24*60*60*1000); 

		 long hour=(l/(60*60*1000)-day*24); 

		 long min=((l/(60*1000))-day*24*60-hour*60); 

		 long s=(l/1000-day*24*60*60-hour*60*60-min*60); 

		 System.out.println(""+day+"天"+hour+"小时"+min+"分"+s+"秒");  
		 
		
		 
	 }

}
