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

package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the {@link ApplicationContext} that it runs in.
 * 
 * <p> 希望被通知其运行的ApplicationContext的任何对象实现的接口。
 *
 * <p>Implementing this interface makes sense for example when an object
 * requires access to a set of collaborating beans. Note that configuration
 * via bean references is preferable to implementing this interface just
 * for bean lookup purposes.
 * 
 * <p> 例如，当对象需要访问一组协作bean时，实现此接口是有意义的。请注意，通过bean引用进行配置比仅
 * 用于bean查找目的更好地实现此接口。
 *
 * <p>This interface can also be implemented if an object needs access to file
 * resources, i.e. wants to call {@code getResource}, wants to publish
 * an application event, or requires access to the MessageSource. However,
 * it is preferable to implement the more specific {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} or {@link MessageSourceAware} interface
 * in such a specific scenario.
 * 
 * <p> 如果对象需要访问文件资源，即想要调用getResource，想要发布应用程序事件或需要访问MessageSource，
 * 也可以实现此接口。但是，最好在这种特定方案中实现更具体的ResourceLoaderAware，
 * ApplicationEventPublisherAware或MessageSourceAware接口。
 *
 * <p>Note that file resource dependencies can also be exposed as bean properties
 * of type {@link org.springframework.core.io.Resource}, populated via Strings
 * with automatic type conversion by the bean factory. This removes the need
 * for implementing any callback interface just for the purpose of accessing
 * a specific file resource.
 * 
 * <p> 请注意，文件资源依赖性也可以作为org.springframework.core.io.Resource类型的bean属性公开，
 * 通过bean工厂自动进行类型转换的字符串填充。这样就不需要为了访问特定的文件资源而实现任何回调接口。
 *
 * <p>{@link org.springframework.context.support.ApplicationObjectSupport} is a
 * convenience base class for application objects, implementing this interface.
 * 
 * <p> org.springframework.context.support.ApplicationObjectSupport是应用程序对象的便捷基类，
 * 实现了此接口。
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link org.springframework.beans.factory.BeanFactory BeanFactory javadocs}.
 * 
 * <p> 有关所有bean生命周期方法的列表，请参阅BeanFactory javadocs。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see ResourceLoaderAware
 * @see ApplicationEventPublisherAware
 * @see MessageSourceAware
 * @see org.springframework.context.support.ApplicationObjectSupport
 * @see org.springframework.beans.factory.BeanFactoryAware
 */
public interface ApplicationContextAware extends Aware {

	/**
	 * Set the ApplicationContext that this object runs in.
	 * Normally this call will be used to initialize the object.
	 * 
	 * <p> 设置此对象运行的ApplicationContext。通常，此调用将用于初始化对象。
	 * 
	 * <p>Invoked after population of normal bean properties but before an init callback such
	 * as {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
	 * or a custom init-method. Invoked after {@link ResourceLoaderAware#setResourceLoader},
	 * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} and
	 * {@link MessageSourceAware}, if applicable.
	 * 
	 * <p> 在普通bean属性的填充之后但在init回调之前调用，例如
	 * org.springframework.beans.factory.InitializingBean.afterPropertiesSet（）或自定义init方法。 
	 * 在ResourceLoaderAware.setResourceLoader，ApplicationEventPublisherAware.setApplicationEventPublisher
	 * 和MessageSourceAware之后调用（如果适用）。
	 * 
	 * @param applicationContext the ApplicationContext object to be used by this object
	 * 
	 * <p> 此对象使用的ApplicationContext对象
	 * 
	 * @throws ApplicationContextException in case of context initialization errors
	 * 
	 * <p> 在上下文初始化错误的情况下
	 * 
	 * @throws BeansException if thrown by application context methods
	 * 
	 * <p> 如果由应用程序上下文方法抛出
	 * 
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

}
