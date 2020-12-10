package com.metoo.php.manage.seller.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.PointQueryObject;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;

@Controller
@RequestMapping("/php/seller/point/")
public class PPointSellerAction {

	@Autowired
	private ISysConfigService configService;

	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IPointService pointService;
	@Autowired
	private IGoodsService goodsService;

	/**
	 * 活动添加 保留
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            商品id
	 * @param rules
	 *            活动规则
	 * @param link
	 *            活动链接
	 * @param
	 */
	@RequestMapping("v1/save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String user_id) {
		Result result = null;
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if (user != null) {
			if (user.getUserRole().equals("SELLER")) {
				WebForm wf = new WebForm();
				Point point = null;
				if (id.equals("")) {
					point = wf.toPo(request, Point.class);
					point.setAddTime(new Date());
					point.setStore_id(user.getStore().getId());
				} else {
					Point obj = this.pointService.getObjById(CommUtil.null2Long(id));
					point = (Point) wf.toPo(request, obj);
				}
				boolean flag = false;
				if (id.equals("")) {
					flag = this.pointService.save(point);
				} else {
					flag = this.pointService.update(point);
				}
				code = 5200;
				msg = "Successfully";
				if (!flag) {
					code = 5500;
					msg = "Error";
				}
			} else {
				code = 5204;
				msg = "Resources don't exist";
			}
		} else {
			code = 5401;
			msg = "Unauthorized";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	/**
	 * @description 设置商品为邀约活动商品
	 * @param request
	 * @param response
	 * @param user_id
	 *            用户id
	 * @param goods_id
	 *            商品id
	 * @param point_id
	 *            兑换活动id
	 * @param num
	 *            邀请人数
	 * @param mark
	 *            是否隐藏该商品,不做正常商品出售,只用来做邀约活动
	 */
	@RequestMapping("/v2/ajax.json")
	public void editV2(HttpServletRequest request, HttpServletResponse response, String user_id, String goods_id,
			String point_id, String num, String mark) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		if (user != null && store != null) {
			if (user.getUserRole().equals("SELLER") && null != store) {
				Point obj = this.pointService.getObjById(CommUtil.null2Long(point_id));
				if (null != obj) {
					if (obj.getPtbegin_time().before(new Date()) && obj.getPtend_time().after(new Date())) {
						String goods_ids = obj.getGoods_ids_json();
						List goods_id_list = null;
						if (null == null || "".equals(goods_ids)) {
							goods_id_list = new ArrayList();
						} else {
							goods_id_list = (List) Json.fromJson(obj.getGoods_ids_json());
						}
						Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
						if (goods.getGoods_transfee() == 0) {
							if (goods.getInventory_type().equals("all")) {
								if (goods.getGoods_inventory() > 20) {
									String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator
											+ "luence" + File.separator + "goods";
									int point = goods.getPoint() == 0 ? 1 : 0;
									goods.setPoint(point);
									if (point == 1) {
										goods.setPointNum(CommUtil.null2Int(num));
										goods.setPoint_id(obj.getId().toString());
										goods.setPoint_status(5);
										goods_id_list.add(goods.getId());
										goods.setGoods_status(4);
										obj.setGoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
										/* if ("hidden".equals(mark)) { */
										// 删除索引,将商品状态设置为非正常出售商品
										LuceneUtil lucene = LuceneUtil.instance();
										lucene.setIndex_path(goods_lucene_path);
										lucene.delete_index(goods.getId().toString());
										goods.setGoods_status(4);
										/* } */
									} else {
										goods.setPointNum(0);
										goods.setPoint_id(null);
										goods.setPoint_status(1);
										goods.setPoint_status(0);
										if (goods_id_list.contains(goods.getId())) {
											goods_id_list.remove(goods.getId());
										}
										if (goods.getGoods_status() == 4) {
											goods.setGoods_status(1);// 设置为下架产品
										}
										obj.setGoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
									}
									this.goodsService.update(goods);
									this.pointService.update(obj);
									code = 5200;
									msg = "Successfully";
								} else {
									code = 5212;
									msg = "Inventory not less than 20";
								}
							} else {
								code = 5211;
								msg = "Only single specification is allowed";
							}
						} else {
							code = 5210;
							msg = "Only the buyer is allowed to bear the freight";
						}
					} else {
						obj.setPoint_status(20);
						this.pointService.update(obj);
						code = 5205;
						msg = "The activity is over";
					}
				} else {
					code = 5204;
					msg = "Resources don't exist";
				}
			} else {
				code = 5401;
				msg = "Unauthorized";
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	@RequestMapping("/v1/ajax.json")
	public void edit(HttpServletRequest request, HttpServletResponse response, String user_id, String goods_id,
			String num, String mark) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		if (user != null && store != null) {
			if (user.getUserRole().equals("SELLER") && null != store) {
				Map params = new HashMap();
				params.put("type", 0);
				List<Point> points = this.pointService.query("SELECT obj FROM Point obj where obj.type=:type", params,
						-1, -1);
				if (points.size() > 0) {
					Point obj = points.get(0);
					String goods_ids = obj.getGoods_ids_json();
					List goods_id_list = null;
					if (null == obj || "".equals(goods_ids)) {
						goods_id_list = new ArrayList();
					} else {
						goods_id_list = (List) Json.fromJson(obj.getGoods_ids_json());
					}
					Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
					if(goods.getGoods_store().getId().equals(store.getId())){
						if (goods.getGoods_transfee() == 0) {
							if (goods.getInventory_type().equals("all")) {
								if (goods.getGoods_inventory() >= 15) {
									String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator
											+ "luence" + File.separator + "goods";
									int point = goods.getPoint() == 0 ? 1 : 0;
									goods.setPoint(point);
									if (point == 1) {
										goods.setPointNum(CommUtil.null2Int(num));
										goods.setPoint_id(obj.getId().toString());
										goods.setPoint_status(5);
										goods_id_list.add(goods.getId());
										goods.setGoods_status(4);
										obj.setGoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
										/* if ("hidden".equals(mark)) { */
										// 删除索引,将商品状态设置为非正常出售商品
										LuceneUtil lucene = LuceneUtil.instance();
										lucene.setIndex_path(goods_lucene_path);
										lucene.delete_index(goods.getId().toString());
										goods.setGoods_status(4);
										/* } */
									} else {
										goods.setPointNum(0);
										goods.setPoint_id(null);
										goods.setPoint_status(1);
										goods.setPoint_status(0);
										if (goods_id_list.contains(goods.getId())) {
											goods_id_list.remove(goods.getId());
										}
										if (goods.getGoods_status() == 4) {
											goods.setGoods_status(1);// 设置为下架产品
										}
										obj.setGoods_ids_json(Json.toJson(goods_id_list, JsonFormat.compact()));
									}
									this.goodsService.update(goods);
									this.pointService.update(obj);
									code = 5200;
									msg = "Successfully";
								} else {
									code = 5212;
									msg = "Inventory not less than 20";
								}
							} else {
								code = 5211;
								msg = "Only single specification is allowed";
							}
						} else {
							code = 5210;
							msg = "Only the buyer is allowed to bear the freight";
						}
					}else{
						code = 5215;
						msg = "There is no such item in the store";
					}
				} else {
					code = 5204;
					msg = "Resources don't exist";
				}
			} else {
				code = 5401;
				msg = "Unauthorized";
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	/**
	 * @description 活动开启关闭 弃用
	 * @param request
	 * @param response
	 * @param user_id
	 *            用户id
	 * @param point_id
	 *            活动id
	 */
	@RequestMapping("v1/edit.json")
	public void edit(HttpServletRequest request, HttpServletResponse response, String user_id, String point_id) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(CommUtil.null2Long(user_id)));
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		if (user != null && store != null) {
			Point point = this.pointService.getObjById(CommUtil.null2Long(point_id));
			if (point != null) {
				if (point.getPtbegin_time().before(new Date()) && point.getPtend_time().after(new Date())) {
					int point_status = point.getPoint_status() == 0 ? 1 : 0;
					point.setPoint_status(point_status);
				} else {
					point.setPoint_status(1);
					code = 5205;
					msg = "The activity is over";
				}
				this.pointService.update(point);
				code = 5200;
				msg = "Successfully";
			} else {
				code = 5204;
				msg = "Activity does not exist";
			}
		} else {
			code = 5401;
			msg = "Unauthorized";
		}
		CommUtil.returnJson(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	@RequestMapping("v1/goods.json")
	public void goods(HttpServletRequest request, HttpServletResponse response, String user_id, String currentPage,
			String orderBy, String orderType) {
		int code = -1;
		String msg = "";
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		user = user.getParent() == null ? user : user.getParent();
		if (null != user) {
			Store store = user.getStore();
			if (user.getUserRole().equals("SELLER") && null != store) {
				ModelAndView mv = new JModelAndView("", configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request, response);
				PointQueryObject qo = new PointQueryObject(currentPage, mv, orderBy, orderType);
				if (orderBy == null || orderBy.equals("")) {
					orderBy = "addTime";
				}
				if (orderType == null || orderType.equals("")) {
					orderType = "desc";
				}
				qo.addQuery("obj.store_id", new SysMap("store_id", store.getId()), "=");
				qo.addQuery("obj.point_status", new SysMap("point_status", 0), "=");
				IPageList pList = this.pointService.list(qo);
				List<Point> points = pList.getResult();
				List<Goods> list = new ArrayList<Goods>();
				for (Point point : points) {
					String goods_json = point.getGoods_ids_json();
					if (null != goods_json) {
						Set<Long> ids = this.genericIds(goods_json);
						List goodsList = null;
						if (!ids.isEmpty()) {
							goodsList = new ArrayList();
							Map params = new HashMap();
							params.put("ids", ids);
							params.put("goods_status", 4);
							params.put("store_status", 15);
							params.put("point_status", 10);
							goodsList = this.goodsService.query(
									"select obj from Goods obj where obj.id in (:ids) and obj.goods_status=:goods_status and obj.goods_store.store_status=:store_status and obj.point_status=:point_status order by obj.pointNum",
									params, -1, -1);
							list.addAll(goodsList);
						}
					}
				}
			} else {
				code = 5401;
				msg = "Unauthorized";
			}
		} else {
			code = 5400;
			msg = "The user does not exist";
		}
	}

	private Set<Long> genericIds(String str) {
		Set<Long> ids = new HashSet<Long>();
		List list = (List) Json.fromJson(str);
		for (Object object : list) {
			ids.add(CommUtil.null2Long(object));
		}
		return ids;
	}
}
