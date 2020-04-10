package cn.wulin.temp1.domain.manual_init_bean;

/**
 * 这只是一个普通的bean,但要让spring 容器来管理
 * @author wulin
 *
 */
public class Menu {
	
	private Long id;
	private String name;
	
	private String code;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
