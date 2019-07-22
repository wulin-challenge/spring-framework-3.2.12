package cn.wulin.temp1.test.manual_create_bean_and_aop;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.manual_create_bean.EmptyInterface;
import cn.wulin.temp1.domain.manual_create_bean.ManualClientApp;
import cn.wulin.temp1.domain.manual_create_bean.ManualUser;
import cn.wulin.temp1.domain.manual_create_bean.ObtainBeanFactory;
import cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_bean.ManualAopObtainBeanFactory;
import cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_bean.ManualAopOperationSpringBean;
import cn.wulin.temp1.domain.manual_create_bean_and_aop.service.ManualAopUserService;

/**
 * 手动创建bean和手动添加aop支持
 */
@SuppressWarnings({"resource","unused"})
public class ManualAopTest {
	
	/**
	 * 通过FactoryBean的方式实现手动创建bean
	 */
	@Test
	public void manualAopAndManualBeanTest(){
		ApplicationContext  context = new ClassPathXmlApplicationContext("manual_create_bean_and_aop/spring-manual_create_bean_and_aop.xml");
		
		ManualAopUserService userService = new ManualAopUserService();
		
		
		ManualAopOperationSpringBean.setBeanToBeanFactory("manualUserService", userService);
		
		ManualAopUserService bean = (ManualAopUserService) ManualAopObtainBeanFactory.beanFactory.getBean("manualUserService");
		Map<String, ManualAopUserService> beansOfType = ManualAopObtainBeanFactory.beanFactory.getBeansOfType(ManualAopUserService.class);
		
		String saveUser = bean.saveUser("wl");
		System.out.println(saveUser);
		
	}

}
