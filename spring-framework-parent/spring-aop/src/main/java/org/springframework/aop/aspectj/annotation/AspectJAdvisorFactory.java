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

package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;

/**
 * Interface for factories that can create Spring AOP Advisors from classes
 * annotated with AspectJ annotation syntax.
 * 
 * <p> 可以使用AspectJ注释语法注释的类创建Spring AOP Advisors的工厂接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjTypeSystem
 */
public interface AspectJAdvisorFactory {

	/**
	 * Determine whether or not the given class is an aspect, as reported
	 * by AspectJ's {@link org.aspectj.lang.reflect.AjTypeSystem}.
	 * 
	 * <p> 确定给定的类是否是方面，如AspectJ的org.aspectj.lang.reflect.AjTypeSystem所报告。
	 * 
	 * <p>Will simply return {@code false} if the supposed aspect is
	 * invalid (such as an extension of a concrete aspect class).
	 * Will return true for some aspects that Spring AOP cannot process,
	 * such as those with unsupported instantiation models.
	 * Use the {@link #validate} method to handle these cases if necessary.
	 * 
	 * <p> 如果假定的方面无效（例如具体方面类的扩展），则将简单地返回false。 对于Spring AOP无法处理的某些方面
	 * （例如具有不受支持的实例化模型的方面），将返回true。 如有必要，使用validate方法处理这些情况。
	 * 
	 * @param clazz the supposed annotation-style AspectJ class
	 * 
	 * <p> 假设的注释风格的AspectJ类
	 * 
	 * @return whether or not this class is recognized by AspectJ as an aspect class
	 * 
	 * <p> 该类是否被AspectJ识别为方面类
	 */
	boolean isAspect(Class<?> clazz);

	/**
	 * Is the given class a valid AspectJ aspect class?
	 * 
	 * <p> 给定的类是否是有效的AspectJ方面类？
	 * 
	 * @param aspectClass the supposed AspectJ annotation-style class to validate
	 * 
	 * <p> 假设的AspectJ注释样式类要验证
	 * 
	 * @throws AopConfigException if the class is an invalid aspect
	 * (which can never be legal)
	 * 
	 * <p> 如果这个类是无效的方面（永远不合法）
	 * 
	 * @throws NotAnAtAspectException if the class is not an aspect at all
	 * (which may or may not be legal, depending on the context)
	 * 
	 * <p> 如果课程根本不是一个方面（根据具体情况可能合法，也可能不合法）
	 */
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * Build Spring AOP Advisors for all annotated At-AspectJ methods
	 * on the specified aspect instance.
	 * 
	 * <p> 在指定的方面实例上为所有带注释的At-AspectJ方法构建Spring AOP Advisors。
	 * 
	 * @param aif the aspect instance factory (not the aspect instance itself
	 * in order to avoid eager instantiation)
	 * 
	 * <p> 方面实例工厂（不是方面实例本身，以避免急切的实例化）
	 * 
	 * @return a list of advisors for this class
	 * 
	 * <p> 这个班级的顾问列表
	 * 
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aif);

	/**
	 * Build a Spring AOP Advisor for the given AspectJ advice method.
	 * 
	 * <p> 为给定的AspectJ建议方法构建Spring AOP Advisor。
	 * 
	 * @param candidateAdviceMethod the candidate advice method - 候选人的建议方法
	 * @param aif the aspect instance factory - 方面实例工厂
	 * @param declarationOrderInAspect the declaration order within the aspect
	 * 
	 * <p> 方面内的声明顺序
	 * 
	 * @param aspectName the name of the aspect - 方面的名称
	 * @return {@code null} if the method is not an AspectJ advice method
	 * or if it is a pointcut that will be used by other advice but will not
	 * create a Spring advice in its own right
	 * 
	 * <p> 如果方法不是AspectJ建议方法，或者它是一个将由其他建议使用的切入点但是不会自己创建Spring建议，则返回null
	 * 
	 */
	Advisor getAdvisor(Method candidateAdviceMethod,
			MetadataAwareAspectInstanceFactory aif, int declarationOrderInAspect, String aspectName);

	/**
	 * Build a Spring AOP Advice for the given AspectJ advice method.
	 * 
	 * <p> 为给定的AspectJ建议方法构建Spring AOP建议。
	 * 
	 * @param candidateAdviceMethod the candidate advice method - 候选人的建议方法
	 * @param pointcut the corresponding AspectJ expression pointcut - 相应的AspectJ表达式切入点
	 * @param aif the aspect instance factory - 方面实例工厂
	 * @param declarationOrderInAspect the declaration order within the aspect - 方面内的声明顺序
	 * @param aspectName the name of the aspect - 方面的名称
	 * @return {@code null} if the method is not an AspectJ advice method
	 * or if it is a pointcut that will be used by other advice but will not
	 * create a Spring advice in its own right
	 * 
	 * <p> 如果方法不是AspectJ建议方法，或者它是一个将由其他建议使用的切入点但是不会自己创建Spring建议，则返回null
	 * 
	 * @see org.springframework.aop.aspectj.AspectJAroundAdvice
	 * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice
	 */
	Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut pointcut,
			MetadataAwareAspectInstanceFactory aif, int declarationOrderInAspect, String aspectName);

}
