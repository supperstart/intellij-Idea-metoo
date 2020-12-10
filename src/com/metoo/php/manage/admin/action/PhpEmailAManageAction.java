package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.query.support.IPageList;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Template;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.UserQueryObject;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserService;
import com.metoo.msg.MsgTools;

import net.sf.json.JSONObject;

/**
 * 
 * <p>
 * Title: PhpEmailAManageAction.java
 * </p>
 * 
 * <p>
 * Description: 系统邮件发送控制器
 * </p>
 * 
 * <p>
 * Company: 湖南觅通科技有限公司
 * </p>
 * 
 * @author hkk
 * 
 * @data 2020-11-2
 * 
 * @version metoo_store v1.0 2020版
 *
 */
@Controller
@RequestMapping("/php/email/")
public class PhpEmailAManageAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private IGoodsService goodsService;

	/**
	 * 
	 * @param request
	 * @param response
	 * @param user_id
	 *            收件人id
	 * @param id
	 *            管理员id
	 * @param mark
	 *            模板标识
	 * @return
	 */
	@RequestMapping("sendEmail.json")
	public void sendEmail(HttpServletRequest request, HttpServletResponse response, String user_ids, String goods_ids,
			String id, String mark) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(id));
		if (null != user && user.getUserRole().equals("ADMIN")) {
			if (null != mark && !"".equals(mark)) {
				if(null != user_ids && !"".equals(user_ids)){
					if(null != goods_ids && !"".equals(goods_ids)){
						Set<Long> set = new HashSet<Long>();
						String[] ids = user_ids.split(",");
						for (String uid : ids) {
							set.add(CommUtil.null2Long(uid));
						}
						UserQueryObject uqo = new UserQueryObject();
						uqo.addQuery("obj.id", new SysMap("id", set), "in");
						IPageList pList = this.userService.list(uqo);
						List<User> users = pList.getResult();
						/*
						 * String[] userEmail = new String[users.size()]; for (int i =
						 * 0; i < users.size(); i++) { if (null !=
						 * users.get(i).getEmail() &&
						 * !"".equals(users.get(i).getEmail())) { userEmail[i] =
						 * users.get(i).getEmail(); } }
						 */
						String web = this.configService.getSysConfig().getImageWebServer();
						Set<Long> gset = new HashSet<Long>();
						String gids[] = goods_ids.split(",");
						for(String gid : gids){
							gset.add(CommUtil.null2Long(gid));
						}
						Map params = new HashMap();
						params.put("goods_ids", gset);
						List<Goods> goods_list = this.goodsService.query("select obj from Goods obj where obj.id in(:goods_ids)", params, -1, -1);
						List<Map> goodsList = new ArrayList<Map>();
						for(Goods obj : goods_list){
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("goods_id", obj.getId());
							map.put("goods_name", obj.getGoods_name());
							map.put("goods_price", obj.getGoods_price());
							map.put("goods_current_price", obj.getGoods_current_price());
							map.put("goods_mainphoto_path", obj.getGoods_main_photo().getPath() + "/" + obj.getGoods_main_photo().getName());
							goodsList.add(map);
						}
						String userEmail[] = { "460751446@qq.com"};
						try {
							this.msgTools.sendJMail(web, mark, userEmail, null, null, goodsList);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						code = 5200;
						msg = "Successfully";
					}else{
						code = 5423;
						msg = "Please select the product";
					}
				}else{
					code = 5422;
					msg = "Please select user";
				}
			} else {
				code = 5400;
				msg = "Bad request";
			}
		} else {
			code = 5401;
			msg = "Unauthorized";
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(new Result(code, msg), JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
}
