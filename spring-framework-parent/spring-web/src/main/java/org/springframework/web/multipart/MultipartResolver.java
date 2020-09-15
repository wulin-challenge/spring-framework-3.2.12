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

package org.springframework.web.multipart;

import javax.servlet.http.HttpServletRequest;

/**
 * A strategy interface for multipart file upload resolution in accordance
 * with <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * Implementations are typically usable both within an application context
 * and standalone.
 * 
 * <p> 符合RFC 1867的用于多部分文件上传解析的策略接口。实现通常可在应用程序上下文中使用，也可独立使用。
 *
 * <p>There are two concrete implementations included in Spring, as of Spring 3.1:
 * 
 * <p> 从Spring 3.1开始，Spring中包含两个具体的实现：
 * 
 * <ul>
 * <li>{@link org.springframework.web.multipart.commons.CommonsMultipartResolver} for Jakarta Commons FileUpload
 * <li>{@link org.springframework.web.multipart.support.StandardServletMultipartResolver} for Servlet 3.0 Part API
 * </ul>
 *
 * <p>There is no default resolver implementation used for Spring
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets},
 * as an application might choose to parse its multipart requests itself. To define
 * an implementation, create a bean with the id "multipartResolver" in a
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet's}
 * application context. Such a resolver gets applied to all requests handled
 * by that {@link org.springframework.web.servlet.DispatcherServlet}.
 * 
 * <p> Spring DispatcherServlets没有默认的解析器实现，因为应用程序可能会选择自己解析其多部分请求。要定义实现，
 * 请在DispatcherServlet的应用程序上下文中创建一个ID为“ multipartResolver”的bean。
 * 这样的解析器将应用于该org.springframework.web.servlet.DispatcherServlet处理的所有请求。
 *
 * <p>If a {@link org.springframework.web.servlet.DispatcherServlet} detects
 * a multipart request, it will resolve it via the configured
 * {@link MultipartResolver} and pass on a
 * wrapped {@link javax.servlet.http.HttpServletRequest}.
 * Controllers can then cast their given request to the
 * {@link MultipartHttpServletRequest}
 * interface, which permits access to any
 * {@link MultipartFile MultipartFiles}.
 * Note that this cast is only supported in case of an actual multipart request.
 * 
 * <p> 如果org.springframework.web.servlet.DispatcherServlet检测到多部分请求，它将通过配置的
 * MultipartResolver对其进行解析，并传递包装的javax.servlet.http.HttpServletRequest。然后，
 * 控制器可以将给定的请求转换为MultipartHttpServletRequest接口，该接口允许访问任何MultipartFiles。
 * 请注意，仅在实际的多部分请求的情况下才支持此转换。
 *
 * <pre class="code">
 * public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   MultipartFile multipartFile = multipartRequest.getFile("image");
 *   ...
 * }</pre>
 *
 * Instead of direct access, command or form controllers can register a
 * {@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor}
 * or {@link org.springframework.web.multipart.support.StringMultipartFileEditor}
 * with their data binder, to automatically apply multipart content to command
 * bean properties.
 * 
 * <p> 代替直接访问，命令或表单控制器可以使用其数据绑定器注册
 * org.springframework.web.multipart.support.ByteArrayMultipartFileEditor或
 * org.springframework.web.multipart.support.StringMultipartFileEditor，
 * 以将多部分内容自动应用于Command Bean属性。
 *
 * <p>As an alternative to using a
 * {@link MultipartResolver} with a
 * {@link org.springframework.web.servlet.DispatcherServlet},
 * a {@link org.springframework.web.multipart.support.MultipartFilter} can be
 * registered in {@code web.xml}. It will delegate to a corresponding
 * {@link MultipartResolver} bean in the root
 * application context. This is mainly intended for applications that do not
 * use Spring's own web MVC framework.
 * 
 * <p> 作为将MultipartResolver与org.springframework.web.servlet.DispatcherServlet
 * 结合使用的替代方法，可以在web.xml中注册org.springframework.web.multipart.support.MultipartFilter。
 *  它将委派给根应用程序上下文中的相应MultipartResolver bean。 这主要用于不使用Spring自己的Web MVC框架的应用程序。
 *
 * <p>Note: There is hardly ever a need to access the
 * {@link MultipartResolver} itself
 * from application code. It will simply do its work behind the scenes,
 * making
 * {@link MultipartHttpServletRequest MultipartHttpServletRequests}
 * available to controllers.
 * 
 * <p> 注意：几乎不需要从应用程序代码访问MultipartResolver本身。 它将简单地在后台进行工作，
 * 从而使MultipartHttpServletRequests可用于控制器。
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartHttpServletRequest
 * @see MultipartFile
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.springframework.web.multipart.support.ByteArrayMultipartFileEditor
 * @see org.springframework.web.multipart.support.StringMultipartFileEditor
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public interface MultipartResolver {

	/**
	 * Determine if the given request contains multipart content.
	 * 
	 * <p> 确定给定的请求是否包含多部分内容。
	 * 
	 * <p>Will typically check for content type "multipart/form-data", but the actually
	 * accepted requests might depend on the capabilities of the resolver implementation.
	 * 
	 * <p> 通常将检查内容类型“ multipart / form-data”，但实际接受的请求可能取决于解析程序实现的功能。
	 * 
	 * @param request the servlet request to be evaluated - 要评估的servlet请求
	 * @return whether the request contains multipart content
	 * 
	 * <p> 该请求包含多部分内容
	 * 
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * Parse the given HTTP request into multipart files and parameters,
	 * and wrap the request inside a
	 * {@link org.springframework.web.multipart.MultipartHttpServletRequest} object
	 * that provides access to file descriptors and makes contained
	 * parameters accessible via the standard ServletRequest methods.
	 * 
	 * <p> 将给定的HTTP请求解析为多部分文件和参数，并将请求包装在
	 * org.springframework.web.multipart.MultipartHttpServletRequest对象中，
	 * 该对象提供对文件描述符的访问，并使包含的参数可通过标准ServletRequest方法进行访问。
	 * 
	 * @param request the servlet request to wrap (must be of a multipart content type)
	 * 
	 * <p> 包装的servlet请求（必须为多部分内容类型）
	 * 
	 * @return the wrapped servlet request - 包装的servlet请求
	 * 
	 * @throws MultipartException if the servlet request is not multipart, or if
	 * implementation-specific problems are encountered (such as exceeding file size limits)
	 * 
	 * <p> 如果Servlet请求不是多部分的，或者遇到特定于实现的问题（例如超出文件大小限制）
	 * 
	 * @see MultipartHttpServletRequest#getFile
	 * @see MultipartHttpServletRequest#getFileNames
	 * @see MultipartHttpServletRequest#getFileMap
	 * @see javax.servlet.http.HttpServletRequest#getParameter
	 * @see javax.servlet.http.HttpServletRequest#getParameterNames
	 * @see javax.servlet.http.HttpServletRequest#getParameterMap
	 */
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * Cleanup any resources used for the multipart handling,
	 * like a storage for the uploaded files.
	 * 
	 * <p> 清理用于多部分处理的所有资源，例如上载文件的存储。
	 * 
	 * @param request the request to cleanup resources for
	 * 
	 * <p> 清理资源的请求
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}
