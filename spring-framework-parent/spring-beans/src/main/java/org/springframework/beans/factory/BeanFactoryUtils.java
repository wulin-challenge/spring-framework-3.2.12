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

package org.springframework.beans.factory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Convenience methods operating on bean factories, in particular
 * on the {@link ListableBeanFactory} interface.
 * 
 * <p> 在bean工厂上运行的便捷方法，特别是在ListableBeanFactory接口上。
 *
 * <p>Returns bean counts, bean names or bean instances,
 * taking into account the nesting hierarchy of a bean factory
 * (which the methods defined on the ListableBeanFactory interface don't,
 * in contrast to the methods defined on the BeanFactory interface).
 * 
 * <p> 返回bean计数，bean名称或bean实例，同时考虑bean工厂的嵌套层次结
 * 构（与ListableBeanFactory接口上定义的方法不同，与BeanFactory接口上定义的方法相反）。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2003
 */
public abstract class BeanFactoryUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 * 
	 * <p> 生成的bean名称的分隔符。 如果类名或父名不唯一，则将追加“＃1”，“＃2”等，直到名称变为唯一。
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";


	/**
	 * Return whether the given name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 * 
	 * <p> 返回给定名称是否为工厂取消引用（以工厂取消引用前缀开头）。
	 * 
	 * @param name the name of the bean - bean 的名称
	 * @return whether the given name is a factory dereference
	 * 
	 * <p> 给定名称是否为工厂取消引用
	 * 
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public static boolean isFactoryDereference(String name) {
		return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
	}

	/**
	 * Return the actual bean name, stripping out the factory dereference
	 * prefix (if any, also stripping repeated factory prefixes if found).
	 * 
	 * <p> 返回实际的bean名称，去掉工厂的dereference前缀（如果有的话，还会删除重复的工厂前缀，如果找到的话）。
	 * 
	 * @param name the name of the bean - bean 的名称
	 * @return the transformed name - 改造后的名字
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public static String transformedBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		String beanName = name;
		while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanName;
	}

	/**
	 * Return whether the given name is a bean name which has been generated
	 * by the default naming strategy (containing a "#..." part).
	 * 
	 * <p> 返回给定名称是否是由默认命名策略（包含“＃...”部分）生成的bean名称。
	 * 
	 * @param name the name of the bean - bean的名称
	 * @return whether the given name is a generated bean name
	 * 
	 * <p> 给定名称是否是生成的bean名称
	 * 
	 * @see #GENERATED_BEAN_NAME_SEPARATOR
	 * @see org.springframework.beans.factory.support.BeanDefinitionReaderUtils#generateBeanName
	 * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
	 */
	public static boolean isGeneratedBeanName(String name) {
		return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
	}

	/**
	 * Extract the "raw" bean name from the given (potentially generated) bean name,
	 * excluding any "#..." suffixes which might have been added for uniqueness.
	 * 
	 * <p> 从给定（可能生成的）bean名称中提取“原始”bean名称，不包括可能为唯一性添加的任何“＃...”后缀。
	 * 
	 * @param name the potentially generated bean name - 可能生成的bean名称
	 * @return the raw bean name - 原始bean名称
	 * @see #GENERATED_BEAN_NAME_SEPARATOR
	 */
	public static String originalBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
		return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
	}


	/**
	 * Count all beans in any hierarchy in which this factory participates.
	 * Includes counts of ancestor bean factories.
	 * 
	 * <p> 计算此工厂参与的任何层次结构中的所有bean。 包括祖先豆工厂的数量。
	 * 
	 * <p>Beans that are "overridden" (specified in a descendant factory
	 * with the same name) are only counted once.
	 * 
	 * <p> 被“覆盖”的豆（在具有相同名称的后代工厂中指定）仅计数一次。
	 * 
	 * @param lbf the bean factory  - bean 工厂
	 * @return count of beans including those defined in ancestor factories
	 * 
	 * <p> bean数量，包括祖先工厂中定义的bean数量
	 */
	public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesIncludingAncestors(lbf).length;
	}

	/**
	 * Return all bean names in the factory, including ancestor factories.
	 * 
	 * <p> 返回工厂中的所有bean名称，包括祖先工厂。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @return the array of matching bean names, or an empty array if none
	 * 
	 * <p> 匹配bean名称的数组，如果没有，则为空数组
	 * 
	 * @see #beanNamesForTypeIncludingAncestors
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesForTypeIncludingAncestors(lbf, Object.class);
	}


	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * 
	 * <p> 获取给定类型的所有bean名称，包括在祖先工厂中定义的名称。 在重写bean定义的情况下将返回唯一名称。
	 * 
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * 
	 * <p> 考虑FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。 如果FactoryBean创建的对象不匹配，
	 * 则原始FactoryBean本身将与该类型匹配。
	 * 
	 * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
	 * includes prototypes and FactoryBeans.
	 * 
	 * <p> 此版本的beanNamesForTypeIncludingAncestors自动包含原型和FactoryBeans。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @param type the type that beans must match - bean必须匹配的类型
	 * @return the array of matching bean names, or an empty array if none
	 * 
	 * <p> 匹配bean名称的数组，如果没有，则为空数组
	 */
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type);
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
				List<String> resultList = new ArrayList<String>();
				resultList.addAll(Arrays.asList(result));
				for (String beanName : parentResult) {
					if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
						resultList.add(beanName);
					}
				}
				result = StringUtils.toStringArray(resultList);
			}
		}
		return result;
	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * 
	 * <p> 获取给定类型的所有bean名称，包括在祖先工厂中定义的名称。 在重写bean定义的情况下将返回唯一名称。
	 * 
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "allowEagerInit" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * 
	 * <p> 如果设置了“allowEagerInit”标志，则考虑FactoryBeans创建的对象，这意味着将初始化FactoryBeans。
	 *  如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。
	 *  如果未设置“allowEagerInit”，则仅检查原始FactoryBeans（不需要初始化每个FactoryBean）。
	 *   
	 * @param lbf the bean factory - bean工厂
	 * @param type the class or interface to match, or {@code null} for all concrete beans
	 * 
	 * <p>要匹配的类或接口，或者对所有具体bean都为null
	 * 
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * 
	 * <p> 是否包括原型或范围的bean或仅包括单例（也适用于FactoryBeans）
	 * 
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * 
	 * <p> 是否初始化lazy-init单例和由FactoryBeans创建的对象（或工厂方法使用“factory-bean”引用）进行类型检查。 
	 * 请注意，需要急切地初始化FactoryBeans以确定它们的类型：因此请注意，为此标志传入“true”将初始
	 * 化FactoryBeans和“factory-bean”references.type bean必须匹配的类型
	 * 
	 * @param type the type that beans must match - bean必须匹配的类型
	 * @return the array of matching bean names, or an empty array if none
	 * 
	 * <p> 匹配bean名称的数组，如果没有，则为空数组
	 * 
	 */
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
				List<String> resultList = new ArrayList<String>();
				resultList.addAll(Arrays.asList(result));
				for (String beanName : parentResult) {
					if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
						resultList.add(beanName);
					}
				}
				result = StringUtils.toStringArray(resultList);
			}
		}
		return result;
	}

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The returned Map will only contain beans of this type.
	 * 
	 * <p> 如果当前bean工厂是HierarchicalBeanFactory，则返回给定类型或子类型的所有bean，
	 * 同时拾取在祖先bean工厂中定义的bean。 返回的Map将只包含此类型的bean。
	 * 
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * 
	 * <p> 考虑FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。 
	 * 如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。
	 * 
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * 
	 * <p> 注意：同名的bean将优先于“最低”工厂级别，即这些bean将从找到它们的最低工厂返回，从而在祖先工厂中隐藏相应的bean。
	 *  此功能允许通过在子工厂中明确选择相同的bean名称来“替换”bean; 然后，祖先工厂中的bean将不可见，甚至不能用于类型查找。
	 *  
	 * @param lbf the bean factory - bean工厂
	 * @param type type of bean to match - 要匹配的bean类型
	 * @return the Map of matching bean instances, or an empty Map if none
	 * 
	 * <p> 匹配bean实例的Map，如果没有，则为空Map
	 * 
	 * @throws BeansException if a bean could not be created
	 * 
	 * <p> 如果无法创建bean
	 * 
	 */
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> result = new LinkedHashMap<String, T>(4);
		result.putAll(lbf.getBeansOfType(type));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				Map<String, T> parentResult = beansOfTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
				for (Map.Entry<String, T> entry : parentResult.entrySet()) {
					String beanName = entry.getKey();
					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
						result.put(beanName, entry.getValue());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The returned Map will only contain beans of this type.
	 * 
	 * <p> 如果当前bean工厂是HierarchicalBeanFactory，则返回给定类型或子类型的所有bean，
	 * 同时拾取在祖先bean工厂中定义的bean。返回的Map将只包含此类型的bean。
	 * 
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 * 
	 * <p> 如果设置了“allowEagerInit”标志，则考虑FactoryBeans创建的对象，这意味着将初始化FactoryBeans。
	 * 如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。
	 * 如果未设置“allowEagerInit”，则仅检查原始FactoryBeans（不需要初始化每个FactoryBean）。
	 * 
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * 
	 * <p> 注意：同名的豆将优先于“最低”工厂级别，即这些bean将从找到它们的最低工厂返回，从而在祖先工厂中隐藏相应的bean。
	 * 此功能允许通过在子工厂中明确选择相同的bean名称来“替换”bean;然后，祖先工厂中的bean将不可见，甚至不能用于类型查找。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @param type type of bean to match - 要匹配的bean类型
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * 
	 * <p> 是否包括原型或范围的bean或仅包括单例（也适用于FactoryBeans）
	 * 
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * 
	 * <p> 是否初始化lazy-init单例和由FactoryBeans创建的对象（或工厂方法使用“factory-bean”引用）
	 * 进行类型检查。 请注意，需要急切地初始化FactoryBeans以确定其类型：因此请注意，为此标志传入“true”将初
	 * 始化FactoryBeans和“factory-bean”引用。
	 * 
	 * @return the Map of matching bean instances, or an empty Map if none
	 * 
	 * <p> 匹配bean实例的Map，如果没有，则为空Map
	 * 
	 * @throws BeansException if a bean could not be created - 如果无法创建bean
	 */
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> result = new LinkedHashMap<String, T>(4);
		result.putAll(lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit));
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				Map<String, T> parentResult = beansOfTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
				for (Map.Entry<String, T> entry : parentResult.entrySet()) {
					String beanName = entry.getKey();
					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
						result.put(beanName, entry.getValue());
					}
				}
			}
		}
		return result;
	}


	/**
	 * Return a single bean of the given type or subtypes, also picking up beans
	 * defined in ancestor bean factories if the current bean factory is a
	 * HierarchicalBeanFactory. Useful convenience method when we expect a
	 * single bean and don't care about the bean name.
	 * 
	 * <p> 如果当前bean工厂是HierarchicalBeanFactory，则返回给定类型或子类型的单个bean，
	 * 同时拾取在祖先bean工厂中定义的bean。 当我们期望单个bean并且不关心bean名称时，有用的便利方法。
	 * 
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * 
	 * <p> 考虑FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。 
	 * 如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。
	 * 
	 * <p>This version of {@code beanOfTypeIncludingAncestors} automatically includes
	 * prototypes and FactoryBeans.
	 * 
	 * <p> 此版本的beanOfTypeIncludingAncestors自动包含原型和FactoryBeans。
	 * 
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * 
	 * <p> 注意：同名的bean将优先于“最低”工厂级别，即这些bean将从找到它们的最低工厂返回，
	 * 从而在祖先工厂中隐藏相应的bean。 此功能允许通过在子工厂中明确选择相同的bean名称来“替换”bean; 
	 * 然后，祖先工厂中的bean将不可见，甚至不能用于类型查找。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @param type type of bean to match - 要匹配的bean类型
	 * @return the matching bean instance - 匹配的bean实例
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * 
	 * <p> 如果没有找到给定类型的bean
	 * 
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * 
	 * <p> 如果找到多个给定类型的bean
	 * 
	 * @throws BeansException if the bean could not be created - 如果无法创建bean
	 */
	public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
			throws BeansException {

		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Return a single bean of the given type or subtypes, also picking up beans
	 * defined in ancestor bean factories if the current bean factory is a
	 * HierarchicalBeanFactory. Useful convenience method when we expect a
	 * single bean and don't care about the bean name.
	 * 
	 * <p> 如果当前bean工厂是HierarchicalBeanFactory，则返回给定类型或子类型的单个bean，
	 * 同时拾取在祖先bean工厂中定义的bean。当我们期望单个bean并且不关心bean名称时，有用的便利方法。
	 * 
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 * 
	 * <p> 如果设置了“allowEagerInit”标志，则考虑FactoryBeans创建的对象，这意味着将初始化FactoryBeans。
	 * 如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。如果未设置“allowEagerInit”，
	 * 则仅检查原始FactoryBeans（不需要初始化每个FactoryBean）。
	 * 
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * 
	 * <p> 注意：同名的bean将优先于“最低”工厂级别，即这些bean将从找到它们的最低工厂返回，从而在祖先工厂中隐藏相应的bean。
	 * 此功能允许通过在子工厂中明确选择相同的bean名称来“替换”bean;然后，祖先工厂中的bean将不可见，甚至不能用于类型查找。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @param type type of bean to match - 要匹配的bean类型
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * 
	 * <p> 是否包括原型或范围的bean或仅包括单例（也适用于FactoryBeans）
	 * 
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * 
	 * <p> 是否初始化lazy-init单例和由FactoryBeans创建的对象（或工厂方法使用“factory-bean”引用）进行类型检查。 
	 * 请注意，需要急切地初始化FactoryBeans以确定其类型：因此请注意，
	 * 为此标志传入“true”将初始化FactoryBeans和“factory-bean”引用。
	 * 
	 * @return the matching bean instance - 匹配的bean实例
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * 
	 * <p> 如果没有找到给定类型的bean
	 * 
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * 
	 * <p> 如果找到多个给定类型的bean
	 * 
	 * @throws BeansException if the bean could not be created
	 * 
	 * <p> 如果无法创建bean
	 */
	public static <T> T beanOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor
	 * factories. Useful convenience method when we expect a single bean and
	 * don't care about the bean name.
	 * 
	 * <p> 返回给定类型或子类型的单个bean，而不是查看祖先工厂。 当我们期望单个bean并且不关心bean名称时，有用的便利方法。
	 * 
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * 
	 * <p> 考虑FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。 
	 * 如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。
	 * 
	 * <p>This version of {@code beanOfType} automatically includes
	 * prototypes and FactoryBeans.
	 * 
	 * <p> 此版本的beanOfType自动包含原型和FactoryBeans。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @param type type of bean to match - 要匹配的bean类型
	 * @return the matching bean instance - 匹配的bean实例
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * 
	 * <p> 如果没有找到给定类型的bean
	 * 
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * 
	 * <p> 如果找到多个给定类型的bean
	 * 
	 * @throws BeansException if the bean could not be created
	 * 
	 * <p> 如果无法创建bean
	 * 
	 */
	public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) throws BeansException {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> beansOfType = lbf.getBeansOfType(type);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor
	 * factories. Useful convenience method when we expect a single bean and
	 * don't care about the bean name.
	 * 
	 * <p> 返回给定类型或子类型的单个bean，而不是查看祖先工厂。 当我们期望单个bean并且不关心bean名称时，有用的便利方法。
	 * 
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "allowEagerInit" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * 
	 * <p> 如果设置了“allowEagerInit”标志，则考虑FactoryBeans创建的对象，这意味着将初始化FactoryBeans。 
	 * 如果FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。 如果未设置“allowEagerInit”，
	 * 则仅检查原始FactoryBeans（不需要初始化每个FactoryBean）。
	 * 
	 * @param lbf the bean factory - bean工厂
	 * @param type type of bean to match - 要匹配的bean类型
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * 
	 * <p> 是否包括原型或范围的bean或仅包括单例（也适用于FactoryBeans）
	 * 
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * 
	 * <p> 是否初始化lazy-init单例和由FactoryBeans创建的对象（或工厂方法使用“factory-bean”引用）进行类型检查。 
	 * 请注意，需要急切地初始化FactoryBeans以确定其类型：因此请注意，为此标志传入“true”将初始化FactoryBeans和“factory-bean”引用。
	 * 
	 * @return the matching bean instance - 匹配的bean实例
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * 
	 * <p> 如果没有找到给定类型的bean
	 * 
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * 
	 * <p> 如果找到多个给定类型的bean
	 * 
	 * @throws BeansException if the bean could not be created
	 * 
	 * <p> 如果无法创建bean
	 * 
	 */
	public static <T> T beanOfType(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> beansOfType = lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Extract a unique bean for the given type from the given Map of matching beans.
	 * 
	 * <p> 从给定的匹配bean Map中提取给定类型的唯一bean。
	 * 
	 * @param type type of bean to match - 要匹配的bean类型
	 * @param matchingBeans all matching beans found - 找到所有匹配的bean
	 * @return the unique bean instance - 唯一的bean实例
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * 
	 * <p> 如果没有找到给定类型的bean
	 * 
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * 
	 * <p> 如果找到多个给定类型的bean
	 */
	private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
		int nrFound = matchingBeans.size();
		if (nrFound == 1) {
			return matchingBeans.values().iterator().next();
		}
		else if (nrFound > 1) {
			throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
		}
		else {
			throw new NoSuchBeanDefinitionException(type);
		}
	}

}
