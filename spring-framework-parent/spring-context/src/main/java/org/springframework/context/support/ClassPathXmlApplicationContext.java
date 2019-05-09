/*
 * Copyright 2002-2010 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Standalone XML application context, taking the context definition files
 * from the class path, interpreting plain paths as class path resource names
 * that include the package path (e.g. "mypackage/myresource.txt"). Useful for
 * test harnesses as well as for application contexts embedded within JARs.
 * 
 * <p> 独立的XML应用程序上下文，从类路径获取上下文定义文件，将普通路径解释为包含包路径的类路径资源名称
 * （例如“mypackage / myresource.txt”）。 适用于测试工具以及JAR中嵌入的应用程序上下文。
 *
 * <p>The config location defaults can be overridden via {@link #getConfigLocations},
 * Config locations can either denote concrete files like "/myfiles/context.xml"
 * or Ant-style patterns like "/myfiles/*-context.xml" (see the
 * {@link org.springframework.util.AntPathMatcher} javadoc for pattern details).
 * 
 * <p> 配置位置默认值可以通过getConfigLocations覆盖，配置位置可以表示具体文件，如“/myfiles/context.xml”或Ant样式模式，
 * 如“/myfiles/*-context.xml”（请参阅org.springframework.util） .AntPathMatcher javadoc用于模式细节）。
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 * 
 * <p> 注意：如果有多个配置位置，以后的bean定义将覆盖先前加载的文件中定义的bean定义。 
 * 这可以用来通过额外的XML文件故意覆盖某些bean定义。
 *
 * <p><b>This is a simple, one-stop shop convenience ApplicationContext.
 * Consider using the {@link GenericApplicationContext} class in combination
 * with an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}
 * for more flexible context setup.</b>
 * 
 * <p> 这是一个简单的一站式便利ApplicationContext。 考虑将GenericApplicationContext类与
 * org.springframework.beans.factory.xml.XmlBeanDefinitionReader结合使用，
 * 以获得更灵活的上下文
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 * @see GenericApplicationContext
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	private Resource[] configResources;


	/**
	 * Create a new ClassPathXmlApplicationContext for bean-style configuration.
	 * 
	 * <p> 为bean样式配置创建一个新的ClassPathXmlApplicationContext。
	 * 
	 * @see #setConfigLocation
	 * @see #setConfigLocations
	 * @see #afterPropertiesSet()
	 */
	public ClassPathXmlApplicationContext() {
	}

	/**
	 * Create a new ClassPathXmlApplicationContext for bean-style configuration.
	 * 
	 * <p> 为bean样式配置创建一个新的ClassPathXmlApplicationContext。
	 * 
	 * @param parent the parent context - 父上下文
	 * @see #setConfigLocation
	 * @see #setConfigLocations
	 * @see #afterPropertiesSet()
	 */
	public ClassPathXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML file and automatically refreshing the context.
	 * 
	 * <p> 创建一个新的ClassPathXmlApplicationContext，从给定的XML文件加载定义并自动刷新上下文。
	 * 
	 * @param configLocation resource location - 资源位置
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 */
	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation}, true, null);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files and automatically refreshing the context.
	 * 
	 * <p> 创建一个新的ClassPathXmlApplicationContext，从给定的XML文件加载定义并自动刷新上下文。
	 * 
	 * @param configLocations array of resource locations - 资源位置数组
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 */
	public ClassPathXmlApplicationContext(String... configLocations) throws BeansException {
		this(configLocations, true, null);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files and automatically
	 * refreshing the context.
	 * 
	 * <p> 使用给定父级创建新的ClassPathXmlApplicationContext，从给定的XML文件加载定义并自动刷新上下文。
	 * 
	 * @param configLocations array of resource locations - 资源位置数组
	 * @param parent the parent context - 父上下文
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent) throws BeansException {
		this(configLocations, true, parent);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * 
	 * <p> 创建一个新的ClassPathXmlApplicationContext，从给定的XML文件加载定义。
	 * 
	 * @param configLocations array of resource locations - 资源位置数组
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * 
	 * <p> 是否自动刷新上下文，加载所有bean定义和创建所有单例。 或者，在进一步配置上下文后手动调用刷新。
	 * 
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 * @see #refresh()
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this(configLocations, refresh, null);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * 
	 * <p> 使用给定父级创建新的ClassPathXmlApplicationContext，从给定的XML文件加载定义。
	 * 
	 * @param configLocations array of resource locations - 资源位置数组
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * 
	 * <p> 是否自动刷新上下文，加载所有bean定义和创建所有单例。 或者，在进一步配置上下文后手动调用刷新。
	 * 
	 * @param parent the parent context - 父上下文
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 * @see #refresh()
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {

		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}


	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML file and automatically refreshing the context.
	 * 
	 * <p> 创建一个新的ClassPathXmlApplicationContext，从给定的XML文件加载定义并自动刷新上下文。
	 * 
	 * <p>This is a convenience method to load class path resources relative to a
	 * given Class. For full flexibility, consider using a GenericApplicationContext
	 * with an XmlBeanDefinitionReader and a ClassPathResource argument.
	 * 
	 * <p> 这是一种相对于给定Class加载类路径资源的便捷方法。 要获得完全的灵活性，请考虑使用带有XmlBeanDefinitionReader和
	 * ClassPathResource参数的GenericApplicationContext。
	 * 
	 * @param path relative (or absolute) path within the class path - 类路径中的相对（或绝对）路径
	 * @param clazz the class to load resources with (basis for the given paths) - 用于加载资源的类（给定路径的基础）
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 * @see org.springframework.core.io.ClassPathResource#ClassPathResource(String, Class)
	 * @see org.springframework.context.support.GenericApplicationContext
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	public ClassPathXmlApplicationContext(String path, Class clazz) throws BeansException {
		this(new String[] {path}, clazz);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files and automatically refreshing the context.
	 * 
	 * <p> 创建一个新的ClassPathXmlApplicationContext，从给定的XML文件加载定义并自动刷新上下文。
	 * 
	 * @param paths array of relative (or absolute) paths within the class path
	 * 
	 * <p> 类路径中的相对（或绝对）路径的数组
	 * 
	 * @param clazz the class to load resources with (basis for the given paths)
	 * 
	 * <p> 用于加载资源的类（给定路径的基础）
	 * 
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 * @see org.springframework.core.io.ClassPathResource#ClassPathResource(String, Class)
	 * @see org.springframework.context.support.GenericApplicationContext
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	public ClassPathXmlApplicationContext(String[] paths, Class clazz) throws BeansException {
		this(paths, clazz, null);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files and automatically
	 * refreshing the context.
	 * 
	 * <p> 使用给定父级创建新的ClassPathXmlApplicationContext，从给定的XML文件加载定义并自动刷新上下文。
	 * 
	 * @param paths array of relative (or absolute) paths within the class path - 类路径中的相对（或绝对）路径的数组
	 * @param clazz the class to load resources with (basis for the given paths) - 用于加载资源的类（给定路径的基础）
	 * @param parent the parent context - 父上下文
	 * @throws BeansException if context creation failed - 如果上下文创建失败
	 * @see org.springframework.core.io.ClassPathResource#ClassPathResource(String, Class)
	 * @see org.springframework.context.support.GenericApplicationContext
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	public ClassPathXmlApplicationContext(String[] paths, Class clazz, ApplicationContext parent)
			throws BeansException {

		super(parent);
		Assert.notNull(paths, "Path array must not be null");
		Assert.notNull(clazz, "Class argument must not be null");
		this.configResources = new Resource[paths.length];
		for (int i = 0; i < paths.length; i++) {
			this.configResources[i] = new ClassPathResource(paths[i], clazz);
		}
		refresh();
	}


	@Override
	protected Resource[] getConfigResources() {
		return this.configResources;
	}

}
