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

import org.springframework.dao.DataAccessException;

/**
 * Generic callback interface for code that operates on a PreparedStatement.
 * Allows to execute any number of operations on a single PreparedStatement,
 * for example a single {@code executeUpdate} call or repeated
 * {@code executeUpdate} calls with varying parameters.
 * 
 * <p> 用于在PreparedStatement上运行的代码的通用回调接口。 允许在单个PreparedStatement上执行任意数量的操作，
 * 例如单个executeUpdate调用或具有不同参数的重复executeUpdate调用。
 *
 * <p>Used internally by JdbcTemplate, but also useful for application code.
 * Note that the passed-in PreparedStatement can have been created by the
 * framework or by a custom PreparedStatementCreator. However, the latter is
 * hardly ever necessary, as most custom callback actions will perform updates
 * in which case a standard PreparedStatement is fine. Custom actions will
 * always set parameter values themselves, so that PreparedStatementCreator
 * capability is not needed either.
 * 
 * <p> 由JdbcTemplate内部使用，但对应用程序代码也很有用。 请注意，传入的PreparedStatement可以由框架或自定义
 * PreparedStatementCreator创建。 但是，后者几乎不需要，因为大多数自定义回调操作都会执行更新，在这种情况下，
 * 标准的PreparedStatement就可以了。 自定义操作将始终自行设置参数值，因此也不需要PreparedStatementCreator功能。
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @see JdbcTemplate#execute(String, PreparedStatementCallback)
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 */
public interface PreparedStatementCallback<T> {

	/**
	 * Gets called by {@code JdbcTemplate.execute} with an active JDBC
	 * PreparedStatement. Does not need to care about closing the Statement
	 * or the Connection, or about handling transactions: this will all be
	 * handled by Spring's JdbcTemplate.
	 * 
	 * <p> 由JdbcTemplate.execute使用活动的JDBC PreparedStatement调用。
	 * 不需要关心关闭Statement或Connection，或关于处理事务：这将全部由Spring的JdbcTemplate处理。
	 *
	 * <p><b>NOTE:</b> Any ResultSets opened should be closed in finally blocks
	 * within the callback implementation. Spring will close the Statement
	 * object after the callback returned, but this does not necessarily imply
	 * that the ResultSet resources will be closed: the Statement objects might
	 * get pooled by the connection pool, with {@code close} calls only
	 * returning the object to the pool but not physically closing the resources.
	 * 
	 * <p> 注意：应该在回调实现中的finally块中关闭打开的任何ResultSet。 Spring将在返回回调后关闭Statement对象，
	 * 但这并不一定意味着ResultSet资源将被关闭：Statement对象可能被连接池合并，close调用只将对象返回到池而不是关闭资源。
	 *
	 * <p>If called without a thread-bound JDBC transaction (initiated by
	 * DataSourceTransactionManager), the code will simply get executed on the
	 * JDBC connection with its transactional semantics. If JdbcTemplate is
	 * configured to use a JTA-aware DataSource, the JDBC connection and thus
	 * the callback code will be transactional if a JTA transaction is active.
	 * 
	 * <p> 如果在没有线程绑定的JDBC事务（由DataSourceTransactionManager启动）的情况下调用，
	 * 则代码将简单地在JDBC连接上以其事务语义执行。如果JdbcTemplate配置为使用支持JTA的DataSource，
	 * 那么如果JTA事务处于活动状态，则JDBC连接以及回调代码将是事务性的。
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. Note that there's
	 * special support for single step actions: see JdbcTemplate.queryForObject etc.
	 * A thrown RuntimeException is treated as application exception, it gets
	 * propagated to the caller of the template.
	 * 
	 * <p> 允许返回在回调中创建的结果对象，即域对象或域对象的集合。请注意，对单步操作有特殊支持：请参阅JdbcTemplate.queryForObject等。
	 * 抛出的RuntimeException被视为应用程序异常，它会传播到模板的调用者。
	 *
	 * @param ps active JDBC PreparedStatement - 活动JDBC PreparedStatement
	 * @return a result object, or {@code null} if none - 结果对象，如果没有则为null
	 * @throws SQLException if thrown by a JDBC method, to be auto-converted
	 * to a DataAccessException by a SQLExceptionTranslator
	 * 
	 * <p> 如果由JDBC方法抛出，则由SQLExceptionTranslator自动转换为DataAccessException
	 * 
	 * @throws DataAccessException in case of custom exceptions
	 * 
	 * <p> 如果是自定义异常
	 * 
	 * @see JdbcTemplate#queryForObject(String, Object[], Class)
	 * @see JdbcTemplate#queryForList(String, Object[])
	 */
	T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;

}
