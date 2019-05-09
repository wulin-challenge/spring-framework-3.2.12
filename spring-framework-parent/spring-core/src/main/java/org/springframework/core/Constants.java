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

package org.springframework.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * This class can be used to parse other classes containing constant definitions
 * in public static final members. The {@code asXXXX} methods of this class
 * allow these constant values to be accessed via their string names.
 * 
 * <p>此类可用于解析包含公共静态最终成员中的常量定义的其他类。 此类的asXXXX方法允许通过其字符串名称访问这些常量值。
 *
 * <p>Consider class Foo containing {@code public final static int CONSTANT1 = 66;}
 * An instance of this class wrapping {@code Foo.class} will return the constant value
 * of 66 from its {@code asNumber} method given the argument {@code "CONSTANT1"}.
 * 
 * <p>考虑包含public final static int CONSTANT1 = 66的类Foo; 
 * 包含Foo.class的此类的实例将在给定参数“CONSTANT1”的情况下从其asNumber方法返回常量值66。
 *
 * <p>This class is ideal for use in PropertyEditors, enabling them to
 * recognize the same names as the constants themselves, and freeing them
 * from maintaining their own mapping.
 *
 *<p>此类非常适合在PropertyEditors中使用，使它们能够识别与常量本身相同的名称，并使它们无需维护自己的映射。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.03.2003
 */
public class Constants {

	/** The name of the introspected class */
	/** 内省类的名称 */
	private final String className;

	/** Map from String field name to object value */
	/** 从String字段名称映射到对象值 */
	private final Map<String, Object> fieldCache = new HashMap<String, Object>();


	/**
	 * Create a new Constants converter class wrapping the given class.
	 * <p>All <b>public</b> static final variables will be exposed, whatever their type.
	 * 
	 * <p>创建一个包装给定类的新Constants转换器类。无论其类型如何，所有公共静态最终变量都将被公开。
	 * 
	 * @param clazz the class to analyze 要分析的类
	 * @throws IllegalArgumentException if the supplied {@code clazz} is {@code null} 如果提供的clazz为null
	 */
	public Constants(Class<?> clazz) {
		Assert.notNull(clazz);
		this.className = clazz.getName();
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (ReflectionUtils.isPublicStaticFinal(field)) {
				String name = field.getName();
				try {
					Object value = field.get(null);
					this.fieldCache.put(name, value);
				}
				catch (IllegalAccessException ex) {
					// just leave this field and continue
					// 离开这个属性并且继续
				}
			}
		}
	}


	/**
	 * Return the name of the analyzed class. 返回分析的类的名称。
	 */
	public final String getClassName() {
		return this.className;
	}

	/**
	 * Return the number of constants exposed. 返回暴露的常量数。
	 */
	public final int getSize() {
		return this.fieldCache.size();
	}

	/**
	 * Exposes the field cache to subclasses:
	 * a Map from String field name to object value.
	 * 
	 * <p>将字段高速缓存暴露给子类：从字段字段名称到对象值的映射。
	 */
	protected final Map<String, Object> getFieldCache() {
		return this.fieldCache;
	}


	/**
	 * Return a constant value cast to a Number.
	 * 
	 * <p> 返回一个常量值强制转换为数字。
	 * 
	 * @param code the name of the field (never {@code null})
	 * 
	 * <p> 字段的名称（永不为null）
	 * 
	 * @return the Number value - 数值
	 * @see #asObject
	 * @throws ConstantException if the field name wasn't found
	 * or if the type wasn't compatible with Number
	 * 
	 * <p> 如果找不到字段名称或类型与Number不兼容
	 * 
	 */
	public Number asNumber(String code) throws ConstantException {
		Object obj = asObject(code);
		if (!(obj instanceof Number)) {
			throw new ConstantException(this.className, code, "not a Number");
		}
		return (Number) obj;
	}

	/**
	 * Return a constant value as a String.
	 * 
	 * <p> 返回一个常量值作为String。
	 * 
	 * @param code the name of the field (never {@code null})
	 * 
	 * <p> 字段的名称（永不为null）
	 * 
	 * @return the String value
	 * Works even if it's not a string (invokes {@code toString()}).
	 * 
	 * <p> String值即使它不是字符串也可以工作（调用toString（））。
	 * 
	 * @see #asObject
	 * @throws ConstantException if the field name wasn't found
	 * 
	 * <p> 如果找不到字段名称
	 * 
	 */
	public String asString(String code) throws ConstantException {
		return asObject(code).toString();
	}

	/**
	 * Parse the given String (upper or lower case accepted) and return
	 * the appropriate value if it's the name of a constant field in the
	 * class that we're analysing.
	 * 
	 * <p> 解析给定的String（接受大写或小写）并返回适当的值，如果它是我们正在分析的类中的常量字段的名称。
	 * 
	 * @param code the name of the field (never {@code null})
	 * 
	 * <p> 字段的名称（永不为null）
	 * 
	 * @return the Object value - 对象值
	 * 
	 * @throws ConstantException if there's no such field
	 * 
	 * <p> 如果没有这样的领域
	 * 
	 */
	public Object asObject(String code) throws ConstantException {
		Assert.notNull(code, "Code must not be null");
		String codeToUse = code.toUpperCase(Locale.ENGLISH);
		Object val = this.fieldCache.get(codeToUse);
		if (val == null) {
			throw new ConstantException(this.className, codeToUse, "not found");
		}
		return val;
	}


	/**
	 * Return all names of the given group of constants.
	 * 
	 * <p> 返回给定常量组的所有名称。
	 * 
	 * <p>Note that this method assumes that constants are named
	 * in accordance with the standard Java convention for constant
	 * values (i.e. all uppercase). The supplied {@code namePrefix}
	 * will be uppercased (in a locale-insensitive fashion) prior to
	 * the main logic of this method kicking in.
	 * 
	 * <p> 请注意，此方法假定常量是根据常量值的标准Java约定命名的（即全部大写）。 在此方法的主要逻辑开始之前，
	 * 提供的namePrefix将是大写的（以区域设置不敏感的方式）。
	 * 
	 * @param namePrefix prefix of the constant names to search (may be {@code null})
	 * 
	 * <p> 要搜索的常量名称的前缀（可以为null）
	 * 
	 * @return the set of constant names
	 * 
	 * <p> 常量名称的集合
	 * 
	 */
	public Set<String> getNames(String namePrefix) {
		String prefixToUse = (namePrefix != null ? namePrefix.trim().toUpperCase(Locale.ENGLISH) : "");
		Set<String> names = new HashSet<String>();
		for (String code : this.fieldCache.keySet()) {
			if (code.startsWith(prefixToUse)) {
				names.add(code);
			}
		}
		return names;
	}

	/**
	 * Return all names of the group of constants for the
	 * given bean property name.
	 * 
	 * <p> 返回给定bean属性名称的常量组的所有名称。
	 * 
	 * @param propertyName the name of the bean property
	 * 
	 * <p> bean属性的名称
	 * 
	 * @return the set of values - 返回的值属于set集合
	 * @see #propertyToConstantNamePrefix
	 */
	public Set<String> getNamesForProperty(String propertyName) {
		return getNames(propertyToConstantNamePrefix(propertyName));
	}

	/**
	 * Return all names of the given group of constants.
	 * 
	 * <p> 返回给定常量组的所有名称。
	 * 
	 * <p>Note that this method assumes that constants are named
	 * in accordance with the standard Java convention for constant
	 * values (i.e. all uppercase). The supplied {@code nameSuffix}
	 * will be uppercased (in a locale-insensitive fashion) prior to
	 * the main logic of this method kicking in.
	 * 
	 * <p> 请注意，此方法假定常量是根据常量值的标准Java约定命名的（即全部大写）。 在此方法的主要逻辑开始之前，
	 * 提供的nameSuffix将是大写的（以区域设置不敏感的方式）。
	 * 
	 * @param nameSuffix suffix of the constant names to search (may be {@code null})
	 * 
	 * <p> 要搜索的常量名称的后缀（可以为null）
	 * 
	 * @return the set of constant names
	 * 
	 * <p> 返回的名称是一个set集合
	 */
	public Set<String> getNamesForSuffix(String nameSuffix) {
		String suffixToUse = (nameSuffix != null ? nameSuffix.trim().toUpperCase(Locale.ENGLISH) : "");
		Set<String> names = new HashSet<String>();
		for (String code : this.fieldCache.keySet()) {
			if (code.endsWith(suffixToUse)) {
				names.add(code);
			}
		}
		return names;
	}


	/**
	 * Return all values of the given group of constants.
	 * 
	 * <p> 返回给定常量组的所有值。
	 * 
	 * <p>Note that this method assumes that constants are named
	 * in accordance with the standard Java convention for constant
	 * values (i.e. all uppercase). The supplied {@code namePrefix}
	 * will be uppercased (in a locale-insensitive fashion) prior to
	 * the main logic of this method kicking in.
	 * 
	 * <p> 请注意，此方法假定常量是根据常量值的标准Java约定命名的（即全部大写）。 在此方法的主要逻辑开始之前，
	 * 提供的namePrefix将是大写的（以区域设置不敏感的方式）。
	 * 
	 * @param namePrefix prefix of the constant names to search (may be {@code null})
	 * 
	 * <p> 要搜索的常量名称的前缀（可以为null）
	 * 
	 * @return the set of values - 返回的值是一个set集合
	 */
	public Set<Object> getValues(String namePrefix) {
		String prefixToUse = (namePrefix != null ? namePrefix.trim().toUpperCase(Locale.ENGLISH) : "");
		Set<Object> values = new HashSet<Object>();
		for (String code : this.fieldCache.keySet()) {
			if (code.startsWith(prefixToUse)) {
				values.add(this.fieldCache.get(code));
			}
		}
		return values;
	}

	/**
	 * Return all values of the group of constants for the
	 * given bean property name.
	 * 
	 * <p> 返回给定bean属性名称的常量组的所有值。
	 * 
	 * @param propertyName the name of the bean property
	 * 
	 * <p> bean属性的名称
	 * 
	 * @return the set of values - 返回的值是一个set集合
	 * @see #propertyToConstantNamePrefix
	 */
	public Set<Object> getValuesForProperty(String propertyName) {
		return getValues(propertyToConstantNamePrefix(propertyName));
	}

	/**
	 * Return all values of the given group of constants.
	 * 
	 * <p> 返回给定常量组的所有值。
	 * 
	 * <p>Note that this method assumes that constants are named
	 * in accordance with the standard Java convention for constant
	 * values (i.e. all uppercase). The supplied {@code nameSuffix}
	 * will be uppercased (in a locale-insensitive fashion) prior to
	 * the main logic of this method kicking in.
	 * 
	 * <p> 请注意，此方法假定常量是根据常量值的标准Java约定命名的（即全部大写）。 在此方法的主要逻辑开始之前，
	 * 提供的nameSuffix将是大写的（以区域设置不敏感的方式）。
	 * 
	 * @param nameSuffix suffix of the constant names to search (may be {@code null})
	 * 
	 * <p> 要搜索的常量名称的后缀（可以为null）
	 * 
	 * @return the set of values - 返回的值是一个set集合
	 */
	public Set<Object> getValuesForSuffix(String nameSuffix) {
		String suffixToUse = (nameSuffix != null ? nameSuffix.trim().toUpperCase(Locale.ENGLISH) : "");
		Set<Object> values = new HashSet<Object>();
		for (String code : this.fieldCache.keySet()) {
			if (code.endsWith(suffixToUse)) {
				values.add(this.fieldCache.get(code));
			}
		}
		return values;
	}


	/**
	 * Look up the given value within the given group of constants.
	 * 
	 * <p> 在给定的常量组中查找给定值。
	 * 
	 * <p>Will return the first match.
	 * 
	 * <p> 将返回第一个匹配的
	 * 
	 * @param value constant value to look up
	 * 
	 * <p> 查找常量值
	 * 
	 * @param namePrefix prefix of the constant names to search (may be {@code null})
	 * 
	 * <p> 要搜索的常量名称的前缀（可以为null）
	 * 
	 * @return the name of the constant field
	 * 
	 * <p> 返回这个常量属性的名称
	 * 
	 * @throws ConstantException if the value wasn't found
	 * 
	 * <p> 如果这个值没有找到,就抛出该异常
	 */
	public String toCode(Object value, String namePrefix) throws ConstantException {
		String prefixToUse = (namePrefix != null ? namePrefix.trim().toUpperCase(Locale.ENGLISH) : "");
		for (Map.Entry<String, Object> entry : this.fieldCache.entrySet()) {
			if (entry.getKey().startsWith(prefixToUse) && entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		throw new ConstantException(this.className, prefixToUse, value);
	}

	/**
	 * Look up the given value within the group of constants for
	 * the given bean property name. Will return the first match.
	 * 
	 * <p> 在给定bean属性名称的常量组中查找给定值。 将返回第一匹配的值
	 * 
	 * @param value constant value to look up
	 * 
	 * <p> 查找常量的值
	 * 
	 * @param propertyName the name of the bean property
	 * 
	 * <p> bean属性的名称
	 * 
	 * @return the name of the constant field
	 * 
	 * <p> 返回这个常量属性的名称
	 * 
	 * @throws ConstantException if the value wasn't found
	 * 
	 * <p> <p> 如果这个值没有找到,就抛出该异常
	 * 
	 * @see #propertyToConstantNamePrefix
	 */
	public String toCodeForProperty(Object value, String propertyName) throws ConstantException {
		return toCode(value, propertyToConstantNamePrefix(propertyName));
	}

	/**
	 * Look up the given value within the given group of constants.
	 * <p>Will return the first match.
	 * 
	 * <p> 将返回第一个匹配的
	 * 
	 * @param value constant value to look up
	 * 
	 * <p> 查找常量的值
	 * 
	 * @param nameSuffix suffix of the constant names to search (may be {@code null})
	 * 
	 * <p> 要搜索的常量名称的前缀（可以为null）
	 * 
	 * @return the name of the constant field
	 * 
	 * <p> 返回这个常量属性的名称
	 * 
	 * @throws ConstantException if the value wasn't found
	 * 
	 * <p> <p> 如果这个值没有找到,就抛出该异常
	 * 
	 */
	public String toCodeForSuffix(Object value, String nameSuffix) throws ConstantException {
		String suffixToUse = (nameSuffix != null ? nameSuffix.trim().toUpperCase(Locale.ENGLISH) : "");
		for (Map.Entry<String, Object> entry : this.fieldCache.entrySet()) {
			if (entry.getKey().endsWith(suffixToUse) && entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		throw new ConstantException(this.className, suffixToUse, value);
	}


	/**
	 * Convert the given bean property name to a constant name prefix.
	 * 
	 * <p> 将给定的bean属性名称转换为常量名称前缀。
	 * 
	 * <p>Uses a common naming idiom: turning all lower case characters to
	 * upper case, and prepending upper case characters with an underscore.
	 * 
	 * <p> 使用一种常见的命名习语：将所有小写字符转换为大写字母，并使用下划线添加大写字符。
	 * 
	 * <p>Example: "imageSize" -> "IMAGE_SIZE"<br>
	 * Example: "imagesize" -> "IMAGESIZE".<br>
	 * Example: "ImageSize" -> "_IMAGE_SIZE".<br>
	 * Example: "IMAGESIZE" -> "_I_M_A_G_E_S_I_Z_E"
	 * @param propertyName the name of the bean property
	 * 
	 * <P> bean属性的名称
	 * 
	 * @return the corresponding constant name prefix
	 * 
	 * <p> 相应的常量名称前缀
	 * 
	 * @see #getValuesForProperty
	 * @see #toCodeForProperty
	 */
	public String propertyToConstantNamePrefix(String propertyName) {
		StringBuilder parsedPrefix = new StringBuilder();
		for (int i = 0; i < propertyName.length(); i++) {
			char c = propertyName.charAt(i);
			if (Character.isUpperCase(c)) {
				parsedPrefix.append("_");
				parsedPrefix.append(c);
			}
			else {
				parsedPrefix.append(Character.toUpperCase(c));
			}
		}
		return parsedPrefix.toString();
	}

}
