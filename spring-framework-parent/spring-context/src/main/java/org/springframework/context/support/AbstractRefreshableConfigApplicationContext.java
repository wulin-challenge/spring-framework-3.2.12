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

package org.springframework.context.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link AbstractRefreshableApplicationContext} subclass that adds common handling
 * of specified config locations. Serves as base class for XML-based application
 * context implementations such as {@link ClassPathXmlApplicationContext} and
 * {@link FileSystemXmlApplicationContext}, as well as
 * {@link org.springframework.web.context.support.XmlWebApplicationContext} and
 * {@link org.springframework.web.portlet.context.XmlPortletApplicationContext}.
 * 
 * <p> AbstractRefreshableApplicationContext子类，用于添加指定配置位置的常见处理。 
 * 用作基于XML的应用程序上下文实现的基类，
 * 例如ClassPathXmlApplicationContext
 * 和FileSystemXmlApplicationContext，以及
 * org.springframework.web.context.support.XmlWebApplicationContext和
 * org.springframework.web.portlet.context.XmlPortletApplicationContext。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see #setConfigLocation
 * @see #setConfigLocations
 * @see #getDefaultConfigLocations
 */
public abstract class AbstractRefreshableConfigApplicationContext extends AbstractRefreshableApplicationContext
		implements BeanNameAware, InitializingBean {

	private String[] configLocations;

	private boolean setIdCalled = false;


	/**
	 * Create a new AbstractRefreshableConfigApplicationContext with no parent.
	 * 
	 * <p> 创建一个没有父级的新AbstractRefreshableConfigApplicationContext。
	 */
	public AbstractRefreshableConfigApplicationContext() {
	}

	/**
	 * Create a new AbstractRefreshableConfigApplicationContext with the given parent context.
	 * 
	 * <p> 使用给定的父上下文创建新的AbstractRefreshableConfigApplicationContext。
	 * 
	 * @param parent the parent context - 父上下文
	 */
	public AbstractRefreshableConfigApplicationContext(ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set the config locations for this application context in init-param style,
	 * i.e. with distinct locations separated by commas, semicolons or whitespace.
	 * 
	 * <p> 以init-param样式设置此应用程序上下文的配置位置，即使用逗号，分号或空格分隔的不同位置。
	 * 
	 * <p>If not set, the implementation may use a default as appropriate.
	 * 
	 * <p> 如果未设置，则实现可以根据需要使用默认值。
	 * 
	 */
	public void setConfigLocation(String location) {
		setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
	}

	/**
	 * Set the config locations for this application context.
	 * 
	 * <p> 设置此应用程序上下文的配置位置。
	 * 
	 * <p>If not set, the implementation may use a default as appropriate.
	 * 
	 * <p> 如果未设置，则实现可以根据需要使用默认值。
	 * 
	 */
	public void setConfigLocations(String[] locations) {
		if (locations != null) {
			Assert.noNullElements(locations, "Config locations must not be null");
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				//解析给定路径
				this.configLocations[i] = resolvePath(locations[i]).trim();
			}
		}
		else {
			this.configLocations = null;
		}
	}

	/**
	 * Return an array of resource locations, referring to the XML bean definition
	 * files that this context should be built with. Can also include location
	 * patterns, which will get resolved via a ResourcePatternResolver.
	 * 
	 * <p> 返回一个资源位置数组，引用应该构建此上下文的XML bean定义文件。 
	 * 还可以包含位置模式，这些模式将通过ResourcePatternResolver解析。
	 * 
	 * <p>The default implementation returns {@code null}. Subclasses can override
	 * this to provide a set of resource locations to load bean definitions from.
	 * 
	 * <p> 默认实现返回null。 子类可以重写此方法以提供一组资源位置以从中加载bean定义。
	 * 
	 * @return an array of resource locations, or {@code null} if none
	 * 
	 * <p> 资源位置数组，如果没有则为null
	 * 
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected String[] getConfigLocations() {
		return (this.configLocations != null ? this.configLocations : getDefaultConfigLocations());
	}

	/**
	 * Return the default config locations to use, for the case where no
	 * explicit config locations have been specified.
	 * 
	 * <p> 如果未指定显式配置位置，则返回要使用的默认配置位置。
	 * 
	 * <p>The default implementation returns {@code null},
	 * requiring explicit config locations.
	 * 
	 * <p> 默认实现返回null，需要显式配置位置。
	 * 
	 * @return an array of default config locations, if any
	 * 
	 * <p> 一组默认配置位置（如果有）
	 * 
	 * @see #setConfigLocations
	 */
	protected String[] getDefaultConfigLocations() {
		return null;
	}

	/**
	 * Resolve the given path, replacing placeholders with corresponding
	 * environment property values if necessary. Applied to config locations.
	 * 
	 * <p> 解析给定路径，必要时用相应的环境属性值替换占位符。 应用于配置位置。
	 * 
	 * @param path the original file path - 原始文件路径
	 * @return the resolved file path - 已解析的文件路径
	 * @see org.springframework.core.env.Environment#resolveRequiredPlaceholders(String)
	 */
	protected String resolvePath(String path) {
		return getEnvironment().resolveRequiredPlaceholders(path);
	}


	@Override
	public void setId(String id) {
		super.setId(id);
		this.setIdCalled = true;
	}

	/**
	 * Sets the id of this context to the bean name by default,
	 * for cases where the context instance is itself defined as a bean.
	 * 
	 * <p> 对于上下文实例本身定义为bean的情况，默认情况下将此上下文的id设置为bean名称。
	 * 
	 */
	public void setBeanName(String name) {
		if (!this.setIdCalled) {
			super.setId(name);
			setDisplayName("ApplicationContext '" + name + "'");
		}
	}

	/**
	 * Triggers {@link #refresh()} if not refreshed in the concrete context's
	 * constructor already.
	 * 
	 * <p> 如果没有在具体上下文的构造函数中刷新，则触发refresh（）。
	 * 
	 */
	public void afterPropertiesSet() {
		if (!isActive()) {
			refresh();
		}
	}

}
