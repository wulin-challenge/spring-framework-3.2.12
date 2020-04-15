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

package org.springframework.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * Central dispatcher for HTTP request handlers/controllers, e.g. for web UI controllers or HTTP-based remote service
 * exporters. Dispatches to registered handlers for processing a web request, providing convenient mapping and exception
 * handling facilities.
 * 
 * <p> HTTP请求处理程序/控制器的中央调度程序，例如 用于Web UI控制器或基于HTTP的远程服务导出器。 调度到已注册的处理程序以处理Web请求，提供方便的映射和异常处理工具。
 *
 * <p>This servlet is very flexible: It can be used with just about any workflow, with the installation of the
 * appropriate adapter classes. It offers the following functionality that distinguishes it from other request-driven
 * web MVC frameworks:
 * 
 * <p> 这个servlet非常灵活：它可以与几乎任何工作流一起使用，并安装适当的适配器类。 它提供以下功能，使其与其他请求驱动的Web MVC框架区别开来：
 *
 * <ul> 
 * <li>It is based around a JavaBeans configuration mechanism.
 * 
 * <li>它基于JavaBeans配置机制.
 * 
 * <li> 
 *
 * <li>It can use any {@link HandlerMapping} implementation - pre-built or provided as part of an application - to
 * control the routing of requests to handler objects. Default is {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}
 * and {@link org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping}. HandlerMapping objects
 * can be defined as beans in the servlet's application context, implementing the HandlerMapping interface, overriding
 * the default HandlerMapping if present. HandlerMappings can be given any bean name (they are tested by type).
 * 
 * <li> 它可以使用任何HandlerMapping实现 - 预构建或作为应用程序的一部分提供 - 来控制对处理程序对象的请求路由。 默认为
 * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}和
 * {@link org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping}。 
 * HandlerMapping对象可以在servlet的应用程序上下文中定义为bean，实现HandlerMapping接口，覆盖默认的HandlerMapping（如果存在）。 
 * HandlerMappings可以被赋予任何bean名称（它们按类型进行测试）。
 * 
 * <li> 
 *
 * <li>It can use any {@link HandlerAdapter}; this allows for using any handler interface. Default adapters are {@link
 * org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter}, {@link org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter},
 * for Spring's {@link org.springframework.web.HttpRequestHandler} and {@link org.springframework.web.servlet.mvc.Controller}
 * interfaces, respectively. A default {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter}
 * will be registered as well. HandlerAdapter objects can be added as beans in the application context, overriding the
 * default HandlerAdapters. Like HandlerMappings, HandlerAdapters can be given any bean name (they are tested by type).
 * 
 * <li> 它可以使用任何HandlerAdapter; 这允许使用任何处理程序接口。 默认适配器是
 * org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter，
 * org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter，用于Spring的
 * org.springframework.web.HttpRequestHandler和org.springframework.web.servlet.mvc.Controller接口， 分别。 
 * 还将注册默认的org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter。 
 * HandlerAdapter对象可以作为bean添加到应用程序上下文中，覆盖默认的HandlerAdapter。 与HandlerMappings一样，
 * HandlerAdapters可以被赋予任何bean名称（它们按类型进行测试）。
 * 
 * <li> 
 *
 * <li>The dispatcher's exception resolution strategy can be specified via a {@link HandlerExceptionResolver}, for
 * example mapping certain exceptions to error pages. Default are
 * {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerExceptionResolver},
 * {@link org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver}, and
 * {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}. These HandlerExceptionResolvers can be overridden
 * through the application context. HandlerExceptionResolver can be given any bean name (they are tested by type).
 * 
 * <li> 可以通过HandlerExceptionResolver指定调度程序的异常解析策略，例如将某些异常映射到错误页面。 默认值为
 * org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerExceptionResolver，
 * org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver和
 * org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver。 可以通过应用程序上下文覆盖这些
 * HandlerExceptionResolvers。 HandlerExceptionResolver可以被赋予任何bean名称（它们按类型进行测试）。
 *  
 * <li> 
 *
 * <li>Its view resolution strategy can be specified via a {@link ViewResolver} implementation, resolving symbolic view
 * names into View objects. Default is {@link org.springframework.web.servlet.view.InternalResourceViewResolver}.
 * ViewResolver objects can be added as beans in the application context, overriding the default ViewResolver.
 * ViewResolvers can be given any bean name (they are tested by type).
 * 
 * <li> 可以通过ViewResolver实现指定其视图解析策略，将符号视图名称解析为View对象。 默认为
 * org.springframework.web.servlet.view.InternalResourceViewResolver。 ViewResolver对象可以作为bean
 * 添加到应用程序上下文中，覆盖默认的ViewResolver。 ViewResolvers可以被赋予任何bean名称（它们按类型进行测试）。
 * 
 * <li> 
 *
 * <li>If a {@link View} or view name is not supplied by the user, then the configured {@link
 * RequestToViewNameTranslator} will translate the current request into a view name. The corresponding bean name is
 * "viewNameTranslator"; the default is {@link org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator}.
 * 
 * <li> 如果用户未提供视图或视图名称，则配置的RequestToViewNameTranslator会将当前请求转换为视图名称。 
 * 相应的bean名称是“viewNameTranslator”; 默认值为
 * {@link org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator}
 * 
 * <li> 
 *
 * <li>The dispatcher's strategy for resolving multipart requests is determined by a {@link
 * org.springframework.web.multipart.MultipartResolver} implementation. Implementations for Jakarta Commons FileUpload
 * and Jason Hunter's COS are included; the typical choise is {@link org.springframework.web.multipart.commons.CommonsMultipartResolver}.
 * The MultipartResolver bean name is "multipartResolver"; default is none.
 * 
 * <li> 为解析多个请求的调度程序策略是由
 * org.springframework.web.multipart.MultipartResolver实现确定。 包括Jakarta Commons FileUpload和Jason Hunter的
 * COS的实现; 典型的选择是{@link org.springframework.web.multipart.commons.CommonsMultipartResolver}.
 *  MultipartResolver bean名称是“multipartResolver”; 默认为none。
 * 
 * <li> 
 *
 * <li>Its locale resolution strategy is determined by a {@link LocaleResolver}. Out-of-the-box implementations work via
 * HTTP accept header, cookie, or session. The LocaleResolver bean name is "localeResolver"; default is {@link
 * org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver}.
 * 
 * <li> 其区域设置解析策略由LocaleResolver确定。 开箱即用的实现通过HTTP接受标头，cookie或会话工作。 LocaleResolver bean名称是“localeResolver”; 
 * 默认是org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver。
 * 
 * <li> 
 *
 * <li>Its theme resolution strategy is determined by a {@link ThemeResolver}. Implementations for a fixed theme and for
 * cookie and session storage are included. The ThemeResolver bean name is "themeResolver"; default is {@link
 * org.springframework.web.servlet.theme.FixedThemeResolver}.
 * 
 *  * <li> 其主题解析策略由ThemeResolver决定。 包括固定主题以及cookie和会话存储的实现。 ThemeResolver bean名称是“themeResolver”; 
 * 默认是org.springframework.web.servlet.theme.FixedThemeResolver。
 * 
 * <li> 
 * 
 * </ul>
 * 
 * <p><b>NOTE: The {@code @RequestMapping} annotation will only be processed if a corresponding
 * {@code HandlerMapping} (for type level annotations) and/or {@code HandlerAdapter} (for method level
 * annotations) is present in the dispatcher.</b> This is the case by default. However, if you are defining custom
 * {@code HandlerMappings} or {@code HandlerAdapters}, then you need to make sure that a corresponding custom
 * {@code DefaultAnnotationHandlerMapping} and/or {@code AnnotationMethodHandlerAdapter} is defined as well -
 * provided that you intend to use {@code @RequestMapping}.
 * 
 * <p> 注意：只有在调度程序中存在相应的HandlerMapping（用于类型级别注释）和/或HandlerAdapter（用于方法级别注释）时，才会处理@RequestMapping注释。 
 * 默认情况下就是这种情况。 但是，如果要定义自定义HandlerMappings或HandlerAdapter，则需要确保定义相应的自定义DefaultAnnotationHandlerMapping
 * 和/或AnnotationMethodHandlerAdapter  - 前提是您打算使用@RequestMapping。
 *
 * <p><b>A web application can define any number of DispatcherServlets.</b> Each servlet will operate in its own
 * namespace, loading its own application context with mappings, handlers, etc. Only the root application context as
 * loaded by {@link org.springframework.web.context.ContextLoaderListener}, if any, will be shared.
 * 
 * <p> Web应用程序可以定义任意数量的DispatcherServlet。 每个servlet将在其自己的命名空间中运行，使用映射，处理程序等加载其自己的应用程序上下文。
 * 只有org.springframework.web.context.ContextLoaderListener加载的根应用程序上下文（如果有）将被共享。
 *
 * <p>As of Spring 3.1, {@code DispatcherServlet} may now be injected with a web
 * application context, rather than creating its own internally. This is useful in Servlet
 * 3.0+ environments, which support programmatic registration of servlet instances. See
 * {@link #DispatcherServlet(WebApplicationContext)} Javadoc for details.
 * 
 * <p> 从Spring 3.1开始，DispatcherServlet现在可以注入Web应用程序上下文，而不是在内部创建自己的上下文。 
 * 这在Servlet 3.0+环境中很有用，它支持servlet实例的编程注册。 
 * 有关详细信息，请参阅DispatcherServlet（WebApplicationContext）Javadoc。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @see org.springframework.web.HttpRequestHandler
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.context.ContextLoaderListener
 */
@SuppressWarnings("serial")
public class DispatcherServlet extends FrameworkServlet {

	/** Well-known name for the MultipartResolver object in the bean factory for this namespace. */
	/** 此命名空间的Bean工厂中的MultipartResolver对象的已知名称。 */
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

	/** Well-known name for the LocaleResolver object in the bean factory for this namespace. */
	/** 此命名空间的Bean工厂中LocaleResolver对象的已知名称。 */
	public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

	/** Well-known name for the ThemeResolver object in the bean factory for this namespace. */
	/** Bean命名空间中此Theme命名空间的ThemeResolver对象的已知名称。 */
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

	/**
	 * Well-known name for the HandlerMapping object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerMappings" is turned off.
	 * 
	 * <p> 此命名空间的Bean工厂中HandlerMapping对象的已知名称。 仅在“detectAllHandlerMappings”关闭时使用。
	 * @see #setDetectAllHandlerMappings
	 */
	public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

	/**
	 * Well-known name for the HandlerAdapter object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerAdapters" is turned off.
	 * 
	 * <p> 此命名空间的Bean工厂中HandlerAdapter对象的已知名称。 仅在“detectAllHandlerAdapters”关闭时使用。
	 * @see #setDetectAllHandlerAdapters
	 */
	public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

	/**
	 * Well-known name for the HandlerExceptionResolver object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerExceptionResolvers" is turned off.
	 * 
	 * <p> 此命名空间的Bean工厂中HandlerExceptionResolver对象的已知名称。 仅在“detectAllHandlerExceptionResolvers”关闭时使用。
	 * 
	 * @see #setDetectAllHandlerExceptionResolvers
	 */
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

	/**
	 * Well-known name for the RequestToViewNameTranslator object in the bean factory for this namespace.
	 * 
	 * <p> Bean命名空间中此命名空间的RequestToViewNameTranslator对象的已知名称。
	 */
	public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 * Only used when "detectAllViewResolvers" is turned off.
	 * 
	 * <p> 此命名空间的Bean工厂中的ViewResolver对象的已知名称。 仅在“detectAllViewResolvers”关闭时使用。
	 * 
	 * @see #setDetectAllViewResolvers
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Well-known name for the FlashMapManager object in the bean factory for this namespace.
	 * 
	 * <p> 此命名空间的Bean工厂中FlashMapManager对象的已知名称。
	 */
	public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

	/**
	 * Request attribute to hold the current web application context.
	 * Otherwise only the global web app context is obtainable by tags etc.
	 * 
	 * <p> 请求属性以持有当前Web应用程序上下文。 否则，只有标签等可以获得全局Web应用程序上下文。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getWebApplicationContext
	 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

	/**
	 * Request attribute to hold the current LocaleResolver, retrievable by views.
	 * 
	 * <p> Request属性用于持有当前LocaleResolver，可通过视图检索。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocaleResolver
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

	/**
	 * Request attribute to hold the current ThemeResolver, retrievable by views.
	 * 
	 * <p> Request属性用于保存当前ThemeResolver，可通过视图检索。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeResolver
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

	/**
	 * Request attribute to hold the current ThemeSource, retrievable by views.
	 * 
	 * <p> Request属性用于保存当前ThemeSource，可通过视图检索。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeSource
	 */
	public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

	/**
	 * Name of request attribute that holds a read-only {@code Map<String,?>}
	 * with "input" flash attributes saved by a previous request, if any.
	 * 
	 * <p> 请求属性的名称，其中包含只读Map <String，？>，其中包含前一个请求保存的“input”flash属性（如果有）。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getInputFlashMap(HttpServletRequest)
	 */
	public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";

	/**
	 * Name of request attribute that holds the "output" {@link FlashMap} with
	 * attributes to save for a subsequent request.
	 * 
	 * <p> 请求属性的名称，用于保存“输出”FlashMap，其中包含要为后续请求保存的属性。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getOutputFlashMap(HttpServletRequest)
	 */
	public static final String OUTPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";

	/**
	 * Name of request attribute that holds the {@link FlashMapManager}.
	 * 
	 * <p> 包含FlashMapManager的请求属性的名称。
	 * 
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getFlashMapManager(HttpServletRequest)
	 */
	public static final String FLASH_MAP_MANAGER_ATTRIBUTE = DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";

	/** Log category to use when no mapped handler is found for a request. */
	/** 未找到请求的映射处理程序时要使用的日志类别。 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	/**
	 * Name of the class path resource (relative to the DispatcherServlet class)
	 * that defines DispatcherServlet's default strategy names.
	 * 
	 * <p> 类路径资源的名称（相对于DispatcherServlet类），它定义DispatcherServlet的默认策略名称。
	 */
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";


	/** Additional logger to use when no mapped handler is found for a request. */
	/** 未找到请求的映射处理程序时使用的其他记录器。 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

	private static final Properties defaultStrategies;

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		
		/*
		 * 从属性文件加载默认策略实现。 这当前是严格内部的，不应由应用程序开发人员定制。
		 */
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
		}
	}

	/** Detect all HandlerMappings or just expect "handlerMapping" bean? */
	/**检测所有HandlerMappings或只是期望“handlerMapping”bean？*/
	private boolean detectAllHandlerMappings = true;

	/** Detect all HandlerAdapters or just expect "handlerAdapter" bean? */
	/** 检测所有HandlerAdapter或只是期望“handlerAdapter”bean？*/
	private boolean detectAllHandlerAdapters = true;

	/** Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean? */
	/** 检测所有HandlerExceptionResolvers或只是期望“handlerExceptionResolver”bean？ */
	private boolean detectAllHandlerExceptionResolvers = true;

	/** Detect all ViewResolvers or just expect "viewResolver" bean? */
	/** 检测所有ViewResolvers或只是期望“viewResolver”bean？ */
	private boolean detectAllViewResolvers = true;

	/** Perform cleanup of request attributes after include request? */
	/** 在包含请求后执行请求属性的清理？ */
	private boolean cleanupAfterInclude = true;

	/** MultipartResolver used by this servlet */
	/** 此servlet使用的MultipartResolver */
	private MultipartResolver multipartResolver;

	/** LocaleResolver used by this servlet */
	/** 此servlet使用的LocaleResolver */
	private LocaleResolver localeResolver;

	/** ThemeResolver used by this servlet */
	/** 这个servlet使用的ThemeResolver */
	private ThemeResolver themeResolver;

	/** List of HandlerMappings used by this servlet */
	/** 此servlet使用的HandlerMappings列表 */
	private List<HandlerMapping> handlerMappings;

	/** List of HandlerAdapters used by this servlet */
	/** 此servlet使用的HandlerAdapter列表 */
	private List<HandlerAdapter> handlerAdapters;

	/** List of HandlerExceptionResolvers used by this servlet */
	/** 此servlet使用的HandlerExceptionResolvers列表 */
	private List<HandlerExceptionResolver> handlerExceptionResolvers;

	/** RequestToViewNameTranslator used by this servlet */
	/** 此servlet使用的RequestToViewNameTranslator */
	private RequestToViewNameTranslator viewNameTranslator;

	/** FlashMapManager used by this servlet */
	/** 此servlet使用的FlashMapManager */
	private FlashMapManager flashMapManager;

	/** List of ViewResolvers used by this servlet */
	/** 此servlet使用的ViewResolvers列表 */
	private List<ViewResolver> viewResolvers;

	/**
	 * Create a new {@code DispatcherServlet} that will create its own internal web
	 * application context based on defaults and values provided through servlet
	 * init-params. Typically used in Servlet 2.5 or earlier environments, where the only
	 * option for servlet registration is through {@code web.xml} which requires the use
	 * of a no-arg constructor.
	 * 
	 * <p> 创建一个新的DispatcherServlet，它将根据servlet init-params提供的缺省值和值创建自己的内部Web应用程序上下文。 
	 * 通常在Servlet 2.5或更早版本的环境中使用，其中servlet注册的唯一选项是通过web.xml，它需要使用no-arg构造函数。
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
	 * indicates which {@code ApplicationContextInitializer} classes should be used to
	 * further configure the internal application context prior to refresh().
	 * 
	 * <p> 调用setContextInitializerClasses（init-param'contextInitializerClasses'）
	 * 指示在refresh（）之前应该使用哪些ApplicationContextInitializer类来进一步配置内部应用程序上下文。
	 * 
	 * @see #DispatcherServlet(WebApplicationContext)
	 */
	public DispatcherServlet() {
		super();
	}

	/**
	 * Create a new {@code DispatcherServlet} with the given web application context. This
	 * constructor is useful in Servlet 3.0+ environments where instance-based registration
	 * of servlets is possible through the {@link ServletContext#addServlet} API.
	 * 
	 * <p> 使用给定的Web应用程序上下文创建新的DispatcherServlet。 这个构造函数在Servlet 3.0+环境中很有用，
	 * 在这些环境中，ServletContext.addServlet API可以通过基于实例的servlet注册。
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
	 * ConfigurableApplicationContext#refresh() refreshed}. If it has <strong>not</strong>
	 * already been refreshed (the recommended approach), then the following will occur:
	 * 
	 * <p> 给定的Web应用程序上下文可能已经或可能尚未刷新。 如果尚未刷新（建议的方法），则会发生以下情况：
	 * 
	 * <ul>
	 * <li>If the given context does not already have a {@linkplain
	 * ConfigurableApplicationContext#setParent parent}, the root application context
	 * will be set as the parent.</li>
	 * 
	 * <li> 如果给定的上下文尚未具有父级，则根应用程序上下文将设置为父级。
	 * 
	 * <li> 
	 * 
	 * <li>If the given context has not already been assigned an {@linkplain
	 * ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * 
	 * <li> 如果尚未为给定的上下文分配ID，则将为其分配一个ID
	 * 
	 * <li> 
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * 
	 * <li> ServletContext和ServletConfig对象将委派给应用程序上下文
	 * 
	 * <li> 
	 * 
	 * <li>{@link #postProcessWebApplicationContext} will be called</li>
	 * 
	 * <li> 将调用postProcessWebApplicationContext
	 * 
	 * <li> 
	 * 
	 * <li>Any {@code ApplicationContextInitializer}s specified through the
	 * "contextInitializerClasses" init-param or through the {@link
	 * #setContextInitializers} property will be applied.</li>
	 * 
	 * <li> 将应用通过“contextInitializerClasses”init-param或setContextInitializers属性指定的任何ApplicationContextInitializers。
	 * 
	 * <li> 
	 * 
	 * <li>{@link ConfigurableApplicationContext#refresh refresh()} will be called if the
	 * context implements {@link ConfigurableApplicationContext}</li>
	 * 
	 * <li> 如果上下文实现ConfigurableApplicationContext，则将调用refresh（）
	 * 
	 * <li> 
	 * </ul>
	 * If the context has already been refreshed, none of the above will occur, under the
	 * assumption that the user has performed these actions (or not) per their specific
	 * needs.
	 * 
	 * <p> 如果上下文已经刷新，则假设用户已根据其特定需要执行（或不执行）这些操作，则不会发生上述情况。
	 * 
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * 
	 * <p> 有关用法示例，请参阅org.springframework.web.WebApplicationInitializer。
	 * 
	 * @param webApplicationContext the context to use - 要使用的上下文
	 * 
	 * @see #initWebApplicationContext
	 * @see #configureAndRefreshWebApplicationContext
	 * @see org.springframework.web.WebApplicationInitializer
	 */
	public DispatcherServlet(WebApplicationContext webApplicationContext) {
		super(webApplicationContext);
	}

	/**
	 * Set whether to detect all HandlerMapping beans in this servlet's context. Otherwise,
	 * just a single bean with name "handlerMapping" will be expected.
	 * 
	 * <p> 设置是否在此servlet的上下文中检测所有HandlerMapping bean。 否则，只需要一个名为“handlerMapping”的bean。
	 * 
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * HandlerMapping, despite multiple HandlerMapping beans being defined in the context.
	 * 
	 * <p> 默认为“true”。 如果您希望此servlet使用单个HandlerMapping，请关闭此功能，尽管在上下文中定义了多个HandlerMapping bean。
	 * 
	 */
	public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
		this.detectAllHandlerMappings = detectAllHandlerMappings;
	}

	/**
	 * Set whether to detect all HandlerAdapter beans in this servlet's context. Otherwise,
	 * just a single bean with name "handlerAdapter" will be expected.
	 * 
	 * <p> 设置是否在此servlet的上下文中检测所有HandlerAdapter bean。 否则，只需要一个名为“handlerAdapter”的bean。
	 * 
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * HandlerAdapter, despite multiple HandlerAdapter beans being defined in the context.
	 * 
	 * <p> 默认为“true”。 如果您希望此servlet使用单个HandlerAdapter，请关闭此项，尽管在上下文中定义了多个HandlerAdapter bean。
	 * 
	 */
	public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
		this.detectAllHandlerAdapters = detectAllHandlerAdapters;
	}

	/**
	 * Set whether to detect all HandlerExceptionResolver beans in this servlet's context. Otherwise,
	 * just a single bean with name "handlerExceptionResolver" will be expected.
	 * 
	 * <p> 设置是否在此servlet的上下文中检测所有HandlerExceptionResolver bean。 否则，只需要一个名为“handlerExceptionResolver”的bean。
	 * 
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * HandlerExceptionResolver, despite multiple HandlerExceptionResolver beans being defined in the context.
	 * 
	 * <p> 默认为“true”。 如果您希望此servlet使用单个HandlerExceptionResolver，尽管在上下文中定义了多个HandlerExceptionResolver bean，请将其关闭。
	 */
	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}

	/**
	 * Set whether to detect all ViewResolver beans in this servlet's context. Otherwise,
	 * just a single bean with name "viewResolver" will be expected.
	 * 
	 * <p> 设置是否在此servlet的上下文中检测所有ViewResolver bean。 否则，只需要一个名为“viewResolver”的bean。
	 * 
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * ViewResolver, despite multiple ViewResolver beans being defined in the context.
	 * 
	 * <p> 默认为“true”。 如果您希望此servlet使用单个ViewResolver，尽管在上下文中定义了多个ViewResolver bean，请将其关闭。
	 */
	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
	}

	/**
	 * Set whether to perform cleanup of request attributes after an include request, that is,
	 * whether to reset the original state of all request attributes after the DispatcherServlet
	 * has processed within an include request. Otherwise, just the DispatcherServlet's own
	 * request attributes will be reset, but not model attributes for JSPs or special attributes
	 * set by views (for example, JSTL's).
	 * 
	 * <p> 设置是否在包含请求后执行请求属性的清除，即在包含请求中处理DispatcherServlet之后是否重置所有请求属性的原始状态。 
	 * 否则，只会重置DispatcherServlet自己的请求属性，但不会重置JSP的模型属性或视图设置的特殊属性（例如，JSTL）。
	 * 
	 * <p>Default is "true", which is strongly recommended. Views should not rely on request attributes
	 * having been set by (dynamic) includes. This allows JSP views rendered by an included controller
	 * to use any model attributes, even with the same names as in the main JSP, without causing side
	 * effects. Only turn this off for special needs, for example to deliberately allow main JSPs to
	 * access attributes from JSP views rendered by an included controller.
	 * 
	 * <p> 默认为“true”，强烈建议使用。 视图不应该依赖于由（动态）包含设置的请求属性。 这允许包含的控制器呈现的JSP视图使用任何模型属性，
	 * 即使使用与主JSP中相同的名称，也不会产生副作用。 仅针对特殊需要关闭此选项，例如，故意允许主JSP从包含的控制器呈现的JSP视图中访问属性。
	 * 
	 */
	public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
		this.cleanupAfterInclude = cleanupAfterInclude;
	}

	/**
	 * This implementation calls {@link #initStrategies}.
	 * 
	 * <p> 此实现调用initStrategies。
	 */
	@Override
	protected void onRefresh(ApplicationContext context) {
		initStrategies(context);
	}

	/**
	 * Initialize the strategy objects that this servlet uses.
	 * 
	 * <p> 初始化此servlet使用的策略对象。
	 * 
	 * <p>May be overridden in subclasses in order to initialize further strategy objects.
	 * 
	 * <p> 可以在子类中重写以初始化其他策略对象。
	 */
	protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		initHandlerExceptionResolvers(context);
		initRequestToViewNameTranslator(context);
		initViewResolvers(context);
		initFlashMapManager(context);
	}

	/**
	 * Initialize the MultipartResolver used by this class.
	 * 
	 * <p> 初始化此类使用的MultipartResolver。
	 * 
	 * <p>If no bean is defined with the given name in the BeanFactory for this namespace,
	 * no multipart handling is provided.
	 * 
	 * <p> 如果没有在BeanFactory中为此命名空间定义具有给定名称的bean，则不提供多部分处理。
	 */
	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
						"': no multipart request handling provided");
			}
		}
	}

	/**
	 * Initialize the LocaleResolver used by this class.
	 * 
	 * <p> 初始化此类使用的LocaleResolver。
	 * 
	 * <p>If no bean is defined with the given name in the BeanFactory for this namespace,
	 * we default to AcceptHeaderLocaleResolver.
	 * 
	 * <p> 如果在此命名空间的BeanFactory中没有使用给定名称定义bean，则默认为AcceptHeaderLocaleResolver。
	 */
	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME +
						"': using default [" + this.localeResolver + "]");
			}
		}
	}

	/**
	 * Initialize the ThemeResolver used by this class.
	 * 
	 * <p> 初始化此类使用的ThemeResolver。
	 * 
	 * <p>If no bean is defined with the given name in the BeanFactory for this namespace,
	 * we default to a FixedThemeResolver.
	 * 
	 * <p> 如果在此命名空间的BeanFactory中没有使用给定名称定义bean，则默认为FixedThemeResolver。
	 * 
	 */
	private void initThemeResolver(ApplicationContext context) {
		try {
			this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using ThemeResolver [" + this.themeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME + "': using default [" +
								this.themeResolver + "]");
			}
		}
	}

	/**
	 * Initialize the HandlerMappings used by this class.
	 * 
	 * <p> 初始化此类使用的HandlerMappings。
	 * 
	 * <p>If no HandlerMapping beans are defined in the BeanFactory for this namespace,
	 * we default to BeanNameUrlHandlerMapping.
	 * 
	 * <p> 如果没有在BeanFactory中为此命名空间定义HandlerMapping bean，我们默认为BeanNameUrlHandlerMapping。
	 */
	private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;

		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerMapping> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				OrderComparator.sort(this.handlerMappings);
			}
		}
		else {
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		
		// 如果没有找到其他映射，请确保我们至少有一个HandlerMapping，通过注册默认的HandlerMapping。
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the HandlerAdapters used by this class.
	 * 
	 * <p> 初始化此类使用的HandlerAdapter。
	 * 
	 * <p>If no HandlerAdapter beans are defined in the BeanFactory for this namespace,
	 * we default to SimpleControllerHandlerAdapter.
	 * 
	 * <p> 如果BeanFactory中没有为此命名空间定义HandlerAdapter bean，则默认为SimpleControllerHandlerAdapter。
	 */
	private void initHandlerAdapters(ApplicationContext context) {
		this.handlerAdapters = null;

		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
			// 在ApplicationContext中查找所有HandlerAdapter，包括祖先上下文。
			Map<String, HandlerAdapter> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				// 我们按顺序排列HandlerAdapters。
				OrderComparator.sort(this.handlerAdapters);
			}
		}
		else {
			try {
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
				// 忽略，我们稍后会添加一个默认的HandlerAdapter。
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		
		// 如果没有找到其他适配器，请通过注册默认的HandlerAdapter来确保我们至少有一些HandlerAdapter。
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the HandlerExceptionResolver used by this class.
	 * 
	 * <p> 初始化此类使用的HandlerExceptionResolver。
	 * 
	 * <p>If no bean is defined with the given name in the BeanFactory for this namespace,
	 * we default to no exception resolver.
	 * 
	 * <p> 如果在此命名空间的BeanFactory中没有使用给定名称定义bean，则默认情况下不会出现异常解析程序。
	 */
	private void initHandlerExceptionResolvers(ApplicationContext context) {
		this.handlerExceptionResolvers = null;

		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
				// We keep HandlerExceptionResolvers in sorted order.
				OrderComparator.sort(this.handlerExceptionResolvers);
			}
		}
		else {
			try {
				HandlerExceptionResolver her =
						context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
			}
		}

		// Ensure we have at least some HandlerExceptionResolvers, by registering
		// default HandlerExceptionResolvers if no other resolvers are found.
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the RequestToViewNameTranslator used by this servlet instance.
	 * 
	 * <p> 初始化此servlet实例使用的RequestToViewNameTranslator。
	 * 
	 * <p>If no implementation is configured then we default to DefaultRequestToViewNameTranslator.
	 * 
	 * <p> 如果未配置任何实现，则我们默认为DefaultRequestToViewNameTranslator。
	 * 
	 */
	private void initRequestToViewNameTranslator(ApplicationContext context) {
		try {
			this.viewNameTranslator =
					context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
						REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
						"]");
			}
		}
	}

	/**
	 * Initialize the ViewResolvers used by this class.
	 * 
	 * <p> 初始化此类使用的ViewResolvers。
	 * 
	 * <p>If no ViewResolver beans are defined in the BeanFactory for this
	 * namespace, we default to InternalResourceViewResolver.
	 * 
	 * <p> 如果没有为此命名空间的BeanFactory定义ViewResolver bean，我们默认为InternalResourceViewResolver。
	 */
	private void initViewResolvers(ApplicationContext context) {
		this.viewResolvers = null;

		if (this.detectAllViewResolvers) {
			// Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
			// 在ApplicationContext中查找所有ViewResolvers，包括祖先上下文。
			Map<String, ViewResolver> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.values());
				// We keep ViewResolvers in sorted order.
				// 我们按顺序保留ViewResolvers。
				OrderComparator.sort(this.viewResolvers);
			}
		}
		else {
			try {
				ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
				this.viewResolvers = Collections.singletonList(vr);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default ViewResolver later.
				// 忽略，我们稍后会添加一个默认的ViewResolver。
			}
		}

		// Ensure we have at least one ViewResolver, by registering
		// a default ViewResolver if no other resolvers are found.
		
		// 如果没有找到其他解析器，请通过注册默认的ViewResolver来确保我们至少有一个ViewResolver。
		if (this.viewResolvers == null) {
			this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the {@link FlashMapManager} used by this servlet instance.
	 * 
	 * <p> 初始化此servlet实例使用的FlashMapManager。
	 * 
	 * <p>If no implementation is configured then we default to
	 * {@code org.springframework.web.servlet.support.DefaultFlashMapManager}.
	 * 
	 * <p> 如果没有配置任何实现，那么我们默认为org.springframework.web.servlet.support.DefaultFlashMapManager。
	 * 
	 */
	private void initFlashMapManager(ApplicationContext context) {
		try {
			this.flashMapManager =
					context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using FlashMapManager [" + this.flashMapManager + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate FlashMapManager with name '" +
						FLASH_MAP_MANAGER_BEAN_NAME + "': using default [" + this.flashMapManager + "]");
			}
		}
	}

	/**
	 * Return this servlet's ThemeSource, if any; else return {@code null}.
	 * 
	 * <p> 如果有的话，返回这个servlet的ThemeSource; else返回null。
	 * 
	 * <p>Default is to return the WebApplicationContext as ThemeSource,
	 * provided that it implements the ThemeSource interface.
	 * 
	 * <p> 默认是将WebApplicationContext作为ThemeSource返回，前提是它实现了ThemeSource接口。
	 * 
	 * @return the ThemeSource, if any - ThemeSource，如果有的话
	 * @see #getWebApplicationContext()
	 */
	public final ThemeSource getThemeSource() {
		if (getWebApplicationContext() instanceof ThemeSource) {
			return (ThemeSource) getWebApplicationContext();
		}
		else {
			return null;
		}
	}

	/**
	 * Obtain this servlet's MultipartResolver, if any.
	 * 
	 * <p> 获取此servlet的MultipartResolver（如果有）。
	 * 
	 * @return the MultipartResolver used by this servlet, or {@code null} if none
	 * (indicating that no multipart support is available)
	 * 
	 * <p> 此servlet使用的MultipartResolver，如果没有则为null（表示没有可用的多部分支持）
	 */
	public final MultipartResolver getMultipartResolver() {
		return this.multipartResolver;
	}

	/**
	 * Return the default strategy object for the given strategy interface.
	 * 
	 * <p> 返回给定策略接口的默认策略对象。
	 * 
	 * <p>The default implementation delegates to {@link #getDefaultStrategies},
	 * expecting a single object in the list.
	 * 
	 * <p> 默认实现委托给getDefaultStrategies，期望列表中有一个对象。
	 * 
	 * @param context the current WebApplicationContext
	 * @param strategyInterface the strategy interface
	 * @return the corresponding strategy object
	 * @see #getDefaultStrategies
	 */
	protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
		List<T> strategies = getDefaultStrategies(context, strategyInterface);
		if (strategies.size() != 1) {
			throw new BeanInitializationException(
					"DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
		}
		return strategies.get(0);
	}

	/**
	 * Create a List of default strategy objects for the given strategy interface.
	 * 
	 * <p> 为给定的策略接口创建默认策略对象列表。
	 * 
	 * <p>The default implementation uses the "DispatcherServlet.properties" file (in the same
	 * package as the DispatcherServlet class) to determine the class names. It instantiates
	 * the strategy objects through the context's BeanFactory.
	 * 
	 * <p> 默认实现使用“DispatcherServlet.properties”文件（与DispatcherServlet类在同一个包中）来确定类名。 
	 * 它通过上下文的BeanFactory实例化策略对象。
	 * 
	 * @param context the current WebApplicationContext
	 * @param strategyInterface the strategy interface
	 * @return the List of corresponding strategy objects
	 */
	@SuppressWarnings("unchecked")
	protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
		String key = strategyInterface.getName();
		String value = defaultStrategies.getProperty(key);
		if (value != null) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
			List<T> strategies = new ArrayList<T>(classNames.length);
			for (String className : classNames) {
				try {
					Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
					Object strategy = createDefaultStrategy(context, clazz);
					strategies.add((T) strategy);
				}
				catch (ClassNotFoundException ex) {
					throw new BeanInitializationException(
							"Could not find DispatcherServlet's default strategy class [" + className +
									"] for interface [" + key + "]", ex);
				}
				catch (LinkageError err) {
					throw new BeanInitializationException(
							"Error loading DispatcherServlet's default strategy class [" + className +
									"] for interface [" + key + "]: problem with class file or dependent class", err);
				}
			}
			return strategies;
		}
		else {
			return new LinkedList<T>();
		}
	}

	/**
	 * Create a default strategy.
	 * 
	 * <p> 创建默认策略。
	 * 
	 * <p>The default implementation uses {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean}.
	 * 
	 * <p> 默认实现使用org.springframework.beans.factory.config.AutowireCapableBeanFactory.createBean。
	 * 
	 * @param context the current WebApplicationContext
	 * @param clazz the strategy implementation class to instantiate - 要实例化的策略实现类
	 * @return the fully configured strategy instance
	 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean
	 */
	protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
		return context.getAutowireCapableBeanFactory().createBean(clazz);
	}


	/**
	 * Exposes the DispatcherServlet-specific request attributes and delegates to {@link #doDispatch}
	 * for the actual dispatching.
	 * 
	 * <p> 将DispatcherServlet特定的请求属性和委托公开给doDispatch以进行实际调度。
	 */
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
			logger.debug("DispatcherServlet with name '" + getServletName() + "'" + resumed +
					" processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
		}

		// Keep a snapshot of the request attributes in case of an include,
		// to be able to restore the original attributes after the include.
		
		// 在包含的情况下保留请求属性的快照，以便能够在包含之后恢复原始属性。
		Map<String, Object> attributesSnapshot = null;
		if (WebUtils.isIncludeRequest(request)) {
			attributesSnapshot = new HashMap<String, Object>();
			Enumeration<?> attrNames = request.getAttributeNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
					attributesSnapshot.put(attrName, request.getAttribute(attrName));
				}
			}
		}

		// Make framework objects available to handlers and view objects.
		// 使框架对象可供处理程序和视图对象使用。
		
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
		request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

		FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
		if (inputFlashMap != null) {
			request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
		}
		request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
		request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);

		try {
			doDispatch(request, response);
		}
		finally {
			if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
				return;
			}
			// Restore the original attribute snapshot, in case of an include.
			// 如果是include，则还原原始属性快照。
			if (attributesSnapshot != null) {
				restoreAttributesAfterInclude(request, attributesSnapshot);
			}
		}
	}

	/**
	 * Process the actual dispatching to the handler.
	 * 
	 * <p> 处理实际调度到处理程序。
	 * 
	 * <p>The handler will be obtained by applying the servlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the servlet's installed HandlerAdapters
	 * to find the first that supports the handler class.
	 * 
	 * <p> 处理程序将通过按顺序应用servlet的HandlerMappings来获得。 将通过查询servlet安装的HandlerAdapter来获取
	 * HandlerAdapter，以找到支持处理程序类的第一个。
	 * 
	 * <p>All HTTP methods are handled by this method. It's up to HandlerAdapters or handlers
	 * themselves to decide which methods are acceptable.
	 * 
	 * <p> 所有HTTP方法都由此方法处理。 由HandlerAdapters或处理程序自行决定哪些方法可以接受。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of any kind of processing failure - 在任何处理失败的情况下
	 */
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		boolean multipartRequestParsed = false;

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			ModelAndView mv = null;
			Exception dispatchException = null;

			try {
				processedRequest = checkMultipart(request);
				multipartRequestParsed = processedRequest != request;

				// Determine handler for the current request.
				// 确定当前请求的处理程序。
				mappedHandler = getHandler(processedRequest, false);
				if (mappedHandler == null || mappedHandler.getHandler() == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// Determine handler adapter for the current request.
				// 确定当前请求的处理程序适配器。
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// Process last-modified header, if supported by the handler.
				// 处理最后修改的标头，如果处理程序支持。
				String method = request.getMethod();
				boolean isGet = "GET".equals(method);
				if (isGet || "HEAD".equals(method)) {
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (logger.isDebugEnabled()) {
						logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
					}
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}

				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				try {
					// Actually invoke the handler.
					// 实际上调用处理程序。
					mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
				}
				finally {
					if (asyncManager.isConcurrentHandlingStarted()) {
						return;
					}
				}

				applyDefaultViewName(request, mv);
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			}
			catch (Exception ex) {
				dispatchException = ex;
			}
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		}
		catch (Exception ex) {
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		}
		catch (Error err) {
			triggerAfterCompletionWithError(processedRequest, response, mappedHandler, err);
		}
		finally {
			if (asyncManager.isConcurrentHandlingStarted()) {
				// Instead of postHandle and afterCompletion
				// 而不是postHandle和afterCompletion
				mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
				return;
			}
			// Clean up any resources used by a multipart request.
			// 清理多部分请求使用的任何资源。
			if (multipartRequestParsed) {
				cleanupMultipart(processedRequest);
			}
		}
	}

	/**
	 * Do we need view name translation? - 我们需要查看名称翻译吗？
	 */
	private void applyDefaultViewName(HttpServletRequest request, ModelAndView mv) throws Exception {
		if (mv != null && !mv.hasView()) {
			mv.setViewName(getDefaultViewName(request));
		}
	}

	/**
	 * Handle the result of handler selection and handler invocation, which is
	 * either a ModelAndView or an Exception to be resolved to a ModelAndView.
	 * 
	 * <p> 处理处理程序选择和处理程序调用的结果，它是要解析为ModelAndView的ModelAndView或Exception。
	 */
	private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
			HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {

		boolean errorView = false;

		if (exception != null) {
			if (exception instanceof ModelAndViewDefiningException) {
				logger.debug("ModelAndViewDefiningException encountered", exception);
				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
			}
			else {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, exception);
				errorView = (mv != null);
			}
		}

		// Did the handler return a view to render?
		// 处理程序是否返回要渲染的视图？
		if (mv != null && !mv.wasCleared()) {
			render(mv, request, response);
			if (errorView) {
				WebUtils.clearErrorRequestAttributes(request);
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
						"': assuming HandlerAdapter completed request handling");
			}
		}

		if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
			// Concurrent handling started during a forward
			// 并行处理在前进期间开始
			return;
		}

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, null);
		}
	}

	/**
	 * Build a LocaleContext for the given request, exposing the request's primary locale as current locale.
	 * 
	 * <p> 为给定请求构建LocaleContext，将请求的主要区域设置公开为当前区域设置。
	 * 
	 * <p>The default implementation uses the dispatcher's LocaleResolver to obtain the current locale,
	 * which might change during a request.
	 * 
	 * <p> 默认实现使用调度程序的LocaleResolver来获取当前语言环境，该语言环境可能在请求期间发生更改。
	 * 
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext
	 */
	@Override
	protected LocaleContext buildLocaleContext(final HttpServletRequest request) {
		return new LocaleContext() {
			public Locale getLocale() {
				return localeResolver.resolveLocale(request);
			}
			public String toString() {
				return getLocale().toString();
			}
		};
	}

	/**
	 * Convert the request into a multipart request, and make multipart resolver available.
	 * 
	 * <p> 将请求转换为多部分请求，并使多部分解析器可用。
	 * 
	 * <p>If no multipart resolver is set, simply use the existing request.
	 * 
	 * <p> 如果未设置多部分解析程序，只需使用现有请求。
	 * 
	 * @param request current HTTP request
	 * @return the processed request (multipart wrapper if necessary)
	 * 
	 * <p> 已处理的请求（必要时包含多部分包装）
	 * 
	 * @see MultipartResolver#resolveMultipart
	 */
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			if (WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null) {
				logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, " +
						"this typically results from an additional MultipartFilter in web.xml");
			}
			else {
				return this.multipartResolver.resolveMultipart(request);
			}
		}
		// If not returned before: return original request.
		return request;
	}

	/**
	 * Clean up any resources used by the given multipart request (if any).
	 * 
	 * <p> 清除给定多部分请求使用的任何资源（如果有）。
	 * 
	 * @param request current HTTP request
	 * @see MultipartResolver#cleanupMultipart
	 */
	protected void cleanupMultipart(HttpServletRequest request) {
		MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
		if (multipartRequest != null) {
			this.multipartResolver.cleanupMultipart(multipartRequest);
		}
	}

	/**
	 * Return the HandlerExecutionChain for this request. Try all handler mappings in order.
	 * 
	 * <p> 返回此请求的HandlerExecutionChain。 按顺序尝试所有处理程序映射。
	 * 
	 * @param request current HTTP request
	 * @param cache whether to cache the HandlerExecutionChain in a request attribute
	 * 
	 * <p> 是否将HandlerExecutionChain缓存在请求属性中
	 * 
	 * @return the HandlerExecutionChain, or {@code null} if no handler could be found
	 * 
	 * <p> HandlerExecutionChain，如果找不到处理程序，则返回null
	 * 
	 * @deprecated as of Spring 3.0.4, in favor of {@link #getHandler(javax.servlet.http.HttpServletRequest)},
	 * with this method's cache attribute now effectively getting ignored
	 * 
	 * <p> 已过时。 从Spring 3.0.4开始，支持getHandler（javax.servlet.http.HttpServletRequest），这个方法的缓存属性现在被有效地忽略了
	 */
	@Deprecated
	protected HandlerExecutionChain getHandler(HttpServletRequest request, boolean cache) throws Exception {
		return getHandler(request);
	}

	/**
	 * Return the HandlerExecutionChain for this request.
	 * 
	 * <p> 返回此请求的HandlerExecutionChain。
	 * 
	 * <p>Tries all handler mappings in order.
	 * 
	 * <p> 按顺序尝试所有处理程序映射。
	 * 
	 * @param request current HTTP request
	 * @return the HandlerExecutionChain, or {@code null} if no handler could be found
	 * 
	 * <p> HandlerExecutionChain，如果找不到处理程序，则返回null
	 * 
	 */
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		for (HandlerMapping hm : this.handlerMappings) {
			if (logger.isTraceEnabled()) {
				logger.trace(
						"Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
			}
			HandlerExecutionChain handler = hm.getHandler(request);
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}

	/**
	 * No handler found -> set appropriate HTTP response status.
	 * 
	 * <p> 找不到处理程序 - >设置适当的HTTP响应状态。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if preparing the response failed - 如果准备响应失败
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + getRequestUri(request) +
					"] in DispatcherServlet with name '" + getServletName() + "'");
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * Return the HandlerAdapter for this handler object.
	 * 
	 * <p> 返回此处理程序对象的HandlerAdapter。
	 * 
	 * @param handler the handler object to find an adapter for
	 * 
	 * <p> 用于查找适配器的处理程序对象
	 * 
	 * @throws ServletException if no HandlerAdapter can be found for the handler. This is a fatal error.
	 * 
	 * <p> 如果没有为处理程序找到HandlerAdapter。 这是一个致命的错误。
	 * 
	 */
	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		for (HandlerAdapter ha : this.handlerAdapters) {
			if (logger.isTraceEnabled()) {
				logger.trace("Testing handler adapter [" + ha + "]");
			}
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}

	/**
	 * Determine an error ModelAndView via the registered HandlerExceptionResolvers.
	 * 
	 * <p> 通过注册的HandlerExceptionResolvers确定错误ModelAndView。
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler, or {@code null} if none chosen at the time of the exception
	 * (for example, if multipart resolution failed)
	 * 
	 * <p> 执行的处理程序，如果在异常时没有选择，则返回null（例如，如果多部分解析失败）
	 * 
	 * @param ex the exception that got thrown during handler execution
	 * 
	 * <p> 在处理程序执行期间抛出的异常
	 * 
	 * @return a corresponding ModelAndView to forward to - 一个相应的ModelAndView转发到
	 * @throws Exception if no error ModelAndView found - 如果没有找到错误的ModelAndView
	 */
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception ex) throws Exception {

		// Check registered HandlerExceptionResolvers...
		ModelAndView exMv = null;
		for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
			exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
			if (exMv != null) {
				break;
			}
		}
		if (exMv != null) {
			if (exMv.isEmpty()) {
				return null;
			}
			// We might still need view name translation for a plain error model...
			if (!exMv.hasView()) {
				exMv.setViewName(getDefaultViewName(request));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv, ex);
			}
			WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
			return exMv;
		}

		throw ex;
	}

	/**
	 * Render the given ModelAndView.
	 * 
	 * <p> 渲染给定的ModelAndView。
	 * 
	 * <p>This is the last stage in handling a request. It may involve resolving the view by name.
	 * 
	 * <p> 这是处理请求的最后阶段。 它可能涉及按名称解析视图。
	 * 
	 * @param mv the ModelAndView to render
	 * @param request current HTTP servlet request
	 * @param response current HTTP servlet response
	 * @throws ServletException if view is missing or cannot be resolved
	 * @throws Exception if there's a problem rendering the view - 如果渲染视图有问题
	 */
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Determine locale for request and apply it to the response.
		// 确定请求的区域设置并将其应用于响应。
		Locale locale = this.localeResolver.resolveLocale(request);
		response.setLocale(locale);

		View view;
		if (mv.isReference()) {
			// We need to resolve the view name.
			// 我们需要解析视图名称。
			view = resolveViewName(mv.getViewName(), mv.getModelInternal(), locale, request);
			if (view == null) {
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
						"' in servlet with name '" + getServletName() + "'");
			}
		}
		else {
			// No need to lookup: the ModelAndView object contains the actual View object.
			// 无需查找：ModelAndView对象包含实际的View对象。
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
						"View object in servlet with name '" + getServletName() + "'");
			}
		}

		// Delegate to the View object for rendering.
		// 委托View对象进行渲染。
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
		}
		try {
			view.render(mv.getModelInternal(), request, response);
		}
		catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
						getServletName() + "'", ex);
			}
			throw ex;
		}
	}

	/**
	 * Translate the supplied request into a default view name.
	 * 
	 * <p> 将提供的请求转换为默认视图名称。
	 * 
	 * @param request current HTTP servlet request
	 * @return the view name (or {@code null} if no default found)
	 * 
	 * <p> 视图名称（如果未找到默认值，则为null）
	 * 
	 * @throws Exception if view name translation failed
	 */
	protected String getDefaultViewName(HttpServletRequest request) throws Exception {
		return this.viewNameTranslator.getViewName(request);
	}

	/**
	 * Resolve the given view name into a View object (to be rendered).
	 * 
	 * <p> 将给定的视图名称解析为View对象（要渲染）。
	 * 
	 * <p>The default implementations asks all ViewResolvers of this dispatcher.
	 * Can be overridden for custom resolution strategies, potentially based on
	 * specific model attributes or request parameters.
	 * 
	 * <p> 默认实现会询问此调度程序的所有ViewResolvers。 可以根据特定模型属性或请求参数覆盖自定义解析策略。
	 * 
	 * @param viewName the name of the view to resolve
	 * @param model the model to be passed to the view
	 * @param locale the current locale
	 * @param request current HTTP servlet request
	 * @return the View object, or {@code null} if none found
	 * @throws Exception if the view cannot be resolved
	 * (typically in case of problems creating an actual View object)
	 * 
	 * <p> 如果视图无法解析（通常在创建实际View对象时出现问题）
	 * 
	 * @see ViewResolver#resolveViewName
	 */
	protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale,
			HttpServletRequest request) throws Exception {

		for (ViewResolver viewResolver : this.viewResolvers) {
			View view = viewResolver.resolveViewName(viewName, locale);
			if (view != null) {
				return view;
			}
		}
		return null;
	}

	private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
			HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, ex);
		}
		throw ex;
	}

	private void triggerAfterCompletionWithError(HttpServletRequest request, HttpServletResponse response,
			HandlerExecutionChain mappedHandler, Error error) throws Exception, ServletException {

		ServletException ex = new NestedServletException("Handler processing failed", error);
		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, ex);
		}
		throw ex;
	}

	/**
	 * Restore the request attributes after an include.
	 * 
	 * <p> 在包含之后恢复请求属性。
	 * 
	 * @param request current HTTP request
	 * @param attributesSnapshot the snapshot of the request attributes before the include
	 * 
	 * <p> 包含之前的请求属性的快照
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?,?> attributesSnapshot) {
		// Need to copy into separate Collection here, to avoid side effects
		// on the Enumeration when removing attributes.
		
		// 需要在此处复制到单独的Collection中，以避免在删除属性时对Enumeration产生副作用。
		Set<String> attrsToCheck = new HashSet<String>();
		Enumeration<?> attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = (String) attrNames.nextElement();
			if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
				attrsToCheck.add(attrName);
			}
		}

		// Add attributes that may have been removed
		// 添加可能已删除的属性
		attrsToCheck.addAll((Set<String>) attributesSnapshot.keySet());

		// Iterate over the attributes to check, restoring the original value
		// or removing the attribute, respectively, if appropriate.
		
		// 如果合适，分别迭代要检查的属性，恢复原始值或删除属性。
		for (String attrName : attrsToCheck) {
			Object attrValue = attributesSnapshot.get(attrName);
			if (attrValue == null){
				request.removeAttribute(attrName);
			}
			else if (attrValue != request.getAttribute(attrName)) {
				request.setAttribute(attrName, attrValue);
			}
		}
	}

	private static String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return uri;
	}

}
