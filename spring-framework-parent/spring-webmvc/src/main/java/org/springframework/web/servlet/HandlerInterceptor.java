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
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;

/**
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 * 
 * <p> 允许自定义处理程序执行链的工作流接口。应用程序可以为某些处理程序组注册任意数量的现有或自定义拦截器，
 * 以添加常见的预处理行为，而无需修改每个处理程序实现。
 *
 * <p>A HandlerInterceptor gets called before the appropriate HandlerAdapter
 * triggers the execution of the handler itself. This mechanism can be used
 * for a large field of preprocessing aspects, e.g. for authorization checks,
 * or common handler behavior like locale or theme changes. Its main purpose
 * is to allow for factoring out repetitive handler code.
 * 
 * <p> 在适当的HandlerAdapter触发处理程序本身的执行之前调用HandlerInterceptor。该机制可用于大范围的预处理方面，
 * 例如，用于授权检查，或常见的处理程序行为，如区域设置或主题更改。其主要目的是允许分解重复的处理程序代码。
 *
 * <p>In an async processing scenario, the handler may be executed in a separate
 * thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and details see
 * {@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 * 
 * <p> 在异步处理场景中，处理程序可以在单独的线程中执行，而主线程退出而不渲染或调用postHandle和afterCompletion回调。
 * 并发处理程序执行完成后，将调度该请求以继续呈现模型，并再次调用此合同的所有方法。有关更多选项和详细信息，
 * 请参阅org.springframework.web.servlet.AsyncHandlerInterceptor
 *
 * <p>Typically an interceptor chain is defined per HandlerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of handlers, one needs to map the desired handlers via one
 * HandlerMapping bean. The interceptors themselves are defined as beans
 * in the application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 * 
 * <p> 通常，每个HandlerMapping bean定义一个拦截器链，共享其粒度。为了能够将某个拦截器链应用于一组处理程序，
 * 需要通过一个HandlerMapping bean映射所需的处理程序。拦截器本身在应用程序上下文中定义为bean，
 * 由映射bean定义通过其“拦截器”属性引用（在XML中：<list> of <ref>）。
 *
 * <p>HandlerInterceptor is basically similar to a Servlet 2.3 Filter, but in
 * contrast to the latter it just allows custom pre-processing with the option
 * of prohibiting the execution of the handler itself, and custom post-processing.
 * Filters are more powerful, for example they allow for exchanging the request
 * and response objects that are handed down the chain. Note that a filter
 * gets configured in web.xml, a HandlerInterceptor in the application context.
 * 
 * <p> HandlerInterceptor基本上类似于Servlet 2.3 Filter，但与后者相反，它只允许自定义预处理，禁止执行处理程序本身，
 * 以及自定义后处理。过滤器功能更强大，例如，它们允许交换传递链中的请求和响应对象。请注意，过滤器在web.xml中配置，
 * web.xml是应用程序上下文中的HandlerInterceptor。
 *
 * <p>As a basic guideline, fine-grained handler-related preprocessing tasks are
 * candidates for HandlerInterceptor implementations, especially factored-out
 * common handler code and authorization checks. On the other hand, a Filter
 * is well-suited for request content and view content handling, like multipart
 * forms and GZIP compression. This typically shows when one needs to map the
 * filter to certain content types (e.g. images), or to all requests.
 * 
 * <p> 作为基本准则，细粒度处理程序相关的预处理任务是HandlerInterceptor实现的候选者，尤其是分解出来的公共处理程序代码和授权检查。
 * 另一方面，过滤器非常适合请求内容和视图内容处理，如多部分表单和GZIP压缩。这通常表示何时需要将过滤器映射到某些内容类型（例如图像）或所有请求。
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerExecutionChain#getInterceptors
 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor
 * @see org.springframework.web.servlet.i18n.LocaleChangeInterceptor
 * @see org.springframework.web.servlet.theme.ThemeChangeInterceptor
 * @see javax.servlet.Filter
 */
public interface HandlerInterceptor {

	/**
	 * Intercept the execution of a handler. Called after HandlerMapping determined
	 * an appropriate handler object, but before HandlerAdapter invokes the handler.
	 * 
	 * <p> 拦截处理程序的执行。 在HandlerMapping确定适当的处理程序对象之后调用，但在HandlerAdapter调用处理程序之前。
	 * 
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically sending a HTTP error or writing a custom response.
	 * 
	 * <p> DispatcherServlet处理执行链中的处理程序，该处理程序由任意数量的拦截器组成，最后处理程序本身。 使用此方法，
	 * 每个拦截器都可以决定中止执行链，通常发送HTTP错误或编写自定义响应。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * 
	 * <p> 选择要执行的处理程序，用于类型和/或实例评估
	 * 
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * 
	 * <p> 如果执行链应继续执行下一个拦截器或处理程序本身，则为true。 否则，DispatcherServlet假定这个拦截器已经处理了响应本身。
	 * 
	 * @throws Exception in case of errors
	 */
	boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws Exception;

	/**
	 * Intercept the execution of a handler. Called after HandlerAdapter actually
	 * invoked the handler, but before the DispatcherServlet renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 * 
	 * <p> 拦截处理程序的执行。 在HandlerAdapter实际调用处理程序之后调用，但在DispatcherServlet呈现视图之前调用。 
	 * 可以通过给定的ModelAndView将其他模型对象暴露给视图。
	 * 
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can post-process an execution,
	 * getting applied in inverse order of the execution chain.
	 * 
	 * <p> DispatcherServlet处理执行链中的处理程序，该处理程序由任意数量的拦截器组成，最后处理程序本身。 使用此方法，
	 * 每个拦截器都可以对执行进行后处理，并按执行链的逆序进行应用。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * 
	 * <p> 处理程序（或HandlerMethod）启动异步执行，用于类型和/或实例检查
	 * 
	 * @param modelAndView the {@code ModelAndView} that the handler returned
	 * (can also be {@code null})
	 * 
	 * <p> 处理程序返回的ModelAndView（也可以为null）
	 * 
	 * @throws Exception in case of errors
	 */
	void postHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception;

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * 
	 * <p> 完成请求处理后回调，即渲染视图后回调。 将调用处理程序执行的任何结果，从而允许适当的资源清理。
	 * 
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed and returned {@code true}!
	 * 
	 * <p> 注意：只有在拦截器的preHandle方法成功完成并返回true时才会被调用！
	 * 
	 * <p>As with the {@code postHandle} method, the method will be invoked on each
	 * interceptor in the chain in reverse order, so the first interceptor will be
	 * the last to be invoked.
	 * 
	 * <p> 与postHandle方法一样，该方法将以相反的顺序在链中的每个拦截器上调用，因此第一个拦截器将是最后一个被调用的拦截器。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * 
	 * <p> 处理程序（或HandlerMethod）启动异步执行，用于类型和/或实例检查
	 * 
	 * @param ex exception thrown on handler execution, if any
	 * 
	 * <p> 处理程序执行时抛出的异常，如果有的话
	 * 
	 * @throws Exception in case of errors
	 */
	void afterCompletion(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception;

}
