/*
 * Copyright 2002-2008 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.List;

/**
 * Factory interface for advisor chains.
 * 
 * <p> advisor链的工厂接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AdvisorChainFactory {

	/**
	 * Determine a list of {@link org.aopalliance.intercept.MethodInterceptor} objects
	 * for the given advisor chain configuration.
	 * 
	 * <p> 确定给定advisor程序链配置的org.aopalliance.intercept.MethodInterceptor对象列表。
	 * 
	 * @param config the AOP configuration in the form of an Advised object
	 * 
	 * <p> Advised配置形式的AOP配置
	 * 
	 * @param method the proxied method - 代理方法
	 * @param targetClass the target class - 目标类
	 * @return List of MethodInterceptors (may also include InterceptorAndDynamicMethodMatchers)
	 * 
	 * <p> MethodInterceptors列表（也可能包含InterceptorAndDynamicMethodMatchers）
	 * 
	 */
	List<Object> getInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Method method, Class targetClass);
	
	/*
	 * org.aopalliance.intercept.MethodInterceptor:
	 * 
	 * 在到达目标的路上拦截对接口的调用。 它们嵌套在目标的“顶部”。
	 * 用户应实现invoke（MethodInvocation）方法来修改原始行为。 例如。 下面的类实现了一个跟踪拦截器（跟踪截获的方法上的所有调用）：
	 */

}
