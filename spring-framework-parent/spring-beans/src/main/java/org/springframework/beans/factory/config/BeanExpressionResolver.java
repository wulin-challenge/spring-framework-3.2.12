/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Strategy interface for resolving a value through evaluating it
 * as an expression, if applicable.
 * 
 * <p> 用于通过将值作为表达式进行评估来解析值的策略接口（如果适用）。
 *
 * <p>A raw {@link org.springframework.beans.factory.BeanFactory} does not
 * contain a default implementation of this strategy. However,
 * {@link org.springframework.context.ApplicationContext} implementations
 * will provide expression support out of the box.
 * 
 * <p> 原始org.springframework.beans.factory.BeanFactory不包含此策略的默认实现。 
 * 但是，org.springframework.context.ApplicationContext实现将提供开箱即用的表达式支持。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface BeanExpressionResolver {

	/**
	 * Evaluate the given value as an expression, if applicable;
	 * return the value as-is otherwise.
	 * 
	 * <p> 如果适用，将给定值评估为表达式; 否则返回值。
	 * 
	 * @param value the value to check - 要检查的值
	 * @param evalContext the evaluation context - 评估背景
	 * @return the resolved value (potentially the given value as-is) - 已解决的值（可能是给定的值）
	 * @throws BeansException if evaluation failed - 如果评估失败
	 */
	Object evaluate(String value, BeanExpressionContext evalContext) throws BeansException;

}
