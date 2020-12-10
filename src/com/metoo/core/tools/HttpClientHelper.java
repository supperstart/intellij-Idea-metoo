/**
 * 
 */
package com.metoo.core.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.metoo.core.log.model.InnerInterfaceLogModel;
import com.metoo.core.log.model.LogEnum.Channel;
import com.metoo.core.log.util.InnerInterfaceLogger;
import com.metoo.core.log.util.LoggerUtil;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.kuaidi100.post.HttpRequest;



/**
 * Class Name: HttpClientHelper<br>
 * Description: 类功能说明<br>
 * Sample: 该类的典型使用方法和用例<br>
 * Author: ganzq<br>
 * Date: 2013-9-12<br>
 * --------------------------------------------------<br>
 * 修改人　　　　修改日期　　　　　修改描述<br>
 * ganzq	　　　2013-9-12　　　　　　创建<br>
 * --------------------------------------------------<br>
 * @Version  Ver1.0<br>
 */
public class HttpClientHelper {
	/**
     * 日志.
     */
	private static final Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);
	
	@Autowired  
	private static  HttpServletRequest request; 
	@Autowired
	private IUserConfigService userConfigService;
	
	
	private String getActionUrl(String actionUrl) {
		// return "http://localhost:8080/webportal-interface/face/exec";
		//return "http://134.176.1.146:8083/onlinews/face/exeMethod";
		//return new PropertiesHelper().getProperties("interface.url");
		
		if(actionUrl != null && !"".equals(actionUrl)){
			return actionUrl;
		}else{
			return "http://134.176.1.146:8083/onlinews/face/exeMethod";
		}
		
	}

	
	/**
	 * 由接口服务系统颁发的密钥 key.
	 */
	public final static String key="100127AC9F987F649C853EE84122F99644F824766E881A25";
	
	/**
	 * 系统编码.
	 */
	public final static String sysCode="60000";
	
	/**
	 * 生产平台：http://202.102.109.40:8081/unifiedservice-site/common/exec
	 * 测试平台：http://192.168.180.144:8088/unifiedservice-site/common/exec
	 * 测试itv：http://192.168.180.10:8788/unifiedservice-site/common/exec 不管
	 */
	public final static String url="http://134.176.1.146:8083/onlinews/face/exeMethod";
	
//	public final static String url="http://192.168.180.156:7081/ct10000/pbss/executeNew";
	
	//private static final String url="http://192.168.180.10:8088/ct/pbss/executeNew";
	
	/**
	 * 描述: 调用接口服务的公共方法
	 * @param actionUrl  接口服务地址
	 * @param params  参数
	 * @return<br>
	 * @author：ganzq<br>
	 * @date：2013-9-12<br>
	 * --------------------------------------------------<br>
	 * 修改人　　　　修改日期　　　　　修改描述<br>
	 * ganzq	　　　2013-9-12　　　　　　创建<br>
	 * --------------------------------------------------<br>
	 */
	public static String post(String actionUrl, Map<String, String> params) {
	    BasicHttpParams bp = new BasicHttpParams();
        //HttpConnectionParams.setConnectionTimeout(bp, connectionTimeOut); //超时时间设置
        //HttpConnectionParams.setSoTimeout(bp, soTimeout);
	    bp.setParameter(CoreConnectionPNames.TCP_NODELAY, false); 
	    bp.setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 1024 * 1024);
	    HttpClient httpclient = new DefaultHttpClient(bp);
		HttpPost httpPost = new HttpPost(actionUrl);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
			HttpResponse httpResponse = httpclient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String  result = EntityUtils.toString(httpResponse.getEntity());
				return result;
			}else if(httpResponse.getStatusLine().getStatusCode()==404){
				logger.error("actionUrl:{} not found 404!",actionUrl);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return null;
	}
	
	
	/**
	 * Description: 发送post请求对应地址,并且返回字符串
	 * 
	 * @param actionUrl
	 *            请求地址
	 * @param params
	 *            参数
	 * @return
	 */
	public String postS(HttpServletRequest request,JSONObject json,String actionUrl) {
		String interfaceUrl = "";
		String recv = "";
		try {
			interfaceUrl = getActionUrl(actionUrl);

			// System.out.println("json:" + json.toString());
			BasicHttpParams bp = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(bp, 60000);// 超时时间设置
			HttpClient httpclient = new DefaultHttpClient(bp);

			Map<String, String> params = new HashMap<String, String>();

			params.put("param", json.toString());
			HttpPost httpPost = new HttpPost(interfaceUrl);
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
				list.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));

			}
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
				HttpResponse httpResponse = httpclient.execute(httpPost);
				// System.out.println(httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					recv = EntityUtils.toString(httpResponse.getEntity());
					return recv;
				} else {
					recv = "err::statusCode="
							+ httpResponse.getStatusLine().getStatusCode();
				}
			} catch (Exception e) {
				throw e;
			} finally {
				httpclient.getConnectionManager().shutdown();
			}
		} catch (Exception e) {
			recv = "err::" + e.getMessage();
			throw new RuntimeException(e);
		} finally {
			String busiid = "";
			try {
				busiid = (String) request.getAttribute("_BUSILOGID");
			} catch (Exception e) {

			}
			try {
				InnerInterfaceLogger log = LoggerUtil.getInnerInterfaceLogger();
				InnerInterfaceLogModel logModel = new InnerInterfaceLogModel();
				logModel.setChannel(Channel.WEB.toString());
				logModel.setInterfaceUrl(interfaceUrl);
				logModel.setSendArgs(json == null ? "" : json.toString());
				logModel.setRecvArgs(recv);
				logModel.setBusiid(busiid);
				log.info(HttpClientHelper.class, logModel);
			} catch (Exception e) {

			}
		}
		return null;

	}
	
	
	/**
	 * Description: 发送post请求对应地址,并且返回字符串
	 * 
	 * @param actionUrl
	 *            请求地址
	 * @param params
	 *            参数
	 * @return
	 */
	public String post(JSONObject json,String actionUrl) {
		String interfaceUrl = "";
		String recv = "";
		try {
			interfaceUrl = getActionUrl(actionUrl);

			// System.out.println("json:" + json.toString());
			BasicHttpParams bp = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(bp, 60000);// 超时时间设置
			HttpClient httpclient = new DefaultHttpClient(bp);

			Map<String, String> params = new HashMap<String, String>();

			params.put("param", json.toString());
			HttpPost httpPost = new HttpPost(interfaceUrl);
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
				list.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));

			}
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
				HttpResponse httpResponse = httpclient.execute(httpPost);
				// System.out.println(httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					recv = EntityUtils.toString(httpResponse.getEntity());
					return recv;
				} else {
					recv = "err::statusCode="
							+ httpResponse.getStatusLine().getStatusCode();
				}
			} catch (Exception e) {
				throw e;
			} finally {
				httpclient.getConnectionManager().shutdown();
			}
		} catch (Exception e) {
			recv = "err::" + e.getMessage();
			throw new RuntimeException(e);
		} 
		return null;

	}
	
	
	/**
	 * 
	 * 描述: 返回调用接口的基本参数
	 * 
	 * @param actionCode
	 * @param areaCode
	 * @return
	 * 
	 * @author "huangrougang" date 2011-9-29
	 *         -------------------------------------------------- 修改人 修改日期 修改描述
	 *         "huangrougang" 2011-9-29 创建
	 *         --------------------------------------------------
	 * @throws Exception 
	 * @Version Ver1.0
	 */
	
	public static JSONObject createParam(HttpServletRequest request,String actionCode) throws Exception {
		JSONObject json = new JSONObject();
		try {
			String token = "";
			String busiid = "";
			String sessionid = "";
			String account = "";
			String ip = RequestUtils.getIpAddr(request);
			String user_id = "";
			String CPMIS_provider_code = "";
			try {
				//token = SessionHelper.getInstance().getToken();
				sessionid = request.getSession().getId();
				ip = RequestUtils.getIpAddrAll(request);
				try {
					user_id = "";
				} catch (Exception ex) {
					;
				}
			} catch (Exception e) {
				;
			}
			String serialNumber = UUIDHelper.getSerialNumber();
			json.put("_tokenID", token);
			json.put("actionCode", actionCode);
			json.put("_serialNumber", serialNumber);
			//json.put("_auth", Md5Encrypt.md5(serialNumber+"adfsdfsdffsfsf"));
			json.put("_channel", "wt");
			json.put("_sessionid", sessionid);
			json.put("_user_id", user_id);
			json.put("_ipAddress", ip);
			json.put("_callip", request.getRemoteAddr());
		} catch (Exception e) {
			throw new Exception(e);
		}
		return json;
	}	
	
	
}
