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

import java.io.Closeable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * SPI interface to be implemented by most if not all application contexts.
 * Provides facilities to configure an application context in addition
 * to the application context client methods in the
 * {@link org.springframework.context.ApplicationContext} interface.
 * 
 * <p> SPI接口由大多数（如果不是全部）应用程序上下文实现。 除了
 * org.springframework.context.ApplicationContext接口中的应用程序上下文客户端方法之外，
 * 还提供配置应用程序上下文的工具。
 *
 * <p>Configuration and lifecycle methods are encapsulated here to avoid
 * making them obvious to ApplicationContext client code. The present
 * methods should only be used by startup and shutdown code.
 * 
 * <p> 这里封装了配置和生命周期方法，以避免使它们对ApplicationContext客户端代码显而易见。 
 * 本方法只能由启动和关闭代码使用。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 03.11.2003
 */
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple context config paths in a single String value.
	 * 
	 * <p> 任何数量的这些字符都被视为单个String值中多个上下文配置路径之间的分隔符。
	 * 
	 * @see org.springframework.context.support.AbstractXmlApplicationContext#setConfigLocation
	 * @see org.springframework.web.context.ContextLoader#CONFIG_LOCATION_PARAM
	 * @see org.springframework.web.servlet.FrameworkServlet#setContextConfigLocation
	 */
	String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	/**
	 * Name of the ConversionService bean in the factory.
	 * If none is supplied, default conversion rules apply.
	 * 
	 * <p> 工厂中的ConversionService bean的名称。 如果未提供，则应用默认转换规则。
	 * 
	 * @see org.springframework.core.convert.ConversionService
	 */
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	/**
	 * Name of the LoadTimeWeaver bean in the factory. If such a bean is supplied,
	 * the context will use a temporary ClassLoader for type matching, in order
	 * to allow the LoadTimeWeaver to process all actual bean classes.
	 * 
	 * <p> 工厂中LoadTimeWeaver bean的名称。 如果提供了这样的bean，则上下文将使用临时ClassLoader进行类型匹配，以便允许LoadTimeWeaver处理所有实际的bean类。
	 * 
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver
	 */
	String LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver";

	/**
	 * Name of the {@link Environment} bean in the factory.
	 * 
	 * <p> 工厂中环境bean的名称。
	 * 
	 */
	String ENVIRONMENT_BEAN_NAME = "environment";

	/**
	 * Name of the System properties bean in the factory.
	 * 
	 * <p> 工厂中System属性bean的名称。
	 * 
	 * @see java.lang.System#getProperties()
	 */
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";

	/**
	 * Name of the System environment bean in the factory.
	 * 
	 * <p> 工厂中的系统环境bean的名称。
	 * 
	 * @see java.lang.System#getenv()
	 */
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";


	/**
	 * Set the unique id of this application context.
	 * 
	 * <p> 设置此应用程序上下文的唯一ID。
	 */
	void setId(String id);

	/**
	 * Set the parent of this application context.
	 * 
	 * <p> 设置此应用程序上下文的父级。
	 * 
	 * <p>Note that the parent shouldn't be changed: It should only be set outside
	 * a constructor if it isn't available when an object of this class is created,
	 * for example in case of WebApplicationContext setup.
	 * 
	 * <p> 请注意，不应更改父级：如果在创建此类的对象时不可用，则应仅在构造函数外部设置它，例如，在WebApplicationContext设置的情况下。
	 * 
	 * @param parent the parent context - 父亲上下文
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext
	 */
	void setParent(ApplicationContext parent);

	/**
	 * Return the Environment for this application context in configurable form.
	 * 
	 * <p> 以可配置的形式返回此应用程序上下文的环境。
	 * 
	 */
	ConfigurableEnvironment getEnvironment();

	/**
	 * Set the {@code Environment} for this application context.
	 * 
	 * <p> 为此应用程序上下文设置环境。
	 * @param environment the new environment - 新环境
	 */
	void setEnvironment(ConfigurableEnvironment environment);

	/**
	 * Add a new BeanFactoryPostProcessor that will get applied to the internal
	 * bean factory of this application context on refresh, before any of the
	 * bean definitions get evaluated. To be invoked during context configuration.
	 * 
	 * <p> 添加一个新的BeanFactoryPostProcessor，在评估任何bean定义之前，它将在刷新时应用于此应用程序上下文的内部bean工厂。 在上下文配置期间调用。
	 * 
	 * @param beanFactoryPostProcessor the factory processor to register
	 * 
	 * <p> 工厂处理器注册
	 * 
	 */
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);

	/**
	 * Add a new ApplicationListener that will be notified on context events
	 * such as context refresh and context shutdown.
	 * 
	 * <p> 添加一个新的ApplicationListener，它将在上下文事件（例如上下文刷新和上下文关闭）上得到通知。
	 * 
	 * <p>Note that any ApplicationListener registered here will be applied
	 * on refresh if the context is not active yet, or on the fly with the
	 * current event multicaster in case of a context that is already active.
	 * 
	 * <p> 请注意，如果上下文尚未处于活动状态，则此处注册的任何ApplicationListener将在刷新时应用，
	 * 或者在已经处于活动状态的上下文中，将在当前事件多播时动态应用。
	 * 
	 * @param listener the ApplicationListener to register
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * Load or refresh the persistent representation of the configuration,
	 * which might an XML file, properties file, or relational database schema.
	 * 
	 * <p> 加载或刷新配置的持久表示，可能是XML文件，属性文件或关系数据库模式。
	 * 
	 * <p>As this is a startup method, it should destroy already created singletons
	 * if it fails, to avoid dangling resources. In other words, after invocation
	 * of that method, either all or no singletons at all should be instantiated.
	 * 
	 * <p> 由于这是一个启动方法，它应该销毁已创建的单例，如果它失败，以避免悬空资源。 
	 * 换句话说，在调用该方法之后，应该实例化所有单个或不单个。
	 * 
	 * @throws BeansException if the bean factory could not be initialized
	 * 
	 * <p> 如果bean工厂无法初始化
	 * 
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 * 
	 * <p> 如果已初始化并且不支持多次刷新尝试
	 * 
	 */
	void refresh() throws BeansException, IllegalStateException;

	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * 
	 * <p> 向JVM运行时注册关闭挂钩，在JVM关闭时关闭此上下文，除非此时已关闭。
	 * 
	 * <p>This method can be called multiple times. Only one shutdown hook
	 * (at max) will be registered for each context instance.
	 * 
	 * <p> 可以多次调用此方法。 每个上下文实例只会注册一个关闭挂钩（最大值）。
	 * 
	 * @see java.lang.Runtime#addShutdownHook
	 * @see #close()
	 */
	void registerShutdownHook();

	/**
	 * Close this application context, releasing all resources and locks that the
	 * implementation might hold. This includes destroying all cached singleton beans.
	 * 
	 * <p> 关闭此应用程序上下文，释放实现可能包含的所有资源和锁。 这包括销毁所有缓存的单例bean。
	 * 
	 * <p>Note: Does <i>not</i> invoke {@code close} on a parent context;
	 * parent contexts have their own, independent lifecycle.
	 * 
	 * <p> 注意：不在父上下文上调用close; 父语境有自己独立的生命周期。
	 * 
	 * <p>This method can be called multiple times without side effects: Subsequent
	 * {@code close} calls on an already closed context will be ignored.
	 * 
	 * <p> 可以多次调用此方法而不会产生副作用：将忽略对已经关闭的上下文的后续关闭调用。
	 * 
	 */
	void close();

	/**
	 * Determine whether this application context is active, that is,
	 * whether it has been refreshed at least once and has not been closed yet.
	 * 
	 * <p> 确定此应用程序上下文是否处于活动状态，即是否已至少刷新一次并且尚未关闭。
	 * 
	 * @return whether the context is still active
	 * 
	 * <p> 上下文是否仍然有效
	 * 
	 * @see #refresh()
	 * @see #close()
	 * @see #getBeanFactory()
	 */
	boolean isActive();

	/**
	 * Return the internal bean factory of this application context.
	 * Can be used to access specific functionality of the underlying factory.
	 * 
	 * <p> 返回此应用程序上下文的内部bean工厂。 可用于访问基础工厂的特定功能。
	 * 
	 * <p>Note: Do not use this to post-process the bean factory; singletons
	 * will already have been instantiated before. Use a BeanFactoryPostProcessor
	 * to intercept the BeanFactory setup process before beans get touched.
	 * 
	 * <p> 注意：不要使用它来后处理bean工厂; 单身人士之前已经被实例化了。 在触摸bean之前，
	 * 使用BeanFactoryPostProcessor拦截BeanFactory设置过程。
	 * 
	 * <p>Generally, this internal factory will only be accessible while the context
	 * is active, that is, inbetween {@link #refresh()} and {@link #close()}.
	 * The {@link #isActive()} flag can be used to check whether the context
	 * is in an appropriate state.
	 * 
	 * <p> 通常，只有在上下文处于活动状态时，即在refresh（）和close（）之间，才能访问此内部工厂。 
	 * isActive（）标志可用于检查上下文是否处于适当的状态。
	 * 
	 * @return the underlying bean factory - 底层bean工厂
	 * @throws IllegalStateException if the context does not hold an internal
	 * bean factory (usually if {@link #refresh()} hasn't been called yet or
	 * if {@link #close()} has already been called)
	 * 
	 * <p> 如果上下文不包含内部bean工厂（通常如果还没有调用refresh（）或者已经调用了close（））
	 * 
	 * @see #isActive()
	 * @see #refresh()
	 * @see #close()
	 * @see #addBeanFactoryPostProcessor
	 */
	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

}
