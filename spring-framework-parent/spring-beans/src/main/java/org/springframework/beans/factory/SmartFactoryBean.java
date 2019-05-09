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

/**
 * Extension of the {@link FactoryBean} interface. Implementations may
 * indicate whether they always return independent instances, for the
 * case where their {@link #isSingleton()} implementation returning
 * {@code false} does not clearly indicate independent instances.
 * 
 * <p> FactoryBean接口的扩展。 实现可以指示它们是否总是返回独立实例，
 * 因为它们的isSingleton（）实现返回false并不能清楚地指示独立实例。
 *
 * <p>Plain {@link FactoryBean} implementations which do not implement
 * this extended interface are simply assumed to always return independent
 * instances if their {@link #isSingleton()} implementation returns
 * {@code false}; the exposed object is only accessed on demand.
 * 
 * <p> 如果它们的isSingleton（）实现返回false，则假定不实现此扩展接口的Plain FactoryBean实现总是返
 * 回独立实例; 只能按需访问公开的对象。
 *
 * <p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework and within collaborating frameworks.
 * In general, application-provided FactoryBeans should simply implement
 * the plain {@link FactoryBean} interface. New methods might be added
 * to this extended interface even in point releases.
 * 
 * <p> 注意：此接口是一个专用接口，主要供框架内和协作框架内部使用。 通常，应用程序提供的FactoryBeans应该只
 * 实现普通的FactoryBean接口。 即使在点发布中，也可以向此扩展接口添加新方法。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see #isPrototype()
 * @see #isSingleton()
 */
public interface SmartFactoryBean<T> extends FactoryBean<T> {

	/**
	 * Is the object managed by this factory a prototype? That is,
	 * will {@link #getObject()} always return an independent instance?
	 * 
	 * <p> 这个工厂管理的对象是原型吗？也就是说，getObject（）总会返回一个独立的实例吗？
	 * 
	 * <p>The prototype status of the FactoryBean itself will generally
	 * be provided by the owning {@link BeanFactory}; usually, it has to be
	 * defined as singleton there.
	 * 
	 * <p> FactoryBean本身的原型状态通常由拥有BeanFactory提供; 通常，它必须被定义为单例.
	 * 
	 * <p>This method is supposed to strictly check for independent instances;
	 * it should not return {@code true} for scoped objects or other
	 * kinds of non-singleton, non-independent objects. For this reason,
	 * this is not simply the inverted form of {@link #isSingleton()}.
	 * 
	 * <p> 该方法应该严格检查独立实例; 对于范围对象或其他类型的非单例非独立对象，它不应返回true。
	 * 因此，这不仅仅是isSingleton（）的倒置形式。
	 * 
	 * @return whether the exposed object is a prototype
	 * 
	 * <p> 暴露对象是否是原型
	 * 
	 * @see #getObject()
	 * @see #isSingleton()
	 */
	boolean isPrototype();

	/**
	 * Does this FactoryBean expect eager initialization, that is,
	 * eagerly initialize itself as well as expect eager initialization
	 * of its singleton object (if any)?
	 * 
	 * <p> 这个FactoryBean是否期望急切初始化，即急切地初始化自己以及期望对其单例对象（如果有的话）进行急切初始化？
	 * 
	 * <p>A standard FactoryBean is not expected to initialize eagerly:
	 * Its {@link #getObject()} will only be called for actual access, even
	 * in case of a singleton object. Returning {@code true} from this
	 * method suggests that {@link #getObject()} should be called eagerly,
	 * also applying post-processors eagerly. This may make sense in case
	 * of a {@link #isSingleton() singleton} object, in particular if
	 * post-processors expect to be applied on startup.
	 * 
	 * <p> 标准的FactoryBean不会急切地初始化：它的getObject（）只会被调用以进行实际访问，
	 * 即使是单例对象也是如此。 从这个方法中返回true表明应该急切地调用getObject（），
	 * 同时也热切地应用后处理器。 在单例对象的情况下这可能是有意义的，特别是如果期望在启动时应用后处理器。
	 * 
	 * @return whether eager initialization applies - 是否适用初始化
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
	 */
	boolean isEagerInit();

}
