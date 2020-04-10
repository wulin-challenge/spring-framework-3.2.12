package cn.wulin.security.token.persistence;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import cn.wulin.security.token.authentication.CustomTokenRequestMatcher;

/**
 * 自定义的token委托SecurityContext存储器
 * @author wulin
 */
public class CustomTokenDelegateSecurityContextRepository implements SecurityContextRepository{
	private SecurityContextRepository sessionRepository;
	private SecurityContextRepository delegateRepository;
	private HttpSecurity http;
	private RequestMatcher requestMatcher;
	
	public CustomTokenDelegateSecurityContextRepository(HttpSecurity http) {
		super();
		this.http = http;
		sessionRepository = buildSessionRepository();
		delegateRepository = new CustomTokenMemorySecurityContextRepository();
		requestMatcher = buildPersistenceRequestMatcher();
	}

	public CustomTokenDelegateSecurityContextRepository(HttpSecurity http,SecurityContextRepository sessionRepository,
			SecurityContextRepository delegateRepository, RequestMatcher requestMatcher) {
		super();
		this.http = http;
		this.sessionRepository = sessionRepository;
		this.delegateRepository = delegateRepository;
		this.requestMatcher = requestMatcher;
	}

	@Override
	public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
		if(requestMatcher.matches(requestResponseHolder.getRequest())) {
			return delegateRepository.loadContext(requestResponseHolder);
		}else {
			return sessionRepository.loadContext(requestResponseHolder);
		}
	}

	@Override
	public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
		if(requestMatcher.matches(request)) {
			delegateRepository.saveContext(context, request, response);
		}else {
			sessionRepository.saveContext(context, request, response);
		}
	}

	@Override
	public boolean containsContext(HttpServletRequest request) {
		if(requestMatcher.matches(request)) {
			return delegateRepository.containsContext(request);
		}else {
			return sessionRepository.containsContext(request);
		}
	}

	/**
	 * 构建存储请求匹配器
	 * @return
	 */
	private RequestMatcher buildPersistenceRequestMatcher() {
		return new CustomTokenRequestMatcher(new CustomTokenPersistenceRequestMatcher());
	}
	/**
	 * 构建sessionRepository
	 */
	private SecurityContextRepository buildSessionRepository() {
		SecurityContextRepository securityContextRepository = http.getSharedObject(SecurityContextRepository.class);
		if(securityContextRepository == null) {
		   HttpSessionSecurityContextRepository httpSecurityRepository = new HttpSessionSecurityContextRepository();
           httpSecurityRepository.setDisableUrlRewriting(true);
           httpSecurityRepository.setAllowSessionCreation(true);
           AuthenticationTrustResolver trustResolver = http.getSharedObject(AuthenticationTrustResolver.class);
           if(trustResolver != null) {
               httpSecurityRepository.setTrustResolver(trustResolver);
           }
           
           securityContextRepository = httpSecurityRepository;
	   }
	   return securityContextRepository;
	}
}
