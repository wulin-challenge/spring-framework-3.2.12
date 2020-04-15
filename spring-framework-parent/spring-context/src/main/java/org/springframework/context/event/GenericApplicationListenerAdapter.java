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

package org.springframework.context.event;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * {@link SmartApplicationListener} adapter that determines supported event types
 * through introspecting the generically declared type of the target listener.
 * 
 * <p> SmartApplicationListener适配器，通过内省一般声明的目标侦听器类型来确定支持的事件类型。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.context.ApplicationListener#onApplicationEvent
 */
public class GenericApplicationListenerAdapter implements SmartApplicationListener {

	private final ApplicationListener delegate;


	/**
	 * Create a new GenericApplicationListener for the given delegate.
	 * 
	 * <p> 为给定的委托创建一个新的GenericApplicationListener。
	 * 
	 * @param delegate the delegate listener to be invoked - 要调用的委托侦听器
	 */
	public GenericApplicationListenerAdapter(ApplicationListener delegate) {
		Assert.notNull(delegate, "Delegate listener must not be null");
		this.delegate = delegate;
	}


	@SuppressWarnings("unchecked")
	public void onApplicationEvent(ApplicationEvent event) {
		this.delegate.onApplicationEvent(event);
	}

	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), ApplicationListener.class);
		if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
			Class<?> targetClass = AopUtils.getTargetClass(this.delegate);
			if (targetClass != this.delegate.getClass()) {
				typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
			}
		}
		return (typeArg == null || typeArg.isAssignableFrom(eventType));
	}

	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	public int getOrder() {
		return (this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE);
	}

}
