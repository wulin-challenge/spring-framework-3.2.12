package cn.wulin.temp1.domain.factory_bean;

public class Car {

	/**
	 * 品牌
	 */
	private String brand;
	
	/**
	 * 最大速度
	 */
	private int maxSpeed; 
	
	/**
	 * 价格
	 */
	private double price;

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
