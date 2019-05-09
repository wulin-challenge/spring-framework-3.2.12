/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;

/**
 * Default object instantiation strategy for use in BeanFactories.
 * 
 * <p>在BeanFactories中使用的默认对象实例化策略。
 *
 * <p>Uses CGLIB to generate subclasses dynamically if methods need to be
 * overridden by the container to implement <em>Method Injection</em>.
 * 
 * <p>如果容器需要重写方法以实现方法注入，则使用CGLIB动态生成子类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {

	/**
	 * Index in the CGLIB callback array for passthrough behavior,
	 * in which case the subclass won't override the original class.
	 * 
	 * <p>用于直通行为的CGLIB回调数组中的索引，在这种情况下，子类不会覆盖原始类。
	 */
	private static final int PASSTHROUGH = 0;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden to provide <em>method lookup</em>.
	 * 
	 * <p>CGLIB回调数组中的索引，用于应该重写以提供方法查找的方法。
	 */
	private static final int LOOKUP_OVERRIDE = 1;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden using generic <em>method replacer</em> functionality.
	 * 
	 * <p>CGLIB回调数组中的索引，用于应使用通用方法替换器功能覆盖的方法。
	 */
	private static final int METHOD_REPLACER = 2;


	@Override
	protected Object instantiateWithMethodInjection(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {

		// Must generate CGLIB subclass.
		// 必须生成CGLIB子类。
		return new CglibSubclassCreator(beanDefinition, owner).instantiate(null, null);
	}

	@Override
	protected Object instantiateWithMethodInjection(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Constructor<?> ctor, Object[] args) {

		return new CglibSubclassCreator(beanDefinition, owner).instantiate(ctor, args);
	}


	/**
	 * An inner class created for historical reasons to avoid external CGLIB dependency
	 * in Spring versions earlier than 3.2.
	 * 
	 * <p>由于历史原因而创建的内部类，以避免在早于3.2的Spring版本中出现外部CGLIB依赖。
	 * 
	 */
	private static class CglibSubclassCreator {

		private static final Log logger = LogFactory.getLog(CglibSubclassCreator.class);

		private final RootBeanDefinition beanDefinition;

		private final BeanFactory owner;

		public CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
			this.beanDefinition = beanDefinition;
			this.owner = owner;
		}

		/**
		 * Create a new instance of a dynamically generated subclass implementing the
		 * required lookups.
		 * 
		 * <p>创建动态生成的子类的新实例，实现所需的查找。
		 * 
		 * @param ctor constructor to use. If this is {@code null}, use the
		 * no-arg constructor (no parameterization, or Setter Injection)
		 * 
		 * <p>构造函数要使用。 如果为null，则使用no-arg构造函数（无参数化或Setter注入）
		 * 
		 * @param args arguments to use for the constructor.
		 * Ignored if the {@code ctor} parameter is {@code null}.
		 * 
		 * <p>用于构造函数的参数。 如果ctor参数为null，则忽略。
		 * @return new instance of the dynamically generated subclass - 动态生成的子类的新实例
		 */
		public Object instantiate(Constructor<?> ctor, Object[] args) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(this.beanDefinition.getBeanClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setCallbackFilter(new CallbackFilterImpl());
			enhancer.setCallbacks(new Callback[] {
					NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor(),
					new ReplaceOverrideMethodInterceptor()
			});

			return (ctor != null ? enhancer.create(ctor.getParameterTypes(), args) : enhancer.create());
		}


		/**
		 * Class providing hashCode and equals methods required by CGLIB to
		 * ensure that CGLIB doesn't generate a distinct class per bean.
		 * Identity is based on class and bean definition.
		 * 
		 * <p>类提供hashCode和equals CGLIB所需的方法，以确保CGLIB不会为每个bean生成不同的类。 标识基于类和bean定义。
		 */
		private class CglibIdentitySupport {

			/**
			 * Exposed for equals method to allow access to enclosing class field
			 * 
			 * <p>暴露为equals方法以允许访问封闭的类字段
			 */
			protected RootBeanDefinition getBeanDefinition() {
				return beanDefinition;
			}

			@Override
			public boolean equals(Object other) {
				return (other.getClass().equals(getClass()) &&
						((CglibIdentitySupport) other).getBeanDefinition().equals(beanDefinition));
			}

			@Override
			public int hashCode() {
				return beanDefinition.hashCode();
			}
		}


		/**
		 * CGLIB MethodInterceptor to override methods, replacing them with an
		 * implementation that returns a bean looked up in the container.
		 * 
		 * <p>CGLIB MethodInterceptor重写方法，用一个返回容器中查找的bean的实现替换它们。
		 */
		private class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

			public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
				// Cast is safe, as CallbackFilter filters are used selectively.
				// Cast是安全的，因为有选择地使用CallbackFilter过滤器。
				LookupOverride lo = (LookupOverride) beanDefinition.getMethodOverrides().getOverride(method);
				return owner.getBean(lo.getBeanName());
			}
		}


		/**
		 * CGLIB MethodInterceptor to override methods, replacing them with a call
		 * to a generic MethodReplacer.
		 * 
		 * <p>CGLIB MethodInterceptor重写方法，用对通用MethodReplacer的调用替换它们。
		 */
		private class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

			public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
				ReplaceOverride ro = (ReplaceOverride) beanDefinition.getMethodOverrides().getOverride(method);
				// TODO could cache if a singleton for minor performance optimization
				// TODO 如果一个单例用于次要性能优化，则可以缓存
				MethodReplacer mr = (MethodReplacer) owner.getBean(ro.getMethodReplacerBeanName());
				return mr.reimplement(obj, method, args);
			}
		}


		/**
		 * CGLIB object to filter method interception behavior.
		 * 
		 * <p>CGLIB对象用于过滤方法拦截行为。
		 */
		private class CallbackFilterImpl extends CglibIdentitySupport implements CallbackFilter {

			public int accept(Method method) {
				MethodOverride methodOverride = beanDefinition.getMethodOverrides().getOverride(method);
				if (logger.isTraceEnabled()) {
					logger.trace("Override for '" + method.getName() + "' is [" + methodOverride + "]");
				}
				if (methodOverride == null) {
					return PASSTHROUGH;
				}
				else if (methodOverride instanceof LookupOverride) {
					return LOOKUP_OVERRIDE;
				}
				else if (methodOverride instanceof ReplaceOverride) {
					return METHOD_REPLACER;
				}
				throw new UnsupportedOperationException(
						"Unexpected MethodOverride subclass: " + methodOverride.getClass().getName());
			}
		}
	}

}
