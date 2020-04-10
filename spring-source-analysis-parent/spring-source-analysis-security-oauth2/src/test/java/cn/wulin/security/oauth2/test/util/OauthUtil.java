package cn.wulin.security.oauth2.test.util;

/**
 * Oauth的工具类
 * @author wulin
 *
 */
public class OauthUtil {
	
	/**
	 * 得到客户端身份认证的Basic的base64码
	 * @return
	 */
	public static String getAuthorizationBase64() {
		String authorization = "Basic ";
		String authorizationPrefix= "test:test"; //这是客户端Id和客户端秘钥
		try {
			authorization += Base64Util.encode(authorizationPrefix);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authorization;
	}

}
