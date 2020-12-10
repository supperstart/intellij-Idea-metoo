package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.FootPoint;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.FootPointQueryObject;
import com.metoo.foundation.domain.virtual.FootPointView;
import com.metoo.foundation.service.IFootPointService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.buyer.tools.FootPointTools;


@Controller
@RequestMapping("/app/")
public class MFootPointBuyerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IFootPointService footPointService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private FootPointTools footPointTools;
	@Autowired
	private IUserService userService;

	@SecurityMapping(title = "用户足迹记录", value = "/buyer/foot_point.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
//	@RequestMapping("/buyer_foot_point.json")
	@RequestMapping(value = "v1/foot_point.json", method = RequestMethod.GET)
	public void metoo_foot_point(HttpServletRequest request,
			HttpServletResponse response, String currentPage,
			String token, String language) {
		Result result = null;
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/foot_point.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				FootPointQueryObject qo = new FootPointQueryObject();
				qo.setCurrentPage(CommUtil.null2Int(currentPage));
				qo.addQuery("obj.fp_user_id", new SysMap("fp_user_id", user.getId()),
						"=");
				qo.setOrderBy("addTime");
				qo.setOrderType("desc");
				IPageList pList = this.footPointService.list(qo);
				
				List<FootPoint> footPointList = pList.getResult();
				List<Map<String, Object>> footPoints = new ArrayList<Map<String, Object>>();
				for(FootPoint footPoint : footPointList){
					Map<String, Object> map = new HashMap<String, Object>();   
					map.put("fp_date", footPoint.getFp_date());
					map.put("fp_goods_count", footPoint.getFp_goods_count());
					List<FootPointView> footGoodsinfos = footPointTools.generic_fpv(footPoint.getFp_goods_content());
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					for(FootPointView footPointView : footGoodsinfos){
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						goodsMap.put("goods_id", footPointView.getFpv_goods_id());
						goodsMap.put("goods_name", footPointView.getFpv_goods_name());
						if("1".equals(language)){
							goodsMap.put("goods_name", footPointView.getFpv_ksa_goods_name() != null && 
																			!"".equals(footPointView.getFpv_ksa_goods_name()) 
																			? "^"
																			+ footPointView.getFpv_ksa_goods_name() 
																			: footPointView.getFpv_goods_name());
						}
						goodsMap.put("goods_price", footPointView.getFpv_goods_price());
						goodsMap.put("goods_current_price", footPointView.getFpv_goods_current_price());
						goodsMap.put("goods_discount_rate", footPointView.getFpv_goods_discount_rate());
						goodsMap.put("goods_sale", footPointView.getFpv_goods_sale());
						goodsMap.put("goods_photo", footPointView.getFpv_goods_img_path());
						goodsMap.put("goods_status", footPointView.getFpv_goods_status());
						goodsMap.put("store_status", footPointView.getFpv_store_status());
						list.add(goodsMap);
					}
					map.put("footgoods", list);
					footPoints.add(map);
				}
				result = new Result(0, "Successfully", footPoints);
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
	
	@SecurityMapping(title = "用户足迹记录删除", value = "/buyer/foot_point_remove.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
//	@RequestMapping("/buyer_foot_point_remove.json")
	@RequestMapping(value = "v1/foot_point_remove.json", method = RequestMethod.DELETE)
	public void foot_point_remove(HttpServletRequest request,
			HttpServletResponse response, String date, String goods_id, String token) {
		Result result = null;
		if(token.equals("")){
			result = new Result(-100, "token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user != null){
				result = new Result(-100,"token Invalidation");
			}else{
				Map<String, Object> params = new HashMap<String, Object>();
				if (!CommUtil.null2String(date).equals("")
						&& CommUtil.null2String(goods_id).equals("")) {// 删除当日所有足迹
					params.clear();
					params.put("fp_date", CommUtil.formatDate(date));
					params.put("fp_user_id", user.getId());
					List<FootPoint> fps = this.footPointService
							.query("select obj from FootPoint obj where obj.fp_date=:fp_date and obj.fp_user_id=:fp_user_id",
										params, -1, -1);
					for (FootPoint fp : fps) {
						this.footPointService.delete(fp.getId());
					}
				}
				if (!CommUtil.null2String(date).equals("")
						&& !CommUtil.null2String(goods_id).equals("")) {// 删除某一个足迹
					params.clear();
					params.put("fp_date", CommUtil.formatDate(date));
					params.put("fp_user_id",  user.getId());
					List<FootPoint> fps = this.footPointService
							.query("select obj from FootPoint obj where obj.fp_date=:fp_date and obj.fp_user_id=:fp_user_id",
									params, -1, -1);
					for (FootPoint fp : fps) {
						List<Map> list = Json.fromJson(List.class,
								fp.getFp_goods_content());
						for (Map map : list) {
							if (CommUtil.null2String(
											map.get("goods_id")).equals(goods_id)) {
								list.remove(map);
								break;
							}
						}
						fp.setFp_goods_content(Json.toJson(list, JsonFormat.compact()));
						fp.setFp_goods_count(list.size());
						this.footPointService.update(fp);
					}
				}
				result = new Result(0, "Successfully");
			}
		}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result,  JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
