package cn.wulin.temp1.domain.manual_init_bean;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 这是一个普通的bean,自己用new 关键字创建的对象,但menu属性的值得有spring容器自动注入
 * @author ThinkPad
 *
 */
public class Role implements InitializingBean{
	
	private Long id;
	
	private String roleName;
	
	@Autowired
	private Menu menu;
	
	public Role(Long id, String roleName) {
		super();
		this.id = id;
		this.roleName = roleName;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("role afterProperitesSet");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

}
