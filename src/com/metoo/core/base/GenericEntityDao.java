package com.metoo.core.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;

import com.metoo.core.exception.CanotRemoveObjectException;
import com.metoo.core.tools.CommUtil;

/**
 * 
 * <p>
 * Title: GenericEntityDao.java
 * </p>
 * 
 * <p>
 * Description: 数据库操作基础DAO，系统使用JPA完成所有数据库操作，默认JPA的实现为Hibernate
 * </p>
 * @SuppressWarnings("deprecation") 表示不检测过期的方法
 */
@SuppressWarnings("deprecation")
public class GenericEntityDao extends JpaDaoSupport {

	public GenericEntityDao() {
	}

	public Object get(Class clazz, Serializable id) {
		if (id == null)
			return null;
		return this.getJpaTemplate().find(clazz, id);
	}

	public List<Object> find(Class clazz, final String construct,
			final String queryStr, final Map params, final int begin,
			final int max) {
		// TODO Auto-generated method stub
		final Class claz = clazz;
		List<Object> ret = (List<Object>) this.getJpaTemplate().execute(
				new JpaCallback() {

					public Object doInJpa(EntityManager em)
							throws PersistenceException {
						// TODO Auto-generated method stub
						// System.out.println("查询构造函数:" + construct);
						String clazzName = claz.getName();
						StringBuffer sb = null;
						if (construct != null && !construct.equals("")) {
							sb = new StringBuffer("select " + construct
									+ " from ");
						} else {
							sb = new StringBuffer("select obj from ");
						}
						sb.append(clazzName).append(" obj").append(" where ")
								.append(queryStr);
						Query query = em.createQuery(sb.toString());
						for (Object key : params.keySet()) {
							query.setParameter(key.toString(), params.get(key));
						}
						if (begin >= 0 && max > 0) {
							query.setFirstResult(begin);
							query.setMaxResults(max);
						}
						// System.out.println(sb.toString());
						
						/**
						 * 通过数据库实体类中table注解获得tableName
						 * 系统视图命名统一为‘v_tfkc_’前缀，如果是视图  则关闭二级缓存
						 * 普通table表数据变更操作时，hibernate会自动刷新缓存，视图则无法刷新  
						 */
				        Table annotation = (Table)claz.getAnnotation(Table.class);   
				        boolean isView = false;
				        if(annotation != null){            
				        	String tableName =  annotation.name();
				        	//if(tableName!= null && tableName.startsWith("v_tfkc_")){
				        	if(tableName!= null && tableName.startsWith("metoo_")){
				        		isView = true;
				        	}
				        }
				        if(isView){
							query.setHint("org.hibernate.cacheable", false);
				        }else{
				        	query.setHint("org.hibernate.cacheable", true);
				        }
				        
						Session session = (Session) em.getDelegate();
						Statistics stat = session.getSessionFactory()
								.getStatistics();
						System.out.println(stat);
						return query.getResultList();
					}
				});
		if (ret != null && ret.size() >= 0) {
			return ret;
		} else {
			return new ArrayList<Object>();
		}
	}

	public List query(final String queryStr, final Map params, final int begin,
			final int max) {
		List list = (List) this.getJpaTemplate().execute(new JpaCallback() {
			@Override
			public Object doInJpa(EntityManager em) throws PersistenceException {
				// TODO Auto-generated method stub
				Query query = em.createQuery(queryStr);
				if (params != null && params.size() > 0) {
					for (Object key : params.keySet()) {
						System.out.println(params.get(key));
						query.setParameter(key.toString(), params.get(key));
					}
				}
				if (begin >= 0 && max > 0) {
					query.setFirstResult(begin);
					query.setMaxResults(max);
				}
				query.setHint("org.hibernate.cacheable", true);
				return query.getResultList();
			}

		});
		if (list != null && list.size() > 0) {
			return list;
		} else
			return new ArrayList();
	}

	public void remove(Class clazz, Serializable id)
			throws CanotRemoveObjectException {
		// TODO Auto-generated method stub
		Object object = this.get(clazz, id);
		if (object != null) {
			try {
				this.getJpaTemplate().remove(object);
			} catch (Exception e) {
				throw new CanotRemoveObjectException();
			}
		}
	}

	public void save(Object instance) {
		// TODO Auto-generated method stub
		this.getJpaTemplate().persist(instance);
	}

	public Object getBy(Class clazz, final String construct,
			final String propertyName, final Object value) {
		// TODO Auto-generated method stub
		final Class claz = clazz;
		List<Object> ret = (List<Object>) this.getJpaTemplate().execute(
				new JpaCallback() {

					public Object doInJpa(EntityManager em)
							throws PersistenceException {
						// TODO Auto-generated method stub
						String clazzName = claz.getName();
						StringBuffer sb = null;
						if (construct != null && !construct.equals("")) {
							sb = new StringBuffer("select " + construct
									+ " from ");
						} else {
							sb = new StringBuffer("select obj from ");
						}
						sb.append(clazzName).append(" obj");
						Query query = null;
						if (propertyName != null && value != null) {
							sb.append(" where obj.").append(propertyName)
									.append(" = :value");
							query = em.createQuery(sb.toString()).setParameter(
									"value", value);
						} else {
							query = em.createQuery(sb.toString());
						}
						query.setHint("org.hibernate.cacheable", true);
						return query.getResultList();
					}
				});
		if (ret != null && ret.size() == 1) {
			return ret.get(0);
		} else if (ret != null && ret.size() > 1) {
			throw new java.lang.IllegalStateException(
					"worning  --more than one object find!!");
		} else {
			return null;
		}
	}

	public List executeNamedQuery(final String queryName,
			final Object[] params, final int begin, final int max) {
		List ret = (List) this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNamedQuery(queryName);
				int parameterIndex = 1;
				if (params != null && params.length > 0) {
					for (Object obj : params) {
						query.setParameter(parameterIndex++, obj);
					}
				}
				if (begin >= 0 && max > 0) {
					query.setFirstResult(begin);
					query.setMaxResults(max);
				}
				query.setHint("org.hibernate.cacheable", true);
				return query.getResultList();
			}
		});
		if (ret != null && ret.size() >= 0) {
			return ret;
		} else {
			return new ArrayList();
		}
	}

	public void update(Object instance) {
		// TODO Auto-generated method stub
		this.getJpaTemplate().merge(instance);
	}

	public List executeNativeNamedQuery(final String nnq) {
		Object ret = this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(nnq);
				return query.getResultList();
			}
		});
		return (List) ret;
	}

	public List executeNativeQuery(final String nnq, final Map params,
			final int begin, final int max) {
		List ret = (List) this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(nnq);
				
				int parameterIndex = 1;
				if (params != null) {
					Iterator its = params.keySet().iterator();
					while (its.hasNext()) {
						Object obj = its.next();
						
						query.setParameter(CommUtil.null2String(obj),
								params.get(obj));
					}
				}
				if (begin >= 0 && max > 0) {
					query.setFirstResult(begin);
					query.setMaxResults(max);
				}
				// query.setHint("org.hibernate.cacheable", true);
				return query.getResultList();
			}
		});
		if (ret != null && ret.size() >= 0) {
			return ret;
		} else {
			return new ArrayList();
		}
	}

	public List executeNativeQuery(final String nnq, final Object[] params,
			final int begin, final int max) {
		List ret = (List) this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(nnq);
				int parameterIndex = 1;
				if (params != null && params.length > 0) {
					for (Object obj : params) {
						query.setParameter(parameterIndex++, obj);
					}
				}
				if (begin >= 0 && max > 0) {
					query.setFirstResult(begin);
					query.setMaxResults(max);
				}
				// query.setHint("org.hibernate.cacheable", true);
				return query.getResultList();
			}
		});
		if (ret != null && ret.size() >= 0) {
			return ret;
		} else {
			return new ArrayList();
		}
	}

	public int executeNativeSQL(final String nnq) {
		Object ret = this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(nnq);
				query.setHint("org.hibernate.cacheable", true);
				return query.executeUpdate();
			}
		});
		return (Integer) ret;
	}
	
	public int executeNativeSQL(final String nnq, final Map params) {
		Object ret = this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(nnq);
				query.setHint("org.hibernate.cacheable", true);
				
				int parameterIndex = 1;
				if (params != null) {
					Iterator its = params.keySet().iterator();
					while (its.hasNext()) {
						Object obj = its.next();
						
						query.setParameter(CommUtil.null2String(obj),
								params.get(obj));
					}
				}
				return query.executeUpdate();
			}
		});
		return (Integer) ret;
	}

	public int batchUpdate(final String jpql, final Object[] params) {
		Object ret = this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(jpql);
				int parameterIndex = 1;
				if (params != null && params.length > 0) {
					for (Object obj : params) {
						query.setParameter(parameterIndex++, obj);
					}
				}
				query.setHint("org.hibernate.cacheable", true);
				return query.executeUpdate();
			}
		});
		return (Integer) ret;
	}

	public void flush() {
		this.getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				em.getTransaction().commit();
				return null;
			}
		});
	}
}
