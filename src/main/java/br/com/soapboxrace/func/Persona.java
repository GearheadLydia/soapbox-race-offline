package br.com.soapboxrace.func;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class Persona {
	
	private Functions fx = new Functions();
	private static String BadgesA = "<BadgePacket><AchievementRankId>0</AchievementRankId><BadgeDefinitionId>0</BadgeDefinitionId><IsRare>true</IsRare><Rarity>0</Rarity><SlotId>";
	private static String BadgesB = "<Badges>" + BadgesA + "0</SlotId></BadgePacket>" + BadgesA + "1</SlotId></BadgePacket>" + BadgesA + "2</SlotId></BadgePacket>" + BadgesA + "3</SlotId></BadgePacket></Badges>";
	private static String InventoryA = "<ProductId>DO NOT USE ME</ProductId><RemainingUseCount>25</RemainingUseCount><ResellPrice>0.00000</ResellPrice><Status>ACTIVE</Status><StringHash>";
	private static String InventoryB = "</StringHash><VirtualItemType>powerup</VirtualItemType></InventoryItemTrans>";

	public void createPersona(String name, String iconindex) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			int personaAm = fx.CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/GetPermanentSession.xml")), StandardCharsets.UTF_8), "<ProfileData>", "</personas>");
			int personaId = (personaAm == 0 ? 100 : Integer.valueOf(doc.getElementsByTagName("PersonaId").item(personaAm - 1).getTextContent()) + 1);
			int boostAm = (personaAm == 0 ? 5000 : Integer.valueOf(doc.getElementsByTagName("Boost").item(0).getTextContent()));
			String sessionId = doc.getElementsByTagName("defaultPersonaIdx").item(0).getTextContent();
			Functions.personaId = String.valueOf(personaId);
			Functions.log(" -->: Creating Persona: " + name + " / ID: " + personaId + ") / Avatar: " + iconindex);
			Node parent = doc.getElementsByTagName("personas").item(0);
			String pData = "<ProfileData>"
					+ "<Boost>" + boostAm + "</Boost>"
					+ "<Cash>200000</Cash>"
					+ "<IconIndex>" + iconindex + "</IconIndex>"
					+ "<Level>1</Level>"
					+ "<Motto/>"
					+ "<Name>" + name + "</Name>"
					+ "<PercentToLevel>0</PercentToLevel>"
					+ "<PersonaId>" + personaId + "</PersonaId>"
					+ "<Rating>0</Rating>"
					+ "<Rep>0</Rep>"
					+ "<RepAtCurrentLevel>0</RepAtCurrentLevel>"
					+ "<ccar i:nil=\"true\"/>"
					+ "</ProfileData>";
			Node fragmentNode = docBuilder.parse(new InputSource(new StringReader(pData))).getDocumentElement();
			fragmentNode = doc.importNode(fragmentNode, true);
			parent.appendChild(fragmentNode);
			if (personaId == 100) {doc.getElementsByTagName("defaultPersonaIdx").item(0).setTextContent("0");} 
			else {doc.getElementsByTagName("defaultPersonaIdx").item(0).setTextContent(String.valueOf(Integer.parseInt(sessionId) + 1));}
			fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/GetPermanentSession.xml");

			Path pathA = Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/cars.xml");
			Files.createDirectories(pathA.getParent()); Files.createFile(pathA);
			Path pathB = Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
			Files.createDirectories(pathB.getParent()); Files.createFile(pathB);
			Path pathC = Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/defaultcar.xml");
			Files.createDirectories(pathC.getParent()); Files.createFile(pathC);
			Path pathD = Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/objects.xml");
			Files.createDirectories(pathD.getParent()); Files.createFile(pathD);
			Path pathE = Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaBaseFromList.xml");
			Files.createDirectories(pathE.getParent()); Files.createFile(pathE);
			Path pathF = Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaInfo.xml");
			Files.createDirectories(pathF.getParent()); Files.createFile(pathF);

	        fx.WriteText("www/soapbox/Engine.svc/personas/" + personaId + "/cars.xml", "<OwnedCarTrans></OwnedCarTrans>");
	        fx.WriteText("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml",
	        		"<CarSlotInfoTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"
	        		+ "<CarsOwnedByPersona></CarsOwnedByPersona>"
	        		+ "<DefaultOwnedCarIndex>0</DefaultOwnedCarIndex>"
	        		+ "<ObtainableSlots><ProductTrans><BundleItems i:nil=\"true\"/><CategoryId i:nil=\"true\"/><Currency>_NS</Currency><Description>New car slot !!</Description>"
	        		+ "<DurationMinute>0</DurationMinute><Hash>-1143680669</Hash><Icon>128_cash</Icon><Level>0</Level><LongDescription>New car slot !!</LongDescription>"
	        		+ "<Price>100.0000</Price><Priority>0</Priority><ProductId>SRV-GARAGESLOT</ProductId><ProductTitle>New car slot !!</ProductTitle><ProductType>CARSLOT</ProductType>"
	        		+ "<SecondaryIcon/><UseCount>1</UseCount><VisualStyle/><WebIcon/><WebLocation/></ProductTrans></ObtainableSlots><OwnedCarSlotsCount>5</OwnedCarSlotsCount>"
	        		+ "</CarSlotInfoTrans>");
	        fx.WriteText("www/soapbox/Engine.svc/personas/" + personaId + "/defaultcar.xml", "<OwnedCarTrans></OwnedCarTrans>");
	        fx.WriteText("www/soapbox/Engine.svc/personas/" + personaId + "/objects.xml",
	        		"<InventoryTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><InventoryItems>"
	        		+ "<InventoryItemTrans><EntitlementTag>runflattires</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-537557654</Hash><InventoryId>1</InventoryId>" + InventoryA + "0xdff5856a" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>trafficmagnet</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>125509666</Hash><InventoryId>2</InventoryId>" + InventoryA + "0x77b2022" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>instantcooldown</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-1692359144</Hash><InventoryId>3</InventoryId>" + InventoryA + "0x9b20a618" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>shield</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-364944936</Hash><InventoryId>4</InventoryId>" + InventoryA + "0xea3f61d8" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>slingshot</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>2236629</Hash><InventoryId>5</InventoryId>" + InventoryA + "0x2220d5" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>ready</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>957701799</Hash><InventoryId>6</InventoryId>" + InventoryA + "0x39155ea7" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>juggernaut</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>1805681994</Hash><InventoryId>7</InventoryId>" + InventoryA + "0x6ba0854a" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>emergencyevade</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-611661916</Hash><InventoryId>8</InventoryId>" + InventoryA + "0xdb8ac7a4" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>team_emergencyevade</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-1564932069</Hash><InventoryId>9</InventoryId>" + InventoryA + "0xa2b9081b" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>nosshot</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-1681514783</Hash><InventoryId>10</InventoryId>" + InventoryA + "0x9bc61ee1" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>onemorelap</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>1627606782</Hash><InventoryId>11</InventoryId>" + InventoryA + "0x61034efe" + InventoryB
	        		+ "<InventoryItemTrans><EntitlementTag>team_slingshot</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>1113720384</Hash><InventoryId>12</InventoryId>" + InventoryA + "0x42620640" + InventoryB
	        		+ "</InventoryItems>"
	        		+ "<PerformancePartsCapacity>150</PerformancePartsCapacity><PerformancePartsUsedSlotCount>0</PerformancePartsUsedSlotCount>"
	        		+ "<SkillModPartsCapacity>200</SkillModPartsCapacity><SkillModPartsUsedSlotCount>0</SkillModPartsUsedSlotCount>"
	        		+ "<VisualPartsCapacity>300</VisualPartsCapacity><VisualPartsUsedSlotCount>0</VisualPartsUsedSlotCount>"
	        		+ "</InventoryTrans>");
	        fx.WriteText("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaBaseFromList.xml",
	        		"<ArrayOfPersonaBase xmlns=\"http://schemas.datacontract.org/2004/07/Victory.Service.Objects\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"
	        		+ "<PersonaBase>" + BadgesB
	        		+ "<IconIndex>" + iconindex + "</IconIndex>"
	        		+ "<Level>1</Level>"
	        		+ "<Motto/>"
	        		+ "<Name>" + name + "</Name>"
	        		+ "<PersonaId>" + personaId + "</PersonaId>"
	        		+ "<Presence>1</Presence>"
	        		+ "<Score>0</Score>"
	        		+ "<UserId>11111111</UserId></PersonaBase>"
	        		+ "</ArrayOfPersonaBase>");
	        fx.WriteText("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaInfo.xml",
	        		"<ProfileData xmlns=\"http://schemas.datacontract.org/2004/07/Victory.Service.Objects\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"	+ BadgesB
	        		+ "<Cash>200000</Cash>"
	        		+ "<IconIndex>" + iconindex + "</IconIndex>"
	        		+ "<Level>1</Level>"
	        		+ "<Motto/>"
	        		+ "<Name>" + name + "</Name>"
	        		+ "<PercentToLevel>0</PercentToLevel>"
	        		+ "<PersonaId>" + personaId + "</PersonaId>"
	        		+ "<Rating>0</Rating>"
	        		+ "<Rep>0</Rep>"
	        		+ "<RepAtCurrentLevel>0</RepAtCurrentLevel>"
	        		+ "<Score>0</Score>"
	        		+ "</ProfileData>");
			Functions.answerData = pData;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deletePersona(String personaId) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			int index = fx.CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/GetPermanentSession.xml", new String[0])), StandardCharsets.UTF_8), "<ProfileData>", "<PersonaId>" + personaId + "</PersonaId>");
			Functions.log(" -->: Deleting Persona with ID: " + personaId);
			Node persona = doc.getElementsByTagName("ProfileData").item(index - 1);
			doc.getElementsByTagName("personas").item(0).removeChild(persona);
			doc.getElementsByTagName("defaultPersonaIdx").item(0).setTextContent("0");
			fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
		    File x = new File("www/soapbox/Engine.svc/personas/" + personaId); for (File file : x.listFiles()) {file.delete();} x.delete();
			Functions.answerData = "<long>0</long>";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}