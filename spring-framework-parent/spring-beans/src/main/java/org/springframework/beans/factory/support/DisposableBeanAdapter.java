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

import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Adapter that implements the {@link DisposableBean} and {@link Runnable} interfaces
 * performing various destruction steps on a given bean instance:
 * 
 * <p> 实现DisposableBean和Runnable接口的适配器在给定的bean实例上执行各种销毁步骤：
 * 
 * <ul>
 * <li>DestructionAwareBeanPostProcessors;
 * <li>the bean implementing DisposableBean itself; - 实现DisposableBean本身的bean;
 * <li>a custom destroy method specified on the bean definition. - bean定义上指定的自定义destroy方法。
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.0
 * @see AbstractBeanFactory
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
 * @see AbstractBeanDefinition#getDestroyMethodName()
 */
@SuppressWarnings("serial")
class DisposableBeanAdapter implements DisposableBean, Runnable, Serializable {

	private static final String CLOSE_METHOD_NAME = "close";

	private static final String SHUTDOWN_METHOD_NAME = "shutdown";

	private static final Log logger = LogFactory.getLog(DisposableBeanAdapter.class);

	private static Class<?> closeableInterface;

	static {
		try {
			closeableInterface = ClassUtils.forName("java.lang.AutoCloseable",
					DisposableBeanAdapter.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			closeableInterface = Closeable.class;
		}
	}


	private final Object bean;

	private final String beanName;

	private final boolean invokeDisposableBean;

	private final boolean nonPublicAccessAllowed;

	private final AccessControlContext acc;

	private String destroyMethodName;

	private transient Method destroyMethod;

	private List<DestructionAwareBeanPostProcessor> beanPostProcessors;


	/**
	 * Create a new DisposableBeanAdapter for the given bean.
	 * 
	 * <p> 为给定的bean创建一个新的DisposableBeanAdapter。
	 * 
	 * @param bean the bean instance (never {@code null}) - bean实例（永不为null）
	 * @param beanName the name of the bean - bean的名称
	 * @param beanDefinition the merged bean definition - 合并的bean定义
	 * @param postProcessors the List of BeanPostProcessors
	 * (potentially DestructionAwareBeanPostProcessor), if any
	 * 
	 * <p> BeanPostProcessors列表（可能是DestructionAwareBeanPostProcessor），如果有的话
	 */
	public DisposableBeanAdapter(Object bean, String beanName, RootBeanDefinition beanDefinition,
			List<BeanPostProcessor> postProcessors, AccessControlContext acc) {

		Assert.notNull(bean, "Disposable bean must not be null");
		this.bean = bean;
		this.beanName = beanName;
		this.invokeDisposableBean =
				(this.bean instanceof DisposableBean && !beanDefinition.isExternallyManagedDestroyMethod("destroy"));
		this.nonPublicAccessAllowed = beanDefinition.isNonPublicAccessAllowed();
		this.acc = acc;
		String destroyMethodName = inferDestroyMethodIfNecessary(bean, beanDefinition);
		if (destroyMethodName != null && !(this.invokeDisposableBean && "destroy".equals(destroyMethodName)) &&
				!beanDefinition.isExternallyManagedDestroyMethod(destroyMethodName)) {
			this.destroyMethodName = destroyMethodName;
			this.destroyMethod = determineDestroyMethod();
			if (this.destroyMethod == null) {
				if (beanDefinition.isEnforceDestroyMethod()) {
					throw new BeanDefinitionValidationException("Couldn't find a destroy method named '" +
							destroyMethodName + "' on bean with name '" + beanName + "'");
				}
			}
			else {
				Class<?>[] paramTypes = this.destroyMethod.getParameterTypes();
				if (paramTypes.length > 1) {
					throw new BeanDefinitionValidationException("Method '" + destroyMethodName + "' of bean '" +
							beanName + "' has more than one parameter - not supported as destroy method");
				}
				else if (paramTypes.length == 1 && !paramTypes[0].equals(boolean.class)) {
					throw new BeanDefinitionValidationException("Method '" + destroyMethodName + "' of bean '" +
							beanName + "' has a non-boolean parameter - not supported as destroy method");
				}
			}
		}
		this.beanPostProcessors = filterPostProcessors(postProcessors);
	}

	/**
	 * Create a new DisposableBeanAdapter for the given bean.
	 * 
	 * <p> 为给定的bean创建一个新的DisposableBeanAdapter。
	 * 
	 */
	private DisposableBeanAdapter(Object bean, String beanName, boolean invokeDisposableBean,
			boolean nonPublicAccessAllowed, String destroyMethodName,
			List<DestructionAwareBeanPostProcessor> postProcessors) {

		this.bean = bean;
		this.beanName = beanName;
		this.invokeDisposableBean = invokeDisposableBean;
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
		this.acc = null;
		this.destroyMethodName = destroyMethodName;
		this.beanPostProcessors = postProcessors;
	}


	/**
	 * If the current value of the given beanDefinition's "destroyMethodName" property is
	 * {@link AbstractBeanDefinition#INFER_METHOD}, then attempt to infer a destroy method.
	 * Candidate methods are currently limited to public, no-arg methods named "close"
	 * (whether declared locally or inherited). The given BeanDefinition's
	 * "destroyMethodName" is updated to be null if no such method is found, otherwise set
	 * to the name of the inferred method. This constant serves as the default for the
	 * {@code @Bean#destroyMethod} attribute and the value of the constant may also be
	 * used in XML within the {@code <bean destroy-method="">} or {@code
	 * <beans default-destroy-method="">} attributes.
	 * 
	 * <p> 如果给定beanDefinition的“destroyMethodName”属性的当前值
	 * 是AbstractBeanDefinition.INFER_METHOD，则尝试推断destroy方法。 
	 * 候选方法目前仅限于名为“close”的公共，非arg方法（无论是在本地声明还是继承）。 
	 * 如果没有找到这样的方法，给定的BeanDefinition的“destroyMethodName”将更新为null，
	 * 否则设置为推断方法的名称。 此常量用作@ Bean＃destroyMethod属性的缺省值，常量的值也可以
	 * 在<bean destroy-method =“”>或<beans default-destroy-method =“”>属性中的XML中使用。
	 * 
	 * <p>Also processes the {@link java.io.Closeable} and {@link java.lang.AutoCloseable}
	 * interfaces, reflectively calling the "close" method on implementing beans as well.
	 * 
	 * <p> 还处理java.io.Closeable和java.lang.AutoCloseable接口，反过来也调用实现bean的“close”方法。
	 */
	private String inferDestroyMethodIfNecessary(Object bean, RootBeanDefinition beanDefinition) {
		if (AbstractBeanDefinition.INFER_METHOD.equals(beanDefinition.getDestroyMethodName()) ||
				(beanDefinition.getDestroyMethodName() == null && closeableInterface.isInstance(bean))) {
			// Only perform destroy method inference or Closeable detection
			// in case of the bean not explicitly implementing DisposableBean
			
			// 仅在bean未显式实现DisposableBean的情况下执行destroy方法推理或Closeable检测
			if (!(bean instanceof DisposableBean)) {
				try {
					return bean.getClass().getMethod(CLOSE_METHOD_NAME).getName();
				}
				catch (NoSuchMethodException ex) {
					try {
						return bean.getClass().getMethod(SHUTDOWN_METHOD_NAME).getName();
					}
					catch (NoSuchMethodException ex2) {
						// no candidate destroy method found
						// 找不到候选破坏方法
					}
				}
			}
			return null;
		}
		return beanDefinition.getDestroyMethodName();
	}

	/**
	 * Search for all DestructionAwareBeanPostProcessors in the List.
	 * 
	 * <p> 在List中搜索所有DestructionAwareBeanPostProcessors。
	 * 
	 * @param postProcessors the List to search - 要搜索的列表
	 * @return the filtered List of DestructionAwareBeanPostProcessors - 已过滤的DestructionAwareBeanPostProcessors列表
	 */
	private List<DestructionAwareBeanPostProcessor> filterPostProcessors(List<BeanPostProcessor> postProcessors) {
		List<DestructionAwareBeanPostProcessor> filteredPostProcessors = null;
		if (postProcessors != null && !postProcessors.isEmpty()) {
			filteredPostProcessors = new ArrayList<DestructionAwareBeanPostProcessor>(postProcessors.size());
			for (BeanPostProcessor postProcessor : postProcessors) {
				if (postProcessor instanceof DestructionAwareBeanPostProcessor) {
					filteredPostProcessors.add((DestructionAwareBeanPostProcessor) postProcessor);
				}
			}
		}
		return filteredPostProcessors;
	}


	public void run() {
		destroy();
	}

	public void destroy() {
		if (this.beanPostProcessors != null && !this.beanPostProcessors.isEmpty()) {
			for (DestructionAwareBeanPostProcessor processor : this.beanPostProcessors) {
				processor.postProcessBeforeDestruction(this.bean, this.beanName);
			}
		}

		if (this.invokeDisposableBean) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking destroy() on bean with name '" + this.beanName + "'");
			}
			try {
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
							((DisposableBean) bean).destroy();
							return null;
						}
					}, acc);
				}
				else {
					((DisposableBean) bean).destroy();
				}
			}
			catch (Throwable ex) {
				String msg = "Invocation of destroy method failed on bean with name '" + this.beanName + "'";
				if (logger.isDebugEnabled()) {
					logger.warn(msg, ex);
				}
				else {
					logger.warn(msg + ": " + ex);
				}
			}
		}

		if (this.destroyMethod != null) {
			invokeCustomDestroyMethod(this.destroyMethod);
		}
		else if (this.destroyMethodName != null) {
			Method methodToCall = determineDestroyMethod();
			if (methodToCall != null) {
				invokeCustomDestroyMethod(methodToCall);
			}
		}
	}


	private Method determineDestroyMethod() {
		try {
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedAction<Method>() {
					public Method run() {
						return findDestroyMethod();
					}
				});
			}
			else {
				return findDestroyMethod();
			}
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionValidationException("Couldn't find a unique destroy method on bean with name '" +
					this.beanName + ": " + ex.getMessage());
		}
	}

	private Method findDestroyMethod() {
		return (this.nonPublicAccessAllowed ?
				BeanUtils.findMethodWithMinimalParameters(this.bean.getClass(), this.destroyMethodName) :
				BeanUtils.findMethodWithMinimalParameters(this.bean.getClass().getMethods(), this.destroyMethodName));
	}

	/**
	 * Invoke the specified custom destroy method on the given bean.
	 * 
	 * <p> 在给定的bean上调用指定的自定义destroy方法。
	 * 
	 * <p>This implementation invokes a no-arg method if found, else checking
	 * for a method with a single boolean argument (passing in "true",
	 * assuming a "force" parameter), else logging an error.
	 * 
	 * <p> 如果找到，此实现将调用no-arg方法，否则检查具有单个boolean参数的方法（传入“true”，假设为“force”参数），否则记录错误。
	 * 
	 */
	private void invokeCustomDestroyMethod(final Method destroyMethod) {
		Class<?>[] paramTypes = destroyMethod.getParameterTypes();
		final Object[] args = new Object[paramTypes.length];
		if (paramTypes.length == 1) {
			args[0] = Boolean.TRUE;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking destroy method '" + this.destroyMethodName +
					"' on bean with name '" + this.beanName + "'");
		}
		try {
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						ReflectionUtils.makeAccessible(destroyMethod);
						return null;
					}
				});
				try {
					AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
							destroyMethod.invoke(bean, args);
							return null;
						}
					}, acc);
				}
				catch (PrivilegedActionException pax) {
					throw (InvocationTargetException) pax.getException();
				}
			}
			else {
				ReflectionUtils.makeAccessible(destroyMethod);
				destroyMethod.invoke(bean, args);
			}
		}
		catch (InvocationTargetException ex) {
			String msg = "Invocation of destroy method '" + this.destroyMethodName +
					"' failed on bean with name '" + this.beanName + "'";
			if (logger.isDebugEnabled()) {
				logger.warn(msg, ex.getTargetException());
			}
			else {
				logger.warn(msg + ": " + ex.getTargetException());
			}
		}
		catch (Throwable ex) {
			logger.error("Couldn't invoke destroy method '" + this.destroyMethodName +
					"' on bean with name '" + this.beanName + "'", ex);
		}
	}


	/**
	 * Serializes a copy of the state of this class,
	 * filtering out non-serializable BeanPostProcessors.
	 * 
	 * <p> 序列化此类状态的副本，过滤掉不可序列化的BeanPostProcessors。
	 * 
	 */
	protected Object writeReplace() {
		List<DestructionAwareBeanPostProcessor> serializablePostProcessors = null;
		if (this.beanPostProcessors != null) {
			serializablePostProcessors = new ArrayList<DestructionAwareBeanPostProcessor>();
			for (DestructionAwareBeanPostProcessor postProcessor : this.beanPostProcessors) {
				if (postProcessor instanceof Serializable) {
					serializablePostProcessors.add(postProcessor);
				}
			}
		}
		return new DisposableBeanAdapter(this.bean, this.beanName, this.invokeDisposableBean,
				this.nonPublicAccessAllowed, this.destroyMethodName, serializablePostProcessors);
	}


	/**
	 * Check whether the given bean has any kind of destroy method to call.
	 * 
	 * <p> 检查给定的bean是否有任何类型的destroy方法来调用。
	 * 
	 * @param bean the bean instance - bean实例
	 * @param beanDefinition the corresponding bean definition - 相应的bean定义
	 */
	public static boolean hasDestroyMethod(Object bean, RootBeanDefinition beanDefinition) {
		if (bean instanceof DisposableBean || closeableInterface.isInstance(bean)) {
			return true;
		}
		String destroyMethodName = beanDefinition.getDestroyMethodName();
		if (AbstractBeanDefinition.INFER_METHOD.equals(destroyMethodName)) {
			return ClassUtils.hasMethod(bean.getClass(), CLOSE_METHOD_NAME);
		}
		return (destroyMethodName != null);
	}

}
