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

package org.springframework.jdbc.datasource;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * Extension of the {@code javax.sql.DataSource} interface, to be
 * implemented by special DataSources that return JDBC Connections
 * in an unwrapped fashion.
 * 
 * <p> javax.sql.DataSource接口的扩展，由以解包方式返回JDBC Connections的特殊DataSource实现。
 *
 * <p>Classes using this interface can query whether or not the Connection
 * should be closed after an operation. Spring's DataSourceUtils and
 * JdbcTemplate classes automatically perform such a check.
 * 
 * <p> 使用此接口的类可以查询操作后是否应关闭Connection。 Spring的DataSourceUtils和
 * JdbcTemplate类自动执行这样的检查。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SingleConnectionDataSource#shouldClose
 * @see DataSourceUtils#releaseConnection
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public interface SmartDataSource extends DataSource {

	/**
	 * Should we close this Connection, obtained from this DataSource?
	 * 
	 * <p> 我们应该关闭从此DataSource获取的此连接吗？
	 * 
	 * <p>Code that uses Connections from a SmartDataSource should always
	 * perform a check via this method before invoking {@code close()}.
	 * 
	 * <p> 在调用close（）之前，使用来自SmartDataSource的Connections的代码应始终通过此方法执行检查。
	 * 
	 * <p>Note that the JdbcTemplate class in the 'jdbc.core' package takes care of
	 * releasing JDBC Connections, freeing application code of this responsibility.
	 * 
	 * <p> 请注意，'jdbc.core'包中的JdbcTemplate类负责释放JDBC Connections，释放此责任的应用程序代码。
	 * 
	 * @param con the Connection to check - 要检查的连接
	 * 
	 * @return whether the given Connection should be closed
	 * 
	 * <p> whether the given Connection should be closed
	 * 
	 * @see java.sql.Connection#close()
	 */
	boolean shouldClose(Connection con);

}
