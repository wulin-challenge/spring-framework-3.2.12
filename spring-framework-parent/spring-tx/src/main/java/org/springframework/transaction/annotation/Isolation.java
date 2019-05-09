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
 * Enumeration that represents transaction isolation levels for use
 * with the {@link Transactional} annotation, corresponding to the
 * {@link TransactionDefinition} interface.
 * 
 * <p> 枚举，表示与Transactional注释一起使用的事务隔离级别，对应于TransactionDefinition接口。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 */
public enum Isolation {

	/**
	 * Use the default isolation level of the underlying datastore.
	 * All other levels correspond to the JDBC isolation levels.
	 * 
	 * <p> 使用基础数据存储的默认隔离级别。 所有其他级别对应于JDBC隔离级别。
	 * 
	 * @see java.sql.Connection
	 */
	DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),

	/**
	 * A constant indicating that dirty reads, non-repeatable reads and phantom reads
	 * can occur. This level allows a row changed by one transaction to be read by
	 * another transaction before any changes in that row have been committed
	 * (a "dirty read"). If any of the changes are rolled back, the second
	 * transaction will have retrieved an invalid row.
	 * 
	 * <p> 一个常量，表示可以发生脏读，不可重复读和幻像读。 此级别允许在提交该行中的任何更改（“脏读”）之前，
	 * 由另一个事务读取由一个事务更改的行。 如果回滚任何更改，则第二个事务将检索到无效行。
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),

	/**
	 * A constant indicating that dirty reads are prevented; non-repeatable reads
	 * and phantom reads can occur. This level only prohibits a transaction
	 * from reading a row with uncommitted changes in it.
	 * 
	 * <p> 一个常量，表示防止脏读; 可以发生不可重复的读取和幻像读取。 此级别仅禁止事务读取具有未提交更改的行。
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),

	/**
	 * A constant indicating that dirty reads and non-repeatable reads are
	 * prevented; phantom reads can occur. This level prohibits a transaction
	 * from reading a row with uncommitted changes in it, and it also prohibits
	 * the situation where one transaction reads a row, a second transaction
	 * alters the row, and the first transaction rereads the row, getting
	 * different values the second time (a "non-repeatable read").
	 * 
	 * <p> 一个常量，表示防止脏读和不可重复读; 可以发生幻像读取。 此级别禁止事务读取具有未提交更改的行，并且还禁止一个事务读取行，
	 * 第二个事务更改行，第一个事务重新读取行，第二次获取不同值的情况（ “不可重复的阅读”）。
	 * 
	 * 
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),

	/**
	 * A constant indicating that dirty reads, non-repeatable reads and phantom
	 * reads are prevented. This level includes the prohibitions in
	 * {@code ISOLATION_REPEATABLE_READ} and further prohibits the situation
	 * where one transaction reads all rows that satisfy a {@code WHERE}
	 * condition, a second transaction inserts a row that satisfies that
	 * {@code WHERE} condition, and the first transaction rereads for the
	 * same condition, retrieving the additional "phantom" row in the second read.
	 * 
	 * <p> 一个常量，表示禁止脏读，不可重复读和幻像读。 此级别包括ISOLATION_REPEATABLE_READ中的禁止，
	 * 并进一步禁止一个事务读取满足WHERE条件的所有行，第二个事务插入满足该WHERE条件的行，
	 * 并且第一个事务重新读取相同条件的情况，检索附加“ 幻影“在第二次阅读中排。
	 * 
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);


	private final int value;


	Isolation(int value) { this.value = value; }

	public int value() { return this.value; }

}
