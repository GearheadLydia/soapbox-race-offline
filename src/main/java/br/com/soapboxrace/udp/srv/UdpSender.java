package br.com.soapboxrace.udp.srv;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {
	private DatagramSocket serverSocket;
	private InetAddress IPAddress;
	private int port;

	public UdpSender(DatagramSocket serverSocket, DatagramPacket receivePacket) {
		this.serverSocket = serverSocket;
		IPAddress = receivePacket.getAddress();
		port = receivePacket.getPort();
	}

	public void sendData(byte[] sendData) {
		try {
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
