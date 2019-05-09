/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.aop.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInfo;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * Base class for AOP proxy configuration managers.
 * These are not themselves AOP proxies, but subclasses of this class are
 * normally factories from which AOP proxy instances are obtained directly.
 * 
 * <p> AOP代理配置管理器的基类。 它们本身不是AOP代理，但是这个类的子类通常是直接从中获取AOP代理实例的工厂。
 *
 * <p>This class frees subclasses of the housekeeping of Advices
 * and Advisors, but doesn't actually implement proxy creation
 * methods, which are provided by subclasses.
 * 
 * <p> 此类释放了Advices和Advisors管理的子类，但实际上并未实现子类提供的代理创建方法。
 *
 * <p>This class is serializable; subclasses need not be.
 * This class is used to hold snapshots of proxies.
 * 
 * <p> 这个类是可序列化的; 子类不必是。 此类用于保存代理的快照。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AopProxy
 */
@SuppressWarnings("unchecked")
public class AdvisedSupport extends ProxyConfig implements Advised {

	/** use serialVersionUID from Spring 2.0 for interoperability */
	/** 使用Spring 2.0中的serialVersionUID实现互操作性 */
	private static final long serialVersionUID = 2651364800145442165L;


	/**
	 * Canonical TargetSource when there's no target, and behavior is
	 * supplied by the advisors.
	 * 
	 * <p> Canonical TargetSource，当没有目标时，行为由顾问提供。
	 * 
	 */
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;


	/** Package-protected to allow direct access for efficiency */
	/** 包保护允许直接访问以提高效率 */
	TargetSource targetSource = EMPTY_TARGET_SOURCE;

	/** Whether the Advisors are already filtered for the specific target class */
	/** 是否已针对特定目标类过滤了Advisors */
	private boolean preFiltered = false;

	/** The AdvisorChainFactory to use */
	/** 要使用的AdvisorChainFactory */
	AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();

	/** Cache with Method as key and advisor chain List as value */
	/** 使用Method作为键缓存，顾问链列表作为值 */
	private transient Map<MethodCacheKey, List<Object>> methodCache;

	/**
	 * Interfaces to be implemented by the proxy. Held in List to keep the order
	 * of registration, to create JDK proxy with specified order of interfaces.
	 * 
	 * <p> 由代理实现的接口。 在List中保持以保持注册顺序，以指定的接口顺序创建JDK代理。
	 */
	private List<Class> interfaces = new ArrayList<Class>();

	/**
	 * List of Advisors. If an Advice is added, it will be wrapped
	 * in an Advisor before being added to this List.
	 * 
	 * <p> Advisors名单。 如果添加了建议，则在添加到此列表之前，它将被包装在Advisor中。
	 */
	private List<Advisor> advisors = new LinkedList<Advisor>();

	/**
	 * Array updated on changes to the advisors list, which is easier
	 * to manipulate internally.
	 * 
	 * <p> 更新了advisors列表更改的数组，更容易在内部操作。
	 */
	private Advisor[] advisorArray = new Advisor[0];


	/**
	 * No-arg constructor for use as a JavaBean.
	 * 
	 * <p> 用于JavaBean的No-arg构造函数。
	 */
	public AdvisedSupport() {
		initMethodCache();
	}

	/**
	 * Create a AdvisedSupport instance with the given parameters.
	 * 
	 * <p> 使用给定参数创建AdvisedSupport实例。
	 * 
	 * @param interfaces the proxied interfaces - 代理接口
	 */
	public AdvisedSupport(Class[] interfaces) {
		this();
		setInterfaces(interfaces);
	}

	/**
	 * Initialize the method cache.
	 * 
	 * <p> 初始化方法缓存。
	 * 
	 */
	private void initMethodCache() {
		this.methodCache = new ConcurrentHashMap<MethodCacheKey, List<Object>>(32);
	}


	/**
	 * Set the given object as target.
	 * Will create a SingletonTargetSource for the object.
	 * 
	 * <p> 将给定对象设置为目标。 将为该对象创建SingletonTargetSource。
	 * @see #setTargetSource
	 * @see org.springframework.aop.target.SingletonTargetSource
	 */
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}

	public void setTargetSource(TargetSource targetSource) {
		this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
	}

	public TargetSource getTargetSource() {
		return this.targetSource;
	}

	/**
	 * Set a target class to be proxied, indicating that the proxy
	 * should be castable to the given class.
	 * 
	 * <p> 设置要代理的目标类，指示代理应该可以转换为给定的类。
	 * 
	 * <p>Internally, an {@link org.springframework.aop.target.EmptyTargetSource}
	 * for the given target class will be used. The kind of proxy needed
	 * will be determined on actual creation of the proxy.
	 * 
	 * <p> 在内部，将使用给定目标类的org.springframework.aop.target.EmptyTargetSource。 
	 * 所需的代理类型将根据代理的实际创建来确定。
	 * 
	 * <p>This is a replacement for setting a "targetSource" or "target",
	 * for the case where we want a proxy based on a target class
	 * (which can be an interface or a concrete class) without having
	 * a fully capable TargetSource available.
	 * 
	 * <p> 对于我们想要基于目标类（可以是接口或具体类）的代理而没有完全可用的TargetSource的情况，
	 * 这可以替代设置“targetSource”或“target”。
	 * 
	 * @see #setTargetSource
	 * @see #setTarget
	 */
	public void setTargetClass(Class<?> targetClass) {
		this.targetSource = EmptyTargetSource.forClass(targetClass);
	}

	public Class<?> getTargetClass() {
		return this.targetSource.getTargetClass();
	}

	public void setPreFiltered(boolean preFiltered) {
		this.preFiltered = preFiltered;
	}

	public boolean isPreFiltered() {
		return this.preFiltered;
	}

	/**
	 * Set the advisor chain factory to use.
	 * 
	 * <p> 设置顾问链工厂使用。
	 * 
	 * <p>Default is a {@link DefaultAdvisorChainFactory}.
	 * 
	 * <p> 默认值是DefaultAdvisorChainFactory。
	 * 
	 */
	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		Assert.notNull(advisorChainFactory, "AdvisorChainFactory must not be null");
		this.advisorChainFactory = advisorChainFactory;
	}

	/**
	 * Return the advisor chain factory to use (never {@code null}).
	 * 
	 * <p> 返回使用的顾问链工厂（从不为null）。
	 * 
	 */
	public AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}


	/**
	 * Set the interfaces to be proxied.
	 * 
	 * <p> 设置要代理的接口。
	 * 
	 */
	public void setInterfaces(Class<?>... interfaces) {
		Assert.notNull(interfaces, "Interfaces must not be null");
		this.interfaces.clear();
		for (Class ifc : interfaces) {
			addInterface(ifc);
		}
	}

	/**
	 * Add a new proxied interface.
	 * 
	 * <p> 添加新的代理接口。
	 * 
	 * @param intf the additional interface to proxy
	 * 
	 * <p> 代理的附加接口
	 * 
	 */
	public void addInterface(Class<?> intf) {
		Assert.notNull(intf, "Interface must not be null");
		if (!intf.isInterface()) {
			throw new IllegalArgumentException("[" + intf.getName() + "] is not an interface");
		}
		if (!this.interfaces.contains(intf)) {
			this.interfaces.add(intf);
			adviceChanged();
		}
	}

	/**
	 * Remove a proxied interface.
	 * 
	 * <p> 删除代理接口。
	 * 
	 * <p>Does nothing if the given interface isn't proxied.
	 * 
	 * <p> 如果给定的接口未被代理，则不执行任何操作。
	 * 
	 * @param intf the interface to remove from the proxy
	 * 
	 * <p> 要从代理中删除的接口
	 * 
	 * @return {@code true} if the interface was removed; {@code false}
	 * if the interface was not found and hence could not be removed
	 * 
	 * <p> 如果删除了接口，则为true; 如果未找到接口，则为false，因此无法删除
	 * 
	 */
	public boolean removeInterface(Class<?> intf) {
		return this.interfaces.remove(intf);
	}

	public Class<?>[] getProxiedInterfaces() {
		return this.interfaces.toArray(new Class[this.interfaces.size()]);
	}

	public boolean isInterfaceProxied(Class<?> intf) {
		for (Class proxyIntf : this.interfaces) {
			if (intf.isAssignableFrom(proxyIntf)) {
				return true;
			}
		}
		return false;
	}


	public final Advisor[] getAdvisors() {
		return this.advisorArray;
	}

	public void addAdvisor(Advisor advisor) {
		int pos = this.advisors.size();
		addAdvisor(pos, advisor);
	}

	public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
		if (advisor instanceof IntroductionAdvisor) {
			validateIntroductionAdvisor((IntroductionAdvisor) advisor);
		}
		addAdvisorInternal(pos, advisor);
	}

	public boolean removeAdvisor(Advisor advisor) {
		int index = indexOf(advisor);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

	public void removeAdvisor(int index) throws AopConfigException {
		if (isFrozen()) {
			throw new AopConfigException("Cannot remove Advisor: Configuration is frozen.");
		}
		if (index < 0 || index > this.advisors.size() - 1) {
			throw new AopConfigException("Advisor index " + index + " is out of bounds: " +
					"This configuration only has " + this.advisors.size() + " advisors.");
		}

		Advisor advisor = this.advisors.get(index);
		if (advisor instanceof IntroductionAdvisor) {
			IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
			// We need to remove introduction interfaces.
			// 我们需要删除介绍接口。
			for (int j = 0; j < ia.getInterfaces().length; j++) {
				removeInterface(ia.getInterfaces()[j]);
			}
		}

		this.advisors.remove(index);
		updateAdvisorArray();
		adviceChanged();
	}

	public int indexOf(Advisor advisor) {
		Assert.notNull(advisor, "Advisor must not be null");
		return this.advisors.indexOf(advisor);
	}

	public boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
		Assert.notNull(a, "Advisor a must not be null");
		Assert.notNull(b, "Advisor b must not be null");
		int index = indexOf(a);
		if (index == -1) {
			return false;
		}
		removeAdvisor(index);
		addAdvisor(index, b);
		return true;
	}

	/**
	 * Add all of the given advisors to this proxy configuration.
	 * 
	 * <p> 将所有给定的顾问程序添加到此代理配置中。
	 * 
	 * @param advisors the advisors to register
	 * 
	 * <p> 注册的顾问
	 * 
	 * @deprecated as of Spring 3.0, in favor of {@link #addAdvisors}
	 * 
	 * <p> 从Spring 3.0开始，支持addAdvisors
	 * 
	 */
	@Deprecated
	public void addAllAdvisors(Advisor[] advisors) {
		addAdvisors(Arrays.asList(advisors));
	}

	/**
	 * Add all of the given advisors to this proxy configuration.
	 * 
	 * <p> 将所有给定的顾问程序添加到此代理配置中。
	 * 
	 * @param advisors the advisors to register - 注册的advisors
	 */
	public void addAdvisors(Advisor... advisors) {
		addAdvisors(Arrays.asList(advisors));
	}

	/**
	 * Add all of the given advisors to this proxy configuration.
	 * 
	 * <p> 将所有给定的顾问程序添加到此代理配置中。
	 * 
	 * @param advisors the advisors to register - 注册的advisors
	 */
	public void addAdvisors(Collection<Advisor> advisors) {
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (!CollectionUtils.isEmpty(advisors)) {
			for (Advisor advisor : advisors) {
				if (advisor instanceof IntroductionAdvisor) {
					validateIntroductionAdvisor((IntroductionAdvisor) advisor);
				}
				Assert.notNull(advisor, "Advisor must not be null");
				this.advisors.add(advisor);
			}
			updateAdvisorArray();
			adviceChanged();
		}
	}

	private void validateIntroductionAdvisor(IntroductionAdvisor advisor) {
		advisor.validateInterfaces();
		// If the advisor passed validation, we can make the change.
		// 如果advisor通过验证，我们可以进行更改。
		Class[] ifcs = advisor.getInterfaces();
		for (Class ifc : ifcs) {
			addInterface(ifc);
		}
	}

	private void addAdvisorInternal(int pos, Advisor advisor) throws AopConfigException {
		Assert.notNull(advisor, "Advisor must not be null");
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (pos > this.advisors.size()) {
			throw new IllegalArgumentException(
					"Illegal position " + pos + " in advisor list with size " + this.advisors.size());
		}
		this.advisors.add(pos, advisor);
		updateAdvisorArray();
		adviceChanged();
	}

	/**
	 * Bring the array up to date with the list.
	 * 
	 * <p> 使列表更新阵列。
	 */
	protected final void updateAdvisorArray() {
		this.advisorArray = this.advisors.toArray(new Advisor[this.advisors.size()]);
	}

	/**
	 * Allows uncontrolled access to the {@link List} of {@link Advisor Advisors}.
	 * 
	 * <p> 允许不受控制地访问顾问列表。
	 * 
	 * <p>Use with care, and remember to {@link #updateAdvisorArray() refresh the advisor array}
	 * and {@link #adviceChanged() fire advice changed events} when making any modifications.
	 * 
	 * <p> 请小心使用，并记住在进行任何修改时刷新顾问程序数组并触发建议更改事件。
	 */
	protected final List<Advisor> getAdvisorsInternal() {
		return this.advisors;
	}


	public void addAdvice(Advice advice) throws AopConfigException {
		int pos = this.advisors.size();
		addAdvice(pos, advice);
	}

	/**
	 * Cannot add introductions this way unless the advice implements IntroductionInfo.
	 * 
	 * <p> 除非建议实现了IntroductionInfo，否则不能以这种方式添加介绍。
	 * 
	 */
	public void addAdvice(int pos, Advice advice) throws AopConfigException {
		Assert.notNull(advice, "Advice must not be null");
		if (advice instanceof IntroductionInfo) {
			// We don't need an IntroductionAdvisor for this kind of introduction:
			// It's fully self-describing.
			
			// 对于这种介绍，我们不需要IntroductionAdvisor：它完全是自描述的。
			addAdvisor(pos, new DefaultIntroductionAdvisor(advice, (IntroductionInfo) advice));
		}
		else if (advice instanceof DynamicIntroductionAdvice) {
			// We need an IntroductionAdvisor for this kind of introduction.
			// 我们需要一个IntroductionAdvisor来进行此类介绍。
			throw new AopConfigException("DynamicIntroductionAdvice may only be added as part of IntroductionAdvisor");
		}
		else {
			addAdvisor(pos, new DefaultPointcutAdvisor(advice));
		}
	}

	public boolean removeAdvice(Advice advice) throws AopConfigException {
		int index = indexOf(advice);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

	public int indexOf(Advice advice) {
		Assert.notNull(advice, "Advice must not be null");
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Is the given advice included in any advisor within this proxy configuration?
	 * 
	 * <p> 此代理配置中的任何顾问程序中是否包含给定的建议？
	 * 
	 * @param advice the advice to check inclusion of - 检查包含的advice
	 * @return whether this advice instance is included - 是否包含此advice实例
	 */
	public boolean adviceIncluded(Advice advice) {
		if (advice != null) {
			for (Advisor advisor : this.advisors) {
				if (advisor.getAdvice() == advice) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Count advices of the given class.
	 * 
	 * <p> 计算给定类的advices。
	 * 
	 * @param adviceClass the advice class to check - 要检查的advice类
	 * @return the count of the interceptors of this class or subclasses
	 * 
	 * <p> 此类或子类的拦截器数
	 * 
	 */
	public int countAdvicesOfType(Class adviceClass) {
		int count = 0;
		if (adviceClass != null) {
			for (Advisor advisor : this.advisors) {
				if (adviceClass.isInstance(advisor.getAdvice())) {
					count++;
				}
			}
		}
		return count;
	}


	/**
	 * Determine a list of {@link org.aopalliance.intercept.MethodInterceptor} objects
	 * for the given method, based on this configuration.
	 * 
	 * <p> 根据此配置确定给定方法的org.aopalliance.intercept.MethodInterceptor对象列表。
	 * 
	 * @param method the proxied method - 代理方法
	 * @param targetClass the target class - 目标类
	 * @return List of MethodInterceptors (may also include InterceptorAndDynamicMethodMatchers)
	 * 
	 * <p> MethodInterceptors列表（也可能包含InterceptorAndDynamicMethodMatchers）
	 * 
	 */
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class targetClass) {
		MethodCacheKey cacheKey = new MethodCacheKey(method);
		List<Object> cached = this.methodCache.get(cacheKey);
		if (cached == null) {
			cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
					this, method, targetClass);
			this.methodCache.put(cacheKey, cached);
		}
		return cached;
	}

	/**
	 * Invoked when advice has changed.
	 * 
	 * <p> 建议发生变化时调用。
	 * 
	 */
	protected void adviceChanged() {
		this.methodCache.clear();
	}

	/**
	 * Call this method on a new instance created by the no-arg constructor
	 * to create an independent copy of the configuration from the given object.
	 * 
	 * <p> 在no-arg构造函数创建的新实例上调用此方法，以从给定对象创建配置的独立副本。
	 * 
	 * @param other the AdvisedSupport object to copy configuration from
	 * 
	 * <p> AdvisedSupport对象从中复制配置
	 * 
	 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyConfigurationFrom(other, other.targetSource, new ArrayList<Advisor>(other.advisors));
	}

	/**
	 * Copy the AOP configuration from the given AdvisedSupport object,
	 * but allow substitution of a fresh TargetSource and a given interceptor chain.
	 * 
	 * <p> 从给定的AdvisedSupport对象复制AOP配置，但允许替换新的TargetSource和给定的拦截器链。
	 * 
	 * @param other the AdvisedSupport object to take proxy configuration from
	 * 
	 * <p> AdvisedSupport对象从中获取代理配置
	 * 
	 * @param targetSource the new TargetSource - 新的TargetSource
	 * @param advisors the Advisors for the chain - 链的Advisors
	 */
	protected void copyConfigurationFrom(AdvisedSupport other, TargetSource targetSource, List<Advisor> advisors) {
		copyFrom(other);
		this.targetSource = targetSource;
		this.advisorChainFactory = other.advisorChainFactory;
		this.interfaces = new ArrayList<Class>(other.interfaces);
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor) {
				validateIntroductionAdvisor((IntroductionAdvisor) advisor);
			}
			Assert.notNull(advisor, "Advisor must not be null");
			this.advisors.add(advisor);
		}
		updateAdvisorArray();
		adviceChanged();
	}

	/**
	 * Build a configuration-only copy of this AdvisedSupport,
	 * replacing the TargetSource
	 * 
	 * <p> 构建此AdvisedSupport的仅配置副本，替换TargetSource
	 */
	AdvisedSupport getConfigurationOnlyCopy() {
		AdvisedSupport copy = new AdvisedSupport();
		copy.copyFrom(this);
		copy.targetSource = EmptyTargetSource.forClass(getTargetClass(), getTargetSource().isStatic());
		copy.advisorChainFactory = this.advisorChainFactory;
		copy.interfaces = this.interfaces;
		copy.advisors = this.advisors;
		copy.updateAdvisorArray();
		return copy;
	}


	//---------------------------------------------------------------------
	// Serialization support
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		// 依靠默认序列化; 只是在反序列化后初始化状态。
		ois.defaultReadObject();

		// Initialize transient fields.
		// 初始化瞬态字段。
		initMethodCache();
	}


	public String toProxyConfigString() {
		return toString();
	}

	/**
	 * For debugging/diagnostic use.
	 * 
	 * <p> 用于调试/诊断用途。
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": ").append(this.interfaces.size()).append(" interfaces ");
		sb.append(ClassUtils.classNamesToString(this.interfaces)).append("; ");
		sb.append(this.advisors.size()).append(" advisors ");
		sb.append(this.advisors).append("; ");
		sb.append("targetSource [").append(this.targetSource).append("]; ");
		sb.append(super.toString());
		return sb.toString();
	}


	/**
	 * Simple wrapper class around a Method. Used as the key when
	 * caching methods, for efficient equals and hashCode comparisons.
	 * 
	 * <p> 围绕Method的简单包装类。 用作缓存方法时的键，用于有效的equals和hashCode比较。
	 */
	private static class MethodCacheKey {

		private final Method method;

		private final int hashCode;

		public MethodCacheKey(Method method) {
			this.method = method;
			this.hashCode = method.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			MethodCacheKey otherKey = (MethodCacheKey) other;
			return (this.method == otherKey.method);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

}
