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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * Strategy interface for determining whether a specific bean definition
 * qualifies as an autowire candidate for a specific dependency.
 * 
 * <p>用于确定特定bean定义是否有资格作为特定依赖关系的自动线候选的策略接口。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
public interface AutowireCandidateResolver {

	/**
	 * Determine whether the given bean definition qualifies as an
	 * autowire candidate for the given dependency.
	 * 
	 * <p>确定给定的bean定义是否有资格作为给定依赖项的autowire候选。
	 * 
	 * @param bdHolder the bean definition including bean name and aliases - bean定义包括bean名称和别名
	 * @param descriptor the descriptor for the target method parameter or field - 目标方法参数或字段的描述符
	 * @return whether the bean definition qualifies as autowire candidate - bean定义是否有资格作为autowire候选者
	 */
	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);

	/**
	 * Determine whether a default value is suggested for the given dependency.
	 * 
	 * <p>确定是否为给定的依赖项建议了默认值。
	 * 
	 * @param descriptor the descriptor for the target method parameter or field - 目标方法参数或字段的描述符
	 * @return the value suggested (typically an expression String),
	 * or {@code null} if none found - 建议的值（通常是表达式String），如果没有找到则为null
	 * @since 3.0
	 */
	Object getSuggestedValue(DependencyDescriptor descriptor);

}
