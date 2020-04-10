package cn.wulin.security.oauth2.test.authorize;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.wulin.security.oauth2.test.util.GsonUtil;
import cn.wulin.security.oauth2.test.util.HttpClientUtil;
import cn.wulin.security.oauth2.test.util.OauthUtil;

public class TestApp {
	
	private static final String GET_USER_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/json/getUser";
	private static final String LOGIN_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/oauth/token";
	
	/**
	 * 通过密码模式得到token
	 */
	@Test
	public void obtainTokenOfPasswordLogin() {
		Map<String,String> params = new HashMap<String,String>();
		
		params.put("username", "user");
		params.put("password", "password");
		params.put("grant_type", "password");
		params.put("client_id", "test");
		
		String authorizationBase64 = OauthUtil.getAuthorizationBase64();
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", authorizationBase64);
		
		String post = HttpClientUtil.post(LOGIN_URL, params, headers);
		@SuppressWarnings("unchecked")
		HashMap<String,Object> returnTypeEntity = GsonUtil.getReturnTypeEntity(post, HashMap.class);
		System.out.println(returnTypeEntity);
	}

	/**
	 * 得到用户信息
	 */
	@Test
	public void getUserInfo() {
		
		Map<String,String> header = new HashMap<String,String>();
		header.put("Authorization", "bearer afdeb9d8-ebba-43d9-bce0-5e32745b57ce1");
		String result = HttpClientUtil.get(GET_USER_URL, null, header);
		System.out.println(result);
	}
}
