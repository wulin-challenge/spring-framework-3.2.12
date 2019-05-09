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

import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;

/**
 * Interface for registries of Advisor adapters.
 * 
 * <p> Advisor适配器注册表的接口。
 *
 * <p><i>This is an SPI interface, not to be implemented by any Spring user.</i>
 * 
 * <p> 这是一个SPI接口，任何Spring用户都无法实现。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
public interface AdvisorAdapterRegistry {

	/**
	 * Return an Advisor wrapping the given advice.
	 * 
	 * <p> 返回包裹给定advice的Advisor。
	 * 
	 * <p>Should by default at least support
	 * {@link org.aopalliance.intercept.MethodInterceptor},
	 * {@link org.springframework.aop.MethodBeforeAdvice},
	 * {@link org.springframework.aop.AfterReturningAdvice},
	 * {@link org.springframework.aop.ThrowsAdvice}.
	 * 
	 * <p> 默认情况下至少应支持
	 * org.aopalliance.intercept.MethodInterceptor，
	 * org.springframework.aop.MethodBeforeAdvice，
	 * org.springframework.aop.AfterReturningAdvice，
	 * org.springframework.aop.ThrowsAdvice。
	 * 
	 * @param advice object that should be an advice
	 * 
	 * <p> 应该是advice的对象
	 * 
	 * @return an Advisor wrapping the given advice. Never returns {@code null}.
	 * If the advice parameter is an Advisor, return it.
	 * 
	 * <p> 包裹给定advice的Advisor。 永远不会返回null。 如果advice参数是Advisor，则返回它。
	 * 
	 * @throws UnknownAdviceTypeException if no registered advisor adapter
	 * can wrap the supposed advice
	 * 
	 * <p> 如果没有注册的advisor适配器可以包裹所谓的advice
	 * 
	 */
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;

	/**
	 * Return an array of AOP Alliance MethodInterceptors to allow use of the
	 * given Advisor in an interception-based framework.
	 * 
	 * <p> 返回一组AOP Alliance MethodInterceptors，以允许在基于拦截的框架中使用给定的Advisor。
	 * 
	 * <p>Don't worry about the pointcut associated with the Advisor,
	 * if it's a PointcutAdvisor: just return an interceptor.
	 * 
	 * <p> 不要担心与Advisor关联的切入点，如果它是PointcutAdvisor：只返回一个拦截器。
	 * 
	 * @param advisor Advisor to find an interceptor for
	 * 
	 * <p> 寻找拦截器的Advisor
	 * 
	 * @return an array of MethodInterceptors to expose this Advisor's behavior
	 * 
	 * <p> 一组MethodInterceptors，用于公开此Advisor的行为
	 * 
	 * @throws UnknownAdviceTypeException if the Advisor type is
	 * not understood by any registered AdvisorAdapter.
	 * 
	 * <p> 如果任何已注册的AdvisorAdapter不理解Advisor类型。
	 * 
	 */
	MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

	/**
	 * Register the given AdvisorAdapter. Note that it is not necessary to register
	 * adapters for an AOP Alliance Interceptors or Spring Advices: these must be
	 * automatically recognized by an AdvisorAdapterRegistry implementation.
	 * 
	 * <p> 注册给定的AdvisorAdapter。 请注意，没有必要为AOP联盟拦截器或Spring建议注册适配器：
	 * 这些必须由AdvisorAdapterRegistry实现自动识别。
	 * 
	 * @param adapter AdvisorAdapter that understands a particular Advisor
	 * or Advice types
	 * 
	 * <p> AdvisorAdapter，了解特定的Advisor或Advice类型
	 * 
	 */
	void registerAdvisorAdapter(AdvisorAdapter adapter);

}
