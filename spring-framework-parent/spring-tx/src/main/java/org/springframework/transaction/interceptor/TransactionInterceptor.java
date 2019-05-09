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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * AOP Alliance MethodInterceptor for declarative transaction
 * management using the common Spring transaction infrastructure
 * ({@link org.springframework.transaction.PlatformTransactionManager}).
 * 
 * <p> AOP Alliance MethodInterceptor使用公共Spring事务基础结构（
 * org.springframework.transaction.PlatformTransactionManager）进行声明式事务管理。
 *
 * <p>Derives from the {@link TransactionAspectSupport} class which
 * contains the integration with Spring's underlying transaction API.
 * TransactionInterceptor simply calls the relevant superclass methods
 * such as {@link #invokeWithinTransaction} in the correct order.
 * 
 * <p> 从TransactionAspectSupport类派生，该类包含与Spring的底层事务API的集成。 
 * TransactionInterceptor只是以正确的顺序调用相关的超类方法，例如invokeWithinTransaction。
 *
 * <p>TransactionInterceptors are thread-safe.
 * 
 * <p> TransactionInterceptors是线程安全的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactory
 */
@SuppressWarnings("serial")
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor, Serializable {

	/**
	 * Create a new TransactionInterceptor.
	 * 
	 * <p> 创建一个新的TransactionInterceptor。
	 * 
	 * <p>Transaction manager and transaction attributes still need to be set.
	 * 
	 * <p> 仍然需要设置事务管理器和事务属性。
	 * 
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 * @see #setTransactionAttributeSource(TransactionAttributeSource)
	 */
	public TransactionInterceptor() {
	}

	/**
	 * Create a new TransactionInterceptor.
	 * 
	 * <p> 创建一个新的TransactionInterceptor。
	 * 
	 * @param ptm the transaction manager to perform the actual transaction management
	 * 
	 * <p> 事务管理器执行实际的事务管理
	 * 
	 * @param attributes the transaction attributes in properties format
	 * 
	 * <p> 属性格式的事务属性
	 * 
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 */
	public TransactionInterceptor(PlatformTransactionManager ptm, Properties attributes) {
		setTransactionManager(ptm);
		setTransactionAttributes(attributes);
	}

	/**
	 * Create a new TransactionInterceptor.
	 * 
	 * <p> 创建一个新的TransactionInterceptor。
	 * 
	 * @param ptm the transaction manager to perform the actual transaction management
	 * 
	 * <p> 事务管理器执行实际的事务管理
	 * 
	 * @param tas the attribute source to be used to find transaction attributes
	 * 
	 * <p> 用于查找事务属性的属性源
	 * 
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource(TransactionAttributeSource)
	 */
	public TransactionInterceptor(PlatformTransactionManager ptm, TransactionAttributeSource tas) {
		setTransactionManager(ptm);
		setTransactionAttributeSource(tas);
	}

	/**
	 * 实现此方法以在调用之前和之后执行额外的处理。 礼貌的实现肯定会调用Joinpoint.proceed（）。
	 * @param 方法调用连接点
	 * @return 调用Joinpoint.proceed（）的结果可能会被拦截器截获。
	 * @throws Throwable  - 如果拦截器或目标对象抛出异常。
	 */
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be {@code null}.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface.
		
		// 计算目标类：可以为null。 TransactionAttributeSource应该传递目标类以及可以来自接口的方法。
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

		// Adapt to TransactionAspectSupport's invokeWithinTransaction...
		// 适应TransactionAspectSupport的invokeWithinTransaction ...
		return invokeWithinTransaction(invocation.getMethod(), targetClass, new InvocationCallback() {
			public Object proceedWithInvocation() throws Throwable {
				return invocation.proceed();
			}
		});
	}


	//---------------------------------------------------------------------
	// Serialization support
	// 序列化支持 
	//---------------------------------------------------------------------

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Rely on default serialization, although this class itself doesn't carry state anyway...
		// 依靠默认序列化，虽然这个类本身无论如何都不带状态...
		oos.defaultWriteObject();

		// Deserialize superclass fields.
		// 反序列化超类字段。
		oos.writeObject(getTransactionManagerBeanName());
		oos.writeObject(getTransactionManager());
		oos.writeObject(getTransactionAttributeSource());
		oos.writeObject(getBeanFactory());
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization, although this class itself doesn't carry state anyway...
		// 依靠默认序列化，虽然这个类本身无论如何都不带状态...
		ois.defaultReadObject();

		// Serialize all relevant superclass fields.
		// Superclass can't implement Serializable because it also serves as base class
		// for AspectJ aspects (which are not allowed to implement Serializable)!
		
		// 序列化所有相关的超类字段。 Superclass不能实现Serializable，因为它也可以作为AspectJ方面的基类（不允许实现Serializable）！
		setTransactionManagerBeanName((String) ois.readObject());
		setTransactionManager((PlatformTransactionManager) ois.readObject());
		setTransactionAttributeSource((TransactionAttributeSource) ois.readObject());
		setBeanFactory((BeanFactory) ois.readObject());
	}

}
