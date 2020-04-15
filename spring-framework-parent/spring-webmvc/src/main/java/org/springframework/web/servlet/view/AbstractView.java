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

package org.springframework.web.servlet.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Abstract base class for {@link org.springframework.web.servlet.View}
 * implementations. Subclasses should be JavaBeans, to allow for
 * convenient configuration as Spring-managed bean instances.
 * 
 * <p> org.springframework.web.servlet.View实现的抽象基类。 
 * 子类应该是JavaBeans，以便于配置为Spring管理的bean实例。
 *
 * <p>Provides support for static attributes, to be made available to the view,
 * with a variety of ways to specify them. Static attributes will be merged
 * with the given dynamic attributes (the model that the controller returned)
 * for each render operation.
 * 
 * <p> 通过各种方式指定静态属性，为视图提供静态属性支持。 静态属性将与每个渲染操作的给定动态属性（控制器返回的模型）合并。
 *
 * <p>Extends {@link WebApplicationObjectSupport}, which will be helpful to
 * some views. Subclasses just need to implement the actual rendering.
 * 
 * <p> 扩展WebApplicationObjectSupport，这对某些视图很有帮助。 子类只需要实现实际渲染。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setAttributes
 * @see #setAttributesMap
 * @see #renderMergedOutputModel
 */
public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

	/** Default content type. Overridable as bean property. */
	/** 默认内容类型。 作为bean属性可覆盖. */
	public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

	/** Initial size for the temporary output byte array (if any) */
	/** 临时输出字节数组的初始大小（如果有） */
	private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;


	private String beanName;

	private String contentType = DEFAULT_CONTENT_TYPE;

	private String requestContextAttribute;

	/** Map of static attributes, keyed by attribute name (String) */
	/** 静态属性的映射，由属性名称（String）键控 */
	private final Map<String, Object> staticAttributes = new LinkedHashMap<String, Object>();

	/** Whether or not the view should add path variables in the model */
	/** 视图是否应在模型中添加路径变量 */
	private boolean exposePathVariables = true;


	/**
	 * Set the view's name. Helpful for traceability.
	 * 
	 * <p> 设置视图的名称。 有助于追溯。
	 * 
	 * <p>Framework code must call this when constructing views.
	 * 
	 * <p> 框架代码在构造视图时必须调用它。
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Return the view's name. Should never be {@code null},
	 * if the view was correctly configured.
	 * 
	 * <p> 返回视图的名称。 如果视图配置正确，则永远不应为null。
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Set the content type for this view.
	 * Default is "text/html;charset=ISO-8859-1".
	 * 
	 * <p> 设置此视图的内容类型。 默认为“text / html; charset = ISO-8859-1”。
	 * 
	 * <p>May be ignored by subclasses if the view itself is assumed
	 * to set the content type, e.g. in case of JSPs.
	 * 
	 * <p> 如果假定视图本身设置内容类型，则子类可以忽略，例如， 在JSP的情况下。
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Return the content type for this view.
	 * 
	 * <p> 返回此视图的内容类型。
	 */
	public String getContentType() {
		return this.contentType;
	}

	/**
	 * Set the name of the RequestContext attribute for this view.
	 * Default is none.
	 * 
	 * <p> 为此视图设置RequestContext属性的名称。 默认为none。
	 */
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}

	/**
	 * Return the name of the RequestContext attribute, if any.
	 * 
	 * <p> 返回RequestContext属性的名称（如果有）。
	 */
	public String getRequestContextAttribute() {
		return this.requestContextAttribute;
	}

	/**
	 * Set static attributes as a CSV string.
	 * Format is: attname0={value1},attname1={value1}
	 * 
	 * <p> 将静态属性设置为CSV字符串。 格式为：attname0 = {value1}，attname1 = {value1}
	 * 
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * 
	 * <p> “静态”属性是在View实例配置中指定的固定属性。 另一方面，“动态”属性是作为模型的一部分传入的值。
	 */
	public void setAttributesCSV(String propString) throws IllegalArgumentException {
		if (propString != null) {
			StringTokenizer st = new StringTokenizer(propString, ",");
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				int eqIdx = tok.indexOf("=");
				if (eqIdx == -1) {
					throw new IllegalArgumentException("Expected = in attributes CSV string '" + propString + "'");
				}
				if (eqIdx >= tok.length() - 2) {
					throw new IllegalArgumentException(
							"At least 2 characters ([]) required in attributes CSV string '" + propString + "'");
				}
				String name = tok.substring(0, eqIdx);
				String value = tok.substring(eqIdx + 1);

				// Delete first and last characters of value: { and }
				// 删除值的第一个和最后一个字符：{和}
				value = value.substring(1);
				value = value.substring(0, value.length() - 1);

				addStaticAttribute(name, value);
			}
		}
	}

	/**
	 * Set static attributes for this view from a
	 * {@code java.util.Properties} object.
	 * 
	 * <p> 从java.util.Properties对象为此视图设置静态属性。
	 * 
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * 
	 * <p> “静态”属性是在View实例配置中指定的固定属性。 另一方面，“动态”属性是作为模型的一部分传入的值。
	 * 
	 * <p>This is the most convenient way to set static attributes. Note that
	 * static attributes can be overridden by dynamic attributes, if a value
	 * with the same name is included in the model.
	 * 
	 * <p> 这是设置静态属性最方便的方法。 请注意，如果模型中包含具有相同名称的值，则静态属性可以被动态属性覆盖。
	 * 
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor)
	 * or a "props" element in XML bean definitions.
	 * 
	 * <p> 可以使用String“value”（通过PropertiesEditor解析）或XML bean定义中的“props”元素填充。
	 * 
	 * @see org.springframework.beans.propertyeditors.PropertiesEditor
	 */
	public void setAttributes(Properties attributes) {
		CollectionUtils.mergePropertiesIntoMap(attributes, this.staticAttributes);
	}

	/**
	 * Set static attributes for this view from a Map. This allows to set
	 * any kind of attribute values, for example bean references.
	 * 
	 * <p> 从Map设置此视图的静态属性。 这允许设置任何类型的属性值，例如bean引用。
	 * 
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * 
	 * <p> “静态”属性是在View实例配置中指定的固定属性。 另一方面，“动态”属性是作为模型的一部分传入的值。
	 * 
	 * <p>Can be populated with a "map" or "props" element in XML bean definitions.
	 * 
	 * <p> 可以在XML bean定义中使用“map”或“props”元素填充。
	 * 
	 * @param attributes Map with name Strings as keys and attribute objects as values
	 * 
	 * <p> 使用名称将字符串映射为键，将属性对象映射为值
	 */
	public void setAttributesMap(Map<String, ?> attributes) {
		if (attributes != null) {
			for (Map.Entry<String, ?> entry : attributes.entrySet()) {
				addStaticAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Allow Map access to the static attributes of this view,
	 * with the option to add or override specific entries.
	 * 
	 * <p> 允许Map访问此视图的静态属性，并可选择添加或覆盖特定条目。
	 * 
	 * <p>Useful for specifying entries directly, for example via
	 * "attributesMap[myKey]". This is particularly useful for
	 * adding or overriding entries in child view definitions.
	 * 
	 * <p> 用于直接指定条目，例如通过“attributesMap [myKey]”。 
	 * 这对于在子视图定义中添加或覆盖条目特别有用。
	 */
	public Map<String, Object> getAttributesMap() {
		return this.staticAttributes;
	}

	/**
	 * Add static data to this view, exposed in each view.
	 * 
	 * <p> 将静态数据添加到此视图，在每个视图中公开。
	 * 
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * 
	 * <p> “静态”属性是在View实例配置中指定的固定属性。 另一方面，“动态”属性是作为模型的一部分传入的值。
	 * 
	 * <p>Must be invoked before any calls to {@code render}.
	 * 
	 * <p> 必须在任何渲染调用之前调用。
	 * 
	 * @param name the name of the attribute to expose
	 * 
	 * <p> 要公开的属性的名称
	 * 
	 * @param value the attribute value to expose
	 * 
	 * <p> 要公开的属性值
	 * 
	 * @see #render
	 */
	public void addStaticAttribute(String name, Object value) {
		this.staticAttributes.put(name, value);
	}

	/**
	 * Return the static attributes for this view. Handy for testing.
	 * 
	 * <p> 返回此视图的静态属性。 方便测试。
	 * 
	 * <p>Returns an unmodifiable Map, as this is not intended for
	 * manipulating the Map but rather just for checking the contents.
	 * 
	 * <p> 返回一个不可修改的Map，因为它不是用于操作Map而是用于检查内容。
	 * 
	 * @return the static attributes in this view
	 * 
	 * <p> 此视图中的静态属性
	 * 
	 */
	public Map<String, Object> getStaticAttributes() {
		return Collections.unmodifiableMap(this.staticAttributes);
	}

	/**
	 * Specify whether to add path variables to the model or not.
	 * 
	 * <p> 指定是否将路径变量添加到模型。
	 * 
	 * <p>Path variables are commonly bound to URI template variables through the {@code @PathVariable}
	 * annotation. They're are effectively URI template variables with type conversion applied to
	 * them to derive typed Object values. Such values are frequently needed in views for
	 * constructing links to the same and other URLs.
	 * 
	 * <p> 路径变量通常通过@PathVariable注释绑定到URI模板变量。 它们实际上是URI模板变量，
	 * 应用了类型转换来派生类型化的Object值。 在构建到相同URL和其他URL的链接的视图中经常需要这样的值。
	 * 
	 * <p>Path variables added to the model override static attributes (see {@link #setAttributes(Properties)})
	 * but not attributes already present in the model.
	 * 
	 * <p> 添加到模型的路径变量会覆盖静态属性（请参阅setAttributes（Properties）），但不会覆盖模型中已存在的属性。
	 * 
	 * <p>By default this flag is set to {@code true}. Concrete view types can override this.
	 * 
	 * <p> 默认情况下，此标志设置为true。 具体视图类型可以覆盖它。
	 * 
	 * @param exposePathVariables {@code true} to expose path variables, and {@code false} otherwise
	 * 
	 * <p> 如果公开路径变量，则为true，否则为false
	 */
	public void setExposePathVariables(boolean exposePathVariables) {
		this.exposePathVariables = exposePathVariables;
	}

	/**
	 * Return whether to add path variables to the model or not.
	 * 
	 * <p> 返回是否将路径变量添加到模型中。
	 */
	public boolean isExposePathVariables() {
		return this.exposePathVariables;
	}


	/**
	 * Prepares the view given the specified model, merging it with static
	 * attributes and a RequestContext attribute, if necessary.
	 * Delegates to renderMergedOutputModel for the actual rendering.
	 * 
	 * <p> 如果需要，准备给定指定模型的视图，将其与静态属性和RequestContext属性合并。
	 *  委托renderMergedOutputModel进行实际渲染。
	 * 
	 * @see #renderMergedOutputModel
	 */
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("Rendering view with name '" + this.beanName + "' with model " + model +
				" and static attributes " + this.staticAttributes);
		}

		Map<String, Object> mergedModel = createMergedOutputModel(model, request, response);
		prepareResponse(request, response);
		renderMergedOutputModel(mergedModel, request, response);
	}

	/**
	 * Creates a combined output Map (never {@code null}) that includes dynamic values and static attributes.
	 * Dynamic values take precedence over static attributes.
	 * 
	 * <p> 创建包含动态值和静态属性的组合输出Map（永不为null）。 动态值优先于静态属性。
	 */
	protected Map<String, Object> createMergedOutputModel(Map<String, ?> model, HttpServletRequest request,
			HttpServletResponse response) {

		@SuppressWarnings("unchecked")
		Map<String, Object> pathVars = (this.exposePathVariables ?
				(Map<String, Object>) request.getAttribute(View.PATH_VARIABLES) : null);

		// Consolidate static and dynamic model attributes.
		// 合并静态和动态模型属性。
		int size = this.staticAttributes.size();
		size += (model != null ? model.size() : 0);
		size += (pathVars != null ? pathVars.size() : 0);

		Map<String, Object> mergedModel = new LinkedHashMap<String, Object>(size);
		mergedModel.putAll(this.staticAttributes);
		if (pathVars != null) {
			mergedModel.putAll(pathVars);
		}
		if (model != null) {
			mergedModel.putAll(model);
		}

		// Expose RequestContext?
		// 公开RequestContext？
		if (this.requestContextAttribute != null) {
			mergedModel.put(this.requestContextAttribute, createRequestContext(request, response, mergedModel));
		}

		return mergedModel;
	}

	/**
	 * Create a RequestContext to expose under the specified attribute name.
	 * 
	 * <p> 创建一个RequestContext以在指定的属性名称下公开。
	 * 
	 * <p>Default implementation creates a standard RequestContext instance for the
	 * given request and model. Can be overridden in subclasses for custom instances.
	 * 
	 * <p> 默认实现为给定的请求和模型创建标准的RequestContext实例。 可以在自定义实例的子类中重写。
	 * 
	 * @param request current HTTP request
	 * @param model combined output Map (never {@code null}),
	 * with dynamic values taking precedence over static attributes
	 * 
	 * <p> 组合输出Map（从不为null），动态值优先于静态属性
	 * 
	 * @return the RequestContext instance - RequestContext实例
	 * @see #setRequestContextAttribute
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	protected RequestContext createRequestContext(
			HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {

		return new RequestContext(request, response, getServletContext(), model);
	}

	/**
	 * Prepare the given response for rendering.
	 * 
	 * <p> 准备给定的渲染响应。
	 * 
	 * <p>The default implementation applies a workaround for an IE bug
	 * when sending download content via HTTPS.
	 * 
	 * <p> 默认实现在通过HTTPS发送下载内容时应用IE错误的变通方法。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 */
	protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
		if (generatesDownloadContent()) {
			response.setHeader("Pragma", "private");
			response.setHeader("Cache-Control", "private, must-revalidate");
		}
	}

	/**
	 * Return whether this view generates download content
	 * (typically binary content like PDF or Excel files).
	 * 
	 * <p> 返回此视图是否生成下载内容（通常是PDF或Excel文件等二进制内容）。
	 * 
	 * <p>The default implementation returns {@code false}. Subclasses are
	 * encouraged to return {@code true} here if they know that they are
	 * generating download content that requires temporary caching on the
	 * client side, typically via the response OutputStream.
	 * 
	 * <p> 默认实现返回false。 如果子类知道他们正在生成需要在客户端进行临时缓存的下载内容（通常是通过响应OutputStream），
	 * 则鼓励子类在此处返回true。
	 * 
	 * @see #prepareResponse
	 * @see javax.servlet.http.HttpServletResponse#getOutputStream()
	 */
	protected boolean generatesDownloadContent() {
		return false;
	}

	/**
	 * Subclasses must implement this method to actually render the view.
	 * 
	 * <p> 子类必须实现此方法才能实际呈现视图。
	 * 
	 * <p>The first step will be preparing the request: In the JSP case,
	 * this would mean setting model objects as request attributes.
	 * The second step will be the actual rendering of the view,
	 * for example including the JSP via a RequestDispatcher.
	 * 
	 * <p> 第一步是准备请求：在JSP情况下，这意味着将模型对象设置为请求属性。 第二步是视图的实际呈现，
	 * 例如通过RequestDispatcher包含JSP。
	 * 
	 * @param model combined output Map (never {@code null}),
	 * with dynamic values taking precedence over static attributes
	 * 
	 * <p> 组合输出Map（从不为null），动态值优先于静态属性
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if rendering failed
	 */
	protected abstract void renderMergedOutputModel(
			Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;


	/**
	 * Expose the model objects in the given map as request attributes.
	 * Names will be taken from the model Map.
	 * This method is suitable for all resources reachable by {@link javax.servlet.RequestDispatcher}.
	 * 
	 * <p> 将给定映射中的模型对象公开为请求属性。 名称将从模型Map中获取。 此方法适用于
	 * javax.servlet.RequestDispatcher可访问的所有资源。
	 * 
	 * @param model Map of model objects to expose - 要公开的模型对象的映射
	 * 
	 * @param request current HTTP request
	 */
	protected void exposeModelAsRequestAttributes(Map<String, Object> model, HttpServletRequest request) throws Exception {
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			String modelName = entry.getKey();
			Object modelValue = entry.getValue();
			if (modelValue != null) {
				request.setAttribute(modelName, modelValue);
				if (logger.isDebugEnabled()) {
					logger.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() +
							"] to request in view with name '" + getBeanName() + "'");
				}
			}
			else {
				request.removeAttribute(modelName);
				if (logger.isDebugEnabled()) {
					logger.debug("Removed model object '" + modelName +
							"' from request in view with name '" + getBeanName() + "'");
				}
			}
		}
	}

	/**
	 * Create a temporary OutputStream for this view.
	 * 
	 * <p> 为此视图创建临时OutputStream。
	 * 
	 * <p>This is typically used as IE workaround, for setting the content length header
	 * from the temporary stream before actually writing the content to the HTTP response.
	 * 
	 * <p> 这通常用作IE解决方法，用于在将内容实际写入HTTP响应之前从临时流设置内容长度标头。
	 * 
	 */
	protected ByteArrayOutputStream createTemporaryOutputStream() {
		return new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
	}

	/**
	 * Write the given temporary OutputStream to the HTTP response.
	 * 
	 * <p> 将给定的临时OutputStream写入HTTP响应。
	 * 
	 * @param response current HTTP response
	 * @param baos the temporary OutputStream to write
	 * 
	 * <p> 要写的临时OutputStream
	 * 
	 * @throws IOException if writing/flushing failed
	 */
	protected void writeToResponse(HttpServletResponse response, ByteArrayOutputStream baos) throws IOException {
		// Write content type and also length (determined via byte array).
		// 写内容类型和长度（通过字节数组确定）。
		response.setContentType(getContentType());
		response.setContentLength(baos.size());

		// Flush byte array to servlet output stream.
		// 将字节数组刷新到servlet输出流。
		ServletOutputStream out = response.getOutputStream();
		baos.writeTo(out);
		out.flush();
	}

	/**
	 * Set the content type of the response to the configured
	 * {@link #setContentType(String) content type} unless the
	 * {@link View#SELECTED_CONTENT_TYPE} request attribute is present and set
	 * to a concrete media type.
	 * 
	 * <p> 除非View.SELECTED_CONTENT_TYPE请求属性存在并设置为具体媒体类型，
	 * 否则请将响应的内容类型设置为已配置的内容类型。
	 */
	protected void setResponseContentType(HttpServletRequest request, HttpServletResponse response) {
		MediaType mediaType = (MediaType) request.getAttribute(View.SELECTED_CONTENT_TYPE);
		if (mediaType != null && mediaType.isConcrete()) {
			response.setContentType(mediaType.toString());
		}
		else {
			response.setContentType(getContentType());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		if (getBeanName() != null) {
			sb.append(": name '").append(getBeanName()).append("'");
		}
		else {
			sb.append(": unnamed");
		}
		return sb.toString();
	}

}
