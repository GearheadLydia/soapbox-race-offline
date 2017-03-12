package br.com.soapboxrace.udp.srv;

public abstract class PacketHandler {

	private UdpSender udpSender;

	public PacketHandler(UdpSender udpSender) {
		this.udpSender = udpSender;

	}

	protected void sendPacket(byte[] packet) {
		udpSender.sendData(packet);
	}

	public abstract void handlePacket(byte[] packet, long timeDiff);

}
