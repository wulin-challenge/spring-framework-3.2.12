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

package org.springframework.jdbc.core;

/**
 * Interface to be implemented by objects that can close resources
 * allocated by parameters like SqlLobValues.
 * 
 * <p> 由可以关闭由SqlLobValues等参数分配的资源的对象实现的接口。
 *
 * <p>Typically implemented by PreparedStatementCreators and
 * PreparedStatementSetters that support DisposableSqlTypeValue
 * objects (e.g. SqlLobValue) as parameters.
 * 
 * <p> 通常由PreparedStatementCreators和PreparedStatementSetters实现，它们支持
 * DisposableSqlTypeValue对象（例如SqlLobValue）作为参数。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see PreparedStatementCreator
 * @see PreparedStatementSetter
 * @see DisposableSqlTypeValue
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public interface ParameterDisposer {

	/**
	 * Close the resources allocated by parameters that the implementing
	 * object holds, for example in case of a DisposableSqlTypeValue
	 * (like a SqlLobValue).
	 * 
	 * <p> 关闭由实现对象保存的参数分配的资源，例如，在DisposableSqlTypeValue（如SqlLobValue）的情况下。
	 * 
	 * @see DisposableSqlTypeValue#cleanup
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup
	 */
	public void cleanupParameters();

}
