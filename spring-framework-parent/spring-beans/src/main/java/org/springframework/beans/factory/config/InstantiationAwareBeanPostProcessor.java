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

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

/**
 * Subinterface of {@link BeanPostProcessor} that adds a before-instantiation callback,
 * and a callback after instantiation but before explicit properties are set or
 * autowiring occurs.
 * 
 * <p> BeanPostProcessor的子接口，用于添加实例化前回调，实例化后但在显式属性设置或自动装配发生之前的回调。
 *
 * <p>Typically used to suppress default instantiation for specific target beans,
 * for example to create proxies with special TargetSources (pooling targets,
 * lazily initializing targets, etc), or to implement additional injection strategies
 * such as field injection.
 * 
 * <p> 通常用于抑制特定目标bean的默认实例化，例如创建具有特殊TargetSource的代理（池化目标，延迟初始化目标等），
 * 或实现其他注入策略（如字段注入）。
 *
 * <p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework. It is recommended to implement the plain
 * {@link BeanPostProcessor} interface as far as possible, or to derive from
 * {@link InstantiationAwareBeanPostProcessorAdapter} in order to be shielded
 * from extensions to this interface.
 * 
 * <p>  注意：此接口是一个专用接口，主要供框架内部使用。 建议尽可能实现普通的BeanPostProcessor接口，
 * 或者从InstantiationAwareBeanPostProcessorAdapter派生，以便屏蔽此接口的扩展。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.2
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor <i>before the target bean gets instantiated</i>.
	 * The returned bean object may be a proxy to use instead of the target bean,
	 * effectively suppressing default instantiation of the target bean.
	 * 
	 * <p> 在实例化目标bean之前应用此BeanPostProcessor。 返回的bean对象可以是代替目标bean的代理，
	 * 有效地抑制了目标bean的默认实例化。
	 * 
	 * <p>If a non-null object is returned by this method, the bean creation process
	 * will be short-circuited. The only further processing applied is the
	 * {@link #postProcessAfterInitialization} callback from the configured
	 * {@link BeanPostProcessor BeanPostProcessors}.
	 * 
	 * <p> 如果此方法返回非null对象，则bean创建过程将被短路。 应用的唯一进一步处理是来自配置的
	 * BeanPostProcessors的postProcessAfterInitialization回调。
	 * 
	 * <p>This callback will only be applied to bean definitions with a bean class.
	 * In particular, it will not be applied to beans with a "factory-method".
	 * 
	 * <p> 此回调仅适用于具有bean类的bean定义。 特别是，它不会应用于具有“工厂方法”的bean。
	 * 
	 * <p>Post-processors may implement the extended
	 * {@link SmartInstantiationAwareBeanPostProcessor} interface in order
	 * to predict the type of the bean object that they are going to return here.
	 * 
	 * <p> 后处理器可以实现扩展的SmartInstantiationAwareBeanPostProcessor接口，
	 * 以便预测它们将在此处返回的bean对象的类型。
	 * 
	 * @param beanClass the class of the bean to be instantiated - 要实例化的bean的类
	 * @param beanName the name of the bean - 豆的名字
	 * @return the bean object to expose instead of a default instance of the target bean,
	 * or {@code null} to proceed with default instantiation
	 * 
	 * <p> 要公开的bean对象而不是目标bean的默认实例，或者为null以继续进行默认实例化
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors - 如果有错误
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#hasBeanClass
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getFactoryMethodName
	 */
	Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

	/**
	 * Perform operations after the bean has been instantiated, via a constructor or factory method,
	 * but before Spring property population (from explicit properties or autowiring) occurs.
	 * 
	 * <p> 在bean实例化之后，通过构造函数或工厂方法，但在Spring属性填充（来自显式属性或自动装配）之前执行操作。
	 * 
	 * <p>This is the ideal callback for performing field injection on the given bean instance.
	 * See Spring's own {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor}
	 * for a typical example.
	 * 
	 * <p> 这是在给定bean实例上执行字段注入的理想回调。 有关典型示例，请参阅Spring自己的
	 * org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor。
	 * 
	 * @param bean the bean instance created, with properties not having been set yet
	 * 
	 * <p> 创建的bean实例，其属性尚未设置
	 * 
	 * @param beanName the name of the bean - bean的名称
	 * @return {@code true} if properties should be set on the bean; {@code false}
	 * if property population should be skipped. Normal implementations should return {@code true}.
	 * Returning {@code false} will also prevent any subsequent InstantiationAwareBeanPostProcessor
	 * instances being invoked on this bean instance.
	 * 
	 * <p> 如果应该在bean上设置属性，则为true; 如果应跳过封装属性值的过程，则为false。 正常实现应该返回true。 
	 * 返回false还将阻止在此Bean实例上调用任何后续的InstantiationAwareBeanPostProcessor实例。
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors  - 如果有错误
	 */
	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

	/**
	 * Post-process the given property values before the factory applies them
	 * to the given bean. Allows for checking whether all dependencies have been
	 * satisfied, for example based on a "Required" annotation on bean property setters.
	 * 
	 * <p> 在工厂将它们应用于给定bean之前对给定属性值进行后处理。 允许检查是否已满足所有依赖项，
	 * 例如，基于bean属性setter上的“Required”批注。
	 * 
	 * <p>Also allows for replacing the property values to apply, typically through
	 * creating a new MutablePropertyValues instance based on the original PropertyValues,
	 * adding or removing specific values.
	 * 
	 * <p> 还允许替换要应用的属性值，通常是通过基于原始PropertyValues创建新的MutablePropertyValues实例，
	 * 添加或删除特定值。
	 * 
	 * @param pvs the property values that the factory is about to apply (never {@code null})
	 * 
	 * <p> 工厂即将应用的属性值（永不为null）
	 * 
	 * @param pds the relevant property descriptors for the target bean (with ignored
	 * dependency types - which the factory handles specifically - already filtered out)
	 * 
	 * <p> 目标bean的相关属性描述符（具有忽略的依赖类型 - 工厂专门处理的 - 已经过滤掉）
	 * 
	 * @param bean the bean instance created, but whose properties have not yet been set
	 * 
	 * <p> 已创建bean实例，但尚未设置其属性
	 * 
	 * @param beanName the name of the bean - bean的名称
	 * @return the actual property values to apply to to the given bean
	 * (can be the passed-in PropertyValues instance), or {@code null}
	 * to skip property population
	 * 
	 * <p> 要应用于给定bean的实际属性值（可以是传入的PropertyValues实例），或者为null以跳过属性填充
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors - 如果有错误
	 * @see org.springframework.beans.MutablePropertyValues
	 */
	PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
			throws BeansException;

}
