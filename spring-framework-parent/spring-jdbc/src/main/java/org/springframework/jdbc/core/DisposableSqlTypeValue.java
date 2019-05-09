/*
 * Copyright 2002-2008 the original author or authors.
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
 * Subinterface of {@link SqlTypeValue} that adds a cleanup callback,
 * to be invoked after the value has been set and the corresponding
 * statement has been executed.
 * 
 * <p> SqlTypeValue的子接口，用于添加清理回调，在设置值并执行相应的语句后调用。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public interface DisposableSqlTypeValue extends SqlTypeValue {

	/**
	 * Clean up resources held by this type value,
	 * for example the LobCreator in case of a SqlLobValue.
	 * 
	 * <p> 清理此类型值所持有的资源，例如SqlLobValue时的LobCreator。
	 * 
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()
	 * @see org.springframework.jdbc.support.SqlValue#cleanup()
	 */
	void cleanup();

}
