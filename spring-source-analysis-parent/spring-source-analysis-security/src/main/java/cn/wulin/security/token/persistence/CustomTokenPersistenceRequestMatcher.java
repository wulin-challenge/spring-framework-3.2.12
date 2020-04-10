package cn.wulin.security.token.persistence;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 该匹配器的作用是判断 CustomTokenSecurityContextRepository 是否满足token请求,若是token请求,
 * 则交由 CustomTokenSecurityContextRepository 尝试获取SecurityContext
 * @author wulin
 *
 */
public class CustomTokenPersistenceRequestMatcher implements RequestMatcher{

	/**
	 * 直接返回true,具体判断当前请求是否为token请求的逻辑交给 CustomTokenRequestMatcher 实现
	 */
	@Override
	public boolean matches(HttpServletRequest request) {
		return true;
	}
}
