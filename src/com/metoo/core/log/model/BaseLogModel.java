package com.metoo.core.log.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;



/**
 * 日志内容基类
 * 
 * @author hjw
 */
public class BaseLogModel implements Serializable {

	private static final long serialVersionUID = 411112259992775450L;
	public static final String BIZLOGID_SESSION_KEY = "_BUSILOGID";
	/**
	 * 用于事件日志与接口日志之间的关联用日志编号的SESSION名称
	 */
	public HttpServletRequest request;
	public HttpSession session;

	private String uuid = "";// 日志唯一编号
	private String sessionId = "";// SESSION编号
	private String ip = "";// 访问者IP
	private String area = "";// 访问者区号
	private String regionId = "";// 访问者区域编号
	private String account = "";// 访问者登录账号
	private String loginType = "";// 访问者登录类型
	private String custId = "";// 访问者的客户标识码
	private String token = "";

	/**
	 * 使用STRUTS2中的方法获取request，并以此获取相关SESSION信息。
	 */
	public BaseLogModel() {
		// request及session使用STRUTS提供的方法，如果运行在非STRUTS容器下则无法提供相关数据记录。
		try {
			session = request.getSession(true);
		} catch (Exception e) {
			System.out
					.println("Struts not running!The request and session can't get.");
		}
		this.setBaseLogModelInfo(request);
	}

	/**
	 * 使用参数中的request，并以此获取相关SESSION信息。
	 * 
	 * @param request
	 */
	public BaseLogModel(HttpServletRequest request) {
		this.setBaseLogModelInfo(request);
	}

	private void setBaseLogModelInfo(HttpServletRequest request) {
		try {
			this.request = request;
			if (request != null) {
				this.session = request.getSession(true);
			}
			// 产生唯一编码
			uuid = UUID.randomUUID().toString().replaceAll("-", "");

			// 优先从HTTP头中取访问者IP地址链;如果无法取到则调用标准服务器端方法.

			ip = request.getHeader("X-Forwarded-For");
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}

			// 从SESSION中获取用户登录相关的信息
			if (session != null) {
				sessionId = session.getId();
				try {
					area = (String)session.getAttribute("area");
					regionId = (String)session.getAttribute("regionid");
					account = (String)session.getAttribute("account");
					loginType = (String)session.getAttribute("logintype");
					custId = (String)session.getAttribute("custid");
				} catch (Exception e) {
					// e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取日志内容对象的唯一编号
	 * 
	 * @return 32位UUID唯一编号
	 */
	public String getUuid() {
		return uuid;
	}

	protected void setUuid(String uuid) {
		this.uuid = uuid;
	}

	protected String getSessionId() {
		return sessionId;
	}

	protected void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	protected String getIp() {
		return ip;
	}

	protected void setIp(String ip) {
		this.ip = ip;
	}

	protected String getArea() {
		return area;
	}

	protected void setArea(String area) {
		this.area = area;
	}

	protected String getRegionId() {
		return regionId;
	}

	protected void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	protected String getAccount() {
		return account;
	}

	protected void setAccount(String account) {
		this.account = account;
	}

	protected String getLoginType() {
		return loginType;
	}

	protected void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	protected String getCustId() {
		return custId;
	}

	protected void setCustId(String custId) {
		this.custId = custId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("logid=[");
		builder.append(UUID.randomUUID().toString().toUpperCase()
				.replaceAll("-", ""));
		builder.append("]|token=[" + token);
		builder.append("]|sessionid=[");
		builder.append(sessionId);
		builder.append("]|accesstime=[");
		builder.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(new Date()));
		builder.append("]|remoteip=[");
		builder.append(ip);
		builder.append("]|account=[");
		builder.append(account);
		builder.append("]|logintype=[");
		builder.append(loginType);
		builder.append("]|custid=[");
		builder.append(custId);
		builder.append("]|areacode=[");
		builder.append(area);
		builder.append("]|regionid=[");
		builder.append(regionId);
		builder.append("]");
		return formatLog(builder.toString());
	}

	/**
	 * 对日志文件进行格式化。现主要是去掉回车和换行。
	 * 
	 * @param str
	 * @return
	 */
	protected String formatLog(String str) {
		String tmp = str;
		if (str != null) {
			tmp = tmp.replaceAll("\\n|\\r", "");
			tmp = tmp.replaceAll(">\\s*<", "><");
		}
		return tmp;
	}

}
