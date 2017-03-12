package br.com.soapboxrace.udp.srv;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import br.com.soapboxrace.udp.handler.HelloHandler;
import br.com.soapboxrace.udp.handler.HelloSyncHandler;
import br.com.soapboxrace.udp.handler.IdAfterSyncHandler;
import br.com.soapboxrace.udp.handler.IdBeforeSyncHandler;
import br.com.soapboxrace.udp.handler.KeepAliveHandler;
import br.com.soapboxrace.udp.handler.PositionAfterSyncHandler;
import br.com.soapboxrace.udp.handler.PositionBeforeSyncHandler;
import br.com.soapboxrace.udp.handler.SyncDoneHandler;
import br.com.soapboxrace.udp.handler.Type10Handler;
import br.com.soapboxrace.udp.handler.Type11Handler;

public class UdpListener extends Thread {

	private static UdpSender udpSender;
	private DatagramPacket receivePacket;
	private DatagramSocket serverSocket;
	private static HashMap<PacketType, PacketHandler> packetHandlers = new HashMap<>();
	private static long timeLong = 0L;
	public static byte[] helloTime;
	public static byte[] helloSync;

	public UdpListener(DatagramSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public static long getTimeDiff() {
		return System.currentTimeMillis() - timeLong;
	}

	@Override
	public void run() {
		byte[] receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while (true) {
			try {
				serverSocket.receive(receivePacket);
				if (udpSender == null) {
					timeLong = System.currentTimeMillis();
					udpSender = new UdpSender(serverSocket, receivePacket);
				}
				byte[] bytes = getBytes(receivePacket);
				PacketType detect = PacketDetector.detect(bytes);
				// System.out.println("incomming packet type: [" + detect.toString() + "] time: [" + getTimeDiff() + "ms]");
				System.out.println("in  " + UdpDebug.byteArrayToHexString(bytes));
				PacketHandler packetHandler = getPacketHandler(detect);
				packetHandler.handlePacket(bytes, getTimeDiff());
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static PacketHandler getPacketHandler(PacketType packetType) {
		PacketHandler packetHandler = packetHandlers.get(packetType);
		if (packetHandler == null) {
			switch (packetType) {
			case HELLO:
				packetHandler = new HelloHandler(udpSender);
				break;
			case HELLO_SYNC:
				packetHandler = new HelloSyncHandler(udpSender);
				break;
			case ID_AFTER_SYNC:
				packetHandler = new IdAfterSyncHandler(udpSender);
				break;
			case ID_BEFORE_SYNC:
				packetHandler = new IdBeforeSyncHandler(udpSender);
				break;
			case KEEP_ALIVE:
				packetHandler = new KeepAliveHandler(udpSender);
				break;
			case POSITION_BEFORE_SYNC:
				packetHandler = new PositionBeforeSyncHandler(udpSender);
				break;
			case POSTITION_AFTER_SYNC:
				packetHandler = new PositionAfterSyncHandler(udpSender);
				break;
			case SYNC_DONE:
				packetHandler = new SyncDoneHandler(udpSender);
				break;
			case TYPE_10:
				packetHandler = new Type10Handler(udpSender);
				break;
			case TYPE_11:
				packetHandler = new Type11Handler(udpSender);
				break;
			default:
				break;
			}
			packetHandlers.put(packetType, packetHandler);
		}
		return packetHandler;
	}

	private byte[] getBytes(DatagramPacket receivePacket) {
		int length = receivePacket.getLength();
		byte[] data = new byte[length];
		System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
		return data;
	}

}
