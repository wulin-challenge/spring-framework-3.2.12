package cn.wulin.temp1.domain.aop;

import org.springframework.aop.framework.AopContext;

public class TestAopBean {
	private String testStr = "testAopStr";

	public String getTestStr() {
		return testStr;
	}

	public void setTestStr(String testStr) {
		this.testStr = testStr;
	}
	
	public void test(){
		System.out.println("test");
	}
	
	public void test2(){
//		this.test();
		((TestAopBean)AopContext.currentProxy()).test();
		System.out.println("test2");
	}
}
