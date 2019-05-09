/*
 * Copyright 2002-2014 the original author or authors.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Constants;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * Abstract base class that implements Spring's standard transaction workflow,
 * serving as basis for concrete platform transaction managers like
 * {@link org.springframework.transaction.jta.JtaTransactionManager}.
 * 
 * <p> 实现Spring标准事务工作流的抽象基类，作为
 * org.springframework.transaction.jta.JtaTransactionManager等具体平台事务管理器的基础。
 *
 * <p>This base class provides the following workflow handling:
 * 
 * <p> 此基类提供以下工作流处理：
 * 
 * <ul>
 * <li>determines if there is an existing transaction;
 * 
 * <li> 确定是否存在现有交易;
 * 
 * <li>applies the appropriate propagation behavior;
 * 
 * <li> 应用适当的传播行为;
 * 
 * <li>suspends and resumes transactions if necessary;
 * 
 * <li> 必要时暂停和恢复交易;
 * 
 * <li>checks the rollback-only flag on commit;
 * 
 * <li> 检查提交时的rollback-only标志;
 * 
 * <li>applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * 
 * <li> 在回滚时应用适当的修改（实际回滚或仅设置回滚）;
 * 
 * <li>triggers registered synchronization callbacks
 * (if transaction synchronization is active).
 * 
 * <li> 触发已注册的同步回调（如果事务同步处于活动状态）。
 * </ul>
 *
 * <p>Subclasses have to implement specific template methods for specific
 * states of a transaction, e.g.: begin, suspend, resume, commit, rollback.
 * The most important of them are abstract and must be provided by a concrete
 * implementation; for the rest, defaults are provided, so overriding is optional.
 * 
 * <p> 子类必须为事务的特定状态实现特定的模板方法，例如：
 * begin，suspend，resume，commit，rollback。
 * 其中最重要的是抽象的，必须由具体实施提供;对于其余部分，提供了默认值，因此覆盖是可选的。
 *
 * <p>Transaction synchronization is a generic mechanism for registering callbacks
 * that get invoked at transaction completion time. This is mainly used internally
 * by the data access support classes for JDBC, Hibernate, JPA, etc when running
 * within a JTA transaction: They register resources that are opened within the
 * transaction for closing at transaction completion time, allowing e.g. for reuse
 * of the same Hibernate Session within the transaction. The same mechanism can
 * also be leveraged for custom synchronization needs in an application.
 * 
 * <p> 事务同步是一种通用机制，用于注册在事务完成时调用的回调。当在JTA事务中运行时，这主要由
 * JDBC，Hibernate，JPA等的数据访问支持类在内部使用：它们注册在事务中打开的资源，
 * 以便在事务完成时关闭，允许例如在事务中重用相同的Hibernate会话。同样的机制也可以用于应用程序中的自定义同步需求。
 *
 * <p>The state of this class is serializable, to allow for serializing the
 * transaction strategy along with proxies that carry a transaction interceptor.
 * It is up to subclasses if they wish to make their state to be serializable too.
 * They should implement the {@code java.io.Serializable} marker interface in
 * that case, and potentially a private {@code readObject()} method (according
 * to Java serialization rules) if they need to restore any transient state.
 * 
 * <p> 此类的状态是可序列化的，以允许序列化事务策略以及携带事务拦截器的代理。如果他们希望使其状态也可序列化，则由子类决定。
 * 在这种情况下，它们应该实现java.io.Serializable标记接口，如果需要恢复任何瞬态，
 * 它们可能是私有的readObject（）方法（根据Java序列化规则）。
 *
 * @author Juergen Hoeller
 * @since 28.03.2003
 * @see #setTransactionSynchronization
 * @see TransactionSynchronizationManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
@SuppressWarnings("serial")
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager, Serializable {

	/**
	 * Always activate transaction synchronization, even for "empty" transactions
	 * that result from PROPAGATION_SUPPORTS with no existing backend transaction.
	 * 
	 * <p> 始终激活事务同步，即使对于没有现有后端事务的PROPAGATION_SUPPORTS导致的“空”事务也是如此。
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_SUPPORTS
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NOT_SUPPORTED
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NEVER
	 */
	public static final int SYNCHRONIZATION_ALWAYS = 0;

	/**
	 * Activate transaction synchronization only for actual transactions,
	 * that is, not for empty ones that result from PROPAGATION_SUPPORTS with
	 * no existing backend transaction.
	 * 
	 * <p> 仅为实际事务激活事务同步，即不是由PROPAGATION_SUPPORTS产生且没有现有后端事务的空事务。
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_MANDATORY
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
	 */
	public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;

	/**
	 * Never active transaction synchronization, not even for actual transactions.
	 * 
	 * <p> 从不激活事务同步，即使对于实际事务也不是。
	 */
	public static final int SYNCHRONIZATION_NEVER = 2;


	/** Constants instance for AbstractPlatformTransactionManager */
	/** AbstractPlatformTransactionManager的常量实例 */
	private static final Constants constants = new Constants(AbstractPlatformTransactionManager.class);


	protected transient Log logger = LogFactory.getLog(getClass());

	private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

	private int defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;

	private boolean nestedTransactionAllowed = false;

	private boolean validateExistingTransaction = false;

	private boolean globalRollbackOnParticipationFailure = true;

	private boolean failEarlyOnGlobalRollbackOnly = false;

	private boolean rollbackOnCommitFailure = false;


	/**
	 * Set the transaction synchronization by the name of the corresponding constant
	 * in this class, e.g. "SYNCHRONIZATION_ALWAYS".
	 * 
	 * <p> 通过此类中相应常量的名称设置事务同步，例如“SYNCHRONIZATION_ALWAYS”。
	 * 
	 * @param constantName name of the constant
	 * @see #SYNCHRONIZATION_ALWAYS
	 */
	public final void setTransactionSynchronizationName(String constantName) {
		setTransactionSynchronization(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set when this transaction manager should activate the thread-bound
	 * transaction synchronization support. Default is "always".
	 * 
	 * <p> 在此事务管理器应该激活线程绑定事务同步支持时设置。 默认为“始终”。
	 * 
	 * <p>Note that transaction synchronization isn't supported for
	 * multiple concurrent transactions by different transaction managers.
	 * Only one transaction manager is allowed to activate it at any time.
	 * 
	 * <p> 请注意，不同事务管理器的多个并发事务不支持事务同步。 任何时候只允许一个事务管理器激活它。
	 * 
	 * @see #SYNCHRONIZATION_ALWAYS
	 * @see #SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see #SYNCHRONIZATION_NEVER
	 * @see TransactionSynchronizationManager
	 * @see TransactionSynchronization
	 */
	public final void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Return if this transaction manager should activate the thread-bound
	 * transaction synchronization support.
	 * 
	 * <p> 如果此事务管理器应激活线程绑定事务同步支持，则返回。
	 */
	public final int getTransactionSynchronization() {
		return this.transactionSynchronization;
	}

	/**
	 * Specify the default timeout that this transaction manager should apply
	 * if there is no timeout specified at the transaction level, in seconds.
	 * 
	 * <p> 如果在事务级别未指定超时（以秒为单位），请指定此事务管理器应应用的默认超时。
	 * 
	 * <p>Default is the underlying transaction infrastructure's default timeout,
	 * e.g. typically 30 seconds in case of a JTA provider, indicated by the
	 * {@code TransactionDefinition.TIMEOUT_DEFAULT} value.
	 * 
	 * <p> 默认值是基础事务基础结构的默认超时，例如 对于JTA提供程序，通常为30秒，由
	 * TransactionDefinition.TIMEOUT_DEFAULT值指示。
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#TIMEOUT_DEFAULT
	 */
	public final void setDefaultTimeout(int defaultTimeout) {
		if (defaultTimeout < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid default timeout", defaultTimeout);
		}
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Return the default timeout that this transaction manager should apply
	 * if there is no timeout specified at the transaction level, in seconds.
	 * 
	 * <p> 如果在事务级别没有指定超时（以秒为单位），则返回此事务管理器应应用的默认超时。
	 * 
	 * <p>Returns {@code TransactionDefinition.TIMEOUT_DEFAULT} to indicate
	 * the underlying transaction infrastructure's default timeout.
	 * 
	 * <p> 返回TransactionDefinition.TIMEOUT_DEFAULT以指示基础事务基础结构的默认超时。
	 */
	public final int getDefaultTimeout() {
		return this.defaultTimeout;
	}

	/**
	 * Set whether nested transactions are allowed. Default is "false".
	 * 
	 * <p> 设置是否允许嵌套事务。 默认为“false”。
	 * 
	 * <p>Typically initialized with an appropriate default by the
	 * concrete transaction manager subclass.
	 * 
	 * <p> 通常由具体事务管理器子类以适当的缺省值初始化。
	 * 
	 */
	public final void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
		this.nestedTransactionAllowed = nestedTransactionAllowed;
	}

	/**
	 * Return whether nested transactions are allowed.
	 * 
	 * <p> 返回是否允许嵌套事务。
	 * 
	 */
	public final boolean isNestedTransactionAllowed() {
		return this.nestedTransactionAllowed;
	}

	/**
	 * Set whether existing transactions should be validated before participating
	 * in them.
	 * 
	 * <p> 设置是否应在参与之前验证现有事务。
	 * 
	 * <p>When participating in an existing transaction (e.g. with
	 * PROPAGATION_REQUIRES or PROPAGATION_SUPPORTS encountering an existing
	 * transaction), this outer transaction's characteristics will apply even
	 * to the inner transaction scope. Validation will detect incompatible
	 * isolation level and read-only settings on the inner transaction definition
	 * and reject participation accordingly through throwing a corresponding exception.
	 * 
	 * <p> 当参与现有交易时（例如，PROPAGATION_REQUIRES或PROPAGATION_SUPPORTS遇到现有交易），
	 * 此外部交易的特征甚至将应用于内部交易范围。 验证将检测内部事务定义上的不兼容隔离级别和只读设置，并通过抛出相应的异常来拒绝参与。
	 * 
	 * <p>Default is "false", leniently ignoring inner transaction settings,
	 * simply overriding them with the outer transaction's characteristics.
	 * Switch this flag to "true" in order to enforce strict validation.
	 * 
	 * <p> 默认为“false”，宽大地忽略内部事务设置，只是用外部事务的特征覆盖它们。 将此标志切换为“true”以强制执行严格的验证。
	 */
	public final void setValidateExistingTransaction(boolean validateExistingTransaction) {
		this.validateExistingTransaction = validateExistingTransaction;
	}

	/**
	 * Return whether existing transactions should be validated before participating
	 * in them.
	 * 
	 * <p> 返回现有交易是否应在参与之前进行验证。
	 * 
	 */
	public final boolean isValidateExistingTransaction() {
		return this.validateExistingTransaction;
	}

	/**
	 * Set whether to globally mark an existing transaction as rollback-only
	 * after a participating transaction failed.
	 * 
	 * <p> 设置是否在参与的事务失败后将现有事务全局标记为仅回滚。
	 * 
	 * <p>Default is "true": If a participating transaction (e.g. with
	 * PROPAGATION_REQUIRES or PROPAGATION_SUPPORTS encountering an existing
	 * transaction) fails, the transaction will be globally marked as rollback-only.
	 * The only possible outcome of such a transaction is a rollback: The
	 * transaction originator <i>cannot</i> make the transaction commit anymore.
	 * 
	 * <p> 默认值为“true”：如果参与的事务（例如，PROPAGATION_REQUIRES或PROPAGATION_SUPPORTS遇到现有事务）失败，
	 * 则事务将全局标记为仅回滚。这种事务的唯一可能结果是回滚：事务发起者不能再进行事务提交。
	 * 
	 * <p>Switch this to "false" to let the transaction originator make the rollback
	 * decision. If a participating transaction fails with an exception, the caller
	 * can still decide to continue with a different path within the transaction.
	 * However, note that this will only work as long as all participating resources
	 * are capable of continuing towards a transaction commit even after a data access
	 * failure: This is generally not the case for a Hibernate Session, for example;
	 * neither is it for a sequence of JDBC insert/update/delete operations.
	 * 
	 * <p> 将其切换为“false”以让事务发起者做出回滚决定。如果参与的事务因异常而失败，则调用者仍然可以决定在事务中继续使用不同的路径。
	 * 但是，请注意，只有当所有参与资源都能够继续进行事务提交时，即使数据访问失败，这也只会起作用：例如，Hibernate会话通常不是这种情况;
	 * 它既不是JDBC插入/更新/删除操作的序列。
	 * 
	 * <p><b>Note:</b>This flag only applies to an explicit rollback attempt for a
	 * subtransaction, typically caused by an exception thrown by a data access operation
	 * (where TransactionInterceptor will trigger a {@code PlatformTransactionManager.rollback()}
	 * call according to a rollback rule). If the flag is off, the caller can handle the exception
	 * and decide on a rollback, independent of the rollback rules of the subtransaction.
	 * This flag does, however, <i>not</i> apply to explicit {@code setRollbackOnly}
	 * calls on a {@code TransactionStatus}, which will always cause an eventual
	 * global rollback (as it might not throw an exception after the rollback-only call).
	 * 
	 * <p> 注意：此标志仅适用于子事务的显式回滚尝试，通常由数据访问操作引发的异常引起（其中TransactionInterceptor
	 * 将根据回滚规则触发PlatformTransactionManager.rollback（）调用）。如果标志关闭，则调用者可以处理异常并决定回滚，
	 * 而不依赖于子事务的回滚规则。但是，此标志不适用于TransactionStatus上的显式setRollbackOnly调用，这将始终导致最终的全局回滚
	 * （因为它可能在仅回滚调用后不会引发异常）。
	 * 
	 * <p>The recommended solution for handling failure of a subtransaction
	 * is a "nested transaction", where the global transaction can be rolled
	 * back to a savepoint taken at the beginning of the subtransaction.
	 * PROPAGATION_NESTED provides exactly those semantics; however, it will
	 * only work when nested transaction support is available. This is the case
	 * with DataSourceTransactionManager, but not with JtaTransactionManager.
	 * 
	 * <p> 处理子事务失败的推荐解决方案是“嵌套事务”，其中全局事务可以回滚到在子事务开始时获取的保存点。 
	 * PROPAGATION_NESTED提供了那些语义;但是，它仅在嵌套事务支持可用时才有效。这是DataSourceTransactionManager的情况，
	 * 但不是JtaTransactionManager。
	 * 
	 * @see #setNestedTransactionAllowed
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	public final void setGlobalRollbackOnParticipationFailure(boolean globalRollbackOnParticipationFailure) {
		this.globalRollbackOnParticipationFailure = globalRollbackOnParticipationFailure;
	}

	/**
	 * Return whether to globally mark an existing transaction as rollback-only
	 * after a participating transaction failed.
	 * 
	 * <p> 在参与的事务失败后，返回是否将现有事务全局标记为仅回滚。
	 * 
	 */
	public final boolean isGlobalRollbackOnParticipationFailure() {
		return this.globalRollbackOnParticipationFailure;
	}

	/**
	 * Set whether to fail early in case of the transaction being globally marked
	 * as rollback-only.
	 * 
	 * <p> 设置是否在事务全局标记为仅回滚的情况下提前失败。
	 * 
	 * <p>Default is "false", only causing an UnexpectedRollbackException at the
	 * outermost transaction boundary. Switch this flag on to cause an
	 * UnexpectedRollbackException as early as the global rollback-only marker
	 * has been first detected, even from within an inner transaction boundary.
	 * 
	 * <p> 默认值为“false”，仅在最外层的事务边界处导致UnexpectedRollbackException。 
	 * 早在第一次检测到全局回滚标记时，即使在内部事务边界内，也会切换此标志以导致UnexpectedRollbackException。
	 * 
	 * <p>Note that, as of Spring 2.0, the fail-early behavior for global
	 * rollback-only markers has been unified: All transaction managers will by
	 * default only cause UnexpectedRollbackException at the outermost transaction
	 * boundary. This allows, for example, to continue unit tests even after an
	 * operation failed and the transaction will never be completed. All transaction
	 * managers will only fail earlier if this flag has explicitly been set to "true".
	 * 
	 * <p> 请注意，从Spring 2.0开始，全局回滚标记的失败早期行为已统一：默认情况下，所有事务管理器仅在最外层事务边界处导
	 * 致UnexpectedRollbackException。 例如，即使在操作失败并且永远不会完成事务之后，这也允许继续单元测试。 
	 * 如果此标志已明确设置为“true”，则所有事务管理器只会提前失败。
	 * 
	 * @see org.springframework.transaction.UnexpectedRollbackException
	 */
	public final void setFailEarlyOnGlobalRollbackOnly(boolean failEarlyOnGlobalRollbackOnly) {
		this.failEarlyOnGlobalRollbackOnly = failEarlyOnGlobalRollbackOnly;
	}

	/**
	 * Return whether to fail early in case of the transaction being globally marked
	 * as rollback-only.
	 * 
	 * <p> 如果事务全局标记为仅回滚，则返回是否提前失败。
	 */
	public final boolean isFailEarlyOnGlobalRollbackOnly() {
		return this.failEarlyOnGlobalRollbackOnly;
	}

	/**
	 * Set whether {@code doRollback} should be performed on failure of the
	 * {@code doCommit} call. Typically not necessary and thus to be avoided,
	 * as it can potentially override the commit exception with a subsequent
	 * rollback exception.
	 * 
	 * <p> 设置是否应在doCommit调用失败时执行doRollback。 通常不是必需的，因此要避免，因为它可能会使用后续的回滚异常覆盖提交异常。
	 * 
	 * <p>Default is "false".
	 * 
	 * <p> 默认为“false”。
	 * 
	 * @see #doCommit
	 * @see #doRollback
	 */
	public final void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * Return whether {@code doRollback} should be performed on failure of the
	 * {@code doCommit} call.
	 * 
	 * <p> 返回是否应该在doCommit调用失败时执行doRollback。
	 * 
	 */
	public final boolean isRollbackOnCommitFailure() {
		return this.rollbackOnCommitFailure;
	}


	//---------------------------------------------------------------------
	// Implementation of PlatformTransactionManager
	// PlatformTransactionManager的实现
	//---------------------------------------------------------------------

	/**
	 * This implementation handles propagation behavior. Delegates to
	 * {@code doGetTransaction}, {@code isExistingTransaction}
	 * and {@code doBegin}.
	 * 
	 * <p> 此实现处理传播行为。 代表doGetTransaction，isExistingTransaction和doBegin。
	 * 
	 * @see #doGetTransaction
	 * @see #isExistingTransaction
	 * @see #doBegin
	 */
	public final TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		Object transaction = doGetTransaction();

		// Cache debug flag to avoid repeated checks.
		// 缓存调试标志以避免重复检查。
		boolean debugEnabled = logger.isDebugEnabled();

		if (definition == null) {
			// Use defaults if no transaction definition given.
			// 如果没有给出事务定义，则使用默认值。
			definition = new DefaultTransactionDefinition();
		}

		if (isExistingTransaction(transaction)) {
			// Existing transaction found -> check propagation behavior to find out how to behave.
			// 找到现有事务 - >检查传播行为以了解如何表现。
			return handleExistingTransaction(definition, transaction, debugEnabled);
		}

		// Check definition settings for new transaction.
		// 检查新事务的定义设置。
		if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
		}

		// No existing transaction found -> check propagation behavior to find out how to proceed.
		// 找不到现有的事务 - >检查传播行为以了解如何继续。
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			throw new IllegalTransactionStateException(
					"No existing transaction found for transaction marked with propagation 'mandatory'");
		}
		else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
			definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			SuspendedResourcesHolder suspendedResources = suspend(null);
			if (debugEnabled) {
				logger.debug("Creating new transaction with name [" + definition.getName() + "]: " + definition);
			}
			try {
				boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(
						definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
				doBegin(transaction, definition);
				prepareSynchronization(status, definition);
				return status;
			}
			catch (RuntimeException ex) {
				resume(null, suspendedResources);
				throw ex;
			}
			catch (Error err) {
				resume(null, suspendedResources);
				throw err;
			}
		}
		else {
			// Create "empty" transaction: no actual transaction, but potentially synchronization.
			// 创建“空”事务：没有实际事务，但可能是同步。
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			return prepareTransactionStatus(definition, null, true, newSynchronization, debugEnabled, null);
		}
	}

	/**
	 * Create a TransactionStatus for an existing transaction.
	 * 
	 * <p> 为现有事务创建TransactionStatus。
	 */
	private TransactionStatus handleExistingTransaction(
			TransactionDefinition definition, Object transaction, boolean debugEnabled)
			throws TransactionException {

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
			throw new IllegalTransactionStateException(
					"Existing transaction found for transaction marked with propagation 'never'");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction");
			}
			Object suspendedResources = suspend(transaction);
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			return prepareTransactionStatus(
					definition, null, false, newSynchronization, debugEnabled, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction, creating new transaction with name [" +
						definition.getName() + "]");
			}
			SuspendedResourcesHolder suspendedResources = suspend(transaction);
			try {
				boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(
						definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
				doBegin(transaction, definition);
				prepareSynchronization(status, definition);
				return status;
			}
			catch (RuntimeException beginEx) {
				resumeAfterBeginException(transaction, suspendedResources, beginEx);
				throw beginEx;
			}
			catch (Error beginErr) {
				resumeAfterBeginException(transaction, suspendedResources, beginErr);
				throw beginErr;
			}
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (!isNestedTransactionAllowed()) {
				throw new NestedTransactionNotSupportedException(
						"Transaction manager does not allow nested transactions by default - " +
						"specify 'nestedTransactionAllowed' property with value 'true'");
			}
			if (debugEnabled) {
				logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
			}
			if (useSavepointForNestedTransaction()) {
				// Create savepoint within existing Spring-managed transaction,
				// through the SavepointManager API implemented by TransactionStatus.
				// Usually uses JDBC 3.0 savepoints. Never activates Spring synchronization.
				// 通过TransactionStatus实现的SavepointManager API在现有的Spring管理的事务中创建保存点。 通常使用JDBC 3.0保存点。 
				// 永远不会激活Spring同步。
				DefaultTransactionStatus status =
						prepareTransactionStatus(definition, transaction, false, false, debugEnabled, null);
				status.createAndHoldSavepoint();
				return status;
			}
			else {
				// Nested transaction through nested begin and commit/rollback calls.
				// Usually only for JTA: Spring synchronization might get activated here
				// in case of a pre-existing JTA transaction.
				// 嵌套事务通过嵌套的begin和commit / rollback调用。 通常仅针对JTA：如果存在预先存在的JTA事务，则可能会在此处激活Spring同步。
				boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(
						definition, transaction, true, newSynchronization, debugEnabled, null);
				doBegin(transaction, definition);
				prepareSynchronization(status, definition);
				return status;
			}
		}

		// Assumably PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED.
		// 可能是PROPAGATION_SUPPORTS或PROPAGATION_REQUIRED。
		if (debugEnabled) {
			logger.debug("Participating in existing transaction");
		}
		if (isValidateExistingTransaction()) {
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
					Constants isoConstants = DefaultTransactionDefinition.constants;
					throw new IllegalTransactionStateException("Participating transaction with definition [" +
							definition + "] specifies isolation level which is incompatible with existing transaction: " +
							(currentIsolationLevel != null ?
									isoConstants.toCode(currentIsolationLevel, DefaultTransactionDefinition.PREFIX_ISOLATION) :
									"(unknown)"));
				}
			}
			if (!definition.isReadOnly()) {
				if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
					throw new IllegalTransactionStateException("Participating transaction with definition [" +
							definition + "] is not marked as read-only but existing transaction is");
				}
			}
		}
		boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
		return prepareTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
	}

	/**
	 * Create a new TransactionStatus for the given arguments,
	 * also initializing transaction synchronization as appropriate.
	 * 
	 * <p> 为给定的参数创建一个新的TransactionStatus，同时根据需要初始化事务同步。
	 * 
	 * @see #newTransactionStatus
	 * @see #prepareTransactionStatus
	 */
	protected final DefaultTransactionStatus prepareTransactionStatus(
			TransactionDefinition definition, Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean debug, Object suspendedResources) {

		DefaultTransactionStatus status = newTransactionStatus(
				definition, transaction, newTransaction, newSynchronization, debug, suspendedResources);
		prepareSynchronization(status, definition);
		return status;
	}

	/**
	 * Create a rae TransactionStatus instance for the given arguments.
	 * 
	 * <p> 为给定的参数创建一个rae TransactionStatus实例。
	 * 
	 */
	protected DefaultTransactionStatus newTransactionStatus(
			TransactionDefinition definition, Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean debug, Object suspendedResources) {

		boolean actualNewSynchronization = newSynchronization &&
				!TransactionSynchronizationManager.isSynchronizationActive();
		return new DefaultTransactionStatus(
				transaction, newTransaction, actualNewSynchronization,
				definition.isReadOnly(), debug, suspendedResources);
	}

	/**
	 * Initialize transaction synchronization as appropriate.
	 * 
	 * <p> 根据需要初始化事务同步。
	 * 
	 */
	protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
			TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
					(definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) ?
							definition.getIsolationLevel() : null);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
			TransactionSynchronizationManager.initSynchronization();
		}
	}

	/**
	 * Determine the actual timeout to use for the given definition.
	 * Will fall back to this manager's default timeout if the
	 * transaction definition doesn't specify a non-default value.
	 * 
	 * <p> 确定用于给定定义的实际超时。 如果事务定义未指定非默认值，则将回退到此管理器的默认超时。
	 * 
	 * @param definition the transaction definition - 事务定义
	 * @return the actual timeout to use
	 * 
	 * <p> 要使用的实际超时
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#getTimeout()
	 * @see #setDefaultTimeout
	 */
	protected int determineTimeout(TransactionDefinition definition) {
		if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
			return definition.getTimeout();
		}
		return this.defaultTimeout;
	}


	/**
	 * Suspend the given transaction. Suspends transaction synchronization first,
	 * then delegates to the {@code doSuspend} template method.
	 * 
	 * <p> 暂停给定的事务。 首先暂停事务同步，然后委托给doSuspend模板方法。
	 * 
	 * @param transaction the current transaction object
	 * (or {@code null} to just suspend active synchronizations, if any)
	 * 
	 * <p> 当前事务对象（或者只是挂起活动同步的null，如果有的话）
	 * 
	 * @return an object that holds suspended resources
	 * (or {@code null} if neither transaction nor synchronization active)
	 * 
	 * <p> 保存挂起资源的对象（如果事务和同步都不活动，则为null）
	 * 
	 * @see #doSuspend
	 * @see #resume
	 */
	protected final SuspendedResourcesHolder suspend(Object transaction) throws TransactionException {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();
			try {
				Object suspendedResources = null;
				if (transaction != null) {
					suspendedResources = doSuspend(transaction);
				}
				String name = TransactionSynchronizationManager.getCurrentTransactionName();
				TransactionSynchronizationManager.setCurrentTransactionName(null);
				boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
				Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
				boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
				TransactionSynchronizationManager.setActualTransactionActive(false);
				return new SuspendedResourcesHolder(
						suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive);
			}
			catch (RuntimeException ex) {
				// doSuspend failed - original transaction is still active...
				// doSuspend失败 - 原始交易仍然有效......
				doResumeSynchronization(suspendedSynchronizations);
				throw ex;
			}
			catch (Error err) {
				// doSuspend failed - original transaction is still active...
				// doSuspend失败 - 原始交易仍然有效......
				doResumeSynchronization(suspendedSynchronizations);
				throw err;
			}
		}
		else if (transaction != null) {
			// Transaction active but no synchronization active.
			// 事务处于活动状态但未激活同步
			Object suspendedResources = doSuspend(transaction);
			return new SuspendedResourcesHolder(suspendedResources);
		}
		else {
			// Neither transaction nor synchronization active.
			// 事务和同步都不活动。
			return null;
		}
	}

	/**
	 * Resume the given transaction. Delegates to the {@code doResume}
	 * template method first, then resuming transaction synchronization.
	 * 
	 * <p> 恢复给定的交易。 首先委托doResume模板方法，然后恢复事务同步。
	 * 
	 * @param transaction the current transaction object
	 * 
	 * <p> 当前的事务对象
	 * 
	 * @param resourcesHolder the object that holds suspended resources,
	 * as returned by {@code suspend} (or {@code null} to just
	 * resume synchronizations, if any)
	 * 
	 * <p> 保存挂起资源的对象，由suspend返回（或者为只恢复同步，如果有的话）
	 * 
	 * @see #doResume
	 * @see #suspend
	 */
	protected final void resume(Object transaction, SuspendedResourcesHolder resourcesHolder)
			throws TransactionException {

		if (resourcesHolder != null) {
			Object suspendedResources = resourcesHolder.suspendedResources;
			if (suspendedResources != null) {
				doResume(transaction, suspendedResources);
			}
			List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
			if (suspendedSynchronizations != null) {
				TransactionSynchronizationManager.setActualTransactionActive(resourcesHolder.wasActive);
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(resourcesHolder.isolationLevel);
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.readOnly);
				TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.name);
				doResumeSynchronization(suspendedSynchronizations);
			}
		}
	}

	/**
	 * Resume outer transaction after inner transaction begin failed.
	 * 
	 * <p> 内部事务开始失败后恢复外部事务。
	 * 
	 */
	private void resumeAfterBeginException(
			Object transaction, SuspendedResourcesHolder suspendedResources, Throwable beginEx) {

		String exMessage = "Inner transaction begin exception overridden by outer transaction resume exception";
		try {
			resume(transaction, suspendedResources);
		}
		catch (RuntimeException resumeEx) {
			logger.error(exMessage, beginEx);
			throw resumeEx;
		}
		catch (Error resumeErr) {
			logger.error(exMessage, beginEx);
			throw resumeErr;
		}
	}

	/**
	 * Suspend all current synchronizations and deactivate transaction
	 * synchronization for the current thread.
	 * 
	 * <p> 挂起所有当前同步并停用当前线程的事务同步。
	 * 
	 * @return the List of suspended TransactionSynchronization objects
	 * 
	 * <p> 已挂起的TransactionSynchronization对象的列表
	 * 
	 */
	private List<TransactionSynchronization> doSuspendSynchronization() {
		List<TransactionSynchronization> suspendedSynchronizations =
				TransactionSynchronizationManager.getSynchronizations();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.suspend();
		}
		TransactionSynchronizationManager.clearSynchronization();
		return suspendedSynchronizations;
	}

	/**
	 * Reactivate transaction synchronization for the current thread
	 * and resume all given synchronizations.
	 * 
	 * <p> 重新激活当前线程的事务同步并恢复所有给定的同步。
	 * 
	 * @param suspendedSynchronizations List of TransactionSynchronization objects
	 * 
	 * <p> TransactionSynchronization对象列表
	 * 
	 */
	private void doResumeSynchronization(List<TransactionSynchronization> suspendedSynchronizations) {
		TransactionSynchronizationManager.initSynchronization();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.resume();
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		}
	}


	/**
	 * This implementation of commit handles participating in existing
	 * transactions and programmatic rollback requests.
	 * Delegates to {@code isRollbackOnly}, {@code doCommit}
	 * and {@code rollback}.
	 * 
	 * <p> 此提交实现处理参与现有事务和编程回滚请求。 代表isRollbackOnly，doCommit和rollback。
	 * 
	 * @see org.springframework.transaction.TransactionStatus#isRollbackOnly()
	 * @see #doCommit
	 * @see #rollback
	 */
	public final void commit(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) {
			throw new IllegalTransactionStateException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}

		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		if (defStatus.isLocalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Transactional code has requested rollback");
			}
			processRollback(defStatus);
			return;
		}
		if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Global transaction is marked as rollback-only but transactional code requested commit");
			}
			processRollback(defStatus);
			// Throw UnexpectedRollbackException only at outermost transaction boundary
			// or if explicitly asked to.
			// 仅在最外面的事务边界或明确要求时抛出UnexpectedRollbackException。
			if (status.isNewTransaction() || isFailEarlyOnGlobalRollbackOnly()) {
				throw new UnexpectedRollbackException(
						"Transaction rolled back because it has been marked as rollback-only");
			}
			return;
		}

		processCommit(defStatus);
	}

	/**
	 * Process an actual commit.
	 * Rollback-only flags have already been checked and applied.
	 * 
	 * <p> 处理实际提交。 仅回滚标志已被检查并应用。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 * @throws TransactionException in case of commit failure
	 * 
	 * <p> 在提交失败的情况下
	 * 
	 */
	private void processCommit(DefaultTransactionStatus status) throws TransactionException {
		try {
			boolean beforeCompletionInvoked = false;
			try {
				prepareForCommit(status);
				triggerBeforeCommit(status);
				triggerBeforeCompletion(status);
				beforeCompletionInvoked = true;
				boolean globalRollbackOnly = false;
				if (status.isNewTransaction() || isFailEarlyOnGlobalRollbackOnly()) {
					globalRollbackOnly = status.isGlobalRollbackOnly();
				}
				if (status.hasSavepoint()) {
					if (status.isDebug()) {
						logger.debug("Releasing transaction savepoint");
					}
					status.releaseHeldSavepoint();
				}
				else if (status.isNewTransaction()) {
					if (status.isDebug()) {
						logger.debug("Initiating transaction commit");
					}
					doCommit(status);
				}
				// Throw UnexpectedRollbackException if we have a global rollback-only
				// marker but still didn't get a corresponding exception from commit.
				
				// 如果我们有一个全局回滚专用标记，但仍未从提交中获得相应的异常，则抛出UnexpectedRollbackException。
				if (globalRollbackOnly) {
					throw new UnexpectedRollbackException(
							"Transaction silently rolled back because it has been marked as rollback-only");
				}
			}
			catch (UnexpectedRollbackException ex) {
				// can only be caused by doCommit
				// 只能由doCommit引起
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
				throw ex;
			}
			catch (TransactionException ex) {
				// can only be caused by doCommit
				// 只能由doCommit引起
				if (isRollbackOnCommitFailure()) {
					doRollbackOnCommitException(status, ex);
				}
				else {
					triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				}
				throw ex;
			}
			catch (RuntimeException ex) {
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(status);
				}
				doRollbackOnCommitException(status, ex);
				throw ex;
			}
			catch (Error err) {
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(status);
				}
				doRollbackOnCommitException(status, err);
				throw err;
			}

			// Trigger afterCommit callbacks, with an exception thrown there
			// propagated to callers but the transaction still considered as committed.
			// 触发afterCommit回调，抛出异常传播给调用者，但事务仍被视为已提交。
			try {
				triggerAfterCommit(status);
			}
			finally {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
			}

		}
		finally {
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * This implementation of rollback handles participating in existing
	 * transactions. Delegates to {@code doRollback} and
	 * {@code doSetRollbackOnly}.
	 * 
	 * <p> 此实现的回滚处理参与现有事务。 代表doRollback和doSetRollbackOnly。
	 * 
	 * @see #doRollback
	 * @see #doSetRollbackOnly
	 */
	public final void rollback(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) {
			throw new IllegalTransactionStateException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}

		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		processRollback(defStatus);
	}

	/**
	 * Process an actual rollback.
	 * The completed flag has already been checked.
	 * 
	 * <p> 处理实际的回滚。 已完成标志已经过检查。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 * @throws TransactionException in case of rollback failure
	 * 
	 * <p> 在回滚失败的情况下
	 * 
	 */
	private void processRollback(DefaultTransactionStatus status) {
		try {
			try {
				triggerBeforeCompletion(status);
				if (status.hasSavepoint()) {
					if (status.isDebug()) {
						logger.debug("Rolling back transaction to savepoint");
					}
					status.rollbackToHeldSavepoint();
				}
				else if (status.isNewTransaction()) {
					if (status.isDebug()) {
						logger.debug("Initiating transaction rollback");
					}
					doRollback(status);
				}
				else if (status.hasTransaction()) {
					if (status.isLocalRollbackOnly() || isGlobalRollbackOnParticipationFailure()) {
						if (status.isDebug()) {
							logger.debug("Participating transaction failed - marking existing transaction as rollback-only");
						}
						doSetRollbackOnly(status);
					}
					else {
						if (status.isDebug()) {
							logger.debug("Participating transaction failed - letting transaction originator decide on rollback");
						}
					}
				}
				else {
					logger.debug("Should roll back transaction but cannot - no transaction available");
				}
			}
			catch (RuntimeException ex) {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				throw ex;
			}
			catch (Error err) {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				throw err;
			}
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
		}
		finally {
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * Invoke {@code doRollback}, handling rollback exceptions properly.
	 * 
	 * <p> 调用doRollback，正确处理回滚异常。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 * @param ex the thrown application exception or error
	 * 
	 * <p> 抛出的应用程序异常或错误
	 * 
	 * @throws TransactionException in case of rollback failure
	 * 
	 * <p> 在回滚失败的情况下
	 * 
	 * @see #doRollback
	 */
	private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex) throws TransactionException {
		try {
			if (status.isNewTransaction()) {
				if (status.isDebug()) {
					logger.debug("Initiating transaction rollback after commit exception", ex);
				}
				doRollback(status);
			}
			else if (status.hasTransaction() && isGlobalRollbackOnParticipationFailure()) {
				if (status.isDebug()) {
					logger.debug("Marking existing transaction as rollback-only after commit exception", ex);
				}
				doSetRollbackOnly(status);
			}
		}
		catch (RuntimeException rbex) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
			throw rbex;
		}
		catch (Error rberr) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
			throw rberr;
		}
		triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
	}


	/**
	 * Trigger {@code beforeCommit} callbacks.
	 * 
	 * <p> 触发beforeCommit回调。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 */
	protected final void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			if (status.isDebug()) {
				logger.trace("Triggering beforeCommit synchronization");
			}
			TransactionSynchronizationUtils.triggerBeforeCommit(status.isReadOnly());
		}
	}

	/**
	 * Trigger {@code beforeCompletion} callbacks.
	 * 
	 * <p> 触发beforeCompletion回调。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 */
	protected final void triggerBeforeCompletion(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			if (status.isDebug()) {
				logger.trace("Triggering beforeCompletion synchronization");
			}
			TransactionSynchronizationUtils.triggerBeforeCompletion();
		}
	}

	/**
	 * Trigger {@code afterCommit} callbacks.
	 * 
	 * <p> 触发afterCommit回调。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 */
	private void triggerAfterCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			if (status.isDebug()) {
				logger.trace("Triggering afterCommit synchronization");
			}
			TransactionSynchronizationUtils.triggerAfterCommit();
		}
	}

	/**
	 * Trigger {@code afterCompletion} callbacks.
	 * 
	 * <p> 触发afterCompletion回调。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 * @param completionStatus completion status according to TransactionSynchronization constants
	 * 
	 * <p> 根据TransactionSynchronization常量完成状态
	 * 
	 */
	private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus) {
		if (status.isNewSynchronization()) {
			List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			if (!status.hasTransaction() || status.isNewTransaction()) {
				if (status.isDebug()) {
					logger.trace("Triggering afterCompletion synchronization");
				}
				// No transaction or new transaction for the current scope ->
				// invoke the afterCompletion callbacks immediately
				// 当前作用域没有事务或新事务 - >立即调用afterCompletion回调
				invokeAfterCompletion(synchronizations, completionStatus);
			}
			else if (!synchronizations.isEmpty()) {
				// Existing transaction that we participate in, controlled outside
				// of the scope of this Spring transaction manager -> try to register
				// an afterCompletion callback with the existing (JTA) transaction.
				// 我们参与的现有事务，在Spring事务管理器范围之外控制 - >尝试使用现有（JTA）事务注册afterCompletion回调。
				registerAfterCompletionWithExistingTransaction(status.getTransaction(), synchronizations);
			}
		}
	}

	/**
	 * Actually invoke the {@code afterCompletion} methods of the
	 * given Spring TransactionSynchronization objects.
	 * 
	 * <p> 实际上调用给定的Spring TransactionSynchronization对象的afterCompletion方法。
	 * 
	 * <p>To be called by this abstract manager itself, or by special implementations
	 * of the {@code registerAfterCompletionWithExistingTransaction} callback.
	 * 
	 * <p> 由这个抽象管理器本身调用，或者由registerAfterCompletionWithExistingTransaction回调的特殊实现调用。
	 * 
	 * @param synchronizations List of TransactionSynchronization objects
	 * 
	 * <p> TransactionSynchronization对象列表
	 * 
	 * @param completionStatus the completion status according to the
	 * constants in the TransactionSynchronization interface
	 * 
	 * <p> 根据TransactionSynchronization接口中的常量完成状态
	 * 
	 * @see #registerAfterCompletionWithExistingTransaction(Object, java.util.List)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	protected final void invokeAfterCompletion(List<TransactionSynchronization> synchronizations, int completionStatus) {
		TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, completionStatus);
	}

	/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * 
	 * <p> 完成后清理，必要时清除同步，并调用doCleanupAfterCompletion。
	 * 
	 * @param status object representing the transaction
	 * 
	 * <p> 表示事务的对象
	 * 
	 * @see #doCleanupAfterCompletion
	 */
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		status.setCompleted();
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clear();
		}
		if (status.isNewTransaction()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction after completion of inner transaction");
			}
			resume(status.getTransaction(), (SuspendedResourcesHolder) status.getSuspendedResources());
		}
	}


	//---------------------------------------------------------------------
	// Template methods to be implemented in subclasses
	// 要在子类中实现的模板方法
	//---------------------------------------------------------------------

	/**
	 * Return a transaction object for the current transaction state.
	 * 
	 * <p> 返回当前事务状态的事务对象。
	 * 
	 * <p>The returned object will usually be specific to the concrete transaction
	 * manager implementation, carrying corresponding transaction state in a
	 * modifiable fashion. This object will be passed into the other template
	 * methods (e.g. doBegin and doCommit), either directly or as part of a
	 * DefaultTransactionStatus instance.
	 * 
	 * <p> 返回的对象通常特定于具体的事务管理器实现，以可修改的方式携带相应的事务状态。 此对象将直接或作为
	 * DefaultTransactionStatus实例的一部分传递到其他模板方法（例如doBegin和doCommit）。
	 * 
	 * <p>The returned object should contain information about any existing
	 * transaction, that is, a transaction that has already started before the
	 * current {@code getTransaction} call on the transaction manager.
	 * Consequently, a {@code doGetTransaction} implementation will usually
	 * look for an existing transaction and store corresponding state in the
	 * returned transaction object.
	 * 
	 * <p> 返回的对象应包含有关任何现有事务的信息，即在事务管理器上当前的getTransaction调用之前已经启动的事务。 
	 * 因此，doGetTransaction实现通常会查找现有事务并在返回的事务对象中存储相应的状态。
	 * 
	 * @return the current transaction object
	 * 
	 * <p> 当前的事务对象
	 * 
	 * @throws org.springframework.transaction.CannotCreateTransactionException
	 * if transaction support is not available
	 * 
	 * <p> 如果没有交易支持
	 * 
	 * @throws TransactionException in case of lookup or system errors
	 * 
	 * <p> 在查找或系统错误的情况下
	 * 
	 * @see #doBegin
	 * @see #doCommit
	 * @see #doRollback
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * Check if the given transaction object indicates an existing transaction
	 * (that is, a transaction which has already started).
	 * 
	 * <p> 返回当前事务状态的事务对象。
	 * 
	 * <p>The result will be evaluated according to the specified propagation
	 * behavior for the new transaction. An existing transaction might get
	 * suspended (in case of PROPAGATION_REQUIRES_NEW), or the new transaction
	 * might participate in the existing one (in case of PROPAGATION_REQUIRED).
	 * 
	 * <p> 返回的对象通常特定于具体的事务管理器实现，以可修改的方式携带相应的事务状态。 此对象将直接或作为
	 * DefaultTransactionStatus实例的一部分传递到其他模板方法（例如doBegin和doCommit）。
	 * 
	 * <p>The default implementation returns {@code false}, assuming that
	 * participating in existing transactions is generally not supported.
	 * Subclasses are of course encouraged to provide such support.
	 * 
	 * <p> 返回的对象应包含有关任何现有事务的信息，即在事务管理器上当前的getTransaction调用之前已经启动的事务。 
	 * 因此，doGetTransaction实现通常会查找现有事务并在返回的事务对象中存储相应的状态。
	 * 
	 * @param transaction transaction object returned by doGetTransaction
	 * 
	 * <p> doGetTransaction返回的事务对象
	 * 
	 * @return if there is an existing transaction
	 * 
	 * <p> 如果有现有事务
	 * 
	 * @throws TransactionException in case of system errors
	 * 
	 * <p> 在系统错误的情况下
	 * 
	 * @see #doGetTransaction
	 */
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * Return whether to use a savepoint for a nested transaction.
	 * 
	 * <p> 返回是否对嵌套事务使用保存点。
	 * 
	 * <p>Default is {@code true}, which causes delegation to DefaultTransactionStatus
	 * for creating and holding a savepoint. If the transaction object does not implement
	 * the SavepointManager interface, a NestedTransactionNotSupportedException will be
	 * thrown. Else, the SavepointManager will be asked to create a new savepoint to
	 * demarcate the start of the nested transaction.
	 * 
	 * <p> 默认值为true，这会导致委派DefaultTransactionStatus以创建和保存保存点。 如果事务对象未实现SavepointManager接口，
	 * 则将抛出NestedTransactionNotSupportedException。 否则，将要求SavepointManager创建一个新的保存点来划分嵌套事务的开始。
	 * 
	 * <p>Subclasses can override this to return {@code false}, causing a further
	 * call to {@code doBegin} - within the context of an already existing transaction.
	 * The {@code doBegin} implementation needs to handle this accordingly in such
	 * a scenario. This is appropriate for JTA, for example.
	 * 
	 * <p> 子类可以覆盖它以返回false，从而导致对doBegin的进一步调用 - 在已存在的事务的上下文中。 在这种情况下，doBegin实现需要相应地处理它。 
	 * 例如，这适用于JTA。
	 * 
	 * @see DefaultTransactionStatus#createAndHoldSavepoint
	 * @see DefaultTransactionStatus#rollbackToHeldSavepoint
	 * @see DefaultTransactionStatus#releaseHeldSavepoint
	 * @see #doBegin
	 */
	protected boolean useSavepointForNestedTransaction() {
		return true;
	}

	/**
	 * Begin a new transaction with semantics according to the given transaction
	 * definition. Does not have to care about applying the propagation behavior,
	 * as this has already been handled by this abstract manager.
	 * 
	 * <p> 根据给定的事务定义开始具有语义的新事务。 不必关心应用传播行为，因为这已经由此抽象管理器处理。
	 * 
	 * <p>This method gets called when the transaction manager has decided to actually
	 * start a new transaction. Either there wasn't any transaction before, or the
	 * previous transaction has been suspended.
	 * 
	 * <p> 当事务管理器决定实际启动新事务时，将调用此方法。 之前没有任何交易，或者之前的交易已被暂停。
	 * 
	 * <p>A special scenario is a nested transaction without savepoint: If
	 * {@code useSavepointForNestedTransaction()} returns "false", this method
	 * will be called to start a nested transaction when necessary. In such a context,
	 * there will be an active transaction: The implementation of this method has
	 * to detect this and start an appropriate nested transaction.
	 * 
	 * <p> 特殊情况是没有保存点的嵌套事务：如果useSavepointForNestedTransaction（）返回“false”，
	 * 则将调用此方法以在必要时启动嵌套事务。 在这样的上下文中，将存在活动事务：此方法的实现必须检测此并启动适当的嵌套事务。
	 * 
	 * @param transaction transaction object returned by {@code doGetTransaction}
	 * 
	 * <p> doGetTransaction返回的事务对象
	 * 
	 * @param definition TransactionDefinition instance, describing propagation
	 * behavior, isolation level, read-only flag, timeout, and transaction name
	 * 
	 * <p> TransactionDefinition实例，描述传播行为，隔离级别，只读标志，超时和事务名称
	 * 
	 * @throws TransactionException in case of creation or system errors
	 * 
	 * <p> 在创建或系统错误的情况下
	 */
	protected abstract void doBegin(Object transaction, TransactionDefinition definition)
			throws TransactionException;

	/**
	 * Suspend the resources of the current transaction.
	 * Transaction synchronization will already have been suspended.
	 * 
	 * <p> 暂停当前事务的资源。 事务同步已经暂停。
	 * 
	 * <p>The default implementation throws a TransactionSuspensionNotSupportedException,
	 * assuming that transaction suspension is generally not supported.
	 * 
	 * <p> 假设通常不支持事务挂起，默认实现会抛出TransactionSuspensionNotSupportedException。
	 * 
	 * @param transaction transaction object returned by {@code doGetTransaction}
	 * 
	 * <p> doGetTransaction返回的事务对象
	 * 
	 * @return an object that holds suspended resources
	 * (will be kept unexamined for passing it into doResume)
	 * 
	 * <p> 保存暂停资源的对象（将被保留为未经审查以将其传递给doResume）
	 * 
	 * @throws org.springframework.transaction.TransactionSuspensionNotSupportedException
	 * if suspending is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * 
	 * <p> 如果事务管理器不支持suspending implementationTransactionException  - 如果系统错误
	 * 
	 * @see #doResume
	 */
	protected Object doSuspend(Object transaction) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * Resume the resources of the current transaction.
	 * Transaction synchronization will be resumed afterwards.
	 * 
	 * <p> 恢复当前事务的资源。 事务同步将在之后恢复。
	 * 
	 * <p>The default implementation throws a TransactionSuspensionNotSupportedException,
	 * assuming that transaction suspension is generally not supported.
	 * 
	 * <p> 假设通常不支持事务挂起，默认实现会抛出TransactionSuspensionNotSupportedException。
	 * 
	 * @param transaction transaction object returned by {@code doGetTransaction}
	 * 
	 * <p> doGetTransaction返回的事务对象
	 * 
	 * @param suspendedResources the object that holds suspended resources,
	 * as returned by doSuspend
	 * 
	 * <p> 保存挂起资源的对象，由doSuspend返回
	 * 
	 * @throws org.springframework.transaction.TransactionSuspensionNotSupportedException
	 * if resuming is not supported by the transaction manager implementation
	 * 
	 * <p> 如果事务管理器implementationTransactionException不支持恢复 
	 * 
	 * @throws TransactionException in case of system errors - 如果系统错误
	 * 
	 * @see #doSuspend
	 */
	protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * Return whether to call {@code doCommit} on a transaction that has been
	 * marked as rollback-only in a global fashion.
	 * 
	 * <p> 返回是否在以全局方式标记为仅回滚的事务上调用doCommit。
	 * 
	 * <p>Does not apply if an application locally sets the transaction to rollback-only
	 * via the TransactionStatus, but only to the transaction itself being marked as
	 * rollback-only by the transaction coordinator.
	 * 
	 * <p> 如果应用程序本地将事务设置为仅通过TransactionStatus回滚，则不适用，但仅适用于事务本身被事务协调器标记为仅回滚的事务。
	 * 
	 * <p>Default is "false": Local transaction strategies usually don't hold the rollback-only
	 * marker in the transaction itself, therefore they can't handle rollback-only transactions
	 * as part of transaction commit. Hence, AbstractPlatformTransactionManager will trigger
	 * a rollback in that case, throwing an UnexpectedRollbackException afterwards.
	 * 
	 * <p> 默认值为“false”：本地事务策略通常不在事务本身中保留仅回滚标记，因此它们不能处理仅回滚事务作为事务提交的一部分。
	 * 因此，在这种情况下，AbstractPlatformTransactionManager将触发回滚，之后抛出UnexpectedRollbackException。
	 * 
	 * <p>Override this to return "true" if the concrete transaction manager expects a
	 * {@code doCommit} call even for a rollback-only transaction, allowing for
	 * special handling there. This will, for example, be the case for JTA, where
	 * {@code UserTransaction.commit} will check the read-only flag itself and
	 * throw a corresponding RollbackException, which might include the specific reason
	 * (such as a transaction timeout).
	 * 
	 * <p> 如果具体的事务管理器期望doCommit调用甚至是仅回滚事务，则覆盖它以返回“true”，允许在那里进行特殊处理。例如，
	 * 这将是JTA的情况，其中UserTransaction.commit将检查只读标志本身并抛出相应的RollbackException，
	 * 这可能包括特定原因（例如事务超时）。
	 * 
	 * <p>If this method returns "true" but the {@code doCommit} implementation does not
	 * throw an exception, this transaction manager will throw an UnexpectedRollbackException
	 * itself. This should not be the typical case; it is mainly checked to cover misbehaving
	 * JTA providers that silently roll back even when the rollback has not been requested
	 * by the calling code.
	 * 
	 * <p> 如果此方法返回“true”但doCommit实现不引发异常，则此事务管理器将抛出UnexpectedRollbackException本身。
	 * 这不应该是典型的情况;主要检查是否覆盖行为不端的JTA提供程序，即使调用代码未请求回滚，也会静默回滚。
	 * 
	 * @see #doCommit
	 * @see DefaultTransactionStatus#isGlobalRollbackOnly()
	 * @see DefaultTransactionStatus#isLocalRollbackOnly()
	 * @see org.springframework.transaction.TransactionStatus#setRollbackOnly()
	 * @see org.springframework.transaction.UnexpectedRollbackException
	 * @see javax.transaction.UserTransaction#commit()
	 * @see javax.transaction.RollbackException
	 */
	protected boolean shouldCommitOnGlobalRollbackOnly() {
		return false;
	}

	/**
	 * Make preparations for commit, to be performed before the
	 * {@code beforeCommit} synchronization callbacks occur.
	 * 
	 * <p> 准备提交，在beforeCommit同步回调发生之前执行。
	 * 
	 * <p>Note that exceptions will get propagated to the commit caller
	 * and cause a rollback of the transaction.
	 * 
	 * <p> 请注意，异常将传播到提交调用方并导致事务回滚。
	 * 
	 * @param status the status representation of the transaction
	 * 
	 * <p> 事务的状态表示
	 * 
	 * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>
	 * (note: do not throw TransactionException subclasses here!)
	 * 
	 * <p> 如果有错误; 将传播给调用者（注意：不要在这里抛出TransactionException子类！）
	 * 
	 */
	protected void prepareForCommit(DefaultTransactionStatus status) {
	}

	/**
	 * Perform an actual commit of the given transaction.
	 * 
	 * <p> 执行给定事务的实际提交。
	 * 
	 * <p>An implementation does not need to check the "new transaction" flag
	 * or the rollback-only flag; this will already have been handled before.
	 * Usually, a straight commit will be performed on the transaction object
	 * contained in the passed-in status.
	 * 
	 * <p> 实现不需要检查“new transaction”标志或仅回滚标志; 这已经在以前处理过了。 
	 * 通常，将对传入状态中包含的事务对象执行直接提交。
	 * 
	 * @param status the status representation of the transaction
	 * 
	 * <p> 事务的状态表示
	 * 
	 * @throws TransactionException in case of commit or system errors
	 * 
	 * <p> 在提交或系统错误的情况下
	 * 
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Perform an actual rollback of the given transaction.
	 * 
	 * <p> 执行给定事务的实际回滚。
	 * 
	 * <p>An implementation does not need to check the "new transaction" flag;
	 * this will already have been handled before. Usually, a straight rollback
	 * will be performed on the transaction object contained in the passed-in status.
	 * 
	 * <p> 实现不需要检查“新事务”标志; 这已经在以前处理过了。 通常，将对传入状态中包含的事务对象执行直接回滚。
	 * 
	 * @param status the status representation of the transaction
	 * 
	 * <p> 事务的状态表示
	 * 
	 * @throws TransactionException in case of system errors
	 * 
	 * <p> 在系统错误的情况下
	 * 
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Set the given transaction rollback-only. Only called on rollback
	 * if the current transaction participates in an existing one.
	 * 
	 * <p> 设置给定的事务仅回滚。 仅在当前事务参与现有事务时才调用回滚。
	 * 
	 * <p>The default implementation throws an IllegalTransactionStateException,
	 * assuming that participating in existing transactions is generally not
	 * supported. Subclasses are of course encouraged to provide such support.
	 * 
	 * <p> 默认实现抛出IllegalTransactionStateException，假设通常不支持参与现有事务。 当然鼓励子类提供这样的支持。


	 * @param status the status representation of the transaction
	 * 
	 * <p> 事务的状态表示
	 * 
	 * @throws TransactionException in case of system errors
	 * 
	 * <p> 在系统错误的情况下
	 */
	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
		throw new IllegalTransactionStateException(
				"Participating in existing transactions is not supported - when 'isExistingTransaction' " +
				"returns true, appropriate 'doSetRollbackOnly' behavior must be provided");
	}

	/**
	 * Register the given list of transaction synchronizations with the existing transaction.
	 * 
	 * <p> 使用现有事务注册给定的事务同步列表。
	 * 
	 * <p>Invoked when the control of the Spring transaction manager and thus all Spring
	 * transaction synchronizations end, without the transaction being completed yet. This
	 * is for example the case when participating in an existing JTA or EJB CMT transaction.
	 * 
	 * <p> 当Spring事务管理器的控件因此所有Spring事务同步结束时调用，而事务尚未完成。 例如，这是参与现有JTA或EJB CMT事务的情况。
	 * 
	 * <p>The default implementation simply invokes the {@code afterCompletion} methods
	 * immediately, passing in "STATUS_UNKNOWN". This is the best we can do if there's no
	 * chance to determine the actual outcome of the outer transaction.
	 * 
	 * <p> 默认实现只是立即调用afterCompletion方法，传入“STATUS_UNKNOWN”。 如果没有机会确定外部交易的实际结果，这是我们能做的最好的事情。
	 * 
	 * @param transaction transaction object returned by {@code doGetTransaction}
	 * 
	 * <p> doGetTransaction返回的事务对象
	 * 
	 * @param synchronizations List of TransactionSynchronization objects
	 * 
	 * <p> TransactionSynchronization对象列表
	 * 
	 * @throws TransactionException in case of system errors
	 * 
	 * <p> 在系统错误的情况下
	 * 
	 * @see #invokeAfterCompletion(java.util.List, int)
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	protected void registerAfterCompletionWithExistingTransaction(
			Object transaction, List<TransactionSynchronization> synchronizations) throws TransactionException {

		logger.debug("Cannot register Spring after-completion synchronization with existing transaction - " +
				"processing Spring after-completion callbacks immediately, with outcome status 'unknown'");
		invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
	}

	/**
	 * Cleanup resources after transaction completion.
	 * 
	 * <p> 事务完成后清理资源。
	 * 
	 * <p>Called after {@code doCommit} and {@code doRollback} execution,
	 * on any outcome. The default implementation does nothing.
	 * 
	 * <p> 在doCommit和doRollback执行后调用任何结果。 默认实现什么都不做。
	 * 
	 * <p>Should not throw any exceptions but just issue warnings on errors.
	 * 
	 * <p> 不应抛出任何异常，只是发出错误警告。
	 * 
	 * @param transaction transaction object returned by {@code doGetTransaction}
	 * 
	 * <p> doGetTransaction返回的事务对象
	 * 
	 */
	protected void doCleanupAfterCompletion(Object transaction) {
	}


	//---------------------------------------------------------------------
	// Serialization support
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		// 依靠默认序列化; 只是在反序列化后初始化状态。
		ois.defaultReadObject();

		// Initialize transient fields.
		// 初始化瞬态字段。
		this.logger = LogFactory.getLog(getClass());
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by {@code suspend} and {@code resume}.
	 * 
	 * <p> 暂停资源的持有人。 暂停和恢复内部使用。
	 */
	protected static class SuspendedResourcesHolder {

		private final Object suspendedResources;

		private List<TransactionSynchronization> suspendedSynchronizations;

		private String name;

		private boolean readOnly;

		private Integer isolationLevel;

		private boolean wasActive;

		private SuspendedResourcesHolder(Object suspendedResources) {
			this.suspendedResources = suspendedResources;
		}

		private SuspendedResourcesHolder(
				Object suspendedResources, List<TransactionSynchronization> suspendedSynchronizations,
				String name, boolean readOnly, Integer isolationLevel, boolean wasActive) {
			this.suspendedResources = suspendedResources;
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.name = name;
			this.readOnly = readOnly;
			this.isolationLevel = isolationLevel;
			this.wasActive = wasActive;
		}
	}

}
