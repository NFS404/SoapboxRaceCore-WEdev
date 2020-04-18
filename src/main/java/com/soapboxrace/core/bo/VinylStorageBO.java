package com.soapboxrace.core.bo;

import java.util.Random;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.OwnedCarConverter;
import com.soapboxrace.core.dao.FriendListDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.VinylStorageDAO;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.VinylStorageEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.util.MarshalXML;
import com.soapboxrace.jaxb.util.UnmarshalXML;

@Stateless
public class VinylStorageBO {

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private FriendListDAO friendListDAO;

	@EJB
	private DriverPersonaBO driverPersonaBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private TeamsBO teamsBO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private OpenFireRestApiCli openFireRestApiCli;

	@EJB
	private TokenSessionDAO tokenSessionDAO;
	
	@EJB
	private TeamsDAO teamsDAO;
	
	@EJB
	private ReportDAO reportDAO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private CommerceBO commerceBO;
	
	@EJB
	private VinylStorageDAO vinylStorageDAO;

	// Applies the vinyls from DB, uses OwnedCarTrans as a blank for already existed scripts (Not the ideal way...)
	public void vinylStorageApply(Long personaId, String displayName) {
		String entryValue = displayName.replaceFirst("/VINYL ", "");
		VinylStorageEntity vinylStorageEntity = vinylStorageDAO.findByCode(entryValue);
		if (vinylStorageEntity == null) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This vinyl code is invaild, re-check and try again."), personaId);
		}
		else {
			CarSlotEntity defaultCarEntity = personaBO.getDefaultCarEntity(personaId);
			boolean isCompatible = true;
			if (vinylStorageEntity.getCarHash() != defaultCarEntity.getOwnedCar().getCustomCar().getPhysicsProfileHash()) {
				isCompatible = false;
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This vinyl is incompatible with your car."), personaId);
			}
			if (isCompatible) {
				String paintTrans = vinylStorageEntity.getPaintTrans();
				String vinylTrans = vinylStorageEntity.getVinylTrans();
					
				String transForm1 = "<OwnedCarTrans><CustomCar><BaseCar>0</BaseCar><CarClassHash>0</CarClassHash><Id>0</Id><IsPreset>true</IsPreset>" +
				"<Level>0</Level><Name/>";
				String transForm2 = "<PerformanceParts/><PhysicsProfileHash>0</PhysicsProfileHash><Rating>0</Rating><ResalePrice>0.0</ResalePrice>" +
				"<RideHeightDrop>0.0</RideHeightDrop><SkillModParts/><SkillModSlotCount>6</SkillModSlotCount><Version>0</Version>";
				String transForm3 = "<VisualParts/></CustomCar><Durability>100</Durability><Heat>1.0</Heat><Id>0</Id>" +
				"<OwnershipType>CustomizedCar</OwnershipType></OwnedCarTrans>";
				String transFormEnd = transForm1 + paintTrans + transForm2 + vinylTrans + transForm3;
				
				OwnedCarTrans OwnedVinylTrans = UnmarshalXML.unMarshal(transFormEnd, OwnedCarTrans.class);
				
				CustomCarTrans customCarTrans = OwnedVinylTrans.getCustomCar();
					
				commerceBO.updateCarVinyl(customCarTrans, defaultCarEntity);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully applied to your current car. Go to the garage to check out the livery."), personaId);
			}
		}
	}
	
	public void vinylStorageUpload(Long personaId) {
		VinylStorageEntity vinylStorageEntity = new VinylStorageEntity();
		OwnedCarEntity ownedCarEntity = personaBO.getDefaultCarEntity(personaId).getOwnedCar();
		CustomCarEntity defaultCarEntity = ownedCarEntity.getCustomCar();
		OwnedCarTrans ownedCarTrans = OwnedCarConverter.entity2Trans(personaBO.getDefaultCarEntity(personaId).getOwnedCar());
		CustomCarTrans customCarTrans = ownedCarTrans.getCustomCar();
		
		vinylStorageEntity.setPersonaId(personaId);
		vinylStorageEntity.setCarHash(defaultCarEntity.getPhysicsProfileHash());
		
		String paintTrans = MarshalXML.marshal(customCarTrans.getPaints());
		paintTrans = paintTrans.replace("<ArrayOfCustomPaintTrans>", "<Paints>");
		paintTrans = paintTrans.replace("</ArrayOfCustomPaintTrans>", "</Paints>");
		String vinylTrans = MarshalXML.marshal(customCarTrans.getVinyls());
		vinylTrans = vinylTrans.replace("<ArrayOfCustomVinylTrans>", "<Vinyls>");
		vinylTrans = vinylTrans.replace("</ArrayOfCustomVinylTrans>", "</Vinyls>");
		vinylStorageEntity.setPaintTrans(paintTrans);
		vinylStorageEntity.setVinylTrans(vinylTrans);
		
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
		String str = new Random().ints(9, 0, chars.length()).mapToObj(i -> "" + chars.charAt(i)).collect(Collectors.joining());
		vinylStorageEntity.setCode(str);
		
		vinylStorageDAO.insert(vinylStorageEntity);
		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully uploaded. Vinyl Storage code: " + str), personaId);
	}
	
	public void vinylStorageRemove(Long personaId, String displayName) {
		String entryValue = displayName.replaceFirst("/VINYLREMOVE ", "");
		VinylStorageEntity vinylStorageEntity = vinylStorageDAO.findByCode(entryValue);
		if (vinylStorageEntity == null) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This vinyl code is invaild, re-check and try again."), personaId);
		}
		else {
			boolean isCompatible = true;
			if (!vinylStorageEntity.getPersonaId().equals(personaId)) {
				isCompatible = false;
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unable to remove - you are not the vinyl author."), personaId);
			}
			if (isCompatible) {
				vinylStorageDAO.delete(vinylStorageEntity);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully deleted from the Storage."), personaId);
			}
		}
	}
}
