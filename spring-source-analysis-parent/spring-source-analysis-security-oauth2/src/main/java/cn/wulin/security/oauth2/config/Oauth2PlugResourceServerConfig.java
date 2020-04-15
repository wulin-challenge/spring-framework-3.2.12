package cn.wulin.security.oauth2.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
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

/**
 * 资源服务配置
 * @author wubo
 *
 */
@Configuration
@EnableResourceServer

//@Import({WebSecurityConfiguration.class,ObjectPostProcessorConfiguration.class})
//@EnableGlobalAuthentication
//@EnableGlobalMethodSecurity(prePostEnabled=true) //对应的权限表达式为  @PreAuthorize("hasAuthority('ROLE_TELLER')")
public class Oauth2PlugResourceServerConfig extends ResourceServerConfigurerAdapter{
	
private AuthenticationManagerBuilder auth;
	
	private UserDetailsService userDetailsService;
	
	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	this.auth = auth;
    	// 只为暴露出 userDetailsService
    			InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager(new ArrayList<UserDetails>());
    			userDetailsService = inMemoryUserDetailsManager;
    			MyInMemoryUserDetailsManagerConfigurer configurer = new MyInMemoryUserDetailsManagerConfigurer<>(inMemoryUserDetailsManager);
    			configurer
    			 .withUser("user").password("password").roles("USER");
    			
    			auth.apply(configurer);
    			
    			
//    	        auth
//    	            .inMemoryAuthentication()
//    	                .withUser("user").password("password").roles("USER");
    }
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
//		resources.authenticationEntryPoint(oAuth2PlugAuthenticationEntryPoint);
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
		.apply(new CustomTokenSecurityConfig(userDetailsService)).and()
		.apply(new CustomTokenDefaultLoginPageConfigurer()).and()
		.securityContext().securityContextRepository(SecurityContextRepositoryHelper.getSecurityContextRepository(http))
		.and()
		.csrf().disable()
		.exceptionHandling()
		.authenticationEntryPoint(getAuthenticationEntryPoint(http))
		.and()
        .authorizeRequests()
        .antMatchers(findAllFilterURL())
		.permitAll()
            .anyRequest().authenticated()
            .and()
        .formLogin()
//        .loginPage("/entry")
        .and()
        .httpBasic();
	}
	
	private AuthenticationEntryPoint getAuthenticationEntryPoint(HttpSecurity http) {
		//处理token的情况
		LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<RequestMatcher, AuthenticationEntryPoint>();
		entryPoints.put(new CustomTokenRequestMatcher(new CustomTokenPersistenceRequestMatcher()), new CustomTokenAuthenticationEntryPoint("/tokenNoLogin"));
		
		//
		 ContentNegotiationStrategy contentNegotiationStrategy = http.getSharedObject(ContentNegotiationStrategy.class);
        if(contentNegotiationStrategy == null) {
            contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
        }
        MediaTypeRequestMatcher preferredMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.APPLICATION_XHTML_XML, new MediaType("image","*"), MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
        preferredMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
    
        entryPoints.put(preferredMatcher, new LoginUrlAuthenticationEntryPoint("/login"));
        DelegatingAuthenticationEntryPoint delegatingEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
	        
        
		// 默认处理web情况
		delegatingEntryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
		return delegatingEntryPoint;
	}
	
//	public void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests()
//		.antMatchers(findAllFilterURL())
//		.permitAll()
//		.anyRequest().authenticated()
//		.and()
//		.exceptionHandling()
////		.accessDeniedHandler(new Oauth2PlugAccessDeniedHandler())
//		.and()
//		.formLogin()
//		.and()
//		.csrf().disable();
//	}
//	
	public static String[] findAllFilterURL() throws IOException {
		String[] filterUrls = 
			{"/authorization/login","/authorization/logout"};
		return filterUrls;
	}
}
