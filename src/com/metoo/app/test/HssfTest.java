package com.metoo.app.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.constant.Globals;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsService;

/**
 * Excel 导出测试
 * @author 46075
 *
 */
@Controller
public class HssfTest {
	
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private DatabaseTools databaseTools;
	
	/**
	 * Excel 导出
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("hssf_export.json")
	public void hssp(HttpServletRequest request, 
			HttpServletResponse response) throws IOException{
		Goods obj = this.goodsService.getObjById(Long.parseLong("5634"));
		List<Goods> goodsList = new ArrayList<Goods>();
		goodsList.add(obj);
		// 创建Excel的工作书册 Workbook,对应到一个excel文档
		HSSFWorkbook wb = new HSSFWorkbook();
		// 创建Excel的工作sheet,对应到一个excel文档的tab
		HSSFSheet sheet = wb.createSheet("商品信息");
		for(int i = 0; i <= 5; i ++){
			sheet.setColumnWidth(i, 5000);
		}
		// 创建字体样式
		HSSFFont font = wb.createFont();
		font.setFontName("Verdana");
		font.setBoldweight((short) 100);
		font.setFontHeight((short) 300);
		font.setColor(HSSFColor.BLUE.index);
		//创建单元格样式
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		//设置边框
		style.setBottomBorderColor(HSSFColor.RED.index);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		//创建Excel的第一行
		HSSFRow row = sheet.createRow(0);
		row.setHeight((short) 500);// 设定行的高度
		//创建一个Excel的单元格
		HSSFCell cell = row.createCell(0);
		// 合并单元格(startRow，endRow，startColumn，endColumn)
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
		cell.setCellStyle(style);
		cell.setCellValue("商品信息");
		
		//设置单元格内容格式
		HSSFCellStyle style1 = wb.createCellStyle();
		style1.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-mm-dd"));
		style1.setWrapText(true);// 自动换行
		style1.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFCellStyle style2 = wb.createCellStyle();
		style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		row = sheet.createRow(1);
		cell = row.createCell(0);
		cell.setCellStyle(style2);
		cell.setCellValue("id");
		cell = row.createCell(1);
		cell.setCellValue("Name");
		cell = row.createCell(2);
		cell.setCellValue("Price");
		cell = row.createCell(3);
		cell.setCellValue("Current_price");
		cell = row.createCell(4);
		cell.setCellValue("goods_inventory");
		cell = row.createCell(5);
		cell.setCellValue("store_name");
		cell = row.createCell(6);
		cell.setCellValue("store_id");
		for(int j = 2; j <= goodsList.size() + 1; j ++){
			row = sheet.createRow(j);
			//设置单元格样式
			int i = 0;
			cell = row.createCell(i);
			cell.setCellStyle(style2);
			cell.setCellValue(goodsList.get(j - 2).getId());
			
			cell = row.createCell(++i);
			cell.setCellStyle(style1);
			cell.setCellValue(goodsList.get(j - 2).getGoods_name());
			
			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(CommUtil.null2String(goodsList.get(j - 2).getGoods_price()));
			
			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(CommUtil.null2String(goodsList.get(j - 2).getGoods_current_price()));
			
			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(CommUtil.null2String(goodsList.get(j - 2).getGoods_inventory()));
			
			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(CommUtil.null2String(goodsList.get(j - 2).getGoods_store().getStore_name()));
			
			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(CommUtil.null2String(goodsList.get(j - 2).getGoods_store().getId()));
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String excel_name = sdf.format(new Date());
		response.setContentType("application/x-download");
		response.addHeader("Content-Disposition",
				"attachment;filename=" + excel_name + ".xls");
		response.setContentType("application/msexcel;charset=UTF-8");//设置类型
		//输出Excel文件
		FileOutputStream output= new FileOutputStream("C:\\Users\\46075\\Desktop\\metoo\\workbook.xls");
		//OutputStream os;
		try {
			//os = response.getOutputStream();
			wb.write(output);
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException("导出Excel出现严重异常，异常信息：" + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
