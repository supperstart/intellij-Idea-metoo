package com.metoo.app.test.jpa;

import org.springframework.stereotype.Component;
/**
  * 解决实体管理器工厂的浪费资源和耗时问题
  *      通过静态代码块的形式，当程序第一次访问此工具类时，创建一个公共的实体管理器工厂对象
  *
  * 第一次访问getEntityManager方法：经过静态代码块创建一个factory对象，再调用方法创建一个EntityManager对象
  * 第二次方法getEntityManager方法：直接通过一个已经创建好的factory对象，创建EntityManager对象
  * 
  *  getTransaction : 获取事务对象
  *  persist ： 保存操作
  *  merge ： 更新操作
  *  remove ： 删除操作
  *  find/getReference ： 根据id查询
  *
  *  在 JPA 规范中, EntityTransaction是完成事务操作的核心对象，对于EntityTransaction在我们的java代码中承接的功能比较简单
  *  begin：开启事务
  *	 commit：提交事务
  *	 rollback：回滚事务
  *
  */
@Component
public class JpaUtil {

	//private static EntityManagerFactory entityManagerFactory;
	
/*	static{
		//加载配置
		entityManagerFactory = Persistence.createEntityManagerFactory("metoo_b2b2c");
	}
	
	public static EntityManager  getEntityManage(){
		return entityManagerFactory.createEntityManager();
		
	};*/
}
