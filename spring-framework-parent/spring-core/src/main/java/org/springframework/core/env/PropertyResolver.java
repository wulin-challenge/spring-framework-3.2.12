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

package org.springframework.core.env;

/**
 * Interface for resolving properties against any underlying source.
 * 
 * <p> 用于解析任何基础源的属性的接口。
 *
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see PropertySourcesPropertyResolver
 */
public interface PropertyResolver {

	/**
	 * Return whether the given property key is available for resolution, i.e.,
	 * the value for the given key is not {@code null}.
	 * 
	 * <p> 返回给定属性键是否可用于解析，即给定键的值不为空。
	 */
	boolean containsProperty(String key);

	/**
	 * Return the property value associated with the given key, or {@code null}
	 * if the key cannot be resolved.
	 * 
	 * <p> 返回与给定键关联的属性值，如果无法解析该键，则返回null。
	 * 
	 * @param key the property name to resolve - 要解析的属性名称
	 * @see #getProperty(String, String)
	 * @see #getProperty(String, Class)
	 * @see #getRequiredProperty(String)
	 */
	String getProperty(String key);

	/**
	 * Return the property value associated with the given key, or
	 * {@code defaultValue} if the key cannot be resolved.
	 * 
	 * <p> 返回与给定键关联的属性值，如果无法解析键，则返回defaultValue。
	 * 
	 * @param key the property name to resolve - 要解析的属性名称
	 * @param defaultValue the default value to return if no value is found
	 * 
	 * <p> 如果未找到任何值，则返回默认值
	 * 
	 * @see #getRequiredProperty(String)
	 * @see #getProperty(String, Class)
	 */
	String getProperty(String key, String defaultValue);

	/**
	 * Return the property value associated with the given key, or {@code null}
	 * if the key cannot be resolved.
	 * 
	 * <p> 返回与给定键关联的属性值，如果无法解析该键，则返回null。
	 * 
	 * @param key the property name to resolve - 要解析的属性名称
	 * @param targetType the expected type of the property value
	 * 
	 * <p> 预期的属性值类型
	 * 
	 * @see #getRequiredProperty(String, Class)
	 */
	<T> T getProperty(String key, Class<T> targetType);

	/**
	 * Return the property value associated with the given key, or
	 * {@code defaultValue} if the key cannot be resolved.
	 * 
	 * <p> 返回与给定键关联的属性值，如果无法解析键，则返回defaultValue。
	 * 
	 * @param key the property name to resolve - 要解析的属性名称
	 * @param targetType the expected type of the property value
	 * 
	 * <p> 预期的属性值类型
	 * 
	 * @param defaultValue the default value to return if no value is found
	 * 
	 * <p> 如果未找到任何值，则返回默认值
	 * 
	 * @see #getRequiredProperty(String, Class)
	 */
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);

	/**
	 * Convert the property value associated with the given key to a {@code Class}
	 * of type {@code T} or {@code null} if the key cannot be resolved.
	 * 
	 * <p> 将与给定键关联的属性值转换为类型为T的Class，如果无法解析该键，则将其转换为null。
	 * 
	 * @throws org.springframework.core.convert.ConversionException if class specified
	 * by property value cannot be found  or loaded or if targetType is not assignable
	 * from class specified by property value
	 * 
	 * <p> 如果无法找到或加载由属性值指定的类，或者无法从属性值指定的类分配targetType
	 * 
	 * @see #getProperty(String, Class)
	 */
	<T> Class<T> getPropertyAsClass(String key, Class<T> targetType);

	/**
	 * Return the property value associated with the given key, converted to the given
	 * targetType (never {@code null}).
	 * 
	 * <p> 返回与给定键关联的属性值，转换为给定的targetType（从不为null）。
	 * 
	 * @throws IllegalStateException if the key cannot be resolved
	 * 
	 * <p> 如果密钥无法解决
	 * 
	 * @see #getRequiredProperty(String, Class)
	 */
	String getRequiredProperty(String key) throws IllegalStateException;

	/**
	 * Return the property value associated with the given key, converted to the given
	 * targetType (never {@code null}).
	 * 
	 * <p> 返回与给定键关联的属性值，转换为给定的targetType（从不为null）。
	 * 
	 * @throws IllegalStateException if the given key cannot be resolved - 如果给定的密钥无法解决
	 */
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

	/**
	 * Resolve ${...} placeholders in the given text, replacing them with corresponding
	 * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
	 * no default value are ignored and passed through unchanged.
	 * 
	 * <p> 解析给定文本中的$ {...}占位符，将其替换为getProperty解析的相应属性值。 没有默认值的无法解决的占位符将被忽略并传递不变。
	 * 
	 * @param text the String to resolve - 要解析的字符串
	 * @return the resolved String (never {@code null})
	 * 
	 * <p> 已解析的String（永不为null）
	 * 
	 * @throws IllegalArgumentException if given text is {@code null}
	 * 
	 * <p> 如果给定的文本为null
	 * 
	 * @see #resolveRequiredPlaceholders
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String)
	 */
	String resolvePlaceholders(String text);

	/**
	 * Resolve ${...} placeholders in the given text, replacing them with corresponding
	 * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
	 * no default value will cause an IllegalArgumentException to be thrown.
	 * 
	 * <p> 解析给定文本中的$ {...}占位符，将其替换为getProperty解析的相应属性值。 
	 * 没有默认值的无法解析的占位符将导致抛出IllegalArgumentException。
	 * 
	 * @return the resolved String (never {@code null})
	 * 
	 * <p> 已解析的String（永不为null）
	 * 
	 * @throws IllegalArgumentException if given text is {@code null}
	 * or if any placeholders are unresolvable
	 * 
	 * <p> 如果给定的文本为null或者任何占位符不可解析
	 * 
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String, boolean)
	 */
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
