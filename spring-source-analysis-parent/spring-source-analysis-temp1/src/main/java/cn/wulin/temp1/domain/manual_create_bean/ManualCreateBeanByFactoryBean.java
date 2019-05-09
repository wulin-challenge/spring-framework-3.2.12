package cn.wulin.temp1.domain.manual_create_bean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * 利用 FactoryBean 实现公共代理
 *
 */
public class ManualCreateBeanByFactoryBean implements FactoryBean<Object>{
	public static DefaultListableBeanFactory beanFactory;
	
	private Class<?> targetClass;

	public ManualCreateBeanByFactoryBean(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	@Override
	public Object getObject() throws Exception {
		return CustomBeanFactory.getBean(targetClass);
	}

	@Override
	public Class<?> getObjectType() {
		return targetClass;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
