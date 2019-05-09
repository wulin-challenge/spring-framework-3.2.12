package cn.wulin.temp1.domain.manual_create_bean;

import java.util.Date;

/**
 * spring 容器自动创建的bean
 *
 */
public class AutoRole {
	
	private String name;
	private Date createDate;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}
