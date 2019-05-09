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

package org.springframework.transaction;

/**
 * Interface that specifies an API to programmatically manage transaction
 * savepoints in a generic fashion. Extended by TransactionStatus to
 * expose savepoint management functionality for a specific transaction.
 * 
 * <p> 指定API的接口，以通用方式以编程方式管理事务保存点。通过TransactionStatus进行扩展，以显示特定事务的保存点管理功能。
 *
 * <p>Note that savepoints can only work within an active transaction.
 * Just use this programmatic savepoint handling for advanced needs;
 * else, a subtransaction with PROPAGATION_NESTED is preferable.
 * 
 * <p> 请注意，保存点只能在活动事务中工作。只需使用此程序化保存点处理即可满足高级需求;否则，最好使用PROPAGATION_NESTED进行子事务。
 *
 * <p>This interface is inspired by JDBC 3.0's Savepoint mechanism
 * but is independent from any specific persistence technology.
 * 
 * <p> 此接口受JDBC 3.0的Savepoint机制启发，但独立于任何特定的持久性技术。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionStatus
 * @see TransactionDefinition#PROPAGATION_NESTED
 * @see java.sql.Savepoint
 */
public interface SavepointManager {

	/**
	 * Create a new savepoint. You can roll back to a specific savepoint
	 * via {@code rollbackToSavepoint}, and explicitly release a
	 * savepoint that you don't need anymore via {@code releaseSavepoint}.
	 * 
	 * <p> 创建一个新的保存点。 您可以通过rollbackToSavepoint回滚到特定的保存点，并通过releaseSavepoint显式释放您不再需要的保存点。
	 * 
	 * <p>Note that most transaction managers will automatically release
	 * savepoints at transaction completion.
	 * 
	 * <p> 请注意，大多数事务管理器将在事务完成时自动释放保存点。
	 * 
	 * @return a savepoint object, to be passed into rollbackToSavepoint
	 * or releaseSavepoint
	 * 
	 * <p> 要传递给rollbackToSavepoint或releaseSavepoint的保存点对象
	 * 
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * 
	 * <p> 如果基础事务不支持保存点
	 * 
	 * @throws TransactionException if the savepoint could not be created,
	 * for example because the transaction is not in an appropriate state
	 * 
	 * <p> 如果无法创建保存点，例如因为事务处于不适当的状态
	 * 
	 * @see java.sql.Connection#setSavepoint
	 */
	Object createSavepoint() throws TransactionException;

	/**
	 * Roll back to the given savepoint. The savepoint will be
	 * automatically released afterwards.
	 * 
	 * <p> 回滚到给定的保存点。 保存点将在之后自动释放。
	 * 
	 * @param savepoint the savepoint to roll back to
	 * 
	 * <p> 要回滚的保存点
	 * 
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * 
	 * <p> 如果基础事务不支持保存点
	 * 
	 * @throws TransactionException if the rollback failed
	 * 
	 * <p> 如果回滚失败
	 * 
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

	/**
	 * Explicitly release the given savepoint.
	 * 
	 * <p> 显式释放给定的保存点。
	 * 
	 * <p>Note that most transaction managers will automatically release
	 * savepoints at transaction completion.
	 * 
	 * <p> 请注意，大多数事务管理器将在事务完成时自动释放保存点。
	 * 
	 * <p>Implementations should fail as silently as possible if
	 * proper resource cleanup will still happen at transaction completion.
	 * 
	 * <p> 如果在事务完成时仍然会进行适当的资源清理，那么实现应尽可能无声地失败。
	 * 
	 * @param savepoint the savepoint to release - 要发布的保存点
	 * 
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * 
	 * <p> 如果基础事务不支持保存点
	 * 
	 * @throws TransactionException if the release failed
	 * 
	 * <p> 如果发布失败
	 * 
	 * @see java.sql.Connection#releaseSavepoint
	 */
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
