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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Object representing the override of a method on a managed object by the IoC
 * container.
 * 
 * <p> 对象表示IoC容器对托管对象的方法的覆盖。
 *
 * <p>Note that the override mechanism is <em>not</em> intended as a generic
 * means of inserting crosscutting code: use AOP for that.
 * 
 * <p> 请注意，覆盖机制不是插入横切代码的通用方法：使用AOP。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 */
public abstract class MethodOverride implements BeanMetadataElement {

	private final String methodName;

	private boolean overloaded = true;

	private Object source;


	/**
	 * Construct a new override for the given method.
	 * 
	 * <p> 为给定方法构造新的覆盖。
	 * 
	 * @param methodName the name of the method to override - 要覆盖的方法的名称
	 */
	protected MethodOverride(String methodName) {
		Assert.notNull(methodName, "Method name must not be null");
		this.methodName = methodName;
	}


	/**
	 * Return the name of the method to be overridden.
	 * 
	 * <p> 返回要覆盖的方法的名称。
	 * 
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * Set whether the overridden method is <em>overloaded</em> (i.e., whether argument
	 * type matching needs to occur to disambiguate methods of the same name).
	 * 
	 * <p> 设置重写方法是否过载（即，是否需要进行参数类型匹配以消除同名方法的歧义）。
	 * 
	 * <p>Default is {@code true}; can be switched to {@code false} to optimize
	 * runtime performance.
	 * 
	 * <p> 默认为true; 可以切换为false以优化运行时性能。
	 */
	protected void setOverloaded(boolean overloaded) {
		this.overloaded = overloaded;
	}

	/**
	 * Return whether the overridden method is <em>overloaded</em> (i.e., whether argument
	 * type matching needs to occur to disambiguate methods of the same name).
	 * 
	 * <p> 返回重写的方法是否被重载（即，是否需要进行参数类型匹配以消除相同名称的方法的歧义）。
	 */
	protected boolean isOverloaded() {
		return this.overloaded;
	}

	/**
	 * Set the configuration source {@code Object} for this metadata element.
	 * 
	 * <p> 为此元数据元素设置配置源Object。
	 * 
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 * 
	 * <p> 对象的确切类型取决于所使用的配置机制。
	 * 
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}

	/**
	 * Subclasses must override this to indicate whether they <em>match</em> the
	 * given method. This allows for argument list checking as well as method
	 * name checking.
	 * 
	 * <p> 子类必须覆盖它以指示它们是否与给定方法匹配。 这允许参数列表检查以及方法名称检查。
	 * 
	 * @param method the method to check - 要检查的方法
	 * @return whether this override matches the given method - 此覆盖是否与给定方法匹配
	 */
	public abstract boolean matches(Method method);


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MethodOverride)) {
			return false;
		}
		MethodOverride that = (MethodOverride) other;
		return (ObjectUtils.nullSafeEquals(this.methodName, that.methodName) &&
				ObjectUtils.nullSafeEquals(this.source, that.source));
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(this.methodName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.source);
		return hashCode;
	}

}
