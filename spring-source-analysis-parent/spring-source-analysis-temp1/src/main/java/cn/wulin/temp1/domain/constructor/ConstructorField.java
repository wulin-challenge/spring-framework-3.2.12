package cn.wulin.temp1.domain.constructor;

public class ConstructorField {

	private String clazzName;

//	public ConstructorField(){}
	public ConstructorField(Class<?> clazz) {
		super();
		this.clazzName = clazz.getSimpleName();
	}

	public String getClazzName() {
		return clazzName;
	}
}
