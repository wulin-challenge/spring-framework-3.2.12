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

package org.springframework.web;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * Servlet 3.0 {@link ServletContainerInitializer} designed to support code-based
 * configuration of the servlet container using Spring's {@link WebApplicationInitializer}
 * SPI as opposed to (or possibly in combination with) the traditional
 * {@code web.xml}-based approach.
 * 
 * <p> Servlet 3.0 ServletContainerInitializer设计为使用Spring的WebApplicationInitializer SPI支持
 * Servlet容器的基于代码的配置，这与传统的基于web.xml的方法相反（或可能与之结合）。
 *
 * <h2>Mechanism of Operation</h2>
 * 
 * <p> 运行机制
 * 
 * <p> This class will be loaded and instantiated and have its {@link #onStartup}
 * method invoked by any Servlet 3.0-compliant container during container startup assuming
 * that the {@code spring-web} module JAR is present on the classpath. This occurs through
 * the JAR Services API {@link ServiceLoader#load(Class)} method detecting the
 * {@code spring-web} module's {@code META-INF/services/javax.servlet.ServletContainerInitializer}
 * service provider configuration file. See the
 * <a href="http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider">
 * JAR Services API documentation</a> as well as section <em>8.2.4</em> of the Servlet 3.0
 * Final Draft specification for complete details.
 * 
 * <p> 假定在类路径中存在spring-web模块JAR，那么将在容器启动期间由任何与Servlet 3.0兼容的容器调用该类并将其实例化，
 * 并使其onStartup方法被调用。这是通过JAR Services API ServiceLoader.load（Class）方法来检测
 * spring-web模块的META-INF / services / javax.servlet.ServletContainerInitializer服务提供程序配置文件而发生的。
 * 有关完整的详细信息，请参见JAR Services API文档以及Servlet 3.0最终草案规范的8.2.4节。
 *
 * <h3>In combination with {@code web.xml}</h3>
 * 
 * <p> 与web.xml结合
 * 
 * <p> A web application can choose to limit the amount of classpath scanning the Servlet
 * container does at startup either through the {@code metadata-complete} attribute in
 * {@code web.xml}, which controls scanning for Servlet annotations or through an
 * {@code <absolute-ordering>} element also in {@code web.xml}, which controls which
 * web fragments (i.e. jars) are allowed to perform a {@code ServletContainerInitializer}
 * scan. When using this feature, the {@link SpringServletContainerInitializer}
 * can be enabled by adding "spring_web" to the list of named web fragments in
 * {@code web.xml} as follows:
 * 
 * <p> Web应用程序可以选择通过限制web.xml中的meta-complete属性（它控制扫描Servlet注释的扫描）或也通过web.xml
 * 中的<absolute-ordering>元素来限制Servlet容器在启动时扫描的类路径的数量。 ，它控制允许哪些Web片段（即jars）执行
 * ServletContainerInitializer扫描。使用此功能时，可以通过如下方式启用SpringServletContainerInitializer：
 * 将“ spring_web”添加到web.xml中的命名Web片段列表中，如下所示：
 *
 * <pre class="code">
 * {@code
 * <absolute-ordering>
 *   <name>some_web_fragment</name>
 *   <name>spring_web</name>
 * </absolute-ordering>
 * }</pre>
 *
 * <h2>Relationship to Spring's {@code WebApplicationInitializer}</h2>
 * 
 * <p> 与Spring的WebApplicationInitializer的关系
 * 
 * <p> Spring's {@code WebApplicationInitializer} SPI consists of just one method:
 * {@link WebApplicationInitializer#onStartup(ServletContext)}. The signature is intentionally
 * quite similar to {@link ServletContainerInitializer#onStartup(Set, ServletContext)}:
 * simply put, {@code SpringServletContainerInitializer} is responsible for instantiating
 * and delegating the {@code ServletContext} to any user-defined
 * {@code WebApplicationInitializer} implementations. It is then the responsibility of
 * each {@code WebApplicationInitializer} to do the actual work of initializing the
 * {@code ServletContext}. The exact process of delegation is described in detail in the
 * {@link #onStartup onStartup} documentation below.
 * 
 * <p> Spring的WebApplicationInitializer SPI仅包含一种方法：WebApplicationInitializer.onStartup（ServletContext）。
 * 该签名有意与ServletContainerInitializer.onStartup（Set，ServletContext）非常相似：简而言之，
 * SpringServletContainerInitializer负责实例化ServletContext并将其委派给任何用户定义的WebApplicationInitializer实现。
 * 然后，每个WebApplicationInitializer都有责任完成初始化ServletContext的实际工作。委派的确切过程在下面的onStartup文档中详细描述。
 *
 * <h2>General Notes</h2>
 * 
 * <p> 一般注意事项
 * 
 * <p> In general, this class should be viewed as <em>supporting infrastructure</em> for
 * the more important and user-facing {@code WebApplicationInitializer} SPI. Taking
 * advantage of this container initializer is also completely <em>optional</em>: while
 * it is true that this initializer will be loaded and invoked under all Servlet 3.0+
 * runtimes, it remains the user's choice whether to make any
 * {@code WebApplicationInitializer} implementations available on the classpath. If no
 * {@code WebApplicationInitializer} types are detected, this container initializer will
 * have no effect.
 * 
 * <p> 通常，应将此类视为更重要的和面向用户的WebApplicationInitializer SPI的支持基础结构。利用此容器初始化程序也是完全可选的：
 * 尽管确实会在所有Servlet 3.0+运行时下加载并调用此初始化程序，但用户仍可以选择是否在类路径上提供任何WebApplicationInitializer实现。
 * 如果未检测到WebApplicationInitializer类型，则此容器初始化程序将无效。
 *
 * <p>Note that use of this container initializer and of {@code WebApplicationInitializer}
 * is not in any way "tied" to Spring MVC other than the fact that the types are shipped
 * in the {@code spring-web} module JAR. Rather, they can be considered general-purpose
 * in their ability to facilitate convenient code-based configuration of the
 * {@code ServletContext}. In other words, any servlet, listener, or filter may be
 * registered within a {@code WebApplicationInitializer}, not just Spring MVC-specific
 * components.
 * 
 * <p> 请注意，除了在Spring-Web模块JAR中附带类型之外，不以任何方式将此容器初始化程序和WebApplicationInitializer绑定到Spring MVC。
 * 而是，它们可以被认为具有通用性，以促进ServletContext的基于代码的便捷配置。换句话说，任何servlet，侦听器或过滤器都可以在
 * WebApplicationInitializer中注册，而不仅仅是Spring MVC特定的组件。
 *
 * <p>This class is neither designed for extension nor intended to be extended.
 * It should be considered an internal type, with {@code WebApplicationInitializer}
 * being the public-facing SPI.
 * 
 * <p> 此类既不是为扩展而设计的，也不是旨在扩展的。应该将其视为内部类型，WebApplicationInitializer是面向公众的SPI。
 *
 * <h2>See Also</h2>
 * <p> 也可以看看
 * 
 * <p> See {@link WebApplicationInitializer} Javadoc for examples and detailed usage
 * recommendations.<p>
 * 
 * <p> 有关示例和详细的使用建议，请参见WebApplicationInitializer Javadoc。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see #onStartup(Set, ServletContext)
 * @see WebApplicationInitializer
 */
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {

	/**
	 * Delegate the {@code ServletContext} to any {@link WebApplicationInitializer}
	 * implementations present on the application classpath.
	 * 
	 * <p> 将ServletContext委托给应用程序类路径上存在的任何WebApplicationInitializer实现。
	 *
	 * <p>Because this class declares @{@code HandlesTypes(WebApplicationInitializer.class)},
	 * Servlet 3.0+ containers will automatically scan the classpath for implementations
	 * of Spring's {@code WebApplicationInitializer} interface and provide the set of all
	 * such types to the {@code webAppInitializerClasses} parameter of this method.
	 * 
	 * <p> 因为此类声明了@HandlesTypes（WebApplicationInitializer.class），
	 * 所以Servlet 3.0+容器将自动扫描类路径以查找Spring的WebApplicationInitializer接口的实现，
	 * 并将所有此类类型的集合提供给此方法的webAppInitializerClasses参数。
	 *
	 * <p>If no {@code WebApplicationInitializer} implementations are found on the
	 * classpath, this method is effectively a no-op. An INFO-level log message will be
	 * issued notifying the user that the {@code ServletContainerInitializer} has indeed
	 * been invoked but that no {@code WebApplicationInitializer} implementations were
	 * found.
	 * 
	 * <p> 如果在类路径上没有找到WebApplicationInitializer实现，则此方法实际上是无操作的。将发出INFO级别的日志消息，
	 * 通知用户确实已调用ServletContainerInitializer，但是未找到WebApplicationInitializer实现。
	 *
	 * <p>Assuming that one or more {@code WebApplicationInitializer} types are detected,
	 * they will be instantiated (and <em>sorted</em> if the @{@link
	 * org.springframework.core.annotation.Order @Order} annotation is present or
	 * the {@link org.springframework.core.Ordered Ordered} interface has been
	 * implemented). Then the {@link WebApplicationInitializer#onStartup(ServletContext)}
	 * method will be invoked on each instance, delegating the {@code ServletContext} such
	 * that each instance may register and configure servlets such as Spring's
	 * {@code DispatcherServlet}, listeners such as Spring's {@code ContextLoaderListener},
	 * or any other Servlet API componentry such as filters.
	 * 
	 * <p> 假设检测到一个或多个WebApplicationInitializer类型，则将实例化它们（并在存在@@ Order批注或已实现Ordered接口的情况下进行排序）。
	 * 然后将在每个实例上调用WebApplicationInitializer.onStartup（ServletContext）方法，委派ServletContext，
	 * 以便每个实例可以注册和配置Servlet（例如Spring的DispatcherServlet），侦听器（例如Spring的ContextLoaderListener）或任何其他Servlet API组件（例如过滤器）。
	 *
	 * @param webAppInitializerClasses all implementations of
	 * {@link WebApplicationInitializer} found on the application classpath
	 * 
	 * <p> 在应用程序类路径上找到的WebApplicationInitializer的所有实现
	 * 
	 * @param servletContext the servlet context to be initialized
	 * 
	 * <p> 要初始化的servlet上下文
	 * 
	 * @see WebApplicationInitializer#onStartup(ServletContext)
	 * @see AnnotationAwareOrderComparator
	 */
	public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = new LinkedList<WebApplicationInitializer>();

		if (webAppInitializerClasses != null) {
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebApplicationInitializer) waiClass.newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		AnnotationAwareOrderComparator.sort(initializers);
		servletContext.log("Spring WebApplicationInitializers detected on classpath: " + initializers);

		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

}
