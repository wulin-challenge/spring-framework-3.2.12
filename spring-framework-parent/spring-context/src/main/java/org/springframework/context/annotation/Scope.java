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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * When used as a type-level annotation in conjunction with the {@link Component}
 * annotation, indicates the name of a scope to use for instances of the annotated
 * type.
 * 
 * <p> 当与Component注释一起用作类型级别注释时，指示要用于注释类型实例的范围的名称。
 *
 * <p>When used as a method-level annotation in conjunction with the
 * {@link Bean} annotation, indicates the name of a scope to use for
 * the instance returned from the method.
 * 
 * <p> 当与Bean注释一起用作方法级注释时，指示要用于从方法返回的实例的范围的名称。
 *
 * <p>In this context, scope means the lifecycle of an instance, such as
 * {@code singleton}, {@code prototype}, and so forth. Scopes provided out of the box in
 * Spring may be referred to using the {@code SCOPE_*} constants available in
 * via {@link ConfigurableBeanFactory} and {@code WebApplicationContext} interfaces.
 * 
 * <p> 在此上下文中，范围表示实例的生命周期，例如单例，原型等。 可以使用via ConfigurableBeanFactory和
 * WebApplicationContext接口中提供的SCOPE_ *常量来引用Spring中提供的开箱即用的范围。
 *
 * <p>To register additional custom scopes, see
 * {@link org.springframework.beans.factory.config.CustomScopeConfigurer CustomScopeConfigurer}.
 * 
 * <p> 要注册其他自定义作用域，请参阅CustomScopeConfigurer。
 *
 * @author Mark Fisher
 * @author Chris Beams
 * @since 2.5
 * @see Component
 * @see Bean
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

	/**
	 * Specifies the scope to use for the annotated component/bean.
	 * 
	 * <p> 指定用于带注释的组件/ bean的范围。
	 * 
	 * @see ConfigurableBeanFactory#SCOPE_SINGLETON
	 * @see ConfigurableBeanFactory#SCOPE_PROTOTYPE
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_SESSION
	 */
	String value() default ConfigurableBeanFactory.SCOPE_SINGLETON;

	/**
	 * Specifies whether a component should be configured as a scoped proxy
	 * and if so, whether the proxy should be interface-based or subclass-based.
	 * 
	 * <p> 指定是否应将组件配置为作用域代理，如果是，则指定代理应基于接口还是基于子类。
	 * 
	 * <p>Defaults to {@link ScopedProxyMode#NO}, indicating that no scoped
	 * proxy should be created.
	 * 
	 * <p> 默认为ScopedProxyMode.NO，表示不应创建范围代理。
	 * 
	 * <p>Analogous to {@code <aop:scoped-proxy/>} support in Spring XML.
	 * 
	 * <p> 类似于Spring XML中的<aop：scoped-proxy />支持。
	 */
	ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;

}
