/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.cache.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP Alliance MethodInterceptor for declarative cache
 * management using the common Spring caching infrastructure
 * ({@link org.springframework.cache.Cache}).
 * 
 * <p> AOP Alliance MethodInterceptor用于使用常见的Spring缓存基础结构
 * （org.springframework.cache.Cache）进行声明式缓存管理。
 *
 * <p>Derives from the {@link CacheAspectSupport} class which
 * contains the integration with Spring's underlying caching API.
 * CacheInterceptor simply calls the relevant superclass methods
 * 
 * <p> 从CacheAspectSupport类派生，该类包含与Spring的基础缓存API的集成。 
 * CacheInterceptor只是以正确的顺序调用相关的超类方法。
 * 
 * in the correct order.
 *
 * <p>CacheInterceptors are thread-safe.
 * 
 * <p> CacheInterceptor是线程安全的。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
@SuppressWarnings("serial")
public class CacheInterceptor extends CacheAspectSupport implements MethodInterceptor, Serializable {

	private static class ThrowableWrapper extends RuntimeException {
		private final Throwable original;

		ThrowableWrapper(Throwable original) {
			this.original = original;
		}
	}

	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();

		Invoker aopAllianceInvoker = new Invoker() {
			public Object invoke() {
				try {
					return invocation.proceed();
				} catch (Throwable ex) {
					throw new ThrowableWrapper(ex);
				}
			}
		};

		try {
			return execute(aopAllianceInvoker, invocation.getThis(), method, invocation.getArguments());
		} catch (ThrowableWrapper th) {
			throw th.original;
		}
	}
}
