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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

/**
 * Strategy interface used by {@link TransactionInterceptor} for metadata retrieval.
 * 
 * <p> TransactionInterceptor用于元数据检索的策略接口。
 *
 * <p>Implementations know how to source transaction attributes, whether from configuration,
 * metadata attributes at source level (such as Java 5 annotations), or anywhere else.
 * 
 * <p> 实现知道如何从配置，源级别的元数据属性（例如Java 5注释）或其他任何地方获取事务属性。
 *
 * @author Rod Johnson
 * @since 15.04.2003
 * @see TransactionInterceptor#setTransactionAttributeSource
 * @see TransactionProxyFactoryBean#setTransactionAttributeSource
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 */
public interface TransactionAttributeSource {

	/**
	 * Return the transaction attribute for the given method,
	 * or {@code null} if the method is non-transactional.
	 * 
	 * <p> 返回给定方法的transaction属性，如果该方法是非事务性的，则返回null。
	 * 
	 * @param method the method to introspect - 内省的方法
	 * @param targetClass the target class. May be {@code null},
	 * in which case the declaring class of the method must be used.
	 * 
	 * <p> 目标类。 可以为null，在这种情况下，必须使用方法的声明类。
	 * 
	 * @return TransactionAttribute the matching transaction attribute,
	 * or {@code null} if none found
	 * 
	 * <p> TransactionAttribute匹配的事务属性，如果没有找到则返回null
	 * 
	 */
	TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass);

}
