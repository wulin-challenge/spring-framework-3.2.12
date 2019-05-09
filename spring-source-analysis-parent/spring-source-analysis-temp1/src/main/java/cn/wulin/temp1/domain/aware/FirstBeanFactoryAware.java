package cn.wulin.temp1.domain.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class FirstBeanFactoryAware implements BeanFactoryAware{
	
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	public void printHello(){
		HelloBean bean = (HelloBean) beanFactory.getBean("helloBean");
		System.out.println(bean.getName());
	}

}
