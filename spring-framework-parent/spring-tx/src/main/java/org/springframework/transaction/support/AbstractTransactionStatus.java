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

import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionUsageException;

/**
 * Abstract base implementation of the
 * {@link org.springframework.transaction.TransactionStatus} interface.
 * 
 * <p> org.springframework.transaction.TransactionStatus接口的抽象基础实现。
 *
 * <p>Pre-implements the handling of local rollback-only and completed flags, and
 * delegation to an underlying {@link org.springframework.transaction.SavepointManager}.
 * Also offers the option of a holding a savepoint within the transaction.
 * 
 * <p> 预先实现本地回滚和已完成标志的处理，并委托给基础org.springframework.transaction.SavepointManager。 
 * 还提供在交易中持有保存点的选项。
 *
 * <p>Does not assume any specific internal transaction handling, such as an
 * underlying transaction object, and no transaction synchronization mechanism.
 * 
 * <p> 不承担任何特定的内部事务处理，例如底层事务对象，也没有事务同步机制。
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 * @see #setRollbackOnly()
 * @see #isRollbackOnly()
 * @see #setCompleted()
 * @see #isCompleted()
 * @see #getSavepointManager()
 * @see SimpleTransactionStatus
 * @see DefaultTransactionStatus
 */
public abstract class AbstractTransactionStatus implements TransactionStatus {

	private boolean rollbackOnly = false;

	private boolean completed = false;

	private Object savepoint;


	//---------------------------------------------------------------------
	// Handling of current transaction state
	// 处理当前交易状态
	//---------------------------------------------------------------------

	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	/**
	 * Determine the rollback-only flag via checking both the local rollback-only flag
	 * of this TransactionStatus and the global rollback-only flag of the underlying
	 * transaction, if any.
	 * 
	 * <p> 通过检查此TransactionStatus的本地回滚标志和基础事务的全局回滚标志（如果有）来确定仅回滚标志。
	 * 
	 * @see #isLocalRollbackOnly()
	 * @see #isGlobalRollbackOnly()
	 */
	public boolean isRollbackOnly() {
		return (isLocalRollbackOnly() || isGlobalRollbackOnly());
	}

	/**
	 * Determine the rollback-only flag via checking this TransactionStatus.
	 * 
	 * <p> 通过检查此TransactionStatus确定仅回滚标志。
	 * 
	 * <p>Will only return "true" if the application called {@code setRollbackOnly}
	 * on this TransactionStatus object.
	 * 
	 * <p> 如果应用程序在此TransactionStatus对象上调用setRollbackOnly，则仅返回“true”。
	 */
	public boolean isLocalRollbackOnly() {
		return this.rollbackOnly;
	}

	/**
	 * Template method for determining the global rollback-only flag of the
	 * underlying transaction, if any.
	 * 
	 * <p> 用于确定基础事务的全局回滚标志的模板方法（如果有）。
	 * 
	 * <p>This implementation always returns {@code false}.
	 * 
	 * <p> 此实现始终返回false。
	 * 
	 */
	public boolean isGlobalRollbackOnly() {
		return false;
	}

	/**
	 * This implementations is empty, considering flush as a no-op.
	 * 
	 * <p> 这种实现是空的，将flush视为无操作。
	 */
	public void flush() {
	}

	/**
	 * Mark this transaction as completed, that is, committed or rolled back.
	 * 
	 * <p> 将此事务标记为已完成，即已提交或已回滚。
	 */
	public void setCompleted() {
		this.completed = true;
	}

	public boolean isCompleted() {
		return this.completed;
	}


	//---------------------------------------------------------------------
	// Handling of current savepoint state
	// 处理当前的保存点状态
	//---------------------------------------------------------------------

	/**
	 * Set a savepoint for this transaction. Useful for PROPAGATION_NESTED.
	 * 
	 * <p> 为此事务设置保存点。 适用于PROPAGATION_NESTED。
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED
	 */
	protected void setSavepoint(Object savepoint) {
		this.savepoint = savepoint;
	}

	/**
	 * Get the savepoint for this transaction, if any.
	 * 
	 * <p> 获取此事务的保存点（如果有）。
	 * 
	 */
	protected Object getSavepoint() {
		return this.savepoint;
	}

	public boolean hasSavepoint() {
		return (this.savepoint != null);
	}

	/**
	 * Create a savepoint and hold it for the transaction.
	 * 
	 * <p> 创建一个保存点并为事务保留它。
	 * 
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 * 
	 * <p> 如果底层事务不支持savepointsTransactionException
	 * 
	 */
	public void createAndHoldSavepoint() throws TransactionException {
		setSavepoint(getSavepointManager().createSavepoint());
	}

	/**
	 * Roll back to the savepoint that is held for the transaction.
	 * 
	 * <p> 回滚到为事务保留的保存点。
	 */
	public void rollbackToHeldSavepoint() throws TransactionException {
		if (!hasSavepoint()) {
			throw new TransactionUsageException("No savepoint associated with current transaction");
		}
		getSavepointManager().rollbackToSavepoint(getSavepoint());
		setSavepoint(null);
	}

	/**
	 * Release the savepoint that is held for the transaction.
	 * 
	 * <p> 释放为事务保留的保存点。
	 * 
	 */
	public void releaseHeldSavepoint() throws TransactionException {
		if (!hasSavepoint()) {
			throw new TransactionUsageException("No savepoint associated with current transaction");
		}
		getSavepointManager().releaseSavepoint(getSavepoint());
		setSavepoint(null);
	}


	//---------------------------------------------------------------------
	// Implementation of SavepointManager
	// SavepointManager的实现
	//---------------------------------------------------------------------

	/**
	 * This implementation delegates to a SavepointManager for the
	 * underlying transaction, if possible.
	 * 
	 * <p> 如果可能，此实现将委托给基础事务的SavepointManager。
	 * 
	 * @see #getSavepointManager()
	 * @see org.springframework.transaction.SavepointManager
	 */
	public Object createSavepoint() throws TransactionException {
		return getSavepointManager().createSavepoint();
	}

	/**
	 * This implementation delegates to a SavepointManager for the
	 * underlying transaction, if possible.
	 * 
	 * <p> 如果可能，此实现将委托给基础事务的SavepointManager。
	 * 
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * @see #getSavepointManager()
	 * @see org.springframework.transaction.SavepointManager
	 */
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().rollbackToSavepoint(savepoint);
	}

	/**
	 * This implementation delegates to a SavepointManager for the
	 * underlying transaction, if possible.
	 * 
	 * <> 如果可能，此实现将委托给基础事务的SavepointManager。
	 * 
	 * @see #getSavepointManager()
	 * @see org.springframework.transaction.SavepointManager
	 */
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().releaseSavepoint(savepoint);
	}

	/**
	 * Return a SavepointManager for the underlying transaction, if possible.
	 * 
	 * <p> 如果可能，为基础事务返回SavepointManager。
	 * 
	 * <p>Default implementation always throws a NestedTransactionNotSupportedException.
	 * 
	 * <p> 默认实现始终抛出NestedTransactionNotSupportedException。
	 * 
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 * 
	 * <p> 如果基础事务不支持保存点
	 * 
	 */
	protected SavepointManager getSavepointManager() {
		throw new NestedTransactionNotSupportedException("This transaction does not support savepoints");
	}

}
