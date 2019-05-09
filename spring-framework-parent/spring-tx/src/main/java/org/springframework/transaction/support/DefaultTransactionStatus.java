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

/**
 * Default implementation of the {@link org.springframework.transaction.TransactionStatus}
 * interface, used by {@link AbstractPlatformTransactionManager}. Based on the concept
 * of an underlying "transaction object".
 * 
 * <p> 由AbstractPlatformTransactionManager使用的org.springframework.transaction.TransactionStatus
 * 接口的默认实现。 基于底层“交易对象”的概念。
 *
 * <p>Holds all status information that {@link AbstractPlatformTransactionManager}
 * needs internally, including a generic transaction object determined by the
 * concrete transaction manager implementation.
 * 
 * <p> 保存AbstractPlatformTransactionManager内部需要的所有状态信息，包括由具体事务管理器实现确定的通用事务对象。
 *
 * <p>Supports delegating savepoint-related methods to a transaction object
 * that implements the {@link SavepointManager} interface.
 * 
 * <p> 支持将与保存点相关的方法委派给实现SavepointManager接口的事务对象。
 *
 * <p><b>NOTE:</b> This is <i>not</i> intended for use with other PlatformTransactionManager
 * implementations, in particular not for mock transaction managers in testing environments.
 * Use the alternative {@link SimpleTransactionStatus} class or a mock for the plain
 * {@link org.springframework.transaction.TransactionStatus} interface instead.
 * 
 * <p> 注意：这不适用于其他PlatformTransactionManager实现，特别是不适用于测试环境中的模拟事务管理器。 
 * 请使用替代的SimpleTransactionStatus类或简单的org.springframework.transaction.TransactionStatus接口的模拟。
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.transaction.SavepointManager
 * @see #getTransaction
 * @see #createSavepoint
 * @see #rollbackToSavepoint
 * @see #releaseSavepoint
 * @see SimpleTransactionStatus
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus {

	private final Object transaction;

	private final boolean newTransaction;

	private final boolean newSynchronization;

	private final boolean readOnly;

	private final boolean debug;

	private final Object suspendedResources;


	/**
	 * Create a new DefaultTransactionStatus instance.
	 * 
	 * <p> 创建一个新的DefaultTransactionStatus实例。
	 * 
	 * @param transaction underlying transaction object that can hold
	 * state for the internal transaction implementation
	 * 
	 * <p> 可以为内部事务实现保存状态的基础事务对象
	 * 
	 * @param newTransaction if the transaction is new,
	 * else participating in an existing transaction
	 * 
	 * <p> 如果交易是新的，则参与现有交易
	 * 
	 * @param newSynchronization if a new transaction synchronization
	 * has been opened for the given transaction
	 * 
	 * <p> 如果已为给定事务打开新的事务同步
	 * 
	 * @param readOnly whether the transaction is read-only
	 * 
	 * <p> 交易是否为只读
	 * 
	 * @param debug should debug logging be enabled for the handling of this transaction?
	 * Caching it in here can prevent repeated calls to ask the logging system whether
	 * debug logging should be enabled.
	 * 
	 * <p> 应该启用调试日志记录来处理此事务吗？ 在此处缓存它可以防止重复调用以询问日志记录系统是否应启用调试日志记录。
	 * 
	 * @param suspendedResources a holder for resources that have been suspended
	 * for this transaction, if any
	 * 
	 * <p> 持有此交易暂停资源的持有人（如有）
	 */
	public DefaultTransactionStatus(
			Object transaction, boolean newTransaction, boolean newSynchronization,
			boolean readOnly, boolean debug, Object suspendedResources) {

		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.newSynchronization = newSynchronization;
		this.readOnly = readOnly;
		this.debug = debug;
		this.suspendedResources = suspendedResources;
	}


	/**
	 * Return the underlying transaction object.
	 * 
	 * <p> 返回底层事务对象。
	 */
	public Object getTransaction() {
		return this.transaction;
	}

	/**
	 * Return whether there is an actual transaction active.
	 * 
	 * <p> 返回是否有实际的事务处于活动状态。
	 * 
	 */
	public boolean hasTransaction() {
		return (this.transaction != null);
	}

	public boolean isNewTransaction() {
		return (hasTransaction() && this.newTransaction);
	}

	/**
	 * Return if a new transaction synchronization has been opened
	 * for this transaction.
	 * 
	 * <p> 如果已为此事务打开新的事务同步，则返回。
	 * 
	 */
	public boolean isNewSynchronization() {
		return this.newSynchronization;
	}

	/**
	 * Return if this transaction is defined as read-only transaction.
	 * 
	 * <p> 如果此事务被定义为只读事务，则返回。
	 * 
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Return whether the progress of this transaction is debugged. This is used
	 * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
	 * calls to logger.isDebug(). Not really intended for client code.
	 * 
	 * <p> 返回是否调试此事务的进度。 这被AbstractPlatformTransactionManager用作优化，
	 * 以防止重复调用logger.isDebug（）。 不是真正用于客户端代码。
	 * 
	 */
	public boolean isDebug() {
		return this.debug;
	}

	/**
	 * Return the holder for resources that have been suspended for this transaction,
	 * if any.
	 * 
	 * <p> 如果有的话，退还已暂停的资源的持有者。
	 * 
	 */
	public Object getSuspendedResources() {
		return this.suspendedResources;
	}


	//---------------------------------------------------------------------
	// Enable functionality through underlying transaction object
	// 通过底层事务对象启用功能
	//---------------------------------------------------------------------

	/**
	 * Determine the rollback-only flag via checking both the transaction object,
	 * provided that the latter implements the {@link SmartTransactionObject} interface.
	 * 
	 * <p> 通过检查事务对象来确定仅回滚标志，前提是后者实现了SmartTransactionObject接口。
	 * 
	 * <p>Will return "true" if the transaction itself has been marked rollback-only
	 * by the transaction coordinator, for example in case of a timeout.
	 * 
	 * <p> 如果事务本身已被事务协调器标记为仅回滚，则返回“true”，例如在超时的情况下。
	 * 
	 * @see SmartTransactionObject#isRollbackOnly
	 */
	@Override
	public boolean isGlobalRollbackOnly() {
		return ((this.transaction instanceof SmartTransactionObject) &&
				((SmartTransactionObject) this.transaction).isRollbackOnly());
	}

	/**
	 * Delegate the flushing to the transaction object,
	 * provided that the latter implements the {@link SmartTransactionObject} interface.
	 * 
	 * <p> 将刷新委派给事务对象，前提是后者实现了SmartTransactionObject接口。
	 * 
	 */
	@Override
	public void flush() {
		if (this.transaction instanceof SmartTransactionObject) {
			((SmartTransactionObject) this.transaction).flush();
		}
	}

	/**
	 * This implementation exposes the SavepointManager interface
	 * of the underlying transaction object, if any.
	 * 
	 * <p> 此实现公开底层事务对象的SavepointManager接口（如果有）。
	 */
	@Override
	protected SavepointManager getSavepointManager() {
		if (!isTransactionSavepointManager()) {
			throw new NestedTransactionNotSupportedException(
				"Transaction object [" + getTransaction() + "] does not support savepoints");
		}
		return (SavepointManager) getTransaction();
	}

	/**
	 * Return whether the underlying transaction implements the
	 * SavepointManager interface.
	 * 
	 * <p> 返回基础事务是否实现SavepointManager接口。
	 * 
	 * @see #getTransaction
	 * @see org.springframework.transaction.SavepointManager
	 */
	public boolean isTransactionSavepointManager() {
		return (getTransaction() instanceof SavepointManager);
	}

}
