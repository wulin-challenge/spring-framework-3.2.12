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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;

/**
 * Extension of the {@link BeanFactory} interface to be implemented by bean factories
 * that can enumerate all their bean instances, rather than attempting bean lookup
 * by name one by one as requested by clients. BeanFactory implementations that
 * preload all their bean definitions (such as XML-based factories) may implement
 * this interface.
 * 
 * <p>BeanFactory接口的扩展由bean工厂实现，可以枚举所有bean实例，而不是按客户端的请求逐个按名称查找bean。预加载所
 * 有bean定义（例如基于XML的工厂）的BeanFactory实现可以实现此接口。
 * 
 *
 * <p>If this is a {@link HierarchicalBeanFactory}, the return values will <i>not</i>
 * take any BeanFactory hierarchy into account, but will relate only to the beans
 * defined in the current factory. Use the {@link BeanFactoryUtils} helper class
 * to consider beans in ancestor factories too.
 * 
 * <p>如果这是HierarchicalBeanFactory，则返回值不会考虑任何BeanFactory层次结构，而只会涉及当前工厂中定
 * 义的bean。使用BeanFactoryUtils帮助程序类来考虑祖先工厂中的bean。
 * 
 *
 * <p>The methods in this interface will just respect bean definitions of this factory.
 * They will ignore any singleton beans that have been registered by other means like
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}'s
 * {@code registerSingleton} method, with the exception of
 * {@code getBeanNamesOfType} and {@code getBeansOfType} which will check
 * such manually registered singletons too. Of course, BeanFactory's {@code getBean}
 * does allow transparent access to such special beans as well. However, in typical
 * scenarios, all beans will be defined by external bean definitions anyway, so most
 * applications don't need to worry about this differentation.
 * 
 * <p>此接口中的方法将仅考虑此工厂的bean定义。他们将忽略任何已通过其他方式注册的单例bean，
 * 如org.springframework.beans.factory.config.ConfigurableBeanFactory的registerSingleton方法，
 * 但getBeanNamesOfType和getBeansOfType除外，它将检查此类手动注册的单例。当然，BeanFactory的getBean也允许透
 * 明访问这些特殊的bean。但是，在典型的场景中，所有bean都将由外部bean定义定义，因此大多数应用程序不需要担心这种差异。
 * 
 *
 * <p><b>NOTE:</b> With the exception of {@code getBeanDefinitionCount}
 * and {@code containsBeanDefinition}, the methods in this interface
 * are not designed for frequent invocation. Implementations may be slow.
 * 
 * <p>注意：除了getBeanDefinitionCount和containsBeanDefinition之外，此接口中的方法不是为频繁调用而设计的。实施可能很慢。
 * 
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * 
	 * <p>检查此bean工厂是否包含具有给定名称的bean定义。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in,
	 * and ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构，并忽略已通过bean定义以外的其他方式注册的任何单例bean。
	 * 
	 * @param beanName the name of the bean to look for - 要查找的bean的名称
	 * @return if this bean factory contains a bean definition with the given name
	 * 
	 * <p>如果此bean工厂包含具有给定名称的bean定义
	 * 
	 * @see #containsBean
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * Return the number of beans defined in the factory.
	 * 
	 * <p>返回工厂中定义的bean数。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in,
	 * and ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构，并忽略已通过bean定义以外的其他方式注册的任何单例bean。
	 * 
	 * @return the number of beans defined in the factory - 工厂中定义的bean数量
	 */
	int getBeanDefinitionCount();

	/**
	 * Return the names of all beans defined in this factory. 
	 * 
	 * <p>返回此工厂中定义的所有bean的名称。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in,
	 * and ignores any singleton beans that have been registered by
	 * other means than bean definitions.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构，并忽略已通过bean定义以外的其他方式注册的任何单例bean。
	 * 
	 * @return the names of all beans defined in this factory,
	 * or an empty array if none defined
	 * 
	 * <p>此工厂中定义的所有bean的名称，如果没有定义，则为空数组
	 * 
	 */
	String[] getBeanDefinitionNames();

	/**
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of {@code getObjectType}
	 * in the case of FactoryBeans.
	 * 
	 * <p>返回与给定类型（包括子类）匹配的bean的名称，从bean定义或FactoryBeans的getObjectType值判断。
	 * 
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 * 
	 * <p>注意：此方法仅对顶级bean进行内省。它不会检查可能与指定类型匹配的嵌套bean。
	 * 
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * 
	 * <p>考虑FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，则原始FactoryBean本
	 * 身将与该类型匹配。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beanNamesForTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构。使用BeanFactoryUtils的beanNamesForTypeIncludingAncestors也包括祖先工厂中的bean。
	 * 
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * 
	 * <p>注意：不要忽略通过bean定义之外的其他方式注册的单例bean。
	 * 
	 * <p>This version of {@code getBeanNamesForType} matches all kinds of beans,
	 * be it singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for {@code getBeanNamesOfType(type, true, true)}.
	 * 
	 * <p>此版本的getBeanNamesForType匹配所有类型的bean，无论是单例，原型还是FactoryBeans。在大多数实现中，结
	 * 果与getBeanNamesOfType（type，true，true）的结果相同。
	 * 
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 * 
	 * <p>此方法返回的Bean名称应始终尽可能按后端配置中的定义顺序返回bean名称。
	 * 
	 * @param type the class or interface to match, or {@code null} for all bean names
	 * 
	 * <p>要匹配的类或接口，或者对所有bean名称都为null
	 * 
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none
	 * 
	 * <p>与给定对象类型（包括子类）匹配的bean（或FactoryBeans创建的对象）的名称，如果没有，则为空数组
	 * 
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	String[] getBeanNamesForType(Class<?> type);

	/**
	 * Return the names of beans matching the given type (including subclasses),
	 * judging from either bean definitions or the value of {@code getObjectType}
	 * in the case of FactoryBeans.
	 * 
	 * <p>返回与给定类型（包括子类）匹配的bean的名称，从bean定义或FactoryBeans的getObjectType值判断。
	 * 
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 * 
	 * <p>注意：此方法仅对顶级bean进行内省。它不会检查可能与指定类型匹配的嵌套bean。
	 * 
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 * 
	 * <p>如果设置了“allowEagerInit”标志，则考虑FactoryBeans创建的对象，这意味着将初始化FactoryBeans。如
	 * 果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。如果未设置“allowEagerInit”，则仅检
	 * 查原始FactoryBeans（不需要初始化每个FactoryBean）。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beanNamesForTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构。使用BeanFactoryUtils的beanNamesForTypeIncludingAncestors也包括祖先工
	 * 厂中的bean。
	 * 
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * 
	 * <p>注意：不要忽略通过bean定义之外的其他方式注册的单例bean。
	 * 
	 * <p>Bean names returned by this method should always return bean names <i>in the
	 * order of definition</i> in the backend configuration, as far as possible.
	 * 
	 * <p>此方法返回的Bean名称应始终尽可能按后端配置中的定义顺序返回bean名称。
	 * 
	 * @param type the class or interface to match, or {@code null} for all bean names
	 * 
	 * <p>要匹配的类或接口，或者对所有bean名称都为null
	 * 
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * 
	 * <p>是否包括原型或范围的bean或仅包括单例（也适用于FactoryBeans）
	 * 
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * 
	 * <p>是否初始化lazy-init单例和由FactoryBeans创建的对象（或工厂方法使用“factory-bean”引用）进行类型检
	 * 查。 请注意，需要急切地初始化FactoryBeans以确定其类型：因此请注意，为此标志传入“true”将初
	 * 始化FactoryBeans和“factory-bean”引用。
	 * 
	 * @return the names of beans (or objects created by FactoryBeans) matching
	 * the given object type (including subclasses), or an empty array if none
	 * 
	 * <p>与给定对象类型（包括子类）匹配的bean（或FactoryBeans创建的对象）的名称，如果没有，则为空数组
	 * 
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * {@code getObjectType} in the case of FactoryBeans.
	 * 
	 * <p>返回与给定对象类型（包括子类）匹配的bean实例，从bean定义或FactoryBeans情况下的getObjectType值判断。
	 * 
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 * 
	 * <p>注意：此方法仅对顶级bean进行内省。它不会检查可能与指定类型匹配的嵌套bean。
	 * 
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * 
	 * <p>考虑FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，则原
	 * 始FactoryBean本身将与该类型匹配。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beansOfTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构。使用BeanFactoryUtils的beansOfTypeIncludingAncestors也可
	 * 以在祖先工厂中包含bean。
	 * 
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * 
	 * <p>注意：不要忽略通过bean定义之外的其他方式注册的单例bean。
	 * 
	 * <p>This version of getBeansOfType matches all kinds of beans, be it
	 * singletons, prototypes, or FactoryBeans. In most implementations, the
	 * result will be the same as for {@code getBeansOfType(type, true, true)}.
	 * 
	 * <p>此版本的getBeansOfType匹配所有类型的bean，无论是单例，原型还是FactoryBeans。在大多数实现中，结果
	 * 与getBeansOfType（type，true，true）的结果相同。
	 * 
	 * <p>The Map returned by this method should always return bean names and
	 * corresponding bean instances <i>in the order of definition</i> in the
	 * backend configuration, as far as possible.
	 * 
	 * <p>此方法返回的Map应始终尽可能按后端配置中的定义顺序返回bean名称和相应的bean实例。
	 * 
	 * @param type the class or interface to match, or {@code null} for all concrete beans
	 * 
	 * <p>要匹配的类或接口，或者对所有具体bean都为null
	 * 
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * 
	 * <p>带有匹配bean的Map，包含bean名称作为键，相应的bean实例作为值
	 * 
	 * @throws BeansException if a bean could not be created - 如果无法创建bean
	 * @since 1.1.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * {@code getObjectType} in the case of FactoryBeans.
	 * 
	 * <p>返回与给定对象类型（包括子类）匹配的bean实例，从bean定义或FactoryBeans情况下的getObjectType值判断。
	 * 
	 * <p><b>NOTE: This method introspects top-level beans only.</b> It does <i>not</i>
	 * check nested beans which might match the specified type as well.
	 * 
	 * <p>注意：此方法仅对顶级bean进行内省。它不会检查可能与指定类型匹配的嵌套bean。
	 * 
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 * 
	 * <p>如果设置了“allowEagerInit”标志，则考虑FactoryBeans创建的对象，这意味着将初始化FactoryBeans。如
	 * 果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。如果未设置“allowEagerInit”，则仅检
	 * 查原始FactoryBeans（不需要初始化每个FactoryBean）。
	 * 
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * Use BeanFactoryUtils' {@code beansOfTypeIncludingAncestors}
	 * to include beans in ancestor factories too.
	 * 
	 * <p>不考虑此工厂可能参与的任何层次结构。使用BeanFactoryUtils的beansOfTypeIncludingAncestors也可以在祖先工厂中包含bean。
	 * 
	 * <p>Note: Does <i>not</i> ignore singleton beans that have been registered
	 * by other means than bean definitions.
	 * 
	 * <p>注意：不要忽略通过bean定义之外的其他方式注册的单例bean。
	 * 
	 * <p>The Map returned by this method should always return bean names and
	 * corresponding bean instances <i>in the order of definition</i> in the
	 * backend configuration, as far as possible.
	 * 
	 * <p>此方法返回的Map应始终尽可能按后端配置中的定义顺序返回bean名称和相应的bean实例。
	 * 
	 * @param type the class or interface to match, or {@code null} for all concrete beans
	 * 
	 * <p>要匹配的类或接口，或者对所有具体bean都为null
	 * 
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * 
	 * <p>是否包括原型或范围的bean或仅包括单例（也适用于FactoryBeans）
	 * 
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * 
	 * <p>是否初始化lazy-init单例和由FactoryBeans创建的对象（或工厂方法使用“factory-bean”引用）进行类型检查。 
	 * 请注意，需要急切地初始化FactoryBeans以确定其类型：因此请注意，为此标志传入“true”将初始
	 * 化FactoryBeans和“factory-bean”引用。
	 * 
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * 
	 * <p>带有匹配bean的Map，包含bean名称作为键，相应的bean实例作为值
	 * 
	 * @throws BeansException if a bean could not be created - 如果无法创建bean
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	<T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	/**
	 * Find all beans whose {@code Class} has the supplied {@link java.lang.annotation.Annotation} type.
	 * 
	 * <p>查找其Class具有提供的java.lang.annotation.Annotation类型的所有bean。
	 * 
	 * @param annotationType the type of annotation to look for - 要查找的注释类型
	 * @return a Map with the matching beans, containing the bean names as
	 * keys and the corresponding bean instances as values
	 * 
	 * <p>带有匹配bean的Map，包含bean名称作为键，相应的bean实例作为值
	 * 
	 * @throws BeansException if a bean could not be created - 如果无法创建bean
	 */
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException;

	/**
	 * Find a {@link Annotation} of {@code annotationType} on the specified
	 * bean, traversing its interfaces and super classes if no annotation can be
	 * found on the given class itself.
	 * 
	 * <p>如果在给定的类本身上找不到注释，则在指定的bean上查找annotationType的注释，遍历其接口和超类。
	 * 
	 * @param beanName the name of the bean to look for annotations on - 要查找注释的bean的名称
	 * @param annotationType the annotation class to look for - 要查找的注释类
	 * @return the annotation of the given type found, or {@code null} - 找到的给定类型的注释，或null
	 */
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType);

}
