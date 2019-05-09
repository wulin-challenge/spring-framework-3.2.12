/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.framework.autoproxy;

import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.OrderComparator;

/**
 * Generic auto proxy creator that builds AOP proxies for specific beans
 * based on detected Advisors for each bean.
 * 
 * <p> 通用自动代理创建程序，它根据检测到的每个bean的Advisors为特定bean构建AOP代理。
 *
 * <p>Subclasses must implement the abstract {@link #findCandidateAdvisors()}
 * method to return a list of Advisors applying to any object. Subclasses can
 * also override the inherited {@link #shouldSkip} method to exclude certain
 * objects from auto-proxying.
 * 
 * <p> 子类必须实现抽象的findCandidateAdvisors（）方法，以返回应用于任何对象的顾问列表。 
 * 子类还可以覆盖继承的shouldSkip方法，以从自动代理中排除某些对象。
 *
 * <p>Advisors or advices requiring ordering should implement the
 * {@link org.springframework.core.Ordered} interface. This class sorts
 * Advisors by Ordered order value. Advisors that don't implement the
 * Ordered interface will be considered as unordered; they will appear
 * at the end of the advisor chain in undefined order.
 * 
 * <p> 需要订购的顾问或建议应实施org.springframework.core.Ordered接口。 此类按排序值对Advisors进行排序。 
 * 未实施Ordered接口的顾问将被视为无序; 它们将以未定义的顺序出现在顾问链的末尾。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalStateException("Cannot use AdvisorAutoProxyCreator without a ConfigurableListableBeanFactory");
		}
		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}


	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String beanName, TargetSource targetSource) {
		List advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * Find all eligible Advisors for auto-proxying this class.
	 * 
	 * <p> 找到所有符合条件的自动代理此类class的Advisors。
	 * 
	 * @param beanClass the clazz to find advisors for - 寻找advisors的clazz
	 * @param beanName the name of the currently proxied bean - 当前代理的bean的名称
	 * @return the empty List, not {@code null},
	 * if there are no pointcuts or interceptors
	 * 
	 * <p> 如果没有切入点或拦截器，则为空List，而不为null
	 * 
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	protected List<Advisor> findEligibleAdvisors(Class beanClass, String beanName) {
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * Find all candidate Advisors to use in auto-proxying.
	 * 
	 * <p> 找到要在自动代理中使用的所有候选Advisors。
	 * 
	 * @return the List of candidate Advisors - 候选Advisors名单
	 */
	protected List<Advisor> findCandidateAdvisors() {
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * Search the given candidate Advisors to find all Advisors that
	 * can apply to the specified bean.
	 * 
	 * <p> 搜索给定的候选Advisors，找到可以应用于指定bean的所有Advisors。
	 * 
	 * @param candidateAdvisors the candidate Advisors - 候选Advisors
	 * @param beanClass the target's bean class -目标的bean类
	 * @param beanName the target's bean name - 目标的bean名称
	 * @return the List of applicable Advisors - 适用Advisors名单
	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
	 */
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class beanClass, String beanName) {

		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			//过滤已经得到的 advisors
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * Return whether the Advisor bean with the given name is eligible
	 * for proxying in the first place.
	 * 
	 * <p> 返回具有给定名称的Advisor bean是否有资格首先进行代理。
	 * 
	 * @param beanName the name of the Advisor bean - Advisor bean的名称
	 * @return whether the bean is eligible - bean是否符合条件
	 */
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * Sort advisors based on ordering. Subclasses may choose to override this
	 * method to customize the sorting strategy.
	 * 
	 * <p> 根据订购对顾问进行排序。 子类可以选择覆盖此方法以自定义排序策略。
	 * 
	 * @param advisors the source List of Advisors - 顾问清单来源
	 * @return the sorted List of Advisors - 顾问的排序清单
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.OrderComparator
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		OrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * Extension hook that subclasses can override to register additional Advisors,
	 * given the sorted Advisors obtained to date.
	 * 
	 * <p> 给定到目前为止获得的排序Advisors，子类可以覆盖的扩展钩子以注册其他Advisors。
	 * 
	 * <p>The default implementation is empty.
	 * 
	 * <p> 默认实现为空。
	 * 
	 * <p>Typically used to add Advisors that expose contextual information
	 * required by some of the later advisors.
	 * 
	 * <p> 通常用于添加Advisors，公开某些后期顾问所需的上下文信息。
	 * 
	 * @param candidateAdvisors Advisors that have already been identified as
	 * applying to a given bean
	 * 
	 * <p> 已被确定为适用于给定bean的顾问
	 * 
	 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * This auto-proxy creator always returns pre-filtered Advisors.
	 * 
	 * <p> 此自动代理创建者始终返回前置过滤的Advisors。
	 * 
	 */
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
	 * surrounding AbstractAdvisorAutoProxyCreator facilities.
	 * 
	 * <p> BeanFactoryAdvisorRetrievalHelper的子类，它委托给周围的AbstractAdvisorAutoProxyCreator工具。
	 * 
	 */
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}
