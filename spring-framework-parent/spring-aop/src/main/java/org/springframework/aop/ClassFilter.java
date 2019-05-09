/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop;

/**
 * Filter that restricts matching of a pointcut or introduction to
 * a given set of target classes.
 * 
 * <p> 过滤器限制切入点或 introduction 与给定目标类集的匹配。
 *
 * <p>Can be used as part of a {@link Pointcut} or for the entire
 * targeting of an {@link IntroductionAdvisor}.
 * 
 * <p> 可用作Pointcut的一部分或用于IntroductionAdvisor的整个目标。
 *
 * @author Rod Johnson
 * @see Pointcut
 * @see MethodMatcher
 */
public interface ClassFilter {

	/**
	 * Should the pointcut apply to the given interface or target class?
	 * 
	 * <p> 切入点是否应用于给定的接口或目标类？
	 * 
	 * @param clazz the candidate target class
	 * 
	 * <p> 候选目标类
	 * 
	 * @return whether the advice should apply to the given target class
	 * 
	 * <p> advice是否应该适用于给定的目标类
	 */
	boolean matches(Class<?> clazz);


	/**
	 * Canonical instance of a ClassFilter that matches all classes.
	 * 
	 * <p> ClassFilter的Canonical实例，匹配所有类。
	 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}
