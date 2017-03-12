package br.com.soapboxrace.xmpp;

import br.com.soapboxrace.func.Constants;

public class XmppLobbyThread extends Thread {

	private String personaId;
	private String eventId;

	public XmppLobbyThread(String personaId, String eventId) {
		this.personaId = personaId;
		this.eventId = eventId;
	}

	public void run() {
		sleepFor(15000L);
		String acceptEvent = Constants.acceptEvent(personaId, eventId);
		//System.out.println(acceptEvent);
		XmppSrv.sendMsg(Long.valueOf(personaId), acceptEvent);
	}

	private void sleepFor(Long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
