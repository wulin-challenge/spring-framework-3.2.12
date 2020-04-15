package cn.wulin.security.oauth2.config;

import org.springframework.security.config.annotation.authentication.ProviderManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.provisioning.UserDetailsManager;

public class MyInMemoryUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>> extends
        UserDetailsManagerConfigurer<B,MyInMemoryUserDetailsManagerConfigurer<B>> {

	public MyInMemoryUserDetailsManagerConfigurer(UserDetailsManager userDetailsManager) {
		super(userDetailsManager);
		// TODO Auto-generated constructor stub
	}

	
}