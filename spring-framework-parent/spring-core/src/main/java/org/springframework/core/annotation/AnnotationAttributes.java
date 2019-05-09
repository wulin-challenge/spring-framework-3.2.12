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

package org.springframework.core.annotation;

import static java.lang.String.format;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link LinkedHashMap} subclass representing annotation attribute key/value pairs
 * as read by Spring's reflection- or ASM-based {@link
 * org.springframework.core.type.AnnotationMetadata AnnotationMetadata} implementations.
 * Provides 'pseudo-reification' to avoid noisy Map generics in the calling code as well
 * as convenience methods for looking up annotation attributes in a type-safe fashion.
 * 
 * <p> LinkedHashMap子类，表示由Spring的基于反射或基于ASM的AnnotationMetadata实现读取的注释属性键/值对。 
 * 提供“伪设置”以避免调用代码中的噪声Map泛型以及以类型安全的方式查找注释属性的便捷方法。
 *
 * @author Chris Beams
 * @since 3.1.1
 */
@SuppressWarnings("serial")
public class AnnotationAttributes extends LinkedHashMap<String, Object> {

	/**
	 * Create a new, empty {@link AnnotationAttributes} instance.
	 * 
	 * <p> 创建一个新的空AnnotationAttributes实例。
	 */
	public AnnotationAttributes() {
	}

	/**
	 * Create a new, empty {@link AnnotationAttributes} instance with the given initial
	 * capacity to optimize performance.
	 * 
	 * <p> 使用给定的初始容量创建一个新的空AnnotationAttributes实例以优化性能。
	 * 
	 * @param initialCapacity initial size of the underlying map 
	 * 
	 * <p> 底层map的初始大小
	 */
	public AnnotationAttributes(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Create a new {@link AnnotationAttributes} instance, wrapping the provided map
	 * and all its key/value pairs.
	 * 
	 * <p> 创建一个新的AnnotationAttributes实例，包装提供的映射及其所有键/值对。
	 * 
	 * @param map original source of annotation attribute key/value pairs to wrap
	 * 
	 * <p> 要包装的注释属性键/值对的原始源
	 * 
	 * @see #fromMap(Map)
	 */
	public AnnotationAttributes(Map<String, Object> map) {
		super(map);
	}

	/**
	 * Return an {@link AnnotationAttributes} instance based on the given map; if the map
	 * is already an {@code AnnotationAttributes} instance, it is casted and returned
	 * immediately without creating any new instance; otherwise create a new instance by
	 * wrapping the map with the {@link #AnnotationAttributes(Map)} constructor.
	 * 
	 * <p> 返回基于给定映射的AnnotationAttributes实例; 如果映射已经是AnnotationAttributes实例，则会立即转换并返回它，
	 * 而不创建任何新实例; 否则通过使用AnnotationAttributes（Map）构造函数包装map来创建新实例。
	 * 
	 * @param map original source of annotation attribute key/value pairs
	 * 
	 * <p> 注释属性键/值对的原始来源
	 */
	public static AnnotationAttributes fromMap(Map<String, Object> map) {
		if (map == null) {
			return null;
		}

		if (map instanceof AnnotationAttributes) {
			return (AnnotationAttributes) map;
		}

		return new AnnotationAttributes(map);
	}

	public String getString(String attributeName) {
		return doGet(attributeName, String.class);
	}

	public String[] getStringArray(String attributeName) {
		return doGet(attributeName, String[].class);
	}

	public boolean getBoolean(String attributeName) {
		return doGet(attributeName, Boolean.class);
	}

	@SuppressWarnings("unchecked")
	public <N extends Number> N getNumber(String attributeName) {
		return (N) doGet(attributeName, Integer.class);
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<?>> E getEnum(String attributeName) {
		return (E) doGet(attributeName, Enum.class);
	}

	@SuppressWarnings("unchecked")
	public <T> Class<? extends T> getClass(String attributeName) {
		return doGet(attributeName, Class.class);
	}

	public Class<?>[] getClassArray(String attributeName) {
		return doGet(attributeName, Class[].class);
	}

	public AnnotationAttributes getAnnotation(String attributeName) {
		return doGet(attributeName, AnnotationAttributes.class);
	}

	public AnnotationAttributes[] getAnnotationArray(String attributeName) {
		return doGet(attributeName, AnnotationAttributes[].class);
	}

	@SuppressWarnings("unchecked")
	private <T> T doGet(String attributeName, Class<T> expectedType) {
		Assert.hasText(attributeName, "attributeName must not be null or empty");
		Object value = this.get(attributeName);
		Assert.notNull(value, format("Attribute '%s' not found", attributeName));
		Assert.isAssignable(expectedType, value.getClass(),
				format("Attribute '%s' is of type [%s], but [%s] was expected. Cause: ",
						attributeName, value.getClass().getSimpleName(), expectedType.getSimpleName()));
		return (T) value;
	}

	public String toString() {
		Iterator<Map.Entry<String, Object>> entries = entrySet().iterator();
		StringBuilder sb = new StringBuilder("{");
		while (entries.hasNext()) {
			Map.Entry<String, Object> entry = entries.next();
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(valueToString(entry.getValue()));
			sb.append(entries.hasNext() ? ", " : "");
		}
		sb.append("}");
		return sb.toString();
	}

	private String valueToString(Object value) {
		if (value == this) {
			return "(this Map)";
		}
		if (value instanceof Object[]) {
			return "[" + StringUtils.arrayToCommaDelimitedString((Object[]) value) + "]";
		}
		return String.valueOf(value);
	}
}
