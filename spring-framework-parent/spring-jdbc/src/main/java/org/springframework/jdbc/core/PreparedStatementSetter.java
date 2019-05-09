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

/**
 * General callback interface used by the {@link JdbcTemplate} class.
 * 
 * <p> JdbcTemplate类使用的常规回调接口。
 *
 * <p>This interface sets values on a {@link java.sql.PreparedStatement} provided
 * by the JdbcTemplate class, for each of a number of updates in a batch using the
 * same SQL. Implementations are responsible for setting any necessary parameters.
 * SQL with placeholders will already have been supplied.
 * 
 * <p> 此接口为JdbcTemplate类提供的java.sql.PreparedStatement设置值，对于使用相同SQL的批处理中的每个更新。 
 * 实现负责设置任何必要的参数。 已经提供了带占位符的SQL。
 *
 * <p>It's easier to use this interface than {@link PreparedStatementCreator}:
 * The JdbcTemplate will create the PreparedStatement, with the callback
 * only being responsible for setting parameter values.
 * 
 * <p> 使用此接口比PreparedStatementCreator更容易：JdbcTemplate将创建PreparedStatement，回调仅负责设置参数值。
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 * 
 * <p> 实现不需要关心可能从它们尝试的操作抛出的SQLExceptions。 JdbcTemplate类将适当地捕获和处理SQLExceptions。
 *
 * @author Rod Johnson
 * @since March 2, 2003
 * @see JdbcTemplate#update(String, PreparedStatementSetter)
 * @see JdbcTemplate#query(String, PreparedStatementSetter, ResultSetExtractor)
 */
public interface PreparedStatementSetter {

	/**
	 * Set parameter values on the given PreparedStatement.
	 * 
	 * <p> 在给定的PreparedStatement上设置参数值。
	 * 
	 * @param ps the PreparedStatement to invoke setter methods on - PreparedStatement用于调用setter方法
	 * @throws SQLException if a SQLException is encountered
	 * (i.e. there is no need to catch SQLException)
	 * 
	 * <p> 如果遇到SQLException（即不需要捕获SQLException）
	 */
	void setValues(PreparedStatement ps) throws SQLException;

}
