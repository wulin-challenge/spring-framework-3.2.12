package cn.wulin.security.oauth2.test.domain;

/**
 * 测试用的token
 * @author wulin
 *
 */
public class TestToken {
//	{access_token=59fb11d9-2ce0-40ee-8bd3-13fd14d3bce1, scope=all, token_type=bearer, expires_in=43100.0}
	/**
	 * 访问token
	 */
	private String access_token;
	
	/**
	 * token的作用范围
	 */
	private String scope;
	
	/**
	 * token类型,一般为 bearer
	 */
	private String token_type;
	
	/**
	 * token的过期时间
	 */
	private Long expires_in;

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public Long getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(Long expires_in) {
		this.expires_in = expires_in;
	}
}
