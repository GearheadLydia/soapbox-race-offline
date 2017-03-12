package br.com.soapboxrace.func;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.soapboxrace.srv.HttpSrv;
import br.com.soapboxrace.xmpp.XmppSrv;

public class Functions {
	private static Random rand = new Random();
	public static String personaId = "100";
	public static String answerData = null;
	private static javax.swing.JTextArea logTextArea;

	public static void setLogTextArea(javax.swing.JTextArea logTextArea) {
		Functions.logTextArea = logTextArea;
	}

	public static int[][] rankDrop = new int[][] { new int[] {}, new int[] { 1, 0, 3, 2, 0, -1, 1, 2, 3, 0 },
			new int[] { 1, 0, 0, 2, 0, -1, 1, 2, 0, 0 }, new int[] { 1, 0, 0, 1, 0, -1, 1, 1, 0, 0 } };
	public static int[] rewards = new int[] { 20000, 250, 1000, 500, 1000 };
	public static double[] multipliers = new double[] { 0.5, 1.0, 1.45 };

	public String ReadCarIndex() throws ParserConfigurationException, SAXException, IOException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml")
				.getElementsByTagName("DefaultOwnedCarIndex").item(0).getTextContent();
	}

	public void ChangeCarIndex(String carId, Boolean literal) {
		try {
			String carIndex;
			if (literal) {
				carIndex = carId;
			} else {
				carIndex = String.valueOf(CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml")), StandardCharsets.UTF_8), "<OwnedCarTrans>", "<Id>" + carId + "</Id>") - 1);
			}
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
			doc.getElementsByTagName("DefaultOwnedCarIndex").item(0).setTextContent(carIndex);
			log(" -->: Car Index has been set to " + carIndex);
			Node OwnedCar = doc.getElementsByTagName("OwnedCarTrans").item(Integer.parseInt(carIndex));
			DOMImplementationLS lsImpl = (DOMImplementationLS) OwnedCar.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
			LSSerializer serializer = lsImpl.createLSSerializer();
			serializer.getDomConfig().setParameter("xml-declaration", false);
			String StringOwnedCar = serializer.writeToString(OwnedCar);
			WriteTempCar(StringOwnedCar);
			WriteXML(doc, "www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void DoChatSpam(){
		try {
			String spam = "";
			boolean forward = true;
			for (int i = 0; i <= 10; i++) {
				int maxlength = rand.nextInt(21)+5;
				while (forward) {
					spam += "=";
					Thread.sleep(50);
					sendChat(spam);
					if (spam.length() == maxlength) forward = false;
				}
				while (!forward) {
					spam = spam.substring(0, spam.length()-1);
					Thread.sleep(50);
					sendChat(spam);
					if (spam.length() == 1) forward = true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void FixCarslots() {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
			int _carId = 0;
			int ids = CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml")), StandardCharsets.UTF_8), "<Id>", "</CarsOwnedByPersona>") - 1;
			for (int i = 0; i <= ids; i++) {
				if (i % 2 != 0) {
					_carId++;
					doc.getElementsByTagName("Id").item(i).setTextContent(String.valueOf(_carId));
				}
			}
			WriteXML(doc, "www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void ChangeDefaultPersona(String persona) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			int index = CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/GetPermanentSession.xml")), StandardCharsets.UTF_8), "<ProfileData>", "<PersonaId>"+ persona +"</PersonaId>") - 1;
			log(" -->: Setting default Persona Index to " + index + ".");
			doc.getElementsByTagName("defaultPersonaIdx").item(0).setTextContent(String.valueOf(index));
			WriteXML(doc, "www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void AddCarslot() {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml"));
			doc.getElementsByTagName("OwnedCarSlotsCount").item(0).setTextContent(String.valueOf(Integer.parseInt(doc.getElementsByTagName("OwnedCarSlotsCount").item(0).getTextContent()) + 1));
			WriteXML(doc, "www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
			log(" -->: Carslot amount increased. [+1]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processDurability() {
		try{
			Document docSlot = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml"));
			int carId = Integer.parseInt(docSlot.getElementsByTagName("DefaultOwnedCarIndex").item(0).getTextContent());
			int current = Integer.parseInt(docSlot.getElementsByTagName("Durability").item(carId).getTextContent());
			if (current >= 4) {
				docSlot.getElementsByTagName("Durability").item(carId).setTextContent(String.valueOf(current - 4));
			} else {
				docSlot.getElementsByTagName("Durability").item(carId).setTextContent("0");
			}
			WriteXML(docSlot, "www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml");
			log(" -->: Your Car lost 4% Durability.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPersonaMotto(String MottoPacket) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(MottoPacket)));
			String message = doc.getElementsByTagName("message").item(0).getTextContent();
			String persona = doc.getElementsByTagName("personaId").item(0).getTextContent();
			Document doc0 = docBuilder.parse("www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			Document doc1 = docBuilder.parse("www/soapbox/Engine.svc/personas/" + persona + "/GetPersonaInfo.xml");
			Document doc2 = docBuilder.parse("www/soapbox/Engine.svc/personas/" + persona + "/GetPersonaBaseFromList.xml");
			int index = CountInstances(new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/personas/GetPermanentSession.xml")), StandardCharsets.UTF_8), "<ProfileData>", "<PersonaId>"+ persona +"</PersonaId>") - 1;
			doc0.getElementsByTagName("Motto").item(index).setTextContent(message);
			doc1.getElementsByTagName("Motto").item(0).setTextContent(message);
			doc2.getElementsByTagName("Motto").item(0).setTextContent(message);
			WriteXML(doc0, "www/soapbox/Engine.svc/personas/GetPermanentSession.xml");
			WriteXML(doc1, "www/soapbox/Engine.svc/personas/" + persona + "/GetPersonaInfo.xml");
			WriteXML(doc2, "www/soapbox/Engine.svc/personas/" + persona + "/GetPersonaBaseFromList.xml");
			log(" -->: Motto of Persona "+ persona +" has been set to: "+ message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void WriteTempCar(String carData) {
		if ("" != carData) {
			BufferedWriter bw;
			try {
				// defaultcar
				bw = new BufferedWriter(
				new FileWriter(new File("www/soapbox/Engine.svc/personas/" + personaId + "/defaultcar.xml").getAbsoluteFile()));
				bw.write(carData);
				bw.close();
				// cars
				String carsW = carData.replace("<OwnedCarTrans>","<OwnedCarTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://../.w3.org/2001/XMLSchema-instance\">");
				bw = new BufferedWriter(new FileWriter(new File("www/soapbox/Engine.svc/personas/" + personaId + "/cars.xml").getAbsoluteFile()));
				bw.write(carsW);
				bw.close();
				// commerce
				if (HttpSrv.modifiedTarget == "commerce") {
					String commerceW = carData.replace("<OwnedCarTrans>", "<UpdatedCar>").replace("</OwnedCarTrans>","</UpdatedCar>");
					Functions.answerData = "<CommerceSessionResultTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://../.w3.org/2001/XMLSchema-instance\"><InvalidBasket i:nil=\"true\"/><InventoryItems><InventoryItemTrans><EntitlementTag>SPOILER_STYLE01_LARGE</EntitlementTag><ExpirationDate i:nil=\"true\"/><Hash>-232471336</Hash><InventoryId>2898928898</InventoryId><ProductId>DO NOT USE ME</ProductId><RemainingUseCount>1</RemainingUseCount><ResellPrice>0.00000</ResellPrice><Status>ACTIVE</Status><StringHash>0xf224c4d8</StringHash><VirtualItemType>visualpart</VirtualItemType></InventoryItemTrans></InventoryItems><Status>Success</Status>"
							+ commerceW + "<Wallets><WalletTrans><Balance>" + String.valueOf(Economy.amount)
							+ "</Balance><Currency>" + (Economy.type == 0 ? "CASH" : "BOOST")
							+ "</Currency></WalletTrans></Wallets></CommerceResultTrans>";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void ChangeBadges(String BadgesPacket) {
		try {
			log(" -->: Change Badges action detected.");
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document packet = docBuilder.parse(new InputSource(new StringReader(BadgesPacket)));
			Document achievements = docBuilder.parse(new InputSource(new StringReader(Constants.AchievementA + Constants.AchievementB)));
			Document doc = docBuilder.parse("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaBaseFromList.xml");
			Document doc2 = docBuilder.parse("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaInfo.xml");
			log(" -->: 4 documents have been loaded into memory.");
			int loopA = CountInstances(BadgesPacket, "<SlotId>", "</BadgeBundle>");
			log(" -->: Amount of new badges: " + String.valueOf(loopA) + ". Starting loop to rewrite badge data.");
			for (int i = 1; i <= loopA; i++) {
				int slotId = Integer.parseInt(packet.getElementsByTagName("SlotId").item(i - 1).getTextContent());
				String badgeId = packet.getElementsByTagName("BadgeDefinitionId").item(i - 1).getTextContent();
				int achId = CountInstances((Constants.AchievementA + Constants.AchievementB), "<BadgeDefinitionId>" + badgeId + "</BadgeDefinitionId>", "</Badges>") - 1;
				String rankId = achievements.getElementsByTagName("AchievementRanks").item(achId).getLastChild().getChildNodes().item(1).getTextContent();
				String isRare = achievements.getElementsByTagName("AchievementRanks").item(achId).getLastChild().getChildNodes().item(2).getTextContent();
				String rarity = achievements.getElementsByTagName("AchievementRanks").item(achId).getLastChild().getChildNodes().item(5).getTextContent();
				log(" -->: New badge data ->  RankID: " + rankId + ", isRare: " + isRare + ", Rarity: " + rarity + ".");
				doc.getElementsByTagName("AchievementRankId").item(slotId).setTextContent(rankId);
				doc.getElementsByTagName("BadgeDefinitionId").item(slotId).setTextContent(badgeId);
				doc.getElementsByTagName("IsRare").item(slotId).setTextContent(isRare);
				doc.getElementsByTagName("Rarity").item(slotId).setTextContent(rarity);
				doc2.getElementsByTagName("AchievementRankId").item(slotId).setTextContent(rankId);
				doc2.getElementsByTagName("BadgeDefinitionId").item(slotId).setTextContent(badgeId);
				doc2.getElementsByTagName("IsRare").item(slotId).setTextContent(isRare);
				doc2.getElementsByTagName("Rarity").item(slotId).setTextContent(rarity);
				log(" -->: New badge data written.");
			}
			WriteXML(doc, "www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaBaseFromList.xml");
			WriteXML(doc2, "www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaInfo.xml");
			log(" -->: Change Badges action finalized.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void StartNewTH(boolean isCompleted) {
		try {
			Functions.log(" -->: Generating new TH.");
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml"));
			Random random = new Random();
			int seed = random.nextInt(7);

			if (isCompleted && doc.getElementsByTagName("CoinsCollected").item(0).getTextContent().equals("1073741823")) { // On Streak
				doc.getElementsByTagName("Seed").item(0).setTextContent(String.valueOf(seed));
				doc.getElementsByTagName("CoinsCollected").item(0).setTextContent("0");
				WriteXML(doc, "www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml");
				log(" -->: A new TH has been generated.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void SaveTHProgress(String coins) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new File("www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml"));
			doc.getElementsByTagName("CoinsCollected").item(0).setTextContent(coins);
			if (coins.equals("1073741823"))
			doc.getElementsByTagName("Streak").item(0).setTextContent(String.valueOf(Integer.parseInt(doc.getElementsByTagName("Streak").item(0).getTextContent()) + 1));
			WriteXML(doc, "www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendChat(String messageText) {
		String chat = "<message from='nfsw.engine.engine@127.0.0.1/EA_Chat' id='JN_0' to='nfsw.RELAYPERSONA@127.0.0.1'><body>&lt;response status='1' ticket='0'&gt;&lt;ChatBroadcast&gt;&lt;ChatBlob&gt;&lt;FromName&gt;System&lt;/FromName&gt;&lt;FromPersonaId&gt;0&lt;/FromPersonaId&gt;&lt;FromUserId&gt;0&lt;/FromUserId&gt;&lt;Message&gt;" + messageText + "&lt;/Message&gt;&lt;ToId&gt;0&lt;/ToId&gt;&lt;Type&gt;2&lt;/Type&gt;&lt;/ChatBlob&gt;&lt;/ChatBroadcast&gt;&lt;/response&gt;</body><subject>LOLnope.</subject></message>";
		String msg = new String(chat).replace("RELAYPERSONA", Functions.personaId);
		Long personaIdLong = Long.decode(Functions.personaId);
		XmppSrv.sendMsg(personaIdLong, msg);
		return;
	}

	public static void sendAchievement(String time) {
		String chat1 = "<message from='nfsw.engine.engine@127.0.0.1/EA_Chat' id='JN_0' to='nfsw.RELAYPERSONA@127.0.0.1'><body>&lt;response status='1' ticket='0'&gt;&lt;AchievementAwarded&gt;&lt;AchievedOn&gt;" + time + "&lt;/AchievedOn&gt;&lt;AchievementDefinitionId&gt;96&lt;/AchievementDefinitionId&gt;&lt;AchievementRankId&gt;309&lt;/AchievementRankId&gt;&lt;Clip&gt;AchievementFlasherBase&lt;/Clip&gt;&lt;ClipLengthInSeconds&gt;5&lt;/ClipLengthInSeconds&gt;&lt;Description&gt;GM_ACHIEVEMENT_0000026E&lt;/Description&gt;&lt;Icon&gt;ACH_USE_NOS&lt;/Icon&gt;&lt;IsRare&gt;true&lt;/IsRare&gt;&lt;Name&gt;GM_ACHIEVEMENT_0000010C&lt;/Name&gt;&lt;Points&gt;10&lt;/Points&gt;&lt;Rarity&gt;0&lt;/Rarity&gt;&lt;/AchievementAwarded&gt;&lt;/response&gt;</body><subject>LOLnope.</subject></body></message>";
		String msg = new String(chat1).replace("RELAYPERSONA", Functions.personaId);
		Long personaIdLong = Long.decode(Functions.personaId);
		XmppSrv.sendMsg(personaIdLong, msg);
		return;
	}

	public int GetLevel() throws ParserConfigurationException, SAXException, IOException {
		return Integer.parseInt(DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse("www/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaBaseFromList.xml")
				.getElementsByTagName("Level").item(0).getTextContent());
	}

	public int GetTHStreak() throws DOMException, SAXException, IOException, ParserConfigurationException {
		return Integer.parseInt(DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse("www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml").getElementsByTagName("Streak")
				.item(0).getTextContent());
	}
	
	public int calcRepair() throws SAXException, IOException, ParserConfigurationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("www/soapbox/Engine.svc/personas/" + personaId + "/carslots.xml"));
		int carId = Integer.parseInt(doc.getElementsByTagName("DefaultOwnedCarIndex").item(0).getTextContent());
		int current = Integer.parseInt(doc.getElementsByTagName("Durability").item(carId).getTextContent());
		int price =  150 * (100 - current);
		return price;
	}
/*
	public String GetIsTHStreakBroken() throws DOMException, SAXException, IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse("www/soapbox/Engine.svc/personas/gettreasurehunteventsession.xml")
				.getElementsByTagName("IsStreakBroken").item(0).getTextContent();
	}
*/
	public int CountInstances(String dataString, String toCount, String toFind) {
		int maxIndex = dataString.indexOf(toFind);
		int currentIndex = 0;
		int occurrences = 0;

		while (currentIndex < maxIndex) {
			currentIndex = dataString.indexOf(toCount, currentIndex) + toCount.length();
			if (currentIndex > maxIndex || currentIndex == (toCount.length() - 1))
				break;
			occurrences++;
		}
		return occurrences;
	}

	public String parseBasketId(String basketTrans) {
		String basketId = "";
		String pattern = "<ProductId>(.*)</ProductId>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(basketTrans);
		if (m.find() && m.groupCount() > 0) {
			basketId = m.group(1).replace(":", "");
		} else {
			log(" -->: BasketId Parse Error.");
		}
		return basketId;
	}

	public int[] StringArrayToIntArray(String input) {
		String[] strArray = input.split(",");
		int[] intArray = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			intArray[i] = Integer.parseInt(strArray[i]);
		}
		return intArray;
	}

	public static void log(String text) {
		Functions.logTextArea.append(text + "\n");
		System.out.println(text);
	}

	public String ReadText(String location) {
		try {
			return new String(Files.readAllBytes(Paths.get(location)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void WriteText(String location, String text) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(location).getAbsoluteFile()));
			bw.write(text);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void WriteXML(Document doc, String location) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(location).getAbsoluteFile()));
			bw.write(sw.toString());
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
