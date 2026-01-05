package com.wizarpos.mdbtest;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment implements View.OnClickListener{

	private Button 	openBtn, closeBtn, btnGetVersion, btnFactoryMode,
				   	btnTest, btnGetHardwareVersion, btnDiagnoseHardware,
					btnMdbStart, btnMdbStop;
	private Context context;

	private DataSendListener dataSendListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		openBtn = (Button) view.findViewById(R.id.open);
		closeBtn = (Button) view.findViewById(R.id.close);
		btnGetVersion = (Button) view.findViewById(R.id.btn_get_version);
		btnFactoryMode = (Button) view.findViewById(R.id.btn_factory_mode);
		btnMdbStart = (Button) view.findViewById(R.id.btn_mdb_start);
		btnMdbStop = (Button) view.findViewById(R.id.btn_mdb_stop);
		btnTest = view.findViewById(R.id.btn_test);
		btnGetHardwareVersion = view.findViewById(R.id.btn_get_hardware_version);
		btnDiagnoseHardware = view.findViewById(R.id.btn_diagnose_hardware);

		openBtn.setOnClickListener(this);
		closeBtn.setOnClickListener(this);
		btnGetVersion.setOnClickListener(this);
		btnFactoryMode.setOnClickListener(this);
		btnTest.setOnClickListener(this);
		btnGetHardwareVersion.setOnClickListener(this);
		btnDiagnoseHardware.setOnClickListener(this);
		btnMdbStart.setOnClickListener(this);
		btnMdbStop.setOnClickListener(this);

		btnMdbStop.setEnabled(false);

		context = getContext();

		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try{
			dataSendListener = (DataSendListener) context;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.open:
				dataSendListener.onOpenClicked();
				break;
			case R.id.close:
				dataSendListener.onCloseClicked();
				break;
			case R.id.btn_get_version:
				dataSendListener.onGetVersionClicked();
				break;
			case R.id.btn_factory_mode:
				dataSendListener.onFactoryModeClicked();
				break;
			case R.id.btn_test:
				dataSendListener.onTestClicked();
				break;
			case R.id.btn_get_hardware_version:
				dataSendListener.onGetHardwareVersionClicked();
				break;
			case R.id.btn_diagnose_hardware:
				dataSendListener.onDiagnoseHardwareClicked();
				break;
			case R.id.btn_mdb_start:
				dataSendListener.onMdbStartClicked();
				break;
			case R.id.btn_mdb_stop:
				dataSendListener.onMdbStopClicked();
				break;
		}
	}
}
