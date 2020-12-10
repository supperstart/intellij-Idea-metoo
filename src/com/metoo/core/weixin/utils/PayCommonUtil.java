package com.metoo.core.weixin.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class PayCommonUtil {

	// 随机字符串生成
	public static String getRandomString(int length) { // length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	// 请求xml组装
	public static String getRequestXml(SortedMap<String, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (!"sign".equals(key)) {
				if ("attach".equalsIgnoreCase(key)
						|| "body".equalsIgnoreCase(key)) {
					sb.append("<" + key + ">" + "<![CDATA[" + value + "]]></"
							+ key + ">");
				} else {
					sb.append("<" + key + ">" + value + "</" + key + ">");
				}
			}

		}
		sb.append("<sign>" + "<![CDATA[" + parameters.get("sign")
				+ "]]></sign>");
		sb.append("</xml>");
		try {
			return new String(sb.toString().getBytes(), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 生成签名
	public static String createSign(String characterEncoding,
			SortedMap<String, Object> parameters, String key) throws Exception {
		StringBuffer sb = new StringBuffer();
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}

		System.out.println(".............");
		System.out.println(key);
		sb.append("key=" + key);
		System.out.println(sb.toString());
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding)
				.toUpperCase();
		return sign;
	}

	// 生成签名
	public static String seconedCreateSign(String characterEncoding,
			SortedMap<String, Object> parameters) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("appId=").append(parameters.get("appId").toString().trim());
		sb.append("&nonceStr=").append(
				parameters.get("nonceStr").toString().trim());
		sb.append("&package=").append(
				parameters.get("package").toString().trim());
		sb.append("&signType=").append(
				parameters.get("signType").toString().trim());
		sb.append("&timeStamp=").append(
				parameters.get("timeStamp").toString().trim());
		// sb.append("&key=").append(parameters.get("key").toString().trim());

		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding)
				.toUpperCase();
		return sign;
	}

	/**
	 * 验证回调签名
	 * 
	 * @param packageParams
	 * @param key
	 * @param charset
	 * @return
	 */
	public static boolean isTenpaySign(Map<String, String> map) {
		String characterEncoding = "utf-8";
		String signFromAPIResponse = map.get("sign");
		if (signFromAPIResponse == null || signFromAPIResponse.equals("")) {
			System.out.println("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
			return false;
		}
		System.out.println("服务器回包里面的签名是:" + signFromAPIResponse);
		// 过滤空 设置 TreeMap
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		for (String parameter : map.keySet()) {
			String parameterValue = map.get(parameter);
			String v = "";
			if (null != parameterValue) {
				v = parameterValue.trim();
			}
			packageParams.put(parameter, v);
		}

		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + packageParams.get("key").toString());

		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较

		// 算出签名
		String resultSign = "";
		String tobesign = sb.toString();

		resultSign = MD5Util.MD5Encode(tobesign, characterEncoding)
				.toUpperCase();

		String tenpaySign = ((String) packageParams.get("sign")).toUpperCase();
		return tenpaySign.equals(resultSign);
	}

	// 请求方法
	public static String httpsRequest(String requestUrl, String requestMethod,
			String outputStr) {
		try {

			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("content-type",
					"application/x-www-form-urlencoded");
			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 从输入流读取返回内容
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			// 释放资源
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			conn.disconnect();
			return buffer.toString();
		} catch (ConnectException ce) {
			System.out.println("连接超时：{}" + ce);
		} catch (Exception e) {
			System.out.println("https请求异常：{}" + e);
		}
		return null;
	}

	// //退款的请求方法
	// public static String httpsRequest2(String requestUrl, String
	// requestMethod, String outputStr) throws Exception {
	// KeyStore keyStore = KeyStore.getInstance("PKCS12");
	// StringBuilder res = new StringBuilder("");
	// FileInputStream instream = new FileInputStream(new
	// File("/home/apiclient_cert.p12"));
	// try {
	// keyStore.load(instream, "".toCharArray());
	// } finally {
	// instream.close();
	// }
	//
	// // Trust own CA and all self-signed certs
	// SSLContext sslcontext = SSLContexts.custom()
	// .loadKeyMaterial(keyStore, "1313329201".toCharArray())
	// .build();
	// // Allow TLSv1 protocol only
	// SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	// sslcontext,
	// new String[] { "TLSv1" },
	// null,
	// SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	// CloseableHttpClient httpclient = HttpClients.custom()
	// .setSSLSocketFactory(sslsf)
	// .build();
	// try {
	//
	// HttpPost httpost = new
	// HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
	// httpost.addHeader("Connection", "keep-alive");
	// httpost.addHeader("Accept", "*/*");
	// httpost.addHeader("Content-Type",
	// "application/x-www-form-urlencoded; charset=UTF-8");
	// httpost.addHeader("Host", "api.mch.weixin.qq.com");
	// httpost.addHeader("X-Requested-With", "XMLHttpRequest");
	// httpost.addHeader("Cache-Control", "max-age=0");
	// httpost.addHeader("User-Agent",
	// "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
	// StringEntity entity2 = new StringEntity(outputStr ,Consts.UTF_8);
	// httpost.setEntity(entity2);
	// System.out.println("executing request" + httpost.getRequestLine());
	//
	// CloseableHttpResponse response = httpclient.execute(httpost);
	//
	// try {
	// HttpEntity entity = response.getEntity();
	//
	// System.out.println("----------------------------------------");
	// System.out.println(response.getStatusLine());
	// if (entity != null) {
	// System.out.println("Response content length: " +
	// entity.getContentLength());
	// BufferedReader bufferedReader = new BufferedReader(new
	// InputStreamReader(entity.getContent()));
	// String text = "";
	// res.append(text);
	// while ((text = bufferedReader.readLine()) != null) {
	// res.append(text);
	// System.out.println(text);
	// }
	//
	// }
	// EntityUtils.consume(entity);
	// } finally {
	// response.close();
	// }
	// } finally {
	// httpclient.close();
	// }
	// return res.toString();
	//
	// }

	// xml解析
	public static Map doXMLParse(String strxml) throws JDOMException,
			IOException {
		strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

		if (null == strxml || "".equals(strxml)) {
			return null;
		}

		Map m = new HashMap();

		InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if (children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = getChildrenText(children);
			}

			m.put(k, v);
		}

		// 关闭流
		in.close();

		return m;
	}

	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if (!children.isEmpty()) {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if (!list.isEmpty()) {
					sb.append(getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		String appId = "wx230ecfa4c5bdb96d";
		String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
		String nonceStr = PayCommonUtil.getRandomString(32);
		String packageValue = "perpay_id=wx2017052114370048c8e3ce60120332209";
		String signType = "MD5";
		String key = "XqsTxIsBkoePHCKLPNT225AwtzENMWtK";

		StringBuffer sb = new StringBuffer();
		sb.append("appId=").append("wx230ecfa4c5bdb96d");
		sb.append("&timeStamp=").append(timeStamp);
		sb.append("&nonceStr=").append(nonceStr);
		sb.append("&package=").append(packageValue);
		sb.append("&signType=").append(signType);
		sb.append("&key=").append(key);

		String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();

		System.out.println("时间戳 ： " + timeStamp);
		System.out.println("随机串 ：" + nonceStr);
		System.out.println("微信签名 ：" + sign);
	}

	/**
	 * SSL POST
	 * @param url
	 * @param body
	 * @param certificatePath
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static String executeBySslPost(String url, String body,
			String certificatePath, String password) throws Exception {
		String result = ""; // 商户id //指定读取证书格式为PKCS12
		KeyStore keyStore = KeyStore.getInstance("PKCS12"); // 读取本机存放的PKCS12证书文件
		FileInputStream instream = new FileInputStream(
				new File(certificatePath));
		try { // 指定PKCS12的密码(商户ID)
			keyStore.load(instream, password.toCharArray());
		} finally {
			instream.close();
		}
		SSLContext sslcontext = SSLContexts.custom()
				.loadKeyMaterial(keyStore, password.toCharArray()).build(); // 指定TLS版本
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER); // 设置httpclient的SSLSocketFactory
		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf).build();
		try {
			HttpPost httppost = new HttpPost(url);
			StringEntity reqEntity = new StringEntity(body, "UTF-8");
			httppost.setEntity(reqEntity);
			System.out.println("Executing request: "
					+ httppost.getRequestLine());
			CloseableHttpResponse response = null;
			try {
				response = httpclient.execute(httppost);
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}