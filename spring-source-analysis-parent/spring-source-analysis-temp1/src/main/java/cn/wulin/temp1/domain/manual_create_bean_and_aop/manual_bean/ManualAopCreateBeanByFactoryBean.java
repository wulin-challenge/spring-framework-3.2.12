package cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_bean;

import org.springframework.beans.factory.FactoryBean;

/**
 * 利用 FactoryBean 实现公共代理
 *
 */
public class ManualAopCreateBeanByFactoryBean implements FactoryBean<Object>{
	
	private Class<?> targetClass;
	private String beanName;

	public ManualAopCreateBeanByFactoryBean(Class<?> targetClass) {
		this.targetClass = targetClass;
	}
	
	public ManualAopCreateBeanByFactoryBean(Class<?> targetClass,String beanName) {
		this.targetClass = targetClass;
		this.beanName = beanName;
	}

	@Override
	public Object getObject() throws Exception {
		return ManualAopCustomBeanFactory.getBean(targetClass);
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
