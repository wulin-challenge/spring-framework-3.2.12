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

package org.springframework.transaction.support;

import java.util.Date;

import org.springframework.transaction.TransactionTimedOutException;

/**
 * Convenient base class for resource holders.
 * 
 * <p> 资源持有者的便捷基类。
 *
 * <p>Features rollback-only support for nested transactions.
 * Can expire after a certain number of seconds or milliseconds,
 * to determine transactional timeouts.
 * 
 * <p> 具有仅对回滚事务的回滚支持。 可以在一定的秒数或毫秒后过期，以确定事务超时。
 *
 * @author Juergen Hoeller
 * @since 02.02.2004
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin
 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
 */
public abstract class ResourceHolderSupport implements ResourceHolder {

	private boolean synchronizedWithTransaction = false;

	private boolean rollbackOnly = false;

	private Date deadline;

	private int referenceCount = 0;

	private boolean isVoid = false;


	/**
	 * Mark the resource as synchronized with a transaction.
	 * 
	 * <p> 将资源标记为与事务同步。
	 */
	public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction) {
		this.synchronizedWithTransaction = synchronizedWithTransaction;
	}

	/**
	 * Return whether the resource is synchronized with a transaction.
	 * 
	 * <p> 返回资源是否与事务同步。
	 */
	public boolean isSynchronizedWithTransaction() {
		return this.synchronizedWithTransaction;
	}

	/**
	 * Mark the resource transaction as rollback-only.
	 * 
	 * <p> 将资源事务标记为仅回滚。
	 */
	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	/**
	 * Return whether the resource transaction is marked as rollback-only.
	 * 
	 * <p> 返回资源事务是否标记为仅回滚。
	 * 
	 */
	public boolean isRollbackOnly() {
		return this.rollbackOnly;
	}

	/**
	 * Set the timeout for this object in seconds.
	 * 
	 * <p> 以秒为单位设置此对象的超时。
	 * 
	 * @param seconds number of seconds until expiration
	 * 
	 * <p> 到期前的秒数
	 */
	public void setTimeoutInSeconds(int seconds) {
		setTimeoutInMillis(seconds * 1000);
	}

	/**
	 * Set the timeout for this object in milliseconds.
	 * 
	 * <p> 设置此对象的超时（以毫秒为单位）。
	 * 
	 * @param millis number of milliseconds until expiration - 到期前的毫秒数
	 */
	public void setTimeoutInMillis(long millis) {
		this.deadline = new Date(System.currentTimeMillis() + millis);
	}

	/**
	 * Return whether this object has an associated timeout.
	 * 
	 * <p> 返回此对象是否具有关联的超时。
	 */
	public boolean hasTimeout() {
		return (this.deadline != null);
	}

	/**
	 * Return the expiration deadline of this object.
	 * 
	 * <p> 返回此对象的到期截止日期。
	 * 
	 * @return the deadline as Date object - 截止日期为Date对象
	 */
	public Date getDeadline() {
		return this.deadline;
	}

	/**
	 * Return the time to live for this object in seconds.
	 * Rounds up eagerly, e.g. 9.00001 still to 10.
	 * 
	 * <p> 在几秒钟内返回此对象的生存时间。 急切地，例如， 9.00001仍然是10。
	 * 
	 * @return number of seconds until expiration
	 * 
	 * <p> 到期前的秒数
	 * 
	 * @throws TransactionTimedOutException if the deadline has already been reached
	 * 
	 * <p> 如果截止日期已经达到
	 * 
	 */
	public int getTimeToLiveInSeconds() {
		double diff = ((double) getTimeToLiveInMillis()) / 1000;
		int secs = (int) Math.ceil(diff);
		checkTransactionTimeout(secs <= 0);
		return secs;
	}

	/**
	 * Return the time to live for this object in milliseconds.
	 * @return number of millseconds until expiration
	 * @throws TransactionTimedOutException if the deadline has already been reached
	 */
	public long getTimeToLiveInMillis() throws TransactionTimedOutException{
		if (this.deadline == null) {
			throw new IllegalStateException("No timeout specified for this resource holder");
		}
		long timeToLive = this.deadline.getTime() - System.currentTimeMillis();
		checkTransactionTimeout(timeToLive <= 0);
		return timeToLive;
	}

	/**
	 * Set the transaction rollback-only if the deadline has been reached,
	 * and throw a TransactionTimedOutException.
	 * 
	 * <p> 如果已达到截止日期，则仅设置事务回滚，并抛出TransactionTimedOutException。
	 * 
	 */
	private void checkTransactionTimeout(boolean deadlineReached) throws TransactionTimedOutException {
		if (deadlineReached) {
			setRollbackOnly();
			throw new TransactionTimedOutException("Transaction timed out: deadline was " + this.deadline);
		}
	}

	/**
	 * Increase the reference count by one because the holder has been requested
	 * (i.e. someone requested the resource held by it).
	 * 
	 * <p> 将引用计数增加1，因为已经请求了持有者（即有人请求其持有的资源）。
	 * 
	 */
	public void requested() {
		this.referenceCount++;
	}

	/**
	 * Decrease the reference count by one because the holder has been released
	 * (i.e. someone released the resource held by it).
	 * 
	 * <p> 将引用计数减1，因为持有者已被释放（即有人释放了它所持有的资源）。
	 * 
	 */
	public void released() {
		this.referenceCount--;
	}

	/**
	 * Return whether there are still open references to this holder.
	 * 
	 * <p> 返回是否仍有对该持有人的公开引用。
	 */
	public boolean isOpen() {
		return (this.referenceCount > 0);
	}

	/**
	 * Clear the transactional state of this resource holder.
	 * 
	 * <p> 清除此资源持有者的事务状态。
	 */
	public void clear() {
		this.synchronizedWithTransaction = false;
		this.rollbackOnly = false;
		this.deadline = null;
	}

	/**
	 * Reset this resource holder - transactional state as well as reference count.
	 * 
	 * <p> 重置此资源持有者 - 事务状态以及引用计数。
	 * 
	 */
	public void reset() {
		clear();
		this.referenceCount = 0;
	}

	public void unbound() {
		this.isVoid = true;
	}

	public boolean isVoid() {
		return this.isVoid;
	}

}
