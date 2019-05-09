package cn.wulin.temp1.test.conversion_service;

import java.sql.Date;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;

import cn.wulin.temp1.domain.conversion_service.String2DateConverter;

public class TestConversionService {
	
	@Test
	public void testConversionService(){
		ApplicationContext context = new ClassPathXmlApplicationContext("conversion_service/conversion_service.xml");
		
		Object person = context.getBean("person");
		System.out.println(person);
		System.out.println(context);
	}
	
	@Test
	public void testString2DateConvert(){
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new String2DateConverter());
		
		Date convert = conversionService.convert("2018-12-02", Date.class);
		System.out.println(convert);
	}

}
