package cn.wulin.temp1.domain.environment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * 将使用 PropertiesLoaderSupport 加载的配置设置到标准的 environment中,方便使用
 * @author wulin
 *
 */
public class PropertiesToEnvironmentConfigurer implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {

	private StandardEnvironment environment;

	private final Log logger = LogFactory.getLog(getClass());

	private static final String FIELD_NAME_LOCATION = "locations";

	private String fileEncoding;

	private AtomicInteger count = new AtomicInteger();

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE-1000;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		String[] beanNames = beanFactory.getBeanNamesForType(PropertiesLoaderSupport.class);
		if (beanNames == null || beanNames.length == 0) {
			return;
		}

		for (String beanName : beanNames) {
			Resource[] locations = getLocations(beanFactory, beanName);

			if (locations == null) {
				continue;
			}

			Properties props = new Properties();
			loadProperties(locations, props);

			// 添加到环境中
			addEnvironment(props);
		}
	}

	/**
	 * 添加到环境中
	 * 
	 * @param props
	 */
	private void addEnvironment(Properties props) {
		Properties newProps = getNewProps(props);
		// 添加到环境中
		PropertySource<?> localPropertySource = new PropertiesPropertySource("CustomizeEnvironment"+count.getAndIncrement(), newProps);
		environment.getPropertySources().addLast(localPropertySource);
	}

	/**
	 * 得到要添加到标准环境中的属性
	 * 
	 * @param props
	 * @return
	 */
	private Properties getNewProps(Properties props) {
		Properties newProps = new Properties();
		Enumeration<Object> keys = props.keys();
		while (keys.hasMoreElements()) {
			Object nextElement = keys.nextElement();
			if (nextElement instanceof String) {
				String key = (String) nextElement;

				if (!environment.containsProperty(key)) {
					newProps.setProperty(key, props.getProperty(key));

				}
			}
		}
		return newProps;
	}

	private Resource[] getLocations(ConfigurableListableBeanFactory beanFactory, String beanName) {
		PropertiesLoaderSupport bean = beanFactory.getBean(beanName, PropertiesLoaderSupport.class);

		try {
			Field field = PropertiesLoaderSupport.class.getDeclaredField(FIELD_NAME_LOCATION);

			if (!field.isAccessible()) {
				field.setAccessible(true);
			}

			Object object = field.get(bean);
			if (object instanceof Resource[]) {
				return (Resource[]) object;
			}
		} catch (Exception e) {
			logger.error("获取  " + FIELD_NAME_LOCATION + "是失败!", e);
		}
		return null;
	}

	@Override
	public void setEnvironment(Environment environment) {
		if (environment instanceof StandardEnvironment) {
			this.environment = (StandardEnvironment) environment;
		}

	}

	protected void loadProperties(Resource[] locations, Properties props) {
		if (locations != null) {
			for (Resource location : locations) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					PropertiesLoaderUtils.fillProperties(props, new EncodedResource(location, fileEncoding));
				} catch (IOException e) {
					logger.error("获取资源失败!", e);
				}
			}
		}
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}
}
