package cn.wulin.security.oauth2.helper;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import cn.wulin.security.oauth2.token.persistence.CustomTokenDelegateSecurityContextRepository;

public class SecurityContextRepositoryHelper {
	
	private static CustomTokenDelegateSecurityContextRepository repository;
	
	public static CustomTokenDelegateSecurityContextRepository getSecurityContextRepository(HttpSecurity http) {
		if(repository == null) {
			synchronized(SecurityContextRepositoryHelper.class) {
				if(repository == null) {
					repository = new CustomTokenDelegateSecurityContextRepository(http);
				}
			}
		}else {
			return new CustomTokenDelegateSecurityContextRepository(http,repository.buildSessionRepository(),repository.getDelegateRepository(),repository.buildPersistenceRequestMatcher());
		}
		return repository;
	}
	

}
