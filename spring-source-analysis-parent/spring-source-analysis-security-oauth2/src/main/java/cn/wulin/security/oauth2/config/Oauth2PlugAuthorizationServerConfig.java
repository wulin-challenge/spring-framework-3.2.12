package cn.wulin.security.oauth2.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;


/**
 * 认证服务配置
 * @author wubo
 *
 */
@Configuration
@EnableAuthorizationServer
//@DependsOn("securityConfig")
//@Import({ObjectPostProcessorConfiguration.class,AuthenticationConfiguration.class})
//@Import({WebSecurityConfiguration.class,ObjectPostProcessorConfiguration.class,AuthenticationConfiguration.class})
public class Oauth2PlugAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter implements BeanFactoryAware{
	
//	@Value("${appId}")
//	private String appId;
//	
//	@Value("${appSecret}")
//	private String appSecret;
	
//	@Autowired
//	private ClientDetailsService clientDetails;
//	
//	@Autowired
//	private AuthorizationServerTokenServices tokenServices;
//	
//	@Autowired
//	private AuthorizationCodeServices authorizationCodeServices;
//	
//	@Autowired
//	private OAuth2RequestFactory requestFactory;
//	
//	@Autowired
//	private AuthenticationManager authenticationManager2;
	
	private AuthenticationManager authenticationManager;

	private AuthenticationManagerBuilder auth;
	private SecurityConfig securityConfig;
	private AuthorizationServerEndpointsConfigurer endpoints;
	
	
	
	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,SecurityConfig securityConfig) throws Exception {
		
//		
        auth
            .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER");
          this.auth = auth;
          this.securityConfig = securityConfig;
//        AuthenticationManager object = auth.getObject();
//        authenticationManager = auth.build();
    }
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//		clients.setBuilder(builder);
		InMemoryClientDetailsServiceBuilder builder = clients.inMemory();
		builder.withClient("test")
				.secret("test")
				.autoApprove(true)// 设置自动批准,就不在弹出确认批准请求,必须要求 scopes设置为 "all"才行
				.autoApprove("all")
				.scopes("all")
				.authorizedGrantTypes("authorization_code")
				.authorizedGrantTypes("password");
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		this.endpoints = endpoints;
		System.out.println();
//		AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
//		ClientDetailsService clientDetails = endpoints.getClientDetailsService();
//		OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();
//		
//		endpoints.tokenGranter(new MyResourceOwnerPasswordTokenGranter(null,auth, tokenServices,
//						clientDetails, requestFactory));
		
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Object bean = beanFactory.getBean("springSecurityFilterChain");
		authenticationManager = auth.getObject();
		
		endpoints.authenticationManager(authenticationManager);
		System.out.println(bean);
		
	}
	

}
