package com.metoo.manage.admin.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Advert;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.ComplaintGoods;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.GoldLog;
import com.metoo.foundation.domain.GoldRecord;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GroupInfo;
import com.metoo.foundation.domain.GroupLifeGoods;
import com.metoo.foundation.domain.IntegralGoodsOrder;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.Message;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.domain.PredepositCash;
import com.metoo.foundation.domain.Role;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreGrade;
import com.metoo.foundation.domain.StorePoint;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.SysLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.UserQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAdvertService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IComplaintGoodsService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.IDeliveryAddressService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoldLogService;
import com.metoo.foundation.service.IGoldRecordService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsSpecificationService;
import com.metoo.foundation.service.IGroupInfoService;
import com.metoo.foundation.service.IGroupLifeGoodsService;
import com.metoo.foundation.service.IIntegralGoodsOrderService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IMessageService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IPredepositCashService;
import com.metoo.foundation.service.IPredepositService;
import com.metoo.foundation.service.IRoleService;
import com.metoo.foundation.service.IStoreGradeService;
import com.metoo.foundation.service.IStorePointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ISysLogService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserGoodsClassService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.StoreTools;
import com.metoo.module.sns.domain.SnsAttention;
import com.metoo.module.sns.service.ISnsAttentionService;

@Controller
public class UserManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IStoreGradeService storeGradeService;
	@Autowired
	private IMessageService messageService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IAdvertService advertService;
	@Autowired
	private IPredepositService predepositService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IUserGoodsClassService ugcService;
	@Autowired
	private ISysLogService syslogService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IGroupLifeGoodsService grouplifegoodsService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGroupInfoService groupinfoService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IPayoffLogService paylogService;
	@Autowired
	private IGoodsSpecPropertyService specpropertyService;
	@Autowired
	private IGoodsSpecificationService specService;
	@Autowired
	private IGoldLogService goldlogService;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IComplaintGoodsService complaintGoodsService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IGoldRecordService grService;
	@Autowired
	private IStorePointService storepointService;
	@Autowired
	private IGoldLogService glService;
	@Autowired
	private IPredepositCashService redepositcashService;
	@Autowired
	private StoreTools storeTools;
	@Autowired
	private IIntegralGoodsOrderService integralGoodsOrderService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private ISnsAttentionService snsAttentionService;
	@Autowired
	private IDeliveryAddressService deliveryAddressService;

	@SecurityMapping(title = "会员添加", value = "/admin/user_add.htm*", rtype = "admin", rname = "会员管理", rcode = "user_manage", rgroup = "会员")
	@RequestMapping("/admin/user_add.htm")
	public ModelAndView user_add(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("admin/blue/user_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}

	@SecurityMapping(title = "会员编辑", value = "/admin/user_edit.htm*", rtype = "admin", rname = "会员管理", rcode = "user_manage", rgroup = "会员")
	@RequestMapping("/admin/user_edit.htm")
	public ModelAndView user_edit(HttpServletRequest request,
			HttpServletResponse response, String id, String op) {
		ModelAndView mv = new JModelAndView("admin/blue/user_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("obj", this.userService.getObjById(Long.parseLong(id)));
		mv.addObject("edit", true);
		return mv;
	}

	@SecurityMapping(title = "企业用户", value = "/admin/company_user.htm*", rtype = "admin", rname = "会员管理", rcode = "user_manage", rgroup = "会员")
	@RequestMapping("/admin/company_user.htm")
	public ModelAndView company_user(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/company_user.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("obj", this.userService.getObjById(Long.parseLong(id)));
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "会员列表", value = "/admin/user_list.htm*", rtype = "admin", rname = "会员管理", rcode = "user_manage", rgroup = "会员")
	@RequestMapping("/admin/user_list.htm")
	public ModelAndView user_list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String condition, String value) {
		ModelAndView mv = new JModelAndView("admin/blue/user_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		UserQueryObject uqo = new UserQueryObject(
				"new User(id, addTime,userName,trueName,email,  mobile,  QQ , WW,  MSN, availableBalance, "
						+ "freezeBlance,  integral,gold, loginCount, lastLoginDate, lastLoginIp)",
				currentPage, mv, orderBy, orderType);
		WebForm wf = new WebForm();
		wf.toQueryPo(request, uqo, User.class, mv);
		condition = request.getParameter("condition");
		value = request.getParameter("value");
		uqo.addQuery("obj.userRole", new SysMap("userRole", "ADMIN"), "!=");
		uqo.addQuery("obj.deleteStatus", new SysMap("deleteStatus", 0), "=");
		if (condition != null && !CommUtil.null2String(value).equals("")) {
			if (condition.equals("userName")) {
				uqo.addQuery("obj.userName", new SysMap("userName", value), "=");
			}
			if (condition.equals("email")) {
				uqo.addQuery("obj.email", new SysMap("email", value), "=");
			}
			if (condition.equals("trueName")) {
				uqo.addQuery("obj.trueName", new SysMap("trueName", value), "=");
			}
			mv.addObject("value", value);
			mv.addObject("condition", condition);
		}
	//	uqo.addQuery("obj.parent.id is null", null);
		IPageList pList = this.userService.list(uqo);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		CommUtil.saveIPageList2ModelAndView(url + "/admin/user_list.htm", "",
				"", pList, mv);
		mv.addObject("userRole", "USER");
		mv.addObject("storeTools", storeTools);
		return mv;
	}

	@SecurityMapping(title = "会员保存", value = "/admin/user_save.htm*", rtype = "admin", rname = "会员管理", rcode = "user_manage", rgroup = "会员")
	@RequestMapping("/admin/user_save.htm")
	public ModelAndView user_save(HttpServletRequest request,
			HttpServletResponse response, String id, String role_ids,
			String list_url, String add_url, String userName, String password) {
		WebForm wf = new WebForm();
		User user = null;
		if (id.equals("")) {
			user = wf.toPo(request, User.class);
			user.setAddTime(new Date());
		} else {
			User u = this.userService.getObjById(Long.parseLong(id));
			user = (User) wf.toPo(request, u);
		}
		if (userName != null && !userName.equals("")) {
			user.setUserName(userName);
		}
		if (password != null && !password.equals("")) {
			user.setPassword(Md5Encrypt.md5(password).toLowerCase());
		}
		if (id.equals("")) {
			user.setUserRole("BUYER");
			user.getRoles().clear();
			Map params = new HashMap();
			params.put("type", "BUYER");
			List<Role> roles = this.roleService.query(
					"select new Role(id) from Role obj where obj.type=:type",
					params, -1, -1);
			user.getRoles().addAll(roles);
			this.userService.save(user);
		/*	// 创建用户默认相册
			Album album = new Album();
			album.setAddTime(new Date());
			album.setAlbum_default(true);
			album.setAlbum_name("默认相册");
			album.setAlbum_sequence(-10000);
			album.setUser(user);
			this.albumService.save(album);*/
		} else {
			this.userService.update(user);
		}
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", list_url);
		mv.addObject("op_title", "保存用户成功");
		if (add_url != null) {
			mv.addObject("add_url", add_url);
		}
		return mv;
	}

	@SecurityMapping(title = "会员删除", value = "/admin/user_del.htm*", rtype = "admin", rname = "会员管理", rcode = "user_manage", rgroup = "会员")
	@RequestMapping("/admin/user_del.htm")
	public String user_del(HttpServletRequest request, String mulitId,
			String currentPage) throws Exception {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				User parent = this.userService.getObjById(Long.parseLong(id));
				if (!parent.getUsername().equals("admin")) {
					for (User user : parent.getChilds()) {
						user.getRoles().clear();
						if (user.getStore() != null) {
							if (parent.getStore() != null) {
								this.store_del(request, user.getStore().getId());// 删除店铺
							}
							Map map = new HashMap();
							map.put("uid", user.getId().toString());
							List<OrderForm> ofs = this.orderFormService
									.query("select obj.id from OrderForm obj where obj.user_id=:uid",
											map, -1, -1);
							for (OrderForm of : ofs) {// 删除订单
								this.orderFormService.delete(of.getId());
							}
						}
						for (CouponInfo ci : parent.getCouponinfos()) {// 用户拥有的优惠券
							this.couponInfoService.delete(ci.getId());
						}
						parent.getCouponinfos().remove(parent.getCouponinfos());
						for (Accessory acc : parent.getFiles()) {// 用户附件
							if (acc.getAlbum() != null) {
								if (acc.getAlbum().getAlbum_cover() != null) {
									if (acc.getAlbum().getAlbum_cover().getId()
											.equals(acc.getId())) {
										acc.getAlbum().setAlbum_cover(null);
										this.albumService
												.update(acc.getAlbum());
									}
								}
							}
							CommUtil.del_acc(request, acc);
							this.accessoryService.delete(acc.getId());
						}
						parent.getFiles().removeAll(parent.getFiles());
						parent.getCouponinfos().remove(parent.getCouponinfos());// 用户的所有购物车
						for (GoodsCart cart : parent.getGoodscarts()) {
							this.goodsCartService.delete(cart.getId());
						}
						// 充值记录
						Map params = new HashMap();
						params.put("uid", user.getId());
						List<PredepositCash> PredepositCash_list = this.redepositcashService
								.query("select obj from PredepositCash obj where obj.cash_user.id=:uid",
										params, -1, -1);
						for (PredepositCash pc : PredepositCash_list) {
							this.redepositcashService.delete(pc.getId());
						}
						// 删除积分订单
						params.clear();
						params.put("user_id", parent.getId());
						List<IntegralGoodsOrder> integralGoodsOrders = this.integralGoodsOrderService
								.query("select obj from IntegralGoodsOrder obj where obj.igo_user.id=:user_id",
										params, -1, -1);
						for (IntegralGoodsOrder integralGoodsOrder : integralGoodsOrders) {
							this.integralGoodsOrderService
									.delete(integralGoodsOrder.getId());
						}
						// 删除积分日志
						params.clear();
						params.put("user_id", parent.getId());
						List<IntegralLog> integralLogs = this.integralLogService
								.query("select obj from IntegralLog obj where obj.integral_user.id=:user_id",
										params, -1, -1);
						for (IntegralLog integralLog : integralLogs) {
							this.integralLogService.delete(integralLog.getId());
						}

						params.clear();
						params.put("uid", user.getId());
						List<GoldLog> GoldLog_list = this.goldlogService
								.query("select obj from GoldLog obj where obj.gl_user.id=:uid",
										params, -1, -1);
						for (GoldLog gl : GoldLog_list) {
							this.goldlogService.delete(gl.getId());
						}
						params.clear();
						params.put("uid", user.getId());
						List<StorePoint> storepoint_list = this.storepointService
								.query("select obj from StorePoint obj where obj.user.id=:uid",
										params, -1, -1);
						for (StorePoint sp : storepoint_list) {
							this.storepointService.delete(sp.getId());
						}
						params.clear();
						params.put("uid", user.getId());// 商家广告
						List<Advert> adv_list = this.advertService
								.query("select obj from Advert obj where obj.ad_user.id=:uid",
										params, -1, -1);
						for (Advert ad : adv_list) {
							this.advertService.delete(ad.getId());
						}
						this.userService.delete(user.getId());
						// 自提点
						if (user.getDelivery_id() != null
								&& !user.getDelivery_id().equals("")) {
							this.deliveryAddressService.delete(user
									.getDelivery_id());
						}
						// 删除sns关注信息
						params.clear();
						params.put("fromUser", user.getId());
						params.put("toUser", user.getId());
						List<SnsAttention> list = this.snsAttentionService
								.query("select obj from SnsAttention obj where obj.fromUser.id=:fromUser or obj.toUser.id=:toUser",
										params, -1, -1);
						for (SnsAttention sa : list) {
							this.snsAttentionService.delete(sa.getId());
						}
						// 删除用户日志
						params.clear();
						params.put("user_id", user.getId());
						List<SysLog> logs = this.syslogService
								.query("select obj from SysLog obj where obj.user_id=:user_id",
										params, -1, -1);
						for (SysLog log : logs) {
							this.syslogService.delete(log.getId());
						}
					}
					//删除用户订单
					Map map = new HashMap();
					map.put("uid", parent.getId().toString());
					List<OrderForm> ofs = this.orderFormService
							.query("select obj from OrderForm obj where obj.user_id=:uid",
									map, -1, -1);
					for (OrderForm of : ofs) {// 删除订单
						this.orderFormService.delete(of.getId());
					}
					
					parent.getRoles().clear();
					if (parent.getStore() != null) {
						this.store_del(request, parent.getStore().getId());
					}
					for (Accessory acc : parent.getFiles()) {// 用户附件
						if (acc.getAlbum() != null) {
							if (acc.getAlbum().getAlbum_cover() != null) {
								if (acc.getAlbum().getAlbum_cover().getId()
										.equals(acc.getId())) {
									acc.getAlbum().setAlbum_cover(null);
									this.albumService.update(acc.getAlbum());
								}
							}
						}
						CommUtil.del_acc(request, acc);
						this.accessoryService.delete(acc.getId());
					}
					parent.getFiles().removeAll(parent.getFiles());
					for (CouponInfo ci : parent.getCouponinfos()) {// 用户拥有的优惠券
						this.couponInfoService.delete(ci.getId());
					}
					parent.getCouponinfos().remove(parent.getCouponinfos());// 用户的所有购物车
					for (GoodsCart cart : parent.getGoodscarts()) {
						this.goodsCartService.delete(cart.getId());
					}
					parent.getGoodscarts().removeAll(parent.getGoodscarts());
					// 充值记录
					Map params = new HashMap();
					params.put("uid", parent.getId());
					List<PredepositCash> PredepositCash_list = this.redepositcashService
							.query("select obj from PredepositCash obj where obj.cash_user.id=:uid",
									params, -1, -1);
					for (PredepositCash pc : PredepositCash_list) {
						this.redepositcashService.delete(pc.getId());
					}
					params.clear();
					params.put("uid", parent.getId());
					List<GoldLog> GoldLog_list = this.goldlogService
							.query("select obj from GoldLog obj where obj.gl_user.id=:uid",
									params, -1, -1);
					for (GoldLog gl : GoldLog_list) {
						this.goldlogService.delete(gl.getId());
					}
					params.clear();
					params.put("uid", parent.getId());// 店铺统计
					List<StorePoint> storepoint_list = this.storepointService
							.query("select obj from StorePoint obj where obj.user.id=:uid",
									params, -1, -1);
					for (StorePoint sp : storepoint_list) {
						this.storepointService.delete(sp.getId());
					}
					params.clear();
					params.put("uid", parent.getId());// 商家广告
					List<Advert> adv_list = this.advertService
							.query("select obj from Advert obj where obj.ad_user.id=:uid",
									params, -1, -1);
					for (Advert ad : adv_list) {
						this.advertService.delete(ad.getId());
					}
					// 自提点
					if (parent.getDelivery_id() != null
							&& !parent.getDelivery_id().equals("")) {
						this.deliveryAddressService.delete(parent
								.getDelivery_id());
					}
					// 删除sns关注信息
					params.clear();
					params.put("fromUser", parent.getId());
					params.put("toUser", parent.getId());
					List<SnsAttention> list = this.snsAttentionService
							.query("select obj from SnsAttention obj where obj.fromUser.id=:fromUser or obj.toUser.id=:toUser",
									params, -1, -1);
					for (SnsAttention sa : list) {
						this.snsAttentionService.delete(sa.getId());
					}
					// 删除用户日志
					params.clear();
					params.put("user_id", parent.getId());
					List<SysLog> logs = this.syslogService
							.query("select obj from SysLog obj where obj.user_id=:user_id",
									params, -1, -1);
					for (SysLog log : logs) {
						this.syslogService.delete(log.getId());
					}
					params.clear();
				/*	params.put("type", 0);*/
					params.put("user_id", parent.getId());
					List<Favorite> favorites = this.favoriteService.query("select obj from Favorite obj where obj.user_id=:user_id",
							params, -1, -1);
					if(favorites.size() > 0){
						for(Favorite favorite : favorites){
							this.favoriteService.delete(favorite.getId());
						}
					}
					this.userService.delete(parent.getId());
				}
			}
		}
		return "redirect:user_list.htm?currentPage=" + currentPage;
	}

	private void store_del(HttpServletRequest request, Long id)
			throws Exception {
		if (!id.equals("")) {
			Store store = this.storeService.getObjById(id);
			if (store.getUser() != null)
				store.getUser().setStore(null);
			User user = store.getUser();
			if (user != null) {
				user.getRoles().clear();// 删除用户所有权限
				user.setUserRole("BUYER");
				// 给用户赋予买家权限
				Map params = new HashMap();
				params.put("type", "BUYER");
				List<Role> roles = this.roleService.query(
						"select obj from Role obj where obj.type=:type",
						params, -1, -1);
				user.setStore_apply_step(0);
				user.getRoles().addAll(roles);
				this.userService.update(user);
				for (Goods goods : store.getGoods_list()) {// 店铺内的商品
					goods.setGoods_main_photo(null);
					goods.setGoods_brand(null);
					this.goodsService.update(goods);
					goods.getGoods_photos().clear();
					goods.getGoods_specs().clear();
					goods.getGoods_ugcs().clear();
				}
				for (Goods goods : store.getGoods_list()) {// 删除店铺内的商品
					for (GoodsCart gc : goods.getCarts()) {
						this.goodsCartService.delete(gc.getId());
					}
					List<Evaluate> evaluates = goods.getEvaluates();
					for (Evaluate e : evaluates) {
						this.evaluateService.delete(e.getId());
					}
					for (ComplaintGoods cg : goods.getCgs()) {
						this.complaintGoodsService.delete(cg.getId());
					}
					goods.getCarts().removeAll(goods.getCarts());// 移除对象中的购物车
					goods.getEvaluates().removeAll(goods.getEvaluates());
					goods.getCgs().removeAll(goods.getCgs());
					this.goodsService.delete(goods.getId());
				}
				store.getGoods_list().removeAll(store.getGoods_list());
				for (GoldRecord gr : user.getGold_record()) {// 用户充值记录
					this.grService.delete(gr.getId());
				}
				params.clear();
				params.put("uid", user.getId());
				List<GoldLog> gls = this.glService
						.query("select obj from GoldLog obj where obj.gl_user.id=:uid",
								params, -1, -1);
				for (GoldLog gl : gls) {
					this.glService.delete(gl.getId());
				}
				for (GoldRecord gr : user.getGold_record()) {
					this.grService.delete(gr.getId());
				}
				for (GroupLifeGoods glg : user.getGrouplifegoods()) {// 用户发布的生活购
					for (GroupInfo gi : glg.getGroupInfos()) {
						this.groupinfoService.delete(gi.getId());
					}
					glg.getGroupInfos().removeAll(glg.getGroupInfos());
					this.grouplifegoodsService.delete(CommUtil.null2Long(glg
							.getId()));
				}
				for (PayoffLog log : user.getPaylogs()) {// 商家结算日志
					this.paylogService.delete(log.getId());
				}
				for (Album album : user.getAlbums()) {// 商家相册删除
					album.setAlbum_cover(null);
					this.albumService.update(album);
					params.clear();
					params.put("album_id", album.getId());
					List<Accessory> accs = this.accessoryService
							.query("select obj from Accessory obj where obj.album_id=:album_id",
									params, -1, -1);
					for (Accessory acc : accs) {
						CommUtil.del_acc(request, acc);
						this.accessoryService.delete(acc.getId());
					}
					this.albumService.delete(album.getId());
				}
				for (GoodsSpecification spec : store.getSpecs()) {// 店铺规格
					for (GoodsSpecProperty pro : spec.getProperties()) {
						this.specpropertyService.delete(pro.getId());
					}
					spec.getProperties().removeAll(spec.getProperties());
				}
			}
			String path = request.getSession().getServletContext()
					.getRealPath("/")
					+ this.configService.getSysConfig().getUploadFilePath()
					+ File.separator + "store" + File.separator + store.getId();
			CommUtil.deleteFolder(path);
			this.storeService.delete(id);
		}
	}

	@SecurityMapping(title = "会员通知", value = "/admin/user_msg.htm*", rtype = "admin", rname = "会员通知", rcode = "user_msg", rgroup = "会员")
	@RequestMapping("/admin/user_msg.htm")
	public ModelAndView user_msg(HttpServletRequest request,
			HttpServletResponse response, String userName, String list_url) {
		ModelAndView mv = new JModelAndView("admin/blue/user_msg.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<StoreGrade> grades = this.storeGradeService.query(
				"select obj from StoreGrade obj order by obj.sequence asc",
				null, -1, -1);
		mv.addObject("grades", grades);
		if (!"".equals(userName)) {
			mv.addObject("userName", userName);
		}
		if (!"".equals(list_url)) {
			mv.addObject("list_url", list_url);
		}
		return mv;
	}

	@SecurityMapping(title = "会员通知发送", value = "/admin/user_msg_send.htm*", rtype = "admin", rname = "会员通知", rcode = "user_msg", rgroup = "会员")
	@RequestMapping("/admin/user_msg_send.htm")
	public ModelAndView user_msg_send(HttpServletRequest request,
			HttpServletResponse response, String type, String list_url,
			String users, String grades, String content) throws IOException {
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<User> user_list = new ArrayList<User>();
		if (type.equals("all_user")) {
			Map params = new HashMap();
			params.put("userRole", "ADMIN");
			user_list = this.userService
					.query("select obj from User obj where obj.userRole!=:userRole order by obj.addTime desc",
							params, -1, -1);
		}
		if (type.equals("the_user")) {
			List<String> user_names = CommUtil.str2list(users);
			for (String user_name : user_names) {
				User user = this.userService.getObjByProperty(null, "userName",
						user_name);
				user_list.add(user);
			}
		}
		if (type.equals("all_store")) {
			user_list = this.userService
					.query("select obj from User obj where obj.store.id is not null order by obj.addTime desc",
							null, -1, -1);
		}
		if (type.equals("the_store")) {
			Map params = new HashMap();
			Set<Long> grade_ids = new TreeSet<Long>();
			for (String grade : grades.split(",")) {
				grade_ids.add(Long.parseLong(grade));
			}
			params.put("grade_ids", grade_ids);
			user_list = this.userService
					.query("select obj from User obj where obj.store.grade.id in(:grade_ids)",
							params, -1, -1);
		}
		for (User user : user_list) {
			Message msg = new Message();
			msg.setAddTime(new Date());
			msg.setContent(content);
			msg.setFromUser(SecurityUserHolder.getCurrentUser());
			msg.setToUser(user);
			this.messageService.save(msg);
		}
		mv.addObject("op_title", "会员通知发送成功");
		mv.addObject("list_url", list_url);
		return mv;
	}

	@SecurityMapping(title = "会员等级", value = "/admin/user_level.htm*", rtype = "admin", rname = "会员等级", rcode = "user_level", rgroup = "会员")
	@RequestMapping("/admin/user_level.htm")
	public ModelAndView user_level(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("admin/blue/user_level.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		return mv;
	}

	@SecurityMapping(title = "会员等级保存", value = "/admin/user_level_save.htm*", rtype = "admin", rname = "会员等级", rcode = "user_level", rgroup = "会员")
	@RequestMapping("/admin/user_level_save.htm")
	public ModelAndView user_level_save(HttpServletRequest request,
			HttpServletResponse response, String id, String list_url) {
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		SysConfig sc = this.configService.getSysConfig();
		Map map = new HashMap();
		for (int i = 0; i <= 6; i++) {
			map.put("creditrule" + i,
					CommUtil.null2Int(request.getParameter("creditrule" + i)));
		}
		String user_creditrule = Json.toJson(map, JsonFormat.compact());
		sc.setUser_level(user_creditrule);
		if (id.equals("")) {
			this.configService.save(sc);
		} else
			this.configService.update(sc);
		mv.addObject("list_url", list_url);
		mv.addObject("op_title", "保存会员等级成功");
		return mv;
	}
}
