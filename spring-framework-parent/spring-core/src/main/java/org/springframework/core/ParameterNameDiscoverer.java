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

package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Interface to discover parameter names for methods and constructors.
 * 
 * <p>用于发现方法和构造函数的参数名称的接口。
 *
 * <p>Parameter name discovery is not always possible, but various strategies are
 * available to try, such as looking for debug information that may have been
 * emitted at compile time, and looking for argname annotation values optionally
 * accompanying AspectJ annotated methods.
 * 
 * <p>参数名称发现并不总是可行，但可以尝试各种策略，例如查找可能在编译时发出的调试信息，以及查找可选的AspectJ注释方法的argname注释值。
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public interface ParameterNameDiscoverer {

	/**
	 * Return parameter names for this method,
	 * or {@code null} if they cannot be determined.
	 * 
	 * <p>返回此方法的参数名称，如果无法确定，则返回null。
	 * 
	 * @param method method to find parameter names for - 查找参数名称的方法
	 * @return an array of parameter names if the names can be resolved,
	 * or {@code null} if they cannot - 如果可以解析名称，则为参数名称数组;如果不能，则为null
	 */
	String[] getParameterNames(Method method);

	/**
	 * Return parameter names for this constructor,
	 * or {@code null} if they cannot be determined.
	 * 
	 * <p>返回此构造函数的参数名称，如果无法确定，则返回null。
	 * 
	 * @param ctor constructor to find parameter names for - 构造函数，用于查找参数名称
	 * @return an array of parameter names if the names can be resolved,
	 * or {@code null} if they cannot - 如果可以解析名称，则为参数名称数组;如果不能，则为null
	 */
	String[] getParameterNames(Constructor<?> ctor);

}
