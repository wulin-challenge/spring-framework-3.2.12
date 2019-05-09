/*
 * Copyright 2002-2013 the original author or authors.
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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.Assert;

/**
 * A root bean definition represents the merged bean definition that backs
 * a specific bean in a Spring BeanFactory at runtime. It might have been created
 * from multiple original bean definitions that inherit from each other,
 * typically registered as {@link GenericBeanDefinition GenericBeanDefinitions}.
 * A root bean definition is essentially the 'unified' bean definition view at runtime.
 * 
 * <p>根bean定义表示合并的bean定义，该定义在运行时支持Spring BeanFactory中的特定bean。 它可能是从多个原始bean定义创建的，
 * 这些定义相互继承，通常注册为GenericBeanDefinitions。 根bean定义本质上是运行时的“统一”bean定义视图。
 *
 * <p>Root bean definitions may also be used for registering individual bean definitions
 * in the configuration phase. However, since Spring 2.5, the preferred way to register
 * bean definitions programmatically is the {@link GenericBeanDefinition} class.
 * GenericBeanDefinition has the advantage that it allows to dynamically define
 * parent dependencies, not 'hard-coding' the role as a root bean definition.
 * 
 * <p>根bean定义也可用于在配置阶段注册单个bean定义。 但是，从Spring 2.5开始，以编程方式注册bean定义的首选方法是GenericBeanDefinition类。
 *  GenericBeanDefinition的优点是它允许动态定义父依赖关系，而不是将角色“硬编码”为根bean定义。
 * 
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	boolean allowCaching = true;

	private BeanDefinitionHolder decoratedDefinition;

	private volatile Class<?> targetType;

	boolean isFactoryMethodUnique = false;

	final Object constructorArgumentLock = new Object();

	/** Package-visible field for caching the resolved constructor or factory method */
	/** 包可见字段，用于缓存已解析的构造函数或工厂方法 */
	Object resolvedConstructorOrFactoryMethod;

	/** Package-visible field that marks the constructor arguments as resolved */
	/** 包可见字段，用于将构造函数参数标记为已解析 */
	boolean constructorArgumentsResolved = false;

	/** Package-visible field for caching fully resolved constructor arguments */
	/** 用于缓存完全解析的构造函数参数的包可见字段 */
	Object[] resolvedConstructorArguments;

	/** Package-visible field for caching partly prepared constructor arguments */
	/** 用于缓存部分准备的构造函数参数的包可见字段 */
	Object[] preparedConstructorArguments;

	final Object postProcessingLock = new Object();

	/** Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied */
	/** 包可见字段，指示已应用MergedBeanDefinitionPostProcessor */
	boolean postProcessed = false;

	/** Package-visible field that indicates a before-instantiation post-processor having kicked in */
	/** 包 - 可见字段，指示已经启动的实例化后处理器 */
	volatile Boolean beforeInstantiationResolved;

	private Set<Member> externallyManagedConfigMembers;

	private Set<String> externallyManagedInitMethods;

	private Set<String> externallyManagedDestroyMethods;


	/**
	 * Create a new RootBeanDefinition, to be configured through its bean
	 * properties and configuration methods.
	 * 
	 * <p> 创建一个新的RootBeanDefinition，通过其bean属性和配置方法进行配置。
	 * 
	 * @see #setBeanClass
	 * @see #setBeanClassName
	 * @see #setScope
	 * @see #setAutowireMode
	 * @see #setDependencyCheck
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public RootBeanDefinition() {
		super();
	}

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 * 
	 * <p> 为单例创建新的RootBeanDefinition。
	 * 
	 * @param beanClass the class of the bean to instantiate
	 * 
	 * <p> 要实例化的bean的类
	 * 
	 */
	public RootBeanDefinition(Class<?> beanClass) {
		super();
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status.
	 * 
	 * <p> 使用给定的单例状态创建新的RootBeanDefinition。
	 * 
	 * @param beanClass the class of the bean to instantiate
	 * 
	 * <p> 要实例化的bean的类
	 * 
	 * @param singleton the singleton status of the bean
	 * 
	 * <p> bean的单例状态
	 * 
	 * @deprecated since Spring 2.5, in favor of {@link #setScope}
	 * 
	 * <p> 从Spring 2.5开始，支持setScope
	 * 
	 */
	@Deprecated
	public RootBeanDefinition(Class beanClass, boolean singleton) {
		super();
		setBeanClass(beanClass);
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * 
	 * <p> 使用给定的自动装配模式为单例创建新的RootBeanDefinition。
	 * 
	 * @param beanClass the class of the bean to instantiate
	 * 
	 * <p> 要实例化的bean的类
	 * 
	 * @param autowireMode by name or type, using the constants in this interface
	 * 
	 * <p> autowireMode按名称或类型，使用此接口中的常量
	 *
	 * @deprecated as of Spring 3.0, in favor of {@link #setAutowireMode} usage
	 * 
	 * <p> 从Spring 3.0开始，支持setAutowireMode用法
	 */
	@Deprecated
	public RootBeanDefinition(Class beanClass, int autowireMode) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * 
	 * <p> 使用给定的自动装配模式为单例创建新的RootBeanDefinition。
	 * 
	 * @param beanClass the class of the bean to instantiate
	 * 
	 * <p> 要实例化的bean的类
	 * 
	 * @param autowireMode by name or type, using the constants in this interface
	 * 
	 * <p> 按名称或类型，使用此接口中的常量
	 * 
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 * 
	 * <p> 是否对对象执行依赖性检查（不适用于自动装配构造函数，因此在那里忽略）
	 * 
	 */
	public RootBeanDefinition(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing property values.
	 * 
	 * <p> 为单例创建新的RootBeanDefinition，提供属性值。
	 * 
	 * @param beanClass the class of the bean to instantiate 
	 * 
	 * <p> 要实例化的bean的类
	 * 
	 * @param pvs the property values to apply - 要应用的属性值
	 * @deprecated as of Spring 3.0, in favor of {@link #getPropertyValues} usage
	 * 
	 * <p> 从Spring 3.0开始，支持getPropertyValues用法
	 * 
	 */
	@Deprecated
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs) {
		super(null, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status,
	 * providing property values.
	 * 
	 * <p> 使用给定的singleton状态创建一个新的RootBeanDefinition，提供属性值。
	 * 
	 * @param beanClass the class of the bean to instantiate - 要实例化的bean的类
	 * @param pvs the property values to apply - 要应用的属性值
	 * @param singleton the singleton status of the bean - bean的单例状态
	 * @deprecated since Spring 2.5, in favor of {@link #setScope}
	 * 
	 * <p> 从Spring 2.5开始，支持setScope
	 */
	@Deprecated
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs, boolean singleton) {
		super(null, pvs);
		setBeanClass(beanClass);
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * 
	 * <p>  为单例创建一个新的RootBeanDefinition，提供构造函数参数和属性值。
	 * 
	 * @param beanClass the class of the bean to instantiate - 要实例化的bean的类
	 * @param cargs the constructor argument values to apply
	 * 
	 * <p> 要应用的构造函数参数值
	 * 
	 * @param pvs the property values to apply - 要应用的属性值
	 */
	public RootBeanDefinition(Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * 
	 * <p> 为单例创建一个新的RootBeanDefinition，提供构造函数参数和属性值。
	 * 
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * 
	 * <p> 采用bean类名称以避免急切加载bean类。
	 * 
	 * @param beanClassName the name of the class to instantiate - 要实例化的类的名称
	 */
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * 
	 * <p> 为单例创建一个新的RootBeanDefinition，提供构造函数参数和属性值。
	 * 
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * 
	 * <p> 采用bean类名称以避免急切加载bean类。
	 * 
	 * @param beanClassName the name of the class to instantiate
	 * 
	 * <p> 要实例化的类的名称
	 * 
	 * @param cargs the constructor argument values to apply - 要应用的构造函数参数值
	 * @param pvs the property values to apply - 要应用的属性值
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * 
	 * <p> 创建一个新的RootBeanDefinition作为给定bean定义的深层副本。
	 * 
	 * @param original the original bean definition to copy from
	 * 
	 * <p> 要从中复制的原始bean定义
	 * 
	 */
	public RootBeanDefinition(RootBeanDefinition original) {
		super((BeanDefinition) original);
		this.allowCaching = original.allowCaching;
		this.decoratedDefinition = original.decoratedDefinition;
		this.targetType = original.targetType;
		this.isFactoryMethodUnique = original.isFactoryMethodUnique;
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * 
	 * <p> 创建一个新的RootBeanDefinition作为给定bean定义的深层副本。
	 * 
	 * @param original the original bean definition to copy from
	 * 
	 * <p> 要从中复制的原始bean定义
	 * 
	 */
	RootBeanDefinition(BeanDefinition original) {
		super(original);
	}


	public String getParentName() {
		return null;
	}

	public void setParentName(String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}

	/**
	 * Register a target definition that is being decorated by this bean definition.
	 * 
	 * <p> 注册由此bean定义修饰的目标定义。
	 * 
	 */
	public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	/**
	 * Return the target definition that is being decorated by this bean definition, if any.
	 * 
	 * <p> 返回由此bean定义修饰的目标定义（如果有）。
	 * 
	 */
	public BeanDefinitionHolder getDecoratedDefinition() {
		return this.decoratedDefinition;
	}

	/**
	 * Specify the target type of this bean definition, if known in advance.
	 * 
	 * <p> 如果事先知道，请指定此bean定义的目标类型。
	 * 
	 */
	public void setTargetType(Class<?> targetType) {
		this.targetType = targetType;
	}

	/**
	 * Return the target type of this bean definition, if known
	 * (either specified in advance or resolved on first instantiation).
	 * 
	 * <p> 返回此bean定义的目标类型（如果已知）（预先指定或在第一次实例化时解析）。
	 * 
	 */
	public Class<?> getTargetType() {
		return this.targetType;
	}

	/**
	 * Specify a factory method name that refers to a non-overloaded method.
	 * 
	 * <p> 指定引用非重载方法的工厂方法名称。
	 * 
	 */
	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	/**
	 * Check whether the given candidate qualifies as a factory method.
	 * 
	 * <p> 检查给定的候选人是否有资格作为工厂方法。
	 * 
	 */
	public boolean isFactoryMethod(Method candidate) {
		return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
	}

	/**
	 * Return the resolved factory method as a Java Method object, if available.
	 * 
	 * <p> 将已解析的工厂方法作为Java Method对象返回（如果可用）。
	 * 
	 * @return the factory method, or {@code null} if not found or not resolved yet
	 * 
	 * <p> 工厂方法，如果未找到或尚未解决，则为null
	 * 
	 */
	public Method getResolvedFactoryMethod() {
		synchronized (this.constructorArgumentLock) {
			Object candidate = this.resolvedConstructorOrFactoryMethod;
			return (candidate instanceof Method ? (Method) candidate : null);
		}
	}

	public void registerExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedConfigMembers == null) {
				this.externallyManagedConfigMembers = new HashSet<Member>(1);
			}
			this.externallyManagedConfigMembers.add(configMember);
		}
	}

	public boolean isExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null &&
					this.externallyManagedConfigMembers.contains(configMember));
		}
	}

	public void registerExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedInitMethods == null) {
				this.externallyManagedInitMethods = new HashSet<String>(1);
			}
			this.externallyManagedInitMethods.add(initMethod);
		}
	}

	public boolean isExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null &&
					this.externallyManagedInitMethods.contains(initMethod));
		}
	}

	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedDestroyMethods == null) {
				this.externallyManagedDestroyMethods = new HashSet<String>(1);
			}
			this.externallyManagedDestroyMethods.add(destroyMethod);
		}
	}

	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null &&
					this.externallyManagedDestroyMethods.contains(destroyMethod));
		}
	}


	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}

}
