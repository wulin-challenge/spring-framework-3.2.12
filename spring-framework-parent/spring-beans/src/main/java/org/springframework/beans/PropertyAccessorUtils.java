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

package org.springframework.beans;

/**
 * Utility methods for classes that perform bean property access
 * according to the {@link PropertyAccessor} interface.
 * 
 * <p> 根据PropertyAccessor接口执行bean属性访问的类的实用程序方法。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 */
public abstract class PropertyAccessorUtils {

	/**
	 * Return the actual property name for the given property path.
	 * 
	 * <p> 返回给定属性路径的实际属性名称。
	 * 
	 * @param propertyPath the property path to determine the property name
	 * for (can include property keys, for example for specifying a map entry)
	 * 
	 * <p> 用于确定属性名称的属性路径（可以包含属性键，例如用于指定映射条目）
	 * 
	 * @return the actual property name, without any key elements
	 * 
	 * <p> 实际的属性名称，没有任何关键元素
	 * 
	 */
	public static String getPropertyName(String propertyPath) {
		int separatorIndex = (propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX) ?
				propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) : -1);
		return (separatorIndex != -1 ? propertyPath.substring(0, separatorIndex) : propertyPath);
	}

	/**
	 * Check whether the given property path indicates an indexed or nested property.
	 * 
	 * <p> 检查给定的属性路径是否指示索引或嵌套属性。
	 * 
	 * @param propertyPath the property path to check - 要检查的属性路径
	 * @return whether the path indicates an indexed or nested property
	 * 
	 * <p> 路径是指示索引属性还是嵌套属性
	 */
	public static boolean isNestedOrIndexedProperty(String propertyPath) {
		if (propertyPath == null) {
			return false;
		}
		for (int i = 0; i < propertyPath.length(); i++) {
			char ch = propertyPath.charAt(i);
			if (ch == PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR ||
					ch == PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine the first nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * 
	 * <p> 确定给定属性路径中的第一个嵌套属性分隔符，忽略键中的点（例如“map [my.key]”）。
	 * 
	 * @param propertyPath the property path to check - 要检查的属性路径
	 * @return the index of the nested property separator, or -1 if none
	 * 
	 * <p> 嵌套属性分隔符的索引，如果没有，则返回-1
	 * 
	 */
	public static int getFirstNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, false);
	}

	/**
	 * Determine the first nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * 
	 * <p> 确定给定属性路径中的第一个嵌套属性分隔符，忽略键中的点（例如“map [my.key]”）。
	 * 
	 * @param propertyPath the property path to check - 要检查的属性路径
	 * @return the index of the nested property separator, or -1 if none
	 * 
	 * <p> 嵌套属性分隔符的索引，如果没有，则返回-1
	 * 
	 */
	public static int getLastNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, true);
	}

	/**
	 * Determine the first (or last) nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * 
	 * <p> 确定给定属性路径中的第一个（或最后一个）嵌套属性分隔符，忽略键中的点（如“map [my.key]”）。
	 * 
	 * @param propertyPath the property path to check - 要检查的属性路径
	 * @param last whether to return the last separator rather than the first
	 * 
	 * <p> 是否返回最后一个分隔符而不是第一个分隔符
	 * 
	 * @return the index of the nested property separator, or -1 if none
	 * 
	 * <p> 嵌套属性分隔符的索引，如果没有，则返回-1
	 * 
	 */
	private static int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
		boolean inKey = false;
		int length = propertyPath.length();
		int i = (last ? length - 1 : 0);
		while (last ? i >= 0 : i < length) {
			switch (propertyPath.charAt(i)) {
				case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
				case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
					inKey = !inKey;
					break;
				case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
					if (!inKey) {
						return i;
					}
			}
			if (last) {
				i--;
			}
			else {
				i++;
			}
		}
		return -1;
	}

	/**
	 * Determine whether the given registered path matches the given property path,
	 * either indicating the property itself or an indexed element of the property.
	 * 
	 * <p> 确定给定的已注册路径是否与给定的属性路径匹配，指示属性本身或属性的索引元素。
	 * 
	 * @param propertyPath the property path (typically without index)
	 * 
	 * <p> 属性路径（通常没有索引）
	 * 
	 * @param registeredPath the registered path (potentially with index)
	 * 
	 * <p> 注册路径（可能带索引）
	 * 
	 * @return whether the paths match - 路径是否匹配
	 */
	public static boolean matchesProperty(String registeredPath, String propertyPath) {
		if (!registeredPath.startsWith(propertyPath)) {
			return false;
		}
		if (registeredPath.length() == propertyPath.length()) {
			return true;
		}
		if (registeredPath.charAt(propertyPath.length()) != PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
			return false;
		}
		return (registeredPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR, propertyPath.length() + 1) ==
				registeredPath.length() - 1);
	}

	/**
	 * Determine the canonical name for the given property path.
	 * Removes surrounding quotes from map keys:<br>
	 * 
	 * <p> 确定给定属性路径的规范名称。 从地图键中删除周围的引号：
	 * 
	 * {@code map['key']} -> {@code map[key]}<br>
	 * {@code map["key"]} -> {@code map[key]}
	 * @param propertyName the bean property path - bean属性路径
	 * @return the canonical representation of the property path
	 * 
	 * <p> 属性路径的规范表示
	 * 
	 */
	public static String canonicalPropertyName(String propertyName) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder(propertyName);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = sb.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = sb.indexOf(
						PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					String key = sb.substring(keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length(), keyEnd);
					if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
						sb.delete(keyStart + 1, keyStart + 2);
						sb.delete(keyEnd - 2, keyEnd - 1);
						keyEnd = keyEnd - 2;
					}
					searchIndex = keyEnd + PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Determine the canonical names for the given property paths.
	 * 
	 * <p> 确定给定属性路径的规范名称。
	 * 
	 * @param propertyNames the bean property paths (as array)
	 * 
	 * <p> bean属性路径（作为数组）
	 * 
	 * @return the canonical representation of the property paths
	 * (as array of the same size)
	 * 
	 * <p> 属性路径的规范表示（作为相同大小的数组）
	 * 
	 * @see #canonicalPropertyName(String)
	 */
	public static String[] canonicalPropertyNames(String[] propertyNames) {
		if (propertyNames == null) {
			return null;
		}
		String[] result = new String[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			result[i] = canonicalPropertyName(propertyNames[i]);
		}
		return result;
	}

}
