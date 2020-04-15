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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to be implemented by objects that define a mapping between
 * requests and handler objects.
 * 
 * <p> 由定义请求和处理程序对象之间的映射的对象实现的接口。
 *
 * <p>This class can be implemented by application developers, although this is not
 * necessary, as {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}
 * and {@link org.springframework.web.servlet.handler.SimpleUrlHandlerMapping}
 * are included in the framework. The former is the default if no
 * HandlerMapping bean is registered in the application context.
 * 
 * <p> 此类可以由应用程序开发人员实现，但这不是必需的，因为org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 * 和org.springframework.web.servlet.handler.SimpleUrlHandlerMapping包含在框架中。如果在应用程序上下文中未注册
 * HandlerMapping bean，则前者是缺省值。
 *
 * <p>HandlerMapping implementations can support mapped interceptors but do not
 * have to. A handler will always be wrapped in a {@link HandlerExecutionChain}
 * instance, optionally accompanied by some {@link HandlerInterceptor} instances.
 * The DispatcherServlet will first call each HandlerInterceptor's
 * {@code preHandle} method in the given order, finally invoking the handler
 * itself if all {@code preHandle} methods have returned {@code true}.
 * 
 * <p> HandlerMapping实现可以支持映射的拦截器，但不必如此。处理程序将始终包装在HandlerExecutionChain实例中，
 * 可选地伴随一些HandlerInterceptor实例。 DispatcherServlet将首先按给定的顺序调用每个HandlerInterceptor
 * 的preHandle方法，如果所有preHandle方法都返回true，则最终调用处理程序本身。
 *
 * <p>The ability to parameterize this mapping is a powerful and unusual
 * capability of this MVC framework. For example, it is possible to write
 * a custom mapping based on session state, cookie state or many other
 * variables. No other MVC framework seems to be equally flexible.
 * 
 * <p> 参数化此映射的能力是此MVC框架的强大且不寻常的功能。例如，可以基于会话状态，
 * cookie状态或许多其他变量编写自定义映射。没有其他MVC框架似乎同样灵活。
 *
 * <p>Note: Implementations can implement the {@link org.springframework.core.Ordered}
 * interface to be able to specify a sorting order and thus a priority for getting
 * applied by DispatcherServlet. Non-Ordered instances get treated as lowest priority.
 * 
 * <p> 注意：实现可以实现org.springframework.core.Ordered接口，以便能够指定排序顺序，从而获得
 * DispatcherServlet应用的优先级。非有序实例被视为最低优先级。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.core.Ordered
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 */
public interface HandlerMapping {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the path
	 * within the handler mapping, in case of a pattern match, or the full
	 * relevant URI (typically within the DispatcherServlet's mapping) else.
	 * 
	 * <p> HttpServletRequest属性的名称，该属性包含处理程序映射中的路径（如果是模式匹配），或者包含完整相关
	 * URI（通常在DispatcherServlet的映射中）。
	 * 
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 * 
	 * <p> 注意：所有HandlerMapping实现都不需要此属性。 基于URL的HandlerMappings通常会支持它，
	 * 但处理程序不一定要求在所有方案中都存在此请求属性。
	 * 
	 */
	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the
	 * best matching pattern within the handler mapping.
	 * 
	 * <p> HttpServletRequest属性的名称，该属性包含处理程序映射中的最佳匹配模式。
	 * 
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 * 
	 * <p> 注意：所有HandlerMapping实现都不需要此属性。 基于URL的HandlerMappings通常会支持它，
	 * 但处理程序不一定要求在所有方案中都存在此请求属性。
	 * 
	 */
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";

	/**
	 * Name of the boolean {@link HttpServletRequest} attribute that indicates
	 * whether type-level mappings should be inspected.
	 * 
	 * <p> 布尔HttpServletRequest属性的名称，指示是否应检查类型级别映射。
	 * 
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations.
	 * 
	 * <p> 注意：所有HandlerMapping实现都不需要此属性。
	 */
	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the URI
	 * templates map, mapping variable names to values.
	 * 
	 * <p> 包含URI模板映射的HttpServletRequest属性的名称，将变量名称映射到值。
	 * 
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 * 
	 * <p> 注意：所有HandlerMapping实现都不需要此属性。 基于URL的HandlerMappings通常会支持它，
	 * 但处理程序不一定要求在所有方案中都存在此请求属性。
	 */
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a map with
	 * URI matrix variables.
	 * 
	 * <p> 包含具有URI矩阵变量的映射的HttpServletRequest属性的名称。
	 * 
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations and may also not be present depending on
	 * whether the HandlerMapping is configured to keep matrix variable content
	 * in the request URI.
	 * 
	 * <p> 注意：此属性不需要所有HandlerMapping实现都支持，也可能不存在，具体取决于HandlerMapping
	 * 是否配置为在请求URI中保留矩阵变量内容。
	 */
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the set of
	 * producible MediaTypes applicable to the mapped handler.
	 * 
	 * <p> HttpServletRequest属性的名称，该属性包含适用于映射处理程序的可生成MediaType集。
	 * 
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. Handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 * 
	 * <p> 注意：所有HandlerMapping实现都不需要此属性。 处理程序不一定要求在所有方案中都存在此请求属性。
	 */
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";

	/**
	 * Return a handler and any interceptors for this request. The choice may be made
	 * on request URL, session state, or any factor the implementing class chooses.
	 * 
	 * <p> 返回此请求的处理程序和任何拦截器。 可以根据请求URL，会话状态或实现类选择的任何因素进行选择。
	 * 
	 * <p>The returned HandlerExecutionChain contains a handler Object, rather than
	 * even a tag interface, so that handlers are not constrained in any way.
	 * For example, a HandlerAdapter could be written to allow another framework's
	 * handler objects to be used.
	 * 
	 * <p> 返回的HandlerExecutionChain包含一个处理程序Object，而不是一个标记接口，因此处理程序不会受到任何限制。 
	 * 例如，可以编写HandlerAdapter以允许使用另一个框架的处理程序对象。
	 * 
	 * <p>Returns {@code null} if no match was found. This is not an error.
	 * The DispatcherServlet will query all registered HandlerMapping beans to find
	 * a match, and only decide there is an error if none can find a handler.
	 * 
	 * <p> 如果未找到匹配项，则返回null。 这不是错误。 DispatcherServlet将查询所有已注册的
	 * HandlerMapping bean以查找匹配项，并且只有在没有找到处理程序时才会确定存在错误。
	 * 
	 * @param request current HTTP request
	 * @return a HandlerExecutionChain instance containing handler object and
	 * any interceptors, or {@code null} if no mapping found
	 * 
	 * <p> 包含处理程序对象和任何拦截器的HandlerExecutionChain实例，如果未找到映射，则返回null
	 * 
	 * @throws Exception if there is an internal error
	 * 
	 * <p> 如果有内部错误
	 */
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
