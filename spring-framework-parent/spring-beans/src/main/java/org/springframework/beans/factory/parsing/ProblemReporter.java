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

package org.springframework.beans.factory.parsing;

/**
 * SPI interface allowing tools and other external processes to handle errors
 * and warnings reported during bean definition parsing.
 * 
 * <p> SPI接口允许工具和其他外部进程处理bean定义解析期间报告的错误和警告。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see Problem
 */
public interface ProblemReporter {

	/**
	 * Called when a fatal error is encountered during the parsing process.
	 * 
	 * <p> 在解析过程中遇到致命错误时调用。
	 * 
	 * <p>Implementations must treat the given problem as fatal,
	 * i.e. they have to eventually raise an exception.
	 * 
	 * <p> 实现必须将给定的问题视为致命的，即它们必须最终引发异常。
	 * 
	 * @param problem the source of the error (never {@code null}) - 错误的来源（永远不为null）
	 */
	void fatal(Problem problem);

	/**
	 * Called when an error is encountered during the parsing process.
	 * 
	 * <p> 在解析过程中遇到错误时调用。
	 * 
	 * <p>Implementations may choose to treat errors as fatal.
	 * 
	 * <p> 实现可能会选择将错误视为致命错误。
	 * 
	 * @param problem the source of the error (never {@code null})  - 错误的来源（永远不为null）
	 */
	void error(Problem problem);

	/**
	 * Called when a warning is raised during the parsing process.
	 * 
	 * <p> 在解析过程中发出警告时调用。
	 * 
	 * <p>Warnings are <strong>never</strong> considered to be fatal.
	 * 
	 * <p> 警告从未被视为致命。
	 * 
	 * @param problem the source of the warning (never {@code null})  - 警告的来源（永远不为null）
	 */
	void warning(Problem problem);

}
