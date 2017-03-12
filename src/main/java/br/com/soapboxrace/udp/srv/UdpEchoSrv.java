package br.com.soapboxrace.udp.srv;

import java.net.DatagramSocket;

public class UdpEchoSrv {

	private DatagramSocket serverSocket;

	public void start(int srvPort) {
		try {
			serverSocket = new DatagramSocket(srvPort);
			UdpListener udpListener = new UdpListener(serverSocket);
			udpListener.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		int port = 9998;
		UdpEchoSrv estudos = new UdpEchoSrv();
		estudos.start(port);
	}

}
