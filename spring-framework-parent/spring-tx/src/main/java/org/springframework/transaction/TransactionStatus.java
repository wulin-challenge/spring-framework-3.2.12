/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.transaction;

/**
 * Representation of the status of a transaction.
 * 
 * <p> 表示交易状态。
 *
 * <p>Transactional code can use this to retrieve status information,
 * and to programmatically request a rollback (instead of throwing
 * an exception that causes an implicit rollback).
 * 
 * <p> 事务代码可以使用它来检索状态信息，并以编程方式请求回滚（而不是抛出导致隐式回滚的异常）。
 *
 * <p>Derives from the SavepointManager interface to provide access
 * to savepoint management facilities. Note that savepoint management
 * is only available if supported by the underlying transaction manager.
 * 
 * <p> 从SavepointManager界面派生，以提供对保存点管理工具的访问。 请注意，只有在基础事务管理器支持的情况下，保存点管理才可用。
 *
 * @author Juergen Hoeller
 * @since 27.03.2003
 * @see #setRollbackOnly()
 * @see PlatformTransactionManager#getTransaction
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()
 */
public interface TransactionStatus extends SavepointManager {

	/**
	 * Return whether the present transaction is new (else participating
	 * in an existing transaction, or potentially not running in an
	 * actual transaction in the first place).
	 * 
	 * <p> 返回当前事务是否为新事务（否则参与现有事务，或者可能不首先在实际事务中运行）。
	 */
	boolean isNewTransaction();

	/**
	 * Return whether this transaction internally carries a savepoint,
	 * that is, has been created as nested transaction based on a savepoint.
	 * 
	 * <p> 返回此事务是否在内部携带保存点，即基于保存点创建为嵌套事务。
	 * 
	 * <p>This method is mainly here for diagnostic purposes, alongside
	 * {@link #isNewTransaction()}. For programmatic handling of custom
	 * savepoints, use SavepointManager's operations.
	 * 
	 * <p> 除了isNewTransaction（）之外，此方法主要用于诊断目的。 对于自定义保存点的编程处理，请使用SavepointManager的操作。
	 * 
	 * @see #isNewTransaction()
	 * @see #createSavepoint
	 * @see #rollbackToSavepoint(Object)
	 * @see #releaseSavepoint(Object)
	 */
	boolean hasSavepoint();

	/**
	 * Set the transaction rollback-only. This instructs the transaction manager
	 * that the only possible outcome of the transaction may be a rollback, as
	 * alternative to throwing an exception which would in turn trigger a rollback.
	 * 
	 * <p> 设置仅事务回滚。 这指示事务管理器事务的唯一可能结果可能是回滚，作为抛出异常的替代方法，而异常又会触发回滚。
	 * 
	 * <p>This is mainly intended for transactions managed by
	 * {@link org.springframework.transaction.support.TransactionTemplate} or
	 * {@link org.springframework.transaction.interceptor.TransactionInterceptor},
	 * where the actual commit/rollback decision is made by the container.
	 * 
	 * <p> 这主要用于由org.springframework.transaction.support.TransactionTemplate或
	 * org.springframework.transaction.interceptor.TransactionInterceptor管理的事务，
	 * 其中实际的提交/回滚决策由容器决定。
	 * 
	 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn
	 */
	void setRollbackOnly();

	/**
	 * Return whether the transaction has been marked as rollback-only
	 * (either by the application or by the transaction infrastructure).
	 * 
	 * <p> 返回事务是否已标记为仅回滚（由应用程序或事务基础结构）。
	 * 
	 */
	boolean isRollbackOnly();

	/**
	 * Flush the underlying session to the datastore, if applicable:
	 * for example, all affected Hibernate/JPA sessions.
	 * 
	 * <p> 如果适用，将基础会话刷新到数据存储区：例如，所有受影响的Hibernate / JPA会话。
	 * 
	 */
	void flush();

	/**
	 * Return whether this transaction is completed, that is,
	 * whether it has already been committed or rolled back.
	 * 
	 * <p> 返回此事务是否已完成，即是否已提交或回滚。
	 * 
	 * @see PlatformTransactionManager#commit
	 * @see PlatformTransactionManager#rollback
	 */
	boolean isCompleted();

}
