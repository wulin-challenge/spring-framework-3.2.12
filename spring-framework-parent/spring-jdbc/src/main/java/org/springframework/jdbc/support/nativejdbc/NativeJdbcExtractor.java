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

package org.springframework.jdbc.support.nativejdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Interface for extracting native JDBC objects from wrapped objects coming from
 * connection pools. This is necessary to allow for casting to native implementations
 * like {@code OracleConnection} or {@code OracleResultSet} in application
 * code, for example to create Blobs or to access vendor-specific features.
 * 
 * <p> 用于从连接池中包装的对象中提取本机JDBC对象的接口。这对于允许在应用程序代码中转换为OracleConnection或
 * OracleResultSet等本机实现是必要的，例如创建Blob或访问特定于供应商的功能。
 *
 * <p>Note: Setting a custom {@code NativeJdbcExtractor} is just necessary
 * if you intend to cast to database-specific implementations like
 * {@code OracleConnection} or {@code OracleResultSet}.
 * Otherwise, any wrapped JDBC object will be fine, with no need for unwrapping.
 * 
 * <p> 注意：如果您打算转换为特定于数据库的实现（如OracleConnection或OracleResultSet），
 * 则只需设置自定义NativeJdbcExtractor。否则，任何包装好的JDBC对象都没问题，不需要解包。
 *
 * <p>Note: To be able to support any pool's strategy of native ResultSet wrapping,
 * it is advisable to get both the native Statement <i>and</i> the native ResultSet
 * via this extractor. Some pools just allow to unwrap the Statement, some just to
 * unwrap the ResultSet - the above strategy will cover both. It is typically
 * <i>not</i> necessary to unwrap the Connection to retrieve a native ResultSet.
 * 
 * <p> 注意：为了能够支持任何池的本机ResultSet包装策略，建议通过此提取程序获取本机Statement和本机ResultSet。
 * 有些池只允许解包Statement，有些只是为了解包ResultSet  - 上面的策略将涵盖两者。
 * 通常不需要打开Connection来检索本机ResultSet。
 *
 * <p>When working with a simple connection pool that wraps Connections but not
 * Statements, a {@link SimpleNativeJdbcExtractor} is often sufficient. However,
 * some pools (like Jakarta's Commons DBCP) wrap <i>all</i> JDBC objects that they
 * return: Therefore, you need to use a specific {@code NativeJdbcExtractor}
 * (like {@link CommonsDbcpNativeJdbcExtractor}) with them.
 * 
 * <p> 使用包装Connections但不包含语句的简单连接池时，SimpleNativeJdbcExtractor通常就足够了。
 * 但是，某些池（如Jakarta的Commons DBCP）会包装它们返回的所有JDBC对象：因此，
 * 您需要使用特定的NativeJdbcExtractor（如CommonsDbcpNativeJdbcExtractor）。
 *
 * <p>{@link org.springframework.jdbc.core.JdbcTemplate} can properly apply a
 * {@code NativeJdbcExtractor} if specified, unwrapping all JDBC objects
 * that it creates. Note that this is just necessary if you intend to cast to
 * native implementations in your data access code.
 * 
 * <p> org.springframework.jdbc.core.JdbcTemplate可以正确应用NativeJdbcExtractor（如果已指定），
 * 解包它创建的所有JDBC对象。请注意，如果您打算在数据访问代码中强制转换为本机实现，则这是必要的。
 *
 * <p>{@link org.springframework.jdbc.support.lob.OracleLobHandler},
 * the Oracle-specific implementation of Spring's
 * {@link org.springframework.jdbc.support.lob.LobHandler} interface, requires a
 * {@code NativeJdbcExtractor} for obtaining the native {@code OracleConnection}.
 * This is also necessary for other Oracle-specific features that you may want
 * to leverage in your applications, such as Oracle InterMedia.
 * 
 * <p> org.springframework.jdbc.support.lob.OracleLobHandler是Spring的
 * org.springframework.jdbc.support.lob.LobHandler接口的Oracle特定实现，需要NativeJdbcExtractor
 * 来获取本机OracleConnection。对于您可能希望在应用程序中使用的其他Oracle特定功能（例如Oracle InterMedia），
 * 这也是必需的。
 *
 * @author Juergen Hoeller
 * @since 25.08.2003
 * @see SimpleNativeJdbcExtractor
 * @see CommonsDbcpNativeJdbcExtractor
 * @see org.springframework.jdbc.core.JdbcTemplate#setNativeJdbcExtractor
 * @see org.springframework.jdbc.support.lob.OracleLobHandler#setNativeJdbcExtractor
 */
public interface NativeJdbcExtractor {

	/**
	 * Return whether it is necessary to work on the native Connection to
	 * receive native Statements.
	 * 
	 * <p> 返回是否有必要在本机Connection上工作以接收本机语句。
	 * 
	 * <p>This should be true if the connection pool does not allow to extract
	 * the native JDBC objects from its Statement wrapper but supports a way
	 * to retrieve the native JDBC Connection. This way, applications can
	 * still receive native Statements and ResultSet via working on the
	 * native JDBC Connection.
	 * 
	 * <p> 如果连接池不允许从其Statement包装器中提取本机JDBC对象，但支持检索本机JDBC连接的方法，
	 * 则应该为true。 这样，应用程序仍然可以通过处理本机JDBC连接来接收本机语句和ResultSet。
	 */
	boolean isNativeConnectionNecessaryForNativeStatements();

	/**
	 * Return whether it is necessary to work on the native Connection to
	 * receive native PreparedStatements.
	 * 
	 * <p> 返回是否有必要在本机Connection上工作以接收本机PreparedStatements。
	 * 
	 * <p>This should be true if the connection pool does not allow to extract
	 * the native JDBC objects from its PreparedStatement wrappers but
	 * supports a way to retrieve the native JDBC Connection. This way,
	 * applications can still receive native Statements and ResultSet via
	 * working on the native JDBC Connection.
	 * 
	 * <p> 如果连接池不允许从其PreparedStatement包装器中提取本机JDBC对象，但支持检索本机JDBC连接的方法，
	 * 则应该为true。 这样，应用程序仍然可以通过处理本机JDBC连接来接收本机语句和ResultSet。
	 */
	boolean isNativeConnectionNecessaryForNativePreparedStatements();

	/**
	 * Return whether it is necessary to work on the native Connection to
	 * receive native CallableStatements.
	 * 
	 * <p> 返回是否有必要在本机Connection上工作以接收本机CallableStatements。
	 * 
	 * <p>This should be true if the connection pool does not allow to extract
	 * the native JDBC objects from its CallableStatement wrappers but
	 * supports a way to retrieve the native JDBC Connection. This way,
	 * applications can still receive native Statements and ResultSet via
	 * working on the native JDBC Connection.
	 * 
	 * <p> 如果连接池不允许从其CallableStatement包装器中提取本机JDBC对象，但支持检索本机JDBC连接的方法，
	 * 则应该为true。 这样，应用程序仍然可以通过处理本机JDBC连接来接收本机语句和ResultSet。
	 */
	boolean isNativeConnectionNecessaryForNativeCallableStatements();

	/**
	 * Retrieve the underlying native JDBC Connection for the given Connection.
	 * Supposed to return the given Connection if not capable of unwrapping.
	 * 
	 * <p> 检索给定Connection的基础本机JDBC连接。 如果不能解包，则假定返回给定的连接。
	 * 
	 * @param con the Connection handle, potentially wrapped by a connection pool
	 * 
	 * <p> Connection句柄，可能由连接池包装
	 * 
	 * @return the underlying native JDBC Connection, if possible;
	 * else, the original Connection
	 * 
	 * <p> 如果可能，底层的本机JDBC连接; 否则，原来的连接
	 * 
	 * @throws SQLException if thrown by JDBC methods - 如果被JDBC方法抛出
	 */
	Connection getNativeConnection(Connection con) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC Connection for the given Statement.
	 * Supposed to return the {@code Statement.getConnection()} if not
	 * capable of unwrapping.
	 * 
	 * <p> 检索给定Statement的基础本机JDBC连接。 如果不能解包，则返回Statement.getConnection（）。
	 * 
	 * <p>Having this extra method allows for more efficient unwrapping if data
	 * access code already has a Statement. {@code Statement.getConnection()}
	 * often returns the native JDBC Connection even if the Statement itself
	 * is wrapped by a pool.
	 * 
	 * <p> 如果数据访问代码已经具有Statement，则使用这种额外的方法可以更有效地解包。 
	 * Statement.getConnection（）经常返回本机JDBC连接，即使Statement本身被池包装也是如此。
	 * 
	 * @param stmt the Statement handle, potentially wrapped by a connection pool
	 * 
	 * <p> Statement句柄，可能由连接池包装
	 * 
	 * @return the underlying native JDBC Connection, if possible;
	 * else, the original Connection
	 * 
	 * <p> 如果可能，底层的本机JDBC连接; 否则，原来的连接
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 * 
	 * @see java.sql.Statement#getConnection()
	 */
	Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC Statement for the given Statement.
	 * Supposed to return the given Statement if not capable of unwrapping.
	 * 
	 * <p> 检索给定Statement的基础本机JDBC语句。 如果不能解包，则返回给定的Statement。
	 * 
	 * @param stmt the Statement handle, potentially wrapped by a connection pool
	 * 
	 * <p> Statement句柄，可能由连接池包装
	 * 
	 * @return the underlying native JDBC Statement, if possible;
	 * else, the original Statement
	 * 
	 * <p> 如果可能，底层的本机JDBC语句; 否则，原始声明
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 * 
	 */
	Statement getNativeStatement(Statement stmt) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC PreparedStatement for the given statement.
	 * Supposed to return the given PreparedStatement if not capable of unwrapping.
	 * 
	 * <p> 检索给定语句的基础本机JDBC PreparedStatement。 如果不能解包，则返回给定的PreparedStatement。
	 * 
	 * @param ps the PreparedStatement handle, potentially wrapped by a connection pool
	 * 
	 * <p> PreparedStatement句柄，可能由连接池包装
	 * 
	 * @return the underlying native JDBC PreparedStatement, if possible;
	 * else, the original PreparedStatement
	 * 
	 * <p> 如果可能，底层的本机JDBC PreparedStatement; 否则，原始的PreparedStatement
	 * 
	 * @throws SQLException if thrown by JDBC methods
	 * 
	 * <p> 如果被JDBC方法抛出
	 */
	PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC CallableStatement for the given statement.
	 * Supposed to return the given CallableStatement if not capable of unwrapping.
	 * 
	 * <p> 检索给定语句的基础本机JDBC CallableStatement。 如果不能解包，则返回给定的CallableStatement。
	 * 
	 * @param cs the CallableStatement handle, potentially wrapped by a connection pool
	 * 
	 * <p>  CallableStatement句柄，可能由连接池包装
	 * 
	 * @return the underlying native JDBC CallableStatement, if possible;
	 * else, the original CallableStatement
	 * 
	 * <p> 如果可能，底层的本机JDBC CallableStatement; 否则，原来的CallableStatement
	 * 
	 * @throws SQLException if thrown by JDBC methods - 如果被JDBC方法抛出
	 */
	CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC ResultSet for the given statement.
	 * Supposed to return the given ResultSet if not capable of unwrapping.
	 * 
	 * <p> 检索给定语句的基础本机JDBC ResultSet。 如果不能解包，则返回给定的ResultSet。
	 * 
	 * @param rs the ResultSet handle, potentially wrapped by a connection pool
	 * 
	 * <p> ResultSet句柄，可能由连接池包装
	 * 
	 * @return the underlying native JDBC ResultSet, if possible;
	 * else, the original ResultSet
	 * 
	 * <p> 如果可能，底层的本机JDBC ResultSet; 否则，原始ResultSet
	 * 
	 * @throws SQLException if thrown by JDBC methods - 如果被JDBC方法抛出
	 */
	ResultSet getNativeResultSet(ResultSet rs) throws SQLException;

}
