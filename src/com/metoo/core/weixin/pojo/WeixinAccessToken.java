package com.metoo.core.weixin.pojo;

/**
* 类名: WeixinAccessToken </br>
* 描述:  接口凭证  </br>
* 开发人员： souvc </br>
* 创建时间：  2015-11-27 </br>
* 发布版本：V1.0  </br>
 */
public class WeixinAccessToken {
    // 网页授权接口调用凭证
    private String accessToken;
    // 凭证有效时长
    private int expiresIn;
    

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}