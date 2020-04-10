package cn.wulin.security.oauth2.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

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
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
//		resources.authenticationEntryPoint(oAuth2PlugAuthenticationEntryPoint);
	}
	
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers(findAllFilterURL())
		.permitAll()
		.anyRequest().authenticated()
		.and()
		.exceptionHandling()
//		.accessDeniedHandler(new Oauth2PlugAccessDeniedHandler())
		.and()
		.formLogin()
		.and()
		.csrf().disable();
	}
	
	public static String[] findAllFilterURL() throws IOException {
		String[] filterUrls = 
			{"/authorization/login","/authorization/logout"};
		return filterUrls;
	}
}
