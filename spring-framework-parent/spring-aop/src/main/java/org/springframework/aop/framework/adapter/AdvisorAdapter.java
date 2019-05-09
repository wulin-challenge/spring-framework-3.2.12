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

package org.springframework.aop.framework.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;

/**
 * Interface allowing extension to the Spring AOP framework to allow
 * handling of new Advisors and Advice types.
 * 
 * <p> 接口允许扩展到Spring AOP框架，以允许处理新的Advisors和Advice类型。
 *
 * <p>Implementing objects can create AOP Alliance Interceptors from
 * custom advice types, enabling these advice types to be used
 * in the Spring AOP framework, which uses interception under the covers.
 * 
 * <p> 实现对象可以从自定义通知类型创建AOP联盟拦截器，使这些通知类型可以在Spring AOP框架中使用，该框架使用拦截。
 *
 * <p>There is no need for most Spring users to implement this interface;
 * do so only if you need to introduce more Advisor or Advice types to Spring.
 * 
 * <p> 大多数Spring用户不需要实现此接口; 只有在需要向Spring引入更多Advisor或Advice类型时才这样做。
 *
 * @author Rod Johnson
 */
public interface AdvisorAdapter {

	/**
	 * Does this adapter understand this advice object? Is it valid to
	 * invoke the {@code getInterceptors} method with an Advisor that
	 * contains this advice as an argument?
	 * 
	 * <p> 此适配器是否知道此advice对象？ 使用包含此advice的Advisor作为参数调用getInterceptors方法是否有效？
	 * 
	 * @param advice an Advice such as a BeforeAdvice
	 * 
	 * <p> 诸如BeforeAdvice之类的Advice
	 * 
	 * @return whether this adapter understands the given advice object
	 * 
	 * <p> 此适配器是否理解给定的advice对象
	 * 
	 * @see #getInterceptor(org.springframework.aop.Advisor)
	 * @see org.springframework.aop.BeforeAdvice
	 */
	boolean supportsAdvice(Advice advice);

	/**
	 * Return an AOP Alliance MethodInterceptor exposing the behavior of
	 * the given advice to an interception-based AOP framework.
	 * 
	 * <p> 返回AOP Alliance MethodInterceptor，将给定advice的行为暴露给基于拦截的AOP框架。
	 * 
	 * <p>Don't worry about any Pointcut contained in the Advisor;
	 * the AOP framework will take care of checking the pointcut.
	 * 
	 * <p> 不要担心Advisor中包含的任何Pointcut; AOP框架将负责检查切入点。
	 * 
	 * @param advisor the Advisor. The supportsAdvice() method must have
	 * returned true on this object
	 * 
	 * <p> Advisor。 supportsAdvice（）方法必须在此对象上返回true
	 * 
	 * @return an AOP Alliance interceptor for this Advisor. There's
	 * no need to cache instances for efficiency, as the AOP framework
	 * caches advice chains.
	 * 
	 * <p> 这个Advisor的AOP联盟拦截器。 由于AOP框架缓存了advice链，因此无需为了提高效率而缓存实例。
	 * 
	 */
	MethodInterceptor getInterceptor(Advisor advisor);

}
