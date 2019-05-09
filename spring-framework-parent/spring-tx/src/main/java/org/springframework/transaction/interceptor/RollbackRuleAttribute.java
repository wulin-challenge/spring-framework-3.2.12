/*
 * Copyright 2002-2014 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * Rule determining whether or not a given exception (and any subclasses)
 * should cause a rollback.
 * 
 * <p> 规则确定给定异常（和任何子类）是否应该导致回滚。
 *
 * <p>Multiple such rules can be applied to determine whether a transaction
 * should commit or rollback after an exception has been thrown.
 * 
 * <p> 可以应用多个此类规则来确定在抛出异常后事务是应该提交还是回滚。
 *
 * @author Rod Johnson
 * @since 09.04.2003
 * @see NoRollbackRuleAttribute
 */
@SuppressWarnings("serial")
public class RollbackRuleAttribute implements Serializable{

	/**
	 * The {@link RollbackRuleAttribute rollback rule} for
	 * {@link RuntimeException RuntimeExceptions}.
	 * 
	 * <p> RuntimeExceptions的回滚规则。
	 * 
	 */
	public static final RollbackRuleAttribute ROLLBACK_ON_RUNTIME_EXCEPTIONS =
			new RollbackRuleAttribute(RuntimeException.class);


	/**
	 * Could hold exception, resolving class name but would always require FQN.
	 * This way does multiple string comparisons, but how often do we decide
	 * whether to roll back a transaction following an exception?
	 * 
	 * <p> 可以保持异常，解析类名但总是需要FQN。 这种方式可以进行多个字符串比较，但是我们经常决定是否在异常后回滚事务？
	 * 
	 */
	private final String exceptionName;


	/**
	 * Create a new instance of the {@code RollbackRuleAttribute} class.
	 * 
	 * <p> 创建RollbackRuleAttribute类的新实例。
	 * 
	 * <p>This is the preferred way to construct a rollback rule that matches
	 * the supplied {@link Exception} class (and subclasses).
	 * 
	 * <p> 这是构造与提供的Exception类（和子类）匹配的回滚规则的首选方法。
	 * 
	 * @param clazz throwable class; must be {@link Throwable} or a subclass
	 * of {@code Throwable}
	 * 
	 * <p> 可抛出的阶级; 必须是Throwable或Throwable的子类
	 * 
	 * @throws IllegalArgumentException if the supplied {@code clazz} is
	 * not a {@code Throwable} type or is {@code null}
	 * 
	 * <p> 如果提供的clazz不是Throwable类型或为null
	 * 
	 */
	public RollbackRuleAttribute(Class<?> clazz) {
		Assert.notNull(clazz, "'clazz' cannot be null");
		if (!Throwable.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(
					"Cannot construct rollback rule from [" + clazz.getName() + "]: it's not a Throwable");
		}
		this.exceptionName = clazz.getName();
	}

	/**
	 * Create a new instance of the {@code RollbackRuleAttribute} class
	 * for the given {@code exceptionName}.
	 * 
	 * <p> 为给定的exceptionName创建RollbackRuleAttribute类的新实例。
	 * 
	 * <p>This can be a substring, with no wildcard support at present. A value
	 * of "ServletException" would match
	 * {@code javax.servlet.ServletException} and subclasses, for example.
	 * 
	 * <p> 这可以是子字符串，目前没有通配符支持。 例如，值“ServletException”将匹配javax.servlet.ServletException和子类。
	 * 
	 * <p><b>NB:</b> Consider carefully how specific the pattern is, and
	 * whether to include package information (which is not mandatory). For
	 * example, "Exception" will match nearly anything, and will probably hide
	 * other rules. "java.lang.Exception" would be correct if "Exception" was
	 * meant to define a rule for all checked exceptions. With more unusual
	 * exception names such as "BaseBusinessException" there's no need to use a
	 * fully package-qualified name.
	 * 
	 * <p> 注意：仔细考虑模式的具体程度，以及是否包含包信息（这不是必需的）。 例如，“Exception”几乎可以匹配任何内容，
	 * 并且可能会隐藏其他规则。 如果“异常”用于为所有已检查的异常定义规则，则“java.lang.Exception”将是正确的。 
	 * 使用更多不寻常的异常名称，例如“BaseBusinessException”，不需要使用完全包限定名称。
	 * 
	 * @param exceptionName the exception name pattern; can also be a fully
	 * package-qualified class name
	 * 
	 * <p> 异常名称模式; 也可以是完全包限定的类名
	 * 
	 * @throws IllegalArgumentException if the supplied
	 * {@code exceptionName} is {@code null} or empty
	 * 
	 * <p> 如果提供的exceptionName为null或为空
	 * 
	 */
	public RollbackRuleAttribute(String exceptionName) {
		Assert.hasText(exceptionName, "'exceptionName' cannot be null or empty");
		this.exceptionName = exceptionName;
	}


	/**
	 * Return the pattern for the exception name.
	 * 
	 * <p> 返回异常名称的模式。
	 * 
	 */
	public String getExceptionName() {
		return exceptionName;
	}

	/**
	 * Return the depth of the superclass matching.
	 * 
	 * <p> 返回超类匹配的深度。
	 * 
	 * <p>{@code 0} means {@code ex} matches exactly. Returns
	 * {@code -1} if there is no match. Otherwise, returns depth with the
	 * lowest depth winning.
	 * 
	 * <p> 0表示ex完全匹配。 如果没有匹配则返回-1。 否则，以最低深度获胜返回深度。
	 * 
	 */
	public int getDepth(Throwable ex) {
		return getDepth(ex.getClass(), 0);
	}


	private int getDepth(Class<?> exceptionClass, int depth) {
		if (exceptionClass.getName().contains(this.exceptionName)) {
			// Found it!
			// 找到了！
			return depth;
		}
		// If we've gone as far as we can go and haven't found it...
		// 如果我们走得尽可能远，却找不到它......
		if (exceptionClass.equals(Throwable.class)) {
			return -1;
		}
		return getDepth(exceptionClass.getSuperclass(), depth + 1);
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RollbackRuleAttribute)) {
			return false;
		}
		RollbackRuleAttribute rhs = (RollbackRuleAttribute) other;
		return this.exceptionName.equals(rhs.exceptionName);
	}

	@Override
	public int hashCode() {
		return this.exceptionName.hashCode();
	}

	@Override
	public String toString() {
		return "RollbackRuleAttribute with pattern [" + this.exceptionName + "]";
	}

}
