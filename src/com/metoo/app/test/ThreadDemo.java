package com.metoo.app.test;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @descript Thread学习
 * 	Thread.Sleep(0)的作用，就是“触发操作系统立刻重新进行一次CPU竞争”
 * @author 46075
 *
 */
public class ThreadDemo extends Thread{
	   private Thread t;
	   private String threadName;
	   //采用静态代码块，初始化超时时间设置，再根据配置生成默认HttpClient对象 
       // 配置请求的超时设置
	   static{
		   RequestConfig config =  RequestConfig.custom().setConnectTimeout(0).setSocketTimeout(0).build();
		      HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	   }
	   
	   ThreadDemo( String name) {
	      this.threadName = name;
	      System.out.println("Creating " +  threadName );
	   }
	   
	   public void run() {
	      System.out.println("Running " +  threadName );
	      try {
	         for(int i = 4; i > 0; i--) {
	            System.out.println("Thread: " + threadName + ", " + i);
	            // 让线程睡眠一会
	            Thread.sleep(5000);
	         }
	      }catch (InterruptedException e) {
	         System.out.println("Thread " +  threadName + " interrupted.");
	      }
	      System.out.println("Thread " +  threadName + " exiting.");
	   }
	   
	   public void start () {
	      System.out.println("Starting " +  threadName );
	      if (t == null) {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }
	   
	 
}
