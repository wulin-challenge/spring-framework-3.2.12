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

package org.springframework.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PathMatcher implementation for Ant-style path patterns. Examples are provided below.
 * 
 * <p>Ant样式路径模式的PathMatcher实现。 以下提供实例。
 *
 * <p>Part of this mapping code has been kindly borrowed from <a href="http://ant.apache.org">Apache Ant</a>.
 * 
 * <p>部分映射代码已经从Apache Ant中借用。
 *
 * <p>The mapping matches URLs using the following rules:<br> <ul> <li>? matches one character</li> <li>* matches zero
 * or more characters</li> <li>** matches zero or more 'directories' in a path</li> </ul>
 * 
 * 映射使用以下规则匹配URL：
 * <p>•? 匹配一个字符
 * <p>•* 匹配零个或多个字符
 * <p>•** 匹配路径中的零个或多个“目录”
 *
 * <p>Some examples:<br> <ul> <li>{@code com/t?st.jsp} - matches {@code com/test.jsp} but also
 * {@code com/tast.jsp} or {@code com/txst.jsp}</li> <li>{@code com/*.jsp} - matches all
 * {@code .jsp} files in the {@code com} directory</li> <li>{@code com/&#42;&#42;/test.jsp} - matches all
 * {@code test.jsp} files underneath the {@code com} path</li> <li>{@code org/springframework/&#42;&#42;/*.jsp}
 * - matches all {@code .jsp} files underneath the {@code org/springframework} path</li>
 * <li>{@code org/&#42;&#42;/servlet/bla.jsp} - matches {@code org/springframework/servlet/bla.jsp} but also
 * {@code org/springframework/testing/servlet/bla.jsp} and {@code org/servlet/bla.jsp}</li> </ul>
 *
 * <p> 一下例子:
 * <p> com/t?st.jsp                    - 匹配com/test.jsp以及com/tast.jsp或com/txst.jsp
 * <p> com/*.jsp                       - 匹配com目录中的所有.jsp文件
 * <p> com/** /test.jsp                - 匹配com路径下的所有test.jsp文件
 * <p> org/springframework/** / * .jsp - 匹配org/springframework路径下的所有.jsp文件   
 * <p> org/** /servlet/bla.jsp         - 匹配org/springframework/servlet/bla.jsp，还
 * 有org/springframework/testing/servlet/bla.jsp和org/servlet/bla.jsp
 * 
 * <p> 注意: (/** / 后面有一个空格是因为没有空格会与java的注释符合冲突)
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 16.07.2003
 */
public class AntPathMatcher implements PathMatcher {

	/** Default path separator: "/" */
	public static final String DEFAULT_PATH_SEPARATOR = "/";

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}");


	private String pathSeparator = DEFAULT_PATH_SEPARATOR;

	private boolean trimTokens = true;

	private final Map<String, AntPathStringMatcher> stringMatcherCache =
			new ConcurrentHashMap<String, AntPathStringMatcher>(256);


	/**
	 * Set the path separator to use for pattern parsing.
	 * Default is "/", as in Ant.
	 * 
	 * <p>设置用于模式解析的路径分隔符。 默认为“/”，如Ant中所示。
	 * 
	 */
	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = (pathSeparator != null ? pathSeparator : DEFAULT_PATH_SEPARATOR);
	}

	/**
	 * Specify whether to trim tokenized paths and patterns.
	 * Default is {@code true}.
	 * 
	 * <p> 指定是否修剪标记化的路径和模式。 默认为true。
	 * 
	 */
	public void setTrimTokens(boolean trimTokens) {
		this.trimTokens = trimTokens;
	}


	public boolean isPattern(String path) {
		return (path.indexOf('*') != -1 || path.indexOf('?') != -1);
	}

	public boolean match(String pattern, String path) {
		return doMatch(pattern, path, true, null);
	}

	public boolean matchStart(String pattern, String path) {
		return doMatch(pattern, path, false, null);
	}


	/**
	 * Actually match the given {@code path} against the given {@code pattern}.
	 * 
	 * <p> 实际上将给定路径与给定模式匹配。
	 * 
	 * @param pattern the pattern to match against - 要匹配的模式
	 * @param path the path String to test - 要测试的路径String
	 * @param fullMatch whether a full pattern match is required (else a pattern match
	 * as far as the given base path goes is sufficient)
	 * <p> 是否需要完整模式匹配（否则，只要给定基本路径的模式匹配就足够了）
	 * 
	 * @return {@code true} if the supplied {@code path} matched, {@code false} if it didn't
	 * 
	 * <p> 如果提供的路径匹配，则返回true;否则返回false
	 * 
	 */
	protected boolean doMatch(String pattern, String path, boolean fullMatch, Map<String, String> uriTemplateVariables) {
		if (path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
			return false;
		}

		String[] pattDirs = tokenizePath(pattern);
		String[] pathDirs = tokenizePath(path);

		int pattIdxStart = 0;
		int pattIdxEnd = pattDirs.length - 1;
		int pathIdxStart = 0;
		int pathIdxEnd = pathDirs.length - 1;

		// Match all elements up to the first **
		while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			String pattDir = pattDirs[pattIdxStart];
			if ("**".equals(pattDir)) {
				break;
			}
			if (!matchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
				return false;
			}
			pattIdxStart++;
			pathIdxStart++;
		}

		if (pathIdxStart > pathIdxEnd) {
			// Path is exhausted, only match if rest of pattern is * or **'s
			if (pattIdxStart > pattIdxEnd) {
				return (pattern.endsWith(this.pathSeparator) ? path.endsWith(this.pathSeparator) :
						!path.endsWith(this.pathSeparator));
			}
			if (!fullMatch) {
				return true;
			}
			if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(this.pathSeparator)) {
				return true;
			}
			for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
				if (!pattDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		}
		else if (pattIdxStart > pattIdxEnd) {
			// String not exhausted, but pattern is. Failure.
			return false;
		}
		else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
			// Path start definitely matches due to "**" part in pattern.
			return true;
		}

		// up to last '**'
		while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			String pattDir = pattDirs[pattIdxEnd];
			if (pattDir.equals("**")) {
				break;
			}
			if (!matchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
				return false;
			}
			pattIdxEnd--;
			pathIdxEnd--;
		}
		if (pathIdxStart > pathIdxEnd) {
			// String is exhausted
			for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
				if (!pattDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		}

		while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			int patIdxTmp = -1;
			for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
				if (pattDirs[i].equals("**")) {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == pattIdxStart + 1) {
				// '**/**' situation, so skip one
				pattIdxStart++;
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between
			// strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - pattIdxStart - 1);
			int strLength = (pathIdxEnd - pathIdxStart + 1);
			int foundIdx = -1;

			strLoop:
			for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					String subPat = pattDirs[pattIdxStart + j + 1];
					String subStr = pathDirs[pathIdxStart + i + j];
					if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
						continue strLoop;
					}
				}
				foundIdx = pathIdxStart + i;
				break;
			}

			if (foundIdx == -1) {
				return false;
			}

			pattIdxStart = patIdxTmp;
			pathIdxStart = foundIdx + patLength;
		}

		for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
			if (!pattDirs[i].equals("**")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Tokenize the given path String into parts, based on this matcher's settings.
	 * 
	 * <p> 根据此匹配器的设置，将给定路径字符串标记为部分。
	 * 
	 * @param path the path to tokenize - 标记化的路径
	 * @return the tokenized path parts - 标记化的路径部分
	 */
	protected String[] tokenizePath(String path) {
		return StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true);
	}

	/**
	 * Tests whether or not a string matches against a pattern. The pattern may contain two special characters:
	 * 
	 * <p> 测试字符串是否与模式匹配。 该模式可能包含两个特殊字符：
	 * 
	 * <br>'*' means zero or more characters - '*'表示零个或多个字符
	 * <br>'?' means one and only one character - '？' 意味着只有一个角色
	 * @param pattern pattern to match against. Must not be {@code null}.
	 * 
	 * <p> 匹配的模式。 不能为空。
	 * 
	 * @param str string which must be matched against the pattern. Must not be {@code null}.
	 * 
	 * <p> 必须与模式匹配的字符串。 不能为空。
	 * 
	 * @return {@code true} if the string matches against the pattern, or {@code false} otherwise.
	 * 
	 * <p> 如果字符串与模式匹配，则返回true;否则返回false。
	 * 
	 */
	private boolean matchStrings(String pattern, String str, Map<String, String> uriTemplateVariables) {
		AntPathStringMatcher matcher = this.stringMatcherCache.get(pattern);
		if (matcher == null) {
			matcher = new AntPathStringMatcher(pattern);
			this.stringMatcherCache.put(pattern, matcher);
		}
		return matcher.matchStrings(str, uriTemplateVariables);
	}

	/**
	 * Given a pattern and a full path, determine the pattern-mapped part. <p>For example: <ul>
	 * <li>'{@code /docs/cvs/commit.html}' and '{@code /docs/cvs/commit.html} -> ''</li>
	 * <li>'{@code /docs/*}' and '{@code /docs/cvs/commit} -> '{@code cvs/commit}'</li>
	 * <li>'{@code /docs/cvs/*.html}' and '{@code /docs/cvs/commit.html} -> '{@code commit.html}'</li>
	 * <li>'{@code /docs/**}' and '{@code /docs/cvs/commit} -> '{@code cvs/commit}'</li>
	 * <li>'{@code /docs/**\/*.html}' and '{@code /docs/cvs/commit.html} -> '{@code cvs/commit.html}'</li>
	 * <li>'{@code /*.html}' and '{@code /docs/cvs/commit.html} -> '{@code docs/cvs/commit.html}'</li>
	 * <li>'{@code *.html}' and '{@code /docs/cvs/commit.html} -> '{@code /docs/cvs/commit.html}'</li>
	 * <li>'{@code *}' and '{@code /docs/cvs/commit.html} -> '{@code /docs/cvs/commit.html}'</li> </ul>
	 * <p>Assumes that {@link #match} returns {@code true} for '{@code pattern}' and '{@code path}', but
	 * does <strong>not</strong> enforce this.
	 */
	public String extractPathWithinPattern(String pattern, String path) {
		String[] patternParts = StringUtils.tokenizeToStringArray(pattern, this.pathSeparator, this.trimTokens, true);
		String[] pathParts = StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true);

		StringBuilder builder = new StringBuilder();

		// Add any path parts that have a wildcarded pattern part.
		int puts = 0;
		for (int i = 0; i < patternParts.length; i++) {
			String patternPart = patternParts[i];
			if ((patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) && pathParts.length >= i + 1) {
				if (puts > 0 || (i == 0 && !pattern.startsWith(this.pathSeparator))) {
					builder.append(this.pathSeparator);
				}
				builder.append(pathParts[i]);
				puts++;
			}
		}

		// Append any trailing path parts.
		for (int i = patternParts.length; i < pathParts.length; i++) {
			if (puts > 0 || i > 0) {
				builder.append(this.pathSeparator);
			}
			builder.append(pathParts[i]);
		}

		return builder.toString();
	}

	public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		Map<String, String> variables = new LinkedHashMap<String, String>();
		boolean result = doMatch(pattern, path, true, variables);
		Assert.state(result, "Pattern \"" + pattern + "\" is not a match for \"" + path + "\"");
		return variables;
	}

	/**
	 * Combines two patterns into a new pattern that is returned.
	 * <p>This implementation simply concatenates the two patterns, unless the first pattern
	 * contains a file extension match (such as {@code *.html}. In that case, the second pattern
	 * should be included in the first, or an {@code IllegalArgumentException} is thrown.
	 * <p>For example: <table>
	 * <tr><th>Pattern 1</th><th>Pattern 2</th><th>Result</th></tr> <tr><td>/hotels</td><td>{@code
	 * null}</td><td>/hotels</td></tr> <tr><td>{@code null}</td><td>/hotels</td><td>/hotels</td></tr>
	 * <tr><td>/hotels</td><td>/bookings</td><td>/hotels/bookings</td></tr> <tr><td>/hotels</td><td>bookings</td><td>/hotels/bookings</td></tr>
	 * <tr><td>/hotels/*</td><td>/bookings</td><td>/hotels/bookings</td></tr> <tr><td>/hotels/&#42;&#42;</td><td>/bookings</td><td>/hotels/&#42;&#42;/bookings</td></tr>
	 * <tr><td>/hotels</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr> <tr><td>/hotels/*</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
	 * <tr><td>/hotels/&#42;&#42;</td><td>{hotel}</td><td>/hotels/&#42;&#42;/{hotel}</td></tr>
	 * <tr><td>/*.html</td><td>/hotels.html</td><td>/hotels.html</td></tr> <tr><td>/*.html</td><td>/hotels</td><td>/hotels.html</td></tr>
	 * <tr><td>/*.html</td><td>/*.txt</td><td>IllegalArgumentException</td></tr> </table>
	 * @param pattern1 the first pattern
	 * @param pattern2 the second pattern
	 * @return the combination of the two patterns
	 * @throws IllegalArgumentException when the two patterns cannot be combined
	 */
	public String combine(String pattern1, String pattern2) {
		if (!StringUtils.hasText(pattern1) && !StringUtils.hasText(pattern2)) {
			return "";
		}
		else if (!StringUtils.hasText(pattern1)) {
			return pattern2;
		}
		else if (!StringUtils.hasText(pattern2)) {
			return pattern1;
		}

		boolean pattern1ContainsUriVar = pattern1.indexOf('{') != -1;
		if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
			// /* + /hotel -> /hotel ; "/*.*" + "/*.html" -> /*.html
			// However /user + /user -> /usr/user ; /{foo} + /bar -> /{foo}/bar
			return pattern2;
		}
		else if (pattern1.endsWith("/*")) {
			if (pattern2.startsWith("/")) {
				// /hotels/* + /booking -> /hotels/booking
				return pattern1.substring(0, pattern1.length() - 1) + pattern2.substring(1);
			}
			else {
				// /hotels/* + booking -> /hotels/booking
				return pattern1.substring(0, pattern1.length() - 1) + pattern2;
			}
		}
		else if (pattern1.endsWith("/**")) {
			if (pattern2.startsWith("/")) {
				// /hotels/** + /booking -> /hotels/**/booking
				return pattern1 + pattern2;
			}
			else {
				// /hotels/** + booking -> /hotels/**/booking
				return pattern1 + "/" + pattern2;
			}
		}
		else {
			int dotPos1 = pattern1.indexOf('.');
			if (dotPos1 == -1 || pattern1ContainsUriVar) {
				// simply concatenate the two patterns
				if (pattern1.endsWith("/") || pattern2.startsWith("/")) {
					return pattern1 + pattern2;
				}
				else {
					return pattern1 + "/" + pattern2;
				}
			}
			String fileName1 = pattern1.substring(0, dotPos1);
			String extension1 = pattern1.substring(dotPos1);
			String fileName2;
			String extension2;
			int dotPos2 = pattern2.indexOf('.');
			if (dotPos2 != -1) {
				fileName2 = pattern2.substring(0, dotPos2);
				extension2 = pattern2.substring(dotPos2);
			}
			else {
				fileName2 = pattern2;
				extension2 = "";
			}
			String fileName = fileName1.endsWith("*") ? fileName2 : fileName1;
			String extension = extension1.startsWith("*") ? extension2 : extension1;

			return fileName + extension;
		}
	}

	/**
	 * Given a full path, returns a {@link Comparator} suitable for sorting patterns in order of explicitness.
	 * <p>The returned {@code Comparator} will {@linkplain java.util.Collections#sort(java.util.List,
	 * java.util.Comparator) sort} a list so that more specific patterns (without uri templates or wild cards) come before
	 * generic patterns. So given a list with the following patterns: <ol> <li>{@code /hotels/new}</li>
	 * <li>{@code /hotels/{hotel}}</li> <li>{@code /hotels/*}</li> </ol> the returned comparator will sort this
	 * list so that the order will be as indicated.
	 * <p>The full path given as parameter is used to test for exact matches. So when the given path is {@code /hotels/2},
	 * the pattern {@code /hotels/2} will be sorted before {@code /hotels/1}.
	 * @param path the full path to use for comparison
	 * @return a comparator capable of sorting patterns in order of explicitness
	 */
	public Comparator<String> getPatternComparator(String path) {
		return new AntPatternComparator(path);
	}


	private static class AntPatternComparator implements Comparator<String> {

		private final String path;

		private AntPatternComparator(String path) {
			this.path = path;
		}

		public int compare(String pattern1, String pattern2) {
			if (pattern1 == null && pattern2 == null) {
				return 0;
			}
			else if (pattern1 == null) {
				return 1;
			}
			else if (pattern2 == null) {
				return -1;
			}
			boolean pattern1EqualsPath = pattern1.equals(path);
			boolean pattern2EqualsPath = pattern2.equals(path);
			if (pattern1EqualsPath && pattern2EqualsPath) {
				return 0;
			}
			else if (pattern1EqualsPath) {
				return -1;
			}
			else if (pattern2EqualsPath) {
				return 1;
			}
			int wildCardCount1 = getWildCardCount(pattern1);
			int wildCardCount2 = getWildCardCount(pattern2);

			int bracketCount1 = StringUtils.countOccurrencesOf(pattern1, "{");
			int bracketCount2 = StringUtils.countOccurrencesOf(pattern2, "{");

			int totalCount1 = wildCardCount1 + bracketCount1;
			int totalCount2 = wildCardCount2 + bracketCount2;

			if (totalCount1 != totalCount2) {
				return totalCount1 - totalCount2;
			}

			int pattern1Length = getPatternLength(pattern1);
			int pattern2Length = getPatternLength(pattern2);

			if (pattern1Length != pattern2Length) {
				return pattern2Length - pattern1Length;
			}

			if (wildCardCount1 < wildCardCount2) {
				return -1;
			}
			else if (wildCardCount2 < wildCardCount1) {
				return 1;
			}

			if (bracketCount1 < bracketCount2) {
				return -1;
			}
			else if (bracketCount2 < bracketCount1) {
				return 1;
			}

			return 0;
		}

		private int getWildCardCount(String pattern) {
			if (pattern.endsWith(".*")) {
				pattern = pattern.substring(0, pattern.length() - 2);
			}
			return StringUtils.countOccurrencesOf(pattern, "*");
		}

		/**
		 * Returns the length of the given pattern, where template variables are considered to be 1 long.
		 */
		private int getPatternLength(String pattern) {
			Matcher m = VARIABLE_PATTERN.matcher(pattern);
			return m.replaceAll("#").length();
		}
	}


	/**
	 * Tests whether or not a string matches against a pattern via a {@link Pattern}.
	 * <p>The pattern may contain special characters: '*' means zero or more characters; '?' means one and
	 * only one character; '{' and '}' indicate a URI template pattern. For example <tt>/users/{user}</tt>.
	 */
	private static class AntPathStringMatcher {

		private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}");

		private static final String DEFAULT_VARIABLE_PATTERN = "(.*)";

		private final Pattern pattern;

		private final List<String> variableNames = new LinkedList<String>();

		public AntPathStringMatcher(String pattern) {
			StringBuilder patternBuilder = new StringBuilder();
			Matcher m = GLOB_PATTERN.matcher(pattern);
			int end = 0;
			while (m.find()) {
				patternBuilder.append(quote(pattern, end, m.start()));
				String match = m.group();
				if ("?".equals(match)) {
					patternBuilder.append('.');
				}
				else if ("*".equals(match)) {
					patternBuilder.append(".*");
				}
				else if (match.startsWith("{") && match.endsWith("}")) {
					int colonIdx = match.indexOf(':');
					if (colonIdx == -1) {
						patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
						this.variableNames.add(m.group(1));
					}
					else {
						String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
						patternBuilder.append('(');
						patternBuilder.append(variablePattern);
						patternBuilder.append(')');
						String variableName = match.substring(1, colonIdx);
						this.variableNames.add(variableName);
					}
				}
				end = m.end();
			}
			patternBuilder.append(quote(pattern, end, pattern.length()));
			this.pattern = Pattern.compile(patternBuilder.toString());
		}

		private String quote(String s, int start, int end) {
			if (start == end) {
				return "";
			}
			return Pattern.quote(s.substring(start, end));
		}

		/**
		 * Main entry point.
		 * @return {@code true} if the string matches against the pattern, or {@code false} otherwise.
		 */
		public boolean matchStrings(String str, Map<String, String> uriTemplateVariables) {
			Matcher matcher = this.pattern.matcher(str);
			if (matcher.matches()) {
				if (uriTemplateVariables != null) {
					// SPR-8455
					Assert.isTrue(this.variableNames.size() == matcher.groupCount(),
							"The number of capturing groups in the pattern segment " + this.pattern +
							" does not match the number of URI template variables it defines, which can occur if " +
							" capturing groups are used in a URI template regex. Use non-capturing groups instead.");
					for (int i = 1; i <= matcher.groupCount(); i++) {
						String name = this.variableNames.get(i - 1);
						String value = matcher.group(i);
						uriTemplateVariables.put(name, value);
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
	}

}
