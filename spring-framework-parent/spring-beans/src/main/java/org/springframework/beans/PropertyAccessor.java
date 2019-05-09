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

import java.util.Map;

import org.springframework.core.convert.TypeDescriptor;

/**
 * Common interface for classes that can access named properties
 * (such as bean properties of an object or fields in an object)
 * Serves as base interface for {@link BeanWrapper}.
 * 
 * <p> 可以访问命名属性的类的公共接口（例如对象的bean属性或对象中的字段）用作BeanWrapper的基接口。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see BeanWrapper
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see PropertyAccessorFactory#forDirectFieldAccess
 */
public interface PropertyAccessor {

	/**
	 * Path separator for nested properties.
	 * Follows normal Java conventions: getFoo().getBar() would be "foo.bar".
	 * 
	 * <p> 嵌套属性的路径分隔符。 遵循正常的Java约定：getFoo().getBar()将是“foo.bar”。
	 */
	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

	/**
	 * Marker that indicates the start of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 * 
	 * <p> 标记，指示索引或映射属性（如“person.addresses [0]”）的属性键的开头。
	 */
	String PROPERTY_KEY_PREFIX = "[";
	char PROPERTY_KEY_PREFIX_CHAR = '[';

	/**
	 * Marker that indicates the end of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 * 
	 * <p> 标记，指示索引或映射属性（如“person.addresses [0]”）的属性键的结尾。
	 * 
	 */
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_SUFFIX_CHAR = ']';


	/**
	 * Determine whether the specified property is readable.
	 * 
	 * <p> 确定指定的属性是否可读。
	 * 
	 * <p>Returns {@code false} if the property doesn't exist.
	 * 
	 * <p> 如果该属性不存在，则返回false。
	 * 
	 * @param propertyName the property to check
	 * (may be a nested path and/or an indexed/mapped property)
	 * 
	 * <p> 要检查的属性（可能是嵌套路径和/或索引/映射属性）
	 * 
	 * @return whether the property is readable - 该属性是否可读
	 */
	boolean isReadableProperty(String propertyName);

	/**
	 * Determine whether the specified property is writable.
	 * 
	 * <p> 确定指定的属性是否可写。
	 * 
	 * <p>Returns {@code false} if the property doesn't exist.
	 * 
	 * <p> 如果该属性不存在，则返回false。
	 * 
	 * @param propertyName the property to check
	 * (may be a nested path and/or an indexed/mapped property)
	 * 
	 * <p> 要检查的属性（可能是嵌套路径和/或索引/映射属性）
	 * 
	 * @return whether the property is writable - 该属性是否可写
	 */
	boolean isWritableProperty(String propertyName);

	/**
	 * Determine the property type for the specified property,
	 * either checking the property descriptor or checking the value
	 * in case of an indexed or mapped element.
	 * 
	 * <p> 确定指定属性的属性类型，检查属性描述符或在索引元素或映射元素的情况下检查值。
	 * 
	 * @param propertyName the property to check
	 * (may be a nested path and/or an indexed/mapped property)
	 * 
	 * <p> 要检查的属性（可能是嵌套路径和/或索引/映射属性）
	 * 
	 * @return the property type for the particular property,
	 * or {@code null} if not determinable
	 * 
	 * <p> 特定属性的属性类型，如果不可确定，则为null
	 * 
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't readable
	 * 
	 * <p> 如果没有这样的属性或属性不可读
	 * 
	 * @throws PropertyAccessException if the property was valid but the
	 * accessor method failed
	 * 
	 * <p> 如果属性有效但访问方法失败
	 * 
	 */
	Class getPropertyType(String propertyName) throws BeansException;

	/**
	 * Return a type descriptor for the specified property:
	 * preferably from the read method, falling back to the write method.
	 * 
	 * <p> 返回指定属性的类型描述符：最好从read方法返回write方法。
	 * 
	 * @param propertyName the property to check
	 * (may be a nested path and/or an indexed/mapped property)
	 * 
	 * <p> 要检查的属性（可能是嵌套路径和/或索引/映射属性）
	 * 
	 * @return the property type for the particular property,
	 * or {@code null} if not determinable
	 * 
	 * <p> 特定属性的属性类型，如果不可确定，则为null
	 * 
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't readable
	 * 
	 * <p> 如果没有这样的属性或属性不可读
	 */
	TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;

	/**
	 * Get the current value of the specified property.
	 * 
	 * <p> 获取指定属性的当前值。
	 * 
	 * @param propertyName the name of the property to get the value of
	 * (may be a nested path and/or an indexed/mapped property)
	 * 
	 * <p> 获取值的属性的名称（可以是嵌套路径和/或索引/映射属性）
	 * 
	 * @return the value of the property - 属性的价值
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't readable
	 * 
	 * <p> 如果没有这样的属性或属性不可读
	 * 
	 * @throws PropertyAccessException if the property was valid but the
	 * accessor method failed
	 * 
	 * <p> 如果属性有效但访问方法失败
	 */
	Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * Set the specified value as current property value.
	 * 
	 * <p> 将指定值设置为当前属性值。
	 * 
	 * @param propertyName the name of the property to set the value of
	 * (may be a nested path and/or an indexed/mapped property)
	 * 
	 * <p> 要设置值的属性的名称（可以是嵌套路径和/或索引/映射属性）
	 * 
	 * @param value the new value - 新的值
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * 
	 * <p> 如果没有这样的属性或属性不可写
	 * 
	 * @throws PropertyAccessException if the property was valid but the
	 * accessor method failed or a type mismatch occured
	 * 
	 * <p> 如果属性有效但访问方法失败或类型不匹配
	 * 
	 */
	void setPropertyValue(String propertyName, Object value) throws BeansException;

	/**
	 * Set the specified value as current property value.
	 * 
	 * <p> 将指定值设置为当前属性值。
	 * 
	 * @param pv an object containing the new property value
	 * 
	 * <p> 包含新属性值的对象
	 * 
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * 
	 * <p> 如果没有这样的属性或属性不可写
	 * 
	 * @throws PropertyAccessException if the property was valid but the
	 * accessor method failed or a type mismatch occured
	 * 
	 * <p> 如果属性有效但访问方法失败或类型不匹配
	 */
	void setPropertyValue(PropertyValue pv) throws BeansException;

	/**
	 * Perform a batch update from a Map.
	 * 
	 * <p> 从Map执行批量更新。
	 * 
	 * <p>Bulk updates from PropertyValues are more powerful: This method is
	 * provided for convenience. Behavior will be identical to that of
	 * the {@link #setPropertyValues(PropertyValues)} method.
	 * 
	 * <p> PropertyValues的批量更新功能更强大：此方法是为方便起见而提供的。
	 *  行为将与setPropertyValues（PropertyValues）方法的行为相同。
	 *  
	 * @param map Map to take properties from. Contains property value objects,
	 * keyed by property name
	 * 
	 * <p> 从map获取属性。 包含属性值对象，由属性名称键入
	 * 
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * 
	 * <p> 如果没有这样的属性或属性不可写
	 * 
	 * @throws PropertyBatchUpdateException if one or more PropertyAccessExceptions
	 * occured for specific properties during the batch update. This exception bundles
	 * all individual PropertyAccessExceptions. All other properties will have been
	 * successfully updated.
	 * 
	 * <p> 如果在批量更新期间发生特定属性的一个或多个PropertyAccessExceptions。
	 *  此异常捆绑了所有单个PropertyAccessExceptions。 所有其他属性都已成功更新。
	 */
	void setPropertyValues(Map<?, ?> map) throws BeansException;

	/**
	 * The preferred way to perform a batch update.
	 * 
	 * <p> 执行批量更新的首选方法。
	 * 
	 * <p>Note that performing a batch update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a type mismatch, but <b>not</b> an
	 * invalid field name or the like) is encountered, throwing a
	 * {@link PropertyBatchUpdateException} containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated remain changed.
	 * 
	 * <p> 请注意，执行批量更新与执行单个更新不同，因为如果遇到可恢复的错误（例如类型不匹配，但不是无效的字段名称等），
	 * 则此类的实现将继续更新属性 包含所有单个错误的PropertyBatchUpdateException。
	 *  稍后可以检查此异常以查看所有绑定错误。 已成功更新的属性仍会更改。
	 *  
	 * <p>Does not allow unknown fields or invalid fields.
	 * 
	 * <p> 不允许使用未知字段或无效字段。
	 * 
	 * @param pvs PropertyValues to set on the target object - PropertyValues设置在目标对象上
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * 
	 * <p> 如果没有这样的属性或属性不可写
	 * 
	 * @throws PropertyBatchUpdateException if one or more PropertyAccessExceptions
	 * occured for specific properties during the batch update. This exception bundles
	 * all individual PropertyAccessExceptions. All other properties will have been
	 * successfully updated.
	 * 
	 * <p> 如果在批量更新期间发生特定属性的一个或多个PropertyAccessExceptions。 
	 * 此异常捆绑了所有单个PropertyAccessExceptions。 所有其他属性都已成功更新。
	 * 
	 * @see #setPropertyValues(PropertyValues, boolean, boolean)
	 */
	void setPropertyValues(PropertyValues pvs) throws BeansException;

	/**
	 * Perform a batch update with more control over behavior.
	 * 
	 * <p> 通过更多控制行为执行批量更新。
	 * 
	 * <p>Note that performing a batch update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a type mismatch, but <b>not</b> an
	 * invalid field name or the like) is encountered, throwing a
	 * {@link PropertyBatchUpdateException} containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated remain changed.
	 * 
	 * <p> 请注意，执行批量更新与执行单个更新不同，因为如果遇到可恢复的错误（例如类型不匹配，但不是无效的字段名称等），
	 * 则此类的实现将继续更新属性 包含所有单个错误的PropertyBatchUpdateException。 
	 * 稍后可以检查此异常以查看所有绑定错误。 已成功更新的属性仍会更改。
	 * 
	 * @param pvs PropertyValues to set on the target object - PropertyValues设置在目标对象上
	 * @param ignoreUnknown should we ignore unknown properties (not found in the bean)
	 * 
	 * <p> 我们应该忽略未知的属性（在bean中找不到）
	 * 
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * 
	 * <p> 如果没有这样的属性或属性不可写
	 * 
	 * @throws PropertyBatchUpdateException if one or more PropertyAccessExceptions
	 * occured for specific properties during the batch update. This exception bundles
	 * all individual PropertyAccessExceptions. All other properties will have been
	 * successfully updated.
	 * 
	 * <p> 如果在批量更新期间发生特定属性的一个或多个PropertyAccessExceptions。
	 *  此异常捆绑了所有单个PropertyAccessExceptions。 所有其他属性都已成功更新。
	 * 
	 * @see #setPropertyValues(PropertyValues, boolean, boolean)
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
			throws BeansException;

	/**
	 * Perform a batch update with full control over behavior.
	 * 
	 * <p> 通过完全控制行为执行批量更新。
	 * 
	 * <p>Note that performing a batch update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a type mismatch, but <b>not</b> an
	 * invalid field name or the like) is encountered, throwing a
	 * {@link PropertyBatchUpdateException} containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated remain changed.
	 * 
	 * <p> 请注意，执行批量更新与执行单个更新不同，因为如果遇到可恢复的错误（例如类型不匹配，但不是无效的字段名称等），
	 * 则此类的实现将继续更新属性 包含所有单个错误的PropertyBatchUpdateException。
	 *  稍后可以检查此异常以查看所有绑定错误。 已成功更新的属性仍会更改。
	 *  
	 * @param pvs PropertyValues to set on the target object - PropertyValues设置在目标对象上
	 * @param ignoreUnknown should we ignore unknown properties (not found in the bean)
	 * 
	 * <p> 我们应该忽略未知的属性（在bean中找不到）
	 * 
	 * @param ignoreInvalid should we ignore invalid properties (found but not accessible)
	 * 
	 * <p> 我们应该忽略无效的属性（找到但不可访问）
	 * 
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * 
	 * <p> 如果没有这样的属性或属性不可写
	 * 
	 * @throws PropertyBatchUpdateException if one or more PropertyAccessExceptions
	 * occured for specific properties during the batch update. This exception bundles
	 * all individual PropertyAccessExceptions. All other properties will have been
	 * successfully updated.
	 * 
	 * <p> 如果在批量更新期间发生特定属性的一个或多个PropertyAccessExceptions。 
	 * 此异常捆绑了所有单个PropertyAccessExceptions。 所有其他属性都已成功更新。
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException;

}
