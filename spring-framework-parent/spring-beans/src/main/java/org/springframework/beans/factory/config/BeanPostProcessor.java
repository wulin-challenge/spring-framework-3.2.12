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

/**
 * Factory hook that allows for custom modification of new bean instances,
 * e.g. checking for marker interfaces or wrapping them with proxies.
 * 
 * <p>工厂钩子，允许自定义修改新的bean实例，例如 检查标记接口或用代理包装它们。
 *
 * <p>ApplicationContexts can autodetect BeanPostProcessor beans in their
 * bean definitions and apply them to any beans subsequently created.
 * Plain bean factories allow for programmatic registration of post-processors,
 * applying to all beans created through this factory.
 * 
 * <p>ApplicationContexts可以在其bean定义中自动检测BeanPostProcessor bean，并将它们应用于随后创建的任何bean。
 *  普通bean工厂允许对后处理器进行编程注册，适用于通过该工厂创建的所有bean。
 * 
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 * 
 * <p>通常，通过标记接口等填充bean的后处理器将实现postProcessBeforeInitialization，而使用代理包装bean的后处理器通常会实
 * 现postProcessAfterInitialization。
 * 
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * 
	 * <p>在任何bean初始化回调之前将此BeanPostProcessor应用于给定的新bean实例（如InitializingBean
	 * 的afterPropertiesSet或自定义init方法）。 bean已经填充了属性值。 返回的bean实例可能是原始实例的包装器。
	 * 
	 * @param bean the new bean instance - 新的bean实例
	 * @param beanName the name of the bean - bean 的名称
	 * @return the bean instance to use, either the original or a wrapped one; if
	 * {@code null}, no subsequent BeanPostProcessors will be invoked
	 * 
	 * <p>要使用的bean实例，无论是原始实例还是包装实例; 如果为null，则不会调用后续的BeanPostProcessors
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors - 如果有错误
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * 
	 * <p>在任何bean初始化回调（如InitializingBean的afterPropertiesSet或自定义init方法）之后，
	 * 将此BeanPostProcessor应用于给定的新bean实例。 bean已经填充了属性值。 返回的bean实例可能是原始实例的包装器。
	 * 
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * 
	 * <p>对于FactoryBean，将为FactoryBean实例和FactoryBean创建的对象（从Spring 2.0开始）调用此回调。 
	 * 后处理器可以通过相应的Bean instanceof FactoryBean检查来决定是应用于FactoryBean还是应用于创建的对象。
	 * 
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other BeanPostProcessor callbacks.
	 * 
	 * <p>与所有其他BeanPostProcessor回调相比，
	 * 在InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation方法触发的短路之后，也将调用此回调。
	 * 
	 * @param bean the new bean instance - 新的bean实例
	 * @param beanName the name of the bean - bean 的名称
	 * @return the bean instance to use, either the original or a wrapped one; if
	 * {@code null}, no subsequent BeanPostProcessors will be invoked
	 * 
	 * <p>要使用的bean实例，无论是原始实例还是包装实例; 如果为null，则不会调用后续的BeanPostProcessors
	 * 
	 * @throws org.springframework.beans.BeansException in case of errors - 如果有错误
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}
