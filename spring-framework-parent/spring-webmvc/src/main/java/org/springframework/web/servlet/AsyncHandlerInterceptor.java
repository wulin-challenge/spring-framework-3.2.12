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
 * Extends {@code HandlerInterceptor} with a callback method invoked during
 * asynchronous request handling.
 * 
 * <p> 使用在异步请求处理期间调用的回调方法扩展HandlerInterceptor。
 *
 * <p>When a handler starts asynchronous request handling, the DispatcherServlet
 * exits without invoking {@code postHandle} and {@code afterCompletion}, as it
 * normally does, since the results of request handling (e.g. ModelAndView)
 * will. be produced concurrently in another thread. In such scenarios,
 * {@link #afterConcurrentHandlingStarted(HttpServletRequest, HttpServletResponse, Object)}
 * is invoked instead allowing implementations to perform tasks such as cleaning
 * up thread bound attributes.
 * 
 * <p> 当处理程序启动异步请求处理时，DispatcherServlet退出而不像通常那样调用postHandle和afterCompletion，
 * 因为请求处理的结果（例如ModelAndView）将会。 在另一个线程中同时生成。 在这种情况下，调用
 * afterConcurrentHandlingStarted（HttpServletRequest，HttpServletResponse，Object），
 * 而不是允许实现执行诸如清理线程绑定属性之类的任务。
 *
 * <p>When asynchronous handling completes, the request is dispatched to the
 * container for further processing. At this stage the DispatcherServlet invokes
 * {@code preHandle}, {@code postHandle} and {@code afterCompletion} as usual.
 *
 * <p> 异步处理完成后，将请求分派给容器以进行进一步处理。 在这个阶段，DispatcherServlet像往常一样调用
 * preHandle，postHandle和afterCompletion。
 * 
 * @author Rossen Stoyanchev
 * @since 3.2
 *
 * @see org.springframework.web.context.request.async.WebAsyncManager
 * @see org.springframework.web.context.request.async.CallableProcessingInterceptor
 * @see org.springframework.web.context.request.async.DeferredResultProcessingInterceptor
 */
public interface AsyncHandlerInterceptor extends HandlerInterceptor {

	/**
	 * Called instead of {@code postHandle} and {@code afterCompletion}, when
	 * the a handler is being executed concurrently. Implementations may use the
	 * provided request and response but should avoid modifying them in ways
	 * that would conflict with the concurrent execution of the handler. A
	 * typical use of this method would be to clean thread local variables.
	 * 
	 * <p> 当一个处理程序同时执行时，调用而不是postHandle和afterCompletion。 实现可以使用提供的请求和响应，
	 * 但应避免以与处理程序的并发执行冲突的方式修改它们。 此方法的典型用法是清除线程局部变量。
	 *
	 * @param request the current request
	 * @param response the current response
	 * @param handler handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * 
	 * <p> 处理程序（或HandlerMethod）启动异步执行，用于类型和/或实例检查
	 * 
	 * @throws Exception in case of errors
	 */
	void afterConcurrentHandlingStarted(
			HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception;

}
