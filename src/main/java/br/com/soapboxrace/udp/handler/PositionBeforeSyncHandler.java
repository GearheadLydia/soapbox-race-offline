package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.UdpSender;

public class PositionBeforeSyncHandler extends PacketHandler {

	public PositionBeforeSyncHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		// TODO Auto-generated method stub
	}

}
