package cn.wulin.security.oauth2.test.token;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.wulin.security.oauth2.test.util.GsonUtil;
import cn.wulin.security.oauth2.test.util.HttpClientUtil;

/**
 * 测试app使用token的方式进行访问
 * @author wulin
 *
 */
public class TestAppToken {

	public static final String TOKEN_LOGIN_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/login";
	public static final String TOKEN_ROLEUSER_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/role/getRoleUser";
	
	@Test
	public void appTokenLoginTest() {
		getToken();
	}
	
	@Test
	public void getRoleUserTest() {
		String token = getToken();
		
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("request_type", "token");
		headers.put("token", token);
//		headers.put("token", "5f4bf159-53e9-48db-8d91-b0331a7333641");
		
		String post = HttpClientUtil.post(TOKEN_ROLEUSER_URL, null, headers);
		System.out.println(post);
	}
	
	public String getToken() {
		Map<String,String> params = new HashMap<String,String>();
		
		params.put("username", "user");
		params.put("password", "password");
		
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("request_type", "token");
		
		String post = HttpClientUtil.post(TOKEN_LOGIN_URL, params, headers);
		System.out.println(post);
		HashMap<?, ?> returnTypeEntity = GsonUtil.getReturnTypeEntity(post, HashMap.class);
		return (String) returnTypeEntity.get("token");
	}
	

	
}
