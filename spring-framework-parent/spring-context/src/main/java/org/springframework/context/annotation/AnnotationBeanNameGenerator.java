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

package org.springframework.context.annotation;

import java.beans.Introspector;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.support.BeanNameGenerator}
 * implementation for bean classes annotated with the
 * {@link org.springframework.stereotype.Component @Component} annotation
 * or with another annotation that is itself annotated with
 * {@link org.springframework.stereotype.Component @Component} as a
 * meta-annotation. For example, Spring's stereotype annotations (such as
 * {@link org.springframework.stereotype.Repository @Repository}) are
 * themselves annotated with
 * {@link org.springframework.stereotype.Component @Component}.
 * 
 * <p> org.springframework.beans.factory.support.BeanNameGenerator实现，
 * 用于使用@Component注释或使用@Component作为元注释注释的另一个注释注释的bean类。 
 * 例如，Spring的构造型注释（例如@Repository）本身就是用@Component注释的。
 *
 * <p>Also supports Java EE 6's {@link javax.annotation.ManagedBean} and
 * JSR-330's {@link javax.inject.Named} annotations, if available. Note that
 * Spring component annotations always override such standard annotations.
 * 
 * <p> 还支持Java EE 6的javax.annotation.ManagedBean和JSR-330的javax.inject.Named注释（如果可用）。 
 * 请注意，Spring组件注释始终覆盖此类标准注释。
 *
 * <p>If the annotation's value doesn't indicate a bean name, an appropriate
 * name will be built based on the short name of the class (with the first
 * letter lower-cased). For example:
 * 
 * <p> 如果注释的值不指示bean名称，则将根据类的短名称（第一个字母为低位）构建相应的名称。 例如：
 *
 * <pre class="code">com.xyz.FooServiceImpl -&gt; fooServiceImpl</pre>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see org.springframework.stereotype.Component#value()
 * @see org.springframework.stereotype.Repository#value()
 * @see org.springframework.stereotype.Service#value()
 * @see org.springframework.stereotype.Controller#value()
 * @see javax.inject.Named#value()
 */
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

	private static final String COMPONENT_ANNOTATION_CLASSNAME = "org.springframework.stereotype.Component";


	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		if (definition instanceof AnnotatedBeanDefinition) {
			String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
			if (StringUtils.hasText(beanName)) {
				// Explicit bean name found.
				// 找到显式bean名称。
				return beanName;
			}
		}
		// Fallback: generate a unique default bean name.
		// 后备：生成唯一的默认bean名称。
		return buildDefaultBeanName(definition, registry);
	}

	/**
	 * Derive a bean name from one of the annotations on the class.
	 * 
	 * <p> 从类中的一个注释派生bean名称。
	 * 
	 * @param annotatedDef the annotation-aware bean definition
	 * 
	 * <p> 注释感知bean定义
	 * 
	 * @return the bean name, or {@code null} if none is found
	 * 
	 * <p> bean名称，如果没有找到则为null
	 * 
	 */
	protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
		AnnotationMetadata amd = annotatedDef.getMetadata();
		Set<String> types = amd.getAnnotationTypes();
		String beanName = null;
		for (String type : types) {
			AnnotationAttributes attributes = MetadataUtils.attributesFor(amd, type);
			if (isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {
				Object value = attributes.get("value");
				if (value instanceof String) {
					String strVal = (String) value;
					if (StringUtils.hasLength(strVal)) {
						if (beanName != null && !strVal.equals(beanName)) {
							throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
									"component names: '" + beanName + "' versus '" + strVal + "'");
						}
						beanName = strVal;
					}
				}
			}
		}
		return beanName;
	}

	/**
	 * Check whether the given annotation is a stereotype that is allowed
	 * to suggest a component name through its annotation {@code value()}.
	 * 
	 * <p> 检查给定的注释是否是允许通过其注释值（）建议组件名称的构造型。
	 * 
	 * @param annotationType the name of the annotation class to check
	 * 
	 * <p> 要检查的注释类的名称
	 * 
	 * @param metaAnnotationTypes the names of meta-annotations on the given annotation
	 * 
	 * <p> 给定注释上的元注释的名称
	 * 
	 * @param attributes the map of attributes for the given annotation
	 * 
	 * <p> 给定注释的属性映射
	 * 
	 * @return whether the annotation qualifies as a stereotype with component name
	 * 
	 * <p> 注释是否有资格作为具有组件名称的构造型
	 */
	protected boolean isStereotypeWithNameValue(String annotationType,
			Set<String> metaAnnotationTypes, Map<String, Object> attributes) {

		boolean isStereotype = annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) ||
				(metaAnnotationTypes != null && metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME)) ||
				annotationType.equals("javax.annotation.ManagedBean") ||
				annotationType.equals("javax.inject.Named");

		return (isStereotype && attributes != null && attributes.containsKey("value"));
	}

	/**
	 * Derive a default bean name from the given bean definition.
	 * 
	 * <p> 从给定的bean定义派生默认bean名称。
	 * 
	 * <p>The default implementation delegates to {@link #buildDefaultBeanName(BeanDefinition)}.
	 * 
	 * <p> 默认实现委托给buildDefaultBeanName（BeanDefinition）。
	 * 
	 * @param definition the bean definition to build a bean name for
	 * 
	 * <p> 用于构建bean名称的bean定义
	 * 
	 * @param registry the registry that the given bean definition is being registered with
	 * 
	 * <p> 正在注册给定bean定义的注册表
	 * 
	 * @return the default bean name (never {@code null})
	 * 
	 * <p> 默认的bean名称（永远不为null）
	 * 
	 */
	protected String buildDefaultBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return buildDefaultBeanName(definition);
	}

	/**
	 * Derive a default bean name from the given bean definition.
	 * 
	 * <p> 从给定的bean定义派生默认bean名称。
	 * 
	 * <p>The default implementation simply builds a decapitalized version
	 * of the short class name: e.g. "mypackage.MyJdbcDao" -> "myJdbcDao".
	 * 
	 * <p> 默认实现只是构建短类名称的decapitalized版本：例如 “mypackage.MyJdbcDao” - >“myJdbcDao”。
	 * 
	 * <p>Note that inner classes will thus have names of the form
	 * "outerClassName.innerClassName", which because of the period in the
	 * name may be an issue if you are autowiring by name.
	 * 
	 * <p> 请注意，内部类将具有“outerClassName.innerClassName”形式的名称，如果您按名称自动装配，则由于名称中的句点可能会出现问题。
	 * 
	 * @param definition the bean definition to build a bean name for
	 * 
	 * <p> 用于构建bean名称的bean定义
	 * 
	 * @return the default bean name (never {@code null})
	 * 
	 * <p> 默认的bean名称（永远不为null）
	 */
	protected String buildDefaultBeanName(BeanDefinition definition) {
		String shortClassName = ClassUtils.getShortName(definition.getBeanClassName());
		
		/**
		 * 获取字符串并将其转换为普通Java变量名称大小写的实用方法。 这通常意味着将第一个字符从大写转换为小写，
		 * 但在（异常）特殊情况下，当有多个字符并且第一个和第二个字符都是大写时，我们不管它。
		 * 
		 * @param name 首字母大写的字符串
		 */
		return Introspector.decapitalize(shortClassName);
	}

}
