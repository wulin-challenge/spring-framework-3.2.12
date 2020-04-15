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
import java.util.Collection;

/**
 * Interface used by {@link CacheInterceptor}. Implementations know how to source
 * cache operation attributes, whether from configuration, metadata attributes at
 * source level, or elsewhere.
 * 
 * <p> CacheInterceptor使用的接口。 实现知道如何从配置级别，源级别或其他地方的元数据属性中获取缓存操作属性。
 *
 * @author Costin Leau
 * @since 3.1
 */
public interface CacheOperationSource {

	/**
	 * Return the collection of cache operations for this method, or {@code null}
	 * if the method contains no <em>cacheable</em> annotations.
	 * 
	 * <p> 返回此方法的缓存操作的集合；如果该方法不包含可缓存的注释，则返回null。
	 * 
	 * @param method the method to introspect - 内省的方法
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the declaring class of the method must be used)
	 * 
	 * <p> 目标类（可以为null，在这种情况下，必须使用方法的声明类）
	 * @return all cache operations for this method, or {@code null} if none found
	 * 
	 * <p> 此方法的所有缓存操作；如果找不到，则为null
	 * 
	 */
	Collection<CacheOperation> getCacheOperations(Method method, Class<?> targetClass);

}
