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

import java.lang.reflect.Method;

/**
 * Advice invoked before a method is invoked. Such advices cannot
 * prevent the method call proceeding, unless they throw a Throwable.
 * 
 * <p> 在调用方法之前调用的Advice。 这样的建议不能阻止方法调用进行，除非他们抛出Throwable。
 *
 * @see AfterReturningAdvice
 * @see ThrowsAdvice
 *
 * @author Rod Johnson
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

	/**
	 * Callback before a given method is invoked.
	 * 
	 * <p> 调用给定方法之前的回调。
	 * 
	 * @param method method being invoked
	 * 
	 * <p> 被调用的方法
	 * 
	 * @param args arguments to the method
	 * 
	 * <p> 方法的参数
	 * 
	 * @param target target of the method invocation. May be {@code null}.
	 * 
	 * <p> 方法调用的目标。 可能为空。
	 * 
	 * @throws Throwable if this object wishes to abort the call.
	 * Any exception thrown will be returned to the caller if it's
	 * allowed by the method signature. Otherwise the exception
	 * will be wrapped as a runtime exception.
	 * 
	 * <p> 如果此对象希望中止呼叫。 如果方法签名允许，则抛出的任何异常都将返回给调用者。 否则，异常将被包装为运行时异常。
	 */
	void before(Method method, Object[] args, Object target) throws Throwable;

}
