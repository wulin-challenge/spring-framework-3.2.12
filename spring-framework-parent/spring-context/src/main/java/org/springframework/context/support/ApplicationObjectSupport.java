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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;

/**
 * Convenient superclass for application objects that want to be aware of
 * the application context, e.g. for custom lookup of collaborating beans
 * or for context-specific resource access. It saves the application
 * context reference and provides an initialization callback method.
 * Furthermore, it offers numerous convenience methods for message lookup.
 * 
 * <p> 想要了解应用程序上下文的应用程序对象的便捷超类，例如 用于自定义查找协作Bean或用于特定于上下文的资源访问。 
 * 它保存应用程序上下文引用并提供初始化回调方法。 此外，它为消息查找提供了许多便利方法。
 *
 * <p>There is no requirement to subclass this class: It just makes things
 * a little easier if you need access to the context, e.g. for access to
 * file resources or to the message source. Note that many application
 * objects do not need to be aware of the application context at all,
 * as they can receive collaborating beans via bean references.
 * 
 * <p> 没有要求对这个类进行子类化：如果你需要访问上下文，它只会让事情变得容易一些，例如： 用于访问文件资源或消息源。 
 * 请注意，许多应用程序对象根本不需要了解应用程序上下文，因为它们可以通过bean引用接收协作bean。
 *
 * <p>Many framework classes are derived from this class, particularly
 * within the web support.
 * 
 * <p> 许多框架类都派生自此类，特别是在Web支持中。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.support.WebApplicationObjectSupport
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** ApplicationContext this object runs in */
	private ApplicationContext applicationContext;

	/** MessageSourceAccessor for easy message access */
	private MessageSourceAccessor messageSourceAccessor;


	public final void setApplicationContext(ApplicationContext context) throws BeansException {
		if (context == null && !isContextRequired()) {
			// Reset internal context state.
			this.applicationContext = null;
			this.messageSourceAccessor = null;
		}
		else if (this.applicationContext == null) {
			// Initialize with passed-in context.
			if (!requiredContextClass().isInstance(context)) {
				throw new ApplicationContextException(
						"Invalid application context: needs to be of type [" + requiredContextClass().getName() + "]");
			}
			this.applicationContext = context;
			this.messageSourceAccessor = new MessageSourceAccessor(context);
			initApplicationContext(context);
		}
		else {
			// Ignore reinitialization if same context passed in.
			if (this.applicationContext != context) {
				throw new ApplicationContextException(
						"Cannot reinitialize with different application context: current one is [" +
						this.applicationContext + "], passed-in one is [" + context + "]");
			}
		}
	}

	/**
	 * Determine whether this application object needs to run in an ApplicationContext.
	 * 
	 * <p> 确定此应用程序对象是否需要在ApplicationContext中运行。
	 * 
	 * <p>Default is "false". Can be overridden to enforce running in a context
	 * (i.e. to throw IllegalStateException on accessors if outside a context).
	 * 
	 * <p> 默认为“false”。 可以重写以强制在上下文中运行（即，如果在上下文之外，则在访问器上抛出IllegalStateException）。
	 * 
	 * @see #getApplicationContext
	 * @see #getMessageSourceAccessor
	 */
	protected boolean isContextRequired() {
		return false;
	}

	/**
	 * Determine the context class that any context passed to
	 * {@code setApplicationContext} must be an instance of.
	 * Can be overridden in subclasses.
	 * 
	 * <p> 确定传递给setApplicationContext的任何上下文必须是其实例的上下文类。 可以在子类中重写。
	 * 
	 * @see #setApplicationContext
	 */
	protected Class requiredContextClass() {
		return ApplicationContext.class;
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called by {@code setApplicationContext} after setting the context instance.
	 * 
	 * <p> 子类可以覆盖此自定义初始化行为。 设置上下文实例后，由setApplicationContext调用。
	 * 
	 * <p>Note: Does </i>not</i> get called on reinitialization of the context
	 * but rather just on first initialization of this object's context reference.
	 * 
	 * <p> 注意：不会在上下文重新初始化时调用，而是在第一次初始化此对象的上下文引用时调用。
	 * 
	 * <p>The default implementation calls the overloaded {@link #initApplicationContext()}
	 * method without ApplicationContext reference.
	 * 
	 * <p> 默认实现在没有ApplicationContext引用的情况下调用重载的initApplicationContext（）方法。
	 * 
	 * @param context the containing ApplicationContext - 包含ApplicationContext
	 * 
	 * @throws ApplicationContextException in case of initialization errors
	 * 
	 * <p> 在初始化错误的情况下
	 * 
	 * @throws BeansException if thrown by ApplicationContext methods
	 * 
	 * <p> 如果由ApplicationContext方法抛出
	 * 
	 * @see #setApplicationContext
	 */
	protected void initApplicationContext(ApplicationContext context) throws BeansException {
		initApplicationContext();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * 
	 * <p> 子类可以覆盖此自定义初始化行为。
	 * 
	 * <p>The default implementation is empty. Called by
	 * {@link #initApplicationContext(org.springframework.context.ApplicationContext)}.
	 * 
	 * <p> 默认实现为空。 由initApplicationContext（org.springframework.context.ApplicationContext）调用。
	 * 
	 * @throws ApplicationContextException in case of initialization errors
	 * 
	 * <p> 在初始化错误的情况下
	 * 
	 * @throws BeansException if thrown by ApplicationContext methods
	 * 
	 * <p> 如果由ApplicationContext方法抛出
	 * 
	 * @see #setApplicationContext
	 */
	protected void initApplicationContext() throws BeansException {
	}


	/**
	 * Return the ApplicationContext that this object is associated with.
	 * 
	 * <p> 返回与此对象关联的ApplicationContext。
	 * 
	 * @throws IllegalStateException if not running in an ApplicationContext
	 * 
	 * <p> 如果没有在ApplicationContext中运行
	 */
	public final ApplicationContext getApplicationContext() throws IllegalStateException {
		if (this.applicationContext == null && isContextRequired()) {
			throw new IllegalStateException(
					"ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.applicationContext;
	}

	/**
	 * Return a MessageSourceAccessor for the application context
	 * used by this object, for easy message access.
	 * @throws IllegalStateException if not running in an ApplicationContext
	 */
	protected final MessageSourceAccessor getMessageSourceAccessor() throws IllegalStateException {
		if (this.messageSourceAccessor == null && isContextRequired()) {
			throw new IllegalStateException(
					"ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.messageSourceAccessor;
	}

}
