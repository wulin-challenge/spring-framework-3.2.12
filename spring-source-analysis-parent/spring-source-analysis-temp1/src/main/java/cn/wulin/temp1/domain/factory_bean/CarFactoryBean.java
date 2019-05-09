package cn.wulin.temp1.domain.factory_bean;

import org.springframework.beans.factory.FactoryBean;

public class CarFactoryBean implements FactoryBean<Car>{
	
	//车的基本属性信息
	private String carInfo;

	@Override
	public Car getObject() throws Exception {
		String[] split = carInfo.split(",");
		Car car = new Car();
		car.setBrand(split[0]);
		car.setMaxSpeed(Integer.parseInt(split[1]));
		car.setPrice(Double.parseDouble(split[2]));
		return car;
	}

	@Override
	public Class<?> getObjectType() {
		return Car.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getCarInfo() {
		return carInfo;
	}

	public void setCarInfo(String carInfo) {
		this.carInfo = carInfo;
	}
}
