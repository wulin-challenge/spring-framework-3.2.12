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

package org.springframework.util;

import java.util.Comparator;
import java.util.Map;

/**
 * Strategy interface for {@code String}-based path matching.
 * 
 * <p> 基于String的路径匹配的策略接口。
 *
 * <p>Used by {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
 * {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping},
 * {@link org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver},
 * and {@link org.springframework.web.servlet.mvc.WebContentInterceptor}.
 * 
 * <p> 由
 * org.springframework.core.io.support.PathMatchingResourcePatternResolver，
 * org.springframework.web.servlet.handler.AbstractUrlHandlerMapping，
 * org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver和
 * org.springframework.web.servlet.mvc
 * 使用.WebContentInterceptor。
 *
 * <p>The default implementation is {@link AntPathMatcher}, supporting the
 * Ant-style pattern syntax.
 * 
 * <p> 默认实现是AntPathMatcher，支持Ant样式模式语法。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see AntPathMatcher
 */
public interface PathMatcher {

	/**
	 * Does the given {@code path} represent a pattern that can be matched
	 * by an implementation of this interface?
	 * 
	 * <p> 给定路径是否表示可以通过此接口的实现进行匹配的模式？
	 * 
	 * <p>If the return value is {@code false}, then the {@link #match}
	 * method does not have to be used because direct equality comparisons
	 * on the static path Strings will lead to the same result.
	 * 
	 * <p> 如果返回值为false，则不必使用match方法，因为在静态路径字符串上的直接相等比较将导致相同的结果。
	 * 
	 * @param path the path String to check - 要检查的路径String
	 * @return {@code true} if the given {@code path} represents a pattern
	 * 
	 * <p> 如果给定路径表示模式，则为true
	 * 
	 */
	boolean isPattern(String path);

	/**
	 * Match the given {@code path} against the given {@code pattern},
	 * according to this PathMatcher's matching strategy.
	 * 
	 * <p> 根据PathMatcher的匹配策略，将给定路径与给定模式匹配。
	 * 
	 * @param pattern the pattern to match against - 要匹配的模式
	 * @param path the path String to test - 要测试的路径String
	 * @return {@code true} if the supplied {@code path} matched,
	 * {@code false} if it didn't
	 * 
	 * <p> 如果提供的路径匹配，则返回true;否则返回false
	 * 
	 */
	boolean match(String pattern, String path);

	/**
	 * Match the given {@code path} against the corresponding part of the given
	 * {@code pattern}, according to this PathMatcher's matching strategy.
	 * 
	 * <p> 根据PathMatcher的匹配策略，将给定路径与给定模式的相应部分进行匹配。
	 * 
	 * <p>Determines whether the pattern at least matches as far as the given base
	 * path goes, assuming that a full path may then match as well.
	 * 
	 * <p> 确定模式是否至少匹配给定的基本路径，假设完整路径也可以匹配。
	 * 
	 * @param pattern the pattern to match against - 要匹配的模式
	 * @param path the path String to test - 要测试的路径String
	 * @return {@code true} if the supplied {@code path} matched,
	 * {@code false} if it didn't
	 * 
	 * <p> 如果提供的路径匹配，则返回true;否则返回false
	 * 
	 */
	boolean matchStart(String pattern, String path);

	/**
	 * Given a pattern and a full path, determine the pattern-mapped part.
	 * 
	 * <p> 给定模式和完整路径，确定模式映射部分。
	 * 
	 * <p>This method is supposed to find out which part of the path is matched
	 * dynamically through an actual pattern, that is, it strips off a statically
	 * defined leading path from the given full path, returning only the actually
	 * pattern-matched part of the path.
	 * 
	 * <p> 该方法应该找出路径的哪个部分通过实际模式动态匹配，也就是说，它从给定的完整路径中剥离静态定义的前导路径，
	 * 仅返回路径的实际模式匹配部分。
	 * 
	 * <p>For example: For "myroot/*.html" as pattern and "myroot/myfile.html"
	 * as full path, this method should return "myfile.html". The detailed
	 * determination rules are specified to this PathMatcher's matching strategy.
	 * 
	 * <p> 例如：对于“myroot / * .html”作为模式而“myroot / myfile.html”作为完整路径，此方法应返回“myfile.html”。 
	 * 详细的确定规则是针对此PathMatcher的匹配策略指定的。
	 * 
	 * <p>A simple implementation may return the given full path as-is in case
	 * of an actual pattern, and the empty String in case of the pattern not
	 * containing any dynamic parts (i.e. the {@code pattern} parameter being
	 * a static path that wouldn't qualify as an actual {@link #isPattern pattern}).
	 * A sophisticated implementation will differentiate between the static parts
	 * and the dynamic parts of the given path pattern.
	 * 
	 * <p> 一个简单的实现可以在实际模式的情况下返回给定的完整路径，并且在模式不包含任何动态部分的情况下返
	 * 回空String（即模式参数是不符合实际条件的静态路径） 图案）。 复杂的实现将区分静态部分和给定路径模式的动态部分。
	 * 
	 * @param pattern the path pattern - 路径模式
	 * @param path the full path to introspect - 内省的完整途径
	 * @return the pattern-mapped part of the given {@code path}
	 * (never {@code null})
	 * 
	 * <p> 给定路径的模式映射部分（从不为null）
	 * 
	 */
	String extractPathWithinPattern(String pattern, String path);

	/**
	 * Given a pattern and a full path, extract the URI template variables. URI template
	 * variables are expressed through curly brackets ('{' and '}').
	 * 
	 * <p> 给定模式和完整路径，提取URI模板变量。 URI模板变量通过大括号（'{'和'}'）表示。
	 * 
	 * <p>For example: For pattern "/hotels/{hotel}" and path "/hotels/1", this method will
	 * return a map containing "hotel"->"1".
	 * 
	 * <p> 例如：对于模式“/ hotels / {hotel}”和路径“/ hotels / 1”，此方法将返回包含“hotel” - >“1”的地图。
	 * 
	 * @param pattern the path pattern, possibly containing URI templates
	 * 
	 * <p> 路径模式，可能包含URI模板
	 * 
	 * @param path the full path to extract template variables from
	 * 
	 * <p> 从中提取模板变量的完整路径
	 * 
	 * @return a map, containing variable names as keys; variables values as values
	 * 
	 * <p> map，包含变量名称作为键; 变量值为值
	 */
	Map<String, String> extractUriTemplateVariables(String pattern, String path);

	/**
	 * Given a full path, returns a {@link Comparator} suitable for sorting patterns
	 * in order of explicitness for that path.
	 * 
	 * <p> 给定完整路径，返回适合于按照该路径的显式顺序排序模式的比较器。
	 * 
	 * <p>The full algorithm used depends on the underlying implementation, but generally,
	 * the returned {@code Comparator} will
	 * {@linkplain java.util.Collections#sort(java.util.List, java.util.Comparator) sort}
	 * a list so that more specific patterns come before generic patterns.
	 * 
	 * <p> 使用的完整算法取决于底层实现，但通常，返回的Comparator将对列表进行排序，以便在通用模式之前出现更具体的模式。
	 * 
	 * @param path the full path to use for comparison - 用于比较的完整路径
	 * @return a comparator capable of sorting patterns in order of explicitness
	 * 
	 * <p> 能够按照显式顺序对模式进行排序的比较器
	 * 
	 */
	Comparator<String> getPatternComparator(String path);

	/**
	 * Combines two patterns into a new pattern that is returned.
	 * 
	 * <p> 将两种模式组合成一个返回的新模式。
	 * 
	 * <p>The full algorithm used for combining the two pattern depends on the underlying implementation.
	 * 
	 * <p> 用于组合两种模式的完整算法取决于底层实现。
	 * 
	 * @param pattern1 the first pattern - 第一种模式
	 * @param pattern2 the second pattern - 第二种模式
	 * @return the combination of the two patterns - 这两种模式的结合
	 * @throws IllegalArgumentException when the two patterns cannot be combined - 当两种模式不能合并时
	 */
	String combine(String pattern1, String pattern2);

}
