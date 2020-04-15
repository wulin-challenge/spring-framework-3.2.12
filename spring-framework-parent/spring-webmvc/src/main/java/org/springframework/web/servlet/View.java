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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

/**
 * MVC View for a web interaction. Implementations are responsible for rendering
 * content, and exposing the model. A single view exposes multiple model attributes.
 * 
 * <p> MVC查看Web交互。 实现负责呈现内容并公开模型。 单个视图公开多个模型属性。
 *
 * <p>This class and the MVC approach associated with it is discussed in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * 
 * <p> 这个类和与之相关的MVC方法在Rod Johnson的Expert One-On-One J2EE设计和开发的第12章中讨论过（Wrox，2002）。
 *
 * <p>View implementations may differ widely. An obvious implementation would be
 * JSP-based. Other implementations might be XSLT-based, or use an HTML generation library.
 * This interface is designed to avoid restricting the range of possible implementations.
 * 
 * <p> 视图实现可能有很大不同。 一个明显的实现是基于JSP的。 其他实现可能是基于XSLT的，或使用HTML生成库。 此接口旨在避免限制可能的实现范围。
 *
 * <p>Views should be beans. They are likely to be instantiated as beans by a ViewResolver.
 * As this interface is stateless, view implementations should be thread-safe.
 *
 * <p> 视图应该是bean。 它们很可能被ViewResolver实例化为bean。 由于此接口是无状态的，因此视图实现应该是线程安全的。
 * 
 * @author Rod Johnson
 * @author Arjen Poutsma
 * @see org.springframework.web.servlet.view.AbstractView
 * @see org.springframework.web.servlet.view.InternalResourceView
 */
public interface View {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the response status code.
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 */
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a Map with path variables.
	 * The map consists of String-based URI template variable names as keys and their corresponding
	 * Object-based values -- extracted from segments of the URL and type converted.
	 *
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 */
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";

	/**
	 * The {@link MediaType} selected during content negotiation, which may be
	 * more specific than the one the View is configured with. For example:
	 * "application/vnd.example-v1+xml" vs "application/*+xml".
	 */
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";

	/**
	 * Return the content type of the view, if predetermined.
	 * <p>Can be used to check the content type upfront,
	 * before the actual rendering process.
	 * @return the content type String (optionally including a character set),
	 * or {@code null} if not predetermined.
	 */
	String getContentType();

	/**
	 * Render the view given the specified model.
	 * 
	 * <p> 给定指定模型的视图。
	 * 
	 * <p>The first step will be preparing the request: In the JSP case,
	 * this would mean setting model objects as request attributes.
	 * The second step will be the actual rendering of the view,
	 * for example including the JSP via a RequestDispatcher.
	 * 
	 * <p> 第一步是准备请求：在JSP情况下，这意味着将模型对象设置为请求属性。 第二步是视图的实际呈现，
	 * 例如通过RequestDispatcher包含JSP。
	 * 
	 * @param model Map with name Strings as keys and corresponding model
	 * objects as values (Map can also be {@code null} in case of empty model)
	 * 
	 * <p> 使用名称将字符串映射为键，将相应的模型对象映射为值（在空模型的情况下，映射也可以为null）
	 * 
	 * @param request current HTTP request
	 * @param response HTTP response we are building
	 * @throws Exception if rendering failed
	 */
	void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
