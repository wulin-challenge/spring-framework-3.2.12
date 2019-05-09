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
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Configuration interface to be implemented by most listable bean factories.
 * In addition to {@link ConfigurableBeanFactory}, it provides facilities to
 * analyze and modify bean definitions, and to pre-instantiate singletons.
 * 
 * <p>配置接口由大多数可列出的bean工厂实现。 除了ConfigurableBeanFactory之外，它还提供了分析和修改bean定义以及预先实例
 * 化单例的工具。
 *
 * <p>This subinterface of {@link org.springframework.beans.factory.BeanFactory}
 * is not meant to be used in normal application code: Stick to
 * {@link org.springframework.beans.factory.BeanFactory} or
 * {@link org.springframework.beans.factory.ListableBeanFactory} for typical
 * use cases. This interface is just meant to allow for framework-internal
 * plug'n'play even when needing access to bean factory configuration methods.
 * 
 * <p>org.springframework.beans.factory.BeanFactory的这个子接口并不适用于普通的应用程序代码：对于典型的用
 * 例，请坚持使用org.springframework.beans.factory.BeanFactory
 * 或org.springframework.beans.factory.ListableBeanFactory。 这个接口只是为了允许框架内部的即插即用，即
 * 使在需要访问bean工厂配置方法时也是如此。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory()
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	/**
	 * Ignore the given dependency type for autowiring:
	 * for example, String. Default is none.
	 * 
	 * <p>忽略自动装配的给定依赖关系类型：例如，String。 默认为none。
	 * @param type the dependency type to ignore - 要忽略的依赖类型
	 */
	void ignoreDependencyType(Class<?> type);

	/**
	 * Ignore the given dependency interface for autowiring.
	 *
	 * <p>忽略给定的自动装配依赖关系接口。
	 * 
	 * <p>This will typically be used by application contexts to register
	 * dependencies that are resolved in other ways, like BeanFactory through
	 * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
	 * 
	 * <p>这通常由应用程序上下文用于注册以其他方式解析的依赖关系，例如BeanFactory通
	 * 过BeanFactoryAware或ApplicationContext通过ApplicationContextAware。
	 * 
	 * <p>By default, only the BeanFactoryAware interface is ignored.
	 * For further types to ignore, invoke this method for each type.
	 * 
	 * <p>默认情况下，仅忽略BeanFactoryAware接口。 要忽略其他类型，请为每种类型调用此方法。
	 * 
	 * @param ifc the dependency interface to ignore - 要忽略的依赖接口
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyInterface(Class<?> ifc);

	/**
	 * Register a special dependency type with corresponding autowired value.
	 * 
	 * <p>使用相应的自动装配值注册特殊依赖关系类型。
	 * 
	 * <p>This is intended for factory/context references that are supposed
	 * to be autowirable but are not defined as beans in the factory:
	 * e.g. a dependency of type ApplicationContext resolved to the
	 * ApplicationContext instance that the bean is living in.
	 * 
	 * <p>这适用于应该是可自动操作但在工厂中未定义为bean的工厂/上下文引用：例
	 * 如 ApplicationContext类型的依赖关系解析为bean所在的ApplicationContext实例。
	 * 
	 * <p>Note: There are no such default types registered in a plain BeanFactory,
	 * not even for the BeanFactory interface itself.
	 * 
	 * <p>注意：在简单的BeanFactory中没有注册这样的默认类型，即使BeanFactory接口本身也没有。
	 * 
	 * @param dependencyType the dependency type to register. This will typically
	 * be a base interface such as BeanFactory, with extensions of it resolved
	 * as well if declared as an autowiring dependency (e.g. ListableBeanFactory),
	 * as long as the given value actually implements the extended interface.
	 * 
	 * <p>要注册的依赖类型。 这通常是一个基本接口，例如BeanFactory，只要给定值实际实现扩展接口，如果声明为自动装
	 * 配依赖（例如ListableBeanFactory），它的扩展也会被解析。
	 * 
	 * @param autowiredValue the corresponding autowired value. This may also be an
	 * implementation of the {@link org.springframework.beans.factory.ObjectFactory}
	 * interface, which allows for lazy resolution of the actual target value.
	 * 
	 * <p>相应的自动装配值。 这也可以是org.springframework.beans.factory.ObjectFactory接口的实现，它允许实际目标值的延迟解析。
	 * 
	 */
	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);

	/**
	 * Determine whether the specified bean qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * 
	 * <p>确定指定的bean是否有资格作为autowire候选者，注入到声明匹配类型依赖关系的其他bean中。
	 * 
	 * <p>This method checks ancestor factories as well. - 此方法也会检查祖先工厂。
	 * @param beanName the name of the bean to check - 要检查的bean的名称
	 * @param descriptor the descriptor of the dependency to resolve - 要解析的依赖项的描述符
	 * @return whether the bean should be considered as autowire candidate - 该bean是否应被视为autowire候选者
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name - 如果没有给定名称的bean
	 */
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException;

	/**
	 * Return the registered BeanDefinition for the specified bean, allowing access
	 * to its property values and constructor argument value (which can be
	 * modified during bean factory post-processing).
	 * 
	 * <p>返回指定bean的已注册BeanDefinition，允许访问其属性值和构造函数参数值（可以在bean工厂后处理期间修改）。
	 * 
	 * <p>A returned BeanDefinition object should not be a copy but the original
	 * definition object as registered in the factory. This means that it should
	 * be castable to a more specific implementation type, if necessary.
	 * 
	 * <p> 返回的BeanDefinition对象不应是副本，而应是在工厂中注册的原始定义对象。 这意味着如果需要，它应该可以转换为更具体的实现类型。
	 * 
	 * <p><b>NOTE:</b> This method does <i>not</i> consider ancestor factories.
	 * It is only meant for accessing local bean definitions of this factory.
	 * 
	 * <p>注意：此方法不考虑祖先工厂。 它仅用于访问此工厂的本地bean定义。
	 * 
	 * @param beanName the name of the bean
	 * @return the registered BeanDefinition - 注册的BeanDefinition
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * defined in this factory - 如果没有在此工厂中定义给定名称的bean
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Freeze all bean definitions, signalling that the registered bean definitions
	 * will not be modified or post-processed any further.
	 * 
	 * <p>冻结所有bean定义，表明注册的bean定义不会被修改或进一步后处理。
	 * 
	 * <p>This allows the factory to aggressively cache bean definition metadata.
	 * 
	 * <p>这允许工厂积极地缓存bean定义元数据。
	 */
	void freezeConfiguration();

	/**
	 * Return whether this factory's bean definitions are frozen,
	 * i.e. are not supposed to be modified or post-processed any further.
	 * 
	 * <p>返回该工厂的bean定义是否被冻结，即不应该进一步修改或后处理。
	 * 
	 * @return {@code true} if the factory's configuration is considered frozen
	 * 
	 * <p>如果工厂的配置被视为冻结，则为true
	 */
	boolean isConfigurationFrozen();

	/**
	 * Ensure that all non-lazy-init singletons are instantiated, also considering
	 * {@link org.springframework.beans.factory.FactoryBean FactoryBeans}.
	 * Typically invoked at the end of factory setup, if desired.
	 * 
	 * <p>确保所有非lazy-init单例都被实例化，同时考虑FactoryBeans。 如果需要，通常在出厂设置结束时调用。
	 * 
	 * @throws BeansException if one of the singleton beans could not be created.
	 * Note: This may have left the factory with some beans already initialized!
	 * Call {@link #destroySingletons()} for full cleanup in this case.
	 * 
	 * <p>如果无法创建其中一个单例bean。 注意：这可能已经离开了工厂，已经初始化了一些bean！ 在这种情况下，调
	 * 用destroySingletons（）进行完全清理。
	 * 
	 * @see #destroySingletons()
	 */
	void preInstantiateSingletons() throws BeansException;

}
