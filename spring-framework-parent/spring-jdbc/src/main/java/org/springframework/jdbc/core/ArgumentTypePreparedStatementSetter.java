/*
 * Copyright 2002-2013 the original author or authors.
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
import java.sql.Types;
import java.util.Collection;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Simple adapter for {@link PreparedStatementSetter} that applies
 * given arrays of arguments and JDBC argument types.
 *
 * @author Juergen Hoeller
 * @since 3.2.3
 */
public class ArgumentTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final Object[] args;

	private final int[] argTypes;


	/**
	 * Create a new ArgTypePreparedStatementSetter for the given arguments.
	 * 
	 * <p> 为给定的参数创建一个新的ArgTypePreparedStatementSetter。
	 * 
	 * @param args the arguments to set - 要设置的参数
	 * @param argTypes the corresponding SQL types of the arguments - 参数的相应SQL类型
	 */
	public ArgumentTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
				(args != null && args.length != argTypes.length)) {
			throw new InvalidDataAccessApiUsageException("args and argTypes parameters must match");
		}
		this.args = args;
		this.argTypes = argTypes;
	}


	public void setValues(PreparedStatement ps) throws SQLException {
		int parameterPosition = 1;
		if (this.args != null) {
			//遍历每个参数以作类型匹配及转换
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				//如果是集合类则需要进行集合类内部递归解析集合内部属性
				if (arg instanceof Collection && this.argTypes[i] != Types.ARRAY) {
					Collection entries = (Collection) arg;
					for (Object entry : entries) {
						if (entry instanceof Object[]) {
							Object[] valueArray = ((Object[]) entry);
							for (Object argValue : valueArray) {
								doSetValue(ps, parameterPosition, this.argTypes[i], argValue);
								parameterPosition++;
							}
						}
						else {
							//解析当前属性
							doSetValue(ps, parameterPosition, this.argTypes[i], entry);
							parameterPosition++;
						}
					}
				}
				else {
					doSetValue(ps, parameterPosition, this.argTypes[i], arg);
					parameterPosition++;
				}
			}
		}
	}

	/**
	 * Set the value for the prepared statement's specified parameter position using the passed in
	 * value and type. This method can be overridden by sub-classes if needed.
	 * 
	 * <p> 使用传入的值和类型设置预准备语句的指定参数位置的值。 如果需要，可以通过子类覆盖此方法。
	 * 
	 * @param ps the PreparedStatement
	 * @param parameterPosition index of the parameter position - 参数位置的索引
	 * @param argType the argument type - 参数类型
	 * @param argValue the argument value 参数值
	 * @throws SQLException
	 */
	protected void doSetValue(PreparedStatement ps, int parameterPosition, int argType, Object argValue)
			throws SQLException {

		StatementCreatorUtils.setParameterValue(ps, parameterPosition, argType, argValue);
	}

	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
