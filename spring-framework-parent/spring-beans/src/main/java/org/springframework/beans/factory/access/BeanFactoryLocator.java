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

package org.springframework.beans.factory.access;

import org.springframework.beans.BeansException;

/**
 * Defines a contract for the lookup, use, and release of a
 * {@link org.springframework.beans.factory.BeanFactory},
 * or a {@code BeanFactory} subclass such as an
 * {@link org.springframework.context.ApplicationContext}.
 * 
 * <p> 定义用于查找、使用和发布org.springframework.beans.factory.beanfactory
 * 或beanfactory子类（如org.springframework.context.applicationContext）的合同。
 *
 *
 * <p>Where this interface is implemented as a singleton class such as
 * {@link SingletonBeanFactoryLocator}, the Spring team <strong>strongly</strong>
 * suggests that it be used sparingly and with caution. By far the vast majority
 * of the code inside an application is best written in a Dependency Injection
 * style, where that code is served out of a
 * {@code BeanFactory}/{@code ApplicationContext} container, and has
 * its own dependencies supplied by the container when it is created. However,
 * even such a singleton implementation sometimes has its use in the small glue
 * layers of code that is sometimes needed to tie other code together. For
 * example, third party code may try to construct new objects directly, without
 * the ability to force it to get these objects out of a {@code BeanFactory}.
 * If the object constructed by the third party code is just a small stub or
 * proxy, which then uses an implementation of this class to get a
 * {@code BeanFactory} from which it gets the real object, to which it
 * delegates, then proper Dependency Injection has been achieved.
 * 
 * <p> 如果将此接口实现为Singleton类，例如SingletonBeanFactoryLocator，那么Spring团队强烈建议谨慎使用它并谨慎使用。 
 * 到目前为止，应用程序中的绝大多数代码最好以依赖注入样式编写，其中该代码由BeanFactory / ApplicationContext容器提供，
 * 并且在创建容器时由容器提供自己的依赖关系。 然而，即使这样的单例实现有时也会在代码的小胶合层中使用，有时需要将其他代码绑定在一起。 
 * 例如，第三方代码可能会尝试直接构造新对象，而无法强制它从BeanFactory中获取这些对象。 如果由第三方代码构造的对象只是一个小的存根或代理，
 * 然后使用该类的实现来获取BeanFactory，从中获取它所委托的真实对象，则已经实现了适当的依赖注入。
 *
 * <p>As another example, in a complex J2EE app with multiple layers, with each
 * layer having its own {@code ApplicationContext} definition (in a
 * hierarchy), a class like {@code SingletonBeanFactoryLocator} may be used
 * to demand load these contexts.
 * 
 * <p> 另一个例子，在具有多个层的复杂J2EE应用程序中，每个层都有自己的ApplicationContext定义（在层次结构中），
 * 可以使用类似SingletonBeanFactoryLocator的类来请求加载这些上下文。
 *
 * @author Colin Sampaleanu
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.context.access.DefaultLocatorFactory
 * @see org.springframework.context.ApplicationContext
 */
public interface BeanFactoryLocator {

	/**
	 * Use the {@link org.springframework.beans.factory.BeanFactory} (or derived
	 * interface such as {@link org.springframework.context.ApplicationContext})
	 * specified by the {@code factoryKey} parameter.
	 * 
	 * <p> 使用由factoryKey参数指定的org.springframework.beans.factory.BeanFactory（或派生接口，如
	 * org.springframework.context.ApplicationContext）。
	 * 
	 * <p>The definition is possibly loaded/created as needed.
	 * 
	 * <p> 可以根据需要加载/创建定义。
	 * 
	 * @param factoryKey a resource name specifying which {@code BeanFactory} the
	 * {@code BeanFactoryLocator} must return for usage. The actual meaning of the
	 * resource name is specific to the implementation of {@code BeanFactoryLocator}.
	 * 
	 * <p> 一个资源名称，指定BeanFactoryLocator必须返回以供使用的BeanFactory。 
	 * 资源名称的实际含义特定于BeanFactoryLocator的实现。
	 * 
	 * @return the {@code BeanFactory} instance, wrapped as a {@link BeanFactoryReference} object
	 * 
	 * <p> BeanFactory实例，包装为BeanFactoryReference对象
	 * 
	 * @throws BeansException if there is an error loading or accessing the {@code BeanFactory}
	 * 
	 * <p> 如果加载或访问BeanFactory时出错
	 */
	BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException;

}
