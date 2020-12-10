package com.metoo.core.constant;

public enum  AppClientType {
	
	WEB_PC("PC端浏览器"),
	WEB_MOBILE("手机端浏览器"),
	APP_MOBILE("手机APP");
	
	String ZHName;
	
	public String getZHName() {
		return ZHName;
	}
	
	@Override
	public String toString(){
		//可以把中文输出
		return this.name();
	}
	
	public String toZHString(){
		//可以把中文输出
		return this.getZHName();
	}
	
	
	private AppClientType(String ZHName) {
		this.ZHName = ZHName;
	}
	
}


