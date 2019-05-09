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

package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that need to react once all their
 * properties have been set by a BeanFactory: for example, to perform custom
 * initialization, or merely to check that all mandatory properties have been set.
 * 
 * <p> 接口由Bean实现，这些bean在BeanFactory设置了所有属性后需要做出反应：例如，
 * 执行自定义初始化，或仅检查是否已设置所有必需属性。
 *
 * <p>An alternative to implementing InitializingBean is specifying a custom
 * init-method, for example in an XML bean definition.
 * For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 * 
 * <p> 实现InitializingBean的替代方法是指定自定义init方法，例如在XML bean定义中。 
 * 有关所有bean生命周期方法的列表，请参阅BeanFactory javadocs。
 *
 * @author Rod Johnson
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see BeanFactory
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.context.ApplicationContextAware
 */
public interface InitializingBean {

	/**
	 * Invoked by a BeanFactory after it has set all bean properties supplied
	 * (and satisfied BeanFactoryAware and ApplicationContextAware).
	 * 
	 * <p> BeanFactory设置了所有提供的bean属性（并且满
	 * 足BeanFactoryAware和ApplicationContextAware）之后调用它。
	 * 
	 * <p>This method allows the bean instance to perform initialization only
	 * possible when all bean properties have been set and to throw an
	 * exception in the event of misconfiguration.
	 * 
	 * <p> 此方法允许bean实例仅在设置了所有bean属性时执行初始化，并在配置错误时抛出异常。
	 * 
	 * @throws Exception in the event of misconfiguration (such
	 * as failure to set an essential property) or if initialization fails.
	 * 
	 * <p> 如果配置错误（例如未能设置基本属性）或初始化失败。
	 */
	void afterPropertiesSet() throws Exception;

}
