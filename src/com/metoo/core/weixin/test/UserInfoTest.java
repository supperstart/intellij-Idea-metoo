package com.metoo.core.weixin.test;

import com.metoo.core.weixin.action.WeixinAction;
import com.metoo.core.weixin.pojo.SNSUserInfo;
import com.metoo.core.weixin.utils.CommonUtil;

public class UserInfoTest {
    private static final String PREFIX_XML = "<xml>";
private static final String SUFFIX_XML = "</xml>";

private static final String PREFIX_CDATA = "<![CDATA[";

private static final String SUFFIX_CDATA = "]]>";
	public static void main(String args[]) {
        // 获取接口访问凭证
        String accessToken = CommonUtil.getToken("wx230ecfa4c5bdb96d", "ee309dbd9d578a32e4d81f580677790b").getAccessToken();
        /**
         * 获取用户信息
         */
        SNSUserInfo user = WeixinAction.getUserInfo(accessToken, "oN_FjwETZk_NuZq_0BovO5e8d1vk");
        System.out.println("OpenID：" + user.getOpenId());
        System.out.println(" 关注状态：" + user.getSubscribe());
        System.out.println("关注时间：" + user.getSubscribe_time());
        System.out.println("昵称：" + user.getNickname());
        System.out.println("性别：" + user.getSex());
        System.out.println("国家：" + user.getCountry());
        System.out.println("省份：" + user.getProvince());
        System.out.println("城市：" + user.getCity());
        System.out.println("头像：" + user.getHeadImgUrl());
    }
	
	
	/*@Test
	
	public static String mapToXml() {
		HashMap<Object,Object> map = new HashMap<Object,Object>();
		map.put("name", "hk");
		map.put("pwd","123456");
		StringBuffer strbuff = new StringBuffer(PREFIX_XML);
		if (null != map) {
			for (Entry<Object, Object> entry : map.entrySet()) {
				strbuff.append("<").append(entry.getKey()).append(">");
				if (false) {
					strbuff.append(PREFIX_CDATA);
					if (null != entry.getValue()) {
						strbuff.append(entry.getValue());
					}
					strbuff.append(SUFFIX_CDATA);
				} else {
					if (null != entry.getValue()) {
						strbuff.append(entry.getValue());
					}
				}
				strbuff.append("</").append(entry.getKey()).append(">");
			}
		}
		System.out.println(strbuff.append(SUFFIX_XML).toString());
		return strbuff.append(SUFFIX_XML).toString();
	}*/
}
