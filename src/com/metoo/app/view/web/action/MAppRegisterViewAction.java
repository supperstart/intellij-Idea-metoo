package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MobileTools;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Address;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Document;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.Role;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.AddressQueryObject;
import com.metoo.foundation.service.IAddressService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IDocumentService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IRoleService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.msg.MsgTools;

@Controller
@RequestMapping("/app/")
public class MAppRegisterViewAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IDocumentService documentService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private MobileTools mobileTools;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private MsgTools msgTools;

	private static final String REGEX1 = "(.*管理员.*)";
	private static final String REGEX2 = "(.*admin.*)";

	@RequestMapping("v1/register.json")
	public void register_json(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> registermap = new HashMap<String, Object>();
		request.getSession(false).removeAttribute("verify_code");
		Document doc = this.documentService.getObjByProperty(null, "mark", "reg_agree");
		registermap.put("dos_content", doc.getContent());
		registermap.put("dos_id", doc.getId());
		Result result = new Result(0, "success", registermap);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @RequestMapping("/verify_username.json")
	@RequestMapping("v1/verify_username.json")
	public void verify_username(HttpServletRequest request, HttpServletResponse response, String userName, String id) {
		int ret = 1;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userName", userName.replace(" ", ""));
		params.put("id", CommUtil.null2Long(id));
		List<User> users = this.userService.query(
				"select obj.id from User obj where (obj.userName=:userName or obj.mobile=:userName or obj.email=:userName) and obj.id!=:id",
				params, -1, -1);
		if (users.size() > 0) {
			ret = 0;
		}
		Result result = new Result(ret);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 验证Email
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 */
	@RequestMapping("v1/verify_email.json")
	public void verify_email(HttpServletRequest request, HttpServletResponse response, String email, String id) {
		int ret = -1;
		String msg = "";
		if (!CommUtil.null2String(email).equals("")) {
			Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
			Matcher matcher = pattern.matcher(CommUtil.null2String(email));
			if (matcher.matches()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("email", email);
				params.put("id", CommUtil.null2Long(id));
				List<User> users = this.userService
						.query("select obj.id from User obj where obj.email=:email and obj.id!=:id", params, -1, -1);
				if (users.size() == 0) {
					ret = 4200;
					msg = "Successfully";
				}
			} else {
				ret = 4400;
				msg = "Email format error";
			}
		}
		Result result = new Result(ret, msg);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @description 自动注册验证用户邮箱是否被占用
	 * @param request
	 * @param response
	 * @param email
	 * @param id
	 */
	@RequestMapping("v2/verify_email.json")
	public void verify_email2(HttpServletRequest request, HttpServletResponse response, String email, String mobile, String id) {
		int code = -1;
		String msg = "";
		if (!CommUtil.null2String(email).equals("")) {
			Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
			Matcher matcher = pattern.matcher(CommUtil.null2String(email));
			Map map = this.mobileTools.mobile(mobile);
			if (matcher.matches()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("email", email);
				params.put("id", CommUtil.null2Long(id));
				List<User> users = this.userService
						.query("select obj from User obj where obj.email=:email and obj.id!=:id", params, -1, -1);
				if (users.size() > 0) {
					boolean flag = false;
					for(User user : users){
						if(map.get("areaMobile").toString().equals(user.getTelephone())){
							flag = true;
							break;
						}
					}if(flag){
						code = 4200;
						msg = "Successfully";
					}else{
						code = 4300;
						msg = "The mailbox is already in use";
					}
				}else{
					code = 4200;
					msg = "Successfully";
				}
			} else {
				code = 4400;
				msg = "Email format error";
			}
		} else {
			code = 4403;
			msg = "The mailbox is empty";
		}
		Result result = new Result(code, msg);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * 验证Mobile
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 */
	@RequestMapping("v1/verify_mobile.json")
	public void verify_mobile(HttpServletRequest request, HttpServletResponse response, String mobile, String id) {
		int code = -1;
		String msg = "";
		boolean flag = this.mobileTools.verify(mobile);
		Map map = this.mobileTools.mobile(mobile);
		String areaMobile = (String) map.get("areaMobile");
		String userMobile = (String) map.get("userMobile");
		if(flag){
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("mobile", userMobile);
			params.put("userName", areaMobile);
			params.put("id", CommUtil.null2Long(id));
			List<User> users = this.userService.query(
					"select obj.id from User obj where obj.mobile=:mobile or obj.userName=:userName and obj.id!=:id",
					params, -1, -1);
			if (users.size() > 0) {
				code = 4300;
				msg = "The current account is registered";
			} else {
				code = 4200;
				msg = "Successfully";
			}
		}else{
			code = 4400;
			msg = "";
		}
		this.send_json(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	/**
	 * @description 用户注册
	 * @param request
	 * @param response
	 * @param userName
	 * @param password
	 * @param email
	 * @param mobile
	 * @param code
	 * @param user_type
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@EmailMapping(title = "手动注册", value = "register_finish")
	@RequestMapping("v1/register_finish.json")
	public void register_finish_json(HttpServletRequest request, HttpServletResponse response, String userName,
			String password, String email, String mobile, String code, String user_type, String invitation,
			@RequestParam(value = "imei", required = true) String imei) throws HttpException, InterruptedException {
		Result result = null;
		Map<String, Object> registerMap = new HashMap<String, Object>();
		List<User> invi = new ArrayList<User>();
		boolean flag = false;
		boolean register = false;
		// 使用邀请码时检测设备，一台设备只允许使用一次邀请码
		if (invitation != null && !invitation.equals("")) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("code", invitation);
			invi = this.userService.query("select obj from User obj where obj.code=:code", params, -1, -1);
			if (invi.size() < 0) {
				result = new Result(4205, "邀请码错误");
			} else {
				if (!imei.equals("")) {
					User imeiUser = this.userService.getObjByProperty(null, "imei", imei);
					if (imeiUser != null) {
						result = new Result(4215, "邀请码在同一台设备");
					} else {
						flag = true;
					}
				}
			}
		} else {
			register = true;
		}
		if (flag || register) {
			try {
				boolean reg = true;
				// 防止机器注册，如后台开启验证码则强行验证验证码
				/*
				 * if (code != null && !code.equals("")) { code =
				 * CommUtil.filterHTML(code);// 过滤验证码 } //
				 * System.out.println(this.configService.getSysConfig().
				 * isSecurityCodeRegister()); if
				 * (this.configService.getSysConfig().isSecurityCodeRegister ())
				 * { if (!request.getSession(false).getAttribute("verify_code")
				 * .equals(code)) { reg = false; } }
				 */
				// 禁止用户注册带有 管理员 admin 等字样用户名
				if (userName.matches(REGEX1) || userName.toLowerCase().matches(REGEX2)) {
					reg = false;
				}
				if (reg) {
					Map<String, String> map = new HashMap<String, String>();
					if (mobile != null && !mobile.equals("")) {
						boolean mFlag = this.mobileTools.verify(mobile);
						if (mFlag) {
							map = this.mobileTools.mobile(mobile);
							mobile = (String) map.get("areaMobile");
						}
					}
					User user = new User();
					user.setUserName(userName.replace(" ", ""));
					user.setUserRole("BUYER");
					user.setAddTime(new Date());
					user.setEmail(email);
					user.setMobile(map.get("userMobile"));
					user.setTelephone(map.get("areaMobile"));
					user.setAvailableBalance(BigDecimal.valueOf(0));
					user.setFreezeBlance(BigDecimal.valueOf(0));
					user.setPassword(Md5Encrypt.md5(password).toLowerCase());
					user.setPwd(password);
					user.setImei(imei);
					// user.setRaffle(configService.getSysConfig().getRegister_lottery());

					Map<String, Object> params = new HashMap<String, Object>();
					params.put("type", "BUYER");
					List<Role> roles = this.roleService.query("select new Role(id) from Role obj where obj.type=:type",
							params, -1, -1);
					user.getRoles().addAll(roles);
					String query = "select * from metoo_lucky_draw where switchs = 1";
					ResultSet res = this.databaseTools.selectIn(query);
					int lucky = 0;
					while (res.next()) {
						lucky = res.getInt("register");
					}
					user.setRaffle(lucky);
					registerMap.put("raffle", lucky);
					if (this.configService.getSysConfig().isIntegral()) { // [积分]
						user.setIntegral(this.configService.getSysConfig().getMemberRegister());
						if (invitation != null && !invitation.equals("")) {
							params.clear();
							params.put("code", invitation);
							List<User> objs = this.userService.query("select obj from User obj where obj.code=:code",
									params, -1, -1);
							// User obj =
							// this.userService.getObjByProperty(null, code,
							// invitation);
							if (!objs.isEmpty()) {
								User obj = objs.get(0);
								obj.setPointNum(obj.getPointNum() + 1);
								user.setPointName(obj.getUsername());
								user.setPointId(obj.getId());
								user.setParent(obj);
								this.userService.update(obj);
							}
						}
						this.userService.save(user);
						IntegralLog log = new IntegralLog();
						log.setAddTime(new Date());
						log.setContent("用户注册增加" + this.configService.getSysConfig().getMemberRegister() + "分");
						log.setIntegral(this.configService.getSysConfig().getMemberRegister());
						log.setIntegral_user(user);
						log.setType("reg");
						this.integralLogService.save(log);
					} else {
						if (invitation != null && !invitation.equals("")) {
							params.clear();
							params.put("code", invitation);
							List<User> objs = this.userService.query("select obj from User obj where obj.code=:code",
									params, -1, -1);
							// User obj =
							// this.userService.getObjByProperty(null, code,
							// invitation);
							if (!objs.isEmpty()) {
								User obj = objs.get(0);
								obj.setPointNum(obj.getPointNum() + 1);
								// user.setPointName(obj.getUsername());
								// user.setPointId(obj.getId());
								this.userService.update(obj);
							}
						}
						this.userService.save(user);
					}
					// 创建用户默认相册
					Album album = new Album();
					album.setAddTime(new Date());
					album.setAlbum_default(true);
					album.setAlbum_name("默认相册");
					album.setAlbum_sequence(-10000);
					album.setUser(user);
					this.albumService.save(album);
					request.getSession(false).removeAttribute("verify_code");
					params.clear();
					params.put("employ_type", 1);// 注册优惠券
					List<Coupon> coupons = this.couponService
							.query("select obj from Coupon obj where obj.employ_type=:employ_type", params, -1, -1);
					for (Coupon coupon : coupons) {
						int size = coupon.getCouponinfos().size();
						if (size <= coupon.getCoupon_count() || coupon.getCoupon_count() == 0) {
							CouponInfo info = new CouponInfo();
							info.setAddTime(new Date());
							info.setUser(user);
							info.setCoupon(coupon);
							info.setCoupon_sn(UUID.randomUUID().toString());
							info.setStore_id(CommUtil.null2Long("-1"));
							this.couponInfoService.save(info);
							registerMap.put("coupon_amount", coupon.getCoupon_amount());
						}
					}
					result = new Result(0, "success", registerMap);
				} else {
					result = new Result(4204, "注册名中不允许存在'admin and 管理员'");
				}
			} catch (Exception e) {
				result = new Result(2, "error");
			}
		} else {
			result = new Result(4, "违规注册");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	@EmailMapping(title = "自动注册", value = "wap_register")
	@RequestMapping("v1/wap_register.json")
	public void wap_register(HttpServletRequest request, HttpServletResponse response, String currentPage,
			String area_id, String area_info, String mobile, String true_name, String email)
			throws SQLException, UnsupportedEncodingException {
		ModelAndView mv = new JModelAndView("admin/blue/goods_list.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Map map = new HashMap();
		Map<String, Object> registerMap = new HashMap();
		Result result = null;
		boolean flag = this.mobileTools.verify(mobile);
		if (flag) {
			map = this.mobileTools.mobile(mobile);
			String areaMobile = (String) map.get("areaMobile");
			Map params = new HashMap();
			params.put("email", areaMobile);
			params.put("mobile", areaMobile);
			params.put("userName", areaMobile.replace(" ", ""));
			params.put("areaMobil", areaMobile);
			params.put("deleteStatus", 0);
			List<User> users = this.userService.query(
					"select obj from User obj where obj.deleteStatus=:deleteStatus and obj.userName =:userName or obj.userName =:areaMobil or obj.email=:email or obj.mobile=:mobile",
					params, -1, -1);
			User user = null;
			if (users.size() > 0) {
				user = users.get(0);
				user.setEmail(email);
				this.userService.update(user);
			} else {
				user = new User();
				user.setUserName(map.get("areaMobile").toString());
				user.setMobile(map.get("userMobile").toString());
				user.setTelephone(map.get("areaMobile").toString());
				user.setAddTime(new Date());
				user.setUserRole("BUYER");
				user.setPassword(Md5Encrypt.md5("123456").toLowerCase());
				user.setPwd("123456");
				user.setAutomatic("1");
				user.setEmail(email);
				params.clear();
				params.put("type", "BUYER");
				List<Role> roles = this.roleService.query("select new Role(id) from Role obj where obj.type=:type",
						params, -1, -1);
				user.getRoles().addAll(roles);
				String query = "select * from metoo_lucky_draw where switchs = 1";
				ResultSet res = this.databaseTools.selectIn(query);
				int lucky = 0;
				while (res.next()) {
					lucky = res.getInt("register");
				}
				map.put("raffle", lucky);
				user.setRaffle(lucky);
				registerMap.put("raffle", lucky);
				this.userService.save(user);
			}
			AddressQueryObject qo = new AddressQueryObject(currentPage, mv, null, null);
			qo.addQuery("obj.user.id", new SysMap("user_id", CommUtil.null2Long(user.getId())), "=");
			qo.addQuery("obj.area.id", new SysMap("area_id", CommUtil.null2Long(area_id)), "=");
			qo.addQuery("obj.area_info", new SysMap("area_info", area_info), "=");
			qo.addQuery("obj.mobile", new SysMap("mobile", map.get("areaMobile").toString()), "=");
			qo.addQuery("obj.trueName", new SysMap("true_name", true_name), "=");
			IPageList pList = this.addressService.list(qo);
			List<Address> addressList = pList.getResult();
			Address address = new Address();
			if (addressList.size() == 0) {
				address.setAddTime(new Date());
				address.setTrueName(true_name);
				address.setArea_info(area_info);
				address.setMobile(map.get("phoneNumber").toString());
				address.setTelephone(map.get("phoneNumber").toString());
				address.setDefault_val(1);
				Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
				address.setArea(area);
				User addressUser = this.userService.getObjById(CommUtil.null2Long(user.getId()));
				address.setUser(addressUser);
				address.setEmail(email);
				this.addressService.save(address);
			}

			// 给自动注册买家发送注册通知短信
			if (this.configService.getSysConfig().isSmsEnbale()) {
				String sms_mobile = mobile;
				/*
				 * String content =
				 * "Congratulations on the success of your order in Soarmall." +
				 * " Your account: " + map.get("areaMobile").toString() +
				 * " Your password: 123456" +
				 * " App download link:http://app.soarmall.com/download/ " +
				 * " Contact us: " + " service@soarmall.com" +
				 * " WhatsApp: + 86 18900700488";
				 */

				String content = "Thank you for browsing soarmall, our website insist on giving the best service and goods to every customer."
						+ " Account: " + map.get("areaMobile").toString() + " Password: 123456"
						+ " Welcome to the best shopping website soarmall!" + " WhatsApp: + 86 18900700488"
						+ " Email: service@soarmall.com";
				if (!areaMobile.equals("88888888")) {
					this.msgTools.sendSMS(sms_mobile, content);
				}
			}
			registerMap.put("pwd", "123456");
			registerMap.put("userName", user.getUserName());
			registerMap.put("phoneNumber", map.get("phoneNumber").toString());
			result = new Result(4200, "Successfully", registerMap);
		} else {
			result = new Result(4400, "Wrong number format");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	private void send_json(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
