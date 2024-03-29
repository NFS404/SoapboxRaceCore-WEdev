package com.soapboxrace.core.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.jaxb.http.RegionInfo;

@Path("/getregioninfo")
public class GetRegionInfo {
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public RegionInfo getRegionInfo() {
		RegionInfo regionInfo = new RegionInfo();
		regionInfo.setCountdownProposalInMilliseconds(30000);
		regionInfo.setDirectConnectTimeoutInMilliseconds(1000);
		regionInfo.setDropOutTimeInMilliseconds(15000);
		regionInfo.setEventLoadTimeoutInMilliseconds(30000);
		regionInfo.setHeartbeatIntervalInMilliseconds(1000);
		regionInfo.setUdpRelayBandwidthInBps(9600);
		regionInfo.setUdpRelayTimeoutInMilliseconds(35000);
		return regionInfo;

	}
}
