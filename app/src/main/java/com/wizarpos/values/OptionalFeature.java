package com.wizarpos.values;

import com.wizarpos.util.LogHelper;

public class OptionalFeature {
	//count from right side
	private boolean fileTransportLayerSupportedMdb;// 0
	private boolean monetaryFormat32Mdb;// 1
	private boolean multiCurrencySupportedMdb;// 2
	private boolean negativeVendAllowedMdb;// 3
	private boolean dataEntryAllowedMdb;// 4
	private boolean alwaysIdleMdb;// 5
	private byte optionalFeatureBitsMdb;

	private boolean fileTransportLayerSupportedVmc;// 0
	private boolean monetaryFormat32Vmc;// 1
	private boolean multiCurrencySupportedVmc;// 2
	private boolean negativeVendAllowedVmc;// 3
	private boolean dataEntryAllowedVmc;// 4
	private boolean alwaysIdleVmc;// 5
	private byte optionalFeatureBitsVmc;



	public OptionalFeature() {
		this.fileTransportLayerSupportedMdb = false;
		this.monetaryFormat32Mdb = false;
		this.multiCurrencySupportedMdb = false;
		this.negativeVendAllowedMdb = false;
		this.dataEntryAllowedMdb = false;
		this.alwaysIdleMdb = false;
		this.optionalFeatureBitsMdb = 0x00;

		this.fileTransportLayerSupportedVmc = false;
		this.monetaryFormat32Vmc = false;
		this.multiCurrencySupportedVmc = false;
		this.negativeVendAllowedVmc = false;
		this.dataEntryAllowedVmc = false;
		this.alwaysIdleVmc = false;
		this.optionalFeatureBitsVmc = 0x00;
	}

	public boolean isFileTransportLayerSupportedMdb() {
		return fileTransportLayerSupportedMdb;
	}

	public void setFileTransportLayerSupportedMdb(boolean fileTransportLayerSupportedMdb) {
		this.fileTransportLayerSupportedMdb = fileTransportLayerSupportedMdb;
		LogHelper.massiveLog('d', "OptionalFeature", "setFileTransportLayerSupportedMdb: " + fileTransportLayerSupportedMdb);
		setOptionalFeatureBitsMdb(true, 0);
	}

	public boolean isFileTransportLayerSupportedVmc() {
		return fileTransportLayerSupportedVmc;
	}

	public void setFileTransportLayerSupportedVmc(boolean fileTransportLayerSupportedVmc) {
		this.fileTransportLayerSupportedVmc = fileTransportLayerSupportedVmc;
		setOptionalFeatureBitsVmc(fileTransportLayerSupportedVmc, 0);
	}

	public boolean isMonetaryFormat32Mdb() {
		return monetaryFormat32Mdb;
	}

	public void setMonetaryFormat32Mdb(boolean monetaryFormat32Mdb) {
		this.monetaryFormat32Mdb = monetaryFormat32Mdb;
		LogHelper.massiveLog('d', "OptionalFeature", "setMonetaryFormat32Mdb: " + monetaryFormat32Mdb);
		setOptionalFeatureBitsMdb(monetaryFormat32Mdb, 1);
	}

	public boolean isMonetaryFormat32Vmc() {
		return monetaryFormat32Vmc;
	}

	public void setMonetaryFormat32Vmc(boolean monetaryFormat32Vmc) {
		this.monetaryFormat32Vmc = monetaryFormat32Vmc;
		setOptionalFeatureBitsVmc(monetaryFormat32Vmc, 1);
	}

	public boolean isMultiCurrencySupportedMdb() {
		return multiCurrencySupportedMdb;
	}

	public void setMultiCurrencySupportedMdb(boolean multiCurrencySupportedMdb) {
		this.multiCurrencySupportedMdb = multiCurrencySupportedMdb;
		LogHelper.massiveLog('d', "OptionalFeature", "setMultiCurrencySupportedMdb: " + multiCurrencySupportedMdb);
		setOptionalFeatureBitsMdb(multiCurrencySupportedMdb, 2);
	}

	public boolean isMultiCurrencySupportedVmc() {
		return multiCurrencySupportedVmc;
	}

	public void setMultiCurrencySupportedVmc(boolean multiCurrencySupportedVmc) {
		this.multiCurrencySupportedVmc = multiCurrencySupportedVmc;
		setOptionalFeatureBitsVmc(multiCurrencySupportedVmc, 2);
	}

	public boolean isNegativeVendAllowedMdb() {
		return negativeVendAllowedMdb;
	}

	public void setNegativeVendAllowedMdb(boolean negativeVendAllowedMdb) {
		this.negativeVendAllowedMdb = negativeVendAllowedMdb;
		LogHelper.massiveLog('d', "OptionalFeature", "setNegativeVendAllowedMdb: " + negativeVendAllowedMdb);
		setOptionalFeatureBitsMdb(negativeVendAllowedMdb, 3);

	}

	public boolean isNegativeVendAllowedVmc() {
		return negativeVendAllowedVmc;
	}

	public void setNegativeVendAllowedVmc(boolean negativeVendAllowedVmc) {
		this.negativeVendAllowedVmc = negativeVendAllowedVmc;
		setOptionalFeatureBitsVmc(negativeVendAllowedVmc, 3);

	}

	public boolean isDataEntryAllowedMdb() {
		return dataEntryAllowedMdb;
	}

	public void setDataEntryAllowedMdb(boolean dataEntryAllowedMdb) {
		this.dataEntryAllowedMdb = dataEntryAllowedMdb;
		LogHelper.massiveLog('d', "OptionalFeature", "setDataEntryAllowedMdb: " + dataEntryAllowedMdb);
		setOptionalFeatureBitsMdb(dataEntryAllowedMdb, 4);

	}

	public boolean isDataEntryAllowedVmc() {
		return dataEntryAllowedVmc;
	}

	public void setDataEntryAllowedVmc(boolean dataEntryAllowedVmc) {
		this.dataEntryAllowedVmc = dataEntryAllowedVmc;
		setOptionalFeatureBitsVmc(dataEntryAllowedVmc, 4);

	}

	public boolean isAlwaysIdleMdb() {
		return alwaysIdleMdb;
	}

	public void setAlwaysIdleMdb(boolean alwaysIdleMdb) {
		this.alwaysIdleMdb = alwaysIdleMdb;
		LogHelper.massiveLog('e', "OptionalFeature", "setAlwaysIdleMdb: " + this.alwaysIdleMdb);
		setOptionalFeatureBitsMdb(alwaysIdleMdb, 5);
		LogHelper.massiveLog('e', "OptionalFeature", "optionalFeatureBitsMdb: " + this.optionalFeatureBitsMdb);
	}

	public boolean isAlwaysIdleVmc() {
		return alwaysIdleVmc;
	}

	public void setAlwaysIdleVmc(boolean alwaysIdleVmc) {
		this.alwaysIdleVmc = alwaysIdleVmc;
		LogHelper.massiveLog('d', "OptionalFeature", "setAlwaysIdleVmc: " + alwaysIdleVmc);
		setOptionalFeatureBitsVmc(alwaysIdleVmc, 5);
	}

	public byte getOptionalFeatureBitsMdb() {
		return optionalFeatureBitsMdb;
	}

	public void setOptionalFeatureBitsMdb(byte optionalFeatureBitsMdb) {
		this.optionalFeatureBitsMdb = optionalFeatureBitsMdb;
		setFileTransportLayerSupportedMdb((optionalFeatureBitsMdb & 0x01) == 0x01);
		setMonetaryFormat32Mdb(((optionalFeatureBitsMdb >> 1) & 0x01) == 0x01);
		setMultiCurrencySupportedMdb(((optionalFeatureBitsMdb >> 2) & 0x01) == 0x01);
		setNegativeVendAllowedMdb(((optionalFeatureBitsMdb >> 3) & 0x01) == 0x01);
		setDataEntryAllowedMdb(((optionalFeatureBitsMdb >> 4) & 0x01) == 0x01);
		setAlwaysIdleMdb(((optionalFeatureBitsMdb >> 5) & 0x01) == 0x01);
	}

	public void setOptionalFeatureBitsMdb(boolean state, int bit) {
		if (state) {
			optionalFeatureBitsMdb |= (byte) (1 << bit);
		} else {
			optionalFeatureBitsMdb &= (byte) ~(1 << bit);
		}
	}

	public byte getOptionalFeatureBitsVmc() {
		return optionalFeatureBitsVmc;
	}

	public void setOptionalFeatureBitsVmc(byte optionalFeatureBitsVmc) {
		this.optionalFeatureBitsVmc = optionalFeatureBitsVmc;
		setFileTransportLayerSupportedVmc((optionalFeatureBitsVmc & 0x01) == 0x01);
		setMonetaryFormat32Vmc(((optionalFeatureBitsVmc >> 1) & 0x01) == 0x01);
		setMultiCurrencySupportedVmc(((optionalFeatureBitsVmc >> 2) & 0x01) == 0x01);
		setNegativeVendAllowedVmc(((optionalFeatureBitsVmc >> 3) & 0x01) == 0x01);
		setDataEntryAllowedVmc(((optionalFeatureBitsVmc >> 4) & 0x01) == 0x01);
		setAlwaysIdleVmc(((optionalFeatureBitsVmc >> 5) & 0x01) == 0x01);
	}

	public void setOptionalFeatureBitsVmc(boolean state, int bit) {
		if (state) {
			optionalFeatureBitsVmc |= (byte) (1 << bit);
		} else {
			optionalFeatureBitsVmc &= (byte) ~(1 << bit);
		}
	}

	@Override
	public String toString() {
		return "OptionalFeature{" +
				"fileTransportLayerSupportedMdb=" + fileTransportLayerSupportedMdb +
				", monetaryFormat32Mdb=" + monetaryFormat32Mdb +
				", multiCurrencySupportedMdb=" + multiCurrencySupportedMdb +
				", negativeVendAllowedMdb=" + negativeVendAllowedMdb +
				", dataEntryAllowedMdb=" + dataEntryAllowedMdb +
				", alwaysIdleMdb=" + alwaysIdleMdb +
				", optionalFeatureBitsMdb=" + optionalFeatureBitsMdb +
				", fileTransportLayerSupportedVmc=" + fileTransportLayerSupportedVmc +
				", monetaryFormat32Vmc=" + monetaryFormat32Vmc +
				", multiCurrencySupportedVmc=" + multiCurrencySupportedVmc +
				", negativeVendAllowedVmc=" + negativeVendAllowedVmc +
				", dataEntryAllowedVmc=" + dataEntryAllowedVmc +
				", alwaysIdleVmc=" + alwaysIdleVmc +
				", optionalFeatureBitsVmc=" + optionalFeatureBitsVmc +
				'}';
	}
}
