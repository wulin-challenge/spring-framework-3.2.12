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

package org.springframework.context;

import org.springframework.beans.factory.Aware;
import org.springframework.core.io.ResourceLoader;

/**
 * Interface to be implemented by any object that wishes to be notified of
 * the <b>ResourceLoader</b> (typically the ApplicationContext) that it runs in.
 * This is an alternative to a full ApplicationContext dependency via the
 * ApplicationContextAware interface.
 * 
 * <p> 希望被通知其运行的ResourceLoader（通常是ApplicationContext）的任何对象实现的接口。
 * 这是通过ApplicationContextAware接口替代完整的ApplicationContext依赖项。
 *
 * <p>Note that Resource dependencies can also be exposed as bean properties
 * of type Resource, populated via Strings with automatic type conversion by
 * the bean factory. This removes the need for implementing any callback
 * interface just for the purpose of accessing a specific file resource.
 * 
 * <p> 请注意，资源依赖关系也可以作为Resource类型的bean属性公开，通过bean工厂自动类型转换的字符串填充。
 * 这样就不需要为了访问特定的文件资源而实现任何回调接口。
 *
 * <p>You typically need a ResourceLoader when your application object has
 * to access a variety of file resources whose names are calculated. A good
 * strategy is to make the object use a DefaultResourceLoader but still
 * implement ResourceLoaderAware to allow for overriding when running in an
 * ApplicationContext. See ReloadableResourceBundleMessageSource for an example.
 * 
 * <p> 当您的应用程序对象必须访问其名称已计算的各种文件资源时，通常需要ResourceLoader。
 * 一个好的策略是使对象使用DefaultResourceLoader但仍然实现ResourceLoaderAware以允许在ApplicationContext中运行时覆盖。
 * 有关示例，请参阅ReloadableResourceBundleMessageSource。
 *
 * <p>A passed-in ResourceLoader can also be checked for the
 * <b>ResourcePatternResolver</b> interface and cast accordingly, to be able
 * to resolve resource patterns into arrays of Resource objects. This will always
 * work when running in an ApplicationContext (the context interface extends
 * ResourcePatternResolver). Use a PathMatchingResourcePatternResolver as default.
 * See also the {@code ResourcePatternUtils.getResourcePatternResolver} method.
 * 
 * <p> 还可以检查传入的ResourceLoader以获取ResourcePatternResolver接口并进行相应的转换，
 * 以便能够将资源模式解析为Resource对象的数组。当在ApplicationContext中运行时（上下文接口扩展ResourcePatternResolver），
 * 这将始终有效。使用PathMatchingResourcePatternResolver作为默认值。
 * 另请参见ResourcePatternUtils.getResourcePatternResolver方法。
 *
 * <p>As alternative to a ResourcePatternResolver dependency, consider exposing
 * bean properties of type Resource array, populated via pattern Strings with
 * automatic type conversion by the bean factory.
 * 
 * <p> 作为ResourcePatternResolver依赖项的替代方案，请考虑公开Resource数组类型的bean属性，
 * 通过bean工厂自动类型转换的模式字符串填充。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 10.03.2004
 * @see ApplicationContextAware
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.core.io.support.ResourcePatternUtils#getResourcePatternResolver
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public interface ResourceLoaderAware extends Aware {

	/**
	 * Set the ResourceLoader that this object runs in.
	 * 
	 * <p> 设置此对象运行的ResourceLoader。
	 * 
	 * <p>This might be a ResourcePatternResolver, which can be checked
	 * through {@code instanceof ResourcePatternResolver}. See also the
	 * {@code ResourcePatternUtils.getResourcePatternResolver} method.
	 * 
	 * <p> 这可能是ResourcePatternResolver，可以通过ResourcePatternResolver实例进行检查。
	 *  另请参见ResourcePatternUtils.getResourcePatternResolver方法。
	 * 
	 * <p>Invoked after population of normal bean properties but before an init callback
	 * like InitializingBean's {@code afterPropertiesSet} or a custom init-method.
	 * Invoked before ApplicationContextAware's {@code setApplicationContext}.
	 * 
	 * <p> 在普通bean属性的填充之后但在初始化回调之前调用，例如InitializingBean的afterPropertiesSet或自定义init方法。
	 *  在ApplicationContextAware的setApplicationContext之前调用。
	 *  
	 * @param resourceLoader ResourceLoader object to be used by this object
	 * 
	 * <p> 此对象要使用的ResourceLoader对象
	 * 
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.ResourcePatternUtils#getResourcePatternResolver
	 */
	void setResourceLoader(ResourceLoader resourceLoader);

}
