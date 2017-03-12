package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.SoapBoxPacketProcessor;
import br.com.soapboxrace.udp.srv.UdpSender;

public class KeepAliveHandler extends PacketHandler {

	public KeepAliveHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] transformByteTypeA = SoapBoxPacketProcessor.transformByteTypeA(packet, timeDiff);
		sendPacket(transformByteTypeA);
	}

}
