/*
 * Copyright 2002-2014 the original author or authors.
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that wraps each eligible bean with an AOP proxy, delegating to specified interceptors
 * before invoking the bean itself.
 * 
 * <p> org.springframework.beans.factory.config.BeanPostProcessor实现，
 * 它使用AOP代理包装每个符合条件的bean，在调用bean本身之前委托给指定的拦截器。
 *
 * <p>This class distinguishes between "common" interceptors: shared for all proxies it
 * creates, and "specific" interceptors: unique per bean instance. There need not
 * be any common interceptors. If there are, they are set using the interceptorNames
 * property. As with ProxyFactoryBean, interceptors names in the current factory
 * are used rather than bean references to allow correct handling of prototype
 * advisors and interceptors: for example, to support stateful mixins.
 * Any advice type is supported for "interceptorNames" entries.
 * 
 * <p> 此类区分“常见”拦截器：为其创建的所有代理共享，以及“特定”拦截器：每个bean实例唯一。不需要任何常见的拦截器。
 * 如果有，则使用interceptorNames属性设置它们。与ProxyFactoryBean一样，使用当前工厂中的拦截器名称而不是bean
 * 引用来允许正确处理原型顾问程序和拦截器：例如，支持有状态的mixin。 “interceptorNames”条目支持任何建议类型。
 *
 * <p>Such auto-proxying is particularly useful if there's a large number of beans that
 * need to be wrapped with similar proxies, i.e. delegating to the same interceptors.
 * Instead of x repetitive proxy definitions for x target beans, you can register
 * one single such post processor with the bean factory to achieve the same effect.
 * 
 * <p> 如果存在大量需要用类似代理包装的bean，即委托给相同的拦截器，则这种自动代理特别有用。
 * 而不是x目标bean的x重复代理定义，您可以使用bean工厂注册一个这样的后处理器以实现相同的效果。
 *
 * <p>Subclasses can apply any strategy to decide if a bean is to be proxied,
 * e.g. by type, by name, by definition details, etc. They can also return
 * additional interceptors that should just be applied to the specific bean
 * instance. The default concrete implementation is BeanNameAutoProxyCreator,
 * identifying the beans to be proxied via a list of bean names.
 * 
 * <p> 子类可以应用任何策略来决定是否要代理bean，例如按类型，按名称，按定义细节等。
 * 它们还可以返回应该只应用于特定bean实例的其他拦截器。默认的具体实现是BeanNameAutoProxyCreator，
 * 通过bean名称列表标识要代理的bean。
 *
 * <p>Any number of {@link TargetSourceCreator} implementations can be used to create
 * a custom target source - for example, to pool prototype objects. Auto-proxying will
 * occur even if there is no advice, as long as a TargetSourceCreator specifies a custom
 * {@link org.springframework.aop.TargetSource}. If there are no TargetSourceCreators set,
 * or if none matches, a {@link org.springframework.aop.target.SingletonTargetSource}
 * will be used by default to wrap the target bean instance.
 * 
 * <p> 可以使用任意数量的TargetSourceCreator实现来创建自定义目标源 - 例如，池化原型对象。
 * 只要TargetSourceCreator指定自定义org.springframework.aop.TargetSource，即使没有建议，也会发生自动代理。
 * 如果没有设置TargetSourceCreators，或者如果没有匹配，则默认情况下将使用
 * org.springframework.aop.target.SingletonTargetSource来包装目标bean实例。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 13.10.2003
 * @see #setInterceptorNames
 * @see #getAdvicesAndAdvisorsForBean
 * @see BeanNameAutoProxyCreator
 * @see DefaultAdvisorAutoProxyCreator
 */
@SuppressWarnings("serial")
public abstract class AbstractAutoProxyCreator extends ProxyConfig
		implements SmartInstantiationAwareBeanPostProcessor, BeanClassLoaderAware, BeanFactoryAware,
		Ordered, AopInfrastructureBean {

	/**
	 * Convenience constant for subclasses: Return value for "do not proxy".
	 * 
	 * <p> 子类的便捷常量：“不代理”的返回值。
	 * 
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	protected static final Object[] DO_NOT_PROXY = null;

	/**
	 * Convenience constant for subclasses: Return value for
	 * "proxy without additional interceptors, just the common ones".
	 * 
	 * <p> 子类的便捷常量：“没有额外拦截器的代理，只是常见的拦截器”的返回值。
	 * 
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];


	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Default value is same as non-ordered */
	/** 默认值与非订购相同 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	/** Default is global AdvisorAdapterRegistry */
	/** 默认为全局AdvisorAdapterRegistry */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/**
	 * Indicates whether or not the proxy should be frozen. Overridden from super
	 * to prevent the configuration from becoming frozen too early.
	 * 
	 * <p> 指示是否应冻结代理。 从超级覆盖以防止配置过早冻结。
	 * 
	 */
	private boolean freezeProxy = false;

	/** Default is no common interceptors */
	/** 默认是没有常见的拦截器 */
	private String[] interceptorNames = new String[0];

	private boolean applyCommonInterceptorsFirst = true;

	private TargetSourceCreator[] customTargetSourceCreators;

	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	private boolean classLoaderConfigured = false;

	private BeanFactory beanFactory;

	private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<Object, Boolean>(64);

	// using a ConcurrentHashMap as a Set
	// 使用ConcurrentHashMap作为Set
	private final Map<String, Boolean> targetSourcedBeans = new ConcurrentHashMap<String, Boolean>(16);

	// using a ConcurrentHashMap as a Set
	// 使用ConcurrentHashMap作为Set
	private final Map<Object, Boolean> earlyProxyReferences = new ConcurrentHashMap<Object, Boolean>(16);

	private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<Object, Class<?>>(16);


	/**
	 * Set the ordering which will apply to this class's implementation
	 * of Ordered, used when applying multiple BeanPostProcessors.
	 * 
	 * <p> 设置将应用于此类的Ordered实现的顺序，在应用多个BeanPostProcessors时使用。
	 * 
	 * <p>Default value is {@code Integer.MAX_VALUE}, meaning that it's non-ordered.
	 * 
	 * <p> 默认值为Integer.MAX_VALUE，表示它是非有序的。
	 * 
	 * @param order ordering value - 排序值
	 */
	public final void setOrder(int order) {
		this.order = order;
	}

	public final int getOrder() {
		return this.order;
	}

	/**
	 * Set whether or not the proxy should be frozen, preventing advice
	 * from being added to it once it is created.
	 * 
	 * <p> 设置是否应冻结代理，防止在创建建议后将建议添加到其中。
	 * 
	 * <p>Overridden from the super class to prevent the proxy configuration
	 * from being frozen before the proxy is created.
	 * 
	 * <p> 从超类重写以防止在创建代理之前冻结代理配置。
	 * 
	 */
	@Override
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}

	@Override
	public boolean isFrozen() {
		return this.freezeProxy;
	}

	/**
	 * Specify the AdvisorAdapterRegistry to use.
	 * Default is the global AdvisorAdapterRegistry.
	 * 
	 * <p> 指定要使用的AdvisorAdapterRegistry。 默认是全局AdvisorAdapterRegistry。
	 * 
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * Set custom TargetSourceCreators to be applied in this order.
	 * If the list is empty, or they all return null, a SingletonTargetSource
	 * will be created for each bean.
	 * 
	 * <p> 设置要按此顺序应用的自定义TargetSourceCreators。 如果列表为空，或者它们都返回null，
	 * 则将为每个bean创建SingletonTargetSource。
	 * 
	 * <p>Note that TargetSourceCreators will kick in even for target beans
	 * where no advices or advisors have been found. If a TargetSourceCreator
	 * returns a TargetSource for a specific bean, that bean will be proxied
	 * in any case.
	 * 
	 * <p> 请注意，即使对于没有找到建议或顾问的目标bean，TargetSourceCreators也会启动。 
	 * 如果TargetSourceCreator返回特定bean的TargetSource，则无论如何都将代理该bean。
	 * 
	 * <p>TargetSourceCreators can only be invoked if this post processor is used
	 * in a BeanFactory, and its BeanFactoryAware callback is used.
	 * 
	 * <p> 只有在BeanFactory中使用此后处理器并且使用其BeanFactoryAware回调时，才能调用TargetSourceCreators。
	 * 
	 * @param targetSourceCreators list of TargetSourceCreator.
	 * Ordering is significant: The TargetSource returned from the first matching
	 * TargetSourceCreator (that is, the first that returns non-null) will be used.
	 * 
	 * <p> TargetSourceCreator列表。 排序很重要：将使用从第一个匹配的TargetSourceCreator返回
	 * 的TargetSource（即，返回非null的第一个）。
	 * 
	 */
	public void setCustomTargetSourceCreators(TargetSourceCreator... targetSourceCreators) {
		this.customTargetSourceCreators = targetSourceCreators;
	}

	/**
	 * Set the common interceptors. These must be bean names in the current factory.
	 * They can be of any advice or advisor type Spring supports.
	 * 
	 * <p> 设置常见拦截器。 这些必须是当前工厂中的bean名称。 它们可以是Spring支持的任何建议或顾问类型。
	 * 
	 * <p>If this property isn't set, there will be zero common interceptors.
	 * This is perfectly valid, if "specific" interceptors such as matching
	 * Advisors are all we want.
	 * 
	 * <p> 如果未设置此属性，则将存在零个常见拦截器。 如果我们想要的是“匹配顾问”之类的“特定”拦截器，这是完全有效的。
	 * 
	 */
	public void setInterceptorNames(String... interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	/**
	 * Set whether the common interceptors should be applied before bean-specific ones.
	 * Default is "true"; else, bean-specific interceptors will get applied first.
	 * 
	 * <p> 设置是否应在bean特定的拦截器之前应用常见拦截器。 默认为“true”; 否则，将首先应用特定于bean的拦截器。
	 * 
	 */
	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
	}

	/**
	 * Set the ClassLoader to generate the proxy class in.
	 * 
	 * <p> 设置ClassLoader以生成代理类。
	 * 
	 * <p>Default is the bean ClassLoader, i.e. the ClassLoader used by the
	 * containing BeanFactory for loading all bean classes. This can be
	 * overridden here for specific proxies.
	 * 
	 * <p> 默认是bean ClassLoader，即包含BeanFactory用于加载所有bean类的ClassLoader。 
	 * 对于特定代理，可以在此处覆盖此内容。
	 */
	public void setProxyClassLoader(ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the owning BeanFactory.
	 * May be {@code null}, as this object doesn't need to belong to a bean factory.
	 * 
	 * <p> 返回拥有的BeanFactory。 可以为null，因为此对象不需要属于bean工厂。
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}


	public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
		Object cacheKey = getCacheKey(beanClass, beanName);
		return this.proxyTypes.get(cacheKey);
	}

	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		Object cacheKey = getCacheKey(bean.getClass(), beanName);
		if (!this.earlyProxyReferences.containsKey(cacheKey)) {
			this.earlyProxyReferences.put(cacheKey, Boolean.TRUE);
		}
		return wrapIfNecessary(bean, beanName, cacheKey);
	}

	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		Object cacheKey = getCacheKey(beanClass, beanName);

		if (beanName == null || !this.targetSourcedBeans.containsKey(beanName)) {
			if (this.advisedBeans.containsKey(cacheKey)) {
				return null;
			}
			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return null;
			}
		}

		// Create proxy here if we have a custom TargetSource.
		// Suppresses unnecessary default instantiation of the target bean:
		// The TargetSource will handle target instances in a custom fashion.
		
		// 如果我们有自定义TargetSource，请在此处创建代理。 禁止目标bean的不必要的默认实例化：TargetSource将以自定义方式处理目标实例。
		if (beanName != null) {
			TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
			if (targetSource != null) {
				this.targetSourcedBeans.put(beanName, Boolean.TRUE);
				Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
				Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
				this.proxyTypes.put(cacheKey, proxy.getClass());
				return proxy;
			}
		}

		return null;
	}

	public boolean postProcessAfterInstantiation(Object bean, String beanName) {
		return true;
	}

	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {

		return pvs;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	/**
	 * Create a proxy with the configured interceptors if the bean is
	 * identified as one to proxy by the subclass.
	 * 
	 * <p> 如果bean被标识为子类代理的bean，则使用配置的拦截器创建代理。
	 * 
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean != null) {
			//根据给定的bean的class和name构建出一个key,格式:beanClassname_bean
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (!this.earlyProxyReferences.containsKey(cacheKey)) {
				//如果它合适被代理,则需要封装指定bean
				//是否是由于避免循环依赖而创建的bean代理
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}


	/**
	 * Build a cache key for the given bean class and bean name.
	 * 
	 * <p> 为给定的bean类和bean名称构建缓存键。
	 * 
	 * @param beanClass the bean class - bean的class
	 * @param beanName the bean name - bean的名称
	 * @return the cache key for the given class and name - 给定类和名称的缓存键
	 */
	protected Object getCacheKey(Class<?> beanClass, String beanName) {
		return beanClass.getName() + "_" + beanName;
	}

	/**
	 * Wrap the given bean if necessary, i.e. if it is eligible for being proxied.
	 * 
	 * <p> 必要时包装给定的bean，即它是否有资格被代理。
	 * 
	 * @param bean the raw bean instance - 原始bean实例
	 * @param beanName the name of the bean - bean的名称
	 * @param cacheKey the cache key for metadata access - 用于元数据访问的缓存键
	 * @return a proxy wrapping the bean, or the raw bean instance as-is
	 * 
	 * <p> 包装bean的代理，或原始bean实例的原样
	 * 
	 */
	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		//如果已经处理过
		if (beanName != null && this.targetSourcedBeans.containsKey(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		//给定的bean类是否代表一个基础设施类,基础设施类不应代理,或者配置了指定bean不需要自动代理
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// Create proxy if we have advice.
		// 如果我们有advice，请创建代理。
		
		//如果存在增强方法则创建代理
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		//如果获取到了增强则需要针对增强创建代理
		if (specificInterceptors != DO_NOT_PROXY) {
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			//创建代理
			Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}

	/**
	 * Return whether the given bean class represents an infrastructure class
	 * that should never be proxied.
	 * 
	 * <p> 返回给定的bean类是否表示永远不应该代理的基础结构类。
	 * 
	 * <p>The default implementation considers Advices, Advisors and
	 * AopInfrastructureBeans as infrastructure classes.
	 * 
	 * <p> 默认实现将Advices，Advisors和AopInfrastructureBeans视为基础结构类。
	 * 
	 * @param beanClass the class of the bean - bean的class
	 * @return whether the bean represents an infrastructure class - bean是否代表基础结构类
	 * @see org.aopalliance.aop.Advice
	 * @see org.springframework.aop.Advisor
	 * @see org.springframework.aop.framework.AopInfrastructureBean
	 * @see #shouldSkip
	 */
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
				Advisor.class.isAssignableFrom(beanClass) ||
				AopInfrastructureBean.class.isAssignableFrom(beanClass);
		if (retVal && logger.isTraceEnabled()) {
			logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
		}
		return retVal;
	}

	/**
	 * Subclasses should override this method to return {@code true} if the
	 * given bean should not be considered for auto-proxying by this post-processor.
	 * 
	 * <p> 如果此后处理器不应考虑给定的bean进行自动代理，则子类应重写此方法以返回true。
	 * 
	 * <p>Sometimes we need to be able to avoid this happening if it will lead to
	 * a circular reference. This implementation returns {@code false}.
	 * 
	 * <p> 有时我们需要能够避免这种情况发生，如果它会导致循环引用。 此实现返回false。
	 * 
	 * @param beanClass the class of the bean - bean的class
	 * @param beanName the name of the bean - bean的名称
	 * @return whether to skip the given bean - 是否跳过给定的bean
	 */
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
		return false;
	}

	/**
	 * Create a target source for bean instances. Uses any TargetSourceCreators if set.
	 * Returns {@code null} if no custom TargetSource should be used.
	 * 
	 * <p> 为bean实例创建目标源。 如果设置，则使用任何TargetSourceCreators。 如果不应使用自定义TargetSource，则返回null。
	 * 
	 * <p>This implementation uses the "customTargetSourceCreators" property.
	 * Subclasses can override this method to use a different mechanism.
	 * 
	 * <p> 此实现使用“customTargetSourceCreators”属性。 子类可以重写此方法以使用不同的机制。
	 * 
	 * @param beanClass the class of the bean to create a TargetSource for - 用于创建TargetSource的bean的类
	 * @param beanName the name of the bean - bean的名称
	 * @return a TargetSource for this bean - 这个bean的TargetSource
	 * @see #setCustomTargetSourceCreators
	 */
	protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
		// We can't create fancy target sources for directly registered singletons.
		// 我们无法为直接注册的单例创建想要的目标来源。
		if (this.customTargetSourceCreators != null &&
				this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
			for (TargetSourceCreator tsc : this.customTargetSourceCreators) {
				TargetSource ts = tsc.getTargetSource(beanClass, beanName);
				if (ts != null) {
					// Found a matching TargetSource.
					// 找到了匹配的TargetSource。
					if (logger.isDebugEnabled()) {
						logger.debug("TargetSourceCreator [" + tsc +
								" found custom TargetSource for bean with name '" + beanName + "'");
					}
					return ts;
				}
			}
		}

		// No custom TargetSource found.
		// 找不到自定义TargetSource。
		return null;
	}

	/**
	 * Create an AOP proxy for the given bean.
	 * 
	 * <p> 为给定的bean创建AOP代理。
	 * 
	 * @param beanClass the class of the bean - bean的class
	 * @param beanName the name of the bean - bean的名称
	 * @param specificInterceptors the set of interceptors that is
	 * specific to this bean (may be empty, but not null)
	 * 
	 * <p> 特定于此bean的拦截器集（可能为空，但不为null）
	 * 
	 * @param targetSource the TargetSource for the proxy,
	 * already pre-configured to access the bean
	 * 
	 * <p> 代理的TargetSource，已经预先配置为访问bean
	 * 
	 * @return the AOP proxy for the bean - bean的AOP代理
	 * @see #buildAdvisors
	 */
	protected Object createProxy(
			Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {

		ProxyFactory proxyFactory = new ProxyFactory();
		// Copy our properties (proxyTargetClass etc) inherited from ProxyConfig.
		// 复制从ProxyConfig继承的属性（proxyTargetClass等）。
		
		//获取当前类中相关属性
		proxyFactory.copyFrom(this);

		/**
		 * 决定对于给定的bean是否应该使用 targetClass而不是他的接口代理,
		 * 检测proxyTargetClass设置以及preserveTargetClass
		 */
		if (!shouldProxyTargetClass(beanClass, beanName)) {
			// Must allow for introductions; can't just set interfaces to
			// the target's interfaces only.
			// 必须允许介绍; 不能只设置接口到目标接口。
			Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, this.proxyClassLoader);
			for (Class<?> targetInterface : targetInterfaces) {
				//添加代理接口
				proxyFactory.addInterface(targetInterface);
			}
		}

		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		for (Advisor advisor : advisors) {
			//加入增强器
			proxyFactory.addAdvisor(advisor);
		}

		//设置要代理的类
		proxyFactory.setTargetSource(targetSource);
		//定制代理
		customizeProxyFactory(proxyFactory);

		/**
		 * 用来控制代理工厂被配置之后,是否还允许修改通知,缺省值为false(即在代理被配置之后,不允许修改代理的配置).
		 */
		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) {
			proxyFactory.setPreFiltered(true);
		}

		return proxyFactory.getProxy(this.proxyClassLoader);
	}

	/**
	 * Determine whether the given bean should be proxied with its target
	 * class rather than its interfaces. Checks the
	 * {@link #setProxyTargetClass "proxyTargetClass" setting} as well as the
	 * {@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" attribute}
	 * of the corresponding bean definition.
	 * 
	 * <p> 确定给定的bean是否应该使用其目标类而不是其接口进行代理。 检查“proxyTargetClass”设置以及相应bean定
	 * 义的“preserveTargetClass”属性。
	 * 
	 * @param beanClass the class of the bean - bean的class
	 * @param beanName the name of the bean - bean的名称
	 * @return whether the given bean should be proxied with its target class
	 * 
	 * <p> 是否应该使用其目标类代理给定的bean
	 * 
	 * @see AutoProxyUtils#shouldProxyTargetClass
	 */
	protected boolean shouldProxyTargetClass(Class<?> beanClass, String beanName) {
		return (isProxyTargetClass() ||
				(this.beanFactory instanceof ConfigurableListableBeanFactory &&
						AutoProxyUtils.shouldProxyTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName)));
	}

	/**
	 * Return whether the Advisors returned by the subclass are pre-filtered
	 * to match the bean's target class already, allowing the ClassFilter check
	 * to be skipped when building advisors chains for AOP invocations.
	 * 
	 * <p> 返回子类返回的Advisors是否已经前置过滤以匹配bean的目标类，允许在构建AOP调用的顾问程序链时跳过ClassFilter检查。
	 * 
	 * <p>Default is {@code false}. Subclasses may override this if they
	 * will always return pre-filtered Advisors.
	 * 
	 * <p> 默认值为false。 如果子类始终返回预过滤的Advisor，则子类可以覆盖它。
	 * 
	 * @return whether the Advisors are pre-filtered - Advisors是否经过预先筛选
	 * @see #getAdvicesAndAdvisorsForBean
	 * @see org.springframework.aop.framework.Advised#setPreFiltered
	 */
	protected boolean advisorsPreFiltered() {
		return false;
	}

	/**
	 * Determine the advisors for the given bean, including the specific interceptors
	 * as well as the common interceptor, all adapted to the Advisor interface.
	 * 
	 * <p> 确定给定bean的advisors，包括特定的拦截器以及公共拦截器，这些都适用于Advisor接口。
	 * 
	 * @param beanName the name of the bean - bean的名称
	 * @param specificInterceptors the set of interceptors that is
	 * specific to this bean (may be empty, but not null)
	 * 
	 * <p> 特定于此bean的拦截器集（可能为空，但不为null）
	 * 
	 * @return the list of Advisors for the given bean - 给定bean的Advisors列表
	 */
	protected Advisor[] buildAdvisors(String beanName, Object[] specificInterceptors) {
		// Handle prototypes correctly...
		// 正确处理原型......
		
		//解析注册的所有 interceptorName
		Advisor[] commonInterceptors = resolveInterceptorNames();

		List<Object> allInterceptors = new ArrayList<Object>();
		if (specificInterceptors != null) {
			//加入拦截器
			allInterceptors.addAll(Arrays.asList(specificInterceptors));
			if (commonInterceptors != null) {
				if (this.applyCommonInterceptorsFirst) {
					allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
				}
				else {
					allInterceptors.addAll(Arrays.asList(commonInterceptors));
				}
			}
		}
		if (logger.isDebugEnabled()) {
			int nrOfCommonInterceptors = (commonInterceptors != null ? commonInterceptors.length : 0);
			int nrOfSpecificInterceptors = (specificInterceptors != null ? specificInterceptors.length : 0);
			logger.debug("Creating implicit proxy for bean '" + beanName + "' with " + nrOfCommonInterceptors +
					" common interceptors and " + nrOfSpecificInterceptors + " specific interceptors");
		}

		Advisor[] advisors = new Advisor[allInterceptors.size()];
		for (int i = 0; i < allInterceptors.size(); i++) {
			//拦截器进行封装转化为advisor
			advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
		}
		return advisors;
	}

	/**
	 * Resolves the specified interceptor names to Advisor objects.
	 * 
	 * <p> 将指定的拦截器名称解析为Advisor对象。
	 * 
	 * @see #setInterceptorNames
	 */
	private Advisor[] resolveInterceptorNames() {
		ConfigurableBeanFactory cbf = (this.beanFactory instanceof ConfigurableBeanFactory) ?
				(ConfigurableBeanFactory) this.beanFactory : null;
		List<Advisor> advisors = new ArrayList<Advisor>();
		for (String beanName : this.interceptorNames) {
			if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
				Object next = this.beanFactory.getBean(beanName);
				advisors.add(this.advisorAdapterRegistry.wrap(next));
			}
		}
		return advisors.toArray(new Advisor[advisors.size()]);
	}

	/**
	 * Subclasses may choose to implement this: for example,
	 * to change the interfaces exposed.
	 * 
	 * <p> 子类可以选择实现此目的：例如，更改公开的接口。
	 * 
	 * <p>The default implementation is empty.
	 * 
	 * <p> 默认实现为空。
	 * 
	 * @param proxyFactory ProxyFactory that is already configured with
	 * TargetSource and interfaces and will be used to create the proxy
	 * immediably after this method returns
	 * 
	 * <p> 已经使用TargetSource和接口配置的ProxyFactory将在此方法返回后用于创建代理
	 * 
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}


	/**
	 * Return whether the given bean is to be proxied, what additional
	 * advices (e.g. AOP Alliance interceptors) and advisors to apply.
	 * 
	 * <p> 返回是否要代理给定的bean，要应用的其他advices（例如AOP联盟拦截器）和advisors。
	 * 
	 * @param beanClass the class of the bean to advise - 建议的bean的类
	 * @param beanName the name of the bean - bean的名称
	 * @param customTargetSource the TargetSource returned by the
	 * {@link #getCustomTargetSource} method: may be ignored.
	 * Will be {@code null} if no custom target source is in use.
	 * 
	 * <p> getCustomTargetSource方法返回的TargetSource：可能会被忽略。 如果没有使用自定义目标源，则为null。
	 * 
	 * @return an array of additional interceptors for the particular bean;
	 * or an empty array if no additional interceptors but just the common ones;
	 * or {@code null} if no proxy at all, not even with the common interceptors.
	 * See constants DO_NOT_PROXY and PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS.
	 * 
	 * <p> 特定bean的一系列额外拦截器; 如果没有额外的拦截器而只是常见的拦截器，则为空数组; 如果没有代理，则为null，
	 * 甚至不使用常见拦截器。 请参见常量DO_NOT_PROXY和PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS。
	 * 
	 * @throws BeansException in case of errors - 如果有错误
	 * @see #DO_NOT_PROXY
	 * @see #PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
	 */
	protected abstract Object[] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException;

}
