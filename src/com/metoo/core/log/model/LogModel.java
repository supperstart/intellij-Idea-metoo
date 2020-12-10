package com.metoo.core.log.model;


/**
 * 通用日志消息内容对象
 * 
 * @author hjw
 * 
 */
public class LogModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2824651305613781456L;

	private String msg = "";

	public LogModel() {

	}

	public LogModel(String msg) {
		this.msg = msg;
	}


	/**
	 * 设置日志消息文本
	 * 
	 * @param msg
	 *            消息文本
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public String toString() {
		return formatLog(msg);
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
