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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Interface responsible for creating instances corresponding to a root bean definition.
 * 
 * <p>负责创建与根bean定义相对应的实例的接口。
 *
 * <p>This is pulled out into a strategy as various approaches are possible,
 * including using CGLIB to create subclasses on the fly to support Method Injection.
 * 
 * <p>由于各种方法都可以实现，因此将其纳入策略，包括使用CGLIB动态创建子类以支持方法注入。
 *
 * @author Rod Johnson
 * @since 1.1
 */
public interface InstantiationStrategy {

	/**
	 * Return an instance of the bean with the given name in this factory.
	 * 
	 * <p>在此工厂中返回具有给定名称的bean实例。
	 * 
	 * @param beanDefinition the bean definition
	 * @param beanName name of the bean when it's created in this context.
	 * The name can be {@code null} if we're autowiring a bean that
	 * doesn't belong to the factory.
	 * 
	 * <p>在此上下文中创建bean的名称。 如果我们自动装配不属于工厂的bean，则该名称可以为null。
	 * 
	 * @param owner owning BeanFactory - 拥有BeanFactory
	 * @return a bean instance for this bean definition - 此bean定义的bean实例
	 * @throws BeansException if the instantiation failed - 如果实例化失败
	 */
	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner)
			throws BeansException;

	/**
	 * Return an instance of the bean with the given name in this factory,
	 * creating it via the given constructor.
	 * 
	 * <p>在此工厂中返回具有给定名称的bean实例，通过给定的构造函数创建它。
	 * 
	 * @param beanDefinition the bean definition
	 * @param beanName name of the bean when it's created in this context.
	 * The name can be {@code null} if we're autowiring a bean
	 * that doesn't belong to the factory.
	 * 
	 * <p>在此上下文中创建bean的名称。 如果我们自动装配不属于工厂的bean，则该名称可以为null。
	 * 
	 * @param owner owning BeanFactory - 拥有BeanFactory
	 * @param ctor the constructor to use - 要使用的构造函数
	 * @param args the constructor arguments to apply - 要应用的构造函数参数
	 * @return a bean instance for this bean definition - 此bean定义的bean实例
	 * @throws BeansException if the instantiation failed - 如果实例化失败
	 */
	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Constructor<?> ctor, Object[] args) throws BeansException;

	/**
	 * Return an instance of the bean with the given name in this factory,
	 * creating it via the given factory method.
	 * 
	 * <p>在此工厂中返回具有给定名称的bean实例，通过给定的工厂方法创建它。
	 * 
	 * @param beanDefinition bean definition
	 * @param beanName name of the bean when it's created in this context.
	 * The name can be {@code null} if we're autowiring a bean
	 * that doesn't belong to the factory.
	 * 
	 * <p>在此上下文中创建bean的名称。 如果我们自动装配不属于工厂的bean，则该名称可以为null。
	 * 
	 * @param owner owning BeanFactory - 拥有BeanFactory
	 * @param factoryBean the factory bean instance to call the factory method on,
	 * or {@code null} in case of a static factory method
	 * 
	 * <p>用于调用工厂方法的工厂bean实例，或者在静态工厂方法的情况下为null
	 * 
	 * @param factoryMethod the factory method to use - 工厂使用的方法
	 * @param args the factory method arguments to apply - 要应用的工厂方法参数
	 * @return a bean instance for this bean definition - 此bean定义的bean实例
	 * @throws BeansException if the instantiation failed - 如果实例化失败
	 */
	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Object factoryBean, Method factoryMethod, Object[] args) throws BeansException;

}
