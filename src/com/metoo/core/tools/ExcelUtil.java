package com.metoo.core.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.constant.Globals;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsService;

/**
 * 
 * <p>
 * Title: ExcelUtil.java
 * <p>
 * 
 * <p>
 * Description: 该类适用导入excel进行批量操作数据
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2020
 * </p>
 * 
 * <p>
 * Company: 觅通科技 
 * </p>
 * 
 * @author 46075
 *
 */

@Controller
public class ExcelUtil {
	
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private DatabaseTools databaseTools;
	
	/**
	 * 刷评-计算好评率
	 * @param request
	 * @param response
	 */
	@RequestMapping("hssf_import.json")
	public void hssf_export(HttpServletRequest request,
			HttpServletResponse response, String goods_ids, String path, String excel_name){
		String file = request.getServletContext().getRealPath("/") + path + File.separator + excel_name;
		//File file = new File(path);
		//File file = new File("C:\\Users\\46075\\Desktop\\metoo\\评论2020-8-06(1)(1)(1).xls");
		Result result = null;
		try {
			FileInputStream  fileInputStream = new FileInputStream(file);
            //获取系统文档
			POIFSFileSystem fspoi = new POIFSFileSystem(fileInputStream);
			//创建工作簿对象
			HSSFWorkbook wb = new HSSFWorkbook(fspoi); 
			//创建工作表
			HSSFSheet sheet1 = wb.getSheet("Sheet1");
			//得到Excel表格
           // HSSFRow row = sheet1.getRow(2);
            //得到Excel工作表指定行的单元格
            //HSSFCell cell = row.getCell(2);
			List list = new ArrayList();
            for(int i = 1; i < sheet1.getPhysicalNumberOfRows(); i ++ ){
            	List list1 = new ArrayList();
            	HSSFRow row = sheet1.getRow(i);
            	for(int j = 0; j < row.getLastCellNum(); j ++){
//            		map.put(sheet1.getRow(1).getCell(j), row.getCell(j));
            		list1.add(row.getCell(j));
            	}
            	list.add(list1);
            }
         String[] ids =  goods_ids.split(",");
        /* String[] ids = new String[]{"5884", "5928", "5875", "5867"};*/
         String[] date = new String[]{"6", "7", "8"};
         int[] number = new int[]{3,4,5};
         int[] dateNumber = new int[]{1,2,3,4,};
		 for(int i=0; i < list.size(); i++){
            	List list2 = (List)list.get(i);
            	for(int j = 0; j < list2.size(); j++ ){
            		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(ids[new Random().nextInt(ids.length)]));
            		Evaluate eva = new Evaluate();
            		goods.setEvaluate_count(goods.getEvaluate_count() + 1);
            		eva.setAddTime(CommUtil.formatDate("2020-" + date[new Random().nextInt(date.length)] + "-" + (int)(Math.random()*(29-1+1) + 1),"yyyy-MM-dd"));
            		eva.setEvaluate_goods(goods);
            		eva.setEvaluate_info(list2.size() == 1 ? "" : CommUtil.null2String(list2.get(j+1)));
            		//eva.setEvaluate_photos(request.getParameter("evaluate_photos"));
            		eva.setEvaluate_buyer_val(number[new Random().nextInt(number.length)]);
            		eva.setDescription_evaluate(CommUtil.null2BigDecimal(number[new Random().nextInt(number.length)]));
            		eva.setService_evaluate(CommUtil.null2BigDecimal(number[new Random().nextInt(number.length)]));
            		eva.setShip_evaluate(CommUtil.null2BigDecimal(number[new Random().nextInt(number.length)]));
            		eva.setEvaluate_type("Brush");
            		eva.setUser_name(CommUtil.null2String(list2.get(j)) == "" ? "Khalidi" : CommUtil.null2String(list2.get(j)));
            		eva.setReply_status(0);
            		this.goodsService.update(goods);
            		this.evaluateService.save(eva);
            		break;
            	}
            }
		 
		 	//计算商品好评率
		 	Map params = new HashMap();
		 	List<Goods> goods_list = new ArrayList<Goods>();
		 	for(String str : ids){
		 		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(str));
		 		goods_list.add(obj);
		 	}

			for (Goods goods : goods_list) {
				// 统计所有商品的描述相符评分
				double description_evaluate = 0;
				params.clear();
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
					//params.put("evaluate_buyer_val", 5);
					String id = CommUtil.null2String(goods.getId());
					//星级率
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
			result = new Result(0, "Successfully");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = new Result(0, "error");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = new Result(0, "error");
		} 
		
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@RequestMapping("importExcel.json")
	public void importExcel(HttpServletRequest request, 
			HttpServletResponse response, String excel, MultipartFile file) throws IOException{
		InputStream input = null;
		HSSFWorkbook wb = null;
		List<List<String>> data  = null;
			input = file.getInputStream();
			wb = new HSSFWorkbook(input);
			HSSFSheet sheet1 = wb.getSheet("Sheet1");
			data = new ArrayList<List<String>>();
            for(int i = 1; i < sheet1.getPhysicalNumberOfRows(); i ++ ){
            	List<String> rowCell = new ArrayList<String>();
            	HSSFRow row = sheet1.getRow(i);
            	System.out.println(i);
            	for(int j = 0; j < row.getLastCellNum(); j ++){
            		try {
						rowCell.add(row.getCell(j).toString());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	data.add(rowCell);
            }
		/*String extend = file.getOriginalFilename()
				.substring(file.getOriginalFilename().lastIndexOf(".") + 1)
				.toLowerCase();*/
		try {
			response.getWriter().print(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
