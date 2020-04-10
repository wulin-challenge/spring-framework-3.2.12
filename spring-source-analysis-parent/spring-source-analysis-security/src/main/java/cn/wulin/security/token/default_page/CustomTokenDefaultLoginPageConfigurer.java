package cn.wulin.security.token.default_page;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

/**
 * 自定义默认等领域FilterConfig,为什么要自定义呢?
 * <p> 因为 DefaultLoginPageConfigurer 中当指定了 ExceptionHandlingConfigurer的AuthenticationEntryPoint
 * <p> 默认的 DefaultLoginPageGeneratingFilter 就不会在添加,这里只是为了测试避免在自定义登录页,所以将将其添加
 * @author wulin
 *
 */
public final class CustomTokenDefaultLoginPageConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = new DefaultLoginPageGeneratingFilter();

	@Override
	public void init(HttpSecurity http) throws Exception {
		 http.setSharedObject(DefaultLoginPageGeneratingFilter.class, loginPageGeneratingFilter);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		 AuthenticationEntryPoint authenticationEntryPoint = null;
        loginPageGeneratingFilter = postProcess(loginPageGeneratingFilter);
        http.addFilter(loginPageGeneratingFilter);
	}
}