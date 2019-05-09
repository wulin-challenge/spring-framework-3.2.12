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

package org.springframework.aop;

/**
 * Interface supplying the information necessary to describe an introduction.
 * 
 * <p> 提供描述介绍所需信息的接口。
 *
 * <p>{@link IntroductionAdvisor IntroductionAdvisors} must implement this
 * interface. If an {@link org.aopalliance.aop.Advice} implements this,
 * it may be used as an introduction without an {@link IntroductionAdvisor}.
 * In this case, the advice is self-describing, providing not only the
 * necessary behavior, but describing the interfaces it introduces.
 * 
 * <p> IntroductionAdvisors必须实现此接口。 如果org.aopalliance.aop.Advice实现了这一点，
 * 它可以用作没有IntroductionAdvisor的介绍。 在这种情况下，advice 是自描述的，不仅提供必要的行为，而且描述它引入的接口。
 *
 * @author Rod Johnson
 * @since 1.1.1
 */
public interface IntroductionInfo {

	/**
	 * Return the additional interfaces introduced by this Advisor or Advice.
	 * 
	 * <p> 返回此Advisor或Advice引入的其他接口。
	 * 
	 * @return the introduced interfaces - introduced的接口
	 */
	Class<?>[] getInterfaces();

}
