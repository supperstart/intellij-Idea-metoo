package com.metoo.core.weixin.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.wechat.WeixinTemplate;

import net.sf.json.JSONObject;

public class WeixinUtil {

    /**
    * 方法名：httpRequest</br>
    * 详述：发送http请求</br>
    * 开发人员：souvc </br>
    * 创建时间：2016-1-5  </br>
    * @param requestUrl
    * @param requestMethod
    * @param outputStr
    * @return 说明返回值含义
    * @throws 说明发生此异常的条件
     */
    public static JSONObject httpRequest(String requestUrl,String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestMethod(requestMethod);
            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    
    /**
    * 方法名：getWxConfig</br>
    * 详述：获取微信的配置信息 </br>
    * 开发人员：souvc  </br>
    * 创建时间：2016-1-5  </br>
    * @param sysConfig
    * @return 说明返回值含义
    * @throws 说明发生此异常的条件
     */
    public static Map<String, Object> getWxConfig(HttpServletRequest request, SysConfig config) {
    	
        Map<String, Object> ret = new HashMap<String, Object>();
		try {
	    	
	    	String requestUrl = request.getScheme()+"://"; //请求协议 http 或 https  
	    	requestUrl+=request.getHeader("host"); // 请求服务器  
	    	requestUrl+=request.getRequestURI();// 工程名    
	    	 if(request.getQueryString()!=null) //判断请求参数是否为空
	    		 requestUrl+="?"+request.getQueryString();// 参数 
	    	
			String appId =config.getWeixin_appId(); // 必填，公众号的唯一标识
			String access_token = config.getWeixin_token();
			String jsapi_ticket = config.getWeixin_jsapi_ticket();
      
			String timestamp = Long.toString(System.currentTimeMillis() / 1000); // 必填，生成签名的时间戳
			String nonceStr = UUID.randomUUID().toString(); // 必填，生成签名的随机串
			String url = "";
			
			JSONObject json = new JSONObject();
			
			if (StringUtils.isEmpty(jsapi_ticket) && !StringUtils.isEmpty(access_token)) {
			    url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+ access_token + "&type=jsapi";
			    json = WeixinUtil.httpRequest(url, "GET", null);
			    
			    if (json != null) {
			        jsapi_ticket = json.getString("ticket");
			    }
			}
			String signature = "";
			// 注意这里参数名必须全部小写，且必须有序
			String sign = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonceStr+ "&timestamp=" + timestamp + "&url=" + requestUrl;
			System.out.println("....................");
			System.out.println("sign "+sign);
			
			try {
			    MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			    crypt.reset();
			    crypt.update(sign.getBytes("UTF-8"));
			    signature = byteToHex(crypt.digest());
			    System.out.println("signature : "+signature);
			} catch (NoSuchAlgorithmException e) {
			    e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
			    e.printStackTrace();
			}
			ret.put("appId", appId);
			ret.put("timestamp", timestamp);
			ret.put("nonceStr", nonceStr);
			ret.put("signature", signature);
			ret.put("jsapi_ticket", jsapi_ticket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ret;
    }

    
    /**
    * 方法名：byteToHex</br>
    * 详述：字符串加密辅助方法 </br>
    * 开发人员：souvc  </br>
    * 创建时间：2016-1-5  </br>
    * @param hash
    * @return 说明返回值含义
    * @throws 说明发生此异常的条件
     */
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;

    }
    
    public static boolean sendTemplateMsg(String token,WeixinTemplate template){
		
		boolean flag=false;
		
		String requestUrl="https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
		requestUrl=requestUrl.replace("ACCESS_TOKEN", token);
	
		JSONObject jsonResult=CommonUtil.httpsRequest(requestUrl, "POST", template.toJSON());
		System.out.println("++++++++++++++++++++");
		System.out.println(jsonResult);
		if(jsonResult!=null){
			int errorCode=jsonResult.getInt("errcode");
			String errorMessage=jsonResult.getString("errmsg");
			if(errorCode==0){
				flag=true;
			}else{
				System.out.println("模板消息发送失败:"+errorCode+","+errorMessage);
				flag=false;
			}
		}
		return flag;
	}
}