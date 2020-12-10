package com.metoo.manage.timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.foundation.service.ISysConfigService;
/**
 * <p>
 * Title: JobManageAction.java
 * </p>
 * 
 *  <p>
 * Description: 系统定时任务控制器，每天00:00:01秒执行
 * metoo版开始，系统定时器方法移到configService中，执行方法分别为runTimerByDay，runTimerByHalfhour
 * 移到configService中能够有效保持所有数据一致性。
 * </p>
 * 
 * @author hk
 *
 */
@Component(value = "order_job")
public class OrderManageAction {
	
	@Autowired
	private ISysConfigService configService;
	
	public void execute() throws Exception{
		this.configService.runTimeOrder();
		
	}
	
}
