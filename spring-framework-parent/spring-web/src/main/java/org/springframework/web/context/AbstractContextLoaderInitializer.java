/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.WebApplicationInitializer;

/**
 * Convenient base class for {@link WebApplicationInitializer} implementations
 * that register a {@link ContextLoaderListener} in the servlet context.
 * 
 * <p> WebApplicationInitializer实现的便捷基类，该实现在Servlet上下文中注册了ContextLoaderListener。
 *
 * <p>The only method required to be implemented by subclasses is
 * {@link #createRootApplicationContext()}, which gets invoked from
 * {@link #registerContextLoaderListener(ServletContext)}.
 * 
 * <p> 子类唯一需要实现的方法是createRootApplicationContext（），该方法从registerContextLoaderListener（ServletContext）调用。
 *
 * @author Arjen Poutsma
 * @author Chris Beams
 * @since 3.2
 */
public abstract class AbstractContextLoaderInitializer implements WebApplicationInitializer {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	public void onStartup(ServletContext servletContext) throws ServletException {
		registerContextLoaderListener(servletContext);
	}

	/**
	 * Register a {@link ContextLoaderListener} against the given servlet context. The
	 * {@code ContextLoaderListener} is initialized with the application context returned
	 * from the {@link #createRootApplicationContext()} template method.
	 * 
	 * <p> 针对给定的servlet上下文注册ContextLoaderListener。 使用从createRootApplicationContext（）
	 * 模板方法返回的应用程序上下文初始化ContextLoaderListener。
	 * 
	 * @param servletContext the servlet context to register the listener against
	 * 
	 * <p> Servlet上下文以注册侦听器
	 */
	protected void registerContextLoaderListener(ServletContext servletContext) {
		WebApplicationContext rootAppContext = createRootApplicationContext();
		if (rootAppContext != null) {
			servletContext.addListener(new ContextLoaderListener(rootAppContext));
		}
		else {
			logger.debug("No ContextLoaderListener registered, as " +
					"createRootApplicationContext() did not return an application context");
		}
	}

	/**
	 * Create the "<strong>root</strong>" application context to be provided to the
	 * {@code ContextLoaderListener}.
	 * 
	 * <p> 创建要提供给ContextLoaderListener的“根”应用程序上下文。
	 * 
	 * <p>The returned context is delegated to
	 * {@link ContextLoaderListener#ContextLoaderListener(WebApplicationContext)} and will
	 * be established as the parent context for any {@code DispatcherServlet} application
	 * contexts. As such, it typically contains middle-tier services, data sources, etc.
	 * 
	 * <p> 返回的上下文委派给ContextLoaderListener.ContextLoaderListener（WebApplicationContext），
	 * 并将其建立为任何DispatcherServlet应用程序上下文的父上下文。 因此，它通常包含中间层服务，数据源等。
	 * 
	 * @return the root application context, or {@code null} if a root context is not
	 * desired
	 * 
	 * <p> 根应用程序上下文；如果不需要根上下文，则为null
	 * 
	 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer
	 */
	protected abstract WebApplicationContext createRootApplicationContext();

}
