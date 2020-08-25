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

package org.springframework.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Interface to be implemented in Servlet 3.0+ environments in order to configure the
 * {@link ServletContext} programmatically -- as opposed to (or possibly in conjunction
 * with) the traditional {@code web.xml}-based approach.
 * 
 * <p> 在Servlet 3.0+环境中实现的接口，以便以编程方式配置ServletContext-与传统的基于web.xml的方法相反（或可能与之结合）。
 *
 * <p>Implementations of this SPI will be detected automatically by {@link
 * SpringServletContainerInitializer}, which itself is bootstrapped automatically
 * by any Servlet 3.0 container. See {@linkplain SpringServletContainerInitializer its
 * Javadoc} for details on this bootstrapping mechanism.
 * 
 * <p> SpringServletContainerInitializer会自动检测到此SPI的实现，它本身会被任何Servlet 3.0容器自动引导。
 *  有关此自举机制的详细信息，请参见其Javadoc。
 *
 * <h2>Example</h2>
 * <p> 例子
 * 
 * <h3>The traditional, XML-based approach</h3>
 * 
 * <p> 基于XML的传统方法
 * 
 * <p> Most Spring users building a web application will need to register Spring's {@code
 * DispatcherServlet}. For reference, in WEB-INF/web.xml, this would typically be done as
 * follows:
 * 
 * <p> 构建Web应用程序的大多数Spring用户将需要注册Spring的DispatcherServlet。 作为参考，通常在WEB-INF / web.xml中执行以下操作：
 * 
 * <pre class="code">
 * {@code
 * <servlet>
 *   <servlet-name>dispatcher</servlet-name>
 *   <servlet-class>
 *     org.springframework.web.servlet.DispatcherServlet
 *   </servlet-class>
 *   <init-param>
 *     <param-name>contextConfigLocation</param-name>
 *     <param-value>/WEB-INF/spring/dispatcher-config.xml</param-value>
 *   </init-param>
 *   <load-on-startup>1</load-on-startup>
 * </servlet>
 *
 * <servlet-mapping>
 *   <servlet-name>dispatcher</servlet-name>
 *   <url-pattern>/</url-pattern>
 * </servlet-mapping>}</pre>
 *
 * <h3>The code-based approach with {@code WebApplicationInitializer}</h3>
 * 
 * <p> WebApplicationInitializer的基于代码的方法
 * 
 * <p> Here is the equivalent {@code DispatcherServlet} registration logic,
 * {@code WebApplicationInitializer}-style:
 * 
 * <p> 这是WebApplicationInitializer样式的等效DispatcherServlet注册逻辑：
 * 
 * <pre class="code">
 * public class MyWebAppInitializer implements WebApplicationInitializer {
 *
 *    &#064;Override
 *    public void onStartup(ServletContext container) {
 *      XmlWebApplicationContext appContext = new XmlWebApplicationContext();
 *      appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
 *
 *      ServletRegistration.Dynamic dispatcher =
 *        container.addServlet("dispatcher", new DispatcherServlet(appContext));
 *      dispatcher.setLoadOnStartup(1);
 *      dispatcher.addMapping("/");
 *    }
 *
 * }</pre>
 *
 * As an alternative to the above, you can also extend from {@link
 * org.springframework.web.servlet.support.AbstractDispatcherServletInitializer}.
 * 
 * 作为上述替代方案，您还可以从org.springframework.web.servlet.support.AbstractDispatcherServletInitializer扩展。
 * 
 * <p> As you can see, thanks to Servlet 3.0's new {@link ServletContext#addServlet} method
 * we're actually registering an <em>instance</em> of the {@code DispatcherServlet}, and
 * this means that the {@code DispatcherServlet} can now be treated like any other object
 * -- receiving constructor injection of its application context in this case.
 * 
 * <p> 如您所见，多亏了Servlet 3.0的新ServletContext.addServlet方法，我们实际上正在注册DispatcherServlet的实例，
 * 这意味着DispatcherServlet现在可以像其他任何对象一样对待—接收其应用程序上下文的构造方法注入在这种情况下。
 *
 * <p>This style is both simpler and more concise. There is no concern for dealing with
 * init-params, etc, just normal JavaBean-style properties and constructor arguments. You
 * are free to create and work with your Spring application contexts as necessary before
 * injecting them into the {@code DispatcherServlet}.
 * 
 * <p> 这种样式既简单又简洁。不用担心处理init-params等问题，只需要处理普通的JavaBean样式的属性和构造函数参数即可。
 * 在将它们注入DispatcherServlet之前，您可以根据需要自由创建和使用Spring应用程序上下文。
 *
 * <p>Most major Spring Web components have been updated to support this style of
 * registration.  You'll find that {@code DispatcherServlet}, {@code FrameworkServlet},
 * {@code ContextLoaderListener} and {@code DelegatingFilterProxy} all now support
 * constructor arguments. Even if a component (e.g. non-Spring, other third party) has not
 * been specifically updated for use within {@code WebApplicationInitializers}, they still
 * may be used in any case. The Servlet 3.0 {@code ServletContext} API allows for setting
 * init-params, context-params, etc programmatically.
 * 
 * <p> 大多数主要的Spring Web组件已更新为支持这种注册样式。您会发现
 * DispatcherServlet，FrameworkServlet，ContextLoaderListener和DelegatingFilterProxy现在都支持构造函数参数。
 * 即使未针对WebApplicationInitializers中使用的组件（例如非Spring，其他第三方）进行专门更新，也仍然可以在任何情况下使用它们。 
 * Servlet 3.0 ServletContext API允许以编程方式设置init-params，context-params等。
 *
 * <h2>A 100% code-based approach to configuration</h2>
 * 
 * <p> 100％基于代码的配置方法
 * 
 * <p> In the example above, {@code WEB-INF/web.xml} was successfully replaced with code in
 * the form of a {@code WebApplicationInitializer}, but the actual
 * {@code dispatcher-config.xml} Spring configuration remained XML-based.
 * {@code WebApplicationInitializer} is a perfect fit for use with Spring's code-based
 * {@code @Configuration} classes. See @{@link
 * org.springframework.context.annotation.Configuration Configuration} Javadoc for
 * complete details, but the following example demonstrates refactoring to use Spring's
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} in lieu of {@code XmlWebApplicationContext}, and
 * user-defined {@code @Configuration} classes {@code AppConfig} and
 * {@code DispatcherConfig} instead of Spring XML files. This example also goes a bit
 * beyond those above to demonstrate typical configuration of the 'root' application
 * context and registration of the {@code ContextLoaderListener}:
 * 
 * <p> 在上面的示例中，成功用WebApplicationInitializer形式的代码替换了WEB-INF / web.xml，
 * 但是实际的dispatcher-config.xml Spring配置仍然基于XML。 WebApplicationInitializer非常适合与
 * Spring的基于代码的@Configuration类一起使用。 有关完整的详细信息，请参见@Configuration Javadoc，
 * 但是以下示例演示了重构以使用Spring的AnnotationConfigWebApplicationContext代替XmlWebApplicationContext
 * 以及用户定义的@Configuration类AppConfig和DispatcherConfig而不是Spring XML文件。 此示例还超出了上面的示例，
 * 以演示“根”应用程序上下文的典型配置以及ContextLoaderListener的注册：
 * 
 * <pre class="code">
 * public class MyWebAppInitializer implements WebApplicationInitializer {
 *
 *    &#064;Override
 *    public void onStartup(ServletContext container) {
 *      // Create the 'root' Spring application context
 *      AnnotationConfigWebApplicationContext rootContext =
 *        new AnnotationConfigWebApplicationContext();
 *      rootContext.register(AppConfig.class);
 *
 *      // Manage the lifecycle of the root application context
 *      container.addListener(new ContextLoaderListener(rootContext));
 *
 *      // Create the dispatcher servlet's Spring application context
 *      AnnotationConfigWebApplicationContext dispatcherContext =
 *        new AnnotationConfigWebApplicationContext();
 *      dispatcherContext.register(DispatcherConfig.class);
 *
 *      // Register and map the dispatcher servlet
 *      ServletRegistration.Dynamic dispatcher =
 *        container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
 *      dispatcher.setLoadOnStartup(1);
 *      dispatcher.addMapping("/");
 *    }
 *
 * }</pre>
 *
 * As an alternative to the above, you can also extend from {@link
 * org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
 * 
 * <p> 作为上述替代方案，您还可以从
 * org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer扩展。
 *
 * <p> Remember that {@code WebApplicationInitializer} implementations are <em>detected
 * automatically</em> -- so you are free to package them within your application as you
 * see fit.
 * 
 * <p> 请记住，WebApplicationInitializer实现是自动检测到的，因此您可以随意将它们打包到应用程序中。
 *
 * <h2>Ordering {@code WebApplicationInitializer} execution</h2>
 * 
 * <p> 排序执行WebApplicationInitializer
 * 
 * <p> {@code WebApplicationInitializer} implementations may optionally be annotated at the
 * class level with Spring's @{@link org.springframework.core.annotation.Order Order}
 * annotation or may implement Spring's {@link org.springframework.core.Ordered Ordered}
 * interface. If so, the initializers will be ordered prior to invocation. This provides
 * a mechanism for users to ensure the order in which servlet container initialization
 * occurs. Use of this feature is expected to be rare, as typical applications will likely
 * centralize all container initialization within a single {@code WebApplicationInitializer}.
 *
 *<p> WebApplicationInitializer实现可以选择在类级别使用Spring的@Order注释进行注释，或者可以实现Spring的Ordered接口。 
 *如果是这样，将在调用之前对初始化程序进行排序。 这为用户提供了一种机制，以确保servlet容器初始化发生的顺序。 预期很少使用此功能，
 *因为典型的应用程序可能会将所有容器初始化集中在单个WebApplicationInitializer中。
 *
 * <h2>Caveats</h2>
 * 
 * <p> 注意事项
 *
 * <h3>web.xml versioning</h3>
 * 
 * <p> web.xml版本控制
 * 
 * <p>{@code WEB-INF/web.xml} and {@code WebApplicationInitializer} use are not mutually
 * exclusive; for example, web.xml can register one servlet, and a {@code
 * WebApplicationInitializer} can register another. An initializer can even
 * <em>modify</em> registrations performed in {@code web.xml} through methods such as
 * {@link ServletContext#getServletRegistration(String)}. <strong>However, if
 * {@code WEB-INF/web.xml} is present in the application, its {@code version} attribute
 * must be set to "3.0" or greater, otherwise {@code ServletContainerInitializer}
 * bootstrapping will be ignored by the servlet container.</strong>
 * 
 * <p> WEB-INF / web.xml和WebApplicationInitializer的使用不是互斥的； 例如，web.xml可以注册一个servlet，
 * 而WebApplicationInitializer可以注册另一个。 初始化程序甚至可以通过
 * ServletContext.getServletRegistration（String）之类的方法来修改web.xml中执行的注册。 但是，
 * 如果应用程序中存在WEB-INF / web.xml，则必须将其version属性设置为“ 3.0”或更高，否则Servlet容器将忽略
 * ServletContainerInitializer自举。
 *
 * <h3>Mapping to '/' under Tomcat</h3>
 * 
 * <p> 映射到Tomcat下的'/'
 * 
 * <p>Apache Tomcat maps its internal {@code DefaultServlet} to "/", and on Tomcat versions
 * &lt;= 7.0.14, this servlet mapping <em>cannot be overridden programmatically</em>.
 * 7.0.15 fixes this issue. Overriding the "/" servlet mapping has also been tested
 * successfully under GlassFish 3.1.<p>
 * 
 * <p> Apache Tomcat将其内部DefaultServlet映射为“ /”，并且在Tomcat版本<= 7.0.14上，无法以编程方式覆盖此Servlet映射。 
 * 7.0.15解决了此问题。 覆盖“ /” servlet映射也已在GlassFish 3.1下成功测试。
 *
 * @author Chris Beams
 * @since 3.1
 * @see SpringServletContainerInitializer
 * @see org.springframework.web.context.AbstractContextLoaderInitializer
 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer
 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer
 */
public interface WebApplicationInitializer {

	/**
	 * Configure the given {@link ServletContext} with any servlets, filters, listeners
	 * context-params and attributes necessary for initializing this web application. See
	 * examples {@linkplain WebApplicationInitializer above}.
	 * 
	 * <p> 使用初始化此Web应用程序所需的任何Servlet，过滤器，侦听器上下文参数和属性来配置给定的ServletContext。 请参阅上面的示例。
	 * 
	 * @param servletContext the {@code ServletContext} to initialize
	 * @throws ServletException if any call against the given {@code ServletContext}
	 * throws a {@code ServletException}
	 * 
	 * <p> 如果对给定ServletContext的任何调用均抛出ServletException
	 */
	void onStartup(ServletContext servletContext) throws ServletException;

}
