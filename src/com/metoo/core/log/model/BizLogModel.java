package com.metoo.core.log.model;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.enterprise.deploy.shared.ActionType;

import com.metoo.core.log.model.LogEnum.Channel;

/**
 * 业务日志内容对象
 * 
 * @author hjw
 */
public class BizLogModel extends BaseLogModel
{

	private static final long serialVersionUID = -1098023272916318838L;

	private String channel = "";// 渠道

	private String busiId = "";// 业务操作唯一标识
	private String busiCode = "";// 业务代码
	private String busiName = "";// 业务名称
	private String busiOperateTime = "";// 业务处理时间
	private String actionClass = "";// 业务处理类
	private String actionMethod = "";// 业务方法
	private String actionType = "";// 业务运作
	private String messageInfo = "";// 异常信息
	private Map<String, String> actionAttr = new LinkedHashMap<String, String>();// 业务相关属性值

	private String operateTime =  "";

	/**
	 * @return the accesstime
	 */
	public String getOperateTime()
	{
		return operateTime;
	}

	/**
	 * @param accesstime the accesstime to set
	 */
	public void setOperateTime(String accesstime)
	{
		this.operateTime = accesstime;
	}
	protected String getChannel()
	{
		return channel;
	}

	/**
	 * 设置日志产生的渠道
	 * 
	 * @param channel
	 *        渠道类型
	 */
	public void setChannel(Channel channel)
	{
		this.channel = channel.toString();
	}

	protected String getActionType()
	{
		return actionType;
	}

	/**
	 * 设置业务动作
	 * 
	 * @param actionType
	 *        动作类型
	 */
	public void setActionType(ActionType actionType)
	{
		this.actionType = actionType.toString();
	}

	protected Map<String, String> getActionAttr()
	{
		return actionAttr;
	}

	protected void setActionAttr(Map<String, String> actionAttr)
	{
		this.actionAttr = actionAttr;
	}

	/**
	 * 设置业务事件中的属性值
	 * 
	 * @param key
	 *        属性名
	 * @param value
	 *        属性值
	 */
	public void setAttributes(String key, String value)
	{
		this.actionAttr.put(key, value);
	}

	/**
	 * @return the busiId
	 */
	public String getBusiId()
	{
		return busiId;
	}

	/**
	 * @param busiId
	 *        the busiId to set
	 */
	public void setBusiId(String busiId)
	{
		this.busiId = busiId;
	}

	/**
	 * @return the busiCode
	 */
	public String getBusiCode()
	{
		return busiCode;
	}

	/**
	 * @param busiCode
	 *        the busiCode to set
	 */
	public void setBusiCode(String busiCode)
	{
		this.busiCode = busiCode;
	}

	/**
	 * @return the busiName
	 */
	public String getBusiName()
	{
		return busiName;
	}

	/**
	 * @param busiName
	 *        the busiName to set
	 */
	public void setBusiName(String busiName)
	{
		this.busiName = busiName;
	}

	/**
	 * @return the busiOperateTime
	 */
	public String getBusiOperateTime()
	{
		return busiOperateTime;
	}

	/**
	 * @param busiOperateTime
	 *        the busiOperateTime to set
	 */
	public void setBusiOperateTime(String busiOperateTime)
	{
		this.busiOperateTime = busiOperateTime;
	}

	/**
	 * @return the actionClass
	 */
	public String getActionClass()
	{
		return actionClass;
	}

	/**
	 * @param actionClass
	 *        the actionClass to set
	 */
	public void setActionClass(String actionClass)
	{
		this.actionClass = actionClass;
	}

	/**
	 * @return the actionMethod
	 */
	public String getActionMethod()
	{
		return actionMethod;
	}

	/**
	 * @param actionMethod
	 *        the actionMethod to set
	 */
	public void setActionMethod(String actionMethod)
	{
		this.actionMethod = actionMethod;
	}

	/**
	 * @return the messageInfo
	 */
	public String getMessageInfo()
	{
		return messageInfo;
	}

	/**
	 * @param messageInfo
	 *        the messageInfo to set
	 */
	public void setMessageInfo(String messageInfo)
	{
		this.messageInfo = messageInfo;
	}



	/**
	 * @param channel
	 *        the channel to set
	 */
	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	/**
	 * @param actionType
	 *        the actionType to set
	 */
	public void setActionType(String actionType)
	{
		this.actionType = actionType;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("|channel=[");
		builder.append(channel);
		builder.append("]|busiid=[");
		builder.append(busiId);
		builder.append("]|busicode=[");
		builder.append(busiCode);
		builder.append("]|businame=[");
		builder.append(busiName);
		builder.append("]|busioperatetime=[");
		builder.append(busiOperateTime);
		builder.append("]|actiontype=[");
		builder.append(actionType);
		builder.append("]|actionclass=[");
		builder.append(actionClass);
		builder.append("]|actionmethod=[");
		builder.append(actionMethod);
		builder.append("]|messageinfo=[");
		builder.append(messageInfo);
		builder.append("]|operatortime=[");
		builder.append(operateTime);
		builder.append("]");
		return super.toString() + formatLog(builder.toString());

	}

}
