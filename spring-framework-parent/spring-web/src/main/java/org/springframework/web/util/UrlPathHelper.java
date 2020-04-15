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

package org.springframework.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Helper class for URL path matching. Provides support for URL paths in
 * RequestDispatcher includes and support for consistent URL decoding.
 * 
 * <p> URL路径匹配的助手类。 在RequestDispatcher中提供对URL路径的支持，包括并支持一致的URL解码。
 *
 * <p>Used by {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping},
 * {@link org.springframework.web.servlet.mvc.multiaction.AbstractUrlMethodNameResolver}
 * and {@link org.springframework.web.servlet.support.RequestContext} for path matching
 * and/or URI determination.
 * 
 * <p> 由org.springframework.web.servlet.handler.AbstractUrlHandlerMapping，
 * org.springframework.web.servlet.mvc.multiaction.AbstractUrlMethodNameResolver和
 * org.springframework.web.servlet.support.RequestContext用于路径匹配和/或URI确定。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rossen Stoyanchev
 * @since 14.01.2004
 */
public class UrlPathHelper {

	/**
	 * Special WebSphere request attribute, indicating the original request URI.
	 * Preferable over the standard Servlet 2.4 forward attribute on WebSphere,
	 * simply because we need the very first URI in the request forwarding chain.
	 * 
	 * <p> 特殊的WebSphere请求属性，指示原始请求URI。 优先于WebSphere上的标准Servlet 2.4 forward属性，
	 * 仅仅因为我们需要请求转发链中的第一个URI。
	 * 
	 */
	private static final String WEBSPHERE_URI_ATTRIBUTE = "com.ibm.websphere.servlet.uri_non_decoded";

	private static final Log logger = LogFactory.getLog(UrlPathHelper.class);

	static volatile Boolean websphereComplianceFlag;


	private boolean alwaysUseFullPath = false;

	private boolean urlDecode = true;

	private boolean removeSemicolonContent = true;

	private String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;


	/**
	 * Set if URL lookup should always use full path within current servlet
	 * context. Else, the path within the current servlet mapping is used
	 * if applicable (i.e. in the case of a ".../*" servlet mapping in web.xml).
	 * Default is "false".
	 * 
	 * <p> 设置URL查找是否应始终使用当前servlet上下文中的完整路径。 否则，如果适用，则使用当前servlet映射中的路径
	 * （即，在web.xml中的“... / *”servlet映射的情况下）。 默认为“false”。
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.alwaysUseFullPath = alwaysUseFullPath;
	}

	/**
	 * Set if context path and request URI should be URL-decoded.
	 * Both are returned <i>undecoded</i> by the Servlet API,
	 * in contrast to the servlet path.
	 * 
	 * <p> 设置是否应对上下文路径和请求URI进行URL解码。 与servlet路径相比，Servlet API都返回未编码的两者。
	 * 
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * 
	 * <p> 根据Servlet规范（ISO-8859-1）使用请求编码或默认编码。
	 * 
	 * <p>Default is "true", as of Spring 2.5.
	 * 
	 * <p> 从Spring 2.5开始，默认值为“true”。
	 * 
	 * @see #getServletPath
	 * @see #getContextPath
	 * @see #getRequestUri
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 * @see java.net.URLDecoder#decode(String, String)
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlDecode = urlDecode;
	}

	/**
	 * Set if ";" (semicolon) content should be stripped from the request URI.
	 * 
	 * <p> 设置是否“;” （分号）内容应从请求URI中删除。
	 * 
	 * <p>Default is "true".
	 * 
	 * <p> 默认为“true”。
	 */
	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		this.removeSemicolonContent = removeSemicolonContent;
	}

	/**
	 * Whether configured to remove ";" (semicolon) content from the request URI.
	 * 
	 * <p> 是否配置为删除“;” 来自请求URI的（分号）内容。
	 */
	public boolean shouldRemoveSemicolonContent() {
		return this.removeSemicolonContent;
	}

	/**
	 * Set the default character encoding to use for URL decoding.
	 * Default is ISO-8859-1, according to the Servlet spec.
	 * 
	 * <p> 设置用于URL解码的默认字符编码。 根据Servlet规范，默认值为ISO-8859-1。
	 * 
	 * <p>If the request specifies a character encoding itself, the request
	 * encoding will override this setting. This also allows for generically
	 * overriding the character encoding in a filter that invokes the
	 * {@code ServletRequest.setCharacterEncoding} method.
	 * 
	 * <p> 如果请求指定了自身的字符编码，请求编码将覆盖此设置。 这也允许在调用ServletRequest.setCharacterEncoding
	 * 方法的过滤器中一般覆盖字符编码。
	 * 
	 * @param defaultEncoding the character encoding to use
	 * 
	 * <p> 要使用的字符编码
	 * 
	 * @see #determineEncoding
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(String)
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Return the default character encoding to use for URL decoding.
	 * 
	 * <p> 返回用于URL解码的默认字符编码。
	 */
	protected String getDefaultEncoding() {
		return this.defaultEncoding;
	}


	/**
	 * Return the mapping lookup path for the given request, within the current
	 * servlet mapping if applicable, else within the web application.
	 * 
	 * <p> 返回给定请求的映射查找路径，如果适用，则返回当前servlet映射，否则返回Web应用程序中。
	 * 
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * 
	 * <p> 如果在RequestDispatcher include中调用，则检测包括请求URL。
	 * 
	 * @param request current HTTP request
	 * @return the lookup path - 查找路径
	 * @see #getPathWithinApplication
	 * @see #getPathWithinServletMapping
	 */
	public String getLookupPathForRequest(HttpServletRequest request) {
		// Always use full path within current servlet context?
		// 始终在当前servlet上下文中使用完整路径？
		if (this.alwaysUseFullPath) {
			return getPathWithinApplication(request);
		}
		// Else, use path within current servlet mapping if applicable
		// 否则，如果适用，请在当前servlet映射中使用path
		String rest = getPathWithinServletMapping(request);
		if (!"".equals(rest)) {
			return rest;
		}
		else {
			return getPathWithinApplication(request);
		}
	}

	/**
	 * Return the path within the servlet mapping for the given request,
	 * i.e. the part of the request's URL beyond the part that called the servlet,
	 * or "" if the whole URL has been used to identify the servlet.
	 * 
	 * <p> 返回给定请求的servlet映射中的路径，即请求URL的一部分超出调用servlet的部分，如果整个URL已用于标识servlet，则返回“”。
	 * 
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * 
	 * <p> 如果在RequestDispatcher include中调用，则检测包括请求URL。
	 * 
	 * <p>E.g.: servlet mapping = "/test/*"; request URI = "/test/a" -> "/a".
	 * 
	 * <p> 例如：servlet mapping =“/ test / *”; 请求URI =“/ test / a” - >“/ a”。
	 * 
	 * <p>E.g.: servlet mapping = "/test"; request URI = "/test" -> "".
	 * 
	 * <p> 例如：servlet mapping =“/ test”; 请求URI =“/ test” - >“”。
	 * 
	 * <p>E.g.: servlet mapping = "/*.test"; request URI = "/a.test" -> "".
	 * 
	 * <p> 例如：servlet mapping =“/ *。test”; 请求URI =“/ a.test” - >“”。
	 * 
	 * @param request current HTTP request
	 * @return the path within the servlet mapping, or ""
	 * 
	 * <p> servlet映射中的路径，或“”
	 */
	public String getPathWithinServletMapping(HttpServletRequest request) {
		String pathWithinApp = getPathWithinApplication(request);
		String servletPath = getServletPath(request);
		String path = getRemainingPath(pathWithinApp, servletPath, false);
		if (path != null) {
			// Normal case: URI contains servlet path.
			// 正常情况：URI包含servlet路径。
			return path;
		}
		else {
			// Special case: URI is different from servlet path.
			// 特殊情况：URI与servlet路径不同。
			String pathInfo = request.getPathInfo();
			if (pathInfo != null) {
				// Use path info if available. Indicates index page within a servlet mapping?
				// e.g. with index page: URI="/", servletPath="/index.html"
				// 使用路径信息（如果有）。 表示servlet映射中的索引页面？ 例如 索引页面：URI =“/”，servletPath =“/ index.html”
				return pathInfo;
			}
			if (!this.urlDecode) {
				// No path info... (not mapped by prefix, nor by extension, nor "/*")
				// For the default servlet mapping (i.e. "/"), urlDecode=false can
				// cause issues since getServletPath() returns a decoded path.
				// If decoding pathWithinApp yields a match just use pathWithinApp.
				
				/*
				 * 没有路径信息...（不是通过前缀，也不是通过扩展，也不是“/ *”映射）对于默认的servlet映射（即“/”），
				 * urlDecode = false可能会导致问题，因为getServletPath（）返回解码的路径。 
				 * 如果解码pathWithinApp产生匹配，则只使用pathWithinApp。
				 */
				path = getRemainingPath(decodeInternal(request, pathWithinApp), servletPath, false);
				if (path != null) {
					return pathWithinApp;
				}
			}
			// Otherwise, use the full servlet path.
			// 否则，请使用完整的servlet路径。
			return servletPath;
		}
	}

	/**
	 * Return the path within the web application for the given request.
	 * 
	 * <p> 返回Web应用程序中针对给定请求的路径。
	 * 
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * 
	 * <p> 如果在RequestDispatcher include中调用，则检测包括请求URL。
	 * 
	 * @param request current HTTP request
	 * @return the path within the web application
	 * 
	 * <p> Web应用程序中的路径
	 */
	public String getPathWithinApplication(HttpServletRequest request) {
		String contextPath = getContextPath(request);
		String requestUri = getRequestUri(request);
		String path = getRemainingPath(requestUri, contextPath, true);
		if (path != null) {
			// Normal case: URI contains context path.
			// 正常情况：URI包含上下文路径。
			return (StringUtils.hasText(path) ? path : "/");
		}
		else {
			return requestUri;
		}
	}

	/**
	 * Match the given "mapping" to the start of the "requestUri" and if there
	 * is a match return the extra part. This method is needed because the
	 * context path and the servlet path returned by the HttpServletRequest are
	 * stripped of semicolon content unlike the requesUri.
	 * 
	 * <p> 将给定的“映射”与“requestUri”的开头匹配，如果匹配则返回额外的部分。 需要此方法，因为与requesUri不同，
	 * HttpServletRequest返回的上下文路径和servlet路径被剥去分号内容。
	 */
	private String getRemainingPath(String requestUri, String mapping, boolean ignoreCase) {
		int index1 = 0;
		int index2 = 0;
		for (; (index1 < requestUri.length()) && (index2 < mapping.length()); index1++, index2++) {
			char c1 = requestUri.charAt(index1);
			char c2 = mapping.charAt(index2);
			if (c1 == ';') {
				index1 = requestUri.indexOf('/', index1);
				if (index1 == -1) {
					return null;
				}
				c1 = requestUri.charAt(index1);
			}
			if (c1 == c2) {
				continue;
			}
			if (ignoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2))) {
				continue;
			}
			return null;
		}
		if (index2 != mapping.length()) {
			return null;
		}
		if (index1 == requestUri.length()) {
			return "";
		}
		else if (requestUri.charAt(index1) == ';') {
			index1 = requestUri.indexOf('/', index1);
		}
		return (index1 != -1 ? requestUri.substring(index1) : "");
	}

	/**
	 * Return the request URI for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 * 
	 * <p> 返回给定请求的请求URI，如果在RequestDispatcher include中调用，则检测包含请求URL。
	 * 
	 * <p>As the value returned by {@code request.getRequestURI()} is <i>not</i>
	 * decoded by the servlet container, this method will decode it.
	 * 
	 * <p> 由于servlet容器未解码request.getRequestURI（）返回的值，因此该方法将对其进行解码。
	 * 
	 * <p>The URI that the web container resolves <i>should</i> be correct, but some
	 * containers like JBoss/Jetty incorrectly include ";" strings like ";jsessionid"
	 * in the URI. This method cuts off such incorrect appendices.
	 * 
	 * <p> Web容器解析的URI应该是正确的，但是像JBoss / Jetty这样的容器错误地包含“;” URI中的字符串如“; 
	 * jsessionid”。 这种方法切断了这些不正确的附录。
	 * 
	 * @param request current HTTP request
	 * @return the request URI
	 */
	public String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return decodeAndCleanUriString(request, uri);
	}

	/**
	 * Return the context path for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 * 
	 * <p> 返回给定请求的上下文路径，如果在RequestDispatcher include中调用，则检测包含请求URL。
	 * 
	 * <p>As the value returned by {@code request.getContextPath()} is <i>not</i>
	 * decoded by the servlet container, this method will decode it.
	 * 
	 * <p> 由于servlet容器未解码request.getContextPath（）返回的值，因此该方法将对其进行解码。
	 * 
	 * @param request current HTTP request
	 * @return the context path
	 */
	public String getContextPath(HttpServletRequest request) {
		String contextPath = (String) request.getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
		if (contextPath == null) {
			contextPath = request.getContextPath();
		}
		if ("/".equals(contextPath)) {
			// Invalid case, but happens for includes on Jetty: silently adapt it.
			// 无效的情况，但发生在Jetty上的包含：默默地适应它。
			contextPath = "";
		}
		return decodeRequestString(request, contextPath);
	}

	/**
	 * Return the servlet path for the given request, regarding an include request
	 * URL if called within a RequestDispatcher include.
	 * 
	 * <p> 如果在RequestDispatcher include中调用了include请求URL，则返回给定请求的servlet路径。
	 * 
	 * <p>As the value returned by {@code request.getServletPath()} is already
	 * decoded by the servlet container, this method will not attempt to decode it.
	 * 
	 * <p> 由于request.getServletPath（）返回的值已由servlet容器解码，因此此方法不会尝试对其进行解码。
	 * 
	 * @param request current HTTP request
	 * @return the servlet path
	 */
	public String getServletPath(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		if (servletPath.length() > 1 && servletPath.endsWith("/") && shouldRemoveTrailingServletPathSlash(request)) {
			// On WebSphere, in non-compliant mode, for a "/foo/" case that would be "/foo"
			// on all other servlet containers: removing trailing slash, proceeding with
			// that remaining slash as final lookup path...
			
			/*
			 * 在WebSphere上，在非兼容模式下，对于在所有其他servlet容器上为“/ foo”的“/ foo /”情况：
			 * 删除尾部斜杠，继续使用剩余的斜杠作为最终查找路径...
			 */
			servletPath = servletPath.substring(0, servletPath.length() - 1);
		}
		return servletPath;
	}


	/**
	 * Return the request URI for the given request. If this is a forwarded request,
	 * correctly resolves to the request URI of the original request.
	 * 
	 * <p> 返回给定请求的请求URI。 如果这是转发请求，请正确解析为原始请求的请求URI。
	 */
	public String getOriginatingRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WEBSPHERE_URI_ATTRIBUTE);
		if (uri == null) {
			uri = (String) request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
			if (uri == null) {
				uri = request.getRequestURI();
			}
		}
		return decodeAndCleanUriString(request, uri);
	}

	/**
	 * Return the context path for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 * 
	 * <p> 返回给定请求的上下文路径，如果在RequestDispatcher中包含中调用，则检测包含请求URL。
	 * 
	 * <p>As the value returned by {@code request.getContextPath()} is <i>not</i>
	 * decoded by the servlet container, this method will decode it.
	 * 
	 * <p> 由于小服务程序容器未解码request.getContextPath（）返回的值，因此该方法将对其进行解码。
	 * 
	 * @param request current HTTP request
	 * @return the context path
	 */
	public String getOriginatingContextPath(HttpServletRequest request) {
		String contextPath = (String) request.getAttribute(WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE);
		if (contextPath == null) {
			contextPath = request.getContextPath();
		}
		return decodeRequestString(request, contextPath);
	}

	/**
	 * Return the servlet path for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 * 
	 * <p> 返回给定请求的servlet路径，如果在RequestDispatcher include中调用，则检测包含请求URL。
	 * 
	 * @param request current HTTP request
	 * @return the servlet path
	 */
	public String getOriginatingServletPath(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(WebUtils.FORWARD_SERVLET_PATH_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		return servletPath;
	}

	/**
	 * Return the query string part of the given request's URL. If this is a forwarded request,
	 * correctly resolves to the query string of the original request.
	 * 
	 * <p> 返回给定请求的URL的查询字符串部分。 如果这是转发请求，请正确解析为原始请求的查询字符串。
	 * 
	 * @param request current HTTP request
	 * @return the query string
	 */
	public String getOriginatingQueryString(HttpServletRequest request) {
		if ((request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE) != null) ||
			(request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE) != null)) {
			return (String) request.getAttribute(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE);
		}
		else {
			return request.getQueryString();
		}
	}

	/**
	 * Decode the supplied URI string and strips any extraneous portion after a ';'.
	 * 
	 * <p> 解码提供的URI字符串并删除';'后的任何无关部分。
	 * 
	 */
	private String decodeAndCleanUriString(HttpServletRequest request, String uri) {
		uri = removeSemicolonContent(uri);
		uri = decodeRequestString(request, uri);
		return uri;
	}

	/**
	 * Decode the given source string with a URLDecoder. The encoding will be taken
	 * from the request, falling back to the default "ISO-8859-1".
	 * 
	 * <p> 使用URLDecoder解码给定的源字符串。 编码将从请求中获取，然后回退到默认的“ISO-8859-1”。
	 * 
	 * <p>The default implementation uses {@code URLDecoder.decode(input, enc)}.
	 * 
	 * <p> 默认实现使用URLDecoder.decode（input，enc）。
	 * 
	 * @param request current HTTP request
	 * @param source the String to decode - 要解码的字符串
	 * @return the decoded String
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see java.net.URLDecoder#decode(String, String)
	 * @see java.net.URLDecoder#decode(String)
	 */
	public String decodeRequestString(HttpServletRequest request, String source) {
		if (this.urlDecode) {
			return decodeInternal(request, source);
		}
		return source;
	}

	@SuppressWarnings("deprecation")
	private String decodeInternal(HttpServletRequest request, String source) {
		String enc = determineEncoding(request);
		try {
			return UriUtils.decode(source, enc);
		}
		catch (UnsupportedEncodingException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not decode request string [" + source + "] with encoding '" + enc +
						"': falling back to platform default encoding; exception message: " + ex.getMessage());
			}
			return URLDecoder.decode(source);
		}
	}

	/**
	 * Determine the encoding for the given request.
	 * Can be overridden in subclasses.
	 * 
	 * <p> 确定给定请求的编码。 可以在子类中重写。
	 * 
	 * <p>The default implementation checks the request encoding,
	 * falling back to the default encoding specified for this resolver.
	 * 
	 * <p> 默认实现检查请求编码，然后回退到为此解析程序指定的默认编码。
	 * 
	 * @param request current HTTP request
	 * @return the encoding for the request (never {@code null})
	 * 
	 * <p> 请求的编码（永远不为null）
	 * 
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 * @see #setDefaultEncoding
	 */
	protected String determineEncoding(HttpServletRequest request) {
		String enc = request.getCharacterEncoding();
		if (enc == null) {
			enc = getDefaultEncoding();
		}
		return enc;
	}

	/**
	 * Remove ";" (semicolon) content from the given request URI if the
	 * {@linkplain #setRemoveSemicolonContent(boolean) removeSemicolonContent}
	 * property is set to "true". Note that "jssessionid" is always removed.
	 * 
	 * <p> 去掉 ”;” 如果removeSemicolonContent属性设置为“true”，则来自给定请求URI的（分号）内容。 
	 * 请注意，“jssessionid”始终被删除。
	 * 
	 * @param requestUri the request URI string to remove ";" content from
	 * 
	 * <p> 要删除的请求URI字符串“;” 来自瑞士的内容：
	 * 
	 * @return the updated URI string - 更新的URI字符串
	 */
	public String removeSemicolonContent(String requestUri) {
		return this.removeSemicolonContent ?
				removeSemicolonContentInternal(requestUri) : removeJsessionid(requestUri);
	}

	private String removeSemicolonContentInternal(String requestUri) {
		int semicolonIndex = requestUri.indexOf(';');
		while (semicolonIndex != -1) {
			int slashIndex = requestUri.indexOf('/', semicolonIndex);
			String start = requestUri.substring(0, semicolonIndex);
			requestUri = (slashIndex != -1) ? start + requestUri.substring(slashIndex) : start;
			semicolonIndex = requestUri.indexOf(';', semicolonIndex);
		}
		return requestUri;
	}

	private String removeJsessionid(String requestUri) {
		int startIndex = requestUri.toLowerCase().indexOf(";jsessionid=");
		if (startIndex != -1) {
			int endIndex = requestUri.indexOf(';', startIndex + 12);
			String start = requestUri.substring(0, startIndex);
			requestUri = (endIndex != -1) ? start + requestUri.substring(endIndex) : start;
		}
		return requestUri;
	}

	/**
	 * Decode the given URI path variables via
	 * {@link #decodeRequestString(HttpServletRequest, String)} unless
	 * {@link #setUrlDecode(boolean)} is set to {@code true} in which case it is
	 * assumed the URL path from which the variables were extracted is already
	 * decoded through a call to
	 * {@link #getLookupPathForRequest(HttpServletRequest)}.
	 * 
	 * <p> 通过decodeRequestString（HttpServletRequest，String）解码给定的URI路径变量，除非
	 * setUrlDecode（boolean）设置为true，在这种情况下，假定从中提取变量的URL路径已经通过调用
	 * getLookupPathForRequest（HttpServletRequest）进行解码。
	 * 
	 * @param request current HTTP request
	 * @param vars URI variables extracted from the URL path
	 * 
	 * <p> 从URL路径中提取的URI变量
	 * 
	 * @return the same Map or a new Map instance
	 * 
	 * <p> 相同的Map或新的Map实例
	 */
	public Map<String, String> decodePathVariables(HttpServletRequest request, Map<String, String> vars) {
		if (this.urlDecode) {
			return vars;
		}
		else {
			Map<String, String> decodedVars = new LinkedHashMap<String, String>(vars.size());
			for (Entry<String, String> entry : vars.entrySet()) {
				decodedVars.put(entry.getKey(), decodeInternal(request, entry.getValue()));
			}
			return decodedVars;
		}
	}

	/**
	 * Decode the given matrix variables via
	 * {@link #decodeRequestString(HttpServletRequest, String)} unless
	 * {@link #setUrlDecode(boolean)} is set to {@code true} in which case it is
	 * assumed the URL path from which the variables were extracted is already
	 * decoded through a call to
	 * {@link #getLookupPathForRequest(HttpServletRequest)}.
	 * 
	 * <p> 通过decodeRequestString（HttpServletRequest，String）解码给定的矩阵变量，除非
	 * setUrlDecode（boolean）设置为true，在这种情况下，假定从中提取变量的URL路径已经通过调用
	 * getLookupPathForRequest（HttpServletRequest）进行解码。
	 * 
	 * @param request current HTTP request
	 * @param vars URI variables extracted from the URL path
	 * 
	 * <p> 从URL路径中提取的URI变量
	 * 
	 * @return the same Map or a new Map instance
	 * 
	 * <p> 相同的Map或新的Map实例
	 * 
	 */
	public MultiValueMap<String, String> decodeMatrixVariables(HttpServletRequest request, MultiValueMap<String, String> vars) {
		if (this.urlDecode) {
			return vars;
		}
		else {
			MultiValueMap<String, String> decodedVars = new LinkedMultiValueMap	<String, String>(vars.size());
			for (String key : vars.keySet()) {
				for (String value : vars.get(key)) {
					decodedVars.add(key, decodeInternal(request, value));
				}
			}
			return decodedVars;
		}
	}

	private boolean shouldRemoveTrailingServletPathSlash(HttpServletRequest request) {
		if (request.getAttribute(WEBSPHERE_URI_ATTRIBUTE) == null) {
			// Regular servlet container: behaves as expected in any case,
			// so the trailing slash is the result of a "/" url-pattern mapping.
			// Don't remove that slash.
			
			// 常规servlet容器：在任何情况下都按预期运行，因此尾部斜杠是“/”url-pattern映射的结果。 不要删除斜杠。
			return false;
		}
		if (websphereComplianceFlag == null) {
			ClassLoader classLoader = UrlPathHelper.class.getClassLoader();
			String className = "com.ibm.ws.webcontainer.WebContainer";
			String methodName = "getWebContainerProperties";
			String propName = "com.ibm.ws.webcontainer.removetrailingservletpathslash";
			boolean flag = false;
			try {
				Class<?> cl = classLoader.loadClass(className);
				Properties prop = (Properties) cl.getMethod(methodName).invoke(null);
				flag = Boolean.parseBoolean(prop.getProperty(propName));
			}
			catch (Throwable ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not introspect WebSphere web container properties: " + ex);
				}
			}
			websphereComplianceFlag = flag;
		}
		// Don't bother if WebSphere is configured to be fully Servlet compliant.
		// However, if it is not compliant, do remove the improper trailing slash!
		// 如果WebSphere配置为完全符合Servlet，请不要理会。 但是，如果它不符合要求，请删除不正确的尾部斜杠！
		return !websphereComplianceFlag;
	}

}
