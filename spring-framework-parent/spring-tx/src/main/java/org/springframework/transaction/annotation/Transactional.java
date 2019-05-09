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

package org.springframework.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.TransactionDefinition;

/**
 * Describes transaction attributes on a method or class.
 * 
 * <p> 描述方法或类的事务属性。
 *
 * <p>This annotation type is generally directly comparable to Spring's
 * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute}
 * class, and in fact {@link AnnotationTransactionAttributeSource} will directly
 * convert the data to the latter class, so that Spring's transaction support code
 * does not have to know about annotations. If no rules are relevant to the exception,
 * it will be treated like
 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute}
 * (rolling back on runtime exceptions).
 * 
 * <p> 这个注释类型通常可以直接与Spring的
 * org.springframework.transaction.interceptor.RuleBasedTransactionAttribute类进行比较，
 * 实际上AnnotationTransactionAttributeSource将直接将数据转换为后一个类，因此Spring的事务支持代码不必知道注释。 
 * 如果没有规则与异常相关，则将其视为
 * org.springframework.transaction.interceptor.DefaultTransactionAttribute（回滚运行时异常）。
 *
 * <p>For specific information about the semantics of this annotation's attributes,
 * consider the {@link org.springframework.transaction.TransactionDefinition} and
 * {@link org.springframework.transaction.interceptor.TransactionAttribute} javadocs.
 * 
 * <p> 有关此注释属性的语义的特定信息，请考虑org.springframework.transaction.TransactionDefinition和
 * org.springframework.transaction.interceptor.TransactionAttribute javadocs。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

	/**
	 * A qualifier value for the specified transaction.
	 * 
	 * <p> 指定事务的限定符值。
	 * 
	 * <p>May be used to determine the target transaction manager,
	 * matching the qualifier value (or the bean name) of a specific
	 * {@link org.springframework.transaction.PlatformTransactionManager}
	 * bean definition.
	 * 
	 * <p> 可用于确定目标事务管理器，匹配特定
	 * org.springframework.transaction.PlatformTransactionManager bean定义的限定符值（或bean名称）。
	 */
	String value() default "";

	/**
	 * The transaction propagation type.
	 * Defaults to {@link Propagation#REQUIRED}.
	 * 
	 * <p> 事务传播类型。 默认为Propagation.REQUIRED。
	 * 
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getPropagationBehavior()
	 */
	Propagation propagation() default Propagation.REQUIRED;

	/**
	 * The transaction isolation level.
	 * Defaults to {@link Isolation#DEFAULT}.
	 * 
	 * <p> 事务隔离级别。 默认为Isolation.DEFAULT。
	 * 
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getIsolationLevel()
	 */
	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * The timeout for this transaction.
	 * Defaults to the default timeout of the underlying transaction system.
	 * 
	 * <p> 此事务的超时。 默认为基础事务系统的默认超时。
	 * 
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * {@code true} if the transaction is read-only.
	 * Defaults to {@code false}.
	 * 
	 * <p> 如果事务是只读的，则为true。 默认为false。
	 * 
	 * <p>This just serves as a hint for the actual transaction subsystem;
	 * it will <i>not necessarily</i> cause failure of write access attempts.
	 * A transaction manager which cannot interpret the read-only hint will
	 * <i>not</i> throw an exception when asked for a read-only transaction.
	 * 
	 * <p> 这仅仅是实际事务子系统的提示; 它不一定会导致写访问尝试失败。 当被要求进行只读事务时，
	 * 不能解释只读提示的事务管理器不会引发异常。
	 * 
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
	 */
	boolean readOnly() default false;

	/**
	 * Defines zero (0) or more exception {@link Class classes}, which must be a
	 * subclass of {@link Throwable}, indicating which exception types must cause
	 * a transaction rollback.
	 * 
	 * <p> 定义零（0）或更多异常类，它们必须是Throwable的子类，指示哪些异常类型必须导致事务回滚。
	 * 
	 * <p>This is the preferred way to construct a rollback rule, matching the
	 * exception class and subclasses.
	 * 
	 * <p> 这是构造回滚规则的首选方法，匹配异常类和子类。
	 * 
	 * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class clazz)}
	 * 
	 * <p> 与org.springframework.transaction.interceptor.RollbackRuleAttribute.RollbackRuleAttribute（Class clazz）类似
	 * 
	 */
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * Defines zero (0) or more exception names (for exceptions which must be a
	 * subclass of {@link Throwable}), indicating which exception types must cause
	 * a transaction rollback.
	 * 
	 * <p> 定义零（0）或更多异常名称（对于必须是Throwable的子类的异常），指示哪些异常类型必须导致事务回滚。
	 * 
	 * <p>This can be a substring, with no wildcard support at present.
	 * A value of "ServletException" would match
	 * {@link javax.servlet.ServletException} and subclasses, for example.
	 * 
	 * <p> 这可以是子字符串，目前没有通配符支持。 例如，值“ServletException”将匹配
	 * javax.servlet.ServletException和子类。
	 * 
	 * <p><b>NB: </b>Consider carefully how specific the pattern is, and whether
	 * to include package information (which isn't mandatory). For example,
	 * "Exception" will match nearly anything, and will probably hide other rules.
	 * "java.lang.Exception" would be correct if "Exception" was meant to define
	 * a rule for all checked exceptions. With more unusual {@link Exception}
	 * names such as "BaseBusinessException" there is no need to use a FQN.
	 * 
	 * <p> 注意：仔细考虑模式的具体程度，以及是否包含包信息（这不是必需的）。 
	 * 例如，“Exception”几乎可以匹配任何内容，并且可能会隐藏其他规则。 如果“异常”用于为所有已检查的异常定义规则，
	 * 则“java.lang.Exception”将是正确的。 使用更多不寻常的异常名称，例如“BaseBusinessException”，不需要使用FQN。
	 * 
	 * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String exceptionName)}
	 * 
	 * <p> 与
	 * org.springframework.transaction.interceptor.RollbackRuleAttribute.RollbackRuleAttribute（String exceptionName）
	 * 相似
	 * 
	 */
	String[] rollbackForClassName() default {};

	/**
	 * Defines zero (0) or more exception {@link Class Classes}, which must be a
	 * subclass of {@link Throwable}, indicating which exception types must <b>not</b>
	 * cause a transaction rollback.
	 * 
	 * <p> 定义零（0）或更多异常类，它们必须是Throwable的子类，指示哪些异常类型不得导致事务回滚。
	 * 
	 * <p>This is the preferred way to construct a rollback rule, matching the
	 * exception class and subclasses.
	 * 
	 * <p> 这是构造回滚规则的首选方法，匹配异常类和子类。
	 * 
	 * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class clazz)}
	 * 
	 * <p> 与
	 * org.springframework.transaction.interceptor.NoRollbackRuleAttribute.NoRollbackRuleAttribute（Class clazz）
	 * 类似
	 * 
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * Defines zero (0) or more exception names (for exceptions which must be a
	 * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
	 * cause a transaction rollback.
	 * 
	 * <p> 定义零（0）或更多异常名称（对于必须是Throwable的子类的异常），指示哪些异常类型不得导致事务回滚。
	 * 
	 * <p>See the description of {@link #rollbackForClassName()} for more info on how
	 * the specified names are treated.
	 * 
	 * <p> 有关如何处理指定名称的详细信息，请参阅rollbackForClassName（）的说明。
	 * 
	 * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String exceptionName)}
	 * 
	 * <p> 与
	 * org.springframework.transaction.interceptor.NoRollbackRuleAttribute.NoRollbackRuleAttribute（String exceptionName）
	 * 相似
	 * 
	 */
	String[] noRollbackForClassName() default {};

}
