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

import java.util.Locale;

/**
 * Interface to be implemented by objects that can resolve views by name.
 * 
 * <p> 由可以按名称解析视图的对象实现的接口。
 *
 * <p>View state doesn't change during the running of the application,
 * so implementations are free to cache views.
 * 
 * <p> 在运行应用程序期间，视图状态不会更改，因此实现可以自由地缓存视图。
 *
 * <p>Implementations are encouraged to support internationalization,
 * i.e. localized view resolution.
 * 
 * <p> 鼓励实现支持国际化，即本地化视图解析。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see org.springframework.web.servlet.view.ResourceBundleViewResolver
 * @see org.springframework.web.servlet.view.XmlViewResolver
 */
public interface ViewResolver {

	/**
	 * Resolve the given view by name.
	 * 
	 * <p> 按名称解析给定视图。
	 * 
	 * <p>Note: To allow for ViewResolver chaining, a ViewResolver should
	 * return {@code null} if a view with the given name is not defined in it.
	 * However, this is not required: Some ViewResolvers will always attempt
	 * to build View objects with the given name, unable to return {@code null}
	 * (rather throwing an exception when View creation failed).
	 * 
	 * <p> 注意：要允许ViewResolver链接，如果未在其中定义具有给定名称的视图，则ViewResolver应返回null。 
	 * 但是，这不是必需的：某些ViewResolvers将始终尝试使用给定名称构建View对象，无法返回null（而在View创建失败时抛出异常）。
	 * 
	 * @param viewName name of the view to resolve
	 * @param locale Locale in which to resolve the view.
	 * ViewResolvers that support internationalization should respect this.
	 * 
	 * <p> 用于解析视图的区域设置。 支持国际化的ViewResolvers应该尊重这一点。
	 * 
	 * @return the View object, or {@code null} if not found
	 * (optional, to allow for ViewResolver chaining)
	 * 
	 * <p> View对象，如果未找到则为null（可选，允许ViewResolver链接）
	 * 
	 * @throws Exception if the view cannot be resolved
	 * (typically in case of problems creating an actual View object)
	 * 
	 * <p> 如果视图无法解析（通常在创建实际View对象时出现问题）
	 */
	View resolveViewName(String viewName, Locale locale) throws Exception;

}
