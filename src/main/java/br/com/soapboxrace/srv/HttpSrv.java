package br.com.soapboxrace.srv;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import br.com.soapboxrace.func.Basket;
import br.com.soapboxrace.func.Commerce;
import br.com.soapboxrace.func.Constants;
import br.com.soapboxrace.func.Event;
import br.com.soapboxrace.func.Matchmaking;
import br.com.soapboxrace.func.Persona;
import br.com.soapboxrace.func.Functions;
import br.com.soapboxrace.swing.MainWindow;
import br.com.soapboxrace.xmpp.XmppLobbyThread;
//import br.com.soapboxrace.xmpp.SubjectCalc;
import br.com.soapboxrace.xmpp.XmppSrv;

public class HttpSrv extends GzipHandler {

	private Basket basket = new Basket();
	private Commerce commerce = new Commerce();
	private Event event = new Event();
	private Matchmaking matchmaking = new Matchmaking();
	private Persona persona = new Persona();
	private static Functions fx = new Functions();

	public static String modifiedTarget;
//	public static boolean THBroken = false;
	public static int iEvent = 0;
	private String eventIdTmp;
	private int[] randEventIds = { 88, 89, 91, 93, 96, 99, 100, 101, 105, 106, 107, 111, 114, 115, 118, 119, 121, 122, 123, 125, 126, 129, 130, 132, 136, 138, 139, 140, 142, 143, 150, 152, 153, 154, 160, 162, 163, 164, 167, 168, 170, 171, 172, 174, 177, 178, 180, 181, 182, 184, 186, 187, 189, 190, 192, 193, 199, 201, 203, 204, 206, 207, 208, 213, 223, 224, 225, 226, 227, 228, 235, 271, 272, 273, 274, 275, 276, 291, 297, 300, 304, 306, 310, 311, 312, 313, 338, 339, 340, 344, 347, 348, 353 };

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			String sLastTarget = target.split("/")[target.split("/").length - 1];
			//System.out.println(baseRequest);
			if (!target.contains("broad") && !target.contains("heart") && !target.contains("powerup") && !target.contains("queue") && !target.contains("catalog") && !target.contains("Gifts") && !target.contains("hunt") && !target.contains("ersona") && !target.contains("event") && !target.contains(".jpg")) {
				if ("POST".equals(baseRequest.getMethod())) {
					Functions.log(baseRequest.getMethod() + ": ----------------------------------------------------------------> " + target.replaceAll("/soapbox/Engine.svc/", ""));
				} else {
					Functions.log(" " + baseRequest.getMethod() + ": ----------------------------------------------------------------> " + target.replaceAll("/soapbox/Engine.svc/", ""));
				}
			}
			modifiedTarget = target;
			boolean isXmpp = false;

			if (target.matches("/soapbox/Engine.svc/User/SecureLoginPersona")) {
				Functions.personaId = baseRequest.getParameter("personaId");
				fx.ChangeDefaultPersona(String.valueOf(Integer.parseInt(baseRequest.getParameter("personaId"))));

			} else if (target.contains("getrebroadcasters")){
				modifiedTarget = "x"; Functions.answerData = Constants.Rebroadcasters;
			} else if (target.contains("heartbeat")){
				modifiedTarget = "x"; Functions.answerData = Constants.HeartBeat;
			} else if (sLastTarget.equals("cryptoticket")){
				modifiedTarget = "x"; Functions.answerData = Constants.Cryptoticket;
			} else if(target.contains("relaycryptoticket")){
				modifiedTarget = "x"; Functions.answerData = Constants.RelayCryptoTicket;
			} else if(target.contains("joinqueueevent")){
				String[] split = target.split("/");
				eventIdTmp = split[5];
				XmppSrv.sendMsg(Long.valueOf(Functions.personaId), Constants.joinEvent(Functions.personaId, split[5]));
			} else if (target.contains("acceptinvite")) {
				modifiedTarget = "x"; Functions.answerData = Constants.acceptInvite(Functions.personaId, eventIdTmp);
				new XmppLobbyThread(Functions.personaId, eventIdTmp).start();
			} else if (target.contains("client")){
				modifiedTarget = "x"; Functions.answerData = Constants.ClientLog;
			} else if (target.contains("GetAndTriggerAvailableLevelGifts")){
				modifiedTarget = "x"; Functions.answerData = "<ArrayOfLevelGiftDefinition/>";
			} else if (target.contains("fraudConfig")){
				modifiedTarget = "x"; Functions.answerData = Constants.FraudConfig;
			} else if (target.contains("getfriendlistfromuserid")){
				modifiedTarget = "x"; Functions.answerData = Constants.Friendlist;
			} else if (target.contains("carclasses")){
				modifiedTarget = "x"; Functions.answerData = Constants.CarClasses;
			} else if (target.contains("GetChatInfo")){
				modifiedTarget = "x"; Functions.answerData = Constants.ChatInfo;
			} else if (target.contains("GetExpLevelPointsMap")){
				modifiedTarget = "x"; Functions.answerData = Constants.ExpLvlPtsMap;
			} else if (target.contains("getregioninfo")){
				modifiedTarget = "x"; Functions.answerData = Constants.RegionInfo;
			} else if (target.contains("systeminfo")){
				modifiedTarget = "x"; Functions.answerData = Constants.SystemInfo;
			} else if (target.contains("LoginAnnouncements")){
				modifiedTarget = "x"; Functions.answerData = Constants.LoginAnnouncements;
			} else if (target.contains("loadall")){
				modifiedTarget = "x"; Functions.answerData = Constants.AchievementA + Constants.AchievementB;
			} else if ((target.contains("getblockeduserlist")) || (target.contains("getblockersbyusers"))){
				modifiedTarget = "x"; Functions.answerData = "<ArrayOflong/>";
			} else if ((target.contains("getusersettings")) || (target.contains("setusersettings"))){
				modifiedTarget = "x"; Functions.answerData = Constants.UserSettings;
			} else if ((target.contains("getsocialsettings")) || (target.contains("setsocialsettings"))){
				modifiedTarget = "x"; Functions.answerData = Constants.SocialSettings;
			} else if (target.contains("repair")){
				modifiedTarget = "x"; Functions.answerData = "<int>100</int>";
			} else if (target.contains("launchevent")){
				modifiedTarget = "x"; matchmaking.launch(sLastTarget);
			} else if (target.contains("availableatlevel")) {
				modifiedTarget = "x"; Functions.answerData = Constants.EventsAvailable;
			} else if (target.contains("NewsArticles")){
				modifiedTarget = "x"; Functions.answerData = "<ArrayOfNewsArticleTrans/>";
				Thread.sleep(500);
				Functions.sendChat(Constants.WelcomeMessage);
				iEvent = randEventIds[new Random().nextInt(randEventIds.length)];
				Functions.sendChat("New Random-Event generated. ID: " + iEvent);
				Functions.log(" -->: New Random-Event generated / ID: " + iEvent);
			} else if (target.contains("joinqueueracenow")) {
				iEvent = randEventIds[new Random().nextInt(randEventIds.length)];
				Functions.sendChat("New Random-Event generated. ID: " + iEvent);
				Functions.log(" -->: New Random-Event generated / ID: " + iEvent);


			} else if ((target.contains("productsInCategory")) || (target.contains("categories"))) {
				modifiedTarget = "/soapbox/Engine.svc/catalog/products_" + baseRequest.getParameter("categoryName");
				if (modifiedTarget.contains("BoosterPacks") || modifiedTarget.contains("BOOSTERPACKS") || modifiedTarget.contains("VISUALPARTS") || modifiedTarget.contains("CARDPACK") || modifiedTarget.contains("CARS") || modifiedTarget.contains("AMPLIFIERS") || modifiedTarget.contains("STORE_SKILL")){
					modifiedTarget = "x"; Functions.answerData = "<ArrayOfProductTrans/>";
				} else if (modifiedTarget.contains("CARSLOTS")){
					modifiedTarget = "x"; Functions.answerData = Constants.CarSlot;
				} else if (modifiedTarget.contains("PAINTS_BODY")){
					modifiedTarget = "x"; Functions.answerData = Constants.PaintsBody;
				} else if (modifiedTarget.contains("PAINTS_WHEEL")){
					modifiedTarget = "x"; Functions.answerData = Constants.PaintsWheel;
				} else if (modifiedTarget.contains("REPAIRS")){
					modifiedTarget = "x"; Functions.answerData = Constants.CarRepair;
				} else if (modifiedTarget.contains("Starting_Cars")){
					modifiedTarget = "x"; Functions.answerData = Constants.StarterCars;
				} else if (modifiedTarget.contains("POWERUPS")){
					modifiedTarget = "x"; Functions.answerData = Constants.Powerups;
				} else if (modifiedTarget.contains("STREAK")){
					modifiedTarget = "x"; Functions.answerData = Constants.StreakRecovery;
				} else if (modifiedTarget.contains("VINYLCATEGORIES")){
					modifiedTarget = "x"; Functions.answerData = Constants.ShopVinylCats;
				}
				
			} else if (target.contains("UpdateStatusMessage")) {
				fx.setPersonaMotto(readInputStream(request));
			} else if (target.contains("powerups/activated")) {
				isXmpp = true; event.processPowerup(sLastTarget, -1);
			} else if (target.contains("badges/set")) {
				fx.ChangeBadges(readInputStream(request));
			} else if (target.contains("personas/(.*)/baskets")) {
				modifiedTarget = "baskets"; basket.processBasket(readInputStream(request));
			} else if (target.contains("personas/(.*)/commerce")) {
				modifiedTarget = "commerce"; commerce.saveCommerceData(readInputStream(request));
			} else if (target.contains("personas/inventory/sell/(.*)")) {
				commerce.sell(sLastTarget, 0);
			} else if (target.contains("personas/(.*)/defaultcar/(.*)")) {
				fx.ChangeCarIndex(target.split("/")[6], false);
			} else if (target.contains("personas/(.*)/cars") && baseRequest.getMethod() == "POST") {
				basket.SellCar(baseRequest.getParameter("serialNumber"));
			} else if (target.contains("personas/(.*)/carslots")) {
				fx.FixCarslots();
			} else if (target.contains("GetPermanentSession")) {
				modifiedTarget = "/soapbox/Engine.svc/personas/GetPermanentSession";
			} else if (target.contains("GetPersonaInfo")) {
				modifiedTarget = "/soapbox/Engine.svc/personas/" + Functions.personaId + "/GetPersonaInfo";
			} else if (target.contains("GetPersonaBaseFromList")) {
				String readInputStream = readInputStream(request);
				String pattern = "(.*)<array:long>(.*)</array:long>(.*)";
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(readInputStream);
				String personaId = Functions.personaId ;
				if (m.find()) {personaId = m.group(2);}
				modifiedTarget = "/soapbox/Engine.svc/personas/" + personaId + "/GetPersonaBaseFromList";
			} else if (target.contains("gettreasurehunteventsession")) {
				modifiedTarget = "/soapbox/Engine.svc/personas/gettreasurehunteventsession";
			} else if (target.contains("inventory/objects")) {
				modifiedTarget = "/soapbox/Engine.svc/personas/" + Functions.personaId + "/objects";
			} else if (target.contains("CreatePersona")) {
				modifiedTarget = "CreatePersona"; persona.createPersona(baseRequest.getParameter("name"), baseRequest.getParameter("iconIndex"));
			} else if (target.contains("DeletePersona")) {
				modifiedTarget = "DeletePersona"; persona.deletePersona(baseRequest.getParameter("personaId"));
			} else if (target.contains("ReserveName")) {
				modifiedTarget = "ReserveName"; Functions.answerData = "<ArrayOfstring/>";
			} else if (target.contains("event/arbitration")) {
				event.ReadArbitration(readInputStream(request)); fx.processDurability(); modifiedTarget = "Arbitration";
			} else if (target.contains("event/bust")) {
				event.ReadBust(); fx.processDurability(); modifiedTarget = "Busted";
			} else if (target.contains("events/instancedaccolades")) {
				event.SetPrize(Event.RaceReward); modifiedTarget = "RaceReward";
			} else if (target.contains("events/accolades")) {
				event.ReadArbitration("<TreasureHunt/>"); modifiedTarget = "THCompleted";
			} else if (target.contains("events/notifycoincollected")) {
				fx.SaveTHProgress(baseRequest.getParameter("coins"));
				if (baseRequest.getParameter("coins").equals("1073741823")) {
					Functions.log(" -->: Detected TH Finished event.");
					event.ReadArbitration("<TreasureHunt/>"); modifiedTarget = "THCompleted";
				}
			}

			if (target.contains(".jpg")) {response.setContentType("image/jpeg");} else {response.setContentType("application/xml;charset=utf-8");}
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Connection", "close");
			response.setHeader("Content-Encoding", "gzip");

			byte[] content = null;
			if (Files.exists(Paths.get("www" + modifiedTarget + ".xml"))) {
				content = Files.readAllBytes(Paths.get("www" + modifiedTarget + ".xml"));
			} else if (Files.exists(Paths.get("www" + modifiedTarget)) && !Files.isDirectory(Paths.get("www" + modifiedTarget))) {
				content = Files.readAllBytes(Paths.get("www" + modifiedTarget));
			} else if (modifiedTarget != target) {
				content = Functions.answerData.getBytes(StandardCharsets.UTF_8);
			}

			if (content == null) {
				response.getOutputStream().println();
				response.getOutputStream().flush();
			} else {
				if (!target.contains(".jpg")) {
					String sContent = new String(content, StandardCharsets.UTF_8);
					if (sContent.contains("RELAYPERSONA")){
						sContent = sContent.replace("RELAYPERSONA", Functions.personaId);}
					if (sContent.contains("ACTUALTIME")){
						String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
						sContent = sContent.replace("ACTUALTIME", currentTime);
						Functions.log(" -->: Current Server Time has been set to " + currentTime + ".");}
					if (sContent.contains("SCENERY")){
						SimpleDateFormat sdf = new SimpleDateFormat("dd.MM");
						Date todayDate = sdf.parse(sdf.format(new Date()));
						if ((todayDate.after(sdf.parse("19.09"))) && (todayDate.before(sdf.parse("11.10")))){
							sContent = sContent.replace("SCENERY_ENABLE", "SCENERY_GROUP_OKTOBERFEST");
							sContent = sContent.replace("SCENERY_ID", "1");
							sContent = sContent.replace("SCENERY_DISABLE", "SCENERY_GROUP_OKTOBERFEST_DISABLE");
							Functions.log(" -->: Current Server Decoration has been set to OKTOBERFEST");}
						else if (todayDate.equals(sdf.parse("31.10"))){
							sContent = sContent.replace("SCENERY_ENABLE", "SCENERY_GROUP_HALLOWEEN");
							sContent = sContent.replace("SCENERY_ID", "2");
							sContent = sContent.replace("SCENERY_DISABLE", "SCENERY_GROUP_HALLOWEEN_DISABLE");
							Functions.log(" -->: Current Server Decoration has been set to HALLOWEEN");				}
						else if (((todayDate.after(sdf.parse("30.11"))) && (todayDate.before(sdf.parse("31.12")))) || ((todayDate.after(sdf.parse("01.01"))) && (todayDate.before(sdf.parse("01.02"))))){
							sContent = sContent.replace("SCENERY_ENABLE", "SCENERY_GROUP_CHRISTMAS");
							sContent = sContent.replace("SCENERY_ID", "3");
							sContent = sContent.replace("SCENERY_DISABLE", "SCENERY_GROUP_CHRISTMAS_DISABLE");
							Functions.log(" -->: Current Server Decoration has been set to WINTER / CHRISTMAS");}
						else if ((todayDate.equals(sdf.parse("31.12"))) || (todayDate.equals(sdf.parse("01.01")))){
							sContent = sContent.replace("<a:string>SCENERY_ENABLE</a:string>", "<a:string>SCENERY_GROUP_CHRISTMAS</a:string><a:string>SCENERY_GROUP_NEWYEARS</a:string>");
							sContent = sContent.replace("<a:long>SCENERY_ID</a:long>", "<a:long>3</a:long><a:long>5</a:long>");
							sContent = sContent.replace("<a:string>SCENERY_DISABLE</a:string>", "<a:string>SCENERY_GROUP_CHRISTMAS_DISABLE</a:string><a:string>SCENERY_GROUP_NEWYEARS_DISABLE</a:string>");
							Functions.log(" -->: Current Server Decoration has been set to WINTER / NEWYEAR");}
						else {
							sContent = sContent.replace("SCENERY_ENABLE", "SCENERY_GROUP_NORMAL");
							sContent = sContent.replace("SCENERY_ID", "0");
							sContent = sContent.replace("SCENERY_DISABLE", "SCENERY_GROUP_NORMAL_DISABLE");
							Functions.log(" -->: Current Server Decoration has been set to NORMAL");}
					}
					content = gzip(sContent.getBytes(StandardCharsets.UTF_8));
					response.setContentLength(content.length);
					response.getOutputStream().write(content);
					response.getOutputStream().flush();
				} else {
					response.setContentLength(content.length);
					response.getOutputStream().write(content);
					response.getOutputStream().flush();
					response.getOutputStream().println();
					response.getOutputStream().flush();
				}
			}
			baseRequest.setHandled(true);
			if (isXmpp) {
				sendXmpp(target);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String readInputStream(HttpServletRequest request) {
		StringBuilder buffer = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	private byte[] gzip(byte[] data) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
		try {
			OutputStream gzipout = new GZIPOutputStream(byteStream) {
				{
					def.setLevel(1);
				}
			};
			try {
				gzipout.write(data);
			} finally {
				gzipout.close();
			}
		} finally {
			byteStream.close();
		}
		return byteStream.toByteArray();
	}
/*
	private static String setXmppSubject(String msg) {
		String[] splitMsg = msg.split("<body>|</body>");
		String[] splitMsgTo = splitMsg[0].split("\\\"");
		String msgTo = splitMsgTo[5];
		String msgBody = splitMsg[1];
		msgBody = msgBody.replace("&lt;", "<");
		msgBody = msgBody.replace("&gt;", ">");
		Long subject = SubjectCalc.calculateHash(msgTo.toCharArray(), msgBody.toCharArray());
		msg = msg.replace("LOLnope.", subject.toString());
		return msg;
	}
*/
	private void sendXmpp(String target) {
		try {
			String encoded = Constants.powerupXmpp(target.split("/")[target.split("/").length - 1]);
				if (encoded != null) {
					String msg = new String(encoded).replace("RELAYPERSONA", Functions.personaId);
					Long personaIdLong = Long.decode(Functions.personaId);
					//msg = setXmppSubject(msg);
					XmppSrv.sendMsg(personaIdLong, msg);
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String gameExePath = "...";
		try {
			gameExePath = new String(Files.readAllBytes(Paths.get("gameExePath.txt")));
		} catch (Exception e) {
			//
		}

		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
		mainWindow.setGamePathLabelText(gameExePath);
		Functions.setLogTextArea(mainWindow.getLogTextArea());
		Functions.log("Starting offline server");
		System.setProperty("jsse.enableCBCProtection", "false");
		try {
			Locale newLocale = new Locale("en", "GB");
			Locale.setDefault(newLocale);

			Server server = new Server(7331);
			server.setHandler(new HttpSrv());
			server.start();

			XmppSrv xmppSrv = new XmppSrv();
			xmppSrv.start();

			Functions.log("");
			String THDate = fx.ReadText("www/soapbox/Engine.svc/settings/THDate");
			if (THDate != LocalDate.now().toString()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ENGLISH);
				LocalDate lastCompletedTHDate = LocalDate.parse(THDate, formatter);
				LocalDate nowDate = LocalDate.now();
				long days = ChronoUnit.DAYS.between(lastCompletedTHDate, nowDate);

				Functions.log(" -->: Last TH completed was on " + lastCompletedTHDate.toString() + ".");
				if (days == 0) {
					Functions.log(" -->: Since that date is today, nothing will be done.");
				} else if (days >= 1) {
					fx.StartNewTH(true);
				} else {
					Functions.log(" -->: Go back where you came from time traveller!");
				}
			}

			String[] settings = Files.readAllLines(Paths.get("www/soapbox/Engine.svc/settings/DropRates")).toArray(new String[] {});
			Functions.rewards = new int[] { Integer.parseInt(settings[2]), Integer.parseInt(settings[10]), Integer.parseInt(settings[12]), Integer.parseInt(settings[14]), Integer.parseInt(settings[16]) };
			Functions.multipliers = new double[] { Double.parseDouble(settings[4]), Double.parseDouble(settings[6]), Double.parseDouble(settings[8]) };
			Functions.rankDrop = new int[][] { new int[] {}, fx.StringArrayToIntArray(settings[19]), fx.StringArrayToIntArray(settings[20]), fx.StringArrayToIntArray(settings[21]), fx.StringArrayToIntArray(settings[22])};
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
