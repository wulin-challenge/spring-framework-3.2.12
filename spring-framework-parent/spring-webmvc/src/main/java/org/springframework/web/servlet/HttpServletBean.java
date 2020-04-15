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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Simple extension of {@link javax.servlet.http.HttpServlet} which treats
 * its config parameters ({@code init-param} entries within the
 * {@code servlet} tag in {@code web.xml}) as bean properties.
 * 
 * <p> javax.servlet.http.HttpServlet的简单扩展，它将其配置参数（web.xml中servlet标记内的init-param条目）视为bean属性。
 *
 * <p>A handy superclass for any type of servlet. Type conversion of config
 * parameters is automatic, with the corresponding setter method getting
 * invoked with the converted value. It is also possible for subclasses to
 * specify required properties. Parameters without matching bean property
 * setter will simply be ignored.
 * 
 * <p> 适用于任何类型servlet的便捷超类。配置参数的类型转换是自动的，相应的setter方法将使用转换后的值进行调用。
 * 子类也可以指定必需的属性。不会匹配bean属性setter的参数将被忽略。
 *
 * <p>This servlet leaves request handling to subclasses, inheriting the default
 * behavior of HttpServlet ({@code doGet}, {@code doPost}, etc).
 * 
 * <p> 这个servlet将请求处理留给了子类，继承了HttpServlet（doGet，doPost等）的默认行为。
 *
 * <p>This generic servlet base class has no dependency on the Spring
 * {@link org.springframework.context.ApplicationContext} concept. Simple
 * servlets usually don't load their own context but rather access service
 * beans from the Spring root application context, accessible via the
 * filter's {@link #getServletContext() ServletContext} (see
 * {@link org.springframework.web.context.support.WebApplicationContextUtils}).
 * 
 * <p> 这个通用servlet基类不依赖于Spring org.springframework.context.ApplicationContext概念。
 * 简单的servlet通常不加载它们自己的上下文，而是从Spring根应用程序上下文访问服务bean，可以通过过滤器的ServletContext访问
 * （参见org.springframework.web.context.support.WebApplicationContextUtils）。
 *
 * <p>The {@link FrameworkServlet} class is a more specific servlet base
 * class which loads its own application context. FrameworkServlet serves
 * as direct base class of Spring's full-fledged {@link DispatcherServlet}.
 * 
 * <p> FrameworkServlet类是一个更具体的servlet基类，它加载自己的应用程序上下文。
 *  FrameworkServlet是Spring完整的DispatcherServlet的直接基类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #addRequiredProperty
 * @see #initServletBean
 * @see #doGet
 * @see #doPost
 */
@SuppressWarnings("serial")
public abstract class HttpServletBean extends HttpServlet
		implements EnvironmentCapable, EnvironmentAware {

	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Set of required properties (Strings) that must be supplied as
	 * config parameters to this servlet.
	 * 
	 * <p> 必须属性（字符串）的集合，必须作为配置参数提供给此servlet。
	 */
	private final Set<String> requiredProperties = new HashSet<String>();

	private ConfigurableEnvironment environment;


	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter. This should be called
	 * from the constructor of a subclass.
	 * 
	 * <p> 子类可以调用此方法来指定此属性（必须与它们公开的JavaBean属性匹配）是必需的，并且必须作为config参数提供。 
	 * 这应该从子类的构造函数中调用。
	 * 
	 * <p>This method is only relevant in case of traditional initialization
	 * driven by a ServletConfig instance.
	 * 
	 * <p> 此方法仅适用于由ServletConfig实例驱动的传统初始化。
	 * 
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}

	/**
	 * Map config parameters onto bean properties of this servlet, and
	 * invoke subclass initialization.
	 * 
	 * <p> 将配置参数映射到此servlet的bean属性，并调用子类初始化。
	 * 
	 * @throws ServletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 * 
	 * <p> 如果bean属性无效（或缺少必需的属性），或者子类初始化失败。
	 * 
	 */
	@Override
	public final void init() throws ServletException { 
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		// Set bean properties from init parameters.
		try {
			PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
			ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
			bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
			initBeanWrapper(bw);
			bw.setPropertyValues(pvs, true);
		}
		catch (BeansException ex) {
			logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
			throw ex;
		}

		// Let subclasses do whatever initialization they like.
		initServletBean();

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}

	/**
	 * Initialize the BeanWrapper for this HttpServletBean,
	 * possibly with custom editors.
	 * 
	 * <p> 初始化此HttpServletBean的BeanWrapper，可能使用自定义编辑器。
	 * 
	 * <p>This default implementation is empty.
	 * 
	 * <p> 此默认实现为空。
	 * 
	 * @param bw the BeanWrapper to initialize - BeanWrapper初始化
	 * 
	 * @throws BeansException if thrown by BeanWrapper methods - 如果由BeanWrapper方法抛出
	 * 
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}


	/**
	 * Overridden method that simply returns {@code null} when no
	 * ServletConfig set yet.
	 * 
	 * <p> 重写方法，当没有设置ServletConfig时，它只返回null。
	 * 
	 * @see #getServletConfig()
	 */
	@Override
	public final String getServletName() {
		return (getServletConfig() != null ? getServletConfig().getServletName() : null);
	}

	/**
	 * Overridden method that simply returns {@code null} when no
	 * ServletConfig set yet.
	 * 
	 * <p> 重写方法，当没有设置ServletConfig时，它只返回null。
	 * 
	 * @see #getServletConfig()
	 */
	@Override
	public final ServletContext getServletContext() {
		return (getServletConfig() != null ? getServletConfig().getServletContext() : null);
	}


	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this servlet will have been set before this
	 * method is invoked.
	 * 
	 * <p> 子类可以重写此操作以执行自定义初始化。 在调用此方法之前，将设置此servlet的所有bean属性。
	 * 
	 * <p>This default implementation is empty.
	 * 
	 * <p> 此默认实现为空。
	 * 
	 * @throws ServletException if subclass initialization fails
	 */
	protected void initServletBean() throws ServletException {
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if environment is not assignable to
	 * {@code ConfigurableEnvironment}.
	 * 
	 * <p> 如果环境不能分配给ConfigurableEnvironment。
	 */
	public void setEnvironment(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
		this.environment = (ConfigurableEnvironment) environment;
	}

	/**
	 * {@inheritDoc}
	 * <p>If {@code null}, a new environment will be initialized via
	 * {@link #createEnvironment()}.
	 * 
	 * <p> 如果为null，则将通过createEnvironment（）初始化新环境。
	 * 
	 */
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = this.createEnvironment();
		}
		return this.environment;
	}

	/**
	 * Create and return a new {@link StandardServletEnvironment}. Subclasses may override
	 * in order to configure the environment or specialize the environment type returned.
	 * 
	 * <p> 创建并返回一个新的StandardServletEnvironment。 子类可以覆盖以配置环境或专门化返回的环境类型。
	 * 
	 */
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}


	/**
	 * PropertyValues implementation created from ServletConfig init parameters.
	 * 
	 * <p> 从ServletConfig init参数创建的PropertyValues实现。
	 * 
	 */
	private static class ServletConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new ServletConfigPropertyValues.
		 * 
		 * <p> 创建新的ServletConfigPropertyValues。
		 * 
		 * @param config ServletConfig we'll use to take PropertyValues from
		 * 
		 * <p> 我们将使用ServletConfig从中获取PropertyValues
		 * 
		 * @param requiredProperties set of property names we need, where
		 * we can't accept default values
		 * 
		 * <p> 我们需要的一组属性名称，我们不能接受默认值
		 * 
		 * @throws ServletException if any required properties are missing
		 * 
		 * <p> 如果缺少任何必需的属性
		 * 
		 */
		public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties)
			throws ServletException {

			Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
					new HashSet<String>(requiredProperties) : null;

			Enumeration en = config.getInitParameterNames();
			while (en.hasMoreElements()) {
				String property = (String) en.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// Fail if we are still missing properties.
			// 如果我们仍然缺少属性，则失败。
			if (missingProps != null && missingProps.size() > 0) {
				throw new ServletException(
					"Initialization from ServletConfig for servlet '" + config.getServletName() +
					"' failed; the following required properties were missing: " +
					StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
