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

package org.springframework.core.io.support;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.VfsResource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * A {@link ResourcePatternResolver} implementation that is able to resolve a
 * specified resource location path into one or more matching Resources.
 * The source path may be a simple path which has a one-to-one mapping to a
 * target {@link org.springframework.core.io.Resource}, or alternatively
 * may contain the special "{@code classpath*:}" prefix and/or
 * internal Ant-style regular expressions (matched using Spring's
 * {@link org.springframework.util.AntPathMatcher} utility).
 * Both of the latter are effectively wildcards.
 * 
 * <p>ResourcePatternResolver实现，它能够将指定的资源位置路径解析为一个或多个匹配的资源。源路径可以是一个简单的路径，
 * 它与目标org.springframework.core.io.Resource有一对一的映射，或者可能包含特殊的“classpath *：”前缀和/或内部Ant样式的常规表达式（使
 * 用Spring的org.springframework.util.AntPathMatcher实用程序进行匹配）。后者都是有效的通配符。
 *
 * <p><b>No Wildcards:</b>
 * 
 * <p>没有通配符：
 *
 * <p>In the simple case, if the specified location path does not start with the
 * {@code "classpath*:}" prefix, and does not contain a PathMatcher pattern,
 * this resolver will simply return a single resource via a
 * {@code getResource()} call on the underlying {@code ResourceLoader}.
 * Examples are real URLs such as "{@code file:C:/context.xml}", pseudo-URLs
 * such as "{@code classpath:/context.xml}", and simple unprefixed paths
 * such as "{@code /WEB-INF/context.xml}". The latter will resolve in a
 * fashion specific to the underlying {@code ResourceLoader} (e.g.
 * {@code ServletContextResource} for a {@code WebApplicationContext}).
 * 
 * <p>在简单的情况下，如果指定的位置路径不以“classpath *：”前缀开头，并且不包含PathMatcher模式，则此解析器将通过底
 * 层ResourceLoader上的getResource（）调用返回单个资源。示例是真实的URL，例如“file：C：/context.xml”，伪URL，例
 * 如“classpath：/context.xml”，以及简单的无前缀路径，例如“/WEB-INF/context.xml”。后者将以特定于底层ResourceLoader的方式解
 * 析（例如，WebApplicationContext的ServletContextResource）。
 *
 * <p><b>Ant-style Patterns:</b> 蚂蚁风格图案：
 *
 * <p>When the path location contains an Ant-style pattern, e.g.:
 * <pre class="code">
 * /WEB-INF/*-context.xml
 * com/mycompany/**&#47;applicationContext.xml
 * file:C:/some/path/*-context.xml
 * classpath:com/mycompany/**&#47;applicationContext.xml</pre>
 * the resolver follows a more complex but defined procedure to try to resolve
 * the wildcard. It produces a {@code Resource} for the path up to the last
 * non-wildcard segment and obtains a {@code URL} from it. If this URL is
 * not a "{@code jar:}" URL or container-specific variant (e.g.
 * "{@code zip:}" in WebLogic, "{@code wsjar}" in WebSphere", etc.),
 * then a {@code java.io.File} is obtained from it, and used to resolve the
 * wildcard by walking the filesystem. In the case of a jar URL, the resolver
 * either gets a {@code java.net.JarURLConnection} from it, or manually parses
 * the jar URL, and then traverses the contents of the jar file, to resolve the
 * wildcards.
 * 
 * <p>当路径位置包含Ant样式模式时，例如：
 * /WEB-INF/*-context.xml 
 * COM / myCompany中/ ** / applicationContext.xml中 
 * 文件：C：/一些/路径/ * - context.xml中 
 * 类路径：COM / myCompany中/ ** / applicationContext.xml中 
 * 解析器遵循更复杂但定义的过程来尝试解析通配符。它为直到最后一个非通配符段的路径生成一个Resource，并从中获取一个URL。如
 * 果此URL不是“jar：”URL或特定于容器的变体（例如，WebLogic中的“zip：”，WebSphere中的“wsjar”等），则从中获取java.io.File，并使
 * 用通过遍历文件系统来解析通配符。对于jar URL，解析器要么从它获取java.net.JarURLConnection，要么手动解析jar URL，然后遍历jar文
 * 件的内容，以解决通配符。
 *
 * <p><b>Implications on portability:</b> 对便携性的影响：
 *
 * <p>If the specified path is already a file URL (either explicitly, or
 * implicitly because the base {@code ResourceLoader} is a filesystem one,
 * then wildcarding is guaranteed to work in a completely portable fashion.
 * 
 * <p>如果指定的路径已经是文件URL（显式或隐式，因为基本ResourceLoader是文件系统），那么通配符保证以完全可移植的方式工作。
 *
 * <p>If the specified path is a classpath location, then the resolver must
 * obtain the last non-wildcard path segment URL via a
 * {@code Classloader.getResource()} call. Since this is just a
 * node of the path (not the file at the end) it is actually undefined
 * (in the ClassLoader Javadocs) exactly what sort of a URL is returned in
 * this case. In practice, it is usually a {@code java.io.File} representing
 * the directory, where the classpath resource resolves to a filesystem
 * location, or a jar URL of some sort, where the classpath resource resolves
 * to a jar location. Still, there is a portability concern on this operation.
 * 
 * <p>如果指定的路径是类路径位置，则解析程序必须通过Classloader.getResource（）调用获取最后一个非通配符路径段URL。由于这只是路
 * 径的一个节点（不是最后的文件），因此在这种情况下，实际上未定义（在ClassLoader Javadocs中）究竟返回了什么类型的URL。在实践中，它
 * 通常是表示目录的java.io.File，其中类路径资源解析为文件系统位置，或者某种类型的jar URL，其中类路径资源解析为jar位置。尽管如此，这
 * 种操作还是存在可移植性问题。
 *
 * <p>If a jar URL is obtained for the last non-wildcard segment, the resolver
 * must be able to get a {@code java.net.JarURLConnection} from it, or
 * manually parse the jar URL, to be able to walk the contents of the jar,
 * and resolve the wildcard. This will work in most environments, but will
 * fail in others, and it is strongly recommended that the wildcard
 * resolution of resources coming from jars be thoroughly tested in your
 * specific environment before you rely on it.
 * 
 * <p>如果获取最后一个非通配符段的jar URL，则解析器必须能够从中获取java.net.JarURLConnection，或者手动解析jar URL，以便能够
 * 遍历jar的内容，并解析通配符。这适用于大多数环境，但在其他环境中会失败，强烈建议在依赖它之前，在特定环境中对来自jar的资源的通配符解析进行全面测试。
 *
 * <p><b>{@code classpath*:} Prefix:</b>  classpath *：前缀：
 *
 * <p>There is special support for retrieving multiple class path resources with
 * the same name, via the "{@code classpath*:}" prefix. For example,
 * "{@code classpath*:META-INF/beans.xml}" will find all "beans.xml"
 * files in the class path, be it in "classes" directories or in JAR files.
 * This is particularly useful for autodetecting config files of the same name
 * at the same location within each jar file. Internally, this happens via a
 * {@code ClassLoader.getResources()} call, and is completely portable.
 * 
 * <p>通过“classpath *：”前缀，可以检索具有相同名称的多个类路径资源。例如，“classpath *：META-INF / beans.xml”将在类路
 * 径中找到所有“beans.xml”文件，无论是在“classes”目录中还是在JAR文件中。这对于在每个jar文件中的相同位置自动检测同名的配置文件特别
 * 有用。在内部，这通过ClassLoader.getResources（）调用发生，并且是完全可移植的。
 *
 * <p>The "classpath*:" prefix can also be combined with a PathMatcher pattern in
 * the rest of the location path, for example "classpath*:META-INF/*-beans.xml".
 * In this case, the resolution strategy is fairly simple: a
 * {@code ClassLoader.getResources()} call is used on the last non-wildcard
 * path segment to get all the matching resources in the class loader hierarchy,
 * and then off each resource the same PathMatcher resolution strategy described
 * above is used for the wildcard subpath.
 * 
 * <p>“classpath *：”前缀也可以与位置路径的其余部分中的PathMatcher模式组合，例如“classpath *：META-INF / * - beans.xml”。在这
 * 种情况下，解析策略非常简单：在最后一个非通配符路径段上使用ClassLoader.getResources（）调用来获取类加载器层次结构中的所有匹配资源，然后关
 * 闭每个资源相同的PathMatcher解析策略上面描述的用于通配符子路径。
 *
 * <p><b>Other notes:</b> 其他说明：
 *
 * <p><b>WARNING:</b> Note that "{@code classpath*:}" when combined with
 * Ant-style patterns will only work reliably with at least one root directory
 * before the pattern starts, unless the actual target files reside in the file
 * system. This means that a pattern like "{@code classpath*:*.xml}" will
 * <i>not</i> retrieve files from the root of jar files but rather only from the
 * root of expanded directories. This originates from a limitation in the JDK's
 * {@code ClassLoader.getResources()} method which only returns file system
 * locations for a passed-in empty String (indicating potential roots to search).
 * 
 * <p>警告：请注意，“classpath*：”与Ant样式模式结合使用时，只能在模式启动前与至少一个根目录可靠地工作，除非实际目标文件驻留在文件系统中。这意
 * 味着像“classpath *：*.xml”这样的模式不会从jar文件的根目录中检索文件，而只能从扩展目录的根目录中检索文件。这源
 * 于JDK的ClassLoader.getResources（）方法中的限制，该方法仅返回传入的空字符串的文件系统位置（指示搜索的潜在根）。
 *
 * <p><b>WARNING:</b> Ant-style patterns with "classpath:" resources are not
 * guaranteed to find matching resources if the root package to search is available
 * in multiple class path locations. This is because a resource such as
 * <pre class="code">
 *     com/mycompany/package1/service-context.xml
 * </pre>
 * 
 * <p>警告：如果要搜索的根包在多个类路径位置中可用，则不保证具有“classpath：”资源的Ant样式模式可以找到匹配的资源。 这是因为资源如
 * com/mycompany/package1/service-context.xml
 * 
 * may be in only one location, but when a path such as
 * <pre class="code">
 *     classpath:com/mycompany/**&#47;service-context.xml
 * </pre>
 * 
 * <p>可能只在一个位置，但是当路径如classpath：com / mycompany / ** / service-context.xml时
 * 
 * is used to try to resolve it, the resolver will work off the (first) URL
 * returned by {@code getResource("com/mycompany");}. If this base package
 * node exists in multiple classloader locations, the actual end resource may
 * not be underneath. Therefore, preferably, use "{@code classpath*:}" with the same
 * Ant-style pattern in such a case, which will search <i>all</i> class path
 * locations that contain the root package.
 * 
 * <p>用于尝试解决它，解析器将解决getResource（“com / mycompany”）;返回的（第一个）URL。 如果此基本包节点存在于多个类加载器位
 * 置中，则实际的最终资源可能不在下面。 因此，最好在这种情况下使用具有相同Ant样式模式的“classpath *：”，它将搜索包含根包的所有类路径位置。
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Marius Bogoevici
 * @author Costin Leau
 * @since 1.0.2
 * @see #CLASSPATH_ALL_URL_PREFIX
 * @see org.springframework.util.AntPathMatcher
 * @see org.springframework.core.io.ResourceLoader#getResource(String)
 * @see ClassLoader#getResources(String)
 */
public class PathMatchingResourcePatternResolver implements ResourcePatternResolver {

	private static final Log logger = LogFactory.getLog(PathMatchingResourcePatternResolver.class);

	private static Method equinoxResolveMethod;

	static {
		try {
			// Detect Equinox OSGi (e.g. on WebSphere 6.1)
			Class<?> fileLocatorClass = ClassUtils.forName("org.eclipse.core.runtime.FileLocator",
					PathMatchingResourcePatternResolver.class.getClassLoader());
			equinoxResolveMethod = fileLocatorClass.getMethod("resolve", URL.class);
			logger.debug("Found Equinox FileLocator for OSGi bundle URL resolution");
		}
		catch (Throwable ex) {
			equinoxResolveMethod = null;
		}
	}


	private final ResourceLoader resourceLoader;

	private PathMatcher pathMatcher = new AntPathMatcher();


	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * <p>ClassLoader access will happen via the thread context class loader.
	 * 
	 * <p>使用DefaultResourceLoader创建一个新的PathMatchingResourcePatternResolver。
	 * ClassLoader访问将通过线程上下文类加载器进行。
	 * 
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver.
	 * 
	 * <p>创建一个新的PathMatchingResourcePatternResolver。
	 * 
	 * <p>ClassLoader access will happen via the thread context class loader.
	 * 
	 * <p>ClassLoader访问将通过线程上下文类加载器进行。
	 * 
	 * @param resourceLoader the ResourceLoader to load root directories and
	 * actual resources with
	 * 
	 * <p>ResourceLoader用于加载根目录和实际资源
	 */
	public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * 
	 * <p>使用DefaultResourceLoader创建一个新的PathMatchingResourcePatternResolver。
	 * 
	 * @param classLoader the ClassLoader to load classpath resources with,
	 * or {@code null} for using the thread context class loader
	 * at the time of actual resource access
	 * 
	 * <p>用于加载类路径资源的ClassLoader，或者在实际资源访问时使用线程上下文类加载器为null
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver(ClassLoader classLoader) {
		this.resourceLoader = new DefaultResourceLoader(classLoader);
	}


	/**
	 * Return the ResourceLoader that this pattern resolver works with.
	 * 
	 * <p>返回此模式解析器使用的ResourceLoader。
	 */
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public ClassLoader getClassLoader() {
		return getResourceLoader().getClassLoader();
	}

	/**
	 * Set the PathMatcher implementation to use for this
	 * resource pattern resolver. Default is AntPathMatcher.
	 * 
	 * <p>设置PathMatcher实现以用于此资源模式解析程序。 默认为AntPathMatcher。
	 * 
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Return the PathMatcher that this resource pattern resolver uses.
	 * 
	 * <p>返回此资源模式解析程序使用的PathMatcher。
	 * 
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}


	public Resource getResource(String location) {
		return getResourceLoader().getResource(location);
	}

	public Resource[] getResources(String locationPattern) throws IOException {
		Assert.notNull(locationPattern, "Location pattern must not be null");
		if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
			// a class path resource (multiple resources for same name possible)
			// 类路径资源（可能有多个同名资源）
			if (getPathMatcher().isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
				// a class path resource pattern
				// 类路径资源模式
				return findPathMatchingResources(locationPattern);
			}
			else {
				// all class path resources with the given name
				// 具有给定名称的所有类路径资源
				return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()));
			}
		}
		else {
			// Only look for a pattern after a prefix here
			// 只在这里找到前缀后的模式
			// (to not get fooled by a pattern symbol in a strange prefix).
			// （不要被奇怪的前缀中的模式符号所迷惑）。
			int prefixEnd = locationPattern.indexOf(":") + 1;
			if (getPathMatcher().isPattern(locationPattern.substring(prefixEnd))) {
				// a file pattern
				// 文件模式
				return findPathMatchingResources(locationPattern);
			}
			else {
				// a single resource with the given name
				// 具有给定名称的单个资源
				return new Resource[] {getResourceLoader().getResource(locationPattern)};
			}
		}
	}

	/**
	 * Find all class location resources with the given location via the ClassLoader.
	 * 
	 * <p>通过ClassLoader查找具有给定位置的所有类位置资源。
	 * 
	 * @param location the absolute path within the classpath - 类路径中的绝对路径
	 * @return the result as Resource array - 结果为资源数组
	 * @throws IOException in case of I/O errors - 在I / O错误的情况下
	 * @see java.lang.ClassLoader#getResources
	 * @see #convertClassLoaderURL
	 */
	protected Resource[] findAllClassPathResources(String location) throws IOException {
		String path = location;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		ClassLoader cl = getClassLoader();
		Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path));
		Set<Resource> result = new LinkedHashSet<Resource>(16);
		while (resourceUrls.hasMoreElements()) {
			URL url = resourceUrls.nextElement();
			result.add(convertClassLoaderURL(url));
		}
		return result.toArray(new Resource[result.size()]);
	}

	/**
	 * Convert the given URL as returned from the ClassLoader into a Resource object.
	 * 
	 * <p>将从ClassLoader返回的给定URL转换为Resource对象。
	 * 
	 * <p>The default implementation simply creates a UrlResource instance.
	 * 
	 * <p>默认实现只是创建一个UrlResource实例。
	 * 
	 * @param url a URL as returned from the ClassLoader - 从ClassLoader返回的URL
	 * @return the corresponding Resource object - 相应的Resource对象
	 * @see java.lang.ClassLoader#getResources
	 * @see org.springframework.core.io.Resource
	 */
	protected Resource convertClassLoaderURL(URL url) {
		return new UrlResource(url);
	}

	/**
	 * Find all resources that match the given location pattern via the
	 * Ant-style PathMatcher. Supports resources in jar files and zip files
	 * and in the file system.
	 * 
	 * <p>通过Ant风格的PathMatcher查找与给定位置模式匹配的所有资源。 支持jar文件和zip文件以及文件系统中的资源。
	 * 
	 * @param locationPattern the location pattern to match - 要匹配的位置模式
	 * @return the result as Resource array - 结果为资源数组
	 * @throws IOException in case of I/O errors - 在I / O错误的情况下
	 * @see #doFindPathMatchingJarResources
	 * @see #doFindPathMatchingFileResources
	 * @see org.springframework.util.PathMatcher
	 */
	protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
		String rootDirPath = determineRootDir(locationPattern);
		String subPattern = locationPattern.substring(rootDirPath.length());
		Resource[] rootDirResources = getResources(rootDirPath);
		Set<Resource> result = new LinkedHashSet<Resource>(16);
		for (Resource rootDirResource : rootDirResources) {
			rootDirResource = resolveRootDirResource(rootDirResource);
			if (rootDirResource.getURL().getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
				result.addAll(VfsResourceMatchingDelegate.findMatchingResources(rootDirResource, subPattern, getPathMatcher()));
			}
			else if (isJarResource(rootDirResource)) {
				result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
			}
			else {
				result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Resolved location pattern [" + locationPattern + "] to resources " + result);
		}
		return result.toArray(new Resource[result.size()]);
	}

	/**
	 * Determine the root directory for the given location.
	 * 
	 * <p>确定给定位置的根目录。
	 * 
	 * <p>Used for determining the starting point for file matching,
	 * resolving the root directory location to a {@code java.io.File}
	 * and passing it into {@code retrieveMatchingFiles}, with the
	 * remainder of the location as pattern.
	 * 
	 * <p>用于确定文件匹配的起始点，将根目录位置解析为java.io.File并将其传递给retrieveMatchingFiles，其余位置为模式。
	 * 
	 * <p>Will return "/WEB-INF/" for the pattern "/WEB-INF/*.xml",
	 * for example.
	 * 
	 * <p>例如，将为“/WEB-INF/*.xml”模式返回“/ WEB-INF /”。
	 * 
	 * @param location the location to check - 要检查的位置
	 * @return the part of the location that denotes the root directory - 表示根目录的位置部分
	 * @see #retrieveMatchingFiles
	 */
	protected String determineRootDir(String location) {
		int prefixEnd = location.indexOf(":") + 1;
		int rootDirEnd = location.length();
		while (rootDirEnd > prefixEnd && getPathMatcher().isPattern(location.substring(prefixEnd, rootDirEnd))) {
			rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
		}
		if (rootDirEnd == 0) {
			rootDirEnd = prefixEnd;
		}
		return location.substring(0, rootDirEnd);
	}

	/**
	 * Resolve the specified resource for path matching.
	 * 
	 * <p>解析指定的资源以进行路径匹配。
	 * 
	 * <p>The default implementation detects an Equinox OSGi "bundleresource:"
	 * / "bundleentry:" URL and resolves it into a standard jar file URL that
	 * can be traversed using Spring's standard jar file traversal algorithm.
	 * 
	 * <p>默认实现检测到Equinox OSGi“bundleresource：”/“bundleentry：”URL并将其解析为可以使
	 * 用Spring的标准jar文件遍历算法遍历的标准jar文件URL。
	 * 
	 * @param original the resource to resolve - 要解决的资源
	 * @return the resolved resource (may be identical to the passed-in resource) - 已解析的资源（可能与传入的资源相同）
	 * @throws IOException in case of resolution failure - 在解决方案失败的情况下
	 */
	protected Resource resolveRootDirResource(Resource original) throws IOException {
		if (equinoxResolveMethod != null) {
			URL url = original.getURL();
			if (url.getProtocol().startsWith("bundle")) {
				return new UrlResource((URL) ReflectionUtils.invokeMethod(equinoxResolveMethod, null, url));
			}
		}
		return original;
	}

	/**
	 * Return whether the given resource handle indicates a jar resource
	 * that the {@code doFindPathMatchingJarResources} method can handle.
	 * 
	 * <p>返回给定资源句柄是否指示doFindPathMatchingJarResources方法可以处理的jar资源。
	 * 
	 * <p>The default implementation checks against the URL protocols
	 * "jar", "zip" and "wsjar" (the latter are used by BEA WebLogic Server
	 * and IBM WebSphere, respectively, but can be treated like jar files).
	 * 
	 * <p>默认实现检查URL协议“jar”，“zip”和“wsjar”（后者分别由BEA WebLogic Server和IBM WebSphere使用，
	 * 但可以像jar文件一样对待）。
	 * 
	 * @param resource the resource handle to check
	 * (usually the root directory to start path matching from) - 要检查的资源句柄（通常是从根目录开始路径匹配）
	 * @see #doFindPathMatchingJarResources
	 * @see org.springframework.util.ResourceUtils#isJarURL
	 */
	protected boolean isJarResource(Resource resource) throws IOException {
		return ResourceUtils.isJarURL(resource.getURL());
	}

	/**
	 * Find all resources in jar files that match the given location pattern
	 * via the Ant-style PathMatcher.
	 * 
	 * <p>通过Ant样式的PathMatcher查找与给定位置模式匹配的jar文件中的所有资源。
	 * 
	 * @param rootDirResource the root directory as Resource - 根目录为Resource
	 * @param subPattern the sub pattern to match (below the root directory) - 要匹配的子模式（在根目录下）
	 * @return the Set of matching Resource instances - 匹配的资源实例集
	 * @throws IOException in case of I/O errors - 在I / O错误的情况下
	 * @see java.net.JarURLConnection
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set<Resource> doFindPathMatchingJarResources(Resource rootDirResource, String subPattern)
			throws IOException {

		URLConnection con = rootDirResource.getURL().openConnection();
		JarFile jarFile;
		String jarFileUrl;
		String rootEntryPath;
		boolean newJarFile = false;

		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			// 通常应该是传统JAR文件的情况。
			JarURLConnection jarCon = (JarURLConnection) con;
			ResourceUtils.useCachesIfNecessary(jarCon);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		}
		else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			
			//没有JarURLConnection - >需要求助于URL文件解析。
			//我们假设格式为“jar：path！/ entry”的URL，只要遵循条目格式，协议就是任意的。
			//我们还将处理带有和不带有“file：”前缀的路径。
			String urlFile = rootDirResource.getURL().getFile();
			int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
			if (separatorIndex != -1) {
				jarFileUrl = urlFile.substring(0, separatorIndex);
				rootEntryPath = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
				jarFile = getJarFile(jarFileUrl);
			}
			else {
				jarFile = new JarFile(urlFile);
				jarFileUrl = urlFile;
				rootEntryPath = "";
			}
			newJarFile = true;
		}

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Looking for matching resources in jar file [" + jarFileUrl + "]");
			}
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper matching.
				// The Sun JRE does not return a slash here, but BEA JRockit does.
				
				//根条目路径必须以斜杠结尾才能进行正确匹配。
				//Sun JRE在这里没有返回斜线，但BEA JRockit确实如此。
				rootEntryPath = rootEntryPath + "/";
			}
			Set<Resource> result = new LinkedHashSet<Resource>(8);
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				if (entryPath.startsWith(rootEntryPath)) {
					String relativePath = entryPath.substring(rootEntryPath.length());
					if (getPathMatcher().match(subPattern, relativePath)) {
						result.add(rootDirResource.createRelative(relativePath));
					}
				}
			}
			return result;
		}
		finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			
			//关闭jar文件，但仅限于刚刚获得的 - 不是来自JarURLConnection，它可能会缓存文件引用。
			if (newJarFile) {
				jarFile.close();
			}
		}
	}

	/**
	 * Resolve the given jar file URL into a JarFile object.
	 * 
	 * <p>将给定的jar文件URL解析为Jar File对象。
	 * 
	 */
	protected JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			try {
				return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
			}
			catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever happen).
				// 无效URI的URL的后备（几乎不会发生）。
				return new JarFile(jarFileUrl.substring(ResourceUtils.FILE_URL_PREFIX.length()));
			}
		}
		else {
			return new JarFile(jarFileUrl);
		}
	}

	/**
	 * Find all resources in the file system that match the given location pattern
	 * via the Ant-style PathMatcher.
	 * 
	 * <p>通过Ant样式的PathMatcher查找文件系统中与给定位置模式匹配的所有资源。
	 * 
	 * @param rootDirResource the root directory as Resource - 根目录为Resource
	 * @param subPattern the sub pattern to match (below the root directory) - 要匹配的子模式（在根目录下）
	 * @return the Set of matching Resource instances - 匹配的资源实例集
	 * @throws IOException in case of I/O errors - 在I / O错误的情况下
	 * @see #retrieveMatchingFiles
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set<Resource> doFindPathMatchingFileResources(Resource rootDirResource, String subPattern)
			throws IOException {

		File rootDir;
		try {
			rootDir = rootDirResource.getFile().getAbsoluteFile();
		}
		catch (IOException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cannot search for matching files underneath " + rootDirResource +
						" because it does not correspond to a directory in the file system", ex);
			}
			return Collections.emptySet();
		}
		return doFindMatchingFileSystemResources(rootDir, subPattern);
	}

	/**
	 * Find all resources in the file system that match the given location pattern
	 * via the Ant-style PathMatcher.
	 * 
	 * <p>通过Ant样式的PathMatcher查找文件系统中与给定位置模式匹配的所有资源。
	 * 
	 * @param rootDir the root directory in the file system - 文件系统中的根目录
	 * @param subPattern the sub pattern to match (below the root directory) - 要匹配的子模式（在根目录下）
	 * @return the Set of matching Resource instances - 匹配的资源实例集
	 * @throws IOException in case of I/O errors - 在I / O错误的情况下
	 * @see #retrieveMatchingFiles
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set<Resource> doFindMatchingFileSystemResources(File rootDir, String subPattern) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Looking for matching resources in directory tree [" + rootDir.getPath() + "]");
		}
		Set<File> matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
		Set<Resource> result = new LinkedHashSet<Resource>(matchingFiles.size());
		for (File file : matchingFiles) {
			result.add(new FileSystemResource(file));
		}
		return result;
	}

	/**
	 * Retrieve files that match the given path pattern,
	 * checking the given directory and its subdirectories.
	 * 
	 * <p>检索与给定路径模式匹配的文件，检查给定目录及其子目录。
	 * 
	 * @param rootDir the directory to start from - 要从中开始的目录
	 * @param pattern the pattern to match against,
	 * relative to the root directory - 相对于根目录匹配的模式
	 * @return the Set of matching File instances - 匹配文件实例的集合
	 * @throws IOException if directory contents could not be retrieved - 如果无法检索目录内容
	 */
	protected Set<File> retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
		if (!rootDir.exists()) {
			// Silently skip non-existing directories.
			// 静默跳过不存在的目录。
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping [" + rootDir.getAbsolutePath() + "] because it does not exist");
			}
			return Collections.emptySet();
		}
		if (!rootDir.isDirectory()) {
			// Complain louder if it exists but is no directory.
			// 如果它存在但是没有目录，则抱怨更响亮。
			if (logger.isWarnEnabled()) {
				logger.warn("Skipping [" + rootDir.getAbsolutePath() + "] because it does not denote a directory");
			}
			return Collections.emptySet();
		}
		if (!rootDir.canRead()) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cannot search for matching files underneath directory [" + rootDir.getAbsolutePath() +
						"] because the application is not allowed to read the directory");
			}
			return Collections.emptySet();
		}
		String fullPattern = StringUtils.replace(rootDir.getAbsolutePath(), File.separator, "/");
		if (!pattern.startsWith("/")) {
			fullPattern += "/";
		}
		fullPattern = fullPattern + StringUtils.replace(pattern, File.separator, "/");
		Set<File> result = new LinkedHashSet<File>(8);
		doRetrieveMatchingFiles(fullPattern, rootDir, result);
		return result;
	}

	/**
	 * Recursively retrieve files that match the given pattern,
	 * adding them to the given result list.
	 * 
	 * <p>递归检索与给定模式匹配的文件，将它们添加到给定的结果列表中。
	 * 
	 * @param fullPattern the pattern to match against,
	 * with prepended root directory path
	 * 
	 * <p>与之前根目录路径匹配的模式
	 * 
	 * @param dir the current directory - 当前目录
	 * @param result the Set of matching File instances to add to - 结果要添加的匹配文件实例集
	 * @throws IOException if directory contents could not be retrieved - 如果无法检索目录内容
	 */
	protected void doRetrieveMatchingFiles(String fullPattern, File dir, Set<File> result) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching directory [" + dir.getAbsolutePath() +
					"] for files matching pattern [" + fullPattern + "]");
		}
		File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
			}
			return;
		}
		for (File content : dirContents) {
			String currPath = StringUtils.replace(content.getAbsolutePath(), File.separator, "/");
			if (content.isDirectory() && getPathMatcher().matchStart(fullPattern, currPath + "/")) {
				if (!content.canRead()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipping subdirectory [" + dir.getAbsolutePath() +
								"] because the application is not allowed to read the directory");
					}
				}
				else {
					doRetrieveMatchingFiles(fullPattern, content, result);
				}
			}
			if (getPathMatcher().match(fullPattern, currPath)) {
				result.add(content);
			}
		}
	}


	/**
	 * Inner delegate class, avoiding a hard JBoss VFS API dependency at runtime.
	 * 
	 * <p>内部委托类，在运行时避免硬JBoss VFS API依赖。
	 * 
	 */
	private static class VfsResourceMatchingDelegate {

		public static Set<Resource> findMatchingResources(
				Resource rootResource, String locationPattern, PathMatcher pathMatcher) throws IOException {
			Object root = VfsPatternUtils.findRoot(rootResource.getURL());
			PatternVirtualFileVisitor visitor =
					new PatternVirtualFileVisitor(VfsPatternUtils.getPath(root), locationPattern, pathMatcher);
			VfsPatternUtils.visit(root, visitor);
			return visitor.getResources();
		}
	}


	/**
	 * VFS visitor for path matching purposes.
	 * 
	 * <p>VFS访问者用于路径匹配目的。
	 * 
	 */
	@SuppressWarnings("unused")
	private static class PatternVirtualFileVisitor implements InvocationHandler {

		private final String subPattern;

		private final PathMatcher pathMatcher;

		private final String rootPath;

		private final Set<Resource> resources = new LinkedHashSet<Resource>();

		public PatternVirtualFileVisitor(String rootPath, String subPattern, PathMatcher pathMatcher) {
			this.subPattern = subPattern;
			this.pathMatcher = pathMatcher;
			this.rootPath = (rootPath.length() == 0 || rootPath.endsWith("/") ? rootPath : rootPath + "/");
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if (Object.class.equals(method.getDeclaringClass())) {
				if (methodName.equals("equals")) {
					// Only consider equal when proxies are identical.
					// 只有当代理相同时才考虑相等。
					return (proxy == args[0]);
				}
				else if (methodName.equals("hashCode")) {
					return System.identityHashCode(proxy);
				}
			}
			else if ("getAttributes".equals(methodName)) {
				return getAttributes();
			}
			else if ("visit".equals(methodName)) {
				visit(args[0]);
				return null;
			}
			else if ("toString".equals(methodName)) {
				return toString();
			}

			throw new IllegalStateException("Unexpected method invocation: " + method);
		}

		public void visit(Object vfsResource) {
			if (this.pathMatcher.match(this.subPattern,
					VfsPatternUtils.getPath(vfsResource).substring(this.rootPath.length()))) {
				this.resources.add(new VfsResource(vfsResource));
			}
		}

		public Object getAttributes() {
			return VfsPatternUtils.getVisitorAttribute();
		}

		public Set<Resource> getResources() {
			return this.resources;
		}

		public int size() {
			return this.resources.size();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("sub-pattern: ").append(this.subPattern);
			sb.append(", resources: ").append(this.resources);
			return sb.toString();
		}
	}

}
