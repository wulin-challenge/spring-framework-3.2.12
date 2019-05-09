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

package org.springframework.core.type.classreading;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Caching implementation of the {@link MetadataReaderFactory} interface,
 * caching {@link MetadataReader} per Spring {@link Resource} handle
 * (i.e. per ".class" file).
 * 
 * <p> 缓存MetadataReaderFactory接口的实现，每个Spring Resource句柄缓存MetadataReader（即每个“.class”文件）。
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.5
 */
public class CachingMetadataReaderFactory extends SimpleMetadataReaderFactory {

	/** Default maximum number of entries for the MetadataReader cache: 256 */
	/** MetadataReader缓存的默认最大条目数：256 */
	public static final int DEFAULT_CACHE_LIMIT = 256;


	private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

	@SuppressWarnings("serial")
	private final Map<Resource, MetadataReader> metadataReaderCache =
			new LinkedHashMap<Resource, MetadataReader>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Resource, MetadataReader> eldest) {
					return size() > getCacheLimit();
				}
			};


	/**
	 * Create a new CachingMetadataReaderFactory for the default class loader.
	 * 
	 * <p> 为默认的类加载器创建一个新的CachingMetadataReaderFactory。
	 */
	public CachingMetadataReaderFactory() {
		super();
	}

	/**
	 * Create a new CachingMetadataReaderFactory for the given resource loader.
	 * 
	 * <p> 为给定的资源加载器创建一个新的CachingMetadataReaderFactory。
	 * 
	 * @param resourceLoader the Spring ResourceLoader to use
	 * (also determines the ClassLoader to use)
	 * 
	 * <p> 要使用的Spring ResourceLoader（也确定要使用的ClassLoader）
	 */
	public CachingMetadataReaderFactory(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	/**
	 * Create a new CachingMetadataReaderFactory for the given class loader.
	 * 
	 * <p> 为给定的类加载器创建一个新的CachingMetadataReaderFactory。
	 * 
	 * @param classLoader the ClassLoader to use - 要使用的ClassLoader
	 */
	public CachingMetadataReaderFactory(ClassLoader classLoader) {
		super(classLoader);
	}


	/**
	 * Specify the maximum number of entries for the MetadataReader cache.
	 * Default is 256.
	 * 
	 * <p> 指定MetadataReader缓存的最大条目数。 默认值为256。
	 * 
	 */
	public void setCacheLimit(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}

	/**
	 * Return the maximum number of entries for the MetadataReader cache.
	 * 
	 * <p> 返回MetadataReader缓存的最大条目数。
	 * 
	 */
	public int getCacheLimit() {
		return this.cacheLimit;
	}


	@Override
	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		if (getCacheLimit() <= 0) {
			return super.getMetadataReader(resource);
		}
		synchronized (this.metadataReaderCache) {
			MetadataReader metadataReader = this.metadataReaderCache.get(resource);
			if (metadataReader == null) {
				metadataReader = super.getMetadataReader(resource);
				this.metadataReaderCache.put(resource, metadataReader);
			}
			return metadataReader;
		}
	}

	/**
	 * Clear the entire MetadataReader cache, removing all cached class metadata.
	 * 
	 * <p> 清除整个MetadataReader缓存，删除所有缓存的类元数据。
	 */
	public void clearCache() {
		synchronized (this.metadataReaderCache) {
			this.metadataReaderCache.clear();
		}
	}

}
