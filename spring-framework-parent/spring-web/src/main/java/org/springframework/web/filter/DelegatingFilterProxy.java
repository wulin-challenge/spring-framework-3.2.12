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

package org.springframework.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Proxy for a standard Servlet 2.3 Filter, delegating to a Spring-managed
 * bean that implements the Filter interface. Supports a "targetBeanName"
 * filter init-param in {@code web.xml}, specifying the name of the
 * target bean in the Spring application context.
 * 
 * <p> 标准Servlet 2.3 Filter的代理，委托给实现Filter接口的Spring管理的bean。
 * 支持web.xml中的“targetBeanName”过滤器init-param，在Spring应用程序上下文中指定目标bean的名称。
 *
 * <p>{@code web.xml} will usually contain a {@code DelegatingFilterProxy} definition,
 * with the specified {@code filter-name} corresponding to a bean name in
 * Spring's root application context. All calls to the filter proxy will then
 * be delegated to that bean in the Spring context, which is required to implement
 * the standard Servlet 2.3 Filter interface.
 * 
 * <p> web.xml通常包含DelegatingFilterProxy定义，其指定的filter-name对应于Spring的根应用程序上下文中的bean名称。
 * 然后，所有对过滤器代理的调用都将委托给Spring上下文中的那个bean，这是实现标准Servlet 2.3过滤器接口所必需的。
 *
 * <p>This approach is particularly useful for Filter implementation with complex
 * setup needs, allowing to apply the full Spring bean definition machinery to
 * Filter instances. Alternatively, consider standard Filter setup in combination
 * with looking up service beans from the Spring root application context.
 * 
 * <p> 这种方法对于具有复杂设置需求的Filter实现特别有用，允许将完整的Spring bean定义机制应用于Filter实例。
 * 或者，考虑标准的Filter设置以及从Spring root应用程序上下文中查找服务bean。
 *
 * <p><b>NOTE:</b> The lifecycle methods defined by the Servlet Filter interface
 * will by default <i>not</i> be delegated to the target bean, relying on the
 * Spring application context to manage the lifecycle of that bean. Specifying
 * the "targetFilterLifecycle" filter init-param as "true" will enforce invocation
 * of the {@code Filter.init} and {@code Filter.destroy} lifecycle methods
 * on the target bean, letting the servlet container manage the filter lifecycle.
 * 
 * <p> 注意：默认情况下，Servlet Filter接口定义的生命周期方法不会委托给目标bean，
 * 而是依赖Spring应用程序上下文来管理该bean的生命周期。将“targetFilterLifecycle”
 * 过滤器init-param指定为“true”将强制在目标bean上调用Filter.init和Filter.destroy生命周期方法，
 * 让servlet容器管理过滤器生命周期。
 *
 * <p>As of Spring 3.1, {@code DelegatingFilterProxy} has been updated to optionally accept
 * constructor parameters when using Servlet 3.0's instance-based filter registration
 * methods, usually in conjunction with Spring 3.1's
 * {@link org.springframework.web.WebApplicationInitializer} SPI. These constructors allow
 * for providing the delegate Filter bean directly, or providing the application context
 * and bean name to fetch, avoiding the need to look up the application context from the
 * ServletContext.
 * 
 * <p> 从Spring 3.1开始，在使用Servlet 3.0的基于实例的过滤器注册方法时，
 * DelegatingFilterProxy已经更新为可选地接受构造函数参数，通常与Spring 3.1的
 * org.springframework.web.WebApplicationInitializer SPI结合使用。这些构造函数允许直接提供委托Filter bean，
 * 或者提供应用程序上下文和bean名称来获取，从而无需从ServletContext中查找应用程序上下文。
 *
 * <p>This class was originally inspired by Spring Security's {@code FilterToBeanProxy}
 * class, written by Ben Alex.
 * 
 * <p> 这个类最初的灵感来自Spring Security的FilterToBeanProxy类，由Ben Alex编写。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Chris Beams
 * @since 1.2
 * @see #setTargetBeanName
 * @see #setTargetFilterLifecycle
 * @see javax.servlet.Filter#doFilter
 * @see javax.servlet.Filter#init
 * @see javax.servlet.Filter#destroy
 * @see #DelegatingFilterProxy(Filter)
 * @see #DelegatingFilterProxy(String)
 * @see #DelegatingFilterProxy(String, WebApplicationContext)
 * @see javax.servlet.ServletContext#addFilter(String, Filter)
 * @see org.springframework.web.WebApplicationInitializer
 */
public class DelegatingFilterProxy extends GenericFilterBean {

	private String contextAttribute;

	private WebApplicationContext webApplicationContext;

	private String targetBeanName;

	private boolean targetFilterLifecycle = false;

	private volatile Filter delegate;

	private final Object delegateMonitor = new Object();


	/**
	 * Create a new {@code DelegatingFilterProxy}. For traditional (pre-Servlet 3.0) use
	 * in {@code web.xml}.
	 * 
	 * <p> 创建一个新的DelegatingFilterProxy。 对于传统（Servlet 3.0之前的版本），请在web.xml中使用。
	 * 
	 * @see #setTargetBeanName(String)
	 */
	public DelegatingFilterProxy() {
	}

	/**
	 * Create a new {@code DelegatingFilterProxy} with the given {@link Filter} delegate.
	 * Bypasses entirely the need for interacting with a Spring application context,
	 * specifying the {@linkplain #setTargetBeanName target bean name}, etc.
	 * <p>For use in Servlet 3.0+ environments where instance-based registration of
	 * filters is supported.
	 * @param delegate the {@code Filter} instance that this proxy will delegate to and
	 * manage the lifecycle for (must not be {@code null}).
	 * @see #doFilter(ServletRequest, ServletResponse, FilterChain)
	 * @see #invokeDelegate(Filter, ServletRequest, ServletResponse, FilterChain)
	 * @see #destroy()
	 * @see #setEnvironment(org.springframework.core.env.Environment)
	 */
	public DelegatingFilterProxy(Filter delegate) {
		Assert.notNull(delegate, "delegate Filter object must not be null");
		this.delegate = delegate;
	}

	/**
	 * Create a new {@code DelegatingFilterProxy} that will retrieve the named target
	 * bean from the Spring {@code WebApplicationContext} found in the {@code ServletContext}
	 * (either the 'root' application context or the context named by
	 * {@link #setContextAttribute}).
	 * <p>For use in Servlet 3.0+ environments where instance-based registration of
	 * filters is supported.
	 * <p>The target bean must implement the standard Servlet Filter.
	 * @param targetBeanName name of the target filter bean to look up in the Spring
	 * application context (must not be {@code null}).
	 * @see #findWebApplicationContext()
	 * @see #setEnvironment(org.springframework.core.env.Environment)
	 */
	public DelegatingFilterProxy(String targetBeanName) {
		this(targetBeanName, null);
	}

	/**
	 * Create a new {@code DelegatingFilterProxy} that will retrieve the named target
	 * bean from the given Spring {@code WebApplicationContext}.
	 * <p>For use in Servlet 3.0+ environments where instance-based registration of
	 * filters is supported.
	 * <p>The target bean must implement the standard Servlet Filter interface.
	 * <p>The given {@code WebApplicationContext} may or may not be refreshed when passed
	 * in. If it has not, and if the context implements {@link ConfigurableApplicationContext},
	 * a {@link ConfigurableApplicationContext#refresh() refresh()} will be attempted before
	 * retrieving the named target bean.
	 * <p>This proxy's {@code Environment} will be inherited from the given
	 * {@code WebApplicationContext}.
	 * @param targetBeanName name of the target filter bean in the Spring application
	 * context (must not be {@code null}).
	 * @param wac the application context from which the target filter will be retrieved;
	 * if {@code null}, an application context will be looked up from {@code ServletContext}
	 * as a fallback.
	 * @see #findWebApplicationContext()
	 * @see #setEnvironment(org.springframework.core.env.Environment)
	 */
	public DelegatingFilterProxy(String targetBeanName, WebApplicationContext wac) {
		Assert.hasText(targetBeanName, "target Filter bean name must not be null or empty");
		this.setTargetBeanName(targetBeanName);
		this.webApplicationContext = wac;
		if (wac != null) {
			this.setEnvironment(wac.getEnvironment());
		}
	}

	/**
	 * Set the name of the ServletContext attribute which should be used to retrieve the
	 * {@link WebApplicationContext} from which to load the delegate {@link Filter} bean.
	 */
	public void setContextAttribute(String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}

	/**
	 * Return the name of the ServletContext attribute which should be used to retrieve the
	 * {@link WebApplicationContext} from which to load the delegate {@link Filter} bean.
	 */
	public String getContextAttribute() {
		return this.contextAttribute;
	}

	/**
	 * Set the name of the target bean in the Spring application context.
	 * The target bean must implement the standard Servlet 2.3 Filter interface.
	 * 
	 * <p> 在Spring应用程序上下文中设置目标bean的名称。 目标bean必须实现标准的Servlet 2.3 Filter接口。
	 * 
	 * <p>By default, the {@code filter-name} as specified for the
	 * DelegatingFilterProxy in {@code web.xml} will be used.
	 * 
	 * <p> 默认情况下，将使用为web.xml中的DelegatingFilterProxy指定的过滤器名称。
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/**
	 * Return the name of the target bean in the Spring application context.
	 * 
	 * <p> 在Spring应用程序上下文中返回目标bean的名称。
	 */
	protected String getTargetBeanName() {
		return this.targetBeanName;
	}

	/**
	 * Set whether to invoke the {@code Filter.init} and
	 * {@code Filter.destroy} lifecycle methods on the target bean.
	 * <p>Default is "false"; target beans usually rely on the Spring application
	 * context for managing their lifecycle. Setting this flag to "true" means
	 * that the servlet container will control the lifecycle of the target
	 * Filter, with this proxy delegating the corresponding calls.
	 */
	public void setTargetFilterLifecycle(boolean targetFilterLifecycle) {
		this.targetFilterLifecycle = targetFilterLifecycle;
	}

	/**
	 * Return whether to invoke the {@code Filter.init} and
	 * {@code Filter.destroy} lifecycle methods on the target bean.
	 */
	protected boolean isTargetFilterLifecycle() {
		return this.targetFilterLifecycle;
	}

    /**
     * initFilterBean()该方法主要完成两个功能：
     * <p> 1、找到被代理类在spring中配置的id并赋值给targetBeanName。
     * <p> 2、使用找到的id从spring容器中找到具体被代理的类，并赋值给delegate
     */
	@Override
	protected void initFilterBean() throws ServletException {
		synchronized (this.delegateMonitor) {
			if (this.delegate == null) {
				// If no target bean name specified, use filter name.
				// 如果未指定目标bean名称，请使用过滤器名称。
				if (this.targetBeanName == null) {
					// 找到要被代理的filter在spring中配置的beanName
					this.targetBeanName = getFilterName();
				}
				// Fetch Spring root application context and initialize the delegate early,
				// if possible. If the root application context will be started after this
				// filter proxy, we'll have to resort to lazy initialization.
				WebApplicationContext wac = findWebApplicationContext();
				if (wac != null) {
					//找到具体被代理的filter
					this.delegate = initDelegate(wac);
				}
			}
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Lazily initialize the delegate if necessary.
		// 必要时延迟初始化委托。
		Filter delegateToUse = this.delegate; // 拿到委托的filter
		if (delegateToUse == null) {
			synchronized (this.delegateMonitor) {
				if (this.delegate == null) {
					WebApplicationContext wac = findWebApplicationContext();
					if (wac == null) {
						throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
					}
					this.delegate = initDelegate(wac);
				}
				delegateToUse = this.delegate;
			}
		}

		// Let the delegate perform the actual doFilter operation. 
		// 让委托执行实际的doFilter操作。
		
		// 执行被代理filter的doFilter方法
		invokeDelegate(delegateToUse, request, response, filterChain);
	}

	@Override
	public void destroy() {
		Filter delegateToUse = this.delegate;
		if (delegateToUse != null) {
			destroyDelegate(delegateToUse);
		}
	}


	/**
	 * Return the {@code WebApplicationContext} passed in at construction time, if available.
	 * Otherwise, attempt to retrieve a {@code WebApplicationContext} from the
	 * {@code ServletContext} attribute with the {@linkplain #setContextAttribute
	 * configured name} if set. Otherwise look up a {@code WebApplicationContext} under
	 * the well-known "root" application context attribute. The
	 * {@code WebApplicationContext} must have already been loaded and stored in the
	 * {@code ServletContext} before this filter gets initialized (or invoked).
	 * <p>Subclasses may override this method to provide a different
	 * {@code WebApplicationContext} retrieval strategy.
	 * @return the {@code WebApplicationContext} for this proxy, or {@code null} if not found
	 * @see #DelegatingFilterProxy(String, WebApplicationContext)
	 * @see #getContextAttribute()
	 * @see WebApplicationContextUtils#getWebApplicationContext(javax.servlet.ServletContext)
	 * @see WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	protected WebApplicationContext findWebApplicationContext() {
		if (this.webApplicationContext != null) {
			// the user has injected a context at construction time -> use it
			if (this.webApplicationContext instanceof ConfigurableApplicationContext) {
				if (!((ConfigurableApplicationContext)this.webApplicationContext).isActive()) {
					// the context has not yet been refreshed -> do so before returning it
					((ConfigurableApplicationContext)this.webApplicationContext).refresh();
				}
			}
			return this.webApplicationContext;
		}
		String attrName = getContextAttribute();
		if (attrName != null) {
			return WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
		}
		else {
			return WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		}
	}

	/**
	 * Initialize the Filter delegate, defined as bean the given Spring
	 * application context.
	 * 
	 * <p> 初始化Filter委托，在给定的Spring应用程序上下文中定义为bean。
	 * 
	 * <p>The default implementation fetches the bean from the application context
	 * and calls the standard {@code Filter.init} method on it, passing
	 * in the FilterConfig of this Filter proxy.
	 * 
	 * <p> 缺省实现从应用程序上下文获取Bean，并在其上调用标准Filter.init方法，并传入此Filter代理的FilterConfig。
	 * 
	 * @param wac the root application context
	 * @return the initialized delegate Filter
	 * @throws ServletException if thrown by the Filter
	 * @see #getTargetBeanName()
	 * @see #isTargetFilterLifecycle()
	 * @see #getFilterConfig()
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
		Filter delegate = wac.getBean(getTargetBeanName(), Filter.class);
		if (isTargetFilterLifecycle()) {
			delegate.init(getFilterConfig());
		}
		return delegate;
	}

	/**
	 * Actually invoke the delegate Filter with the given request and response.
	 * @param delegate the delegate Filter
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @param filterChain the current FilterChain
	 * @throws ServletException if thrown by the Filter
	 * @throws IOException if thrown by the Filter
	 */
	protected void invokeDelegate(
			Filter delegate, ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		delegate.doFilter(request, response, filterChain);
	}

	/**
	 * Destroy the Filter delegate.
	 * Default implementation simply calls {@code Filter.destroy} on it.
	 * @param delegate the Filter delegate (never {@code null})
	 * @see #isTargetFilterLifecycle()
	 * @see javax.servlet.Filter#destroy()
	 */
	protected void destroyDelegate(Filter delegate) {
		if (isTargetFilterLifecycle()) {
			delegate.destroy();
		}
	}

}
