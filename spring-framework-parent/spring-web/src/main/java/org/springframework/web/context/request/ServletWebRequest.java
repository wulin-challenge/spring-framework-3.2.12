/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.context.request;

import java.security.Principal;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * {@link WebRequest} adapter for an {@link javax.servlet.http.HttpServletRequest}.
 * 
 * <p> 用于javax.servlet.http.HttpServletRequest的WebRequest适配器。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ServletWebRequest extends ServletRequestAttributes implements NativeWebRequest {

	private static final String HEADER_ETAG = "ETag";

	private static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

	private static final String HEADER_IF_NONE_MATCH = "If-None-Match";

	private static final String HEADER_LAST_MODIFIED = "Last-Modified";

	private static final String METHOD_GET = "GET";

	private static final String METHOD_HEAD = "HEAD";


	private HttpServletResponse response;

	private boolean notModified = false;


	/**
	 * Create a new ServletWebRequest instance for the given request.
	 * 
	 * <p> 为给定的请求创建一个新的ServletWebRequest实例。
	 * 
	 * @param request current HTTP request
	 */
	public ServletWebRequest(HttpServletRequest request) {
		super(request);
	}

	/**
	 * Create a new ServletWebRequest instance for the given request/response pair.
	 * 
	 * <p> 为给定的请求/响应对创建新的ServletWebRequest实例。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response (for automatic last-modified handling)
	 * 
	 * <p> 当前HTTP响应（用于自动最后修改处理）
	 */
	public ServletWebRequest(HttpServletRequest request, HttpServletResponse response) {
		this(request);
		this.response = response;
	}


	/**
	 * Exposes the native {@link HttpServletResponse} that we're wrapping (if any).
	 * 
	 * <p> 公开我们正在包装的本机HttpServletResponse（如果有的话）。
	 */
	public final HttpServletResponse getResponse() {
		return this.response;
	}

	public Object getNativeRequest() {
		return getRequest();
	}

	public Object getNativeResponse() {
		return getResponse();
	}

	public <T> T getNativeRequest(Class<T> requiredType) {
		return WebUtils.getNativeRequest(getRequest(), requiredType);
	}

	public <T> T getNativeResponse(Class<T> requiredType) {
		return WebUtils.getNativeResponse(getResponse(), requiredType);
	}


	public String getHeader(String headerName) {
		return getRequest().getHeader(headerName);
	}

	public String[] getHeaderValues(String headerName) {
		String[] headerValues = StringUtils.toStringArray(getRequest().getHeaders(headerName));
		return (!ObjectUtils.isEmpty(headerValues) ? headerValues : null);
	}

	public Iterator<String> getHeaderNames() {
		return CollectionUtils.toIterator(getRequest().getHeaderNames());
	}

	public String getParameter(String paramName) {
		return getRequest().getParameter(paramName);
	}

	public String[] getParameterValues(String paramName) {
		return getRequest().getParameterValues(paramName);
	}

	public Iterator<String> getParameterNames() {
		return CollectionUtils.toIterator(getRequest().getParameterNames());
	}

	public Map<String, String[]> getParameterMap() {
		return getRequest().getParameterMap();
	}

	public Locale getLocale() {
		return getRequest().getLocale();
	}

	public String getContextPath() {
		return getRequest().getContextPath();
	}

	public String getRemoteUser() {
		return getRequest().getRemoteUser();
	}

	public Principal getUserPrincipal() {
		return getRequest().getUserPrincipal();
	}

	public boolean isUserInRole(String role) {
		return getRequest().isUserInRole(role);
	}

	public boolean isSecure() {
		return getRequest().isSecure();
	}

	@SuppressWarnings("deprecation")
	public boolean checkNotModified(long lastModifiedTimestamp) {
		if (lastModifiedTimestamp >= 0 && !this.notModified &&
				(this.response == null || !this.response.containsHeader(HEADER_LAST_MODIFIED))) {
			long ifModifiedSince = -1;
			try {
				ifModifiedSince = getRequest().getDateHeader(HEADER_IF_MODIFIED_SINCE);
			}
			catch (IllegalArgumentException ex) {
				String headerValue = getRequest().getHeader(HEADER_IF_MODIFIED_SINCE);
				// Possibly an IE 10 style value: "Wed, 09 Apr 2014 09:57:42 GMT; length=13774"
				// 可能是IE 10风格的价值：“Wed，09 Apr 2014 09:57:42 GMT; length = 13774”
				int separatorIndex = headerValue.indexOf(';');
				if (separatorIndex != -1) {
					String datePart = headerValue.substring(0, separatorIndex);
					try {
						ifModifiedSince = Date.parse(datePart);
					}
					catch (IllegalArgumentException ex2) {
						// Giving up
						// 放弃
					}
				}
			}
			this.notModified = (ifModifiedSince >= (lastModifiedTimestamp / 1000 * 1000));
			if (this.response != null) {
				if (this.notModified && supportsNotModifiedStatus()) {
					this.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
				else {
					this.response.setDateHeader(HEADER_LAST_MODIFIED, lastModifiedTimestamp);
				}
			}
		}
		return this.notModified;
	}

	@Override
	public boolean checkNotModified(String etag) {
		if (StringUtils.hasLength(etag) && !this.notModified &&
				(this.response == null || !this.response.containsHeader(HEADER_ETAG))) {
			String ifNoneMatch = getRequest().getHeader(HEADER_IF_NONE_MATCH);
			this.notModified = etag.equals(ifNoneMatch);
			if (this.response != null) {
				if (this.notModified && supportsNotModifiedStatus()) {
					this.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
				else {
					this.response.setHeader(HEADER_ETAG, etag);
				}
			}
		}
		return this.notModified;
	}

	private boolean supportsNotModifiedStatus() {
		String method = getRequest().getMethod();
		return (METHOD_GET.equals(method) || METHOD_HEAD.equals(method));
	}

	public boolean isNotModified() {
		return this.notModified;
	}

	public String getDescription(boolean includeClientInfo) {
		HttpServletRequest request = getRequest();
		StringBuilder sb = new StringBuilder();
		sb.append("uri=").append(request.getRequestURI());
		if (includeClientInfo) {
			String client = request.getRemoteAddr();
			if (StringUtils.hasLength(client)) {
				sb.append(";client=").append(client);
			}
			HttpSession session = request.getSession(false);
			if (session != null) {
				sb.append(";session=").append(session.getId());
			}
			String user = request.getRemoteUser();
			if (StringUtils.hasLength(user)) {
				sb.append(";user=").append(user);
			}
		}
		return sb.toString();
	}


	@Override
	public String toString() {
		return "ServletWebRequest: " + getDescription(true);
	}

}
