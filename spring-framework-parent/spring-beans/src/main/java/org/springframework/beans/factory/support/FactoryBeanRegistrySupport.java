/*
 * Copyright 2002-2014 the original author or authors.
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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;

/**
 * Support base class for singleton registries which need to handle
 * {@link org.springframework.beans.factory.FactoryBean} instances,
 * integrated with {@link DefaultSingletonBeanRegistry}'s singleton management.
 * 
 * <p>支持需要处理org.springframework.beans.factory.FactoryBean实例的单例注册表的基类，
 * 与DefaultSingletonBeanRegistry的单例管理集成。
 *
 * <p>Serves as base class for {@link AbstractBeanFactory}.
 * <p>用为AbstractBeanFactory的基类。
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

	/** Cache of singleton objects created by FactoryBeans: FactoryBean name --> object */
	/** FactoryBeans创建的单例对象的缓存：FactoryBean名称 - >对象 */
	private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>(16);


	/**
	 * Determine the type for the given FactoryBean.
	 * <p>确定给定FactoryBean的类型。
	 * @param factoryBean the FactoryBean instance to check 检查FactoryBean实例
	 * @return the FactoryBean's object type,
	 * or {@code null} if the type cannot be determined yet
	 * 
	 * <p>FactoryBean的对象类型，如果尚未确定类型，则返回null
	 */
	protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
		try {
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
					public Class<?> run() {
						return factoryBean.getObjectType();
					}
				}, getAccessControlContext());
			}
			else {
				return factoryBean.getObjectType();
			}
		}
		catch (Throwable ex) {
			// Thrown from the FactoryBean's getObjectType implementation.
			logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " +
					"that it should return null if the type of its object cannot be determined yet", ex);
			return null;
		}
	}

	/**
	 * Obtain an object to expose from the given FactoryBean, if available
	 * in cached form. Quick check for minimal synchronization.
	 * 
	 * <p>获取要从给定FactoryBean公开的对象（如果在缓存形式中可用）。 快速检查最小同步。
	 * 
	 * @param beanName the name of the bean  bean的名称
	 * @return the object obtained from the FactoryBean,
	 * or {@code null} if not available
	 * 
	 * <p>从FactoryBean获取的对象，如果不可用则为null
	 */
	protected Object getCachedObjectForFactoryBean(String beanName) {
		Object object = this.factoryBeanObjectCache.get(beanName);
		return (object != NULL_OBJECT ? object : null);
	}

	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * 
	 * <p>获取要从给定FactoryBean公开的对象。
	 * 
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @param shouldPostProcess whether the bean is subject to post-processing - bean是否需要进行后期处理
	 * @return the object obtained from the FactoryBean - 从FactoryBean获取的对象
	 * @throws BeanCreationException if FactoryBean object creation failed - 如果FactoryBean对象创建失败
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
		//如果是单例模式
		if (factory.isSingleton() && containsSingleton(beanName)) {
			synchronized (getSingletonMutex()) {
				Object object = this.factoryBeanObjectCache.get(beanName);
				if (object == null) {
					object = doGetObjectFromFactoryBean(factory, beanName);
					// Only post-process and store if not put there already during getObject() call above
					// (e.g. because of circular reference processing triggered by custom getBean calls)
					Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
					if (alreadyThere != null) {
						object = alreadyThere;
					}
					else {
						if (object != null && shouldPostProcess) {
							try {
								//调用ObjectFactory的后处理器
								object = postProcessObjectFromFactoryBean(object, beanName);
							}
							catch (Throwable ex) {
								throw new BeanCreationException(beanName,
										"Post-processing of FactoryBean's singleton object failed", ex);
							}
						}
						this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
					}
				}
				return (object != NULL_OBJECT ? object : null);
			}
		}
		else {
			Object object = doGetObjectFromFactoryBean(factory, beanName);
			if (object != null && shouldPostProcess) {
				try {
					object = postProcessObjectFromFactoryBean(object, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
				}
			}
			return object;
		}
	}

	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * 
	 * <p>获取要从给定FactoryBean公开的对象。
	 * 
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @return the object obtained from the FactoryBean - 从FactoryBean获取的对象
	 * @throws BeanCreationException if FactoryBean object creation failed - 如果FactoryBean对象创建失败
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
			throws BeanCreationException {

		Object object;
		try {
			//需要权限验证
			if (System.getSecurityManager() != null) {
				AccessControlContext acc = getAccessControlContext();
				try {
					object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
								return factory.getObject();
							}
						}, acc);
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
				//直接调用getObject方法
				object = factory.getObject();
			}
		}
		catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}

		// Do not accept a null value for a FactoryBean that's not fully
		// initialized yet: Many FactoryBeans just return null then.
		//不接受尚未完全初始化的FactoryBean的空值：许多FactoryBeans只返回null。
		if (object == null && isSingletonCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(
					beanName, "FactoryBean which is currently in creation returned null from getObject");
		}
		return object;
	}

	/**
	 * Post-process the given object that has been obtained from the FactoryBean.
	 * The resulting object will get exposed for bean references.
	 * 
	 * <p>对从FactoryBean获取的给定对象进行后处理。 生成的对象将暴露给bean引用。
	 * 
	 * <p>The default implementation simply returns the given object as-is.
	 * Subclasses may override this, for example, to apply post-processors.
	 * 
	 * <p>默认实现只是按原样返回给定的对象。 子类可以覆盖它，例如，应用后处理器。
	 * 
	 * @param object the object obtained from the FactoryBean. - 从FactoryBean获取的对象。
	 * @param beanName the name of the bean
	 * @return the object to expose - 要暴露的对象
	 * @throws org.springframework.beans.BeansException if any post-processing failed - 如果任何后处理失败
	 */
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
		return object;
	}

	/**
	 * Get a FactoryBean for the given bean if possible.
	 * 
	 * <p>如果可能，为给定的bean获取FactoryBean。
	 * 
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance - 相应的bean实例
	 * @return the bean instance as FactoryBean - bean实例为FactoryBean
	 * @throws BeansException if the given bean cannot be exposed as a FactoryBean - 如果给定的bean不能作为FactoryBean公开
	 */
	protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
		if (!(beanInstance instanceof FactoryBean)) {
			throw new BeanCreationException(beanName,
					"Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
		}
		return (FactoryBean<?>) beanInstance;
	}

	/**
	 * Overridden to clear the FactoryBean object cache as well.
	 * 
	 * <p>重写以清除FactoryBean对象缓存。
	 */
	@Override
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		this.factoryBeanObjectCache.remove(beanName);
	}

	/**
	 * Returns the security context for this bean factory. If a security manager
	 * is set, interaction with the user code will be executed using the privileged
	 * of the security context returned by this method.
	 * 
	 * <p>返回此Bean工厂的安全上下文。 如果设置了安全管理器，则将使用此方法返回的安全上下文的特权执行与用户代码的交互。
	 * 
	 * @see AccessController#getContext()
	 */
	protected AccessControlContext getAccessControlContext() {
		return AccessController.getContext();
	}

}
