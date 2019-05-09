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

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

/**
 * Connection holder, wrapping a JDBC Connection.
 * {@link DataSourceTransactionManager} binds instances of this class
 * to the thread, for a specific DataSource.
 * 
 * <p> 连接持有者，包装JDBC连接。 DataSourceTransactionManager将此类的实例绑定到特定DataSource的线程。
 *
 * <p>Inherits rollback-only support for nested JDBC transactions
 * and reference count functionality from the base class.
 * 
 * <p> 从基类继承对嵌套JDBC事务和引用计数功能的仅回滚支持。
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 * 
 * <p> 注意：这是一个SPI类，不适合应用程序使用。
 *
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see DataSourceTransactionManager
 * @see DataSourceUtils
 */
public class ConnectionHolder extends ResourceHolderSupport {

	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";


	private ConnectionHandle connectionHandle;

	private Connection currentConnection;

	private boolean transactionActive = false;

	private Boolean savepointsSupported;

	private int savepointCounter = 0;


	/**
	 * Create a new ConnectionHolder for the given ConnectionHandle.
	 * 
	 * <p> 为给定的ConnectionHandle创建一个新的ConnectionHolder。
	 * 
	 * @param connectionHandle the ConnectionHandle to hold
	 * 
	 * <p> 要持有的ConnectionHandle
	 */
	public ConnectionHolder(ConnectionHandle connectionHandle) {
		Assert.notNull(connectionHandle, "ConnectionHandle must not be null");
		this.connectionHandle = connectionHandle;
	}

	/**
	 * Create a new ConnectionHolder for the given JDBC Connection,
	 * wrapping it with a {@link SimpleConnectionHandle},
	 * assuming that there is no ongoing transaction.
	 * 
	 * <p> 为给定的JDBC Connection创建一个新的ConnectionHolder，使用SimpleConnectionHandle包装它，假设没有正在进行的事务。
	 * 
	 * @param connection the JDBC Connection to hold
	 * 
	 * <p> 要保持的JDBC连接
	 * 
	 * @see SimpleConnectionHandle
	 * @see #ConnectionHolder(java.sql.Connection, boolean)
	 */
	public ConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}

	/**
	 * Create a new ConnectionHolder for the given JDBC Connection,
	 * wrapping it with a {@link SimpleConnectionHandle}.
	 * 
	 * <p> 为给定的JDBC Connection创建一个新的ConnectionHolder，并使用SimpleConnectionHandle包装它。
	 * 
	 * @param connection the JDBC Connection to hold
	 * 
	 * <p> 要保持的JDBC连接
	 * 
	 * @param transactionActive whether the given Connection is involved
	 * in an ongoing transaction
	 * 
	 * <p> 是否给定的连接涉及正在进行的事务
	 * 
	 * @see SimpleConnectionHandle
	 */
	public ConnectionHolder(Connection connection, boolean transactionActive) {
		this(connection);
		this.transactionActive = transactionActive;
	}


	/**
	 * Return the ConnectionHandle held by this ConnectionHolder.
	 * 
	 * <p> 返回此ConnectionHolder持有的ConnectionHandle。
	 */
	public ConnectionHandle getConnectionHandle() {
		return this.connectionHandle;
	}

	/**
	 * Return whether this holder currently has a Connection.
	 * 
	 * <p> 返回此持有者当前是否有连接。
	 * 
	 */
	protected boolean hasConnection() {
		return (this.connectionHandle != null);
	}

	/**
	 * Set whether this holder represents an active, JDBC-managed transaction.
	 * 
	 * <p> 设置此holder是否表示由JDBC管理的活动事务。
	 * 
	 * @see DataSourceTransactionManager
	 */
	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	/**
	 * Return whether this holder represents an active, JDBC-managed transaction.
	 * 
	 * <p> 返回此持有者是否表示由JDBC管理的活动事务。
	 * 
	 */
	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	/**
	 * Override the existing Connection handle with the given Connection.
	 * Reset the handle if given {@code null}.
	 * 
	 * <p> 使用给定的Connection覆盖现有的Connection句柄。 如果给定null，则重置句柄。
	 * 
	 * <p>Used for releasing the Connection on suspend (with a {@code null}
	 * argument) and setting a fresh Connection on resume.
	 * 
	 * <p> 用于在挂起时释放连接（带有空参数）并在恢复时设置新连接。
	 * 
	 */
	protected void setConnection(Connection connection) {
		if (this.currentConnection != null) {
			this.connectionHandle.releaseConnection(this.currentConnection);
			this.currentConnection = null;
		}
		if (connection != null) {
			this.connectionHandle = new SimpleConnectionHandle(connection);
		}
		else {
			this.connectionHandle = null;
		}
	}

	/**
	 * Return the current Connection held by this ConnectionHolder.
	 * 
	 * <p> 返回此ConnectionHolder持有的当前Connection。
	 * 
	 * <p>This will be the same Connection until {@code released}
	 * gets called on the ConnectionHolder, which will reset the
	 * held Connection, fetching a new Connection on demand.
	 * 
	 * <p> 在ConnectionHolder上调用release之前，它将是相同的Connection，它将重置保持的Connection，并根据需要获取新的Connection。
	 * 
	 * @see ConnectionHandle#getConnection()
	 * @see #released()
	 */
	public Connection getConnection() {
		Assert.notNull(this.connectionHandle, "Active Connection is required");
		if (this.currentConnection == null) {
			this.currentConnection = this.connectionHandle.getConnection();
		}
		return this.currentConnection;
	}

	/**
	 * Return whether JDBC 3.0 Savepoints are supported.
	 * Caches the flag for the lifetime of this ConnectionHolder.
	 * 
	 * <p> 返回是否支持JDBC 3.0保存点。 缓存此ConnectionHolder生命周期的标志。
	 * 
	 * @throws SQLException if thrown by the JDBC driver
	 * 
	 * <p> 如果由JDBC驱动程序抛出
	 * 
	 */
	public boolean supportsSavepoints() throws SQLException {
		if (this.savepointsSupported == null) {
			this.savepointsSupported = new Boolean(getConnection().getMetaData().supportsSavepoints());
		}
		return this.savepointsSupported.booleanValue();
	}

	/**
	 * Create a new JDBC 3.0 Savepoint for the current Connection,
	 * using generated savepoint names that are unique for the Connection.
	 * 
	 * <p> 使用为Connection唯一的生成的保存点名称为当前Connection创建新的JDBC 3.0保存点。
	 * 
	 * @return the new Savepoint
	 * 
	 * <p> 新的Savepoint
	 * 
	 * @throws SQLException if thrown by the JDBC driver
	 * 
	 * <p> 如果由JDBC驱动程序抛出
	 * 
	 */
	public Savepoint createSavepoint() throws SQLException {
		this.savepointCounter++;
		return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}

	/**
	 * Releases the current Connection held by this ConnectionHolder.
	 * 
	 * <p> 释放此ConnectionHolder持有的当前Connection。
	 * 
	 * <p>This is necessary for ConnectionHandles that expect "Connection borrowing",
	 * where each returned Connection is only temporarily leased and needs to be
	 * returned once the data operation is done, to make the Connection available
	 * for other operations within the same transaction. This is the case with
	 * JDO 2.0 DataStoreConnections, for example.
	 * 
	 * <p> 这对于期望“连接借用”的ConnectionHandle是必需的，其中每个返回的连接仅被临时租用并且在数据操作完成后需要返回，
	 * 以使Connection可用于同一事务中的其他操作。 例如，JDO 2.0 DataStoreConnections就是这种情况。
	 * 
	 * @see org.springframework.orm.jdo.DefaultJdoDialect#getJdbcConnection
	 */
	@Override
	public void released() {
		super.released();
		if (!isOpen() && this.currentConnection != null) {
			this.connectionHandle.releaseConnection(this.currentConnection);
			this.currentConnection = null;
		}
	}


	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
		this.savepointsSupported = null;
		this.savepointCounter = 0;
	}

}
