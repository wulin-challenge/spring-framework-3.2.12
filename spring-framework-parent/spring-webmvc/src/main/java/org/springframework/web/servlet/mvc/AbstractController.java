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

package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.WebUtils;

/**
 * <p>Convenient superclass for controller implementations, using the Template
 * Method design pattern.</p>
 * 
 * <p> 控制器实现的便捷超类，使用模板方法设计模式。
 *
 * <p>As stated in the {@link Controller Controller}
 * interface, a lot of functionality is already provided by certain abstract
 * base controllers. The AbstractController is one of the most important
 * abstract base controller providing basic features such as the generation
 * of caching headers and the enabling or disabling of
 * supported methods (GET/POST).</p>
 * 
 * <p> 如Controller接口中所述，某些抽象基本控制器已经提供了许多功能。 AbstractController是最重要的
 * 抽象基本控制器之一，它提供了一些基本功能，例如生成缓存标头以及启用或禁用支持的方法（GET / POST）。
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="Controller.html#workflow">and that defined by interface</a>):</b><br>
 * 工作流程（由界面定义）：
 * <ol>
 *  <li>{@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest()}
 *      will be called by the DispatcherServlet</li>
 *  <li> 
 *  <li> handleRequest（）将由DispatcherServlet调用
 *  <li> 
 *  <li>Inspection of supported methods (ServletException if request method
 *      is not support)</li>
 *  <li> 
 *  <li> 检查支持的方法（如果请求方法不支持，则为ServletException）
 *  <li>     
 *  <li>If session is required, try to get it (ServletException if not found)</li>
 *  <li> 
 *  <li> 如果需要session，请尝试获取它（如果找不到ServletException）
 *  <li> 
 *  <li>Set caching headers if needed according to the cacheSeconds property</li>
 *  <li> 
 *  <li> 根据cacheSeconds属性，根据需要设置缓存头
 *  <li> 
 *  <li>Call abstract method {@link #handleRequestInternal(HttpServletRequest, HttpServletResponse) handleRequestInternal()}
 *      (optionally synchronizing around the call on the HttpSession),
 *      which should be implemented by extending classes to provide actual
 *      functionality to return {@link org.springframework.web.servlet.ModelAndView ModelAndView} objects.</li>
 *  <li> 
 *  <li> 调用抽象方法handleRequestInternal（）（可选地围绕HttpSession上的调用进行同步），这应该通过扩展类来实现，
 *  以提供返回ModelAndView对象的实际功能。
 *  <li> 
 * </ol>
 * </p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="Controller.html#config">and those defined by interface</a>):</b><br>
 * 暴露的配置属性（以及接口定义的属性）：
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></th>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>supportedMethods</td>
 *      <td>GET,POST</td>
 *      <td>comma-separated (CSV) list of methods supported by this controller,
 *          such as GET, POST and PUT
 *          
 *          <br/><br/>
 *          此控制器支持的逗号分隔（CSV）方法列表，例如GET，POST和PUT
 *          </td>
 *  </tr>
 *  <tr>
 *      <td>requireSession</td>
 *      <td>false</td>
 *      <td>whether a session should be required for requests to be able to
 *          be handled by this controller. This ensures that derived controller
 *          can - without fear of null pointers - call request.getSession() to
 *          retrieve a session. If no session can be found while processing
 *          the request, a ServletException will be thrown
 *          <br/><br/>
 *          是否应该要求会话以使该控制器能够处理请求。 这可以确保派生控制器可以 - 无需担心空指针 - 调用
 *          request.getSession（）来检索会话。 如果在处理请求时找不到会话，则抛出ServletException
 *          </td>
 *  </tr>
 *  <tr>
 *      <td>cacheSeconds</td>
 *      <td>-1</td>
 *      <td>indicates the amount of seconds to include in the cache header
 *          for the response following on this request. 0 (zero) will include
 *          headers for no caching at all, -1 (the default) will not generate
 *          <i>any headers</i> and any positive number will generate headers
 *          that state the amount indicated as seconds to cache the content
 *          
 *          <br/><br/>
 *          
 *          表示此请求后面的响应的缓存头中包含的秒数。 0（零）将包括根本不缓存的标头，-1（默认值）不会生成任何标头，
 *          任何正数将生成标头，表示缓存内容的秒数
 *          </td>
 *  </tr>
 *  <tr>
 *      <td>synchronizeOnSession</td>
 *      <td>false</td>
 *      <td>whether the call to {@code handleRequestInternal} should be
 *          synchronized around the HttpSession, to serialize invocations
 *          from the same client. No effect if there is no HttpSession.
 *          
 *          <br/><br/>
 *          
 *          是否应该围绕HttpSession同步对handleRequestInternal的调用，以序列化来自同一客户端的调用。 
 *          如果没有HttpSession，则无效。
 *      </td>
 *  </tr>
 * </table>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see WebContentInterceptor
 */
public abstract class AbstractController extends WebContentGenerator implements Controller {

	private boolean synchronizeOnSession = false;


	/**
	 * Set if controller execution should be synchronized on the session,
	 * to serialize parallel invocations from the same client.
	 * 
	 * <p> 设置是否应在会话上同步控制器执行，以序列化来自同一客户端的并行调用。
	 * 
	 * <p>More specifically, the execution of the {@code handleRequestInternal}
	 * method will get synchronized if this flag is "true". The best available
	 * session mutex will be used for the synchronization; ideally, this will
	 * be a mutex exposed by HttpSessionMutexListener.
	 * 
	 * <p> 更具体地说，如果此标志为“true”，则handleRequestInternal方法的执行将同步。 
	 * 最佳可用会话互斥锁将用于同步; 理想情况下，这将是HttpSessionMutexListener公开的互斥锁。
	 * 
	 * <p>The session mutex is guaranteed to be the same object during
	 * the entire lifetime of the session, available under the key defined
	 * by the {@code SESSION_MUTEX_ATTRIBUTE} constant. It serves as a
	 * safe reference to synchronize on for locking on the current session.
	 * 
	 * <p> 会话互斥锁在会话的整个生命周期内保证是相同的对象，在SESSION_MUTEX_ATTRIBUTE常量定义的密钥下可用。 
	 * 它用作同步锁定当前会话的安全引用。
	 * 
	 * <p>In many cases, the HttpSession reference itself is a safe mutex
	 * as well, since it will always be the same object reference for the
	 * same active logical session. However, this is not guaranteed across
	 * different servlet containers; the only 100% safe way is a session mutex.
	 * 
	 * <p> 在许多情况下，HttpSession引用本身也是一个安全的互斥锁，因为它始终是同一个活动逻辑会话的相同对象引用。
	 *  但是，不能在不同的servlet容器中保证这一点; 唯一100％安全的方式是会话互斥。
	 *  
	 * @see AbstractController#handleRequestInternal
	 * @see org.springframework.web.util.HttpSessionMutexListener
	 * @see org.springframework.web.util.WebUtils#getSessionMutex(javax.servlet.http.HttpSession)
	 */
	public final void setSynchronizeOnSession(boolean synchronizeOnSession) {
		this.synchronizeOnSession = synchronizeOnSession;
	}

	/**
	 * Return whether controller execution should be synchronized on the session.
	 * 
	 * <p> 返回是否应在会话上同步控制器执行。
	 */
	public final boolean isSynchronizeOnSession() {
		return this.synchronizeOnSession;
	}


	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// Delegate to WebContentGenerator for checking and preparing.
		// 委托WebContentGenerator进行检查和准备。
		checkAndPrepare(request, response, this instanceof LastModified);

		// Execute handleRequestInternal in synchronized block if required.
		// 如果需要，在synchronized块中执行handleRequestInternal。
		if (this.synchronizeOnSession) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
					return handleRequestInternal(request, response);
				}
			}
		}

		return handleRequestInternal(request, response);
	}

	/**
	 * Template method. Subclasses must implement this.
	 * The contract is the same as for {@code handleRequest}.
	 * 
	 * <p> 模板方法。 子类必须实现这一点。 合同与handleRequest相同
	 * 
	 * @see #handleRequest
	 */
	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	    throws Exception;

}
