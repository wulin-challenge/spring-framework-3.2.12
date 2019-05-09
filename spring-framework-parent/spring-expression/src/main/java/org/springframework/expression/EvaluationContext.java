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

package org.springframework.expression;

import java.util.List;

/**
 * Expressions are executed in an evaluation context. It is in this context that
 * references are resolved when encountered during expression evaluation.
 * 
 * <p> 表达式在评估上下文中执行。 在此上下文中，在表达式评估期间遇到引用。
 *
 * <p>There is a default implementation of the EvaluationContext,
 * {@link org.springframework.expression.spel.support.StandardEvaluationContext} that can
 * be extended, rather than having to implement everything.
 * 
 * <p> 有一个可以扩展的EvaluationContext，
 * org.springframework.expression.spel.support.StandardEvaluationContext的默认实现，而不是必须实现所有内容。
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface EvaluationContext {

	/**
	 * Return the default root context object against which unqualified
	 * properties/methods/etc should be resolved. This can be overridden
	 * when evaluating an expression.
	 * 
	 * <p> 返回应该解析非限定属性/方法/等的默认根上下文对象。 在计算表达式时可以覆盖它。
	 * 
	 */
	TypedValue getRootObject();

	/**
	 * Return a list of resolvers that will be asked in turn to locate a constructor.
	 * 
	 * <p> 返回一个解析器列表，这些解析器将依次被询问以找到构造函数。
	 * 
	 */
	List<ConstructorResolver> getConstructorResolvers();

	/**
	 * Return a list of resolvers that will be asked in turn to locate a method.
	 * 
	 * <p> 返回一个解析器列表，这些解析器将依次被要求查找方法。
	 * 
	 */
	List<MethodResolver> getMethodResolvers();

	/**
	 * Return a list of accessors that will be asked in turn to read/write a property.
	 * 
	 * <p> 返回一个访问者列表，这些访问者将依次被要求读/写一个属性。
	 * 
	 */
	List<PropertyAccessor> getPropertyAccessors();

	/**
	 * Return a type locator that can be used to find types, either by short or
	 * fully qualified name.
	 * 
	 * <p> 返回可用于查找类型的类型定位器，可以是短名称或完全限定名称。
	 * 
	 */
	TypeLocator getTypeLocator();

	/**
	 * Return a type converter that can convert (or coerce) a value from one type to another.
	 * 
	 * <p> 返回一个类型转换器，可以将值从一种类型转换（或强制）为另一种类型。
	 * 
	 */
	TypeConverter getTypeConverter();

	/**
	 * Return a type comparator for comparing pairs of objects for equality.
	 * 
	 * <p> 返回一个类型比较器，用于比较对象对的相等性。
	 * 
	 */
	TypeComparator getTypeComparator();

	/**
	 * Return an operator overloader that may support mathematical operations
	 * between more than the standard set of types.
	 * 
	 * <p> 返回一个运算符重载器，它可以支持多个标准类型集之间的数学运算。
	 * 
	 */
	OperatorOverloader getOperatorOverloader();

	/**
	 * Return a bean resolver that can look up beans by name.
	 * 
	 * <p> 返回一个可以按名称查找bean的bean解析器。
	 */
	BeanResolver getBeanResolver();

	/**
	 * Set a named variable within this evaluation context to a specified value.
	 * 
	 * <p> 在此评估上下文中将命名变量设置为指定值。
	 * 
	 * @param name variable to set - 要设置的变量
	 * @param value value to be placed in the variable - 要放在变量中的值
	 */
	void setVariable(String name, Object value);

	/**
	 * Look up a named variable within this evaluation context.
	 * 
	 * <p> 在此评估上下文中查找命名变量。
	 * 
	 * @param name variable to lookup - 变量来查找
	 * @return the value of the variable - 变量的值
	 */
	Object lookupVariable(String name);

}
