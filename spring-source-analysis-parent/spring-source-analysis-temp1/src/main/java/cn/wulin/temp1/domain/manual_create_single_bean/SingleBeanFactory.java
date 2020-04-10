package cn.wulin.temp1.domain.manual_create_single_bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class SingleBeanFactory implements BeanFactoryAware {

	private DefaultListableBeanFactory beanFactory;
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if(beanFactory instanceof DefaultListableBeanFactory) {
			this.beanFactory = (DefaultListableBeanFactory)beanFactory;
		}
	}

	public DefaultListableBeanFactory getBeanFactory() {
		return beanFactory;
	}

}
