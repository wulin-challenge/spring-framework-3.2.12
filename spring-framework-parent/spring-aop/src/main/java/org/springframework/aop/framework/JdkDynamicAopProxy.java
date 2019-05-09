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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * JDK-based {@link AopProxy} implementation for the Spring AOP framework,
 * based on JDK {@link java.lang.reflect.Proxy dynamic proxies}.
 * 
 * <p> 基于JDK动态代理的Spring AOP框架的基于JDK的AopProxy实现。
 *
 * <p>Creates a dynamic proxy, implementing the interfaces exposed by
 * the AopProxy. Dynamic proxies <i>cannot</i> be used to proxy methods
 * defined in classes, rather than interfaces.
 * 
 * <p> 创建动态代理，实现AopProxy公开的接口。 动态代理不能用于代理在类中定义的方法，而不是接口。
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an {@link AdvisedSupport} class. This class is internal
 * to Spring's AOP framework and need not be used directly by client code.
 * 
 * <p> 此类对象应通过代理工厂获取，由AdvisedSupport类配置。 该类是Spring的AOP框架的内部，
 * 不需要由客户端代码直接使用。
 *
 * <p>Proxies created using this class will be thread-safe if the
 * underlying (target) class is thread-safe.
 * 
 * <p> 如果底层（目标）类是线程安全的，使用此类创建的代理将是线程安全的。
 *
 * <p>Proxies are serializable so long as all Advisors (including Advices
 * and Pointcuts) and the TargetSource are serializable.
 * 
 * <p> 只要所有Advisors（包括Advices和Pointcuts）和TargetSource都是可序列化的，代理就是可序列化的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @see java.lang.reflect.Proxy
 * @see AdvisedSupport
 * @see ProxyFactory
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	/** 使用Spring 1.2中的serialVersionUID实现互操作性 */
	private static final long serialVersionUID = 5531744639992436476L;


	/*
	 * NOTE: We could avoid the code duplication between this class and the CGLIB
	 * proxies by refactoring "invoke" into a template method. However, this approach
	 * adds at least 10% performance overhead versus a copy-paste solution, so we sacrifice
	 * elegance for performance. (We have a good test suite to ensure that the different
	 * proxies behave the same :-)
	 * This way, we can also more easily take advantage of minor optimizations in each class.
	 * 
	 * <p> 注意：我们可以通过将“invoke”重构为模板方法来避免此类与CGLIB代理之间的代码重复。 但是，与复制粘贴解决方案相比，
	 * 这种方法增加了至少10％的性能开销，因此我们牺牲了优雅的性能。 （我们有一个很好的测试套件，以确保不同的代理行为相同:-)这样，
	 * 我们也可以更轻松地利用每个类中的次要优化。
	 */

	/** We use a static Log to avoid serialization issues */
	/** 我们使用静态日志来避免序列化问题 */
	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/** Config used to configure this proxy */
	/** 配置用于配置此代理 */
	private final AdvisedSupport advised;

	/**
	 * Is the {@link #equals} method defined on the proxied interfaces?
	 * 
	 * <p> 是否在代理接口上定义了equals方法？
	 */
	private boolean equalsDefined;

	/**
	 * Is the {@link #hashCode} method defined on the proxied interfaces?
	 * 
	 * <p> 是否在代理接口上定义了hashCode方法？
	 */
	private boolean hashCodeDefined;


	/**
	 * Construct a new JdkDynamicAopProxy for the given AOP configuration.
	 * 
	 * <p> 为给定的AOP配置构造一个新的JdkDynamicAopProxy。
	 * 
	 * @param config the AOP configuration as AdvisedSupport object
	 * 
	 * <p> AOP配置为AdvisedSupport对象
	 * 
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 * 
	 * <p> 如果配置无效。 在这种情况下，我们试图抛出一个信息异常，而不是让一个神秘的失败发生。
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
	}


	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
		}
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}

	/**
	 * Finds any {@link #equals} or {@link #hashCode} method that may be defined
	 * on the supplied set of interfaces.
	 * 
	 * <p> 查找可在所提供的接口集上定义的任何equals或hashCode方法。
	 * 
	 * @param proxiedInterfaces the interfaces to introspect - 内省的接口
	 */
	private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
		for (Class<?> proxiedInterface : proxiedInterfaces) {
			Method[] methods = proxiedInterface.getDeclaredMethods();
			for (Method method : methods) {
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			}
		}
	}


	/**
	 * Implementation of {@code InvocationHandler.invoke}.
	 * 
	 * <p> InvocationHandler.invoke的实现。
	 * 
	 * <p>Callers will see exactly the exception thrown by the target,
	 * unless a hook method throws an exception.
	 * 
	 * <p> 除非钩子方法抛出异常，否则调用者将完全看到目标抛出的异常。
	 * 
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Class<?> targetClass = null;
		Object target = null;

		try {
			//equals方法的处理
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// The target does not implement the equals(Object) method itself.
				// 目标不实现equals（Object）方法本身。
				return equals(args[0]);
			}
			// hashCode方法的处理
			if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// The target does not implement the hashCode() method itself.
				// 目标不实现hashCode（）方法本身。
				return hashCode();
			}
			
			/**
			 * Class类的isAssignableFrom(Class cls) 方法
			 * 如果调用这个方法的class或接口 与 参数 cls表示的类或接口相同,或者是参数cls表示的类或接口的父类,则返回true.
			 * 形象地: 自身类.class.isAssignableFrom(自身类或子类.class) 返回true
			 */
			if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// Service invocations on ProxyConfig with the proxy config...
				// ProxyConfig上的服务调用与代理配置...
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;

			//有时候目标对象内部的自我调用将无法实施切面中的增强则需要通过此属性暴露代理
			if (this.advised.exposeProxy) {
				// Make invocation available if necessary.
				// 如有必要，可以进行调用。
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// May be null. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			// 可能为空。 尽可能缩短我们“拥有”目标的时间，以防它来自一个池。
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}

			// Get the interception chain for this method.
			// 获取当前方法的拦截链。
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			// Check whether we have any advice. If we don't, we can fallback on direct
			// reflective invocation of the target, and avoid creating a MethodInvocation.
			// 检查我们是否有任何建议。 如果我们不这样做，我们可以回退直接反射调用目标，并避免创建MethodInvocation。
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
				// 我们可以跳过创建一个MethodInvocation：直接调用目标请注意，最终的调用者必须是一个InvokerInterceptor，
				// 所以我们知道它只对目标进行反射操作，没有热插拔或花哨的代理。
				
				//如果没有发现任何拦截器那么直接调用节点方法
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
			}
			else {
				// We need to create a method invocation...
				// 我们需要创建一个方法调用...
				
				/**
				 * 将拦截器封装在 ReflectiveMethodInvocation,以便于使用其 proceed 进行链接表拦截器
				 */
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// Proceed to the joinpoint through the interceptor chain.
				// 通过拦截器链进入连接点。
				
				// 执行拦截器链
				retVal = invocation.proceed();
			}

			// Massage return value if necessary.
			// 必要时消息返回值。
			Class<?> returnType = method.getReturnType();
			// 返回结果
			if (retVal != null && retVal == target && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// Special case: it returned "this" and the return type of the method
				// is type-compatible. Note that we can't help if the target sets
				// a reference to itself in another returned object.
				
				/**
				 * 特殊情况：它返回“this”，方法的返回类型是类型兼容的。 请注意，如果目标在另一个返回的对象中设置对自身的引用，
				 * 我们将无法帮助您。
				 */
				retVal = proxy;
			}
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource.
				// 必须来自TargetSource。
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// Restore old proxy.
				// 恢复旧代理。
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Equality means interfaces, advisors and TargetSource are equal.
	 * 
	 * <p> 平等意味着接口，顾问和TargetSource是相同的。
	 * 
	 * <p>The compared object may be a JdkDynamicAopProxy instance itself
	 * or a dynamic proxy wrapping a JdkDynamicAopProxy instance.
	 * 
	 * <p> 比较对象可以是JdkDynamicAopProxy实例本身，也可以是包装JdkDynamicAopProxy实例的动态代理。
	 * 
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		JdkDynamicAopProxy otherProxy;
		if (other instanceof JdkDynamicAopProxy) {
			otherProxy = (JdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = (JdkDynamicAopProxy) ih;
		}
		else {
			// Not a valid comparison...
			return false;
		}

		// If we get here, otherProxy is the other AopProxy.
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * Proxy uses the hash code of the TargetSource.
	 * 
	 * <p> Proxy使用TargetSource的哈希码。
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

}
