package com.metoo.module.sns.manage.buyer.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.metoo.foundation.domain.Consult;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IConsultService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.module.sns.domain.SnsAttention;
import com.metoo.module.sns.domain.UserDynamic;
import com.metoo.module.sns.domain.UserShare;
import com.metoo.module.sns.domain.query.SnsAttentionQueryObject;
import com.metoo.module.sns.domain.query.UserShareQueryObject;
import com.metoo.module.sns.service.ISnsAttentionService;
import com.metoo.module.sns.service.IUserDynamicService;
import com.metoo.module.sns.service.IUserShareService;
import com.metoo.module.sns.view.tools.SnsFreeTools;
import com.metoo.module.sns.view.tools.SnsTools;
import com.metoo.view.web.tools.EvaluateViewTools;
import com.metoo.view.web.tools.IntegralViewTools;

/**
 * 
 * <p>
 * Title: SnsBuyerAction.java
 * </p>
 * 
 * <p>
 * Description:用户SNS功能控制器
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com
 * </p>
 * 
 * @author jinxinzhe
 * 
 * @date 2014-11-21
 * 
 * @version koala_b2b2c 2015
 */
@Controller
public class SnsBuyerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ISnsAttentionService snsAttentionService;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private EvaluateViewTools evaluateViewTools;
	@Autowired
	private IConsultService consultService;
	@Autowired
	private SnsFreeTools freeTools;
	@Autowired
	private SnsTools snsTools;
	@Autowired
	private IUserShareService userShareService;
	@Autowired
	private IUserDynamicService dynamicService;

	/**
	 * 用户查看自己的个人主页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SecurityMapping(title = "买家sns首页", value = "/buyer/my_sns_index.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_index.htm")
	public ModelAndView my_sns_index(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_index.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		mv.addObject("user", user);
		// 加载关注人信息
		Map params = new HashMap();
		params.put("fromUser", CommUtil.null2Long(user.getId()));
		List<SnsAttention> tempSnss = this.snsAttentionService
				.query("select obj from SnsAttention obj where obj.fromUser.id=:fromUser order by obj.addTime desc",
						params, 0, 10);
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
		params.put("toUser", CommUtil.null2Long(user.getId()));
		tempSnss = this.snsAttentionService
				.query("select obj from SnsAttention obj where obj.toUser.id=:toUser order by obj.addTime desc",
						params, 0, 10);
		List<Map<String, String>> userFansList = new ArrayList<Map<String, String>>();
		for (SnsAttention sns : tempSnss) {
			Map map = new HashMap<String, String>();
			map.put("user_id", sns.getFromUser().getId());
			map.put("user_name", sns.getFromUser().getUserName());
			map.put("sns_time", sns.getAddTime());
			userFansList.add(map);
		}
		mv.addObject("userFansList", userFansList);
		// 加载分享商品
		mv.addObject("userShare", snsTools.querylastUserShare(user.getId()));
		// 加载收藏信息
		mv.addObject("fav", snsTools.queryLastUserFav(user.getId()));
		// 加载评价与晒单
		params.clear();
		params.put("user_id", user.getId());
		List<Evaluate> evas = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and  (obj.evaluate_photos is null or obj.evaluate_photos='') order by obj.addTime desc",
						params, 0, 2);
		mv.addObject("evas", evas);
		params.put("user_id", user.getId());
		List<Evaluate> evaPhotos = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and  (obj.evaluate_photos is not null and obj.evaluate_photos!='') order by obj.addTime desc",
						params, 0, 2);
		mv.addObject("evaPhotos", evaPhotos);

		// 加载动态信息
		params.clear();
		params.put("user_id", user.getId());
		List<UserDynamic> userDynamics = this.dynamicService
				.query("select obj from UserDynamic obj where obj.user_id =:user_id order by obj.addTime desc",
						params, 0, 1);
		if (userDynamics.size() > 0) {
			mv.addObject("userDynamics", userDynamics.get(0));
		}
		mv.addObject("snsTools", snsTools);
		return mv;
	}

	@SecurityMapping(title = "买家sns头部", value = "/buyer/my_sns_head.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_head.htm")
	public ModelAndView my_sns_head(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_head.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		int attsCount = this.snsTools.queryAtts(user.getId().toString());
		int fansCount = this.snsTools.queryFans(user.getId().toString());
		int favsCount = this.snsTools.queryfavCount(user.getId().toString());
		mv.addObject("attsCount", attsCount);
		mv.addObject("fansCount", fansCount);
		mv.addObject("favsCount", favsCount);
		mv.addObject("user", user);
		return mv;
	}

	@SecurityMapping(title = "买家sns导航", value = "/buyer/my_sns_head.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_nav.htm")
	public ModelAndView my_sns_nav(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_nav.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		String op = CommUtil.null2String(request.getAttribute("op"));
		mv.addObject("op", op);
		return mv;
	}

	@SecurityMapping(title = "买家sns开启访问权限", value = "/buyer/sns_lock_on.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/sns_lock_on.htm")
	public void sns_lock_on(HttpServletRequest request,
			HttpServletResponse response) {
		int ret = 1;
		if (SecurityUserHolder.getCurrentUser() != null) {
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			user.setWhether_attention(0);
			this.userService.update(user);
			ret = 0;
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

	@SecurityMapping(title = "买家sns关闭访问权限", value = "/buyer/sns_lock_off.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/sns_lock_off.htm")
	public void sns_lock_off(HttpServletRequest request,
			HttpServletResponse response) {
		int ret = 1;
		if (SecurityUserHolder.getCurrentUser() != null) {
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			user.setWhether_attention(1);
			this.userService.update(user);
			ret = 0;
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

	@SecurityMapping(title = "买家sns分享列表", value = "/buyer/my_sns_share.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_share.htm")
	public ModelAndView my_sns_share(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_share.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String param = "";
		UserShareQueryObject qo = new UserShareQueryObject(currentPage, mv,
				"addTime", "desc");
		qo.addQuery("obj.user_id", new SysMap("user_id", SecurityUserHolder
				.getCurrentUser().getId()), "=");
		qo.setPageSize(15);
		IPageList pList = this.userShareService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/buyer/my_sns_share.htm",
				"", param, pList, mv);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "买家sns分享删除", value = "/buyer/my_sns_share_del.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_share_del.htm")
	public void my_sns_share_del(HttpServletRequest request,
			HttpServletResponse response, String share_id) {
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		boolean ret = false;
		UserShare userShare = this.userShareService.getObjById(CommUtil
				.null2Long(share_id));
		if (userShare.getUser_id().equals(user.getId())) {
			ret = this.userShareService.delete(userShare.getId());
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

	@SecurityMapping(title = "买家sns收藏", value = "/buyer/my_sns_fav.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_fav.htm")
	public ModelAndView my_sns_fav(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_fav.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Favorite> favorites = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id = :user_id and obj.type=0 order by obj.addTime desc",
						params, 0, 10);
		mv.addObject("favorites", favorites);
		return mv;
	}

	@SecurityMapping(title = "买家sns收藏ajax", value = "/buyer/sns_ajax_favs.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/sns_ajax_favs.htm")
	public ModelAndView sns_ajax_favs(HttpServletRequest request,
			HttpServletResponse response, String size) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/sns_ajax_favs.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Favorite> favorites = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id = :user_id and obj.type=0 order by obj.addTime desc",
						params, begin, end);
		mv.addObject("favorites", favorites);
		return mv;
	}

	@SecurityMapping(title = "买家sns评价", value = "/buyer/my_sns_evas.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_evas.htm")
	public ModelAndView my_sns_evas(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_evas.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Evaluate> evas = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and  (obj.evaluate_photos is null or obj.evaluate_photos='') order by obj.addTime desc",
						params, 0, 10);
		mv.addObject("evas", evas);
		return mv;
	}

	@SecurityMapping(title = "买家sns评价ajax", value = "/buyer/sns_ajax_evas.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/sns_ajax_evas.htm")
	public ModelAndView sns_ajax_evas(HttpServletRequest request,
			HttpServletResponse response, String size) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/sns_ajax_evas.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Evaluate> evas = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and (obj.evaluate_photos is null or obj.evaluate_photos='') order by obj.addTime desc",
						params, begin, end);
		mv.addObject("evas", evas);
		return mv;
	}

	/**
	 * 晒单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SecurityMapping(title = "买家sns晒单", value = "/buyer/my_sns_evaps.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_evaps.htm")
	public ModelAndView my_sns_evaps(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_evaps.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Evaluate> evas = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and  (obj.evaluate_photos is not null and obj.evaluate_photos!='') order by obj.addTime desc",
						params, 0, 10);
		mv.addObject("evas", evas);
		mv.addObject("evaluateViewTools", evaluateViewTools);
		return mv;
	}

	@SecurityMapping(title = "买家sns晒单ajax", value = "/buyer/sns_ajax_evas.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/sns_ajax_evaps.htm")
	public ModelAndView sns_ajax_evaps(HttpServletRequest request,
			HttpServletResponse response, String size) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/sns_ajax_evaps.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Evaluate> evas = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and (obj.evaluate_photos is not null and obj.evaluate_photos!='') order by obj.addTime desc",
						params, begin, end);
		mv.addObject("evas", evas);
		mv.addObject("evaluateViewTools", evaluateViewTools);
		return mv;
	}

	@SecurityMapping(title = "买家sns", value = "/buyer/my_sns_cons.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_cons.htm")
	public ModelAndView my_sns_cons(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_cons.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Consult> cons = this.consultService
				.query("select obj from Consult obj where obj.consult_user_id = :user_id",
						params, 0, 10);
		mv.addObject("cons", cons);
		mv.addObject("freeTools", freeTools);
		return mv;
	}

	@SecurityMapping(title = "买家sns", value = "/buyer/sns_ajax_cons.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/sns_ajax_cons.htm")
	public ModelAndView sns_ajax_cons(HttpServletRequest request,
			HttpServletResponse response, String size) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/sns_ajax_cons.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Consult> cons = this.consultService
				.query("select obj from Consult obj where obj.consult_user_id = :user_id",
						params, begin, end);
		mv.addObject("cons", cons);
		mv.addObject("freeTools", freeTools);
		return mv;
	}

	@SecurityMapping(title = "买家sns关注人", value = "/buyer/my_sns_atts.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_atts.htm")
	public ModelAndView my_sns_atts(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_atts.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String param = "";
		SnsAttentionQueryObject qo = new SnsAttentionQueryObject(currentPage,
				mv, "addTime", "desc");
		qo.addQuery("obj.fromUser.id", new SysMap("user_id", user.getId()), "=");
		qo.setPageSize(16);
		IPageList pList = this.snsAttentionService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/buyer/my_sns_atts.htm", "",
				param, pList, mv);
		mv.addObject("snsTools", snsTools);
		return mv;
	}

	@SecurityMapping(title = "买家sns粉丝", value = "/buyer/my_sns_fans.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/my_sns_fans.htm")
	public ModelAndView my_sns_fans(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/sns/my_sns_fans.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String param = "";
		SnsAttentionQueryObject qo = new SnsAttentionQueryObject(currentPage,
				mv, "addTime", "desc");
		qo.addQuery("obj.toUser.id", new SysMap("user_id", user.getId()), "=");
		qo.setPageSize(16);
		IPageList pList = this.snsAttentionService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/buyer/my_sns_fans.htm", "",
				param, pList, mv);
		mv.addObject("snsTools", snsTools);
		return mv;
	}

}