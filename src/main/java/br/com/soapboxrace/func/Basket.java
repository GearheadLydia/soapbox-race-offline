package br.com.soapboxrace.func;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class Basket {

	private Functions fx = new Functions();

	public void processBasket(String basketTrans) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String basketId = fx.parseBasketId(basketTrans);
			Functions.log(" -->: Basket action detected.");
			if (basketId.contains("SRV-CAR")) {
				int a = Integer.valueOf(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("www/soapbox/Engine.svc/personas/"+ Functions.personaId +"/carslots.xml").getElementsByTagName("OwnedCarSlotsCount").item(0).getTextContent());
				int b = fx.CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml")), StandardCharsets.UTF_8), "<OwnedCarTrans>", "</CarsOwnedByPersona>");
				if (a <= b) {
					Functions.log(" -->: Your Garage is full. Please buy an new Carslot first.");
					Functions.answerData = "<CommerceResultTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://../.w3.org/2001/XMLSchema-instance\"><CommerceItems /><InvalidBasket i:nil=\"true\" /><InventoryItems><InventoryItemTrans/></InventoryItems><PurchasedCars/><Status>Fail_InsufficientCarSlots</Status><Wallets/></CommerceResultTrans>";
					return;
				}
			}
			Functions.log(" -->: Initializing economy module...");
			Economy economy = new Economy(mapBasketId(basketId), basketId, false);
			if (mapBasketId(basketId) == null)
				return;
			if (economy.transCurrency(true)) {
				if (basketId.contains("SRV-GARAGESLOT")) {
					fx.AddCarslot();
				} else if (basketId.contains("SRV-REPAIR")) {
					Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml"));
					doc.getElementsByTagName("Durability").item(Integer.parseInt(fx.ReadCarIndex())).setTextContent("100");
					fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml");
					Functions.log(" -->: The car has been repaired.");
				} else if (basketId.contains("SRV-POWERUP")) {
					int index = Integer.parseInt(basketId.replace("SRV-POWERUP", ""));
					Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/objects.xml"));
					String newAmount = String.valueOf(Integer.parseInt(doc.getElementsByTagName("RemainingUseCount").item(index).getTextContent()) + 15);
					doc.getElementsByTagName("RemainingUseCount").item(index).setTextContent(newAmount);
					fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/" + Functions.personaId + "/objects.xml");
				} else if (basketId.contains("SRV-THREVIVE")) {
					Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml"));
					doc.getElementsByTagName("IsStreakBroken").item(0).setTextContent("false");
					fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml");
				} else {
					if (Files.exists(Paths.get("www/basket/" + basketId + ".xml"))) {
						Functions.log(" -->: Purchase for car " + basketId + " was successful.");
						fx.FixCarslots();
						Document docShop = docBuilder.parse(new File("www/soapbox/Engine.svc/catalog/products_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml"));
						int basketIdVal = Integer.parseInt(basketId.replaceAll("SRV-CAR", ""));
						int price = Integer.parseInt(docShop.getElementsByTagName("Price").item(basketIdVal).getTextContent());
						Functions.log(" -->: New Car was bought for " + price + " IGC.");
						String resellPrice = String.valueOf(price / 2);
						Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml"));
						int lastIdIndex = doc.getElementsByTagName("Id").getLength() - 1;
						String carId = "1";
						if (lastIdIndex <= 0) {lastIdIndex = 0; carId = "1";} else {carId = String.valueOf(Integer.parseInt(doc.getElementsByTagName("Id").item(lastIdIndex).getTextContent()) + 1);}
						Document doc2 = docBuilder.parse(new File("www/basket/" + basketId + ".xml"));
						doc2.getElementsByTagName("Id").item(1).setTextContent(carId);
						doc2.getElementsByTagName("ResalePrice").item(0).setTextContent(resellPrice);
						Node carTrans = doc.importNode(doc2.getFirstChild(), true);
						doc.getElementsByTagName("CarsOwnedByPersona").item(0).appendChild(carTrans);
						int _carId = Integer.parseInt(carId) - 1;
						doc.getElementsByTagName("DefaultOwnedCarIndex").item(0).setTextContent(String.valueOf(_carId));
						fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml");
						fx.WriteTempCar(new String(Files.readAllBytes(Paths.get("www/basket/" + basketId + ".xml")), StandardCharsets.UTF_8));
						Functions.log(" -->: The car [ID = " + carId + "; Index = " + _carId + "] has been bought and set.");
					} else {
						Functions.log(" -->: Basket not found: " + basketId);
					}
				}
			} else {
				Functions.answerData = "<CommerceResultTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://../.w3.org/2001/XMLSchema-instance\"><CommerceItems/><InvalidBasket i:nil=\"true\" /><InventoryItems><InventoryItemTrans/></InventoryItems><PurchasedCars/><Status>Fail_InsufficientFunds</Status><Wallets/></CommerceResultTrans>";
				Functions.log(" -->: You do not have enough currency!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void SellCar(String serialNumber) {
		if (serialNumber != null) {
			try {
				Functions.log(" -->: Sell Car action detected.");
				int carId = fx.CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml")), StandardCharsets.UTF_8), "<OwnedCarTrans>", "<Id>" + serialNumber + "</Id>") - 1;
				int carAm = fx.CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml")), StandardCharsets.UTF_8), "<OwnedCarTrans>", "</CarsOwnedByPersona>");
				if (carAm > 1) {
					Functions.log(" -->: More than 1 car is found, can proceed.");
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml");
					doc.getElementsByTagName("DefaultOwnedCarIndex").item(0).setTextContent("0");
					Functions.log(" -->: Initializing economy module...");
					Economy economy = new Economy(doc.getElementsByTagName("ResalePrice").item(carId).getTextContent(), "0", true);
					Functions.log(" -->: " + doc.getElementsByTagName("ResalePrice").item(carId).getTextContent() + " IGC to be added to your balance.");
					economy.transCurrency(false);
					Node carToSell = doc.getElementsByTagName("OwnedCarTrans").item(carId);
					doc.getElementsByTagName("CarsOwnedByPersona").item(0).removeChild(carToSell);
					Functions.log(" -->: The car has been removed from carslots.");
					Node OwnedCar = doc.getElementsByTagName("OwnedCarTrans").item(0);
					DOMImplementationLS lsImpl = (DOMImplementationLS) OwnedCar.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
					LSSerializer serializer = lsImpl.createLSSerializer();
					serializer.getDomConfig().setParameter("xml-declaration", false);
					String StringOwnedCar = serializer.writeToString(OwnedCar);
					fx.WriteTempCar(StringOwnedCar);
					fx.WriteXML(doc, "www/soapbox/Engine.svc/personas/" + Functions.personaId + "/carslots.xml");
					Functions.log(" -->: The car has been sold.");
				} else {
					Functions.log(" -->: The car is the last car, can't be sold.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String mapBasketId(String basketId) {
		if (basketId.contains("SRV-POWERUP")) {
			return "Powerups";
		} else if (basketId.contains("SRV-GARAGESLOT")) {
			return "CarSlot";
		} else if (basketId.contains("SRV-REPAIR")) {
			return "CarRepair";
		} else if (basketId.contains("SRV-THREVIVE")) {
			return "StreakRecovery";
		} else if (basketId.contains("SRV-CAR")) {
			return "www/soapbox/Engine.svc/catalog/products_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml";
		}
		return null;
	}
}
