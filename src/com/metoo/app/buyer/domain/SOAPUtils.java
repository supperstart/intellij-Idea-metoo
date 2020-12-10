package com.metoo.app.buyer.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.stereotype.Component;

import com.metoo.ddu.pojo.DduTaskRequest;
/**
 * <p>
 * Title: SOAPUtils.java
 * </p>
 * 
 * <p>
 * Description:
 * 系统物流工具类，使用该工具类获取该订单对应的物流信息，获取物流信息 拼接请求报文
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: metoo 
 * </p>
 * 
 * @author hu
 * 
 * @date 2019-10-10
 * 
 * @version metoo_b2b2c v1.0 2019版
 *
 */
@Component
public class SOAPUtils {
	
	/**
	 * @methodsName WebServiceTow
	 * @description 模拟soapUI调用WebService,解析返回报文
	 * @param ddutaskRequest ddu
	 * Created by PengHongfu 2018-04-26 15:36
	 */
    public static StringBuffer webServiceTow(DduTaskRequest ddutaskRequest) throws Exception {
        
        String sendMsg = appendXmlContextTow(ddutaskRequest);//拼接请求报文
        InputStreamReader isr = null;// 开启HTTP连接
        BufferedReader inReader = null;
        StringBuffer result = null;
        OutputStream outObject = null;
        try {
            URL url = new URL("http://courier.ddu-express.com/api/webservice.php?wsdl");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection(); //[JDK网络类Java.net.HttpURLConnection]
            //设置请求方式默认为GET
            httpConn.setRequestMethod("POST");
           /**
            *  设置请求头
            *  请求协议(此处是http)生成的URLConnection类 的子类HttpURLConnection,故此处最好将其转化为HttpURLConnection类型的对象,以便用到HttpURLConnection更多的API.如下: HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection; ]
    		*  设置HTTP请求相关信息
     	    *  设定传送的内容类型是可序列化的java对象 
            *  如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException) 
            */
            httpConn.setRequestProperty("Content-Length",
                    String.valueOf(sendMsg.getBytes().length));
            httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            httpConn.setDoOutput(true);//设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在http正文内，因此需要设为true, 默认情况下是false;
            httpConn.setDoInput(true); //设置是否从httpUrlConnection读入，默认情况下是true;
            outObject = httpConn.getOutputStream();// 进行HTTP请求,此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法， 所以在开发中不调用上述的connect()也可以)
            outObject.write(sendMsg.getBytes());
            if (200 != (httpConn.getResponseCode())) {
                throw new Exception("HTTP Request is not success, Response code is " + httpConn.getResponseCode());
            }
           
            isr = new InputStreamReader(
                    httpConn.getInputStream(), "utf-8"); // 获取HTTP响应流（响应数据）
            inReader = new BufferedReader(isr);
            result = new StringBuffer();
            String inputLine;
            while ((inputLine = inReader.readLine()) != null) {
                result.append(inputLine);
            }
            return result;
 
        } catch (IOException e) {
            throw e;
        } finally {
            // 关闭输入流
            if (inReader != null) {
                inReader.close();
            }
            if (isr != null) {
                isr.close();
            }
            // 关闭输出流
            if (outObject != null) {
                outObject.close();
            }
        }
 
    }
    
    /**
     * @methodsName appendXmlContextTow
     * @description 拼接请求报文
     * @param ddutaskRequest
     * @return String
     */
    private static String appendXmlContextTow(DduTaskRequest ddutaskRequest) {
        // 构建请求报文
    	List<String> list  = new ArrayList<String>();
    	list.add("123");
        StringBuffer stringBuffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://com.foresee.top.service/\">\n" +
                "  <soapenv:Body>\n" +
                "  <CustomertToCustomerBooking>\n" +
                "  <SaveProductResult>\n" +
                "    <BatchNumber>" + ddutaskRequest.getBatchNumber() + "</BatchNumber>\n" +
                "    <FromCompany>" + ddutaskRequest.getFromCompany() + "</FromCompany>\n" +
                "    <FromAddress>" + ddutaskRequest.getFromAddress() + "</FromAddress>\n" +
                "    <FromLocation>" + ddutaskRequest.getFromLocation() + "</FromLocation>\n" +
                "    <FromCountry>" + ddutaskRequest.getFromCountry() + "</FromCountry>\n" +
                "    <FromCperson>" + ddutaskRequest.getFromCperson() + "</FromCperson>\n" +
                "    <FromContactno>" + ddutaskRequest.getFromContactno() + "</FromContactno>\n" +
                "    <FromMobileno>" + ddutaskRequest.getFromMobileno() + "</FromMobileno>\n" +
                "    <ToAddress>" + ddutaskRequest.getToAddress() + "</ToAddress>\n" +
                "    <ToLocation>" + ddutaskRequest.getToLocation() + "</ToLocation>\n" +
                "    <ToCountry>" + ddutaskRequest.getToCountry() + "</ToCountry>\n" +
                "    <ToCperson>" + ddutaskRequest.getToCperson() + "</ToCperson>\n" +
                "    <ToContactno>" + ddutaskRequest.getToContactno() + "</ToContactno>\n" +
                "    <ToMobileno>" + ddutaskRequest.getToMobileno() + "</ToMobileno>\n" +
                "    <ReferenceNumber>" + ddutaskRequest.getReferenceNumber() + "</ReferenceNumber>\n" +
                "    <CompanyCode>" + ddutaskRequest.getCompanyCode() + "</CompanyCode>\n" +
                "    <Pieces>" +ddutaskRequest.getPieces() + "</Pieces>\n" +
                "    <PackageType>" + ddutaskRequest.getPackageType() + "</PackageType>\n" +
                "    <CurrencyCode>" + ddutaskRequest.getCurrencyCode() + "</CurrencyCode>\n" +
                "    <NcndAmount>" + ddutaskRequest.getNcndAmount() + "</NcndAmount>\n" +
                "    <Weight>" + ddutaskRequest.getWeight() + "</Weight>\n" +
                "    <ItemDescription>" + ddutaskRequest.getItemDescription() + "</ItemDescription>\n" +
                "    <SpecialInstruction>" + ddutaskRequest.getSpecialInstruction() + "</SpecialInstruction>\n" +
                "  </SaveProductResult>\n" +
                "  </CustomertToCustomerBooking>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>");
        return stringBuffer.toString();
    }
 
    /**
     * @methodsName getXmlMessageByName
     * @description 解析报文，根据末节点名称获取值
     * @param xmlResult
     * @param nodeName
     * @return String
     * @throws DocumentException
     */
    public static String getXmlMessageByName(String xmlResult, String nodeName) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlResult);
        DefaultXPath xPath = new DefaultXPath("//" + nodeName);
        xPath.setNamespaceURIs(Collections.singletonMap("ns1", "http://cn.gov.chinatax.gt3nf.nfzcpt.service/"));
        List list = xPath.selectNodes(doc);
        if (!list.isEmpty() && list.size() > 0) {
            Element node = (Element) list.get(0);
            return node.getText();
        }
        return "";
    }
}

