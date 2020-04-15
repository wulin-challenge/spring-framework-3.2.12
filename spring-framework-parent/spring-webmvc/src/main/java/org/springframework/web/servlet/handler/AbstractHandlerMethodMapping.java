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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Abstract base class for {@link HandlerMapping} implementations that define a
 * mapping between a request and a {@link HandlerMethod}.
 * 
 * <p> HandlerMapping实现的抽象基类，用于定义请求和HandlerMethod之间的映射。
 *
 * <p>For each registered handler method, a unique mapping is maintained with
 * subclasses defining the details of the mapping type {@code <T>}.
 * 
 * <p> 对于每个已注册的处理程序方法，维护唯一映射，其中子类定义映射类型<T>的详细信息。
 *
 * @param <T> The mapping for a {@link HandlerMethod} containing the conditions
 * needed to match the handler method to incoming request.
 * 
 * <p> HandlerMethod的映射，包含将处理程序方法与传入请求匹配所需的条件。
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {

	private boolean detectHandlerMethodsInAncestorContexts = false;

	private final Map<T, HandlerMethod> handlerMethods = new LinkedHashMap<T, HandlerMethod>();

	private final MultiValueMap<String, T> urlMap = new LinkedMultiValueMap<String, T>();


	/**
	 * Whether to detect handler methods in beans in ancestor ApplicationContexts.
	 * 
	 * <p> 是否在祖先ApplicationContexts中检测bean中的处理程序方法。
	 * 
	 * <p>Default is "false": Only beans in the current ApplicationContext are
	 * considered, i.e. only in the context that this HandlerMapping itself
	 * is defined in (typically the current DispatcherServlet's context).
	 * 
	 * <p> 默认值为“false”：仅考虑当前ApplicationContext中的bean，即仅在定义此HandlerMapping
	 * 本身的上下文中（通常是当前的DispatcherServlet的上下文）。
	 * 
	 * <p>Switch this flag on to detect handler beans in ancestor contexts
	 * (typically the Spring root WebApplicationContext) as well.
	 * 
	 * <p> 切换此标志以检测祖先上下文中的处理程序bean（通常是Spring根WebApplicationContext）。
	 */
	public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
		this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
	}

	/**
	 * Return a map with all handler methods and their mappings.
	 * 
	 * <p> 返回包含所有处理程序方法及其映射的映射。
	 */
	public Map<T, HandlerMethod> getHandlerMethods() {
		return Collections.unmodifiableMap(this.handlerMethods);
	}

	/**
	 * Detects handler methods at initialization.
	 * 
	 * <p> 在初始化时检测处理程序方法。
	 */
	public void afterPropertiesSet() {
		initHandlerMethods();
	}

	/**
	 * Scan beans in the ApplicationContext, detect and register handler methods.
	 * 
	 * <p> 在ApplicationContext中扫描bean，检测并注册处理程序方法。
	 * 
	 * @see #isHandler(Class)
	 * @see #getMappingForMethod(Method, Class)
	 * @see #handlerMethodsInitialized(Map)
	 */
	protected void initHandlerMethods() {
		if (logger.isDebugEnabled()) {
			logger.debug("Looking for request mappings in application context: " + getApplicationContext());
		}

		String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ?
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
				getApplicationContext().getBeanNamesForType(Object.class));

		for (String beanName : beanNames) {
			if (isHandler(getApplicationContext().getType(beanName))){
				detectHandlerMethods(beanName);
			}
		}
		handlerMethodsInitialized(getHandlerMethods());
	}

	/**
	 * Whether the given type is a handler with handler methods.
	 * 
	 * <p> 给定类型是否是具有处理程序方法的处理程序。
	 * 
	 * @param beanType the type of the bean being checked
	 * 
	 * <p> 被检查的bean的类型
	 * 
	 * @return "true" if this a handler type, "false" otherwise.
	 * 
	 * <p> 如果这是一个处理程序类型，则为“true”，否则为“false”。
	 */
	protected abstract boolean isHandler(Class<?> beanType);

	/**
	 * Look for handler methods in a handler.
	 * 
	 * <p> 在处理程序中查找处理程序方法。
	 * 
	 * @param handler the bean name of a handler or a handler instance
	 * 
	 * <p> 处理程序或处理程序实例的bean名称
	 */
	protected void detectHandlerMethods(final Object handler) {
		Class<?> handlerType =
				(handler instanceof String ? getApplicationContext().getType((String) handler) : handler.getClass());

		// Avoid repeated calls to getMappingForMethod which would rebuild RequestMatchingInfo instances
		// 避免重复调用getMappingForMethod来重建RequestMatchingInfo实例
		final Map<Method, T> mappings = new IdentityHashMap<Method, T>();
		final Class<?> userType = ClassUtils.getUserClass(handlerType);

		Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new MethodFilter() {
			public boolean matches(Method method) {
				T mapping = getMappingForMethod(method, userType);
				if (mapping != null) {
					mappings.put(method, mapping);
					return true;
				}
				else {
					return false;
				}
			}
		});

		for (Method method : methods) {
			registerHandlerMethod(handler, method, mappings.get(method));
		}
	}

	/**
	 * Provide the mapping for a handler method. A method for which no
	 * mapping can be provided is not a handler method.
	 * 
	 * <p> 提供处理程序方法的映射。 不能提供映射的方法不是处理程序方法。
	 * 
	 * @param method the method to provide a mapping for
	 * 
	 * <p> 提供映射的方法
	 * 
	 * @param handlerType the handler type, possibly a sub-type of the method's
	 * declaring class
	 * 
	 * <p> 处理程序类型，可能是方法声明类的子类型
	 * 
	 * @return the mapping, or {@code null} if the method is not mapped
	 * 
	 * <p> 映射，如果未映射方法，则返回null
	 */
	protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

	/**
	 * Register a handler method and its unique mapping.
	 * 
	 * <p> 注册处理程序方法及其唯一映射。
	 * 
	 * @param handler the bean name of the handler or the handler instance
	 * 
	 * <p> 处理程序的bean名称或处理程序实例
	 * 
	 * @param method the method to register
	 * 
	 * <p> 注册的方法
	 * 
	 * @param mapping the mapping conditions associated with the handler method
	 * 
	 * <p> 与处理程序方法关联的映射条件
	 * 
	 * @throws IllegalStateException if another method was already registered
	 * under the same mapping
	 * 
	 * <p> 如果另一个方法已在同一映射下注册
	 * 
	 */
	protected void registerHandlerMethod(Object handler, Method method, T mapping) {
		HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
		HandlerMethod oldHandlerMethod = this.handlerMethods.get(mapping);
		if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
			throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + newHandlerMethod.getBean() +
					"' bean method \n" + newHandlerMethod + "\nto " + mapping + ": There is already '" +
					oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
		}

		this.handlerMethods.put(mapping, newHandlerMethod);
		if (logger.isInfoEnabled()) {
			logger.info("Mapped \"" + mapping + "\" onto " + newHandlerMethod);
		}

		Set<String> patterns = getMappingPathPatterns(mapping);
		for (String pattern : patterns) {
			if (!getPathMatcher().isPattern(pattern)) {
				this.urlMap.add(pattern, mapping);
			}
		}
	}

	/**
	 * Create the HandlerMethod instance.
	 * 
	 * <p> 创建HandlerMethod实例。
	 * 
	 * @param handler either a bean name or an actual handler instance
	 * 
	 * <p> bean名称或实际处理程序实例
	 * 
	 * @param method the target method
	 * @return the created HandlerMethod
	 */
	protected HandlerMethod createHandlerMethod(Object handler, Method method) {
		HandlerMethod handlerMethod;
		if (handler instanceof String) {
			String beanName = (String) handler;
			handlerMethod = new HandlerMethod(beanName, getApplicationContext(), method);
		}
		else {
			handlerMethod = new HandlerMethod(handler, method);
		}
		return handlerMethod;
	}

	/**
	 * Extract and return the URL paths contained in a mapping.
	 * 
	 * <p> 提取并返回映射中包含的URL路径。
	 */
	protected abstract Set<String> getMappingPathPatterns(T mapping);

	/**
	 * Invoked after all handler methods have been detected.
	 * 
	 * <p> 检测到所有处理程序方法后调用。
	 * 
	 * @param handlerMethods a read-only map with handler methods and mappings.
	 * 
	 * <p> 带有处理程序方法和映射的只读映射。
	 */
	protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
	}


	/**
	 * Look up a handler method for the given request.
	 * 
	 * <p> 查找给定请求的处理程序方法。
	 */
	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up handler method for path " + lookupPath);
		}
		HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
		if (logger.isDebugEnabled()) {
			if (handlerMethod != null) {
				logger.debug("Returning handler method [" + handlerMethod + "]");
			}
			else {
				logger.debug("Did not find handler method for [" + lookupPath + "]");
			}
		}
		return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
	}

	/**
	 * Look up the best-matching handler method for the current request.
	 * If multiple matches are found, the best match is selected.
	 * 
	 * <p> 查找当前请求的最佳匹配处理程序方法。 如果找到多个匹配项，则选择最佳匹配项。
	 * 
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * 
	 * <p> 映射当前servlet映射中的查找路径
	 * 
	 * @param request the current request
	 * @return the best-matching handler method, or {@code null} if no match
	 * 
	 * <p> 最佳匹配处理程序方法，如果不匹配，则返回null
	 * 
	 * @see #handleMatch(Object, String, HttpServletRequest)
	 * @see #handleNoMatch(Set, String, HttpServletRequest)
	 */
	protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
		List<Match> matches = new ArrayList<Match>();
		List<T> directPathMatches = this.urlMap.get(lookupPath);
		if (directPathMatches != null) {
			addMatchingMappings(directPathMatches, matches, request);
		}
		if (matches.isEmpty()) {
			// No choice but to go through all mappings...
			// 别无选择，只能通过所有映射......
			addMatchingMappings(this.handlerMethods.keySet(), matches, request);
		}

		if (!matches.isEmpty()) {
			Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
			Collections.sort(matches, comparator);
			if (logger.isTraceEnabled()) {
				logger.trace("Found " + matches.size() + " matching mapping(s) for [" + lookupPath + "] : " + matches);
			}
			Match bestMatch = matches.get(0);
			if (matches.size() > 1) {
				Match secondBestMatch = matches.get(1);
				if (comparator.compare(bestMatch, secondBestMatch) == 0) {
					Method m1 = bestMatch.handlerMethod.getMethod();
					Method m2 = secondBestMatch.handlerMethod.getMethod();
					throw new IllegalStateException(
							"Ambiguous handler methods mapped for HTTP path '" + request.getRequestURL() + "': {" +
							m1 + ", " + m2 + "}");
				}
			}
			handleMatch(bestMatch.mapping, lookupPath, request);
			return bestMatch.handlerMethod;
		}
		else {
			return handleNoMatch(handlerMethods.keySet(), lookupPath, request);
		}
	}

	private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpServletRequest request) {
		for (T mapping : mappings) {
			T match = getMatchingMapping(mapping, request);
			if (match != null) {
				matches.add(new Match(match, this.handlerMethods.get(mapping)));
			}
		}
	}

	/**
	 * Check if a mapping matches the current request and return a (potentially
	 * new) mapping with conditions relevant to the current request.
	 * 
	 * <p> 检查映射是否与当前请求匹配，并返回具有与当前请求相关的条件的（可能是新的）映射。
	 * 
	 * @param mapping the mapping to get a match for
	 * 
	 * <p> 获取匹配的映射
	 * 
	 * @param request the current HTTP servlet request
	 * 
	 * <p> 当前的HTTP servlet请求
	 * 
	 * @return the match, or {@code null} if the mapping doesn't match
	 * 
	 * <p> 匹配，如果映射不匹配，则返回null
	 * 
	 */
	protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);

	/**
	 * Return a comparator for sorting matching mappings.
	 * The returned comparator should sort 'better' matches higher.
	 * 
	 * <p> 返回比较器以对匹配映射进行排序。 返回的比较器应该排序'更好'匹配更高。
	 * 
	 * @param request the current request
	 * @return the comparator, never {@code null} - 比较器，永远不会为空
	 */
	protected abstract Comparator<T> getMappingComparator(HttpServletRequest request);

	/**
	 * Invoked when a matching mapping is found.
	 * 
	 * <p> 找到匹配的映射时调用。
	 * 
	 * @param mapping the matching mapping
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * 
	 * <p> 映射当前servlet映射中的查找路径
	 * 
	 * @param request the current request
	 */
	protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
	}

	/**
	 * Invoked when no matching mapping is not found.
	 * 
	 * <p> 未找到匹配的映射时调用。
	 * 
	 * @param mappings all registered mappings
	 * 
	 * <p> 所有已注册的映射
	 * 
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * 
	 * <p> 映射当前servlet映射中的查找路径
	 * 
	 * @param request the current request
	 * @throws ServletException in case of errors
	 */
	protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request)
			throws Exception {

		return null;
	}


	/**
	 * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
	 * comparing the best match with a comparator in the context of the current request.
	 * 
	 * <p> 围绕匹配的HandlerMethod及其映射的薄包装，用于在当前请求的上下文中比较最佳匹配与比较器。
	 */
	private class Match {

		private final T mapping;

		private final HandlerMethod handlerMethod;

		public Match(T mapping, HandlerMethod handlerMethod) {
			this.mapping = mapping;
			this.handlerMethod = handlerMethod;
		}

		@Override
		public String toString() {
			return this.mapping.toString();
		}
	}


	private class MatchComparator implements Comparator<Match> {

		private final Comparator<T> comparator;

		public MatchComparator(Comparator<T> comparator) {
			this.comparator = comparator;
		}

		public int compare(Match match1, Match match2) {
			return this.comparator.compare(match1.mapping, match2.mapping);
		}
	}

}
