package cn.wulin.temp1.domain.manual_create_single_bean;

public class SingleRole {

	private Long id;
	private String name;
	
	public SingleRole() {
	}
	public SingleRole(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
