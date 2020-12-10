
package com.metoo.core.tools;

import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class UUIDHelper {
	//数字加字母
	private final static  char[] letterAndNum = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',     
             'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',     
             'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };  
	//纯数字
	private final static char[] num = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }; 
	
	
	private final static  char[]  letter= { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',     
        'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',     
        'x', 'y', 'z' };
	
	/**
	 * 获取一个UUID字符串
	 * 
	 * @return
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		String result = str.toUpperCase().replaceAll("-", "");
		return result;
	}
	
	
	

	/**
	 * 获取一个JAVA JDK自带的随机UUID实例
	 * 
	 * @return
	 */
	public static UUID getUUIDrandomInstance() {
		return UUID.randomUUID();
	}
	
	/**
	 * Description:获取当前时间 ，时间格式yyyyMMddHHmmss
	 * @return
	 */
	public static String currentDateTime(){
		String time = DateHelper.formatDateTime(new Date(),
				"yyyyMMddHHmmss", Locale.CHINA);
		return time;
	}	
	
	/**
	 * 
	 * 描述: 返回流水号
	 * @return
	 * @author     "huangrougang"
	 * date        2011-9-29
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * "huangrougang"        2011-9-29       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	public static String getSerialNumber(){
		return  currentTimeStampString()+getRandomLetter(6);
		
	}
	
	

	
	
	public static String currentYYDateTime(){
		String time = DateHelper.formatDateTime(new Date(),
				"yyMMddHHmmss", Locale.CHINA);
		return time;
	}
	
	/**
	 * Description:指定长度的字符 包括字母和数字
	 * @param len
	 * @return
	 */
	public static String getRandomLetterAndNum(int len){
		return genRandomChar(len,letterAndNum); 
	}
	
	/**
	 * Description:指定长度的字符 只含数字
	 * @param len
	 * @return
	 */
	public static String getRandomNum(int len){
		return genRandomChar(len,num);
	}
	
	
	/**
	 * Description:指定长度的字符，只含字母
	 * @param len
	 * @return
	 */
	public static String getRandomLetter(int len){
		return genRandomChar(len,letter);
	}
	
	
	public static String getCurrentTimeStampString() {
		String time = DateHelper.formatDateTime(new Date(),
				"yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINA);
		return time;
	}
	
	/**
	 * 返回与营帐系统对账用流水号
	 * @return
	 */
	public static String getSerialNumForAccount()
	{
		return currentTimeStampString() + getRandomNum(13);
	}
	/**
	 * 返回与营帐系统对账用流水号
	 * @return
	 */
	public static String getSerialNumForICMVAccount()
	{
		return currentTimeStampString() + getRandomNum(1);
	}
	public static String currentTimeStampString() {
		String time = DateHelper.formatDateTime(new Date(),
				"yyyyMMddHHmmssSSS", Locale.CHINA);
		return time;
	}
	
	
	 private static String genRandomChar(int len,char[] str) {      
	       // 35是因为数组是从0开始的，26个字母+10个数字      
	      // final int maxNum = 10;      
	       int i; // 生成的随机数      
	       int count = 0; // 生成的密码的长度      
	    
	       StringBuffer p = new StringBuffer("");      
	       Random r = new Random();      
	       while (count < len) {      
	           // 生成随机数，取绝对值，防止生成负数，      
	    
	           i = Math.abs(r.nextInt(str.length)); // 生成的数最大为36-1      
	    
	           if (i >= 0 && i < str.length) {      
	               p.append(str[i]);      
	               count++;      
	           }      
	       }      
	    
	       return p.toString();      
	   }    
	 /**
	  * 
	  * 描述: 生成64位的随机数字<br>
	  * @return<br>
	  * @author： ji_jinliang<br>
	  * @date：2011-10-20<br>
	  * --------------------------------------------------<br>
	  * 修改人　　　　修改日期　　　　　修改描述<br>
	  * ji_jinliang　　　2011-10-20　　　　　　创建<br>
	  * --------------------------------------------------<br>
	  */
	 public static String genRandomNumFor64(){
		 return "0000000000000000000000000000000000000000"+currentTimeStampString()+getRandomNum(3);
	 }
	 
	  public static void main(String[] args) {
		 System.out.println(getSerialNumForAccount()); 
	}
	
	
}
