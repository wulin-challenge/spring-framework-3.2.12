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

package org.springframework.jdbc.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple interface for complex types to be set as statement parameters.
 * 
 * <p> 将复杂类型的简单接口设置为语句参数。
 *
 * <p>Implementations perform the actual work of setting the actual values. They must
 * implement the callback method {@code setValue} which can throw SQLExceptions
 * that will be caught and translated by the calling code. This callback method has
 * access to the underlying Connection via the given PreparedStatement object, if that
 * should be needed to create any database-specific objects.
 * 
 * <p> 实现执行设置实际值的实际工作。 它们必须实现回调方法setValue，它可以抛出将被调用代码捕获和转换的SQLExceptions。 
 * 如果需要创建任何特定于数据库的对象，则此回调方法可以通过给定的PreparedStatement对象访问基础Connection。
 *
 * @author Juergen Hoeller
 * @since 2.5.6
 * @see org.springframework.jdbc.core.SqlTypeValue
 * @see org.springframework.jdbc.core.DisposableSqlTypeValue
 */
public interface SqlValue {

	/**
	 * Set the value on the given PreparedStatement.
	 * 
	 * <p> 在给定的PreparedStatement上设置值。
	 * 
	 * @param ps the PreparedStatement to work on
	 * 
	 * <p> 要处理的PreparedStatement
	 * 
	 * @param paramIndex the index of the parameter for which we need to set the value
	 * 
	 * <p> 我们需要为其设置值的参数的索引
	 * 
	 * @throws SQLException if a SQLException is encountered while setting parameter values
	 * 
	 * <p> 如果在设置参数值时遇到SQLException
	 * 
	 */
	void setValue(PreparedStatement ps, int paramIndex)	throws SQLException;

	/**
	 * Clean up resources held by this value object.
	 * 
	 * <p> 清理此值对象持有的资源。
	 * 
	 */
	void cleanup();

}
