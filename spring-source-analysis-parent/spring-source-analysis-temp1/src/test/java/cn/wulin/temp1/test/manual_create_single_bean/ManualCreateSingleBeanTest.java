package cn.wulin.temp1.test.manual_create_single_bean;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.manual_create_single_bean.SingleBeanFactory;
import cn.wulin.temp1.domain.manual_create_single_bean.SingleRole;
import cn.wulin.temp1.domain.manual_create_single_bean.SingleUser;

/**
 * 使用两种方式 实现让spring 容器管理手动创建bean
 */
@SuppressWarnings({"resource","unused"})
public class ManualCreateSingleBeanTest {
	
	/**
	 * 通过FactoryBean的方式实现手动创建bean
	 */
	@Test
	public void manualCreateSingleBeanTest(){
		ApplicationContext  context = new ClassPathXmlApplicationContext("manual_create_single_bean/spring-manual_create_single_bean.xml");
		
		SingleBeanFactory bean = context.getBean(SingleBeanFactory.class);
		DefaultListableBeanFactory beanFactory = bean.getBeanFactory();
		
		// 创建SingleRole 对象
		SingleRole role = new SingleRole(1L, "roleName");
		
		// 构建SingleUser的bean定义
		String beanName = SingleUser.class.getName();
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(SingleUser.class);
		bd.getConstructorArgumentValues().addIndexedArgumentValue(0, 55L);
		bd.getConstructorArgumentValues().addIndexedArgumentValue(1, role);
		// 其他属性,若果该属性是一个bean,则可以用该bean类型进行自动注入
		bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		
		//注册bean定义
		beanFactory.registerBeanDefinition(beanName, bd);
		
		//获取bean,其实就实例化bean
		Object bean2 = beanFactory.getBean(beanName);
		
		System.out.println(bean2);
		
	}

	
}
