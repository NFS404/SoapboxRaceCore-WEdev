package com.soapboxrace.core.bo.util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import org.igniterealtime.restclient.entity.MUCRoomEntity;
import java.util.List;
import java.util.stream.Collectors;

// From WorldOnlinePL sources, by Metonator
@Stateless
public class SendToAllXMPP {
	@EJB
    private OpenFireRestApiCli restApiCli;

    @EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;

	public void sendMessage(String message, String channelname) {
		List<MUCRoomEntity> channels = restApiCli.getAllRooms()
			.stream()
			.filter(r -> r.getRoomName().startsWith(channelname))
			.collect(Collectors.toList());

        String msg = XmppChat.createSystemMessage(message);

        for (MUCRoomEntity channel : channels) {
            List<Long> members = restApiCli.getAllOccupantsInRoom(channel.getRoomName());

            for (Long member : members) {
                openFireSoapBoxCli.send(msg, member);
            }
        }
	}

	// internalXmpp.sendMessageToChannel("A Random message", "channelname"); // sends to a specific channel, ie channel.en__1 sends to EN 1
	// internalXmpp.sendMessage("Random msg"); // sends to all messages, including groups, channels, and events
	public void sendMessage(String message) {
		sendMessage(message, "channel");
	}
}