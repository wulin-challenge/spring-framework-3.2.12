package cn.wulin.temp1.domain.environment;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * 属性必须以 user为前缀
 * @author wulin
 *
 */
public class UserProperties implements BeanFactoryAware,EnvironmentAware,InitializingBean{
	private Environment environment;
	private DefaultListableBeanFactory beanFactory;
	
	private Long id;
	
	private String username;
	
	private String password;
	
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		id = environment.getProperty("user.id", Long.class);
		username = environment.getProperty("user.username", String.class);
		password = environment.getProperty("user.password", String.class);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if(beanFactory instanceof DefaultListableBeanFactory) {
			this.beanFactory = (DefaultListableBeanFactory)beanFactory;
		}
	}
}
