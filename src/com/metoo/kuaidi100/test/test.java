package com.metoo.kuaidi100.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.metoo.core.tools.Md5Encrypt;

public class test {

	public static void main(String[] args) {
		String content = srearchKuaidiInfo("yunda", "1202443176364");
		System.out.println(content);
	}

	public static String srearchKuaidiInfo(String com,String nu){
		String content = "";
		StringBuffer sb = new StringBuffer();
		try {
			/*sb.append("http://www.kuaidi100.com/applyurl?key=CNbeXYJm2595");
			sb.append("&com=").append(com);
			sb.append("&nu=").append(nu);*/
			
			String query_url = "https://poll.kuaidi100.com/poll/query.do"
					+"?customer=EF91A38461385824F6FB14D0C594E54E"
					+"&param={'com':'yunda','num':'1202443176364','from':'','to':''}"
					+"&sign=5E74B3648D03E98BDDF1CC865D22EE18";
			URL url = new URL(query_url);
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			InputStream urlStream = url.openStream();
			byte b[] = new byte[10000];
			int numRead = urlStream.read(b);
			content = new String(b, 1, numRead);
			while (numRead != -1) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					// String newContent = new String(b, 1, numRead);
					//String newContent = new String(b, 0, numRead, "UTF-8");
					String newContent = new String(b, 0, numRead, "UTF-8");
					content += newContent;
				}
			}
			
			urlStream.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return content;
	}
}
