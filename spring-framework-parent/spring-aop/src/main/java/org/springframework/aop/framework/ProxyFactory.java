/*
 * Copyright 2002-2014 the original author or authors.
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

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.TargetSource;
import org.springframework.util.ClassUtils;

/**
 * Factory for AOP proxies for programmatic use, rather than via a bean
 * factory. This class provides a simple way of obtaining and configuring
 * AOP proxies in code.
 * 
 * <p> 用于程序使用的AOP代理工厂，而不是通过豆工厂。 此类提供了一种在代码中获取和配置AOP代理的简单方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 14.03.2003
 */
@SuppressWarnings("serial")
public class ProxyFactory extends ProxyCreatorSupport {

	/**
	 * Create a new ProxyFactory.
	 * 
	 * <p> 创建一个新的ProxyFactory。
	 */
	public ProxyFactory() {
	}

	/**
	 * Create a new ProxyFactory.
	 * 
	 * <p> 创建一个新的ProxyFactory。
	 * 
	 * <p>Will proxy all interfaces that the given target implements.
	 * 
	 * <p> 将代理给定目标实现的所有接口。
	 * 
	 * @param target the target object to be proxied
	 * 
	 * <p> 要代理的目标对象
	 */
	public ProxyFactory(Object target) {
		setTarget(target);
		setInterfaces(ClassUtils.getAllInterfaces(target));
	}

	/**
	 * Create a new ProxyFactory.
	 * 
	 * <p> 创建一个新的ProxyFactory。
	 * 
	 * <p>No target, only interfaces. Must add interceptors.
	 * 
	 * <p> 没有目标，只有接口。 必须添加拦截器。
	 * 
	 * @param proxyInterfaces the interfaces that the proxy should implement
	 * 
	 * <p> 代理应该实现的接口
	 */
	public ProxyFactory(Class<?>... proxyInterfaces) {
		setInterfaces(proxyInterfaces);
	}

	/**
	 * Create a new ProxyFactory for the given interface and interceptor.
	 * 
	 * <p> 为给定的接口和拦截器创建一个新的ProxyFactory。
	 * 
	 * <p>Convenience method for creating a proxy for a single interceptor,
	 * assuming that the interceptor handles all calls itself rather than
	 * delegating to a target, like in the case of remoting proxies.
	 * 
	 * <p> 为单个拦截器创建代理的便捷方法，假设拦截器处理所有调用本身而不是委托给目标，就像远程处理代理一样。
	 * 
	 * @param proxyInterface the interface that the proxy should implement
	 * 
	 * <p> 代理应该实现的接口
	 * 
	 * @param interceptor the interceptor that the proxy should invoke
	 * 
	 * <p> 代理应该调用的拦截器
	 */
	public ProxyFactory(Class<?> proxyInterface, Interceptor interceptor) {
		addInterface(proxyInterface);
		addAdvice(interceptor);
	}

	/**
	 * Create a ProxyFactory for the specified {@code TargetSource},
	 * making the proxy implement the specified interface.
	 * 
	 * <p> 为指定的TargetSource创建ProxyFactory，使代理实现指定的接口。
	 * 
	 * @param proxyInterface the interface that the proxy should implement
	 * 
	 * <p> 代理应该实现的接口
	 * 
	 * @param targetSource the TargetSource that the proxy should invoke
	 * 
	 * <p> 代理应该调用的TargetSource
	 * 
	 */
	public ProxyFactory(Class<?> proxyInterface, TargetSource targetSource) {
		addInterface(proxyInterface);
		setTargetSource(targetSource);
	}


	/**
	 * Create a new proxy according to the settings in this factory.
	 * 
	 * <p> 根据此工厂中的设置创建新代理。
	 * 
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * 
	 * <p. 可以反复调用。 如果我们添加或删除了界面，效果会有所不同。 可以添加和删除拦截器。
	 * 
	 * <p>Uses a default class loader: Usually, the thread context class loader
	 * (if necessary for proxy creation).
	 * 
	 * <p> 使用默认的类加载器：通常是线程上下文类加载器（如果需要创建代理）。
	 * 
	 * @return the proxy object - 代理对象
	 */
	public Object getProxy() {
		return createAopProxy().getProxy();
	}

	/**
	 * Create a new proxy according to the settings in this factory.
	 * 
	 * <p> 根据此工厂中的设置创建新代理。
	 * 
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * 
	 * <p> 可以反复调用。 如果我们添加或删除了接口，效果会有所不同。 可以添加和删除拦截器。
	 * 
	 * <p>Uses the given class loader (if necessary for proxy creation).
	 * 
	 * <p> 使用给定的类加载器（如果需要创建代理）。
	 * 
	 * @param classLoader the class loader to create the proxy with
	 * (or {@code null} for the low-level proxy facility's default)
	 * 
	 * <p> 用于创建代理的类加载器（或者对于低级代理工具的默认值为null）
	 * 
	 * @return the proxy object - 代理对象
	 */
	public Object getProxy(ClassLoader classLoader) {
		return createAopProxy().getProxy(classLoader);
	}


	/**
	 * Create a new proxy for the given interface and interceptor.
	 * 
	 * <p> 为给定的接口和拦截器创建一个新的代理。
	 * 
	 * <p>Convenience method for creating a proxy for a single interceptor,
	 * assuming that the interceptor handles all calls itself rather than
	 * delegating to a target, like in the case of remoting proxies.
	 * 
	 * <p> 为单个拦截器创建代理的便捷方法，假设拦截器处理所有调用本身而不是委托给目标，就像远程处理代理一样。
	 * 
	 * @param proxyInterface the interface that the proxy should implement
	 * 
	 * <p> 代理应该实现的接口
	 * 
	 * @param interceptor the interceptor that the proxy should invoke
	 * 
	 * <p> 代理应该调用的拦截器
	 * 
	 * @return the proxy object - 代理对象
	 * 
	 * @see #ProxyFactory(Class, org.aopalliance.intercept.Interceptor)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> proxyInterface, Interceptor interceptor) {
		return (T) new ProxyFactory(proxyInterface, interceptor).getProxy();
	}

	/**
	 * Create a proxy for the specified {@code TargetSource},
	 * implementing the specified interface.
	 * 
	 * <p> 为指定的TargetSource创建代理，实现指定的接口。
	 * 
	 * @param proxyInterface the interface that the proxy should implement
	 * 
	 * <p> 代理应该实现的接口
	 * 
	 * @param targetSource the TargetSource that the proxy should invoke
	 * 
	 * <p> 代理应该调用的TargetSource
	 * 
	 * @return the proxy object - 代理对象
	 * 
	 * @see #ProxyFactory(Class, org.springframework.aop.TargetSource)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> proxyInterface, TargetSource targetSource) {
		return (T) new ProxyFactory(proxyInterface, targetSource).getProxy();
	}

	/**
	 * Create a proxy for the specified {@code TargetSource} that extends
	 * the target class of the {@code TargetSource}.
	 * 
	 * <p> 为指定的TargetSource创建一个代理，用于扩展TargetSource的目标类。
	 * 
	 * @param targetSource the TargetSource that the proxy should invoke
	 * 
	 * <p> 代理应该调用的TargetSource
	 * 
	 * @return the proxy object - 代理对象
	 */
	public static Object getProxy(TargetSource targetSource) {
		if (targetSource.getTargetClass() == null) {
			throw new IllegalArgumentException("Cannot create class proxy for TargetSource with null target class");
		}
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(targetSource);
		proxyFactory.setProxyTargetClass(true);
		return proxyFactory.getProxy();
	}

}
