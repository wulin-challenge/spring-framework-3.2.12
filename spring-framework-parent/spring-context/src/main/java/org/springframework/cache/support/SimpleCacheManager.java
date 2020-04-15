/*
 * Copyright 2002-2011 the original author or authors.
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

import org.springframework.cache.Cache;

/**
 * Simple cache manager working against a given collection of caches.
 * Useful for testing or simple caching declarations.
 * 
 * <p> 简单的缓存管理器针对给定的缓存集合进行工作。 对于测试或简单的缓存声明很有用。
 *
 * @author Costin Leau
 * @since 3.1
 */
public class SimpleCacheManager extends AbstractCacheManager {

	private Collection<? extends Cache> caches;

	/**
	 * Specify the collection of Cache instances to use for this CacheManager.
	 * 
	 * <p> 指定用于此CacheManager的Cache实例的集合。
	 */
	public void setCaches(Collection<? extends Cache> caches) {
		this.caches = caches;
	}

	@Override
	protected Collection<? extends Cache> loadCaches() {
		return this.caches;
	}

}
