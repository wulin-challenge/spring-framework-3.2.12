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

package org.springframework.cache;

/**
 * Interface that defines the common cache operations.
 * 
 * <p> 定义通用缓存操作的接口。
 *
 * <p> <b>Note:</b> Due to the generic use of caching, it is recommended that
 * implementations allow storage of <tt>null</tt> values (for example to
 * cache methods that return {@code null}).
 * 
 * <p> 注意：由于一般使用缓存，建议实现允许存储空值（例如，缓存返回空值的方法）。
 *
 * @author Costin Leau
 * @since 3.1
 */
public interface Cache {

	/**
	 * Return the cache name.
	 */
	String getName();

	/**
	 * Return the the underlying native cache provider.
	 * 
	 * <p> 返回基础本机缓存提供程序。
	 */
	Object getNativeCache();

	/**
	 * Return the value to which this cache maps the specified key.
	 * 
	 * <p> 返回此缓存将指定键映射到的值。
	 * 
	 * <p>Returns {@code null} if the cache contains no mapping for this key;
	 * otherwise, the cached value (which may be {@code null} itself) will
	 * be returned in a {@link ValueWrapper}.
	 * 
	 * <p> 如果缓存不包含此键的映射，则返回null；否则返回null。 否则，缓存的值（本身可能为null）将在ValueWrapper中返回。
	 * 
	 * @param key the key whose associated value is to be returned
	 * 
	 * <p> 要返回其关联值的密钥
	 * 
	 * @return the value to which this cache maps the specified key,
	 * contained within a {@link ValueWrapper} which may also hold
	 * a cached {@code null} value. A straight {@code null} being
	 * returned means that the cache contains no mapping for this key.
	 * 
	 * <p> 此缓存将指定键映射到的值，该值包含在ValueWrapper中，ValueWrapper也可能包含缓存的null值。 
	 * 直接返回null表示高速缓存不包含此键的映射。
	 */
	ValueWrapper get(Object key);

	/**
	 * Associate the specified value with the specified key in this cache.
	 * 
	 * <p> 将指定的值与此高速缓存中的指定键相关联。
	 * 
	 * <p>If the cache previously contained a mapping for this key, the old
	 * value is replaced by the specified value.
	 * 
	 * <p> 如果高速缓存先前包含此键的映射，则旧值将替换为指定值。
	 * 
	 * @param key the key with which the specified value is to be associated
	 * 
	 * <p> 与指定值关联的键
	 * 
	 * @param value the value to be associated with the specified key
	 * 
	 * <p> 与指定键关联的值
	 */
	void put(Object key, Object value);

	/**
	 * Evict the mapping for this key from this cache if it is present.
	 * 
	 * <p> 如果存在，请从此缓存中退出此键的映射。
	 * 
	 * @param key the key whose mapping is to be removed from the cache
	 * 
	 * <p> 要从缓存中删除其映射的键
	 */
	void evict(Object key);

	/**
	 * Remove all mappings from the cache.
	 * 
	 * <p> 从缓存中删除所有映射。
	 */
	void clear();


	/**
	 * A (wrapper) object representing a cache value.
	 * 
	 * <p> 代表缓存值的（包装）对象。
	 */
	interface ValueWrapper {

		/**
		 * Return the actual value in the cache.
		 * 
		 * <p> 返回缓存中的实际值。
		 */
		Object get();
	}

}
