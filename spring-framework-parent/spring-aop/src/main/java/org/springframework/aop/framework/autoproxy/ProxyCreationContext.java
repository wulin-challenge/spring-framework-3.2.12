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

package org.springframework.aop.framework.autoproxy;

import org.springframework.core.NamedThreadLocal;

/**
 * Holder for the current proxy creation context, as exposed by auto-proxy creators
 * such as {@link AbstractAdvisorAutoProxyCreator}.
 * 
 * <p> 当前代理创建上下文的持有者，由自动代理创建者（如AbstractAdvisorAutoProxyCreator）公开。
 *
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.5
 */
public class ProxyCreationContext {

	/** ThreadLocal holding the current proxied bean name during Advisor matching */
	/** 在Advisor匹配期间持有当前代理bean名称的ThreadLocal */
	private static final ThreadLocal<String> currentProxiedBeanName =
			new NamedThreadLocal<String>("Name of currently proxied bean");


	/**
	 * Return the name of the currently proxied bean instance.
	 * 
	 * <p> 返回当前代理的bean实例的名称。
	 * 
	 * @return the name of the bean, or {@code null} if none available
	 * 
	 * <p> bean的名称，如果没有，则返回null
	 */
	public static String getCurrentProxiedBeanName() {
		return currentProxiedBeanName.get();
	}

	/**
	 * Set the name of the currently proxied bean instance.
	 * 
	 * <p> 设置当前代理的bean实例的名称。
	 * 
	 * @param beanName the name of the bean, or {@code null} to reset it
	 * 
	 * <p> bean的名称，或null以重置它
	 * 
	 */
	static void setCurrentProxiedBeanName(String beanName) {
		if (beanName != null) {
			currentProxiedBeanName.set(beanName);
		}
		else {
			currentProxiedBeanName.remove();
		}
	}

}
