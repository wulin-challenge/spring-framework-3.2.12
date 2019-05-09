package cn.wulin.temp1.test.other.object_key;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * 测试对象key
 * @author wubo
 *
 */
public class TestObjectKey {
	
	@Test
	public void testObjectKey() throws NoSuchMethodException, SecurityException{
		
		TestObjectKeyClass testObjectKeyClass = new TestObjectKeyClass();
		
		Method method = testObjectKeyClass.getClass().getMethod("hello", String.class);
		
		ObjectKey ObjectKey = new ObjectKey(method,TestObjectKeyClass.class);
		
		ObjectKey ObjectKey2 = new ObjectKey(method,TestObjectKeyClass.class);
		
		if(ObjectKey.equals(ObjectKey2)){
			System.out.println(ObjectKey);
		}
		
		System.out.println(ObjectKey);
		System.out.println(ObjectKey2);
	}

}
