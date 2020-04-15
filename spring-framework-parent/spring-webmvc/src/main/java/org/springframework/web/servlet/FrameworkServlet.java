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

package org.springframework.web.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * Base servlet for Spring's web framework. Provides integration with
 * a Spring application context, in a JavaBean-based overall solution.
 * 
 * <p> Spring的Web框架的基本servlet。 在基于JavaBean的整体解决方案中提供与Spring应用程序上下文的集成。
 *
 * <p>This class offers the following functionality:
 * 
 * <p> 该类提供以下功能：
 * 
 * <ul>
 * <li>Manages a {@link org.springframework.web.context.WebApplicationContext
 * WebApplicationContext} instance per servlet. The servlet's configuration is determined
 * by beans in the servlet's namespace.
 * 
 * <li> 管理每个servlet的WebApplicationContext实例。 servlet的配置由servlet命名空间中的bean确定。
 * 
 * <li> 
 * 
 * <li>Publishes events on request processing, whether or not a request is
 * successfully handled.
 * 
 * <li> 无论请求是否成功处理，都会根据请求处理发布事件。
 * 
 * <li> 
 * </ul>
 *
 * <p>Subclasses must implement {@link #doService} to handle requests. Because this extends
 * {@link HttpServletBean} rather than HttpServlet directly, bean properties are
 * automatically mapped onto it. Subclasses can override {@link #initFrameworkServlet()}
 * for custom initialization.
 * 
 * <p> 子类必须实现doService来处理请求。因为这会直接扩展HttpServletBean而不是HttpServlet，
 * 所以bean属性会自动映射到它上面。子类可以覆盖initFrameworkServlet（）以进行自定义初始化。
 *
 * <p>Detects a "contextClass" parameter at the servlet init-param level,
 * falling back to the default context class,
 * {@link org.springframework.web.context.support.XmlWebApplicationContext
 * XmlWebApplicationContext}, if not found. Note that, with the default
 * {@code FrameworkServlet}, a custom context class needs to implement the
 * {@link org.springframework.web.context.ConfigurableWebApplicationContext
 * ConfigurableWebApplicationContext} SPI.
 * 
 * <p> 检测servlet init-param级别的“contextClass”参数，如果找不到则返回默认上下文类
 * XmlWebApplicationContext。请注意，使用默认的FrameworkServlet，自定义上下文类需要实现
 * ConfigurableWebApplicationContext SPI。
 *
 * <p>Accepts an optional "contextInitializerClasses" servlet init-param that
 * specifies one or more {@link org.springframework.context.ApplicationContextInitializer
 * ApplicationContextInitializer} classes. The managed web application context will be
 * delegated to these initializers, allowing for additional programmatic configuration,
 * e.g. adding property sources or activating profiles against the {@linkplain
 * org.springframework.context.ConfigurableApplicationContext#getEnvironment() context's
 * environment}. See also {@link org.springframework.web.context.ContextLoader} which
 * supports a "contextInitializerClasses" context-param with identical semantics for
 * the "root" web application context.
 * 
 * <p> 接受可选的“contextInitializerClasses”servlet init-param，它指定一个或多个ApplicationContextInitializer类。
 * 托管的Web应用程序上下文将被委托给这些初始化程序，从而允许进行额外的编程配置，例如，根据上下文环境添加属性源或激活配置文件。另请参阅
 * org.springframework.web.context.ContextLoader，它支持“contextInitializerClasses”context-param，
 * 其中“root”Web应用程序上下文具有相同的语义。
 *
 * <p>Passes a "contextConfigLocation" servlet init-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by any
 * number of commas and spaces, like "test-servlet.xml, myServlet.xml".
 * If not explicitly specified, the context implementation is supposed to build a
 * default location from the namespace of the servlet.
 * 
 * <p> 将“contextConfigLocation”servlet init-param传递给上下文实例，将其解析为可能由多个逗号和空格分隔的多个文件路径，
 * 例如“test-servlet.xml，myServlet.xml”。如果没有明确指定，则上下文实现应该从servlet的命名空间构建一个默认位置。
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using Spring's
 * default ApplicationContext implementation. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 * 
 * <p> 注意：如果有多个配置位置，以后的bean定义将覆盖先前加载的文件中定义的那些，至少在使用Spring的默认ApplicationContext实现时。
 * 这可以用来通过额外的XML文件故意覆盖某些bean定义。
 *
 * <p>The default namespace is "'servlet-name'-servlet", e.g. "test-servlet" for a
 * servlet-name "test" (leading to a "/WEB-INF/test-servlet.xml" default location
 * with XmlWebApplicationContext). The namespace can also be set explicitly via
 * the "namespace" servlet init-param.
 * 
 * <p> 默认命名空间是“'servlet-name'-servlet”，例如servlet-name“test”的“test-servlet”
 * （通过XmlWebApplicationContext导致“/WEB-INF/test-servlet.xml”默认位置）。也可以通过
 * “namespace”servlet init-param显式设置命名空间。
 *
 * <p>As of Spring 3.1, {@code FrameworkServlet} may now be injected with a web
 * application context, rather than creating its own internally. This is useful in Servlet
 * 3.0+ environments, which support programmatic registration of servlet instances. See
 * {@link #FrameworkServlet(WebApplicationContext)} Javadoc for details.
 * 
 * <p> 从Spring 3.1开始，FrameworkServlet现在可以注入Web应用程序上下文，而不是在内部创建自己的Web应用程序上下文。
 * 这在Servlet 3.0+环境中很有用，它支持servlet实例的编程注册。有关详细信息，
 * 请参阅FrameworkServlet（WebApplicationContext）Javadoc。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @see #doService
 * @see #setContextClass
 * @see #setContextConfigLocation
 * @see #setContextInitializerClasses
 * @see #setNamespace
 */
@SuppressWarnings("serial")
public abstract class FrameworkServlet extends HttpServletBean {

	/**
	 * Suffix for WebApplicationContext namespaces. If a servlet of this class is
	 * given the name "test" in a context, the namespace used by the servlet will
	 * resolve to "test-servlet".
	 * 
	 * <p> WebApplicationContext命名空间的后缀。 如果此类的servlet在上下文中被赋予名称“test”，
	 * 则servlet使用的命名空间将解析为“test-servlet”。
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/**
	 * Default context class for FrameworkServlet.
	 * 
	 * <p> FrameworkServlet的默认上下文类。
	 * 
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * Prefix for the ServletContext attribute for the WebApplicationContext.
	 * The completion is the servlet name.
	 * 
	 * <p> WebApplicationContext的ServletContext属性的前缀。 完成是servlet名称。
	 * 
	 */
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple values in a single init-param String value.
	 * 
	 * <p> 在单个init-param字符串值中，任何数量的这些字符都被视为多个值之间的分隔符。
	 */
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";


	/** ServletContext attribute to find the WebApplicationContext in */
	/** ServletContext属性用于查找WebApplicationContext */
	private String contextAttribute;

	/** WebApplicationContext implementation class to create */
	/** 要创建的WebApplicationContext实现类 */
	private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

	/** WebApplicationContext id to assign */
	/** 要分配的WebApplicationContext id */
	private String contextId;

	/** Namespace for this servlet */
	/** 此servlet的命名空间 */
	private String namespace;

	/** Explicit context config location */
	/** 显式上下文配置位置 */
	private String contextConfigLocation;

	/** Actual ApplicationContextInitializer instances to apply to the context */
	/** 要应用于上下文的实际ApplicationContextInitializer实例 */
	private final ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers =
			new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();

	/** Comma-delimited ApplicationContextInitializer class names set through init param */
	/** 通过init param设置的逗号分隔的ApplicationContextInitializer类名 */
	private String contextInitializerClasses;

	/** Should we publish the context as a ServletContext attribute? */
	/** 我们应该将上下文作为ServletContext属性发布吗？ */
	private boolean publishContext = true;

	/** Should we publish a ServletRequestHandledEvent at the end of each request? */
	/** 我们应该在每个请求结束时发布ServletRequestHandledEvent吗？ */
	private boolean publishEvents = true;

	/** Expose LocaleContext and RequestAttributes as inheritable for child threads? */
	/** 将LocaleContext和RequestAttributes公开为子线程可继承？ */
	private boolean threadContextInheritable = false;

	/** Should we dispatch an HTTP OPTIONS request to {@link #doService}? */
	/** 我们应该向{@link #doService}发送HTTP OPTIONS请求吗？ */
	private boolean dispatchOptionsRequest = false;

	/** Should we dispatch an HTTP TRACE request to {@link #doService}? */
	/** 我们应该向{@link #doService}发送HTTP TRACE请求吗？ */
	private boolean dispatchTraceRequest = false;

	/** WebApplicationContext for this servlet */
	/** 此servlet的WebApplicationContext */
	private WebApplicationContext webApplicationContext;

	/** Flag used to detect whether onRefresh has already been called */
	/** 用于检测是否已调用onRefresh的标志 */
	private boolean refreshEventReceived = false;


	/**
	 * Create a new {@code FrameworkServlet} that will create its own internal web
	 * application context based on defaults and values provided through servlet
	 * init-params. Typically used in Servlet 2.5 or earlier environments, where the only
	 * option for servlet registration is through {@code web.xml} which requires the use
	 * of a no-arg constructor.
	 * 
	 * <p> 创建一个新的FrameworkServlet，它将根据servlet init-params提供的缺省值和值创建自己的内部Web应用程序上下文。
	 *  通常在Servlet 2.5或更早版本的环境中使用，其中servlet注册的唯一选项是通过web.xml，它需要使用no-arg构造函数。
	 *  
	 * <p>Calling {@link #setContextConfigLocation} (init-param 'contextConfigLocation')
	 * will dictate which XML files will be loaded by the
	 * {@linkplain #DEFAULT_CONTEXT_CLASS default XmlWebApplicationContext}
	 * 
	 * <p> 调用setContextConfigLocation（init-param'contextureConfigLocation'）将指定默认的
	 * XmlWebApplicationContext将加载哪些XML文件
	 * 
	 * <p>Calling {@link #setContextClass} (init-param 'contextClass') overrides the
	 * default {@code XmlWebApplicationContext} and allows for specifying an alternative class,
	 * such as {@code AnnotationConfigWebApplicationContext}.
	 * 
	 * <p> 调用setContextClass（init-param'contextageClass'）会覆盖默认的XmlWebApplicationContext，
	 * 并允许指定替代类，例如AnnotationConfigWebApplicationContext。
	 * 
	 * <p>Calling {@link #setContextInitializerClasses} (init-param 'contextInitializerClasses')
	 * indicates which {@link ApplicationContextInitializer} classes should be used to
	 * further configure the internal application context prior to refresh().
	 * 
	 * <p> 调用setContextInitializerClasses（init-param'contextInitializerClasses'）指示在refresh（）
	 * 之前应该使用哪些ApplicationContextInitializer类来进一步配置内部应用程序上下文。
	 * 
	 * @see #FrameworkServlet(WebApplicationContext)
	 */
	public FrameworkServlet() {
	}

	/**
	 * Create a new {@code FrameworkServlet} with the given web application context. This
	 * constructor is useful in Servlet 3.0+ environments where instance-based registration
	 * of servlets is possible through the {@link ServletContext#addServlet} API.
	 * 
	 * <p> 使用给定的Web应用程序上下文创建新的FrameworkServlet。 这个构造函数在Servlet 3.0+环境中很有用，在这些环境中，
	 * ServletContext.addServlet API可以通过基于实例的servlet注册。
	 * 
	 * <p>Using this constructor indicates that the following properties / init-params
	 * will be ignored:
	 * 
	 * <p> 使用此构造函数表示将忽略以下属性/ init-params：
	 * 
	 * <ul>
	 * <li>{@link #setContextClass(Class)} / 'contextClass'</li>
	 * <li>{@link #setContextConfigLocation(String)} / 'contextConfigLocation'</li>
	 * <li>{@link #setContextAttribute(String)} / 'contextAttribute'</li>
	 * <li>{@link #setNamespace(String)} / 'namespace'</li>
	 * </ul>
	 * <p>The given web application context may or may not yet be {@linkplain
	 * ConfigurableApplicationContext#refresh() refreshed}. If it (a) is an implementation
	 * of {@link ConfigurableWebApplicationContext} and (b) has <strong>not</strong>
	 * already been refreshed (the recommended approach), then the following will occur:
	 * 
	 * <p> 给定的Web应用程序上下文可能已经或可能尚未刷新。 如果（a）是ConfigurableWebApplicationContext的实现并且（b）
	 * 尚未刷新（推荐的方法），则会发生以下情况：
	 * 
	 * <ul>
	 * <li>If the given context does not already have a {@linkplain
	 * ConfigurableApplicationContext#setParent parent}, the root application context
	 * will be set as the parent.</li>
	 * 
	 * <li> 如果给定的上下文还没有父级，则根应用程序上下文将被设置为父级。
	 * 
	 * <li> 
	 * 
	 * <li>If the given context has not already been assigned an {@linkplain
	 * ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * 
	 * <li> 如果给定的上下文尚未分配id，则将为其分配一个id
	 * 
	 * <li> 
	 * 
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * 
	 * <li> ServletContext和ServletConfig对象将委托给应用程序上下文
	 * 
	 * <li> 
	 * 
	 * <li>{@link #postProcessWebApplicationContext} will be called</li>
	 * 
	 * <li> 将调用postProcessWebApplicationContext
	 * 
	 * <li> 
	 * 
	 * <li>Any {@link ApplicationContextInitializer}s specified through the
	 * "contextInitializerClasses" init-param or through the {@link
	 * #setContextInitializers} property will be applied.</li>
	 * 
	 * <li> 将应用通过“contextInitializerClasses”init-param或通过setContextInitializers属性指定的任何
	 * ApplicationContextInitializers。
	 * 
	 * <li> 
	 * 
	 * <li>{@link ConfigurableApplicationContext#refresh refresh()} will be called</li>
	 * 
	 * <li> 将调用refresh（）
	 * 
	 * <li> 
	 * </ul>
	 * If the context has already been refreshed or does not implement
	 * {@code ConfigurableWebApplicationContext}, none of the above will occur under the
	 * assumption that the user has performed these actions (or not) per his or her
	 * specific needs.
	 * 
	 * <p> 如果上下文已经刷新或未实现ConfigurableWebApplicationContext，则假设用户已根据其特定需求执行（或不执行）
	 * 这些操作，则不会发生上述任何情况。
	 * 
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * 
	 * <p> 有关用法示例，请参阅org.springframework.web.WebApplicationInitializer。
	 * 
	 * @param webApplicationContext the context to use - 要使用的上下文
	 * @see #initWebApplicationContext
	 * @see #configureAndRefreshWebApplicationContext
	 * @see org.springframework.web.WebApplicationInitializer
	 */
	public FrameworkServlet(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
	}


	/**
	 * Set the name of the ServletContext attribute which should be used to retrieve the
	 * {@link WebApplicationContext} that this servlet is supposed to use.
	 * 
	 * <p> 设置ServletContext属性的名称，该属性应该用于检索此servlet应该使用的WebApplicationContext。
	 * 
	 */
	public void setContextAttribute(String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}

	/**
	 * Return the name of the ServletContext attribute which should be used to retrieve the
	 * {@link WebApplicationContext} that this servlet is supposed to use.
	 * 
	 * <p> 返回ServletContext属性的名称，该属性应该用于检索此servlet应该使用的WebApplicationContext。
	 * 
	 */
	public String getContextAttribute() {
		return this.contextAttribute;
	}

	/**
	 * Set a custom context class. This class must be of type
	 * {@link org.springframework.web.context.WebApplicationContext}.
	 * 
	 * <p> 设置自定义上下文类。 此类必须是org.springframework.web.context.WebApplicationContext类型。
	 * 
	 * <p>When using the default FrameworkServlet implementation,
	 * the context class must also implement the
	 * {@link org.springframework.web.context.ConfigurableWebApplicationContext}
	 * interface.
	 * 
	 * <p> 使用默认的FrameworkServlet实现时，上下文类还必须实现
	 * org.springframework.web.context.ConfigurableWebApplicationContext接口。
	 * 
	 * @see #createWebApplicationContext
	 */
	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Return the custom context class.
	 * 
	 * <p> 返回自定义上下文类。
	 */
	public Class<?> getContextClass() {
		return this.contextClass;
	}

	/**
	 * Specify a custom WebApplicationContext id,
	 * to be used as serialization id for the underlying BeanFactory.
	 * 
	 * <p> 指定自定义WebApplicationContext标识，以用作基础BeanFactory的序列化标识。
	 * 
	 */
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	/**
	 * Return the custom WebApplicationContext id, if any.
	 * 
	 * <p> 返回自定义WebApplicationContext标识（如果有）。
	 */
	public String getContextId() {
		return this.contextId;
	}

	/**
	 * Set a custom namespace for this servlet,
	 * to be used for building a default context config location.
	 * 
	 * <p> 为此servlet设置自定义命名空间，以用于构建默认上下文配置位置。
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Return the namespace for this servlet, falling back to default scheme if
	 * no custom namespace was set: e.g. "test-servlet" for a servlet named "test".
	 * 
	 * <p> 返回此servlet的命名空间，如果未设置自定义命名空间，则返回默认方案：
	 * 例如 名为“test”的servlet的“test-servlet”。
	 */
	public String getNamespace() {
		return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
	}

	/**
	 * Set the context config location explicitly, instead of relying on the default
	 * location built from the namespace. This location string can consist of
	 * multiple locations separated by any number of commas and spaces.
	 * 
	 * <p> 显式设置上下文配置位置，而不是依赖于从命名空间构建的默认位置。 此位置字符串可以包含由任意数量的逗号和空格分隔的多个位置。
	 */
	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	/**
	 * Return the explicit context config location, if any.
	 */
	public String getContextConfigLocation() {
		return this.contextConfigLocation;
	}

	/**
	 * Specify which {@link ApplicationContextInitializer} instances should be used
	 * to initialize the application context used by this {@code FrameworkServlet}.
	 * 
	 * <p> 指定应使用哪个ApplicationContextInitializer实例初始化此FrameworkServlet使用的应用程序上下文。
	 * 
	 * @see #configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext)
	 * @see #applyInitializers(ConfigurableApplicationContext)
	 */
	@SuppressWarnings("unchecked")
	public void setContextInitializers(ApplicationContextInitializer<? extends ConfigurableApplicationContext>... contextInitializers) {
		for (ApplicationContextInitializer<? extends ConfigurableApplicationContext> initializer : contextInitializers) {
			this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
		}
	}

	/**
	 * Specify the set of fully-qualified {@link ApplicationContextInitializer} class
	 * names, per the optional "contextInitializerClasses" servlet init-param.
	 * 
	 * <p> 根据可选的“contextInitializerClasses”servlet init-param指定一组完全限定的
	 * ApplicationContextInitializer类名。
	 * 
	 * @see #configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext)
	 * @see #applyInitializers(ConfigurableApplicationContext)
	 */
	public void setContextInitializerClasses(String contextInitializerClasses) {
		this.contextInitializerClasses = contextInitializerClasses;
	}

	/**
	 * Set whether to publish this servlet's context as a ServletContext attribute,
	 * available to all objects in the web container. Default is "true".
	 * 
	 * <p> 设置是否将此servlet的上下文作为ServletContext属性发布，可用于Web容器中的所有对象。 默认为“true”。
	 * 
	 * <p>This is especially handy during testing, although it is debatable whether
	 * it's good practice to let other application objects access the context this way.
	 * 
	 * <p> 这在测试期间尤其方便，尽管让其他应用程序对象以这种方式访问上下文是否是一种好的做法值得商榷。
	 * 
	 */
	public void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}

	/**
	 * Set whether this servlet should publish a ServletRequestHandledEvent at the end
	 * of each request. Default is "true"; can be turned off for a slight performance
	 * improvement, provided that no ApplicationListeners rely on such events.
	 * 
	 * <p> 设置此servlet是否应在每个请求结束时发布ServletRequestHandledEvent。 默认为“true”; 
	 * 可以关闭以获得轻微的性能提升，前提是没有ApplicationListeners依赖此类事件。
	 * 
	 * @see org.springframework.web.context.support.ServletRequestHandledEvent
	 */
	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}

	/**
	 * Set whether to expose the LocaleContext and RequestAttributes as inheritable
	 * for child threads (using an {@link java.lang.InheritableThreadLocal}).
	 * 
	 * <p> 设置是否将LocaleContext和RequestAttributes公开为子线程可继承
	 * （使用java.lang.InheritableThreadLocal）。
	 * 
	 * <p>Default is "false", to avoid side effects on spawned background threads.
	 * Switch this to "true" to enable inheritance for custom child threads which
	 * are spawned during request processing and only used for this request
	 * (that is, ending after their initial task, without reuse of the thread).
	 * 
	 * <p> 默认为“false”，以避免对衍生的后台线程产生副作用。 将其切换为“true”以启用在请求处理期间生成的自定义子线程的继承，
	 * 并仅用于此请求（即，在其初始任务之后结束，而不重用该线程）。
	 * 
	 * <p><b>WARNING:</b> Do not use inheritance for child threads if you are
	 * accessing a thread pool which is configured to potentially add new threads
	 * on demand (e.g. a JDK {@link java.util.concurrent.ThreadPoolExecutor}),
	 * since this will expose the inherited context to such a pooled thread.
	 * 
	 * <p> 警告：如果要访问配置为可能按需添加新线程的线程池（例如JDK java.util.concurrent.ThreadPoolExecutor），
	 * 请不要对子线程使用继承，因为这会将继承的上下文暴露给这样的池 线。
	 * 
	 */
	public void setThreadContextInheritable(boolean threadContextInheritable) {
		this.threadContextInheritable = threadContextInheritable;
	}

	/**
	 * Set whether this servlet should dispatch an HTTP OPTIONS request to
	 * the {@link #doService} method.
	 * 
	 * <p> 设置此servlet是否应将HTTP OPTIONS请求分派给doService方法。
	 * 
	 * <p>Default is "false", applying {@link javax.servlet.http.HttpServlet}'s
	 * default behavior (i.e. enumerating all standard HTTP request methods
	 * as a response to the OPTIONS request).
	 * 
	 * <p> 默认为“false”，应用javax.servlet.http.HttpServlet的默认行为
	 * （即枚举所有标准HTTP请求方法作为对OPTIONS请求的响应）。
	 * 
	 * <p>Turn this flag on if you prefer OPTIONS requests to go through the
	 * regular dispatching chain, just like other HTTP requests. This usually
	 * means that your controllers will receive those requests; make sure
	 * that those endpoints are actually able to handle an OPTIONS request.
	 * 
	 * <p> 如果您喜欢OPTIONS请求通过常规调度链，请打开此标志，就像其他HTTP请求一样。 
	 * 这通常意味着您的控制器将接收这些请求; 确保这些端点实际上能够处理OPTIONS请求。
	 * 
	 * <p>Note that HttpServlet's default OPTIONS processing will be applied
	 * in any case if your controllers happen to not set the 'Allow' header
	 * (as required for an OPTIONS response).
	 * 
	 * <p> 请注意，如果您的控制器碰巧没有设置“允许”标头（根据OPTIONS响应的要求），将在任何情况下应用HttpServlet的默认OPTIONS处理。
	 */
	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}

	/**
	 * Set whether this servlet should dispatch an HTTP TRACE request to
	 * the {@link #doService} method.
	 * 
	 * <p> 设置此servlet是否应将HTTP TRACE请求分派给doService方法。
	 * 
	 * <p>Default is "false", applying {@link javax.servlet.http.HttpServlet}'s
	 * default behavior (i.e. reflecting the message received back to the client).
	 * 
	 * <p> 默认值为“false”，应用javax.servlet.http.HttpServlet的默认行为（即反映收到的消息返回给客户端）。
	 * 
	 * <p>Turn this flag on if you prefer TRACE requests to go through the
	 * regular dispatching chain, just like other HTTP requests. This usually
	 * means that your controllers will receive those requests; make sure
	 * that those endpoints are actually able to handle a TRACE request.
	 * 
	 * <p> 如果您希望TRACE请求通过常规调度链，请打开此标志，就像其他HTTP请求一样。 这通常意味着您的控制器将接收这些请求; 
	 * 确保这些端点实际上能够处理TRACE请求。
	 * 
	 * <p>Note that HttpServlet's default TRACE processing will be applied
	 * in any case if your controllers happen to not generate a response
	 * of content type 'message/http' (as required for a TRACE response).
	 * 
	 * <p> 请注意，如果您的控制器碰巧没有生成内容类型“message / http”的响应（如TRACE响应所需），
	 * 则将在任何情况下应用HttpServlet的默认TRACE处理。
	 * 
	 */
	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}


	/**
	 * Overridden method of {@link HttpServletBean}, invoked after any bean properties
	 * have been set. Creates this servlet's WebApplicationContext.
	 * 
	 * <p> 重写的HttpServletBean方法，在设置任何bean属性后调用。 创建此servlet的WebApplicationContext。
	 * 
	 */
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
		if (this.logger.isInfoEnabled()) {
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
		}
		long startTime = System.currentTimeMillis();

		try {
			this.webApplicationContext = initWebApplicationContext();
			initFrameworkServlet();
		}
		catch (ServletException ex) {
			this.logger.error("Context initialization failed", ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			this.logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (this.logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
					elapsedTime + " ms");
		}
	}

	/**
	 * Initialize and publish the WebApplicationContext for this servlet.
	 * 
	 * <p> 初始化并发布此servlet的WebApplicationContext。
	 * 
	 * <p>Delegates to {@link #createWebApplicationContext} for actual creation
	 * of the context. Can be overridden in subclasses.
	 * 
	 * <p> 委托创建WebApplicationContext以实际创建上下文。 可以在子类中重写。
	 * 
	 * @return the WebApplicationContext instance
	 * @see #FrameworkServlet(WebApplicationContext)
	 * @see #setContextClass
	 * @see #setContextConfigLocation
	 */
	protected WebApplicationContext initWebApplicationContext() {
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		if (this.webApplicationContext != null) {
			// A context instance was injected at construction time -> use it
			// 在构造时注入了一个上下文实例 - >使用它
			wac = this.webApplicationContext;
			if (wac instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
				if (!cwac.isActive()) {
					// The context has not yet been refreshed -> provide services such as
					// setting the parent context, setting the application context id, etc
					
					//上下文尚未刷新 - >提供诸如设置父上下文，设置应用程序上下文ID等服务
					if (cwac.getParent() == null) {
						// The context instance was injected without an explicit parent -> set
						// the root application context (if any; may be null) as the parent
						
						// 在没有显式父级的情况下注入上下文实例 ->将根应用程序上下文（如果有的话;可以为null）设置为父级
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);
				}
			}
		}
		if (wac == null) {
			// No context instance was injected at construction time -> see if one
			// has been registered in the servlet context. If one exists, it is assumed
			// that the parent context (if any) has already been set and that the
			// user has performed any initialization such as setting the context id
			
			/*
			 * 在构造时没有注入上下文实例 - >查看是否已在servlet上下文中注册了一个。 如果存在，
			 * 则假设已经设置了父上下文（如果有）并且用户已经执行了任何初始化，例如设置上下文id
			 */
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// No context instance is defined for this servlet -> create a local one
			// 没有为此servlet定义上下文实例 - >创建本地实例
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// Either the context is not a ConfigurableApplicationContext with refresh
			// support or the context injected at construction time had already been
			// refreshed -> trigger initial onRefresh manually here.
			
			/*
			 * 上下文不是具有刷新支持的ConfigurableApplicationContext，
			 * 或者在构造时注入的上下文已经刷新 - >在此手动触发初始onRefresh。
			 */
			onRefresh(wac);
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute.
			// 将上下文发布为servlet上下文属性。
			String attrName = getServletContextAttributeName();
			getServletContext().setAttribute(attrName, wac);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
						"' as ServletContext attribute with name [" + attrName + "]");
			}
		}

		return wac;
	}

	/**
	 * Retrieve a {@code WebApplicationContext} from the {@code ServletContext}
	 * attribute with the {@link #setContextAttribute configured name}. The
	 * {@code WebApplicationContext} must have already been loaded and stored in the
	 * {@code ServletContext} before this servlet gets initialized (or invoked).
	 * 
	 * <p> 使用配置的名称从ServletContext属性检索WebApplicationContext。 在初始化（或调用）此servlet之前，
	 * WebApplicationContext必须已加载并存储在ServletContext中。
	 * 
	 * <p>Subclasses may override this method to provide a different
	 * {@code WebApplicationContext} retrieval strategy.
	 * 
	 * <p> 子类可以重写此方法以提供不同的WebApplicationContext检索策略。
	 * 
	 * @return the WebApplicationContext for this servlet, or {@code null} if not found
	 * @see #getContextAttribute()
	 */
	protected WebApplicationContext findWebApplicationContext() {
		String attrName = getContextAttribute();
		if (attrName == null) {
			return null;
		}
		WebApplicationContext wac =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
		}
		return wac;
	}

	/**
	 * Instantiate the WebApplicationContext for this servlet, either a default
	 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
	 * or a {@link #setContextClass custom context class}, if set.
	 * 
	 * <p> 实例化此servlet的WebApplicationContext，默认为
	 * org.springframework.web.context.support.XmlWebApplicationContext或自定义上下文类（如果已设置）。
	 * 
	 * <p>This implementation expects custom contexts to implement the
	 * {@link org.springframework.web.context.ConfigurableWebApplicationContext}
	 * interface. Can be overridden in subclasses.
	 * 
	 * <p> 此实现需要自定义上下文来实现
	 * org.springframework.web.context.ConfigurableWebApplicationContext接口。 可以在子类中重写。
	 * 
	 * <p>Do not forget to register this servlet instance as application listener on the
	 * created context (for triggering its {@link #onRefresh callback}, and to call
	 * {@link org.springframework.context.ConfigurableApplicationContext#refresh()}
	 * before returning the context instance.
	 * 
	 * <p> 不要忘记在创建的上下文中将此servlet实例注册为应用程序侦听器（用于触发其回调，并在返回上下文实例之前调用
	 * org.springframework.context.ConfigurableApplicationContext.refresh（））。
	 * 
	 * @param parent the parent ApplicationContext to use, or {@code null} if none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {
		Class<?> contextClass = getContextClass();
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Servlet with name '" + getServletName() +
					"' will try to create custom WebApplicationContext context of class '" +
					contextClass.getName() + "'" + ", using parent context [" + parent + "]");
		}
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException(
					"Fatal initialization error in servlet with name '" + getServletName() +
					"': custom WebApplicationContext class [" + contextClass.getName() +
					"] is not of type ConfigurableWebApplicationContext");
		}
		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

		wac.setEnvironment(getEnvironment());
		wac.setParent(parent);
		wac.setConfigLocation(getContextConfigLocation());

		configureAndRefreshWebApplicationContext(wac);

		return wac;
	}

	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value
			// -> assign a more useful id based on available information
			
			// 应用程序上下文ID仍设置为其原始默认值 - >根据可用信息分配更有用的ID 
			if (this.contextId != null) {
				wac.setId(this.contextId);
			}
			else {
				// Generate default id...
				// 生成默认ID ...
				ServletContext sc = getServletContext();
				if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
					// Servlet <= 2.4: resort to name specified in web.xml, if any.
					// Servlet <= 2.4：求助于web.xml中指定的名称（如果有）。
					String servletContextName = sc.getServletContextName();
					if (servletContextName != null) {
						wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + servletContextName +
								"." + getServletName());
					}
					else {
						wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + getServletName());
					}
				}
				else {
					// Servlet 2.5's getContextPath available!
					// Servlet 2.5的getContextPath可用！
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
							ObjectUtils.getDisplayString(sc.getContextPath()) + "/" + getServletName());
				}
			}
		}

		wac.setServletContext(getServletContext());
		wac.setServletConfig(getServletConfig());
		wac.setNamespace(getNamespace());
		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

		// The wac environment's #initPropertySources will be called in any case when the context
		// is refreshed; do it eagerly here to ensure servlet property sources are in place for
		// use in any post-processing or initialization that occurs below prior to #refresh
		
		/*
		 * 在刷新上下文时，无论如何都会调用wac环境的#initPropertySources; 
		 * 在这里热切地确保servlet属性源适用于在#refresh之前发生的任何后处理或初始化
		 */
		ConfigurableEnvironment env = wac.getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
		}

		postProcessWebApplicationContext(wac);
		applyInitializers(wac);
		wac.refresh();
	}

	/**
	 * Instantiate the WebApplicationContext for this servlet, either a default
	 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
	 * or a {@link #setContextClass custom context class}, if set.
	 * Delegates to #createWebApplicationContext(ApplicationContext).
	 * 
	 * <p> 实例化此servlet的WebApplicationContext，默认为
	 * org.springframework.web.context.support.XmlWebApplicationContext或自定义上下文类（如果已设置）。
	 * 委托给#createWebApplicationContext（ApplicationContext）。
	 * 
	 * @param parent the parent WebApplicationContext to use, or {@code null} if none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 * @see #createWebApplicationContext(ApplicationContext)
	 */
	protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
		return createWebApplicationContext((ApplicationContext) parent);
	}

	/**
	 * Post-process the given WebApplicationContext before it is refreshed
	 * and activated as context for this servlet.
	 * 
	 * <p> 在给定的WebApplicationContext刷新并作为此servlet的上下文激活之前对其进行后处理。
	 * 
	 * <p>The default implementation is empty. {@code refresh()} will
	 * be called automatically after this method returns.
	 * 
	 * <p> 默认实现为空。 此方法返回后将自动调用refresh（）。
	 * 
	 * <p>Note that this method is designed to allow subclasses to modify the application
	 * context, while {@link #initWebApplicationContext} is designed to allow
	 * end-users to modify the context through the use of
	 * {@link ApplicationContextInitializer}s.
	 * 
	 * <p> 请注意，此方法旨在允许子类修改应用程序上下文，而initWebApplicationContext旨在允许最终用户通过使用
	 * ApplicationContextInitializers来修改上下文。
	 * 
	 * @param wac the configured WebApplicationContext (not refreshed yet)
	 * 
	 * <p> 配置的WebApplicationContext（尚未刷新）
	 * 
	 * @see #createWebApplicationContext
	 * @see #initWebApplicationContext
	 * @see ConfigurableWebApplicationContext#refresh()
	 */
	protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
	}

	/**
	 * Delegate the WebApplicationContext before it is refreshed to any
	 * {@link ApplicationContextInitializer} instances specified by the
	 * "contextInitializerClasses" servlet init-param.
	 * 
	 * <p> 在将WebApplicationContext刷新到由“contextInitializerClasses”
	 * servlet init-param指定的任何ApplicationContextInitializer实例之前，将其委派。
	 * 
	 * <p>See also {@link #postProcessWebApplicationContext}, which is designed to allow
	 * subclasses (as opposed to end-users) to modify the application context, and is
	 * called immediately before this method.
	 * 
	 * <p> 另请参见postProcessWebApplicationContext，它旨在允许子类（而不是最终用户）修改应用程序上下文，并在此方法之前立即调用。
	 * 
	 * @param wac the configured WebApplicationContext (not refreshed yet)
	 * 
	 * <p> 配置的WebApplicationContext（尚未刷新）
	 * 
	 * @see #createWebApplicationContext
	 * @see #postProcessWebApplicationContext
	 * @see ConfigurableApplicationContext#refresh()
	 */
	protected void applyInitializers(ConfigurableApplicationContext wac) {
		String globalClassNames = getServletContext().getInitParameter(ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM);
		if (globalClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		if (this.contextInitializerClasses != null) {
			for (String className : StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		AnnotationAwareOrderComparator.sort(this.contextInitializers);
		for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
			initializer.initialize(wac);
		}
	}

	@SuppressWarnings("unchecked")
	private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(
			String className, ConfigurableApplicationContext wac) {
		try {
			Class<?> initializerClass = ClassUtils.forName(className, wac.getClassLoader());
			Class<?> initializerContextClass =
					GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
			if (initializerContextClass != null) {
				Assert.isAssignable(initializerContextClass, wac.getClass(), String.format(
						"Could not add context initializer [%s] since its generic parameter [%s] " +
						"is not assignable from the type of application context used by this " +
						"framework servlet [%s]: ", initializerClass.getName(), initializerContextClass.getName(),
						wac.getClass().getName()));
			}
			return BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(String.format("Could not instantiate class [%s] specified " +
					"via 'contextInitializerClasses' init-param", className), ex);
		}
	}

	/**
	 * Return the ServletContext attribute name for this servlet's WebApplicationContext.
	 * 
	 * <p> 返回此servlet的WebApplicationContext的ServletContext属性名称。
	 * 
	 * <p>The default implementation returns
	 * {@code SERVLET_CONTEXT_PREFIX + servlet name}.
	 * 
	 * <p> 默认实现返回SERVLET_CONTEXT_PREFIX + servlet名称。
	 * 
	 * @see #SERVLET_CONTEXT_PREFIX
	 * @see #getServletName
	 */
	public String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}

	/**
	 * Return this servlet's WebApplicationContext.
	 */
	public final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}


	/**
	 * This method will be invoked after any bean properties have been set and
	 * the WebApplicationContext has been loaded. The default implementation is empty;
	 * subclasses may override this method to perform any initialization they require.
	 * 
	 * <p> 在设置任何bean属性并加载WebApplicationContext之后，将调用此方法。 
	 * 默认实现为空; 子类可以重写此方法以执行它们所需的任何初始化。
	 * 
	 * @throws ServletException in case of an initialization exception
	 * 
	 * <p> 在初始化异常的情况下
	 * 
	 */
	protected void initFrameworkServlet() throws ServletException {
	}

	/**
	 * Refresh this servlet's application context, as well as the
	 * dependent state of the servlet.
	 * 
	 * <p> 刷新此servlet的应用程序上下文以及servlet的依赖状态。
	 * 
	 * @see #getWebApplicationContext()
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	public void refresh() {
		WebApplicationContext wac = getWebApplicationContext();
		if (!(wac instanceof ConfigurableApplicationContext)) {
			throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
		}
		((ConfigurableApplicationContext) wac).refresh();
	}

	/**
	 * Callback that receives refresh events from this servlet's WebApplicationContext.
	 * 
	 * <p> 从此servlet的WebApplicationContext接收刷新事件的回调。
	 * 
	 * <p>The default implementation calls {@link #onRefresh},
	 * triggering a refresh of this servlet's context-dependent state.
	 * 
	 * <p> 默认实现调用onRefresh，触发刷新此servlet的依赖于上下文的状态。
	 * 
	 * @param event the incoming ApplicationContext event
	 * 
	 * <p> 传入的ApplicationContext事件
	 * 
	 */
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.refreshEventReceived = true;
		onRefresh(event.getApplicationContext());
	}

	/**
	 * Template method which can be overridden to add servlet-specific refresh work.
	 * Called after successful context refresh.
	 * 
	 * <p> 可以重写的模板方法，以添加特定于servlet的刷新工作。 成功完成上下文后调用。
	 * 
	 * <p>This implementation is empty.
	 * 
	 * <p> 此实现为空。
	 * 
	 * @param context the current WebApplicationContext
	 * @see #refresh()
	 */
	protected void onRefresh(ApplicationContext context) {
		// For subclasses: do nothing by default.
	}

	/**
	 * Close the WebApplicationContext of this servlet.
	 * 
	 * <p> 关闭此servlet的WebApplicationContext。
	 * 
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	@Override
	public void destroy() {
		getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
		if (this.webApplicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.webApplicationContext).close();
		}
	}


	/**
	 * Override the parent class implementation in order to intercept PATCH
	 * requests.
	 * 
	 * <p> 覆盖父类实现以拦截PATCH请求。
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String method = request.getMethod();
		if (method.equalsIgnoreCase(RequestMethod.PATCH.name())) {
			processRequest(request, response);
		}
		else {
			super.service(request, response);
		}
	}

	/**
	 * Delegate GET requests to processRequest/doService.
	 * 
	 * <p> 将GET请求委托给processRequest / doService。
	 * 
	 * <p>Will also be invoked by HttpServlet's default implementation of {@code doHead},
	 * with a {@code NoBodyResponse} that just captures the content length.
	 * 
	 * <p> 也将由HttpServlet的doHead的默认实现调用，其中NoBodyResponse仅捕获内容长度。
	 * 
	 * @see #doService
	 * @see #doHead
	 */
	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate POST requests to {@link #processRequest}.
	 * 
	 * <p> 将POST请求委托给processRequest。
	 * 
	 * @see #doService
	 */
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate PUT requests to {@link #processRequest}.
	 * 
	 * <p> 将PUT请求委托给processRequest。
	 * 
	 * @see #doService
	 */
	@Override
	protected final void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate DELETE requests to {@link #processRequest}.
	 * 
	 * <p> 将DELETE请求委托给processRequest。
	 * 
	 * @see #doService
	 */
	@Override
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate OPTIONS requests to {@link #processRequest}, if desired.
	 * 
	 * <p> 如果需要，将OPTIONS请求委托给processRequest。
	 * 
	 * <p>Applies HttpServlet's standard OPTIONS processing otherwise,
	 * and also if there is still no 'Allow' header set after dispatching.
	 * 
	 * <p> 否则应用HttpServlet的标准OPTIONS处理，如果在分派后仍然没有设置'Allow'标头。
	 * 
	 * @see #doService
	 */
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchOptionsRequest) {
			processRequest(request, response);
			if (response.containsHeader("Allow")) {
				// Proper OPTIONS response coming from a handler - we're done.
				return;
			}
		}

		// Use response wrapper for Servlet 2.5 compatibility where
		// the getHeader() method does not exist
		super.doOptions(request, new HttpServletResponseWrapper(response) {
			@Override
			public void setHeader(String name, String value) {
				if ("Allow".equals(name)) {
					value = (StringUtils.hasLength(value) ? value + ", " : "") + RequestMethod.PATCH.name();
				}
				super.setHeader(name, value);
			}
		});
	}

	/**
	 * Delegate TRACE requests to {@link #processRequest}, if desired.
	 * 
	 * <p> 如果需要，将TRACE请求委托给processRequest。
	 * 
	 * <p>Applies HttpServlet's standard TRACE processing otherwise.
	 * 
	 * <p> 否则应用HttpServlet的标准TRACE处理。
	 * 
	 * @see #doService
	 */
	@Override
	protected void doTrace(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchTraceRequest) {
			processRequest(request, response);
			if ("message/http".equals(response.getContentType())) {
				// Proper TRACE response coming from a handler - we're done.
				return;
			}
		}
		super.doTrace(request, response);
	}

	/**
	 * Process this request, publishing an event regardless of the outcome.
	 * 
	 * <p> 处理此请求，无论结果如何都发布事件
	 * 
	 * <p>The actual event handling is performed by the abstract
	 * {@link #doService} template method.
	 * 
	 * <p> 实际的事件处理由抽象的doService模板方法执行。
	 * 
	 */
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContext localeContext = buildLocaleContext(request);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

		initContextHolders(request, localeContext, requestAttributes);

		try {
			doService(request, response);
		}
		catch (ServletException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Throwable ex) {
			failureCause = ex;
			throw new NestedServletException("Request processing failed", ex);
		}

		finally {
			resetContextHolders(request, previousLocaleContext, previousAttributes);
			if (requestAttributes != null) {
				requestAttributes.requestCompleted();
			}

			if (logger.isDebugEnabled()) {
				if (failureCause != null) {
					this.logger.debug("Could not complete request", failureCause);
				}
				else {
					if (asyncManager.isConcurrentHandlingStarted()) {
						logger.debug("Leaving response open for concurrent processing");
					}
					else {
						this.logger.debug("Successfully completed request");
					}
				}
			}

			publishRequestHandledEvent(request, startTime, failureCause);
		}
	}

	/**
	 * Build a LocaleContext for the given request, exposing the request's
	 * primary locale as current locale.
	 * 
	 * <p> 为给定请求构建LocaleContext，将请求的主要区域设置公开为当前区域设置。
	 * 
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext, or {@code null} if none to bind
	 * 
	 * <p> 相应的LocaleContext，如果没有要绑定，则返回null
	 * 
	 * @see LocaleContextHolder#setLocaleContext
	 */
	protected LocaleContext buildLocaleContext(HttpServletRequest request) {
		return new SimpleLocaleContext(request.getLocale());
	}

	/**
	 * Build ServletRequestAttributes for the given request (potentially also
	 * holding a reference to the response), taking pre-bound attributes
	 * (and their type) into consideration.
	 * 
	 * <p> 为给定请求构建ServletRequestAttributes（可能还包含对响应的引用），考虑预绑定属性（及其类型）。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param previousAttributes pre-bound RequestAttributes instance, if any
	 * 
	 * <p> 预绑定的RequestAttributes实例（如果有）
	 * 
	 * @return the ServletRequestAttributes to bind, or {@code null} to preserve
	 * the previously bound instance (or not binding any, if none bound before)
	 * 
	 * <p> 要绑定的ServletRequestAttributes，要么为null以保留以前绑定的实例（或者如果之前没有绑定则不绑定任何实例）
	 * 
	 * @see RequestContextHolder#setRequestAttributes
	 */
	protected ServletRequestAttributes buildRequestAttributes(
			HttpServletRequest request, HttpServletResponse response, RequestAttributes previousAttributes) {

		if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
			return new ServletRequestAttributes(request);
		}
		else {
			return null;  // preserve the pre-bound RequestAttributes instance
		}
	}

	private void initContextHolders(
			HttpServletRequest request, LocaleContext localeContext, RequestAttributes requestAttributes) {

		if (localeContext != null) {
			LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
		}
		if (requestAttributes != null) {
			RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Bound request context to thread: " + request);
		}
	}

	private void resetContextHolders(HttpServletRequest request,
			LocaleContext prevLocaleContext, RequestAttributes previousAttributes) {

		LocaleContextHolder.setLocaleContext(prevLocaleContext, this.threadContextInheritable);
		RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
		if (logger.isTraceEnabled()) {
			logger.trace("Cleared thread-bound request context: " + request);
		}
	}

	private void publishRequestHandledEvent(HttpServletRequest request, long startTime, Throwable failureCause) {
		if (this.publishEvents) {
			// Whether or not we succeeded, publish an event.
			long processingTime = System.currentTimeMillis() - startTime;
			this.webApplicationContext.publishEvent(
					new ServletRequestHandledEvent(this,
							request.getRequestURI(), request.getRemoteAddr(),
							request.getMethod(), getServletConfig().getServletName(),
							WebUtils.getSessionId(request), getUsernameForRequest(request),
							processingTime, failureCause));
		}
	}

	/**
	 * Determine the username for the given request.
	 * 
	 * <p> 确定给定请求的用户名。
	 * 
	 * <p>The default implementation takes the name of the UserPrincipal, if any.
	 * Can be overridden in subclasses.
	 * 
	 * <p> 默认实现采用UserPrincipal的名称（如果有）。 可以在子类中重写。
	 * 
	 * @param request current HTTP request
	 * @return the username, or {@code null} if none found
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	protected String getUsernameForRequest(HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		return (userPrincipal != null ? userPrincipal.getName() : null);
	}


	/**
	 * Subclasses must implement this method to do the work of request handling,
	 * receiving a centralized callback for GET, POST, PUT and DELETE.
	 * 
	 * <p> 子类必须实现此方法来执行请求处理，接收GET，POST，PUT和DELETE的集中回调。
	 * 
	 * <p>The contract is essentially the same as that for the commonly overridden
	 * {@code doGet} or {@code doPost} methods of HttpServlet.
	 * 
	 * <p> 该契约基本上与HttpServlet的通常重写的doGet或doPost方法的契约相同。
	 * 
	 * <p>This class intercepts calls to ensure that exception handling and
	 * event publication takes place.
	 * 
	 * <p> 此类拦截调用以确保发生异常处理和事件发布。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of any kind of processing failure
	 * 
	 * <p> 在任何处理失败的情况下
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet
	 * @see javax.servlet.http.HttpServlet#doPost
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
			throws Exception;


	/**
	 * ApplicationListener endpoint that receives events from this servlet's WebApplicationContext
	 * only, delegating to {@code onApplicationEvent} on the FrameworkServlet instance.
	 * 
	 * <p> ApplicationListener端点，仅从此servlet的WebApplicationContext接收事件，
	 * 委派给FrameworkServlet实例上的onApplicationEvent。
	 * 
	 */
	private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

		public void onApplicationEvent(ContextRefreshedEvent event) {
			FrameworkServlet.this.onApplicationEvent(event);
		}
	}


	/**
	 * CallableProcessingInterceptor implementation that initializes and resets
	 * FrameworkServlet's context holders, i.e. LocaleContextHolder and RequestContextHolder.
	 * 
	 * <p> CallableProcessingInterceptor实现，初始化并重置FrameworkServlet的上下文持有者，
	 * 即LocaleContextHolder和RequestContextHolder。
	 * 
	 */
	private class RequestBindingInterceptor extends CallableProcessingInterceptorAdapter {

		@Override
		public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				HttpServletResponse response = webRequest.getNativeRequest(HttpServletResponse.class);
				initContextHolders(request, buildLocaleContext(request), buildRequestAttributes(request, response, null));
			}
		}
		@Override
		public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, Object concurrentResult) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				resetContextHolders(request, null, null);
			}
		}
	}

}
