package cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_bean;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * 手动注册spring bean,借助FactoryBean来实现
 *
 */
public class ManualAopOperationSpringBean {
	
	/**
	 * 向BeanFactory中设置Bean
	 */
	public static void setBeanToBeanFactory(String beanName,Object bean){
	
		ManualAopCustomBeanFactory.addBean(bean.getClass(), bean);
		
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(ManualAopCreateBeanByFactoryBean.class);
		//设置目标bean的class和beanName通过构造函数
		bd.getConstructorArgumentValues().addGenericArgumentValue(bean.getClass()); // issue #59
		bd.getConstructorArgumentValues().addGenericArgumentValue(beanName);
		bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
		
		ManualAopObtainBeanFactory.beanFactory.registerBeanDefinition(beanName, bd);
		
		createProxy(beanName, bean);
	}
	
	/**
	 * 创建代理
	 * @param beanName
	 * @param bean
	 */
	private static void createProxy(String beanName,Object bean) {
		
		AnnotationAwareAspectJAutoProxyCreator proxyCreatorBean = (AnnotationAwareAspectJAutoProxyCreator) ManualAopObtainBeanFactory.beanFactory.getBean(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		proxyCreatorBean.postProcessAfterInitialization(bean, beanName);
	}
}
