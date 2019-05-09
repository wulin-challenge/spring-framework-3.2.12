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

package org.springframework.transaction;

import java.sql.Connection;

/**
 * Interface that defines Spring-compliant transaction properties.
 * Based on the propagation behavior definitions analogous to EJB CMT attributes.
 * 
 * <p> 定义符合Spring的事务属性的接口。 基于类似于EJB CMT属性的传播行为定义。
 *
 * <p>Note that isolation level and timeout settings will not get applied unless
 * an actual new transaction gets started. As only {@link #PROPAGATION_REQUIRED},
 * {@link #PROPAGATION_REQUIRES_NEW} and {@link #PROPAGATION_NESTED} can cause
 * that, it usually doesn't make sense to specify those settings in other cases.
 * Furthermore, be aware that not all transaction managers will support those
 * advanced features and thus might throw corresponding exceptions when given
 * non-default values.
 * 
 * <p> 请注意，除非启动实际的新事务，否则不会应用隔离级别和超时设置。 由于只有PROPAGATION_REQUIRED，PROPAGATION_REQUIRES_NEW
 * 和PROPAGATION_NESTED会导致这种情况，因此在其他情况下指定这些设置通常没有意义。 此外，请注意，并非所有事务管理器都支持这些高级功能，
 * 因此在给定非默认值时可能会抛出相应的异常。
 *
 * <p>The {@link #isReadOnly() read-only flag} applies to any transaction context,
 * whether backed by an actual resource transaction or operating non-transactionally
 * at the resource level. In the latter case, the flag will only apply to managed
 * resources within the application, such as a Hibernate {@code Session}.
 *
 * <p> 只读标志适用于任何事务上下文，无论是由实际资源事务支持还是在资源级别以非事务方式操作。 在后一种情况下，
 * 该标志仅适用于应用程序内的受管资源，例如Hibernate会话。
 * 
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see PlatformTransactionManager#getTransaction(TransactionDefinition)
 * @see org.springframework.transaction.support.DefaultTransactionDefinition
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 */
public interface TransactionDefinition {

	/**
	 * Support a current transaction; create a new one if none exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * 
	 * <p> 支持当前事务; 如果不存在则创建一个新的。 类似于同名的EJB事务属性。
	 * 
	 * <p>This is typically the default setting of a transaction definition,
	 * and typically defines a transaction synchronization scope.
	 * 
	 * <p> 这通常是事务定义的缺省设置，通常定义事务同步范围。
	 * 
	 */
	int PROPAGATION_REQUIRED = 0;

	/**
	 * Support a current transaction; execute non-transactionally if none exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * 
	 * <p> 支持当前事务; 如果不存在则执行非事务性。 类似于同名的EJB事务属性。
	 * 
	 * <p><b>NOTE:</b> For transaction managers with transaction synchronization,
	 * {@code PROPAGATION_SUPPORTS} is slightly different from no transaction
	 * at all, as it defines a transaction scope that synchronization might apply to.
	 * As a consequence, the same resources (a JDBC {@code Connection}, a
	 * Hibernate {@code Session}, etc) will be shared for the entire specified
	 * scope. Note that the exact behavior depends on the actual synchronization
	 * configuration of the transaction manager!
	 * 
	 * <p> 注意：对于具有事务同步的事务管理器，PROPAGATION_SUPPORTS与根本没有事务略有不同，因为它定义了同步可能适用的事务范围。 
	 * 因此，将为整个指定范围共享相同的资源（JDBC连接，Hibernate会话等）。 请注意，确切的行为取决于事务管理器的实际同步配置！
	 * 
	 * <p>In general, use {@code PROPAGATION_SUPPORTS} with care! In particular, do
	 * not rely on {@code PROPAGATION_REQUIRED} or {@code PROPAGATION_REQUIRES_NEW}
	 * <i>within</i> a {@code PROPAGATION_SUPPORTS} scope (which may lead to
	 * synchronization conflicts at runtime). If such nesting is unavoidable, make sure
	 * to configure your transaction manager appropriately (typically switching to
	 * "synchronization on actual transaction").
	 * 
	 * <p> 一般情况下，请小心使用PROPAGATION_SUPPORTS！ 特别是，不要在PROPAGATION_SUPPORTS范围内依赖
	 * PROPAGATION_REQUIRED或PROPAGATION_REQUIRES_NEW（这可能会导致运行时出现同步冲突）。 
	 * 如果这种嵌套是不可避免的，请确保适当地配置事务管理器（通常切换到“实际事务上的同步”）。
	 * 
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 */
	int PROPAGATION_SUPPORTS = 1;

	/**
	 * Support a current transaction; throw an exception if no current transaction
	 * exists. Analogous to the EJB transaction attribute of the same name.
	 * 
	 * <p> 支持当前事务; 如果不存在当前事务则抛出异常。 类似于同名的EJB事务属性。
	 * 
	 * <p>Note that transaction synchronization within a {@code PROPAGATION_MANDATORY}
	 * scope will always be driven by the surrounding transaction.
	 * 
	 * <p> 请注意，PROPAGATION_MANDATORY范围内的事务同步将始终由周围的事务驱动。
	 * 
	 */
	int PROPAGATION_MANDATORY = 2;

	/**
	 * Create a new transaction, suspending the current transaction if one exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * 
	 * <p> 创建一个新事务，暂停当前事务（如果存在）。 类似于同名的EJB事务属性。
	 * 
	 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * which requires the {@code javax.transaction.TransactionManager}
	 * to be made available it to it (which is server-specific in standard J2EE).
	 * 
	 * <p> 注意：实际的事务暂停将无法在所有事务管理器上开箱即用。 这尤其适用于
	 * org.springframework.transaction.jta.JtaTransactionManager，它需要使
	 * javax.transaction.TransactionManager可用（它在标准J2EE中是特定于服务器的）。
	 * 
	 * <p>A {@code PROPAGATION_REQUIRES_NEW} scope always defines its own
	 * transaction synchronizations. Existing synchronizations will be suspended
	 * and resumed appropriately.
	 * 
	 * <p> PROPAGATION_REQUIRES_NEW范围始终定义自己的事务同步。 现有同步将被暂停并适当恢复。
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_REQUIRES_NEW = 3;

	/**
	 * Do not support a current transaction; rather always execute non-transactionally.
	 * Analogous to the EJB transaction attribute of the same name.
	 * 
	 * <p> 不支持当前事务; 而是总是以非事务方式执行。 类似于同名的EJB事务属性。
	 * 
	 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * which requires the {@code javax.transaction.TransactionManager}
	 * to be made available it to it (which is server-specific in standard J2EE).
	 * 
	 * <p> 注意：实际的事务暂停将无法在所有事务管理器上开箱即用。 这尤其适用于
	 * org.springframework.transaction.jta.JtaTransactionManager，它需要使
	 * javax.transaction.TransactionManager可用（它在标准J2EE中是特定于服务器的）。
	 * 
	 * <p>Note that transaction synchronization is <i>not</i> available within a
	 * {@code PROPAGATION_NOT_SUPPORTED} scope. Existing synchronizations
	 * will be suspended and resumed appropriately.
	 * 
	 * <p> 请注意，PROPAGATION_NOT_SUPPORTED范围内的事务同步不可用。 现有同步将被暂停并适当恢复。
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_NOT_SUPPORTED = 4;

	/**
	 * Do not support a current transaction; throw an exception if a current transaction
	 * exists. Analogous to the EJB transaction attribute of the same name.
	 * 
	 * <p> 不支持当前事务; 如果存在当前事务，则抛出异常。 类似于同名的EJB事务属性。
	 * 
	 * <p>Note that transaction synchronization is <i>not</i> available within a
	 * {@code PROPAGATION_NEVER} scope.
	 * 
	 * <p> 请注意，PROPAGATION_NEVER范围内的事务同步不可用。
	 * 
	 */
	int PROPAGATION_NEVER = 5;

	/**
	 * Execute within a nested transaction if a current transaction exists,
	 * behave like {@link #PROPAGATION_REQUIRED} else. There is no analogous
	 * feature in EJB.
	 * 
	 * <p> 如果当前事务存在，则在嵌套事务中执行，其行为类似于PROPAGATION_REQUIRED else。 EJB中没有类似的功能。
	 * 
	 * <p><b>NOTE:</b> Actual creation of a nested transaction will only work on
	 * specific transaction managers. Out of the box, this only applies to the JDBC
	 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}
	 * when working on a JDBC 3.0 driver. Some JTA providers might support
	 * nested transactions as well.
	 * 
	 * <p> 注意：实际创建嵌套事务仅适用于特定事务管理器。 开箱即用，这仅适用于处理JDBC 3.0驱动程序时的JDBC 
	 * org.springframework.jdbc.datasource.DataSourceTransactionManager。 
	 * 一些JTA提供程序也可能支持嵌套事务。
	 * 
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	int PROPAGATION_NESTED = 6;


	/**
	 * Use the default isolation level of the underlying datastore.
	 * All other levels correspond to the JDBC isolation levels.
	 * 
	 * <p> 使用基础数据存储的默认隔离级别。 所有其他级别对应于JDBC隔离级别。
	 * 
	 * @see java.sql.Connection
	 */
	int ISOLATION_DEFAULT = -1;

	/**
	 * Indicates that dirty reads, non-repeatable reads and phantom reads
	 * can occur.
	 * 
	 * <p> 表示可能发生脏读，不可重复读和幻像读。
	 * 
	 * <p>This level allows a row changed by one transaction to be read by another
	 * transaction before any changes in that row have been committed (a "dirty read").
	 * If any of the changes are rolled back, the second transaction will have
	 * retrieved an invalid row.
	 * 
	 * <p> 此级别允许在提交该行中的任何更改（“脏读”）之前，由另一个事务读取由一个事务更改的行。 
	 * 如果回滚任何更改，则第二个事务将检索到无效行。
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;

	/**
	 * Indicates that dirty reads are prevented; non-repeatable reads and
	 * phantom reads can occur.
	 * 
	 * <p> 表示禁止脏读; 可以发生不可重复的读取和幻像读取。
	 * 
	 * <p>This level only prohibits a transaction from reading a row
	 * with uncommitted changes in it.
	 * 
	 * <p> 此级别仅禁止事务读取具有未提交更改的行。
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	int ISOLATION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;

	/**
	 * Indicates that dirty reads and non-repeatable reads are prevented;
	 * phantom reads can occur.
	 * 
	 * <p> 表示禁止脏读和不可重复读; 可以发生幻像读取。
	 * 
	 * <p>This level prohibits a transaction from reading a row with uncommitted changes
	 * in it, and it also prohibits the situation where one transaction reads a row,
	 * a second transaction alters the row, and the first transaction re-reads the row,
	 * getting different values the second time (a "non-repeatable read").
	 * 
	 * <p> 此级别禁止事务读取具有未提交更改的行，并且还禁止一个事务读取行，第二个事务更改行，第一个事务重新读取行，
	 * 获取不同值的情况 时间（“不可重复读”）。
	 * 
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	int ISOLATION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;

	/**
	 * Indicates that dirty reads, non-repeatable reads and phantom reads
	 * are prevented.
	 * 
	 * <p> 表示禁止脏读，不可重复读和幻像读。
	 * 
	 * <p>This level includes the prohibitions in {@link #ISOLATION_REPEATABLE_READ}
	 * and further prohibits the situation where one transaction reads all rows that
	 * satisfy a {@code WHERE} condition, a second transaction inserts a row
	 * that satisfies that {@code WHERE} condition, and the first transaction
	 * re-reads for the same condition, retrieving the additional "phantom" row
	 * in the second read.
	 * 
	 * <p> 此级别包括ISOLATION_REPEATABLE_READ中的禁止，并进一步禁止一个事务读取满足WHERE条件的所有行，
	 * 第二个事务插入满足该WHERE条件的行，并且第一个事务重新读取相同条件的情况，检索 第二次读取中的附加“幻像”行。
	 * 
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	int ISOLATION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;


	/**
	 * Use the default timeout of the underlying transaction system,
	 * or none if timeouts are not supported.
	 * 
	 * <p> 使用基础事务系统的默认超时，如果不支持超时，则使用none。
	 * 
	 */
	int TIMEOUT_DEFAULT = -1;


	/**
	 * Return the propagation behavior.
	 * 
	 * <p> 返回传播行为。
	 * 
	 * <p>Must return one of the {@code PROPAGATION_XXX} constants
	 * defined on {@link TransactionDefinition this interface}.
	 * 
	 * <p> 必须返回此接口上定义的PROPAGATION_XXX常量之一。
	 * 
	 * @return the propagation behavior - 传播行为
	 * 
	 * @see #PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	int getPropagationBehavior();

	/**
	 * Return the isolation level.
	 * 
	 * <p> 返回隔离级别。
	 * 
	 * <p>Must return one of the {@code ISOLATION_XXX} constants
	 * defined on {@link TransactionDefinition this interface}.
	 * 
	 * <p> 必须返回此接口上定义的ISOLATION_XXX常量之一。
	 * 
	 * <p>Only makes sense in combination with {@link #PROPAGATION_REQUIRED}
	 * or {@link #PROPAGATION_REQUIRES_NEW}.
	 * 
	 * <p> 只有与PROPAGATION_REQUIRED或PROPAGATION_REQUIRES_NEW结合才有意义。
	 * 
	 * <p>Note that a transaction manager that does not support custom isolation levels
	 * will throw an exception when given any other level than {@link #ISOLATION_DEFAULT}.
	 * 
	 * <p> 请注意，如果给定除ISOLATION_DEFAULT之外的任何其他级别，则不支持自定义隔离级别的事务管理器将引发异常。
	 * 
	 * @return the isolation level - 隔离级别
	 */
	int getIsolationLevel();

	/**
	 * Return the transaction timeout.
	 * 
	 * <p> 返回事务超时。
	 * 
	 * <p>Must return a number of seconds, or {@link #TIMEOUT_DEFAULT}.
	 * 
	 * <p> 必须返回几秒或TIMEOUT_DEFAULT。
	 * 
	 * <p>Only makes sense in combination with {@link #PROPAGATION_REQUIRED}
	 * or {@link #PROPAGATION_REQUIRES_NEW}.
	 * 
	 * <p> 只有与PROPAGATION_REQUIRED或PROPAGATION_REQUIRES_NEW结合才有意义。
	 * 
	 * <p>Note that a transaction manager that does not support timeouts will throw
	 * an exception when given any other timeout than {@link #TIMEOUT_DEFAULT}.
	 * 
	 * <p> 请注意，在给定除TIMEOUT_DEFAULT之外的任何其他超时时，不支持超时的事务管理器将引发异常。
	 * 
	 * @return the transaction timeout - 事务超时
	 */
	int getTimeout();

	/**
	 * Return whether to optimize as a read-only transaction.
	 * 
	 * <p> 返回是否优化为只读事务。
	 * 
	 * <p>The read-only flag applies to any transaction context, whether
	 * backed by an actual resource transaction
	 * ({@link #PROPAGATION_REQUIRED}/{@link #PROPAGATION_REQUIRES_NEW}) or
	 * operating non-transactionally at the resource level
	 * ({@link #PROPAGATION_SUPPORTS}). In the latter case, the flag will
	 * only apply to managed resources within the application, such as a
	 * Hibernate {@code Session}.
	 * 
	 * <p> 只读标志适用于任何事务上下文，无论是由实际资源事务（PROPAGATION_REQUIRED / PROPAGATION_REQUIRES_NEW）
	 * 支持还是在资源级别非事务性操作（PROPAGATION_SUPPORTS）。 在后一种情况下，该标志仅适用于应用程序内的受管资源，
	 * 例如Hibernate会话。 
	 * 
	 * <p>This just serves as a hint for the actual transaction subsystem;
	 * it will <i>not necessarily</i> cause failure of write access attempts.
	 * A transaction manager which cannot interpret the read-only hint will
	 * <i>not</i> throw an exception when asked for a read-only transaction.
	 * 
	 * <p> 这仅仅是实际事务子系统的提示; 它不一定会导致写访问尝试失败。 当被要求进行只读事务时，不能解释只读提示的事务管理器不会引发异常。
	 * 
	 * @return {@code true} if the transaction is to be optimized as read-only
	 * 
	 * <p> 如果要将事务优化为只读，则为true
	 * 
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit(boolean)
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	boolean isReadOnly();

	/**
	 * Return the name of this transaction. Can be {@code null}.
	 * 
	 * <p> 返回此事物的名称。 可以为null。
	 * 
	 * <p>This will be used as the transaction name to be shown in a
	 * transaction monitor, if applicable (for example, WebLogic's).
	 * 
	 * <p> 这将用作事务监视器中显示的事务名称（如果适用）（例如，WebLogic的）。
	 * 
	 * <p>In case of Spring's declarative transactions, the exposed name will be
	 * the {@code fully-qualified class name + "." + method name} (by default).
	 * 
	 * <p> 对于Spring的声明性事务，公开的名称将是完全限定的类名+“。”。 +方法名称（默认情况下）。
	 * 
	 * @return the name of this transaction - 此事务的名称
	 * 
	 * @see org.springframework.transaction.interceptor.TransactionAspectSupport
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionName()
	 */
	String getName();

}
