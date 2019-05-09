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

package org.springframework.core.type;

/**
 * Interface that defines abstract metadata of a specific class,
 * in a form that does not require that class to be loaded yet.
 * 
 * <p> 定义特定类的抽象元数据的接口，其形式不需要加载该类。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see StandardClassMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getClassMetadata()
 * @see AnnotationMetadata
 */
public interface ClassMetadata {

	/**
	 * Return the name of the underlying class.
	 * 
	 * <p> 返回基础类的名称。
	 * 
	 */
	String getClassName();

	/**
	 * Return whether the underlying class represents an interface.
	 * 
	 * <p> 返回底层类是否表示接口。
	 * 
	 */
	boolean isInterface();

	/**
	 * Return whether the underlying class is marked as abstract.
	 * 
	 * <p> 返回底层类是否标记为抽象。
	 * 
	 */
	boolean isAbstract();

	/**
	 * Return whether the underlying class represents a concrete class,
	 * i.e. neither an interface nor an abstract class.
	 * 
	 * <p> 返回底层类是表示具体类，即既不是接口也不是抽象类。
	 * 
	 */
	boolean isConcrete();

	/**
	 * Return whether the underlying class is marked as 'final'.
	 * 
	 * <p> 返回底层类是否标记为“final”。
	 * 
	 */
	boolean isFinal();

	/**
	 * Determine whether the underlying class is independent,
	 * i.e. whether it is a top-level class or a nested class
	 * (static inner class) that can be constructed independent
	 * from an enclosing class.
	 * 
	 * <p> 确定底层类是否是独立的，即它是一个顶层类还是一个嵌套类（静态内部类），它可以独立于一个封闭类构造。
	 * 
	 */
	boolean isIndependent();

	/**
	 * Return whether the underlying class has an enclosing class
	 * (i.e. the underlying class is an inner/nested class or
	 * a local class within a method).
	 * 
	 * <p> 返回底层类是否具有封闭类（即底层类是内部/嵌套类或方法中的本地类）。
	 * 
	 * <p>If this method returns {@code false}, then the
	 * underlying class is a top-level class.
	 * 
	 * <p> 如果此方法返回false，则底层类是顶级类。
	 * 
	 */
	boolean hasEnclosingClass();

	/**
	 * Return the name of the enclosing class of the underlying class,
	 * or {@code null} if the underlying class is a top-level class.
	 * 
	 * <p> 返回基础类的封闭类的名称，如果基础类是顶级类，则返回null。
	 */
	String getEnclosingClassName();

	/**
	 * Return whether the underlying class has a super class.
	 * 
	 * <p> 返回底层类是否具有超类。
	 * 
	 */
	boolean hasSuperClass();

	/**
	 * Return the name of the super class of the underlying class,
	 * or {@code null} if there is no super class defined.
	 * 
	 * <p> 返回基础类的超类的名称，如果没有定义超类，则返回null。
	 * 
	 */
	String getSuperClassName();

	/**
	 * Return the names of all interfaces that the underlying class
	 * implements, or an empty array if there are none.
	 * 
	 * <p> 返回基础类实现的所有接口的名称，如果没有则返回空数组。
	 * 
	 */
	String[] getInterfaceNames();

	/**
	 * Return the names of all classes declared as members of the class represented by
	 * this ClassMetadata object. This includes public, protected, default (package)
	 * access, and private classes and interfaces declared by the class, but excludes
	 * inherited classes and interfaces. An empty array is returned if no member classes
	 * or interfaces exist.
	 * 
	 * <p> 返回声明为此ClassMetadata对象表示的类成员的所有类的名称。 这包括公共，受保护，默认（包）访问以及类声明的私有类和接口，
	 * 但不包括继承的类和接口。 如果不存在任何成员类或接口，则返回空数组。
	 */
	String[] getMemberClassNames();

}
