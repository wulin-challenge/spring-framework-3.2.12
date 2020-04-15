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

package org.springframework.web.servlet.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ContextExposingHttpServletRequest;
import org.springframework.web.util.WebUtils;

/**
 * Wrapper for a JSP or other resource within the same web application.
 * Exposes model objects as request attributes and forwards the request to
 * the specified resource URL using a {@link javax.servlet.RequestDispatcher}.
 * 
 * <p> 适用于同一Web应用程序中的JSP或其他资源的包装器。 将模型对象公开为请求属性，并使用
 * javax.servlet.RequestDispatcher将请求转发到指定的资源URL。
 *
 * <p>A URL for this view is supposed to specify a resource within the web
 * application, suitable for RequestDispatcher's {@code forward} or
 * {@code include} method.
 * 
 * <p> 此视图的URL应该指定Web应用程序中的资源，适用于RequestDispatcher的forward或include方法。
 *
 * <p>If operating within an already included request or within a response that
 * has already been committed, this view will fall back to an include instead of
 * a forward. This can be enforced by calling {@code response.flushBuffer()}
 * (which will commit the response) before rendering the view.
 * 
 * <p> 如果在已包含的请求中或在已提交的响应内操作，则此视图将回退到包含而不是转发。 
 * 这可以通过在呈现视图之前调用response.flushBuffer（）（将提交响应）来强制执行。
 *
 * <p>Typical usage with {@link InternalResourceViewResolver} looks as follows,
 * from the perspective of the DispatcherServlet context definition:
 * 
 * <p> 从DispatcherServlet上下文定义的角度来看，InternalResourceViewResolver的典型用法如下所示：
 *
 * <pre class="code">&lt;bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver"&gt;
 *   &lt;property name="prefix" value="/WEB-INF/jsp/"/&gt;
 *   &lt;property name="suffix" value=".jsp"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * Every view name returned from a handler will be translated to a JSP
 * resource (for example: "myView" -> "/WEB-INF/jsp/myView.jsp"), using
 * this view class by default.
 * 
 * <p> 从处理程序返回的每个视图名称都将被转换为JSP资源（例如：“myView” - >“/ WWE-INF / jsp / myView.jsp”），
 * 默认情况下使用此视图类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see javax.servlet.RequestDispatcher#forward
 * @see javax.servlet.RequestDispatcher#include
 * @see javax.servlet.ServletResponse#flushBuffer
 * @see InternalResourceViewResolver
 * @see JstlView
 */
public class InternalResourceView extends AbstractUrlBasedView {

	private boolean alwaysInclude = false;

	private volatile Boolean exposeForwardAttributes;

	private boolean exposeContextBeansAsAttributes = false;

	private Set<String> exposedContextBeanNames;

	private boolean preventDispatchLoop = false;


	/**
	 * Constructor for use as a bean.
	 * @see #setUrl
	 * @see #setAlwaysInclude
	 */
	public InternalResourceView() {
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * 
	 * <p> 使用给定的URL创建一个新的InternalResourceView。
	 * 
	 * @param url the URL to forward to - 转发到的URL
	 * @see #setAlwaysInclude
	 */
	public InternalResourceView(String url) {
		super(url);
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * @param url the URL to forward to
	 * @param alwaysInclude whether to always include the view rather than forward to it
	 */
	public InternalResourceView(String url, boolean alwaysInclude) {
		super(url);
		this.alwaysInclude = alwaysInclude;
	}


	/**
	 * Specify whether to always include the view rather than forward to it.
	 * <p>Default is "false". Switch this flag on to enforce the use of a
	 * Servlet include, even if a forward would be possible.
	 * @see javax.servlet.RequestDispatcher#forward
	 * @see javax.servlet.RequestDispatcher#include
	 * @see #useInclude(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	/**
	 * Set whether to explictly expose the Servlet 2.4 forward request attributes
	 * when forwarding to the underlying view resource.
	 * <p>Default is "true" on Servlet containers up until 2.4, and "false" for
	 * Servlet 2.5 and above. Note that Servlet containers at 2.4 level and above
	 * should expose those attributes automatically! This InternalResourceView
	 * feature exists for Servlet 2.3 containers and misbehaving 2.4 containers.
	 */
	public void setExposeForwardAttributes(boolean exposeForwardAttributes) {
		this.exposeForwardAttributes = exposeForwardAttributes;
	}

	/**
	 * Set whether to make all Spring beans in the application context accessible
	 * as request attributes, through lazy checking once an attribute gets accessed.
	 * <p>This will make all such beans accessible in plain {@code ${...}}
	 * expressions in a JSP 2.0 page, as well as in JSTL's {@code c:out}
	 * value expressions.
	 * <p>Default is "false". Switch this flag on to transparently expose all
	 * Spring beans in the request attribute namespace.
	 * <p><b>NOTE:</b> Context beans will override any custom request or session
	 * attributes of the same name that have been manually added. However, model
	 * attributes (as explicitly exposed to this view) of the same name will
	 * always override context beans.
	 * @see #getRequestToExpose
	 */
	public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) {
		this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
	}

	/**
	 * Specify the names of beans in the context which are supposed to be exposed.
	 * If this is non-null, only the specified beans are eligible for exposure as
	 * attributes.
	 * 
	 * <p> 在上下文中指定应该公开的bean的名称。 如果这是非null，则只有指定的bean才有资格作为属性公开。
	 * 
	 * <p>If you'd like to expose all Spring beans in the application context, switch
	 * the {@link #setExposeContextBeansAsAttributes "exposeContextBeansAsAttributes"}
	 * flag on but do not list specific bean names for this property.
	 * 
	 * <p> 如果您想在应用程序上下文中公开所有Spring bean，请切换“exposeContextBeansAsAttributes”标志，
	 * 但不要列出此属性的特定bean名称。
	 */
	public void setExposedContextBeanNames(String... exposedContextBeanNames) {
		this.exposedContextBeanNames = new HashSet<String>(Arrays.asList(exposedContextBeanNames));
	}

	/**
	 * Set whether to explicitly prevent dispatching back to the
	 * current handler path.
	 * 
	 * <p> 设置是否显式阻止调度回当前处理程序路径。
	 * 
	 * <p>Default is "false". Switch this to "true" for convention-based
	 * views where a dispatch back to the current handler path is a
	 * definitive error.
	 * 
	 * <p> 默认为“false”。 对于基于约定的视图，将其切换为“true”，其中发送回当前处理程序路径是确定性错误。
	 */
	public void setPreventDispatchLoop(boolean preventDispatchLoop) {
		this.preventDispatchLoop = preventDispatchLoop;
	}

	/**
	 * An ApplicationContext is not strictly required for InternalResourceView.
	 * 
	 * <p> InternalResourceView并不严格要求ApplicationContext。
	 */
	@Override
	protected boolean isContextRequired() {
		return false;
	}

	/**
	 * Checks whether we need to explicitly expose the Servlet 2.4 request attributes
	 * by default.
	 * 
	 * <p> 检查我们是否需要默认显式公开Servlet 2.4请求属性。
	 * 
	 * @see #setExposeForwardAttributes
	 * @see #exposeForwardRequestAttributes(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected void initServletContext(ServletContext sc) {
		if (this.exposeForwardAttributes == null && sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
			this.exposeForwardAttributes = Boolean.TRUE;
		}
	}


	/**
	 * Render the internal resource given the specified model.
	 * This includes setting the model as request attributes.
	 * 
	 * <p> 给定指定模型的内部资源。 这包括将模型设置为请求属性。
	 */
	@Override
	protected void renderMergedOutputModel(
			Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// Determine which request handle to expose to the RequestDispatcher.
		// 确定要向RequestDispatcher公开的请求句柄。
		HttpServletRequest requestToExpose = getRequestToExpose(request);

		// Expose the model object as request attributes.
		// 将模型对象公开为请求属性。
		exposeModelAsRequestAttributes(model, requestToExpose);

		// Expose helpers as request attributes, if any.
		// 将助手公开为请求属性（如果有）。
		exposeHelpers(requestToExpose);

		// Determine the path for the request dispatcher.
		// 确定请求调度程序的路径。
		String dispatcherPath = prepareForRendering(requestToExpose, response);

		// Obtain a RequestDispatcher for the target resource (typically a JSP).
		// 获取目标资源（通常是JSP）的RequestDispatcher。
		RequestDispatcher rd = getRequestDispatcher(requestToExpose, dispatcherPath);
		if (rd == null) {
			throw new ServletException("Could not get RequestDispatcher for [" + getUrl() +
					"]: Check that the corresponding file exists within your web application archive!");
		}

		// If already included or response already committed, perform include, else forward.
		// 如果已经包含或响应已经提交，请执行include，else forward。
		if (useInclude(requestToExpose, response)) {
			response.setContentType(getContentType());
			if (logger.isDebugEnabled()) {
				logger.debug("Including resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
			}
			rd.include(requestToExpose, response);
		}

		else {
			// Note: The forwarded resource is supposed to determine the content type itself.
			// 注意：转发的资源应该确定内容类型本身。
			exposeForwardRequestAttributes(requestToExpose);
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarding to resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
			}
			rd.forward(requestToExpose, response);
		}
	}

	/**
	 * Get the request handle to expose to the RequestDispatcher, i.e. to the view.
	 * 
	 * <p> 获取请求句柄以向RequestDispatcher公开，即向视图公开。
	 * 
	 * <p>The default implementation wraps the original request for exposure of
	 * Spring beans as request attributes (if demanded).
	 * 
	 * <p> 默认实现将原始bean的暴露请求包装为请求属性（如果需要）。
	 * 
	 * @param originalRequest the original servlet request as provided by the engine
	 * 
	 * <p> 引擎提供的原始servlet请求
	 * 
	 * @return the wrapped request, or the original request if no wrapping is necessary
	 * 
	 * <p> 包装的请求，或原始请求，如果不需要包装
	 * 
	 * @see #setExposeContextBeansAsAttributes
	 * @see org.springframework.web.context.support.ContextExposingHttpServletRequest
	 */
	protected HttpServletRequest getRequestToExpose(HttpServletRequest originalRequest) {
		if (this.exposeContextBeansAsAttributes || this.exposedContextBeanNames != null) {
			return new ContextExposingHttpServletRequest(
					originalRequest, getWebApplicationContext(), this.exposedContextBeanNames);
		}
		return originalRequest;
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's contexts etc.
	 * 
	 * <p> 公开每个渲染操作独有的助手。 这是必要的，以便不同的渲染操作不会覆盖彼此的上下文等。
	 * 
	 * <p>Called by {@link #renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)}.
	 * The default implementation is empty. This method can be overridden to add
	 * custom helpers as request attributes.
	 * 
	 * <p> 由renderMergedOutputModel（Map，HttpServletRequest，HttpServletResponse）调用。
	 * 默认实现为空。 可以重写此方法以将自定义帮助程序添加为请求属性。
	 * 
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding attributes
	 * 
	 * <p> 如果我们在添加属性时出现致命错误
	 * 
	 * @see #renderMergedOutputModel
	 * @see JstlView#exposeHelpers
	 */
	protected void exposeHelpers(HttpServletRequest request) throws Exception {
	}

	/**
	 * Prepare for rendering, and determine the request dispatcher path
	 * to forward to (or to include).
	 * 
	 * <p> 准备渲染，并确定转发到（或包括）的请求调度程序路径。
	 * 
	 * <p>This implementation simply returns the configured URL.
	 * Subclasses can override this to determine a resource to render,
	 * typically interpreting the URL in a different manner.
	 * 
	 * <p> 此实现只返回配置的URL。 子类可以覆盖它以确定要呈现的资源，通常以不同的方式解释URL。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return the request dispatcher path to use
	 * 
	 * <p> 请求调度程序使用的路径
	 * 
	 * @throws Exception if preparations failed - 如果准备失败了
	 * @see #getUrl()
	 */
	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String path = getUrl();
		if (this.preventDispatchLoop) {
			String uri = request.getRequestURI();
			if (path.startsWith("/") ? uri.equals(path) : uri.equals(StringUtils.applyRelativePath(uri, path))) {
				throw new ServletException("Circular view path [" + path + "]: would dispatch back " +
						"to the current handler URL [" + uri + "] again. Check your ViewResolver setup! " +
						"(Hint: This may be the result of an unspecified view, due to default view name generation.)");
			}
		}
		return path;
	}

	/**
	 * Obtain the RequestDispatcher to use for the forward/include.
	 * 
	 * <p> 获取RequestDispatcher以用于forward / include。
	 * 
	 * <p>The default implementation simply calls
	 * {@link HttpServletRequest#getRequestDispatcher(String)}.
	 * Can be overridden in subclasses.
	 * 
	 * <p> 默认实现只调用HttpServletRequest.getRequestDispatcher（String）。 可以在子类中重写。
	 * 
	 * @param request current HTTP request
	 * @param path the target URL (as returned from {@link #prepareForRendering})
	 * 
	 * <p> 目标URL（从prepareForRendering返回）
	 * 
	 * @return a corresponding RequestDispatcher
	 */
	protected RequestDispatcher getRequestDispatcher(HttpServletRequest request, String path) {
		return request.getRequestDispatcher(path);
	}

	/**
	 * Determine whether to use RequestDispatcher's {@code include} or
	 * {@code forward} method.
	 * 
	 * <p> 确定是否使用RequestDispatcher的include或forward方法。
	 * 
	 * <p>Performs a check whether an include URI attribute is found in the request,
	 * indicating an include request, and whether the response has already been committed.
	 * In both cases, an include will be performed, as a forward is not possible anymore.
	 * 
	 * <p> 执行检查是否在请求中找到包含URI属性，指示包含请求以及响应是否已提交。 在这两种情况下，
	 * 都将执行包含，因为不再可能进行转发。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return {@code true} for include, {@code false} for forward
	 * 
	 * <p> 对于包含为true，对于向前为false
	 * 
	 * @see javax.servlet.RequestDispatcher#forward
	 * @see javax.servlet.RequestDispatcher#include
	 * @see javax.servlet.ServletResponse#isCommitted
	 * @see org.springframework.web.util.WebUtils#isIncludeRequest
	 */
	protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
		return (this.alwaysInclude || WebUtils.isIncludeRequest(request) || response.isCommitted());
	}

	/**
	 * Expose the current request URI and paths as {@link HttpServletRequest}
	 * attributes under the keys defined in the Servlet 2.4 specification,
	 * for Servlet 2.3 containers as well as misbehaving Servlet 2.4 containers
	 * (such as OC4J).
	 * 
	 * <p> 将当前请求URI和路径公开为Servlet 2.4规范中定义的键下的HttpServletRequest属性，Servlet 2.3
	 * 容器以及行为不当的Servlet 2.4容器（例如OC4J）。
	 * 
	 * <p>Does not expose the attributes on Servlet 2.5 or above, mainly for
	 * GlassFish compatibility (GlassFish gets confused by pre-exposed attributes).
	 * In any case, Servlet 2.5 containers should finally properly support
	 * Servlet 2.4 features, shouldn't they...
	 * 
	 * <p> 不公开Servlet 2.5或更高版本的属性，主要用于GlassFish兼容性（GlassFish被预先公开的属性搞糊涂）。 
	 * 无论如何，Servlet 2.5容器应该最终正确支持Servlet 2.4的功能，不应该......
	 * 
	 * @param request current HTTP request
	 * @see org.springframework.web.util.WebUtils#exposeForwardRequestAttributes
	 */
	protected void exposeForwardRequestAttributes(HttpServletRequest request) {
		if (this.exposeForwardAttributes != null && this.exposeForwardAttributes) {
			try {
				WebUtils.exposeForwardRequestAttributes(request);
			}
			catch (Exception ex) {
				// Servlet container rejected to set internal attributes, e.g. on TriFork.
				// 拒绝Servlet容器以设置内部属性，例如 在TriFork。
				this.exposeForwardAttributes = Boolean.FALSE;
			}
		}
	}

}
