package com.metoo.foundation.test;

import com.metoo.foundation.service.impl.SysConfigService;

public class TestTime {
	public static SysConfigService SysConfigServiceiml;
public static void main(String[] args) {
	try {
		SysConfigServiceiml.runTimerByHalfhour();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}


}
