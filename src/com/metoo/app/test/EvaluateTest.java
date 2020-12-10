package com.metoo.app.test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.constant.Globals;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsService;

@Controller
@RequestMapping("/evaluate")
public class EvaluateTest {
	@Autowired 
	private IGoodsService goodsService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private DatabaseTools databaseTools;
	
	@RequestMapping("/batch.json")
	public void evaluate(HttpServletRequest request,
			HttpServletResponse response,String id, String date){
		Evaluate eva = new Evaluate();
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
		goods.setEvaluate_count(goods.getEvaluate_count() + 1);
		eva.setAddTime(CommUtil.formatDate(date,"yyyy-MM-dd"));
		eva.setEvaluate_goods(goods);
		eva.setEvaluate_info(request.getParameter("evaluate_info"));
		//eva.setEvaluate_photos(request.getParameter("evaluate_photos"));
		eva.setEvaluate_buyer_val(CommUtil
				.null2Int(eva_rate(request.getParameter("evaluate_buyer_val"))));
		eva.setDescription_evaluate(BigDecimal.valueOf(CommUtil
				.null2Double(eva_rate(request.getParameter("description_evaluate")))));
		eva.setService_evaluate(BigDecimal.valueOf(CommUtil
				.null2Double(eva_rate(request.getParameter("service_evaluate")))));
		eva.setShip_evaluate(BigDecimal.valueOf(
				CommUtil.null2Double(eva_rate(request.getParameter("ship_evaluate")))));
		eva.setEvaluate_type("goods");
		eva.setUser_name(request.getParameter("username"));
		eva.setReply_status(0);
		this.goodsService.update(goods);
		boolean flag = this.evaluateService.save(eva);
			try {
				response.getWriter().print(flag);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private int eva_rate(String rate) {
		int score = 0;
		if (rate.equals("a")) {
			score = 1;
		} else if (rate.equals("b")) {
			score = 2;
		} else if (rate.equals("c")) {
			score = 3;
		} else if (rate.equals("d")) {
			score = 4;
		} else if (rate.equals("e")) {
			score = 5;
		}
		return score;
	}
	
	@Test
	public void StartsWith(){
		String name =  "Abbas al";
		//System.out.println(name.charAt(0) + "***" + name.charAt(name.length()));
	}
	
	
	@RequestMapping("/evaluate.json")
	public void evaluate(HttpServletRequest request,
			HttpServletResponse response){
		Map params = new HashMap();
		List<Goods> goods_list = this.goodsService.query(
				"select distinct obj.evaluate_goods from Evaluate obj ", null,
				-1, -1);
		for (Goods goods : goods_list) {
			// 统计所有商品的描述相符评分
			double description_evaluate = 0;
			params.clear();
			System.out.println(goods.getId());
			params.put("evaluate_goods_id", goods.getId());
			List<Evaluate> eva_list = this.evaluateService
					.query("select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id",
							params, -1, -1);
			//[add 浮点数据加法]
			for (Evaluate eva : eva_list) {
				description_evaluate = CommUtil.add(
						eva.getDescription_evaluate(), description_evaluate);
			}
			//[div 浮点数除法运算]
			description_evaluate = CommUtil.div(description_evaluate,
					eva_list.size());
			
			goods.setDescription_evaluate(BigDecimal
					.valueOf(description_evaluate));
			if (eva_list.size() > 0) {// 商品有评价情况下
				// 统计所有商品的好评率
				double well_evaluate = 0;
				double well_evaluate_num = 0;
				params.clear();
				params.put("evaluate_goods_id", goods.getId());
				//[买家评价，level 1 2 3 4 5 ]
				//params.put("evaluate_buyer_val", 5);
				
				String id = CommUtil.null2String(goods.getId());
//				int num = this.databaseTools.queryNum("select Count(evaluate_buyer_val) from "
//						+ Globals.DEFAULT_TABLE_SUFFIX 
//						+ "evaluate where evaluate_goods_id="
//						+ id 
//						+ " and evaluate_buyer_val BETWEEN 4 AND 5");
//				
				//well_evaluate_num = CommUtil.mul(5, eva_list.size());
				
				int num = this.databaseTools.queryNum("select SUM(evaluate_buyer_val) from "
						+ Globals.DEFAULT_TABLE_SUFFIX 
						+ "evaluate where evaluate_goods_id="
						+ id 
						+ " and evaluate_buyer_val BETWEEN 1 AND 5 ");
				
				well_evaluate_num = CommUtil.mul(5, eva_list.size());
				well_evaluate = CommUtil.div(num, well_evaluate_num);
				goods.setWell_evaluate(BigDecimal.valueOf(well_evaluate));
				// 统计所有商品的中评率
				double middle_evaluate = 0;
				params.clear();
				params.put("evaluate_goods_id", goods.getId());
				//params.put("evaluate_buyer_val", 3);
				List<Evaluate> middle_list = this.evaluateService
						.query("select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id and obj.evaluate_buyer_val BETWEEN 2 AND 3",
								params, -1, -1);
				middle_evaluate = CommUtil.div(middle_list.size(),
						eva_list.size());
				goods.setMiddle_evaluate(BigDecimal.valueOf(middle_evaluate));
				// 统计所有商品的差评率
				double bad_evaluate = 0;
				params.clear();
				params.put("evaluate_goods_id", goods.getId());
				params.put("evaluate_buyer_val", 1);
				List<Evaluate> bad_list = this.evaluateService
						.query("select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id and obj.evaluate_buyer_val=:evaluate_buyer_val",
								params, -1, -1);
				bad_evaluate = CommUtil.div(bad_list.size(), eva_list.size());
				goods.setBad_evaluate(BigDecimal.valueOf(bad_evaluate));
			}
			this.goodsService.update(goods);
			}
	}

}
