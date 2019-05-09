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

/**
 * Singleton to publish a shared DefaultAdvisorAdapterRegistry instance.
 *
 * <p> Singleton发布共享的DefaultAdvisorAdapterRegistry实例。
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @see DefaultAdvisorAdapterRegistry
 */
public abstract class GlobalAdvisorAdapterRegistry {

	/**
	 * Keep track of a single instance so we can return it to classes that request it.
	 * 
	 * <p> 跟踪单个实例，以便我们将其返回给请求它的类。
	 * 
	 */
	private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

	/**
	 * Return the singleton {@link DefaultAdvisorAdapterRegistry} instance.
	 * 
	 * <p> 返回单例DefaultAdvisorAdapterRegistry实例。
	 * 
	 */
	public static AdvisorAdapterRegistry getInstance() {
		return instance;
	}

	/**
	 * Reset the singleton {@link DefaultAdvisorAdapterRegistry}, removing any
	 * {@link AdvisorAdapterRegistry#registerAdvisorAdapter(AdvisorAdapter) registered}
	 * adapters.
	 * 
	 * <p> 重置单例DefaultAdvisorAdapterRegistry，删除任何已注册的适配器。
	 */
	static void reset() {
		instance = new DefaultAdvisorAdapterRegistry();
	}

}
