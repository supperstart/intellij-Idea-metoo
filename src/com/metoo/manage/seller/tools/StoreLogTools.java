package com.metoo.manage.seller.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreService;
@Component
public class StoreLogTools {
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IStoreLogService storeLogService;
	@Autowired
	private IOrderFormService orderFormService;
	
	private static org.slf4j.Logger logger =  LoggerFactory.getLogger(StoreLogTools.class);

	public StoreLog getTodayStoreLog(long id){
		try {
			Map logParams = new HashMap();
			Date now = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			logParams.put("beginDate", cal.getTime());
			now = cal.getTime();
			cal.add(Calendar.DAY_OF_YEAR, 1);
			logParams.put("endDate", cal.getTime());
			logParams.put("store_id", id);
			List<StoreLog> storeLogs = storeLogService.query("select obj from StoreLog obj where obj.store.id=:store_id and obj.addTime>=:beginDate and obj.addTime<:endDate", logParams, -1, -1);
			if(storeLogs.size() == 0){
				StoreLog storeLog = new StoreLog();
				Store store = this.storeService.getObjById(id);
				storeLog.setAddTime(now);
				if(store == null){
					storeLog.setLog_form(0);
				}else{
					storeLog.setLog_form(1);
					storeLog.setStore(store);
					storeLog.setStore_name(store.getStore_name());
				}
				this.storeLogService.save(storeLog);
				return storeLog;
			}else{
				return storeLogs.get(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
