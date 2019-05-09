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

package org.springframework.beans.factory;

/**
 * Interface to be implemented by objects used within a {@link BeanFactory}
 * which are themselves factories. If a bean implements this interface,
 * it is used as a factory for an object to expose, not directly as a bean
 * instance that will be exposed itself.
 * 
 * <p> 由BeanFactory中使用的对象实现的接口，这些对象本身就是工厂。
 * 如果bean实现了这个接口，它将被用作公开的对象的工厂，而不是直接作为将自己公开的bean实例。
 *
 * <p><b>NB: A bean that implements this interface cannot be used as a
 * normal bean.</b> A FactoryBean is defined in a bean style, but the
 * object exposed for bean references ({@link #getObject()} is always
 * the object that it creates.
 * 
 * <p> 注意：实现此接口的bean不能用作普通bean。 FactoryBean以bean样式定义，
 * 但为bean引用公开的对象（getObject（）始终是它创建的对象。
 *
 * <p>FactoryBeans can support singletons and prototypes, and can
 * either create objects lazily on demand or eagerly on startup.
 * The {@link SmartFactoryBean} interface allows for exposing
 * more fine-grained behavioral metadata.
 * 
 * <p> FactoryBeans可以支持单例和原型，可以根据需要懒散地创建对象，也可以在启动时急切地创建对象。 
 * SmartFactoryBean接口允许公开更细粒度的行为元数据。
 *
 * <p>This interface is heavily used within the framework itself, for
 * example for the AOP {@link org.springframework.aop.framework.ProxyFactoryBean}
 * or the {@link org.springframework.jndi.JndiObjectFactoryBean}.
 * It can be used for application components as well; however,
 * this is not common outside of infrastructure code.
 * 
 * <p> 该接口在框架内部大量使用，例如用
 * 于AOP org.springframework.aop.framework.ProxyFactoryBean
 * 或org.springframework.jndi.JndiObjectFactoryBean。
 * 它也可以用于应用程序组件;但是，这在基础设施代码之外并不常见。
 *
 * <p><b>NOTE:</b> FactoryBean objects participate in the containing
 * BeanFactory's synchronization of bean creation. There is usually no
 * need for internal synchronization other than for purposes of lazy
 * initialization within the FactoryBean itself (or the like).
 * 
 * <p> 注意：FactoryBean对象参与包含BeanFactory的bean创建同步。
 * 除了FactoryBean本身（或类似）中的延迟初始化之外，通常不需要内部同步。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 08.03.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public interface FactoryBean<T> {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * 
	 * <p> 返回此工厂管理的对象的实例（可能是共享的或独立的）。
	 * 
	 * <p>As with a {@link BeanFactory}, this allows support for both the
	 * Singleton and Prototype design pattern.
	 * 
	 * <p> 与BeanFactory一样，这允许支持Singleton和Prototype设计模式。
	 * 
	 * <p>If this FactoryBean is not fully initialized yet at the time of
	 * the call (for example because it is involved in a circular reference),
	 * throw a corresponding {@link FactoryBeanNotInitializedException}.
	 * 
	 * <p> 如果在调用时尚未完全初始化此FactoryBean（例如，因为它涉及循环引用），
	 * 则抛出相应的FactoryBeanNotInitializedException。
	 * 
	 * <p>As of Spring 2.0, FactoryBeans are allowed to return {@code null}
	 * objects. The factory will consider this as normal value to be used; it
	 * will not throw a FactoryBeanNotInitializedException in this case anymore.
	 * FactoryBean implementations are encouraged to throw
	 * FactoryBeanNotInitializedException themselves now, as appropriate.
	 * 
	 * <p> 从Spring 2.0开始，允许FactoryBeans返回null对象。 工厂会将此视为正常使用价值; 
	 * 在这种情况下，它不再抛出FactoryBeanNotInitializedException。 
	 * 鼓励FactoryBean实现现在自己抛出FactoryBeanNotInitializedException，视情况而定。
	 * 
	 * @return an instance of the bean (can be {@code null}) - bean的一个实例（可以为null）
	 * @throws Exception in case of creation errors - 如果出现创建错误
	 * @see FactoryBeanNotInitializedException
	 */
	T getObject() throws Exception;

	/**
	 * Return the type of object that this FactoryBean creates,
	 * or {@code null} if not known in advance.
	 * 
	 * <p> 返回此FactoryBean创建的对象类型，如果事先不知道则返回null。
	 * 
	 * <p>This allows one to check for specific types of beans without
	 * instantiating objects, for example on autowiring.
	 * 
	 * <p> 这允许人们在不实例化对象的情况下检查特定类型的bean，例如在自动装配时。
	 * 
	 * <p>In the case of implementations that are creating a singleton object,
	 * this method should try to avoid singleton creation as far as possible;
	 * it should rather estimate the type in advance.
	 * For prototypes, returning a meaningful type here is advisable too.
	 * 
	 * <p> 在创建单个对象的实现的情况下，此方法应尽量避免单例创建; 它应该提前估计类型。 
	 * 对于原型，在这里返回有意义的类型也是可取的。
	 * 
	 * <p>This method can be called <i>before</i> this FactoryBean has
	 * been fully initialized. It must not rely on state created during
	 * initialization; of course, it can still use such state if available.
	 * 
	 * <p> 可以在完全初始化此FactoryBean之前调用此方法。 它不能依赖于初始化期间创建的状态; 
	 * 当然，如果可以的话，它仍然可以使用这种状态。
	 * 
	 * <p><b>NOTE:</b> Autowiring will simply ignore FactoryBeans that return
	 * {@code null} here. Therefore it is highly recommended to implement
	 * this method properly, using the current state of the FactoryBean.
	 * 
	 * <p> 注意：自动装配将简单地忽略在此处返回null的FactoryBeans。 
	 * 因此，强烈建议使用FactoryBean的当前状态正确实现此方法。
	 * 
	 * @return the type of object that this FactoryBean creates,
	 * or {@code null} if not known at the time of the call
	 * 
	 * <p> 此FactoryBean创建的对象类型，如果在调用时未知，则为null
	 * 
	 * @see ListableBeanFactory#getBeansOfType
	 */
	Class<?> getObjectType();

	/**
	 * Is the object managed by this factory a singleton? That is,
	 * will {@link #getObject()} always return the same object
	 * (a reference that can be cached)?
	 * 
	 * <p> 这个工厂管理的对象是单件吗？ 也就是说，getObject（）总是会返回相同的对象（可以缓存的引用）吗？
	 * 
	 * <p><b>NOTE:</b> If a FactoryBean indicates to hold a singleton object,
	 * the object returned from {@code getObject()} might get cached
	 * by the owning BeanFactory. Hence, do not return {@code true}
	 * unless the FactoryBean always exposes the same reference.
	 * 
	 * <p> 注意：如果FactoryBean指示持有单个对象，则从getObject（）返回的对象可能会由拥有BeanFactory缓存。 
	 * 因此，除非FactoryBean始终公开相同的引用，否则不返回true。
	 * 
	 * <p>The singleton status of the FactoryBean itself will generally
	 * be provided by the owning BeanFactory; usually, it has to be
	 * defined as singleton there.
	 * 
	 * <p> FactoryBean本身的单例状态通常由拥有BeanFactory提供; 通常，它必须被定义为单身。
	 * 
	 * <p><b>NOTE:</b> This method returning {@code false} does not
	 * necessarily indicate that returned objects are independent instances.
	 * An implementation of the extended {@link SmartFactoryBean} interface
	 * may explicitly indicate independent instances through its
	 * {@link SmartFactoryBean#isPrototype()} method. Plain {@link FactoryBean}
	 * implementations which do not implement this extended interface are
	 * simply assumed to always return independent instances if the
	 * {@code isSingleton()} implementation returns {@code false}.
	 * 
	 * <p> 注意：此方法返回false并不一定表示返回的对象是独立实例。 扩展SmartFactoryBean接口的
	 * 实现可以通过其SmartFactoryBean.isPrototype（）方法显式指示独立实例。 
	 * 如果isSingleton（）实现返回false，则假定不实现此扩展接口的Plain FactoryBean实现始终返回独立实例。
	 * 
	 * @return whether the exposed object is a singleton - 暴露的对象是否是单例
	 * @see #getObject()
	 * @see SmartFactoryBean#isPrototype()
	 */
	boolean isSingleton();

}
