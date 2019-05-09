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

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Convenient base class for {@link org.springframework.context.ApplicationContext}
 * implementations, drawing configuration from XML documents containing bean definitions
 * understood by an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 * 
 * <p> org.springframework.context.ApplicationContext实现的便捷基类，
 * 从包含org.springframework.beans.factory.xml.XmlBeanDefinitionReader理解的bean定义的XML文档中绘制配置。
 *
 * <p>Subclasses just have to implement the {@link #getConfigResources} and/or
 * the {@link #getConfigLocations} method. Furthermore, they might override
 * the {@link #getResourceByPath} hook to interpret relative paths in an
 * environment-specific fashion, and/or {@link #getResourcePatternResolver}
 * for extended pattern resolution.
 * 
 * <p> 子类只需要实现getConfigResources和/或getConfigLocations方法。 
 * 此外，它们可能会覆盖getResourceByPath挂钩以特定于环境的方式解释相对路径，
 * 和/或getResourcePatternResolver以扩展模式解析。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

	private boolean validating = true;


	/**
	 * Create a new AbstractXmlApplicationContext with no parent.
	 * 
	 * <p> 创建一个没有父项的新AbstractXmlApplicationContext。
	 * 
	 */
	public AbstractXmlApplicationContext() {
	}

	/**
	 * Create a new AbstractXmlApplicationContext with the given parent context.
	 * 
	 * <p> 使用给定的父上下文创建新的AbstractXmlApplicationContext。
	 * 
	 * @param parent the parent context - 父上下文
	 */
	public AbstractXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether to use XML validation. Default is {@code true}.
	 * 
	 * <p> 设置是否使用XML验证。 默认为true。
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}


	/**
	 * Loads the bean definitions via an XmlBeanDefinitionReader.
	 * 
	 * <p> 通过XmlBeanDefinitionReader加载bean定义。
	 * 
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		// 为给定的BeanFactory创建一个新的XmlBeanDefinitionReader。
		
		//为给定beanFactory创建 XmlBeanDefinitionReader
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		
		// 使用此上下文的资源加载环境配置bean定义读取器。
		
		//对 beanDefinitionReader 进行环境变量的设置
		beanDefinitionReader.setEnvironment(this.getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		
		// 允许子类提供读者的自定义初始化，然后继续实际加载bean定义。
		
		//对 beanDefinitionReader 进行设置,可以覆盖
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context. Default implementation is empty.
	 * 
	 * <p> 初始化用于加载此上下文的bean定义的bean定义读取器。 默认实现为空。
	 * 
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different XmlBeanDefinitionParser implementation.
	 * 
	 * <p> 可以在子类中重写，例如 用于关闭XML验证或使用不同的XmlBeanDefinitionParser实现。
	 * 
	 * @param reader the bean definition reader used by this context
	 * 
	 * <p> 此上下文使用的bean定义读取器
	 * 
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * 
	 * <p> 使用给定的XmlBeanDefinitionReader加载bean定义。
	 * 
	 * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
	 * method; hence this method is just supposed to load and/or register bean definitions.
	 * 
	 * <p> bean工厂的生命周期由refreshBeanFactory方法处理; 因此，这个方法只是加载和/或注册bean定义。
	 * 
	 * @param reader the XmlBeanDefinitionReader to use
	 * 
	 * <p> 要使用的XmlBeanDefinitionReader
	 * 
	 * @throws BeansException in case of bean registration errors - 在bean注册错误的情况下
	 * @throws IOException if the required XML document isn't found - 如果找不到所需的XML文档
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * Return an array of Resource objects, referring to the XML bean definition
	 * files that this context should be built with.
	 * 
	 * <p> 返回一个Resource对象数组，引用应该构建此上下文的XML bean定义文件。
	 * 
	 * <p>The default implementation returns {@code null}. Subclasses can override
	 * this to provide pre-built Resource objects rather than location Strings.
	 * 
	 * <p> 默认实现返回null。 子类可以覆盖它以提供预构建的Resource对象而不是位置字符串。
	 * 
	 * @return an array of Resource objects, or {@code null} if none
	 * 
	 * <p> 一组Resource对象，如果没有则为null
	 * 
	 * @see #getConfigLocations()
	 */
	protected Resource[] getConfigResources() {
		return null;
	}

}
