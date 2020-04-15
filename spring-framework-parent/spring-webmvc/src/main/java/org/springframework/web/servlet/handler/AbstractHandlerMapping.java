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

package org.springframework.web.servlet.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * Abstract base class for {@link org.springframework.web.servlet.HandlerMapping}
 * implementations. Supports ordering, a default handler, handler interceptors,
 * including handler interceptors mapped by path patterns.
 * 
 * <p> org.springframework.web.servlet.HandlerMapping实现的抽象基类。 
 * 支持排序，默认处理程序，处理程序拦截器，包括由路径模式映射的处理程序拦截器。
 *
 * <p>Note: This base class does <i>not</i> support exposure of the
 * {@link #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}. Support for this attribute
 * is up to concrete subclasses, typically based on request URL mappings.
 * 
 * <p> 注意：此基类不支持PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE的曝光。 对此属性的支持取决于具体的子类，通常基于请求URL映射。
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 07.04.2003
 * @see #getHandlerInternal
 * @see #setDefaultHandler
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 * @see org.springframework.util.AntPathMatcher
 * @see #setInterceptors
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport
		implements HandlerMapping, Ordered {

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Object defaultHandler;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private final List<Object> interceptors = new ArrayList<Object>();

	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();

	private final List<MappedInterceptor> mappedInterceptors = new ArrayList<MappedInterceptor>();


	/**
	 * Specify the order value for this HandlerMapping bean.
	 * 
	 * <p> 指定此HandlerMapping bean的排序值。
	 * 
	 * <p>Default value is {@code Integer.MAX_VALUE}, meaning that it's non-ordered.
	 * 
	 * <p>默认值为Integer.MAX_VALUE，表示它是非有序的。
	 * 
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return this.order;
	}

	/**
	 * Set the default handler for this handler mapping.
	 * This handler will be returned if no specific mapping was found.
	 * 
	 * <p> 设置此处理程序映射的默认处理程序。 如果未找到特定映射，则将返回此处理程序。
	 * 
	 * <p>Default is {@code null}, indicating no default handler.
	 * 
	 * <p> 默认值为null，表示没有默认处理程序。
	 */
	public void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	/**
	 * Return the default handler for this handler mapping,
	 * or {@code null} if none.
	 * 
	 * <p> 返回此处理程序映射的默认处理程序，如果没有，则返回null。
	 */
	public Object getDefaultHandler() {
		return this.defaultHandler;
	}

	/**
	 * Set if URL lookup should always use the full path within the current servlet
	 * context. Else, the path within the current servlet mapping is used if applicable
	 * (that is, in the case of a ".../*" servlet mapping in web.xml).
	 * 
	 * <p> 设置URL查找是否应始终使用当前servlet上下文中的完整路径。 否则，如果适用，则使用当前servlet映射中的路径
	 * （即，在web.xml中的“... / *”servlet映射的情况下）。
	 * 
	 * <p>Default is "false".
	 * 
	 * <p> 默认为“false”。
	 * 
	 * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
	}

	/**
	 * Set if context path and request URI should be URL-decoded. Both are returned
	 * <i>undecoded</i> by the Servlet API, in contrast to the servlet path.
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
	}

	/**
	 * Set if ";" (semicolon) content should be stripped from the request URI.
	 * <p>The default value is {@code true}.
	 * @see org.springframework.web.util.UrlPathHelper#setRemoveSemicolonContent(boolean)
	 */
	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
	}

	/**
	 * Set the UrlPathHelper to use for resolution of lookup paths.
	 * <p>Use this to override the default UrlPathHelper with a custom subclass,
	 * or to share common UrlPathHelper settings across multiple HandlerMappings
	 * and MethodNameResolvers.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}

	/**
	 * Return the UrlPathHelper implementation to use for resolution of lookup paths.
	 * 
	 * <p> 返回UrlPathHelper实现以用于解析查找路径。
	 */
	public UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}

	/**
	 * Set the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns. Default is AntPathMatcher.
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Return the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns.
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}

	/**
	 * Set the interceptors to apply for all handlers mapped by this handler mapping.
	 * <p>Supported interceptor types are HandlerInterceptor, WebRequestInterceptor, and MappedInterceptor.
	 * Mapped interceptors apply only to request URLs that match its path patterns.
	 * Mapped interceptor beans are also detected by type during initialization.
	 * @param interceptors array of handler interceptors, or {@code null} if none
	 * @see #adaptInterceptor
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 */
	public void setInterceptors(Object[] interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}


	/**
	 * Initializes the interceptors.
	 * @see #extendInterceptors(java.util.List)
	 * @see #initInterceptors()
	 */
	@Override
	protected void initApplicationContext() throws BeansException {
		extendInterceptors(this.interceptors);
		detectMappedInterceptors(this.mappedInterceptors);
		initInterceptors();
	}

	/**
	 * Extension hook that subclasses can override to register additional interceptors,
	 * given the configured interceptors (see {@link #setInterceptors}).
	 * 
	 * <p> 在给定配置的拦截器的情况下，子类可以覆盖以扩展其他拦截器的扩展钩子（请参阅setInterceptors）。
	 * 
	 * <p>Will be invoked before {@link #initInterceptors()} adapts the specified
	 * interceptors into {@link HandlerInterceptor} instances.
	 * 
	 * <p> 将在initInterceptors（）将指定的拦截器调整为HandlerInterceptor实例之前调用。
	 * 
	 * <p>The default implementation is empty.
	 * 
	 * <p> 默认实现为空。
	 * 
	 * @param interceptors the configured interceptor List (never {@code null}), allowing
	 * to add further interceptors before as well as after the existing interceptors
	 * 
	 * <p> 配置的拦截器List（从不为null），允许在现有拦截器之前和之后添加更多拦截器
	 */
	protected void extendInterceptors(List<Object> interceptors) {
	}

	/**
	 * Detect beans of type {@link MappedInterceptor} and add them to the list of mapped interceptors.
	 * 
	 * <p> 检测MappedInterceptor类型的bean并将它们添加到映射的拦截器列表中。
	 * 
	 * <p>This is called in addition to any {@link MappedInterceptor}s that may have been provided
	 * via {@link #setInterceptors}, by default adding all beans of type {@link MappedInterceptor}
	 * from the current context and its ancestors. Subclasses can override and refine this policy.
	 * 
	 * <p> 除了可能通过setInterceptors提供的任何MappedInterceptors之外，还调用此方法，默认情况下，
	 * 从当前上下文及其祖先添加MappedInterceptor类型的所有bean。 子类可以覆盖和优化此策略。
	 * 
	 * @param mappedInterceptors an empty list to add {@link MappedInterceptor} instances to
	 * 
	 * <p> 一个空列表，用于添加MappedInterceptor实例
	 */
	protected void detectMappedInterceptors(List<MappedInterceptor> mappedInterceptors) {
		mappedInterceptors.addAll(
				BeanFactoryUtils.beansOfTypeIncludingAncestors(
						getApplicationContext(), MappedInterceptor.class, true, false).values());
	}

	/**
	 * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and
	 * adapting {@link HandlerInterceptor}s and {@link WebRequestInterceptor}s if necessary.
	 * 
	 * <p> 初始化指定的拦截器，检查MappedInterceptors并在必要时调整HandlerInterceptors和WebRequestInterceptors。
	 * 
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				if (interceptor instanceof MappedInterceptor) {
					this.mappedInterceptors.add((MappedInterceptor) interceptor);
				}
				else {
					this.adaptedInterceptors.add(adaptInterceptor(interceptor));
				}
			}
		}
	}

	/**
	 * Adapt the given interceptor object to the {@link HandlerInterceptor} interface.
	 * 
	 * <p> 使给定的拦截器对象适应HandlerInterceptor接口。
	 * 
	 * <p>By default, the supported interceptor types are {@link HandlerInterceptor}
	 * and {@link WebRequestInterceptor}. Each given {@link WebRequestInterceptor}
	 * will be wrapped in a {@link WebRequestHandlerInterceptorAdapter}.
	 * Can be overridden in subclasses.
	 * 
	 * <p> 默认情况下，支持的拦截器类型是HandlerInterceptor和WebRequestInterceptor。 每个给定的
	 * WebRequestInterceptor都将包装在WebRequestHandlerInterceptorAdapter中。 可以在子类中重写。
	 * 
	 * @param interceptor the specified interceptor object
	 * 
	 * <p> 指定的拦截器对象
	 * 
	 * @return the interceptor wrapped as HandlerInterceptor
	 * 
	 * <p> 拦截器包装为HandlerInterceptor
	 * 
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter
	 */
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}
		else if (interceptor instanceof WebRequestInterceptor) {
			return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
		}
		else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}

	/**
	 * Return the adapted interceptors as {@link HandlerInterceptor} array.
	 * 
	 * <p> 将适应的拦截器返回为HandlerInterceptor数组。
	 * 
	 * @return the array of {@link HandlerInterceptor}s, or {@code null} if none
	 * 
	 * <p> HandlerInterceptors数组，如果没有，则返回null
	 */
	protected final HandlerInterceptor[] getAdaptedInterceptors() {
		int count = this.adaptedInterceptors.size();
		return (count > 0 ? this.adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null);
	}

	/**
	 * Return all configured {@link MappedInterceptor}s as an array.
	 * 
	 * <p> 将所有已配置的MappedInterceptors作为数组返回。
	 * 
	 * @return the array of {@link MappedInterceptor}s, or {@code null} if none
	 * 
	 * <p> MappedInterceptors数组，如果没有则为null
	 */
	protected final MappedInterceptor[] getMappedInterceptors() {
		int count = this.mappedInterceptors.size();
		return (count > 0 ? this.mappedInterceptors.toArray(new MappedInterceptor[count]) : null);
	}

	/**
	 * Look up a handler for the given request, falling back to the default
	 * handler if no specific one is found.
	 * 
	 * <p> 查找给定请求的处理程序，如果找不到特定的请求，则返回默认处理程序。
	 * 
	 * @param request current HTTP request
	 * @return the corresponding handler instance, or the default handler
	 * 
	 * <p> 相应的处理程序实例或默认处理程序
	 * 
	 * @see #getHandlerInternal
	 */
	public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		Object handler = getHandlerInternal(request);
		if (handler == null) {
			handler = getDefaultHandler();
		}
		if (handler == null) {
			return null;
		}
		// Bean name or resolved handler?
		// Bean名称或已解析的处理程序
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}
		return getHandlerExecutionChain(handler, request);
	}

	/**
	 * Look up a handler for the given request, returning {@code null} if no
	 * specific one is found. This method is called by {@link #getHandler};
	 * a {@code null} return value will lead to the default handler, if one is set.
	 * 
	 * <p> 查找给定请求的处理程序，如果未找到特定的请求，则返回null。 getHandler调用此方法;
	 *  如果设置了一个null返回值将导致默认处理程序。
	 * 
	 * <p>Note: This method may also return a pre-built {@link HandlerExecutionChain},
	 * combining a handler object with dynamically determined interceptors.
	 * Statically specified interceptors will get merged into such an existing chain.
	 * 
	 * <p> 注意：此方法还可以返回预先构建的HandlerExecutionChain，将处理程序对象与动态确定的拦截器组合在一起。 
	 * 静态指定的拦截器将合并到这样的现有链中。
	 * 
	 * @param request current HTTP request
	 * @return the corresponding handler instance, or {@code null} if none found
	 * 
	 * <p> 相应的处理程序实例，如果没有找到则返回null
	 * 
	 * @throws Exception if there is an internal error
	 * 
	 * <p> 如果有内部错误
	 */
	protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

	/**
	 * Build a {@link HandlerExecutionChain} for the given handler, including
	 * applicable interceptors.
	 * 
	 * <p> 为给定的处理程序构建HandlerExecutionChain，包括适用的拦截器。
	 * 
	 * <p>The default implementation builds a standard {@link HandlerExecutionChain}
	 * with the given handler, the handler mapping's common interceptors, and any
	 * {@link MappedInterceptor}s matching to the current request URL. Subclasses
	 * may override this in order to extend/rearrange the list of interceptors.
	 * 
	 * <p> 默认实现使用给定的处理程序，处理程序映射的公共拦截器以及与当前请求URL匹配的任何MappedInterceptors
	 * 构建标准HandlerExecutionChain。 子类可以覆盖它以扩展/重新排列拦截器列表。
	 * 
	 * <p><b>NOTE:</b> The passed-in handler object may be a raw handler or a
	 * pre-built {@link HandlerExecutionChain}. This method should handle those
	 * two cases explicitly, either building a new {@link HandlerExecutionChain}
	 * or extending the existing chain.
	 * 
	 * <p> 注意：传入的处理程序对象可以是原始处理程序或预构建的HandlerExecutionChain。 此方法应明确处理这两种情况，
	 * 即构建新的HandlerExecutionChain或扩展现有链。
	 * 
	 * <p>For simply adding an interceptor in a custom subclass, consider calling
	 * {@code super.getHandlerExecutionChain(handler, request)} and invoking
	 * {@link HandlerExecutionChain#addInterceptor} on the returned chain object.
	 * 
	 * <p> 要简单地在自定义子类中添加拦截器，请考虑调用super.getHandlerExecutionChain（handler，request）
	 * 并在返回的链对象上调用HandlerExecutionChain.addInterceptor。
	 * 
	 * @param handler the resolved handler instance (never {@code null})
	 * 
	 * <p> 已解析的处理程序实例（从不为null）
	 * 
	 * @param request current HTTP request
	 * @return the HandlerExecutionChain (never {@code null})
	 * @see #getAdaptedInterceptors()
	 */
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
				(HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
		chain.addInterceptors(getAdaptedInterceptors());

		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		for (MappedInterceptor mappedInterceptor : this.mappedInterceptors) {
			if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
				chain.addInterceptor(mappedInterceptor.getInterceptor());
			}
		}

		return chain;
	}

}
