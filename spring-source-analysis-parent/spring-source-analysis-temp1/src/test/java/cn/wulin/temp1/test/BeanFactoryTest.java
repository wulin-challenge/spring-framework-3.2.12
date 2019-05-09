package cn.wulin.temp1.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.temp1.domain.MyTestBean;
import cn.wulin.temp1.domain.MyTestBean2;

@SuppressWarnings("deprecation")
public class BeanFactoryTest {
	
	/**
	 * 源码阅读进度: AbstractAutowireCapableBeanFactory ->invokeInitMethods(beanName, wrappedBean, mbd);
	 *  -> 1777
	 */
	
	@Test
	public void testSimpleLoad(){
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("spring-temp1-1.xml"));
		MyTestBean bean = (MyTestBean) bf.getBean("myTestBean");
		
		String testStr = bean.getTestStr();
		System.out.println(testStr);
		
		//测试是否读取 子标签中的 meta 标签的值,答案是否定的
		MyTestBean2 bean2 = (MyTestBean2) bf.getBean("myTestBean2");
		String testStr2 = bean2.getTestStr2();
		System.out.println(testStr2);
		
//		MyTestBean bean = MyTestBean.class.cast(bf.getBean("myTestBean"));
		assertEquals("testStr",bean.getTestStr());
		
	}

}
