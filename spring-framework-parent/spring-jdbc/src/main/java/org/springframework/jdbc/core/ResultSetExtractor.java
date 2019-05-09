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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

/**
 * Callback interface used by {@link JdbcTemplate}'s query methods.
 * Implementations of this interface perform the actual work of extracting
 * results from a {@link java.sql.ResultSet}, but don't need to worry
 * about exception handling. {@link java.sql.SQLException SQLExceptions}
 * will be caught and handled by the calling JdbcTemplate.
 * 
 * <p> JdbcTemplate的查询方法使用的回调接口。 此接口的实现执行从java.sql.ResultSet中提取结果的实际工作，
 * 但不需要担心异常处理。 调用JdbcTemplate将捕获并处理SQLExceptions。
 *
 * <p>This interface is mainly used within the JDBC framework itself.
 * A {@link RowMapper} is usually a simpler choice for ResultSet processing,
 * mapping one result object per row instead of one result object for
 * the entire ResultSet.
 * 
 * <p> 该接口主要用于JDBC框架本身。 对于ResultSet处理，RowMapper通常是一个更简单的选择，每行映射一个结果对象，
 * 而不是整个ResultSet映射一个结果对象。
 *
 * <p>Note: In contrast to a {@link RowCallbackHandler}, a ResultSetExtractor
 * object is typically stateless and thus reusable, as long as it doesn't
 * access stateful resources (such as output streams when streaming LOB
 * contents) or keep result state within the object.
 * 
 * <p> 注意：与RowCallbackHandler相比，ResultSetExtractor对象通常是无状态的，因此可重用，只要它不访问有状态资源
 * （例如流式传输LOB内容时的输出流）或在对象中保持结果状态。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since April 24, 2003
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see RowMapper
 * @see org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor
 */
public interface ResultSetExtractor<T> {

	/**
	 * Implementations must implement this method to process the entire ResultSet.
	 * 
	 * <p> 实现必须实现此方法来处理整个ResultSet。
	 * 
	 * @param rs ResultSet to extract data from. Implementations should
	 * not close this: it will be closed by the calling JdbcTemplate.
	 * 
	 * <p> ResultSet从中提取数据。 实现不应该关闭它：它将被调用JdbcTemplate关闭。
	 * 
	 * @return an arbitrary result object, or {@code null} if none
	 * (the extractor will typically be stateful in the latter case).
	 * 
	 * <p> 任意结果对象，如果没有则为null（在后一种情况下，提取器通常是有状态的）。
	 * 
	 * @throws SQLException if a SQLException is encountered getting column
	 * values or navigating (that is, there's no need to catch SQLException)
	 * 
	 * <p> 如果遇到SQLException获取列值或导航（也就是说，不需要捕获SQLException）
	 * 
	 * @throws DataAccessException in case of custom exceptions
	 * 
	 * <p> 如果是自定义异常
	 * 
	 */
	T extractData(ResultSet rs) throws SQLException, DataAccessException;

}
