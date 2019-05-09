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

package org.springframework.core.type;

import java.util.Map;
import java.util.Set;

/**
 * Interface that defines abstract access to the annotations of a specific
 * class, in a form that does not require that class to be loaded yet.
 * 
 * <p> 定义对特定类的注释的抽象访问的接口，其形式不需要加载该类。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see StandardAnnotationMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getAnnotationMetadata()
 */
public interface AnnotationMetadata extends ClassMetadata {

	/**
	 * Return the names of all annotation types defined on the underlying class.
	 * 
	 * <p> 返回在基础类上定义的所有注释类型的名称。
	 * 
	 * @return the annotation type names - 注释类型名称
	 */
	Set<String> getAnnotationTypes();

	/**
	 * Return the names of all meta-annotation types defined on the
	 * given annotation type of the underlying class.
	 * 
	 * <p> 返回在基础类的给定注释类型上定义的所有元注释类型的名称。
	 * 
	 * @param annotationType the meta-annotation type to look for
	 * 
	 * <p> 要查找的元注释类型
	 * 
	 * @return the meta-annotation type names - 元注释类型名称
	 */
	Set<String> getMetaAnnotationTypes(String annotationType);

	/**
	 * Determine whether the underlying class has an annotation of the given
	 * type defined.
	 * 
	 * <p> 确定基础类是否具有定义的给定类型的注释。
	 * 
	 * @param annotationType the annotation type to look for
	 * 
	 * <p> 要查找的注释类型
	 * 
	 * @return whether a matching annotation is defined
	 * 
	 * <p> 是否定义了匹配的注释
	 * 
	 */
	boolean hasAnnotation(String annotationType);

	/**
	 * Determine whether the underlying class has an annotation that
	 * is itself annotated with the meta-annotation of the given type.
	 * 
	 * <p> 确定基础类是否具有注释，该注释本身使用给定类型的元注释进行注释。
	 * 
	 * @param metaAnnotationType the meta-annotation type to look for
	 * 
	 * <p> 要查找的元注释类型
	 * 
	 * @return whether a matching meta-annotation is defined
	 * 
	 * <p> 是否定义了匹配的元注释
	 */
	boolean hasMetaAnnotation(String metaAnnotationType);

	/**
	 * Determine whether the underlying class has an annotation or
	 * meta-annotation of the given type defined.
	 * 
	 * <p> 确定基础类是否具有定义的给定类型的注释或元注释。
	 * 
	 * <p>This is equivalent to a "hasAnnotation || hasMetaAnnotation"
	 * check. If this method returns {@code true}, then
	 * {@link #getAnnotationAttributes} will return a non-null Map.
	 * 
	 * <p> 这相当于“hasAnnotation || hasMetaAnnotation”检查。 如果此方法返回true，
	 * 则getAnnotationAttributes将返回非空Map。
	 * 
	 * @param annotationType the annotation type to look for
	 * 
	 * <p> 要查找的注释类型
	 * 
	 * @return whether a matching annotation is defined
	 * 
	 * <p> 是否定义了匹配的注释
	 * 
	 */
	boolean isAnnotated(String annotationType);

	/**
	 * Retrieve the attributes of the annotation of the given type,
	 * if any (i.e. if defined on the underlying class, as direct
	 * annotation or as meta-annotation).
	 * 
	 * <p> 检索给定类型的注释的属性（如果有）（即，如果在基础类上定义，则为直接注释或元注释）。
	 * 
	 * @param annotationType the annotation type to look for
	 * 
	 * <p> 要查找的注释类型
	 * 
	 * @return a Map of attributes, with the attribute name as key (e.g. "value")
	 * and the defined attribute value as Map value. This return value will be
	 * {@code null} if no matching annotation is defined.
	 * 
	 * <p> 属性映射，属性名称为键（例如“value”），定义的属性值为Map值。 如果未定义匹配的注释，则此返回值将为null。
	 * 
	 */
	Map<String, Object> getAnnotationAttributes(String annotationType);

	/**
	 * Retrieve the attributes of the annotation of the given type,
	 * if any (i.e. if defined on the underlying class, as direct
	 * annotation or as meta-annotation).
	 * 
	 * <p> 检索给定类型的注释的属性（如果有）（即，如果在基础类上定义，则为直接注释或元注释）。
	 * 
	 * @param annotationType the annotation type to look for
	 * 
	 * <p> 要查找的注释类型
	 * 
	 * @param classValuesAsString whether to convert class references to String
	 * class names for exposure as values in the returned Map, instead of Class
	 * references which might potentially have to be loaded first
	 * 
	 * <p> 是否将类引用转换为String类名，以便在返回的Map中将值作为值转换，而不是可能必须首先加载的类引用
	 * 
	 * @return a Map of attributes, with the attribute name as key (e.g. "value")
	 * and the defined attribute value as Map value. This return value will be
	 * {@code null} if no matching annotation is defined.
	 * 
	 * <p> 属性映射，属性名称为键（例如“value”），定义的属性值为Map值。 如果未定义匹配的注释，则此返回值将为null。
	 * 
	 */
	Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString);

	/**
	 * Determine whether the underlying class has any methods that are
	 * annotated (or meta-annotated) with the given annotation type.
	 * 
	 * <p> 确定基础类是否具有使用给定注释类型进行注释（或元注释）的任何方法。
	 * 
	 */
	boolean hasAnnotatedMethods(String annotationType);

	/**
	 * Retrieve the method metadata for all methods that are annotated
	 * (or meta-annotated) with the given annotation type.
	 * 
	 * <p> 检索使用给定注释类型注释（或元注释）的所有方法的方法元数据。
	 * 
	 * <p>For any returned method, {@link MethodMetadata#isAnnotated} will
	 * return {@code true} for the given annotation type.
	 * 
	 * <p> 对于任何返回的方法，MethodMetadata.isAnnotated将对给定的注释类型返回true。
	 * 
	 * @param annotationType the annotation type to look for
	 * 
	 * <p> 要查找的注释类型
	 * 
	 * @return a Set of {@link MethodMetadata} for methods that have a matching
	 * annotation. The return value will be an empty set if no methods match
	 * the annotation type.
	 * 
	 * <p> 一组MethodMetadata，用于具有匹配注释的方法。 如果没有方法与注释类型匹配，则返回值将为空集。
	 * 
	 */
	Set<MethodMetadata> getAnnotatedMethods(String annotationType);

}
