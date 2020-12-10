package com.metoo.app.v1.manage.buyer.action;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.EvaluateQueryObject;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.ImageTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.buyer.tools.EvaluateTools;

@Controller
@RequestMapping("/app/")
public class MEvaluateBuyerAction {
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private ImageTools imageTools;
	@Autowired
	private EvaluateTools evaluateTools;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IUserService userService;
	
	@SecurityMapping(title = "买家评价列表", value = "/buyer/evaluate_list.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	/*@RequestMapping("/buyer_evaluate_list.json")*/
	@RequestMapping(value = "v1/evaluate_list.json", method = RequestMethod.GET)
	public void evaluate_list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String token, String language) {
		ModelAndView mv = new JModelAndView(
				"null",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Result result = null;
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				EvaluateQueryObject qo = new EvaluateQueryObject(currentPage, mv,
						"addTime", "desc");
				qo.addQuery("obj.evaluate_user.id", new SysMap("user_id",
						user.getId()), "=");
				IPageList pList = this.evaluateService.list(qo);
				List<Evaluate> evaluates = pList.getResult();
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for(Evaluate evaluate : evaluates){
					Map<String, Object> map = new HashMap<String, Object>();
					Goods obj = evaluate.getEvaluate_goods();
					map.put("goods_id", obj.getId());
					map.put("goods_name", obj.getGoods_name());
					if("1".equals(language)){
						map.put("goods_name", obj.getKsa_goods_name() != null &&
														!"".equals(obj.getKsa_goods_name()) 
														? "^"
														+ obj.getKsa_goods_name() 
														: obj.getGoods_name());
					}
					map.put("goods_current_price", obj.getGoods_current_price());
					map.put("goods_photo", obj.getGoods_main_photo() != null
													? imageWebServer + "/" 
													+ obj.getGoods_main_photo().getPath() 
													+ "/"
													+ obj.getGoods_main_photo().getName() 
													+ "_middle." 
													+ obj.getGoods_main_photo().getExt()
													: imageWebServer);
					map.put("id", evaluate.getId());
					map.put("order_id", evaluate.getOf() == null ? "" : evaluate.getOf().getId());
					map.put("Add_Time", evaluate.getAddTime());
					map.put("evaluate_buyer_val", evaluate.getEvaluate_buyer_val());
					map.put("service_evaluate", evaluate.getService_evaluate());
					map.put("description_evaluate", evaluate.getDescription_evaluate());
					map.put("ship_evaluate", evaluate.getShip_evaluate());
					map.put("evaluate_info", evaluate.getEvaluate_info());
					map.put("evaluate_status", evaluate.getEvaluate_status());
					List<Accessory> accessorys = imageTools
														.queryImgs(evaluate.getEvaluate_photos());
					List<Map<String, Object>> evaluate_list 
														= new ArrayList<Map<String, Object>>();
					if(accessorys.size() > 0){
						for(Accessory accessory : accessorys){
							Map<String, Object> photoMap = new HashMap<String, Object>();
							photoMap.put("photo", this.configService.getSysConfig().getImageWebServer()
															+ "/"
															+ accessory.getPath()
															+ "/"
															+ accessory.getName());
							evaluate_list.add(photoMap);
						}
						map.put("photos", evaluate_list);
					}
					map.put("Addeva_info", evaluate.getAddeva_info());//追评
					map.put("Addeva_status", evaluate.getAddeva_status());
					map.put("Addeva_time", evaluate.getAddeva_time());
					List<Accessory> add_accessorys = imageTools.queryImgs(evaluate.getAddeva_photos());
					List<Map<String, String>> add_list = new ArrayList<Map<String, String>>();
					if(add_accessorys.size() > 0){
						for(Accessory accessory : add_accessorys){
							Map<String, String> photo_map = new HashMap<String, String>();
							photo_map.put("photo", this.configService.getSysConfig().getImageWebServer()
															+ "/"
															+ accessory
																.getPath()
															+ "/"
															+ accessory
																.getName());
							add_list.add(photo_map);
						
						}
						map.put("eva_photo", add_list);
					}
					map.put("reply_status", evaluate.getReply_status());// 评价回复的状态默认为0未回复 已回复为1；
					map.put("reply", evaluate.getReply());// 评价的回复
					map.put("evaluate_buyer_val", evaluate.getEvaluate_buyer_val());//评价类型// 买家评价，评价类型，1为好评，0为中评，-1为差评
					int flag = 0;
					if(evaluate.getAddeva_status() == 0){
						flag = evaluateTools.evaluate_add_able1(evaluate.getAddTime());
					}else{
						flag = 1;	
					}
					map.put("flag", flag);
					list.add(map);
				}
				result = new Result(4200, "Successfully" ,list);
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @description 买家追评保存
	 * @param request
	 * @param response
	 * @param id
	 * @param token
	 */
/*	@RequestMapping("/buyer_add_evaluate.json")*/
	@RequestMapping(value = "v1/add_evaluate.json", method = RequestMethod.POST)
	public void add_evaluate(HttpServletRequest request, 
			HttpServletResponse response, String id, String token, String language){
		int code = -1;
		String msg = "";
		Map<String, Object> evaluatemap = new HashMap<String, Object>();
		if(token != null && !token.equals("")){
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user != null){
				Evaluate evaluate = this.evaluateService.getObjById(CommUtil
						.null2Long(id));
				if(evaluate != null){
					Goods goods = evaluate.getEvaluate_goods();
					OrderForm of = evaluate.getOf();
					if(of != null){
						if(orderFormTools.evaluate_able(evaluate.getAddTime()) == 0){
							code = 4251;
							msg = "已超出评价追加期限";
						}else{
							if(evaluate.getAddeva_status() == 0
									&& goods != null
									&& of.getUser_id().equals(user.getId().toString())){
								String store_name = of.getStore_name();
								List<Map> goods_info= orderFormTools.queryGoodsInfo(of.getGoods_info());
								for (Map map : goods_info) {
									if(goods.getId().toString().equals(map.get("goods_id").toString())){
										evaluatemap.put("goods_id", map.get("goods_id"));
										evaluatemap.put("goods_name", map.get("goods_name"));
										if("1".equals(language)){
											evaluatemap.put("goods_name", map.get("ksa_goods_name") == null && "".equals(map.get("ksa_goods_name").toString())? map.get("goods_name") : "^"+map.get("ksa_goods_name"));
										}
										evaluatemap.put("eva_goods_count", map.get("goods_count"));
										evaluatemap.put("eva_goods_price", map.get("goods_price"));
										evaluatemap.put("eva_goods_type", map.get("goods_type"));
										evaluatemap.put("eva_goods_gsp_val", map.get("goods_gsp_val"));
										evaluatemap.put("goods_color", map.get("color"));
										evaluatemap.put("eva_goods_main_photo_path", this.configService.getSysConfig().getImageWebServer()+"/"+map.get("goods_mainphoto_path"));
										evaluatemap.put("eva_goods_domainPath", map.get("goods_domainPath"));
										evaluatemap.put("eva_combin_suit_info", map.get("combin_suit_info"));
										evaluatemap.put("store_name", store_name);
									}
								}
								evaluatemap.put("id", id);
								String evaluate_session = CommUtil.randomString(32);
								request.getSession(false).setAttribute("evaluate_session", evaluate_session);
								evaluatemap.put("evaluate_session", evaluate_session);
//								if (of.getOrder_status() >= 50) {
//									code = 4200;
//									msg = "Successfully";
//								}else{
//									code = 4206;
//									msg = "该订单还未确认收货";
//								}
								code = 4200;
								msg = "Successfully";
							}else{
								code = 4205;
								msg = "商品信息异常 or 不允许重复追加 or 该订单异常";
							}
						}
					}else{
						code = 4205;
						msg = "商品信息异常 or 不允许重复追加 or 该订单异常";
					}
				
				}else{
					code = 4205;
					msg = "商品信息异常";
				}
			}else{
				code = -100;
				msg = "登陆异常";
			}
		}else{
			code = -100;
			msg = "登陆异常";
		}
		Result result = new Result(code, msg, evaluatemap);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
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
	 * @description 追加评论保存
	 * @param request
	 * @param response
	 * @param jsessionid
	 * @param token
	 * @param id
	 */
//	@RequestMapping("/buyer_add_evaluate_save.json")
	@RequestMapping(value = "v1/add_evaluate_save.json", method = RequestMethod.POST)
	public void add_evaluate_save(HttpServletRequest request,
			HttpServletResponse response, String jsessionid, 
			String token, @RequestParam(value = "id", required = true)String id){
		int code = -1;
		String msg = "";
		if(jsessionid != null && !jsessionid.equals("") && !id.equals("")){
			Evaluate evaluate = this.evaluateService
					.getObjById(CommUtil.null2Long(id));
			Goods goods = evaluate.getEvaluate_goods();
			OrderForm of = evaluate.getOf();
			String session = (String)request.getSession(false).getAttribute("evaluate_session");
			if(of != null){
				if(orderFormTools.evaluate_able(evaluate.getAddTime()) == 0){
					code = 4251;
					msg = "已超出评价追加期限";
				}else{
					if(session != null &&
							session.equals(jsessionid)){
						request.getSession(false).removeAttribute("evaluate_session");
						if(goods != null){
							User user = this.userService.getObjByProperty(null, "app_login_token", token);
							if(user != null && of.getUser_id().equals(user.getId().toString())){
//								if(of.getOrder_status() == 50){
//									OrderFormLog log = new OrderFormLog();
//									log.setAddTime(new Date());
//									log.setLog_info("追加评价订单");
//									log.setLog_user(user);
//									log.setOf(of);
//									this.orderFormLogService.save(log);
									if(evaluate.getAddeva_status() == 0){
										evaluate.setAddeva_status(1);
										evaluate.setAddeva_info(request.getParameter("evaluate_info"));
										evaluate.setAddeva_time(new Date());
										evaluate.setAddeva_photos(request.getParameter("evaluate_photos"));
										this.evaluateService.save(evaluate);
									}
									code = 4200;
									msg = "Successfully";
								//}
							}else{
								code = -100;
								msg = "You don't have this order";	
							}
						}else{
							code = 4206;
							msg = "The goods or item does not exist";
						}
					}else{
						code = 4401;
						msg = "No repeat evaluation";
					}
				}
			}else{
				code = 4205;
				msg = "The order does not exist";
			}
		}else{
			code = 4400;
			msg = "Comment is invalid";
		}
		Result result = new Result(code, msg);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
