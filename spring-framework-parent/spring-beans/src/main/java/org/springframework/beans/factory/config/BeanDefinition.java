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

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 * 
 * <p>BeanDefinition描述了一个bean实例，它具有属性值，构造函数参数值以及具体实现提供的更多信息。
 *
 * <p>This is just a minimal interface: The main intention is to allow a
 * {@link BeanFactoryPostProcessor} such as {@link PropertyPlaceholderConfigurer}
 * to introspect and modify property values and other bean metadata.
 * 
 * <p>这只是一个最小的接口：主要目的是允许BeanFactoryPostProcessor（
 * 如PropertyPlaceholderConfigurer）内省和修改属性值和其他bean元数据。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	/**
	 * Scope identifier for the standard singleton scope: "singleton".
	 * 
	 * <p>标准单例范围的范围标识符：“singleton”。
	 * 
	 * <p>Note that extended bean factories might support further scopes.
	 * 
	 * <p>请注意，扩展bean工厂可能支持更多范围。
	 * @see #setScope
	 */
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

	/**
	 * Scope identifier for the standard prototype scope: "prototype".
	 * 
	 * <p>标准原型范围的范围标识符：“prototype”。
	 * 
	 * <p>Note that extended bean factories might support further scopes.
	 * 
	 * <p>请注意，扩展bean工厂可能支持更多范围。
	 * 
	 * @see #setScope
	 */
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;


	/**
	 * Role hint indicating that a {@code BeanDefinition} is a major part
	 * of the application. Typically corresponds to a user-defined bean.
	 * 
	 * <p>角色提示，指示BeanDefinition是应用程序的主要部分。 通常对应于用户定义的bean。
	 * 
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is a supporting
	 * part of some larger configuration, typically an outer
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 * {@code SUPPORT} beans are considered important enough to be aware
	 * of when looking more closely at a particular
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition},
	 * but not when looking at the overall configuration of an application.
	 * 
	 * <p> 角色提示，指示BeanDefinition是某些较大配置的支持部分，通常是外部
	 * org.springframework.beans.factory.parsing.ComponentDefinition。
	 *  当仔细查看特定的org.springframework.beans.factory.parsing.ComponentDefinition时，
	 *  支持bean被认为是非常重要的，但在查看应用程序的整体配置时则不然。
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is providing an
	 * entirely background role and has no relevance to the end-user. This hint is
	 * used when registering beans that are completely part of the internal workings
	 * of a {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 * 
	 * <p>角色提示，指示BeanDefinition提供完全后台角色并且与最终用户无关。 注册bean时，使用此提示，
	 * 这些bean完全是org.springframework.beans.factory.parsing.ComponentDefinition的内部工作的一部分。
	 * 
	 */
	int ROLE_INFRASTRUCTURE = 2;


	/**
	 * Return the name of the parent definition of this bean definition, if any.
	 * 
	 * <p>返回此bean定义的父定义的名称（如果有）。
	 */
	String getParentName();

	/**
	 * Set the name of the parent definition of this bean definition, if any.
	 * 
	 * <p>设置此bean定义的父定义的名称（如果有）。
	 * 
	 */
	void setParentName(String parentName);

	/**
	 * Return the current bean class name of this bean definition.
	 * 
	 * <p> 返回此bean定义的当前bean类名。
	 * 
	 * <p>Note that this does not have to be the actual class name used at runtime, in
	 * case of a child definition overriding/inheriting the class name from its parent.
	 * Hence, do <i>not</i> consider this to be the definitive bean type at runtime but
	 * rather only use it for parsing purposes at the individual bean definition level.
	 * 
	 * <p> 请注意，如果子定义从其父级覆盖/继承类名，则不必是运行时使用的实际类名。 
	 * 因此，不要在运行时将其视为最终的bean类型，而是仅在单个bean定义级别将其用于解析目的。
	 */
	String getBeanClassName();

	/**
	 * Override the bean class name of this bean definition.
	 * 
	 * <p>返回此bean定义的当前bean类名。
	 * 
	 * <p>The class name can be modified during bean factory post-processing,
	 * typically replacing the original class name with a parsed variant of it.
	 * 
	 * <p>请注意，如果子定义从其父级覆盖/继承类名，则不必是运行时使用的实际类名。 因此，不要在运行时将其视为最
	 * 终的bean类型，而是仅在单个bean定义级别将其用于解析目的。
	 * 
	 */
	void setBeanClassName(String beanClassName);

	/**
	 * Return the factory bean name, if any.
	 * 
	 * <p>返回工厂bean名称（如果有）。
	 */
	String getFactoryBeanName();

	/**
	 * Specify the factory bean to use, if any.
	 * 
	 * <p>指定要使用的工厂bean（如果有）。
	 * 
	 */
	void setFactoryBeanName(String factoryBeanName);

	/**
	 * Return a factory method, if any.
	 * 
	 * <p>返回工厂方法（如果有）。
	 * 
	 */
	String getFactoryMethodName();

	/**
	 * Specify a factory method, if any. This method will be invoked with
	 * constructor arguments, or with no arguments if none are specified.
	 * The method will be invoked on the specified factory bean, if any,
	 * or otherwise as a static method on the local bean class.
	 * 
	 * <p.指定工厂方法（如果有）。 将使用构造函数参数调用此方法，如果未指定任何参数，则不使用参数调用此方法。
	 *  该方法将在指定的工厂bean（如果有）上调用，或者作为本地bean类的静态方法调用。
	 * 
	 * @param factoryMethodName static factory method name,
	 * or {@code null} if normal constructor creation should be used
	 * 
	 * <p>静态工厂方法名称，如果应使用正常的构造函数创建，则为null
	 * 
	 * @see #getBeanClassName()
	 */
	void setFactoryMethodName(String factoryMethodName);

	/**
	 * Return the name of the current target scope for this bean,
	 * or {@code null} if not known yet.
	 */
	String getScope();

	/**
	 * Override the target scope of this bean, specifying a new scope name.
	 * 
	 * <p>返回此bean的当前目标作用域的名称，如果尚未知，则返回null。
	 * 
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	void setScope(String scope);

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 * 
	 * <p>返回是否应该懒惰地初始化此bean，即在启动时不急切实例化。 仅适用于单例bean。
	 * 
	 */
	boolean isLazyInit();

	/**
	 * Set whether this bean should be lazily initialized.
	 * 
	 * <p>设置是否应该懒惰地初始化此bean。
	 * 
	 * <p>If {@code false}, the bean will get instantiated on startup by bean
	 * factories that perform eager initialization of singletons.
	 * 
	 * <p>如果为false，那么bean将在启动时由bean工厂实例化，这些工厂执行单例的初始化。
	 * 
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * Return the bean names that this bean depends on.
	 * 
	 * <p>返回此bean依赖的bean名称。
	 * 
	 */
	String[] getDependsOn();

	/**
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized first.
	 * 
	 * <p>设置此bean依赖于初始化的bean的名称。 bean工厂将保证首先初始化这些bean。
	 * 
	 */
	void setDependsOn(String[] dependsOn);

	/**
	 * Return whether this bean is a candidate for getting autowired into some other bean.
	 * 
	 * <p>返回此bean是否可以自动连接到其他bean中。
	 * 
	 */
	boolean isAutowireCandidate();

	/**
	 * Set whether this bean is a candidate for getting autowired into some other bean.
	 * 
	 * <p>设置此bean是否可以自动连接到其他bean。
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * Return whether this bean is a primary autowire candidate.
	 * If this value is true for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 * 
	 * <p>返回此bean是否是主要的autowire候选者。 如果这个值对于多个匹配的候选者中的一个bean来说是真的，那么它将作为打破平局。
	 * 
	 */
	boolean isPrimary();

	/**
	 * Set whether this bean is a primary autowire candidate.
	 * <p>If this value is true for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 */
	void setPrimary(boolean primary);


	/**
	 * Return the constructor argument values for this bean.
	 * 
	 * <p>设置此bean是否为主要autowire候选者。
	 * 
	 * <p>The returned instance can be modified during bean factory post-processing.
	 * @return the ConstructorArgumentValues object (never {@code null})
	 * 
	 * <p>如果这个值对于多个匹配的候选者中的一个bean来说是真的，那么它将作为打破平局。
	 * 
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * Return the property values to be applied to a new instance of the bean.
	 * 
	 * <p>返回要应用于bean的新实例的属性值。
	 * 
	 * <p>The returned instance can be modified during bean factory post-processing.
	 * 
	 * <p> 可以在bean工厂后处理期间修改返回的实例。
	 * 
	 * @return the MutablePropertyValues object (never {@code null})
	 * 
	 * <p>MutablePropertyValues对象（永不为null）
	 * 
	 */
	MutablePropertyValues getPropertyValues();


	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 * 
	 * <p>返回是否为Singleton，在所有调用中返回单个共享实例。
	 * 
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * 
	 * <p>返回是否为Prototype，每次调用返回一个独立实例。
	 * 
	 * @see #SCOPE_PROTOTYPE
	 */
	boolean isPrototype();

	/**
	 * Return whether this bean is "abstract", that is, not meant to be instantiated.
	 * 
	 * <p>返回此bean是否为“抽象”，即不是要实例化。
	 * 
	 */
	boolean isAbstract();

	/**
	 * Get the role hint for this {@code BeanDefinition}. The role hint
	 * provides tools with an indication of the importance of a particular
	 * {@code BeanDefinition}.
	 * 
	 * <p>获取此BeanDefinition的角色提示。 角色提示为工具提供了特定BeanDefinition重要性的指示。
	 * 
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_INFRASTRUCTURE
	 * @see #ROLE_SUPPORT
	 */
	int getRole();

	/**
	 * Return a human-readable description of this bean definition.
	 * 
	 * <p>返回此bean定义的可读描述。
	 * 
	 */
	String getDescription();

	/**
	 * Return a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 * 
	 * <p>返回此bean定义来源的资源的描述（为了在出现错误时显示上下文）。
	 * 
	 */
	String getResourceDescription();

	/**
	 * Return the originating BeanDefinition, or {@code null} if none.
	 * Allows for retrieving the decorated bean definition, if any.
	 * 
	 * <p>返回原始BeanDefinition，如果没有则返回null。 允许检索修饰的bean定义（如果有）。
	 * 
	 * <p>Note that this method returns the immediate originator. Iterate through the
	 * originator chain to find the original BeanDefinition as defined by the user.
	 * 
	 * <p>请注意，此方法返回直接发起者。 遍历创建者链以查找用户定义的原始BeanDefinition。
	 * 
	 */
	BeanDefinition getOriginatingBeanDefinition();

}
