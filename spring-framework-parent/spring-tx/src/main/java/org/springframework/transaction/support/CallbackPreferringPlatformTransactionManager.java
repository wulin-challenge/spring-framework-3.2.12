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

package org.springframework.transaction.support;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Extension of the {@link org.springframework.transaction.PlatformTransactionManager}
 * interface, exposing a method for executing a given callback within a transaction.
 * 
 * <p> 扩展org.springframework.transaction.PlatformTransactionManager接口，公开在事务中执行给定回调的方法。
 *
 * <p>Implementors of this interface automatically express a preference for
 * callbacks over programmatic {@code getTransaction}, {@code commit}
 * and {@code rollback} calls. Calling code may check whether a given
 * transaction manager implements this interface to choose to prepare a
 * callback instead of explicit transaction demarcation control.
 * 
 * <p> 此接口的实现程序会自动表示对程序化getTransaction，commit和rollback调用的回调首选项。 
 * 调用代码可以检查给定的事务管理器是否实现此接口以选择准备回调而不是显式事务划分控制。
 *
 * <p>Spring's {@link TransactionTemplate} and
 * {@link org.springframework.transaction.interceptor.TransactionInterceptor}
 * detect and use this PlatformTransactionManager variant automatically.
 * 
 * <p> Spring的TransactionTemplate和org.springframework.transaction.interceptor.TransactionInterceptor
 * 自动检测并使用此PlatformTransactionManager变量。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see TransactionTemplate
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */
public interface CallbackPreferringPlatformTransactionManager extends PlatformTransactionManager {

	/**
	 * Execute the action specified by the given callback object within a transaction.
	 * 
	 * <p> 在事务中执行给定回调对象指定的操作。
	 * 
	 * <p>Allows for returning a result object created within the transaction, that is,
	 * a domain object or a collection of domain objects. A RuntimeException thrown
	 * by the callback is treated as a fatal exception that enforces a rollback.
	 * Such an exception gets propagated to the caller of the template.
	 * 
	 * <p> 允许返回在事务中创建的结果对象，即域对象或域对象的集合。 回调抛出的RuntimeException被视为强制执行回滚的致命异常。 
	 * 这种异常会传播到模板的调用者。
	 * 
	 * @param definition the definition for the transaction to wrap the callback in
	 * 
	 * <p> 用于包装回调的事务的定义
	 * 
	 * @param callback the callback object that specifies the transactional action
	 * 
	 * <p> 指定事务操作的回调对象
	 * 
	 * @return a result object returned by the callback, or {@code null} if none
	 * 
	 * <p> 回调返回的结果对象，如果没有则返回null
	 * 
	 * @throws TransactionException in case of initialization, rollback, or system errors
	 * 
	 * <p> 在初始化，回滚或系统错误的情况下
	 * 
	 * @throws RuntimeException if thrown by the TransactionCallback
	 * 
	 * <p> 如果由TransactionCallback抛出
	 */
	<T> T execute(TransactionDefinition definition, TransactionCallback<T> callback)
			throws TransactionException;

}
