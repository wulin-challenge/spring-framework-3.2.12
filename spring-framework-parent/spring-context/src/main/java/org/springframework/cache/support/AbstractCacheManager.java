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

package org.springframework.cache.support;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Abstract base class implementing the common {@link CacheManager} methods.
 * Useful for 'static' environments where the backing caches do not change.
 * 
 * <p> 实现公共CacheManager方法的抽象基类。 对于备用缓存不变的“静态”环境很有用。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractCacheManager implements CacheManager, InitializingBean {

	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);

	private Set<String> cacheNames = new LinkedHashSet<String>(16);


	public void afterPropertiesSet() {
		Collection<? extends Cache> caches = loadCaches();

		// Preserve the initial order of the cache names
		// 保留缓存名称的初始顺序
		this.cacheMap.clear();
		this.cacheNames.clear();
		for (Cache cache : caches) {
			addCache(cache);
		}
	}

	protected final void addCache(Cache cache) {
		this.cacheMap.put(cache.getName(), decorateCache(cache));
		this.cacheNames.add(cache.getName());
	}

	/**
	 * Decorate the given Cache object if necessary.
	 * 
	 * <p> 如有必要，装饰给定的Cache对象。
	 * 
	 * @param cache the Cache object to be added to this CacheManager
	 * 
	 * <p> 要添加到此CacheManager的Cache对象
	 * 
	 * @return the decorated Cache object to be used instead,
	 * or simply the passed-in Cache object by default
	 * 
	 * <p> 装饰的Cache对象代替使用，或者默认情况下只是传入的Cache对象
	 * 
	 */
	protected Cache decorateCache(Cache cache) {
		return cache;
	}


	public Cache getCache(String name) {
		return this.cacheMap.get(name);
	}

	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheNames);
	}


	/**
	 * Load the initial caches for this cache manager.
	 * <p>Called by {@link #afterPropertiesSet()} on startup.
	 * The returned collection may be empty but must not be {@code null}.
	 */
	protected abstract Collection<? extends Cache> loadCaches();

}
