package com.metoo.module.sns.manage.buyer.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.AccessoryQueryObject;
import com.metoo.foundation.domain.query.FavoriteQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.module.sns.domain.SnsAttention;
import com.metoo.module.sns.domain.UserDynamic;
import com.metoo.module.sns.service.ISnsAttentionService;
import com.metoo.module.sns.service.IUserDynamicService;
import com.metoo.module.sns.view.tools.SnsTools;

/**
 * 
* <p>Title: SnsBuyerDynamicAction.java</p>

* <p>Description: 用户sns动态功能控制器</p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 沈阳网之商科技有限公司 www.koala.com</p>

* @author jy

* @date 2015-1-21

* @version koala_b2b2c_2015
 */
@Controller
public class SnsBuyerDynamicAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private SnsTools snsTools;
	@Autowired
	private IUserDynamicService userdynamicService;	
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private ISnsAttentionService snsAttentionService;
	
	
	@SecurityMapping(title = "买家sns动态", value = "/buyer/my_sns_dynamic.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_dynamic.htm")
	public ModelAndView my_sns_dynamic(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_dynamic.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId());
		//查询用户动态
		Map params = new HashMap();
		params.put("user_id",user.getId());
		List<UserDynamic> userDynamics = this.userdynamicService.query("select obj from UserDynamic obj where obj.user_id =:user_id order by obj.addTime desc", params, 0,12);
		mv.addObject("userDynamics",userDynamics);
		mv.addObject("snsTools",snsTools);
		// 查询收藏商品和晒单图片
		FavoriteQueryObject fqo = new FavoriteQueryObject();
		fqo.addQuery("obj.type", new SysMap("type", 0), "=");
		fqo.addQuery("obj.user_id",
				new SysMap("user_id", user.getId()), "=");
		fqo.setConstruct("new Favorite(id,type,goods_name,goods_id,goods_photo,goods_type,goods_store_id,goods_current_price, goods_photo_ext,goods_store_second_domain, user_name, user_id)");
		fqo.setPageSize(4);
		IPageList pList = this.favoriteService.list(fqo);
		mv.addObject("fav_objs", pList.getResult());
		AccessoryQueryObject aqo = new AccessoryQueryObject();
		aqo.addQuery("obj.user.id",new SysMap("user_id", user.getId()), "=");
		aqo.addQuery("obj.info",new SysMap("info","eva_img"), "=");
		aqo.setPageSize(4);
		pList = this.accessoryService.list(aqo);
		mv.addObject("eva_objs", pList.getResult());
		// 加载关注人信息
		params.clear();
		params.put("fromUser", user.getId());
		List<SnsAttention> tempSnss = this.snsAttentionService
				.query("select obj from SnsAttention obj where obj.fromUser.id=:fromUser order by obj.addTime desc",
						params, 0, 6);
		List<Map<String, String>> userAttsList = new ArrayList<Map<String, String>>();
		for (SnsAttention sns : tempSnss) {
			Map map = new HashMap<String, String>();
			map.put("user_id", sns.getToUser().getId());
			map.put("user_name", sns.getToUser().getUserName());
			map.put("sns_time", sns.getAddTime());
			userAttsList.add(map);
		}
		mv.addObject("userAttsList", userAttsList);
		// 加载粉丝信息
		params.clear();
		params.put("toUser",  user.getId());
		tempSnss = this.snsAttentionService
				.query("select obj from SnsAttention obj where obj.toUser.id=:toUser order by obj.addTime desc",
						params, 0, 6);
		List<Map<String, String>> userFansList = new ArrayList<Map<String, String>>();
		for (SnsAttention sns : tempSnss) {
			Map map = new HashMap<String, String>();
			map.put("user_id", sns.getFromUser().getId());
			map.put("user_name", sns.getFromUser().getUserName());
			map.put("sns_time", sns.getAddTime());
			userFansList.add(map);
		}
		mv.addObject("userFansList", userFansList);
		return mv;
	}
	
	@RequestMapping("/buyer/my_sns_dynamic_ajax.htm")
	public ModelAndView my_sns_dynamic_ajax(HttpServletRequest request,
			HttpServletResponse response,String count) {
		ModelAndView mv = new JModelAndView("user/default/usercenter/sns/dynamic_ajax.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId());
		//查询用户动态
		Map params = new HashMap();
		params.put("user_id",user.getId());
		List<UserDynamic> userDynamics = this.userdynamicService.query("select obj from UserDynamic obj where obj.user_id =:user_id order by obj.addTime desc",
				params,CommUtil.null2Int(count),12);
		mv.addObject("userDynamics",userDynamics);
		mv.addObject("snsTools",snsTools);
		return mv;
	}
	
	
	@SecurityMapping(title = "买家sns动态保存", value = "/buyer/my_sns_dynamic_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_dynamic_save.htm")
	public void my_sns_dynamic_save(HttpServletRequest request,
			HttpServletResponse response,String dynamic_content,String img_info){
		User user = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId());
		WebForm wf = new WebForm();
		UserDynamic userdynamic = wf.toPo(request, UserDynamic.class);
		userdynamic.setAddTime(new Date());
		userdynamic.setUser_id(user.getId());
		userdynamic.setUser_name(user.getUserName());
		boolean ret = this.userdynamicService.save(userdynamic);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SecurityMapping(title = "买家sns动态删除", value = "/buyer/my_sns_dynamic_del.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_dynamic_del.htm")
	public void my_sns_dynamic_del(HttpServletRequest request,
			HttpServletResponse response,String dynamic_id){
		User user = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId());
		boolean ret = false;
		UserDynamic userdynamic = this.userdynamicService.getObjById(CommUtil.null2Long(dynamic_id));
		if(userdynamic.getUser_id().equals(user.getId())){
			ret = this.userdynamicService.delete(userdynamic.getId());
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@SecurityMapping(title = "动态中收藏商品ajax", value = "/buyer/dynamic_fav_ajax.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/dynamic_fav_ajax.htm")
	public ModelAndView dynamic_fav_ajax(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView("user/default/usercenter/sns/dynamic_fav_ajax.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		FavoriteQueryObject fqo = new FavoriteQueryObject();
		fqo.addQuery("obj.type", new SysMap("type", 0), "=");
		fqo.addQuery("obj.user_id", new SysMap("user_id", user.getId()), "=");
		fqo.setConstruct("new Favorite(id,type,goods_name,goods_id,goods_photo,goods_type,goods_store_id,goods_current_price, goods_photo_ext,goods_store_second_domain, user_name, user_id)");
		fqo.setCurrentPage(CommUtil.null2Int(currentPage));
		fqo.setPageSize(4);
		IPageList pList = this.favoriteService.list(fqo);
		if(CommUtil.null2Int(currentPage)<1){
			currentPage = "1";
		}
		if(CommUtil.null2Int(currentPage)>pList.getPages()){
			currentPage = Integer.toString(pList.getPages());
		};
		mv.addObject("fav_objs", pList.getResult());
		mv.addObject("goPage",CommUtil.null2Int(currentPage)+1);
		mv.addObject("backPage",CommUtil.null2Int(currentPage)-1);
		return mv;
	}
	
	@SecurityMapping(title = "动态中晒单图片ajax", value = "/buyer/dynamic_eva_ajax.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/dynamic_eva_ajax.htm")
	public ModelAndView dynamic_eva_ajax(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView("user/default/usercenter/sns/dynamic_eva_ajax.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		AccessoryQueryObject aqo = new AccessoryQueryObject();
		aqo.addQuery("obj.user.id",new SysMap("user_id", user.getId()), "=");
		aqo.addQuery("obj.info",new SysMap("info","eva_img"), "=");
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		aqo.setPageSize(4);
		IPageList pList = this.accessoryService.list(aqo);
		if(CommUtil.null2Int(currentPage)<1){
			currentPage = "1";
		}
		if(CommUtil.null2Int(currentPage)>pList.getPages()){
			currentPage = Integer.toString(pList.getPages());
		};
		mv.addObject("eva_objs", pList.getResult());
		mv.addObject("goPage",CommUtil.null2Int(currentPage)+1);
		mv.addObject("backPage",CommUtil.null2Int(currentPage)-1);
		return mv;
	}

	@RequestMapping("/buyer/dynamic_add_img.htm")
	public void dynamic_add_img(HttpServletRequest request,
			HttpServletResponse response,String id ,String img_info,String type){
		List<Map> list = null;
		int num = 0;
		if(img_info!=null&&!img_info.equals("")){
			list = Json.fromJson(ArrayList.class, img_info);
			num = CommUtil.null2Int(list.get(list.size()-1).get("num"))+1;
		}else{
			list = new ArrayList<Map>();			
		}
		Accessory acc = null;
		if("eva".equals(type)){
			acc = this.accessoryService.getObjById(CommUtil.null2Long(id));
		};
		if("fav".equals(type)){
			Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			acc = goods.getGoods_main_photo();			
		}
		Map map = new HashMap();
		map.put("id", id);
		map.put("num", num);
		map.put("img",acc.getPath()+"/"+acc.getName());
		list.add(map);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(list, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@RequestMapping("/buyer/dynamic_del_img.htm")
	public void dynamic_del_img(HttpServletRequest request,
			HttpServletResponse response,String num,String img_info){
		List<Map> list = null;
		if(img_info!=null&&!img_info.equals("")){
			list = Json.fromJson(ArrayList.class, img_info);
			for (Map map : list) {
				if(map.get("num").equals(CommUtil.null2Int(num))){
					list.remove(map);
					break;
				}
			}
		}
		String jsonStr = "";
		if(list.size()>0){
			jsonStr = Json.toJson(list, JsonFormat.compact());
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(jsonStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
