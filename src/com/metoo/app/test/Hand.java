package com.metoo.app.test;

import java.math.BigDecimal;

import org.junit.Test;
import org.springframework.stereotype.Controller;

import com.metoo.core.tools.CommUtil;

@Controller
public class Hand {
	
	
	//@RequestMapping("/hands.json")
	public void hand(){
		 String name = null;
		 String sex = null;
		System.out.println(name + sex);
	}
	

    @Test
    public void rate(){
    	String goods_price = "35";
    	String store_price = "19";
    	if (goods_price != null && store_price != null) {
			double subtract = CommUtil.subtract(goods_price, store_price);
			double div = CommUtil.div(subtract, goods_price);
			double mul = CommUtil.mul(div, 100);
			BigDecimal e = new BigDecimal(mul);
			System.out.println(e);
		}
    }

}
