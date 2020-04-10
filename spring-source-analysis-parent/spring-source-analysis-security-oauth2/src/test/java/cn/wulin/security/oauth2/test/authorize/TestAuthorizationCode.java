package cn.wulin.security.oauth2.test.authorize;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.wulin.security.oauth2.test.domain.TestToken;
import cn.wulin.security.oauth2.test.util.GsonUtil;
import cn.wulin.security.oauth2.test.util.HttpClientUtil;
import cn.wulin.security.oauth2.test.util.OauthUtil;

/**
 * app的方式实现授权码模式获取令牌,具体流程可以借助用浏览器测试
 * <p> 场景梳理
 * <p> a.com 和 b.com 分别是两个不同的应用,用户在a.com上注册了账户,账户名为userA,用户在b.com上注册了账户,账户名为userB,
 * 现在用户希望在a.com应用中访问b.com应用中自己的数据
 * @author wulin
 *
 */
public class TestAuthorizationCode {
	private static final String LOGIN_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/oauth/token";
	private static final String OAUTHORIZE_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/oauth/authorize";
	private static final String CLIENT_URL = "http://localhost:8081/spring-source-analysis-security-oauth2/userA/responseCode";
	
	@Test
	public void authorizationCodeTest() {
		//首先要求用户先登录B应用,这是在B应用app的内部完成的
		TestToken token = loginUserB();
		
		requestOauthorizeCode(token.getAccess_token());
		System.out.println();
		
		
		
	}
	
	/**
	 * a.com应用想b.com应用发起请求获取授权码的请求
	 */
	private void requestOauthorizeCode(String token) {
		Map<String,String> params = new HashMap<String,String>();
		params.put("response_type", "code");
		params.put("redirect_uri", CLIENT_URL);
		params.put("client_id", "test");
		params.put("client_secret", "test");
		params.put("scope", "all");
		
		Map<String,String> header = new HashMap<String,String>();
		header.put("Authorization", "bearer "+token);
		
		String result = HttpClientUtil.get(OAUTHORIZE_URL, params, header);
		System.out.println(result);
	}
	
	/**
	 * 首先要求用户先登录B应用,这是在B应用app的内部完成的
	 * <p> 若是手机端可用使用密码模式登录,
	 * <p> 若是浏览器端则正常使用浏览器登录即可,浏览器端的登录地址 http://localhost:8081/spring-source-analysis-security-oauth2/login
	 */
	private TestToken loginUserB() {
		Map<String,String> params = new HashMap<String,String>();
		params.put("username", "user");
		params.put("password", "password");
		params.put("grant_type", "password");
		params.put("client_id", "test");
		
		String authorizationBase64 = OauthUtil.getAuthorizationBase64();
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", authorizationBase64);
		
		String post = HttpClientUtil.post(LOGIN_URL, params, headers);
		TestToken returnTypeEntity = GsonUtil.getReturnTypeEntity(post, TestToken.class);
		
		
		return returnTypeEntity;
	}
}
