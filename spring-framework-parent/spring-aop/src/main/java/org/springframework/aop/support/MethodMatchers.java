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

package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.util.Assert;

/**
 * Static utility methods for composing {@link MethodMatcher MethodMatchers}.
 * 
 * <p> 用于编写MethodMatchers的静态实用程序方法。
 *
 * <p>A MethodMatcher may be evaluated statically (based on method and target
 * class) or need further evaluation dynamically (based on arguments at the
 * time of method invocation).
 * 
 * <p> 可以静态地评估MethodMatcher（基于方法和目标类）或者需要动态地进行进一步评估（基于方法调用时的参数）。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 11.11.2003
 * @see ClassFilters
 * @see Pointcuts
 */
public abstract class MethodMatchers {

	/**
	 * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.
	 * 
	 * <p> 匹配给定MethodMatchers中任一个（或两个）匹配的所有方法。
	 * 
	 * @param mm1 the first MethodMatcher - 第一个MethodMatcher
	 * @param mm2 the second MethodMatcher - 第二个MethodMatcher
	 * @return a distinct MethodMatcher that matches all methods that either
	 * of the given MethodMatchers matches
	 * 
	 * <p> 一个独特的MethodMatcher，它匹配给定MethodMatchers匹配的所有方法
	 */
	public static MethodMatcher union(MethodMatcher mm1, MethodMatcher mm2) {
		return new UnionMethodMatcher(mm1, mm2);
	}

	/**
	 * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.
	 * 
	 * <p> 匹配给定MethodMatchers中任一个（或两个）匹配的所有方法。
	 * 
	 * @param mm1 the first MethodMatcher - 第一个MethodMatcher
	 * @param cf1 the corresponding ClassFilter for the first MethodMatcher
	 * 
	 * <p> 第一个MethodMatcher的相应ClassFilter
	 * 
	 * @param mm2 the second MethodMatcher - 第二个MethodMatcher
	 * 
	 * @param cf2 the corresponding ClassFilter for the second MethodMatcher
	 * 
	 * <p> 第二个MethodMatcher的相应ClassFilter
	 * 
	 * @return a distinct MethodMatcher that matches all methods that either
	 * of the given MethodMatchers matches
	 * 
	 * <p> 一个独特的MethodMatcher，它匹配给定MethodMatchers匹配的所有方法
	 * 
	 */
	static MethodMatcher union(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
		return new ClassFilterAwareUnionMethodMatcher(mm1, cf1, mm2, cf2);
	}

	/**
	 * Match all methods that <i>both</i> of the given MethodMatchers match.
	 * 
	 * <p> 匹配两个给定MethodMatchers匹配的所有方法。
	 * 
	 * @param mm1 the first MethodMatcher -  第一个MethodMatcher
	 * @param mm2 the second MethodMatcher - 第二个MethodMatcher
	 * @return a distinct MethodMatcher that matches all methods that both
	 * of the given MethodMatchers match
	 * 
	 * <p> 一个独特的MethodMatcher，它匹配给定MethodMatchers匹配的所有方法
	 */
	public static MethodMatcher intersection(MethodMatcher mm1, MethodMatcher mm2) {
		return new IntersectionMethodMatcher(mm1, mm2);
	}

	/**
	 * Apply the given MethodMatcher to the given Method, supporting an
	 * {@link org.springframework.aop.IntroductionAwareMethodMatcher}
	 * (if applicable).
	 * 
	 * <p> 将给定的MethodMatcher应用于给定的Method，支持org.springframework.aop.IntroductionAwareMethodMatcher（如果适用）。
	 * 
	 * @param mm the MethodMatcher to apply (may be an IntroductionAwareMethodMatcher)
	 * 
	 * <p> 要应用的MethodMatcher（可能是IntroductionAwareMethodMatcher）
	 * 
	 * @param method the candidate method - 候选方法
	 * 
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * 
	 * <p> 目标类（可以为null，在这种情况下候选类必须被视为方法的声明类）
	 * 
	 * @param hasIntroductions {@code true} if the object on whose behalf we are
	 * asking is the subject on one or more introductions; {@code false} otherwise
	 * 
	 * <p> 如果我们要求的对象是一个或多个介绍的主题，则为true; 否则是假的
	 * 
	 * @return whether or not this method matches statically
	 * 
	 * <p> 此方法是否与静态匹配
	 * 
	 */
	public static boolean matches(MethodMatcher mm, Method method, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(mm, "MethodMatcher must not be null");
		return ((mm instanceof IntroductionAwareMethodMatcher &&
				((IntroductionAwareMethodMatcher) mm).matches(method, targetClass, hasIntroductions)) ||
				mm.matches(method, targetClass));
	}


	/**
	 * MethodMatcher implementation for a union of two given MethodMatchers.
	 * 
	 * <p> MethodMatcher实现两个给定MethodMatchers的并集。
	 * 
	 */
	@SuppressWarnings("serial")
	private static class UnionMethodMatcher implements IntroductionAwareMethodMatcher, Serializable {

		private final MethodMatcher mm1;

		private final MethodMatcher mm2;

		public UnionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
			Assert.notNull(mm1, "First MethodMatcher must not be null");
			Assert.notNull(mm2, "Second MethodMatcher must not be null");
			this.mm1 = mm1;
			this.mm2 = mm2;
		}

		public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
			return (matchesClass1(targetClass) && MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions)) ||
					(matchesClass2(targetClass) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
		}

		public boolean matches(Method method, Class<?> targetClass) {
			return (matchesClass1(targetClass) && this.mm1.matches(method, targetClass)) ||
					(matchesClass2(targetClass) && this.mm2.matches(method, targetClass));
		}

		protected boolean matchesClass1(Class<?> targetClass) {
			return true;
		}

		protected boolean matchesClass2(Class<?> targetClass) {
			return true;
		}

		public boolean isRuntime() {
			return this.mm1.isRuntime() || this.mm2.isRuntime();
		}

		public boolean matches(Method method, Class<?> targetClass, Object[] args) {
			return this.mm1.matches(method, targetClass, args) || this.mm2.matches(method, targetClass, args);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof UnionMethodMatcher)) {
				return false;
			}
			UnionMethodMatcher that = (UnionMethodMatcher) obj;
			return (this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2));
		}

		@Override
		public int hashCode() {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.mm1.hashCode();
			hashCode = 37 * hashCode + this.mm2.hashCode();
			return hashCode;
		}
	}


	/**
	 * MethodMatcher implementation for a union of two given MethodMatchers,
	 * supporting an associated ClassFilter per MethodMatcher.
	 * 
	 * <p> MethodMatcher实现两个给定MethodMatchers的并集，支持每个MethodMatcher关联的ClassFilter。
	 * 
	 */
	@SuppressWarnings("serial")
	private static class ClassFilterAwareUnionMethodMatcher extends UnionMethodMatcher {

		private final ClassFilter cf1;

		private final ClassFilter cf2;

		public ClassFilterAwareUnionMethodMatcher(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
			super(mm1, mm2);
			this.cf1 = cf1;
			this.cf2 = cf2;
		}

		@Override
		protected boolean matchesClass1(Class<?> targetClass) {
			return this.cf1.matches(targetClass);
		}

		@Override
		protected boolean matchesClass2(Class<?> targetClass) {
			return this.cf2.matches(targetClass);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!super.equals(other)) {
				return false;
			}
			ClassFilter otherCf1 = ClassFilter.TRUE;
			ClassFilter otherCf2 = ClassFilter.TRUE;
			if (other instanceof ClassFilterAwareUnionMethodMatcher) {
				ClassFilterAwareUnionMethodMatcher cfa = (ClassFilterAwareUnionMethodMatcher) other;
				otherCf1 = cfa.cf1;
				otherCf2 = cfa.cf2;
			}
			return (this.cf1.equals(otherCf1) && this.cf2.equals(otherCf2));
		}
	}


	/**
	 * MethodMatcher implementation for an intersection of two given MethodMatchers.
	 * 
	 * <p> MethodMatcher实现两个给定MethodMatchers的交集。
	 * 
	 */
	@SuppressWarnings("serial")
	private static class IntersectionMethodMatcher implements IntroductionAwareMethodMatcher, Serializable {

		private final MethodMatcher mm1;

		private final MethodMatcher mm2;

		public IntersectionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
			Assert.notNull(mm1, "First MethodMatcher must not be null");
			Assert.notNull(mm2, "Second MethodMatcher must not be null");
			this.mm1 = mm1;
			this.mm2 = mm2;
		}

		public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
			return MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions) &&
					MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions);
		}

		public boolean matches(Method method, Class<?> targetClass) {
			return this.mm1.matches(method, targetClass) && this.mm2.matches(method, targetClass);
		}

		public boolean isRuntime() {
			return this.mm1.isRuntime() || this.mm2.isRuntime();
		}

		public boolean matches(Method method, Class<?> targetClass, Object[] args) {
			// Because a dynamic intersection may be composed of a static and dynamic part,
			// we must avoid calling the 3-arg matches method on a dynamic matcher, as
			// it will probably be an unsupported operation.
			// 因为动态交集可能由静态和动态部分组成，所以我们必须避免在动态匹配器上调用3-arg匹配方法，因为它可能是一个不受支持的操作。
			boolean aMatches = this.mm1.isRuntime() ?
					this.mm1.matches(method, targetClass, args) : this.mm1.matches(method, targetClass);
			boolean bMatches = this.mm2.isRuntime() ?
					this.mm2.matches(method, targetClass, args) : this.mm2.matches(method, targetClass);
			return aMatches && bMatches;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof IntersectionMethodMatcher)) {
				return false;
			}
			IntersectionMethodMatcher that = (IntersectionMethodMatcher) other;
			return (this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2));
		}

		@Override
		public int hashCode() {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.mm1.hashCode();
			hashCode = 37 * hashCode + this.mm2.hashCode();
			return hashCode;
		}
	}

}
