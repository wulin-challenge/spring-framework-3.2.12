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
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Adapter implementation of the ResultSetExtractor interface that delegates
 * to a RowMapper which is supposed to create an object for each row.
 * Each object is added to the results List of this ResultSetExtractor.
 * 
 * <p> ResultSetExtractor接口的适配器实现，该接口委托给RowMapper，RowMapper应该为每一行创建一个对象。 
 * 每个对象都添加到此ResultSetExtractor的结果列表中。
 *
 * <p>Useful for the typical case of one object per row in the database table.
 * The number of entries in the results list will match the number of rows.
 * 
 * <p> 对于数据库表中每行一个对象的典型情况很有用。 结果列表中的条目数将与行数匹配。
 *
 * <p>Note that a RowMapper object is typically stateless and thus reusable;
 * just the RowMapperResultSetExtractor adapter is stateful.
 * 
 * <p> 请注意，RowMapper对象通常是无状态的，因此可以重用; 只是RowMapperResultSetExtractor适配器是有状态的。
 *
 * <p>A usage example with JdbcTemplate:
 * 
 * <p> JdbcTemplate的用法示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object
 * RowMapper rowMapper = new UserRowMapper();  // reusable object
 *
 * List allUsers = (List) jdbcTemplate.query(
 *     "select * from user",
 *     new RowMapperResultSetExtractor(rowMapper, 10));
 *
 * User user = (User) jdbcTemplate.queryForObject(
 *     "select * from user where id=?", new Object[] {id},
 *     new RowMapperResultSetExtractor(rowMapper, 1));</pre>
 *
 * <p>Alternatively, consider subclassing MappingSqlQuery from the {@code jdbc.object}
 * package: Instead of working with separate JdbcTemplate and RowMapper objects,
 * you can have executable query objects (containing row-mapping logic) there.
 * 
 * <p> 或者，考虑从jdbc.object包中继承MappingSqlQuery：您可以在那里使用可执行查询对象（包含行映射逻辑），
 * 而不是使用单独的JdbcTemplate和RowMapper对象。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see RowMapper
 * @see JdbcTemplate
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

	private final RowMapper<T> rowMapper;

	private final int rowsExpected;


	/**
	 * Create a new RowMapperResultSetExtractor.
	 * 
	 * <p> 创建一个新的RowMapperResultSetExtractor。
	 * 
	 * @param rowMapper the RowMapper which creates an object for each row
	 * 
	 * <p> RowMapper为每一行创建一个对象
	 * 
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
		this(rowMapper, 0);
	}

	/**
	 * Create a new RowMapperResultSetExtractor.
	 * 
	 * <p> 创建一个新的RowMapperResultSetExtractor。
	 * 
	 * @param rowMapper the RowMapper which creates an object for each row
	 * 
	 * <p> RowMapper为每一行创建一个对象
	 * 
	 * @param rowsExpected the number of expected rows
	 * (just used for optimized collection handling)
	 * 
	 * <p> 预期行数（仅用于优化集合处理）
	 * 
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
		Assert.notNull(rowMapper, "RowMapper is required");
		this.rowMapper = rowMapper;
		this.rowsExpected = rowsExpected;
	}


	public List<T> extractData(ResultSet rs) throws SQLException {
		List<T> results = (this.rowsExpected > 0 ? new ArrayList<T>(this.rowsExpected) : new ArrayList<T>());
		int rowNum = 0;
		while (rs.next()) {
			results.add(this.rowMapper.mapRow(rs, rowNum++));
		}
		return results;
	}

}
