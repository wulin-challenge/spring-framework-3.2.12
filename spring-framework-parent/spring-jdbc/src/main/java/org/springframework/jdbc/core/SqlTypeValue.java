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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * Interface to be implemented for setting values for more complex database-specific
 * types not supported by the standard {@code setObject} method. This is
 * effectively an extended variant of {@link org.springframework.jdbc.support.SqlValue}.
 * 
 * <p> 要实现的接口，用于为标准setObject方法不支持的更复杂的特定于数据库的类型设置值。 这实际上是
 * org.springframework.jdbc.support.SqlValue的扩展变体。
 *
 * <p>Implementations perform the actual work of setting the actual values. They must
 * implement the callback method {@code setTypeValue} which can throw SQLExceptions
 * that will be caught and translated by the calling code. This callback method has
 * access to the underlying Connection via the given PreparedStatement object, if that
 * should be needed to create any database-specific objects.
 * 
 * <p> 实现执行设置实际值的实际工作。 它们必须实现回调方法setTypeValue，它可以抛出将被调用代码捕获和转换的SQLExceptions。 
 * 如果需要创建任何特定于数据库的对象，则此回调方法可以通过给定的PreparedStatement对象访问基础Connection。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see java.sql.Types
 * @see java.sql.PreparedStatement#setObject
 * @see JdbcOperations#update(String, Object[], int[])
 * @see org.springframework.jdbc.support.SqlValue
 */
public interface SqlTypeValue {

	/**
	 * Constant that indicates an unknown (or unspecified) SQL type.
	 * Passed into {@code setTypeValue} if the original operation method
	 * does not specify a SQL type.
	 * 
	 * <p> 指示未知（或未指定）SQL类型的常量。 如果原始操作方法未指定SQL类型，则传入setTypeValue。
	 * 
	 * @see java.sql.Types
	 * @see JdbcOperations#update(String, Object[])
	 */
	int TYPE_UNKNOWN = JdbcUtils.TYPE_UNKNOWN;


	/**
	 * Set the type value on the given PreparedStatement.
	 * 
	 * <p> 在给定的PreparedStatement上设置类型值。
	 * 
	 * @param ps the PreparedStatement to work on
	 * 
	 * <p> 要处理的PreparedStatement
	 * 
	 * @param paramIndex the index of the parameter for which we need to set the value
	 * 
	 * <p> 我们需要为其设置值的参数的索引
	 * 
	 * @param sqlType SQL type of the parameter we are setting
	 * 
	 * <p> 我们正在设置的参数的SQL类型
	 * 
	 * @param typeName the type name of the parameter (optional)
	 * 
	 * <p> 参数的类型名称（可选）
	 * 
	 * @throws SQLException if a SQLException is encountered while setting parameter values
	 * 
	 * <p> 如果在设置参数值时遇到SQLException
	 * 
	 * @see java.sql.Types
	 * @see java.sql.PreparedStatement#setObject
	 */
	void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException;

}
