package com.metoo.module.sns.view.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.AccessoryQueryObject;
import com.metoo.foundation.domain.query.FavoriteQueryObject;
import com.metoo.foundation.service.IConsultService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.module.sns.domain.SnsAttention;
import com.metoo.module.sns.domain.UserDynamic;
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
 * Title: SnsViewAction.java
 * </p>
 * 
 * <p>
 * Description:前台sns控制器
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
 * @author jinxinzhe,jy
 * 
 * @date 2014-11-21
 * 
 * @version koala_b2b2c 2015
 */
@Controller
public class SnsOtherUserViewAction {
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
	private IUserDynamicService userdynamicService;
	@Autowired
	private IUserDynamicService dynamicService;

	/**
	 * 用户查看其他人的个人主页
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/sns/other_sns.htm")
	public String other_sns(HttpServletRequest request,
			HttpServletResponse response, String id) throws IOException {
		String url = "/sns/to_other_sns.htm?id=" + id;
		if (SecurityUserHolder.getCurrentUser() != null) {
			if (SecurityUserHolder.getCurrentUser().getId().toString()
					.equals(id)) {
				url = "/buyer/my_sns_index.htm";
			}
		}
		return "redirect:" + url;
	}

	/**
	 * 跳转到他人个人主页
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/sns/to_other_sns.htm")
	public ModelAndView to_other_sns(HttpServletRequest request,
			HttpServletResponse response, String id) throws IOException {
		ModelAndView mv = new JModelAndView("sns/other_sns_index.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User user = this.userService.getObjById(CommUtil.null2Long(id));
		if (user != null) {
			if (user.getWhether_attention() == 0) {
				mv = new JModelAndView("error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "该用户禁止其他用户访问其主页");
				mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
				return mv;
			}
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

			mv.addObject("uid", id);
			if (SecurityUserHolder.getCurrentUser() != null) {
				User currentUser = this.userService
						.getObjById(SecurityUserHolder.getCurrentUser().getId());
				mv.addObject("currentUser", currentUser);
			}
			// 加载动态
			params.clear();
			params.put("user_id", user.getId());
			List<UserDynamic> dynamics = this.dynamicService
					.query("select obj from UserDynamic obj where obj.user_id =:user_id order by obj.addTime desc",
							params, 0, 1);
			if (dynamics.size() > 0) {
				mv.addObject("userDynamics", dynamics.get(0));
			}
			mv.addObject("snsTools", snsTools);
		} else {
			mv = new JModelAndView("error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您所访问的地址不存在");
			mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
		}
		return mv;
	}

	@RequestMapping("/sns/other_sns_lock.htm")
	public ModelAndView other_sns_lock(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView("sns/sns_lock.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User otherUser = this.userService.getObjById(CommUtil.null2Long(id));
		mv.addObject("otherUser", otherUser);
		return mv;
	}

	/**
	 * 他人主页-头部
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/other_sns_head.htm")
	public ModelAndView other_sns_head(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("sns/sns_head.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String otherUser_id = CommUtil.null2String(request.getAttribute("uid"));
		User user = this.userService.getObjById(CommUtil
				.null2Long(otherUser_id));
		int attsCount = this.snsTools.queryAtts(otherUser_id);
		int fansCount = this.snsTools.queryFans(otherUser_id);
		int favsCount = this.snsTools.queryfavCount(otherUser_id);
		mv.addObject("attsCount", attsCount);
		mv.addObject("fansCount", fansCount);
		mv.addObject("favsCount", favsCount);
		mv.addObject("otherUser", user);
		mv.addObject("currentUser", SecurityUserHolder.getCurrentUser());
		mv.addObject("snsTools", snsTools);
		return mv;
	}

	/**
	 * 他人主页-导航
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/other_sns_nav.htm")
	public ModelAndView other_sns_nav(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("sns/sns_nav.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String op = CommUtil.null2String(request.getAttribute("op"));
		mv.addObject("op", op);
		String uid = CommUtil.null2String(request.getAttribute("uid"));
		mv.addObject("uid", uid);
		return mv;
	}

	/**
	 * 他人主页 - 动态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/buyer/other_sns_dynamic.htm")
	public ModelAndView other_sns_dynamic(HttpServletRequest request,
			HttpServletResponse response, String other_id) {
		ModelAndView mv = new JModelAndView("sns/other_sns_dynamic.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		// 查询用户动态
		Map params = new HashMap();
		params.put("user_id", CommUtil.null2Long(other_id));
		List<UserDynamic> userDynamics = this.userdynamicService
				.query("select obj from UserDynamic obj where obj.user_id =:user_id order by obj.addTime desc",
						params, 0, 12);
		mv.addObject("userDynamics", userDynamics);
		mv.addObject("snsTools", snsTools);
		User otherUser = this.userService.getObjById(CommUtil
				.null2Long(other_id));
		mv.addObject("other_id", other_id);
		mv.addObject("otherUser", otherUser);
		// 加载关注人信息
		params.clear();
		params.put("fromUser", CommUtil.null2Long(other_id));
		List<SnsAttention> tempSnss = this.snsAttentionService
				.query("select obj from SnsAttention obj where obj.fromUser.id=:fromUser order by obj.addTime desc",
						params, 0, 6);
		List<Map<String, String>> userAttsList = new ArrayList<Map<String, String>>();
		mv.addObject("userAttsList", userAttsList);
		// 加载粉丝信息
		params.clear();
		params.put("toUser", CommUtil.null2Long(other_id));
		tempSnss = this.snsAttentionService
				.query("select obj from SnsAttention obj where obj.toUser.id=:toUser order by obj.addTime desc",
						params, 0, 6);
		List<Map<String, String>> userFansList = new ArrayList<Map<String, String>>();
		mv.addObject("userFansList", userFansList);
		return mv;
	}

	@RequestMapping("/buyer/ajax_dynamic.htm")
	public ModelAndView ajax_dynamic(HttpServletRequest request,
			HttpServletResponse response, String count, String otherId) {
		ModelAndView mv = new JModelAndView("sns/sns_ajax_dynamic.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User otherUser = this.userService.getObjById(CommUtil
				.null2Long(otherId));
		Map params = new HashMap();
		params.put("user_id", otherUser.getId());
		List<UserDynamic> userDynamics = this.userdynamicService
				.query("select obj from UserDynamic obj where obj.user_id =:user_id order by obj.addTime desc",
						params, CommUtil.null2Int(count), 12);
		mv.addObject("userDynamics", userDynamics);
		mv.addObject("snsTools", snsTools);
		mv.addObject("otherUser", otherUser);
		return mv;
	}

	/**
	 * 他人主页-评价
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/other_sns_evas.htm")
	public ModelAndView other_sns_evas(HttpServletRequest request,
			HttpServletResponse response, String other_id) throws IOException {
		ModelAndView mv = new JModelAndView("sns/other_sns_evas.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User user = this.userService.getObjById(CommUtil.null2Long(other_id));
		if (user != null && user.getWhether_attention() == 1) {
			Map params = new HashMap();
			params.put("user_id", user.getId());
			List<Evaluate> evas = this.evaluateService
					.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and  (obj.evaluate_photos is null or obj.evaluate_photos='') order by obj.addTime desc",
							params, 0, 10);
			mv.addObject("evas", evas);
			mv.addObject("other_id", other_id);
		} else {
			mv = new JModelAndView("error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "请求参数错误");
			mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
		}
		return mv;
	}

	/**
	 * 他人主页-评价ajax
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/ajax_evas.htm")
	public ModelAndView sns_ajax_evas(HttpServletRequest request,
			HttpServletResponse response, String size, String other_id) {
		ModelAndView mv = new JModelAndView("sns/sns_ajax_evas.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", CommUtil.null2Long(other_id));
		List<Evaluate> evas = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and (obj.evaluate_photos is null or obj.evaluate_photos='') order by obj.addTime desc",
						params, begin, end);
		mv.addObject("evas", evas);
		return mv;
	}

	/**
	 * 他人主页-晒单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/other_sns_evaps.htm")
	public ModelAndView other_sns_evaps(HttpServletRequest request,
			HttpServletResponse response, String other_id) throws IOException {
		ModelAndView mv = new JModelAndView("sns/other_sns_evaps.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User user = this.userService.getObjById(CommUtil.null2Long(other_id));
		if (user != null && user.getWhether_attention() == 1) {
			Map params = new HashMap();
			params.put("user_id", user.getId());
			List<Evaluate> evaps = this.evaluateService
					.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and  (obj.evaluate_photos is not null and obj.evaluate_photos!='') order by obj.addTime desc",
							params, 0, 10);
			mv.addObject("evaps", evaps);
			mv.addObject("other_id", other_id);
			mv.addObject("evaluateViewTools", evaluateViewTools);
		} else {
			mv = new JModelAndView("error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "请求参数错误");
			mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
		}
		return mv;
	}

	@RequestMapping("/sns/ajax_evaps.htm")
	public ModelAndView sns_ajax_evaps(HttpServletRequest request,
			HttpServletResponse response, String size, String id) {
		ModelAndView mv = new JModelAndView("sns/sns_ajax_evaps.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", CommUtil.null2Long(id));
		List<Evaluate> evaps = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_user.id = :user_id and obj.evaluate_status=0 and (obj.evaluate_photos is not null and obj.evaluate_photos!='') order by obj.addTime desc",
						params, begin, end);
		mv.addObject("evaps", evaps);
		mv.addObject("evaluateViewTools", evaluateViewTools);
		return mv;
	}

	/**
	 * 他人主页-分享
	 * 
	 * @param request
	 * @param response
	 * @param currentPage
	 * @param other_id
	 * @return
	 */
	@RequestMapping("/sns/other_sns_share.htm")
	public ModelAndView other_sns_share(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String other_id) {
		ModelAndView mv = new JModelAndView("sns/other_sns_share.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String param = "";
		UserShareQueryObject qo = new UserShareQueryObject(currentPage, mv,
				"addTime", "desc");
		qo.addQuery("obj.user_id",
				new SysMap("user_id", CommUtil.null2Long(other_id)), "=");
		IPageList pList = this.userShareService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/sns/other_sns_share.htm",
				"", param, pList, mv);
		mv.addObject("currentPage", currentPage);
		mv.addObject("other_id", other_id);
		return mv;
	}

	/**
	 * 他人主页-收藏
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/other_sns_fav.htm")
	public ModelAndView other_sns_fav(HttpServletRequest request,
			HttpServletResponse response, String other_id) {
		ModelAndView mv = new JModelAndView("sns/other_sns_favs.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Map params = new HashMap();
		params.put("user_id", CommUtil.null2Long(other_id));
		List<Favorite> favorites = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id = :user_id and obj.type=0 order by obj.addTime desc",
						params, 0, 10);
		mv.addObject("favorites", favorites);
		mv.addObject("other_id", other_id);
		return mv;
	}

	/**
	 * 他人主页-收藏ajax
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/sns/ajax_favs.htm")
	public ModelAndView sns_ajax_favs(HttpServletRequest request,
			HttpServletResponse response, String size, String other_id) {
		ModelAndView mv = new JModelAndView("sns/sns_ajax_favs.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		int current_size = CommUtil.null2Int(size);
		int begin = current_size * 5;
		int end = begin + 10;
		Map params = new HashMap();
		params.put("user_id", CommUtil.null2Long(other_id));
		List<Favorite> favorites = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id = :user_id and obj.type=0 order by obj.addTime desc",
						params, begin, end);
		mv.addObject("favorites", favorites);
		mv.addObject("other_id", other_id);
		return mv;
	}

	/**
	 * 他人主页-关注
	 * 
	 * @param request
	 * @param response
	 * @param currentPage
	 * @param other_id
	 * @return
	 */
	@RequestMapping("/sns/other_sns_atts.htm")
	public ModelAndView other_sns_atts(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String other_id) {
		ModelAndView mv = new JModelAndView("/sns/other_sns_atts.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String param = "";
		SnsAttentionQueryObject qo = new SnsAttentionQueryObject(currentPage,
				mv, "addTime", "desc");
		qo.addQuery("obj.fromUser.id",
				new SysMap("user_id", CommUtil.null2Long(other_id)), "=");
		IPageList pList = this.snsAttentionService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/sns/other_sns_atts.html",
				"", param, pList, mv);
		mv.addObject("other_id", other_id);
		mv.addObject("snsTools", snsTools);
		return mv;
	}

	/**
	 * 他人主页-粉丝
	 * 
	 * @param request
	 * @param response
	 * @param currentPage
	 * @param other_id
	 * @return
	 */
	@RequestMapping("/sns/other_sns_fans.htm")
	public ModelAndView other_sns_fans(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String other_id) {
		ModelAndView mv = new JModelAndView("/sns/other_sns_fans.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String param = "";
		SnsAttentionQueryObject qo = new SnsAttentionQueryObject(currentPage,
				mv, "addTime", "desc");
		qo.addQuery("obj.toUser.id",
				new SysMap("user_id", CommUtil.null2Long(other_id)), "=");
		IPageList pList = this.snsAttentionService.list(qo);
		CommUtil.saveIPageList2ModelAndView(url + "/sns/other_sns_fans.htm",
				"", param, pList, mv);
		mv.addObject("snsTools", snsTools);
		mv.addObject("other_id", other_id);
		return mv;
	}

}
