package com.example.qrcode.qr.zxing;

final class BlockPair {

	  private final byte[] dataBytes;
	  private final byte[] errorCorrectionBytes;

	  BlockPair(byte[] data, byte[] errorCorrection) {
	    dataBytes = data;
	    errorCorrectionBytes = errorCorrection;
	  }

	  public byte[] getDataBytes() {
	    return dataBytes;
	  }

	  public byte[] getErrorCorrectionBytes() {
	    return errorCorrectionBytes;
	  }

	}