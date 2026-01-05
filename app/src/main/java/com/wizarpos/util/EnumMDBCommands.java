package com.wizarpos.util;


import android.os.Build;
import android.util.Log;

import com.wizarpos.mdbtest.Item;
import com.wizarpos.values.MDBValues;
import com.wizarpos.values.PulseValues;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class EnumMDBCommands {

	public enum MDB_RESPONSE {
		RESET,
		SETUP_CONFIG,
		SETUP_PRICE,
		EXPANSION_REQUESTID,
//		ENABLE_READER,
		REVALUE_APPROVED,
		REVALUE_LIMIT_AMOUNT,
		VEND_SUCCESS,
		END_SESSION,
		VEND_APPROVED,
		VEND_DENIED,
		NEGATIVE_VEND_APPROVED,
		READER_CANCELLED,

		COMMON_ACK,
	}

	public enum MDB_REQUEST {
		VERSION_REQUEST,
		TRIGGER_PULSE,
		TRIGGER_PULSE_ONESHOT,
		HARDWARE_DIAGNOSE_TEST,
		HARDWARE_VERSION_REQUEST,

		ACTIVE_CASHLESS,
		SET_PARAM_PULSE_DURATION,
		SET_PARAM_PULSE_VOLTAGE,
		SET_PARAM_ENTER_FACTORY_MODE,
		SET_PARAM_QUIT_FACTORY_MODE,
		SET_PARAM_CASHLESS_ADDRESS,
		SET_PARAM_DEFAULT_ACTIVE_STATUS,
		GET_PARAM_PULSE_DURATION,
		GET_PARAM_PULSE_VOLTAGE,
		GET_PARAM_FACTORY_MODE,
		GET_PARAM_CASHLESS_ADDRESS,
		GET_PARAM_DEFAULT_ACTIVE_STATUS,


		RESET,
		SETUP_CONFIG,
		SETUP_PRICE,
		EXPANSION_REQUEST_ID,
		EXPANSION_ENABLE_OPTIONS,
		ENABLE_READER,
		BEGIN_SESSION,
		VEND_REQUEST,
		VEND_DISPATCH,
		END_SESSION,
		NEGATIVE_VEND_APPROVED,
	}

	public static final byte MDB_CMD_COMMON_RESPONSE = 0x01;
	public static final byte MDB_CMD_COMMON_REQUEST = 0x00;
	public static final byte MDB_CMD_RESET = 0x00;
	public static final byte MDB_CMD_READER_CONFIG = 0x01;
	public static final byte MDB_CMD_EXPANSION_PERIPHERALID = 0x09;
	public static final byte MDB_CMD_EXPANSION_ENABLE_OPTIONS = 0x17;
	public static final byte MDB_CMD_REVALUE_APPROVED = 0x0D;
	public static final byte MDB_CMD_REVALUE_LIMIT_AMOUNT = 0x0F;
	public static final byte MDB_CMD_END_SESSION = 0x07;
	public static final byte MDB_CMD_VEND_APPROVED = 0x05;
	public static final byte MDB_CMD_VEND_DENIED = 0x06;
	public static final byte MDB_CMD_BEGIN_SESSION = 0x03;
	public static final byte MDB_CMD_READER_CANCEL = 0x08;
	public static final byte MDB_CMD_NEGATIVE_VEND_APPROVED = 0x05;

	public static final byte MDB_CMD_TRIGGER_PULSE = 0x20;
	public static final byte MDB_CMD_TRIGGER_PULSE_ONESHOT = 0x21;

	public static final byte MDB_CMD_SET_PARAMETER = (byte)0x90;
	public static final byte MDB_CMD_GET_PARAMETER = (byte)0x91;
	public static final byte MDB_CMD_PARAM_PULSE_DURATION = 0x03;
	public static final byte MDB_CMD_PARAM_PULSE_VOLTAGE = 0x04;
	public static final byte MDB_CMD_PARAM_FACTORY_MODE = 0x05;
	public static final byte MDB_CMD_PARAM_CASHLESS_ADDRESS = 0x06;
	public static final byte MDB_CMD_PARAM_ACTIVE_STATUS = 0x07;
	public static final byte[] MDB_CMD_VERSION_REQUEST = {0x09, 0x03, 0x00, (byte) 0x92, (byte) 0x6E, (byte) 0x0D};
	public static final byte MDB_CMD_HARDWARE_VERSION_REQUEST = (byte)0x93;
	public static final byte MDB_CMD_HARDWARE_DIAGNOSE_TEST = (byte)0x94;
	public static final byte MDB_CMD_ACTIVE_CASHLESS = (byte)0x96;

	static Map<MDB_RESPONSE, Function<Object[], byte[]>> mdbResponseMap = new HashMap<>();
	static Map<MDB_REQUEST, Function<Object[], byte[]>> mdbRequestMap = new HashMap<>();

	public static byte[] getResponseOfMdbCommands(MDB_RESPONSE mdbResponse, Object... args) {
		{
			mdbResponseMap.put(MDB_RESPONSE.RESET, EnumMDBCommands::getResponseReset);
			mdbResponseMap.put(MDB_RESPONSE.SETUP_CONFIG, EnumMDBCommands::getResponseSetupConfig);
			mdbResponseMap.put(MDB_RESPONSE.SETUP_PRICE, EnumMDBCommands::getResponseSetupPrice);
			mdbResponseMap.put(MDB_RESPONSE.EXPANSION_REQUESTID, EnumMDBCommands::getResponseExpansionRequestID);
//			mdbResponseMap.put(MDB_RESPONSE.ENABLE_READER, EnumMDBCommands::getResponseEnableReader);
			mdbResponseMap.put(MDB_RESPONSE.REVALUE_APPROVED, EnumMDBCommands::getResponseRevalueApproved);
			mdbResponseMap.put(MDB_RESPONSE.REVALUE_LIMIT_AMOUNT, EnumMDBCommands::getResponseRevalueLimitAmount);
			mdbResponseMap.put(MDB_RESPONSE.VEND_SUCCESS, EnumMDBCommands::getResponseVendSuccess);
			mdbResponseMap.put(MDB_RESPONSE.END_SESSION, EnumMDBCommands::getResponseEndSession);
			mdbResponseMap.put(MDB_RESPONSE.VEND_APPROVED, EnumMDBCommands::getResponseVendApproved);
			mdbResponseMap.put(MDB_RESPONSE.VEND_DENIED, EnumMDBCommands::getResponseVenddenied);
			mdbResponseMap.put(MDB_RESPONSE.NEGATIVE_VEND_APPROVED, EnumMDBCommands::getResponseNegativeVendApproved);
			mdbResponseMap.put(MDB_RESPONSE.READER_CANCELLED, EnumMDBCommands::getResponseReaderCancelled);

			mdbResponseMap.put(MDB_RESPONSE.COMMON_ACK, EnumMDBCommands::getResponseCommonAck);

		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return mdbResponseMap.get(mdbResponse).apply(args);
		} else {
			Log.e("MDBTransaction EnumMDB", "Error: SDK version is less than 24");
			return null;
		}
	}

	public static byte[] getRequestOfMdbCommands(MDB_REQUEST mdbRequest, Object... args) {
		{
			mdbRequestMap.put(MDB_REQUEST.VERSION_REQUEST, EnumMDBCommands::getRequestVersionRequest);
			mdbRequestMap.put(MDB_REQUEST.TRIGGER_PULSE, EnumMDBCommands::getRequestTriggerPulse);
			mdbRequestMap.put(MDB_REQUEST.TRIGGER_PULSE_ONESHOT, EnumMDBCommands::getRequestTriggerPulseOneshot);
			mdbRequestMap.put(MDB_REQUEST.SET_PARAM_PULSE_DURATION, EnumMDBCommands::getRequestSetPulseDuration);
			mdbRequestMap.put(MDB_REQUEST.SET_PARAM_PULSE_VOLTAGE, EnumMDBCommands::getRequestSetPulseVoltage);
			mdbRequestMap.put(MDB_REQUEST.SET_PARAM_ENTER_FACTORY_MODE, EnumMDBCommands::getRequestEnterFactoryMode);
			mdbRequestMap.put(MDB_REQUEST.SET_PARAM_QUIT_FACTORY_MODE, EnumMDBCommands::getRequestQuitFactoryMode);
			mdbRequestMap.put(MDB_REQUEST.HARDWARE_DIAGNOSE_TEST, EnumMDBCommands::getRequestHardwareDiagnoseTest);
			mdbRequestMap.put(MDB_REQUEST.HARDWARE_VERSION_REQUEST, EnumMDBCommands::getRequestHardwareVersionRequest);
			mdbRequestMap.put(MDB_REQUEST.SET_PARAM_CASHLESS_ADDRESS, EnumMDBCommands::getRequestSetCashlessAddress);
			mdbRequestMap.put(MDB_REQUEST.ACTIVE_CASHLESS, EnumMDBCommands::getRequestActiveCashless);
			mdbRequestMap.put(MDB_REQUEST.GET_PARAM_PULSE_DURATION, EnumMDBCommands::getRequestGetParamPulseDuration);
			mdbRequestMap.put(MDB_REQUEST.GET_PARAM_PULSE_VOLTAGE, EnumMDBCommands::getRequestGetParamPulseVoltage);
			mdbRequestMap.put(MDB_REQUEST.GET_PARAM_FACTORY_MODE, EnumMDBCommands::getRequestGetParamFactoryMode);
			mdbRequestMap.put(MDB_REQUEST.GET_PARAM_CASHLESS_ADDRESS, EnumMDBCommands::getRequestGetParamCashlessAddress);
			mdbRequestMap.put(MDB_REQUEST.GET_PARAM_DEFAULT_ACTIVE_STATUS, EnumMDBCommands::getRequestGetParamDefaultActiveStatus);
			mdbRequestMap.put(MDB_REQUEST.SET_PARAM_DEFAULT_ACTIVE_STATUS, EnumMDBCommands::getRequestSetDefaultActiveStatus);

			mdbRequestMap.put(MDB_REQUEST.RESET, EnumMDBCommands::getRequestReset);
			mdbRequestMap.put(MDB_REQUEST.SETUP_CONFIG, EnumMDBCommands::getRequestSetupConfig);
			mdbRequestMap.put(MDB_REQUEST.SETUP_PRICE, EnumMDBCommands::getRequestSetupPrice);
			mdbRequestMap.put(MDB_REQUEST.EXPANSION_REQUEST_ID, EnumMDBCommands::getRequestExpansionRequestID);
			mdbRequestMap.put(MDB_REQUEST.EXPANSION_ENABLE_OPTIONS, EnumMDBCommands::getRequestExpansionEnableOptions);
			mdbRequestMap.put(MDB_REQUEST.ENABLE_READER, EnumMDBCommands::getRequestExpansionEnableReader);
			mdbRequestMap.put(MDB_REQUEST.BEGIN_SESSION, EnumMDBCommands::getRequestBeginSession);
			mdbRequestMap.put(MDB_REQUEST.VEND_REQUEST, EnumMDBCommands::getRequestVendRequest);
			mdbRequestMap.put(MDB_REQUEST.VEND_DISPATCH, EnumMDBCommands::getRequestVendDispatch);
			mdbRequestMap.put(MDB_REQUEST.END_SESSION, EnumMDBCommands::getRequestEndSession);
			mdbRequestMap.put(MDB_REQUEST.NEGATIVE_VEND_APPROVED, EnumMDBCommands::getRequestNegativeVendApproved);

		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return mdbRequestMap.get(mdbRequest).apply(args);
		} else {
			Log.e("EnumMDBCommands", "Error: SDK version is less than 24");
			return null;
		}
	}

	public static <T> byte[] getResponseReset(T... args) {
		byte[] dataBytes = {MDB_CMD_RESET};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseSetupConfig(T... args) {
		MDBValues mdbValues = (MDBValues) args[0];
		Item item = (Item) args[1];

		byte[] dataBytes = {MDB_CMD_READER_CONFIG};
		byte[] readFeatureLevel = {(byte) mdbValues.getMdbLevel()}; //Reader Feature Level
		byte[] countryCode = {0x11, 0x56};//Country Code
		byte[] scaleFactor = {0x01};//Scale Factor (x when calculating price)
		item.setPriceX(Integer.toHexString(scaleFactor[0]));
		byte[] decimalPlaces = {0x02};//Decimal Places (y when calculating price)
		item.setPriceY(Integer.toHexString(decimalPlaces[0]));
		byte[] applicationMaxRespTime = {0x59};//Application Maximum Response Time
		byte[] miscellanenousOptions = {0x0d};//Miscellaneous Options
		dataBytes = MDBUtils.connectBytes(dataBytes, readFeatureLevel, countryCode, scaleFactor,
				decimalPlaces, applicationMaxRespTime, miscellanenousOptions);
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseSetupPrice(T... args) {
		byte[] dataBytes = new byte[1];
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseExpansionRequestID(T... args) {
		MDBValues mdbValues = (MDBValues) args[0];
		byte[] dataBytes = {MDB_CMD_EXPANSION_PERIPHERALID};
		byte[] manufacturerCode = {0x77, 0x7a, 0x70};//Manufacturer Code
		byte[] serialNumber = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01};//Serial Number
		byte[] modelNumber = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01};//Model Number
		byte[] softwareVersion = {0x00, 0x01};//Software Version
		dataBytes = MDBUtils.connectBytes(dataBytes, manufacturerCode, serialNumber, modelNumber, softwareVersion);
		if(mdbValues.getMdbLevel() == 3 && mdbValues.getVmcLevel() == 3) {
			LogHelper.massiveLog('d', "MDB getResponseExpansionRequestID",
							"mdbValues.getOptionalFeature().getOptionalFeatureBitsMdb(): " + mdbValues.getOptionalFeature().getOptionalFeatureBitsMdb());
			byte[] optionalFeatureBits = {0x00, 0x00, 0x00, mdbValues.getOptionalFeature().getOptionalFeatureBitsMdb()};//Optional Feature Bits
			LogHelper.massiveLog('d', "MDB getResponseExpansionRequestID", "optionalFeatureBits: " + ByteConvertStringUtil.buf2StringCompact(optionalFeatureBits));
			dataBytes = MDBUtils.connectBytes(dataBytes, optionalFeatureBits);
		}
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	//e.g. Enable Reader: 09 03 01 00 FF 0D  no resp
	//Cash Sale: 09 03 01 00 FF 0D  no resp
	public static <T> byte[] getResponseCommonAck(T... args) {
		byte[] dataBytes = {MDB_CMD_COMMON_REQUEST};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseRevalueApproved(T... args) {
		byte[] dataBytes = {MDB_CMD_REVALUE_APPROVED};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseRevalueLimitAmount(T... args) {
		byte[] dataBytes = {MDB_CMD_REVALUE_LIMIT_AMOUNT};
		byte[] revalueLimitAmount = {0x27, 0x10};//Revalue Limit Amount
		dataBytes = MDBUtils.connectBytes(dataBytes, revalueLimitAmount);
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseVendSuccess(T... args) {
		byte[] dataBytes = new byte[1];
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseEndSession(T... args) {
		byte[] dataBytes = {MDB_CMD_END_SESSION};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseVendApproved(T... args) {
		Item item = (Item) args[0];
		byte[] dataBytes = {MDB_CMD_VEND_APPROVED};
		byte[] itemId = {(byte) Integer.parseInt(item.getItemAmount().substring(0,2), 16),
				(byte) Integer.parseInt(item.getItemAmount().substring(2), 16)};
		dataBytes = MDBUtils.connectBytes(dataBytes, itemId);
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseVenddenied(T... args) {
		byte[] dataBytes = {MDB_CMD_VEND_DENIED};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseNegativeVendApproved(T... args) {
		MDBValues mdbValues = (MDBValues) args[0];
		Item item = (Item) args[1];
		byte[] dataBytes = {MDB_CMD_NEGATIVE_VEND_APPROVED};
		byte[] itemAmount;
		if(!mdbValues.getOptionalFeature().isMonetaryFormat32Mdb()){
			itemAmount = new byte[]{(byte) Integer.parseInt(item.getItemAmount().substring(0, 2), 16),
					(byte) Integer.parseInt(item.getItemAmount().substring(2), 16)};
		} else {
			itemAmount = new byte[]{(byte) Integer.parseInt(item.getItemAmount().substring(0, 2), 16),
					(byte) Integer.parseInt(item.getItemAmount().substring(2, 4), 16),
					(byte) Integer.parseInt(item.getItemAmount().substring(4, 6), 16),
					(byte) Integer.parseInt(item.getItemAmount().substring(6), 16)};
		}
		dataBytes = MDBUtils.connectBytes(dataBytes, itemAmount);
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}

	public static <T> byte[] getResponseReaderCancelled(T... args) {
		byte[] dataBytes = {MDB_CMD_READER_CANCEL};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_RESPONSE, dataBytes);
	}


	public static <T> byte[] getRequestTriggerPulse(T... args) {
		byte[] dataBytes = {MDB_CMD_TRIGGER_PULSE};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestTriggerPulseOneshot(T... args) {
		PulseValues pulseValues = (PulseValues) args[0];
		byte[] dataBytes = {MDB_CMD_TRIGGER_PULSE_ONESHOT};
		byte[] voltageByte = MDBUtils.intToByteArrayLittleEndian(pulseValues.getPulseVoltage());
		byte[] durationByte = MDBUtils.intToByteArrayLittleEndian(pulseValues.getPulseDuration());
		byte[] intervalByte = MDBUtils.intToByteArrayLittleEndian(pulseValues.getPulseInterval());
		byte[] frequencyByte = MDBUtils.intToByteArrayLittleEndian(pulseValues.getPulseFrequency());
		dataBytes = MDBUtils.connectBytes(dataBytes, voltageByte, durationByte, intervalByte, frequencyByte);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestSetPulseDuration(T... args) {
		int duration = (int) args[0];
		byte[] dataBytes = {MDB_CMD_SET_PARAMETER, MDB_CMD_PARAM_PULSE_DURATION};
		byte[] durationByte = MDBUtils.intToByteArrayLittleEndian(duration);
		dataBytes = MDBUtils.connectBytes(dataBytes, durationByte);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestSetPulseVoltage(T... args) {
		int voltage = (int) args[0];
		byte[] dataBytes = {MDB_CMD_SET_PARAMETER, MDB_CMD_PARAM_PULSE_VOLTAGE};
		byte[] voltageByte = MDBUtils.intToByteArrayLittleEndian(voltage);
		dataBytes = MDBUtils.connectBytes(dataBytes, voltageByte);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestEnterFactoryMode(T... args) {
		byte[] dataBytes = {MDB_CMD_SET_PARAMETER, MDB_CMD_PARAM_FACTORY_MODE};
		byte[] factoryModeData = MDBUtils.intToByteArrayLittleEndian(1);
		dataBytes = MDBUtils.connectBytes(dataBytes, factoryModeData);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestQuitFactoryMode(T... args) {
		byte[] dataBytes = {MDB_CMD_SET_PARAMETER, MDB_CMD_PARAM_FACTORY_MODE};
		byte[] factoryModeData = MDBUtils.intToByteArrayLittleEndian(0);
		dataBytes = MDBUtils.connectBytes(dataBytes, factoryModeData);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestSetCashlessAddress(T... args) {
		byte[] dataBytes = {MDB_CMD_SET_PARAMETER, MDB_CMD_PARAM_CASHLESS_ADDRESS};
		byte[] cashlessAddressData;
		int deviceType = (int) args[0];
		if(deviceType == 1){
			cashlessAddressData = MDBUtils.intToByteArrayLittleEndian(0);
		} else {
			cashlessAddressData = MDBUtils.intToByteArrayLittleEndian(1);
		}
		dataBytes = MDBUtils.connectBytes(dataBytes, cashlessAddressData);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestActiveCashless(T... args) {
		byte[] dataBytes = {MDB_CMD_ACTIVE_CASHLESS};
		byte[] activeCashless;
		boolean active = (boolean) args[0];
		if(active){
			activeCashless = new byte[]{0x01};
		} else {
			activeCashless = new byte[]{0x00};
		}
		dataBytes = MDBUtils.connectBytes(dataBytes, activeCashless);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestGetParamPulseDuration(T... args) {
		byte[] dataBytes = {MDB_CMD_GET_PARAMETER, MDB_CMD_PARAM_PULSE_DURATION};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestGetParamPulseVoltage(T... args) {
		byte[] dataBytes = {MDB_CMD_GET_PARAMETER, MDB_CMD_PARAM_PULSE_VOLTAGE};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestGetParamFactoryMode(T... args) {
		byte[] dataBytes = {MDB_CMD_GET_PARAMETER, MDB_CMD_PARAM_FACTORY_MODE};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestGetParamCashlessAddress(T... args) {
		byte[] dataBytes = {MDB_CMD_GET_PARAMETER, MDB_CMD_PARAM_CASHLESS_ADDRESS};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestGetParamDefaultActiveStatus(T... args) {
		byte[] dataBytes = {MDB_CMD_GET_PARAMETER, MDB_CMD_PARAM_ACTIVE_STATUS};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestSetDefaultActiveStatus(T... args) {
		byte[] dataBytes = {MDB_CMD_SET_PARAMETER, MDB_CMD_PARAM_ACTIVE_STATUS};
		byte[] activeStatusData;
		int active = (int) args[0];
		// 1: inactive, 0: active
		activeStatusData = MDBUtils.intToByteArrayLittleEndian(active);
		dataBytes = MDBUtils.connectBytes(dataBytes, activeStatusData);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	//req: 09 03 00 94 6C 0D
	public static <T> byte[] getRequestHardwareDiagnoseTest(T... args) {
		byte[] dataBytes = {MDB_CMD_HARDWARE_DIAGNOSE_TEST};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestVersionRequest(T... args) {
		return MDB_CMD_VERSION_REQUEST;
	}

	public static <T> byte[] getRequestHardwareVersionRequest(T... args) {
		byte[] dataBytes = {MDB_CMD_HARDWARE_VERSION_REQUEST};
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestReset(T... args) {
		byte[] dataBytes = {MDB_CMD_RESET};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST,dataBytes);
	}

	public static <T> byte[] getRequestSetupConfig(T... args) {
		byte[] dataBytes = {0x11, 0x00, 0x03, 0x00, 0x00, 0x01};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestSetupPrice(T... args) {
		byte[] dataBytes = {0x11, 0x01, 0x2b, 0x5c, 0x00, 0x0a};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestExpansionRequestID(T... args) {
		byte[] dataBytes = {0x17, 0x00, 0x49, 0x44, 0x53, 0x30, 0x30, 0x30,
							0x30, 0x31, 0x30, 0x30, 0x30, 0x36, 0x36, 0x30,
							0x32, 0x49, 0x44, 0x53, 0x4d, 0x44, 0x42, 0x56,
							0x4d, 0x43, 0x43, 0x4f, 0x4e, 0x30, 0x32};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestExpansionEnableOptions(T... args) {
		byte[] dataBytes = {0x17, 0x04, 0x00, 0x00, 0x00, 0x3E};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestExpansionEnableReader(T... args) {
		byte[] dataBytes = {0x14, 0x01};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	//09 0D 00 03 03 E8 FF FF FF FF 00 00 00 63 3A 0D resp: 09 03 01 00 FF 0D
	public static <T> byte[] getRequestBeginSession(T... args) {
		MDBValues mdbValues = (MDBValues) args[0];
		byte[] dataBytes = {MDB_CMD_BEGIN_SESSION};
		byte[] balanceAvailable = new byte[2];
		if(mdbValues.getBalance() < 0) {
			balanceAvailable = new byte[]{0x03, (byte) 0xE8};//funds/balance Available
		} else {
			balanceAvailable = new byte[]{(byte)(mdbValues.getBalance() >> 8 & 0xFF), (byte)(mdbValues.getBalance() & 0xFF)};//funds/balance Available
		}
		byte[] paymentMediaID = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};//Payment Media ID
		byte[] paymentType = {0x00};//Payment Type
		byte[] paymentData = {0x00, 0x00};//Payment Data
		dataBytes = MDBUtils.connectBytes(dataBytes, balanceAvailable, paymentMediaID, paymentType, paymentData);
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestVendRequest(T... args) {
		byte[] dataBytes = {0x13, 0x00, 0x00, 0x64, 0x00, (byte)0xe1};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestVendDispatch(T... args) {
		byte[] dataBytes = {0x13, 0x02, 0x00, (byte)0xe1};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestEndSession(T... args) {
		byte[] dataBytes = {0x13, 0x04};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

	public static <T> byte[] getRequestNegativeVendApproved(T... args) {
		byte[] dataBytes = {0x13, 0x06, 0x00, 0x00, 0x00, 0x64, (byte)0xFF, (byte)0xFF};
		dataBytes = MDBUtils.addMdbCheckSum(dataBytes);
		return MDBUtils.mergePacket(MDB_CMD_COMMON_REQUEST, dataBytes);
	}

}
