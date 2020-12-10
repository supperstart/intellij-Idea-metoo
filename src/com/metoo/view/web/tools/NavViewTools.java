package com.metoo.view.web.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Navigation;
import com.metoo.foundation.service.IActivityService;
import com.metoo.foundation.service.IArticleService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.INavigationService;

/**
 * 
* <p>Title: NavViewTools.java</p>

* <p>Description:前台导航工具类，查询显示对应的导航信息 </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 沈阳网之商科技有限公司 www.koala.com</p>

* @author erikzhang

* @date 2014-8-25

* @version koala_b2b2c v2.0 2015版 
 */
@Component
public class NavViewTools {
	@Autowired
	private INavigationService navService;
	@Autowired
	private IArticleService articleService;
	@Autowired
	private IActivityService activityService;
	@Autowired
	private IGoodsClassService goodsClassService;
	Result result = null;
	/**
	 * 查询页面导航
	 * 
	 * @param position
	 *            导航位置，-1为顶部，0为中间，1为底部
	 * @param count
	 *            导航数目，查询导航数目，-1为查询所有
	 * @return
	 */
	public List<Navigation> queryNav(int location, int count) {
		List<Navigation> navs = new ArrayList<Navigation>();
		Map params = new HashMap();
		params.put("display", true);
		params.put("location", location);
		params.put("type", "sparegoods");
		navs = this.navService
				.query("select obj from Navigation obj where obj.display=:display and obj.location=:location and obj.type!=:type order by obj.sequence asc",
						params, 0, count);
		return navs;
	}
	public void queryNav_api(HttpServletRequest request,
			HttpServletResponse response,int location, int count) {
		List<Navigation> navs = new ArrayList<Navigation>();
		Map navigaList = new HashMap();
		Map params = new HashMap();
		params.put("display", true);
		params.put("location", location);
		params.put("type", "sparegoods");
		navs = this.navService
				.query("select obj from Navigation obj where obj.display=:display and obj.location=:location and obj.type!=:type order by obj.sequence asc",
						params, 0, count);
		if(!navs.isEmpty()){
			List<Map> navigamap = new ArrayList<Map>(); 
			for(Navigation navigation:navs){
				Map navMap = new HashMap();
				navMap.put("", navigation.getUrl());
				navMap.put("", navigation.getTitle());
				//是否新窗口打开，1为新窗口打开，0为默认页面打开
				navMap.put("", navigation.getNew_win());
				navMap.put("", navigation.getUrl());
				navigamap.add(navMap);
				navigaList.put("navigamap", navigamap);
			}
		}
		if(CommUtil.isNotNull(navigaList)){
			result = new Result(0,"查询成功",navigaList);
		}else{
			result = new Result(1,"查询失败");
		}
		
		String navigaTemp = Json.toJson(result, JsonFormat.compact());
		try {
			response.getWriter().print(navigaTemp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
