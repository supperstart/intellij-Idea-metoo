package com.metoo.core.log.model;


/**
 * 应用向内部接口服务器日志内容对象
 * 
 * @author hjw
 * 
 */
public class InnerInterfaceLogModel extends BaseLogModel {


	private static final long serialVersionUID = 6055189708346378762L;

	private String channel = ""; // 访问的途径
	private String interfaceUrl = ""; // 访问URL
	private String sendArgs = ""; // 发送参数
	private String recvArgs = ""; // 返回参数
	private String busiid = "";//业务ID


	public String getChannel() {
		return channel;
	}



	public void setChannel(String channel) {
		this.channel = channel;
	}



	public String getInterfaceUrl() {
		return interfaceUrl;
	}



	public void setInterfaceUrl(String interfaceUrl) {
		this.interfaceUrl = interfaceUrl;
	}



	public String getSendArgs() {
		return sendArgs;
	}



	public void setSendArgs(String sendArgs) {
		this.sendArgs = sendArgs;
	}



	public String getRecvArgs() {
		return recvArgs;
	}



	public void setRecvArgs(String recvArgs) {
		this.recvArgs = recvArgs;
	}




	public void setBusiid(String busiid) {
		this.busiid = busiid;
	}



	public String getBusiid() {
		return busiid;
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("|channel=[");
		builder.append(channel);
		builder.append("]|busiid=[");
		builder.append(busiid);		
		builder.append("]|interfaceurl=[");
		builder.append(interfaceUrl);
		builder.append("]|sendargs=[");
		builder.append(sendArgs);
		builder.append("]|recvargs=[");
		builder.append(recvArgs);
		builder.append("]");
		return super.toString() + formatLog(builder.toString());
	}
}
