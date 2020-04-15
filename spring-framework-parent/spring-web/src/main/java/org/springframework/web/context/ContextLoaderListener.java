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

package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener to start up and shut down Spring's root {@link WebApplicationContext}.
 * Simply delegates to {@link ContextLoader} as well as to {@link ContextCleanupListener}.
 * 
 * <p> 引导侦听器启动和关闭Spring的根WebApplicationContext。只需委托给ContextLoader和ContextCleanupListener。
 *
 * <p>This listener should be registered after
 * {@link org.springframework.web.util.Log4jConfigListener}
 * in {@code web.xml}, if the latter is used.
 * 
 * <p> 如果使用后者,这个监听器应该在{@link org.springframework.web.util.Log4jConfigListener}之后被注册
 *
 * <p>As of Spring 3.1, {@code ContextLoaderListener} supports injecting the root web
 * application context via the {@link #ContextLoaderListener(WebApplicationContext)}
 * constructor, allowing for programmatic configuration in Servlet 3.0+ environments. See
 * {@link org.springframework.web.WebApplicationInitializer} for usage examples.
 * 
 * <p> 从Spring 3.1开始，ContextLoaderListener支持通过ContextLoaderListener（WebApplicationContext）
 * 构造函数注入根Web应用程序上下文，允许在Servlet 3.0+环境中进行编程配置。 有关用法示例，
 * 请参阅org.springframework.web.WebApplicationInitializer。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 17.02.2003
 * @see org.springframework.web.WebApplicationInitializer
 * @see org.springframework.web.util.Log4jConfigListener
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

	private ContextLoader contextLoader;


	/**
	 * Create a new {@code ContextLoaderListener} that will create a web application
	 * context based on the "contextClass" and "contextConfigLocation" servlet
	 * context-params. See {@link ContextLoader} superclass documentation for details on
	 * default values for each.
	 * 
	 * <p> 创建一个新的ContextLoaderListener，它将基于“contextClass”和“contextConfigLocation”
	 * servlet context-params创建Web应用程序上下文。 有关每个默认值的详细信息，请参阅ContextLoader超类文档。
	 * 
	 * <p>This constructor is typically used when declaring {@code ContextLoaderListener}
	 * as a {@code <listener>} within {@code web.xml}, where a no-arg constructor is
	 * required.
	 * 
	 * <p> 挡在在web.xml中声明{@code ContextLoaderListener}作为一个监听器时,这个构造器通常被使用,无参构造函数是必须的
	 * 
	 * <p>The created application context will be registered into the ServletContext under
	 * the attribute name {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE}
	 * and the Spring application context will be closed when the {@link #contextDestroyed}
	 * lifecycle method is invoked on this listener.
	 * 
	 * <p> 创建的应用程序上下文将在属性名称WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 * 下注册到ServletContext中，并且在此侦听器上调用contextDestroyed生命周期方法时，将关闭Spring应用程序上下文。
	 * 
	 * @see ContextLoader
	 * @see #ContextLoaderListener(WebApplicationContext)
	 * @see #contextInitialized(ServletContextEvent)
	 * @see #contextDestroyed(ServletContextEvent)
	 */
	public ContextLoaderListener() {
	}

	/**
	 * Create a new {@code ContextLoaderListener} with the given application context. This
	 * constructor is useful in Servlet 3.0+ environments where instance-based
	 * registration of listeners is possible through the {@link javax.servlet.ServletContext#addListener}
	 * API.
	 * 
	 * <p> 使用给定的应用程序上下文创建新的ContextLoaderListener。 此构造函数在Servlet 3.0+环境中非常有用，在这些环境中，
	 * 可以通过javax.servlet.ServletContext.addListener API对基于实例的侦听器进行注册。
	 * 
	 * <p>The context may or may not yet be {@linkplain
	 * org.springframework.context.ConfigurableApplicationContext#refresh() refreshed}. If it
	 * (a) is an implementation of {@link ConfigurableWebApplicationContext} and
	 * (b) has <strong>not</strong> already been refreshed (the recommended approach),
	 * then the following will occur:
	 * 
	 * <p> 上下文可能会或可能尚未刷新。 如果（a）是ConfigurableWebApplicationContext的实现并且（b）尚未刷新（推荐的方法），
	 * 则会发生以下情况：
	 * 
	 * <ul>
	 * <li>If the given context has not already been assigned an {@linkplain
	 * org.springframework.context.ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * 
	 * <li>如果给定的上下文尚未分配id，则将为其分配一个id</li>
	 * 
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * 
	 * <li>ServletContext和ServletConfig对象将委托给应用程序上下文</li>
	 * 
	 * <li>{@link #customizeContext} will be called</li>
	 * 
	 * <li>{@link #customizeContext} 将被调用</li>
	 * 
	 * <li>Any {@link org.springframework.context.ApplicationContextInitializer ApplicationContextInitializer}s
	 * specified through the "contextInitializerClasses" init-param will be applied.</li>
	 * 
	 * <li>将应用通过“contextInitializerClasses”init-param指定的任何ApplicationContextInitializers。</li>
	 * 
	 * <li>{@link org.springframework.context.ConfigurableApplicationContext#refresh refresh()} will be called</li>
	 * 
	 * <li>refresh()将被调用</li>
	 * 
	 * </ul>
	 * If the context has already been refreshed or does not implement
	 * {@code ConfigurableWebApplicationContext}, none of the above will occur under the
	 * assumption that the user has performed these actions (or not) per his or her
	 * specific needs.
	 * 
	 * <p> 如果上下文已经刷新或未实现ConfigurableWebApplicationContext，则假设用户已根据其特定需求执行（或不执行）这些操作，
	 * 则不会发生上述任何情况。
	 * 
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * 
	 * <p> 有关用法示例，请参阅org.springframework.web.WebApplicationInitializer。
	 * 
	 * <p>In any case, the given application context will be registered into the
	 * ServletContext under the attribute name {@link
	 * WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} and the Spring
	 * application context will be closed when the {@link #contextDestroyed} lifecycle
	 * method is invoked on this listener.
	 * 
	 * <p> 在任何情况下，给定的应用程序上下文将在属性名称
	 * WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE下注册到ServletContext中，
	 * 并且当在此侦听器上调用contextDestroyed生命周期方法时，将关闭Spring应用程序上下文。
	 * 
	 * @param context the application context to manage - 要管理的应用程序上下文
	 * @see #contextInitialized(ServletContextEvent)
	 * @see #contextDestroyed(ServletContextEvent)
	 */
	public ContextLoaderListener(WebApplicationContext context) {
		super(context);
	}

	/**
	 * Initialize the root web application context.
	 * 
	 * <p> 初始化根Web应用程序上下文。
	 * 
	 */
	public void contextInitialized(ServletContextEvent event) {
		this.contextLoader = createContextLoader();
		if (this.contextLoader == null) {
			this.contextLoader = this;
		}
		this.contextLoader.initWebApplicationContext(event.getServletContext());
	}

	/**
	 * Create the ContextLoader to use. Can be overridden in subclasses.
	 * 
	 * <p> 创建要使用的ContextLoader。 可以在子类中重写。
	 * 
	 * @return the new ContextLoader 返回一个新的 ContextLoader
	 * 
	 * @deprecated in favor of simply subclassing ContextLoaderListener itself
	 * (which extends ContextLoader, as of Spring 3.0)
	 * 
	 * <p> 已过时。 支持简单地继承ContextLoaderListener本身（从Spring 3.0开始，它扩展了ContextLoader）
	 */
	@Deprecated
	protected ContextLoader createContextLoader() {
		return null;
	}

	/**
	 * Return the ContextLoader used by this listener.
	 * 
	 * <p> 返回此侦听器使用的ContextLoader。
	 * 
	 * @return the current ContextLoader - 当前 ContextLoader
	 * 
	 * @deprecated in favor of simply subclassing ContextLoaderListener itself
	 * (which extends ContextLoader, as of Spring 3.0)
	 * 
	 * <p> 已过时。 支持简单地继承ContextLoaderListener本身（从Spring 3.0开始，它扩展了ContextLoader）
	 */
	@Deprecated
	public ContextLoader getContextLoader() {
		return this.contextLoader;
	}


	/**
	 * Close the root web application context.
	 * 
	 * <p> 关闭根Web应用程序上下文。
	 */
	public void contextDestroyed(ServletContextEvent event) {
		if (this.contextLoader != null) {
			this.contextLoader.closeWebApplicationContext(event.getServletContext());
		}
		ContextCleanupListener.cleanupAttributes(event.getServletContext());
	}

}
