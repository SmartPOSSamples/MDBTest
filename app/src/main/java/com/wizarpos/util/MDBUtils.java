package com.wizarpos.util;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class MDBUtils {

	public static final int BLACK_LOG = 1;
	public static final int RED_LOG = 2;
	public static final int BLUE_LOG = 3;
	public static final int GREEN_LOG = 4;
	public static final int SUB_THREAD_CMD_AFTER_WAIT = 5;
	public static final int DIALOG_CONFIRM = 8;
	public static final int DIALOG_SET_PULSE = 9;
	public static final int DIALOG_ITEM = 10;
	public static final int DIALOG_READCARD = 11;
	public static final int DIALOG_READCARD_CLOSE = 12;
	public static final int DIALOG_WAIT = 13;
	public static final int ENABLE_ALL_UI = 15;
	public static final int DISABLE_ALL_UI = 16;

	public static final int TYPE_PULSE_INTERVAL = 20;
	public static final int TYPE_PULSE_FREQUENCY = 21;
	public static final int TYPE_PULSE_DURATION = 22;
	public static final int TYPE_PULSE_VOLTAGE = 23;
	public static final int TYPE_BALANCE = 24;
	public static final int TYPE_SPN_FIRMWARE_ITEM = 25;
	public static final int TYPE_SPN_MDB_LEVEL = 26;
	public static final int TYPE_CB_CHECK_ALWAYS_IDLE = 27;
	public static final int TYPE_CB_CHECK_32BIT_MONETARY = 28;
	public static final int TYPE_CB_CHECK_NEGATIVE_VEND = 29;
	public static final int TYPE_X = 30;
	public static final int TYPE_Y = 31;
	public static final int TYPE_SPN_DEVICE_TYPE = 32;
	public static final int TYPE_CB_CHECK_DEFAULT_ACTIVE = 33;

	public static final int TEST_MSG = 99;



	public static byte[] addMdbCheckSum(byte[] buf) {
		byte[] ret = connectBytes(buf, new byte[]{0x00});
		for(int i = 0; i < ret.length-1; i++){
			ret[ret.length-1] += ret[i];
		}
		return ret;
	}

	/*
	 * add 09 before data,add checksum and 0D after length and mode
	 * data: data content
	 * mode: 00: request, 01: response
	 */
	public static byte[] mergePacket(int mode, byte[] data) {
		byte startCode = 0x09;
		byte endCode = 0x0d;
		byte modebyte = 0x00;
		if (mode == 1) {
			modebyte = 0x01;
		}
		//------
		byte[] modeANDdata = new byte[1 + data.length];
		modeANDdata[0] = modebyte;
		System.arraycopy(data, 0, modeANDdata, 1, data.length);
		byte checkSumLRC = getLRC(modeANDdata);
		//------
		byte[] mergeBytes = new byte[1 + 1 + 1 + data.length + 1 + 1];
		mergeBytes[0] = startCode;
		mergeBytes[1] = (byte) (1 + 1 + data.length);
		mergeBytes[2] = modebyte;
		System.arraycopy(data, 0, mergeBytes, 3, data.length);
		mergeBytes[mergeBytes.length - 2] = checkSumLRC;
		mergeBytes[mergeBytes.length - 1] = endCode;
		//String.format("%02X", byte)
		return mergeBytes;
	}

	public static byte getLRC(byte[] buf) {
		int rr = 0x00;
		for (byte b : buf) {
			rr += (b & 0xFF);
		}
		rr = ~rr + 1;
		return (byte) rr;
	}

	/**
	 * split byte[]
	 */
	public static byte[][] splitBytes(byte[] bytes, int size) {
		double splitLength = Double.parseDouble(size + "");
		int arrayLength = (int) Math.ceil(bytes.length / splitLength);
		byte[][] result = new byte[arrayLength][];
		int from, to;
		for (int i = 0; i < arrayLength; i++) {

			from = (int) (i * splitLength);
			to = (int) (from + splitLength);
			if (to > bytes.length)
				to = bytes.length;
			result[i] = Arrays.copyOfRange(bytes, from, to);
		}
		return result;
	}

	public static byte[] intToByteArrayLittleEndian(int value) {
		return new byte[] {
				(byte) (value & 0xFF),
				(byte) ((value >> 8) & 0xFF),
				(byte) ((value >> 16) & 0xFF),
				(byte) ((value >> 24) & 0xFF)
		};
	}

	public static int byteArrayToIntLittleEndian(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException("Array must be of length 4");
		}
		return ((bytes[0] & 0xFF)) |
				((bytes[1] & 0xFF) << 8) |
				((bytes[2] & 0xFF) << 16) |
				((bytes[3] & 0xFF) << 24);
	}

	public static byte[] connectBytes(byte[]... bytes) {
		int length = 0;
		for (byte[] b : bytes) {
			length += b.length;
		}
		byte[] result = new byte[length];
		int pos = 0;
		for (byte[] b : bytes) {
			System.arraycopy(b, 0, result, pos, b.length);
			pos += b.length;
		}
		return result;
	}

	//金额计算公式 ActualPrice = P * X * 10 ^(-Y) here P is this.balance, ActualPrice(a) is the parameter balance
	// so, p = a * 10^y / x, p: this.balance, a: balance
	//MDB resp:0101020086"0101"00048f 这里x= 01的十进制1, y= 01的十进制1
	//0101021156"0102"590dd3
	public static int getBalanceFromBigDecimal(BigDecimal actualPrice, int x, int y) throws ArithmeticException{
		int ret = -1;
		BigDecimal bigDecimalX = BigDecimal.valueOf(x);
		BigDecimal bigDecimalY = BigDecimal.valueOf(y);
		BigDecimal bdBalance = actualPrice.multiply(BigDecimal.TEN.pow(y))
				.divide(bigDecimalX, 2, RoundingMode.HALF_UP);
		Log.d("MDBValues", "setBalance: actualPrice=" + actualPrice + ", scale factor x=" + x + ",decimal places y=" + y + ", bdBalance=" + bdBalance);
		try {
			ret = bdBalance.intValueExact();
		} catch (ArithmeticException e) {
// 			If the value cannot be represented as an int, we round it
//			bdBalance = bdBalance.setScale(0, RoundingMode.HALF_UP);
			// If the value cannot be represented as an int, use default value 100
			throw new ArithmeticException("Balance cannot be represented as an int, please check the value of x and y");
		}
		return ret;
	}

}
