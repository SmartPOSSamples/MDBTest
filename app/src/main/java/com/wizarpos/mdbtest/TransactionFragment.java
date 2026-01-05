package com.wizarpos.mdbtest;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wizarpos.util.ByteConvertStringUtil;
import com.wizarpos.util.MDBUtils;
import com.wizarpos.util.PreferenceHelper;

import java.math.BigDecimal;

public class TransactionFragment extends Fragment implements View.OnClickListener {

	private Context context;
	private DataSendListener dataSendListener;
	private final String TAG = "MDBTest Transaction";
	private Button btnSetBalance;
	private EditText etBalance;
	private CheckBox cbAlwaysIdle, cb32BitMonetary, cbNegativeVend, cbDefaultActive;
	private Spinner spnMdbLevel, spnDeviceType;
	private String spnItemMdbLevel, spnItemDeviceType;
	private StepperView svX, svY;
	private int selectedSpnPosMdbLevel = -1;
	private int selectedSpnPosDevType = -1;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_transaction, container, false);
		btnSetBalance = view.findViewById(R.id.btn_set_balance);

		btnSetBalance.setOnClickListener(this);
		etBalance = view.findViewById(R.id.et_balance);
		spnMdbLevel = view.findViewById(R.id.spn_mdb_level);
		spnDeviceType = view.findViewById(R.id.spn_device_type);
		cbAlwaysIdle = view.findViewById(R.id.cb_always_idle);
		cb32BitMonetary = view.findViewById(R.id.cb_32bit_monetary);
		cbNegativeVend = view.findViewById(R.id.cb_negative_vend);
		cbDefaultActive = view.findViewById(R.id.cb_default_active);

		svX = view.findViewById(R.id.sv_balanceX);
		svX.setMaxValue(9);
		svX.setMinValue(1);
		svY = view.findViewById(R.id.sv_balanceY);
		svY.setMaxValue(9);
		svY.setMinValue(0);
		svY.getTvValue().setText("0");
		svY.setCurrentValue(0);

		cbAlwaysIdle.setChecked(false);
		cb32BitMonetary.setChecked(false);
		cbNegativeVend.setChecked(false);
		cbDefaultActive.setChecked(true);

		context = getContext();

		ArrayAdapter<CharSequence> adapterMdbLevel = ArrayAdapter.createFromResource(context,
				R.array.spn_mdb_level, android.R.layout.simple_spinner_item);
		adapterMdbLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnMdbLevel.setAdapter(adapterMdbLevel);
//		if(savedInstanceState != null){
//			selectedSpnPosMdbLevel = savedInstanceState.getInt("selectedSpnPositonLevel", -1);
//		}
		selectedSpnPosMdbLevel = PreferenceHelper.getInstance(context).getIntValue("selectedSpnPositonLevel");
		if(selectedSpnPosMdbLevel != -1){
			spnMdbLevel.setSelection(selectedSpnPosMdbLevel);
		}
		spnItemMdbLevel = spnMdbLevel.getSelectedItem().toString();
		int nMdbLevel = Integer.parseInt(spnItemMdbLevel.substring(spnItemMdbLevel.length() - 1));
		dataSendListener.onIntValueSent(MDBUtils.TYPE_SPN_MDB_LEVEL, nMdbLevel);
		if(nMdbLevel == 3){
			cbAlwaysIdle.setChecked(true);
		}
		spnMdbLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				selectedSpnPosMdbLevel = i;
				PreferenceHelper.getInstance(context).setIntValue("selectedSpnPositonLevel", selectedSpnPosMdbLevel);
				spnItemMdbLevel = spnMdbLevel.getSelectedItem().toString();
				int nMdbLevel = Integer.parseInt(spnItemMdbLevel.substring(spnItemMdbLevel.length() - 1));
				dataSendListener.onIntValueSent(MDBUtils.TYPE_SPN_MDB_LEVEL, nMdbLevel);
				if(nMdbLevel == 3){
					cbAlwaysIdle.setChecked(true);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}

		});

		ArrayAdapter<CharSequence> adapterDeviceType = ArrayAdapter.createFromResource(context,
				R.array.spn_device_type, android.R.layout.simple_spinner_item);
		adapterDeviceType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDeviceType.setAdapter(adapterDeviceType);
		if(savedInstanceState != null){
			selectedSpnPosDevType = savedInstanceState.getInt("selectedSpnPositonType", -1);
		}
		if(selectedSpnPosDevType != -1){
			spnDeviceType.setSelection(selectedSpnPosDevType);
		}
		spnDeviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				selectedSpnPosDevType = i;
				spnItemDeviceType = spnDeviceType.getSelectedItem().toString();
				int nMdbLevel = Integer.parseInt(spnItemDeviceType.substring(spnItemDeviceType.length() - 1));
				dataSendListener.onIntValueSent(MDBUtils.TYPE_SPN_DEVICE_TYPE, nMdbLevel);
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}

		});
		cbAlwaysIdle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dataSendListener.onBooleanValueSent(MDBUtils.TYPE_CB_CHECK_ALWAYS_IDLE, isChecked);
			}
		});
		cb32BitMonetary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dataSendListener.onBooleanValueSent(MDBUtils.TYPE_CB_CHECK_32BIT_MONETARY, isChecked);
			}
		});
		cbNegativeVend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dataSendListener.onBooleanValueSent(MDBUtils.TYPE_CB_CHECK_NEGATIVE_VEND, isChecked);
			}
		});
		cbDefaultActive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataSendListener.onBooleanValueSent(MDBUtils.TYPE_CB_CHECK_DEFAULT_ACTIVE, cbDefaultActive.isChecked());
			}
		});
		etBalance.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE){
					clearFocusAndHideKeyboard();
				}
				return false;
			}
		});
		etBalance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){

				} else {
					BigDecimal bdBalance = getBalance();
					dataSendListener.onBigDecimalValueSent(MDBUtils.TYPE_BALANCE, bdBalance);
				}
			}
		});

		svX.setOnValueChangeListener(new StepperView.OnValueChangeListener() {
			@Override
			public void onValueChanged(int value) {
				dataSendListener.onIntValueSent(MDBUtils.TYPE_X, value);
			}
		});

		svY.setOnValueChangeListener(new StepperView.OnValueChangeListener() {
			@Override
			public void onValueChanged(int value) {
				dataSendListener.onIntValueSent(MDBUtils.TYPE_Y, value);
			}
		});

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View rootView = getView();
		rootView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					clearFocusAndHideKeyboard();
				}
				return false;
			}
		});
		dataSendListener.onTransactionFragmentViewCreated(cbDefaultActive);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if(hidden){
			BigDecimal bdBalance = getBalance();
			dataSendListener.onBigDecimalValueSent(MDBUtils.TYPE_BALANCE, bdBalance);
			dataSendListener.onLogSent('d', TAG, "Scale Factor: " + svX.getTvValue().getText().toString() +
					", Decimal Places: " + svY.getTvValue().getText().toString());
		}
		if(!hidden) {
			dataSendListener.onTransactionFragmentViewCreated(cbDefaultActive);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("selectedSpnPositonLevel", selectedSpnPosMdbLevel);
		outState.putInt("selectedSpnPositonType", selectedSpnPosDevType);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.btn_set_balance:
				dataSendListener.onBigDecimalValueSent(MDBUtils.TYPE_BALANCE, getBalance());
				dataSendListener.onLogSent('d', TAG, "set balance succeed");
//				dataSendListener.onSetBalanceClicked();
				break;
			default:
				break;
		}

	}

	//default: 100
	public BigDecimal getBalance(){
		String sContent = etBalance.getText().toString();
		if(sContent.trim().isEmpty()){
			dataSendListener.onLogSent('d', TAG, "balance is empty, will be set to default: 100");
			return new BigDecimal("100");
		}
		if(ByteConvertStringUtil.isValidBigDecimalFormat(sContent)){
			BigDecimal ret = new BigDecimal(sContent);
			if(ret.compareTo(BigDecimal.ZERO) < 0){
				dataSendListener.onLogSent('e', TAG, "balance should be greater than 0, will be set to default: 100");
				return new BigDecimal("100");
			} else {
				return ret;
			}
		} else {
			dataSendListener.onLogSent('e', TAG, "balance is illegal, will be set to default: 100");
			return new BigDecimal("100");
		}
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

	private void clearFocusAndHideKeyboard(){
		View focusView = requireActivity().getCurrentFocus();
		if(focusView instanceof EditText) {
			focusView.clearFocus();

			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
