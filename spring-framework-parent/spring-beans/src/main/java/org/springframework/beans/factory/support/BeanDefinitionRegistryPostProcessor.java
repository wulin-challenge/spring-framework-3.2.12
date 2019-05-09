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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions <i>before</i> regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 * 
 * <p> 扩展到标准BeanFactoryPostProcessor SPI，允许在常规BeanFactoryPostProcessor检测开始之前
 * 注册其他bean定义。特别是，BeanDefinitionRegistryPostProcessor可以注册更多的bean定义，
 * 而bean定义又定义了BeanFactoryPostProcessor实例。
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 * 
	 * <p> 在标准初始化之后修改应用程序上下文的内部bean定义注册表。 将加载所有常规bean定义，
	 * 但尚未实例化任何bean。 这允许在下一个后处理阶段开始之前添加更多的bean定义。
	 * 
	 * @param registry the bean definition registry used by the application context
	 * 
	 * <p> 应用程序上下文使用的bean定义注册表
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors - 如果有错误
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
