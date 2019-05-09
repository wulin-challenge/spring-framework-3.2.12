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

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Miscellaneous class utility methods.
 * Mainly for internal use within the framework.
 * 
 * <p>杂项类实用程序方法。 主要供框架内部使用。
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 1.1
 * @see TypeUtils
 * @see ReflectionUtils
 */
public abstract class ClassUtils {

	/** Suffix for array class names: "[]" */
	/** 数组类名称的后缀: "[]" */
	public static final String ARRAY_SUFFIX = "[]";

	/** Prefix for internal array class names: "[" */
	/** 内部数组类名的前缀: "[" */
	private static final String INTERNAL_ARRAY_PREFIX = "[";

	/** Prefix for internal non-primitive array class names: "[L" */
	/** 内部非基本数组类名的前缀: "[L" */
	private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

	/** The package separator character '.' */
	/** 包分隔符 '.' */
	private static final char PACKAGE_SEPARATOR = '.';

	/** The inner class separator character '$' */
	/** 内部类分隔符 '$' */
	private static final char INNER_CLASS_SEPARATOR = '$';

	/** The CGLIB class separator character "$$" */
	/** CGLIB类分隔符 "$$" */
	public static final String CGLIB_CLASS_SEPARATOR = "$$";

	/** The ".class" file suffix */
	/** “.class”文件后缀 */
	public static final String CLASS_FILE_SUFFIX = ".class";


	/**
	 * Map with primitive wrapper type as key and corresponding primitive
	 * type as value, for example: Integer.class -> int.class.
	 * 
	 * <p>使用基本包装类型作为键映射，并将相应的基元类型作为值映射，例如：Integer.class - > int.class。
	 * 
	 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);

	/**
	 * Map with primitive type as key and corresponding wrapper
	 * type as value, for example: int.class -> Integer.class.
	 * 
	 * <p>将原始类型映射为键，将相应的包装类型映射为值，例如：int.class - > Integer.class。
	 * 
	 */
	private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>, Class<?>>(8);

	/**
	 * Map with primitive type name as key and corresponding primitive
	 * type as value, for example: "int" -> "int.class".
	 * 
	 * <p>将原始类型名称映射为键，将相应的原始类型映射为值，例如：“int” - >“int.class”。
	 * 
	 */
	private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(32);

	/**
	 * Map with common "java.lang" class name as key and corresponding Class as value.
	 * 
	 * <p>将常用的“java.lang”类名称作为键映射，并将相应的Class作为值映射。
	 * 
	 * Primarily for efficient deserialization of remote invocations.
	 * 
	 * <p>主要用于远程调用的有效反序列化。
	 * 
	 */
	private static final Map<String, Class<?>> commonClassCache = new HashMap<String, Class<?>>(32);


	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);

		for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
			primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
			registerCommonClasses(entry.getKey());
		}

		Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(32);
		primitiveTypes.addAll(primitiveWrapperTypeMap.values());
		primitiveTypes.addAll(Arrays.asList(new Class<?>[] {
				boolean[].class, byte[].class, char[].class, double[].class,
				float[].class, int[].class, long[].class, short[].class}));
		primitiveTypes.add(void.class);
		for (Class<?> primitiveType : primitiveTypes) {
			primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
		}

		registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
				Float[].class, Integer[].class, Long[].class, Short[].class);
		registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
				Object.class, Object[].class, Class.class, Class[].class);
		registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
				Error.class, StackTraceElement.class, StackTraceElement[].class);
	}


	/**
	 * Register the given common classes with the ClassUtils cache.
	 * 
	 * <p>使用ClassUtils缓存注册给定的公共类。
	 * 
	 */
	private static void registerCommonClasses(Class<?>... commonClasses) {
		for (Class<?> clazz : commonClasses) {
			commonClassCache.put(clazz.getName(), clazz);
		}
	}

	/**
	 * Return the default ClassLoader to use: typically the thread context
	 * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
	 * class will be used as fallback.
	 * 
	 * <p>返回使用的默认ClassLoader：通常是线程上下文ClassLoader（如果可用）; 加载ClassUtils类的ClassLoader将用作回退。
	 * 
	 * <p>Call this method if you intend to use the thread context ClassLoader
	 * in a scenario where you clearly prefer a non-null ClassLoader reference:
	 * for example, for class path resource loading (but not necessarily for
	 * {@code Class.forName}, which accepts a {@code null} ClassLoader
	 * reference as well).
	 * 
	 * <p>如果您打算在明确偏好非空ClassLoader引用的场景中使用线程上下文ClassLoader，请调用此方法：例如，对于类路
	 * 径资源加载（但不一定是Class.forName，它接受null ClassLoader引用为 好）。
	 * 
	 * @return the default ClassLoader (only {@code null} if even the system
	 * ClassLoader isn't accessible)
	 * 
	 * <p>默认的ClassLoader（即使系统ClassLoader不可访问也只为null）
	 * 
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back...
			// 无法访问线程上下文ClassLoader - 退回...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			// 没有线程上下文类加载器 - >使用此类的类加载器。
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				//getClassLoader（）返回null表示引导程序ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ex) {
					// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
					// 无法访问系统ClassLoader - 哦，也许调用者可以使用null ...
				}
			}
		}
		return cl;
	}

	/**
	 * Override the thread context ClassLoader with the environment's bean ClassLoader
	 * if necessary, i.e. if the bean ClassLoader is not equivalent to the thread
	 * context ClassLoader already.
	 * 
	 * <p>如果需要，用环境的bean ClassLoader覆盖线程上下文ClassLoader，即如果bean ClassLoader已经不等同于线程上下文ClassLoader。
	 * 
	 * @param classLoaderToUse the actual ClassLoader to use for the thread context
	 * 
	 * <p>用于线程上下文的实际ClassLoader
	 * 
	 * @return the original thread context ClassLoader, or {@code null} if not overridden
	 * 
	 * <p>原始线程上下文ClassLoader，如果不重写则为null
	 */
	public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
		Thread currentThread = Thread.currentThread();
		ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
		if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
			currentThread.setContextClassLoader(classLoaderToUse);
			return threadContextClassLoader;
		}
		else {
			return null;
		}
	}

	/**
	 * Replacement for {@code Class.forName()} that also returns Class instances
	 * for primitives (like "int") and array class names (like "String[]").
	 * 
	 * <p> Class.forName（）的替换，它还返回基元（如“int”）和数组类名称（如“String []”）的Class实例。
	 * 
	 * <p>Always uses the default class loader: that is, preferably the thread context
	 * class loader, or the ClassLoader that loaded the ClassUtils class as fallback.
	 * 
	 * <p> 始终使用默认的类加载器：即，最好是线程上下文类加载器，或者加载ClassUtils类作为回退的ClassLoader。
	 * 
	 * @param name the name of the Class - 类的名称
	 * @return Class instance for the supplied name - 提供的名称的类实例
	 * @throws ClassNotFoundException if the class was not found - 如果没找到类
	 * @throws LinkageError if the class file could not be loaded - 如果无法加载类文件
	 * @see Class#forName(String, boolean, ClassLoader)
	 * @see #getDefaultClassLoader()
	 * @deprecated as of Spring 3.0, in favor of specifying a ClassLoader explicitly:
	 * see {@link #forName(String, ClassLoader)}
	 * 
	 * <p> 从Spring 3.0开始，支持显式指定ClassLoader：请参阅forName（String，ClassLoader）
	 */
	@Deprecated
	public static Class<?> forName(String name) throws ClassNotFoundException, LinkageError {
		return forName(name, getDefaultClassLoader());
	}

	/**
	 * Replacement for {@code Class.forName()} that also returns Class instances
	 * for primitives (e.g. "int") and array class names (e.g. "String[]").
	 * Furthermore, it is also capable of resolving inner class names in Java source
	 * style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
	 * 
	 * <p> Class.forName（）的替换，它还返回基元（例如“int”）和数组类名称（例如“String []”）的Class实例。 
	 * 此外，它还能够以Java源代码样式解析内部类名（例如“java.lang.Thread.State”而不是“java.lang.Thread $ State”）。
	 * 
	 * @param name the name of the Class - 类的名称
	 * @param classLoader the class loader to use
	 * (may be {@code null}, which indicates the default class loader)
	 * 
	 * <p> 要使用的类加载器（可以为null，表示默认的类加载器）
	 * 
	 * @return Class instance for the supplied name - 提供的名称的类实例
	 * @throws ClassNotFoundException if the class was not found - 如果没找到类
	 * @throws LinkageError if the class file could not be loaded - 如果无法加载类文件
	 * @see Class#forName(String, boolean, ClassLoader)
	 */
	public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
		Assert.notNull(name, "Name must not be null");

		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz == null) {
			clazz = commonClassCache.get(name);
		}
		if (clazz != null) {
			return clazz;
		}

		// "java.lang.String[]" style arrays
		if (name.endsWith(ARRAY_SUFFIX)) {
			String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
			Class<?> elementClass = forName(elementClassName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[Ljava.lang.String;" style arrays
		if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
			String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[[I" or "[[Ljava.lang.String;" style arrays
		if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
			String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		ClassLoader clToUse = classLoader;
		if (clToUse == null) {
			clToUse = getDefaultClassLoader();
		}
		try {
			return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
		}
		catch (ClassNotFoundException ex) {
			int lastDotIndex = name.lastIndexOf('.');
			if (lastDotIndex != -1) {
				String innerClassName = name.substring(0, lastDotIndex) + '$' + name.substring(lastDotIndex + 1);
				try {
					return (clToUse != null ? clToUse.loadClass(innerClassName) : Class.forName(innerClassName));
				}
				catch (ClassNotFoundException ex2) {
					// swallow - let original exception get through
				}
			}
			throw ex;
		}
	}

	/**
	 * Resolve the given class name into a Class instance. Supports
	 * primitives (like "int") and array class names (like "String[]").
	 * 
	 * <p> 将给定的类名解析为Class实例。 支持基元（如“int”）和数组类名（如“String []”）。
	 * 
	 * <p>This is effectively equivalent to the {@code forName}
	 * method with the same arguments, with the only difference being
	 * the exceptions thrown in case of class loading failure.
	 * 
	 * <p> 这实际上等效于具有相同参数的forName方法，唯一的区别是类加载失败时抛出的异常。
	 * 
	 * @param className the name of the Class - 类的名称
	 * @param classLoader the class loader to use
	 * (may be {@code null}, which indicates the default class loader)
	 * 
	 * <p> 要使用的类加载器（可以为null，表示默认的类加载器）
	 * 
	 * @return Class instance for the supplied name - 提供的名称的类实例
	 * @throws IllegalArgumentException if the class name was not resolvable
	 * (that is, the class could not be found or the class file could not be loaded)
	 * 
	 * <p>如果类名不可解析（即无法找到类或无法加载类文件）
	 * 
	 * @see #forName(String, ClassLoader)
	 */
	public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {
		try {
			return forName(className, classLoader);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Cannot find class [" + className + "]", ex);
		}
		catch (LinkageError ex) {
			throw new IllegalArgumentException(
					"Error loading class [" + className + "]: problem with class file or dependent class.", ex);
		}
	}

	/**
	 * Resolve the given class name as primitive class, if appropriate,
	 * according to the JVM's naming rules for primitive classes.
	 * 
	 * <p> 根据JVM的基本类命名规则，将给定的类名解析为基本类（如果适用）。
	 * 
	 * <p>Also supports the JVM's internal class names for primitive arrays.
	 * Does <i>not</i> support the "[]" suffix notation for primitive arrays;
	 * this is only supported by {@link #forName(String, ClassLoader)}.
	 * 
	 * <p> 还支持JVM的原始数组的内部类名。 不支持原始数组的“[]”后缀表示法; 
	 * 这仅由forName（String，ClassLoader）支持。
	 * 
	 * @param name the name of the potentially primitive class - 潜在原始类的名称
	 * @return the primitive class, or {@code null} if the name does not denote
	 * a primitive class or primitive array class
	 * 
	 * <p> 原始类，如果名称不表示基本类或基本数组类，则返回null
	 * 
	 */
	public static Class<?> resolvePrimitiveClassName(String name) {
		Class<?> result = null;
		// Most class names will be quite long, considering that they
		// SHOULD sit in a package, so a length check is worthwhile.
		
		// 考虑到他们应该坐在一个包裹中，大多数班级名称都会很长，所以进行长度检查是值得的。
		if (name != null && name.length() <= 8) {
			// Could be a primitive - likely.
			// 可能是原始的 - 可能。
			result = primitiveTypeNameMap.get(name);
		}
		return result;
	}

	/**
	 * Determine whether the {@link Class} identified by the supplied name is present
	 * and can be loaded. Will return {@code false} if either the class or
	 * one of its dependencies is not present or cannot be loaded.
	 * 
	 * <p> 确定由提供的名称标识的类是否存在且可以加载。 如果类或其中一个依赖项不存在或无法加载，则返回false。
	 * 
	 * @param className the name of the class to check - 要检查的类的名称
	 * @return whether the specified class is present - 是否存在指定的类
	 * @deprecated as of Spring 2.5, in favor of {@link #isPresent(String, ClassLoader)}
	 * 
	 * <p> 从Spring 2.5开始，支持isPresent（String，ClassLoader）
	 * 
	 */
	@Deprecated
	public static boolean isPresent(String className) {
		return isPresent(className, getDefaultClassLoader());
	}

	/**
	 * Determine whether the {@link Class} identified by the supplied name is present
	 * and can be loaded. Will return {@code false} if either the class or
	 * one of its dependencies is not present or cannot be loaded.
	 * 
	 * <p> 确定由提供的名称标识的类是否存在且可以加载。 如果类或其中一个依赖项不存在或无法加载，则返回false。
	 * 
	 * @param className the name of the class to check - 要检查的类的名称
	 * @param classLoader the class loader to use
	 * (may be {@code null}, which indicates the default class loader)
	 * 
	 * <p> 要使用的类加载器（可以为null，表示默认的类加载器）
	 * 
	 * @return whether the specified class is present
	 * 
	 * <p> 是否存在指定的类
	 * 
	 */
	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			forName(className, classLoader);
			return true;
		}
		catch (Throwable ex) {
			// Class or one of its dependencies is not present...
			// 类或其中一个依赖项不存在...
			return false;
		}
	}

	/**
	 * Return the user-defined class for the given instance: usually simply
	 * the class of the given instance, but the original class in case of a
	 * CGLIB-generated subclass.
	 * 
	 * <p> 返回给定实例的用户定义类：通常只是给定实例的类，但是在CGLIB生成的子类的情况下是原始类。
	 * 
	 * @param instance the instance to check - 要检查的实例
	 * @return the user-defined class - 用户定义的类
	 */
	public static Class<?> getUserClass(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getUserClass(instance.getClass());
	}

	/**
	 * Return the user-defined class for the given class: usually simply the given
	 * class, but the original class in case of a CGLIB-generated subclass.
	 * 
	 * <p> 返回给定类的用户定义类：通常只是给定的类，但是在CGLIB生成的子类的情况下返回原始类。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @return the user-defined class - 用户定义的类
	 */
	public static Class<?> getUserClass(Class<?> clazz) {
		if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	/**
	 * Check whether the given class is cache-safe in the given context,
	 * i.e. whether it is loaded by the given ClassLoader or a parent of it.
	 * 
	 * <p> 检查给定类在给定上下文中是否是高速缓存安全的，即它是由给定的ClassLoader还是由其父类加载。
	 * 
	 * @param clazz the class to analyze - 要分析的类
	 * @param classLoader the ClassLoader to potentially cache metadata in - ClassLoader可能会缓存元数据
	 */
	public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");
		try {
			ClassLoader target = clazz.getClassLoader();
			if (target == null) {
				return true;
			}
			ClassLoader cur = classLoader;
			if (cur == target) {
				return true;
			}
			while (cur != null) {
				cur = cur.getParent();
				if (cur == target) {
					return true;
				}
			}
			return false;
		}
		catch (SecurityException ex) {
			// Probably from the system ClassLoader - let's consider it safe.
			// 可能来自系统ClassLoader - 让我们认为它是安全的。
			return true;
		}
	}


	/**
	 * Get the class name without the qualified package name.
	 * 
	 * <p> 获取没有限定包名的类名。
	 * 
	 * @param className the className to get the short name for
	 * 
	 * <p> className获取短名称
	 * 
	 * @return the class name of the class without the package name - 没有包名的类的类名
	 * @throws IllegalArgumentException if the className is empty - 如果className为空
	 */
	public static String getShortName(String className) {
		Assert.hasLength(className, "Class name must not be empty");
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
		if (nameEndIndex == -1) {
			nameEndIndex = className.length();
		}
		String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
		shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
		return shortName;
	}

	/**
	 * Get the class name without the qualified package name. - 获取没有限定包名的类名。
	 * @param clazz the class to get the short name for - 获取短名称的类
	 * @return the class name of the class without the package name - 没有包名的类的类名
	 */
	public static String getShortName(Class<?> clazz) {
		return getShortName(getQualifiedName(clazz));
	}

	/**
	 * Return the short string name of a Java class in uncapitalized JavaBeans
	 * property format. Strips the outer class name in case of an inner class.
	 * 
	 * <p> 以非大写JavaBeans属性格式返回Java类的短字符串名称。 在内部类的情况下剥离外部类名。
	 * 
	 * @param clazz the class - 类
	 * @return the short name rendered in a standard JavaBeans property format
	 * 
	 * <p> 以标准JavaBeans属性格式呈现的短名称
	 * 
	 * @see java.beans.Introspector#decapitalize(String)
	 */
	public static String getShortNameAsProperty(Class<?> clazz) {
		String shortName = ClassUtils.getShortName(clazz);
		int dotIndex = shortName.lastIndexOf('.');
		shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName);
		return Introspector.decapitalize(shortName);
	}

	/**
	 * Determine the name of the class file, relative to the containing
	 * package: e.g. "String.class"
	 * 
	 * <p> 确定类文件的名称，相对于包含的包：例如“String.class”
	 * 
	 * @param clazz the class - 类
	 * @return the file name of the ".class" file - “.class”文件的文件名
	 */
	public static String getClassFileName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
	}

	/**
	 * Determine the name of the package of the given class,
	 * e.g. "java.lang" for the {@code java.lang.String} class.
	 * 
	 * <p> 确定给定类的包的名称，例如 java.lang.String类的“java.lang”。
	 * 
	 * @param clazz the class - 类
	 * @return the package name, or the empty String if the class
	 * is defined in the default package
	 * 
	 * <p> 包名称，如果在默认包中定义了类，则为空String
	 * 
	 */
	public static String getPackageName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return getPackageName(clazz.getName());
	}

	/**
	 * Determine the name of the package of the given fully-qualified class name,
	 * e.g. "java.lang" for the {@code java.lang.String} class name.
	 * 
	 * <p> 确定给定的完全限定类名的包的名称，例如 java.lang.String类名的“java.lang”。
	 * 
	 * @param fqClassName the fully-qualified class name - 完全限定的类名
	 * @return the package name, or the empty String if the class
	 * is defined in the default package
	 * 
	 * <p> 包名称，如果在默认包中定义了类，则为空String
	 * 
	 */
	public static String getPackageName(String fqClassName) {
		Assert.notNull(fqClassName, "Class name must not be null");
		int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
		return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
	}

	/**
	 * Return the qualified name of the given class: usually simply
	 * the class name, but component type class name + "[]" for arrays.
	 * 
	 * <p> 返回给定类的限定名称：通常只是类名，但组件类型类名+“[]”表示数组。
	 * 
	 * @param clazz the class - 类
	 * @return the qualified name of the class - 该类的限定名称
	 */
	public static String getQualifiedName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		}
		else {
			return clazz.getName();
		}
	}

	/**
	 * Build a nice qualified name for an array:
	 * component type class name + "[]".
	 * 
	 * <p> 为数组构建一个很好的限定名：组件类型类名+“[]”。
	 * 
	 * @param clazz the array class - 数组类
	 * @return a qualified name for the array class - 数组类的限定名称
	 */
	private static String getQualifiedNameForArray(Class<?> clazz) {
		StringBuilder result = new StringBuilder();
		while (clazz.isArray()) {
			clazz = clazz.getComponentType();
			result.append(ClassUtils.ARRAY_SUFFIX);
		}
		result.insert(0, clazz.getName());
		return result.toString();
	}

	/**
	 * Return the qualified name of the given method, consisting of
	 * fully qualified interface/class name + "." + method name.
	 * 
	 * <p> 返回给定方法的限定名称，由完全限定的接口/类名+“。”组成。 +方法名称。
	 * 
	 * @param method the method - 方法
	 * @return the qualified name of the method - 方法的限定名称
	 */
	public static String getQualifiedMethodName(Method method) {
		Assert.notNull(method, "Method must not be null");
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	/**
	 * Return a descriptive name for the given object's type: usually simply
	 * the class name, but component type class name + "[]" for arrays,
	 * and an appended list of implemented interfaces for JDK proxies.
	 * 
	 * <p> 返回给定对象类型的描述性名称：通常只是类名，但是数组的组件类型类名+“[]”，以及JDK代理的已实现接口的附加列表。
	 * 
	 * @param value the value to introspect - 内省的值
	 * @return the qualified name of the class - 该类的限定名称
	 */
	public static String getDescriptiveType(Object value) {
		if (value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if (Proxy.isProxyClass(clazz)) {
			StringBuilder result = new StringBuilder(clazz.getName());
			result.append(" implementing ");
			Class<?>[] ifcs = clazz.getInterfaces();
			for (int i = 0; i < ifcs.length; i++) {
				result.append(ifcs[i].getName());
				if (i < ifcs.length - 1) {
					result.append(',');
				}
			}
			return result.toString();
		}
		else if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		}
		else {
			return clazz.getName();
		}
	}

	/**
	 * Check whether the given class matches the user-specified type name.
	 * 
	 * <p> 检查给定的类是否与用户指定的类型名称匹配。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @param typeName the type name to match - 要匹配的类型名称
	 */
	public static boolean matchesTypeName(Class<?> clazz, String typeName) {
		return (typeName != null &&
				(typeName.equals(clazz.getName()) || typeName.equals(clazz.getSimpleName()) ||
				(clazz.isArray() && typeName.equals(getQualifiedNameForArray(clazz)))));
	}


	/**
	 * Determine whether the given class has a public constructor with the given signature.
	 * 
	 * <p> 确定给定的类是否具有具有给定签名的公共构造函数。
	 * 
	 * <p>Essentially translates {@code NoSuchMethodException} to "false".
	 * 
	 * <p> 基本上将NoSuchMethodException转换为“false”。
	 * 
	 * @param clazz the clazz to analyze - 要分析的clazz
	 * @param paramTypes the parameter types of the method - 方法的参数类型
	 * @return whether the class has a corresponding constructor
	 * 
	 * <p> 该类是否具有相应的构造函数
	 * 
	 * @see Class#getMethod
	 */
	public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
		return (getConstructorIfAvailable(clazz, paramTypes) != null);
	}

	/**
	 * Determine whether the given class has a public constructor with the given signature,
	 * and return it if available (else return {@code null}).
	 * 
	 * <p> 确定给定的类是否具有给定签名的公共构造函数，并在可用时返回它（否则返回null）。
	 * 
	 * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
	 * 
	 * <p> 基本上将NoSuchMethodException转换为null。
	 * 
	 * @param clazz the clazz to analyze - 要分析的clazz
	 * @param paramTypes the parameter types of the method - 方法的参数类型
	 * @return the constructor, or {@code null} if not found - 构造函数，如果未找到则为null
	 * @see Class#getConstructor
	 */
	public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		try {
			return clazz.getConstructor(paramTypes);
		}
		catch (NoSuchMethodException ex) {
			return null;
		}
	}

	/**
	 * Determine whether the given class has a public method with the given signature.
	 * 
	 * <p> 确定给定的类是否具有给定签名的公共方法。
	 * 
	 * <p>Essentially translates {@code NoSuchMethodException} to "false".
	 * 
	 * <p> 基本上将NoSuchMethodException转换为“false”。
	 * 
	 * @param clazz the clazz to analyze - 要分析的clazz
	 * @param methodName the name of the method - 方法的名称
	 * @param paramTypes the parameter types of the method - 方法的参数类型
	 * @return whether the class has a corresponding method - 该类是否有相应的方法
	 * @see Class#getMethod
	 */
	public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
	}

	/**
	 * Determine whether the given class has a public method with the given signature,
	 * and return it if available (else throws an {@code IllegalStateException}).
	 * 
	 * <p> 确定给定的类是否具有给定签名的公共方法，并在可用时返回它（否则抛出IllegalStateException）。
	 * 
	 * <p>In case of any signature specified, only returns the method if there is a
	 * unique candidate, i.e. a single public method with the specified name.
	 * 
	 * <p> 如果指定了任何签名，则仅在存在唯一候选者（即具有指定名称的单个公共方法）时才返回该方法。
	 * 
	 * <p>Essentially translates {@code NoSuchMethodException} to {@code IllegalStateException}.
	 * 
	 * <p> 基本上将NoSuchMethodException转换为IllegalStateException。
	 * 
	 * @param clazz the clazz to analyze - 要分析的clazz
	 * @param methodName the name of the method - 方法的名称
	 * @param paramTypes the parameter types of the method
	 * (may be {@code null} to indicate any signature)
	 * 
	 * <p> 方法的参数类型（可以为null以指示任何签名）
	 * 
	 * @return the method (never {@code null}) - 方法（永不为null）
	 * @throws IllegalStateException if the method has not been found - 如果没有找到该方法
	 * @see Class#getMethod
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		if (paramTypes != null) {
			try {
				return clazz.getMethod(methodName, paramTypes);
			}
			catch (NoSuchMethodException ex) {
				throw new IllegalStateException("Expected method not found: " + ex);
			}
		}
		else {
			Set<Method> candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (methodName.equals(method.getName())) {
					candidates.add(method);
				}
			}
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			}
			else if (candidates.isEmpty()) {
				throw new IllegalStateException("Expected method not found: " + clazz + "." + methodName);
			}
			else {
				throw new IllegalStateException("No unique method found: " + clazz + "." + methodName);
			}
		}
	}

	/**
	 * Determine whether the given class has a public method with the given signature,
	 * and return it if available (else return {@code null}).
	 * 
	 * <p> 确定给定的类是否具有给定签名的公共方法，并在可用时返回它（否则返回null）。
	 * 
	 * <p>In case of any signature specified, only returns the method if there is a
	 * unique candidate, i.e. a single public method with the specified name.
	 * 
	 * <p> 如果指定了任何签名，则仅在存在唯一候选者（即具有指定名称的单个公共方法）时才返回该方法。
	 * 
	 * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
	 * 
	 * <p> 基本上将NoSuchMethodException转换为null。
	 * 
	 * @param clazz the clazz to analyze - 要分析的clazz
	 * @param methodName the name of the method - 方法的名称
	 * @param paramTypes the parameter types of the method
	 * (may be {@code null} to indicate any signature)
	 * 
	 * <p> 方法的参数类型（可以为null以指示任何签名）
	 * 
	 * @return the method, or {@code null} if not found - 方法，如果没有找到null
	 * @see Class#getMethod
	 */
	public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		if (paramTypes != null) {
			try {
				return clazz.getMethod(methodName, paramTypes);
			}
			catch (NoSuchMethodException ex) {
				return null;
			}
		}
		else {
			Set<Method> candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (methodName.equals(method.getName())) {
					candidates.add(method);
				}
			}
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			}
			return null;
		}
	}

	/**
	 * Return the number of methods with a given name (with any argument types),
	 * for the given class and/or its superclasses. Includes non-public methods.
	 * 
	 * <p> 对于给定的类和/或其超类，返回具有给定名称（具有任何参数类型）的方法的数量。 包括非公开方法。
	 * 
	 * @param clazz	the clazz to check - 要检查的clazz
	 * @param methodName the name of the method - 方法的名称
	 * @return the number of methods with the given name - 具有给定名称的方法的数量
	 */
	public static int getMethodCountForName(Class<?> clazz, String methodName) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		int count = 0;
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (methodName.equals(method.getName())) {
				count++;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			count += getMethodCountForName(ifc, methodName);
		}
		if (clazz.getSuperclass() != null) {
			count += getMethodCountForName(clazz.getSuperclass(), methodName);
		}
		return count;
	}

	/**
	 * Does the given class or one of its superclasses at least have one or more
	 * methods with the supplied name (with any argument types)?
	 * Includes non-public methods.
	 * 
	 * <p> 给定的类或其中一个超类是否至少具有一个或多个具有所提供名称的方法（具有任何参数类型）？ 包括非公开方法。
	 * 
	 * @param clazz	the clazz to check - 要检查的clazz
	 * @param methodName the name of the method - 方法的名称
	 * @return whether there is at least one method with the given name
	 * 
	 * <p> 是否至少有一个具有给定名称的方法
	 * 
	 */
	public static boolean hasAtLeastOneMethodWithName(Class<?> clazz, String methodName) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.getName().equals(methodName)) {
				return true;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			if (hasAtLeastOneMethodWithName(ifc, methodName)) {
				return true;
			}
		}
		return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(clazz.getSuperclass(), methodName));
	}

	/**
	 * Given a method, which may come from an interface, and a target class used
	 * in the current reflective invocation, find the corresponding target method
	 * if there is one. E.g. the method may be {@code IFoo.bar()} and the
	 * target class may be {@code DefaultFoo}. In this case, the method may be
	 * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.
	 * 
	 * <p> 给定一个可能来自接口的方法，以及当前反射调用中使用的目标类，找到相应的目标方法（如果有）。 例如。 
	 * 方法可以是IFoo.bar（），目标类可以是DefaultFoo。 在这种情况下，
	 * 该方法可以是DefaultFoo.bar（）。 这样可以找到该方法的属性。
	 * 
	 * <p><b>NOTE:</b> In contrast to {@link org.springframework.aop.support.AopUtils#getMostSpecificMethod},
	 * this method does <i>not</i> resolve Java 5 bridge methods automatically.
	 * Call {@link org.springframework.core.BridgeMethodResolver#findBridgedMethod}
	 * if bridge method resolution is desirable (e.g. for obtaining metadata from
	 * the original method definition).
	 * 
	 * <p> 注意：与org.springframework.aop.support.AopUtils.getMostSpecificMethod相比，
	 * 此方法不会自动解析Java 5桥接方法。 如果需要桥接方法解析，
	 * 则调用org.springframework.core.BridgeMethodResolver.findBridgedMethod（例如，用
	 * 于从原始方法定义获取元数据）。
	 * 
	 * <p><b>NOTE:</b> Since Spring 3.1.1, if Java security settings disallow reflective
	 * access (e.g. calls to {@code Class#getDeclaredMethods} etc, this implementation
	 * will fall back to returning the originally provided method.
	 * 
	 * <p> 注意：从Spring 3.1.1开始，如果Java安全设置不允许反射访问（例如调
	 * 用Class＃getDeclaredMethods等，则此实现将回退到返回最初提供的方法。
	 * 
	 * @param method the method to be invoked, which may come from an interface
	 * 
	 * <p> 要调用的方法，可能来自接口
	 * 
	 * @param targetClass the target class for the current invocation.
	 * May be {@code null} or may not even implement the method.
	 * 
	 * <p> 当前调用的目标类。 可能为null或甚至可能不实现该方法。
	 * 
	 * @return the specific target method, or the original method if the
	 * {@code targetClass} doesn't implement it or is {@code null}
	 * 
	 * <p> 特定目标方法，或者如果targetClass没有实现它或者为null，则为原始方法
	 * 
	 */
	public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
		if (method != null && isOverridable(method, targetClass) &&
				targetClass != null && !targetClass.equals(method.getDeclaringClass())) {
			try {
				if (Modifier.isPublic(method.getModifiers())) {
					try {
						return targetClass.getMethod(method.getName(), method.getParameterTypes());
					}
					catch (NoSuchMethodException ex) {
						return method;
					}
				}
				else {
					Method specificMethod =
							ReflectionUtils.findMethod(targetClass, method.getName(), method.getParameterTypes());
					return (specificMethod != null ? specificMethod : method);
				}
			}
			catch (SecurityException ex) {
				// Security settings are disallowing reflective access; fall back to 'method' below.
				// 安全设置禁止反射访问; 回到下面的'方法'。
			}
		}
		return method;
	}

	/**
	 * Determine whether the given method is overridable in the given target class.
	 * 
	 * <p> 确定给定方法在给定目标类中是否可覆盖。
	 * 
	 * @param method the method to check - 要检查的方法
	 * @param targetClass the target class to check against - 要检查的目标类
	 */
	private static boolean isOverridable(Method method, Class<?> targetClass) {
		if (Modifier.isPrivate(method.getModifiers())) {
			return false;
		}
		if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
			return true;
		}
		return getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass));
	}

	/**
	 * Return a public static method of a class.
	 * 
	 * <p> 返回类的公共静态方法。
	 * 
	 * @param methodName the static method name - 静态方法名称
	 * @param clazz the class which defines the method - 定义方法的类
	 * @param args the parameter types to the method - 方法的参数类型
	 * @return the static method, or {@code null} if no static method was found
	 * 
	 * <p> 静态方法，如果没有找到静态方法，则返回null
	 * 
	 * @throws IllegalArgumentException if the method name is blank or the clazz is null
	 * 
	 * <p> 如果方法名称为空或clazz为null
	 * 
	 */
	public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		try {
			Method method = clazz.getMethod(methodName, args);
			return Modifier.isStatic(method.getModifiers()) ? method : null;
		}
		catch (NoSuchMethodException ex) {
			return null;
		}
	}


	/**
	 * Check if the given class represents a primitive wrapper,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 * 
	 * <p> 检查给定的类是否表示基本包装器，即布尔值，字节，字符，短整数，整数，长整数，浮点数或双精度。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @return whether the given class is a primitive wrapper class
	 * 
	 * <p> 给定的类是否是原始包装类
	 * 
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return primitiveWrapperTypeMap.containsKey(clazz);
	}

	/**
	 * Check if the given class represents a primitive (i.e. boolean, byte,
	 * char, short, int, long, float, or double) or a primitive wrapper
	 * (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
	 * 
	 * <p> 检查给定的类是表示基元（即boolean，byte，char，short，int，long，float还是double）或
	 * 原始包装器（即Boolean，Byte，Character，Short，Integer，Long，Float或Double）。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @return whether the given class is a primitive or primitive wrapper class
	 * 
	 * <p> 给定的类是原始包装类还是原始包装类
	 * 
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}

	/**
	 * Check if the given class represents an array of primitives,
	 * i.e. boolean, byte, char, short, int, long, float, or double.
	 * 
	 * <p> 检查给定的类是否表示基元数组，即boolean，byte，char，short，int，long，float或double。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @return whether the given class is a primitive array class
	 * 
	 * <p> 给定的类是否是原始数组类
	 * 
	 */
	public static boolean isPrimitiveArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isArray() && clazz.getComponentType().isPrimitive());
	}

	/**
	 * Check if the given class represents an array of primitive wrappers,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 * 
	 * <p> 检查给定的类是否表示原始包装器的数组，即Boolean，Byte，Character，Short，Integer，Long，Float或Double。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @return whether the given class is a primitive wrapper array class
	 * 
	 * <p> 给定的类是否是原始的包装器数组类
	 * 
	 */
	public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
	}

	/**
	 * Resolve the given class if it is a primitive class,
	 * returning the corresponding primitive wrapper type instead.
	 * 
	 * <p> 如果它是基本类，则解析给定的类，而是返回相应的基元包装类型。
	 * 
	 * @param clazz the class to check - 要检查的类
	 * @return the original class, or a primitive wrapper for the original primitive type
	 * 
	 * <p> 原始类，或原始类型的原始包装器
	 * 
	 */
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() && clazz != void.class? primitiveTypeToWrapperMap.get(clazz) : clazz);
	}

	/**
	 * Check if the right-hand side type may be assigned to the left-hand side
	 * type, assuming setting by reflection. Considers primitive wrapper
	 * classes as assignable to the corresponding primitive types.
	 * 
	 * <p> 假设通过反射进行设置，检查右侧类型是否可以指定为左侧类型。 将原始包装类视为可分配给相应的基元类型。
	 * 
	 * @param lhsType the target type - 目标类型
	 * @param rhsType the value type that should be assigned to the target type
	 * 
	 * <p> 应分配给目标类型的值类型
	 * 
	 * @return if the target type is assignable from the value type
	 * 
	 * <p> 如果目标类型可从值类型中分配
	 * 
	 * @see TypeUtils#isAssignable
	 */
	public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
		Assert.notNull(lhsType, "Left-hand side type must not be null");
		Assert.notNull(rhsType, "Right-hand side type must not be null");
		if (lhsType.isAssignableFrom(rhsType)) {
			return true;
		}
		if (lhsType.isPrimitive()) {
			Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
			if (resolvedPrimitive != null && lhsType.equals(resolvedPrimitive)) {
				return true;
			}
		}
		else {
			Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
			if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if the given type is assignable from the given value,
	 * assuming setting by reflection. Considers primitive wrapper classes
	 * as assignable to the corresponding primitive types.
	 * 
	 * <p> 假定通过反射设置，确定给定类型是否可从给定值分配。 将原始包装类视为可分配给相应的基元类型。
	 * 
	 * @param type the target type - 目标类型
	 * @param value the value that should be assigned to the type - 应分配给该类型的值
	 * @return if the type is assignable from the value - 如果类型可从值中分配
	 */
	public static boolean isAssignableValue(Class<?> type, Object value) {
		Assert.notNull(type, "Type must not be null");
		return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
	}


	/**
	 * Convert a "/"-based resource path to a "."-based fully qualified class name.
	 * 
	 * <p> 将基于“/”的资源路径转换为基于“.”的完全限定类名。
	 * 
	 * @param resourcePath the resource path pointing to a class
	 * 
	 * <p> 指向类的资源路径
	 * 
	 * @return the corresponding fully qualified class name
	 * 
	 * <p> 相应的完全限定类名
	 * 
	 */
	public static String convertResourcePathToClassName(String resourcePath) {
		Assert.notNull(resourcePath, "Resource path must not be null");
		return resourcePath.replace('/', '.');
	}

	/**
	 * Convert a "."-based fully qualified class name to a "/"-based resource path.
	 * 
	 * <p> 将基于“。”的完全限定类名转换为基于“/”的资源路径。
	 * 
	 * @param className the fully qualified class name - 完全限定的类名
	 * @return the corresponding resource path, pointing to the class
	 * 
	 * <p> 相应的资源路径，指向该类
	 * 
	 */
	public static String convertClassNameToResourcePath(String className) {
		Assert.notNull(className, "Class name must not be null");
		return className.replace('.', '/');
	}

	/**
	 * Return a path suitable for use with {@code ClassLoader.getResource}
	 * (also suitable for use with {@code Class.getResource} by prepending a
	 * slash ('/') to the return value). Built by taking the package of the specified
	 * class file, converting all dots ('.') to slashes ('/'), adding a trailing slash
	 * if necessary, and concatenating the specified resource name to this.
	 * 
	 * <p> 返回一个适合与ClassLoader.getResource一起使用的路径（也适用于Class.getResource，
	 * 方法是在返回值前加一个斜杠（'/'））。 通过获取指定类文件的包来构建，将所有点（'。'）转换为斜杠（'/'），
	 * 必要时添加尾部斜杠，并将指定的资源名称连接到此。
	 * 
	 * <br/>As such, this function may be used to build a path suitable for
	 * loading a resource file that is in the same package as a class file,
	 * although {@link org.springframework.core.io.ClassPathResource} is usually
	 * even more convenient.
	 * 
	 * <p> 因此，此函数可用于构建适合于加载与类文件位于同一包中的资源文件的路径，
	 * 尽管org.springframework.core.io.ClassPathResource通常更方便。
	 * 
	 * @param clazz the Class whose package will be used as the base
	 * 
	 * <p> 将其包用作基础的类
	 * 
	 * @param resourceName the resource name to append. A leading slash is optional.
	 * 
	 * <p> 要追加的资源名称。 前导斜杠是可选的。
	 * 
	 * @return the built-up resource path - 建立的资源路径
	 * @see ClassLoader#getResource
	 * @see Class#getResource
	 */
	public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
		Assert.notNull(resourceName, "Resource name must not be null");
		if (!resourceName.startsWith("/")) {
			return classPackageAsResourcePath(clazz) + "/" + resourceName;
		}
		return classPackageAsResourcePath(clazz) + resourceName;
	}

	/**
	 * Given an input class object, return a string which consists of the
	 * class's package name as a pathname, i.e., all dots ('.') are replaced by
	 * slashes ('/'). Neither a leading nor trailing slash is added. The result
	 * could be concatenated with a slash and the name of a resource and fed
	 * directly to {@code ClassLoader.getResource()}. For it to be fed to
	 * {@code Class.getResource} instead, a leading slash would also have
	 * to be prepended to the returned value.
	 * 
	 * <p> 给定一个输入类对象，返回一个由类的包名称组成的字符串作为路径名，即所有点（'。'）都用斜杠（'/'）替换。
	 *  既不添加前导斜杠也不添加尾随斜杠。 结果可以与斜杠和资源名称连接，并直接提供给ClassLoader.getResource（）。 
	 *  为了将它提供给Class.getResource，还必须在返回值之前添加前导斜杠。
	 * 
	 * @param clazz the input class. A {@code null} value or the default
	 * (empty) package will result in an empty string ("") being returned.
	 * 
	 * <p> 输入类。 空值或默认（空）包将导致返回空字符串（“”）。
	 * 
	 * @return a path which represents the package name - 表示包名称的路径
	 * @see ClassLoader#getResource
	 * @see Class#getResource
	 */
	public static String classPackageAsResourcePath(Class<?> clazz) {
		if (clazz == null) {
			return "";
		}
		String className = clazz.getName();
		int packageEndIndex = className.lastIndexOf('.');
		if (packageEndIndex == -1) {
			return "";
		}
		String packageName = className.substring(0, packageEndIndex);
		return packageName.replace('.', '/');
	}

	/**
	 * Build a String that consists of the names of the classes/interfaces
	 * in the given array.
	 * 
	 * <p> 构建一个由给定数组中的类/接口的名称组成的String。
	 * 
	 * <p>Basically like {@code AbstractCollection.toString()}, but stripping
	 * the "class "/"interface " prefix before every class name.
	 * 
	 * <p> 基本上像AbstractCollection.toString（），但在每个类名之前剥去“class”/“interface”前缀。
	 * 
	 * @param classes a Collection of Class objects (may be {@code null})
	 * 
	 * <p> Class对象的集合（可以为null）
	 * 
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 * 
	 * <p> 形式为“[com.foo.Bar，com.foo.Baz]”的字符串
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	public static String classNamesToString(Class... classes) {
		return classNamesToString(Arrays.asList(classes));
	}

	/**
	 * Build a String that consists of the names of the classes/interfaces
	 * in the given collection.
	 * 
	 * <p> 构建一个String，该String由给定集合中的类/接口的名称组成。
	 * 
	 * <p>Basically like {@code AbstractCollection.toString()}, but stripping
	 * the "class "/"interface " prefix before every class name.
	 * 
	 * <p> 基本上像AbstractCollection.toString（），但在每个类名之前剥去“class”/“interface”前缀。
	 * 
	 * @param classes a Collection of Class objects (may be {@code null})
	 * 
	 * <p> Class对象的集合（可以为null）
	 * 
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 * 
	 * <p> 形式为“[com.foo.Bar，com.foo.Baz]”的字符串
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	public static String classNamesToString(Collection<Class> classes) {
		if (CollectionUtils.isEmpty(classes)) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<Class> it = classes.iterator(); it.hasNext(); ) {
			Class clazz = it.next();
			sb.append(clazz.getName());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Copy the given Collection into a Class array.
	 * The Collection must contain Class elements only.
	 * 
	 * <p> 将给定的Collection复制到Class数组中。 集合必须仅包含Class元素。
	 * 
	 * @param collection the Collection to copy - 要复制的集合
	 * @return the Class array ({@code null} if the passed-in
	 * Collection was {@code null})
	 * 
	 * <p> Class数组（如果传入的Collection为null，则为null）
	 */
	public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new Class<?>[collection.size()]);
	}

	/**
	 * Return all interfaces that the given instance implements as array,
	 * including ones implemented by superclasses.
	 * 
	 * <p> 返回给定实例作为数组实现的所有接口，包括由超类实现的接口。
	 * 
	 * @param instance the instance to analyze for interfaces
	 * 
	 * <p> 用于分析接口的实例
	 * 
	 * @return all interfaces that the given instance implements as array
	 * 
	 * <p> 给定实例实现为数组的所有接口
	 * 
	 */
	public static Class<?>[] getAllInterfaces(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getAllInterfacesForClass(instance.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as array,
	 * including ones implemented by superclasses.
	 * 
	 * <p> 返回给定类实现为数组的所有接口，包括由超类实现的接口。
	 * 
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * 
	 * <p> 如果类本身是一个接口，它将作为唯一接口返回。
	 * 
	 * @param clazz the class to analyze for interfaces
	 * 
	 * <p> 用于分析接口的类
	 * 
	 * @return all interfaces that the given object implements as array
	 * 
	 * <p> 给定对象实现为数组的所有接口
	 * 
	 */
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
		return getAllInterfacesForClass(clazz, null);
	}

	/**
	 * Return all interfaces that the given class implements as array,
	 * including ones implemented by superclasses.
	 * 
	 * <p> 返回给定类实现为数组的所有接口，包括由超类实现的接口。
	 * 
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * 
	 * <p> 如果类本身是一个接口，它将作为唯一接口返回。
	 * 
	 * @param clazz the class to analyze for interfaces - 用于分析接口的类
	 * @param classLoader the ClassLoader that the interfaces need to be visible in
	 * (may be {@code null} when accepting all declared interfaces)
	 * 
	 * <p> 接口需要可见的ClassLoader（接受所有声明的接口时可能为null）
	 * 
	 * @return all interfaces that the given object implements as array
	 * 
	 * <p> 给定对象实现为数组的所有接口
	 * 
	 */
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
		Set<Class> ifcs = getAllInterfacesForClassAsSet(clazz, classLoader);
		return ifcs.toArray(new Class[ifcs.size()]);
	}

	/**
	 * Return all interfaces that the given instance implements as Set,
	 * including ones implemented by superclasses.
	 * 
	 * <p> 返回给定实例实现为Set的所有接口，包括由超类实现的接口。
	 * 
	 * @param instance the instance to analyze for interfaces - 用于分析接口的实例
	 * @return all interfaces that the given instance implements as Set
	 * 
	 * <p> 给定实例实现为Set的所有接口
	 * 
	 */
	public static Set<Class> getAllInterfacesAsSet(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getAllInterfacesForClassAsSet(instance.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as Set,
	 * including ones implemented by superclasses.
	 * 
	 * <p> 返回给定类实现为Set的所有接口，包括由超类实现的接口。
	 * 
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * 
	 * <p> 如果类本身是一个接口，它将作为唯一接口返回。
	 * 
	 * @param clazz the class to analyze for interfaces - 用于分析接口的类
	 * @return all interfaces that the given object implements as Set
	 * 
	 * <p> 给定对象实现为Set的所有接口
	 * 
	 */
	public static Set<Class> getAllInterfacesForClassAsSet(Class clazz) {
		return getAllInterfacesForClassAsSet(clazz, null);
	}

	/**
	 * Return all interfaces that the given class implements as Set,
	 * including ones implemented by superclasses.
	 * 
	 * <p> 返回给定类实现为Set的所有接口，包括由超类实现的接口。
	 * 
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * 
	 * <p> 如果类本身是一个接口，它将作为唯一接口返回。
	 * 
	 * @param clazz the class to analyze for interfaces - 用于分析接口的类
	 * @param classLoader the ClassLoader that the interfaces need to be visible in
	 * (may be {@code null} when accepting all declared interfaces)
	 * 
	 * <p> 接口需要可见的ClassLoader（接受所有声明的接口时可能为null）
	 * 
	 * @return all interfaces that the given object implements as Set
	 * 
	 * <p> 给定对象实现为Set的所有接口
	 * 
	 */
	public static Set<Class> getAllInterfacesForClassAsSet(Class clazz, ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface() && isVisible(clazz, classLoader)) {
			return Collections.singleton(clazz);
		}
		Set<Class> interfaces = new LinkedHashSet<Class>();
		while (clazz != null) {
			Class<?>[] ifcs = clazz.getInterfaces();
			for (Class<?> ifc : ifcs) {
				interfaces.addAll(getAllInterfacesForClassAsSet(ifc, classLoader));
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}

	/**
	 * Create a composite interface Class for the given interfaces,
	 * implementing the given interfaces in one single Class.
	 * 
	 * <p> 为给定接口创建复合接口Class，在单个Class中实现给定接口。
	 * 
	 * <p>This implementation builds a JDK proxy class for the given interfaces.
	 * 
	 * <p> 此实现为给定接口构建JDK代理类。
	 * 
	 * @param interfaces the interfaces to merge - 要合并的接口
	 * @param classLoader the ClassLoader to create the composite Class in
	 * 
	 * <p> 用于创建复合类的ClassLoader
	 * 
	 * @return the merged interface as Class - 合并后的接口为Class
	 * @see java.lang.reflect.Proxy#getProxyClass
	 */
	public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
		Assert.notEmpty(interfaces, "Interfaces must not be empty");
		Assert.notNull(classLoader, "ClassLoader must not be null");
		return Proxy.getProxyClass(classLoader, interfaces);
	}

	/**
	 * Determine the common ancestor of the given classes, if any.
	 * 
	 * <p> 确定给定类的共同祖先（如果有）。
	 * 
	 * @param clazz1 the class to introspect - 内省的类
	 * @param clazz2 the other class to introspect - 反省的另一类
	 * @return the common ancestor (i.e. common superclass, one interface
	 * extending the other), or {@code null} if none found. If any of the
	 * given classes is {@code null}, the other class will be returned.
	 * 
	 * <p> 共同的祖先（即公共超类，一个接口扩展另一个），如果没有找到则为null。 如果任何给定的类为null，则返回另一个类。
	 * 
	 * @since 3.2.6
	 */
	public static Class<?> determineCommonAncestor(Class<?> clazz1, Class<?> clazz2) {
		if (clazz1 == null) {
			return clazz2;
		}
		if (clazz2 == null) {
			return clazz1;
		}
		if (clazz1.isAssignableFrom(clazz2)) {
			return clazz1;
		}
		if (clazz2.isAssignableFrom(clazz1)) {
			return clazz2;
		}
		Class<?> ancestor = clazz1;
		do {
			ancestor = ancestor.getSuperclass();
			if (ancestor == null || Object.class.equals(ancestor)) {
				return null;
			}
		}
		while (!ancestor.isAssignableFrom(clazz2));
		return ancestor;
	}

	/**
	 * Check whether the given class is visible in the given ClassLoader.
	 * 
	 * <p> 检查给定的类在给定的ClassLoader中是否可见。
	 * 
	 * @param clazz the class to check (typically an interface)
	 * 
	 * <p> 要检查的类（通常是接口）
	 * 
	 * @param classLoader the ClassLoader to check against (may be {@code null},
	 * in which case this method will always return {@code true})
	 * 
	 * <p> 要检查的ClassLoader（可能为null，在这种情况下，此方法将始终返回true）
	 * 
	 */
	public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
		if (classLoader == null) {
			return true;
		}
		try {
			Class<?> actualClass = classLoader.loadClass(clazz.getName());
			return (clazz == actualClass);
			// Else: different interface class found...
			// 另外：找到不同的接口类......
		}
		catch (ClassNotFoundException ex) {
			// No interface class found...
			// 找不到接口类......
			return false;
		}
	}

	/**
	 * Check whether the given object is a CGLIB proxy.
	 * 
	 * <p> 检查给定对象是否为CGLIB代理。
	 * 
	 * @param object the object to check - 要检查的对象
	 * @see org.springframework.aop.support.AopUtils#isCglibProxy(Object)
	 */
	public static boolean isCglibProxy(Object object) {
		return ClassUtils.isCglibProxyClass(object.getClass());
	}

	/**
	 * Check whether the specified class is a CGLIB-generated class.
	 * 
	 * <p> 检查指定的类是否是CGLIB生成的类。
	 * 
	 * @param clazz the class to check - 要检查的类
	 */
	public static boolean isCglibProxyClass(Class<?> clazz) {
		return (clazz != null && isCglibProxyClassName(clazz.getName()));
	}

	/**
	 * Check whether the specified class name is a CGLIB-generated class.
	 * 
	 * <p> 检查指定的类名是否是CGLIB生成的类。
	 * 
	 * @param className the class name to check - 要检查的类名
	 */
	public static boolean isCglibProxyClassName(String className) {
		return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
	}

}
