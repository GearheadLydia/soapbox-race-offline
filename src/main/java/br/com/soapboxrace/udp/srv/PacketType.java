package br.com.soapboxrace.udp.srv;

public enum PacketType {

	HELLO, //
	HELLO_SYNC, // size 26
	SYNC_DONE, // size 22
	KEEP_ALIVE, // size 18
	ID_BEFORE_SYNC, // 0x02 with count 0xff
	POSITION_BEFORE_SYNC, // 0x12 with count 0xff
	ID_AFTER_SYNC, // 0x02
	POSTITION_AFTER_SYNC, // 0x12
	TYPE_10, //
	TYPE_11;

}
