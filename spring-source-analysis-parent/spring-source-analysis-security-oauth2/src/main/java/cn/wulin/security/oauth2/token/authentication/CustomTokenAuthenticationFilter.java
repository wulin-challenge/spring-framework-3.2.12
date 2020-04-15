package cn.wulin.security.oauth2.token.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 自定义token身份认证filter
 * 
 * <p> 注意: 要进行token登录或者通过token的方式获取受保护的数据,必须满足如下条件
 * <p> 1. 请求头的必须要有 request_type属性,且request_type=token
 * <p> 2. 若 request_type=token,若请求头中必须要有token这个属性
 * @author wulin
 *
 */
public class CustomTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter{
	
	public static final String REQUEST_TYPE_KEY = "request_type";
	public static final String TOKEN_KEY = "token";
	
	/**
	 * 用户名参数
	 */
	private String usernameParameter = "username";
	
	/**
	 * 密码参数
	 */
    private String passwordParameter = "password";
    private boolean postOnly = true;
    
	public CustomTokenAuthenticationFilter() {
		this(new CustomTokenRequestMatcher("/login"));
	}

	public CustomTokenAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
		super(requiresAuthenticationRequestMatcher);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)throws AuthenticationException, IOException, ServletException {
		if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("认证方法类型不支持: " + request.getMethod());
        }

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();
	
        CustomTokenUsernamePasswordAuthenticationToken authRequest = new CustomTokenUsernamePasswordAuthenticationToken(username, password);
        // 允许子类设置“详细信息”属性
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
	}
	
	/**
     * 获取密码的值
     */
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(passwordParameter);
    }

    /**
     * 获取用户名的值
     */
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(usernameParameter);
    }
    
    /**
     * Provided so that subclasses may configure what is put into the authentication request's details
     * property.
     * 
     * <p> 提供以便子类可以配置放入身份验证请求的details属性的内容。
     *
     * @param request that an authentication request is being created for
     * 
     * <p> 正在为其创建身份验证请求
     * 
     * @param authRequest the authentication request object that should have its details set
     * 
     * <p> 应设置其详细信息的身份验证请求对象
     */
    protected void setDetails(HttpServletRequest request, CustomTokenUsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

}
