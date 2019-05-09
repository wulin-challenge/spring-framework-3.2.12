package cn.wulin.temp1.domain.manual_create_bean;

/**
 * 手动创建spring bean对象
 *
 */
public class ManualUser implements EmptyInterface{
	
	private String username;
	private Integer age;
	
	//手动注入spring自动创建bean
	private AutoRole autoRole;
	
	//手动注入自己手动创建bean
	private ManualClientApp manualClientApp;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public AutoRole getAutoRole() {
		return autoRole;
	}
	public void setAutoRole(AutoRole autoRole) {
		this.autoRole = autoRole;
	}
	public ManualClientApp getManualClientApp() {
		return manualClientApp;
	}
	public void setManualClientApp(ManualClientApp manualClientApp) {
		this.manualClientApp = manualClientApp;
	}
}
