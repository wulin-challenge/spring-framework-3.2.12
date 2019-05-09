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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Simple template superclass for {@link FactoryBean} implementations that
 * creates a singleton or a prototype object, depending on a flag.
 * 
 * <p> FactoryBean实现的简单模板超类，它根据标志创建单例或原型对象。
 *
 * <p>If the "singleton" flag is {@code true} (the default),
 * this class will create the object that it creates exactly once
 * on initialization and subsequently return said singleton instance
 * on all calls to the {@link #getObject()} method.
 * 
 * <p> 如果“singleton”标志为true（默认值），则此类将在初始化时创建它只创建一次的对象，
 * 然后在对getObject（）方法的所有调用上返回所述单例实例。
 *
 * <p>Else, this class will create a new instance every time the
 * {@link #getObject()} method is invoked. Subclasses are responsible
 * for implementing the abstract {@link #createInstance()} template
 * method to actually create the object(s) to expose.
 * 
 * <p> 否则，每次调用getObject（）方法时，此类都将创建一个新实例。 
 * 子类负责实现抽象的createInstance（）模板方法，以实际创建要公开的对象。
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 1.0.2
 * @see #setSingleton
 * @see #createInstance()
 */
public abstract class AbstractFactoryBean<T>
		implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean singleton = true;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private BeanFactory beanFactory;

	private boolean initialized = false;

	private T singletonInstance;

	private T earlySingletonInstance;


	/**
	 * Set if a singleton should be created, or a new object on each request
	 * otherwise. Default is {@code true} (a singleton).
	 * 
	 * <p> 设置是否应创建单例，否则设置每个请求的新对象。 默认值为true（单例）。
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isSingleton() {
		return this.singleton;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the BeanFactory that this bean runs in.
	 * 
	 * <p> 返回此bean运行的BeanFactory。
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Obtain a bean type converter from the BeanFactory that this bean
	 * runs in. This is typically a fresh instance for each call,
	 * since TypeConverters are usually <i>not</i> thread-safe.
	 * 
	 * <p> 从Bean运行的BeanFactory中获取bean类型转换器。这通常是每个调用的新实例，
	 * 因为TypeConverters通常不是线程安全的。
	 * 
	 * <p>Falls back to a SimpleTypeConverter when not running in a BeanFactory.
	 * 
	 * <p> 不在BeanFactory中运行时回退到SimpleTypeConverter。
	 * 
	 * @see ConfigurableBeanFactory#getTypeConverter()
	 * @see org.springframework.beans.SimpleTypeConverter
	 */
	protected TypeConverter getBeanTypeConverter() {
		BeanFactory beanFactory = getBeanFactory();
		if (beanFactory instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) beanFactory).getTypeConverter();
		}
		else {
			return new SimpleTypeConverter();
		}
	}

	/**
	 * Eagerly create the singleton instance, if necessary.
	 * 
	 * <p> 如有必要，急切地创建单例实例。
	 */
	public void afterPropertiesSet() throws Exception {
		if (isSingleton()) {
			this.initialized = true;
			this.singletonInstance = createInstance();
			this.earlySingletonInstance = null;
		}
	}


	/**
	 * Expose the singleton instance or create a new prototype instance.
	 * 
	 * <p> 公开单例实例或创建一个新的原型实例。
	 * @see #createInstance()
	 * @see #getEarlySingletonInterfaces()
	 */
	public final T getObject() throws Exception {
		if (isSingleton()) {
			return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
		}
		else {
			return createInstance();
		}
	}

	/**
	 * Determine an 'eager singleton' instance, exposed in case of a
	 * circular reference. Not called in a non-circular scenario.
	 * 
	 * <p> 确定一个'eager singleton'实例，在循环引用的情况下暴露。 未在非循环场景中调用。
	 * 
	 */
	@SuppressWarnings("unchecked")
	private T getEarlySingletonInstance() throws Exception {
		Class[] ifcs = getEarlySingletonInterfaces();
		if (ifcs == null) {
			throw new FactoryBeanNotInitializedException(
					getClass().getName() + " does not support circular references");
		}
		if (this.earlySingletonInstance == null) {
			this.earlySingletonInstance = (T) Proxy.newProxyInstance(
					this.beanClassLoader, ifcs, new EarlySingletonInvocationHandler());
		}
		return this.earlySingletonInstance;
	}

	/**
	 * Expose the singleton instance (for access through the 'early singleton' proxy).
	 * 
	 * <p> 公开单例实例（通过'早期单例'代理进行访问）。
	 * 
	 * @return the singleton instance that this FactoryBean holds - 此FactoryBean包含的单例实例
	 * @throws IllegalStateException if the singleton instance is not initialized
	 * 
	 * <p> 如果单例实例未初始化
	 */
	private T getSingletonInstance() throws IllegalStateException {
		if (!this.initialized) {
			throw new IllegalStateException("Singleton instance not initialized yet");
		}
		return this.singletonInstance;
	}

	/**
	 * Destroy the singleton instance, if any.
	 * 
	 * <p> 如果有的话，销毁单例实例。
	 * 
	 * @see #destroyInstance(Object)
	 */
	public void destroy() throws Exception {
		if (isSingleton()) {
			destroyInstance(this.singletonInstance);
		}
	}


	/**
	 * This abstract method declaration mirrors the method in the FactoryBean
	 * interface, for a consistent offering of abstract template methods.
	 * 
	 * <p> 此抽象方法声明镜像FactoryBean接口中的方法，以提供一致的抽象模板方法。
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public abstract Class<?> getObjectType();

	/**
	 * Template method that subclasses must override to construct
	 * the object returned by this factory.
	 * 
	 * <p> 子类必须重写的模板方法，以构造此工厂返回的对象。
	 * 
	 * <p>Invoked on initialization of this FactoryBean in case of
	 * a singleton; else, on each {@link #getObject()} call.
	 * 
	 * <p> 在单例的情况下，在初始化此FactoryBean时调用; 否则，在每个getObject（）调用上。
	 * 
	 * @return the object returned by this factory - 该工厂返回的对象
	 * @throws Exception if an exception occured during object creation
	 * 
	 * <p> 如果在对象创建期间发生异常
	 * 
	 * @see #getObject()
	 */
	protected abstract T createInstance() throws Exception;

	/**
	 * Return an array of interfaces that a singleton object exposed by this
	 * FactoryBean is supposed to implement, for use with an 'early singleton
	 * proxy' that will be exposed in case of a circular reference.
	 * 
	 * <p> 返回由此FactoryBean公开的单个对象应该实现的接口数组，以便与循环引用时将公开的“早期单例代理”一起使用。
	 * 
	 * <p>The default implementation returns this FactoryBean's object type,
	 * provided that it is an interface, or {@code null} else. The latter
	 * indicates that early singleton access is not supported by this FactoryBean.
	 * This will lead to a FactoryBeanNotInitializedException getting thrown.
	 * 
	 * <p> 默认实现返回此FactoryBean的对象类型，前提是它是一个接口，或者为null。 
	 * 后者表示此FactoryBean不支持早期单例访问。 这将导致抛出FactoryBeanNotInitializedException。
	 * 
	 * @return the interfaces to use for 'early singletons',
	 * or {@code null} to indicate a FactoryBeanNotInitializedException
	 * 
	 * <p> 用于'early singletons'的接口，或null表示FactoryBeanNotInitializedException
	 * 
	 * @see org.springframework.beans.factory.FactoryBeanNotInitializedException
	 */
	protected Class[] getEarlySingletonInterfaces() {
		Class type = getObjectType();
		return (type != null && type.isInterface() ? new Class[] {type} : null);
	}

	/**
	 * Callback for destroying a singleton instance. Subclasses may
	 * override this to destroy the previously created instance.
	 * 
	 * <p> 用于销毁单例实例的回调。 子类可以重写此方法以销毁先前创建的实例。
	 * 
	 * <p>The default implementation is empty.
	 * 
	 * <p> 默认实现为空。
	 * 
	 * @param instance the singleton instance, as returned by
	 * {@link #createInstance()}
	 * 
	 * <p> 单例实例，由createInstance（）返回
	 * 
	 * @throws Exception in case of shutdown errors
	 * @see #createInstance()
	 */
	protected void destroyInstance(T instance) throws Exception {
	}


	/**
	 * Reflective InvocationHandler for lazy access to the actual singleton object.
	 * 
	 * <p> Reflective InvocationHandler用于延迟访问实际的单例对象。
	 * 
	 */
	private class EarlySingletonInvocationHandler implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method)) {
				// Only consider equal when proxies are identical.
				// 只有当代理相同时才考虑相等。
				return (proxy == args[0]);
			}
			else if (ReflectionUtils.isHashCodeMethod(method)) {
				// Use hashCode of reference proxy.
				// 使用引用代理的hashCode。
				return System.identityHashCode(proxy);
			}
			else if (!initialized && ReflectionUtils.isToStringMethod(method)) {
				return "Early singleton proxy for interfaces " +
						ObjectUtils.nullSafeToString(getEarlySingletonInterfaces());
			}
			try {
				return method.invoke(getSingletonInstance(), args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
