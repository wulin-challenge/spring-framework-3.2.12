/*
 * Copyright 2002-2013 the original author or authors.
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

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for web-based locale resolution strategies that allows for
 * both locale resolution via the request and locale modification via
 * request and response.
 * 
 * <p> 基于Web的区域设置解析策略的接口，允许通过请求进行区域设置解析，并通过请求和响应修改区域设置。
 *
 * <p>This interface allows for implementations based on request, session,
 * cookies, etc. The default implementation is
 * {@link org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver},
 * simply using the request's locale provided by the respective HTTP header.
 * 
 * <p> 此接口允许基于请求，会话，cookie等实现。默认实现是
 * org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver，只需使用相应HTTP头提供的请求的语言环境。
 *
 * <p>Use {@link org.springframework.web.servlet.support.RequestContext#getLocale()}
 * to retrieve the current locale in controllers or views, independent
 * of the actual resolution strategy.
 * 
 * <p> 使用org.springframework.web.servlet.support.RequestContext.getLocale（）
 * 来检索控制器或视图中的当前区域设置，与实际的解析策略无关。
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 */
public interface LocaleResolver {

  /**
   * Resolve the current locale via the given request.
   * Can return a default locale as fallback in any case.
   * @param request the request to resolve the locale for
   * @return the current locale (never {@code null})
   */
	Locale resolveLocale(HttpServletRequest request);

  /**
   * Set the current locale to the given one.
   * @param request the request to be used for locale modification
   * @param response the response to be used for locale modification
   * @param locale the new locale, or {@code null} to clear the locale
   * @throws UnsupportedOperationException if the LocaleResolver implementation
   * does not support dynamic changing of the locale
   */
	void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);

}
