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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility methods that are useful for bean definition reader implementations.
 * Mainly intended for internal use.
 * 
 * <p> 对bean定义读取器实现有用的实用方法。 主要供内部使用。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
public class BeanDefinitionReaderUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 * 
	 * <p> 生成的bean名称的分隔符。 如果类名或父名不唯一，则将追加“＃1”，“＃2”等，直到名称变为唯一。
	 * 
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;


	/**
	 * Create a new GenericBeanDefinition for the given parent name and class name,
	 * eagerly loading the bean class if a ClassLoader has been specified.
	 * 
	 * <p> 为给定的父名和类名创建一个新的GenericBeanDefinition，如果指定了ClassLoader，则急切地加载bean类。
	 * @param parentName the name of the parent bean, if any - 父bean的名称（如果有）
	 * @param className the name of the bean class, if any - bean类的名称，如果有的话
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be {@code null} to just register bean classes by name)
	 * 
	 * <p> 用于加载bean类的ClassLoader（可以为null，只是按名称注册bean类）
	 * 
	 * @return the bean definition - bean的定义
	 * @throws ClassNotFoundException if the bean class could not be loaded - 如果无法加载bean类
	 */
	public static AbstractBeanDefinition createBeanDefinition(
			String parentName, String className, ClassLoader classLoader) throws ClassNotFoundException {

		GenericBeanDefinition bd = new GenericBeanDefinition();
		//parentName可能为空
		bd.setParentName(parentName);
		if (className != null) {
			if (classLoader != null) {
				//如果classLoader不为空,则使用以传入的classLoader同一虚拟机加载类对象,否则只是记录className
				bd.setBeanClass(ClassUtils.forName(className, classLoader));
			}
			else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	/**
	 * Generate a bean name for the given bean definition, unique within the
	 * given bean factory.
	 * 
	 * <p> 为给定的bean定义生成bean名称，在给定的bean工厂中是唯一的。
	 * 
	 * @param definition the bean definition to generate a bean name for - 用于生成bean名称的bean定义
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * 
	 * <p> 定义将要注册的bean工厂（检查现有的bean名称）
	 * 
	 * @param isInnerBean whether the given bean definition will be registered
	 * as inner bean or as top-level bean (allowing for special name generation
	 * for inner beans versus top-level beans)
	 * 
	 * <p> 是否将给定的bean定义注册为内部bean或顶级bean（允许内部bean与顶级bean的特殊名称生成）
	 * 
	 * @return the generated bean name - 生成的bean名称
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 * 
	 * <p> 如果没有为给定的bean定义生成唯一名称
	 * 
	 */
	public static String generateBeanName(
			BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean)
			throws BeanDefinitionStoreException {

		String generatedBeanName = definition.getBeanClassName();
		if (generatedBeanName == null) {
			if (definition.getParentName() != null) {
				generatedBeanName = definition.getParentName() + "$child";
			}
			else if (definition.getFactoryBeanName() != null) {
				generatedBeanName = definition.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedBeanName)) {
			throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
					"'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
		}

		String id = generatedBeanName;
		if (isInnerBean) {
			// Inner bean: generate identity hashcode suffix.
			id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
		}
		else {
			// Top-level bean: use plain class name.
			// Increase counter until the id is unique.
			int counter = -1;
			while (counter == -1 || registry.containsBeanDefinition(id)) {
				counter++;
				id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
			}
		}
		return id;
	}

	/**
	 * Generate a bean name for the given top-level bean definition,
	 * unique within the given bean factory.
	 * 
	 * <p> 为给定的顶级bean定义生成bean名称，在给定的bean工厂中是唯一的。
	 * 
	 * @param beanDefinition the bean definition to generate a bean name for - 用于生成bean名称的bean定义
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * 
	 * <p> 定义将要注册的bean工厂（检查现有的bean名称）
	 * 
	 * @return the generated bean name - 生成的bean名称
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 * 
	 * <p> 如果没有为给定的bean定义生成唯一名称
	 * 
	 */
	public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		return generateBeanName(beanDefinition, registry, false);
	}

	/**
	 * Register the given bean definition with the given bean factory.
	 * 
	 * <p> 使用给定的bean工厂注册给定的bean定义。
	 * 
	 * @param definitionHolder the bean definition including name and aliases - bean定义包括名称和别名
	 * @param registry the bean factory to register with - 注册的bean厂
	 * @throws BeanDefinitionStoreException if registration failed - 如果注册失败
	 */
	public static void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		// Register bean definition under primary name.
		// 在主名称下注册bean定义。
		
		//使用bean名称做唯一标识注册
		String beanName = definitionHolder.getBeanName();
		registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

		// Register aliases for bean name, if any.
		// 注册bean名称的别名（如果有）。
		
		//注册所有的别名
		String[] aliases = definitionHolder.getAliases();
		if (aliases != null) {
			for (String aliase : aliases) {
				registry.registerAlias(beanName, aliase);
			}
		}
	}

	/**
	 * Register the given bean definition with a generated name,
	 * unique within the given bean factory.
	 * 
	 * <p> 使用生成的名称注册给定的bean定义，在给定的bean工厂中是唯一的。
	 * 
	 * @param definition the bean definition to generate a bean name for - 用于生成bean名称的bean定义
	 * @param registry the bean factory to register with - 注册的bean厂
	 * @return the generated bean name - 生成的bean名称
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition or the definition cannot be registered
	 * 
	 * <p> 如果没有为给定的bean定义生成唯一名称，或者无法注册定义
	 * 
	 */
	public static String registerWithGeneratedName(
			AbstractBeanDefinition definition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		String generatedName = generateBeanName(definition, registry, false);
		registry.registerBeanDefinition(generatedName, definition);
		return generatedName;
	}

}
