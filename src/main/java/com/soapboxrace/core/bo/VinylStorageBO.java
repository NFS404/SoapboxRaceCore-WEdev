package com.soapboxrace.core.bo;

import java.security.SecureRandom;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.OwnedCarConverter;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.FriendListDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.dao.VinylStorageDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.jpa.VinylStorageEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.util.JAXBUtility;

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
	
	@EJB
	private UserDAO userDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;

	// Applies the vinyls from DB, uses OwnedCarTrans as a blank for already existed scripts (Not the ideal way...)
	public void vinylStorageApply(Long personaId, String displayName) {
		String entryValue = displayName.replaceFirst("/VINYL ", "");
		VinylStorageEntity vinylStorageEntity = vinylStorageDAO.findByCode(entryValue);
		if (vinylStorageEntity == null) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This vinyl code is invaild, re-check and try again."), personaId);
		}
		else {
			CarSlotEntity defaultCarEntity = personaBO.getDefaultCarEntity(personaId);
			int currentCarHash = defaultCarEntity.getOwnedCar().getCustomCar().getPhysicsProfileHash();
			int vinylCarHash = vinylStorageEntity.getCarHash();
			boolean isCompatible = true;
			int baseProfileHash = 0; // If player is with Drift-Spec vehicle, he still will be able to apply the car model's vinyl
			CarClassesEntity carClassesEntity = carClassesDAO.findByHash(currentCarHash);
			if (carClassesEntity.getBaseProfile() != null) {
				CarClassesEntity carBaseEntity = carClassesDAO.findByStoreName(carClassesEntity.getBaseProfile());
				baseProfileHash = carBaseEntity.getHash();
			}
			if (baseProfileHash != vinylCarHash && vinylCarHash != currentCarHash) {
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
				
				OwnedCarTrans OwnedVinylTrans = JAXBUtility.unMarshal(transFormEnd, OwnedCarTrans.class);
				
				CustomCarTrans customCarTrans = OwnedVinylTrans.getCustomCar();
					
				commerceBO.updateCarVinyl(customCarTrans, defaultCarEntity);
				vinylStorageEntity.setAppliedCount(vinylStorageEntity.getAppliedCount() + 1);
				vinylStorageDAO.update(vinylStorageEntity);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully applied to your current car. Go to the garage to check out the livery."), personaId);
			}
		}
	}
	
	public void vinylStorageUpload(Long personaId) {
		int vinylSlotsLimit = parameterBO.getIntParam("VINYLSTORAGE_SLOTS");
		UserEntity userEntity = personaDAO.findById(personaId).getUser();
		boolean isPremium = userEntity.isPremium();
		int vinylSlotsUsed = userEntity.getVinylSlotsUsed();
		if (!isPremium && vinylSlotsUsed >= vinylSlotsLimit) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You are is reached the maximum of Vinyl Storage slots. Remove some of them or got the Premium to increase the slots."), personaId);
		}
		else {
			if (!isPremium) {
				userEntity.setVinylSlotsUsed(vinylSlotsUsed + 1);
				userDAO.update(userEntity);
			}
			VinylStorageEntity vinylStorageEntity = new VinylStorageEntity();
			OwnedCarEntity ownedCarEntity = personaBO.getDefaultCarEntity(personaId).getOwnedCar();
			CustomCarEntity defaultCarEntity = ownedCarEntity.getCustomCar();
			OwnedCarTrans ownedCarTrans = OwnedCarConverter.entity2Trans(personaBO.getDefaultCarEntity(personaId).getOwnedCar());
			CustomCarTrans customCarTrans = ownedCarTrans.getCustomCar();
			
			vinylStorageEntity.setUserId(userEntity.getId());
			vinylStorageEntity.setAppliedCount(0);
			vinylStorageEntity.setCarHash(defaultCarEntity.getPhysicsProfileHash());
			
			String paintTrans = JAXBUtility.marshal(customCarTrans.getPaints());
			paintTrans = paintTrans.replace("<ArrayOfCustomPaintTrans>", "<Paints>");
			paintTrans = paintTrans.replace("</ArrayOfCustomPaintTrans>", "</Paints>");
			String vinylTrans = JAXBUtility.marshal(customCarTrans.getVinyls());
			vinylTrans = vinylTrans.replace("<ArrayOfCustomVinylTrans>", "<Vinyls>");
			vinylTrans = vinylTrans.replace("</ArrayOfCustomVinylTrans>", "</Vinyls>");
			vinylStorageEntity.setPaintTrans(paintTrans);
			vinylStorageEntity.setVinylTrans(vinylTrans);
			
			String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
			String str = new SecureRandom().ints(9, 0, chars.length()).mapToObj(i -> "" + chars.charAt(i)).collect(Collectors.joining());
			vinylStorageEntity.setCode(str);
			
			vinylStorageDAO.insert(vinylStorageEntity);
			if (isPremium) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully uploaded. Vinyl Storage code: " + str), personaId);
			}
			else {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully uploaded (" + 
						userEntity.getVinylSlotsUsed() + "/" + vinylSlotsLimit + " used). Vinyl Storage code: " + str), personaId);
			}
		}
	}
	
	public void vinylStorageRemove(Long personaId, String displayName) {
		UserEntity userEntity = personaDAO.findById(personaId).getUser();
		String entryValue = displayName.replaceFirst("/VINYLREMOVE ", "");
		VinylStorageEntity vinylStorageEntity = vinylStorageDAO.findByCode(entryValue);
		if (vinylStorageEntity == null) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This vinyl code is invaild, re-check and try again."), personaId);
		}
		else {
			boolean isCompatible = true;
			if (!vinylStorageEntity.getUserId().equals(userEntity.getId())) {
				isCompatible = false;
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unable to remove - you are not the vinyl author."), personaId);
			}
			if (isCompatible) {
				userEntity.setVinylSlotsUsed(userEntity.getVinylSlotsUsed() - 1);
				userDAO.update(userEntity);
				vinylStorageDAO.delete(vinylStorageEntity);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Vinyl is successfully deleted from the Storage."), personaId);
			}
		}
	}
	
	public void vinylStorageRemoveAll(Long personaId) {
		UserEntity userEntity = personaDAO.findById(personaId).getUser();
		vinylStorageDAO.deleteAllVinyls(userEntity.getId());
		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your Vinyl Storage has been wiped."), personaId);
	}
}
