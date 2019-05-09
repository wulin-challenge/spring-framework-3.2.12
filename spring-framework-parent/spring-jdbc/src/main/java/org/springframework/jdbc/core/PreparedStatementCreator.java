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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * One of the two central callback interfaces used by the JdbcTemplate class.
 * This interface creates a PreparedStatement given a connection, provided
 * by the JdbcTemplate class. Implementations are responsible for providing
 * SQL and any necessary parameters.
 * 
 * <p> JdbcTemplate类使用的两个中央回调接口之一。 此接口创建一个由JdbcTemplate类提供的连接的PreparedStatement。
 *  实现负责提供SQL和任何必要的参数。
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 * 
 * <p> 实现不需要关心可能从它们尝试的操作抛出的SQLExceptions。 JdbcTemplate类将适当地捕获和处理SQLExceptions。
 *
 * <p>A PreparedStatementCreator should also implement the SqlProvider interface
 * if it is able to provide the SQL it uses for PreparedStatement creation.
 * This allows for better contextual information in case of exceptions.
 * 
 * <p> 如果PreparedStatementCreator能够提供它用于PreparedStatement创建的SQL，它还应该实现SqlProvider接口。 
 * 这可以在异常情况下提供更好的上下文信息。
 *
 * @author Rod Johnson
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 * @see JdbcTemplate#query(PreparedStatementCreator, RowCallbackHandler)
 * @see JdbcTemplate#update(PreparedStatementCreator)
 * @see SqlProvider
 */
public interface PreparedStatementCreator {

	/**
	 * Create a statement in this connection. Allows implementations to use
	 * PreparedStatements. The JdbcTemplate will close the created statement.
	 * 
	 * <p> 在此连接中创建语句。 允许实现使用PreparedStatements。 JdbcTemplate将关闭创建的语句。
	 * 
	 * @param con Connection to use to create statement - 用于创建语句的连接
	 * @return a prepared statement - 准备好的声明
	 * @throws SQLException there is no need to catch SQLExceptions
	 * that may be thrown in the implementation of this method.
	 * The JdbcTemplate class will handle them.
	 * 
	 * <p> 没有必要捕获可能在此方法的实现中抛出的SQLException。 JdbcTemplate类将处理它们。
	 * 
	 */
	PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}
