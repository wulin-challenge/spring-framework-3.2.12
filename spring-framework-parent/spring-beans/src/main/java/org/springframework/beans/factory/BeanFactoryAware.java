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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by beans that wish to be aware of their
 * owning {@link BeanFactory}.
 * 
 * <p> 由希望了解其拥有BeanFactory的bean实现的接口。
 *
 * <p>For example, beans can look up collaborating beans via the factory
 * (Dependency Lookup). Note that most beans will choose to receive references
 * to collaborating beans via corresponding bean properties or constructor
 * arguments (Dependency Injection).
 * 
 * <p> 例如，bean可以通过工厂查找协作bean（Dependency Lookup）。 
 * 请注意，大多数bean将选择通过相应的bean属性或构造函数参数（依赖注入）接收对协作bean的引用。
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 * 
 * <p> 有关所有bean生命周期方法的列表，请参阅BeanFactory javadocs。
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 11.03.2003
 * @see BeanNameAware
 * @see BeanClassLoaderAware
 * @see InitializingBean
 * @see org.springframework.context.ApplicationContextAware
 */
public interface BeanFactoryAware extends Aware {

	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * 
	 * <p> 将拥有工厂提供给bean实例的回调。
	 * 
	 * <p>Invoked after the population of normal bean properties
	 * but before an initialization callback such as
	 * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
	 * 
	 * <p> 在普通bean属性的填充之后但在初始化回调之前调用，
	 * 例如InitializingBean.afterPropertiesSet（）或自定义init方法。
	 * 
	 * @param beanFactory owning BeanFactory (never {@code null}).
	 * The bean can immediately call methods on the factory.
	 * 
	 * <p> 拥有BeanFactory（永远不会为null）。 bean可以立即调用工厂的方法。
	 * 
	 * @throws BeansException in case of initialization errors - 在初始化错误的情况下
	 * @see BeanInitializationException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
