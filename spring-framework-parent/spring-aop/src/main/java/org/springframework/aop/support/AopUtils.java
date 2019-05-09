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

package org.springframework.aop.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Utility methods for AOP support code.
 * Mainly for internal use within Spring's AOP support.
 * 
 * <p> AOP支持代码的实用方法。 主要供Spring内部使用，支持Spring。
 *
 * <p>See {@link org.springframework.aop.framework.AopProxyUtils} for a
 * collection of framework-specific AOP utility methods which depend
 * on internals of Spring's AOP framework implementation.
 * 
 * <p> 有关特定于框架的AOP实用程序方法的集合，请参阅
 * org.springframework.aop.framework.AopProxyUtils，这些方法依赖于Spring的AOP框架实现的内部。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

	/**
	 * Check whether the given object is a JDK dynamic proxy or a CGLIB proxy.
	 * 
	 * <p> 检查给定对象是JDK动态代理还是CGLIB代理。
	 * 
	 * @param object the object to check - 要检查的对象
	 * @see #isJdkDynamicProxy
	 * @see #isCglibProxy
	 */
	public static boolean isAopProxy(Object object) {
		return (object instanceof SpringProxy &&
				(Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass())));
	}

	/**
	 * Check whether the given object is a JDK dynamic proxy.
	 * 
	 * <p> 检查给定对象是否是JDK动态代理。
	 * 
	 * @param object the object to check - 要检查的对象
	 * @see java.lang.reflect.Proxy#isProxyClass
	 */
	public static boolean isJdkDynamicProxy(Object object) {
		return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
	}

	/**
	 * Check whether the given object is a CGLIB proxy. Goes beyond the implementation
	 * in {@link ClassUtils#isCglibProxy(Object)} by checking also to see if the given
	 * object is an instance of {@link SpringProxy}.
	 * 
	 * <p> 检查给定对象是否为CGLIB代理。 通过检查给定对象是否是SpringProxy的实例，
	 * 超出了ClassUtils.isCglibProxy（Object）中的实现。
	 * 
	 * @param object the object to check - 要检查的对象
	 * @see ClassUtils#isCglibProxy(Object)
	 */
	public static boolean isCglibProxy(Object object) {
		return (object instanceof SpringProxy && ClassUtils.isCglibProxy(object));
	}

	/**
	 * Check whether the specified class is a CGLIB-generated class.
	 * 
	 * <p> 检查指定的类是否是CGLIB生成的类。
	 * 
	 * @param clazz the class to check - 要检查的class
	 * 
	 * @deprecated as of Spring 3.1 in favor of {@link ClassUtils#isCglibProxyClass(Class)}
	 * 
	 * <p> 从Spring 3.1开始，支持ClassUtils.isCglibProxyClass（Class）
	 * 
	 */
	@Deprecated
	public static boolean isCglibProxyClass(Class<?> clazz) {
		return ClassUtils.isCglibProxyClass(clazz);
	}

	/**
	 * Check whether the specified class name is a CGLIB-generated class.
	 * 
	 * <p> 检查指定的类名是否是CGLIB生成的类。
	 * 
	 * @param className the class name to check - 要检查的类名
	 * 
	 * @deprecated as of Spring 3.1 in favor of {@link ClassUtils#isCglibProxyClassName(String)}
	 * 
	 * <p> 从Spring 3.1开始，支持ClassUtils.isCglibProxyClassName（String）
	 * 
	 */
	@Deprecated
	public static boolean isCglibProxyClassName(String className) {
		return ClassUtils.isCglibProxyClassName(className);
	}

	/**
	 * Determine the target class of the given bean instance which might be an AOP proxy.
	 * 
	 * <p> 确定给定bean实例的目标类，该实例可能是AOP代理。
	 * 
	 * <p>Returns the target class for an AOP proxy and the plain class else.
	 * 
	 * <p> 返回AOP代理和普通类else的目标类。
	 * 
	 * @param candidate the instance to check (might be an AOP proxy)
	 * 
	 * <p> 要检查的实例（可能是AOP代理）
	 * 
	 * @return the target class (or the plain class of the given object as fallback;
	 * never {@code null})
	 * 
	 * <p> 目标类（或给定对象的普通类作为后备;永远不为null）
	 * 
	 * @see org.springframework.aop.TargetClassAware#getTargetClass()
	 * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)
	 */
	public static Class<?> getTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Class<?> result = null;
		if (candidate instanceof TargetClassAware) {
			result = ((TargetClassAware) candidate).getTargetClass();
		}
		if (result == null) {
			result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}

	/**
	 * Determine whether the given method is an "equals" method.
	 * 
	 * <p> 确定给定方法是否为“等于”方法。
	 * 
	 * @see java.lang.Object#equals
	 */
	public static boolean isEqualsMethod(Method method) {
		return ReflectionUtils.isEqualsMethod(method);
	}

	/**
	 * Determine whether the given method is a "hashCode" method.
	 * 
	 * <p> 确定给定方法是否为“hashCode”方法。
	 * 
	 * @see java.lang.Object#hashCode
	 */
	public static boolean isHashCodeMethod(Method method) {
		return ReflectionUtils.isHashCodeMethod(method);
	}

	/**
	 * Determine whether the given method is a "toString" method.
	 * 
	 * <p> 确定给定方法是否为“toString”方法。
	 * 
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToStringMethod(Method method) {
		return ReflectionUtils.isToStringMethod(method);
	}

	/**
	 * Determine whether the given method is a "finalize" method.
	 * 
	 * <p> 确定给定方法是否为“finalize”方法。
	 * 
	 * @see java.lang.Object#finalize()
	 */
	public static boolean isFinalizeMethod(Method method) {
		return (method != null && method.getName().equals("finalize") &&
				method.getParameterTypes().length == 0);
	}

	/**
	 * Given a method, which may come from an interface, and a target class used
	 * in the current AOP invocation, find the corresponding target method if there
	 * is one. E.g. the method may be {@code IFoo.bar()} and the target class
	 * may be {@code DefaultFoo}. In this case, the method may be
	 * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.
	 * 
	 * <p> 给定一个可能来自接口的方法，以及当前AOP调用中使用的目标类，找到相应的目标方法（如果有）。 例如。 
	 * 方法可以是IFoo.bar（），目标类可以是DefaultFoo。 在这种情况下，该方法可以是DefaultFoo.bar（）。
	 *  这样可以找到该方法的属性。
	 * 
	 * <p><b>NOTE:</b> In contrast to {@link org.springframework.util.ClassUtils#getMostSpecificMethod},
	 * this method resolves Java 5 bridge methods in order to retrieve attributes
	 * from the <i>original</i> method definition.
	 * 
	 * <p> 注意：与org.springframework.util.ClassUtils.getMostSpecificMethod相比，
	 * 此方法解析Java 5桥接方法，以便从原始方法定义中检索属性。
	 * 
	 * @param method the method to be invoked, which may come from an interface
	 * 
	 * <p> 要调用的方法，可能来自接口
	 * 
	 * @param targetClass the target class for the current invocation.
	 * May be {@code null} or may not even implement the method.
	 * 
	 * <p> 当前调用的目标类。 可能为null或甚至可能不实现该方法。
	 * 
	 * @return the specific target method, or the original method if the
	 * {@code targetClass} doesn't implement it or is {@code null}
	 * 
	 * <p> 特定目标方法，或者如果targetClass没有实现它或者为null，则为原始方法
	 * 
	 * @see org.springframework.util.ClassUtils#getMostSpecificMethod
	 */
	public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
		Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		// If we are dealing with method with generic parameters, find the original method.
		// 如果我们使用泛型参数处理方法，请找到原始方法。
		return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
	}


	/**
	 * Can the given pointcut apply at all on the given class?
	 * 
	 * <p> 给定的切入点是否适用于给定的类？
	 * 
	 * <p>This is an important test as it can be used to optimize
	 * out a pointcut for a class.
	 * 
	 * <p> 这是一个重要的测试，因为它可以用来优化类的切入点。
	 * 
	 * @param pc the static or dynamic pointcut to check
	 * 
	 * <p> 要检查的静态或动态切入点
	 * 
	 * @param targetClass the class to test - 要测试的类
	 * @return whether the pointcut can apply on any method
	 * 
	 * <p> 切入点是否适用于任何方法
	 * 
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass) {
		return canApply(pc, targetClass, false);
	}

	/**
	 * Can the given pointcut apply at all on the given class?
	 * 
	 * <p> 给定的切入点是否适用于给定的类？
	 * 
	 * <p>This is an important test as it can be used to optimize
	 * out a pointcut for a class.
	 * 
	 * <p> 这是一个重要的测试，因为它可以用来优化类的切入点。
	 * 
	 * @param pc the static or dynamic pointcut to check - 要检查的静态或动态切入点
	 * @param targetClass the class to test - 要测试的类
	 * @param hasIntroductions whether or not the advisor chain
	 * for this bean includes any introductions
	 * 
	 * <p> 此bean的advisor链是否包含任何introductions
	 * 
	 * @return whether the pointcut can apply on any method
	 * 
	 * <p> 切入点是否适用于任何方法
	 * 
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut must not be null");
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}

		MethodMatcher methodMatcher = pc.getMethodMatcher();
		IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
		if (methodMatcher instanceof IntroductionAwareMethodMatcher) {
			introductionAwareMethodMatcher = (IntroductionAwareMethodMatcher) methodMatcher;
		}

		Set<Class> classes = new LinkedHashSet<Class>(ClassUtils.getAllInterfacesForClassAsSet(targetClass));
		classes.add(targetClass);
		for (Class<?> clazz : classes) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if ((introductionAwareMethodMatcher != null &&
						introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions)) ||
						methodMatcher.matches(method, targetClass)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Can the given advisor apply at all on the given class?
	 * This is an important test as it can be used to optimize
	 * out a advisor for a class.
	 * 
	 * <p> 给定的advisor是否可以在给定的类应用？ 这是一项重要的测试，因为它可以用来优化class advisor。
	 * 
	 * @param advisor the advisor to check - advisor检查
	 * @param targetClass class we're testing - 我们正在测试的class
	 * @return whether the pointcut can apply on any method
	 * 
	 * <p> 切入点是否适用于任何方法
	 * 
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		return canApply(advisor, targetClass, false);
	}

	/**
	 * Can the given advisor apply at all on the given class?
	 * 
	 * <p> 给定的advisor是否可以在给定的class 应用？
	 * 
	 * <p>This is an important test as it can be used to optimize out a advisor for a class.
	 * This version also takes into account introductions (for IntroductionAwareMethodMatchers).
	 * 
	 * <p> 这是一项重要的测试，因为它可以用来优化class advisor。 此版本还考虑了介绍（适用于IntroductionAwareMethodMatchers）。
	 * 
	 * @param advisor the advisor to check - advisor检查
	 * @param targetClass class we're testing - 我们正在测试的class
	 * @param hasIntroductions whether or not the advisor chain for this bean includes
	 * any introductions
	 * 
	 * <p> 此bean的advisor链是否包含任何介绍
	 * 
	 * @return whether the pointcut can apply on any method
	 * 
	 * <p> 切入点是否适用于任何方法
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
		if (advisor instanceof IntroductionAdvisor) {
			return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor) {
			PointcutAdvisor pca = (PointcutAdvisor) advisor;
			return canApply(pca.getPointcut(), targetClass, hasIntroductions);
		}
		else {
			// It doesn't have a pointcut so we assume it applies.
			// 它没有切入点，所以我们认为它适用。
			return true;
		}
	}

	/**
	 * Determine the sublist of the {@code candidateAdvisors} list
	 * that is applicable to the given class.
	 * 
	 * <p> 确定适用于给定类的candidateAdvisors列表的子列表。
	 * 
	 * @param candidateAdvisors the Advisors to evaluate - Advisors进行评估
	 * @param clazz the target class - 目标类
	 * @return sublist of Advisors that can apply to an object of the given class
	 * (may be the incoming List as-is)
	 * 
	 * <p> 可以应用于给定类的对象的Advisors子列表（可以是原样的传入列表）
	 */
	public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
		if (candidateAdvisors.isEmpty()) {
			return candidateAdvisors;
		}
		List<Advisor> eligibleAdvisors = new LinkedList<Advisor>();
		//首先处理引介增强
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
				eligibleAdvisors.add(candidate);
			}
		}
		boolean hasIntroductions = !eligibleAdvisors.isEmpty();
		for (Advisor candidate : candidateAdvisors) {
			//引介增强已经处理
			if (candidate instanceof IntroductionAdvisor) {
				// already processed
				// 已处理完毕
				continue;
			}
			//对于普通bean的处理
			if (canApply(candidate, clazz, hasIntroductions)) {
				eligibleAdvisors.add(candidate);
			}
		}
		return eligibleAdvisors;
	}


	/**
	 * Invoke the given target via reflection, as part of an AOP method invocation.
	 * 
	 * <p> 作为AOP方法调用的一部分，通过反射调用给定目标。
	 * 
	 * @param target the target object - 目标对象
	 * @param method the method to invoke - 要调用的方法
	 * @param args the arguments for the method - 方法的参数
	 * @return the invocation result, if any
	 * 
	 * <p> 调用结果，如果有的话
	 * 
	 * @throws Throwable if thrown by the target method
	 * 
	 * <p> 如果被目标方法抛出
	 * 
	 * @throws org.springframework.aop.AopInvocationException in case of a reflection error
	 * 
	 * <p> 如果出现反射错误
	 * 
	 */
	public static Object invokeJoinpointUsingReflection(Object target, Method method, Object[] args)
			throws Throwable {

		// Use reflection to invoke the method.
		// 使用反射来调用该方法。
		try {
			ReflectionUtils.makeAccessible(method);
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception.
			// We must rethrow it. The client won't see the interceptor.
			
			// 调用的方法抛出了一个检查过的异常。 我们必须重新抛出它。 客户端不会看到拦截器。
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("AOP configuration seems to be invalid: tried calling method [" +
					method + "] on target [" + target + "]", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AopInvocationException("Could not access method [" + method + "]", ex);
		}
	}

}
