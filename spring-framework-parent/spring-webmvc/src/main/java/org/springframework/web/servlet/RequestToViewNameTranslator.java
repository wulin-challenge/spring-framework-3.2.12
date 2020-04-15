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
 * Strategy interface for translating an incoming
 * {@link javax.servlet.http.HttpServletRequest} into a
 * logical view name when no view name is explicitly supplied.
 * 
 * <p> 策略接口，用于在未显式提供视图名称时将传入的javax.servlet.http.HttpServletRequest转换为逻辑视图名称。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface RequestToViewNameTranslator {

	/**
	 * Translate the given {@link HttpServletRequest} into a view name.
	 * 
	 * <p> 将给定的HttpServletRequest转换为视图名称。
	 * 
	 * @param request the incoming {@link HttpServletRequest} providing
	 * the context from which a view name is to be resolved
	 * 
	 * <p> 传入的HttpServletRequest提供要从中解析视图名称的上下文
	 * 
	 * @return the view name (or {@code null} if no default found)
	 * 
	 * <p> 视图名称（如果未找到默认值，则为null）
	 * 
	 * @throws Exception if view name translation fails - 如果视图名称转换失败
	 */
	String getViewName(HttpServletRequest request) throws Exception;

}
