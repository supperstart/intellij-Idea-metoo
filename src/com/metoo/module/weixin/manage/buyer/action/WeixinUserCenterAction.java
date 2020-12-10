package com.metoo.module.weixin.manage.buyer.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Message;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IMessageService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.view.web.tools.IntegralViewTools;
import com.metoo.view.web.tools.StoreViewTools;

/**
 * 
 * 
 * <p>
 * Title:MobileUserCenterAction.java
 * </p>
 * 
 * <p>
 * Description: 移动端用户中心
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
 * @author jy
 * 
 * @date 2014年8月20日
 * 
 * @version koala_b2b2c_2015
 */
@Controller
public class WeixinUserCenterAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormService orderformService;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IMessageService messageService;

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private StoreViewTools storeViewTools;
	/**
	 * 手机客户端商城首页
	 * 
	 * @param request
	 * @param response
	 * @param store_id
	 * @return
	 */
	@SecurityMapping(title = "用户中心", value = "/wap/buyer/center.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/center.htm")
	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/user_index.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
	
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("user_id", user.getId());
		params.put("status", 0);
		//优惠券
		mv.addObject(
				"couponInfos",
				this.couponInfoService
						.query("select obj.id from CouponInfo obj where obj.user.id=:user_id and obj.status = :status",
								params, -1, -1).size());
		params.clear();
		params.put("status", 10);
		params.put("user_id", user.getId().toString());
		mv.addObject(
				"orders_10",
				this.orderformService
						.query("select obj.id from OrderForm obj where obj.order_cat!=2 and obj.user_id= :user_id and obj.order_status = :status",
								params, -1, -1).size());
		mv.addObject("integralViewTools", integralViewTools);
		params.clear();
		params.put("type", 0);
		params.put("user_id", user.getId());
		//收藏商品
		List<Favorite>favorite_goods = this.favoriteService.query("select obj from Favorite obj where obj.type=:type and obj.user_id=:user_id", params,0 , 6);
		mv.addObject(
				"favorite_goods", 	
				favorite_goods);
		params.clear();
		params.put("type", 1);
		params.put("user_id", user.getId());
		//收藏店铺
		mv.addObject(
				"favorite_store", 	
				this.favoriteService
						.query("select obj from Favorite obj where obj.type=:type and obj.user_id=:user_id", params,0 , 6));
		params.clear();
		params.put("status_1", 10);
		params.put("user_id", user.getId().toString());
		params.put("order_main", 1);
		//订单未支付
		List<OrderForm> order_nopays = this.orderformService.query("select count(obj.id) from OrderForm obj where obj.order_status=:status_1 and obj.user_id=:user_id"
				+ " and obj.order_main=:order_main", params, -1, -1);
		String order_nopay = "";
		if(CommUtil.null2Int(order_nopays.get(0))>9){
			order_nopay = "9+";
		}else{
			order_nopay = order_nopays.get(0)+"";
		}
		mv.addObject("order_nopay",order_nopay );
		params.clear();
		params.put("status_1", 20);
		params.put("user_id", user.getId().toString());
		params.put("order_main", 1);
		//订单未发货
		List<OrderForm> order_noships = this.orderformService.query("select count(obj.id) from OrderForm obj where obj.order_status=:status_1 and obj.user_id=:user_id"
				+ " and obj.order_main=:order_main", params, -1, -1);
		String order_noship = "";
		if(CommUtil.null2Int(order_noships.get(0))>9){
			order_noship = "9+";
		}else{
			order_noship = order_noships.get(0)+"";
		}
		mv.addObject("order_noship",order_noship);
		params.clear();
		params.put("status_1", 30);
		params.put("status_2", 35);
		params.put("user_id", user.getId().toString());
		params.put("order_main", 1);
		//订单未收货
		List<OrderForm> order_notakes = this.orderformService.query("select count(obj.id) from OrderForm obj where (obj.order_status=:status_1 or obj.order_status=:status_2) and obj.user_id=:user_id"
				+ " and obj.order_main=:order_main", params, -1, -1);
		String order_notake = "";
		if(CommUtil.null2Int(order_notakes.get(0))>9){
			order_notake = "9+";
		}else{
			order_notake = order_notakes.get(0)+"";
		}
		mv.addObject("order_notake",order_notake);
		List<Goods> your_like_goods = new ArrayList<Goods>();
		/*Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("goodscookie")) {
					String[] like_gcid = cookie.getValue().split(",");
					Set<Long> gc_ids = new HashSet<Long>();
					for (String gcid : like_gcid) {
						Goods goods = this.goodsService
						.getObjById(CommUtil.null2Long(gcid));
						if(goods!=null){
							System.out.println(".................");
							System.out.println(goods.getGc().getId());
							System.out.println(goods.getGc().getParent().getId());
							gc_ids.add(goods.getGc()
									.getParent().getId());
						}
					}
					if(gc_ids.size()>0){
					Map map = new HashMap();
					map.put("ids", gc_ids);
					your_like_goods = this.goodsService
							.query("select obj from Goods obj where obj.goods_status=0 and obj.gc.parent.id in (:ids) order by obj.goods_salenum desc",
									map, 0, 9);
					}else{
						your_like_goods = this.goodsService
								.query("select obj from Goods obj where obj.goods_status=0 order by obj.goods_salenum desc",
										null, 0, 9);
					}
				} else {
					your_like_goods = this.goodsService
							.query("select obj from Goods obj where obj.goods_status=0 order by obj.goods_salenum desc",
									null, 0, 9);
				}
			}
		} else {
			your_like_goods = this.goodsService
					.query("select obj from Goods obj where obj.goods_status=0 order by obj.goods_salenum desc",
							null, 0, 9);
		}*/
		your_like_goods = this.goodsService
				.query("select obj from Goods obj where obj.goods_status=0 order by obj.goods_salenum desc",
						null, 0, 9);
		//猜你喜欢
		mv.addObject("your_like_goods", your_like_goods);
		// 查询未读站内信数量
				List<Message> msgs = new ArrayList<Message>();
				params.clear();
				params.put("status", 0);
				params.put("user_id", user.getId());
				msgs = this.messageService
						.query("select count(obj.id) from Message obj where obj.status=:status and obj.toUser.id=:user_id and obj.parent.id is null order by obj.addTime desc",
								params, -1, -1);
				mv.addObject("msg_size", msgs.get(0));
		mv.addObject("user", user);
		mv.addObject("integralViewTools", integralViewTools);
		
		
		//团购订单数
		params.clear();
		params.put("user_id", user.getId().toString());
		List<OrderForm> gList = orderformService.query("select count(obj.id) from OrderForm obj where obj.order_status > 10" +
				" and obj.order_cat = 3 and obj.user_id=:user_id", params, -1, -1);
		String gOCount = "";
		if(CommUtil.null2Int(gList.get(0))>9){
			gOCount = "9+";
		}else{
			gOCount = gList.get(0)+"";
		}
		mv.addObject("gOCount",gOCount);
		return mv;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param password
	 * @param new_password
	 * @param code
	 * @return
	 */

	@SecurityMapping(title = "用户中心修改密码", value = "/wap/buyer/edit_password.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/edit_password.htm")
	public ModelAndView edit_password(HttpServletRequest request,
			HttpServletResponse response, String password, String new_password,
			String code) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/edit_password.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	@SecurityMapping(title = "用户中心完善资料", value = "/wap/buyer/datum.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/datum.htm")
	public ModelAndView datum(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/datum.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	@SecurityMapping(title = "用户中心完善资料1", value = "/wap/buyer/datum1.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/datum1.htm")
	public ModelAndView datum1(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/datum1.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}
	/**
	 * 完善资料2
	 * 
	 * @param request
	 * @param userName
	 * @param password
	 * @param email
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	@RequestMapping("/wap/buyer/datum2.htm")
	public String datum2(HttpServletRequest request,HttpServletResponse response, 
			String userName, String password) throws HttpException,
			IOException {
		boolean reg = true;
		// 进一步控制用户名不能重复
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userName", userName);
		List<User> users = this.userService.query(
				"select obj from User obj where obj.userName=:userName",
				params, -1, -1);
		if (users != null && users.size() > 0) {
			reg = false;
		}
		if(reg){
			User user = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId());
			user.setUserName(userName);
			user.setMobile(userName);
			user.setPassword(password);
			user.setUserMark(null);
			this.userService.save(user);
		}
		return "redirect:/wap/buyer/center.htm";
	}

	/**
	 * 用户中心修改密码保存
	 * 
	 * @param request
	 * @param response
	 * @param password
	 * @param new_password
	 * @param code
	 * @return
	 */
	@SecurityMapping(title = "用户中心修改密码保存", value = "/wap/buyer/edit_password_save.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/edit_password_save.htm")
	public ModelAndView edit_password_save(HttpServletRequest request,
			HttpServletResponse response, String password, String new_password) {
		ModelAndView mv = new JModelAndView("wap/error.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		if (user.getPassword().equals(Md5Encrypt.md5(password))) {
			user.setPassword(Md5Encrypt.md5(new_password));
			this.userService.update(user);
			mv = new JModelAndView("wap/success.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "修改密码成功！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/center.htm");
		} else {
			mv.addObject("op_title", "原始密码错误！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/edit_password.htm");
		}
		return mv;
	}

	/**
	 * 手机端用户优惠券
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "用户优惠券", value = "/wap/buyer/coupon.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/coupon.htm")
	public ModelAndView buyer_coupon(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/coupon.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		if (user != null) {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("user_id", user.getId());
			List<CouponInfo> couponinfos = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id",
							params, -1, -1);
			mv.addObject("couponinfos", couponinfos);
		}
		return mv;
	}

	/**
	 * 手机端用户收藏的商品
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "移动端商品收藏", value = "/wap/buyer/favorite.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/favorite.htm")
	public ModelAndView favorite(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/favorite.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		params.put("type", 0);
		List<Favorite> favorites = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id=:user_id and obj.type=:type order by obj.addTime desc",
						params, -1, -1);
		mv.addObject("objs", favorites);
		return mv;
	}
	/**
	 * 手机端用户收藏的商品
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "移动端店铺收藏", value = "/wap/buyer/favorite_store.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/favorite_store.htm")
	public ModelAndView favorite_store(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/favorite_store.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		params.put("type", 1);
		List<Favorite> favorites = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id=:user_id and obj.type=:type order by obj.addTime desc",
						params, -1, -1);
		mv.addObject("objs", favorites);
		mv.addObject("storeViewTools", storeViewTools);
		return mv;
	}

	/**
	 * 手机端用户收藏的商品删除
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "移动端商品取消收藏", value = "/wap/buyer/favorite_del.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/favorite_del.htm")
	public String favorite_del(HttpServletRequest request,
			HttpServletResponse response, String id) {
		Favorite favorite = this.favoriteService.getObjById(CommUtil
				.null2Long(id));
		if (favorite != null
				&& favorite.getUser_id().equals(
						SecurityUserHolder.getCurrentUser().getId())) {
			this.favoriteService.delete(CommUtil.null2Long(id));
		}
		return "redirect:/wap/buyer/favorite.htm";
	}
	/**
	 * 手机端用户收藏的商品删除
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "移动端商品取消收藏", value = "/wap/buyer/favorite_store_del.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/favorite_store_del.htm")
	public String favorite_store_del(HttpServletRequest request,
			HttpServletResponse response, String id) {
		Favorite favorite = this.favoriteService.getObjById(CommUtil
				.null2Long(id));
		if (favorite != null
				&& favorite.getUser_id().equals(
						SecurityUserHolder.getCurrentUser().getId())) {
			this.favoriteService.delete(CommUtil.null2Long(id));
		}
		return "redirect:/wap/buyer/favorite_store.htm";
	}

	/**
	 * 手机端用户的消息
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "用户消息", value = "/wap/buyer/message_list.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/message_list.htm")
	public ModelAndView message_list(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/message_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		long user_id = SecurityUserHolder.getCurrentUser().getId();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("uid", user_id);
		List<Message> messages = this.messageService
				.query("select obj from Message obj where obj.toUser.id=:uid order by obj.addTime desc",
						map, -1, -1);
		mv.addObject("objs", messages);
		return mv;
	}
	/**
	 * 手机端用户的消息
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "用户服务中心", value = "/wap/buyer/service_center.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/service_center.htm")
	public ModelAndView service_center(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/service_center.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}
	
	/**
	 * 手机端用户中心关注
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@SecurityMapping(title = "用户服务中心", value = "/wap/buyer/about.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/about.htm")
	public ModelAndView about(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("user/wap/usercenter/about.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}
	
		/**
			 * wap用户中心导航
			 */
			@SecurityMapping(title = "买家中心导航", value = "/buyer/nav.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
			@RequestMapping("/wap/buyer/nav.htm")
			public ModelAndView nav(HttpServletRequest request,
					HttpServletResponse response) {
				ModelAndView mv = new JModelAndView(
						"user/wap/usercenter/centre_left.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request, response);
			/*	String op = CommUtil.null2String(request.getAttribute("op"));
				mv.addObject("op", op);
				List<Message> msgs = new ArrayList<Message>();
				if (SecurityUserHolder.getCurrentUser() != null) {
					Map map = new HashMap();
					map.put("status", 0);
					map.put("user_id", SecurityUserHolder.getCurrentUser().getId());
					msgs = this.messageService
							.query("select count(obj.id) from Message obj where obj.status=:status and obj.toUser.id=:user_id and obj.parent.id is null",
									map, -1, -1);
					mv.addObject("msg_size", msgs.get(0));
				}*/
				return mv;
			}


}
