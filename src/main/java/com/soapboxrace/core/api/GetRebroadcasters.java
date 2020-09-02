package com.soapboxrace.core.api;

import java.net.URI;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.UserBO;
import com.soapboxrace.jaxb.http.ArrayOfUdpRelayInfo;
import com.soapboxrace.jaxb.http.UdpRelayInfo;

@Path("/getrebroadcasters")
public class GetRebroadcasters {

	@Context
	UriInfo uri;
	
	@Context
	private HttpServletRequest sr;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private UserBO userBO;

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ArrayOfUdpRelayInfo getRebroadcasters(@HeaderParam("securityToken") String securityToken) {
		ArrayOfUdpRelayInfo arrayOfUdpRelayInfo = new ArrayOfUdpRelayInfo();
		UdpRelayInfo udpRelayInfo = new UdpRelayInfo();
		String freeroamIp = parameterBO.getStrParam("UDP_FREEROAM_IP");
		if ("127.0.0.1".equals(freeroamIp)) {
			URI myUri = uri.getBaseUri();
			freeroamIp = myUri.getHost();
		}
		udpRelayInfo.setHost(freeroamIp);
		boolean isFRSyncAlt = userBO.isFRSyncAlt(securityToken);
		if (!isFRSyncAlt) {
			udpRelayInfo.setPort(parameterBO.getIntParam("UDP_FREEROAM_PORT"));
		}
		else { // Experimental alternative Freeroam sync module
			udpRelayInfo.setPort(parameterBO.getIntParam("UDP_FREEROAM_ALT_PORT"));
		}
		arrayOfUdpRelayInfo.getUdpRelayInfo().add(udpRelayInfo);
		return arrayOfUdpRelayInfo;
	}
}
