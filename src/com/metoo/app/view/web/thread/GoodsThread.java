package com.metoo.app.view.web.thread;

/**
 * <p>
 * Title: GoodsThread.class
 * </p>
 * 
 * <p>
 * Description: 弃用
 * </p>
 */
import java.util.List;

import com.metoo.app.view.web.tool.GoodsBatcUpdateUtil;
import com.metoo.foundation.domain.Goods;

public class GoodsThread implements Runnable {

	private List<Goods> goodsList;

	public GoodsThread(List<Goods> goodsList) {
		super();
		this.goodsList = goodsList;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}

}
