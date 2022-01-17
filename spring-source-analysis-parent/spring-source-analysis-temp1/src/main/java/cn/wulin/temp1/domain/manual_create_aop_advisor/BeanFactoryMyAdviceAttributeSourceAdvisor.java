package cn.wulin.temp1.domain.manual_create_aop_advisor;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * 我的通知 advisor实现,并实现拦截注解具有继承性
 * @author wulin
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 *
 */
public class BeanFactoryMyAdviceAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor{
	
	private static final long serialVersionUID = 1L;
	
	private MyAdviceAttributeSourcePointcut pointcut = new MyAdviceAttributeSourcePointcut();
	
	public BeanFactoryMyAdviceAttributeSourceAdvisor() {
		setAdvice(new MyAdviceInterceptor());
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}
	
	

}
