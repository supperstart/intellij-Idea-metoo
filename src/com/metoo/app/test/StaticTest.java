package com.metoo.app.test;

import org.junit.Test;

public class StaticTest {
	private static final String staString = "sta";
	private String ticString = "tic";

	
	public static void sta(){
		System.out.println(staString);
		
	}
	
	public static void main(String[] args) {
	}
	
	@Test
	public void tic(){
		//Class.forName(StaticTest.class);
		this.sta();
		System.out.println(staString);
		System.out.println(ticString);
		
	}
}
