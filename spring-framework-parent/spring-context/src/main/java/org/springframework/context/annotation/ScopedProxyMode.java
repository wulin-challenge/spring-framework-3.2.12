/*
 * Copyright 2002-2009 the original author or authors.
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

/**
 * Enumerates the various scoped-proxy options.
 * 
 * <p> 枚举各种范围代理选项。
 *
 * <p>For a fuller discussion of exactly what a scoped-proxy is, see that
 * section of the Spring reference documentation entitled 'Scoped beans as
 * dependencies'.
 * 
 * <p> 有关scoped-proxy的确切内容的更全面讨论，请参阅Spring参考文档中标题为“Scoped beans as dependencies”的部分。
 *
 * @author Mark Fisher
 * @since 2.5
 * @see ScopeMetadata
 */
public enum ScopedProxyMode {

	/**
	 * Default typically equals {@link #NO}, unless a different default
	 * has been configured at the component-scan instruction level.
	 * 
	 * <p> 除非在组件扫描指令级别配置了不同的默认值，否则默认值通常等于NO。
	 */
	DEFAULT,

	/**
	 * Do not create a scoped proxy.
	 * 
	 * <p> 不要创建范围代理。
	 * 
	 * <p>This proxy-mode is not typically useful when used with a
	 * non-singleton scoped instance, which should favor the use of the
	 * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
	 * is to be used as a dependency.
	 * 
	 * <p> 当与非单例作用域实例一起使用时，此代理模式通常不常用，如果要将其用作依赖项，
	 * 则应优先使用INTERFACES或TARGET_CLASS代理模式。
	 */
	NO,

	/**
	 * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
	 * the class of the target object.
	 * 
	 * <p> 创建一个JDK动态代理，实现目标对象的类所公开的所有接口。
	 */
	INTERFACES,

	/**
	 * Create a class-based proxy (requires CGLIB).
	 * 
	 * <p> 创建基于类的代理（需要CGLIB）。
	 */
	TARGET_CLASS

}
