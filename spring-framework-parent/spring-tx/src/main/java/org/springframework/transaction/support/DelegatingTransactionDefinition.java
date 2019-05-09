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

package org.springframework.transaction.support;

import java.io.Serializable;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;

/**
 * {@link TransactionDefinition} implementation that delegates all calls to a given target
 * {@link TransactionDefinition} instance. Abstract because it is meant to be subclassed,
 * with subclasses overriding specific methods that are not supposed to simply delegate
 * to the target instance.
 * 
 * <p> TransactionDefinition实现，它将所有调用委托给给定的目标TransactionDefinition实例。 摘要，
 * 因为它是子类，子类重写不应该简单地委托给目标实例的特定方法。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
@SuppressWarnings("serial")
public abstract class DelegatingTransactionDefinition implements TransactionDefinition, Serializable {

	private final TransactionDefinition targetDefinition;


	/**
	 * Create a DelegatingTransactionAttribute for the given target attribute.
	 * 
	 * <p> 为给定的目标属性创建DelegatingTransactionAttribute。
	 * 
	 * @param targetDefinition the target TransactionAttribute to delegate to
	 * 
	 * <p> 要委托的目标TransactionAttribute
	 * 
	 */
	public DelegatingTransactionDefinition(TransactionDefinition targetDefinition) {
		Assert.notNull(targetDefinition, "Target definition must not be null");
		this.targetDefinition = targetDefinition;
	}


	public int getPropagationBehavior() {
		return this.targetDefinition.getPropagationBehavior();
	}

	public int getIsolationLevel() {
		return this.targetDefinition.getIsolationLevel();
	}

	public int getTimeout() {
		return this.targetDefinition.getTimeout();
	}

	public boolean isReadOnly() {
		return this.targetDefinition.isReadOnly();
	}

	public String getName() {
		return this.targetDefinition.getName();
	}


	@Override
	public boolean equals(Object obj) {
		return this.targetDefinition.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.targetDefinition.hashCode();
	}

	@Override
	public String toString() {
		return this.targetDefinition.toString();
	}

}
