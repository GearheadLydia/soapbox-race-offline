package br.com.soapboxrace.udp.handler;

import java.nio.ByteBuffer;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.UdpDebug;
import br.com.soapboxrace.udp.srv.UdpListener;
import br.com.soapboxrace.udp.srv.UdpSender;

public class HelloHandler extends PacketHandler {

	public HelloHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] sendData = UdpDebug.hexStringToByteArray("00:00:00:01:99:99:66:66:01:01:01:01");
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) timeDiff).array();
		sendData[4] = timeArray[0];
		sendData[5] = timeArray[1];
		parsePacketTimeA(packet);
		sendPacket(sendData);
	}

	private void parsePacketTimeA(byte[] packet) {
		byte[] timeA = new byte[] { packet[(packet.length - 6)], packet[(packet.length - 5)] };
		UdpListener.helloTime = timeA.clone();
	}

}
