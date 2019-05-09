package cn.wulin.temp1.test.custom_application;

import java.util.Properties;
import java.util.Set;

import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cn.wulin.temp1.domain.custom_application.AoDiCar;
import cn.wulin.temp1.domain.custom_application.MyClassPathXmlApplicationContext;

@SuppressWarnings("resource")
public class TestCustomApplication {
	
	/**
	 * AbstractApplicationContext -->registerBeanPostProcessors(beanFactory); -->603
	 */
	@Test
	public void testCustomApplication() {
		
		ApplicationContext bf=new MyClassPathXmlApplicationContext("custom_application/custom-application.xml");
		
		AoDiCar aoDiCar = (AoDiCar) bf.getBean("aoDiCar");
		
		System.out.println(aoDiCar);
		
		
	}
	
	private static class TempProperty extends PropertyPlaceholderConfigurer {

		@Override
		protected String parseStringValue(String strVal, Properties props, Set<?> visitedPlaceholders) {
			
			
			return super.parseStringValue(strVal, props, visitedPlaceholders);
		}
	}
	
	private static class TempAppliction implements ApplicationContextAware{

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//			applicationContext.
		}
		
	}

}
