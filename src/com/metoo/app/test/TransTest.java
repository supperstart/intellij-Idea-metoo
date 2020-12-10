package com.metoo.app.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Address;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAddressService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/transaction")
public class TransTest {
	
	@Autowired
	private IUserService userService;
	@Autowired
	private IAddressService addressService;
	
	//脏读 dirty read
	/**
	 * 
	 * @return
	 * @description 脏读 使用Thread.sleep(7000); 是当前线程睡眠7秒
	 */
	@RequestMapping("/dirtyReadV1")
	@ResponseBody
	public boolean dirtyReadV1(String user_id){
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		try {
			Thread.sleep(7000);
			user.setUserName("ari6-2");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return this.userService.update(user);
	}
	
	/**
	 * 
	 * @return
	 */
	@RequestMapping("dirtyReadV2")
	@ResponseBody
	public boolean dirtyReadV2(String user_id){
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		if (user.getUsername().equals("ari6")) {
			user.setUserName("ari6-1");
			return this.userService.update(user);
		}
		return false;
	}

	/**
	 * 
	 */
	@RequestMapping("non_repeatabilityV1")
	@ResponseBody
	public String non_RepeatabilityV1(String user_id){
		Map map = new HashMap();
		List list = new ArrayList<>();
		List list1 = new ArrayList<>();
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		try {
			
			List<Address> addrsses = user.getAddrs();
			if(addrsses.size() > 0){
				for(Address address:addrsses){
					list.add(address.getTrueName());
				}
				Thread.sleep(7000);
				List<Address> addrsses1 = user.getAddrs();
				for(Address address1:addrsses1){
					list1.add(address1.getTrueName());
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map.put("list", list);
		map.put("list1", list1);
		return JSONObject.toJSONString(map);
		//return JSONArray.toJSONString(list);
	}
	
	@RequestMapping("non_repeatabilityV2")
	@ResponseBody
	public void non_RepeatabilityV2(String user_id){
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
			List<Address> addrsses = user.getAddrs();
			if(addrsses.size() > 0){
				for(Address address:addrsses){
					address.setTrueName("non2");
					this.addressService.update(address);
				}
			}
	}
	
}
