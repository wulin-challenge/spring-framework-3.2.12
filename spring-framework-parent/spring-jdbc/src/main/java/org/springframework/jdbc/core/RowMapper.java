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

/**
 * An interface used by {@link JdbcTemplate} for mapping rows of a
 * {@link java.sql.ResultSet} on a per-row basis. Implementations of this
 * interface perform the actual work of mapping each row to a result object,
 * but don't need to worry about exception handling.
 * {@link java.sql.SQLException SQLExceptions} will be caught and handled
 * by the calling JdbcTemplate.
 * 
 * <p> JdbcTemplate用于基于每行映射java.sql.ResultSet行的接口。 此接口的实现执行将每行映射到结果对象的实际工作，
 * 但不需要担心异常处理。 调用JdbcTemplate将捕获并处理SQLExceptions。
 *
 * <p>Typically used either for {@link JdbcTemplate}'s query methods
 * or for out parameters of stored procedures. RowMapper objects are
 * typically stateless and thus reusable; they are an ideal choice for
 * implementing row-mapping logic in a single place.
 * 
 * <p> 通常用于JdbcTemplate的查询方法或存储过程的out参数。 RowMapper对象通常是无状态的，因此可以重用; 
 * 它们是在一个地方实现行映射逻辑的理想选择。
 *
 * <p>Alternatively, consider subclassing
 * {@link org.springframework.jdbc.object.MappingSqlQuery} from the
 * {@code jdbc.object} package: Instead of working with separate
 * JdbcTemplate and RowMapper objects, you can build executable query
 * objects (containing row-mapping logic) in that style.
 * 
 * <p> 或者，考虑从jdbc.object包中继承org.springframework.jdbc.object.MappingSqlQuery：
 * 您可以使用该样式构建可执行查询对象（包含行映射逻辑），而不是使用单独的JdbcTemplate和RowMapper对象。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see ResultSetExtractor
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
public interface RowMapper<T> {

	/**
	 * Implementations must implement this method to map each row of data
	 * in the ResultSet. This method should not call {@code next()} on
	 * the ResultSet; it is only supposed to map values of the current row.
	 * 
	 * <p> 实现必须实现此方法以映射ResultSet中的每一行数据。 此方法不应调用ResultSet上的next（）; 
	 * 它只应该映射当前行的值。
	 * 
	 * @param rs the ResultSet to map (pre-initialized for the current row)
	 * 
	 * <p> 要映射的ResultSet（为当前行预先初始化）
	 * 
	 * @param rowNum the number of the current row
	 * 
	 * <p>  当前行的编号
	 * 
	 * @return the result object for the current row
	 * 
	 * <p> 当前行的结果对象
	 * 
	 * @throws SQLException if a SQLException is encountered getting
	 * column values (that is, there's no need to catch SQLException)
	 * 
	 * <p> 如果遇到SQLException获取列值（也就是说，不需要捕获SQLException）
	 * 
	 */
	T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
