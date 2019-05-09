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

package org.springframework.jdbc.datasource;

import java.sql.Savepoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * Convenient base class for JDBC-aware transaction objects.
 * Can contain a {@link ConnectionHolder}, and implements the
 * {@link org.springframework.transaction.SavepointManager}
 * interface based on that ConnectionHolder.
 * 
 * <p> JDBC感知事务对象的便捷基类。 可以包含ConnectionHolder，并基于该ConnectionHolder实现
 * org.springframework.transaction.SavepointManager接口。
 *
 * <p>Allows for programmatic management of JDBC 3.0
 * {@link java.sql.Savepoint Savepoints}. Spring's
 * {@link org.springframework.transaction.support.DefaultTransactionStatus}
 * will automatically delegate to this, as it autodetects transaction
 * objects that implement the SavepointManager interface.
 * 
 * <p> 允许对JDBC 3.0 Savepoints进行编程管理。 Spring的
 * org.springframework.transaction.support.DefaultTransactionStatus会自动委托给它，
 * 因为它会自动检测实现SavepointManager接口的事务对象。
 *
 * <p>Note that savepoints are only supported for drivers which
 * support JDBC 3.0 or higher.
 * 
 * <p> 请注意，仅支持JDBC 3.0或更高版本的驱动程序支持保存点。
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class JdbcTransactionObjectSupport implements SavepointManager, SmartTransactionObject {

	private static final Log logger = LogFactory.getLog(JdbcTransactionObjectSupport.class);


	private ConnectionHolder connectionHolder;

	private Integer previousIsolationLevel;

	private boolean savepointAllowed = false;


	public void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public ConnectionHolder getConnectionHolder() {
		return this.connectionHolder;
	}

	public boolean hasConnectionHolder() {
		return (this.connectionHolder != null);
	}

	public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return this.previousIsolationLevel;
	}

	public void setSavepointAllowed(boolean savepointAllowed) {
		this.savepointAllowed = savepointAllowed;
	}

	public boolean isSavepointAllowed() {
		return this.savepointAllowed;
	}

	public void flush() {
		// no-op
	}


	//---------------------------------------------------------------------
	// Implementation of SavepointManager
	// SavepointManager的实现
	//---------------------------------------------------------------------

	/**
	 * This implementation creates a JDBC 3.0 Savepoint and returns it.
	 * 
	 * <p> 此实现创建JDBC 3.0 Savepoint并返回它。
	 * 
	 * @see java.sql.Connection#setSavepoint
	 */
	public Object createSavepoint() throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			if (!conHolder.supportsSavepoints()) {
				throw new NestedTransactionNotSupportedException(
						"Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
			}
		}
		catch (Throwable ex) {
			throw new NestedTransactionNotSupportedException(
					"Cannot create a nested transaction because your JDBC driver is not a JDBC 3.0 driver", ex);
		}
		try {
			return conHolder.createSavepoint();
		}
		catch (Throwable ex) {
			throw new CannotCreateTransactionException("Could not create JDBC savepoint", ex);
		}
	}

	/**
	 * This implementation rolls back to the given JDBC 3.0 Savepoint.
	 * 
	 * <p> 此实现回滚到给定的JDBC 3.0保存点。
	 * 
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionHolderForSavepoint().getConnection().rollback((Savepoint) savepoint);
		}
		catch (Throwable ex) {
			throw new TransactionSystemException("Could not roll back to JDBC savepoint", ex);
		}
	}

	/**
	 * This implementation releases the given JDBC 3.0 Savepoint.
	 * 
	 * <p> 此实现释放给定的JDBC 3.0 Savepoint。
	 * 
	 * @see java.sql.Connection#releaseSavepoint
	 */
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionHolderForSavepoint().getConnection().releaseSavepoint((Savepoint) savepoint);
		}
		catch (Throwable ex) {
			logger.debug("Could not explicitly release JDBC savepoint", ex);
		}
	}

	protected ConnectionHolder getConnectionHolderForSavepoint() throws TransactionException {
		if (!isSavepointAllowed()) {
			throw new NestedTransactionNotSupportedException(
					"Transaction manager does not allow nested transactions");
		}
		if (!hasConnectionHolder()) {
			throw new TransactionUsageException(
					"Cannot create nested transaction if not exposing a JDBC transaction");
		}
		return getConnectionHolder();
	}

}
