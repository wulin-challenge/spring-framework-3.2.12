package cn.wulin.temp1.domain.manual_create_aop_advisor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 同的通知拦截器,并实现拦截注解具有继承性
 * @author ThinkPad
 * @see 参考: org.springframework.transaction.interceptor.TransactionInterceptor
 *
 */
public class MyAdviceInterceptor implements MethodInterceptor{

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		Class<?> clazz = method.getDeclaringClass();
		Class<?> superclass = clazz.getSuperclass();
		
		MyAdvice myAdvice = getMyAdvice(method, clazz, superclass);
		
		System.out.println("before : "+myAdvice.value());
		Object proceed = invocation.proceed();
		System.out.println("after : "+myAdvice.value());
		return proceed;
	}
	
	private MyAdvice getMyAdvice(Method method, Class<?> currentClass,Class<?> parentClass) {
		
		MyAdvice myAdvice = method.getDeclaredAnnotation(MyAdvice.class);
		if(myAdvice != null) {
			return myAdvice;
		}
		
		if(parentClass == null) {
			return null;
		}
		
		try {
			Method parentMethon = parentClass.getMethod(method.getName(), method.getParameterTypes());
			if(parentMethon == null) {
				return null;
			}
			return getMyAdvice(parentMethon,parentClass,parentMethon.getDeclaringClass().getSuperclass());
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}


}
