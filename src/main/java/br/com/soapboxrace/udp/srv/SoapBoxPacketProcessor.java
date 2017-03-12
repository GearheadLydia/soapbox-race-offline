package br.com.soapboxrace.udp.srv;

import java.nio.ByteBuffer;

public class SoapBoxPacketProcessor {

	private static int countA = 1;
	private static int countB = 0;
	private static int countSync = 0;
	private static byte sessionFromClientIdx;
	long timeLong;

	public SoapBoxPacketProcessor() {
		timeLong = System.currentTimeMillis();
	}

	public static byte[] getProcessed(byte[] data, byte sessionFromClientIdx) {
		SoapBoxPacketProcessor.sessionFromClientIdx = sessionFromClientIdx;
		if (isTypeB(data)) {
			return transformByteTypeB(data);
		}
		if (isTypeA(data)) {
			countSync++;
			System.out.println(sessionFromClientIdx + " [" + byteArrayToHexString(data) + "] " + countSync);
			return transformByteTypeA(data, 0);
		}
		if (isTypeASync(data)) {
			return transformByteTypeASync(data, 0);
		}
		return data;
	}

	public static boolean isTypeA(byte[] data) {
		if (data == null) {
			return false;
		}
		if (data[0] == 0x00 && data[3] == 0x07 && data.length != 26) {
			return true;
		}
		return false;
	}

	public static boolean isTypeASync(byte[] data) {
		if (data[0] == 0x00 && data[3] == 0x07 && data.length == 26) {
			return true;
		}
		return false;
	}

	public static boolean isTypeB(byte[] data) {
		if (data[0] == 0x01) {
			return true;
		}
		return false;
	}

	public static byte[] transformByteTypeASync22(byte[] data, long timeDiff) {
		byte[] seqArray = ByteBuffer.allocate(2).putShort((short) countA).array();
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) timeDiff).array();
		int size = data.length;
		byte[] dataTmp = new byte[size];
		dataTmp[1] = seqArray[0];
		dataTmp[2] = seqArray[1];
		int iDataTmp = 3;
		for (int i = 4; i < data.length; i++) {
			dataTmp[iDataTmp++] = data[i];
		}
		dataTmp[4] = timeArray[0];
		dataTmp[5] = timeArray[1];

		dataTmp[15] = (byte) 0x01;
		dataTmp[16] = (byte) 0xff;
		dataTmp[17] = (byte) 0xff;
		dataTmp[18] = (byte) 0x01;
		dataTmp[19] = (byte) 0x01;
		dataTmp[20] = (byte) 0x01;
		dataTmp[21] = (byte) 0x01;
		countA++;
		return dataTmp.clone();
	}

	public static byte[] transformByteTypeA(byte[] data, long timeDiff) {
		byte[] seqArray = ByteBuffer.allocate(2).putShort((short) countA).array();
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) timeDiff).array();
		int size = data.length - 1;
		byte[] dataTmp = new byte[size];
		dataTmp[1] = seqArray[0];
		dataTmp[2] = seqArray[1];
		int iDataTmp = 3;
		for (int i = 4; i < data.length; i++) {
			dataTmp[iDataTmp++] = data[i];
		}
		dataTmp[4] = timeArray[0];
		dataTmp[5] = timeArray[1];
		countA++;
		return dataTmp;
	}

	public static byte[] transformByteTypeASync(byte[] data, long timeDiff) {
		byte[] clone = data.clone();
		clone = transformByteTypeA(clone, timeDiff);
		clone[(clone.length - 11)] = sessionFromClientIdx;
		clone[(clone.length - 6)] = 0x03;
		clone[8] = 0x00;
		clone[9] = 0x01;
		clone[10] = 0x7f;
		countA++;
		return clone;
	}

	public static byte[] transformByteTypeB(byte[] data) {
		if (data.length < 4) {
			return null;
		}
		byte[] seqArray = ByteBuffer.allocate(2).putShort((short) countB).array();
		int size = data.length - 3;
		byte[] dataTmp = new byte[size];
		dataTmp[0] = 1;
		dataTmp[1] = sessionFromClientIdx;
		dataTmp[2] = seqArray[0];
		dataTmp[3] = seqArray[1];
		int iDataTmp = 4;
		for (int i = 6; i < (data.length - 1); i++) {
			dataTmp[iDataTmp++] = data[i];
		}
		dataTmp[4] = (byte) 0xff;
		dataTmp[5] = (byte) 0xff;
		countB++;
		return dataTmp;
	}

	private static String byteArrayToHexString(byte[] b) {
		if (b == null) {
			return "null";
		}
		int len = b.length;
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < len; i++) {
			stringBuffer.append(Integer.toHexString((b[i] >> 4) & 0xf));
			stringBuffer.append(Integer.toHexString(b[i] & 0xf));
			stringBuffer.append(':');
		}
		return stringBuffer.toString();
	}

	public static boolean isTypeBInit(byte[] dataPacket) {
		if (isTypeB(dataPacket)) {
			if (dataPacket[6] == (byte) 0xff && //
					dataPacket[7] == (byte) 0xff && //
					dataPacket[8] == (byte) 0xff && //
					dataPacket[9] == (byte) 0xff && //
					dataPacket[3] == (byte) 0x73 //
			) {
				return true;
			}
		}
		return false;
	}

	public static byte[] transformByteTypeBInit(byte[] dataPacket) {
		if (dataPacket.length <= 50) {
			return dataPacket;
		}
		byte[] transformByteTypeB = transformByteTypeB(dataPacket);
		byte[] dataTmp = transformByteTypeB;

		// player name
		dataTmp[11] = (byte) 0x5e;
		dataTmp[12] = (byte) 0x48;
		dataTmp[13] = (byte) 0x34;
		dataTmp[14] = (byte) 0x58;
		dataTmp[15] = (byte) 0x30;
		dataTmp[16] = (byte) 0x52;
		dataTmp[17] = (byte) 0x5e;

		// personaId
		dataTmp[43] = (byte) 0xc8;
		dataTmp[44] = (byte) 0x00;
		dataTmp[45] = (byte) 0x00;
		dataTmp[46] = (byte) 0x00;

		return dataTmp;
	}

}
