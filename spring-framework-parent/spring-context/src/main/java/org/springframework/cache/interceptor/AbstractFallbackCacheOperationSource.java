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

package org.springframework.cache.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of {@link CacheOperation} that caches
 * attributes for methods and implements a fallback policy: 1. specific
 * target method; 2. target class; 3. declaring method; 4. declaring
 * class/interface.
 * 
 * <p> CacheOperation的抽象实现，该实现可缓存方法的属性并实现回退策略：
 * 1.特定的目标方法； 2.目标阶层； 3.申报方法； 4.声明类/接口。
 *
 * <p>Defaults to using the target class's caching attribute if none is
 * associated with the target method. Any caching attribute associated
 * with the target method completely overrides a class caching attribute.
 * If none found on the target class, the interface that the invoked
 * method has been called through (in case of a JDK proxy) will be
 * checked.
 * 
 * <p> 如果目标方法没有与之关联的默认值，则使用目标类的缓存属性。 与目标方法关联的任何缓存属性都将完全覆盖类缓存属性。 
 * 如果在目标类上未找到任何接口，则将检查已调用调用方法的接口（对于JDK代理而言）。
 *
 * <p>This implementation caches attributes by method after they are
 * first used. If it is ever desirable to allow dynamic changing of
 * cacheable attributes (which is very unlikely), caching could be made
 * configurable.
 * 
 * <p> 此实现在首次使用属性后按方法缓存属性。 如果曾经希望允许动态更改可缓存属性（这是不太可能的），则可以使缓存可配置。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractFallbackCacheOperationSource implements CacheOperationSource {

	/**
	 * Canonical value held in cache to indicate no caching attribute was
	 * found for this method and we don't need to look again.
	 * 
	 * <p> 保留在缓存中的规范值指示未找到此方法的缓存属性，因此我们无需再次查看。
	 */
	private final static Collection<CacheOperation> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

	/**
	 * Logger available to subclasses.
	 * <p>As this base class is not marked Serializable, the logger will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Cache of CacheOperations, keyed by DefaultCacheKey (Method + target Class).
	 * 
	 * <p> CacheOperations的缓存，由DefaultCacheKey（方法+目标类）键入。
	 * 
	 * <p>As this base class is not marked Serializable, the cache will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 * 
	 * <p> 由于此基类未标记为Serializable，因此在具体的子类为Serializable的情况下，将在序列化后重新创建缓存。
	 */
	final Map<Object, Collection<CacheOperation>> attributeCache =
			new ConcurrentHashMap<Object, Collection<CacheOperation>>(1024);


	/**
	 * Determine the caching attribute for this method invocation.
	 * 
	 * <p> 确定此方法调用的缓存属性。
	 * 
	 * <p>Defaults to the class's caching attribute if no method attribute is found.
	 * 
	 * <p> 如果未找到方法属性，则默认为类的缓存属性。
	 * 
	 * @param method the method for the current invocation (never {@code null})
	 * 
	 * <p> 当前调用的方法（绝不为null）
	 * 
	 * @param targetClass the target class for this invocation (may be {@code null})
	 * 
	 * <p> 此调用的目标类（可以为null）
	 * 
	 * @return {@link CacheOperation} for this method, or {@code null} if the method
	 * is not cacheable
	 * 
	 * <p> 此方法的CacheOperation；如果该方法不可缓存，则为null
	 */
	public Collection<CacheOperation> getCacheOperations(Method method, Class<?> targetClass) {
		// First, see if we have a cached value.
		// 首先，看看我们是否有一个缓存的值。
		Object cacheKey = getCacheKey(method, targetClass);
		Collection<CacheOperation> cached = this.attributeCache.get(cacheKey);
		if (cached != null) {
			if (cached == NULL_CACHING_ATTRIBUTE) {
				return null;
			}
			// Value will either be canonical value indicating there is no caching attribute,
			// or an actual caching attribute.
			
			// 值可以是表示没有缓存属性的规范值，也可以是实际的缓存属性。
			return cached;
		}
		else {
			// We need to work it out.
			// 我们需要解决。
			Collection<CacheOperation> cacheOps = computeCacheOperations(method, targetClass);
			// Put it in the cache.
			// 将其放入缓存中。
			if (cacheOps == null) {
				this.attributeCache.put(cacheKey, NULL_CACHING_ATTRIBUTE);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Adding cacheable method '" + method.getName() + "' with attribute: " + cacheOps);
				}
				this.attributeCache.put(cacheKey, cacheOps);
			}
			return cacheOps;
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
	 * <p> 对于重载的方法，不能产生相同的密钥。 必须为同一方法的不同实例产生相同的密钥。
	 * 
	 * @param method the method (never {@code null})
	 * @param targetClass the target class (may be {@code null})
	 * @return the cache key (never {@code null})
	 */
	protected Object getCacheKey(Method method, Class<?> targetClass) {
		return new DefaultCacheKey(method, targetClass);
	}

	private Collection<CacheOperation> computeCacheOperations(Method method, Class<?> targetClass) {
		// Don't allow no-public methods as required.
		// 禁止根据需要使用非公开方法。
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}

		// The method may be on an interface, but we need attributes from the target class.
		// If the target class is null, the method will be unchanged.
		
		// 该方法可能在接口上，但是我们需要目标类的属性。 如果目标类为null，则该方法将保持不变。
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		// If we are dealing with method with generic parameters, find the original method.
		
		// 如果我们要处理具有泛型参数的方法，请找到原始方法。
		specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

		// First try is the method in the target class.
		// 首先尝试的是目标类中的方法。
		Collection<CacheOperation> opDef = findCacheOperations(specificMethod);
		if (opDef != null) {
			return opDef;
		}

		// Second try is the caching operation on the target class.
		// 第二次尝试是对目标类进行缓存操作。
		opDef = findCacheOperations(specificMethod.getDeclaringClass());
		if (opDef != null) {
			return opDef;
		}

		if (specificMethod != method) {
			// Fall back is to look at the original method.
			// 后退是看原始方法。
			opDef = findCacheOperations(method);
			if (opDef != null) {
				return opDef;
			}
			// Last fall back is the class of the original method.
			// 最后回退的是原始方法的类。
			return findCacheOperations(method.getDeclaringClass());
		}
		return null;
	}


	/**
	 * Subclasses need to implement this to return the caching attribute
	 * for the given method, if any.
	 * 
	 * <p> 子类需要实现此功能，以返回给定方法的缓存属性（如果有）。
	 * 
	 * @param method the method to retrieve the attribute for
	 * 
	 * <p> 检索属性的方法
	 * 
	 * @return all caching attribute associated with this method
	 * (or {@code null} if none)
	 * 
	 * <p> 与该方法关联的所有缓存属性（如果没有，则为null）
	 */
	protected abstract Collection<CacheOperation> findCacheOperations(Method method);

	/**
	 * Subclasses need to implement this to return the caching attribute
	 * for the given class, if any.
	 * 
	 * <p> 子类需要实现此功能，以返回给定类的缓存属性（如果有）。
	 * 
	 * @param clazz the class to retrieve the attribute for
	 * 
	 * <p> 检索属性的类
	 * 
	 * @return all caching attribute associated with this class
	 * (or {@code null} if none)
	 * 
	 * <p> 与该类关联的所有缓存属性（如果没有，则为null）
	 */
	protected abstract Collection<CacheOperation> findCacheOperations(Class<?> clazz);

	/**
	 * Should only public methods be allowed to have caching semantics?
	 * 
	 * <p> 应该只允许公共方法具有缓存语义吗？
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
	 * Default cache key for the CacheOperation cache.
	 * 
	 * <p> CacheOperation缓存的默认缓存键。
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
			return (this.method.equals(otherKey.method) && ObjectUtils.nullSafeEquals(this.targetClass,
					otherKey.targetClass));
		}

		@Override
		public int hashCode() {
			return this.method.hashCode() * 29 + (this.targetClass != null ? this.targetClass.hashCode() : 0);
		}
	}
}
