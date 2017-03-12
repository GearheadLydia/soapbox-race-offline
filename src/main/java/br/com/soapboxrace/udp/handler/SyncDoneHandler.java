package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.SoapBoxPacketProcessor;
import br.com.soapboxrace.udp.srv.UdpSender;

public class SyncDoneHandler extends PacketHandler {

	public SyncDoneHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] transformByteTypeASync22 = SoapBoxPacketProcessor.transformByteTypeASync22(packet, timeDiff);
		sendPacket(transformByteTypeASync22);
	}

}
