package br.com.soapboxrace.func;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import br.com.soapboxrace.srv.HttpSrv;

public class Economy {

	private Functions fx = new Functions();

	public int IGC = 0;
	public int Boost = 1;

	public static int amount;
	public static int type;

	private int[] totalPrice = new int[] { 0, 0 };

	public boolean transCurrency(boolean remove) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			int sessionId = Integer.parseInt(doc.getElementsByTagName("defaultPersonaIdx").item(0).getTextContent());
			int personas = fx.CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/GetPermanentSession.xml")), StandardCharsets.UTF_8), "<ProfileData>", "</personas>");
			int newAmount = 0;
			if (type == IGC) {
				Document doc2 = docBuilder.parse("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/GetPersonaInfo.xml");
				newAmount = Integer.parseInt(doc2.getElementsByTagName("Cash").item(0).getTextContent());
				if (remove) {
					newAmount = newAmount - amount;
					if (newAmount < 0)
						return false;
				} else {
					newAmount = newAmount + amount;
					if (newAmount > 999999999) {
						newAmount = 999999999;
					}
				}
				doc.getElementsByTagName("Cash").item(sessionId).setTextContent(String.valueOf(newAmount));
				doc2.getElementsByTagName("Cash").item(0).setTextContent(String.valueOf(newAmount));
				fx.WriteXML(doc2, "www/soapbox/Engine.svc/personas/" + Functions.personaId + "/GetPersonaInfo.xml");
			} else if (type == Boost) {
				newAmount = Integer.parseInt(doc.getElementsByTagName("Boost").item(sessionId).getTextContent());
				if (remove) {
					newAmount = newAmount - amount;
					if (newAmount < 0)
						return false;
				} else {
					newAmount = newAmount + amount;
					if (newAmount > 999999999) {
						newAmount = 999999999;
					}
				}
				for (int i = 0; i < personas; i++) {
					doc.getElementsByTagName("Boost").item(i).setTextContent(String.valueOf(newAmount));
				}
			}
			amount = newAmount;
			fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			if (HttpSrv.modifiedTarget == "baskets")
				Functions.answerData = "<CommerceResultTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://../.w3.org/2001/XMLSchema-instance\"><CommerceItems /><InvalidBasket i:nil=\"true\" /><InventoryItems><InventoryItemTrans/></InventoryItems><PurchasedCars><OwnedCarTrans><CustomCar><BaseCar>1008844988</BaseCar><CarClassHash>415909161</CarClassHash><Id>120</Id><IsPreset>true</IsPreset><Level>0</Level><Name/><Paints/><PerformanceParts/><PhysicsProfileHash>-1550817422</PhysicsProfileHash><Rating>308</Rating><ResalePrice>0</ResalePrice><RideHeightDrop>0.75</RideHeightDrop><SkillModParts /><SkillModSlotCount>5</SkillModSlotCount><Version>0</Version><Vinyls/><VisualParts/></CustomCar><Durability>100</Durability><ExpirationDate i:nil=\"true\" /><Heat>1</Heat><Id>72861440</Id><OwnershipType>PresetCar</OwnershipType></OwnedCarTrans></PurchasedCars><Status>Success</Status><Wallets><WalletTrans><Balance>"
						+ String.valueOf(newAmount) + "</Balance><Currency>" + (type == IGC ? "CASH" : "BOOST") + "</Currency></WalletTrans></Wallets></CommerceResultTrans>";
			Functions.log(" -->: Your new" + (type == IGC ? " IGC " : " Boost ") + "balance is " + String.valueOf(newAmount) + ".");
			totalPrice = new int[] { 0, 0 };
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Economy() {}

	public Economy(String catalogName, String productId, Boolean type2) {
		try {
			if (catalogName == null || productId == null) {
				Functions.log(" -->: Support for this product has not been added, yet.");
				return;
			}
			if (type2) {
				amount = Double.valueOf(catalogName).intValue();
				type = Integer.parseInt(productId);
			} else {
			int catID;
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document catalog;
				if (catalogName.contains("www")){
					catID = fx.CountInstances(new String(Files.readAllBytes(Paths.get(catalogName)), StandardCharsets.UTF_8), "<ProductTrans>", "<ProductId>" + productId + "</ProductId>") - 1;
					catalog = docBuilder.parse(catalogName);
				} else {
					catID = fx.CountInstances((
							catalogName == "Amplifiers" ? Constants.Amplifiers :
							catalogName == "Powerups" ? Constants.Powerups :
							catalogName == "CarSlot" ? Constants.CarSlot :
							catalogName == "CarRepair" ? Constants.repairPrice(fx.calcRepair()) :
							catalogName == "StreakRecovery" ? Constants.StreakRecovery :
							catalogName == "PaintsBody" ? Constants.PaintsBody :
							catalogName == "PaintsWheel" ? Constants.PaintsWheel : null), "<ProductTrans>", "<ProductId>" + productId + "</ProductId>") - 1;
					catalog = docBuilder.parse(new InputSource(new StringReader((
							catalogName == "Amplifiers" ? Constants.Amplifiers :
							catalogName == "Powerups" ? Constants.Powerups :
							catalogName == "CarSlot" ? Constants.CarSlot :
							catalogName == "CarRepair" ? Constants.repairPrice(fx.calcRepair()) :
							catalogName == "StreakRecovery" ? Constants.StreakRecovery :
							catalogName == "PaintsBody" ? Constants.PaintsBody :
							catalogName == "PaintsWheel" ? Constants.PaintsWheel : null))));
				}
				amount = Double.valueOf(catalog.getElementsByTagName("Price").item(catID).getTextContent()).intValue();
				type = catalog.getElementsByTagName("Currency").item(catID).getTextContent().equals("CASH") ? IGC : Boost;
				String productTitle = catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent().isEmpty() ? "'Unknown Item'" : catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent();
				Functions.log(" -->: Purchasing package " + productTitle + " for " + String.valueOf(amount) + (type == IGC ? " IGC" : " Boost") + ".");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void AddCommerce(String catalogName, String productId) {
		try {
			if (catalogName == null || productId == null) {
				Functions.log(" -->: Support for this product has not been added, yet.");
				return;
			}
			int catID;
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document catalog;
			if (catalogName.contains("www")){
				catID = fx.CountInstances(new String(Files.readAllBytes(Paths.get(catalogName)), StandardCharsets.UTF_8), "<ProductTrans>", "<ProductId>" + productId + "</ProductId>") - 1;
				catalog = docBuilder.parse(catalogName);
			} else {
				catID = fx.CountInstances((
						catalogName == "Amplifiers" ? Constants.Amplifiers :
						catalogName == "Powerups" ? Constants.Powerups :
						catalogName == "CarSlot" ? Constants.CarSlot :
						catalogName == "CarRepair" ? Constants.repairPrice(fx.calcRepair()) :
						catalogName == "StreakRecovery" ? Constants.StreakRecovery :
						catalogName == "PaintsBody" ? Constants.PaintsBody :
						catalogName == "PaintsWheel" ? Constants.PaintsWheel : null), "<ProductTrans>", "<ProductId>" + productId + "</ProductId>") - 1;
				catalog = docBuilder.parse(new InputSource(new StringReader((
						catalogName == "Amplifiers" ? Constants.Amplifiers :
						catalogName == "Powerups" ? Constants.Powerups :
						catalogName == "CarSlot" ? Constants.CarSlot :
						catalogName == "CarRepair" ? Constants.repairPrice(fx.calcRepair()) :
						catalogName == "StreakRecovery" ? Constants.StreakRecovery :
						catalogName == "PaintsBody" ? Constants.PaintsBody :
						catalogName == "PaintsWheel" ? Constants.PaintsWheel : null))));
			}
			amount = Double.valueOf(catalog.getElementsByTagName("Price").item(catID).getTextContent()).intValue();
			type = catalog.getElementsByTagName("Currency").item(catID).getTextContent().equals("CASH") ? IGC : Boost;
			totalPrice[type == IGC ? 0 : 1] += Double.valueOf(catalog.getElementsByTagName("Price").item(catID).getTextContent()).intValue();
			String productTitle = catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent().isEmpty() ? "Unknown Item" : catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent();
			Functions.log(" -->: Added package " + productTitle + " for " + String.valueOf(amount) + (type == IGC ? " IGC" : " Boost") + " to the shopping list.");
			Functions.log(" -->: Total Price: " + String.valueOf(totalPrice[0]) + " IGC and " + String.valueOf(totalPrice[1]) + " Boost");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean DoCommerce() {
		if (totalPrice[0] > 0) {
			type = IGC;
			amount = totalPrice[0];
			if (!transCurrency(true))
			return false;
		}
		if (totalPrice[1] > 0) {
			type = Boost;
			amount = totalPrice[1];
			if (!transCurrency(true))
			return false;
		}
		return true;
	}

	public void AddPartToSell(String price) {
		totalPrice[0] += (int) Math.round(Double.valueOf(price));
		Functions.log(" -->: Added 1 part for " + String.valueOf((int) Math.round(Double.valueOf(price))) + " IGC.");
		return;
	}

	public void SellParts() {
		if (totalPrice[0] != 0) {
			Functions.log(" -->: " + String.valueOf(totalPrice[0]) + " IGC to be added to your balance.");
			type = IGC;
			amount = totalPrice[0];
			transCurrency(false);
		}
	}
}
