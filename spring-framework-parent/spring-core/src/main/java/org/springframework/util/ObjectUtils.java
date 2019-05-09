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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Miscellaneous object utility methods.
 * Mainly for internal use within the framework.
 * 
 * <p> 杂项对象实用程序方法。 主要供框架内部使用。
 *
 * <p>Thanks to Alex Ruiz for contributing several enhancements to this class!
 * 
 * <p> 感谢Alex Ruiz为本类程提供了多项增强功能！
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Chris Beams
 * @since 19.03.2004
 */
public abstract class ObjectUtils {

	private static final int INITIAL_HASH = 7;
	private static final int MULTIPLIER = 31;

	private static final String EMPTY_STRING = "";
	private static final String NULL_STRING = "null";
	private static final String ARRAY_START = "{";
	private static final String ARRAY_END = "}";
	private static final String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
	private static final String ARRAY_ELEMENT_SEPARATOR = ", ";


	/**
	 * Return whether the given throwable is a checked exception:
	 * that is, neither a RuntimeException nor an Error.
	 * 
	 * <p> 返回给定的throwable是否为已检查的异常：即，既不是RuntimeException也不是Error。
	 * 
	 * @param ex the throwable to check - 扔掉检查
	 * @return whether the throwable is a checked exception - throwable是否是一个经过检查的异常
	 * @see java.lang.Exception
	 * @see java.lang.RuntimeException
	 * @see java.lang.Error
	 */
	public static boolean isCheckedException(Throwable ex) {
		return !(ex instanceof RuntimeException || ex instanceof Error);
	}

	/**
	 * Check whether the given exception is compatible with the specified
	 * exception types, as declared in a throws clause.
	 * 
	 * <p> 检查给定的异常是否与throws子句中声明的指定异常类型兼容。
	 * 
	 * @param ex the exception to check - 要检查的异常
	 * @param declaredExceptions the exception types declared in the throws clause
	 * 
	 * <p> throws子句中声明的异常类型
	 * 
	 * @return whether the given exception is compatible
	 * 
	 * <p> 给定的异常是否兼容
	 * 
	 */
	public static boolean isCompatibleWithThrowsClause(Throwable ex, Class<?>... declaredExceptions) {
		if (!isCheckedException(ex)) {
			return true;
		}
		if (declaredExceptions != null) {
			for (Class<?> declaredException : declaredExceptions) {
				if (declaredException.isInstance(ex)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determine whether the given object is an array:
	 * either an Object array or a primitive array.
	 * 
	 * <p> 确定给定对象是否为数组：Object数组或基本数组。
	 * 
	 * @param obj the object to check - 要检查的对象
	 */
	public static boolean isArray(Object obj) {
		return (obj != null && obj.getClass().isArray());
	}

	/**
	 * Determine whether the given array is empty:
	 * i.e. {@code null} or of zero length.
	 * 
	 * <p> 确定给定数组是否为空：即null或零长度。
	 * 
	 * @param array the array to check - 要检查的数组
	 */
	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

	/**
	 * Check whether the given array contains the given element.
	 * 
	 * <p> 检查给定数组是否包含给定元素。
	 * 
	 * @param array the array to check (may be {@code null},
	 * in which case the return value will always be {@code false})
	 * 
	 * <p> 要检查的数组（可能为null，在这种情况下返回值将始终为false）
	 * 
	 * @param element the element to check for - 要检查的元素
	 * @return whether the element has been found in the given array
	 * 
	 * <p> 是否在给定数组中找到了该元素
	 * 
	 */
	public static boolean containsElement(Object[] array, Object element) {
		if (array == null) {
			return false;
		}
		for (Object arrayEle : array) {
			if (nullSafeEquals(arrayEle, element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given array of enum constants contains a constant with the given name,
	 * ignoring case when determining a match.
	 * 
	 * <p> 检查给定的枚举常量数组是否包含具有给定名称的常量，在确定匹配时忽略大小写。
	 * 
	 * @param enumValues the enum values to check, typically the product of a call to MyEnum.values()
	 * 
	 * <p> 要检查的枚举值，通常是对MyEnum.values（）的调用的乘积
	 * 
	 * @param constant the constant name to find (must not be null or empty string)
	 * 
	 * <p> 要查找的常量名称（不能为null或空字符串）
	 * 
	 * @return whether the constant has been found in the given array
	 * 
	 * <p> 是否已在给定数组中找到常量
	 * 
	 */
	public static boolean containsConstant(Enum<?>[] enumValues, String constant) {
		return containsConstant(enumValues, constant, false);
	}

	/**
	 * Check whether the given array of enum constants contains a constant with the given name.
	 * 
	 * <p> 检查给定的枚举常量数组是否包含具有给定名称的常量。
	 * 
	 * @param enumValues the enum values to check, typically the product of a call to MyEnum.values()
	 * 
	 * <p> 要检查的枚举值，通常是对MyEnum.values（）的调用的乘积
	 * 
	 * @param constant the constant name to find (must not be null or empty string)
	 * 
	 * <p> 要查找的常量名称（不能为null或空字符串）
	 * 
	 * @param caseSensitive whether case is significant in determining a match
	 * 
	 * <p> 是否在确定匹配时具有重要意义
	 * 
	 * @return whether the constant has been found in the given array
	 * 
	 * <p> 是否已在给定数组中找到常量
	 * 
	 */
	public static boolean containsConstant(Enum<?>[] enumValues, String constant, boolean caseSensitive) {
		for (Enum<?> candidate : enumValues) {
			if (caseSensitive ?
					candidate.toString().equals(constant) :
					candidate.toString().equalsIgnoreCase(constant)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Case insensitive alternative to {@link Enum#valueOf(Class, String)}.
	 * 
	 * <p> Enum.valueOf（Class，String）的不区分大小写的替代。
	 * 
	 * @param <E> the concrete Enum type - 具体的枚举类型
	 * @param enumValues the array of all Enum constants in question, usually per Enum.values()
	 * 
	 * <p> 有问题的所有枚举常量的数组，通常是每个Enum.values（）
	 * 
	 * @param constant the constant to get the enum value of - 获取枚举值的常量
	 * 
	 * @throws IllegalArgumentException if the given constant is not found in the given array
	 * of enum values. Use {@link #containsConstant(Enum[], String)} as a guard to avoid this exception.
	 * 
	 * <p> 如果在给定的枚举值数组中找不到给定的常量。 使用containsConstant（Enum []，String）作为保护来避免此异常。
	 * 
	 */
	public static <E extends Enum<?>> E caseInsensitiveValueOf(E[] enumValues, String constant) {
		for (E candidate : enumValues) {
			if (candidate.toString().equalsIgnoreCase(constant)) {
				return candidate;
			}
		}
		throw new IllegalArgumentException(
				String.format("constant [%s] does not exist in enum type %s",
						constant, enumValues.getClass().getComponentType().getName()));
	}

	/**
	 * Append the given object to the given array, returning a new array
	 * consisting of the input array contents plus the given object.
	 * 
	 * <p> 将给定对象附加到给定数组，返回由输入数组内容和给定对象组成的新数组。
	 * 
	 * @param array the array to append to (can be {@code null}) - 要追加的数组（可以为null）
	 * @param obj the object to append - 要追加的对象
	 * @return the new array (of the same component type; never {@code null})
	 * 
	 * <p> 新数组（具有相同的组件类型;从不为null）
	 * 
	 */
	public static <A, O extends A> A[] addObjectToArray(A[] array, O obj) {
		Class<?> compType = Object.class;
		if (array != null) {
			compType = array.getClass().getComponentType();
		}
		else if (obj != null) {
			compType = obj.getClass();
		}
		int newArrLength = (array != null ? array.length + 1 : 1);
		@SuppressWarnings("unchecked")
		A[] newArr = (A[]) Array.newInstance(compType, newArrLength);
		if (array != null) {
			System.arraycopy(array, 0, newArr, 0, array.length);
		}
		newArr[newArr.length - 1] = obj;
		return newArr;
	}

	/**
	 * Convert the given array (which may be a primitive array) to an
	 * object array (if necessary of primitive wrapper objects).
	 * 
	 * <p> 将给定数组（可以是基本数组）转换为对象数组（如果需要原始包装器对象）。
	 * 
	 * <p>A {@code null} source value will be converted to an
	 * empty Object array.
	 * 
	 * <p> 空源值将转换为空的Object数组。
	 * 
	 * @param source the (potentially primitive) array - （可能是原始的）数组
	 * @return the corresponding object array (never {@code null})
	 * 
	 * <p> 相应的对象数组（永不为null）
	 * 
	 * @throws IllegalArgumentException if the parameter is not an array
	 * 
	 * <p> 如果参数不是数组
	 * 
	 */
	public static Object[] toObjectArray(Object source) {
		if (source instanceof Object[]) {
			return (Object[]) source;
		}
		if (source == null) {
			return new Object[0];
		}
		if (!source.getClass().isArray()) {
			throw new IllegalArgumentException("Source is not an array: " + source);
		}
		int length = Array.getLength(source);
		if (length == 0) {
			return new Object[0];
		}
		Class<?> wrapperType = Array.get(source, 0).getClass();
		Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			newArray[i] = Array.get(source, i);
		}
		return newArray;
	}


	//---------------------------------------------------------------------
	// Convenience methods for content-based equality/hash-code handling
	// 基于内容的相等/哈希码处理的便捷方法
	//---------------------------------------------------------------------

	/**
	 * Determine if the given objects are equal, returning {@code true}
	 * if both are {@code null} or {@code false} if only one is
	 * {@code null}.
	 * 
	 * <p> 确定给定对象是否相等，如果两者都为null则返回true，如果只有一个为null则返回false。
	 * 
	 * <p>Compares arrays with {@code Arrays.equals}, performing an equality
	 * check based on the array elements rather than the array reference.
	 * 
	 * <p> 使用Arrays.equals比较数组，根据数组元素而不是数组引用执行相等性检查。
	 * 
	 * @param o1 first Object to compare - 第一个要比较的对象
	 * @param o2 second Object to compare - 第二个要比较的对象
	 * @return whether the given objects are equal - 给定的对象是否相等
	 * @see java.util.Arrays#equals
	 */
	public static boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (o1.equals(o2)) {
			return true;
		}
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			if (o1 instanceof Object[] && o2 instanceof Object[]) {
				return Arrays.equals((Object[]) o1, (Object[]) o2);
			}
			if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
				return Arrays.equals((boolean[]) o1, (boolean[]) o2);
			}
			if (o1 instanceof byte[] && o2 instanceof byte[]) {
				return Arrays.equals((byte[]) o1, (byte[]) o2);
			}
			if (o1 instanceof char[] && o2 instanceof char[]) {
				return Arrays.equals((char[]) o1, (char[]) o2);
			}
			if (o1 instanceof double[] && o2 instanceof double[]) {
				return Arrays.equals((double[]) o1, (double[]) o2);
			}
			if (o1 instanceof float[] && o2 instanceof float[]) {
				return Arrays.equals((float[]) o1, (float[]) o2);
			}
			if (o1 instanceof int[] && o2 instanceof int[]) {
				return Arrays.equals((int[]) o1, (int[]) o2);
			}
			if (o1 instanceof long[] && o2 instanceof long[]) {
				return Arrays.equals((long[]) o1, (long[]) o2);
			}
			if (o1 instanceof short[] && o2 instanceof short[]) {
				return Arrays.equals((short[]) o1, (short[]) o2);
			}
		}
		return false;
	}

	/**
	 * Return as hash code for the given object; typically the value of
	 * {@code Object#hashCode()}}. If the object is an array,
	 * this method will delegate to any of the {@code nullSafeHashCode}
	 * methods for arrays in this class. If the object is {@code null},
	 * this method returns 0.
	 * 
	 * <p> 返回给定对象的哈希码; 通常是Object＃hashCode（）}的值。 如果对象是数组，
	 * 则此方法将委托给此类中的数组的任何nullSafeHashCode方法。 如果对象为null，则此方法返回0。
	 * 
	 * @see #nullSafeHashCode(Object[])
	 * @see #nullSafeHashCode(boolean[])
	 * @see #nullSafeHashCode(byte[])
	 * @see #nullSafeHashCode(char[])
	 * @see #nullSafeHashCode(double[])
	 * @see #nullSafeHashCode(float[])
	 * @see #nullSafeHashCode(int[])
	 * @see #nullSafeHashCode(long[])
	 * @see #nullSafeHashCode(short[])
	 */
	public static int nullSafeHashCode(Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj.getClass().isArray()) {
			if (obj instanceof Object[]) {
				return nullSafeHashCode((Object[]) obj);
			}
			if (obj instanceof boolean[]) {
				return nullSafeHashCode((boolean[]) obj);
			}
			if (obj instanceof byte[]) {
				return nullSafeHashCode((byte[]) obj);
			}
			if (obj instanceof char[]) {
				return nullSafeHashCode((char[]) obj);
			}
			if (obj instanceof double[]) {
				return nullSafeHashCode((double[]) obj);
			}
			if (obj instanceof float[]) {
				return nullSafeHashCode((float[]) obj);
			}
			if (obj instanceof int[]) {
				return nullSafeHashCode((int[]) obj);
			}
			if (obj instanceof long[]) {
				return nullSafeHashCode((long[]) obj);
			}
			if (obj instanceof short[]) {
				return nullSafeHashCode((short[]) obj);
			}
		}
		return obj.hashCode();
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(Object[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (Object element : array) {
			hash = MULTIPLIER * hash + nullSafeHashCode(element);
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 */
	public static int nullSafeHashCode(boolean[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (boolean element : array) {
			hash = MULTIPLIER * hash + hashCode(element);
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 */
	public static int nullSafeHashCode(byte[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (byte element : array) {
			hash = MULTIPLIER * hash + element;
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(char[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (char element : array) {
			hash = MULTIPLIER * hash + element;
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(double[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (double element : array) {
			hash = MULTIPLIER * hash + hashCode(element);
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(float[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (float element : array) {
			hash = MULTIPLIER * hash + hashCode(element);
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(int[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (int element : array) {
			hash = MULTIPLIER * hash + element;
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(long[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (long element : array) {
			hash = MULTIPLIER * hash + hashCode(element);
		}
		return hash;
	}

	/**
	 * Return a hash code based on the contents of the specified array.
	 * If {@code array} is {@code null}, this method returns 0.
	 * 
	 * <p> 根据指定数组的内容返回哈希码。 如果array为null，则此方法返回0。
	 * 
	 */
	public static int nullSafeHashCode(short[] array) {
		if (array == null) {
			return 0;
		}
		int hash = INITIAL_HASH;
		for (short element : array) {
			hash = MULTIPLIER * hash + element;
		}
		return hash;
	}

	/**
	 * Return the same value as {@link Boolean#hashCode()}}.
	 * 
	 * <p> 返回与Boolean.hashCode（）}相同的值。
	 * 
	 * @see Boolean#hashCode()
	 */
	public static int hashCode(boolean bool) {
		return (bool ? 1231 : 1237);
	}

	/**
	 * Return the same value as {@link Double#hashCode()}}.
	 * 
	 * <p> 返回与Double.hashCode（）}相同的值。
	 * 
	 * @see Double#hashCode()
	 */
	public static int hashCode(double dbl) {
		return hashCode(Double.doubleToLongBits(dbl));
	}

	/**
	 * Return the same value as {@link Float#hashCode()}}.
	 * 
	 * <p> 返回与Float.hashCode（）}相同的值。
	 * 
	 * @see Float#hashCode()
	 */
	public static int hashCode(float flt) {
		return Float.floatToIntBits(flt);
	}

	/**
	 * Return the same value as {@link Long#hashCode()}}.
	 * 
	 * <p> 返回与Long.hashCode（）}相同的值。
	 * 
	 * @see Long#hashCode()
	 */
	public static int hashCode(long lng) {
		return (int) (lng ^ (lng >>> 32));
	}


	//---------------------------------------------------------------------
	// Convenience methods for toString output
	// toString输出的便捷方法
	//---------------------------------------------------------------------

	/**
	 * Return a String representation of an object's overall identity.
	 * 
	 * <p> 返回对象的整体标识的String表示形式。
	 * 
	 * @param obj the object (may be {@code null}) - 对象（可能为null）
	 * @return the object's identity as String representation,
	 * or an empty String if the object was {@code null}
	 * 
	 * <p> 对象的标识为String表示，如果对象为null，则为空String
	 * 
	 */
	public static String identityToString(Object obj) {
		if (obj == null) {
			return EMPTY_STRING;
		}
		return obj.getClass().getName() + "@" + getIdentityHexString(obj);
	}

	/**
	 * Return a hex String form of an object's identity hash code.
	 * 
	 * <p> 返回对象的标识哈希码的十六进制字符串形式。
	 * 
	 * @param obj the object - 对象
	 * @return the object's identity code in hex notation
	 * 
	 * <p> 对象的身份代码用十六进制表示法
	 * 
	 */
	public static String getIdentityHexString(Object obj) {
		return Integer.toHexString(System.identityHashCode(obj));
	}

	/**
	 * Return a content-based String representation if {@code obj} is
	 * not {@code null}; otherwise returns an empty String.
	 * 
	 * <p> 如果obj不为null，则返回基于内容的String表示; 否则返回一个空字符串。
	 * 
	 * <p>Differs from {@link #nullSafeToString(Object)} in that it returns
	 * an empty String rather than "null" for a {@code null} value.
	 * 
	 * <p> 与nullSafeToString（Object）的不同之处在于它为空值返回空字符串而不是“null”。
	 * 
	 * @param obj the object to build a display String for - 构建显示字符串的对象
	 * @return a display String representation of {@code obj} - 显示obj的字符串表示形式
	 * @see #nullSafeToString(Object)
	 */
	public static String getDisplayString(Object obj) {
		if (obj == null) {
			return EMPTY_STRING;
		}
		return nullSafeToString(obj);
	}

	/**
	 * Determine the class name for the given object.
	 * 
	 * <p> 确定给定对象的类名。
	 * 
	 * <p>Returns {@code "null"} if {@code obj} is {@code null}.
	 * 
	 * <p> 如果obj为null，则返回“null”。
	 * 
	 * @param obj the object to introspect (may be {@code null})
	 * 
	 * <p> 内省的对象（可能为null）
	 * 
	 * @return the corresponding class name - 相应的类名
	 */
	public static String nullSafeClassName(Object obj) {
		return (obj != null ? obj.getClass().getName() : NULL_STRING);
	}

	/**
	 * Return a String representation of the specified Object.
	 * 
	 * <p> 返回指定Object的String表示形式。
	 * 
	 * <p>Builds a String representation of the contents in case of an array.
	 * Returns {@code "null"} if {@code obj} is {@code null}.
	 * 
	 * <p> 在数组的情况下构建内容的String表示。 如果obj为null，则返回“null”。
	 * 
	 * @param obj the object to build a String representation for
	 * 
	 * <p> 为其构建String表示的对象
	 * 
	 * @return a String representation of {@code obj}
	 * 
	 * <p> obj的String表示形式
	 */
	public static String nullSafeToString(Object obj) {
		if (obj == null) {
			return NULL_STRING;
		}
		if (obj instanceof String) {
			return (String) obj;
		}
		if (obj instanceof Object[]) {
			return nullSafeToString((Object[]) obj);
		}
		if (obj instanceof boolean[]) {
			return nullSafeToString((boolean[]) obj);
		}
		if (obj instanceof byte[]) {
			return nullSafeToString((byte[]) obj);
		}
		if (obj instanceof char[]) {
			return nullSafeToString((char[]) obj);
		}
		if (obj instanceof double[]) {
			return nullSafeToString((double[]) obj);
		}
		if (obj instanceof float[]) {
			return nullSafeToString((float[]) obj);
		}
		if (obj instanceof int[]) {
			return nullSafeToString((int[]) obj);
		}
		if (obj instanceof long[]) {
			return nullSafeToString((long[]) obj);
		}
		if (obj instanceof short[]) {
			return nullSafeToString((short[]) obj);
		}
		String str = obj.toString();
		return (str != null ? str : EMPTY_STRING);
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for
	 * 
	 * <p> 用于构建String表示的数组
	 * 
	 * @return a String representation of {@code array}
	 * 
	 * <p> 数组的字符串表示形式
	 * 
	 */
	public static String nullSafeToString(Object[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}
			sb.append(String.valueOf(array[i]));
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p>返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for - 用于构建String表示的数组
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(boolean[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}

			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for
	 * 
	 * <p> 用于构建String表示的数组
	 * 
	 * @return a String representation of {@code array}
	 * 
	 * <p> 数组的字符串表示形式
	 * 
	 */
	public static String nullSafeToString(byte[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}
			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for - 用于构建String表示的数组
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(char[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}
			sb.append("'").append(array[i]).append("'");
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for - 用于构建String表示的数组
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(double[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}

			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for - 用于构建String表示的数组
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(float[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}

			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for
	 * 
	 * <p> 用于构建String表示的数组
	 * 
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(int[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}
			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for - 用于构建String表示的数组
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(long[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}
			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

	/**
	 * Return a String representation of the contents of the specified array.
	 * 
	 * <p> 返回指定数组内容的String表示形式。
	 * 
	 * <p>The String representation consists of a list of the array's elements,
	 * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
	 * by the characters {@code ", "} (a comma followed by a space). Returns
	 * {@code "null"} if {@code array} is {@code null}.
	 * 
	 * <p> String表示由数组元素的列表组成，用大括号（“{}”}）括起来。 相邻元素由字符“，”（逗号后跟空格）分隔。 
	 * 如果array为null，则返回“null”。
	 * 
	 * @param array the array to build a String representation for - 用于构建String表示的数组
	 * @return a String representation of {@code array} - 数组的字符串表示形式
	 */
	public static String nullSafeToString(short[] array) {
		if (array == null) {
			return NULL_STRING;
		}
		int length = array.length;
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(ARRAY_START);
			}
			else {
				sb.append(ARRAY_ELEMENT_SEPARATOR);
			}
			sb.append(array[i]);
		}
		sb.append(ARRAY_END);
		return sb.toString();
	}

}
