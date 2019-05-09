/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jdbc.datasource;

import java.sql.Connection;

/**
 * Simple interface to be implemented by handles for a JDBC Connection.
 * Used by JdoDialect, for example.
 * 
 * <p> 由JDBC连接的句柄实现的简单接口。 例如，由JdoDialect使用。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see SimpleConnectionHandle
 * @see ConnectionHolder
 * @see org.springframework.orm.jdo.JdoDialect#getJdbcConnection
 */
public interface ConnectionHandle {

	/**
	 * Fetch the JDBC Connection that this handle refers to.
	 * 
	 * <p> 获取此句柄引用的JDBC连接。
	 */
	Connection getConnection();

	/**
	 * Release the JDBC Connection that this handle refers to.
	 * 
	 * <p> 释放此句柄引用的JDBC连接。
	 * 
	 * @param con the JDBC Connection to release
	 * 
	 * <p> 要发布的JDBC连接
	 * 
	 */
	void releaseConnection(Connection con);

}
