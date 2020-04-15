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

package org.springframework.web.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Encapsulates information about a handler method consisting of a {@linkplain #getMethod() method}
 * and a {@linkplain #getBean() bean}. Provides convenient access to method parameters,
 * method return value, method annotations.
 * 
 * <p> 封装有关由方法和bean组成的处理程序方法的信息。 提供对方法参数，方法返回值，方法注释的方便访问。
 *
 * <p>The class may be created with a bean instance or with a bean name (e.g. lazy-init bean,
 * prototype bean). Use {@link #createWithResolvedBean()} to obtain a {@link HandlerMethod}
 * instance with a bean instance resolved through the associated {@link BeanFactory}.
 * 
 * <p> 可以使用bean实例或bean名称（例如，lazy-init bean，prototype bean）创建该类。 
 * 使用createWithResolvedBean（）获取HandlerMethod实例，其中bean实例通过关联的BeanFactory解析。
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class HandlerMethod {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(HandlerMethod.class);

	private final Object bean;

	private final BeanFactory beanFactory;

	private final Method method;

	private final Method bridgedMethod;

	private final MethodParameter[] parameters;


	/**
	 * Create an instance from a bean instance and a method.
	 * 
	 * <p> 从bean实例和方法创建实例。
	 */
	public HandlerMethod(Object bean, Method method) {
		Assert.notNull(bean, "Bean is required");
		Assert.notNull(method, "Method is required");
		this.bean = bean;
		this.beanFactory = null;
		this.method = method;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.parameters = initMethodParameters();
	}

	/**
	 * Create an instance from a bean instance, method name, and parameter types.
	 * 
	 * <p> 从Bean实例，方法名称和参数类型创建实例。
	 * 
	 * @throws NoSuchMethodException when the method cannot be found
	 * 
	 * <p> 当无法找到方法时
	 */
	public HandlerMethod(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Assert.notNull(bean, "Bean is required");
		Assert.notNull(methodName, "Method name is required");
		this.bean = bean;
		this.beanFactory = null;
		this.method = bean.getClass().getMethod(methodName, parameterTypes);
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(this.method);
		this.parameters = initMethodParameters();
	}

	/**
	 * Create an instance from a bean name, a method, and a {@code BeanFactory}.
	 * The method {@link #createWithResolvedBean()} may be used later to
	 * re-create the {@code HandlerMethod} with an initialized the bean.
	 * 
	 * <p> 从bean名称，方法和BeanFactory创建实例。 稍后可以使用
	 * createWithResolvedBean（）方法通过初始化bean重新创建HandlerMethod。
	 */
	public HandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
		Assert.hasText(beanName, "Bean name is required");
		Assert.notNull(beanFactory, "BeanFactory is required");
		Assert.notNull(method, "Method is required");
		Assert.isTrue(beanFactory.containsBean(beanName),
				"BeanFactory [" + beanFactory + "] does not contain bean [" + beanName + "]");
		this.bean = beanName;
		this.beanFactory = beanFactory;
		this.method = method;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.parameters = initMethodParameters();
	}

	/**
	 * Copy constructor for use in sub-classes.
	 * 
	 * <p> 复制构造函数以在子类中使用。
	 */
	protected HandlerMethod(HandlerMethod handlerMethod) {
		Assert.notNull(handlerMethod, "HandlerMethod is required");
		this.bean = handlerMethod.bean;
		this.beanFactory = handlerMethod.beanFactory;
		this.method = handlerMethod.method;
		this.bridgedMethod = handlerMethod.bridgedMethod;
		this.parameters = handlerMethod.parameters;
	}

	/**
	 * Re-create HandlerMethod with the resolved handler.
	 */
	private HandlerMethod(HandlerMethod handlerMethod, Object handler) {
		Assert.notNull(handlerMethod, "HandlerMethod is required");
		Assert.notNull(handler, "Handler object is required");
		this.bean = handler;
		this.beanFactory = handlerMethod.beanFactory;
		this.method = handlerMethod.method;
		this.bridgedMethod = handlerMethod.bridgedMethod;
		this.parameters = handlerMethod.parameters;
	}


	private MethodParameter[] initMethodParameters() {
		int count = this.bridgedMethod.getParameterTypes().length;
		MethodParameter[] result = new MethodParameter[count];
		for (int i = 0; i < count; i++) {
			result[i] = new HandlerMethodParameter(i);
		}
		return result;
	}

	/**
	 * Returns the bean for this handler method.
	 * 
	 * <p> 返回此处理程序方法的bean。
	 */
	public Object getBean() {
		return this.bean;
	}

	/**
	 * Returns the method for this handler method.
	 * 
	 * <p> 返回此处理程序方法的方法。
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Returns the type of the handler for this handler method.
	 * Note that if the bean type is a CGLIB-generated class, the original, user-defined class is returned.
	 * 
	 * <p> 返回此处理程序方法的处理程序的类型。 请注意，如果bean类型是CGLIB生成的类，则返回原始的用户定义类。
	 */
	public Class<?> getBeanType() {
		Class<?> clazz = (this.bean instanceof String ?
				this.beanFactory.getType((String) this.bean) : this.bean.getClass());
		return ClassUtils.getUserClass(clazz);
	}

	/**
	 * If the bean method is a bridge method, this method returns the bridged (user-defined) method.
	 * Otherwise it returns the same method as {@link #getMethod()}.
	 * 
	 * <p> 如果bean方法是桥接方法，则此方法返回桥接（用户定义）方法。 否则返回与getMethod（）相同的方法。
	 */
	protected Method getBridgedMethod() {
		return this.bridgedMethod;
	}

	/**
	 * Returns the method parameters for this handler method.
	 * 
	 * <p> 返回此处理程序方法的方法参数。
	 */
	public MethodParameter[] getMethodParameters() {
		return this.parameters;
	}

	/**
	 * Return the HandlerMethod return type.
	 * 
	 * <p> 返回HandlerMethod返回类型。
	 */
	public MethodParameter getReturnType() {
		return new HandlerMethodParameter(-1);
	}

	/**
	 * Return the actual return value type.
	 * 
	 * <p> 返回实际的返回值类型。
	 */
	public MethodParameter getReturnValueType(Object returnValue) {
		return new ReturnValueMethodParameter(returnValue);
	}

	/**
	 * Returns {@code true} if the method return type is void, {@code false} otherwise.
	 * 
	 * <p> 如果方法返回类型为void，则返回true，否则返回false。
	 */
	public boolean isVoid() {
		return Void.TYPE.equals(getReturnType().getParameterType());
	}

	/**
	 * Returns a single annotation on the underlying method traversing its super methods if no
	 * annotation can be found on the given method itself.
	 * 
	 * <p> 如果在给定方法本身上找不到注释，则返回遍历其超级方法的基础方法上的单个注释。
	 * 
	 * @param annotationType the type of annotation to introspect the method for.
	 * 
	 * <p> 内省方法的注释类型。
	 * 
	 * @return the annotation, or {@code null} if none found
	 * 
	 * <p> 注释，如果没有找到则为null
	 */
	public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(this.method, annotationType);
	}

	/**
	 * If the provided instance contains a bean name rather than an object instance, the bean name is resolved
	 * before a {@link HandlerMethod} is created and returned.
	 * 
	 * <p> 如果提供的实例包含bean名称而不是对象实例，则在创建并返回HandlerMethod之前解析bean名称。
	 */
	public HandlerMethod createWithResolvedBean() {
		Object handler = this.bean;
		if (this.bean instanceof String) {
			String beanName = (String) this.bean;
			handler = this.beanFactory.getBean(beanName);
		}
		return new HandlerMethod(this, handler);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof HandlerMethod) {
			HandlerMethod other = (HandlerMethod) obj;
			return (this.bean.equals(other.bean) && this.method.equals(other.method));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.bean.hashCode() * 31 + this.method.hashCode();
	}

	@Override
	public String toString() {
		return this.method.toGenericString();
	}


	/**
	 * A MethodParameter with HandlerMethod-specific behavior.
	 * 
	 * <p> 具有HandlerMethod特定行为的MethodParameter。
	 */
	private class HandlerMethodParameter extends MethodParameter {

		public HandlerMethodParameter(int index) {
			super(HandlerMethod.this.bridgedMethod, index);
		}

		@Override
		public Class<?> getDeclaringClass() {
			return HandlerMethod.this.getBeanType();
		}

		@Override
		public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
			return HandlerMethod.this.getMethodAnnotation(annotationType);
		}
	}


	/**
	 * A MethodParameter for a HandlerMethod return type based on an actual return value.
	 * 
	 * <p> HandlerMethod的MethodParameter基于实际返回值返回类型。
	 */
	private class ReturnValueMethodParameter extends HandlerMethodParameter {

		private final Object returnValue;

		public ReturnValueMethodParameter(Object returnValue) {
			super(-1);
			this.returnValue = returnValue;
		}

		@Override
		public Class<?> getParameterType() {
			return (this.returnValue != null ? this.returnValue.getClass() : super.getParameterType());
		}
	}

}
