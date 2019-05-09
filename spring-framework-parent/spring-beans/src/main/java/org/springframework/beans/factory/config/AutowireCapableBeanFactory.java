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

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;

/**
 * Extension of the {@link org.springframework.beans.factory.BeanFactory}
 * interface to be implemented by bean factories that are capable of
 * autowiring, provided that they want to expose this functionality for
 * existing bean instances.
 * 
 * <p>org.springframework.beans.factory.BeanFactory接口的扩展将由能够自动装配的bean工厂实
 * 现，前提是他们希望为现有bean实例公开此功能。
 * 
 *
 * <p>This subinterface of BeanFactory is not meant to be used in normal
 * application code: stick to {@link org.springframework.beans.factory.BeanFactory}
 * or {@link org.springframework.beans.factory.ListableBeanFactory} for
 * typical use cases.
 * 
 * <p>BeanFactory的这个子接口并不适用于普通的应用程序代码：对于典型的用例，请坚持使
 * 用org.springframework.beans.factory.BeanFactory
 * 或org.springframework.beans.factory.ListableBeanFactory。
 *
 * <p>Integration code for other frameworks can leverage this interface to
 * wire and populate existing bean instances that Spring does not control
 * the lifecycle of. This is particularly useful for WebWork Actions and
 * Tapestry Page objects, for example.
 * 
 * <p>其他框架的集成代码可以利用此接口来连接和填充Spring无法控制其生命周期的现有Bean实例。例如，这
 * 对WebWork Actions和Tapestry Page对象特别有用。
 * 
 *
 * <p>Note that this interface is not implemented by
 * {@link org.springframework.context.ApplicationContext} facades,
 * as it is hardly ever used by application code. That said, it is available
 * from an application context too, accessible through ApplicationContext's
 * {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()}
 * method.
 * 
 * <p>请注意，此接口不是由org.springframework.context.ApplicationContext外观实现的，因为应用程序代码几乎不使用它。
 * 也就是说，它也可以从应用程序上下文中获得，可以通过ApplicationContext
 * 的org.springframework.context.ApplicationContext.getAutowireCapableBeanFactory（）方法访问。
 * 
 *
 * <p>You may also implement the {@link org.springframework.beans.factory.BeanFactoryAware}
 * interface, which exposes the internal BeanFactory even when running in an
 * ApplicationContext, to get access to an AutowireCapableBeanFactory:
 * simply cast the passed-in BeanFactory to AutowireCapableBeanFactory.
 * 
 * <p>您还可以实现org.springframework.beans.factory.BeanFactoryAware接口，该接口即使
 * 在ApplicationContext中运行时也会公开内部BeanFactory，以访问AutowireCapableBeanFactory：只需将传
 * 入的BeanFactory强制转换为AutowireCapableBeanFactory。
 * 
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * Constant that indicates no externally defined autowiring. Note that
	 * BeanFactoryAware etc and annotation-driven injection will still be applied.
	 * 
	 * <p>常量，表示没有外部定义的自动装配。 请注意，仍将应用BeanFactoryAware等和注释驱动的注入。
	 * 
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_NO = 0;

	/**
	 * Constant that indicates autowiring bean properties by name
	 * (applying to all bean property setters).
	 * 
	 * <p>常量，表示按名称自动装配bean属性（应用于所有bean属性设置器）。
	 * 
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring bean properties by type
	 * (applying to all bean property setters).
	 * 
	 * <p>指示按类型自动装配bean属性的常量（适用于所有bean属性设置器）。
	 * 
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * Constant that indicates autowiring the greediest constructor that
	 * can be satisfied (involves resolving the appropriate constructor).
	 * 
	 * <p>指示自动装配可以满足的最贪婪构造函数的常量（涉及解析适当的构造函数）。
	 * 
	 * @see #createBean
	 * @see #autowire
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * 
	 * <p>常量，表示通过内省bean类确定适当的自动线路策略。
	 * 
	 * @see #createBean
	 * @see #autowire
	 * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
	 * prefer annotation-based autowiring for clearer demarcation of autowiring needs.
	 * 
	 * <p>从Spring 3.0开始：如果您使用混合自动装配策略，则更喜欢基于注释的自动装配，以更清晰地划分自动装配需求。
	 * 
	 */
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;


	//-------------------------------------------------------------------------
	// Typical methods for creating and populating external bean instances
	// 用于创建和填充外部bean实例的典型方法
	//-------------------------------------------------------------------------

	/**
	 * Fully create a new bean instance of the given class.
	 * 
	 * <p>完全创建给定类的新bean实例。
	 * 
	 * <p>Performs full initialization of the bean, including all applicable
	 * {@link BeanPostProcessor BeanPostProcessors}.
	 * 
	 * <p>执行bean的完全初始化，包括所有适用的BeanPostProcessors。
	 * 
	 * <p>Note: This is intended for creating a fresh instance, populating annotated
	 * fields and methods as well as applying all standard bean initialiation callbacks.
	 * It does <i>not</> imply traditional by-name or by-type autowiring of properties;
	 * use {@link #createBean(Class, int, boolean)} for that purposes.
	 * 
	 * <p>注意：这用于创建新实例，填充带注释的字段和方法以及应用所有标准bean初始化回调。 它并不意味着传统的名称或类型的自动装配属性; 
	 * 为此目的使用createBean（Class，int，boolean）。
	 * 
	 * @param beanClass the class of the bean to create - 要创建的bean的类
	 * @return the new bean instance - 新的bean实例
	 * @throws BeansException if instantiation or wiring failed - 如果实例化或装配失败
	 */
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * Populate the given bean instance through applying after-instantiation callbacks
	 * and bean property post-processing (e.g. for annotation-driven injection).
	 * 
	 * <p>通过应用后实例化回调和bean属性后处理（例如，用于注释驱动注入）来填充给定的bean实例。
	 * 
	 * <p>Note: This is essentially intended for (re-)populating annotated fields and
	 * methods, either for new instances or for deserialized instances. It does
	 * <i>not</i> imply traditional by-name or by-type autowiring of properties;
	 * use {@link #autowireBeanProperties} for that purposes.
	 * 
	 * <p>注意：这主要用于（重新）填充带注释的字段和方法，用于新实例或反序列化实例。 它并不意味着传统的名称或类型的自动装配属性; 
	 * 为此目的使用autowireBeanProperties。
	 * 
	 * @param existingBean the existing bean instance - 现有的bean实例
	 * @throws BeansException if wiring failed - 如果装配失败
	 */
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * Configure the given raw bean: autowiring bean properties, applying
	 * bean property values, applying factory callbacks such as {@code setBeanName}
	 * and {@code setBeanFactory}, and also applying all bean post processors
	 * (including ones which might wrap the given raw bean).
	 * 
	 * <p>配置给定的原始bean：autowiring bean属性，应用bean属性值，应用工厂回调
	 * （如setBeanName和setBeanFactory），还应用所有bean后处理器（包括可能包装给定原始bean的bean）。
	 * 
	 * <p>This is effectively a superset of what {@link #initializeBean} provides,
	 * fully applying the configuration specified by the corresponding bean definition.
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * 
	 * <p>这实际上是initializeBean提供的超集，完全应用相应bean定义指定的配置。 注意：此方法需要给定名称的bean定义！
	 * 
	 * @param existingBean the existing bean instance - 现有的bean实例
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (a bean definition of that name has to be available)
	 * 
	 * <p>要在必要时传递给它的bean的名称（必须提供该名称的bean定义）
	 * 
	 * @return the bean instance to use, either the original or a wrapped one 
	 * 
	 * <p>要使用的bean实例，无论是原始实例还是包装实例
	 * 
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name
	 * 
	 * <p>如果没有给定名称的bean定义
	 * 
	 * @throws BeansException if the initialization failed - 如果初始化失败
	 * @see #initializeBean
	 */
	Object configureBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * Resolve the specified dependency against the beans defined in this factory.
	 * 
	 * <p>针对此工厂中定义的bean解析指定的依赖项。
	 * 
	 * @param descriptor the descriptor for the dependency - 依赖的描述符
	 * @param beanName the name of the bean which declares the present dependency - 声明当前依赖项的bean的名称
	 * @return the resolved object, or {@code null} if none found - 已解析的对象，如果没有找到则为null
	 * @throws BeansException in dependency resolution failed - 在依赖解析失败
	 */
	Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException;


	//-------------------------------------------------------------------------
	// Specialized methods for fine-grained control over the bean lifecycle
	// 用于细粒度控制bean生命周期的专用方法
	//-------------------------------------------------------------------------

	/**
	 * Fully create a new bean instance of the given class with the specified
	 * autowire strategy. All constants defined in this interface are supported here.
	 * 
	 * <p>使用指定的autowire策略完全创建给定类的新bean实例。 此处支持此接口中定义的所有常量。
	 * 
	 * <p>Performs full initialization of the bean, including all applicable
	 * {@link BeanPostProcessor BeanPostProcessors}. This is effectively a superset
	 * of what {@link #autowire} provides, adding {@link #initializeBean} behavior.
	 * 
	 * <p>执行bean的完全初始化，包括所有适用的BeanPostProcessors。 这实际上是autowire提供的超
	 * 集，添加了initializeBean行为。
	 * 
	 * @param beanClass the class of the bean to create - 要创建的bean的类
	 * @param autowireMode by name or type, using the constants in this interface - 按名称或类型，使用此接口中的常量
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 * 
	 * <p>是否对对象执行依赖性检查（不适用于自动装配构造函数，因此在那里忽略）
	 * 
	 * @return the new bean instance - 新的bean实例
	 * @throws BeansException if instantiation or wiring failed - 如果实例化或装配失败
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 */
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * Instantiate a new bean instance of the given class with the specified autowire
	 * strategy. All constants defined in this interface are supported here.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * before-instantiation callbacks (e.g. for annotation-driven injection).
	 * 
	 * <p>使用指定的autowire策略实例化给定类的新bean实例。 此处支持此接口中定义的所有常量。 也可以使用AUTOWIRE_NO调
	 * 用，以便仅应用实例化之前的回调（例如，用于注释驱动的注入）。
	 * 
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the construction of the instance.
	 * 
	 * <p>不应用标准BeanPostProcessors回调或执行bean的任何进一步初始化。 此接口为这些目的提供了不同的细粒度操作，例如initializeBean。 
	 * 但是，如果适用于构造实例，则应用InstantiationAwareBeanPostProcessor回调。
	 * 
	 * @param beanClass the class of the bean to instantiate - 要实例化的bean的类
	 * @param autowireMode by name or type, using the constants in this interface - 按名称或类型，使用此接口中的常量
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance (not applicable to autowiring a constructor,
	 * thus ignored there)
	 * <p>是否对bean实例中的对象引用执行依赖性检查（不适用于自动装配构造函数，因此在那里忽略）
	 * @return the new bean instance - 新的bean实例
	 * @throws BeansException if instantiation or wiring failed - 如果实例化或装配失败
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * Autowire the bean properties of the given bean instance by name or type.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * after-instantiation callbacks (e.g. for annotation-driven injection).
	 * 
	 * <p>按名称或类型自动装配给定bean实例的bean属性。 也可以使用AUTOWIRE_NO调用，以便仅应用后实例化回
	 * 调（例如，用于注释驱动的注入）。
	 * 
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * 
	 * <p>不应用标准BeanPostProcessors回调或执行bean的任何进一步初始化。 此接口为这些目的提供了不同的细粒度操
	 * 作，例如initializeBean。 但是，如果适用于实例的配置，则应用InstantiationAwareBeanPostProcessor回调。
	 * 
	 * @param existingBean the existing bean instance - 现有的bean实例
	 * @param autowireMode by name or type, using the constants in this interface - 按名称或类型，使用此接口中的常量
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance - 是否对bean实例中的对象引用执行依赖性检查
	 * @throws BeansException if wiring failed 如果装配失败
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Apply the property values of the bean definition with the given name to
	 * the given bean instance. The bean definition can either define a fully
	 * self-contained bean, reusing its property values, or just property values
	 * meant to be used for existing bean instances.
	 * 
	 * <p>将具有给定名称的bean定义的属性值应用于给定的bean实例。 bean定义可以定义完全自包含的bean，重用其属
	 * 性值，也可以只定义用于现有bean实例的属性值。
	 * 
	 * <p>This method does <i>not</i> autowire bean properties; it just applies
	 * explicitly defined property values. Use the {@link #autowireBeanProperties}
	 * method to autowire an existing bean instance.
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * 
	 * <p>此方法不会自动装配bean属性; 它只是应用明确定义的属性值。 使用autowireBeanProperties方法自动装
	 * 配现有Bean实例。 注意：此方法需要给定名称的bean定义！
	 * 
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * 
	 * <p>不应用标准BeanPostProcessors回调或执行bean的任何进一步初始化。 此接口为这些目的提供了不同的细粒度操作，
	 * 例如initializeBean。 但是，如果适用于实例的配置，则应用InstantiationAwareBeanPostProcessor回调。
	 * 
	 * @param existingBean the existing bean instance - 现有的bean实例
	 * @param beanName the name of the bean definition in the bean factory
	 * (a bean definition of that name has to be available)
	 * 
	 * <p>bean工厂中bean定义的名称（该名称的bean定义必须可用）
	 * 
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name - 如果没有给定名称的bean定义
	 * @throws BeansException if applying the property values failed - 如果应用属性值失败
	 * @see #autowireBeanProperties
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	/**
	 * Initialize the given raw bean, applying factory callbacks
	 * such as {@code setBeanName} and {@code setBeanFactory},
	 * also applying all bean post processors (including ones which
	 * might wrap the given raw bean).
	 * 
	 * <p>初始化给定的原始bean，应用工厂回调，如setBeanName和setBeanFactory，也应用所
	 * 有bean后处理器（包括可能包装给定原始bean的那些）。
	 * 
	 * <p>Note that no bean definition of the given name has to exist
	 * in the bean factory. The passed-in bean name will simply be used
	 * for callbacks but not checked against the registered bean definitions.
	 * 
	 * <p>请注意，bean工厂中不必存在给定名称的bean定义。 传入的bean名称将仅用于回调，但不会根据已注册的bean定义进行检查。
	 * 
	 * @param existingBean the existing bean instance - 现有的bean实例
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors})
	 * 
	 * <p>bean的名称，必要时传递给它（仅传递给BeanPostProcessors）
	 * 
	 * @return the bean instance to use, either the original or a wrapped one
	 * 
	 * <p>要使用的bean实例，无论是原始实例还是包装实例
	 * 
	 * @throws BeansException if the initialization failed - 如果初始化失败
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessBeforeInitialization} methods.
	 * The returned bean instance may be a wrapper around the original.
	 * 
	 * <p>将BeanPostProcessors应用于给定的现有bean实例，并调用其postProcessBeforeInitialization方法。 
	 * 返回的bean实例可能是原始实例的包装器。
	 * 
	 * @param existingBean the new bean instance - 新的bean实例
	 * @param beanName the name of the bean - bean的名称
	 * @return the bean instance to use, either the original or a wrapped one - 要使用的bean实例，无论是原始实例还是包装实例
	 * @throws BeansException if any post-processing failed - 如果任何后处理失败
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessAfterInitialization} methods.
	 * The returned bean instance may be a wrapper around the original.
	 * 
	 * <p>将BeanPostProcessors应用于给定的现有bean实例，并调用其postProcessAfterInitialization方法。 
	 * 返回的bean实例可能是原始实例的包装器。
	 * 
	 * @param existingBean the new bean instance - 新的bean实例
	 * @param beanName the name of the bean - bean的名称
	 * @return the bean instance to use, either the original or a wrapped one - 要使用的bean实例，无论是原始实例还是包装实例
	 * @throws BeansException if any post-processing failed - 如果任何后面处理失败
	 * @see BeanPostProcessor#postProcessAfterInitialization
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * Resolve the specified dependency against the beans defined in this factory.
	 * 
	 * <p>针对此工厂中定义的bean解析指定的依赖项。
	 * @param descriptor the descriptor for the dependency - 依赖的描述符
	 * @param beanName the name of the bean which declares the present dependency
	 * 
	 * <p>声明当前依赖项的bean的名称
	 * 
	 * @param autowiredBeanNames a Set that all names of autowired beans (used for
	 * resolving the present dependency) are supposed to be added to
	 * 
	 * <p>a应该添加自动装配bean的所有名称（用于解析当前依赖关系）的Set
	 * 
	 * @param typeConverter the TypeConverter to use for populating arrays and
	 * collections - TypeConverter用于填充数组和集合
	 * @return the resolved object, or {@code null} if none found - 已解析的对象，如果没有找到则为null
	 * @throws BeansException in dependency resolution failed - 在依赖解析失败
	 */
	Object resolveDependency(DependencyDescriptor descriptor, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException;

}
