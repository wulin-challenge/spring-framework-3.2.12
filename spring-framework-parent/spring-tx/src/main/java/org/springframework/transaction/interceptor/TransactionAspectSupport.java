/*
 * Copyright 2002-2013 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.core.NamedThreadLocal;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Base class for transactional aspects, such as the {@link TransactionInterceptor}
 * or an AspectJ aspect.
 * 
 * <p> 事务方面的基类，例如TransactionInterceptor或AspectJ方面。
 *
 * <p>This enables the underlying Spring transaction infrastructure to be used easily
 * to implement an aspect for any aspect system.
 * 
 * <p> 这使得基础Spring事务基础结构可以轻松地用于实现任何方面系统的方面。
 *
 * <p>Subclasses are responsible for calling methods in this class in the correct order.
 * 
 * <p> 子类负责以正确的顺序调用此类中的方法。
 *
 * <p>If no transaction name has been specified in the {@code TransactionAttribute},
 * the exposed name will be the {@code fully-qualified class name + "." + method name}
 * (by default).
 * 
 * <p> 如果在TransactionAttribute中未指定任何事务名称，则公开的名称将是完全限定的类名+“。”。 +方法名称（默认情况下）。
 *
 * <p>Uses the <b>Strategy</b> design pattern. A {@code PlatformTransactionManager}
 * implementation will perform the actual transaction management, and a
 * {@code TransactionAttributeSource} is used for determining transaction definitions.
 * 
 * <p> 使用策略设计模式。 PlatformTransactionManager实现将执行实际的事务管理，TransactionAttributeSource用于确定事务定义。
 *
 * <p>A transaction aspect is serializable if its {@code PlatformTransactionManager}
 * and {@code TransactionAttributeSource} are serializable.
 * 
 * <p> 如果事务方面的PlatformTransactionManager和TransactionAttributeSource是可序列化的，则它是可序列化的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setTransactionManager
 * @see #setTransactionAttributes
 * @see #setTransactionAttributeSource
 */
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {

	// NOTE: This class must not implement Serializable because it serves as base
	// class for AspectJ aspects (which are not allowed to implement Serializable)!
	
	// 注意：此类不能实现Serializable，因为它作为AspectJ方面的基类（不允许实现Serializable）！

	/**
	 * Holder to support the {@code currentTransactionStatus()} method,
	 * and to support communication between different cooperating advices
	 * (e.g. before and after advice) if the aspect involves more than a
	 * single method (as will be the case for around advice).
	 * 
	 * <p> 持有者支持currentTransactionStatus（）方法，并支持不同合作建议之间的通信（例如，在建议之前和之后），
	 * 如果方面涉及多个方法（就像周围建议的情况那样）。
	 */
	private static final ThreadLocal<TransactionInfo> transactionInfoHolder =
			new NamedThreadLocal<TransactionInfo>("Current aspect-driven transaction");


	/**
	 * Subclasses can use this to return the current TransactionInfo.
	 * Only subclasses that cannot handle all operations in one method,
	 * such as an AspectJ aspect involving distinct before and after advice,
	 * need to use this mechanism to get at the current TransactionInfo.
	 * An around advice such as an AOP Alliance MethodInterceptor can hold a
	 * reference to the TransactionInfo throughout the aspect method.
	 * 
	 * <p> 子类可以使用它来返回当前的TransactionInfo。 只有在一个方法中无法处理所有操作的子类，
	 * 例如涉及不同建议之前和之后的AspectJ方面，才需要使用此机制来获取当前的TransactionInfo。 
	 * 诸如AOP Alliance MethodInterceptor之类的建议可以在整个方面方法中保持对TransactionInfo的引用。
	 * 
	 * <p>A TransactionInfo will be returned even if no transaction was created.
	 * The {@code TransactionInfo.hasTransaction()} method can be used to query this.
	 * 
	 * <p> 即使没有创建任何事务，也将返回TransactionInfo。 TransactionInfo.hasTransaction（）方法可用于查询此方法。
	 * 
	 * <p>To find out about specific transaction characteristics, consider using
	 * TransactionSynchronizationManager's {@code isSynchronizationActive()}
	 * and/or {@code isActualTransactionActive()} methods.
	 * 
	 * <p> 要了解特定的事务特征，请考虑使用TransactionSynchronizationManager的isSynchronizationActive（）
	 * 和/或isActualTransactionActive（）方法。

	 * @return TransactionInfo bound to this thread, or {@code null} if none
	 * 
	 * <p> TransactionInfo绑定到此线程，如果没有则为null
	 * 
	 * @see TransactionInfo#hasTransaction()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isSynchronizationActive()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	protected static TransactionInfo currentTransactionInfo() throws NoTransactionException {
		return transactionInfoHolder.get();
	}

	/**
	 * Return the transaction status of the current method invocation.
	 * Mainly intended for code that wants to set the current transaction
	 * rollback-only but not throw an application exception.
	 * 
	 * <p> 返回当前方法调用的事务状态。 主要用于想要设置当前事务仅回滚但不抛出应用程序异常的代码。
	 * 
	 * @throws NoTransactionException if the transaction info cannot be found,
	 * because the method was invoked outside an AOP invocation context
	 * 
	 * <p> 如果找不到事务信息，因为该方法是在AOP调用上下文之外调用的
	 * 
	 */
	public static TransactionStatus currentTransactionStatus() throws NoTransactionException {
		TransactionInfo info = currentTransactionInfo();
		if (info == null) {
			throw new NoTransactionException("No transaction aspect-managed TransactionStatus in scope");
		}
		return currentTransactionInfo().transactionStatus;
	}


	protected final Log logger = LogFactory.getLog(getClass());

	private String transactionManagerBeanName;

	private PlatformTransactionManager transactionManager;

	private TransactionAttributeSource transactionAttributeSource;

	private BeanFactory beanFactory;


	/**
	 * Specify the name of the default transaction manager bean.
	 * 
	 * <p> 指定缺省事务管理器bean的名称。
	 */
	public void setTransactionManagerBeanName(String transactionManagerBeanName) {
		this.transactionManagerBeanName = transactionManagerBeanName;
	}

	/**
	 * Return the name of the default transaction manager bean.
	 * 
	 * <p> 返回默认事务管理器bean的名称。
	 */
	protected final String getTransactionManagerBeanName() {
		return this.transactionManagerBeanName;
	}

	/**
	 * Specify the target transaction manager.
	 * 
	 * <p> 指定目标事务管理器。
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Return the transaction manager, if specified.
	 */
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	/**
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * <p>Internally, a NameMatchTransactionAttributeSource will be
	 * created from the given properties.
	 * @see #setTransactionAttributeSource
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		tas.setProperties(transactionAttributes);
		this.transactionAttributeSource = tas;
	}

	/**
	 * Set multiple transaction attribute sources which are used to find transaction
	 * attributes. Will build a CompositeTransactionAttributeSource for the given sources.
	 * @see CompositeTransactionAttributeSource
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSources(TransactionAttributeSource[] transactionAttributeSources) {
		this.transactionAttributeSource = new CompositeTransactionAttributeSource(transactionAttributeSources);
	}

	/**
	 * Set the transaction attribute source which is used to find transaction
	 * attributes. If specifying a String property value, a PropertyEditor
	 * will create a MethodMapTransactionAttributeSource from the value.
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}

	/**
	 * Return the transaction attribute source.
	 * 
	 * <p> 返回事务属性源。
	 * 
	 */
	public TransactionAttributeSource getTransactionAttributeSource() {
		return this.transactionAttributeSource;
	}

	/**
	 * Set the BeanFactory to use for retrieving PlatformTransactionManager beans.
	 * 
	 * <p> 设置BeanFactory以用于检索PlatformTransactionManager bean。
	 * 
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the BeanFactory to use for retrieving PlatformTransactionManager beans.
	 * 
	 * <p> 返回BeanFactory以用于检索PlatformTransactionManager bean。
	 * 
	 */
	protected final BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Check that required properties were set.
	 * 
	 * <p> 检查是否已设置所需的属性。
	 * 
	 */
	public void afterPropertiesSet() {
		if (this.transactionManager == null && this.beanFactory == null) {
			throw new IllegalStateException(
					"Setting the property 'transactionManager' or running in a ListableBeanFactory is required");
		}
		if (this.transactionAttributeSource == null) {
			throw new IllegalStateException(
					"Either 'transactionAttributeSource' or 'transactionAttributes' is required: " +
					"If there are no transactional methods, then don't use a transaction aspect.");
		}
	}


	/**
	 * General delegate for around-advice-based subclasses, delegating to several other template
	 * methods on this class. Able to handle {@link CallbackPreferringPlatformTransactionManager}
	 * as well as regular {@link PlatformTransactionManager} implementations.
	 * 
	 * <p> 基于周围advice的子类的一般委托，委托给这个类的其他几个模板方法。 
	 * 能够处理CallbackPreferringPlatformTransactionManager以及常规的PlatformTransactionManager实现。
	 * 
	 * @param method the Method being invoked - 正在调用的方法
	 * 
	 * @param targetClass the target class that we're invoking the method on
	 * 
	 * <p> 我们正在调用该方法的目标类
	 * 
	 * @param invocation the callback to use for proceeding with the target invocation
	 * 
	 * <p> 用于继续目标调用的回调
	 * 
	 * @return the return value of the method, if any
	 * 
	 * <p> 方法的返回值，如果有的话
	 * 
	 * @throws Throwable propagated from the target invocation
	 * 
	 * <p> 从目标调用传播
	 * 
	 */
	protected Object invokeWithinTransaction(Method method, Class targetClass, final InvocationCallback invocation)
			throws Throwable {

		// If the transaction attribute is null, the method is non-transactional.
		// 如果transaction属性为null，则该方法是非事务性的。
		final TransactionAttribute txAttr = getTransactionAttributeSource().getTransactionAttribute(method, targetClass);
		final PlatformTransactionManager tm = determineTransactionManager(txAttr);
		final String joinpointIdentification = methodIdentification(method, targetClass);

		if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager)) {
			// Standard transaction demarcation with getTransaction and commit/rollback calls.
			// 使用getTransaction和commit / rollback调用的标准事务划分。
			TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);
			Object retVal = null;
			try {
				// This is an around advice: Invoke the next interceptor in the chain.
				// This will normally result in a target object being invoked.
				// 这是一个advice：调用链中的下一个拦截器。 这通常会导致调用目标对象。
				retVal = invocation.proceedWithInvocation();
			}
			catch (Throwable ex) {
				// target invocation exception
				// 目标调用异常
				completeTransactionAfterThrowing(txInfo, ex);
				throw ex;
			}
			finally {
				cleanupTransactionInfo(txInfo);
			}
			commitTransactionAfterReturning(txInfo);
			return retVal;
		}

		else {
			// It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in.
			// 它是一个CallbackPreferringPlatformTransactionManager：传递一个TransactionCallback。
			try {
				Object result = ((CallbackPreferringPlatformTransactionManager) tm).execute(txAttr,
						new TransactionCallback<Object>() {
							public Object doInTransaction(TransactionStatus status) {
								TransactionInfo txInfo = prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
								try {
									return invocation.proceedWithInvocation();
								}
								catch (Throwable ex) {
									if (txAttr.rollbackOn(ex)) {
										// A RuntimeException: will lead to a rollback.
										// RuntimeException：将导致回滚。
										if (ex instanceof RuntimeException) {
											throw (RuntimeException) ex;
										}
										else {
											throw new ThrowableHolderException(ex);
										}
									}
									else {
										// A normal return value: will lead to a commit.
										// 正常的返回值：将导致提交。
										return new ThrowableHolder(ex);
									}
								}
								finally {
									cleanupTransactionInfo(txInfo);
								}
							}
						});

				// Check result: It might indicate a Throwable to rethrow.
				// 检查结果：它可能表示要重新抛出Throwable。
				if (result instanceof ThrowableHolder) {
					throw ((ThrowableHolder) result).getThrowable();
				}
				else {
					return result;
				}
			}
			catch (ThrowableHolderException ex) {
				throw ex.getCause();
			}
		}
	}

	/**
	 * Determine the specific transaction manager to use for the given transaction.
	 * 
	 * <p> 确定要用于给定事务的特定事务管理器。
	 * 
	 */
	protected PlatformTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
		if (this.transactionManager != null || this.beanFactory == null || txAttr == null) {
			return this.transactionManager;
		}
		String qualifier = txAttr.getQualifier();
		if (StringUtils.hasLength(qualifier)) {
			return BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.beanFactory, PlatformTransactionManager.class, qualifier);
		}
		else if (this.transactionManagerBeanName != null) {
			return this.beanFactory.getBean(this.transactionManagerBeanName, PlatformTransactionManager.class);
		}
		else {
			return this.beanFactory.getBean(PlatformTransactionManager.class);
		}
	}

	/**
	 * Convenience method to return a String representation of this Method
	 * for use in logging. Can be overridden in subclasses to provide a
	 * different identifier for the given method.
	 * 
	 * <p> 返回此Method的String表示以便在日志记录中使用的便捷方法。 可以在子类中重写，以便为给定方法提供不同的标识符。
	 * 
	 * @param method the method we're interested in
	 * 
	 * <p> 我们感兴趣的方法
	 * 
	 * @param targetClass the class that the method is being invoked on
	 * 
	 * <p> 正在调用该方法的类
	 * 
	 * @return a String representation identifying this method
	 * 
	 * <p> 标识此方法的String表示形式
	 * 
	 * @see org.springframework.util.ClassUtils#getQualifiedMethodName
	 */
	protected String methodIdentification(Method method, Class targetClass) {
		String simpleMethodId = methodIdentification(method);
		if (simpleMethodId != null) {
			return simpleMethodId;
		}
		return (targetClass != null ? targetClass : method.getDeclaringClass()).getName() + "." + method.getName();
	}

	/**
	 * Convenience method to return a String representation of this Method
	 * for use in logging. Can be overridden in subclasses to provide a
	 * different identifier for the given method.
	 * 
	 * <p> 返回此Method的String表示以便在日志记录中使用的便捷方法。 可以在子类中重写，以便为给定方法提供不同的标识符。
	 * 
	 * @param method the method we're interested in
	 * 
	 * <p> 我们感兴趣的方法
	 * 
	 * @return a String representation identifying this method
	 * 
	 * <p> 标识此方法的String表示形式
	 * 
	 * @deprecated in favor of {@link #methodIdentification(Method, Class)}
	 * 
	 * <p> 已过时。 赞成methodIdentification（Method，Class）
	 * 
	 */
	@Deprecated
	protected String methodIdentification(Method method) {
		return null;
	}

	/**
	 * Create a transaction if necessary, based on the given method and class.
	 * 
	 * <p> 根据给定的方法和类，根据需要创建事务。
	 * 
	 * <p>Performs a default TransactionAttribute lookup for the given method.
	 * 
	 * <p> 对给定方法执行默认TransactionAttribute查找。
	 * 
	 * @param method the method about to execute - 即将执行的方法
	 * 
	 * @param targetClass the class that the method is being invoked on
	 * 
	 * <p> 正在调用该方法的类
	 * 
	 * @return a TransactionInfo object, whether or not a transaction was created.
	 * The {@code hasTransaction()} method on TransactionInfo can be used to
	 * tell if there was a transaction created.
	 * 
	 * <p> TransactionInfo对象，无论是否创建了事务。 TransactionInfo上的hasTransaction（）方法可用于判断是否创建了事务。
	 * 
	 * @see #getTransactionAttributeSource()
	 * @deprecated in favor of
	 * {@link #createTransactionIfNecessary(PlatformTransactionManager, TransactionAttribute, String)}
	 * 
	 * <p> 已过时。 支持createTransactionIfNecessary（PlatformTransactionManager，TransactionAttribute，String）
	 * 
	 */
	@Deprecated
	protected TransactionInfo createTransactionIfNecessary(Method method, Class targetClass) {
		// If the transaction attribute is null, the method is non-transactional.
		// 如果transaction属性为null，则该方法是非事务性的。
		TransactionAttribute txAttr = getTransactionAttributeSource().getTransactionAttribute(method, targetClass);
		PlatformTransactionManager tm = determineTransactionManager(txAttr);
		return createTransactionIfNecessary(tm, txAttr, methodIdentification(method, targetClass));
	}

	/**
	 * Create a transaction if necessary based on the given TransactionAttribute.
	 * 
	 * <p> 必要时根据给定的TransactionAttribute创建事务。
	 * 
	 * <p>Allows callers to perform custom TransactionAttribute lookups through
	 * the TransactionAttributeSource.
	 * 
	 * <p> 允许调用者通过TransactionAttributeSource执行自定义TransactionAttribute查找。
	 * 
	 * @param txAttr the TransactionAttribute (may be {@code null})
	 * 
	 * <p> TransactionAttribute（可以为null）
	 * 
	 * @param joinpointIdentification the fully qualified method name
	 * (used for monitoring and logging purposes)
	 * 
	 * <p> 完全限定的方法名称（用于监视和记录目的）
	 * 
	 * @return a TransactionInfo object, whether or not a transaction was created.
	 * The {@code hasTransaction()} method on TransactionInfo can be used to
	 * tell if there was a transaction created.
	 * 
	 * <p> TransactionInfo对象，无论是否创建了事务。 TransactionInfo上的hasTransaction（）方法可用于判断是否创建了事务。
	 * 
	 * @see #getTransactionAttributeSource()
	 */
	@SuppressWarnings("serial")
	protected TransactionInfo createTransactionIfNecessary(
			PlatformTransactionManager tm, TransactionAttribute txAttr, final String joinpointIdentification) {

		// If no name specified, apply method identification as transaction name.
		// 如果未指定名称，请将方法标识应用为事务名称。
		if (txAttr != null && txAttr.getName() == null) {
			txAttr = new DelegatingTransactionAttribute(txAttr) {
				@Override
				public String getName() {
					return joinpointIdentification;
				}
			};
		}

		TransactionStatus status = null;
		if (txAttr != null) {
			if (tm != null) {
				status = tm.getTransaction(txAttr);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
							"] because no transaction manager has been configured");
				}
			}
		}
		return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
	}

	/**
	 * Prepare a TransactionInfo for the given attribute and status object.
	 * 
	 * <p> 为给定的属性和状态对象准备TransactionInfo。
	 * 
	 * @param txAttr the TransactionAttribute (may be {@code null})
	 * 
	 * <p> TransactionAttribute（可以为null）
	 * 
	 * @param joinpointIdentification the fully qualified method name
	 * (used for monitoring and logging purposes)
	 * 
	 * <p> 完全限定的方法名称（用于监视和记录目的）
	 * 
	 * @param status the TransactionStatus for the current transaction
	 * 
	 * <p> 当前事务的TransactionStatus
	 * 
	 * @return the prepared TransactionInfo object
	 * 
	 * <p> 准备好的TransactionInfo对象
	 * 
	 */
	protected TransactionInfo prepareTransactionInfo(PlatformTransactionManager tm,
			TransactionAttribute txAttr, String joinpointIdentification, TransactionStatus status) {

		TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
		if (txAttr != null) {
			// We need a transaction for this method
			// 我们需要这种方法的交易
			if (logger.isTraceEnabled()) {
				logger.trace("Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
			}
			// The transaction manager will flag an error if an incompatible tx already exists
			// 如果已存在不兼容的tx，则事务管理器将标记错误
			txInfo.newTransactionStatus(status);
		}
		else {
			// The TransactionInfo.hasTransaction() method will return
			// false. We created it only to preserve the integrity of
			// the ThreadLocal stack maintained in this class.
			// TransactionInfo.hasTransaction（）方法将返回false。 我们创建它只是为了保持此类中维护的ThreadLocal堆栈的完整性
			if (logger.isTraceEnabled())
				logger.trace("Don't need to create transaction for [" + joinpointIdentification +
						"]: This method isn't transactional.");
		}

		// We always bind the TransactionInfo to the thread, even if we didn't create
		// a new transaction here. This guarantees that the TransactionInfo stack
		// will be managed correctly even if no transaction was created by this aspect.
		// 我们总是将TransactionInfo绑定到线程，即使我们没有在这里创建新事务。 这保证了即使此方面没有创建任何事务，也将正确管理TransactionInfo堆栈。
		txInfo.bindToThread();
		return txInfo;
	}

	/**
	 * Execute after successful completion of call, but not after an exception was handled.
	 * Do nothing if we didn't create a transaction.
	 * 
	 * <p> 成功完成调用后执行，但在处理异常后不执行。 如果我们没有创建交易，则不执行任何操作。
	 * 
	 * @param txInfo information about the current transaction
	 * 
	 * <p> 有关当前交易的信息
	 * 
	 */
	protected void commitTransactionAfterReturning(TransactionInfo txInfo) {
		if (txInfo != null && txInfo.hasTransaction()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() + "]");
			}
			txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
		}
	}

	/**
	 * Handle a throwable, completing the transaction.
	 * We may commit or roll back, depending on the configuration.
	 * 
	 * <p> 处理一个throwable，完成交易。 我们可能会提交或回滚，具体取决于配置。
	 * 
	 * @param txInfo information about the current transaction
	 * 
	 * <p> 有关当前交易的信息
	 * 
	 * @param ex throwable encountered
	 * 
	 * <p> 扔掉了
	 * 
	 */
	protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex) {
		if (txInfo != null && txInfo.hasTransaction()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
						"] after exception: " + ex);
			}
			if (txInfo.transactionAttribute.rollbackOn(ex)) {
				try {
					txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
				}
				catch (TransactionSystemException ex2) {
					logger.error("Application exception overridden by rollback exception", ex);
					ex2.initApplicationException(ex);
					throw ex2;
				}
				catch (RuntimeException ex2) {
					logger.error("Application exception overridden by rollback exception", ex);
					throw ex2;
				}
				catch (Error err) {
					logger.error("Application exception overridden by rollback error", ex);
					throw err;
				}
			}
			else {
				// We don't roll back on this exception.
				// Will still roll back if TransactionStatus.isRollbackOnly() is true.
				try {
					txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
				}
				catch (TransactionSystemException ex2) {
					logger.error("Application exception overridden by commit exception", ex);
					ex2.initApplicationException(ex);
					throw ex2;
				}
				catch (RuntimeException ex2) {
					logger.error("Application exception overridden by commit exception", ex);
					throw ex2;
				}
				catch (Error err) {
					logger.error("Application exception overridden by commit error", ex);
					throw err;
				}
			}
		}
	}

	/**
	 * Reset the TransactionInfo ThreadLocal.
	 * 
	 * <p> 重置TransactionInfo ThreadLocal。
	 * 
	 * <p>Call this in all cases: exception or normal return!
	 * 
	 * <p> 在所有情况下都要调用它：异常或正常返回！
	 * 
	 * @param txInfo information about the current transaction (may be {@code null})
	 * 
	 * <p> 有关当前事务的信息（可能为null）
	 */
	protected void cleanupTransactionInfo(TransactionInfo txInfo) {
		if (txInfo != null) {
			txInfo.restoreThreadLocalStatus();
		}
	}


	/**
	 * Opaque object used to hold Transaction information. Subclasses
	 * must pass it back to methods on this class, but not see its internals.
	 * 
	 * <p> 用于保存交易信息的不透明对象。 子类必须将它传递回此类的方法，但不能看到它的内部。
	 */
	protected final class TransactionInfo {

		private final PlatformTransactionManager transactionManager;

		private final TransactionAttribute transactionAttribute;

		private final String joinpointIdentification;

		private TransactionStatus transactionStatus;

		private TransactionInfo oldTransactionInfo;

		public TransactionInfo(PlatformTransactionManager transactionManager,
				TransactionAttribute transactionAttribute, String joinpointIdentification) {
			this.transactionManager = transactionManager;
			this.transactionAttribute = transactionAttribute;
			this.joinpointIdentification = joinpointIdentification;
		}

		public PlatformTransactionManager getTransactionManager() {
			return this.transactionManager;
		}

		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}

		/**
		 * Return a String representation of this joinpoint (usually a Method call)
		 * for use in logging.
		 * 
		 * <p> 返回此连接点的String表示形式（通常是Method调用）以用于日志记录。
		 */
		public String getJoinpointIdentification() {
			return this.joinpointIdentification;
		}

		public void newTransactionStatus(TransactionStatus status) {
			this.transactionStatus = status;
		}

		public TransactionStatus getTransactionStatus() {
			return this.transactionStatus;
		}

		/**
		 * Return whether a transaction was created by this aspect,
		 * or whether we just have a placeholder to keep ThreadLocal stack integrity.
		 * 
		 * <p> 返回事务是否由此方面创建，或者我们是否只有一个占位符来保持ThreadLocal堆栈的完整性。
		 */
		public boolean hasTransaction() {
			return (this.transactionStatus != null);
		}

		private void bindToThread() {
			// Expose current TransactionStatus, preserving any existing TransactionStatus
			// for restoration after this transaction is complete.
			// 公开当前的TransactionStatus，在此事务完成后保留任何现有的TransactionStatus以进行恢复。
			this.oldTransactionInfo = transactionInfoHolder.get();
			transactionInfoHolder.set(this);
		}

		private void restoreThreadLocalStatus() {
			// Use stack to restore old transaction TransactionInfo.
			// Will be null if none was set.
			// 使用堆栈恢复旧事务TransactionInfo。 如果没有设置，则为null。
			transactionInfoHolder.set(this.oldTransactionInfo);
		}

		@Override
		public String toString() {
			return this.transactionAttribute.toString();
		}
	}


	/**
	 * Simple callback interface for proceeding with the target invocation.
	 * Concrete interceptors/aspects adapt this to their invocation mechanism.
	 * 
	 * <p> 用于继续目标调用的简单回调接口。 具体拦截器/方面使其适应其调用机制。
	 */
	protected interface InvocationCallback {

		Object proceedWithInvocation() throws Throwable;
	}


	/**
	 * Internal holder class for a Throwable, used as a return value
	 * from a TransactionCallback (to be subsequently unwrapped again).
	 * 
	 * <p> Throwable的内部持有者类，用作TransactionCallback的返回值（随后再次打开）。
	 */
	private static class ThrowableHolder {

		private final Throwable throwable;

		public ThrowableHolder(Throwable throwable) {
			this.throwable = throwable;
		}

		public final Throwable getThrowable() {
			return this.throwable;
		}
	}


	/**
	 * Internal holder class for a Throwable, used as a RuntimeException to be
	 * thrown from a TransactionCallback (and subsequently unwrapped again).
	 */
	@SuppressWarnings("serial")
	private static class ThrowableHolderException extends RuntimeException {

		public ThrowableHolderException(Throwable throwable) {
			super(throwable);
		}

		@Override
		public String toString() {
			return getCause().toString();
		}
	}

}
