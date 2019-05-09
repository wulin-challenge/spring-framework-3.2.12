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

import org.springframework.transaction.TransactionDefinition;

/**
 * Enumeration that represents transaction propagation behaviors for use
 * with the {@link Transactional} annotation, corresponding to the
 * {@link TransactionDefinition} interface.
 * 
 * <p> 枚举，表示与Transactional注释一起使用的事务传播行为，对应于TransactionDefinition接口。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 */
public enum Propagation {

	/**
	 * Support a current transaction, create a new one if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * 
	 * <p> 支持当前事务，如果不存在则创建新事务。 类似于同名的EJB事务属性。
	 * 
	 * <p>This is the default setting of a transaction annotation.
	 * 
	 * <p> 这是事务注释的默认设置。
	 */
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),

	/**
	 * Support a current transaction, execute non-transactionally if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * 
	 * <p> 支持当前事务，如果不存在则以非事务方式执行。 类似于同名的EJB事务属性。
	 * 
	 * <p>Note: For transaction managers with transaction synchronization,
	 * PROPAGATION_SUPPORTS is slightly different from no transaction at all,
	 * as it defines a transaction scope that synchronization will apply for.
	 * As a consequence, the same resources (JDBC Connection, Hibernate Session, etc)
	 * will be shared for the entire specified scope. Note that this depends on
	 * the actual synchronization configuration of the transaction manager.
	 * 
	 * <p> 注意：对于具有事务同步的事务管理器，PROPAGATION_SUPPORTS与根本没有事务略有不同，因为它定义了同步将应用的事务范围。 
	 * 因此，将为整个指定范围共享相同的资源（JDBC连接，Hibernate会话等）。 请注意，这取决于事务管理器的实际同步配置。
	 * 
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 */
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),

	/**
	 * Support a current transaction, throw an exception if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * 
	 * <p> 支持当前事务，如果不存在则抛出异常。 类似于同名的EJB事务属性。
	 * 
	 */
	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),

	/**
	 * Create a new transaction, suspend the current transaction if one exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * 
	 * <p> 创建一个新事务，暂停当前事务（如果存在）。 类似于同名的EJB事务属性。
	 * 
	 * <p>Note: Actual transaction suspension will not work on out-of-the-box
	 * on all transaction managers. This in particular applies to JtaTransactionManager,
	 * which requires the {@code javax.transaction.TransactionManager} to be
	 * made available it to it (which is server-specific in standard J2EE).
	 * 
	 * <p> 注意：实际事务暂停不适用于所有事务管理器的开箱即用。 这尤其适用于JtaTransactionManager，
	 * 它需要将javax.transaction.TransactionManager提供给它（在标准J2EE中是特定于服务器的）。
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),

	/**
	 * Execute non-transactionally, suspend the current transaction if one exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * 
	 * <p> 以非事务方式执行，暂停当前事务（如果存在）。 类似于同名的EJB事务属性。
	 * 
	 * <p>Note: Actual transaction suspension will not work on out-of-the-box
	 * on all transaction managers. This in particular applies to JtaTransactionManager,
	 * which requires the {@code javax.transaction.TransactionManager} to be
	 * made available it to it (which is server-specific in standard J2EE).
	 * 
	 * <p> 注意：实际事务暂停不适用于所有事务管理器的开箱即用。 这尤其适用于JtaTransactionManager，
	 * 它需要将javax.transaction.TransactionManager提供给它（在标准J2EE中是特定于服务器的）。
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),

	/**
	 * Execute non-transactionally, throw an exception if a transaction exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * 
	 * <p> 如果事务存在，则以非事务方式执行，抛出异常。 类似于同名的EJB事务属性。
	 * 
	 */
	NEVER(TransactionDefinition.PROPAGATION_NEVER),

	/**
	 * Execute within a nested transaction if a current transaction exists,
	 * behave like PROPAGATION_REQUIRED else. There is no analogous feature in EJB.
	 * 
	 * <p> 如果当前事务存在，则在嵌套事务中执行，其行为类似于PROPAGATION_REQUIRED else。 EJB中没有类似的功能。
	 * 
	 * <p>Note: Actual creation of a nested transaction will only work on specific
	 * transaction managers. Out of the box, this only applies to the JDBC
	 * DataSourceTransactionManager when working on a JDBC 3.0 driver.
	 * Some JTA providers might support nested transactions as well.
	 * 
	 * <p> 注意：实际创建嵌套事务仅适用于特定事务管理器。 开箱即用，这仅适用于处理JDBC 3.0驱动程序时的JDBC DataSourceTransactionManager。 
	 * 一些JTA提供程序也可能支持嵌套事务。
	 * 
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	NESTED(TransactionDefinition.PROPAGATION_NESTED);


	private final int value;


	Propagation(int value) { this.value = value; }

	public int value() { return this.value; }

}
