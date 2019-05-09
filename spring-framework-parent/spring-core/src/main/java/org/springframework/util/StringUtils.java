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

package org.springframework.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Miscellaneous {@link String} utility methods.
 * 
 * <p>多方面的字符串实用程序方法。
 *
 * <p>Mainly for internal use within the framework; consider
 * <a href="http://jakarta.apache.org/commons/lang/">Jakarta's Commons Lang</a>
 * for a more comprehensive suite of String utilities.
 * 
 * <p>主要供框架内部使用; 考虑<pre>Jakarta's Commons Lang</pre>提供更全面的String实用程序套件。
 *
 * <p>This class delivers some simple functionality that should really
 * be provided by the core Java {@code String} and {@link StringBuilder}
 * classes, such as the ability to {@link #replace} all occurrences of a given
 * substring in a target string. It also provides easy-to-use methods to convert
 * between delimited strings, such as CSV strings, and collections and arrays.
 * 
 * <p>此类提供了一些应由核心Java String和StringBuilder类实际提供的简单功能，例如替
 * 换目标字符串中所有出现的给定子字符串的功能。 它还提供了易于使用的方法来转换分隔字符串，
 * 如CSV字符串，集合和数组。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Rick Evans
 * @author Arjen Poutsma
 * @since 16 April 2001
 */
public abstract class StringUtils {

	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";

	private static final char EXTENSION_SEPARATOR = '.';


	//---------------------------------------------------------------------
	// General convenience methods for working with Strings
	// 使用字符串的一般便捷方法
	//---------------------------------------------------------------------

	/**
	 * Check whether the given String is empty.
	 * 
	 * <p>检查给定的String是否为空.
	 * 
	 * <p>This method accepts any Object as an argument, comparing it to
	 * {@code null} and the empty String. As a consequence, this method
	 * will never return {@code true} for a non-null non-String object.
	 * 
	 * <p>此方法接受任何Object作为参数，将其与null和空String进行比较。 因此，对于非null非String对象，此方法永远不会返回true。
	 * 
	 * <p>The Object signature is useful for general attribute handling code
	 * that commonly deals with Strings but generally has to iterate over
	 * Objects since attributes may e.g. be primitive value objects as well.
	 * 
	 * <p>对象签名对于通常处理字符串的一般属性处理代码很有用，但通常必须迭代对象，因为属性可以例如 也是原始价值对象。
	 * 
	 * @param str the candidate String - 候选字符串
	 * @since 3.2.1
	 */
	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Check that the given CharSequence is neither {@code null} nor of length 0.
	 * Note: Will return {@code true} for a CharSequence that purely consists of whitespace.
	 * 
	 * <p>检查给定的CharSequence既不是null也不是长度为0.注意：对于纯粹由空格组成的CharSequence，将返回true。
	 * 
	 * <p><pre class="code">
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * @param str the CharSequence to check (may be {@code null}) - 要检查的CharSequence（可能为null）
	 * @return {@code true} if the CharSequence is not null and has length - 如果CharSequence不为null且具有长度，则返回true
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check that the given String is neither {@code null} nor of length 0.
	 * Note: Will return {@code true} for a String that purely consists of whitespace.
	 * 
	 * <p>检查给定的String既不是null也不是长度为0.注意：对于纯粹由空格组成的String，将返回true。
	 * @param str the String to check (may be {@code null}) - 要检查的字符串（可以为null）
	 * @return {@code true} if the String is not null and has length - 如果String不为null且具有长度
	 * @see #hasLength(CharSequence)
	 */
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * Check whether the given CharSequence has actual text.
	 * More specifically, returns {@code true} if the string not {@code null},
	 * its length is greater than 0, and it contains at least one non-whitespace character.
	 * 
	 * <p>检查给定的CharSequence是否有实际文本。 更具体地说，
	 * 如果字符串不为null，其长度大于0，并且它包含至少一个非空白字符，则返回true。
	 * <p><pre class="code">
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * 
	 * @param str the CharSequence to check (may be {@code null}) - 要检查的CharSequence（可能为null）
	 * @return {@code true} if the CharSequence is not {@code null},
	 * its length is greater than 0, and it does not contain whitespace only
	 * 
	 * <p>如果CharSequence不为null，则其长度大于0，并且它不包含空格
	 * @see Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given String has actual text.
	 * More specifically, returns {@code true} if the string not {@code null},
	 * its length is greater than 0, and it contains at least one non-whitespace character.
	 * 
	 * <p>检查给定的String是否包含实际文本。 更具体地说，如果字符串不为null，其长度大于0，并且它包含至少一个非空白字符，则返回true。
	 * 
	 * @param str the String to check (may be {@code null}) - 要检查的字符串（可以为null）
	 * @return {@code true} if the String is not {@code null}, its length is
	 * greater than 0, and it does not contain whitespace only
	 * <p>如果String不为null，则其长度大于0，并且它不包含空格
	 * @see #hasText(CharSequence)
	 */
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}

	/**
	 * Check whether the given CharSequence contains any whitespace characters.
	 * 
	 * <p>检查给定的CharSequence是否包含任何空格字符。
	 * 
	 * @param str the CharSequence to check (may be {@code null}) - 要检查的CharSequence（可能为null）
	 * @return {@code true} if the CharSequence is not empty and
	 * contains at least 1 whitespace character
	 * 
	 * <p>如果CharSequence不为空且包含至少1个空格字符，则返回true
	 * 
	 * @see Character#isWhitespace
	 */
	public static boolean containsWhitespace(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given String contains any whitespace characters.
	 * 
	 * <p>检查给定的String是否包含任何空格字符。
	 * 
	 * @param str the String to check (may be {@code null}) - 要检查的字符串（可以为null）
	 * @return {@code true} if the String is not empty and
	 * contains at least 1 whitespace character
	 * 
	 * <p>如果String不为空且包含至少1个空格字符，则返回true
	 * 
	 * @see #containsWhitespace(CharSequence)
	 */
	public static boolean containsWhitespace(String str) {
		return containsWhitespace((CharSequence) str);
	}

	/**
	 * Trim leading and trailing whitespace from the given String.
	 * 
	 * <p>修剪给定String的前导和尾随空格。
	 * 
	 * @param str the String to check - 要检查的字符串
	 * @return the trimmed String - 修剪过的字符串
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trim <i>all</i> whitespace from the given String:
	 * leading, trailing, and in between characters.
	 * 
	 * <p>修剪给定字符串中的所有空格：前导，尾随和字符之间。
	 * 
	 * @param str the String to check - 要检查的字符串
	 * @return the trimmed String - 修剪过的字符串
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimAllWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		int index = 0;
		while (sb.length() > index) {
			if (Character.isWhitespace(sb.charAt(index))) {
				sb.deleteCharAt(index);
			}
			else {
				index++;
			}
		}
		return sb.toString();
	}

	/**
	 * Trim leading whitespace from the given String. - 修剪给定String的前导空格。
	 * @param str the String to check - 要检查的字符串
	 * @return the trimmed String - 修剪过的字符串
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim trailing whitespace from the given String. - 修剪给定String的尾随空格。
	 * @param str the String to check - 要检查的字符串
	 * @return the trimmed String - 修剪过的字符串
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurrences of the supplied leading character from the given String.
	 * 
	 * <p>修剪从给定String中出现的所有提供的前导字符。
	 * 
	 * @param str the String to check - 要检查的字符串
	 * @param leadingCharacter the leading character to be trimmed - 要修剪的主要角色
	 * @return the trimmed String - 修剪过的字符串
	 */
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurrences of the supplied trailing character from the given String.
	 * 
	 * <p>修剪从给定String中出现的所有提供的尾随字符。
	 * 
	 * @param str the String to check - 要检查的字符串
	 * @param trailingCharacter the trailing character to be trimmed - 要修剪的尾随字符
	 * @return the trimmed String
	 */
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}


	/**
	 * Test if the given String starts with the specified prefix,
	 * ignoring upper/lower case.
	 * 
	 * <p>测试给定的String是否以指定的前缀开头，忽略大写/小写。
	 * 
	 * @param str the String to check - 要检查的字符串
	 * @param prefix the prefix to look for - 要查找的前缀
	 * @see java.lang.String#startsWith
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}

	/**
	 * Test if the given String ends with the specified suffix,
	 * ignoring upper/lower case.
	 * 
	 * <p>测试给定的String是否以指定的后缀结尾，忽略大写/小写。
	 * 
	 * @param str the String to check - 要检查的字符串
	 * @param suffix the suffix to look for - 要查找的后缀
	 * @see java.lang.String#endsWith
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		if (str.endsWith(suffix)) {
			return true;
		}
		if (str.length() < suffix.length()) {
			return false;
		}

		String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
		String lcSuffix = suffix.toLowerCase();
		return lcStr.equals(lcSuffix);
	}

	/**
	 * Test whether the given string matches the given substring
	 * at the given index.
	 * 
	 * <p>测试给定字符串是否与给定索引处的给定子字符串匹配。
	 * 
	 * @param str the original string (or StringBuilder) - 原始字符串（或StringBuilder）
	 * @param index the index in the original string to start matching against - 原始字符串中的索引开始匹配
	 * @param substring the substring to match at the given index - 要在给定索引处匹配的子字符串
	 */
	public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
		for (int j = 0; j < substring.length(); j++) {
			int i = index + j;
			if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Count the occurrences of the substring in string s.
	 * 
	 * <p>计算字符串s中子字符串的出现次数。
	 * 
	 * @param str string to search in. Return 0 if this is null. - 要搜索的字符串。如果为null，则返回0。
	 * @param sub string to search for. Return 0 if this is null. - 要搜索的字符串。 如果为null，则返回0。
	 */
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	/**
	 * Replace all occurrences of a substring within a string with
	 * another string. 
	 * 
	 * <p> 用字符串替换字符串中所有出现的子字符串。
	 * 
	 * @param inString String to examine - 要检查的字符串
	 * @param oldPattern String to replace - 要替换的字符串
	 * @param newPattern String to insert - 要插入的字符串
	 * @return a String with the replacements - 带有替换的String
	 */
	public static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sb.append(inString.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	/**
	 * Delete all occurrences of the given substring.
	 * 
	 * <p>删除给定子字符串的所有匹配项。
	 * 
	 * @param inString the original String - 原始字符串
	 * @param pattern the pattern to delete all occurrences of - 删除所有出现的模式
	 * @return the resulting String - 结果字符串
	 */
	public static String delete(String inString, String pattern) {
		return replace(inString, pattern, "");
	}

	/**
	 * Delete any character in a given String. - 删除给定String中的任何字符。
	 * @param inString the original String - 原始字符串
	 * @param charsToDelete a set of characters to delete.
	 * E.g. "az\n" will delete 'a's, 'z's and new lines.
	 * 
	 * <p>要删除的一组字符。 例如。 “az \ n”将删除'a'，'z'和新行。
	 * 
	 * @return the resulting String - 结果字符串
	 */
	public static String deleteAny(String inString, String charsToDelete) {
		if (!hasLength(inString) || !hasLength(charsToDelete)) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// Convenience methods for working with formatted Strings
	// 使用格式化字符串的便捷方法
	//---------------------------------------------------------------------

	/**
	 * Quote the given String with single quotes.
	 * 
	 * <p>用单引号引用给定的String。
	 * 
	 * @param str the input String (e.g. "myString") - 输入字符串（例如“myString”）
	 * @return the quoted String (e.g. "'myString'"),
	 * or {@code null} if the input was {@code null}
	 * 
	 * <p>引用的String（例如“'myString'”），如果输入为null，则返回null
	 * 
	 */
	public static String quote(String str) {
		return (str != null ? "'" + str + "'" : null);
	}

	/**
	 * Turn the given Object into a String with single quotes
	 * if it is a String; keeping the Object as-is else.
	 * 
	 * <p>如果它是一个String，则将给定的Object转换为带单引号的String; 保持对象不在其他地方。
	 * 
	 * @param obj the input Object (e.g. "myString") - 输入对象（例如“myString”）
	 * @return the quoted String (e.g. "'myString'"),
	 * or the input object as-is if not a String
	 * 
	 * <p>引用的String（例如“'myString'”），或输入对象as-is，如果不是String
	 */
	public static Object quoteIfString(Object obj) {
		return (obj instanceof String ? quote((String) obj) : obj);
	}

	/**
	 * Unqualify a string qualified by a '.' dot character. For example,
	 * "this.name.is.qualified", returns "qualified".
	 * 
	 * <p>取消限定由'.'限定的字符串。 点字符。 例如，“this.name.is.qualified”，返回“qualified”。
	 * 
	 * @param qualifiedName the qualified name - 合格的名字
	 */
	public static String unqualify(String qualifiedName) {
		return unqualify(qualifiedName, '.');
	}

	/**
	 * Unqualify a string qualified by a separator character. For example,
	 * "this:name:is:qualified" returns "qualified" if using a ':' separator.
	 * 
	 * <p>取消限定由分隔符限定的字符串。 例如，如果使用'：'分隔符，则“this：name：is：qualified”将返回“qualified”。
	 * 
	 * @param qualifiedName the qualified name - 合格的名字
	 * @param separator the separator - 分隔符
	 */
	public static String unqualify(String qualifiedName, char separator) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
	}

	/**
	 * Capitalize a {@code String}, changing the first letter to
	 * upper case as per {@link Character#toUpperCase(char)}.
	 * No other letters are changed.
	 * 
	 * <p>大写字符串，根据Character.toUpperCase（char）将第一个字母更改为大写字母。 没有其他字母被更改。
	 * 
	 * @param str the String to capitalize, may be {@code null} - 要大写的String，可以为null
	 * @return the capitalized String, {@code null} if null - 大写字符串，如果为null则为null
	 */
	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}

	/**
	 * Uncapitalize a {@code String}, changing the first letter to
	 * lower case as per {@link Character#toLowerCase(char)}.
	 * No other letters are changed.
	 * 
	 * <p>取消大写字符串，根据Character.toLowerCase（char）将第一个字母更改为小写字母。 没有其他字母被更改。
	 * 
	 * @param str the String to uncapitalize, may be {@code null} - 要取消大写的字符串，可以为null
	 * @return the uncapitalized String, {@code null} if null - 未大写的字符串，如果为空，则为空。
	 */
	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str.length());
		if (capitalize) {
			sb.append(Character.toUpperCase(str.charAt(0)));
		}
		else {
			sb.append(Character.toLowerCase(str.charAt(0)));
		}
		sb.append(str.substring(1));
		return sb.toString();
	}

	/**
	 * Extract the filename from the given path,
	 * e.g. "mypath/myfile.txt" -> "myfile.txt".
	 * 
	 * <p>从给定路径中提取文件名，例如 “mypath / myfile.txt” - >“myfile.txt”。
	 * 
	 * @param path the file path (may be {@code null}) - 文件路径（可能为null）
	 * @return the extracted filename, or {@code null} if none - 提取的文件名，如果没有则为null
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}

	/**
	 * Extract the filename extension from the given path,
	 * e.g. "mypath/myfile.txt" -> "txt".
	 * 
	 * <p>从给定路径中提取文件扩展名，例如 “mypath / myfile.txt” - >“txt”。
	 * 
	 * @param path the file path (may be {@code null}) - 文件路径（可能为null）
	 * @return the extracted filename extension, or {@code null} if none
	 * 
	 * <p>提取的文件扩展名，如果没有则为null
	 */
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return null;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return null;
		}
		return path.substring(extIndex + 1);
	}

	/**
	 * Strip the filename extension from the given path,
	 * e.g. "mypath/myfile.txt" -> "mypath/myfile".
	 * 
	 * <p>从给定路径中删除文件扩展名，例如 “mypath / myfile.txt” - >“mypath / myfile”。
	 * 
	 * @param path the file path (may be {@code null}) - 文件路径（可能为null）
	 * @return the path with stripped filename extension,
	 * or {@code null} if none
	 * 
	 * <p>带有文件扩展名的路径，如果没有则为null
	 * 
	 */
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return path;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return path;
		}
		return path.substring(0, extIndex);
	}

	/**
	 * Apply the given relative path to the given path,
	 * assuming standard Java folder separation (i.e. "/" separators).
	 * 
	 * <p>假定标准Java文件夹分离（即“/”分隔符），将给定的相对路径应用于给定路径。
	 * 
	 * @param path the path to start from (usually a full file path) - 从哪条路径开始（通常是完整的文件路径）
	 * @param relativePath the relative path to apply
	 * (relative to the full file path above)
	 * 
	 * <p>应用的相对路径（相对于上面的完整文件路径）
	 * 
	 * @return the full file path that results from applying the relative path - 应用相对路径产生的完整文件路径
	 */
	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR;
			}
			return newPath + relativePath;
		}
		else {
			return relativePath;
		}
	}

	/**
	 * Normalize the path by suppressing sequences like "path/.." and
	 * inner simple dots.
	 * <p>The result is convenient for path comparison. For other uses,
	 * notice that Windows separators ("\") are replaced by simple slashes.
	 * 
	 * <p>通过抑制“path / ..”和内部简单点等序列来规范化路径。结果便于路径比较。 对于其他用途，请注意Windows分隔符（“\”）由简单斜杠替换。
	 * @param path the original path
	 * @return the normalized path  规范化的路径
	 */
	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		}
		String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			if (prefix.contains("/")) {
				prefix = "";
			}
			else {
				pathToUse = pathToUse.substring(prefixIndex + 1);
			}
		}
		if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
			prefix = prefix + FOLDER_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
				// Points to current directory - drop it.
			}
			else if (TOP_PATH.equals(element)) {
				// Registering top path found.
				tops++;
			}
			else {
				if (tops > 0) {
					// Merging path element with element corresponding to top path.
					tops--;
				}
				else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
	}

	/**
	 * Compare two paths after normalization of them.
	 * 
	 * <p>在规范化它们之后比较两个路径。
	 * 
	 * @param path1 first path for comparison - 比较的第一条路径
	 * @param path2 second path for comparison - 第二条比较路径
	 * @return whether the two paths are equivalent after normalization - 标准化后两条路径是否相等
	 */
	public static boolean pathEquals(String path1, String path2) {
		return cleanPath(path1).equals(cleanPath(path2));
	}

	/**
	 * Parse the given {@code localeString} value into a {@link Locale}.
	 * 
	 * <p>将给定的localeString值解析为Locale。
	 * 
	 * <p>This is the inverse operation of {@link Locale#toString Locale's toString}.
	 * 
	 * <p>这是Locale的toString的逆操作。
	 * 
	 * @param localeString the locale String, following {@code Locale's}
	 * {@code toString()} format ("en", "en_UK", etc);
	 * also accepts spaces as separators, as an alternative to underscores
	 * 
	 * <p>locale字符串，遵循Locale的toString（）格式（“en”，“en_UK”等）; 也接受空格作为分隔符，作为下划线的替代
	 * 
	 * @return a corresponding {@code Locale} instance - 相应的Locale实例
	 * @throws IllegalArgumentException in case of an invalid locale specification - 如果区域设置规范无效
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		validateLocalePart(language);
		validateLocalePart(country);
		String variant = "";
		if (parts.length > 2) {
			// There is definitely a variant, and it is everything after the country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the variant.
			variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
			if (variant.startsWith("_")) {
				variant = trimLeadingCharacter(variant, '_');
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	private static void validateLocalePart(String localePart) {
		for (int i = 0; i < localePart.length(); i++) {
			char ch = localePart.charAt(i);
			if (ch != '_' && ch != ' ' && !Character.isLetterOrDigit(ch)) {
				throw new IllegalArgumentException(
						"Locale part \"" + localePart + "\" contains invalid characters");
			}
		}
	}

	/**
	 * Determine the RFC 3066 compliant language tag,
	 * as used for the HTTP "Accept-Language" header.
	 * 
	 * <p>确定符合RFC 3066的语言标记，用于HTTP“Accept-Language”标头。
	 * 
	 * @param locale the Locale to transform to a language tag - 要转换为语言标记的区域设置
	 * @return the RFC 3066 compliant language tag as String - 符合RFC 3066的语言标记为String
	 */
	public static String toLanguageTag(Locale locale) {
		return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
	}


	//---------------------------------------------------------------------
	// Convenience methods for working with String arrays
	// 使用String数组的便捷方法
	//---------------------------------------------------------------------

	/**
	 * Append the given String to the given String array, returning a new array
	 * consisting of the input array contents plus the given String.
	 * 
	 * <p>将给定的String附加到给定的String数组，返回一个由输入数组内容和给定String组成的新数组。
	 * 
	 * @param array the array to append to (can be {@code null}) - 要追加的数组（可以为null）
	 * @param str the String to append - 要追加的字符串
	 * @return the new array (never {@code null}) - 新数组（永不为null）
	 */
	public static String[] addStringToArray(String[] array, String str) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[] {str};
		}
		String[] newArr = new String[array.length + 1];
		System.arraycopy(array, 0, newArr, 0, array.length);
		newArr[array.length] = str;
		return newArr;
	}

	/**
	 * Concatenate the given String arrays into one,
	 * with overlapping array elements included twice.
	 * 
	 * <p>将给定的String数组连接成一个，重叠的数组元素包含两次。
	 * 
	 * <p>The order of elements in the original arrays is preserved.
	 * 
	 * <p>保留原始数组中元素的顺序。
	 * 
	 * @param array1 the first array (can be {@code null}) - 第一个数组（可以为null）
	 * @param array2 the second array (can be {@code null}) - 第二个数组（可以为null）
	 * @return the new array ({@code null} if both given arrays were {@code null})
	 * 
	 * <p>保留原始数组中元素的顺序。
	 * 
	 */
	public static String[] concatenateStringArrays(String[] array1, String[] array2) {
		if (ObjectUtils.isEmpty(array1)) {
			return array2;
		}
		if (ObjectUtils.isEmpty(array2)) {
			return array1;
		}
		String[] newArr = new String[array1.length + array2.length];
		System.arraycopy(array1, 0, newArr, 0, array1.length);
		System.arraycopy(array2, 0, newArr, array1.length, array2.length);
		return newArr;
	}

	/**
	 * Merge the given String arrays into one, with overlapping
	 * array elements only included once.
	 * 
	 * <p>将给定的String数组合并为一个，重叠的数组元素只包含一次。
	 * 
	 * <p>The order of elements in the original arrays is preserved
	 * (with the exception of overlapping elements, which are only
	 * included on their first occurrence).
	 * 
	 * <p>保留原始数组中元素的顺序（重叠元素除外，它们仅在第一次出现时包含）。
	 * 
	 * @param array1 the first array (can be {@code null}) - 第一个数组（可以为null）
	 * @param array2 the second array (can be {@code null}) - 第二个数组（可以为null）
	 * @return the new array ({@code null} if both given arrays were {@code null})
	 * 
	 * <p>新数组（如果给定的数组都为null，则为null）
	 * 
	 */
	public static String[] mergeStringArrays(String[] array1, String[] array2) {
		if (ObjectUtils.isEmpty(array1)) {
			return array2;
		}
		if (ObjectUtils.isEmpty(array2)) {
			return array1;
		}
		List<String> result = new ArrayList<String>();
		result.addAll(Arrays.asList(array1));
		for (String str : array2) {
			if (!result.contains(str)) {
				result.add(str);
			}
		}
		return toStringArray(result);
	}

	/**
	 * Turn given source String array into sorted array.
	 * 
	 * <p>将给定的源String数组转换为有序数组。
	 * 
	 * @param array the source array - 源数组
	 * @return the sorted array (never {@code null}) - 排序数组（永不为null）
	 */
	public static String[] sortStringArray(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		Arrays.sort(array);
		return array;
	}

	/**
	 * Copy the given Collection into a String array.
	 * The Collection must contain String elements only.
	 * 
	 * <p>将给定的Collection复制到String数组中。 集合必须仅包含String元素。
	 * 
	 * @param collection the Collection to copy - 要复制的集合
	 * @return the String array ({@code null} if the passed-in
	 * Collection was {@code null})
	 * 
	 * <p>String数组（如果传入的Collection为null，则为null）
	 */
	public static String[] toStringArray(Collection<String> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new String[collection.size()]);
	}

	/**
	 * Copy the given Enumeration into a String array.
	 * The Enumeration must contain String elements only.
	 * 
	 * <p>将给定的Enumeration复制到String数组中。 Enumeration只能包含String元素。
	 * 
	 * @param enumeration the Enumeration to copy - 要复制的枚举
	 * @return the String array ({@code null} if the passed-in
	 * Enumeration was {@code null})
	 * 
	 * <p>String数组（如果传入的Enumeration为null，则为null）
	 * 
	 */
	public static String[] toStringArray(Enumeration<String> enumeration) {
		if (enumeration == null) {
			return null;
		}
		List<String> list = Collections.list(enumeration);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Trim the elements of the given String array,
	 * calling {@code String.trim()} on each of them.
	 * 
	 * <p>修剪给定String数组的元素，在每个元素上调用String.trim（）。
	 * 
	 * @param array the original String array - 原始的String数组
	 * @return the resulting array (of the same size) with trimmed elements
	 * 
	 * <p>带有修剪元素的结果数组（大小相同）
	 * 
	 */
	public static String[] trimArrayElements(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String element = array[i];
			result[i] = (element != null ? element.trim() : null);
		}
		return result;
	}

	/**
	 * Remove duplicate Strings from the given array.
	 * Also sorts the array, as it uses a TreeSet.
	 * 
	 * <p>从给定数组中删除重复的字符串。 还对数组进行排序，因为它使用TreeSet。
	 * 
	 * @param array the String array - String数组
	 * @return an array without duplicates, in natural sort order
	 * 
	 * <p>一个没有重复的数组，按自然排序顺序排列
	 * 
	 */
	public static String[] removeDuplicateStrings(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return array;
		}
		Set<String> set = new TreeSet<String>();
		for (String element : array) {
			set.add(element);
		}
		return toStringArray(set);
	}

	/**
	 * Split a String at the first occurrence of the delimiter.
	 * Does not include the delimiter in the result.
	 * 
	 * <p>在第一次出现分隔符时拆分String。 结果中不包括分隔符。
	 * 
	 * @param toSplit the string to split - 要拆分的字符串
	 * @param delimiter to split the string up with - 将字符串拆分为
	 * @return a two element array with index 0 being before the delimiter, and
	 * index 1 being after the delimiter (neither element includes the delimiter);
	 * or {@code null} if the delimiter wasn't found in the given input String
	 * 
	 * <p>一个两元素数组，索引0在分隔符之前，索引1在分隔符之后（两个元素都不包括分隔符）; 
	 * 如果在给定的输入String中找不到分隔符，则返回null
	 */
	public static String[] split(String toSplit, String delimiter) {
		if (!hasLength(toSplit) || !hasLength(delimiter)) {
			return null;
		}
		int offset = toSplit.indexOf(delimiter);
		if (offset < 0) {
			return null;
		}
		String beforeDelimiter = toSplit.substring(0, offset);
		String afterDelimiter = toSplit.substring(offset + delimiter.length());
		return new String[] {beforeDelimiter, afterDelimiter};
	}

	/**
	 * Take an array Strings and split each element based on the given delimiter.
	 * A {@code Properties} instance is then generated, with the left of the
	 * delimiter providing the key, and the right of the delimiter providing the value.
	 * 
	 * <p>获取数组字符串并根据给定的分隔符拆分每个元素。 
	 * 然后生成一个Properties实例，分隔符的左边提供密钥，分隔符的右边提供值。
	 * 
	 * <p>Will trim both the key and value before adding them to the
	 * {@code Properties} instance.
	 * 
	 * <p>在将它们添加到Properties实例之前，将修剪键和值。
	 * 
	 * @param array the array to process - 要处理的数组
	 * @param delimiter to split each element using (typically the equals symbol) - 使用（通常是等号）拆分每个元素
	 * @return a {@code Properties} instance representing the array contents,
	 * or {@code null} if the array to process was null or empty
	 * 
	 * <p>表示数组内容的Properties实例，如果要处理的数组为null或为空，则返回null
	 * 
	 */
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
		return splitArrayElementsIntoProperties(array, delimiter, null);
	}

	/**
	 * Take an array Strings and split each element based on the given delimiter.
	 * A {@code Properties} instance is then generated, with the left of the
	 * delimiter providing the key, and the right of the delimiter providing the value.
	 * 
	 * <p>获取数组字符串并根据给定的分隔符拆分每个元素。 然后生成一个Properties实例，分隔符的左边提供密钥，分隔符的右边提供值。
	 * 
	 * <p>Will trim both the key and value before adding them to the
	 * {@code Properties} instance.
	 * 
	 * <p>在将它们添加到Properties实例之前，将修剪键和值。
	 * 
	 * @param array the array to process - 要处理的数组
	 * @param delimiter to split each element using (typically the equals symbol) - 使用（通常是等号）拆分每个元素
	 * @param charsToDelete one or more characters to remove from each element
	 * prior to attempting the split operation (typically the quotation mark
	 * symbol), or {@code null} if no removal should occur
	 * 
	 * <p>在尝试拆分操作之前从每个元素中删除一个或多个字符（通常是引号符号），如果不应该删除则返回null
	 * 
	 * @return a {@code Properties} instance representing the array contents,
	 * or {@code null} if the array to process was {@code null} or empty
	 * 
	 * <p>表示数组内容的Properties实例，如果要处理的数组为null或为空，则返回null
	 * 
	 */
	public static Properties splitArrayElementsIntoProperties(
			String[] array, String delimiter, String charsToDelete) {

		if (ObjectUtils.isEmpty(array)) {
			return null;
		}
		Properties result = new Properties();
		for (String element : array) {
			if (charsToDelete != null) {
				element = deleteAny(element, charsToDelete);
			}
			String[] splittedElement = split(element, delimiter);
			if (splittedElement == null) {
				continue;
			}
			result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
		}
		return result;
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * Trims tokens and omits empty tokens.
	 * 
	 * <p>通过StringTokenizer将给定的String标记为String数组。 修剪标记并省略空标记。
	 * 
	 * <p>The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using {@code delimitedListToStringArray}
	 * 
	 * <p>给定的分隔符字符串应该包含任意数量的分隔符字符。 这些字符中的每一个都可用于分隔令牌。
	 *  分隔符始终是单个字符; 对于多字符分隔符，请考虑使用delimitedListToStringArray
	 *  
	 * @param str the String to tokenize - 要标记化的String
	 * @param delimiters the delimiter characters, assembled as String
	 * (each of those characters is individually considered as delimiter).
	 * 
	 * <p>分隔符字符，汇编为String（每个字符都被单独视为分隔符）。
	 * 
	 * @return an array of the tokens - 一系列令牌
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * 
	 * <p>通过StringTokenizer将给定的String标记为String数组。
	 * 
	 * <p>The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using {@code delimitedListToStringArray}
	 * 
	 * <p>给定的分隔符字符串应该包含任意数量的分隔符字符。 这些字符中的每一个都可用于分隔令牌。 
	 * 分隔符始终是单个字符; 对于多字符分隔符，请考虑使用delimitedListToStringArray
	 * 
	 * @param str the String to tokenize - 要标记化的String
	 * @param delimiters the delimiter characters, assembled as String
	 * (each of those characters is individually considered as delimiter)
	 * 
	 * <p>分隔符字符，汇编为String（每个字符分别被视为分隔符）
	 * 
	 * @param trimTokens trim the tokens via String's {@code trim} - 通过String的修剪修剪标记
	 * @param ignoreEmptyTokens omit empty tokens from the result array
	 * (only applies to tokens that are empty after trimming; StringTokenizer
	 * will not consider subsequent delimiters as token in the first place).
	 * 
	 * <p>省略结果数组中的空标记（仅适用于修剪后为空的标记; StringTokenizer首先不会将后续分隔符视为标记）。
	 * 
	 * @return an array of the tokens ({@code null} if the input String
	 * was {@code null})
	 * 
	 * <p>标记数组（如果输入String为null，则为null）
	 * 
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(
			String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * 
	 * <p>获取一个作为分隔列表的String并将其转换为String数组。
	 * 
	 * <p>A single delimiter can consists of more than one character: It will still
	 * be considered as single delimiter string, rather than as bunch of potential
	 * delimiter characters - in contrast to {@code tokenizeToStringArray}.
	 * 
	 * <p>单个分隔符可以包含多个字符：它仍将被视为单个分隔符字符串，而不是一堆潜在的分隔符字符 - 与tokenizeToStringArray相反。
	 * @param str the input String - 输入字符串
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 * rather than a bunch individual delimiter characters)
	 * 
	 * <p>元素之间的分隔符（这是一个单独的分隔符，而不是一堆单独的分隔符字符）
	 * 
	 * @return an array of the tokens in the list - 列表中的标记数组
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * 
	 * <p>获取一个作为分隔列表的String并将其转换为String数组。
	 * 
	 * <p>A single delimiter can consists of more than one character: It will still
	 * be considered as single delimiter string, rather than as bunch of potential
	 * delimiter characters - in contrast to {@code tokenizeToStringArray}.
	 * 
	 * <p>单个分隔符可以包含多个字符：它仍将被视为单个分隔符字符串，而不是一堆潜在的分隔符字符 - 与tokenizeToStringArray相反。
	 * 
	 * @param str the input String - 输入字符串
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 * rather than a bunch individual delimiter characters)
	 * 
	 * <p>元素之间的分隔符（这是一个单独的分隔符，而不是一堆单独的分隔符字符）
	 * 
	 * @param charsToDelete a set of characters to delete. Useful for deleting unwanted
	 * line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * 
	 * <p>要删除的一组字符。 用于删除不需要的换行符：例如 “\ r \ n \ f”将删除String.Returns中的所有新行和换行符：
	 * 
	 * @return an array of the tokens in the list - 列表中的标记数组
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}
		List<String> result = new ArrayList<String>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}

	/**
	 * Convert a CSV list into an array of Strings.
	 * 
	 * <p>将CSV列表转换为字符串数组。
	 * 
	 * @param str the input String - 输入字符串
	 * @return an array of Strings, or the empty array in case of empty input
	 * 
	 * <p>一个字符串数组，或空输入时的空数组
	 * 
	 */
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}

	/**
	 * Convenience method to convert a CSV string list to a set.
	 * Note that this will suppress duplicates.
	 * 
	 * <p>将CSV字符串列表转换为集合的便捷方法。 请注意，这将抑制重复。
	 *  
	 * @param str the input String 输入字符串
	 * @return a Set of String entries in the list - 列表中的一组字符串条目
	 */
	public static Set<String> commaDelimitedListToSet(String str) {
		Set<String> set = new TreeSet<String>();
		String[] tokens = commaDelimitedListToStringArray(str);
		for (String token : tokens) {
			set.add(token);
		}
		return set;
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. E.g. useful for {@code toString()} implementations.
	 * 
	 * <p>将Collection作为分隔（例如CSV）字符串返回的便捷方法。 例如。 对toString（）实现很有用。
	 * 
	 * @param coll the Collection to display - 要显示的集合
	 * @param delim the delimiter to use (probably a ",") - 要使用的分隔符（可能是“，”）
	 * @param prefix the String to start each element with - 用于启动每个元素的String
	 * @param suffix the String to end each element with - 用于结束每个元素的String
	 * @return the delimited String - 分隔的字符串
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
		if (CollectionUtils.isEmpty(coll)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. E.g. useful for {@code toString()} implementations.
	 * 
	 * <p>将Collection作为分隔（例如CSV）字符串返回的便捷方法。 例如。 对toString（）实现很有用。
	 * 
	 * @param coll the Collection to display - 要显示的集合
	 * @param delim the delimiter to use (probably a ",") - 使用分隔符（可能是“，”）
	 * @return the delimited String - 分隔的字符串
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Convenience method to return a Collection as a CSV String.
	 * E.g. useful for {@code toString()} implementations.
	 * 
	 * <p>将Collection作为CSV字符串返回的便捷方法。 例如。 对toString（）实现很有用。
	 * 
	 * @param coll the Collection to display - 要显示的集合
	 * @return the delimited String - 分隔的字符串
	 */
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}

	/**
	 * Convenience method to return a String array as a delimited (e.g. CSV)
	 * String. E.g. useful for {@code toString()} implementations.
	 * 
	 * <p>将String数组作为分隔（例如CSV）字符串返回的便捷方法。 例如。 对toString（）实现很有用。
	 * 
	 * @param arr the array to display - 要显示的数组
	 * @param delim the delimiter to use (probably a ",") - 要使用的分隔符（可能是“，”）
	 * @return the delimited String - 分隔的字符串
	 */
	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if (ObjectUtils.isEmpty(arr)) {
			return "";
		}
		if (arr.length == 1) {
			return ObjectUtils.nullSafeToString(arr[0]);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * Convenience method to return a String array as a CSV String.
	 * E.g. useful for {@code toString()} implementations.
	 * 
	 * <p>将String数组作为CSV字符串返回的便捷方法。 例如。 对toString（）实现很有用。
	 * 
	 * @param arr the array to display - 要显示的数组
	 * @return the delimited String - 分隔的字符串
	 */
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}

}
