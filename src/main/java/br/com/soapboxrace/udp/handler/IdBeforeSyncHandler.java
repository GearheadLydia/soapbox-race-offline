package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.PacketType;
import br.com.soapboxrace.udp.srv.SoapBoxPacketProcessor;
import br.com.soapboxrace.udp.srv.UdpListener;
import br.com.soapboxrace.udp.srv.UdpSender;

public class IdBeforeSyncHandler extends PacketHandler {

	public IdBeforeSyncHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] transformByteTypeBInit = SoapBoxPacketProcessor.transformByteTypeBInit(packet);
		sendPacket(transformByteTypeBInit);
		HelloSyncHandler helloSyncHandler = (HelloSyncHandler) UdpListener.getPacketHandler(PacketType.HELLO_SYNC);
		helloSyncHandler.reSendPacket();
	}

}
