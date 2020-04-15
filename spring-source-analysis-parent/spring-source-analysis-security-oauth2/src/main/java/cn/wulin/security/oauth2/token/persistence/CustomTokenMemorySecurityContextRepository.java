package cn.wulin.security.oauth2.token.persistence;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;

import cn.wulin.security.oauth2.token.authentication.CustomTokenAuthenticationFilter;
import cn.wulin.security.oauth2.token.authentication.CustomTokenUsernamePasswordAuthenticationToken;

/**
 * 自定义的token 内存式SecurityContext存储器
 * @author wulin
 *
 */
public class CustomTokenMemorySecurityContextRepository implements SecurityContextRepository{
	private static final ConcurrentHashMap<String,SecurityContext> securityContextTable = new ConcurrentHashMap<String,SecurityContext>();
	@Override
	public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
		String token = getToken(requestResponseHolder.getRequest());
		if(StringUtils.hasText(token)) {
			SecurityContext securityContext = securityContextTable.get(token);
			if(securityContext != null) {
				return securityContext;
			}
		}
		return createEmptyContext();
	}

	@Override
	public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = context.getAuthentication();
		if(authentication instanceof CustomTokenUsernamePasswordAuthenticationToken) {
			CustomTokenUsernamePasswordAuthenticationToken authenticationToken = (CustomTokenUsernamePasswordAuthenticationToken)authentication;
			String token = authenticationToken.getToken();
			securityContextTable.put(token, context);
		}
	}

	@Override
	public boolean containsContext(HttpServletRequest request) {
		String token = getToken(request);
		if(StringUtils.hasText(token)) {
			SecurityContext securityContext = securityContextTable.get(token);
			if(securityContext != null) {
				return true;
			}
		}
		return false;
	}
	
	public SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }
	
	/**
	 * 通过request得到token值
	 * @param request
	 * @return
	 */
	private String getToken(HttpServletRequest request) {
		String requestType = request.getHeader(CustomTokenAuthenticationFilter.REQUEST_TYPE_KEY);
		if(StringUtils.hasText(requestType) && CustomTokenAuthenticationFilter.TOKEN_KEY.equals(requestType)) {
			String token = request.getHeader(CustomTokenAuthenticationFilter.TOKEN_KEY);
			if(StringUtils.hasText(token)) {
				return token;
			}
		}
		return null;
	}
}
