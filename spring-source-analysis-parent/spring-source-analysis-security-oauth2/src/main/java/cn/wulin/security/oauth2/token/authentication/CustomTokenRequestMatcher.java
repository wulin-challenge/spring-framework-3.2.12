package cn.wulin.security.oauth2.token.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

/**
 * 判断当前请求是否为 token 请求,若为token请求,则需要做两件事
 * <p> a. 判断当前请求是否满足通过token获取 SecurityContext
 * <p> b. 判断当前请求是否满足token的登录方式
 * <p> 上述两个判断的具体匹配则交给 委托的 requestMatcher进行具体实现,本RequestMatcher只负责判断当前请求是否满足token请求
 * 
 * <p> 注意: 要进行token登录或者通过token的方式获取受保护的数据,必须满足如下条件
 * <p> 1. 请求头的必须要有 request_type属性,且request_type=token
 * <p> 2. 若 request_type=token,若请求头中必须要有token这个属性
 * @author ThinkPad
 */
public class CustomTokenRequestMatcher implements RequestMatcher{
	private RequestMatcher requestMatcher;
	
	/**
	 * 登录处理url
	 * @param loginProcessingUrl
	 */
	public CustomTokenRequestMatcher(String loginProcessingUrl) {
		super();
		this.requestMatcher = createLoginProcessingUrlMatcher(loginProcessingUrl);
	}
	public CustomTokenRequestMatcher(RequestMatcher requestMatcher) {
		super();
		this.requestMatcher = requestMatcher;
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		
		// 判断是否为token请求,若是token请求,则委托给 requestMatcher 进行具体的判断
		String requestType = request.getHeader(CustomTokenAuthenticationFilter.REQUEST_TYPE_KEY);
		if(StringUtils.hasText(requestType) && CustomTokenAuthenticationFilter.TOKEN_KEY.equals(requestType)) {
			return requestMatcher.matches(request);
		}
		return false;
	}
	
	/**
	 * 创建登录处理url匹配器
	 * @param loginProcessingUrl 登录处理url
	 * @return
	 */
    private RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

}
