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

import org.springframework.core.Constants;
import org.springframework.transaction.TransactionDefinition;

/**
 * Default implementation of the {@link TransactionDefinition} interface,
 * offering bean-style configuration and sensible default values
 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).
 * 
 * <p> TransactionDefinition接口的默认实现，提供bean样式配置和合理的默认值
 * （PROPAGATION_REQUIRED，ISOLATION_DEFAULT，TIMEOUT_DEFAULT，readOnly = false）。
 *
 * <p>Base class for both {@link TransactionTemplate} and
 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute}.
 * 
 * <p> TransactionTemplate和
 * org.springframework.transaction.interceptor.DefaultTransactionAttribute的基类。
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

	/** Prefix for the propagation constants defined in TransactionDefinition */
	/** TransactionDefinition中定义的传播常量的前缀 */
	public static final String PREFIX_PROPAGATION = "PROPAGATION_";

	/** Prefix for the isolation constants defined in TransactionDefinition */
	/** TransactionDefinition中定义的隔离常量的前缀 */
	public static final String PREFIX_ISOLATION = "ISOLATION_";

	/** Prefix for transaction timeout values in description strings */
	/** 描述字符串中事务超时值的前缀 */
	public static final String PREFIX_TIMEOUT = "timeout_";

	/** Marker for read-only transactions in description strings */
	/** 标记用于描述字符串中的只读事务 */
	public static final String READ_ONLY_MARKER = "readOnly";


	/** Constants instance for TransactionDefinition */
	/** TransactionDefinition的常量实例 */
	static final Constants constants = new Constants(TransactionDefinition.class);

	private int propagationBehavior = PROPAGATION_REQUIRED;

	private int isolationLevel = ISOLATION_DEFAULT;

	private int timeout = TIMEOUT_DEFAULT;

	private boolean readOnly = false;

	private String name;


	/**
	 * Create a new DefaultTransactionDefinition, with default settings.
	 * Can be modified through bean property setters.
	 * 
	 * <p> 使用默认设置创建新的DefaultTransactionDefinition。 可以通过bean属性setter进行修改。
	 * 
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionDefinition() {
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
	public DefaultTransactionDefinition(TransactionDefinition other) {
		this.propagationBehavior = other.getPropagationBehavior();
		this.isolationLevel = other.getIsolationLevel();
		this.timeout = other.getTimeout();
		this.readOnly = other.isReadOnly();
		this.name = other.getName();
	}

	/**
	 * Create a new DefaultTransactionDefinition with the the given
	 * propagation behavior. Can be modified through bean property setters.
	 * 
	 * <p> 使用给定的传播行为创建新的DefaultTransactionDefinition。 
	 * 可以通过bean属性setter进行修改。
	 * 
	 * @param propagationBehavior one of the propagation constants in the
	 * TransactionDefinition interface
	 * 
	 * <p> TransactionDefinition接口中的传播常量之一
	 * 
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 */
	public DefaultTransactionDefinition(int propagationBehavior) {
		this.propagationBehavior = propagationBehavior;
	}


	/**
	 * Set the propagation behavior by the name of the corresponding constant in
	 * TransactionDefinition, e.g. "PROPAGATION_REQUIRED".
	 * 
	 * <p> 在TransactionDefinition中通过相应常量的名称设置传播行为，例如，“PROPAGATION_REQUIRED”。
	 * 
	 * @param constantName name of the constant
	 * 
	 * <p> 常数的名称
	 * 
	 * @exception IllegalArgumentException if the supplied value is not resolvable
	 * to one of the {@code PROPAGATION_} constants or is {@code null}
	 * 
	 * <p> 如果提供的值无法解析为其中一个PROPAGATION_常量或为null
	 * 
	 * @see #setPropagationBehavior
	 * @see #PROPAGATION_REQUIRED
	 */
	public final void setPropagationBehaviorName(String constantName) throws IllegalArgumentException {
		if (constantName == null || !constantName.startsWith(PREFIX_PROPAGATION)) {
			throw new IllegalArgumentException("Only propagation constants allowed");
		}
		setPropagationBehavior(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the propagation behavior. Must be one of the propagation constants
	 * in the TransactionDefinition interface. Default is PROPAGATION_REQUIRED.
	 * 
	 * <p> 设置传播行为。 必须是TransactionDefinition接口中的传播常量之一。 默认为PROPAGATION_REQUIRED。
	 * 
	 * @exception IllegalArgumentException if the supplied value is not
	 * one of the {@code PROPAGATION_} constants
	 * 
	 * <p> 如果提供的值不是PROPAGATION_常量之一
	 * 
	 * @see #PROPAGATION_REQUIRED
	 */
	public final void setPropagationBehavior(int propagationBehavior) {
		if (!constants.getValues(PREFIX_PROPAGATION).contains(propagationBehavior)) {
			throw new IllegalArgumentException("Only values of propagation constants allowed");
		}
		this.propagationBehavior = propagationBehavior;
	}

	public final int getPropagationBehavior() {
		return this.propagationBehavior;
	}

	/**
	 * Set the isolation level by the name of the corresponding constant in
	 * TransactionDefinition, e.g. "ISOLATION_DEFAULT".
	 * 
	 * <p> 在TransactionDefinition中通过相应常量的名称设置隔离级别，例如，“ISOLATION_DEFAULT”。
	 * 
	 * @param constantName name of the constant
	 * 
	 * <p> 常数的名称
	 * 
	 * @exception IllegalArgumentException if the supplied value is not resolvable
	 * to one of the {@code ISOLATION_} constants or is {@code null}
	 * 
	 * <p> 如果提供的值无法解析为其中一个ISOLATION_常量或为null
	 * 
	 * @see #setIsolationLevel
	 * @see #ISOLATION_DEFAULT
	 */
	public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
		if (constantName == null || !constantName.startsWith(PREFIX_ISOLATION)) {
			throw new IllegalArgumentException("Only isolation constants allowed");
		}
		setIsolationLevel(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the isolation level. Must be one of the isolation constants
	 * in the TransactionDefinition interface. Default is ISOLATION_DEFAULT.
	 * 
	 * <p> 设置隔离级别。 必须是TransactionDefinition接口中的隔离常量之一。 默认值为ISOLATION_DEFAULT。
	 * 
	 * @exception IllegalArgumentException if the supplied value is not
	 * one of the {@code ISOLATION_} constants
	 * 
	 * <p> 如果提供的值不是ISOLATION_常量之一
	 * 
	 * @see #ISOLATION_DEFAULT
	 */
	public final void setIsolationLevel(int isolationLevel) {
		if (!constants.getValues(PREFIX_ISOLATION).contains(isolationLevel)) {
			throw new IllegalArgumentException("Only values of isolation constants allowed");
		}
		this.isolationLevel = isolationLevel;
	}

	public final int getIsolationLevel() {
		return this.isolationLevel;
	}

	/**
	 * Set the timeout to apply, as number of seconds.
	 * Default is TIMEOUT_DEFAULT (-1).
	 * 
	 * <p> 设置要应用的超时，以秒为单位。 默认值为TIMEOUT_DEFAULT（-1）。
	 * 
	 * @see #TIMEOUT_DEFAULT
	 */
	public final void setTimeout(int timeout) {
		if (timeout < TIMEOUT_DEFAULT) {
			throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
		}
		this.timeout = timeout;
	}

	public final int getTimeout() {
		return this.timeout;
	}

	/**
	 * Set whether to optimize as read-only transaction.
	 * Default is "false".
	 * 
	 * <p> 设置是否优化为只读事务。 默认为“false”。
	 * 
	 */
	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public final boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Set the name of this transaction. Default is none.
	 * 
	 * <p> 设置此事务的名称。 默认为none。
	 * 
	 * <p>This will be used as transaction name to be shown in a
	 * transaction monitor, if applicable (for example, WebLogic's).
	 * 
	 * <p> 这将用作事务监视器中显示的事务名称（如果适用）（例如，WebLogic的）。
	 * 
	 */
	public final void setName(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}


	/**
	 * This implementation compares the {@code toString()} results.
	 * 
	 * <p> 此实现比较toString（）结果。
	 * 
	 * @see #toString()
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof TransactionDefinition && toString().equals(other.toString()));
	}

	/**
	 * This implementation returns {@code toString()}'s hash code.
	 * 
	 * <p> 此实现返回toString（）的哈希码。
	 * 
	 * @see #toString()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Return an identifying description for this transaction definition.
	 * 
	 * <p> 返回此事务定义的标识说明。
	 * 
	 * <p>The format matches the one used by
	 * {@link org.springframework.transaction.interceptor.TransactionAttributeEditor},
	 * to be able to feed {@code toString} results into bean properties of type
	 * {@link org.springframework.transaction.interceptor.TransactionAttribute}.
	 * 
	 * <p> 格式与org.springframework.transaction.interceptor.TransactionAttributeEditor
	 * 使用的格式匹配，以便能够将toString结果提供给
	 * org.springframework.transaction.interceptor.TransactionAttribute类型的bean属性。
	 * 
	 * <p>Has to be overridden in subclasses for correct {@code equals}
	 * and {@code hashCode} behavior. Alternatively, {@link #equals}
	 * and {@link #hashCode} can be overridden themselves.
	 * 
	 * <p> 必须在子类中重写以获得正确的equals和hashCode行为。 或者，可以自己覆盖equals和hashCode。
	 * 
	 * @see #getDefinitionDescription()
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	@Override
	public String toString() {
		return getDefinitionDescription().toString();
	}

	/**
	 * Return an identifying description for this transaction definition.
	 * 
	 * <p> 返回此事务定义的标识说明。
	 * 
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 * 
	 * <p> 可用于子类，包含在其toString（）结果中。
	 * 
	 */
	protected final StringBuilder getDefinitionDescription() {
		StringBuilder result = new StringBuilder();
		result.append(constants.toCode(this.propagationBehavior, PREFIX_PROPAGATION));
		result.append(',');
		result.append(constants.toCode(this.isolationLevel, PREFIX_ISOLATION));
		if (this.timeout != TIMEOUT_DEFAULT) {
			result.append(',');
			result.append(PREFIX_TIMEOUT).append(this.timeout);
		}
		if (this.readOnly) {
			result.append(',');
			result.append(READ_ONLY_MARKER);
		}
		return result;
	}

}
