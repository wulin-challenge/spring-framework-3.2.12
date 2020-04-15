package cn.wulin.security.oauth2.token.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomTokenSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>{

	private UserDetailsService securityUserDetailService;
	
	public CustomTokenSecurityConfig(UserDetailsService securityUserDetailService) {
		super();
		this.securityUserDetailService = securityUserDetailService;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		
		CustomTokenAuthenticationFilter usernameAuthenticationFilter = new CustomTokenAuthenticationFilter();
		usernameAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		CustomTokenAuthenticationProvider usernameAuthenticationProvider = new CustomTokenAuthenticationProvider();
		usernameAuthenticationProvider.setUserDetailsService(securityUserDetailService);
		usernameAuthenticationFilter.setAuthenticationSuccessHandler(new CustomTokenAuthenticationSuccessHandler());
//		usernameAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
		http.authenticationProvider(usernameAuthenticationProvider)
			.addFilterBefore(usernameAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
