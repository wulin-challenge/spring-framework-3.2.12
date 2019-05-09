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

package org.springframework.transaction.interceptor;

import java.io.Serializable;

import org.springframework.transaction.support.DelegatingTransactionDefinition;

/**
 * {@link TransactionAttribute} implementation that delegates all calls to a given target
 * {@link TransactionAttribute} instance. Abstract because it is meant to be subclassed,
 * with subclasses overriding specific methods that are not supposed to simply delegate
 * to the target instance.
 * 
 * <p> TransactionAttribute实现，它将所有调用委托给给定的目标TransactionAttribute实例。 
 * 摘要，因为它是子类，子类重写不应该简单地委托给目标实例的特定方法。
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
@SuppressWarnings("serial")
public abstract class DelegatingTransactionAttribute extends DelegatingTransactionDefinition
		implements TransactionAttribute, Serializable {

	private final TransactionAttribute targetAttribute;


	/**
	 * Create a DelegatingTransactionAttribute for the given target attribute.
	 * 
	 * <p> 为给定的目标属性创建DelegatingTransactionAttribute。
	 * 
	 * @param targetAttribute the target TransactionAttribute to delegate to
	 * 
	 * <p> 要委托的目标TransactionAttribute
	 */
	public DelegatingTransactionAttribute(TransactionAttribute targetAttribute) {
		super(targetAttribute);
		this.targetAttribute = targetAttribute;
	}


	public String getQualifier() {
		return this.targetAttribute.getQualifier();
	}

	public boolean rollbackOn(Throwable ex) {
		return this.targetAttribute.rollbackOn(ex);
	}

}
