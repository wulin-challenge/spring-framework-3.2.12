package cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * 获得bean工厂
 *
 */
public class ManualAopObtainBeanFactory implements BeanFactoryAware {
	
	public static DefaultListableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory bf) throws BeansException {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) bf;
		ManualAopObtainBeanFactory.beanFactory = beanFactory;
	}

}