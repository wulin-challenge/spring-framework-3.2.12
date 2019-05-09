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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Simple interface for bean definition readers.
 * Specifies load methods with Resource and String location parameters.
 * 
 * <p>bean定义读者的简单界面。 使用Resource和String位置参数指定加载方法。
 *
 * <p>Concrete bean definition readers can of course add additional
 * load and register methods for bean definitions, specific to
 * their bean definition format.
 * 
 * <p>具体的bean定义读者当然可以为bean定义添加额外的load和register方法，特定于bean定义格式。
 *
 * <p>Note that a bean definition reader does not have to implement
 * this interface. It only serves as suggestion for bean definition
 * readers that want to follow standard naming conventions.
 * 
 * <p>请注意，bean定义读取器不必实现此接口。 它仅作为想要遵循标准命名约定的bean定义读者的建议。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.core.io.Resource
 */
public interface BeanDefinitionReader {

	/**
	 * Return the bean factory to register the bean definitions with.
	 * 
	 * <p>返回bean工厂以注册bean定义。
	 * 
	 * <p>The factory is exposed through the BeanDefinitionRegistry interface,
	 * encapsulating the methods that are relevant for bean definition handling.
	 * 
	 * <p>工厂通过BeanDefinitionRegistry接口公开，封装了与bean定义处理相关的方法。
	 * 
	 */
	BeanDefinitionRegistry getRegistry();

	/**
	 * Return the resource loader to use for resource locations.
	 * Can be checked for the <b>ResourcePatternResolver</b> interface and cast
	 * accordingly, for loading multiple resources for a given resource pattern.
	 * 
	 * <p>返回资源加载程序以用于资源位置。 可以检查ResourcePatternResolver接口并进行相应的转换，以便为给定的资源模式加载多个资源。
	 * 
	 * <p>Null suggests that absolute resource loading is not available
	 * for this bean definition reader.
	 * 
	 * <p>Null表明绝对资源加载不适用于此bean定义读取器。
	 * 
	 * <p>This is mainly meant to be used for importing further resources
	 * from within a bean definition resource, for example via the "import"
	 * tag in XML bean definitions. It is recommended, however, to apply
	 * such imports relative to the defining resource; only explicit full
	 * resource locations will trigger absolute resource loading.
	 * 
	 * <p>这主要用于从bean定义资源中导入更多资源，例如通过XML bean定义中的“import”标记。 但是，建议相对于定
	 * 义资源应用此类导入; 只有显式的完整资源位置才会触发绝对资源加载。
	 * 
	 * <p>There is also a {@code loadBeanDefinitions(String)} method available,
	 * for loading bean definitions from a resource location (or location pattern).
	 * This is a convenience to avoid explicit ResourceLoader handling.
	 * 
	 * <p>还有一个loadBeanDefinitions（String）方法可用于从资源位置（或位置模式）加载bean定义。 
	 * 这是避免显式ResourceLoader处理的便利。
	 * 
	 * @see #loadBeanDefinitions(String)
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 */
	ResourceLoader getResourceLoader();

	/**
	 * Return the class loader to use for bean classes.
	 * 
	 * <p>返回类加载器以用于bean类。
	 * 
	 * <p>{@code null} suggests to not load bean classes eagerly
	 * but rather to just register bean definitions with class names,
	 * with the corresponding Classes to be resolved later (or never).
	 * 
	 * <p>null建议不要急切地加载bean类，而是仅使用类名注册bean定义，并在稍后（或从不）解析相应的Classes。
	 * 
	 */
	ClassLoader getBeanClassLoader();

	/**
	 * Return the BeanNameGenerator to use for anonymous beans
	 * (without explicit bean name specified).
	 * 
	 * <p>返回BeanNameGenerator以用于匿名bean（未指定显式bean名称）。
	 * 
	 */
	BeanNameGenerator getBeanNameGenerator();


	/**
	 * Load bean definitions from the specified resource.
	 * 
	 * <p>从指定的资源加载bean定义。
	 * 
	 * @param resource the resource descriptor - 资源描述符
	 * @return the number of bean definitions found - 找到的bean定义数
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors - 在加载或解析错误的情况下
	 */
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	/**
	 * Load bean definitions from the specified resources.
	 * 
	 * <p>从指定的资源加载bean定义。
	 * 
	 * @param resources the resource descriptors - 资源描述符
	 * @return the number of bean definitions found - 找到的bean定义数
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors - 在加载或解析错误的情况下
	 */
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	/**
	 * Load bean definitions from the specified resource location.
	 * 
	 * <p>从指定的资源位置加载bean定义。
	 * 
	 * <p>The location can also be a location pattern, provided that the
	 * ResourceLoader of this bean definition reader is a ResourcePatternResolver.
	 * 
	 * <p>该位置也可以是位置模式，前提是此bean定义读取器的ResourceLoader是ResourcePatternResolver。
	 * 
	 * @param location the resource location, to be loaded with the ResourceLoader
	 * (or ResourcePatternResolver) of this bean definition reader
	 * 
	 * <p>要使用此bean定义读取器的ResourceLoader（或ResourcePatternResolver）加载的资源位置
	 * 
	 * @return the number of bean definitions found - 找到的bean定义数
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors - 在加载或解析错误的情况下
	 * @see #getResourceLoader()
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource)
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource[])
	 */
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

	/**
	 * Load bean definitions from the specified resource locations. - 从指定的资源位置加载bean定义。
	 * @param locations the resource locations, to be loaded with the ResourceLoader
	 * (or ResourcePatternResolver) of this bean definition reader
	 * 
	 * <p>要使用此bean定义读取器的ResourceLoader（或ResourcePatternResolver）加载的资源位置
	 * 
	 * @return the number of bean definitions found - 找到的bean定义数
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors - 在加载或解析错误的情况下
	 */
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
