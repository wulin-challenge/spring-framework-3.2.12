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

package org.springframework.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Enables Spring's annotation-driven cache management capability, similar to
 * the support found in Spring's {@code <cache:*>} XML namespace. To be used together
 * with @{@link org.springframework.context.annotation.Configuration Configuration}
 * classes as follows:
 * 
 * <p> 启用Spring的注释驱动的缓存管理功能，类似于Spring的<cache：*> XML名称空间中的支持。 与@Configuration类一起使用，如下所示：
 * 
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableCaching
 * public class AppConfig {
 *     &#064;Bean
 *     public MyService myService() {
 *         // configure and return a class having &#064;Cacheable methods
 *         return new MyService();
 *     }
 *
 *     &#064;Bean
 *     public CacheManager cacheManager() {
 *         // configure and return an implementation of Spring's CacheManager SPI
 *         SimpleCacheManager cacheManager = new SimpleCacheManager();
 *         cacheManager.addCaches(Arrays.asList(new ConcurrentMapCache("default")));
 *         return cacheManager;
 *     }
 * }</pre>
 *
 * <p>For reference, the example above can be compared to the following Spring XML
 * configuration:
 * 
 * <p> 作为参考，可以将上面的示例与以下Spring XML配置进行比较：
 * 
 * <pre class="code">
 * {@code
 * <beans>
 *     <cache:annotation-driven/>
 *     <bean id="myService" class="com.foo.MyService"/>
 *     <bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager">
 *         <property name="caches">
 *             <set>
 *                 <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean">
 *                     <property name="name" value="default"/>
 *                 </bean>
 *             </set>
 *         </property>
 *     </bean>
 * </beans>
 * }</pre>
 * In both of the scenarios above, {@code @EnableCaching} and {@code
 * <cache:annotation-driven/>} are responsible for registering the necessary Spring
 * components that power annotation-driven cache management, such as the
 * {@link org.springframework.cache.interceptor.CacheInterceptor CacheInterceptor} and the
 * proxy- or AspectJ-based advice that weaves the interceptor into the call stack when
 * {@link org.springframework.cache.annotation.Cacheable @Cacheable} methods are invoked.
 * 
 * <p> 在上述两种情况下，@EnableCaching和<cache：annotation-driven />都负责注册必要的Spring组件，
 * 这些组件可为注释驱动的缓存管理提供支持，例如CacheInterceptor以及基于代理的或基于AspectJ的建议 调用@Cacheable方法时，
 * 拦截器将进入调用堆栈。
 *
 * <p><strong>A bean of type {@link org.springframework.cache.CacheManager CacheManager}
 * must be registered</strong>, as there is no reasonable default that the framework can
 * use as a convention. And whereas the {@code <cache:annotation-driven>} element assumes
 * a bean <em>named</em> "cacheManager", {@code @EnableCaching} searches for a cache
 * manager bean <em>by type</em>. Therefore, naming of the cache manager bean method is
 * not significant.
 * 
 * <p> 必须注册CacheManager类型的Bean，因为没有合理的默认值可以将该框架用作约定。 
 * 尽管<cache：annotation-driven>元素假定一个名为“ cacheManager”的bean，但@EnableCaching按类型搜索一个缓存管理器bean。 
 * 因此，缓存管理器bean方法的命名并不重要。
 *
 * <p>For those that wish to establish a more direct relationship between
 * {@code @EnableCaching} and the exact cache manager bean to be used,
 * the {@link CachingConfigurer} callback interface may be implemented - notice the
 * {@code implements} clause and the {@code @Override}-annotated methods below:
 * 
 * <p> 对于那些希望在@EnableCaching和要使用的确切缓存管理器bean之间建立更直接关系的用户，
 * 可以实现CachingConfigurer回调接口-请注意下面的Implements子句和@ Override-annotated方法：
 * 
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableCaching
 * public class AppConfig implements CachingConfigurer {
 *     &#064;Bean
 *     public MyService myService() {
 *         // configure and return a class having &#064;Cacheable methods
 *         return new MyService();
 *     }
 *
 *     &#064;Bean
 *     &#064;Override
 *     public CacheManager cacheManager() {
 *         // configure and return an implementation of Spring's CacheManager SPI
 *         SimpleCacheManager cacheManager = new SimpleCacheManager();
 *         cacheManager.addCaches(Arrays.asList(new ConcurrentMapCache("default")));
 *         return cacheManager;
 *     }
 *
 *     &#064;Bean
 *     &#064;Override
 *     public KeyGenerator keyGenerator() {
 *         // configure and return an implementation of Spring's KeyGenerator SPI
 *         return new MyKeyGenerator();
 *     }
 * }</pre>
 * This approach may be desirable simply because it is more explicit, or it may be
 * necessary in order to distinguish between two {@code CacheManager} beans present in the
 * same container.
 * 
 * <p> 仅仅因为它更明确，可能会希望使用此方法，或者可能需要此方法来区分存在于同一容器中的两个CacheManager bean。
 *
 * <p>Notice also the {@code keyGenerator} method in the example above. This allows for
 * customizing the strategy for cache key generation, per Spring's {@link
 * org.springframework.cache.interceptor.KeyGenerator KeyGenerator} SPI. Normally,
 * {@code @EnableCaching} will configure Spring's
 * {@link org.springframework.cache.interceptor.DefaultKeyGenerator DefaultKeyGenerator}
 * for this purpose, but when implementing {@code CachingConfigurer}, a key generator
 * must be provided explicitly. Return {@code new DefaultKeyGenerator()} from this method
 * if no customization is necessary. See {@link CachingConfigurer} Javadoc for further
 * details.
 * 
 * <p> 还要注意上面示例中的keyGenerator方法。这允许根据Spring的KeyGenerator SPI自定义用于生成缓存密钥的策略。
 * 通常，@ EnableCaching为此将配置Spring的DefaultKeyGenerator，但是在实现CachingConfigurer时，
 * 必须显式提供一个密钥生成器。如果不需要自定义，则从此方法返回新的DefaultKeyGenerator（）。
 * 有关更多详细信息，请参见CachingConfigurer Javadoc。
 *
 * <p>The {@link #mode()} attribute controls how advice is applied; if the mode is
 * {@link AdviceMode#PROXY} (the default), then the other attributes such as
 * {@link #proxyTargetClass()} control the behavior of the proxying.
 * 
 * <p> mode（）属性控制建议的应用方式；如果模式为AdviceMode.PROXY（默认设置），
 * 则其他属性（例如proxyTargetClass（））控制代理的行为。
 *
 * <p>If the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then the
 * {@link #proxyTargetClass()} attribute is obsolete. Note also that in this case the
 * {@code spring-aspects} module JAR must be present on the classpath.
 * 
 * <p> 如果模式设置为AdviceMode.ASPECTJ，则proxyTargetClass（）属性已过时。
 * 还要注意，在这种情况下，spring-aspects模块JAR必须存在于类路径中。
 *
 * @author Chris Beams
 * @since 3.1
 * @see CachingConfigurer
 * @see CachingConfigurationSelector
 * @see ProxyCachingConfiguration
 * @see org.springframework.cache.aspectj.AspectJCachingConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CachingConfigurationSelector.class)
public @interface EnableCaching {

	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is {@code false}. <strong>
	 * Applicable only if {@link #mode()} is set to {@link AdviceMode#PROXY}</strong>.
	 *
	 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
	 * Spring-managed beans requiring proxying, not just those marked with
	 * {@code @Cacheable}. For example, other beans marked with Spring's
	 * {@code @Transactional} annotation will be upgraded to subclass proxying at the same
	 * time. This approach has no negative impact in practice unless one is explicitly
	 * expecting one type of proxy vs another, e.g. in tests.
	 */
	boolean proxyTargetClass() default false;

	/**
	 * Indicate how caching advice should be applied. The default is
	 * {@link AdviceMode#PROXY}.
	 * @see AdviceMode
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate the ordering of the execution of the caching advisor
	 * when multiple advices are applied at a specific joinpoint.
	 * The default is {@link Ordered#LOWEST_PRECEDENCE}.
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;
}
