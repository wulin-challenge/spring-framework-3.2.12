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

import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Transaction attribute that takes the EJB approach to rolling
 * back on runtime, but not checked, exceptions.
 * 
 * <p> 使用EJB方法在运行时回滚但未检查异常的事务属性。
 *
 * @author Rod Johnson
 * @since 16.03.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

	private String qualifier;


	/**
	 * Create a new DefaultTransactionAttribute, with default settings.
	 * Can be modified through bean property setters.
	 * 
	 * <p> 使用默认设置创建新的DefaultTransactionAttribute。 可以通过bean属性setter进行修改。
	 * 
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionAttribute() {
		super();
	}

	/**
	 * Copy constructor. Definition can be modified through bean property setters.
	 * 
	 * <p> 复制构造函数。 可以通过bean属性设置器修改定义。
	 * 
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionAttribute(TransactionAttribute other) {
		super(other);
	}

	/**
	 * Create a new DefaultTransactionAttribute with the the given
	 * propagation behavior. Can be modified through bean property setters.
	 * 
	 * <p> 使用给定的传播行为创建新的DefaultTransactionAttribute。 可以通过bean属性setter进行修改。
	 * 
	 * @param propagationBehavior one of the propagation constants in the
	 * TransactionDefinition interface
	 * 
	 * <p> TransactionDefinition中的传播常量之一
	 * 
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 */
	public DefaultTransactionAttribute(int propagationBehavior) {
		super(propagationBehavior);
	}


	/**
	 * Associate a qualifier value with this transaction attribute.
	 * 
	 * <p> 将限定符值与此事务属性相关联。
	 * 
	 * <p>This may be used for choosing a corresponding transaction manager
	 * to process this specific transaction.
	 * 
	 * <p> 这可以用于选择相应的事务管理器来处理该特定事务。
	 * 
	 */
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * Return a qualifier value associated with this transaction attribute.
	 * 
	 * <p> 返回与此事务属性关联的限定符值。
	 * 
	 */
	public String getQualifier() {
		return this.qualifier;
	}

	/**
	 * The default behavior is as with EJB: rollback on unchecked exception.
	 * Additionally attempt to rollback on Error.
	 * 
	 * <p> 默认行为与EJB一样：回滚未经检查的异常。 另外尝试回滚错误。
	 * 
	 * <p>This is consistent with TransactionTemplate's default behavior.
	 * 
	 * <p> 这与TransactionTemplate的默认行为一致。
	 * 
	 */
	public boolean rollbackOn(Throwable ex) {
		return (ex instanceof RuntimeException || ex instanceof Error);
	}


	/**
	 * Return an identifying description for this transaction attribute.
	 * 
	 * <p> 返回此事务属性的标识说明。
	 * 
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 * 
	 * <p> 可用于子类，包含在其toString（）结果中。
	 * 
	 */
	protected final StringBuilder getAttributeDescription() {
		StringBuilder result = getDefinitionDescription();
		if (this.qualifier != null) {
			result.append("; '").append(this.qualifier).append("'");
		}
		return result;
	}

}
