package br.com.soapboxrace.udp.srv;

public class PacketDetector {

	public static PacketType detect(byte[] dataPacket) {
		if (isTypeA(dataPacket)) {
			switch (dataPacket.length) {
			case 26:
				return PacketType.HELLO_SYNC;
			case 22:
				return PacketType.SYNC_DONE;
			default:
				return PacketType.KEEP_ALIVE;
			}
		}
		if (isIdBeforeSync(dataPacket)) {
			return PacketType.ID_BEFORE_SYNC;
		}
		if (isIdAfterSync(dataPacket)) {
			return PacketType.ID_AFTER_SYNC;
		}
		if (isTypeBInit(dataPacket)) {
			return PacketType.POSITION_BEFORE_SYNC;
		}
		if (isTypeB10(dataPacket)) {
			return PacketType.TYPE_10;
		}
		if (isTypeB11(dataPacket)) {
			return PacketType.TYPE_11;
		}
		if (isPositionAfterSync(dataPacket)) {
			return PacketType.POSTITION_AFTER_SYNC;
		}
		return PacketType.HELLO;
	}

	private static boolean isIdAfterSync(byte[] dataPacket) {
		if (isTypeBHeader(dataPacket) && dataPacket[10] == (byte) 0x02) {
			return true;
		}
		return false;
	}

	private static boolean isTypeA(byte[] dataPacket) {
		if (dataPacket[0] == 0x00 && dataPacket[3] == 0x07 && dataPacket[4] == 0x02) {
			return true;
		}
		return false;
	}

	public static boolean isTypeBHeader(byte[] dataPacket) {
		if (dataPacket[0] == 0x01) {
			return true;
		}
		return false;
	}

	private static boolean isIdBeforeSync(byte[] dataPacket) {
		if (isTypeBInit(dataPacket) && dataPacket[10] == (byte) 0x02) {
			return true;
		}
		return false;
	}

	private static boolean isPositionAfterSync(byte[] dataPacket) {
		if (isTypeBHeader(dataPacket) && !isTypeBInitHeader(dataPacket)) {
			if (dataPacket[10] == (byte) 0x12) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTypeB10(byte[] dataPacket) {
		if (isTypeBHeader(dataPacket)) {
			if (dataPacket[10] == (byte) 0x10) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTypeB11(byte[] dataPacket) {
		if (isTypeBHeader(dataPacket)) {
			if (dataPacket[10] == (byte) 0x11) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTypeBInitHeader(byte[] dataPacket) {
		if (isTypeBHeader(dataPacket) && //
				dataPacket[6] == (byte) 0xff && //
				dataPacket[7] == (byte) 0xff && //
				dataPacket[8] == (byte) 0xff && //
				dataPacket[9] == (byte) 0xff //
		) {
			return true;
		}
		return false;
	}

	private static boolean isTypeBInit(byte[] dataPacket) {
		if (isTypeBHeader(dataPacket)) {
			if (dataPacket[6] == (byte) 0xff && //
					dataPacket[7] == (byte) 0xff && //
					dataPacket[8] == (byte) 0xff && //
					dataPacket[9] == (byte) 0xff //
			) {
				return true;
			}
		}
		return false;
	}

}
