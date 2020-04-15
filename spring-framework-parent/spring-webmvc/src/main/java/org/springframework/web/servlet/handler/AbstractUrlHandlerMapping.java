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

package org.springframework.web.servlet.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Abstract base class for URL-mapped {@link org.springframework.web.servlet.HandlerMapping}
 * implementations. Provides infrastructure for mapping handlers to URLs and configurable
 * URL lookup. For information on the latter, see "alwaysUseFullPath" property.
 * 
 * <p> URL映射的org.springframework.web.servlet.HandlerMapping实现的抽象基类。
 *  提供将处理程序映射到URL和可配置URL查找的基础结构。 有关后者的信息，请参阅“alwaysUseFullPath”属性。
 *
 * <p>Supports direct matches, e.g. a registered "/test" matches "/test", and
 * various Ant-style pattern matches, e.g. a registered "/t*" pattern matches
 * both "/test" and "/team", "/test/*" matches all paths in the "/test" directory,
 * "/test/**" matches all paths below "/test". For details, see the
 * {@link org.springframework.util.AntPathMatcher AntPathMatcher} javadoc.
 * 
 * <p> 支持直接匹配，例如 注册的“/ test”匹配“/ test”，以及各种Ant样式模式匹配，例如： 注册的“/ t *”
 * 模式匹配“/ test”和“/ team”，“/ test / *”匹配“/ test”目录中的所有路径，“/ test / **”
 * 匹配下面的所有路径“/ 测试”。 有关详细信息，请参阅AntPathMatcher javadoc。
 *
 * <p>Will search all path patterns to find the most exact match for the
 * current request path. The most exact match is defined as the longest
 * path pattern that matches the current request path.
 * 
 * <p> 将搜索所有路径模式以查找当前请求路径的最精确匹配。 最精确的匹配被定义为与当前请求路径匹配的最长路径模式。
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 16.04.2003
 */
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping {

	private Object rootHandler;

	private boolean lazyInitHandlers = false;

	private final Map<String, Object> handlerMap = new LinkedHashMap<String, Object>();


	/**
	 * Set the root handler for this handler mapping, that is,
	 * the handler to be registered for the root path ("/").
	 * 
	 * <p> 设置此处理程序映射的根处理程序，即要为根路径（“/”）注册的处理程序。
	 * 
	 * <p>Default is {@code null}, indicating no root handler.
	 * 
	 * <p> 默认值为null，表示没有根处理程序。
	 */
	public void setRootHandler(Object rootHandler) {
		this.rootHandler = rootHandler;
	}

	/**
	 * Return the root handler for this handler mapping (registered for "/"),
	 * or {@code null} if none.
	 * 
	 * <p> 返回此处理程序映射的根处理程序（注册为“/”），如果没有，则返回null。
	 */
	public Object getRootHandler() {
		return this.rootHandler;
	}

	/**
	 * Set whether to lazily initialize handlers. Only applicable to
	 * singleton handlers, as prototypes are always lazily initialized.
	 * Default is "false", as eager initialization allows for more efficiency
	 * through referencing the controller objects directly.
	 * 
	 * <p> 设置是否延迟初始化处理程序。 仅适用于单例处理程序，因为原型总是被懒惰地初始化。 默认值为“false”，
	 * 因为急切初始化可以通过直接引用控制器对象来提高效率。
	 * 
	 * <p>If you want to allow your controllers to be lazily initialized,
	 * make them "lazy-init" and set this flag to true. Just making them
	 * "lazy-init" will not work, as they are initialized through the
	 * references from the handler mapping in this case.
	 * 
	 * <p> 如果要允许懒惰地初始化控制器，请将它们设置为“lazy-init”并将此标志设置为true。 
	 * 只是使它们成为“lazy-init”是行不通的，因为在这种情况下它们是通过处理程序映射的引用初始化的。
	 * 
	 */
	public void setLazyInitHandlers(boolean lazyInitHandlers) {
		this.lazyInitHandlers = lazyInitHandlers;
	}

	/**
	 * Look up a handler for the URL path of the given request.
	 * 
	 * <p> 查找给定请求的URL路径的处理程序。
	 * @param request current HTTP request
	 * @return the handler instance, or {@code null} if none found
	 * 
	 * <p> 处理程序实例，如果没有找到则为null
	 */
	@Override
	protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		Object handler = lookupHandler(lookupPath, request);
		if (handler == null) {
			// We need to care for the default handler directly, since we need to
			// expose the PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE for it as well.
			
			// 我们需要直接关注默认处理程序，因为我们还需要为它公开PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE。
			Object rawHandler = null;
			if ("/".equals(lookupPath)) {
				rawHandler = getRootHandler();
			}
			if (rawHandler == null) {
				rawHandler = getDefaultHandler();
			}
			if (rawHandler != null) {
				// Bean name or resolved handler?
				// Bean名称或已解析的处理程序
				if (rawHandler instanceof String) {
					String handlerName = (String) rawHandler;
					rawHandler = getApplicationContext().getBean(handlerName);
				}
				validateHandler(rawHandler, request);
				handler = buildPathExposingHandler(rawHandler, lookupPath, lookupPath, null);
			}
		}
		if (handler != null && logger.isDebugEnabled()) {
			logger.debug("Mapping [" + lookupPath + "] to " + handler);
		}
		else if (handler == null && logger.isTraceEnabled()) {
			logger.trace("No handler mapping found for [" + lookupPath + "]");
		}
		return handler;
	}

	/**
	 * Look up a handler instance for the given URL path.
	 * 
	 * <p> 查找给定URL路径的处理程序实例。
	 * 
	 * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
	 * and various Ant-style pattern matches, e.g. a registered "/t*" matches
	 * both "/test" and "/team". For details, see the AntPathMatcher class.
	 * 
	 * <p> 支持直接匹配，例如 注册的“/ test”匹配“/ test”，以及各种Ant样式模式匹配，例如： 
	 * 注册的“/ t *”匹配“/ test”和“/ team”。 有关详细信息，请参阅AntPathMatcher类。
	 * 
	 * <p>Looks for the most exact pattern, where most exact is defined as
	 * the longest path pattern.
	 * 
	 * <p> 寻找最精确的模式，其中最精确的模式被定义为最长的路径模式。
	 * 
	 * @param urlPath URL the bean is mapped to - bean映射到的URL
	 * @param request current HTTP request (to expose the path within the mapping to)
	 * 
	 * <p> 当前的HTTP请求（用于公开映射中的路径）
	 * 
	 * @return the associated handler instance, or {@code null} if not found
	 * 
	 * <p> 关联的处理程序实例，如果未找到则为null
	 * 
	 * @see #exposePathWithinMapping
	 * @see org.springframework.util.AntPathMatcher
	 */
	protected Object lookupHandler(String urlPath, HttpServletRequest request) throws Exception {
		// Direct match?
		// 直接匹配？
		Object handler = this.handlerMap.get(urlPath);
		if (handler != null) {
			// Bean name or resolved handler?
			// Bean名称或已解析的处理程序
			if (handler instanceof String) {
				String handlerName = (String) handler;
				handler = getApplicationContext().getBean(handlerName);
			}
			validateHandler(handler, request);
			return buildPathExposingHandler(handler, urlPath, urlPath, null);
		}
		// Pattern match?
		// 模式匹配？
		List<String> matchingPatterns = new ArrayList<String>();
		for (String registeredPattern : this.handlerMap.keySet()) {
			if (getPathMatcher().match(registeredPattern, urlPath)) {
				matchingPatterns.add(registeredPattern);
			}
		}
		String bestPatternMatch = null;
		Comparator<String> patternComparator = getPathMatcher().getPatternComparator(urlPath);
		if (!matchingPatterns.isEmpty()) {
			Collections.sort(matchingPatterns, patternComparator);
			if (logger.isDebugEnabled()) {
				logger.debug("Matching patterns for request [" + urlPath + "] are " + matchingPatterns);
			}
			bestPatternMatch = matchingPatterns.get(0);
		}
		if (bestPatternMatch != null) {
			handler = this.handlerMap.get(bestPatternMatch);
			// Bean name or resolved handler?
			// Bean名称或已解析的处理程序
			if (handler instanceof String) {
				String handlerName = (String) handler;
				handler = getApplicationContext().getBean(handlerName);
			}
			validateHandler(handler, request);
			String pathWithinMapping = getPathMatcher().extractPathWithinPattern(bestPatternMatch, urlPath);

			// There might be multiple 'best patterns', let's make sure we have the correct URI template variables
			// for all of them
			
			// 可能存在多个“最佳模式”，让我们确保所有这些都具有正确的URI模板变量
			Map<String, String> uriTemplateVariables = new LinkedHashMap<String, String>();
			for (String matchingPattern : matchingPatterns) {
				if (patternComparator.compare(bestPatternMatch, matchingPattern) == 0) {
					Map<String, String> vars = getPathMatcher().extractUriTemplateVariables(matchingPattern, urlPath);
					Map<String, String> decodedVars = getUrlPathHelper().decodePathVariables(request, vars);
					uriTemplateVariables.putAll(decodedVars);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("URI Template variables for request [" + urlPath + "] are " + uriTemplateVariables);
			}
			return buildPathExposingHandler(handler, bestPatternMatch, pathWithinMapping, uriTemplateVariables);
		}
		// No handler found...
		return null;
	}

	/**
	 * Validate the given handler against the current request.
	 * 
	 * <p> 根据当前请求验证给定的处理程序。
	 * 
	 * <p>The default implementation is empty. Can be overridden in subclasses,
	 * for example to enforce specific preconditions expressed in URL mappings.
	 * 
	 * <p> 默认实现为空。 可以在子类中重写，例如，强制执行URL映射中表示的特定前提条件。
	 * 
	 * @param handler the handler object to validate
	 * 
	 * <p> 要验证的处理程序对象
	 * 
	 * @param request current HTTP request
	 * @throws Exception if validation failed
	 */
	protected void validateHandler(Object handler, HttpServletRequest request) throws Exception {
	}

	/**
	 * Build a handler object for the given raw handler, exposing the actual
	 * handler, the {@link #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}, as well as
	 * the {@link #URI_TEMPLATE_VARIABLES_ATTRIBUTE} before executing the handler.
	 * 
	 * <p> 为给定的原始处理程序构建处理程序对象，在执行处理程序之前公开实际处理程序
	 * PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE以及URI_TEMPLATE_VARIABLES_ATTRIBUTE。
	 * 
	 * <p>The default implementation builds a {@link HandlerExecutionChain}
	 * with a special interceptor that exposes the path attribute and uri template variables
	 * 
	 * <p> 默认实现使用特殊拦截器构建HandlerExecutionChain，该拦截器公开path属性和uri模板变量
	 * 
	 * @param rawHandler the raw handler to expose
	 * 
	 * <p> 要暴露的原始处理程序
	 * 
	 * @param pathWithinMapping the path to expose before executing the handler
	 * 
	 * <p> 在执行处理程序之前公开的路径
	 * 
	 * @param uriTemplateVariables the URI template variables, can be {@code null} if no variables found
	 * 
	 * <p> URI模板变量，如果没有找到变量，则可以为null
	 * 
	 * @return the final handler object
	 * 
	 * <p> 最终的处理程序对象
	 */
	protected Object buildPathExposingHandler(Object rawHandler, String bestMatchingPattern,
			String pathWithinMapping, Map<String, String> uriTemplateVariables) {

		HandlerExecutionChain chain = new HandlerExecutionChain(rawHandler);
		chain.addInterceptor(new PathExposingHandlerInterceptor(bestMatchingPattern, pathWithinMapping));
		if (!CollectionUtils.isEmpty(uriTemplateVariables)) {
			chain.addInterceptor(new UriTemplateVariablesHandlerInterceptor(uriTemplateVariables));
		}
		return chain;
	}

	/**
	 * Expose the path within the current mapping as request attribute.
	 * 
	 * <p> 将当前映射中的路径公开为请求属性。
	 * 
	 * @param pathWithinMapping the path within the current mapping
	 * 
	 * <p> 当前映射中的路径
	 * 
	 * @param request the request to expose the path to - 公开路径的请求
	 * @see #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
	 */
	protected void exposePathWithinMapping(String bestMatchingPattern, String pathWithinMapping, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, bestMatchingPattern);
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, pathWithinMapping);
	}

	/**
	 * Expose the URI templates variables as request attribute.
	 * 
	 * <p> 将URI模板变量公开为请求属性。
	 * 
	 * @param uriTemplateVariables the URI template variables
	 * 
	 * <p> URI模板变量
	 * 
	 * @param request the request to expose the path to- 公开路径的请求
	 * @see #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
	 */
	protected void exposeUriTemplateVariables(Map<String, String> uriTemplateVariables, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariables);
	}

	/**
	 * Register the specified handler for the given URL paths.
	 * 
	 * <p> 为给定的URL路径注册指定的处理程序。
	 * 
	 * @param urlPaths the URLs that the bean should be mapped to
	 * 
	 * <p> bean应该映射到的URL
	 * 
	 * @param beanName the name of the handler bean
	 * 
	 * <p> 处理程序bean的名称
	 * 
	 * @throws BeansException if the handler couldn't be registered
	 * 
	 * <p> 如果处理程序无法注册
	 * 
	 * @throws IllegalStateException if there is a conflicting handler registered
	 * 
	 * <p> 如果存在冲突的处理程序注册
	 */
	protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
		Assert.notNull(urlPaths, "URL path array must not be null");
		for (String urlPath : urlPaths) {
			registerHandler(urlPath, beanName);
		}
	}

	/**
	 * Register the specified handler for the given URL path.
	 * 
	 * <p> 注册给定URL路径的指定处理程序。
	 * 
	 * @param urlPath the URL the bean should be mapped to
	 * 
	 * <p> bean应该映射到的URL
	 * 
	 * @param handler the handler instance or handler bean name String
	 * (a bean name will automatically be resolved into the corresponding handler bean)
	 * 
	 * <p> 处理程序实例或处理程序bean名称String（bean名称将自动解析为相应的处理程序bean）
	 * 
	 * @throws BeansException if the handler couldn't be registered
	 * 
	 * <p> 如果处理程序无法注册
	 * 
	 * @throws IllegalStateException if there is a conflicting handler registered
	 * 
	 * <p> 如果存在冲突的处理程序注册
	 * 
	 */
	protected void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {
		Assert.notNull(urlPath, "URL path must not be null");
		Assert.notNull(handler, "Handler object must not be null");
		Object resolvedHandler = handler;

		// Eagerly resolve handler if referencing singleton via name.
		// 如果通过名称引用singleton，则急切地解析处理程序。
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				resolvedHandler = getApplicationContext().getBean(handlerName);
			}
		}

		Object mappedHandler = this.handlerMap.get(urlPath);
		if (mappedHandler != null) {
			if (mappedHandler != resolvedHandler) {
				throw new IllegalStateException(
						"Cannot map " + getHandlerDescription(handler) + " to URL path [" + urlPath +
						"]: There is already " + getHandlerDescription(mappedHandler) + " mapped.");
			}
		}
		else {
			if (urlPath.equals("/")) {
				if (logger.isInfoEnabled()) {
					logger.info("Root mapping to " + getHandlerDescription(handler));
				}
				setRootHandler(resolvedHandler);
			}
			else if (urlPath.equals("/*")) {
				if (logger.isInfoEnabled()) {
					logger.info("Default mapping to " + getHandlerDescription(handler));
				}
				setDefaultHandler(resolvedHandler);
			}
			else {
				this.handlerMap.put(urlPath, resolvedHandler);
				if (logger.isInfoEnabled()) {
					logger.info("Mapped URL path [" + urlPath + "] onto " + getHandlerDescription(handler));
				}
			}
		}
	}

	private String getHandlerDescription(Object handler) {
		return "handler " + (handler instanceof String ? "'" + handler + "'" : "of type [" + handler.getClass() + "]");
	}


	/**
	 * Return the registered handlers as an unmodifiable Map, with the registered path
	 * as key and the handler object (or handler bean name in case of a lazy-init handler)
	 * as value.
	 * 
	 * <p> 将已注册的处理程序作为不可修改的Map返回，将注册的路径作为键，将处理程序对象（或lazy-init处理程序中的处理程序bean名称）作为值。
	 * 
	 * @see #getDefaultHandler()
	 */
	public final Map<String, Object> getHandlerMap() {
		return Collections.unmodifiableMap(this.handlerMap);
	}

	/**
	 * Indicates whether this handler mapping support type-level mappings. Default to {@code false}.
	 * 
	 * <p> 指示此处理程序映射是否支持类型级别映射。 默认为false。
	 */
	protected boolean supportsTypeLevelMappings() {
		return false;
	}


	/**
	 * Special interceptor for exposing the
	 * {@link AbstractUrlHandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE} attribute.
	 * 
	 * <p> 用于公开AbstractUrlHandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE属性的特殊拦截器。
	 * 
	 * @see AbstractUrlHandlerMapping#exposePathWithinMapping
	 */
	private class PathExposingHandlerInterceptor extends HandlerInterceptorAdapter {

		private final String bestMatchingPattern;

		private final String pathWithinMapping;

		public PathExposingHandlerInterceptor(String bestMatchingPattern, String pathWithinMapping) {
			this.bestMatchingPattern = bestMatchingPattern;
			this.pathWithinMapping = pathWithinMapping;
		}

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
			exposePathWithinMapping(this.bestMatchingPattern, this.pathWithinMapping, request);
			request.setAttribute(HandlerMapping.INTROSPECT_TYPE_LEVEL_MAPPING, supportsTypeLevelMappings());
			return true;
		}

	}

	/**
	 * Special interceptor for exposing the
	 * {@link AbstractUrlHandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE} attribute.
	 * 
	 * <p> 用于公开AbstractUrlHandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE属性的特殊拦截器。
	 * 
	 * @see AbstractUrlHandlerMapping#exposePathWithinMapping
	 */
	private class UriTemplateVariablesHandlerInterceptor extends HandlerInterceptorAdapter {

		private final Map<String, String> uriTemplateVariables;

		public UriTemplateVariablesHandlerInterceptor(Map<String, String> uriTemplateVariables) {
			this.uriTemplateVariables = uriTemplateVariables;
		}

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
			exposeUriTemplateVariables(this.uriTemplateVariables, request);
			return true;
		}
	}

}
