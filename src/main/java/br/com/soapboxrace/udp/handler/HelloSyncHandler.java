package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.SoapBoxPacketProcessor;
import br.com.soapboxrace.udp.srv.UdpListener;
import br.com.soapboxrace.udp.srv.UdpSender;

public class HelloSyncHandler extends PacketHandler {

	private byte[] helloSync;

	public HelloSyncHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] transformByteTypeA = SoapBoxPacketProcessor.transformByteTypeASync(packet, timeDiff);
		this.helloSync = packet;
		sendPacket(transformByteTypeA);
	}

	public void reSendPacket() {
		long timeDiff = UdpListener.getTimeDiff();
		byte[] transformByteTypeA = SoapBoxPacketProcessor.transformByteTypeASync(helloSync, timeDiff);
		sendPacket(transformByteTypeA);
	}

}
