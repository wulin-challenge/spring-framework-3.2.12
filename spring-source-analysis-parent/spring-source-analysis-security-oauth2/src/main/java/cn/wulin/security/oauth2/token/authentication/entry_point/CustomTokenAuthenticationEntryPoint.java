package cn.wulin.security.oauth2.token.authentication.entry_point;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;

/**
 * 参考:  org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
 * @author wulin
 *
 */
public class CustomTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private static final Log logger = LogFactory.getLog(CustomTokenAuthenticationEntryPoint.class);
	
	private String loginFormUrl;
	
	private PortMapper portMapper = new PortMapperImpl();

	private PortResolver portResolver = new PortResolverImpl();
	
	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
    public CustomTokenAuthenticationEntryPoint(String loginFormUrl) {
		super();
		this.loginFormUrl = loginFormUrl;
	}

	@Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
		String redirectUrl = null;

		if ("http".equals(request.getScheme()) && !isAjaxRequest(request)) {
			// First redirect the current request to HTTPS.
			// When that request is received, the forward to the login page will be
			// used.
			redirectUrl = buildHttpsRedirectUrlForRequest(request);
		}

		if (redirectUrl == null) {
			String loginForm = determineUrlToUseForThisRequest(request, response,
					authException);

			if (logger.isDebugEnabled()) {
				logger.debug("Server side forward to: " + loginForm);
			}

			RequestDispatcher dispatcher = request.getRequestDispatcher(loginForm);

			dispatcher.forward(request, response);

			return;
		}
	    // redirect to login page
		redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
		redirectStrategy.sendRedirect(request, response, redirectUrl);
    }
	
	/**
	 * 检查是否Ajax请求
	 * @param request
	 * @return ajax请求返回true,反之返回false
	 */
	private boolean isAjaxRequest(HttpServletRequest request) {
		return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
	}
    
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException) {

		String loginForm = determineUrlToUseForThisRequest(request, response,
				authException);

		if (UrlUtils.isAbsoluteUrl(loginForm)) {
			return loginForm;
		}

		int serverPort = portResolver.getServerPort(request);
		String scheme = request.getScheme();

		RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();

		urlBuilder.setScheme(scheme);
		urlBuilder.setServerName(request.getServerName());
		urlBuilder.setPort(serverPort);
		urlBuilder.setContextPath(request.getContextPath());
		urlBuilder.setPathInfo(loginForm);

		return urlBuilder.getUrl();
	}
    
    /**
	 * Allows subclasses to modify the login form URL that should be applicable for a
	 * given request.
	 *
	 * @param request the request
	 * @param response the response
	 * @param exception the exception
	 * @return the URL (cannot be null or empty; defaults to {@link #getLoginFormUrl()})
	 */
	protected String determineUrlToUseForThisRequest(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception) {

		return getLoginFormUrl();
	}
	
	public String getLoginFormUrl() {
		return loginFormUrl;
	}
	
	/**
	 * Builds a URL to redirect the supplied request to HTTPS. Used to redirect the
	 * current request to HTTPS, before doing a forward to the login page.
	 */
	protected String buildHttpsRedirectUrlForRequest(HttpServletRequest request)
			throws IOException, ServletException {

		int serverPort = portResolver.getServerPort(request);
		Integer httpsPort = portMapper.lookupHttpsPort(Integer.valueOf(serverPort));

		if (httpsPort != null) {
			RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
			urlBuilder.setScheme("https");
			urlBuilder.setServerName(request.getServerName());
			urlBuilder.setPort(httpsPort.intValue());
			urlBuilder.setContextPath(request.getContextPath());
			urlBuilder.setServletPath(request.getServletPath());
			urlBuilder.setPathInfo(request.getPathInfo());
			urlBuilder.setQuery(request.getQueryString());

			return urlBuilder.getUrl();
		}

		// Fall through to server-side forward with warning message
		logger.warn("Unable to redirect to HTTPS as no port mapping found for HTTP port "
				+ serverPort);

		return null;
	}
	
}