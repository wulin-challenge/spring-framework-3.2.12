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

import org.springframework.transaction.TransactionStatus;

/**
 * Callback interface for transactional code. Used with {@link TransactionTemplate}'s
 * {@code execute} method, often as anonymous class within a method implementation.
 * 
 * <p> 事务代码的回调接口。 与TransactionTemplate的execute方法一起使用，通常作为方法实现中的匿名类。
 *
 * <p>Typically used to assemble various calls to transaction-unaware data access
 * services into a higher-level service method with transaction demarcation. As an
 * alternative, consider the use of declarative transaction demarcation (e.g. through
 * Spring's {@link org.springframework.transaction.annotation.Transactional} annotation).
 * 
 * <p> 通常用于将对事务无意识数据访问服务的各种调用组合成具有事务划分的更高级服务方法。 作为替代方案，请考虑使用声明式事务划分
 * （例如，通过Spring的org.springframework.transaction.annotation.Transactional注释）。
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see TransactionTemplate
 * @see CallbackPreferringPlatformTransactionManager
 */
public interface TransactionCallback<T> {

	/**
	 * Gets called by {@link TransactionTemplate#execute} within a transactional context.
	 * Does not need to care about transactions itself, although it can retrieve
	 * and influence the status of the current transaction via the given status
	 * object, e.g. setting rollback-only.
	 * 
	 * <p> 在事务上下文中由TransactionTemplate.execute调用。 不需要关心事务本身，尽管它可以通过给定的状态对象检索和影响当前事务的状态，
	 * 例如， 设置仅回滚。
	 *
	 * <p>Allows for returning a result object created within the transaction, i.e.
	 * a domain object or a collection of domain objects. A RuntimeException thrown
	 * by the callback is treated as application exception that enforces a rollback.
	 * Any such exception will be propagated to the caller of the template, unless
	 * there is a problem rolling back, in which case a TransactionException will be
	 * thrown.
	 * 
	 * <p> 允许返回在事务中创建的结果对象，即域对象或域对象的集合。 回调抛出的RuntimeException被视为强制执行回滚的应用程序异常。 
	 * 除非存在回滚问题，否则任何此类异常都将传播到模板的调用者，在这种情况下将抛出TransactionException。
	 *
	 * @param status associated transaction status - 关联交易状态
	 * @return a result object, or {@code null}
	 * 
	 * <p> 结果对象，或null
	 * 
	 * @see TransactionTemplate#execute
	 * @see CallbackPreferringPlatformTransactionManager#execute
	 */
	T doInTransaction(TransactionStatus status);

}
