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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

/**
 * Interface for registries that hold bean definitions, for example RootBeanDefinition
 * and ChildBeanDefinition instances. Typically implemented by BeanFactories that
 * internally work with the AbstractBeanDefinition hierarchy.
 * 
 * <p>包含bean定义的注册表的接口，例如RootBeanDefinition和ChildBeanDefinition实例。 
 * 通常由BeanFactories实现，BeanFactories内部使用AbstractBeanDefinition层次结构。
 * 
 *
 * <p>This is the only interface in Spring's bean factory packages that encapsulates
 * <i>registration</i> of bean definitions. The standard BeanFactory interfaces
 * only cover access to a <i>fully configured factory instance</i>.
 * 
 * <p>这是Spring的bean工厂包中唯一封装bean定义注册的接口。 标准BeanFactory接口仅涵盖对完全配置的工厂实例的访问。
 *
 * <p>Spring's bean definition readers expect to work on an implementation of this
 * interface. Known implementors within the Spring core are DefaultListableBeanFactory
 * and GenericApplicationContext.
 * 
 * <p>Spring的bean定义读者期望在这个接口的实现上工作。 Spring核心中的已知实现者是DefaultListableBeanFactory
 * 和GenericApplicationContext。
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
 */
public interface BeanDefinitionRegistry extends AliasRegistry {

	/**
	 * Register a new bean definition with this registry.
	 * Must support RootBeanDefinition and ChildBeanDefinition.
	 * 
	 * <p>使用此注册表注册新的bean定义。 必须支持RootBeanDefinition和ChildBeanDefinition。
	 * 
	 * @param beanName the name of the bean instance to register - 要注册的bean实例的名称
	 * @param beanDefinition definition of the bean instance to register - 要注册的bean实例的定义
	 * @throws BeanDefinitionStoreException if the BeanDefinition is invalid
	 * or if there is already a BeanDefinition for the specified bean name
	 * (and we are not allowed to override it)
	 * 
	 * <p>如果BeanDefinition无效或者已经有指定bean名称的BeanDefinition（并且我们不允许覆盖它）
	 * 
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	/**
	 * Remove the BeanDefinition for the given name.
	 * 
	 * <p>删除给定名称的BeanDefinition。
	 * 
	 * @param beanName the name of the bean instance to register - 要注册的bean实例的名称
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition - 如果没有这样的bean定义
	 */
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Return the BeanDefinition for the given bean name.
	 * 
	 * <p>返回给定bean名称的BeanDefinition。
	 * 
	 * @param beanName name of the bean to find a definition for - 要查找其定义的bean的名称
	 * @return the BeanDefinition for the given name (never {@code null}) - 给定名称的BeanDefinition（永远不为null）
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition - 如果没有这样的bean定义
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Check if this registry contains a bean definition with the given name.
	 * 
	 * <p>检查此注册表是否包含具有给定名称的bean定义。
	 * 
	 * @param beanName the name of the bean to look for - 要查找的bean的名称
	 * @return if this registry contains a bean definition with the given name - 如果此注册表包含具有给定名称的bean定义
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * Return the names of all beans defined in this registry.
	 * 
	 * <p>返回此注册表中定义的所有bean的名称。
	 * 
	 * @return the names of all beans defined in this registry,
	 * or an empty array if none defined
	 * 
	 * <p>此注册表中定义的所有bean的名称，如果没有定义，则为空数组
	 * 
	 */
	String[] getBeanDefinitionNames();

	/**
	 * Return the number of beans defined in the registry. - 返回注册表中定义的bean数。
	 * @return the number of beans defined in the registry - 注册表中定义的bean数量
	 */
	int getBeanDefinitionCount();

	/**
	 * Determine whether the given bean name is already in use within this registry,
	 * i.e. whether there is a local bean or alias registered under this name.
	 * 
	 * <p>确定给定的bean名称是否已在此注册表中使用，即是否存在以此名称注册的本地bean或别名。
	 * 
	 * @param beanName the name to check - 要检查的名称
	 * @return whether the given bean name is already in use - 是否已使用给定的bean名称
	 */
	boolean isBeanNameInUse(String beanName);

}
