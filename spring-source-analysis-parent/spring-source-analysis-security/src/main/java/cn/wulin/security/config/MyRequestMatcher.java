package cn.wulin.security.config;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 自定义匹配器放行某个请求
 * @author wulin
 *
 */
public class MyRequestMatcher implements RequestMatcher {
	
	private Set<String> permitRequest = new HashSet<String>();

	@Override
	public boolean matches(HttpServletRequest request) {
		
		String url = UrlUtils.buildFullRequestUrl(request);
		if(permitRequest.contains(url)) {
			return true;
		}
		return false;
	}

}
