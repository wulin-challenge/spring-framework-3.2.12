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

package org.springframework.aop.framework;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;

/**
 * Spring's implementation of the AOP Alliance
 * {@link org.aopalliance.intercept.MethodInvocation} interface,
 * implementing the extended
 * {@link org.springframework.aop.ProxyMethodInvocation} interface.
 * 
 * <p> Spring实现了AOP Alliance org.aopalliance.intercept.MethodInvocation接口，
 * 实现了扩展的org.springframework.aop.ProxyMethodInvocation接口。
 *
 * <p>Invokes the target object using reflection. Subclasses can override the
 * {@link #invokeJoinpoint()} method to change this behavior, so this is also
 * a useful base class for more specialized MethodInvocation implementations.
 * 
 * <p> 使用反射调用目标对象。 子类可以重写invokeJoinpoint（）方法来更改此行为，
 * 因此这也是更专业的MethodInvocation实现的有用基类。
 *
 * <p>It is possible to clone an invocation, to invoke {@link #proceed()}
 * repeatedly (once per clone), using the {@link #invocableClone()} method.
 * It is also possible to attach custom attributes to the invocation,
 * using the {@link #setUserAttribute} / {@link #getUserAttribute} methods.
 * 
 * <p> 可以使用invocableClone（）方法克隆调用，重复调用proceed（）（每个克隆一次）。 
 * 也可以使用setUserAttribute / getUserAttribute方法将自定义属性附加到调用。
 *
 * <p><b>NOTE:</b> This class is considered internal and should not be
 * directly accessed. The sole reason for it being public is compatibility
 * with existing framework integrations (e.g. Pitchfork). For any other
 * purposes, use the {@link ProxyMethodInvocation} interface instead.
 * 
 * <p> 注意：此类被视为内部类，不应直接访问。 公开的唯一原因是与现有框架集成（例如Pitchfork）的兼容性。 
 * 出于任何其他目的，请改用ProxyMethodInvocation接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @see #invokeJoinpoint
 * @see #proceed
 * @see #invocableClone
 * @see #setUserAttribute
 * @see #getUserAttribute
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {

	/**
	 * 调用的代理对象
	 */
	protected final Object proxy;

	/**
	 * 要调用的目标对象
	 */
	protected final Object target;

	/**
	 * 要调用的目标方法
	 */
	protected final Method method;

	/**
	 * 用于调用的目标方法的参数
	 */
	protected Object[] arguments;

	/**
	 *  MethodMatcher调用的目标类
	 */
	private final Class targetClass;

	/**
	 * Lazily initialized map of user-specific attributes for this invocation.
	 * 
	 * <p> Lazily初始化了此调用的用户特定属性的映射。
	 * 
	 */
	private Map<String, Object> userAttributes;

	/**
	 * List of MethodInterceptor and InterceptorAndDynamicMethodMatcher
	 * that need dynamic checks.
	 * 
	 * <p> 需要动态检查的MethodInterceptor和InterceptorAndDynamicMethodMatcher的列表。
	 * 
	 */
	protected final List interceptorsAndDynamicMethodMatchers;

	/**
	 * Index from 0 of the current interceptor we're invoking.
	 * -1 until we invoke: then the current interceptor.
	 * 
	 * <p> 从我们正在调用的当前拦截器的0开始的索引。 -1直到我们调用：然后是当前的拦截器。
	 * 
	 */
	private int currentInterceptorIndex = -1;


	/**
	 * Construct a new ReflectiveMethodInvocation with the given arguments.
	 * 
	 * <p> 使用给定的参数构造一个新的ReflectiveMethodInvocation。
	 * 
	 * @param proxy the proxy object that the invocation was made on - 调用的代理对象
	 * @param target the target object to invoke - 要调用的目标对象
	 * @param method the method to invoke - 要调用的方法
	 * @param arguments the arguments to invoke the method with - 用于调用方法的参数
	 * @param targetClass the target class, for MethodMatcher invocations - MethodMatcher调用的目标类
	 * @param interceptorsAndDynamicMethodMatchers interceptors that should be applied,
	 * along with any InterceptorAndDynamicMethodMatchers that need evaluation at runtime.
	 * MethodMatchers included in this struct must already have been found to have matched
	 * as far as was possibly statically. Passing an array might be about 10% faster,
	 * but would complicate the code. And it would work only for static pointcuts.
	 * 
	 * <p> 应该应用的拦截器，以及需要在运行时进行评估的任何InterceptorAndDynamicMethodMatchers。 
	 * 必须已经发现此结构中包含的MethodMatchers已尽可能静态匹配。 传递数组可能会快10％左右，但会使代码复杂化。 它只适用于静态切入点。
	 * 
	 */
	protected ReflectiveMethodInvocation(
			Object proxy, Object target, Method method, Object[] arguments,
			Class targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {

		this.proxy = proxy;
		this.target = target;
		this.targetClass = targetClass;
		this.method = BridgeMethodResolver.findBridgedMethod(method);
		this.arguments = arguments;
		this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
	}


	public final Object getProxy() {
		return this.proxy;
	}

	public final Object getThis() {
		return this.target;
	}

	public final AccessibleObject getStaticPart() {
		return this.method;
	}

	/**
	 * Return the method invoked on the proxied interface.
	 * May or may not correspond with a method invoked on an underlying
	 * implementation of that interface.
	 * 
	 * <p> 返回在代理接口上调用的方法。 可能或可能不对应于在该接口的底层实现上调用的方法。
	 * 
	 */
	public final Method getMethod() {
		return this.method;
	}

	public final Object[] getArguments() {
		return (this.arguments != null ? this.arguments : new Object[0]);
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}


	/**
	 * 继续前进到链中的下一个拦截器。
	 * <p> 此方法的实现和语义取决于实际的连接点类型（请参阅子接口）。
	 * 
	 * @return 查看子接口的继续定义。
	 * @throws Throwable - 如果连接点抛出异常。
	 */
	public Object proceed() throws Throwable {
		//	We start with an index of -1 and increment early.
		// 我们从-1的索引开始并提前递增。
		
		//执行完所有增强后执行切点方法
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}

		//获取下一个要执行的拦截器
		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			// Evaluate dynamic method matcher here: static part will already have
			// been evaluated and found to match.
			
			// 在此评估动态方法匹配器：静态部件已经过评估并且发现匹配。
			
			//动态匹配
			InterceptorAndDynamicMethodMatcher dm =
					(InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
				return dm.interceptor.invoke(this);
			}
			else {
				// Dynamic matching failed.
				// Skip this interceptor and invoke the next in the chain.
				
				// 动态匹配失败。 跳过此拦截器并调用链中的下一个。
				
				//不匹配则不执行拦截器
				return proceed();
			}
		}
		else {
			// It's an interceptor, so we just invoke it: The pointcut will have
			// been evaluated statically before this object was constructed.
			
			// 它是一个拦截器，所以我们只是调用它：在构造这个对象之前，将对切入点进行静态评估。
			
			/**
			 * 普通拦截器,直接调用拦截器,比如
			 * ExposeInvocationInterceptor
			 * DelegatePerTargetObjectIntroductionInterceptor
			 * MethodBeforeAdviceInterceptor
			 * AspectjAroundAdvice,
			 * AspectjAfterAdvice
			 */
			//将this作为参数传递以保证当前实例中调用链的执行
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}

	/**
	 * Invoke the joinpoint using reflection.
	 * Subclasses can override this to use custom invocation.
	 * 
	 * <p> 使用反射调用连接点。 子类可以覆盖它以使用自定义调用。
	 * 
	 * @return the return value of the joinpoint - 连接点的返回值
	 * @throws Throwable if invoking the joinpoint resulted in an exception - 如果调用joinpoint导致异常
	 */
	protected Object invokeJoinpoint() throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
	}


	/**
	 * This implementation returns a shallow copy of this invocation object,
	 * including an independent copy of the original arguments array.
	 * 
	 * <p> 此实现返回此调用对象的浅表副本，包括原始参数数组的独立副本。
	 * 
	 * <p>We want a shallow copy in this case: We want to use the same interceptor
	 * chain and other object references, but we want an independent value for the
	 * current interceptor index.
	 * 
	 * <p> 在这种情况下我们想要一个浅拷贝：我们想要使用相同的拦截器链和其他对象引用，但我们想要一个当前拦截器索引的独立值。
	 * 
	 * @see java.lang.Object#clone()
	 */
	public MethodInvocation invocableClone() {
		Object[] cloneArguments = null;
		if (this.arguments != null) {
			// Build an independent copy of the arguments array.
			// 构建arguments数组的独立副本。
			cloneArguments = new Object[this.arguments.length];
			System.arraycopy(this.arguments, 0, cloneArguments, 0, this.arguments.length);
		}
		return invocableClone(cloneArguments);
	}

	/**
	 * This implementation returns a shallow copy of this invocation object,
	 * using the given arguments array for the clone.
	 * 
	 * <p> 此实现使用克隆的给定arguments数组返回此调用对象的浅表副本。
	 * 
	 * <p>We want a shallow copy in this case: We want to use the same interceptor
	 * chain and other object references, but we want an independent value for the
	 * current interceptor index.
	 * 
	 * <p> 在这种情况下我们想要一个浅拷贝：我们想要使用相同的拦截器链和其他对象引用，但我们想要一个当前拦截器索引的独立值。
	 * 
	 * @see java.lang.Object#clone()
	 */
	public MethodInvocation invocableClone(Object[] arguments) {
		// Force initialization of the user attributes Map,
		// for having a shared Map reference in the clone.
		
		// 强制初始化用户属性Map，以便在克隆中具有共享Map引用。
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<String, Object>();
		}

		// Create the MethodInvocation clone.
		// 创建MethodInvocation克隆。
		try {
			ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
			clone.arguments = arguments;
			return clone;
		}
		catch (CloneNotSupportedException ex) {
			throw new IllegalStateException(
					"Should be able to clone object of type [" + getClass() + "]: " + ex);
		}
	}


	public void setUserAttribute(String key, Object value) {
		if (value != null) {
			if (this.userAttributes == null) {
				this.userAttributes = new HashMap<String, Object>();
			}
			this.userAttributes.put(key, value);
		}
		else {
			if (this.userAttributes != null) {
				this.userAttributes.remove(key);
			}
		}
	}

	public Object getUserAttribute(String key) {
		return (this.userAttributes != null ? this.userAttributes.get(key) : null);
	}

	/**
	 * Return user attributes associated with this invocation.
	 * This method provides an invocation-bound alternative to a ThreadLocal.
	 * 
	 * <p> 返回与此调用关联的用户属性。 此方法提供ThreadLocal的调用绑定替代方法。
	 * 
	 * <p>This map is initialized lazily and is not used in the AOP framework itself.
	 * 
	 * <p> 此映射是惰性初始化的，不在AOP框架本身中使用。
	 * 
	 * @return any user attributes associated with this invocation
	 * (never {@code null})
	 * 
	 * <p> 与此调用关联的任何用户属性（从不为null）
	 * 
	 */
	public Map<String, Object> getUserAttributes() {
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<String, Object>();
		}
		return this.userAttributes;
	}


	@Override
	public String toString() {
		// Don't do toString on target, it may be proxied.
		// 不要在目标上执行toString，它可能是代理的。
		StringBuilder sb = new StringBuilder("ReflectiveMethodInvocation: ");
		sb.append(this.method).append("; ");
		if (this.target == null) {
			sb.append("target is null");
		}
		else {
			sb.append("target is of class [").append(this.target.getClass().getName()).append(']');
		}
		return sb.toString();
	}

}
