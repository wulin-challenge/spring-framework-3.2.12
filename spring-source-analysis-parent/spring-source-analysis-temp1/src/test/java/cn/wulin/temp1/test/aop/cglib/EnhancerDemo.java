package cn.wulin.temp1.test.aop.cglib;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class EnhancerDemo {
	
	public static void main(String[] args) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(EnhancerDemo.class);
		enhancer.setCallback(new MethodInterceptorImpl());
		
		EnhancerDemo demo = (EnhancerDemo)enhancer.create();
		demo.test();
	}
	
	public void test(){
		System.out.println("EnhancerDemo test()");
	}
	
	private static class MethodInterceptorImpl implements MethodInterceptor{

		@Override
		public Object intercept(Object obj, Method method, Object[] arg, MethodProxy proxy) throws Throwable {
			System.out.println("Before invoke "+method);
			Object result = proxy.invokeSuper(obj, arg);
			System.out.println("After invoke "+method);
			return result;
		}
		
	}

}
