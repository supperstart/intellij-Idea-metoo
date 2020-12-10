package com.metoo.app.manage.buyer.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;

@Component
public class MOrderBuyerTools {
	
	@Autowired
	private LuceneVoTools luceneVoTools;
	
	

}
