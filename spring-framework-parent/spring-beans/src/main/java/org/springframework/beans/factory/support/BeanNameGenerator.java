/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Strategy interface for generating bean names for bean definitions.
 * 
 * <p> 用于为bean定义生成bean名称的策略接口。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
public interface BeanNameGenerator {

	/**
	 * Generate a bean name for the given bean definition.
	 * 
	 * <p> 为给定的bean定义生成bean名称。
	 * 
	 * @param definition the bean definition to generate a name for
	 * 
	 * <p> 用于生成名称的bean定义
	 * 
	 * @param registry the bean definition registry that the given definition
	 * is supposed to be registered with
	 * 
	 * <p> 应该定义给定定义的bean定义注册表
	 * 
	 * @return the generated bean name - 生成的bean名称
	 */
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
