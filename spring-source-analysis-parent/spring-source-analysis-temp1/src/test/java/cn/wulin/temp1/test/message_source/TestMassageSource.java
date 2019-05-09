package cn.wulin.temp1.test.message_source;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;

/**
 * 切记:basename是classpash下的路径
 * @author ThinkPad
 *
 */
public class TestMassageSource {
	
	@Test
	public void testMassageSource(){
		MessageSourceAccessor accessor = null;
		ApplicationContext context = new ClassPathXmlApplicationContext("message_source/message_source.xml");
		
		MessageSource  messageSource = (MessageSource) context.getBean("messageSource");
		accessor = new MessageSourceAccessor(messageSource);
		
		Object[] params = new Object[]{"wulin",new GregorianCalendar().getTime()}; 
//		LocaleResolver
		String chinese = accessor.getMessage("test", params, Locale.CHINESE);
		
//		String chinese = context.getMessage("test", params, Locale.CHINESE);
		String us = context.getMessage("test", params, Locale.US);
		System.out.println(context);
	}
	
	@Test
	public void nativeTestMessageSource(){
		String basenames = "message_source/sources/messages";
		ResourceBundle bundle = ResourceBundle.getBundle(basenames, Locale.CHINESE);
		String test = bundle.getString("test");
		System.out.println(bundle);
	}

}
