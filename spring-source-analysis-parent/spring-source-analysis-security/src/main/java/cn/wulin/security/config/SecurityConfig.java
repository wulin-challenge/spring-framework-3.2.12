package cn.wulin.security.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import cn.wulin.security.filter.MySecurityFilter;
import cn.wulin.security.token.authentication.CustomTokenAuthenticationFilter;
import cn.wulin.security.token.authentication.CustomTokenRequestMatcher;
import cn.wulin.security.token.authentication.CustomTokenSecurityConfig;
import cn.wulin.security.token.authentication.entry_point.CustomTokenAuthenticationEntryPoint;
import cn.wulin.security.token.default_page.CustomTokenDefaultLoginPageConfigurer;
import cn.wulin.security.token.persistence.CustomTokenDelegateSecurityContextRepository;
import cn.wulin.security.token.persistence.CustomTokenPersistenceRequestMatcher;

@Configuration
@EnableWebSecurity(debug=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private UserDetailsService userDetailsService;
	
	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// 只为暴露出 userDetailsService
		InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager(new ArrayList<UserDetails>());
		userDetailsService = inMemoryUserDetailsManager;
		MyInMemoryUserDetailsManagerConfigurer configurer = new MyInMemoryUserDetailsManagerConfigurer<>(inMemoryUserDetailsManager);
		configurer
		 .withUser("user").password("password").roles("USER");
		
		auth.apply(configurer);
		
		
//        auth
//            .inMemoryAuthentication()
//                .withUser("user").password("password").roles("USER");
        
    }
	
	

//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
////		super.configure(auth);
//		auth.authenticationProvider(customTokenAuthenticationProvider);
//	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
//		http.authorizeRequests().antMatchers("/**/userlist/**").permitAll();
//		http.addFilterBefore(new MySecurityFilter(),FilterSecurityInterceptor.class);
		http
		.apply(new CustomTokenSecurityConfig(userDetailsService)).and()
		.apply(new CustomTokenDefaultLoginPageConfigurer()).and()
		.securityContext().securityContextRepository(new CustomTokenDelegateSecurityContextRepository(http))
		.and()
		.csrf().disable()
		.exceptionHandling()
		.authenticationEntryPoint(getAuthenticationEntryPoint(http))
		.and()
        .authorizeRequests()
            .requestMatchers(new MyRequestMatcher()).permitAll()
            .anyRequest().authenticated()
            .and()
        .formLogin()
//        .loginPage("/entry")
        .and()
        .httpBasic().and()
        .addFilterBefore(new MySecurityFilter(),FilterSecurityInterceptor.class);
		
		//添加 CustomTokenAuthenticationFilterfilter
//		addAuthenticatonFilter(http);
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
	
	/**
	 * TODO 添加 CustomTokenAuthenticationFilterfilter,不能使用这种方式添加,因为方式 得到 AuthenticationManager有问题,
	 *  待研究
	 * @param http
	 * @throws Exception
	 */
	private void addAuthenticatonFilter(HttpSecurity http) throws Exception {
		CustomTokenAuthenticationFilter filter = new CustomTokenAuthenticationFilter();
		filter.setAuthenticationManager(authenticationManager());
		
		//这个方式添加的 authenticationProvider 有问题,待研究
//		CustomTokenAuthenticationProvider usernameAuthenticationProvider = new CustomTokenAuthenticationProvider();
//		http.authenticationProvider(usernameAuthenticationProvider);
		
		http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
	}
	
//	 SecurityContextRepository securityContextRepository = http.getSharedObject(SecurityContextRepository.class);
//     boolean stateless = isStateless();
//
//     if(securityContextRepository == null) {
//         if(stateless) {
//             http.setSharedObject(SecurityContextRepository.class, new NullSecurityContextRepository());
//         } else {
//             HttpSessionSecurityContextRepository httpSecurityRepository = new HttpSessionSecurityContextRepository();
//             httpSecurityRepository.setDisableUrlRewriting(!enableSessionUrlRewriting);
//             httpSecurityRepository.setAllowSessionCreation(isAllowSessionCreation());
//             AuthenticationTrustResolver trustResolver = http.getSharedObject(AuthenticationTrustResolver.class);
//             if(trustResolver != null) {
//                 httpSecurityRepository.setTrustResolver(trustResolver);
//             }
//             http.setSharedObject(SecurityContextRepository.class, httpSecurityRepository);
//         }
//     }
	
}
