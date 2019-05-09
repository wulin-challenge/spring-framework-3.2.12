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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.NamedThreadLocal;
import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

/**
 * Central delegate that manages resources and transaction synchronizations per thread.
 * To be used by resource management code but not by typical application code.
 * 
 * <p> 管理每个线程的资源和事务同步的中央委托。由资源管理代码使用，但不是由典型的应用程序代码使用。
 *
 * <p>Supports one resource per key without overwriting, that is, a resource needs
 * to be removed before a new one can be set for the same key.
 * Supports a list of transaction synchronizations if synchronization is active.
 * 
 * <p> 每个密钥支持一个资源而不覆盖，也就是说，在为同一个密钥设置新资源之前需要删除资源。如果同步处于活动状态，则支持事务同步列表。
 *
 * <p>Resource management code should check for thread-bound resources, e.g. JDBC
 * Connections or Hibernate Sessions, via {@code getResource}. Such code is
 * normally not supposed to bind resources to threads, as this is the responsibility
 * of transaction managers. A further option is to lazily bind on first use if
 * transaction synchronization is active, for performing transactions that span
 * an arbitrary number of resources.
 * 
 * <p> 资源管理代码应检查线程绑定资源，例如JDBC连接或Hibernate会话，通过getResource。
 * 这样的代码通常不应该将资源绑定到线程，因为这是事务管理器的责任。另一个选择是，如果事务同步处于活动状态，
 * 则在首次使用时延迟绑定，以执行跨越任意数量资源的事务。
 *
 * <p>Transaction synchronization must be activated and deactivated by a transaction
 * manager via {@link #initSynchronization()} and {@link #clearSynchronization()}.
 * This is automatically supported by {@link AbstractPlatformTransactionManager},
 * and thus by all standard Spring transaction managers, such as
 * {@link org.springframework.transaction.jta.JtaTransactionManager} and
 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}.
 * 
 * <p> 事务管理器必须通过initSynchronization（）和clearSynchronization（）激活和取消激活事务同步。
 * 这由AbstractPlatformTransactionManager自动支持，因此由所有标准的Spring事务管理器支持，例如
 * org.springframework.transaction.jta.JtaTransactionManager和
 * org.springframework.jdbc.datasource.DataSourceTransactionManager。
 *
 * <p>Resource management code should only register synchronizations when this
 * manager is active, which can be checked via {@link #isSynchronizationActive};
 * it should perform immediate resource cleanup else. If transaction synchronization
 * isn't active, there is either no current transaction, or the transaction manager
 * doesn't support transaction synchronization.
 * 
 * <p> 资源管理代码只应在此管理器处于活动状态时注册同步，可以通过isSynchronizationActive进行检查;它应该立即执行资源清理。
 * 如果事务同步未处于活动状态，则表示没有当前事务，或者事务管理器不支持事务同步。
 *
 * <p>Synchronization is for example used to always return the same resources
 * within a JTA transaction, e.g. a JDBC Connection or a Hibernate Session for
 * any given DataSource or SessionFactory, respectively.
 * 
 * <p> 例如，同步用于始终在JTA事务中返回相同的资源，例如，分别针对任何给定的DataSource或SessionFactory的JDBC连接或
 * Hibernate会话。
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see #isSynchronizationActive
 * @see #registerSynchronization
 * @see TransactionSynchronization
 * @see AbstractPlatformTransactionManager#setTransactionSynchronization
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 */
public abstract class TransactionSynchronizationManager {

	private static final Log logger = LogFactory.getLog(TransactionSynchronizationManager.class);

	private static final ThreadLocal<Map<Object, Object>> resources =
			new NamedThreadLocal<Map<Object, Object>>("Transactional resources");

	private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations =
			new NamedThreadLocal<Set<TransactionSynchronization>>("Transaction synchronizations");

	private static final ThreadLocal<String> currentTransactionName =
			new NamedThreadLocal<String>("Current transaction name");

	private static final ThreadLocal<Boolean> currentTransactionReadOnly =
			new NamedThreadLocal<Boolean>("Current transaction read-only status");

	private static final ThreadLocal<Integer> currentTransactionIsolationLevel =
			new NamedThreadLocal<Integer>("Current transaction isolation level");

	private static final ThreadLocal<Boolean> actualTransactionActive =
			new NamedThreadLocal<Boolean>("Actual transaction active");


	//-------------------------------------------------------------------------
	// Management of transaction-associated resource handles
	// 管理与事务相关的资源句柄
	//-------------------------------------------------------------------------

	/**
	 * Return all resources that are bound to the current thread.
	 * 
	 * <p> 返回绑定到当前线程的所有资源。
	 * 
	 * <p>Mainly for debugging purposes. Resource managers should always invoke
	 * {@code hasResource} for a specific resource key that they are interested in.
	 * @return a Map with resource keys (usually the resource factory) and resource
	 * values (usually the active resource object), or an empty Map if there are
	 * currently no resources bound
	 * 
	 * <p> 主要用于调试目的。 资源管理器应始终为他们感兴趣的特定资源键调用hasResource。
	 * 
	 * @see #hasResource
	 */
	public static Map<Object, Object> getResourceMap() {
		Map<Object, Object> map = resources.get();
		return (map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap());
	}

	/**
	 * Check if there is a resource for the given key bound to the current thread.
	 * 
	 * <p> 检查绑定到当前线程的给定键是否有资源。
	 * 
	 * @param key the key to check (usually the resource factory)
	 * 
	 * <p> 检查的key（通常是资源工厂）
	 * 
	 * @return if there is a value bound to the current thread
	 * 
	 * <p> 如果有一个绑定到当前线程的值
	 * 
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static boolean hasResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doGetResource(actualKey);
		return (value != null);
	}

	/**
	 * Retrieve a resource for the given key that is bound to the current thread.
	 * 
	 * <p> 检索绑定到当前线程的给定键的资源。
	 * 
	 * @param key the key to check (usually the resource factory)
	 * 
	 * <p> 检查的关键（通常是资源工厂）
	 * 
	 * @return a value bound to the current thread (usually the active
	 * resource object), or {@code null} if none
	 * 
	 * <p> 绑定到当前线程（通常是活动资源对象）的值，如果没有，则返回null
	 * 
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static Object getResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doGetResource(actualKey);
		if (value != null && logger.isTraceEnabled()) {
			logger.trace("Retrieved value [" + value + "] for key [" + actualKey + "] bound to thread [" +
					Thread.currentThread().getName() + "]");
		}
		return value;
	}

	/**
	 * Actually check the value of the resource that is bound for the given key.
	 * 
	 * <p> 实际上检查绑定给定键的资源的值。
	 */
	private static Object doGetResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.get(actualKey);
		// Transparently remove ResourceHolder that was marked as void...
		// 透明地删除标记为void的ResourceHolder ...
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			map.remove(actualKey);
			// Remove entire ThreadLocal if empty...
			// 如果为空，则删除整个ThreadLocal ...
			if (map.isEmpty()) {
				resources.remove();
			}
			value = null;
		}
		return value;
	}

	/**
	 * Bind the given resource for the given key to the current thread.
	 * 
	 * <p> 将给定key的给定资源绑定到当前线程。
	 * 
	 * @param key the key to bind the value to (usually the resource factory)
	 * 
	 * <p> 绑定值的key（通常是资源工厂）
	 * 
	 * @param value the value to bind (usually the active resource object)
	 * 
	 * <p> 要绑定的值（通常是活动资源对象）
	 * 
	 * @throws IllegalStateException if there is already a value bound to the thread
	 * 
	 * <p> 如果已经有一个绑定到该线程的值
	 * 
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static void bindResource(Object key, Object value) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Assert.notNull(value, "Value must not be null");
		Map<Object, Object> map = resources.get();
		// set ThreadLocal Map if none found
		// 如果找不到，则设置ThreadLocal Map
		if (map == null) {
			map = new HashMap<Object, Object>();
			resources.set(map);
		}
		Object oldValue = map.put(actualKey, value);
		// Transparently suppress a ResourceHolder that was marked as void...
		// 透明地抑制标记为void的ResourceHolder ...
		if (oldValue instanceof ResourceHolder && ((ResourceHolder) oldValue).isVoid()) {
			oldValue = null;
		}
		if (oldValue != null) {
			throw new IllegalStateException("Already value [" + oldValue + "] for key [" +
					actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Bound value [" + value + "] for key [" + actualKey + "] to thread [" +
					Thread.currentThread().getName() + "]");
		}
	}

	/**
	 * Unbind a resource for the given key from the current thread.
	 * 
	 * <p> 解除当前线程中给定键的资源绑定。
	 * 
	 * @param key the key to unbind (usually the resource factory)
	 * 
	 * <p> 取消绑定的关键（通常是资源工厂）
	 * 
	 * @return the previously bound value (usually the active resource object)
	 * 
	 * <p> 先前绑定的值（通常是活动资源对象）
	 * 
	 * @throws IllegalStateException if there is no value bound to the thread
	 * 
	 * <p> 如果没有绑定到该线程的值
	 * 
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static Object unbindResource(Object key) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doUnbindResource(actualKey);
		if (value == null) {
			throw new IllegalStateException(
					"No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
		return value;
	}

	/**
	 * Unbind a resource for the given key from the current thread.
	 * 
	 * <p> 解除当前线程中给定键的资源绑定。
	 * 
	 * @param key the key to unbind (usually the resource factory)
	 * 
	 * <p> 取消绑定的关键（通常是资源工厂）
	 * 
	 * @return the previously bound value, or {@code null} if none bound
	 * 
	 * <p> 先前绑定的值，如果没有绑定，则返回null
	 * 
	 */
	public static Object unbindResourceIfPossible(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		return doUnbindResource(actualKey);
	}

	/**
	 * Actually remove the value of the resource that is bound for the given key.
	 * 
	 * <p> 实际上删除绑定给定键的资源的值。
	 * 
	 */
	private static Object doUnbindResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.remove(actualKey);
		// Remove entire ThreadLocal if empty...
		// 如果为空，则删除整个ThreadLocal ...
		if (map.isEmpty()) {
			resources.remove();
		}
		// Transparently suppress a ResourceHolder that was marked as void...
		// 透明地抑制标记为void的ResourceHolder ...
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			value = null;
		}
		if (value != null && logger.isTraceEnabled()) {
			logger.trace("Removed value [" + value + "] for key [" + actualKey + "] from thread [" +
					Thread.currentThread().getName() + "]");
		}
		return value;
	}


	//-------------------------------------------------------------------------
	// Management of transaction synchronizations
	// 管理事务同步
	//-------------------------------------------------------------------------

	/**
	 * Return if transaction synchronization is active for the current thread.
	 * Can be called before register to avoid unnecessary instance creation.
	 * 
	 * <p> 如果当前线程的事务同步处于活动状态，则返回。 可以在注册前调用，以避免不必要的实例创建。
	 * 
	 * @see #registerSynchronization
	 */
	public static boolean isSynchronizationActive() {
		return (synchronizations.get() != null);
	}

	/**
	 * Activate transaction synchronization for the current thread.
	 * Called by a transaction manager on transaction begin.
	 * 
	 * <p> 激活当前线程的事务同步。 事务管理器在事务开始时调用。
	 * 
	 * @throws IllegalStateException if synchronization is already active
	 * 
	 * <p> 如果同步已经激活
	 * 
	 */
	public static void initSynchronization() throws IllegalStateException {
		if (isSynchronizationActive()) {
			throw new IllegalStateException("Cannot activate transaction synchronization - already active");
		}
		logger.trace("Initializing transaction synchronization");
		synchronizations.set(new LinkedHashSet<TransactionSynchronization>());
	}

	/**
	 * Register a new transaction synchronization for the current thread.
	 * Typically called by resource management code.
	 * 
	 * <p> 为当前线程注册新的事务同步。 通常由资源管理代码调用。
	 * 
	 * <p>Note that synchronizations can implement the
	 * {@link org.springframework.core.Ordered} interface.
	 * They will be executed in an order according to their order value (if any).
	 * 
	 * <p> 请注意，同步可以实现org.springframework.core.Ordered接口。 它们将根据订单价值（如果有的话）按订单执行。
	 * 
	 * @param synchronization the synchronization object to register
	 * 
	 * <p> 要注册的同步对象
	 * 
	 * @throws IllegalStateException if transaction synchronization is not active
	 * 
	 * <p> 如果事务同步未激活
	 * 
	 * @see org.springframework.core.Ordered
	 */
	public static void registerSynchronization(TransactionSynchronization synchronization)
			throws IllegalStateException {

		Assert.notNull(synchronization, "TransactionSynchronization must not be null");
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		synchronizations.get().add(synchronization);
	}

	/**
	 * Return an unmodifiable snapshot list of all registered synchronizations
	 * for the current thread.
	 * 
	 * <p> 返回当前线程的所有已注册同步的不可修改的快照列表。
	 * 
	 * @return unmodifiable List of TransactionSynchronization instances
	 * 
	 * <p> 不可修改的TransactionSynchronization实例列表
	 * 
	 * @throws IllegalStateException if synchronization is not active
	 * 
	 * <p> 如果同步未激活
	 * 
	 * @see TransactionSynchronization
	 */
	public static List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		// Return unmodifiable snapshot, to avoid ConcurrentModificationExceptions
		// while iterating and invoking synchronization callbacks that in turn
		// might register further synchronizations.
		
		// 返回不可修改的快照，以避免在迭代和调用同步回调时进行ConcurrentModificationExceptions，而同步回调又可能会注册进一步的同步。
		if (synchs.isEmpty()) {
			return Collections.emptyList();
		}
		else {
			// Sort lazily here, not in registerSynchronization.
			// 这里懒惰地排序，而不是在registerSynchronization中。
			List<TransactionSynchronization> sortedSynchs = new ArrayList<TransactionSynchronization>(synchs);
			OrderComparator.sort(sortedSynchs);
			return Collections.unmodifiableList(sortedSynchs);
		}
	}

	/**
	 * Deactivate transaction synchronization for the current thread.
	 * Called by the transaction manager on transaction cleanup.
	 * 
	 * <p> 取消激活当前线程的事务同步。 事务管理器在事务清理时调用。
	 * 
	 * @throws IllegalStateException if synchronization is not active
	 * 
	 * <p> 如果同步未激活
	 * 
	 */
	public static void clearSynchronization() throws IllegalStateException {
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
		}
		logger.trace("Clearing transaction synchronization");
		synchronizations.remove();
	}


	//-------------------------------------------------------------------------
	// Exposure of transaction characteristics
	// 事务特征的曝光
	//-------------------------------------------------------------------------

	/**
	 * Expose the name of the current transaction, if any.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * 
	 * <p> 公开当前事务的名称（如果有）。 事务管理器在事务开始和清理时调用。
	 * 
	 * @param name the name of the transaction, or {@code null} to reset it
	 * 
	 * <p> 事务的名称，或null以重置它
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public static void setCurrentTransactionName(String name) {
		currentTransactionName.set(name);
	}

	/**
	 * Return the name of the current transaction, or {@code null} if none set.
	 * To be called by resource management code for optimizations per use case,
	 * for example to optimize fetch strategies for specific named transactions.
	 * 
	 * <p> 返回当前事务的名称，如果没有设置，则返回null。 由资源管理代码调用以针对每个用例进行优化，例如，优化特定命名事务的获取策略。
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public static String getCurrentTransactionName() {
		return currentTransactionName.get();
	}

	/**
	 * Expose a read-only flag for the current transaction.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * 
	 * <p> 公开当前事务的只读标志。 事务管理器在事务开始和清理时调用。
	 * 
	 * @param readOnly {@code true} to mark the current transaction
	 * as read-only; {@code false} to reset such a read-only marker
	 * 
	 * <p> 如果将当前事务标记为只读，则为true; false重置这样的只读标记
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 */
	public static void setCurrentTransactionReadOnly(boolean readOnly) {
		currentTransactionReadOnly.set(readOnly ? Boolean.TRUE : null);
	}

	/**
	 * Return whether the current transaction is marked as read-only.
	 * To be called by resource management code when preparing a newly
	 * created resource (for example, a Hibernate Session).
	 * 
	 * <p> 返回当前事务是否标记为只读。 在准备新创建的资源时由资源管理代码调用（例如，Hibernate会话）。
	 * 
	 * <p>Note that transaction synchronizations receive the read-only flag
	 * as argument for the {@code beforeCommit} callback, to be able
	 * to suppress change detection on commit. The present method is meant
	 * to be used for earlier read-only checks, for example to set the
	 * flush mode of a Hibernate Session to "FlushMode.NEVER" upfront.
	 * 
	 * <p> 请注意，事务同步接收只读标志作为beforeCommit回调的参数，以便能够抑制提交时的更改检测。 
	 * 本方法旨在用于早期的只读检查，例如，将Hibernate会话的刷新模式设置为“FlushMode.NEVER”。
	 * 
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	public static boolean isCurrentTransactionReadOnly() {
		return (currentTransactionReadOnly.get() != null);
	}

	/**
	 * Expose an isolation level for the current transaction.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * 
	 * <p> 公开当前事务的隔离级别。 事务管理器在事务开始和清理时调用。
	 * 
	 * @param isolationLevel the isolation level to expose, according to the
	 * JDBC Connection constants (equivalent to the corresponding Spring
	 * TransactionDefinition constants), or {@code null} to reset it
	 * 
	 * <p> 公开的隔离级别，根据JDBC连接常量（相当于相应的Spring TransactionDefinition常量），或null来重置它
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public static void setCurrentTransactionIsolationLevel(Integer isolationLevel) {
		currentTransactionIsolationLevel.set(isolationLevel);
	}

	/**
	 * Return the isolation level for the current transaction, if any.
	 * To be called by resource management code when preparing a newly
	 * created resource (for example, a JDBC Connection).
	 * 
	 * <p> 返回当前事务的隔离级别（如果有）。 在准备新创建的资源（例如，JDBC连接）时由资源管理代码调用。
	 * 
	 * @return the currently exposed isolation level, according to the
	 * JDBC Connection constants (equivalent to the corresponding Spring
	 * TransactionDefinition constants), or {@code null} if none
	 * 
	 * <p> 当前公开的隔离级别，根据JDBC连接常量（相当于相应的Spring TransactionDefinition常量），如果没有则为null
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public static Integer getCurrentTransactionIsolationLevel() {
		return currentTransactionIsolationLevel.get();
	}

	/**
	 * Expose whether there currently is an actual transaction active.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * 
	 * <p> 公开当前是否有活动的实际交易。 事务管理器在事务开始和清理时调用。
	 * 
	 * @param active {@code true} to mark the current thread as being associated
	 * with an actual transaction; {@code false} to reset that marker
	 * 
	 * <p> 如果将当前线程标记为与实际事务关联，则为true; false重置该标记
	 * 
	 */
	public static void setActualTransactionActive(boolean active) {
		actualTransactionActive.set(active ? Boolean.TRUE : null);
	}

	/**
	 * Return whether there currently is an actual transaction active.
	 * This indicates whether the current thread is associated with an actual
	 * transaction rather than just with active transaction synchronization.
	 * 
	 * <p> 返回当前是否有实际事务处于活动状态。 这指示当前线程是否与实际事务相关联，而不是仅与活动事务同步相关联。
	 * 
	 * <p>To be called by resource management code that wants to discriminate
	 * between active transaction synchronization (with or without backing
	 * resource transaction; also on PROPAGATION_SUPPORTS) and an actual
	 * transaction being active (with backing resource transaction;
	 * on PROPAGATION_REQUIRES, PROPAGATION_REQUIRES_NEW, etc).
	 * 
	 * <p> 由资源管理代码调用，该代码想要区分活动事务同步（有或没有支持资源事务;也在PROPAGATION_SUPPORTS上）
	 * 和实际事务处于活动状态（使用支持资源事务;在PROPAGATION_REQUIRES，PROPAGATION_REQUIRES_NEW等）。
	 * 
	 * @see #isSynchronizationActive()
	 */
	public static boolean isActualTransactionActive() {
		return (actualTransactionActive.get() != null);
	}


	/**
	 * Clear the entire transaction synchronization state for the current thread:
	 * registered synchronizations as well as the various transaction characteristics.
	 * 
	 * <p> 清除当前线程的整个事务同步状态：已注册的同步以及各种事务特征。
	 * 
	 * @see #clearSynchronization()
	 * @see #setCurrentTransactionName
	 * @see #setCurrentTransactionReadOnly
	 * @see #setCurrentTransactionIsolationLevel
	 * @see #setActualTransactionActive
	 */
	public static void clear() {
		clearSynchronization();
		setCurrentTransactionName(null);
		setCurrentTransactionReadOnly(false);
		setCurrentTransactionIsolationLevel(null);
		setActualTransactionActive(false);
	}

}
