package com.metoo.app.buyer.domain;

public class newPhone extends Phone{
	public void showNum() {
		//调用父类已经存在的功能使用super
		super.showNum();
		//增加自己特有显示姓名和头像功能
		System.out.println("显示姓名");
		System.out.println("显示头像");
	}
}
