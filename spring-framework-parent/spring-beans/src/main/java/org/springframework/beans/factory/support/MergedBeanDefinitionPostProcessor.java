/*
 * Copyright 2002-2008 the original author or authors.
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

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Post-processor callback interface for <i>merged</i> bean definitions at runtime.
 * {@link BeanPostProcessor} implementations may implement this sub-interface in
 * order to post-process the merged bean definition that the Spring BeanFactory
 * uses to create a specific bean instance.
 * 
 * <p> 运行时合并bean定义的后处理器回调接口。 BeanPostProcessor实现可以实现此子接口，
 * 以便对Spring BeanFactory用于创建特定bean实例的合并bean定义进行后处理。
 *
 * <p>The {@link #postProcessMergedBeanDefinition} method may for example introspect
 * the bean definition in order to prepare some cached metadata before post-processing
 * actual instances of a bean. It is also allowed to modify the bean definition
 * but <i>only</i> for bean definition properties which are actually intended
 * for concurrent modification. Basically, this only applies to operations
 * defined on the {@link RootBeanDefinition} itself but not to the properties
 * of its base classes.
 * 
 * <p> postProcessMergedBeanDefinition方法可以例如内省bean定义，以便在后处理bean的实际实例之前准备一些
 * 缓存的元数据。 它也允许修改bean定义，但仅适用于实际用于并发修改的bean定义属性。 基本上，
 * 这仅适用于RootBeanDefinition本身定义的操作，但不适用于其基类的属性。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	/**
	 * Post-process the given merged bean definition for the specified bean.
	 * 
	 * <p> 对指定bean的给定合并bean定义进行后处理。
	 * 
	 * @param beanDefinition the merged bean definition for the bean - bean的合并bean定义
	 * @param beanType the actual type of the managed bean instance - 托管bean实例的实际类型
	 * @param beanName the name of the bean - bean的名称
	 */
	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

}
