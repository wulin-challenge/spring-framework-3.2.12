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

package org.springframework.web.servlet.support;

import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;

/**
 * Utility class for easy access to request-specific state which has been
 * set by the {@link org.springframework.web.servlet.DispatcherServlet}.
 * 
 * <p> 用于轻松访问由org.springframework.web.servlet.DispatcherServlet设置的特定于请求的状态的实用程序类。
 *
 * <p>Supports lookup of current WebApplicationContext, LocaleResolver,
 * Locale, ThemeResolver, Theme, and MultipartResolver.
 * 
 * <p> 支持查找当前的WebApplicationContext，LocaleResolver，Locale，ThemeResolver，Theme和MultipartResolver。
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 03.03.2003
 * @see RequestContext
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public abstract class RequestContextUtils {

	/**
	 * Look for the WebApplicationContext associated with the DispatcherServlet
	 * that has initiated request processing.
	 * @param request current HTTP request
	 * @return the request-specific web application context
	 * @throws IllegalStateException if no servlet-specific context has been found
	 */
	public static WebApplicationContext getWebApplicationContext(ServletRequest request)
		throws IllegalStateException {

		return getWebApplicationContext(request, null);
	}

	/**
	 * Look for the WebApplicationContext associated with the DispatcherServlet
	 * that has initiated request processing, and for the global context if none
	 * was found associated with the current request. This method is useful to
	 * allow components outside the framework, such as JSP tag handlers,
	 * to access the most specific application context available.
	 * 
	 * <p> 查找与已启动请求处理的DispatcherServlet关联的WebApplicationContext，如果未找到与当前请求关联的全局上下文，
	 * 则查找全局上下文。 此方法可用于允许框架外部的组件（如JSP标记处理程序）访问可用的最具体的应用程序上下文。
	 * 
	 * @param request current HTTP request
	 * @param servletContext current servlet context
	 * @return the request-specific WebApplicationContext, or the global one
	 * if no request-specific context has been found
	 * 
	 * <p> 特定于请求的WebApplicationContext，如果未找到特定于请求的上下文，则为全局的WebApplicationContext
	 * 
	 * @throws IllegalStateException if neither a servlet-specific nor a
	 * global context has been found
	 * 
	 * <p> 如果既没有找到特定于servlet的也没有找到全局上下文
	 */
	public static WebApplicationContext getWebApplicationContext(
			ServletRequest request, ServletContext servletContext) throws IllegalStateException {

		WebApplicationContext webApplicationContext = (WebApplicationContext) request.getAttribute(
				DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (webApplicationContext == null) {
			if (servletContext == null) {
				throw new IllegalStateException("No WebApplicationContext found: not in a DispatcherServlet request?");
			}
			webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		}
		return webApplicationContext;
	}

	/**
	 * Return the LocaleResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * 
	 * <p> 返回已由DispatcherServlet绑定到请求的LocaleResolver。
	 * 
	 * @param request current HTTP request
	 * @return the current LocaleResolver, or {@code null} if not found
	 * 
	 * <p> 当前的LocaleResolver，如果未找到则为null
	 * 
	 */
	public static LocaleResolver getLocaleResolver(HttpServletRequest request) {
		return (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
	}

	/**
	 * Retrieves the current locale from the given request,
	 * using the LocaleResolver bound to the request by the DispatcherServlet
	 * (if available), falling back to the request's accept-header Locale.
	 * 
	 * <p> 使用由DispatcherServlet（如果可用）绑定到请求的LocaleResolver，从给定请求中检索当前语言环境，
	 * 并返回到请求的accept-header Locale。
	 * 
	 * @param request current HTTP request
	 * @return the current locale, either from the LocaleResolver or from
	 * the plain request
	 * 
	 * <p> 当前语言环境，可以是LocaleResolver，也可以是普通请求
	 * 
	 * @see #getLocaleResolver
	 * @see javax.servlet.http.HttpServletRequest#getLocale()
	 */
	public static Locale getLocale(HttpServletRequest request) {
		LocaleResolver localeResolver = getLocaleResolver(request);
		if (localeResolver != null) {
			return localeResolver.resolveLocale(request);
		}
		else {
			return request.getLocale();
		}
	}

	/**
	 * Return the ThemeResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current ThemeResolver, or {@code null} if not found
	 */
	public static ThemeResolver getThemeResolver(HttpServletRequest request) {
		return (ThemeResolver) request.getAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE);
	}

	/**
	 * Return the ThemeSource that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current ThemeSource
	 */
	public static ThemeSource getThemeSource(HttpServletRequest request) {
		return (ThemeSource) request.getAttribute(DispatcherServlet.THEME_SOURCE_ATTRIBUTE);
	}

	/**
	 * Retrieves the current theme from the given request, using the ThemeResolver
	 * and ThemeSource bound to the request by the DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current theme, or {@code null} if not found
	 * @see #getThemeResolver
	 */
	public static Theme getTheme(HttpServletRequest request) {
		ThemeResolver themeResolver = getThemeResolver(request);
		ThemeSource themeSource = getThemeSource(request);
		if (themeResolver != null && themeSource != null) {
			String themeName = themeResolver.resolveThemeName(request);
			return themeSource.getTheme(themeName);
		}
		else {
			return null;
		}
	}

	/**
	 * Return a read-only {@link Map} with "input" flash attributes saved on a
	 * previous request.
	 * @param request the current request
	 * @return a read-only Map, or {@code null} if not found
	 * @see FlashMap
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ?> getInputFlashMap(HttpServletRequest request) {
		return (Map<String, ?>) request.getAttribute(DispatcherServlet.INPUT_FLASH_MAP_ATTRIBUTE);
	}

	/**
	 * Return the "output" FlashMap with attributes to save for a subsequent request.
	 * @param request the current request
	 * @return a {@link FlashMap} instance (never {@code null} within a DispatcherServlet request)
	 * @see FlashMap
	 */
	public static FlashMap getOutputFlashMap(HttpServletRequest request) {
		return (FlashMap) request.getAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE);
	}

	/**
	 * Return the FlashMapManager instance to save flash attributes with
	 * before a redirect.
	 * @param request the current request
	 * @return a {@link FlashMapManager} instance (never {@code null} within a DispatcherServlet request)
	 */
	public static FlashMapManager getFlashMapManager(HttpServletRequest request) {
		return (FlashMapManager) request.getAttribute(DispatcherServlet.FLASH_MAP_MANAGER_ATTRIBUTE);
	}

}
