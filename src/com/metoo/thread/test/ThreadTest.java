package com.metoo.thread.test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

public class ThreadTest {
	private Lock lock = new ReentrantLock(); // ReentrantLock是Lock的子类

	@Test
	 private void method(Thread thread){
		lock.lock(); // 获取锁对象
		try {
		System.out.println("线程名："+thread.getName() + "获得了锁");
		// Thread.sleep(2000);
			}catch(Exception e){
				e.printStackTrace();
		} finally {
			System.out.println("线程名："+thread.getName() + "释放了锁"); 
			lock.unlock();//释放对象锁 
			}
		}

}
