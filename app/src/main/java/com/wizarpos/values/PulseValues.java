package com.wizarpos.values;

public class PulseValues {
	private int pulseFrequency;
	private int pulseInterval;
	private int pulseDuration;
	private int pulseVoltage;
	private int currentSend;

	public PulseValues() {
		this.pulseFrequency = 1;
		this.pulseInterval = 300;
		this.pulseDuration = 200;
		this.pulseVoltage = 0;
		this.currentSend = 0;
	}

	public int getPulseFrequency() {
		return pulseFrequency;
	}

	public void setPulseFrequency(int pulseFrequency) {
		this.pulseFrequency = pulseFrequency;
	}

	public int getPulseInterval() {
		return pulseInterval;
	}

	public void setPulseInterval(int pulseInterval) {
		this.pulseInterval = pulseInterval;
	}

	public int getPulseDuration() {
		return pulseDuration;
	}

	public void setPulseDuration(int pulseDuration) {
		this.pulseDuration = pulseDuration;
	}

	public int getPulseVoltage() {
		return pulseVoltage;
	}

	public void setPulseVoltage(int pulseVoltage) {
		this.pulseVoltage = pulseVoltage;
	}

	public int getCurrentSend() {
		return currentSend;
	}

	public void setCurrentSend(int currentSend) {
		this.currentSend = currentSend;
	}
}
