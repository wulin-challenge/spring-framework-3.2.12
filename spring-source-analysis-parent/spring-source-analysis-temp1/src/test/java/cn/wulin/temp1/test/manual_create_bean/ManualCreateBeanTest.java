package cn.wulin.temp1.test.manual_create_bean;

import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.manual_create_bean.ObtainBeanFactory;
import cn.wulin.temp1.domain.manual_create_bean.EmptyInterface;
import cn.wulin.temp1.domain.manual_create_bean.ManualClientApp;
import cn.wulin.temp1.domain.manual_create_bean.ManualOperationSpringBean;
import cn.wulin.temp1.domain.manual_create_bean.ManualUser;

/**
 * 使用两种方式 实现让spring 容器管理手动创建bean
 */
@SuppressWarnings({"resource","unused"})
public class ManualCreateBeanTest {
	
	/**
	 * 通过FactoryBean的方式实现手动创建bean
	 */
	@Test
	public void manualCreateBeanByFactoryBeanTest(){
		ApplicationContext  context = new ClassPathXmlApplicationContext("manual_create_bean/spring-manual_create_bean.xml");
		
		ManualClientApp app = new ManualClientApp();
		app.setCreateDate(new Date());
		app.setName("poseidon");
		
		ManualUser user = new ManualUser();
		user.setAge(21);
		user.setUsername("李四");
		user.setManualClientApp(app);
		
		ManualOperationSpringBean.setBeanToBeanFactory("manualClientApp", app);
		ManualOperationSpringBean.setBeanToBeanFactory("manualUser", user);
		
		Object bean = ObtainBeanFactory.beanFactory.getBean("manualClientApp");
		Map<String, ManualClientApp> beansOfType = ObtainBeanFactory.beanFactory.getBeansOfType(ManualClientApp.class);
		Map<String, EmptyInterface> beansOfType2 = ObtainBeanFactory.beanFactory.getBeansOfType(EmptyInterface.class);
		System.out.println();
		
	}

	/**
	 * 通过bean定义的方式实现手动创建bean
	 */
	@Test
	public void manualCreateBeanByBeanDefinitionTest() {
		
		ApplicationContext  context = new ClassPathXmlApplicationContext("manual_create_bean/spring-manual_create_bean.xml");
		System.out.println();
		
		//手动创建ManualClientApp的bean
		manualCreateClientAppBean();
		
		//手动创建ManualUser的bean
		manualCreateUser();
		
		System.out.println();
				
	}

	// 手动创建ManualUser的bean
	private void manualCreateUser() {
		if(ObtainBeanFactory.beanFactory.containsBean("manualUser")){
			Object bean = ObtainBeanFactory.beanFactory.getBean("manualUser");
			
			System.out.println();
		}
		
		// 创建用于承载属性的 AbstractBeanDefinition类型的GenericBeanDefinition
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(ManualUser.class);
		
		bd.getPropertyValues().add("username", "张三");
		bd.getPropertyValues().add("age", 12);
		//可以使用 bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE); 来代替
//		bd.getPropertyValues().add("autoRole", new RuntimeBeanReference("autoRole"));
//		bd.getPropertyValues().add("manualClientApp", new RuntimeBeanReference("manualClientApp"));
		bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
		
		ObtainBeanFactory.beanFactory.registerBeanDefinition("manualUser", bd);

		if(ObtainBeanFactory.beanFactory.containsBean("manualUser")){
			Object bean = ObtainBeanFactory.beanFactory.getBean("manualUser");
			
			System.out.println();
		}
	}
	
	/**
	 *  手动创建ManualClientApp的bean
	 */
	private void manualCreateClientAppBean(){
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(ManualClientApp.class);
		
		bd.getPropertyValues().add("name", "cas");
		bd.getPropertyValues().add("createDate", "2000-12-21");
		bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
		
		ObtainBeanFactory.beanFactory.registerBeanDefinition("manualClientApp", bd);
	}
}
