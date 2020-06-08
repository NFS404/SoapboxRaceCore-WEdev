package com.soapboxrace.core.xmpp.standard;

import java.net.Socket;

import com.soapboxrace.core.xmpp.BaseOpenFireTalk;
import com.soapboxrace.jaxb.util.JAXBUtility;
import com.soapboxrace.jaxb.xmpp.XMPP_IQPingType;
import com.soapboxrace.jaxb.xmpp.XMPP_IQPongType;

public class StandardOpenFireTalk extends BaseOpenFireTalk {
	public StandardOpenFireTalk(Socket socket) {
		super(socket);
	}

	@Override
	public void handleMessage(String msg) {
		if (msg.contains("<ping xmlns=\"urn:xmpp:ping\"/>")) {
			String hostAddress = socket.getInetAddress().getHostAddress();
			XMPP_IQPingType openfirePing = JAXBUtility.unMarshal(msg, XMPP_IQPingType.class);
			write(JAXBUtility.marshal(new XMPP_IQPongType(openfirePing.getId(), hostAddress)));
		}
	}
}
