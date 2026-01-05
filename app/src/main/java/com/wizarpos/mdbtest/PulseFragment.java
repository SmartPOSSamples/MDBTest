package com.wizarpos.mdbtest;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wizarpos.util.MDBUtils;

//duration : 10<= value <=200, default: 200
//interval: 0< value, default: 300
//frequency: 0< value, default: 1
public class PulseFragment extends Fragment implements View.OnClickListener {

	private final String TAG = "PulseFragment";

	public Button btnTriggerPulse, btnSetPulse;
	public EditText etDuration, etFrequency, etLatency;
	public Spinner spnVoltage;

	private String balance, frequency, latency, duration;

	private String spnItemVoltage;

	private DataSendListener dataSendListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pulse, container, false);
		btnTriggerPulse = view.findViewById(R.id.btn_trigger_pulse);
		btnSetPulse = view.findViewById(R.id.btn_set_pulse);

		btnTriggerPulse.setOnClickListener(this);
		btnSetPulse.setOnClickListener(this);

		etDuration = view.findViewById(R.id.et_duration);
		etFrequency = view.findViewById(R.id.et_frequency);
		etLatency = view.findViewById(R.id.et_latency);

		spnVoltage = view.findViewById(R.id.spn_voltage);

		ArrayAdapter<CharSequence> adapterVoltage = ArrayAdapter.createFromResource(this.getContext(),
				R.array.spn_voltage, android.R.layout.simple_spinner_item);
		adapterVoltage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnVoltage.setAdapter(adapterVoltage);
		spnVoltage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				spnItemVoltage = spnVoltage.getSelectedItem().toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			dataSendListener = (DataSendListener) context;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_trigger_pulse:
				dataSendListener.onIntValueSent(MDBUtils.TYPE_PULSE_INTERVAL,getTriggerPulseLatency());
				dataSendListener.onIntValueSent(MDBUtils.TYPE_PULSE_FREQUENCY,getTriggerPulseFrequency());
				dataSendListener.onIntValueSent(MDBUtils.TYPE_PULSE_DURATION,getPulseDuration());
				dataSendListener.onIntValueSent(MDBUtils.TYPE_PULSE_VOLTAGE,getPulseVoltage());
				dataSendListener.onTriggerPulseClicked();
				break;
			case R.id.btn_set_pulse:
				dataSendListener.onIntValueSent(MDBUtils.TYPE_PULSE_DURATION,getPulseDuration());
				dataSendListener.onIntValueSent(MDBUtils.TYPE_PULSE_VOLTAGE,getPulseVoltage());
				dataSendListener.onSetPulseClicked();
				break;
			default:
				break;
		}
	}

	//default: 1
	public int getTriggerPulseFrequency() {
		int ret = 0;
		String sContent = etFrequency.getText().toString();
		if (sContent.trim().isEmpty()) {
			dataSendListener.onLogSent('d', TAG, "frequency is empty, will be set to default: 1");
			return 1;
		}
		try {
			ret = Integer.parseInt(sContent.trim());
		} catch (Exception e) {
			dataSendListener.onLogSent('e', TAG, "frequency must be an integer");
			return ret;
		}
		if (ret <= 0) {
			dataSendListener.onLogSent('e', TAG, "frequency must be greater than 0");
		}
		return ret;
	}

	//default: 300
	public int getTriggerPulseLatency(){
		int ret = 0;
		String sContent = etLatency.getText().toString();
		if(sContent.trim().isEmpty()){
			dataSendListener.onLogSent('d', TAG, "interval is empty, will be set to default: 300");
			return 300;
		}
		try{
			ret = Integer.parseInt(sContent.trim());
		}catch(Exception e){
			dataSendListener.onLogSent('e', TAG, "interval must be a number");
			return ret;
		}
		if(ret <= 0) {
			dataSendListener.onLogSent('e', TAG, "interval must be greater than 0");
			return ret;
		}
		return ret;
	}

	//default: 200
	public int getPulseDuration(){
		int ret = -1;
		String sContent = etDuration.getText().toString();
		if(sContent.trim().isEmpty()){
			dataSendListener.onLogSent('d', TAG, "duration is empty, will be set to default: 200");
			return 200;
		}
		try{
			ret = Integer.parseInt(sContent.trim());
		}catch(Exception e){
			dataSendListener.onLogSent('e', TAG, "duration value must be a number");
			return ret;
		}
		if(ret < 10 || ret > 1000){
			dataSendListener.onLogSent('e', TAG, "error: duration value range: 10~1000, will be set to default: 200");
			return 200;
		}
		return ret;
	}

	public int getPulseVoltage(){
		return Integer.parseInt(spnItemVoltage);
	}

}
