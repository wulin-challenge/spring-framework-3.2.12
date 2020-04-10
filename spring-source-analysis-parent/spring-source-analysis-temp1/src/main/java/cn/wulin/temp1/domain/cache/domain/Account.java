package cn.wulin.temp1.domain.cache.domain;

public class Account {
	
	private String username;
	
	private Long balance;
	
	public Account() {}
	public Account(String username, Long balance) {
		super();
		this.username = username;
		this.balance = balance;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}
	@Override
	public String toString() {
		return "{username:"+username+",balance:"+balance+"}";
	}
	
}
