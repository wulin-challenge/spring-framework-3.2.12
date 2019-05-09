package cn.wulin.temp1.test.factory_bean;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.temp1.domain.factory_bean.Car;
import cn.wulin.temp1.domain.factory_bean.CarFactoryBean;

@SuppressWarnings("deprecation")
public class CarTest {

	@Test
	public void generalBeanTestCar() {
		
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("factory_bean/spring_car.xml"));
		Car car = (Car) bf.getBean("car");
		System.out.println(car);
	}
	
	@Test
	public void factoryBeanTestCar() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("factory_bean/spring_factory_bean_car.xml"));
		Car car = (Car) bf.getBean("factory_bean_car");
		CarFactoryBean carFactoryBean = (CarFactoryBean) bf.getBean("&factory_bean_car");
		System.out.println(car);
	}
}
