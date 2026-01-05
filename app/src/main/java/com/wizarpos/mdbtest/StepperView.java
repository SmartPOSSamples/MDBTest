package com.wizarpos.mdbtest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StepperView extends LinearLayout {

	private int currentValue = 1;
	private TextView tvValue;
	private Button btnIncrease, btnDecrease;
	private OnValueChangeListener listener;

	private int maxValue = 9;
	private int minValue = 0;

	public StepperView(Context context) {
		super(context);
		init(context);
	}

	public StepperView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public StepperView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		try {
			LayoutInflater.from(context).inflate(R.layout.view_stepper, this, true);
			tvValue = findViewById(R.id.tv_value);
			btnIncrease = findViewById(R.id.btn_increase);
			btnDecrease = findViewById(R.id.btn_decrease);

			if (tvValue == null || btnIncrease == null || btnDecrease == null) {
				throw new RuntimeException("Missing required views in view_stepper.xml");
			}

			btnIncrease.setOnClickListener(v -> updateValue(true));
			btnDecrease.setOnClickListener(v -> updateValue(false));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error initializing StepperView: " + e.getMessage());
		}
	}

	private void updateValue(boolean isIncrease) {
		if(isIncrease){
			if(currentValue + 1 <= maxValue) {
				tvValue.setText(String.valueOf(++currentValue));
			}
		} else {
			if(currentValue - 1 >= minValue) {
				tvValue.setText(String.valueOf(--currentValue));
			}
		}
		if (listener != null) {
			listener.onValueChanged(currentValue);
		}
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public TextView getTvValue() {
		return tvValue;
	}

	public void setTvValue(TextView tvValue) {
		this.tvValue = tvValue;
	}

	public int getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	public void setOnValueChangeListener(OnValueChangeListener listener) {
		this.listener = listener;
	}

	public interface OnValueChangeListener {
		void onValueChanged(int newValue);
	}

}
