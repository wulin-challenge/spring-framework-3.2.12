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

package org.springframework.web.servlet.support;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Base class for {@link org.springframework.web.WebApplicationInitializer}
 * implementations that register a {@link DispatcherServlet} in the servlet context.
 * 
 * <p> org.springframework.web.WebApplicationInitializer实现的基类，该实现在Servlet上下文中注册DispatcherServlet。
 *
 * <p>Concrete implementations are required to implement
 * {@link #createServletApplicationContext()}, as well as {@link #getServletMappings()},
 * both of which get invoked from {@link #registerDispatcherServlet(ServletContext)}.
 * Further customization can be achieved by overriding
 * {@link #customizeRegistration(ServletRegistration.Dynamic)}.
 * 
 * <p> 需要具体的实现来实现createServletApplicationContext（）和getServletMappings（），
 * 两者均从registerDispatcherServlet（ServletContext）调用。 可以通过重写
 * customRegistration（ServletRegistration.Dynamic）来实现进一步的自定义。
 *
 * <p>Because this class extends from {@link AbstractContextLoaderInitializer}, concrete
 * implementations are also required to implement {@link #createRootApplicationContext()}
 * to set up a parent "<strong>root</strong>" application context. If a root context is
 * not desired, implementations can simply return {@code null} in the
 * {@code createRootApplicationContext()} implementation.
 * 
 * <p> 由于此类从AbstractContextLoaderInitializer扩展而来，因此还需要具体的实现来实现createRootApplicationContext（）
 * 来设置父“根”应用程序上下文。 如果不需要根上下文，则实现可以在createRootApplicationContext（）实现中简单地返回null。
 *
 * @author Arjen Poutsma
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public abstract class AbstractDispatcherServletInitializer extends AbstractContextLoaderInitializer {

	/**
	 * The default servlet name. Can be customized by overriding {@link #getServletName}.
	 * 
	 * <p> 缺省的servlet名称。 可以通过重写getServletName进行自定义。
	 */
	public static final String DEFAULT_SERVLET_NAME = "dispatcher";


	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);

		registerDispatcherServlet(servletContext);
	}

	/**
	 * Register a {@link DispatcherServlet} against the given servlet context.
	 * 
	 * <p> 针对给定的servlet上下文注册一个DispatcherServlet。
	 * 
	 * <p>This method will create a {@code DispatcherServlet} with the name returned by
	 * {@link #getServletName()}, initializing it with the application context returned
	 * from {@link #createServletApplicationContext()}, and mapping it to the patterns
	 * returned from {@link #getServletMappings()}.
	 * 
	 * <p> 此方法将创建一个由getServletName（）返回的名称的DispatcherServlet，并使用从
	 * createServletApplicationContext（）返回的应用程序上下文对其进行初始化，并将其映射到从getServletMappings（）返回的模式。
	 * 
	 * <p>Further customization can be achieved by overriding {@link
	 * #customizeRegistration(ServletRegistration.Dynamic)}.
	 * 
	 * <p> 可以通过重写customRegistration（ServletRegistration.Dynamic）来实现进一步的自定义。
	 * 
	 * @param servletContext the context to register the servlet against
	 * 
	 * <p> 根据其注册servlet的上下文
	 */
	protected void registerDispatcherServlet(ServletContext servletContext) {
		String servletName = getServletName();
		Assert.hasLength(servletName, "getServletName() may not return empty or null");

		WebApplicationContext servletAppContext = createServletApplicationContext();
		Assert.notNull(servletAppContext,
				"createServletApplicationContext() did not return an application " +
				"context for servlet [" + servletName + "]");

		DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
		ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, dispatcherServlet);
		Assert.notNull(registration,
				"Failed to register servlet with name '" + servletName + "'." +
				"Check if there is another servlet registered under the same name.");

		registration.setLoadOnStartup(1);
		registration.addMapping(getServletMappings());
		registration.setAsyncSupported(isAsyncSupported());

		Filter[] filters = getServletFilters();
		if (!ObjectUtils.isEmpty(filters)) {
			for (Filter filter : filters) {
				registerServletFilter(servletContext, filter);
			}
		}

		customizeRegistration(registration);
	}

	/**
	 * Return the name under which the {@link DispatcherServlet} will be registered.
	 * Defaults to {@link #DEFAULT_SERVLET_NAME}.
	 * 
	 * <p> 返回用于注册DispatcherServlet的名称。 默认为DEFAULT_SERVLET_NAME。
	 * 
	 * @see #registerDispatcherServlet(ServletContext)
	 */
	protected String getServletName() {
		return DEFAULT_SERVLET_NAME;
	}

	/**
	 * Create a servlet application context to be provided to the {@code DispatcherServlet}.
	 * 
	 * <p> 创建要提供给DispatcherServlet的Servlet应用程序上下文。
	 * 
	 * <p>The returned context is delegated to Spring's
	 * {@link DispatcherServlet#DispatcherServlet(WebApplicationContext)}. As such,
	 * it typically contains controllers, view resolvers, locale resolvers, and other
	 * web-related beans.
	 * 
	 * <p> 返回的上下文委托给Spring的DispatcherServlet.DispatcherServlet（WebApplicationContext）。
	 *  因此，它通常包含控制器，视图解析器，语言环境解析器和其他与Web相关的bean。
	 *  
	 * @see #registerDispatcherServlet(ServletContext)
	 */
	protected abstract WebApplicationContext createServletApplicationContext();

	/**
	 * Specify the servlet mapping(s) for the {@code DispatcherServlet} &mdash;
	 * for example {@code "/"}, {@code "/app"}, etc.
	 * 
	 * <p> 指定DispatcherServlet的servlet映射，例如“ /”，“ / app”等。
	 * @see #registerDispatcherServlet(ServletContext)
	 */
	protected abstract String[] getServletMappings();

	/**
	 * Specify filters to add and map to the {@code DispatcherServlet}.
	 * 
	 * <p> 指定要添加并映射到DispatcherServlet的过滤器。
	 * 
	 * @return an array of filters or {@code null}
	 * 
	 * <p> 过滤器数组或null
	 * 
	 * @see #registerServletFilter(ServletContext, Filter)
	 */
	protected Filter[] getServletFilters() {
		return null;
	}

	/**
	 * Add the given filter to the ServletContext and map it to the
	 * {@code DispatcherServlet} as follows:
	 * 
	 * <p> 将给定的过滤器添加到ServletContext并将其映射到DispatcherServlet，如下所示：
	 * 
	 * <ul>
	 * <li>a default filter name is chosen based on its concrete type
	 * 
	 * <p> 根据具体类型选择默认过滤器名称
	 * 
	 * <li>the {@code asyncSupported} flag is set depending on the
	 * return value of {@link #isAsyncSupported() asyncSupported}
	 * 
	 * <p> 根据asyncSupported的返回值设置asyncSupported标志
	 * 
	 * <li>a filter mapping is created with dispatcher types {@code REQUEST},
	 * {@code FORWARD}, {@code INCLUDE}, and conditionally {@code ASYNC} depending
	 * on the return value of {@link #isAsyncSupported() asyncSupported}
	 * 
	 * <p> 根据asyncSupported的返回值，使用调度程序类型REQUEST，FORWARD，INCLUDE和有条件的ASYNC创建过滤器映射
	 * 
	 * </ul>
	 * <p>If the above defaults are not suitable or insufficient, override this
	 * method and register filters directly with the {@code ServletContext}.
	 * 
	 * <p> 如果以上默认值不合适或不足，请重写此方法并直接向ServletContext注册过滤器。
	 * 
	 * @param servletContext the servlet context to register filters with
	 * 
	 * <p> Servlet上下文以向其注册过滤器
	 * 
	 * @param filter the filter to be registered
	 * 
	 * <p> 要注册的过滤器
	 * 
	 * @return the filter registration
	 * 
	 * <p> 过滤器注册
	 */
	protected FilterRegistration.Dynamic registerServletFilter(ServletContext servletContext, Filter filter) {
		String filterName = Conventions.getVariableName(filter);
		Dynamic registration = servletContext.addFilter(filterName, filter);
		registration.setAsyncSupported(isAsyncSupported());
		registration.addMappingForServletNames(getDispatcherTypes(), false, getServletName());
		return registration;
	}

	private EnumSet<DispatcherType> getDispatcherTypes() {
		return isAsyncSupported() ?
			EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ASYNC) :
			EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
	}

	/**
	 * A single place to control the {@code asyncSupported} flag for the
	 * {@code DispatcherServlet} and all filters added via {@link #getServletFilters()}.
	 * 
	 * <p> 一个地方可以控制DispatcherServlet和通过getServletFilters（）添加的所有过滤器的asyncSupported标志。
	 * 
	 * <p>The default value is "true".
	 * 
	 * <p> 默认值是true”。
	 */
	protected boolean isAsyncSupported() {
		return true;
	}

	/**
	 * Optionally perform further registration customization once
	 * {@link #registerDispatcherServlet(ServletContext)} has completed.
	 * 
	 * <p> （可选）在registerDispatcherServlet（ServletContext）完成后，执行进一步的注册自定义。
	 * 
	 * @param registration the {@code DispatcherServlet} registration to be customized
	 * 
	 * <p> 要定制的DispatcherServlet注册
	 * @see #registerDispatcherServlet(ServletContext)
	 */
	protected void customizeRegistration(ServletRegistration.Dynamic registration) {
	}

}
