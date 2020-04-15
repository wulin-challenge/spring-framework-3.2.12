package cn.wulin.security.oauth2.token.authentication;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * 自定义token的认证方式token
 * @author wulin
 *
 */
public class CustomTokenUsernamePasswordAuthenticationToken extends AbstractAuthenticationToken{
	private static final long serialVersionUID = 1L;
    //~ Instance fields ================================================================================================

    private final Object principal;
    private Object credentials;

	
	/**
	 * 这是具体的token值
	 */
	private String token;
	
	/**
	 * token是否过期
	 */
	private Boolean isExpire = false;
	
	/**
	 * 过期原因
	 */
	private String expireInfo;
	
	/**
	 * token的过期时间,单位秒,默认半小时
	 */
	private int expireSecond;
	
	/**
	 * token过期刷新时间,单位为秒,当 ((当前时间-expireFreshTime)>expireSecond/2)则刷新expireFreshTime时间
	 */
	private long expireFreshTime =  System.currentTimeMillis();
	
	/**
	 * token创建时间
	 */
	private long createTime = System.currentTimeMillis();

	public CustomTokenUsernamePasswordAuthenticationToken(Object principal, Object credentials) {
		 super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
	}

	public CustomTokenUsernamePasswordAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities) {
		 super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true); // must use super, as we override
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getIsExpire() {
		return isExpire;
	}

	public void setIsExpire(Boolean isExpire) {
		this.isExpire = isExpire;
	}

	public String getExpireInfo() {
		return expireInfo;
	}

	public void setExpireInfo(String expireInfo) {
		this.expireInfo = expireInfo;
	}

	public int getExpireSecond() {
		return expireSecond;
	}

	public void setExpireSecond(int expireSecond) {
		this.expireSecond = expireSecond;
	}

	public long getExpireFreshTime() {
		return expireFreshTime;
	}

	public void setExpireFreshTime(long expireFreshTime) {
		this.expireFreshTime = expireFreshTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	

    //~ Methods ========================================================================================================

    public Object getCredentials() {
        return this.credentials;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Once created you cannot set this token to authenticated. Create a new instance using the constructor which takes a GrantedAuthority list will mark this as authenticated.");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        credentials = null;
    }
}
