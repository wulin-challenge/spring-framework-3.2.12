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

package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * Base interface holding AOP <b>advice</b> (action to take at a joinpoint)
 * and a filter determining the applicability of the advice (such as
 * a pointcut). <i>This interface is not for use by Spring users, but to
 * allow for commonality in support for different types of advice.</i>
 * 
 * <p> 基于接口持有Aop的advice（在连接点采取的动作）和确定advice适用性的过滤器（例如切入点）。
 *  此接口不供Spring用户使用，但允许支持不同类型的advice的通用性。
 *
 * <p>Spring AOP is based around <b>around advice</b> delivered via method
 * <b>interception</b>, compliant with the AOP Alliance interception API.
 * The Advisor interface allows support for different types of advice,
 * such as <b>before</b> and <b>after</b> advice, which need not be
 * implemented using interception.
 * 
 * <p> Spring AOP基于通过方法拦截提供的advice，符合AOP Alliance拦截API。 
 * Advisor接口允许支持不同类型的advice，例如advice之前和之后，不需要使用拦截来实现。
 *
 * @author Rod Johnson
 */
public interface Advisor {

	/**
	 * Return the advice part of this aspect. An advice may be an
	 * interceptor, a before advice, a throws advice, etc.
	 * 
	 * <p> 返回这方面的advice部分。 advice可能是拦截器，一个before advice ，一个 throws advice等。
	 * 
	 * @return the advice that should apply if the pointcut matches
	 * 
	 * <p> 切入点匹配时应该应用的advice
	 * 
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see BeforeAdvice
	 * @see ThrowsAdvice
	 * @see AfterReturningAdvice
	 */
	Advice getAdvice();

	/**
	 * Return whether this advice is associated with a particular instance
	 * (for example, creating a mixin) or shared with all instances of
	 * the advised class obtained from the same Spring bean factory.
	 * 
	 * <p> 返回此advice是否与特定实例相关联（例如，创建mixin）或与从同一Spring bean工厂获取的advice类的所有实例共享。
	 * 
	 * <p><b>Note that this method is not currently used by the framework.</b>
	 * Typical Advisor implementations always return {@code true}.
	 * Use singleton/prototype bean definitions or appropriate programmatic
	 * proxy creation to ensure that Advisors have the correct lifecycle model.
	 * 
	 * <p> 请注意，框架当前不使用此方法。 典型的Advisor实现总是返回true。 使用singleton / prototype bean定义或
	 * 适当的编程代理创建来确保Advisor具有正确的生命周期模型。
	 * 
	 * @return whether this advice is associated with a particular target instance
	 * 
	 * <p> 此advice是否与特定目标实例相关联
	 */
	boolean isPerInstance();

}
