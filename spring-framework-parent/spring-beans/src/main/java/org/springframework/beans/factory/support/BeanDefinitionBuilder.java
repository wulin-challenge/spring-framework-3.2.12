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

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.util.ObjectUtils;

/**
 * Programmatic means of constructing
 * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}
 * using the builder pattern. Intended primarily for use when implementing Spring 2.0
 * {@link org.springframework.beans.factory.xml.NamespaceHandler NamespaceHandlers}.
 * 
 * <p> 使用构建器模式构造BeanDefinitions的编程方法。 主要用于实现Spring 2.0 NamespaceHandlers时使用。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanDefinitionBuilder {

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造GenericBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 */
	public static BeanDefinitionBuilder genericBeanDefinition() {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new GenericBeanDefinition();
		return builder;
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造GenericBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param beanClass the {@code Class} of the bean that the definition is being created for
	 * 
	 * <p> 正在为其创建定义的bean的类
	 * 
	 */
	public static BeanDefinitionBuilder genericBeanDefinition(Class beanClass) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new GenericBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		return builder;
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造GenericBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param beanClassName the class name for the bean that the definition is being created for
	 * 
	 * <p> beanClassName为其创建定义的bean的类名
	 * 
	 */
	public static BeanDefinitionBuilder genericBeanDefinition(String beanClassName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new GenericBeanDefinition();
		builder.beanDefinition.setBeanClassName(beanClassName);
		return builder;
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造RootBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param beanClass the {@code Class} of the bean that the definition is being created for
	 * 
	 * <p> 正在为其创建定义的bean的类
	 * 
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
		return rootBeanDefinition(beanClass, null);
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造RootBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param beanClass the {@code Class} of the bean that the definition is being created for
	 * 
	 * <p> 正在为其创建定义的bean的类
	 * 
	 * @param factoryMethodName the name of the method to use to construct the bean instance
	 * 
	 * <p> 用于构造bean实例的方法的名称
	 * 
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造RootBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param beanClassName the class name for the bean that the definition is being created for
	 * 
	 * <p> 正在为其创建定义的bean的类名
	 * 
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
		return rootBeanDefinition(beanClassName, null);
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造RootBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param beanClassName the class name for the bean that the definition is being created for
	 * 
	 * <p> 正在为其创建定义的bean的类名
	 * 
	 * @param factoryMethodName the name of the method to use to construct the bean instance
	 * 
	 * <p> 用于构造bean实例的方法的名称
	 * 
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClassName(beanClassName);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}

	/**
	 * Create a new {@code BeanDefinitionBuilder} used to construct a {@link ChildBeanDefinition}.
	 * 
	 * <p> 创建一个用于构造ChildBeanDefinition的新BeanDefinitionBuilder。
	 * 
	 * @param parentName the name of the parent bean - 父bean的名称
	 */
	public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new ChildBeanDefinition(parentName);
		return builder;
	}


	/**
	 * The {@code BeanDefinition} instance we are creating.
	 * 
	 * <p> 我们正在创建的BeanDefinition实例。
	 * 
	 */
	private AbstractBeanDefinition beanDefinition;

	/**
	 * Our current position with respect to constructor args.
	 * 
	 * <p> 我们目前在构造函数方面的立场。
	 * 
	 */
	private int constructorArgIndex;


	/**
	 * Enforce the use of factory methods.
	 * 
	 * <p> 强制使用工厂方法。
	 * 
	 */
	private BeanDefinitionBuilder() {
	}

	/**
	 * Return the current BeanDefinition object in its raw (unvalidated) form.
	 * 
	 * <p> 以原始（未经验证的）形式返回当前BeanDefinition对象。
	 * 
	 * @see #getBeanDefinition()
	 */
	public AbstractBeanDefinition getRawBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * Validate and return the created BeanDefinition object.
	 * 
	 * <p> 验证并返回创建的BeanDefinition对象。
	 * 
	 */
	public AbstractBeanDefinition getBeanDefinition() {
		this.beanDefinition.validate();
		return this.beanDefinition;
	}


	/**
	 * Set the name of the parent definition of this bean definition.
	 * 
	 * <p> 设置此bean定义的父定义的名称。
	 * 
	 */
	public BeanDefinitionBuilder setParentName(String parentName) {
		this.beanDefinition.setParentName(parentName);
		return this;
	}

	/**
	 * Set the name of the factory method to use for this definition.
	 * 
	 * <p> 设置要用于此定义的工厂方法的名称。
	 * 
	 */
	public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	/**
	 * Set the name of the factory bean to use for this definition.
	 * 
	 * <p> 设置要用于此定义的工厂bean的名称。
	 * 
	 * @deprecated since Spring 2.5, in favor of preparing this on the
	 * {@link #getRawBeanDefinition() raw BeanDefinition object}
	 * 
	 * <p> 从Spring 2.5开始，支持在原始BeanDefinition对象上准备这个
	 * 
	 */
	@Deprecated
	public BeanDefinitionBuilder setFactoryBean(String factoryBean, String factoryMethod) {
		this.beanDefinition.setFactoryBeanName(factoryBean);
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	/**
	 * Add an indexed constructor arg value. The current index is tracked internally
	 * and all additions are at the present point.
	 * 
	 * <p> 添加索引构造函数arg值。 内部跟踪当前索引，所有添加都在当前点。
	 * 
	 * @deprecated since Spring 2.5, in favor of {@link #addConstructorArgValue}
	 * 
	 * <p> 从Spring 2.5开始，支持addConstructorArgValue
	 */
	@Deprecated
	public BeanDefinitionBuilder addConstructorArg(Object value) {
		return addConstructorArgValue(value);
	}

	/**
	 * Add an indexed constructor arg value. The current index is tracked internally
	 * and all additions are at the present point.
	 * 
	 * <p> 添加索引构造函数arg值。 内部跟踪当前索引，所有添加都在当前点。
	 * 
	 */
	public BeanDefinitionBuilder addConstructorArgValue(Object value) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				this.constructorArgIndex++, value);
		return this;
	}

	/**
	 * Add a reference to a named bean as a constructor arg.
	 * 
	 * <p> 添加对命名bean的引用作为构造函数arg。
	 * 
	 * @see #addConstructorArgValue(Object)
	 */
	public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				this.constructorArgIndex++, new RuntimeBeanReference(beanName));
		return this;
	}

	/**
	 * Add the supplied property value under the given name.
	 * 
	 * <p> 在给定名称下添加提供的属性值。
	 * 
	 */
	public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
		this.beanDefinition.getPropertyValues().add(name, value);
		return this;
	}

	/**
	 * Add a reference to the specified bean name under the property specified.
	 * 
	 * <p> 在指定的属性下添加对指定bean名称的引用。
	 * 
	 * @param name the name of the property to add the reference to - 要添加引用的属性的名称
	 * @param beanName the name of the bean being referenced - 被引用的bean的名称
	 */
	public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
		this.beanDefinition.getPropertyValues().add(name, new RuntimeBeanReference(beanName));
		return this;
	}

	/**
	 * Set the init method for this definition.
	 * 
	 * <p> 为此定义设置init方法。
	 * 
	 */
	public BeanDefinitionBuilder setInitMethodName(String methodName) {
		this.beanDefinition.setInitMethodName(methodName);
		return this;
	}

	/**
	 * Set the destroy method for this definition.
	 * 
	 * <p> 为此定义设置destroy方法。
	 * 
	 */
	public BeanDefinitionBuilder setDestroyMethodName(String methodName) {
		this.beanDefinition.setDestroyMethodName(methodName);
		return this;
	}


	/**
	 * Set the scope of this definition.
	 * 
	 * <p> 设置此定义的范围。
	 * 
	 * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_SINGLETON
	 * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_PROTOTYPE
	 */
	public BeanDefinitionBuilder setScope(String scope) {
		this.beanDefinition.setScope(scope);
		return this;
	}

	/**
	 * Set whether or not this definition describes a singleton bean,
	 * as alternative to {@link #setScope}.
	 * 
	 * <p> 设置此定义是否描述单个bean，作为setScope的替代。
	 * 
	 * @deprecated since Spring 2.5, in favor of {@link #setScope}
	 * 
	 * <p> 从Spring 2.5开始，支持setScope
	 */
	@Deprecated
	public BeanDefinitionBuilder setSingleton(boolean singleton) {
		this.beanDefinition.setSingleton(singleton);
		return this;
	}

	/**
	 * Set whether or not this definition is abstract.
	 * 
	 * <p> 设置此定义是否为抽象。
	 * 
	 */
	public BeanDefinitionBuilder setAbstract(boolean flag) {
		this.beanDefinition.setAbstract(flag);
		return this;
	}

	/**
	 * Set whether beans for this definition should be lazily initialized or not.
	 * 
	 * <p> 设置是否应该懒惰地初始化此定义的bean。
	 * 
	 */
	public BeanDefinitionBuilder setLazyInit(boolean lazy) {
		this.beanDefinition.setLazyInit(lazy);
		return this;
	}

	/**
	 * Set the autowire mode for this definition.
	 * 
	 * <p> 为此定义设置自动装配模式。
	 * 
	 */
	public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
		beanDefinition.setAutowireMode(autowireMode);
		return this;
	}

	/**
	 * Set the depency check mode for this definition.
	 * 
	 * <p> 设置此定义的依赖性检查模式。
	 * 
	 */
	public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
		beanDefinition.setDependencyCheck(dependencyCheck);
		return this;
	}

	/**
	 * Append the specified bean name to the list of beans that this definition
	 * depends on.
	 * 
	 * <p> 将指定的bean名称附加到此定义所依赖的bean列表中。
	 * 
	 */
	public BeanDefinitionBuilder addDependsOn(String beanName) {
		if (this.beanDefinition.getDependsOn() == null) {
			this.beanDefinition.setDependsOn(new String[] {beanName});
		}
		else {
			String[] added = ObjectUtils.addObjectToArray(this.beanDefinition.getDependsOn(), beanName);
			this.beanDefinition.setDependsOn(added);
		}
		return this;
	}

	/**
	 * Set the role of this definition.
	 * 
	 * <p> 设置此定义的角色。
	 * 
	 */
	public BeanDefinitionBuilder setRole(int role) {
		this.beanDefinition.setRole(role);
		return this;
	}

	/**
	 * Set the source of this definition.
	 * 
	 * <p> 设置此定义的来源。
	 * 
	 * @deprecated since Spring 2.5, in favor of preparing this on the
	 * {@link #getRawBeanDefinition() raw BeanDefinition object}
	 * 
	 * <p> 从Spring 2.5开始，支持在原始BeanDefinition对象上准备这个
	 * 
	 */
	@Deprecated
	public BeanDefinitionBuilder setSource(Object source) {
		this.beanDefinition.setSource(source);
		return this;
	}

	/**
	 * Set the description associated with this definition.
	 * 
	 * <p> 设置与此定义关联的描述。
	 * 
	 * @deprecated since Spring 2.5, in favor of preparing this on the
	 * {@link #getRawBeanDefinition() raw BeanDefinition object}
	 * 
	 * <p> 从Spring 2.5开始，支持在原始BeanDefinition对象上准备这个
	 */
	@Deprecated
	public BeanDefinitionBuilder setResourceDescription(String resourceDescription) {
		this.beanDefinition.setResourceDescription(resourceDescription);
		return this;
	}

}
