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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.ObjectUtils;

/**
 * Bean definition for beans which inherit settings from their parent.
 * Child bean definitions have a fixed dependency on a parent bean definition.
 * 
 * <p>bean的Bean定义，从父级继承设置。 子bean定义对父bean定义具有固定依赖性。
 *
 * <p>A child bean definition will inherit constructor argument values,
 * property values and method overrides from the parent, with the option
 * to add new values. If init method, destroy method and/or static factory
 * method are specified, they will override the corresponding parent settings.
 * The remaining settings will <i>always</i> be taken from the child definition:
 * depends on, autowire mode, dependency check, singleton, lazy init.
 * 
 * <p>子bean定义将从父级继承构造函数参数值，属性值和方法覆盖，并具有添加新值的选项。
 * 如果指定了init方法，destroy方法和/或静态工厂方法，它们将覆盖相应的父设置。其余设置将始终从子定义中获取：
 * 取决于，autowire模式，依赖性检查，单例，惰性初始化。
 *
 * <p><b>NOTE:</b> Since Spring 2.5, the preferred way to register bean
 * definitions programmatically is the {@link GenericBeanDefinition} class,
 * which allows to dynamically define parent dependencies through the
 * {@link GenericBeanDefinition#setParentName} method. This effectively
 * supersedes the ChildBeanDefinition class for most use cases.
 *
 * <p> 注意：从Spring 2.5开始，以编程方式注册bean定义的首选方法是GenericBeanDefinition类，
 * 它允许通过GenericBeanDefinition.setParentName方法动态定义父依赖项。 
 * 这有效地取代了大多数用例的ChildBeanDefinition类。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see GenericBeanDefinition
 * @see RootBeanDefinition
 */
@SuppressWarnings("serial")
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;


	/**
	 * Create a new ChildBeanDefinition for the given parent, to be
	 * configured through its bean properties and configuration methods.
	 * 
	 * <p> 为给定父级创建新的ChildBeanDefinition，通过其bean属性和配置方法进行配置。
	 * 
	 * @param parentName the name of the parent bean - 父bean的名称
	 * @see #setBeanClass
	 * @see #setBeanClassName
	 * @see #setScope
	 * @see #setAutowireMode
	 * @see #setDependencyCheck
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public ChildBeanDefinition(String parentName) {
		super();
		this.parentName = parentName;
	}

	/**
	 * Create a new ChildBeanDefinition for the given parent.
	 * 
	 * <p> 为给定父级创建新的ChildBeanDefinition。
	 * 
	 * @param parentName the name of the parent bean - 父bean的名称
	 * @param pvs the additional property values of the child -孩子的额外属性值
	 */
	public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
		super(null, pvs);
		this.parentName = parentName;
	}

	/**
	 * Create a new ChildBeanDefinition for the given parent.
	 * 
	 * <p> 为给定父级创建新的ChildBeanDefinition。
	 * 
	 * @param parentName the name of the parent bean - 父bean的名称
	 * @param cargs the constructor argument values to apply - 要应用的构造函数参数值
	 * @param pvs the additional property values of the child - 孩子的额外属性值
	 */
	public ChildBeanDefinition(
			String parentName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
	}

	/**
	 * Create a new ChildBeanDefinition for the given parent,
	 * providing constructor arguments and property values.
	 * 
	 * <p> 为给定父级创建新的ChildBeanDefinition，提供构造函数参数和属性值。
	 * 
	 * @param parentName the name of the parent bean - 父bean的名称
	 * @param beanClass the class of the bean to instantiate - 要实例化的bean的类
	 * @param cargs the constructor argument values to apply - 要应用的构造函数参数值
	 * @param pvs the property values to apply - 要应用的属性值
	 */
	public ChildBeanDefinition(
			String parentName, Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClass(beanClass);
	}

	/**
	 * Create a new ChildBeanDefinition for the given parent,
	 * providing constructor arguments and property values.
	 * Takes a bean class name to avoid eager loading of the bean class.
	 * 
	 * <p> 为给定父级创建新的ChildBeanDefinition，提供构造函数参数和属性值。 采用bean类名称以避免急切加载bean类。
	 * 
	 * @param parentName the name of the parent bean - 父bean的名称
	 * @param beanClassName the name of the class to instantiate - 要实例化的类的名称
	 * @param cargs the constructor argument values to apply - 要应用的构造函数参数值
	 * @param pvs the property values to apply - 要应用的属性值
	 */
	public ChildBeanDefinition(
			String parentName, String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new ChildBeanDefinition as deep copy of the given
	 * bean definition.
	 * 
	 * <p> 创建一个新的ChildBeanDefinition作为给定bean定义的深层副本。
	 * 
	 * @param original the original bean definition to copy from - 要从中复制的原始bean定义
	 */
	public ChildBeanDefinition(ChildBeanDefinition original) {
		super((BeanDefinition) original);
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getParentName() {
		return this.parentName;
	}

	@Override
	public void validate() throws BeanDefinitionValidationException {
		super.validate();
		if (this.parentName == null) {
			throw new BeanDefinitionValidationException("'parentName' must be set in ChildBeanDefinition");
		}
	}


	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new ChildBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ChildBeanDefinition)) {
			return false;
		}
		ChildBeanDefinition that = (ChildBeanDefinition) other;
		return (ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.parentName) * 29 + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Child bean with parent '");
		sb.append(this.parentName).append("': ").append(super.toString());
		return sb.toString();
	}

}
