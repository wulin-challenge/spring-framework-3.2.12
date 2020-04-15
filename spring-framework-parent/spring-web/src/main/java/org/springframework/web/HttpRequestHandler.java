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

package org.springframework.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Plain handler interface for components that process HTTP requests,
 * analogous to a Servlet. Only declares {@link javax.servlet.ServletException}
 * and {@link java.io.IOException}, to allow for usage within any
 * {@link javax.servlet.http.HttpServlet}}. This interface is essentially the
 * direct equivalent of an HttpServlet, reduced to a central handle method.
 * 
 * <p> 用于处理HTTP请求的组件的普通处理程序接口，类似于Servlet。仅声明javax.servlet.ServletException和
 * java.io.IOException，以允许在任何javax.servlet.http.HttpServlet中使用}。这个接口本质上是
 * HttpServlet的直接等价物，简化为中央句柄方法。
 *
 * <p>The easiest way to expose an HttpRequestHandler bean in Spring style
 * is to define it in Spring's root web application context and define
 * an {@link org.springframework.web.context.support.HttpRequestHandlerServlet}
 * in {@code web.xml}, pointing at the target HttpRequestHandler bean
 * through its {@code servlet-name} which needs to match the target bean name.
 * 
 * <p> 在Spring样式中公开HttpRequestHandler bean的最简单方法是在Spring的根Web应用程序上下文中定义它，
 * 并在web.xml中定义org.springframework.web.context.support.HttpRequestHandlerServlet，
 * 通过其servlet指向目标HttpRequestHandler bean-需要匹配目标bean名称的名称。
 *
 * <p>Supported as a handler type within Spring's
 * {@link org.springframework.web.servlet.DispatcherServlet}, being able
 * to interact with the dispatcher's advanced mapping and interception
 * facilities. This is the recommended way of exposing an HttpRequestHandler,
 * while keeping the handler implementations free of direct dependencies
 * on a DispatcherServlet environment.
 * 
 * <p> 作为Spring的org.springframework.web.servlet.DispatcherServlet中的处理程序类型支持，
 * 能够与调度程序的高级映射和拦截工具进行交互。这是公开HttpRequestHandler的推荐方法，同时保持处理程序实现不受
 * DispatcherServlet环境的直接依赖。
 *
 * <p>Typically implemented to generate binary responses directly,
 * with no separate view resource involved. This differentiates it from a
 * {@link org.springframework.web.servlet.mvc.Controller} within Spring's Web MVC
 * framework. The lack of a {@link org.springframework.web.servlet.ModelAndView}
 * return value gives a clearer signature to callers other than the
 * DispatcherServlet, indicating that there will never be a view to render.
 * 
 * <p> 通常实现为直接生成二进制响应，不涉及单独的视图资源。这与Spring的Web MVC框架中的
 * org.springframework.web.servlet.mvc.Controller不同。缺少
 * org.springframework.web.servlet.ModelAndView返回值为DispatcherServlet以外的调用者提供了更清晰的签名，
 * 表明永远不会有要呈现的视图。
 *
 * <p>As of Spring 2.0, Spring's HTTP-based remote exporters, such as
 * {@link org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter}
 * and {@link org.springframework.remoting.caucho.HessianServiceExporter},
 * implement this interface rather than the more extensive Controller interface,
 * for minimal dependencies on Spring-specific web infrastructure.
 * 
 * <p> 从Spring 2.0开始，Spring的基于HTTP的远程导出器，例如
 * org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter和
 * org.springframework.remoting.caucho.HessianServiceExporter，实现了这个接口，
 * 而不是更广泛的Controller接口，对Spring的依赖性最小。特定的Web基础结构。
 *
 * <p>Note that HttpRequestHandlers may optionally implement the
 * {@link org.springframework.web.servlet.mvc.LastModified} interface,
 * just like Controllers can, <i>provided that they run within Spring's
 * DispatcherServlet</i>. However, this is usually not necessary, since
 * HttpRequestHandlers typically only support POST requests to begin with.
 * Alternatively, a handler may implement the "If-Modified-Since" HTTP
 * header processing manually within its {@code handle} method.
 * 
 * <p> 请注意，HttpRequestHandlers可以选择实现org.springframework.web.servlet.mvc.LastModified
 * 接口，就像Controllers一样，前提是它们在Spring的DispatcherServlet中运行。但是，这通常不是必需的，因为
 * HttpRequestHandlers通常仅支持POST请求开始。或者，处理程序可以在其handle方法中手动
 * 实现“If-Modified-Since”HTTP头处理。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.context.support.HttpRequestHandlerServlet
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.servlet.ModelAndView
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.servlet.mvc.LastModified
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 * @see org.springframework.remoting.caucho.HessianServiceExporter
 * @see org.springframework.remoting.caucho.BurlapServiceExporter
 */
public interface HttpRequestHandler {

	/**
	 * Process the given request, generating a response.
	 * 
	 * <p> 处理给定的请求，生成响应。
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws ServletException in case of general errors
	 * @throws IOException in case of I/O errors
	 */
	void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException;

}
