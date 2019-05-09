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

/**
 * Object to represent a SQL parameter value, including parameter metadata
 * such as the SQL type and the scale for numeric values.
 * 
 * <p> 用于表示SQL参数值的对象，包括参数元数据，如SQL类型和数值的比例。
 *
 * <p>Designed for use with {@link JdbcTemplate}'s operations that take an array of
 * argument values: Each such argument value may be a {@code SqlParameterValue},
 * indicating the SQL type (and optionally the scale) instead of letting the
 * template guess a default type. Note that this only applies to the operations with
 * a 'plain' argument array, not to the overloaded variants with an explicit type array.
 * 
 * <p> 设计用于带有参数值数组的JdbcTemplate操作：每个这样的参数值可以是SqlParameterValue，指示SQL类型（以及可选的比例），
 * 而不是让模板猜测默认类型。 请注意，这仅适用于具有“plain”参数数组的操作，而不适用于具有显式类型数组的重载变体。
 *
 * @author Juergen Hoeller
 * @since 2.0.5
 * @see java.sql.Types
 * @see JdbcTemplate#query(String, Object[], ResultSetExtractor)
 * @see JdbcTemplate#query(String, Object[], RowCallbackHandler)
 * @see JdbcTemplate#query(String, Object[], RowMapper)
 * @see JdbcTemplate#update(String, Object[])
 */
public class SqlParameterValue extends SqlParameter {

	private final Object value;


	/**
	 * Create a new SqlParameterValue, supplying the SQL type.
	 * 
	 * <p> 创建一个新的SqlParameterValue，提供SQL类型。
	 * 
	 * @param sqlType SQL type of the parameter according to {@code java.sql.Types}
	 * 
	 * <p> 根据java.sql.Types的参数的SQL类型
	 * 
	 * @param value the value object- 值对象
	 */
	public SqlParameterValue(int sqlType, Object value) {
		super(sqlType);
		this.value = value;
	}

	/**
	 * Create a new SqlParameterValue, supplying the SQL type.
	 * 
	 * <p> 创建一个新的SqlParameterValue，提供SQL类型。
	 * 
	 * @param sqlType SQL type of the parameter according to {@code java.sql.Types}
	 * 
	 * <p> 根据java.sql.Types的参数的SQL类型
	 * 
	 * @param typeName the type name of the parameter (optional)
	 * 
	 * <p> 参数的类型名称（可选）
	 * 
	 * @param value the value object - 对象值
	 */
	public SqlParameterValue(int sqlType, String typeName, Object value) {
		super(sqlType, typeName);
		this.value = value;
	}

	/**
	 * Create a new SqlParameterValue, supplying the SQL type.
	 * 
	 * <p> 创建一个新的SqlParameterValue，提供SQL类型。
	 * 
	 * @param sqlType SQL type of the parameter according to {@code java.sql.Types}
	 * 
	 * <p> 根据java.sql.Types的参数的SQL类型
	 * 
	 * @param scale the number of digits after the decimal point
	 * (for DECIMAL and NUMERIC types)
	 * 
	 * <p>  the number of digits after the decimal point (for DECIMAL and NUMERIC types)
	 * 
	 * @param value the value object - 值对象
	 */
	public SqlParameterValue(int sqlType, int scale, Object value) {
		super(sqlType, scale);
		this.value = value;
	}

	/**
	 * Create a new SqlParameterValue based on the given SqlParameter declaration.
	 * 
	 * <p> 根据给定的SqlParameter声明创建一个新的SqlParameterValue。
	 * 
	 * @param declaredParam the declared SqlParameter to define a value for
	 * 
	 * <p> 声明的SqlParameter为其定义一个值
	 * 
	 * @param value the value object - 值对象
	 */
	public SqlParameterValue(SqlParameter declaredParam, Object value) {
		super(declaredParam);
		this.value = value;
	}


	/**
	 * Return the value object that this parameter value holds.
	 * 
	 * <p> 返回此参数值包含的值对象。
	 */
	public Object getValue() {
		return this.value;
	}

}
