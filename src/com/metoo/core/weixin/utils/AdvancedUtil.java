package com.metoo.core.weixin.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metoo.core.weixin.pojo.SNSUserInfo;
import com.metoo.core.weixin.pojo.WeixinAccessToken;
import com.metoo.core.weixin.pojo.WeixinOauth2Token;

import net.sf.json.JSONObject;

public class AdvancedUtil {

    private static Logger log =  LoggerFactory.getLogger(AdvancedUtil.class);
    
    /**
     * 获取公众号的全局唯一接口调用凭据
     * @param appId
     * @param appSecret
     * @return
     */
    public static WeixinAccessToken getAccessToken(String appId, String appSecret) {
    	WeixinAccessToken wat = null;
    	String requestUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    	requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("APPSECRET", appSecret);
        //获取接口凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        
        System.out.println("普通access——token");
        System.out.println(jsonObject);
        if (null != jsonObject) {
            try {
                wat = new WeixinAccessToken();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("获取网页授权凭证失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return wat;
    }
    
    
	/**
     * 获取网页授权凭证
     * 
     * @param appId 公众账号的唯一标识
     * @param appSecret 公众账号的密钥
     * @param code
     * @return WeixinAouth2Token
     */
    public static WeixinOauth2Token getOauth2AccessToken(String appId, String appSecret, String code) {
        WeixinOauth2Token wat = null;
        
        String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("SECRET", appSecret);
        requestUrl = requestUrl.replace("CODE", code);
        // 获取网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        
        if (null != jsonObject) {
            try {
                wat = new WeixinOauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("获取网页授权凭证失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return wat;
    }
    
    /**
     * 通过全局方式授权获取用户信息
     * 
     * @param accessToken 网页授权接口调用凭证
     * @param openId 用户标识
     * @return SNSUserInfo
     */
	public static SNSUserInfo getSNSUserInfo(String accessToken, String openId) {
        SNSUserInfo snsUserInfo = null;
        // 拼接请求地址
        //http请求方式: GET https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN 
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
        // 通过网页授权获取用户信息
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);

        if (null != jsonObject) {
            try {
	            	System.out.println("+++++++++222+++++++++");
	            	System.out.println(jsonObject.toString());
	         
	            	if(StringUtils.isEmpty(jsonObject.toString())){
	            		return null;
	            	}
            	
	            	//用户是否关注
	                //snsUserInfo.setSubscribe(jsonObject.getString("subscribe"));
	
	                if("0".equals(jsonObject.getString("subscribe").toString())){//用户未关注公众号，无法获取其他信息
	                	log.info("用户未关注公众号，无法获取其他信息。");
	                }else{
	                    snsUserInfo = new SNSUserInfo();
	                	// 用户的标识 
	                    snsUserInfo.setOpenId(jsonObject.getString("openid"));
	                    // 昵称
	                    snsUserInfo.setNickname(jsonObject.getString("nickname"));
	                    // 性别（1是男性，2是女性，0是未知）
	                    snsUserInfo.setSex(jsonObject.getInt("sex"));
	                    // 用户所在国家
	                    snsUserInfo.setCountry(jsonObject.getString("country"));
	                    // 用户所在省份
	                    snsUserInfo.setProvince(jsonObject.getString("province"));
	                    // 用户所在城市
	                    snsUserInfo.setCity(jsonObject.getString("city"));
	                    // 用户头像
	                    snsUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
	                    //用户是否关注
	                    snsUserInfo.setSubscribe(jsonObject.getString("subscribe"));
	                    //用户关注时间
	                    snsUserInfo.setSubscribe_time(jsonObject.getString("subscribe_time"));
	                }
	            } catch (Exception e) {
	                snsUserInfo = null;
	                int errorCode = jsonObject.getInt("errcode");
	                String errorMsg = jsonObject.getString("errmsg");
	                log.error("获取用户信息失败 errcode:{} errmsg:{}", errorCode, errorMsg);
	            }
        }
        return snsUserInfo;
    }
    
	
	/**
     * 通过网页授权获取用户信息
     * by zhuzhi
     * @param accessToken 网页授权接口调用凭证
     * @param openId 用户标识
     * @return SNSUserInfo
     */
	public static SNSUserInfo getSNSUserInfo2(String accessToken, String openId) {
        SNSUserInfo snsUserInfo = null;
        // 拼接请求地址
        String requestUrl = " https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN ";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
        // 通过网页授权获取用户信息
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);

        if (null != jsonObject) {
            try {
	            	System.out.println("+++++++++333+++++++++");
	            	System.out.println(jsonObject.toString());
	         
	            	if(StringUtils.isEmpty(jsonObject.toString())){
	            		return null;
	            	}
        
	                    snsUserInfo = new SNSUserInfo();
	                	// 用户的标识 
	                    snsUserInfo.setOpenId(jsonObject.getString("openid"));
	                    // 昵称
	                    snsUserInfo.setNickname(jsonObject.getString("nickname"));
	                    // 性别（1是男性，2是女性，0是未知）
	                    snsUserInfo.setSex(jsonObject.getInt("sex"));
	                    // 用户所在国家
	                    snsUserInfo.setCountry(jsonObject.getString("country"));
	                    // 用户所在省份
	                    snsUserInfo.setProvince(jsonObject.getString("province"));
	                    // 用户所在城市
	                    snsUserInfo.setCity(jsonObject.getString("city"));
	                    // 用户头像
	                    snsUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
	                    //用户是否关注
	                    snsUserInfo.setSubscribe("0");
	                    //用户关注时间
	                    snsUserInfo.setSubscribe_time("100");//表示用户当前没有关注公众号，不管以前是否关注过

	            } catch (Exception e) {
	                snsUserInfo = null;
	                int errorCode = jsonObject.getInt("errcode");
	                String errorMsg = jsonObject.getString("errmsg");
	                log.error("获取用户信息失败 errcode:{} errmsg:{}", errorCode, errorMsg);
	            }
        }
        System.out.println("....snsuserInfo : "+snsUserInfo +"....zzzz2");
        return snsUserInfo;
    }
	
	
	
    public static WeixinOauth2Token refreshToken(String appid ,String refresh_token){
    	WeixinOauth2Token wat = null;
    	String refreshUrl = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
    	refreshUrl = refreshUrl.replace("APPID", appid);
    	refreshUrl = refreshUrl.replace("REFRESH_TOKEN", refresh_token);
    	
    	// 刷新access_token
        JSONObject jsonObject = CommonUtil.httpsRequest(refreshUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new WeixinOauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("刷新网页授权凭证失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return wat;
    }
}
