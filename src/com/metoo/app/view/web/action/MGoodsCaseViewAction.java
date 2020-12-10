package com.metoo.app.view.web.action;

import java.io.IOException;
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

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsCase;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.service.IGoodsCaseService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;

@Controller
@RequestMapping("/app/")
public class MGoodsCaseViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsCaseService goodscaseService;
	@Autowired
	private IGoodsService goodsService;

	/**
	 * 
	 * @param request
	 * @param response
	 * @param case_id
	 *            橱窗标识
	 */
//		@RequestMapping("/query_goodscase.json")
	@RequestMapping("v1/queryGoodsCase.json")
	public void queryGoodsCase(HttpServletRequest request, HttpServletResponse response, String case_id) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("case_id", case_id);
		params.put("display", 1);
		List<GoodsCase> list = this.goodscaseService.query(
				"select obj from GoodsCase obj where obj.case_id=:case_id and obj.display=:display order by obj.sequence",
				params, 0, 6);
		List<Map<String, Object>> caseList = new ArrayList<Map<String, Object>>();
		for (GoodsCase goodscase : list) {
			Map<String, Object> goodsCaseMap = new HashMap<String, Object>();
			goodsCaseMap.put("id", goodscase.getId());
			goodsCaseMap.put("goodsCase_id", goodscase.getCase_id());
			goodsCaseMap.put("goodsCase_name", goodscase.getCase_name());
			goodsCaseMap.put("case_content", goodscase.getCase_content());
			caseList.add(goodsCaseMap);
			map.put("goodscasemap", caseList);
		}
		result = new Result(0, "Successfully", map);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().println(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	@RequestMapping("/query_casegoods.json")
	@RequestMapping("v1/queryCaseGoods.json")
	public void querycasegoods(HttpServletRequest request, HttpServletResponse response, String case_content) {
		List list = (List) Json.fromJson(case_content);
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> caseGoodsList = new ArrayList<Map<String, Object>>();
		if (list.size() > 6) {
			for (Object id : list.subList(0, 6)) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("id", CommUtil.null2Long(id));
				List<Goods> goodsList = this.goodsService.query(
						"select new Goods(id,goods_name,goods_current_price,goods_price,goods_main_photo) from Goods obj where obj.id=:id",
						params, 0, 1);
				if (goodsList.size() > 0) {
					for (Goods goods : goodsList) {
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						goodsMap.put("goods_id", goods.getId());
						goodsMap.put("goods_name", goods.getGoods_name());
						goodsMap.put("well_evaluate", goods.getWell_evaluate());
						goodsMap.put("evaluate_count", goods.getEvaluate_count());
						goodsMap.put("goods_price", goods.getGoods_price());
						goodsMap.put("goods_current_price", goods.getGoods_current_price());
						goodsMap.put("goods_type", goods.getGoods_type());
						caseGoodsList.add(goodsMap);
					}
					map.put("casegoodslist", caseGoodsList);
				}
			}
		} else {
			for (Object id : list) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("id", CommUtil.null2Long(id));
				List<Goods> goodsList = this.goodsService.query(
						"select new Goods(id,goods_name,goods_current_price,goods_price,goods_main_photo) from Goods obj where obj.id=:id",
						params, 0, 1);
				if (goodsList.size() > 0) {
					for (Goods goods : goodsList) {
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						goodsMap.put("goods_id", goods.getId());
						goodsMap.put("goods_name", goods.getGoods_name());
						goodsMap.put("well_evaluate", goods.getWell_evaluate());
						goodsMap.put("evaluate_count", goods.getEvaluate_count());
						goodsMap.put("goods_price", goods.getGoods_price());
						goodsMap.put("goods_current_price", goods.getGoods_current_price());
						goodsMap.put("goods_type", goods.getGoods_type());
						caseGoodsList.add(goodsMap);
					}
					map.put("casegoodslist", caseGoodsList);
				}
			}
		}
		result = new Result(0, "Successfully", map);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().println(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
