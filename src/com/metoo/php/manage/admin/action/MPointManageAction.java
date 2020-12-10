package com.metoo.php.manage.admin.action;

import java.io.File;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;

@Controller
@RequestMapping("/php/admin/point")
public class MPointManageAction {
	@Autowired
	private IPointService pointService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserService userService;

	/**
	 * 活动添加
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            商品id
	 * @param rules
	 *            活动规则
	 * @param link
	 *            活动链接
	 */
	@RequestMapping("/save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String rules, String link) {
		Result result = null;
		WebForm wf = new WebForm();
		Point point = null;
		if (id.equals("")) {
			point = wf.toPo(request, Point.class);
			point.setAddTime(new Date());
		} else {
			Point obj = this.pointService.getObjById(CommUtil.null2Long(id));
			point = (Point) wf.toPo(request, obj);
		}
		point.setRules(rules);
		point.setLink(link);
		boolean flag = false;
		if (id.equals("")) {
			flag = this.pointService.save(point);
		} else {
			flag = this.pointService.update(point);
		}
		result = new Result(0, "Successfully");
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
	 * 活动删除
	 */
	@RequestMapping("/del.json")
	public void del(HttpServletRequest request, HttpServletResponse response, String mulitId) {
		Result result = null;
		if (mulitId != null && !mulitId.equals("")) {
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				Point obj = this.pointService.getObjById(CommUtil.null2Long(id));
				String json = obj.getGoods_ids_json();
				if (json != null && !json.equals("")) {
					List goods_ids = (List) Json.fromJson(json);
					for (Object goods_id : goods_ids) {
						Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
						goods.setPoint(0);
						goods.setPoint_id("");
						this.goodsService.update(goods);
					}
				}
				this.pointService.delete(Long.parseLong(id));
			}
			result = new Result(0, "Successfully");
		} else {
			result = new Result(1001, "parameter error");
		}
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
	 * @description 批量设置商品为邀请兑换商品 ajax更新
	 * @param request
	 * @param response
	 * @param name
	 *            用户名
	 * @param mulitId
	 *            商品id （字符串 “,” 分割）
	 * @param pid
	 *            活动id
	 * @param type
	 *            add (添加) del(移除)
	 * @param num
	 *            商品邀请数量
	 * @param mark
	 *            商品是否正常出售 hidden：禁止出售 show：正常出售
	 */
	@RequestMapping("/ajax.json")
	public void batchEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "name", required = true) String name, String mulitId,
			@RequestParam(value = "pid", required = true) String pid, String type, Integer num, String mark) {
		Result result = null;
		int code = -1;
		String msg = "";
		if (name != null && !name.equals("")) {
			User user = this.userService.getObjByProperty(null, "userName", name);
			Point point = this.pointService.getObjById(CommUtil.null2Long(pid));
			String goods_json = point.getGoods_ids_json();
			List goods_id_list = null;
			if (goods_json != null && !goods_json.equals("")) {
				goods_id_list = (List) Json.fromJson(goods_json);
			} else {
				goods_id_list = new ArrayList();
			}
			String[] ids = mulitId.split(",");
			List<Long> goods_ids = new ArrayList<Long>();
			for (String id : ids) {
				Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
				if (obj.getGoods_transfee() == 0) {
					if (obj.getPoint() == 0 || obj.getPoint_id().equals(pid)) {
						String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
								+ File.separator + "goods";
						if (type.equals("add")) {
							if (obj.getPoint() == 0) {
								if (obj.getCombin_status() == 0 && obj.getGroup_buy() == 0
										&& obj.getActivity_status() == 0 && obj.getF_sale_type() == 0
										&& obj.getAdvance_sale_type() == 0 && obj.getOrder_enough_give_status() == 0
										&& obj.getEnough_reduce() == 0) {
									goods_id_list.add(id);
									obj.setPoint(1);
									obj.setPointNum(num == null ? 0 : num);
									obj.setPoint_id(pid);
									point.setGoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
									if ("hidden".equals(mark)) {
										// 删除索引
										LuceneUtil lucene = LuceneUtil.instance();
										lucene.setIndex_path(goods_lucene_path);
										lucene.delete_index(id);
										obj.setGoods_status(4); // 将商品状态设置为非正常出售商品
									}
								} else {
									result = new Result(3, "该产品已参加其他活动");
								}
							}
						} else {
							if (goods_id_list.contains(id)) {
								goods_id_list.remove(id);
							}
							if (obj.getGoods_status() == 4) {
								obj.setGoods_status(1);// 设置为下架产品
							}
							obj.setPoint(0);
							obj.setPoint_id("");
							obj.setPointNum(0);
							point.setGoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
						}
					}
					this.pointService.update(point);
					this.goodsService.update(obj);
					code = 0;
					msg = "Sucessfully";
				} else {
					code = 1;
					msg = "买家承担运费，不能参加活动";
				}
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		result = new Result(code, msg);
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
	 * @description 兑换商品审核
	 * @param request
	 * @param response
	 * @param user_id
	 * @param mulitId
	 * @param status
	 */
	@RequestMapping("v1/audit.json")
	public void audit(HttpServletRequest request, HttpServletResponse response, String user_id, String mulitId,
			String status, String message) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if (null != user) {
			if (user.getUserRole().equals("ADMIN")) {
				Map params = new HashMap();
				params.put("type", 0);
				List<Point> points = this.pointService.query("SELECT obj FROM Point obj where obj.type=:type", params, -1, -1);
				if (points.size() > 0) {
					Point point = points.get(0);
					String goods_json = point.getGoods_ids_json();
					List goods_id_list = null;
					if (goods_json != null && !goods_json.equals("")) {
						goods_id_list = (List) Json.fromJson(goods_json);
					} else {
						goods_id_list = new ArrayList();
					}
					String[] goods_ids = mulitId.split(",");
					for (String id : goods_ids) {
						Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
						if (goods.getPoint_id().equals(point.getId().toString())) {
							goods.setPoint_status(CommUtil.null2Int(status));
							goods.setPoint_msg(message);
							this.goodsService.update(goods);
						}
					}
					code = 5200;
					msg = "Successfully";
					this.pointService.update(point);
				} else {
					code = 5204;
					msg = "Activity does not exist";
				}
			} else {
				code = 5401;
				msg = "Unauthorized";
			}
		} else {
			code = -100;
			msg = "User not exists ";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	/**
	 * @description 兑换商品审核
	 * @param request
	 * @param response
	 * @param user_id
	 * @param mulitId
	 * @param status
	 */
	@RequestMapping("v2/audit.json")
	public void auditV2(HttpServletRequest request, HttpServletResponse response, String user_id, String mulitId,
			String point_id, String status, String message) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if (null != user) {
			if (user.getUserRole().equals("ADMIN")) {
				Point point = this.pointService.getObjById(CommUtil.null2Long(point_id));
				if (null != point) {
					if (point.getPtbegin_time().before(new Date()) && point.getPtend_time().after(new Date())) {
						String[] goods_ids = mulitId.split(",");
						for (String id : goods_ids) {
							Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
							if (goods.getPoint_id().equals(point.getId().toString())) {
								goods.setPoint_status(CommUtil.null2Int(status));
								goods.setPoint_msg(message);
								this.goodsService.update(goods);
							}
						}
						code = 5200;
						msg = "Successfully";
					} else {
						point.setPoint_status(1);
						code = 5214;
						msg = "Activity expired";
					}
					this.pointService.update(point);
				} else {
					code = 5204;
					msg = "Activity does not exist";
				}
			} else {
				code = 5401;
				msg = "Unauthorized";
			}
		} else {
			code = -100;
			msg = "User not exists ";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	

	@RequestMapping("goods/v1/sequence.json")
	public void editSequence(HttpServletRequest request, HttpServletResponse response, String goods_id, String user_id,
			String sequence) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		if (user.getUserRole().equals("ADMIN")) {
			Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
			Map params = new HashMap();
			params.put("type", 0);
			List<Point> points = this.pointService.query("SELECT obj FROM Point obj where obj.type=:type", params, -1,
					-1);
			if (points.size() > 0) {
				Point point = points.get(0);
				if (obj.getPoint() == 1 && obj.getPoint_status() == 10
						&& obj.getPoint_id().equals(CommUtil.null2String(point.getId()))) {
					obj.setSequence(CommUtil.null2BigDecimal(sequence));
					this.goodsService.update(obj);
					code = 5200;
					msg = "Successfully";
				} else {
					code = 5214;
					msg = "The merchandise was not included in the event";
				}
			} else {
				code = 5205;
				msg = "The activity is over";
			}
		} else {
			code = 5401;
			msg = "Unauthorized";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

}
