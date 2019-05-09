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

package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ObjectFactory;

/**
 * Strategy interface used by a {@link ConfigurableBeanFactory},
 * representing a target scope to hold bean instances in.
 * This allows for extending the BeanFactory's standard scopes
 * {@link ConfigurableBeanFactory#SCOPE_SINGLETON "singleton"} and
 * {@link ConfigurableBeanFactory#SCOPE_PROTOTYPE "prototype"}
 * with custom further scopes, registered for a
 * {@link ConfigurableBeanFactory#registerScope(String, Scope) specific key}.
 * 
 * <p>ConfigurableBeanFactory使用的策略接口，表示保存bean实例的目标作用域。这允许使用为特定键注册的自定义其他作用域扩
 * 展BeanFactory的标准作用域“singleton”和“prototype”。
 *
 * <p>{@link org.springframework.context.ApplicationContext} implementations
 * such as a {@link org.springframework.web.context.WebApplicationContext}
 * may register additional standard scopes specific to their environment,
 * e.g. {@link org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST "request"}
 * and {@link org.springframework.web.context.WebApplicationContext#SCOPE_SESSION "session"},
 * based on this Scope SPI.
 * 
 * <p>org.springframework.context.ApplicationContext实现
 * （例如org.springframework.web.context.WebApplicationContext）
 * 可以注册特定于其环境的其他标准范围，例如， “请求”和“会话”，基于此Scope SPI。
 *
 * <p>Even if its primary use is for extended scopes in a web environment,
 * this SPI is completely generic: It provides the ability to get and put
 * objects from any underlying storage mechanism, such as an HTTP session
 * or a custom conversation mechanism. The name passed into this class's
 * {@code get} and {@code remove} methods will identify the
 * target object in the current scope.
 * 
 * <p>即使它主要用于Web环境中的扩展作用域，这个SPI也是完全通用的：它提供了从任何底层存储机制获取和放置对象的能力，
 * 例如HTTP会话或自定义会话机制。传递给此类的get和remove方法的名称将标识当前范围中的目标对象。
 *
 * <p>{@code Scope} implementations are expected to be thread-safe.
 * One {@code Scope} instance can be used with multiple bean factories
 * at the same time, if desired (unless it explicitly wants to be aware of
 * the containing BeanFactory), with any number of threads accessing
 * the {@code Scope} concurrently from any number of factories.
 * 
 * <p>范围实现应该是线程安全的。如果需要，一个Scope实例可以同时与多个bean工厂一起使用（除非它明
 * 确地想要知道包含BeanFactory），任意数量的线程可以从任意数量的工厂同时访问Scope。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 * @see CustomScopeConfigurer
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 * @see org.springframework.web.context.request.RequestScope
 * @see org.springframework.web.context.request.SessionScope
 */
public interface Scope {

	/**
	 * Return the object with the given name from the underlying scope,
	 * {@link org.springframework.beans.factory.ObjectFactory#getObject() creating it}
	 * if not found in the underlying storage mechanism.
	 * 
	 * <p>从基础范围返回具有给定名称的对象，如果在底层存储机制中找不到则创建它。
	 * 
	 * <p>This is the central operation of a Scope, and the only operation
	 * that is absolutely required.
	 * 
	 * <p>这是Scope的核心操作，也是绝对必需的唯一操作。
	 * 
	 * @param name the name of the object to retrieve - 要检索的对象的名称
	 * @param objectFactory the {@link ObjectFactory} to use to create the scoped
	 * object if it is not present in the underlying storage mechanism
	 * 
	 * <p>ObjectFactory用于创建作用域对象（如果它不存在于底层存储机制中）
	 * 
	 * @return the desired object (never {@code null}) - 所需的对象（永不为null）
	 */
	Object get(String name, ObjectFactory<?> objectFactory);

	/**
	 * Remove the object with the given {@code name} from the underlying scope.
	 * 
	 * <p>从基础范围中删除具有给定名称的对象。
	 * 
	 * <p>Returns {@code null} if no object was found; otherwise
	 * returns the removed {@code Object}.
	 * 
	 * <p>如果没有找到对象，则返回null; 否则返回已删除的Object。
	 * 
	 * <p>Note that an implementation should also remove a registered destruction
	 * callback for the specified object, if any. It does, however, <i>not</i>
	 * need to <i>execute</i> a registered destruction callback in this case,
	 * since the object will be destroyed by the caller (if appropriate).
	 * 
	 * <p>请注意，实现还应删除指定对象的已注册销毁回调（如果有）。 但是，在这种情况下，它确实不需要执行已注册的销毁回调，
	 * 因为对象将被调用者销毁（如果适用）。
	 * 
	 * <p><b>Note: This is an optional operation.</b> Implementations may throw
	 * {@link UnsupportedOperationException} if they do not support explicitly
	 * removing an object.
	 * 
	 * <p>注意：这是可选操作。 如果实现不支持显式删除对象，则可能会抛出UnsupportedOperationException。
	 * 
	 * @param name the name of the object to remove - 要删除的对象的名称
	 * @return the removed object, or {@code null} if no object was present - 已删除的对象，如果没有对象，则返回null
	 * @see #registerDestructionCallback
	 */
	Object remove(String name);

	/**
	 * Register a callback to be executed on destruction of the specified
	 * object in the scope (or at destruction of the entire scope, if the
	 * scope does not destroy individual objects but rather only terminates
	 * in its entirety).
	 * 
	 * <p>注册要在销毁范围内的指定对象时执行的回调（或者在破坏整个范围时，如果范围不破坏单个对象，而只是完全终止）。
	 * 
	 * <p><b>Note: This is an optional operation.</b> This method will only
	 * be called for scoped beans with actual destruction configuration
	 * (DisposableBean, destroy-method, DestructionAwareBeanPostProcessor).
	 * Implementations should do their best to execute a given callback
	 * at the appropriate time. If such a callback is not supported by the
	 * underlying runtime environment at all, the callback <i>must be
	 * ignored and a corresponding warning should be logged</i>.
	 * 
	 * <p>注意：这是可选操作。只有具有实际销毁配置
	 * 的scoped bean（DisposableBean，destroy-method，DestructionAwareBeanPostProcessor）才会调用此方法。
	 * 实现应该尽力在适当的时间执行给定的回调。如果底层运行时环境根本不支持这样的回调，则必须忽略回调并记录相应的警告。
	 * 
	 * <p>Note that 'destruction' refers to to automatic destruction of
	 * the object as part of the scope's own lifecycle, not to the individual
	 * scoped object having been explicitly removed by the application.
	 * If a scoped object gets removed via this facade's {@link #remove(String)}
	 * method, any registered destruction callback should be removed as well,
	 * assuming that the removed object will be reused or manually destroyed.
	 * 
	 * <p>请注意，“销毁”是指作为作用域自身生命周期的一部分自动销毁对象，而不是应用程序已明确删除的单个作用域对象。
	 * 如果通过此facade的remove（String）方法删除了作用域对象，则应删除任何已注册的销毁回调，假设删除的对象将被重用或手动销毁。
	 * 
	 * @param name the name of the object to execute the destruction callback for - 命名要执行销毁回调的对象的名称
	 * @param callback the destruction callback to be executed.
	 * Note that the passed-in Runnable will never throw an exception,
	 * so it can safely be executed without an enclosing try-catch block.
	 * Furthermore, the Runnable will usually be serializable, provided
	 * that its target object is serializable as well.
	 * 
	 * <p>要执行的销毁回调。 请注意，传入的Runnable永远不会抛出异常，因此可以安全地执行它而无需封闭的try-catch块。 
	 * 此外，Runnable通常是可序列化的，前提是它的目标对象也是可序列化的。
	 * 
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getDestroyMethodName()
	 * @see DestructionAwareBeanPostProcessor
	 */
	void registerDestructionCallback(String name, Runnable callback);

	/**
	 * Resolve the contextual object for the given key, if any.
	 * E.g. the HttpServletRequest object for key "request".
	 * 
	 * <p>解析给定键的上下文对象（如果有）。 例如。 键“request”的HttpServletRequest对象。
	 * 
	 * @param key the contextual key - 上下文密钥
	 * @return the corresponding object, or {@code null} if none found - 相应的对象，如果没有找到则为null
	 */
	Object resolveContextualObject(String key);

	/**
	 * Return the <em>conversation ID</em> for the current underlying scope, if any.
	 * 
	 * <p>返回当前基础范围的对话ID（如果有）。
	 * 
	 * <p>The exact meaning of the conversation ID depends on the underlying
	 * storage mechanism. In the case of session-scoped objects, the
	 * conversation ID would typically be equal to (or derived from) the
	 * {@link javax.servlet.http.HttpSession#getId() session ID}; in the
	 * case of a custom conversation that sits within the overall session,
	 * the specific ID for the current conversation would be appropriate.
	 * 
	 * <p>会话ID的确切含义取决于底层存储机制。 在会话范围的对象的情况下，会话ID通常等于（或从其导出）会话ID; 
	 * 对于位于整个会话中的自定义对话，当前对话的特定ID将是合适的。
	 * 
	 * <p><b>Note: This is an optional operation.</b> It is perfectly valid to
	 * return {@code null} in an implementation of this method if the
	 * underlying storage mechanism has no obvious candidate for such an ID.
	 * 
	 * <p>注意：这是可选操作。 如果底层存储机制没有明显的此类ID候选者，则在此方法的实现中返回null是完全有效的。
	 * 
	 * @return the conversation ID, or {@code null} if there is no
	 * conversation ID for the current scope - 会话ID，如果当前作用域没有会话ID，则为null
	 */
	String getConversationId();

}
