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

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;

/**
 * Interface to be implemented by classes that hold the configuration
 * of a factory of AOP proxies. This configuration includes the
 * Interceptors and other advice, and Advisors, and the proxied interfaces.
 * 
 * <p> 由承载AOP代理工厂配置的类实现的接口。 此配置包括拦截器和其他建议，顾问和代理接口。
 *
 * <p>Any AOP proxy obtained from Spring can be cast to this interface to
 * allow manipulation of its AOP advice.
 * 
 * <p> 从Spring获得的任何AOP代理都可以转换为此接口，以允许操作其AOP建议。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.aop.framework.AdvisedSupport
 */
public interface Advised extends TargetClassAware {

	/**
	 * Return whether the Advised configuration is frozen,
	 * in which case no advice changes can be made.
	 * 
	 * <p> 返回是否冻结了Advised配置，在这种情况下，不能进行任何建议更改。
	 * 
	 */
	boolean isFrozen();

	/**
	 * Are we proxying the full target class instead of specified interfaces?
	 * 
	 * <p> 我们代理完整的目标类而不是指定的接口吗？
	 * 
	 */
	boolean isProxyTargetClass();

	/**
	 * Return the interfaces proxied by the AOP proxy. Will not
	 * include the target class, which may also be proxied.
	 * 
	 * <p> 返回AOP代理代理的接口。 不包括目标类，也可能代理。
	 * 
	 */
	Class<?>[] getProxiedInterfaces();

	/**
	 * Determine whether the given interface is proxied.
	 * 
	 * <p> 确定给定接口是否已代理。
	 * 
	 * @param intf the interface to check - 要检查的界面
	 */
	boolean isInterfaceProxied(Class<?> intf);


	/**
	 * Change the TargetSource used by this Advised object.
	 * Only works if the configuration isn't frozen.
	 * 
	 * <p> 更改此Advised对象使用的TargetSource。 仅在未冻结配置时才有效。
	 * 
	 * @param targetSource new TargetSource to use - 要使用的新TargetSource
	 */
	void setTargetSource(TargetSource targetSource);

	/**
	 * Return the TargetSource used by this Advised object.
	 * 
	 * <p> 返回此Advised对象使用的TargetSource。
	 * 
	 */
	TargetSource getTargetSource();

	/**
	 * Set whether the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the AopContext class. This is useful
	 * if an advised object needs to call another advised method on itself.
	 * (If it uses {@code this}, the invocation will not be advised).
	 * 
	 * <p> 设置代理是否应该由AOP框架作为ThreadLocal公开，以便通过AopContext类进行检索。 
	 * 如果建议的对象需要自己调用另一个建议的方法，这将非常有用。 （如果使用它，则不会建议调用）。
	 * 
	 * <p>Default is "false", for optimal performance.
	 * 
	 * <p> 默认为“false”，以获得最佳性能。
	 * 
	 */
	void setExposeProxy(boolean exposeProxy);

	/**
	 * Return whether the factory should expose the proxy as a ThreadLocal.
	 * This can be necessary if a target object needs to invoke a method on itself
	 * benefitting from advice. (If it invokes a method on {@code this} no advice
	 * will apply.) Getting the proxy is analogous to an EJB calling getEJBObject().
	 * 
	 * <p> 返回工厂是否应将代理公开为ThreadLocal。 如果目标对象需要调用自身受益于建议的方法，则这可能是必要的。 
	 * （如果它调用此方法，则不会应用任何建议。）获取代理类似于EJB调用getEJBObject（）。
	 * 
	 * @see AopContext
	 */
	boolean isExposeProxy();

	/**
	 * Set whether this proxy configuration is pre-filtered so that it only
	 * contains applicable advisors (matching this proxy's target class).
	 * 
	 * <p> 设置是否对此代理配置进行预过滤，使其仅包含适用的顾问程序（与此代理的目标类匹配）。
	 * 
	 * <p>Default is "false". Set this to "true" if the advisors have been
	 * pre-filtered already, meaning that the ClassFilter check can be skipped
	 * when building the actual advisor chain for proxy invocations.
	 * 
	 * <p> 默认为“false”。 如果顾问程序已经过预过滤，则将此设置为“true”，这意味着在构建代理调用的实际顾问程序链时可以跳过ClassFilter检查。
	 * 
	 * @see org.springframework.aop.ClassFilter
	 */
	void setPreFiltered(boolean preFiltered);

	/**
	 * Return whether this proxy configuration is pre-filtered so that it only
	 * contains applicable advisors (matching this proxy's target class).
	 * 
	 * <p> 返回此代理配置是否已预过滤，以便它仅包含适用的顾问程序（与此代理的目标类匹配）。
	 * 
	 */
	boolean isPreFiltered();


	/**
	 * Return the advisors applying to this proxy.
	 * 
	 * <p> 返回申请此代理的顾问。
	 * 
	 * @return a list of Advisors applying to this proxy (never {@code null})
	 * 
	 * <p> 适用于此代理的顾问列表（永不为空）
	 * 
	 */
	Advisor[] getAdvisors();

	/**
	 * Add an advisor at the end of the advisor chain.
	 * 
	 * <p> 在顾问链的末尾添加顾问。
	 * 
	 * <p>The Advisor may be an {@link org.springframework.aop.IntroductionAdvisor},
	 * in which new interfaces will be available when a proxy is next obtained
	 * from the relevant factory.
	 * 
	 * <p> 顾问可以是org.springframework.aop.IntroductionAdvisor，其中当下一次从相关工厂获得代理时，新接口将可用。
	 * 
	 * @param advisor the advisor to add to the end of the chain
	 * 
	 * <p> 顾问添加到链的末尾
	 * 
	 * @throws AopConfigException in case of invalid advice - 如果建议无效
	 */
	void addAdvisor(Advisor advisor) throws AopConfigException;

	/**
	 * Add an Advisor at the specified position in the chain.
	 * 
	 * <p> 在链中的指定位置添加Advisor。
	 * 
	 * @param advisor the advisor to add at the specified position in the chain
	 * 
	 * <p> 顾问在链中的指定位置添加
	 * 
	 * @param pos position in chain (0 is head). Must be valid.
	 * 
	 * <p> 链中的位置（0是头）。 必须有效。
	 * 
	 * @throws AopConfigException in case of invalid advice - 如果建议无效
	 */
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

	/**
	 * Remove the given advisor. - 删除给定的顾问。
	 * @param advisor the advisor to remove - 顾问要删除
	 * @return {@code true} if the advisor was removed; {@code false}
	 * if the advisor was not found and hence could not be removed
	 * 
	 * <p> 如果顾问被删除，则为true; 如果未找到顾问，则无法删除，因此无法删除
	 * 
	 */
	boolean removeAdvisor(Advisor advisor);

	/**
	 * Remove the advisor at the given index.
	 * 
	 * <p> 删除给定索引处的顾问程序。
	 * 
	 * @param index index of advisor to remove - 要删除的顾问索引
	 * @throws AopConfigException if the index is invalid - 如果索引无效
	 */
	void removeAdvisor(int index) throws AopConfigException;

	/**
	 * Return the index (from 0) of the given advisor,
	 * or -1 if no such advisor applies to this proxy.
	 * 
	 * <p> 返回给定顾问程序的索引（从0开始），如果没有这样的顾问程序适用于此代理，则返回-1。
	 * 
	 * <p>The return value of this method can be used to index into the advisors array.
	 * 
	 * <p> 此方法的返回值可用于索引到顾问程序数组。
	 * 
	 * @param advisor the advisor to search for - 寻找的顾问
	 * @return index from 0 of this advisor, or -1 if there's no such advisor
	 * 
	 * <p> 此顾问程序的0的索引，如果没有这样的顾问，则为-1
	 * 
	 */
	int indexOf(Advisor advisor);

	/**
	 * Replace the given advisor.
	 * 
	 * <p> 替换给定的顾问。
	 * 
	 * <p><b>Note:</b> If the advisor is an {@link org.springframework.aop.IntroductionAdvisor}
	 * and the replacement is not or implements different interfaces, the proxy will need
	 * to be re-obtained or the old interfaces won't be supported and the new interface
	 * won't be implemented.
	 * 
	 * <p> 注意：如果顾问程序是org.springframework.aop.IntroductionAdvisor并且替换不是或实现不同的接口，
	 * 则需要重新获取代理，否则将不支持旧接口，新接口将不会实现。
	 * 
	 * @param a the advisor to replace - 顾问要更换
	 * @param b the advisor to replace it with - 顾问用它替换它
	 * @return whether it was replaced. If the advisor wasn't found in the
	 * list of advisors, this method returns {@code false} and does nothing.
	 * 
	 * <p> 是否被取代。 如果在顾问列表中找不到顾问程序，则此方法返回false并且不执行任何操作。
	 * 
	 * @throws AopConfigException in case of invalid advice - 如果建议无效
	 */
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;


	/**
	 * Add the given AOP Alliance advice to the tail of the advice (interceptor) chain.
	 * 
	 * <p> 将给定的AOP联盟建议添加到建议（拦截器）链的尾部。
	 * 
	 * <p>This will be wrapped in a DefaultPointcutAdvisor with a pointcut that always
	 * applies, and returned from the {@code getAdvisors()} method in this wrapped form.
	 * 
	 * <p> 这将包含在DefaultPointcutAdvisor中，其中包含始终适用的切入点，并以此包装形式从getAdvisors（）方法返回。
	 * 
	 * <p>Note that the given advice will apply to all invocations on the proxy,
	 * even to the {@code toString()} method! Use appropriate advice implementations
	 * or specify appropriate pointcuts to apply to a narrower set of methods.
	 * 
	 * <p> 请注意，给定的建议将适用于代理上的所有调用，甚至是toString（）方法！ 使用适当的建议实现或指定适当的切入点以应用于更窄的方法集。
	 * 
	 * @param advice advice to add to the tail of the chain - 建议添加到链的尾部
	 * @throws AopConfigException in case of invalid advice - 如果建议无效
	 * @see #addAdvice(int, Advice)
	 * @see org.springframework.aop.support.DefaultPointcutAdvisor
	 */
	void addAdvice(Advice advice) throws AopConfigException;

	/**
	 * Add the given AOP Alliance Advice at the specified position in the advice chain.
	 * 
	 * <p> 在建议链的指定位置添加给定的AOP联盟建议。
	 * 
	 * <p>This will be wrapped in a {@link org.springframework.aop.support.DefaultPointcutAdvisor}
	 * with a pointcut that always applies, and returned from the {@link #getAdvisors()}
	 * method in this wrapped form.
	 * 
	 * <p> 这将包含在org.springframework.aop.support.DefaultPointcutAdvisor中，
	 * 其中包含始终适用的切入点，并以此包装形式从getAdvisors（）方法返回。
	 * 
	 * <p>Note: The given advice will apply to all invocations on the proxy,
	 * even to the {@code toString()} method! Use appropriate advice implementations
	 * or specify appropriate pointcuts to apply to a narrower set of methods.
	 * 
	 * <p> 注意：给定的建议将适用于代理上的所有调用，甚至是toString（）方法！ 使用适当的建议实现或指定适当的切入点以应用于更窄的方法集。
	 * 
	 * @param pos index from 0 (head) - 指数从0（头）
	 * @param advice advice to add at the specified position in the advice chain - 建议添加到建议链中的指定位置
	 * @throws AopConfigException in case of invalid advice - 如果建议无效
	 */
	void addAdvice(int pos, Advice advice) throws AopConfigException;

	/**
	 * Remove the Advisor containing the given advice.
	 * 
	 * <p> 删除包含给定建议的Advisor。
	 * 
	 * @param advice the advice to remove - 删除的建议
	 * @return {@code true} of the advice was found and removed;
	 * {@code false} if there was no such advice
	 * 
	 * <p> 找到并删除了建议的真实性; 如果没有这样的建议，则为假
	 * 
	 */
	boolean removeAdvice(Advice advice);

	/**
	 * Return the index (from 0) of the given AOP Alliance Advice,
	 * or -1 if no such advice is an advice for this proxy.
	 * 
	 * <p> 返回给定AOP联盟建议的索引（从0开始），如果没有这样的建议是对此代理的建议，则返回-1。
	 * 
	 * <p>The return value of this method can be used to index into
	 * the advisors array.
	 * 
	 * <p> 此方法的返回值可用于索引到顾问程序数组。
	 * 
	 * @param advice AOP Alliance advice to search for - AOP联盟建议搜索
	 * @return index from 0 of this advice, or -1 if there's no such advice
	 * 
	 * <p> 此建议的0的索引，如果没有这样的建议，则为-1
	 * 
	 */
	int indexOf(Advice advice);


	/**
	 * As {@code toString()} will normally be delegated to the target,
	 * this returns the equivalent for the AOP proxy.
	 * 
	 * <p> 因为toString（）通常会被委托给目标，所以它返回AOP代理的等价物。
	 * 
	 * @return a string description of the proxy configuration
	 * 
	 * <p> 代理配置的字符串描述
	 * 
	 */
	String toProxyConfigString();

}
