package com.metoo.core.weixin.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.core.weixin.utils.PayCommonUtil;

@Controller
@RequestMapping(value = "/api")
public class WeiXinPayController {
    protected static Logger logger = Logger.getLogger(WeiXinPayController.class);
    
    String timeMillis = String.valueOf(System.currentTimeMillis() / 1000);
    String randomString = PayCommonUtil.getRandomString(32);
    public static String wxnotify = "/api/json/money/wxpay/succ";
    
    /**
     * @param totalAmount    支付金额
     * @param description    描述
     * @param request
     * @return
     */
    @RequestMapping(value = "/weixin/weixinPay/{totalAmount}/{description}/{openId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody    
    public SortedMap<String, Object> ToPay(@PathVariable String sn,@PathVariable BigDecimal totalAmount,@PathVariable String description,@PathVariable String openId,HttpServletRequest request) {
        String sym = request.getRequestURL().toString().split("/api/")[0];
        Long userId = 0L;
        String trade_no0 = userId + "O" + UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
        String trade_no = trade_no0.substring(0,32);
        Map<String, String> map = weixinPrePay(trade_no,totalAmount,description,openId,sym,request);  
        SortedMap<String, Object> finalpackage = new TreeMap<String, Object>();
       // finalpackage.put("appId", PayCommonUtil.APPID);  
        finalpackage.put("timeStamp", timeMillis);  
        finalpackage.put("nonceStr", randomString);  
        finalpackage.put("package", "prepay_id="+map.get("prepay_id"));
        finalpackage.put("signType", "MD5");
//        String sign = PayCommonUtil.createSign("UTF-8", finalpackage);  
//        finalpackage.put("paySign", sign);
        return finalpackage;
    } 
    
    @SuppressWarnings("unchecked")
    public Map<String, String> weixinPrePay(String trade_no,BigDecimal totalAmount,  
            String description, String openid,String sym, HttpServletRequest request) { 
        SortedMap<String, Object> parameterMap = new TreeMap<String, Object>();  
//        parameterMap.put("appid", PayCommonUtil.APPID);  
//        parameterMap.put("mch_id", PayCommonUtil.MCH_ID);  
        parameterMap.put("nonce_str", randomString);  
        parameterMap.put("body", description);
        parameterMap.put("out_trade_no", trade_no);
        parameterMap.put("fee_type", "CNY");  
        System.out.println("jiner");  
        BigDecimal total = totalAmount.multiply(new BigDecimal(100));  
        java.text.DecimalFormat df=new java.text.DecimalFormat("0");  
        parameterMap.put("total_fee", df.format(total));  
        System.out.println("jiner2");  
        parameterMap.put("spbill_create_ip", request.getRemoteAddr());  
        parameterMap.put("notify_url", sym + wxnotify);
        parameterMap.put("trade_type", "JSAPI");
        //trade_type为JSAPI是 openid为必填项
        parameterMap.put("openid", openid);
        System.out.println("");  
//        String sign = PayCommonUtil.createSign("UTF-8", parameterMap); 
//        System.out.println("jiner2");  
//        parameterMap.put("sign", sign);  
        String requestXML = PayCommonUtil.getRequestXml(parameterMap);  
        System.out.println(requestXML);  
        String result = PayCommonUtil.httpsRequest(  
                "https://api.mch.weixin.qq.com/pay/unifiedorder", "POST",  
                requestXML);  
        System.out.println(result);  
        Map<String, String> map = null;  
        try {  
            map = PayCommonUtil.doXMLParse(result);  
        } catch (JDOMException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return map;        
    }

    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/json/money/wxpay/succ",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String wxpaySucc(HttpServletRequest request) throws IOException {
        System.out.println("微信支付回调");
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        String resultxml = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> params = new HashMap<String, String>();
		try {
			params = PayCommonUtil.doXMLParse(resultxml);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        outSteam.close();
        inStream.close();
        if (!PayCommonUtil.isTenpaySign(params)) {
            // 支付失败
            return "fail";
        } else {
            System.out.println("===============付款成功==============");
            // ------------------------------
            // 处理业务开始
            // ------------------------------
            // 此处处理订单状态，结合自己的订单数据完成订单状态的更新
            // ------------------------------

            String total_fee = params.get("total_fee");
            double v = Double.valueOf(total_fee) / 100;
            Long userId = Long.valueOf(params.get("out_trade_no").split("O")[0]);
            //更新
            //updateUserPay(userId, String.valueOf(v));

            // 处理业务完毕
            // ------------------------------
            return "success";
        }
    }
    
}