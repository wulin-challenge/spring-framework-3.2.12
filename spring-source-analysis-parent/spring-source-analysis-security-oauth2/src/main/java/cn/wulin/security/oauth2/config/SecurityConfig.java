package cn.wulin.security.oauth2.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import cn.wulin.security.oauth2.helper.SecurityContextRepositoryHelper;
import cn.wulin.security.oauth2.token.authentication.CustomTokenRequestMatcher;
import cn.wulin.security.oauth2.token.authentication.CustomTokenSecurityConfig;
import cn.wulin.security.oauth2.token.authentication.entry_point.CustomTokenAuthenticationEntryPoint;
import cn.wulin.security.oauth2.token.default_page.CustomTokenDefaultLoginPageConfigurer;
import cn.wulin.security.oauth2.token.persistence.CustomTokenDelegateSecurityContextRepository;
import cn.wulin.security.oauth2.token.persistence.CustomTokenPersistenceRequestMatcher;


@Configuration
@EnableWebSecurity(debug=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
//	private AuthenticationManagerBuilder auth;
//	
//	private UserDetailsService userDetailsService;
//	
//	@Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//    	this.auth = auth;
//    	// 只为暴露出 userDetailsService
//    			InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager(new ArrayList<UserDetails>());
//    			userDetailsService = inMemoryUserDetailsManager;
//    			MyInMemoryUserDetailsManagerConfigurer configurer = new MyInMemoryUserDetailsManagerConfigurer<>(inMemoryUserDetailsManager);
//    			configurer
//    			 .withUser("user").password("password").roles("USER");
//    			
//    			auth.apply(configurer);
//    			
//    			
////    	        auth
////    	            .inMemoryAuthentication()
////    	                .withUser("user").password("password").roles("USER");
//    }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.securityContext().securityContextRepository(SecurityContextRepositoryHelper.getSecurityContextRepository(http))
		.and()
		.csrf().disable()
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .formLogin()
//        .loginPage("/entry")
        .and()
        .httpBasic();
	}
//	
//	private AuthenticationEntryPoint getAuthenticationEntryPoint(HttpSecurity http) {
//		//处理token的情况
//		LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<RequestMatcher, AuthenticationEntryPoint>();
//		entryPoints.put(new CustomTokenRequestMatcher(new CustomTokenPersistenceRequestMatcher()), new CustomTokenAuthenticationEntryPoint("/tokenNoLogin"));
//		
//		//
//		 ContentNegotiationStrategy contentNegotiationStrategy = http.getSharedObject(ContentNegotiationStrategy.class);
//        if(contentNegotiationStrategy == null) {
//            contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
//        }
//        MediaTypeRequestMatcher preferredMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.APPLICATION_XHTML_XML, new MediaType("image","*"), MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
//        preferredMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
//    
//        entryPoints.put(preferredMatcher, new LoginUrlAuthenticationEntryPoint("/login"));
//        DelegatingAuthenticationEntryPoint delegatingEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
//	        
//        
//		// 默认处理web情况
//		delegatingEntryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
//		return delegatingEntryPoint;
//	}
	
	
}
