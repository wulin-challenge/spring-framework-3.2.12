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

package org.springframework.transaction.annotation;

import java.lang.reflect.AnnotatedElement;

import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * Strategy interface for parsing known transaction annotation types.
 * {@link AnnotationTransactionAttributeSource} delegates to such
 * parsers for supporting specific annotation types such as Spring's own
 * {@link Transactional} or EJB3's {@link javax.ejb.TransactionAttribute}.
 * 
 * <p> 用于解析已知事务注释类型的策略接口。 AnnotationTransactionAttributeSource委托给这样的解析器，
 * 以支持特定的注释类型，例如Spring自己的Transactional或EJB3的javax.ejb.TransactionAttribute。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AnnotationTransactionAttributeSource
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 */
public interface TransactionAnnotationParser {

	/**
	 * Parse the transaction attribute for the given method or class,
	 * based on a known annotation type.
	 * 
	 * <p> 基于已知的注释类型解析给定方法或类的事务属性。
	 * 
	 * <p>This essentially parses a known transaction annotation into Spring's
	 * metadata attribute class. Returns {@code null} if the method/class
	 * is not transactional.
	 * 
	 * <p> 这实际上将已知的事务注释解析为Spring的元数据属性类。 如果方法/类不是事务性的，则返回null。
	 * 
	 * @param ae the annotated method or class
	 * 
	 * <p> 带注释的方法或类
	 * 
	 * @return TransactionAttribute the configured transaction attribute,
	 * or {@code null} if none was found
	 * 
	 * <p> TransactionAttribute配置的事务属性，如果未找到，则返回null
	 * 
	 * @see AnnotationTransactionAttributeSource#determineTransactionAttribute
	 */
	TransactionAttribute parseTransactionAnnotation(AnnotatedElement ae);

}
