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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Allows for custom modification of an application context's bean definitions,
 * adapting the bean property values of the context's underlying bean factory.
 * 
 * <p> 允许自定义修改应用程序上下文的bean定义，调整上下文的基础bean工厂的bean属性值。
 *
 * <p>Application contexts can auto-detect BeanFactoryPostProcessor beans in
 * their bean definitions and apply them before any other beans get created.
 * 
 * <p> 应用程序上下文可以在其bean定义中自动检测BeanFactoryPostProcessor bean，并在创建任何其他bean之前应用它们。
 *
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context.
 * 
 * <p> 对于以系统管理员为目标的自定义配置文件非常有用，这些文件覆盖在应用程
 *
 * <p>See PropertyResourceConfigurer and its concrete implementations
 * for out-of-the-box solutions that address such configuration needs.
 * 
 * <p> 请参阅PropertyResourceConfigurer及其针对解决此类配置需求的开箱即用解决方案的具体实现。
 *
 * <p>A BeanFactoryPostProcessor may interact with and modify bean
 * definitions, but never bean instances. Doing so may cause premature bean
 * instantiation, violating the container and causing unintended side-effects.
 * If bean instance interaction is required, consider implementing
 * {@link BeanPostProcessor} instead.
 * 
 * <p> BeanFactoryPostProcessor可以与bean定义交互并修改bean定义，但绝不能与bean实例交互。
 *  这样做可能会导致bean过早实例化，违反容器并导致意外的副作用。 如果需要bean实例交互，请考虑实现BeanPostProcessor。
 *
 * @author Juergen Hoeller
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
public interface BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * 
	 * <p> 在标准初始化之后修改应用程序上下文的内部bean工厂。 将加载所有bean定义，但尚未实例化任何bean。 
	 * 这允许覆盖或添加属性，甚至是初始化bean。
	 * 
	 * @param beanFactory the bean factory used by the application context
	 * 
	 * <p> 应用程序上下文使用的bean工厂
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors - 如果出错就抛出 BeansException异常
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
