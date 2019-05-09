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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.ComposablePointcut;

/**
 * Metadata for an AspectJ aspect class, with an additional Spring AOP pointcut
 * for the per clause.
 * 
 * <p> AspectJ方面类的元数据，为per子句添加了额外的Spring AOP切入点。
 *
 * <p>Uses AspectJ 5 AJType reflection API, so is only supported on Java 5.
 * Enables us to work with different AspectJ instantiation models such as
 * "singleton", "pertarget" and "perthis".
 * 
 * <p> 使用AspectJ 5 AJType反射API，因此仅支持Java 5.使我们能够使用不同的AspectJ实例化模型，
 * 例如“singleton”，“pertarget”和“perthis”。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 */
public class AspectMetadata {

	/**
	 * AspectJ reflection information (AspectJ 5 / Java 5 specific).
	 * 
	 * <p> AspectJ反射信息（AspectJ 5 / Java 5具体）。
	 */
	private final AjType ajType;

	/**
	 * Spring AOP pointcut corresponding to the per clause of the
	 * aspect. Will be the Pointcut.TRUE canonical instance in the
	 * case of a singleton, otherwise an AspectJExpressionPointcut.
	 * 
	 * <p> Spring AOP切入点对应于方面的per子句。 在单例的情况下将是Pointcut.TRUE规范实例，
	 * 否则是AspectJExpressionPointcut。
	 */
	private final Pointcut perClausePointcut;

	/**
	 * The name of this aspect as defined to Spring (the bean name) -
	 * allows us to determine if two pieces of advice come from the
	 * same aspect and hence their relative precedence.
	 * 
	 * <p> Spring定义的这个方面的名称（bean名称） - 允许我们确定两条建议是否来自同一方面，
	 * 因此它们的相对优先级。
	 */
	private String aspectName;


	/**
	 * Create a new AspectMetadata instance for the given aspect class.
	 * 
	 * <p> 为给定的方面类创建一个新的AspectMetadata实例。
	 * 
	 * @param aspectClass the aspect class - 方面类
	 * @param aspectName the name of the aspect - 这个方面的名称
	 */
	public AspectMetadata(Class<?> aspectClass, String aspectName) {
		this.aspectName = aspectName;

		Class<?> currClass = aspectClass;
		AjType ajType = null;
		while (!currClass.equals(Object.class)) {
			AjType ajTypeToCheck = AjTypeSystem.getAjType(currClass);
			if (ajTypeToCheck.isAspect()) {
				ajType = ajTypeToCheck;
				break;
			}
			currClass = currClass.getSuperclass();
		}
		if (ajType == null) {
			throw new IllegalArgumentException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
		}
		this.ajType = ajType;
		if (this.ajType.getDeclarePrecedence().length > 0) {
			throw new IllegalArgumentException("DeclarePrecendence not presently supported in Spring AOP");
		}

		switch (this.ajType.getPerClause().getKind()) {
			case SINGLETON :
				this.perClausePointcut = Pointcut.TRUE;
				return;
			case PERTARGET : case PERTHIS :
				AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
				ajexp.setLocation("@Aspect annotation on " + aspectClass.getName());
				ajexp.setExpression(findPerClause(aspectClass));
				this.perClausePointcut = ajexp;
				return;
			case PERTYPEWITHIN :
				// Works with a type pattern
				this.perClausePointcut = new ComposablePointcut(new TypePatternClassFilter(findPerClause(aspectClass)));
				return;
			default :
				throw new AopConfigException(
						"PerClause " + ajType.getPerClause().getKind() + " not supported by Spring AOP for " + aspectClass);
		}
	}

	/**
	 * Extract contents from String of form {@code pertarget(contents)}.
	 * 
	 * <p> 从表格pertarget（内容）的String中提取内容。
	 */
	private String findPerClause(Class<?> aspectClass) {
		// TODO when AspectJ provides this, we can remove this hack. Hence we don't
		// bother to make it elegant. Or efficient. Or robust :-)
		String str = aspectClass.getAnnotation(Aspect.class).value();
		str = str.substring(str.indexOf("(") + 1);
		str = str.substring(0, str.length() - 1);
		return str;
	}


	/**
	 * Return AspectJ reflection information.
	 * 
	 * <p> 返回AspectJ反射信息。
	 */
	public AjType getAjType() {
		return this.ajType;
	}

	/**
	 * Return the aspect class.
	 * 
	 * <p> 返回方面类。
	 */
	public Class getAspectClass() {
		return this.ajType.getJavaClass();
	}

	/**
	 * Return the aspect class.
	 * 
	 * <p> 返回方面类。
	 */
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * Return a Spring pointcut expression for a singleton aspect.
	 * (e.g. {@code Pointcut.TRUE} if it's a singleton).
	 * 
	 * <p> 返回单个方面的Spring切入点表达式。 （例如，Pointcut.TRUE，如果它是单身）。
	 */
	public Pointcut getPerClausePointcut() {
		return this.perClausePointcut;
	}

	/**
	 * Return whether the aspect is defined as "perthis" or "pertarget".
	 * 
	 * <p> 返回方面是否定义为“perthis”或“pertarget”。
	 */
	public boolean isPerThisOrPerTarget() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
	}

	/**
	 * Return whether the aspect is defined as "pertypewithin".
	 * 
	 * <p> 返回方面是否定义为“pertypewithin”。
	 */
	public boolean isPerTypeWithin() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTYPEWITHIN);
	}

	/**
	 * Return whether the aspect needs to be lazily instantiated.
	 * 
	 * <p> 返回方面是否需要延迟实例化。
	 */
	public boolean isLazilyInstantiated() {
		return (isPerThisOrPerTarget() || isPerTypeWithin());
	}

}
