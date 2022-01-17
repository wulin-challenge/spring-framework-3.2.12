package cn.wulin.temp1.domain.manual_create_aop_advisor;

import java.lang.reflect.Method;

import org.springframework.aop.support.StaticMethodMatcherPointcut;

/**
 * 切点实现,并实现拦截注解具有继承性
 * @author wulin
 * @see org.springframework.transaction.interceptor.TransactionAttributeSourcePointcut
 */
public class MyAdviceAttributeSourcePointcut extends StaticMethodMatcherPointcut {

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		MyAdvice myAdvice = getMyAdvice(method, targetClass, method.getDeclaringClass().getSuperclass());
		//这里默认返回true
		boolean matches = getClassFilter().matches(targetClass);
		if(matches && myAdvice != null) {
			return true;
		}
		return false;
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
