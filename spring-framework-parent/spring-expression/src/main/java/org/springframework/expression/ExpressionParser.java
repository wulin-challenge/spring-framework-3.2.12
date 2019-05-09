/*
 * Copyright 2002-2009 the original author or authors.
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

/**
 * Parses expression strings into compiled expressions that can be evaluated.
 * Supports parsing templates as well as standard expression strings.
 * 
 * <p> 将表达式字符串解析为可以计算的已编译表达式。 支持解析模板以及标准表达式字符串。
 *
 * @author Keith Donald
 * @author Andy Clement
 * @since 3.0
 */
public interface ExpressionParser {

	/**
	 * Parse the expression string and return an Expression object you can use for repeated evaluation.
	 * 
	 * <p> 解析表达式字符串并返回可用于重复评估的Expression对象。
	 * 
	 * <p>Some examples: - 一些例子：
	 * <pre>
	 *     3 + 4
	 *     name.firstName
	 * </pre>
	 * @param expressionString the raw expression string to parse - 要解析的原始表达式字符串
	 * @return an evaluator for the parsed expression - 解析表达式的求值程序
	 * @throws ParseException an exception occurred during parsing - 解析期间发生异常
	 */
	Expression parseExpression(String expressionString) throws ParseException;

	/**
	 * Parse the expression string and return an Expression object you can use for repeated evaluation.
	 * 
	 * <p> 解析表达式字符串并返回可用于重复评估的Expression对象。
	 * 
	 * <p>Some examples: - 一些例子：
	 * <pre>
	 *     3 + 4
	 *     name.firstName
	 * </pre>
	 * @param expressionString the raw expression string to parse - 要解析的原始表达式字符串
	 * @param context a context for influencing this expression parsing routine (optional)
	 * 
	 * <p> 影响此表达式解析例程的上下文（可选）
	 * 
	 * @return an evaluator for the parsed expression - 解析表达式的求值程序
	 * @throws ParseException an exception occurred during parsing - 解析期间发生异常
	 */
	Expression parseExpression(String expressionString, ParserContext context) throws ParseException;

}
