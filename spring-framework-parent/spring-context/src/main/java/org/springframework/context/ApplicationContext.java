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

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 * 
 * <p> 用于为应用程序提供配置的中央接口。这在应用程序运行时是只读的，但如果实现支持，则可以重新加载。
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 * 
 * <p> ApplicationContext提供：
 * <ul>
 *   <li>用于访问应用程序组件的Bean工厂方法。继承自org.springframework.beans.factory.ListableBeanFactory。
 *   <li>以通用方式加载文件资源的能力。继承自org.springframework.core.io.ResourceLoader接口。
 *   <li>向已注册的侦听器发布事件的能力。继承自ApplicationEventPublisher接口。
 *   <li>解决消息，支持国际化的能力。继承自MessageSource接口。
 *   <li>从父上下文继承。后代上下文中的定义始终优先。这意味着，例如，整个Web应用程序可以使用单个父上下文，而每个servlet都有自己的子上下文，该上下文独立于任何其他servlet的子上下文。
 * </ul>
 *
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 * 
 * <p> 除了标准的org.springframework.beans.factory.BeanFactory生命周期功能之外，
 * ApplicationContext实现还检测并调用ApplicationContextAware bean以及ResourceLoaderAware，
 * ApplicationEventPublisherAware和MessageSourceAware bean。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the unique id of this application context.
	 * 
	 * <p> 返回此应用程序上下文的唯一ID。
	 * 
	 * @return the unique id of the context, or {@code null} if none
	 * 
	 * <p> 上下文的唯一ID，如果没有则为null
	 * 
	 */
	String getId();

	/**
	 * Return a name for the deployed application that this context belongs to.
	 * 
	 * <p> 返回此上下文所属的已部署应用程序的名称。
	 * 
	 * @return a name for the deployed application, or the empty String by default
	 * 
	 * <p> 已部署应用程序的名称，或默认情况下为空String
	 * 
	 */
	String getApplicationName();

	/**
	 * Return a friendly name for this context.
	 * 
	 * <p> 返回此上下文的友好名称。
	 * 
	 * @return a display name for this context (never {@code null})
	 * 
	 * <p> 此上下文的显示名称（永不为null）
	 * 
	 */
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * 
	 * <p> 首次加载此上下文时返回时间戳。
	 * 
	 * @return the timestamp (ms) when this context was first loaded
	 * 
	 * <p> 首次加载此上下文时的时间戳（ms）
	 * 
	 */
	long getStartupDate();

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * 
	 * <p> 返回父上下文，如果没有父上下文，则返回null，这是上下文层次结构的根。
	 * 
	 * @return the parent context, or {@code null} if there is no parent
	 * 
	 * <p> 父上下文，如果没有父上下文，则为null
	 * 
	 */
	ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * 
	 * <p> 为此上下文公开AutowireCapableBeanFactory功能。
	 * 
	 * <p>This is not typically used by application code, except for the purpose
	 * of initializing bean instances that live outside the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * 
	 * <p> 除了初始化位于应用程序上下文之外的bean实例，将Spring bean生命周期（全部或部分）应用于它们之外，
	 * 应用程序代码通常不会使用它。
	 * 
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * AutowireCapableBeanFactory interface too. The present method mainly
	 * serves as convenient, specific facility on the ApplicationContext
	 * interface itself.
	 * 
	 * <p> 或者，ConfigurableApplicationContext接口公开的内部BeanFactory也可以访
	 * 问AutowireCapableBeanFactory接口。 本方法主要用作ApplicationContext接口本身的方便，特定的工具。
	 * 
	 * @return the AutowireCapableBeanFactory for this context - 此上下文的AutowireCapableBeanFactory
	 * @throws IllegalStateException if the context does not support
	 * the AutowireCapableBeanFactory interface or does not hold an autowire-capable
	 * bean factory yet (usually if {@code refresh()} has never been called)
	 * 
	 * <p> 如果上下文不支持AutowireCapableBeanFactory接口或者还没有支持支持autowire的bean工厂（通常从未调用过refresh（））
	 * 
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
