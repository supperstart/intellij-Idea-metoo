package com.metoo.app.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.buyer.domain.SOAPUtils;
import com.metoo.app.pojo.Ddu;
import com.metoo.app.v1.manage.buyer.action.MAddressBuyerAction;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreStatService;
import com.metoo.foundation.service.ISystemTipService;
import com.metoo.foundation.service.ITransportService;
import com.metoo.foundation.service.IUserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class DduWebService {
	@Autowired 
	private MAddressBuyerAction add;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ITransportService transportService;
	@Autowired 
	private IUserService userService;
	@Autowired 
	private IEvaluateService evaluateService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IStoreStatService storeStatService;
	@Autowired
	private ISystemTipService systemTipService;
	@Autowired
	private IOrderFormService orderService;
	
	@RequestMapping("/ddu-express.json")
	public void xml() throws Exception {

		// TODO Auto-generated method stub
		Map params = new HashMap();
		params.put("update", 1);
		List<OrderForm> orderForms = this.orderService.query("select obj from OrderForm obj where obj.update_status=:update", params, -1, 10);
		if(orderForms.size() > 0){
			for(OrderForm order : orderForms){
				if(order.getOrder_status() == 30 && order.getUpdate_status() == 1){
					Map express_map = Json.fromJson(Map.class,
							order.getExpress_info());
					//调用ddu
					String sendMsg = appendXmlContextTow("DDU9239348", "E00030");//拼接请求报文
					 InputStreamReader isr = null;// 开启HTTP连接
				     BufferedReader inReader = null;
				     StringBuffer result = null;
				     OutputStream outObject = null;
				    
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
			            String xmlResult = result.toString().replace("<", "<");
				        //报文返回状态码，1表示正常
				        String responseCode = SOAPUtils.getXmlMessageByName(xmlResult, "responseCode");
				        
				        if("1".equals(responseCode)){
				        	JSONArray jsonArray = dom4jXml(xmlResult);
				        	if(jsonArray != null){
				        		List<String> str = new ArrayList<String>();
				        		for(Object obj : jsonArray){
				                    JSONObject jo = (JSONObject) obj;
				                    String status = jo.get("status").toString();
				                    str.add(status);
				        		}
				        		if(str.contains("DELIVERED")){
				        			order.setUpdate_status(-1);
				        		}else{
				        			order.setUpdate_status(2);
				        		}
				        	}
				        	order.setLogistics_info(jsonArray.toString());
				        	this.orderService.update(order);
				        }
				}
			}
		}else{
			params.clear();
			params.put("update", 2);
			orderForms = this.orderService.query("select obj from OrderForm obj where obj.update_status=:update", params, -1, -1);
			if(orderForms.size() > 0){
				for(OrderForm order : orderForms){
					order.setUpdate_status(1);
					this.orderService.update(order);
				}
			}
			
		}
	
	}
	
	public static JSONArray dom4jXml(String interfaceData) throws Exception{
        List<Element> elementList = null;
        List<Ddu> Ddus = null;
        try {
            SAXReader sr = new SAXReader();
            Document document = sr.read(new ByteArrayInputStream(interfaceData.getBytes()));

            Element root = document.getRootElement();
            elementList = root.elements();
            Ddus = new ArrayList();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        for (Element e : elementList) {
            // 解析标签下一级标签
            Element e1 = e.element("GetStatusDetailsResponse");
            Element e2 = e1.element("return");
            Element e3 = e2.element("responseArray");
            List<Element> elementList1 = e3.elements();
            for (Element el:elementList1){
                Ddu ddu = new Ddu();
                ddu.setTime(el.elementText("SDate"));
                ddu.setStatus(el.elementText("CStatus"));
                ddu.setLocation(el.elementText("SLocation"));
                ddu.setDetails(el.elementText("SDetails"));
                Ddus.add(ddu);
            }
        }
        	return JSONArray.fromObject(Ddus);
    }
	
	
	/**
     * @methodsName appendXmlContextTow
     * @description 拼接请求报文
     * @param ddutaskRequest
     * @return String
     */
    private static String appendXmlContextTow(String BookingNumber, String CompanyCode) {
        // 构建请求报文
    	List<String> list  = new ArrayList<String>();
        StringBuffer stringBuffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://com.foresee.top.service/\">\n" +
                "  <soapenv:Body>\n" +
                "  <GetStatusDetails>\n" +
                "  <SaveProductResult>\n" +
                "    <BookingNumber>" + BookingNumber + "</BookingNumber>\n" +
                "    <CompanyCode>" + CompanyCode + "</CompanyCode>\n" +
                "  </SaveProductResult>\n" +
                "  </GetStatusDetails>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>");
        return stringBuffer.toString();
    }
}
