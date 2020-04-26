package com.soapboxrace.core.xmpp;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.jaxb.util.MarshalXML;

@Startup
@Singleton
public class OpenFireSoapBoxCli {

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private IHandshake handshake;

	private IOpenFireTalk xmppTalk;

	@PostConstruct
	public void init() {
		this.xmppTalk = handshake.getXmppTalk();
	}

    @Lock(LockType.READ)
	public void send(String msg, String to) {
		if (xmppTalk.getSocket().isClosed()) {
			restart();
		}
		xmppTalk.send(msg, to, parameterBO);
		// System.out.println("DEBUG OpenFire SendMsg1 attempt");
	}

    @Lock(LockType.READ)
	public void send(String msg, Long to) {
		if (xmppTalk.getSocket().isClosed()) {
			restart();
		}
		xmppTalk.send(msg, to, parameterBO);
		// System.out.println("DEBUG OpenFire SendMsg2 attempt");
	}

    @Lock(LockType.READ)
	public void send(Object object, Long to) {
		if (xmppTalk.getSocket().isClosed()) {
			restart();
		}
		String responseXmlStr = MarshalXML.marshal(object);
		this.send(responseXmlStr, to);
		// System.out.println("DEBUG OpenFire SendObject attempt");
	}

	public void setXmppTalk(IOpenFireTalk xmppTalk) {
		this.xmppTalk = xmppTalk;
	}

	private void restart() {
		handshake.init();
		this.xmppTalk = handshake.getXmppTalk();
	}

}
