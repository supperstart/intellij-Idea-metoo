package com.metoo.core.tools;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间与日期工具
 * 
 * @author hjw
 */
public final class DateHelper {

	private static String defaultDateFormat = "yyyy-MM-dd";
	private static String defaultTimeFormat = "HH:mm:ss";
	private static String defaultDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	private static Locale defaultLocale = Locale.CHINA;

	public static void setDefaultDateFormat(String format) {
		DateHelper.defaultDateFormat = format;
	}
	
	public static String getCurrentTimeStampString(){
		String time = DateHelper.formatDateTime(new Date(), "yyyyMMddHHmmssSSS", Locale.CHINA);
		return time;
	}

	public static String getDefaultDateFormat() {
		return defaultDateFormat;
	}

	public static void setDefaultTimeFormat(String defaultTimeFormat) {
		DateHelper.defaultTimeFormat = defaultTimeFormat;
	}

	public static String getDefaultTimeFormat() {
		return defaultTimeFormat;
	}

	public static void setDefaultDateTimeFormat(String defaultDateTimeFormat) {
		DateHelper.defaultDateTimeFormat = defaultDateTimeFormat;
	}

	public static String getDefaultDateTimeFormat() {
		return defaultDateTimeFormat;
	}

	public static void setDefaultLocal(Locale locale) {
		DateHelper.defaultLocale = locale;
	}

	public static Locale getDefaultLocal() {
		return defaultLocale;
	}

	public static Date getCurrent() {
		return new Date(System.currentTimeMillis());
	}


	/**
	 * 格式化日期时间
	 * 
	 * @param calendar
	 *            时间
	 * @param format
	 *            格式
	 * @param locale
	 *            地域
	 * @return
	 */
	public static String formatDateTime(Calendar calendar, String format,
			Locale locale) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		return sdf.format(calendar.getTime());
	}

	/**
	 * 格式化日期时间
	 * 
	 * @param date
	 *            时间
	 * @param format
	 *            格式
	 * @param locale
	 *            地域
	 * @return
	 */
	public static String formatDateTime(Date date, String format, Locale locale) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		return sdf.format(date);
	}

	/**
	 * 取当前服务器上的日期。使用默认日期格式。<br />
	 * 在没有改变的情况下.默认格式为"yyyy-MM-dd".
	 * 
	 * @return
	 */
	public static String getCurrentDate() {
		return formatDateTime(getCurrent(), getDefaultDateFormat(),
				getDefaultLocal());
	}

	/**
	 * 取当前服务器上的时间.使用默认时间格式.<br />
	 * 在没有改变的情况下.默认格式为"HH:mm:ss".
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		return formatDateTime(getCurrent(), getDefaultTimeFormat(),
				getDefaultLocal());
	}

	/**
	 * 取当前服务器上的日期时间.使用默认日期时间格式.<br />
	 * 在没有改变的情况下.默认格式为"yyyy-MM-dd HH:mm:ss".
	 * 
	 * @return
	 */
	public static String getCurrentDateTime() {
		return formatDateTime(getCurrent(), getDefaultDateTimeFormat(),
				getDefaultLocal());
	}

	/**
	 * 转换Date为Timestamp.
	 * 
	 * @param date
	 * @return
	 */
	public static Timestamp getTimestamp(Date date) {
		return new Timestamp(date.getTime());
	}

	/**
	 * 转换Timestamp为Date.
	 * 
	 * @param timestamp
	 * @return
	 */
	public static Date getDate(Timestamp timestamp) {
		return new Date(timestamp.getTime());
	}

	/**
	 * 返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数
	 * 
	 * @param date
	 * @return
	 */
	public static long getTime(Date date) {
		return date.getTime();
	}

	/**
	 * 使用指定的格式和地域解析字段串为时间
	 * 
	 * @param format
	 *            格式
	 * @param locale
	 *            地域
	 * @param str
	 *            字段串
	 * @return
	 */
	public static Date parseDateTime(String format, Locale locale, String str) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用默认值解析表示日期时间的字符
	 * 
	 * @param str
	 * @return
	 */
	public static Date parseDateTime(String str) {
		return parseDateTime(getDefaultDateTimeFormat(), getDefaultLocal(), str);
	}

	/**
	 * 使用默认值解析表示日期的字符
	 * 
	 * @param str
	 * @return
	 */
	public static Date parseDate(String str) {
		return parseDateTime(getDefaultDateFormat(), getDefaultLocal(), str);
	}

	/**
	 * 使用默认值解析表示时间的字符
	 * 
	 * @param str
	 * @return
	 */
	public static Date parseTime(String str) {
		return parseDateTime(getDefaultTimeFormat(), getDefaultLocal(), str);
	}

	/**
	 * 使用指定格式取指定时间的月首
	 * 
	 * @param format
	 * @return
	 */
	public static String getMonthStart(Date date, String format) {
		Calendar localTime = Calendar.getInstance(getDefaultLocal());
		localTime.setTime(date);
		localTime.set(Calendar.DAY_OF_MONTH, 1);
		return formatDateTime(localTime, format, getDefaultLocal());
	}

	/**
	 * 使用指定格式取指定时间的月末
	 * 
	 * @return
	 */
	public static String getMonthEnd(Date date, String format) {
		Calendar localTime = Calendar.getInstance(getDefaultLocal());
		localTime.setTime(date);
		localTime.add(Calendar.MONTH, 1);
		localTime.set(Calendar.DAY_OF_MONTH, 1);
		localTime.add(Calendar.DAY_OF_MONTH, -1);
		return formatDateTime(localTime, format, getDefaultLocal());
	}

	/**
	 * 对指定时间进行增减操作后返回指定格式的时间串
	 * 
	 * @param date
	 *            指定的时间
	 * @param calendarField
	 *            在{@link java.util.Calendar}类定义的字段
	 * @param amount
	 *            增加数量
	 * @param format
	 *            指定格式
	 * @return
	 */
	public static String addDateTime(Date date, int calendarField, int amount,
			String format) {
		Calendar localTime = Calendar.getInstance(getDefaultLocal());
		localTime.setTime(date);
		localTime.add(calendarField, amount);
		return formatDateTime(localTime, format, getDefaultLocal());
	}

	/**
	 * 比较两个时间的顺序
	 * 
	 * @param src
	 * @param dst
	 * @return 如果src在dst之前则 小于0<br />
	 *         如果src在dst之后则 大于0<br />
	 *         如果相同则 等于0
	 */
	public static int comparaDateTime(Date src, Date dst) {
		return src.compareTo(dst);
	}
	
	/**
	 * 将日期的时分秒部分去掉
	 * @param date
	 * @return
	 */
	public static Date truncateTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
	/**
	 * 将java.util.Date转换成java.sql.Date
	 * @param date
	 * @return
	 */
	public static java.sql.Date transfer2SqlDate(java.util.Date date){
		return new java.sql.Date(date.getTime());
	}
	
	/**
	 * java.util.Date转成java.sql.Timestamp
	 * @param date
	 * @return
	 */
	public static java.sql.Timestamp transfer2Timestamp(java.util.Date date){
		return new java.sql.Timestamp(date.getTime());
	}
	
	public static Date AddDate(Date date, int DateField, int num){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(DateField, num);
		
		return cal.getTime();
	}
	
	public static Date getLastDateOfMonth(Date date){
		Date truncdate = truncateTime(date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(truncdate);
		int maxday = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
//		System.out.println(maxday);
		
		cal.set(Calendar.DAY_OF_MONTH, maxday);
		return cal.getTime();
	}
	
	
	/**
	 * 根据DATE，格示化
	 * 
	 * @param format
	 * @param date
	 * @return
	 */
	public static String parseDateToString(String format, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, getDefaultLocal());
		try {
			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception{
		/*String datestr = "2010-04-01";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date d = simpleDateFormat.parse(datestr);
		System.out.println(getLastDateOfMonth(d));*/
		
		System.out.println(DateHelper.getCurrentTimeStampString());
		
	}

}
