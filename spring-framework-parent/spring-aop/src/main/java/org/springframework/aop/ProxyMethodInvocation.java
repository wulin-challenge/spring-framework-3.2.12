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

import org.aopalliance.intercept.MethodInvocation;

/**
 * Extension of the AOP Alliance {@link org.aopalliance.intercept.MethodInvocation}
 * interface, allowing access to the proxy that the method invocation was made through.
 * 
 * <p> 扩展AOP Alliance org.aopalliance.intercept.MethodInvocation接口，允许访问通过方法调用进行的代理。
 *
 * <p>Useful to be able to substitute return values with the proxy,
 * if necessary, for example if the invocation target returned itself.
 * 
 * <p> 有用的是，如果需要，能够用代理替换返回值，例如，如果调用目标返回自身。
 *
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 1.1.3
 * @see org.springframework.aop.framework.ReflectiveMethodInvocation
 * @see org.springframework.aop.support.DelegatingIntroductionInterceptor
 */
public interface ProxyMethodInvocation extends MethodInvocation {

	/**
	 * Return the proxy that this method invocation was made through.
	 * 
	 * <p> 返回通过此方法调用进行的代理。
	 * 
	 * @return the original proxy object - 原始代理对象
	 */
	Object getProxy();

	/**
	 * Create a clone of this object. If cloning is done before {@code proceed()}
	 * is invoked on this object, {@code proceed()} can be invoked once per clone
	 * to invoke the joinpoint (and the rest of the advice chain) more than once.
	 * 
	 * <p> 创建此对象的克隆。 如果在对此对象调用proceed（）之前完成克隆，
	 * 则可以为每个克隆调用一次proceed（）以多次调用连接点（以及建议链的其余部分）。
	 * 
	 * @return an invocable clone of this invocation.
	 * {@code proceed()} can be called once per clone.
	 * 
	 * <p> 这个调用的可调用克隆。 每个克隆可以调用一次proceed（）。
	 * 
	 */
	MethodInvocation invocableClone();

	/**
	 * Create a clone of this object. If cloning is done before {@code proceed()}
	 * is invoked on this object, {@code proceed()} can be invoked once per clone
	 * to invoke the joinpoint (and the rest of the advice chain) more than once.
	 * 
	 * <p> 创建此对象的克隆。 如果在对此对象调用proceed（）之前完成克隆，则可以为每个克隆调用一次proceed（）
	 * 以多次调用连接点（以及建议链的其余部分）。
	 * 
	 * @param arguments the arguments that the cloned invocation is supposed to use,
	 * overriding the original arguments
	 * 
	 * <p> 克隆调用应该使用的参数，覆盖原始参数
	 * 
	 * @return an invocable clone of this invocation.
	 * {@code proceed()} can be called once per clone.
	 * 
	 * <p> 这个调用的可调用克隆。 每个克隆可以调用一次proceed（）。
	 */
	MethodInvocation invocableClone(Object[] arguments);

	/**
	 * Set the arguments to be used on subsequent invocations in the any advice
	 * in this chain.
	 * 
	 * <p> 在此链中的任何通知中设置要在后续调用中使用的参数。
	 * 
	 * @param arguments the argument array - 参数数组
	 */
	void setArguments(Object[] arguments);

	/**
	 * Add the specified user attribute with the given value to this invocation.
	 * 
	 * <p> 将具有给定值的指定用户属性添加到此调用。
	 * 
	 * <p>Such attributes are not used within the AOP framework itself. They are
	 * just kept as part of the invocation object, for use in special interceptors.
	 * 
	 * <p> 这些属性不在AOP框架内使用。 它们只是作为调用对象的一部分保存，用于特殊拦截器。
	 * 
	 * @param key the name of the attribute - 属性的名称
	 * @param value the value of the attribute, or {@code null} to reset it
	 * 
	 * <p> 属性的值，或null以重置它
	 * 
	 */
	void setUserAttribute(String key, Object value);

	/**
	 * Return the value of the specified user attribute.
	 * 
	 * <p> 返回指定用户属性的值。
	 * 
	 * @param key the name of the attribute - 属性的名称
	 * @return the value of the attribute, or {@code null} if not set
	 * 
	 * <p> 属性的值，如果未设置则为null
	 * 
	 * @see #setUserAttribute
	 */
	Object getUserAttribute(String key);

}
