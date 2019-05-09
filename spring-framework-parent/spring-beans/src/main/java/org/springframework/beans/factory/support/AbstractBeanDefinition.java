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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Base class for concrete, full-fledged
 * {@link org.springframework.beans.factory.config.BeanDefinition} classes,
 * factoring out common properties of {@link GenericBeanDefinition},
 * {@link RootBeanDefinition} and {@link ChildBeanDefinition}.
 * 
 * <p> 具体的，完整的org.springframework.beans.factory.config.BeanDefinition类的基类，
 * 分解GenericBeanDefinition，RootBeanDefinition和ChildBeanDefinition的公共属性。
 *
 * <p>The autowire constants match the ones defined in the
 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * interface.
 * 
 * <p> autowire常量与org.springframework.beans.factory.config.AutowireCapableBeanFactory接
 * 口中定义的常量匹配。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
		implements BeanDefinition, Cloneable {

	/**
	 * Constant for the default scope name: "", equivalent to singleton status
	 * but to be overridden from a parent bean definition (if applicable).
	 * 
	 * <p> 默认范围名称的常量：“”，等同于单例状态，但要从父bean定义（如果适用）覆盖。
	 */
	public static final String SCOPE_DEFAULT = "";

	/**
	 * Constant that indicates no autowiring at all.
	 * 
	 * <p> 常量，表示根本没有自动装配。
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

	/**
	 * Constant that indicates autowiring bean properties by name.
	 * 
	 * <p> 常量，表示按名称自动装配bean属性。
	 * 
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	/**
	 * Constant that indicates autowiring bean properties by type.
	 * 
	 * <p> 常量，表示按类型自动装配bean属性。
	 * 
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

	/**
	 * Constant that indicates autowiring a constructor.
	 * 
	 * <p> 指示自动装配构造函数的常量。
	 * 
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * 
	 * <p> 常量，表示通过内省bean类确定适当的自动线路策略。
	 * 
	 * @see #setAutowireMode
	 * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
	 * use annotation-based autowiring for clearer demarcation of autowiring needs.
	 * 
	 * <p> 从Spring 3.0开始：如果您使用的是混合自动装配策略，请使用基于注释的自动装配来更清晰地划分自动装配需求。
	 */
	@Deprecated
	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

	/**
	 * Constant that indicates no dependency check at all.
	 * 
	 * <p> 常量，表示根本没有依赖性检查。
	 * 
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_NONE = 0;

	/**
	 * Constant that indicates dependency checking for object references.
	 * 
	 * <p> 常量，表示对象引用的依赖性检查。
	 * 
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_OBJECTS = 1;

	/**
	 * Constant that indicates dependency checking for "simple" properties.
	 * 
	 * <p> 常量，表示对“简单”属性的依赖性检查。
	 * 
	 * @see #setDependencyCheck
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	public static final int DEPENDENCY_CHECK_SIMPLE = 2;

	/**
	 * Constant that indicates dependency checking for all properties
	 * (object references as well as "simple" properties).
	 * 
	 * <p> 指示所有属性（对象引用以及“简单”属性）的依赖性检查的常量。
	 * 
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_ALL = 3;

	/**
	 * Constant that indicates the container should attempt to infer the
	 * {@link #setDestroyMethodName destroy method name} for a bean as opposed to
	 * explicit specification of a method name. The value {@value} is specifically
	 * designed to include characters otherwise illegal in a method name, ensuring
	 * no possibility of collisions with legitimately named methods having the same
	 * name.
	 * 
	 * <p> 指示容器应该尝试推断bean的destroy方法名称而不是显式指定方法名称的常量。 值“（推断）”专门设计为包
	 * 含方法名称中非法的字符，确保不会与具有相同名称的合法命名方法发生冲突。
	 * 
	 * <p>Currently, the method names detected during destroy method inference
	 * are "close" and "shutdown", if present on the specific bean class.
	 * 
	 * <p> 目前，在destroy方法推断期间检测到的方法名称是“close”和“shutdown”，如果存在于特定bean类中。
	 * 
	 */
	public static final String INFER_METHOD = "(inferred)";


	private volatile Object beanClass;

	/**
	 * bean 的作用范围,对应bean属性scope
	 */
	private String scope = SCOPE_DEFAULT;

	/**
	 * 是否是单例,来自bean属性scope
	 */
	private boolean singleton = true;

	/**
	 * 是否是原型,来自bean属性scope
	 */
	private boolean prototype = false;

	/**
	 * 是否是抽象,对应bean属性abstract
	 */
	private boolean abstractFlag = false;

	/**
	 * 是否延迟加载,对应bean属性 lazy-init
	 */
	private boolean lazyInit = false;

	/**
	 * 自动注入模式,对应bean属性autowire
	 */
	private int autowireMode = AUTOWIRE_NO;

	/**
	 * 依赖检查,Spring 3.0后弃用这个属性
	 */
	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	/**
	 * 用来表示一个bean的实例化依赖另一个bean先实例化,对应bean属性depend-on
	 */
	private String[] dependsOn;

	/**
	 * autowire-candidate属性设置为fasle,这样容器在检查自动装配对象时,将不考虑该bean,即它不会被考虑作为其
	 * 他bean自动装配的候选者,但是该bean本身还是可以使用自动装配来注入其他bean的.
	 * 对应bean属性autowire-condidate
	 */
	private boolean autowireCandidate = true;

	/**
	 * 自动装配是当出现多个bean候选者时,将作为首选者,对应bean属性primary
	 */
	private boolean primary = false;

	/**
	 * 用于记录qualifier,对应子元素qualifier
	 */
	private final Map<String, AutowireCandidateQualifier> qualifiers =
			new LinkedHashMap<String, AutowireCandidateQualifier>(0);

	/**
	 * 允许访问非公开的构造器和方法,程序设置
	 */
	private boolean nonPublicAccessAllowed = true;

	/**
	 * 是否以一种宽松的模式解析构造函数,默认为true,如果未false,则在如下情况
	 * interface ITest{}
	 * 
	 * class ITestImple implements ITest{};
	 * 
	 * class Main{
	 * 	Main(ITest i){}
	 *  Main(ITestImp i){}
	 * }
	 * 抛出异常,因为Spring无法准确定位那个构造函数
	 */
	private boolean lenientConstructorResolution = true;

	/**
	 * 记录构造函数注入属性,对应bean属性constructor-arg
	 */
	private ConstructorArgumentValues constructorArgumentValues;

	/**
	 * 普通属性集合
	 */
	private MutablePropertyValues propertyValues;

	/**
	 * 方法重写的持有者,记录lookup-method,replaced-method元素
	 */
	private MethodOverrides methodOverrides = new MethodOverrides();

	/**
	 * 对应bean属性factory-bean,用法
	 * <bean id="instanceFactoryBean" class="example.chapter3.InstanceFactoryBean"/>
	 * <bean id="currentTime" factory-bean="instanceFactoryBean" factory-method="createTime"/>
	 */
	private String factoryBeanName;

	/**
	 * 对应bean属性factory-method
	 */
	private String factoryMethodName;

	/**
	 * 初始化方法,对应bean属性init-method
	 */
	private String initMethodName;

	/**
	 * 销毁方法,对应bean属性destory-method
	 */
	private String destroyMethodName;

	/**
	 * 是否执行init-method,程序设置
	 */
	private boolean enforceInitMethod = true;

	/**
	 * 是否执行destory-method,程序设置
	 */
	private boolean enforceDestroyMethod = true;

	/**
	 * 是否是用户定义的而不是应用程序本身定义的,创建aop时候为true,程序设置
	 */
	private boolean synthetic = false;//合成的

	/**
	 * 定义这个bean的应用,application:用户,infrastructure:完全内部使用,与用户无关,support:某些复杂配置的一部分
	 * 程序设置
	 */
	private int role = BeanDefinition.ROLE_APPLICATION;

	/**
	 * bean 的描述信息
	 */
	private String description;

	/**
	 * 这个bean定义的资源
	 */
	private Resource resource;


	/**
	 * Create a new AbstractBeanDefinition with default settings.
	 * 
	 * <p> 使用默认设置创建新的AbstractBeanDefinition。
	 * 
	 */
	protected AbstractBeanDefinition() {
		this(null, null);
	}

	/**
	 * Create a new AbstractBeanDefinition with the given
	 * constructor argument values and property values.
	 * 
	 * <p> 使用给定的构造函数参数值和属性值创建新的AbstractBeanDefinition。
	 * 
	 */
	protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		setConstructorArgumentValues(cargs);
		setPropertyValues(pvs);
	}

	/**
	 * Create a new AbstractBeanDefinition as a deep copy of the given
	 * bean definition.
	 * 
	 * <p> 创建一个新的AbstractBeanDefinition作为给定bean定义的深层副本。
	 * 
	 * @param original the original bean definition to copy from - 要从中复制的原始bean定义
	 * @deprecated since Spring 2.5, in favor of {@link #AbstractBeanDefinition(BeanDefinition)}
	 * 
	 * <p> 从Spring 2.5开始，支持AbstractBeanDefinition（BeanDefinition）
	 * 
	 */
	@Deprecated
	protected AbstractBeanDefinition(AbstractBeanDefinition original) {
		this((BeanDefinition) original);
	}

	/**
	 * Create a new AbstractBeanDefinition as a deep copy of the given
	 * bean definition.
	 * 
	 * <p> 创建一个新的AbstractBeanDefinition作为给定bean定义的深层副本。
	 * 
	 * @param original the original bean definition to copy from - 要从中复制的原始bean定义
	 */
	protected AbstractBeanDefinition(BeanDefinition original) {
		setParentName(original.getParentName());
		setBeanClassName(original.getBeanClassName());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setLazyInit(original.isLazyInit());
		setRole(original.getRole());
		setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
		setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
		setSource(original.getSource());
		copyAttributesFrom(original);

		if (original instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			if (originalAbd.hasBeanClass()) {
				setBeanClass(originalAbd.getBeanClass());
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependencyCheck(originalAbd.getDependencyCheck());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			copyQualifiersFrom(originalAbd);
			setPrimary(originalAbd.isPrimary());
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
			setInitMethodName(originalAbd.getInitMethodName());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodName(originalAbd.getDestroyMethodName());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			setSynthetic(originalAbd.isSynthetic());
			setResource(originalAbd.getResource());
		}
		else {
			setResourceDescription(original.getResourceDescription());
		}
	}


	/**
	 * Override settings in this bean definition (presumably a copied parent
	 * from a parent-child inheritance relationship) from the given bean
	 * definition (presumably the child).
	 * 
	 * <p> 从给定的bean定义（可能是子定义）中覆盖此bean定义中的设置（可能是来自父子继承关系的复制父项）。
	 * 
	 * @deprecated since Spring 2.5, in favor of {@link #overrideFrom(BeanDefinition)}
	 * 
	 * <p> 从Spring 2.5开始，支持overrideFrom（BeanDefinition）
	 * 
	 */
	@Deprecated
	public void overrideFrom(AbstractBeanDefinition other) {
		overrideFrom((BeanDefinition) other);
	}

	/**
	 * Override settings in this bean definition (presumably a copied parent
	 * from a parent-child inheritance relationship) from the given bean
	 * definition (presumably the child).
	 * 
	 * <p> 从给定的bean定义（可能是子定义）中覆盖此bean定义中的设置（可能是来自父子继承关系的复制父项）。
	 * 
	 * <ul>
	 * <li>Will override beanClass if specified in the given bean definition.
	 * 
	 * <li> 如果在给定的bean定义中指定，将覆盖beanClass。
	 * 
	 * <li>Will always take {@code abstract}, {@code scope},
	 * {@code lazyInit}, {@code autowireMode}, {@code dependencyCheck},
	 * and {@code dependsOn} from the given bean definition.
	 * 
	 * <li> 将始终从给定的bean定义中获取abstract，scope，lazyInit，autowireMode，dependencyCheck和dependsOn。
	 * 
	 * <li>Will add {@code constructorArgumentValues}, {@code propertyValues},
	 * {@code methodOverrides} from the given bean definition to existing ones.
	 * 
	 * <li> 将给定bean定义中的constructorArgumentValues，propertyValues，methodOverrides添加到现有bean定义中。
	 * 
	 * <li>Will override {@code factoryBeanName}, {@code factoryMethodName},
	 * {@code initMethodName}, and {@code destroyMethodName} if specified
	 * in the given bean definition.
	 * 
	 * <li> 如果在给定的bean定义中指定，将覆盖factoryBeanName，factoryMethodName，initMethodName和destroyMethodName。
	 * </ul>
	 */
	public void overrideFrom(BeanDefinition other) {
		if (StringUtils.hasLength(other.getBeanClassName())) {
			setBeanClassName(other.getBeanClassName());
		}
		if (StringUtils.hasLength(other.getFactoryBeanName())) {
			setFactoryBeanName(other.getFactoryBeanName());
		}
		if (StringUtils.hasLength(other.getFactoryMethodName())) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		if (StringUtils.hasLength(other.getScope())) {
			setScope(other.getScope());
		}
		setAbstract(other.isAbstract());
		setLazyInit(other.isLazyInit());
		setRole(other.getRole());
		getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
		getPropertyValues().addPropertyValues(other.getPropertyValues());
		setSource(other.getSource());
		copyAttributesFrom(other);

		if (other instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
			if (otherAbd.hasBeanClass()) {
				setBeanClass(otherAbd.getBeanClass());
			}
			setAutowireCandidate(otherAbd.isAutowireCandidate());
			setAutowireMode(otherAbd.getAutowireMode());
			copyQualifiersFrom(otherAbd);
			setPrimary(otherAbd.isPrimary());
			setDependencyCheck(otherAbd.getDependencyCheck());
			setDependsOn(otherAbd.getDependsOn());
			setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
			if (StringUtils.hasLength(otherAbd.getInitMethodName())) {
				setInitMethodName(otherAbd.getInitMethodName());
				setEnforceInitMethod(otherAbd.isEnforceInitMethod());
			}
			if (StringUtils.hasLength(otherAbd.getDestroyMethodName())) {
				setDestroyMethodName(otherAbd.getDestroyMethodName());
				setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
			}
			getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
			setSynthetic(otherAbd.isSynthetic());
			setResource(otherAbd.getResource());
		}
		else {
			setResourceDescription(other.getResourceDescription());
		}
	}

	/**
	 * Apply the provided default values to this bean.
	 * 
	 * <p> 将提供的默认值应用于此bean。
	 * 
	 * @param defaults the defaults to apply
	 * 
	 * <p> 要应用的默认值
	 * 
	 */
	public void applyDefaults(BeanDefinitionDefaults defaults) {
		setLazyInit(defaults.isLazyInit());
		setAutowireMode(defaults.getAutowireMode());
		setDependencyCheck(defaults.getDependencyCheck());
		setInitMethodName(defaults.getInitMethodName());
		setEnforceInitMethod(false);
		setDestroyMethodName(defaults.getDestroyMethodName());
		setEnforceDestroyMethod(false);
	}


	/**
	 * Return whether this definition specifies a bean class.
	 * 
	 * <p> 返回此定义是否指定bean类。
	 * 
	 */
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}

	/**
	 * Specify the class for this bean.
	 * 
	 * <p> 指定此bean的类。
	 * 
	 */
	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	/**
	 * Return the class of the wrapped bean, if already resolved.
	 * 
	 * <p> 如果已经解析，则返回包装bean的类。
	 * 
	 * @return the bean class, or {@code null} if none defined
	 * 
	 * <p> bean类，如果没有定义则为null
	 * 
	 * @throws IllegalStateException if the bean definition does not define a bean class,
	 * or a specified bean class name has not been resolved into an actual Class
	 * 
	 * <p> 如果bean定义没有定义bean类，或者指定的bean类名尚未解析为实际的Class
	 * 
	 */
	public Class<?> getBeanClass() throws IllegalStateException {
		Object beanClassObject = this.beanClass;
		if (beanClassObject == null) {
			throw new IllegalStateException("No bean class specified on bean definition");
		}
		if (!(beanClassObject instanceof Class)) {
			throw new IllegalStateException(
					"Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
		}
		return (Class<?>) beanClassObject;
	}

	public void setBeanClassName(String beanClassName) {
		this.beanClass = beanClassName;
	}

	public String getBeanClassName() {
		Object beanClassObject = this.beanClass;
		if (beanClassObject instanceof Class) {
			return ((Class<?>) beanClassObject).getName();
		}
		else {
			return (String) beanClassObject;
		}
	}

	/**
	 * Determine the class of the wrapped bean, resolving it from a
	 * specified class name if necessary. Will also reload a specified
	 * Class from its name when called with the bean class already resolved.
	 * 
	 * <p> 确定包装bean的类，必要时从指定的类名解析它。 在使用已经解析的bean类调用时，还将从其名称重新加载指定的Class。
	 * 
	 * @param classLoader the ClassLoader to use for resolving a (potential) class name
	 * 
	 * <p> 用于解析（潜在）类名的ClassLoader
	 * 
	 * @return the resolved bean class - 已解析的bean类
	 * @throws ClassNotFoundException if the class name could be resolved - 如果类名可以解决
	 */
	public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
		String className = getBeanClassName();
		if (className == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
		this.beanClass = resolvedClass;
		return resolvedClass;
	}


	/**
	 * Set the name of the target scope for the bean.
	 * 
	 * <p> 设置bean的目标范围的名称。
	 * 
	 * <p>Default is singleton status, although this is only applied once
	 * a bean definition becomes active in the containing factory. A bean
	 * definition may eventually inherit its scope from a parent bean definitionFor this
	 * reason, the default scope name is empty (empty String), with
	 * singleton status being assumed until a resolved scope will be set.
	 * 
	 * <p> 默认值是单例状态，但只有在bean定义在包含工厂中变为活动状态时才会应用此选项。
	 *  bean定义最终可能会从父bean定义继承其作用域。因此，默认作用域名称为空（空字符串），
	 *  假定单例状态为止，直到设置已解析的作用域。
	 *  
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	public void setScope(String scope) {
		this.scope = scope;
		this.singleton = SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
		this.prototype = SCOPE_PROTOTYPE.equals(scope);
	}

	/**
	 * Return the name of the target scope for the bean.
	 * 
	 * <p> 返回bean的目标范围的名称。
	 * 
	 */
	public String getScope() {
		return this.scope;
	}

	/**
	 * Set if this a <b>Singleton</b>, with a single, shared instance returned
	 * on all calls. In case of "false", the BeanFactory will apply the <b>Prototype</b>
	 * design pattern, with each caller requesting an instance getting an independent
	 * instance. How this is exactly defined will depend on the BeanFactory.
	 * 
	 * <p> 设置是否为Singleton，并在所有调用上返回单个共享实例。 在“false”的情况下，
	 * BeanFactory将应用Prototype设计模式，每个调用者请求实例获取独立实例。 
	 * 如何精确定义将取决于BeanFactory。
	 * 
	 * <p>"Singletons" are the commoner type, so the default is "true".
	 * Note that as of Spring 2.0, this flag is just an alternative way to
	 * specify scope="singleton" or scope="prototype".
	 * 
	 * <p> “Singletons”是普通类型，因此默认为“true”。 请注意，从Spring 2.0开始，
	 * 此标志只是指定scope =“singleton”或scope =“prototype”的替代方法。
	 * 
	 * @deprecated since Spring 2.5, in favor of {@link #setScope}
	 * 
	 * <p> 从Spring 2.5开始，支持setScope
	 * 
	 * @see #setScope
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	@Deprecated
	public void setSingleton(boolean singleton) {
		this.scope = (singleton ? SCOPE_SINGLETON : SCOPE_PROTOTYPE);
		this.singleton = singleton;
		this.prototype = !singleton;
	}

	/**
	 * Return whether this a <b>Singleton</b>, with a single shared instance
	 * returned from all calls.
	 * 
	 * <p> 返回是否为Singleton，从所有调用返回单个共享实例。
	 * 
	 * @see #SCOPE_SINGLETON
	 */
	public boolean isSingleton() {
		return this.singleton;
	}

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * 
	 * <p> 返回是否为Prototype，每次调用返回一个独立实例。
	 * 
	 * @see #SCOPE_PROTOTYPE
	 */
	public boolean isPrototype() {
		return this.prototype;
	}

	/**
	 * Set if this bean is "abstract", i.e. not meant to be instantiated itself but
	 * rather just serving as parent for concrete child bean definitions.
	 * 
	 * <p> 设置此bean是否为“抽象”，即不是要自己实例化，而是仅用作具体子bean定义的父级。
	 * 
	 * <p>Default is "false". Specify true to tell the bean factory to not try to
	 * instantiate that particular bean in any case.
	 * 
	 * <p> 默认为“false”。 指定true以告诉bean工厂在任何情况下都不尝试实例化该特定bean。
	 * 
	 */
	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}

	/**
	 * Return whether this bean is "abstract", i.e. not meant to be instantiated
	 * itself but rather just serving as parent for concrete child bean definitions.
	 * 
	 * <p> 返回这个bean是否是“抽象”的，即不是要自己实例化，而是仅仅作为具体子bean定义的父级。
	 * 
	 */
	public boolean isAbstract() {
		return this.abstractFlag;
	}

	/**
	 * Set whether this bean should be lazily initialized.
	 * 
	 * <p> 设置是否应该懒惰地初始化此bean。
	 * 
	 * <p>If {@code false}, the bean will get instantiated on startup by bean
	 * factories that perform eager initialization of singletons.
	 * 
	 * <p> 如果为false，那么bean将在启动时由bean工厂实例化，这些工厂执行单例的初始化。
	 * 
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 * 
	 * <p> 返回是否应该懒惰地初始化此bean，即在启动时不急切实例化。 仅适用于单例bean。
	 * 
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}


	/**
	 * Set the autowire mode. This determines whether any automagical detection
	 * and setting of bean references will happen. Default is AUTOWIRE_NO,
	 * which means there's no autowire.
	 * 
	 * <p> 设置自动装配模式。 这决定了是否会发生bean引用的任何自动检测和设置。 默认值为AUTOWIRE_NO，这意味着没有自动装配。
	 * 
	 * @param autowireMode the autowire mode to set.
	 * Must be one of the constants defined in this class.
	 * 
	 * <p> 要设置的自动装配模式。 必须是此中定义的常量之一
	 * 
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 */
	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	/**
	 * Return the autowire mode as specified in the bean definition.
	 * 
	 * <p> 返回bean定义中指定的autowire模式。
	 * 
	 */
	public int getAutowireMode() {
		return this.autowireMode;
	}

	/**
	 * Return the resolved autowire code,
	 * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
	 * 
	 * <p> 返回已解析的autowire代码，（将AUTOWIRE_AUTODETECT解析为AUTOWIRE_CONSTRUCTOR或AUTOWIRE_BY_TYPE）。
	 * 
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public int getResolvedAutowireMode() {
		if (this.autowireMode == AUTOWIRE_AUTODETECT) {
			// Work out whether to apply setter autowiring or constructor autowiring.
			// If it has a no-arg constructor it's deemed to be setter autowiring,
			// otherwise we'll try constructor autowiring.
			
			// 确定是否应用setter autowiring或构造函数自动装配。如果它有一个no-arg构造函数，
			// 它被认为是setter autowiring，否则我们将尝试构造函数自动装配。
			Constructor<?>[] constructors = getBeanClass().getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterTypes().length == 0) {
					return AUTOWIRE_BY_TYPE;
				}
			}
			return AUTOWIRE_CONSTRUCTOR;
		}
		else {
			return this.autowireMode;
		}
	}

	/**
	 * Set the dependency check code.
	 * 
	 * <p> 设置依赖性检查代码。
	 * 
	 * @param dependencyCheck the code to set.
	 * Must be one of the four constants defined in this class.
	 * 
	 * <p> 要设置的代码。 必须是此类中定义的四个常量之一。
	 * 
	 * @see #DEPENDENCY_CHECK_NONE
	 * @see #DEPENDENCY_CHECK_OBJECTS
	 * @see #DEPENDENCY_CHECK_SIMPLE
	 * @see #DEPENDENCY_CHECK_ALL
	 */
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * Return the dependency check code.
	 * 
	 * <p> 返回依赖性检查代码。
	 * 
	 */
	public int getDependencyCheck() {
		return this.dependencyCheck;
	}

	/**
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized first.
	 * 
	 * <p> 设置此bean依赖于初始化的bean的名称。 bean工厂将保证首先初始化这些bean。
	 * 
	 * <p>Note that dependencies are normally expressed through bean properties or
	 * constructor arguments. This property should just be necessary for other kinds
	 * of dependencies like statics (*ugh*) or database preparation on startup.
	 * 
	 * <p> 请注意，依赖关系通常通过bean属性或构造函数参数表示。 对于其他类型的依赖项，
	 * 例如静态（* ugh *）或启动时的数据库准备，此属性应该是必需的。
	 * 
	 */
	public void setDependsOn(String[] dependsOn) {
		this.dependsOn = dependsOn;
	}

	/**
	 * Return the bean names that this bean depends on.
	 * 
	 * <p> 返回此bean依赖的bean名称。
	 * 
	 */
	public String[] getDependsOn() {
		return this.dependsOn;
	}

	/**
	 * Set whether this bean is a candidate for getting autowired into some other bean.
	 * 
	 * <p> 设置此bean是否可以自动连接到其他bean。
	 * 
	 */
	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}

	/**
	 * Return whether this bean is a candidate for getting autowired into some other bean.
	 * 
	 * <p> 返回此bean是否可以自动连接到其他bean中。
	 * 
	 */
	public boolean isAutowireCandidate() {
		return this.autowireCandidate;
	}

	/**
	 * Set whether this bean is a primary autowire candidate.
	 * If this value is true for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 * 
	 * <p> 设置此bean是否为主要autowire候选者。 如果这个值对于多个匹配的候选者中的一个bean来说是真的，那么它将作为打破平局。
	 * 
	 */
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * Return whether this bean is a primary autowire candidate.
	 * If this value is true for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 * 
	 * <p> 返回此bean是否是主要的autowire候选者。 如果这个值对于多个匹配的候选者中的一个bean来说是真的，那么它将作为打破平局。
	 * 
	 */
	public boolean isPrimary() {
		return this.primary;
	}

	/**
	 * Register a qualifier to be used for autowire candidate resolution,
	 * keyed by the qualifier's type name.
	 * 
	 * <p> 注册用于autowire候选解析的限定符，由限定符的类型名称键入。
	 * 
	 * @see AutowireCandidateQualifier#getTypeName()
	 */
	public void addQualifier(AutowireCandidateQualifier qualifier) {
		this.qualifiers.put(qualifier.getTypeName(), qualifier);
	}

	/**
	 * Return whether this bean has the specified qualifier.
	 * 
	 * <p> 返回此bean是否具有指定的限定符。
	 * 
	 */
	public boolean hasQualifier(String typeName) {
		return this.qualifiers.keySet().contains(typeName);
	}

	/**
	 * Return the qualifier mapped to the provided type name.
	 * 
	 * <p> 返回映射到提供的类型名称的限定符。
	 * 
	 */
	public AutowireCandidateQualifier getQualifier(String typeName) {
		return this.qualifiers.get(typeName);
	}

	/**
	 * Return all registered qualifiers.
	 * 
	 * <p> 返回所有注册的合格者.
	 * 
	 * @return the Set of {@link AutowireCandidateQualifier} objects.
	 * 
	 * <p> AutowireCandidateQualifier对象集。
	 * 
	 */
	public Set<AutowireCandidateQualifier> getQualifiers() {
		return new LinkedHashSet<AutowireCandidateQualifier>(this.qualifiers.values());
	}

	/**
	 * Copy the qualifiers from the supplied AbstractBeanDefinition to this bean definition.
	 * 
	 * <p> 将限定符从提供的AbstractBeanDefinition复制到此bean定义。
	 * 
	 * @param source the AbstractBeanDefinition to copy from - 获取要从中复制的AbstractBeanDefinition
	 */
	public void copyQualifiersFrom(AbstractBeanDefinition source) {
		Assert.notNull(source, "Source must not be null");
		this.qualifiers.putAll(source.qualifiers);
	}


	/**
	 * Specify whether to allow access to non-public constructors and methods,
	 * for the case of externalized metadata pointing to those. The default is
	 * {@code true}; switch this to {@code false} for public access only.
	 * 
	 * <p> 指定是否允许访问非公共构造函数和方法，以用于指向那些的外部化元数据的情况。 默认为true; 将此切换为false仅用于公共访问。
	 * 
	 * <p>This applies to constructor resolution, factory method resolution,
	 * and also init/destroy methods. Bean property accessors have to be public
	 * in any case and are not affected by this setting.
	 * 
	 * <p> 这适用于构造函数解析，工厂方法解析以及init / destroy方法。 Bean属性访问器在任何情况下都必须是公共的，并且不受此设置的影响。
	 * 
	 * <p>Note that annotation-driven configuration will still access non-public
	 * members as far as they have been annotated. This setting applies to
	 * externalized metadata in this bean definition only.
	 * 
	 * <p> 请注意，注释驱动的配置仍将访问非公共成员，只要它们已被注释。 此设置仅适用于此Bean定义中的外部化元数据。
	 */
	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}

	/**
	 * Return whether to allow access to non-public constructors and methods.
	 * 
	 * <p> 返回是否允许访问非公共构造函数和方法。
	 * 
	 */
	public boolean isNonPublicAccessAllowed() {
		return this.nonPublicAccessAllowed;
	}

	/**
	 * Specify whether to resolve constructors in lenient mode ({@code true},
	 * which is the default) or to switch to strict resolution (throwing an exception
	 * in case of ambiguous constructors that all match when converting the arguments,
	 * whereas lenient mode would use the one with the 'closest' type matches).
	 * 
	 * <p> 指定是否以宽松模式解析构造函数（true，这是默认值）或切换到严格解析
	 * （在转换参数时所有匹配的模糊构造函数抛出异常，而宽松模式将使用具有'最接近的模式） '类型匹配）。
	 * 
	 */
	public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
		this.lenientConstructorResolution = lenientConstructorResolution;
	}

	/**
	 * Return whether to resolve constructors in lenient mode or in strict mode.
	 * 
	 * <p> 返回是否以宽松模式或严格模式解析构造函数。
	 * 
	 */
	public boolean isLenientConstructorResolution() {
		return this.lenientConstructorResolution;
	}

	/**
	 * Specify constructor argument values for this bean.
	 * 
	 * <p> 为此bean指定构造函数参数值。
	 * 
	 */
	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues =
				(constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
	}

	/**
	 * Return constructor argument values for this bean (never {@code null}).
	 * 
	 * <p> 返回此bean的构造函数参数值（从不{@code null}）。
	 * 
	 */
	public ConstructorArgumentValues getConstructorArgumentValues() {
		return this.constructorArgumentValues;
	}

	/**
	 * Return if there are constructor argument values defined for this bean.
	 * 
	 * <p> 如果为此bean定义了构造函数参数值，则返回。
	 * 
	 */
	public boolean hasConstructorArgumentValues() {
		return !this.constructorArgumentValues.isEmpty();
	}

	/**
	 * Specify property values for this bean, if any.
	 * 
	 * <p> 指定此bean的属性值（如果有）。
	 * 
	 */
	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
	}

	/**
	 * Return property values for this bean (never {@code null}).
	 * 
	 * <p> 返回此bean的属性值（永不为null）。
	 * 
	 */
	public MutablePropertyValues getPropertyValues() {
		return this.propertyValues;
	}

	/**
	 * Specify method overrides for the bean, if any.
	 * 
	 * <p> 指定bean的方法覆盖（如果有）。
	 * 
	 */
	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = (methodOverrides != null ? methodOverrides : new MethodOverrides());
	}

	/**
	 * Return information about methods to be overridden by the IoC
	 * container. This will be empty if there are no method overrides.
	 * Never returns null.
	 * 
	 * <p> 返回有关IoC容器要覆盖的方法的信息。 如果没有方法覆盖，则为空。 永远不会返回null。
	 * 
	 */
	public MethodOverrides getMethodOverrides() {
		return this.methodOverrides;
	}


	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	public String getFactoryMethodName() {
		return this.factoryMethodName;
	}

	/**
	 * Set the name of the initializer method. The default is {@code null}
	 * in which case there is no initializer method.
	 * 
	 * <p> 设置初始化方法的名称。 默认值为null，在这种情况下没有初始化方法。
	 */
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	/**
	 * Return the name of the initializer method.
	 * 
	 * <p> 返回初始化方法的名称。
	 * 
	 */
	public String getInitMethodName() {
		return this.initMethodName;
	}

	/**
	 * Specify whether or not the configured init method is the default.
	 * Default value is {@code false}.
	 * 
	 * <p> 指定配置的init方法是否为默认方法。 默认值为false。
	 * 
	 * @see #setInitMethodName
	 */
	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}

	/**
	 * Indicate whether the configured init method is the default.
	 * 
	 * <p> 指示配置的init方法是否为默认方法。
	 * 
	 * @see #getInitMethodName()
	 */
	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}

	/**
	 * Set the name of the destroy method. The default is {@code null}
	 * in which case there is no destroy method.
	 * 
	 * <p> 设置destroy方法的名称。 默认值为null，在这种情况下没有destroy方法。
	 * 
	 */
	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	/**
	 * Return the name of the destroy method.
	 * 
	 * <p> 返回destroy方法的名称。
	 * 
	 */
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

	/**
	 * Specify whether or not the configured destroy method is the default.
	 * Default value is {@code false}.
	 * 
	 * <p> 指定配置的destroy方法是否为默认方法。 默认值为false。
	 * 
	 * @see #setDestroyMethodName
	 */
	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}

	/**
	 * Indicate whether the configured destroy method is the default.
	 * 
	 * <p> 指示配置的destroy方法是否为默认方法。
	 * 
	 * @see #getDestroyMethodName
	 */
	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
	}


	/**
	 * Set whether this bean definition is 'synthetic', that is, not defined
	 * by the application itself (for example, an infrastructure bean such
	 * as a helper for auto-proxying, created through {@code &ltaop:config&gt;}).
	 * 
	 * <p> 设置此bean定义是否为“合成”，即不是由应用程序本身定义的（例如，通过＆lt; aop：config＆gt;创建的基础结构bean，如自动代理帮助程序）。
	 * 
	 */
	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	/**
	 * Return whether this bean definition is 'synthetic', that is,
	 * not defined by the application itself.
	 * 
	 * <p> 返回此bean定义是否为“合成”，即未由应用程序本身定义。
	 * 
	 */
	public boolean isSynthetic() {
		return this.synthetic;
	}

	/**
	 * Set the role hint for this {@code BeanDefinition}.
	 * 
	 * <p> 设置此BeanDefinition的角色提示。
	 * 
	 */
	public void setRole(int role) {
		this.role = role;
	}

	/**
	 * Return the role hint for this {@code BeanDefinition}.
	 * 
	 * <p> 返回此BeanDefinition的角色提示。
	 * 
	 */
	public int getRole() {
		return this.role;
	}


	/**
	 * Set a human-readable description of this bean definition.
	 * 
	 * <p> 设置此bean定义的可读描述。
	 * 
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the resource that this bean definition came from
	 * (for the purpose of showing context in case of errors).
	 * 
	 * <p> 设置此bean定义来自的资源（为了在出现错误时显示上下文）。
	 * 
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Return the resource that this bean definition came from.
	 * 
	 * <p> 返回此bean定义来自的资源。
	 * 
	 */
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * Set a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 * 
	 * <p> 设置此bean定义来源的资源的描述（为了在出现错误时显示上下文）。
	 * 
	 */
	public void setResourceDescription(String resourceDescription) {
		this.resource = new DescriptiveResource(resourceDescription);
	}

	public String getResourceDescription() {
		return (this.resource != null ? this.resource.getDescription() : null);
	}

	/**
	 * Set the originating (e.g. decorated) BeanDefinition, if any.
	 * 
	 * <p> 设置原始（例如装饰）BeanDefinition（如果有）。
	 * 
	 */
	public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
		this.resource = new BeanDefinitionResource(originatingBd);
	}

	public BeanDefinition getOriginatingBeanDefinition() {
		return (this.resource instanceof BeanDefinitionResource ?
				((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
	}

	/**
	 * Validate this bean definition.
	 * 
	 * <p> 当bean定义的验证失败时抛出异常。
	 * 
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	public void validate() throws BeanDefinitionValidationException {
		if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
			throw new BeanDefinitionValidationException(
					"Cannot combine static factory method with method overrides: " +
					"the static factory method must create the instance");
		}

		if (hasBeanClass()) {
			prepareMethodOverrides();
		}
	}

	/**
	 * Validate and prepare the method overrides defined for this bean.
	 * Checks for existence of a method with the specified name.
	 * 
	 * <p> 验证并准备为此bean定义的方法覆盖。 检查是否存在具有指定名称的方法。
	 * 
	 * @throws BeanDefinitionValidationException in case of validation failure - 在验证失败的情况下
	 */
	public void prepareMethodOverrides() throws BeanDefinitionValidationException {
		// Check that lookup methods exists.
		// 检查查找方法是否存在。
		MethodOverrides methodOverrides = getMethodOverrides();
		if (!methodOverrides.isEmpty()) {
			for (MethodOverride mo : methodOverrides.getOverrides()) {
				prepareMethodOverride(mo);
			}
		}
	}

	/**
	 * Validate and prepare the given method override.
	 * Checks for existence of a method with the specified name,
	 * marking it as not overloaded if none found.
	 * 
	 * <p> 验证并准备给定的方法覆盖。 检查是否存在具有指定名称的方法，如果没有找到则将其标记为未重载。
	 * 
	 * @param mo the MethodOverride object to validate - 要验证的MethodOverride对象
	 * @throws BeanDefinitionValidationException in case of validation failure - 在验证失败的情况下
	 */
	protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
		//获取对应类中对应方法名的个数
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if (count == 0) {
			throw new BeanDefinitionValidationException(
					"Invalid method override: no method with name '" + mo.getMethodName() +
					"' on class [" + getBeanClassName() + "]");
		}
		else if (count == 1) {
			// Mark override as not overloaded, to avoid the overhead of arg type checking.
			// 将覆盖标记为未重载，以避免arg类型检查的开销。
			
			//标记MethodOverride暂未被覆盖,编码参数类型检查的开销
			mo.setOverloaded(false);
		}
	}


	/**
	 * Public declaration of Object's {@code clone()} method.
	 * Delegates to {@link #cloneBeanDefinition()}.
	 * 
	 * <p> Object clone（）方法的公共声明。 委托给cloneBean Definition（）。
	 * 
	 * @see Object#clone()
	 */
	@Override
	public Object clone() {
		return cloneBeanDefinition();
	}

	/**
	 * Clone this bean definition.
	 * To be implemented by concrete subclasses.
	 * 
	 * <p> 克隆此bean定义。 由具体的子类实现。
	 * 
	 * @return the cloned bean definition object - 克隆的bean定义对象
	 */
	public abstract AbstractBeanDefinition cloneBeanDefinition();


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeanDefinition)) {
			return false;
		}

		AbstractBeanDefinition that = (AbstractBeanDefinition) other;

		if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
		if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
		if (this.abstractFlag != that.abstractFlag) return false;
		if (this.lazyInit != that.lazyInit) return false;

		if (this.autowireMode != that.autowireMode) return false;
		if (this.dependencyCheck != that.dependencyCheck) return false;
		if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
		if (this.autowireCandidate != that.autowireCandidate) return false;
		if (!ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers)) return false;
		if (this.primary != that.primary) return false;

		if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
		if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
		if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides)) return false;

		if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
		if (this.enforceInitMethod != that.enforceInitMethod) return false;
		if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
		if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

		if (this.synthetic != that.synthetic) return false;
		if (this.role != that.role) return false;

		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
		hashCode = 29 * hashCode + super.hashCode();
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class [");
		sb.append(getBeanClassName()).append("]");
		sb.append("; scope=").append(this.scope);
		sb.append("; abstract=").append(this.abstractFlag);
		sb.append("; lazyInit=").append(this.lazyInit);
		sb.append("; autowireMode=").append(this.autowireMode);
		sb.append("; dependencyCheck=").append(this.dependencyCheck);
		sb.append("; autowireCandidate=").append(this.autowireCandidate);
		sb.append("; primary=").append(this.primary);
		sb.append("; factoryBeanName=").append(this.factoryBeanName);
		sb.append("; factoryMethodName=").append(this.factoryMethodName);
		sb.append("; initMethodName=").append(this.initMethodName);
		sb.append("; destroyMethodName=").append(this.destroyMethodName);
		if (this.resource != null) {
			sb.append("; defined in ").append(this.resource.getDescription());
		}
		return sb.toString();
	}

}
