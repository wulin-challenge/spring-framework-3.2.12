package cn.wulin.temp1.domain.manual_create_bean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义的bean工厂
 *
 */
public class CustomBeanFactory {
	
	/**
	 * 自定义bean存储的地方
	 */
	private static ConcurrentHashMap<Class<?>,Object> beanFactory = new ConcurrentHashMap<Class<?>,Object>();
	
	public static void addBean(Class<?> clazz,Object bean){
		beanFactory.put(clazz, bean);
	}
	
	public static Object getBean(Class<?> clazz){
		return beanFactory.get(clazz);
	}
	
}
