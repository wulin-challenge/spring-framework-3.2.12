package cn.wulin.temp1.domain.manual_create_single_bean;

/**
 * 手动创建单个bean,该bean还依赖于SingleRole
 * @author wulin
 *
 */
public class SingleUser {
	private Long id;
	
	private SingleRole role;
	
	
	public SingleUser() {
	}

	public SingleUser(Long id, SingleRole role) {
		super();
		this.id = id;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SingleRole getRole() {
		return role;
	}

	public void setRole(SingleRole role) {
		this.role = role;
	}
	
	

}
