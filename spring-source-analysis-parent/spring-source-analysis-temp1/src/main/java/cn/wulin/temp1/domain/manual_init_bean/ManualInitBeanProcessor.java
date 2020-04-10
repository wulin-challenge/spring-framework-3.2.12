package cn.wulin.temp1.domain.manual_init_bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * 手动初始化bean处理器,即可以自动组装值和回调 beanPostProcesser 和 InitializingBean的回调
 * @author wulin
 *
 */
public class ManualInitBeanProcessor implements BeanFactoryAware{
	private AutowireCapableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if(beanFactory instanceof AutowireCapableBeanFactory) {
			this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
		}
	}
	
	/**
	 * 手动初始化bean
	 * @param existingBean
	 * @param beanName
	 */
	public void manualInitBean(Object existingBean,String beanName) {
		beanFactory.autowireBeanProperties(existingBean, RootBeanDefinition.AUTOWIRE_BY_TYPE, true);
		beanFactory.initializeBean(existingBean, beanName);
	}
}
