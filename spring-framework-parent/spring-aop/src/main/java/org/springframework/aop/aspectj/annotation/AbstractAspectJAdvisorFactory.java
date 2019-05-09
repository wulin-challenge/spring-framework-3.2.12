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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for factories that can create Spring AOP Advisors
 * given AspectJ classes from classes honoring the AspectJ 5 annotation syntax.
 * 
 * <p> 工厂的抽象基类，可以创建Spring AOP Advisors，从尊重AspectJ 5注释语法的类中获取AspectJ类。
 *
 * <p>This class handles annotation parsing and validation functionality.
 * It does not actually generate Spring AOP Advisors, which is deferred to subclasses.
 * 
 * <p> 此类处理注释解析和验证功能。 它实际上并不生成Spring AOP Advisors，它被推迟到子类。
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

	protected static final ParameterNameDiscoverer ASPECTJ_ANNOTATION_PARAMETER_NAME_DISCOVERER =
			new AspectJAnnotationParameterNameDiscoverer();

	private static final String AJC_MAGIC = "ajc$";


	/**
	 * Find and return the first AspectJ annotation on the given method
	 * (there <i>should</i> only be one anyway...)
	 * 
	 * <p> 查找并返回给定方法的第一个AspectJ注释（反正应该只有一个...）
	 */
	@SuppressWarnings("unchecked")
	protected static AspectJAnnotation findAspectJAnnotationOnMethod(Method method) {
		//设置敏感的注解类
		Class<? extends Annotation>[] classesToLookFor = new Class[] {
				Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class, Pointcut.class};
		for (Class<? extends Annotation> c : classesToLookFor) {
			AspectJAnnotation foundAnnotation = findAnnotation(method, c);
			if (foundAnnotation != null) {
				return foundAnnotation;
			}
		}
		return null;
	}

	/**
	 * 获取指定方法上的注解并使用AspectJAnnotation封装
	 * @param method
	 * @param toLookFor
	 * @return
	 */
	private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> toLookFor) {
		A result = AnnotationUtils.findAnnotation(method, toLookFor);
		if (result != null) {
			return new AspectJAnnotation<A>(result);
		}
		else {
			return null;
		}
	}


	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());

	protected final ParameterNameDiscoverer parameterNameDiscoverer;


	protected AbstractAspectJAdvisorFactory() {
		PrioritizedParameterNameDiscoverer prioritizedParameterNameDiscoverer = new PrioritizedParameterNameDiscoverer();
		prioritizedParameterNameDiscoverer.addDiscoverer(ASPECTJ_ANNOTATION_PARAMETER_NAME_DISCOVERER);
		this.parameterNameDiscoverer = prioritizedParameterNameDiscoverer;
	}

	/**
	 * We consider something to be an AspectJ aspect suitable for use by the Spring AOP system
	 * if it has the @Aspect annotation, and was not compiled by ajc. The reason for this latter test
	 * is that aspects written in the code-style (AspectJ language) also have the annotation present
	 * when compiled by ajc with the -1.5 flag, yet they cannot be consumed by Spring AOP.
	 * 
	 * <p> 我们认为某些AspectJ方面适合Spring AOP系统使用，如果它具有@Aspect注释，并且不是由ajc编译的。 后一个测试的原因是，
	 * 当由ajc使用-1.5标志编译时，以代码方式（AspectJ语言）编写的方面也具有注释，但它们不能被Spring AOP使用。
	 */
	public boolean isAspect(Class<?> clazz) {
		return (hasAspectAnnotation(clazz) && !compiledByAjc(clazz));
	}

	private boolean hasAspectAnnotation(Class<?> clazz) {
		return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null);
	}

	/**
	 * We need to detect this as "code-style" AspectJ aspects should not be
	 * interpreted by Spring AOP.
	 * 
	 * <p> 我们需要检测到这一点，因为“代码风格”的AspectJ方面不应该被Spring AOP解释。
	 */
	private boolean compiledByAjc(Class<?> clazz) {
		// The AJTypeSystem goes to great lengths to provide a uniform appearance between code-style and
		// annotation-style aspects. Therefore there is no 'clean' way to tell them apart. Here we rely on
		// an implementation detail of the AspectJ compiler.
		
		// AJTypeSystem竭尽全力在代码风格和注释风格方面之间提供统一的外观。 
		// 因此，没有“干净”的方式来区分它们。 这里我们依赖于AspectJ编译器的实现细节。
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().startsWith(AJC_MAGIC)) {
				return true;
			}
		}
		return false;
	}

	public void validate(Class<?> aspectClass) throws AopConfigException {
		// If the parent has the annotation and isn't abstract it's an error
		// 如果父级具有注释并且不是抽象的，那么这是一个错误
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null &&
				!Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			throw new AopConfigException("[" + aspectClass.getName() + "] cannot extend concrete aspect [" +
					aspectClass.getSuperclass().getName() + "]");
		}

		AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
		if (!ajType.isAspect()) {
			throw new NotAnAtAspectException(aspectClass);
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
			throw new AopConfigException(aspectClass.getName() + " uses percflow instantiation model: " +
					"This is not supported in Spring AOP.");
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
			throw new AopConfigException(aspectClass.getName() + " uses percflowbelow instantiation model: " +
					"This is not supported in Spring AOP.");
		}
	}

	/**
	 * The pointcut and advice annotations both have an "argNames" member which contains a
	 * comma-separated list of the argument names. We use this (if non-empty) to build the
	 * formal parameters for the pointcut.
	 * 
	 * <p> 切入点和建议注释都有一个“argNames”成员，其中包含以逗号分隔的参数名称列表。 我们使用它（如果非空）来构建切入点的形式参数。
	 */
	protected AspectJExpressionPointcut createPointcutExpression(
			Method annotatedMethod, Class declarationScope, String[] pointcutParameterNames) {

		Class<?> [] pointcutParameterTypes = new Class<?>[0];
		if (pointcutParameterNames != null) {
			pointcutParameterTypes = extractPointcutParameterTypes(pointcutParameterNames,annotatedMethod);
		}

		AspectJExpressionPointcut ajexp =
				new AspectJExpressionPointcut(declarationScope,pointcutParameterNames,pointcutParameterTypes);
		ajexp.setLocation(annotatedMethod.toString());
		return ajexp;
	}

	/**
	 * Create the pointcut parameters needed by aspectj based on the given argument names
	 * and the argument types that are available from the adviceMethod. Needs to take into
	 * account (ignore) any JoinPoint based arguments as these are not pointcut context but
	 * rather part of the advice execution context (thisJoinPoint, thisJoinPointStaticPart)
	 * 
	 * <p> 根据给定的参数名称和adviceMethod中提供的参数类型，创建aspectj所需的切入点参数。 需要考虑（忽略）任何基于JoinPoint的参数，
	 * 因为这些不是切入点上下文，而是建议执行上下文的一部分（thisJoinPoint，thisJoinPointStaticPart）
	 */
	private Class<?>[] extractPointcutParameterTypes(String[] argNames, Method adviceMethod) {
		Class<?>[] ret = new Class<?>[argNames.length];
		Class<?>[] paramTypes = adviceMethod.getParameterTypes();
		if (argNames.length > paramTypes.length) {
			throw new IllegalStateException("Expecting at least " + argNames.length +
					" arguments in the advice declaration, but only found " + paramTypes.length);
		}
		// Make the simplifying assumption for now that all of the JoinPoint based arguments
		// come first in the advice declaration.
		
		// 现在简化假设所有基于JoinPoint的参数都在建议声明中排在第一位。
		int typeOffset = paramTypes.length - argNames.length;
		for (int i = 0; i < ret.length; i++) {
			ret[i] = paramTypes[i + typeOffset];
		}
		return ret;
	}


	protected enum AspectJAnnotationType {
		AtPointcut,
		AtBefore,
		AtAfter,
		AtAfterReturning,
		AtAfterThrowing,
		AtAround
	}


	/**
	 * Class modelling an AspectJ annotation, exposing its type enumeration and
	 * pointcut String.
	 * 
	 * <p> 对AspectJ注释进行建模的类，公开其类型枚举和切入点String。
	 */
	protected static class AspectJAnnotation<A extends Annotation> {

		private static final String[] EXPRESSION_PROPERTIES = new String[] {"value", "pointcut"};

		private static Map<Class, AspectJAnnotationType> annotationTypes =
				new HashMap<Class, AspectJAnnotationType>();

		static {
			annotationTypes.put(Pointcut.class,AspectJAnnotationType.AtPointcut);
			annotationTypes.put(After.class,AspectJAnnotationType.AtAfter);
			annotationTypes.put(AfterReturning.class,AspectJAnnotationType.AtAfterReturning);
			annotationTypes.put(AfterThrowing.class,AspectJAnnotationType.AtAfterThrowing);
			annotationTypes.put(Around.class,AspectJAnnotationType.AtAround);
			annotationTypes.put(Before.class,AspectJAnnotationType.AtBefore);
		}

		private final A annotation;

		private final AspectJAnnotationType annotationType;

		private final String pointcutExpression;

		private final String argumentNames;

		public AspectJAnnotation(A annotation) {
			this.annotation = annotation;
			this.annotationType = determineAnnotationType(annotation);
			// We know these methods exist with the same name on each object,
			// but need to invoke them reflectively as there isn't a common interface.
			
			// 我们知道这些方法在每个对象上都存在相同的名称，但需要反射性地调用它们，因为没有通用接口。
			try {
				this.pointcutExpression = resolveExpression(annotation);
				this.argumentNames = (String) annotation.getClass().getMethod("argNames").invoke(annotation);
			}
			catch (Exception ex) {
				throw new IllegalArgumentException(annotation + " cannot be an AspectJ annotation", ex);
			}
		}

		private AspectJAnnotationType determineAnnotationType(A annotation) {
			for (Class type : annotationTypes.keySet()) {
				if (type.isInstance(annotation)) {
					return annotationTypes.get(type);
				}
			}
			throw new IllegalStateException("Unknown annotation type: " + annotation.toString());
		}

		private String resolveExpression(A annotation) throws Exception {
			String expression = null;
			for (String methodName : EXPRESSION_PROPERTIES) {
				Method method;
				try {
					method = annotation.getClass().getDeclaredMethod(methodName);
				}
				catch (NoSuchMethodException ex) {
					method = null;
				}
				if (method != null) {
					String candidate = (String) method.invoke(annotation);
					if (StringUtils.hasText(candidate)) {
						expression = candidate;
					}
				}
			}
			return expression;
		}

		public AspectJAnnotationType getAnnotationType() {
			return this.annotationType;
		}

		public A getAnnotation() {
			return this.annotation;
		}

		public String getPointcutExpression() {
			return this.pointcutExpression;
		}

		public String getArgumentNames() {
			return this.argumentNames;
		}

		@Override
		public String toString() {
			return this.annotation.toString();
		}
	}


	/**
	 * ParameterNameDiscoverer implementation that analyzes the arg names
	 * specified at the AspectJ annotation level.
	 * 
	 * <p> ParameterNameDiscoverer实现，用于分析在AspectJ注释级别指定的arg名称。
	 */
	private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

		public String[] getParameterNames(Method method) {
			if (method.getParameterTypes().length == 0) {
				return new String[0];
			}
			AspectJAnnotation annotation = findAspectJAnnotationOnMethod(method);
			if (annotation == null) {
				return null;
			}
			StringTokenizer strTok = new StringTokenizer(annotation.getArgumentNames(), ",");
			if (strTok.countTokens() > 0) {
				String[] names = new String[strTok.countTokens()];
				for (int i = 0; i < names.length; i++) {
					names[i] = strTok.nextToken();
				}
				return names;
			}
			else {
				return null;
			}
		}

		public String[] getParameterNames(Constructor ctor) {
			throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
		}
	}

}
