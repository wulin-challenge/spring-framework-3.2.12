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

package org.springframework.web.context.request;

/**
 * Abstraction for accessing attribute objects associated with a request.
 * Supports access to request-scoped attributes as well as to session-scoped
 * attributes, with the optional notion of a "global session".
 * 
 * <p> 用于访问与请求关联的属性对象的抽象。 支持访问请求范围的属性以及会话范围的属性，具有“全局会话”的可选概念。
 *
 * <p>Can be implemented for any kind of request/session mechanism,
 * in particular for servlet requests and portlet requests.
 * 
 * <p> 可以为任何类型的请求/会话机制实现，特别是对于servlet请求和portlet请求。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ServletRequestAttributes
 * @see org.springframework.web.portlet.context.PortletRequestAttributes
 */
public interface RequestAttributes {

	/**
	 * Constant that indicates request scope.
	 * 
	 * <p> 表示请求范围的常量。
	 */
	int SCOPE_REQUEST = 0;

	/**
	 * Constant that indicates session scope.
	 * 
	 * <p> 表示会话范围的常量。
	 * 
	 * <p>This preferably refers to a locally isolated session, if such
	 * a distinction is available (for example, in a Portlet environment).
	 * Else, it simply refers to the common session.
	 * 
	 * <p> 如果可以进行这种区分（例如，在Portlet环境中），这优选地指的是本地隔离的会话。 
	 * 否则，它只是指共同会话。
	 * 
	 */
	int SCOPE_SESSION = 1;

	/**
	 * Constant that indicates global session scope.
	 * 
	 * <p> 指示全局会话范围的常量。
	 * 
	 * <p>This explicitly refers to a globally shared session, if such
	 * a distinction is available (for example, in a Portlet environment).
	 * Else, it simply refers to the common session.
	 * 
	 * <p> 如果可以进行此类区分（例如，在Portlet环境中），则显式引用全局共享会话。 否则，它只是指共同会话。
	 */
	int SCOPE_GLOBAL_SESSION = 2;


	/**
	 * Name of the standard reference to the request object: "request".
	 * @see #resolveReference
	 */
	String REFERENCE_REQUEST = "request";

	/**
	 * Name of the standard reference to the session object: "session".
	 * 
	 * <p> 会话对象的标准引用的名称：“session”。
	 * 
	 * @see #resolveReference
	 */
	String REFERENCE_SESSION = "session";


	/**
	 * Return the value for the scoped attribute of the given name, if any.
	 * 
	 * <p> 返回给定名称的scoped属性的值（如果有）。
	 * 
	 * @param name the name of the attribute - 属性的名称
	 * @param scope the scope identifier - 范围标识符
	 * @return the current attribute value, or {@code null} if not found
	 * 
	 * <p> 当前属性值，如果未找到则为null
	 * 
	 */
	Object getAttribute(String name, int scope);

	/**
	 * Set the value for the scoped attribute of the given name,
	 * replacing an existing value (if any).
	 * 
	 * <p> 设置给定名称的scoped属性的值，替换现有值（如果有）。
	 * 
	 * @param name the name of the attribute - 属性的名称
	 * @param scope the scope identifier - 范围标识符
	 * @param value the value for the attribute - 属性的值
	 */
	void setAttribute(String name, Object value, int scope);

	/**
	 * Remove the scoped attribute of the given name, if it exists.
	 * 
	 * <p> 删除给定名称的scoped属性（如果存在）。
	 * 
	 * <p>Note that an implementation should also remove a registered destruction
	 * callback for the specified attribute, if any. It does, however, <i>not</i>
	 * need to <i>execute</i> a registered destruction callback in this case,
	 * since the object will be destroyed by the caller (if appropriate).
	 * 
	 * <p> 请注意，实现还应删除指定属性的已注册销毁回调（如果有）。 但是，在这种情况下，它不需要执行已注册的销毁回调，
	 * 因为对象将被调用者销毁（如果适用）。
	 * 
	 * @param name the name of the attribute
	 * @param scope the scope identifier - 范围标识符
	 */
	void removeAttribute(String name, int scope);

	/**
	 * Retrieve the names of all attributes in the scope.
	 * @param scope the scope identifier
	 * @return the attribute names as String array
	 */
	String[] getAttributeNames(int scope);

	/**
	 * Register a callback to be executed on destruction of the
	 * specified attribute in the given scope.
	 * <p>Implementations should do their best to execute the callback
	 * at the appropriate time: that is, at request completion or session
	 * termination, respectively. If such a callback is not supported by the
	 * underlying runtime environment, the callback <i>must be ignored</i>
	 * and a corresponding warning should be logged.
	 * <p>Note that 'destruction' usually corresponds to destruction of the
	 * entire scope, not to the individual attribute having been explicitly
	 * removed by the application. If an attribute gets removed via this
	 * facade's {@link #removeAttribute(String, int)} method, any registered
	 * destruction callback should be disabled as well, assuming that the
	 * removed object will be reused or manually destroyed.
	 * <p><b>NOTE:</b> Callback objects should generally be serializable if
	 * they are being registered for a session scope. Otherwise the callback
	 * (or even the entire session) might not survive web app restarts.
	 * @param name the name of the attribute to register the callback for
	 * @param callback the destruction callback to be executed
	 * @param scope the scope identifier
	 */
	void registerDestructionCallback(String name, Runnable callback, int scope);

	/**
	 * Resolve the contextual reference for the given key, if any.
	 * <p>At a minimum: the HttpServletRequest/PortletRequest reference for key
	 * "request", and the HttpSession/PortletSession reference for key "session".
	 * @param key the contextual key
	 * @return the corresponding object, or {@code null} if none found
	 */
	Object resolveReference(String key);

	/**
	 * Return an id for the current underlying session.
	 * @return the session id as String (never {@code null})
	 */
	String getSessionId();

	/**
	 * Expose the best available mutex for the underlying session:
	 * that is, an object to synchronize on for the underlying session.
	 * @return the session mutex to use (never {@code null})
	 */
	Object getSessionMutex();

}
