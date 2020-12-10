package com.metoo.core.weixin;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.metoo.core.weixin.pojo.SNSUserInfo;
import com.metoo.core.weixin.pojo.WeixinAccessToken;
import com.metoo.core.weixin.pojo.WeixinOauth2Token;
import com.metoo.core.weixin.utils.AdvancedUtil;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.service.ISysConfigService;

/**
* 类名: OAuthServlet </br>
* 描述: 授权后的回调请求处理 </br>
* 发布版本：V1.0  </br>
 */
public class OAuthServlet extends ServletProxy  {
    private static final long serialVersionUID = -1847238807216447030L;
    private String appId;
    private String appSecret;
    
    @Autowired
    private ISysConfigService configService;
    	
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        try {
			HttpSession session = request.getSession();
			// 用户同意授权后，能获取到code
			String code = request.getParameter("code");
			SysConfig configs = configService.getSysConfig();

			ServletConfig config = this.getServletConfig();
			appId = config.getInitParameter("appId");
			appSecret = config.getInitParameter("appSecret");

			SNSUserInfo snsUserInfo  = null;
			// 用户同意授权
			if (!"authdeny".equals(code)) {
			    // 获取网页授权access_token,仅获取用户信息时使用，无限制
			    WeixinOauth2Token weixinOauth2Token = AdvancedUtil.getOauth2AccessToken(appId, appSecret, code);
			    // 用户标识
			    String openId = weixinOauth2Token.getOpenId();

			    //获取公众号的全局唯一接口调用凭据，
			    String token = configs.getWeixin_token();
			    if(StringUtils.isEmpty(token)){//数据库中的token为空时 
					WeixinAccessToken wot = AdvancedUtil.getAccessToken(appId, appSecret);
					token = wot.getAccessToken();
					configs.setWeixin_token(wot.getAccessToken());
					configService.update(configs);
			    }
			    
			    // 获取用户信息
			    snsUserInfo = AdvancedUtil.getSNSUserInfo(token, openId);
			    if(snsUserInfo == null){ //未获得用户信息，再次获取token后调用用户接口
			    	WeixinAccessToken wot = AdvancedUtil.getAccessToken(appId, appSecret);
					token = wot.getAccessToken();
					configs.setWeixin_token(wot.getAccessToken());
					configService.update(configs);
					
					snsUserInfo = AdvancedUtil.getSNSUserInfo(token, openId);
			    }
			    
			    
			    //zhuzhi 2017-10-06 用网页授权AccessToken获取用户信息
			    
			    if(snsUserInfo == null){ //还未获得用户信息，再次获取token后调用用户接口
			    						
					snsUserInfo = AdvancedUtil.getSNSUserInfo2(weixinOauth2Token.getAccessToken(), openId);
					
			    }
			    
			    System.out.println("....snsuserInfo : "+snsUserInfo +"....");
			    if(snsUserInfo == null){
					System.out.println(" | redirect follow url  !|");
					//String followUrl = "https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=MzIyNzc3NTI4Nw==&scene=124#wechat_redirect";
					String followUrl = "http://mp.weixin.qq.com/s/ZvWLXLUER5n6ZV87loin4g";
					
					response.sendRedirect(followUrl);   
					return;
				}
			    session.setAttribute("snsUserInfo", snsUserInfo);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    	String path = request.getContextPath();
    	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    	System.out.println(basePath);
    	response.sendRedirect(basePath+"wap/weixin_login.htm");        
    }

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
    
}