/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of {@link TransactionAttributeSource} that caches
 * attributes for methods and implements a fallback policy: 1. specific target
 * method; 2. target class; 3. declaring method; 4. declaring class/interface.
 * 
 * <p> TransactionAttributeSource的抽象实现，它缓存方法的属性并实现回退策略：1。特定的目标方法; 
 * 2.目标类; 3.声明方法; 4.声明类/接口。
 *
 * <p>Defaults to using the target class's transaction attribute if none is
 * associated with the target method. Any transaction attribute associated with
 * the target method completely overrides a class transaction attribute.
 * If none found on the target class, the interface that the invoked method
 * has been called through (in case of a JDK proxy) will be checked.
 * 
 * <p> 如果没有与目标方法关联，则默认使用目标类的事务属性。 与目标方法关联的任何事务属性都会完全覆盖类事务属性。 
 * 如果在目标类上找不到，则将检查已调用调用方法的接口（如果是JDK代理）。
 *
 * <p>This implementation caches attributes by method after they are first used.
 * If it is ever desirable to allow dynamic changing of transaction attributes
 * (which is very unlikely), caching could be made configurable. Caching is
 * desirable because of the cost of evaluating rollback rules.
 * 
 * <p> 此实现在首次使用后按方法缓存属性。 如果希望允许动态更改事务属性（这是非常不可能的），
 * 则可以使高速缓存可配置。 由于评估回滚规则的成本，缓存是可取的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {

	/**
	 * Canonical value held in cache to indicate no transaction attribute was
	 * found for this method, and we don't need to look again.
	 * 
	 * <p> 缓存中保存的规范值表示没有找到此方法的事务属性，我们不需要再查看。
	 */
	private final static TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute();


	/**
	 * Logger available to subclasses.
	 * 
	 * <p> 记录器可用于子类。
	 * 
	 * <p>As this base class is not marked Serializable, the logger will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 * 
	 * <p> 由于此基类未标记为Serializable，因此在序列化后将重新创建记录器 - 前提是具体子类为Serializable。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Cache of TransactionAttributes, keyed by DefaultCacheKey (Method + target Class).
	 * 
	 * <p> TransactionAttributes的缓存，由DefaultCacheKey（Method + target Class）键入。
	 * 
	 * <p>As this base class is not marked Serializable, the cache will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 * 
	 * <p> 由于此基类未标记为Serializable，因此在序列化后将重新创建缓存 - 前提是具体子类为Serializable。
	 */
	final Map<Object, TransactionAttribute> attributeCache = new ConcurrentHashMap<Object, TransactionAttribute>(1024);


	/**
	 * Determine the transaction attribute for this method invocation.
	 * 
	 * <p> 确定此方法调用的事务属性。
	 * 
	 * <p>Defaults to the class's transaction attribute if no method attribute is found.
	 * 
	 * <p> 如果未找到方法属性，则默认为类的事务属性。
	 * 
	 * @param method the method for the current invocation (never {@code null})
	 * 
	 * <p> 当前调用的方法（永不为null）
	 * 
	 * @param targetClass the target class for this invocation (may be {@code null})
	 * 
	 * <p> 此调用的目标类（可以为null）
	 * 
	 * @return TransactionAttribute for this method, or {@code null} if the method
	 * is not transactional
	 * 
	 * <p> 此方法的TransactionAttribute，如果方法不是事务性的，则返回null
	 * 
	 */
	public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
		// First, see if we have a cached value.
		// 首先，看看我们是否有缓存值。
		Object cacheKey = getCacheKey(method, targetClass);
		Object cached = this.attributeCache.get(cacheKey);
		if (cached != null) {
			// Value will either be canonical value indicating there is no transaction attribute,
			// or an actual transaction attribute.
			
			// 值将是指示没有事务属性的规范值或实际事务属性。
			if (cached == NULL_TRANSACTION_ATTRIBUTE) {
				return null;
			}
			else {
				return (TransactionAttribute) cached;
			}
		}
		else {
			// We need to work it out.
			// 我们需要解决这个问题。
			TransactionAttribute txAtt = computeTransactionAttribute(method, targetClass);
			// Put it in the cache.
			// 把它放在缓存中。
			if (txAtt == null) {
				this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
			}
			else {
				if (logger.isDebugEnabled()) {
					Class<?> classToLog = (targetClass != null ? targetClass : method.getDeclaringClass());
					logger.debug("Adding transactional method '" + classToLog.getSimpleName() + "." +
							method.getName() + "' with attribute: " + txAtt);
				}
				this.attributeCache.put(cacheKey, txAtt);
			}
			return txAtt;
		}
	}

	/**
	 * Determine a cache key for the given method and target class.
	 * 
	 * <p> 确定给定方法和目标类的缓存键。
	 * 
	 * <p>Must not produce same key for overloaded methods.
	 * Must produce same key for different instances of the same method.
	 * 
	 * <p> 不得为重载方法生成相同的密钥。 必须为同一方法的不同实例生成相同的密钥。
	 * 
	 * @param method the method (never {@code null})
	 * 
	 * <p> 方法（永不为null）
	 * 
	 * @param targetClass the target class (may be {@code null})
	 * 
	 * <p> 目标类（可以为null）
	 * 
	 * @return the cache key (never {@code null})
	 * 
	 * <p> 缓存键（永不为null）
	 */
	protected Object getCacheKey(Method method, Class<?> targetClass) {
		return new DefaultCacheKey(method, targetClass);
	}

	/**
	 * Same signature as {@link #getTransactionAttribute}, but doesn't cache the result.
	 * {@link #getTransactionAttribute} is effectively a caching decorator for this method.
	 * 
	 * <p> 与getTransactionAttribute相同的签名，但不缓存结果。 
	 * getTransactionAttribute实际上是此方法的缓存装饰器。
	 * 
	 * @see #getTransactionAttribute
	 */
	private TransactionAttribute computeTransactionAttribute(Method method, Class<?> targetClass) {
		// Don't allow no-public methods as required.
		// 不要求根据需要使用非公共方法。
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}

		// Ignore CGLIB subclasses - introspect the actual user class.
		// 忽略CGLIB子类 - 内省实际的用户类。
		Class<?> userClass = ClassUtils.getUserClass(targetClass);
		// The method may be on an interface, but we need attributes from the target class.
		// If the target class is null, the method will be unchanged.
		// 该方法可以在接口上，但我们需要来自目标类的属性。 如果目标类为null，则方法将保持不变。
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);
		// If we are dealing with method with generic parameters, find the original method.
		// 如果我们使用泛型参数处理方法，请找到原始方法。
		specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

		// First try is the method in the target class.
		// 首先尝试的是目标类中的方法。
		TransactionAttribute txAtt = findTransactionAttribute(specificMethod);
		if (txAtt != null) {
			return txAtt;
		}

		// Second try is the transaction attribute on the target class.
		// 第二次尝试是目标类的事务属性。
		txAtt = findTransactionAttribute(specificMethod.getDeclaringClass());
		if (txAtt != null) {
			return txAtt;
		}

		if (specificMethod != method) {
			// Fallback is to look at the original method.
			// 后退是看原始方法。
			txAtt = findTransactionAttribute(method);
			if (txAtt != null) {
				return txAtt;
			}
			// Last fallback is the class of the original method.
			// 最后一个回退是原始方法的类。
			return findTransactionAttribute(method.getDeclaringClass());
		}
		return null;
	}


	/**
	 * Subclasses need to implement this to return the transaction attribute
	 * for the given method, if any.
	 * 
	 * <p> 子类需要实现此操作以返回给定方法的事务属性（如果有）。
	 * 
	 * @param method the method to retrieve the attribute for
	 * 
	 * <p> 检索属性的方法
	 * 
	 * @return all transaction attribute associated with this method
	 * (or {@code null} if none)
	 * 
	 * <p> 与此方法关联的所有事务属性（如果没有则为null）
	 */
	protected abstract TransactionAttribute findTransactionAttribute(Method method);

	/**
	 * Subclasses need to implement this to return the transaction attribute
	 * for the given class, if any.
	 * 
	 * <p> 子类需要实现它来返回给定类的事务属性（如果有）。
	 * 
	 * @param clazz the class to retrieve the attribute for
	 * 
	 * <p> 要检索属性的类
	 * 
	 * @return all transaction attribute associated with this class
	 * (or {@code null} if none)
	 * 
	 * <p> 与此类关联的所有事务属性（如果没有则为null）
	 * 
	 */
	protected abstract TransactionAttribute findTransactionAttribute(Class<?> clazz);


	/**
	 * Should only public methods be allowed to have transactional semantics?
	 * 
	 * <p> 是否只允许公共方法具有事务语义？
	 * 
	 * <p>The default implementation returns {@code false}.
	 * 
	 * <p> 默认实现返回false。
	 * 
	 */
	protected boolean allowPublicMethodsOnly() {
		return false;
	}


	/**
	 * Default cache key for the TransactionAttribute cache.
	 * 
	 * <p> TransactionAttribute缓存的默认缓存键。
	 * 
	 */
	private static class DefaultCacheKey {

		private final Method method;

		private final Class<?> targetClass;

		public DefaultCacheKey(Method method, Class<?> targetClass) {
			this.method = method;
			this.targetClass = targetClass;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof DefaultCacheKey)) {
				return false;
			}
			DefaultCacheKey otherKey = (DefaultCacheKey) other;
			return (this.method.equals(otherKey.method) &&
					ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass));
		}

		@Override
		public int hashCode() {
			return this.method.hashCode();
		}
	}

}
