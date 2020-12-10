package com.metoo.core.log.model;

import javax.servlet.http.HttpServletRequest;

import com.metoo.core.log.model.LogEnum.Channel;

/**
 * 访问日志内容对象
 * 
 * @author hjw
 * 
 */
public class AccessLogModel extends BaseLogModel {


	private static final long serialVersionUID = 6055189708346378762L;

	private String channel = ""; // 访问的途径
	private String url = ""; // 访问URL
	private String sourceUrl = ""; // 来源URL
	// private Map<String, String> params = new LinkedHashMap<String, String>();
	// // 参数
	private String params = ""; // 访问参数

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public AccessLogModel(HttpServletRequest request) {
		super(request);
	}

	protected String getChannel() {
		return channel;
	}

	/**
	 * 设置访问渠道
	 * 
	 * @param channel
	 *            渠道类型
	 */
	public void setChannel(Channel channel) {
		this.channel = channel.toString();
	}

	protected String getUrl() {
		return url;
	}

	/**
	 * 设置访问的URL链接
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	protected String getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * 设置访问来源URL
	 * 
	 * @param sourceUrl
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	/**
	 * 设置访问URL中带的参数对
	 * 
	 * @param key
	 *            参数名
	 * @param value
	 *            参数值
	 */
	/*
	 * public void setUrlParam(String key, String value) { this.params.put(key,
	 * value); }
	 * 
	 * protected Map<String, String> getParams() { return params; }
	 * 
	 * protected void setParams(Map<String, String> params) { this.params =
	 * params; }
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("|channel=[");
		builder.append(channel);
		builder.append("]|accessurl=[");
		builder.append(url);
		builder.append("]|urlpra=[");
		builder.append(params);
		builder.append("]|refererurl=[");
		builder.append(sourceUrl);
		builder.append("]");
		return super.toString() + formatLog(builder.toString());
	}
}
