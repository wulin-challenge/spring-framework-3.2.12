package cn.wulin.temp1.domain.manual_create_bean;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * 手动注册spring bean,借助FactoryBean来实现
 *
 */
public class ManualOperationSpringBean {
	
	/**
	 * 向BeanFactory中设置Bean
	 */
	public static void setBeanToBeanFactory(String beanName,Object bean){
		CustomBeanFactory.addBean(bean.getClass(), bean);
		
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(ManualCreateBeanByFactoryBean.class);
		//设置目标bean的class
		bd.getConstructorArgumentValues().addGenericArgumentValue(bean.getClass()); // issue #59
		bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
		
		ObtainBeanFactory.beanFactory.registerBeanDefinition(beanName, bd);
	}
}
