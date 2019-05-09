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
import java.sql.Statement;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class that provides static methods for obtaining JDBC Connections from
 * a {@link javax.sql.DataSource}. Includes special support for Spring-managed
 * transactional Connections, e.g. managed by {@link DataSourceTransactionManager}
 * or {@link org.springframework.transaction.jta.JtaTransactionManager}.
 * 
 * <p> Helper类，提供从javax.sql.DataSource获取JDBC连接的静态方法。 包括对Spring管理的事务连接的特殊支持，
 * 例如 由DataSourceTransactionManager或
 * org.springframework.transaction.jta.JtaTransactionManager管理。
 *
 * <p>Used internally by Spring's {@link org.springframework.jdbc.core.JdbcTemplate},
 * Spring's JDBC operation objects and the JDBC {@link DataSourceTransactionManager}.
 * Can also be used directly in application code.
 * 
 * <p> 由Spring的org.springframework.jdbc.core.JdbcTemplate，Spring的JDBC操作对象和
 * JDBC DataSourceTransactionManager在内部使用。 也可以直接在应用程序代码中使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection
 * @see #releaseConnection
 * @see DataSourceTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public abstract class DataSourceUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up JDBC Connections.
	 * 
	 * <p> 清理JDBC连接的TransactionSynchronization对象的订单值。
	 */
	public static final int CONNECTION_SYNCHRONIZATION_ORDER = 1000;

	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);


	/**
	 * Obtain a Connection from the given DataSource. Translates SQLExceptions into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * 
	 * <p> 从给定的DataSource获取连接。 将SQLExceptions转换为未经检查的通用数据访问异常的Spring层次结构，
	 * 简化调用代码并使任何抛出的异常更有意义。
	 * 
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using {@link DataSourceTransactionManager}. Will bind a Connection to the
	 * thread if transaction synchronization is active, e.g. when running within a
	 * {@link org.springframework.transaction.jta.JtaTransactionManager JTA} transaction).
	 * 
	 * <p> 知道绑定到当前线程的相应Connection，例如使用DataSourceTransactionManager时。 
	 * 如果事务同步处于活动状态，则将Connection连接到线程，例如 在JTA事务中运行时）。
	 * 
	 * @param dataSource the DataSource to obtain Connections from - DataSource从中获取Connections
	 * @return a JDBC Connection from the given DataSource - 来自给定DataSource的JDBC连接
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * 
	 * <p> 如果尝试获取连接失败
	 * 
	 * @see #releaseConnection
	 */
	public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
		try {
			return doGetConnection(dataSource);
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
		}
	}

	/**
	 * Actually obtain a JDBC Connection from the given DataSource.
	 * Same as {@link #getConnection}, but throwing the original SQLException.
	 * 
	 * <p> 实际上从给定的DataSource获取JDBC连接。 与getConnection相同，但抛出原始的SQLException。
	 * 
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using {@link DataSourceTransactionManager}. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * 
	 * <p> 知道绑定到当前线程的相应Connection，例如使用DataSourceTransactionManager时。 
	 * 如果事务同步处于活动状态（例如，如果在JTA事务中），则将Connection连接到线程。
	 * 
	 * <p>Directly accessed by {@link TransactionAwareDataSourceProxy}.
	 * 
	 * <p> 由TransactionAwareDataSourceProxy直接访问。
	 * 
	 * @param dataSource the DataSource to obtain Connections from
	 * 
	 * <p> DataSource从中获取Connections
	 * 
	 * @return a JDBC Connection from the given DataSource
	 * 
	 * <p> 来自给定DataSource的JDBC连接
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 * 
	 * @see #doReleaseConnection
	 */
	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
			conHolder.requested();
			if (!conHolder.hasConnection()) {
				logger.debug("Fetching resumed JDBC Connection from DataSource");
				conHolder.setConnection(dataSource.getConnection());
			}
			return conHolder.getConnection();
		}
		// Else we either got no holder or an empty thread-bound holder here.
		// 否则我们要么没有持有者，要么在这里没有空线程持有者。

		logger.debug("Fetching JDBC Connection from DataSource");
		Connection con = dataSource.getConnection();

		//当前线程支持同步
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for JDBC Connection");
			// Use same Connection for further JDBC actions within the transaction.
			// Thread-bound object will get removed by synchronization at transaction completion.
			// 在事务中使用相同的Connection进一步执行JDBC操作。 在事务完成时，将通过同步删除线程绑定对象。
			
			//在事务中使用同一数据库连接
			ConnectionHolder holderToUse = conHolder;
			if (holderToUse == null) {
				holderToUse = new ConnectionHolder(con);
			}
			else {
				holderToUse.setConnection(con);
			}
			//记录数据库了连接
			holderToUse.requested();
			TransactionSynchronizationManager.registerSynchronization(
					new ConnectionSynchronization(holderToUse, dataSource));
			holderToUse.setSynchronizedWithTransaction(true);
			if (holderToUse != conHolder) {
				TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
			}
		}

		return con;
	}

	/**
	 * Prepare the given Connection with the given transaction semantics.
	 * 
	 * <p> 使用给定的事务语义准备给定的Connection。
	 * 
	 * @param con the Connection to prepare - 连接准备
	 * 
	 * @param definition the transaction definition to apply - 要应用的事务定义
	 * 
	 * @return the previous isolation level, if any
	 * 
	 * <p> 先前的隔离级别（如果有）
	 * 
	 * @throws SQLException if thrown by JDBC methods - 如果被JDBC方法抛出
	 * @see #resetConnectionAfterTransaction
	 */
	public static Integer prepareConnectionForTransaction(Connection con, TransactionDefinition definition)
			throws SQLException {

		Assert.notNull(con, "No Connection specified");

		// Set read-only flag.
		// 设置只读标志。
		if (definition != null && definition.isReadOnly()) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting JDBC Connection [" + con + "] read-only");
				}
				con.setReadOnly(true);
			}
			catch (SQLException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from JDBC 4.0
						// 假设它是连接超时，否则会丢失：例如 来自JDBC 4.0
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" SQLException -> ignore, it's just a hint anyway
				// “只读不支持”SQLException  - >忽略，它只是一个提示
				logger.debug("Could not set JDBC Connection read-only", ex);
			}
			catch (RuntimeException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from Hibernate
						// 假设它是连接超时，否则会丢失：例如 来自Hibernate
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" UnsupportedOperationException -> ignore, it's just a hint anyway
				// “只读不支持”UnsupportedOperationException  - >忽略，它只是一个提示
				logger.debug("Could not set JDBC Connection read-only", ex);
			}
		}

		// Apply specific isolation level, if any.
		// 如果有的话，应用特定的隔离级别。
		Integer previousIsolationLevel = null;
		if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			if (logger.isDebugEnabled()) {
				logger.debug("Changing isolation level of JDBC Connection [" + con + "] to " +
						definition.getIsolationLevel());
			}
			int currentIsolation = con.getTransactionIsolation();
			if (currentIsolation != definition.getIsolationLevel()) {
				previousIsolationLevel = currentIsolation;
				con.setTransactionIsolation(definition.getIsolationLevel());
			}
		}

		return previousIsolationLevel;
	}

	/**
	 * Reset the given Connection after a transaction,
	 * regarding read-only flag and isolation level.
	 * 
	 * <p> 关于只读标志和隔离级别，在事务之后重置给定的连接。
	 * 
	 * @param con the Connection to reset - 连接重置
	 * 
	 * @param previousIsolationLevel the isolation level to restore, if any
	 * 
	 * <p> 要恢复的隔离级别（如果有）
	 * 
	 * @see #prepareConnectionForTransaction
	 */
	public static void resetConnectionAfterTransaction(Connection con, Integer previousIsolationLevel) {
		Assert.notNull(con, "No Connection specified");
		try {
			// Reset transaction isolation to previous value, if changed for the transaction.
			// 如果为事务更改，则将事务隔离重置为先前值。
			if (previousIsolationLevel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of JDBC Connection [" +
							con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel);
			}

			// Reset read-only flag.
			// 重置只读标志。
			if (con.isReadOnly()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}

	/**
	 * Determine whether the given JDBC Connection is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * 
	 * <p> 确定给定的JDBC连接是否是事务性的，即由Spring的事务工具绑定到当前线程。
	 * 
	 * @param con the Connection to check - 要检查的连接
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * 
	 * <p> 从中获取Connection的DataSource（可能为null）
	 * 
	 * @return whether the Connection is transactional
	 * 
	 * <p> Connection是否是事务性的
	 */
	public static boolean isConnectionTransactional(Connection con, DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		return (conHolder != null && connectionEquals(conHolder, con));
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given JDBC Statement object.
	 * 
	 * <p> 将当前事务超时（如果有）应用于给定的JDBC Statement对象。
	 * 
	 * @param stmt the JDBC Statement object - JDBC Statement对象
	 * @param dataSource the DataSource that the Connection was obtained from
	 * 
	 * <p> 从中获取Connection的DataSource
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 * 
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTransactionTimeout(Statement stmt, DataSource dataSource) throws SQLException {
		applyTimeout(stmt, dataSource, 0);
	}

	/**
	 * Apply the specified timeout - overridden by the current transaction timeout,
	 * if any - to the given JDBC Statement object.
	 * 
	 * <p> 将指定的超时 - 由当前事务超时（如果有）覆盖到给定的JDBC Statement对象。
	 * 
	 * @param stmt the JDBC Statement object - JDBC Statement对象
	 * @param dataSource the DataSource that the Connection was obtained from
	 * 
	 * <p> 从中获取Connection的DataSource
	 * 
	 * @param timeout the timeout to apply (or 0 for no timeout outside of a transaction)
	 * 
	 * <p> 要应用的超时（或0表示事务之外没有超时）
	 * 
	 * @throws SQLException if thrown by JDBC methods - 如果被JDBC方法抛出
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTimeout(Statement stmt, DataSource dataSource, int timeout) throws SQLException {
		Assert.notNull(stmt, "No Statement specified");
		Assert.notNull(dataSource, "No DataSource specified");
		ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (holder != null && holder.hasTimeout()) {
			// Remaining transaction timeout overrides specified value.
			// 剩余的事务超时会覆盖指定的值。
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
		else if (timeout > 0) {
			// No current transaction timeout -> apply specified value.
			// 没有当前事务超时 - >应用指定值。
			stmt.setQueryTimeout(timeout);
		}
	}

	/**
	 * Close the given Connection, obtained from the given DataSource,
	 * if it is not managed externally (that is, not bound to the thread).
	 * 
	 * <p> 如果不是从外部管理（即，未绑定到线程），则关闭从给定DataSource获取的给定Connection。
	 * 
	 * @param con the Connection to close if necessary
	 * (if this is {@code null}, the call will be ignored)
	 * 
	 * <p> 如果需要，将关闭连接（如果为null，则将忽略该调用）
	 * 
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * 
	 * <p> 从中获取Connection的DataSource（可能为null）
	 * 
	 * @see #getConnection
	 */
	public static void releaseConnection(Connection con, DataSource dataSource) {
		try {
			doReleaseConnection(con, dataSource);
		}
		catch (SQLException ex) {
			logger.debug("Could not close JDBC Connection", ex);
		}
		catch (Throwable ex) {
			logger.debug("Unexpected exception on closing JDBC Connection", ex);
		}
	}

	/**
	 * Actually close the given Connection, obtained from the given DataSource.
	 * Same as {@link #releaseConnection}, but throwing the original SQLException.
	 * 
	 * <p> 实际上关闭从给定的DataSource获取的给定Connection。 与releaseConnection相同，但抛出原始的SQLException。
	 * 
	 * <p>Directly accessed by {@link TransactionAwareDataSourceProxy}.
	 * 
	 * <p> 由TransactionAwareDataSourceProxy直接访问。
	 * 
	 * @param con the Connection to close if necessary
	 * (if this is {@code null}, the call will be ignored)
	 * 
	 * <p> 如果需要，将关闭连接（如果为null，则将忽略该调用）
	 * 
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * 
	 * <p> 从中获取Connection的DataSource（可能为null）
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 * 
	 * @see #doGetConnection
	 */
	public static void doReleaseConnection(Connection con, DataSource dataSource) throws SQLException {
		if (con == null) {
			return;
		}
		if (dataSource != null) {
			//当前线程存在事务的情况下说明存在共用数据库连接直接使用 Connectionholder中的released方法进行连接数减一而不是真正的释放连接
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// It's the transactional Connection: Don't close it.
				// 这是事务性连接：不要关闭它。
				conHolder.released();
				return;
			}
		}
		logger.debug("Returning JDBC Connection to DataSource");
		doCloseConnection(con, dataSource);
	}

	/**
	 * Close the Connection, unless a {@link SmartDataSource} doesn't want us to.
	 * 
	 * <p> 关闭Connection，除非SmartDataSource不希望我们这样做。
	 * 
	 * @param con the Connection to close if necessary
	 * 
	 * <p> 如有必要，关闭连接
	 * 
	 * @param dataSource the DataSource that the Connection was obtained from
	 * 
	 * <p> 从中获取Connection的DataSource
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 * 
	 * @see Connection#close()
	 * @see SmartDataSource#shouldClose(Connection)
	 */
	public static void doCloseConnection(Connection con, DataSource dataSource) throws SQLException {
		if (!(dataSource instanceof SmartDataSource) || ((SmartDataSource) dataSource).shouldClose(con)) {
			con.close();
		}
	}

	/**
	 * Determine whether the given two Connections are equal, asking the target
	 * Connection in case of a proxy. Used to detect equality even if the
	 * user passed in a raw target Connection while the held one is a proxy.
	 * 
	 * <p> 确定给定的两个Connections是否相等，在代理的情况下询问目标Connection。 用于检测相等性，
	 * 即使用户在保持的目标是代理时传入原始目标Connection也是如此。
	 * 
	 * @param conHolder the ConnectionHolder for the held Connection (potentially a proxy)
	 * 
	 * <p> 保持连接的ConnectionHolder（可能是代理）
	 * 
	 * @param passedInCon the Connection passed-in by the user
	 * (potentially a target Connection without proxy)
	 * 
	 * <p> 用户传入的连接（可能是没有代理的目标连接）
	 * 
	 * @return whether the given Connections are equal
	 * 
	 * <p> whether the given Connections are equal
	 * 
	 * @see #getTargetConnection
	 */
	private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
		if (!conHolder.hasConnection()) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).
		// 显式检查身份：对于没有正确实现“等于”的连接句柄，例如Commons DBCP公开的那些句柄。
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				getTargetConnection(heldCon).equals(passedInCon));
	}

	/**
	 * Return the innermost target Connection of the given Connection. If the given
	 * Connection is a proxy, it will be unwrapped until a non-proxy Connection is
	 * found. Otherwise, the passed-in Connection will be returned as-is.
	 * 
	 * <p> 返回给定Connection的最内层目标Connection。 如果给定的Connection是代理，它将被解包，直到找到非代理连接。 
	 * 否则，传入的Connection将按原样返回。
	 * 
	 * @param con the Connection proxy to unwrap - 解包的Connection代理
	 * 
	 * @return the innermost target Connection, or the passed-in one if no proxy
	 * 
	 * <p> 最内层的目标Connection，如果没有代理，则传入一个
	 * 
	 * @see ConnectionProxy#getTargetConnection()
	 */
	public static Connection getTargetConnection(Connection con) {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy) {
			conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
		}
		return conToUse;
	}

	/**
	 * Determine the connection synchronization order to use for the given
	 * DataSource. Decreased for every level of nesting that a DataSource
	 * has, checked through the level of DelegatingDataSource nesting.
	 * 
	 * <p> 确定要用于给定DataSource的连接同步顺序。 对于DataSource所具有的每个嵌套级别都会降低，
	 * 通过DelegatingDataSource嵌套级别进行检查。
	 * 
	 * @param dataSource the DataSource to check - 要检查的DataSource
	 * @return the connection synchronization order to use - 要使用的连接同步顺序
	 * @see #CONNECTION_SYNCHRONIZATION_ORDER
	 */
	private static int getConnectionSynchronizationOrder(DataSource dataSource) {
		int order = CONNECTION_SYNCHRONIZATION_ORDER;
		DataSource currDs = dataSource;
		while (currDs instanceof DelegatingDataSource) {
			order--;
			currDs = ((DelegatingDataSource) currDs).getTargetDataSource();
		}
		return order;
	}


	/**
	 * Callback for resource cleanup at the end of a non-native JDBC transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * 
	 * <p> 在非本机JDBC事务结束时回调资源清理（例如，在参与JtaTransactionManager事务时）。
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private final ConnectionHolder connectionHolder;

		private final DataSource dataSource;

		private int order;

		private boolean holderActive = true;

		public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
			this.order = getConnectionSynchronizationOrder(dataSource);
		}

		@Override
		public int getOrder() {
			return this.order;
		}

		@Override
		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
					// Release Connection on suspend if the application doesn't keep
					// a handle to it anymore. We will fetch a fresh Connection if the
					// application accesses the ConnectionHolder again after resume,
					// assuming that it will participate in the same transaction.
					
					// 如果应用程序不再保留句柄，则在挂起时释放连接。 如果应用程序在恢复后再次访问ConnectionHolder，
					// 我们将获取一个新连接，假设它将参与同一事务。
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
		}

		@Override
		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			// Release Connection early if the holder is not open anymore
			// (that is, not used by another resource like a Hibernate Session
			// that has its own cleanup via transaction synchronization),
			// to avoid issues with strict JTA implementations that expect
			// the close call before transaction completion.
			
			// 如果持有者不再打开（即，不通过事务同步自己清理的Hibernate会话等其他资源使用），
			// 则提前释放连接，以避免在事务完成之前需要关闭调用的严格JTA实现的问题。
			if (!this.connectionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
				}
			}
		}

		@Override
		public void afterCompletion(int status) {
			// If we haven't closed the Connection in beforeCompletion,
			// close it now. The holder might have been used for other
			// cleanup in the meantime, for example by a Hibernate Session.
			
			// 如果我们还没有在beforeCompletion中关闭Connection，请立即关闭它。 
			// 在此期间，持有者可能已经用于其他清理，例如通过Hibernate Session。
			if (this.holderActive) {
				// The thread-bound ConnectionHolder might not be available anymore,
				// since afterCompletion might get called from a different thread.
				
				// 线程绑定的ConnectionHolder可能不再可用，因为afterCompletion可能从另一个线程调用。
				TransactionSynchronizationManager.unbindResourceIfPossible(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					// Reset the ConnectionHolder: It might remain bound to the thread.
					// 重置ConnectionHolder：它可能仍然绑定到线程。
					this.connectionHolder.setConnection(null);
				}
			}
			this.connectionHolder.reset();
		}
	}

}
