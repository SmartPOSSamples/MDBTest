package com.wizarpos.values;

import java.math.BigDecimal;

public class MDBValues {
	private int balance;
	private int currentVersion;
	private String firmwareName;
	private int mdbLevel;
	private int vmcLevel;
	private int deviceType;
	private boolean factoryMode;
	private BigDecimal actualPrice; // This is the actual price in BigDecimal format
	private int x;
	private int y;
	private OptionalFeature optionalFeature;
	private int defaultActiveStatus; // 1: inactive, 0: active

	public MDBValues() {
		this.balance = 100;
		this.currentVersion = 0;
		this.firmwareName = "";
		this.mdbLevel = 2;
		this.vmcLevel = 2;
		this.deviceType = 1;
		this.factoryMode = false;
		this.actualPrice = BigDecimal.valueOf(100); // Default actual price
		this.x = 1; // Default scale factor x
		this.y = 0; // Default decimal places y
		this.optionalFeature = new OptionalFeature();
		this.defaultActiveStatus = -1; // Default to inactive
	}

	public int getBalance() {
		return balance;
	}

	//金额计算公式 ActualPrice = P * X * 10 ^(-Y) here P is this.balance, ActualPrice(a) is the parameter balance
	// so, p = a * 10^y / x, p: this.balance, a: balance
	//MDB resp:0101020086"0101"00048f 这里x= 01的十进制1, y= 01的十进制1
	//0101021156"0102"590dd3
	public void setBalance(int balance) {
		this.balance = balance;
	}

	public BigDecimal getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(BigDecimal actualPrice) {
		this.actualPrice = actualPrice;
	}

	public int getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(int currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getFirmwareName() {
		return firmwareName;
	}

	public void setFirmwareName(String firmwareName) {
		this.firmwareName = firmwareName;
	}

	public int getMdbLevel() {
		return mdbLevel;
	}

	public void setMdbLevel(int mdbLevel) {
		this.mdbLevel = mdbLevel;
	}

	public int getVmcLevel() {
		return vmcLevel;
	}

	public void setVmcLevel(int vmcLevel) {
		this.vmcLevel = vmcLevel;
	}

	public boolean isFactoryMode() {
		return factoryMode;
	}

	public void setFactoryMode(boolean factoryMode) {
		this.factoryMode = factoryMode;
	}

	public OptionalFeature getOptionalFeature() {
		return optionalFeature;
	}

	public void setOptionalFeature(OptionalFeature mdbOptionalFeature) {
		this.optionalFeature = mdbOptionalFeature;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public int getDefaultActiveStatus() {
		return defaultActiveStatus;
	}

	public void setDefaultActiveStatus(int defaultActiveStatus) {
		this.defaultActiveStatus = defaultActiveStatus;
	}

	@Override
	public String toString() {
		return "MDBValues{" +
				"balance=" + balance +
				", currentVersion=" + currentVersion +
				", firmwareName='" + firmwareName + '\'' +
				", mdbLevel=" + mdbLevel +
				", vmcLevel=" + vmcLevel +
				", deviceType=" + deviceType +
				", factoryMode=" + factoryMode +
				", actualPrice=" + actualPrice +
				", x=" + x +
				", y=" + y +
				", optionalFeature=" + optionalFeature +
				", defaultActiveStatus=" + defaultActiveStatus +
				'}';
	}
}
