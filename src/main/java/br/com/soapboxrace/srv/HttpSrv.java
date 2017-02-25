package br.com.soapboxrace.srv;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import br.com.soapboxrace.func.Basket;
import br.com.soapboxrace.func.Commerce;
import br.com.soapboxrace.func.Event;
import br.com.soapboxrace.func.Persona;
import br.com.soapboxrace.func.Functions;
import br.com.soapboxrace.swing.MainWindow;
import br.com.soapboxrace.xmpp.SubjectCalc;
import br.com.soapboxrace.xmpp.XmppSrv;

public class HttpSrv extends GzipHandler {

	private Basket basket = new Basket();
	private Commerce commerce = new Commerce();
	private Event event = new Event();
	private Persona persona = new Persona();
	private static Functions fx = new Functions();

	public static String modifiedTarget;
	public static boolean THBroken = false;
	private int iEvent = 0;
	private int[] randEventIds = { 88, 89, 91, 93, 96, 99, 100, 101, 105, 106, 107, 111, 114, 115, 118, 119, 121, 122, 125, 126, 129, 130, 132, 136, 138, 139, 140, 142, 143, 150, 152, 154, 160, 162, 163, 164, 167, 168, 170, 171, 172, 174, 177, 178, 180, 181, 182, 184, 186, 187, 189, 190, 192, 193, 199, 201, 203, 204, 206, 207, 208, 213, 223, 224, 225, 226, 227, 228, 235, 271, 272, 273, 274, 275, 276, 291, 297, 300, 304, 306, 310, 311, 312, 313, 338, 339, 340, 344, 347, 348 };

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			String sLastTarget = target.split("/")[target.split("/").length - 1];
			if (!target.contains("broad") && !target.contains("heart") && !target.contains("powerup") && !target.contains("queue") && !target.contains("catalog") && !target.contains("ersona") && !target.contains("Gifts") && !target.contains("hunt") && !target.contains(".jpg")) {
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
				fx.ChangeDefaultPersona(String.valueOf((Integer.parseInt(baseRequest.getParameter("personaId")) / 100) - 1));
			} else if (target.matches("/soapbox/Engine.svc/setusersettings")) {
				fx.WriteText("www/soapbox/Engine.svc/getusersettings.xml",
				new String(Files.readAllBytes(Paths.get("www/soapbox/Engine.svc/getusersettings.xml")),
				StandardCharsets.UTF_8).replace("<starterPackApplied>false</starterPackApplied>","<starterPackApplied>true</starterPackApplied>"));
			} else if (target.matches("/soapbox/Engine.svc/catalog/productsInCategory")) {
				modifiedTarget = target + "_" + baseRequest.getParameter("categoryName");
			} else if (target.matches("/soapbox/Engine.svc/catalog/categories")) {
				modifiedTarget = target + "_" + baseRequest.getParameter("categoryName");
			} else if (target.matches("/soapbox/Engine.svc/powerups/activated(.*)")) {
				isXmpp = true;
				event.processPowerup(sLastTarget, -1);
			} else if ((sLastTarget.equals("joinqueueracenow")) || (sLastTarget.equals("availableatlevel"))) {
				iEvent = this.randEventIds[new Random().nextInt(this.randEventIds.length)];
				Functions.log(" -->: New Random-Event generated / ID " + iEvent);
			} else if ((target.contains("event/60")) || (target.contains("event/374")) || (target.contains("event/378"))) {
				modifiedTarget = "/soapbox/Engine.svc/matchmaking/launchevent/" + iEvent;
			} else if (target.matches("/soapbox/Engine.svc/badges/set")) {
				fx.ChangeBadges(readInputStream(request));
			} else if (target.matches("/soapbox/Engine.svc/personas/(.*)/baskets")) {
				modifiedTarget = "baskets";
				basket.processBasket(readInputStream(request));
			} else if (target.matches("/soapbox/Engine.svc/personas/(.*)/commerce")) {
				modifiedTarget = "commerce";
				commerce.saveCommerceData(readInputStream(request));
			} else if (target.matches("/soapbox/Engine.svc/personas/inventory/sell/(.*)")) {
				commerce.sell(sLastTarget, 0);
			} else if (target.matches("/soapbox/Engine.svc/personas/(.*)/defaultcar/(.*)")) {
				fx.ChangeCarIndex(target.split("/")[6], false);
			} else if (target.matches("/soapbox/Engine.svc/personas/(.*)/cars") && baseRequest.getMethod() == "POST") {
				basket.SellCar(baseRequest.getParameter("serialNumber"));
			} else if (target.matches("/soapbox/Engine.svc/personas/(.*)/carslots")) {
				fx.FixCarslots();
			} else if (target.matches("/soapbox/Engine.svc/DriverPersona/GetPersonaInfo")) {
				modifiedTarget = target + "_" + Functions.personaId;
			} else if (target.matches("/soapbox/Engine.svc/DriverPersona/GetPersonaBaseFromList")) {
				modifiedTarget = target + "_" + Functions.personaId;
			} else if (target.matches("/soapbox/Engine.svc/personas/inventory/objects")) {
				modifiedTarget = "/soapbox/Engine.svc/personas/" + Functions.personaId + "/objects";
			} else if (target.matches("/soapbox/Engine.svc/DriverPersona/CreatePersona(.*)")) {
				persona.createPersona(baseRequest.getParameter("name"), baseRequest.getParameter("iconIndex"));
			} else if (target.matches("/soapbox/Engine.svc/DriverPersona/DeletePersona(.*)")) {
				persona.deletePersona(baseRequest.getParameter("personaId"));
			} else if (target.matches("/soapbox/Engine.svc/events/notifycoincollected")) {
				fx.SaveTHProgress(baseRequest.getParameter("coins"));
				if (baseRequest.getParameter("coins").equals("32767")) {
					Functions.log(" -->: Detected TH Finished event.");
					if (fx.GetIsTHStreakBroken().equals("true")) {
						THBroken = true;
						modifiedTarget = "THBroken";
						Functions.log(" -->: Your TH Streak is broken.");
						Functions.answerData = "<Accolades xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization.Event\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><FinalRewards><Rep>25</Rep><Tokens>78</Tokens></FinalRewards><HasLeveledUp>false</HasLeveledUp><LuckyDrawInfo><Boxes><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox></Boxes><CurrentStreak>"
								+ String.valueOf(fx.GetTHStreak())
								+ "</CurrentStreak><IsStreakBroken>true</IsStreakBroken><Items></Items><NumBoxAnimations>100</NumBoxAnimations></LuckyDrawInfo><OriginalRewards><Rep>0</Rep><Tokens>0</Tokens></OriginalRewards><RewardInfo/></Accolades>";
					} else {
						event.ReadArbitration("<TreasureHunt/>");
						modifiedTarget = "THCompleted";
					}
				}
			} else if (target.matches("/soapbox/Engine.svc/event/arbitration")) {
				event.ReadArbitration(readInputStream(request));
				modifiedTarget = "Arbitration";
			} else if (target.matches("/soapbox/Engine.svc/events/accolades")) {
				if (THBroken) {
					Functions.log(" -->: Your TH Streak will be revived for 1000 Boost.");
					event.ReadArbitration("<TreasureHunt/>");
					modifiedTarget = "THCompleted";
					THBroken = false;
				}
			} else if (target.matches("/soapbox/Engine.svc/events/instancedaccolades")) {
				event.SetPrize(Event.RaceReward);
				modifiedTarget = "RaceReward";
			}

			if (target.contains(".jpg")) {
				response.setContentType("image/jpeg");
			} else {
				response.setContentType("application/xml;charset=utf-8");
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Connection", "close");
			response.setHeader("Content-Encoding", "gzip");

			byte[] content = null;
			if (Files.exists(Paths.get("www" + modifiedTarget + ".xml"))) {
				content = Files.readAllBytes(Paths.get("www" + modifiedTarget + ".xml"));
			} else if (Files.exists(Paths.get("www" + modifiedTarget))
					&& !Files.isDirectory(Paths.get("www" + modifiedTarget))) {
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

	private void sendXmpp(String target) {
		try {
			String path = "www" + target + "_xmpp.xml";
			File fxmpp = new File(path);
			byte[] encoded = null;
			if (fxmpp.exists()) {
				encoded = Files.readAllBytes(Paths.get(path));
				if (encoded != null) {
					String msg = new String(encoded, StandardCharsets.UTF_8).replace("RELAYPERSONA",
							Functions.personaId);
					Long personaIdLong = Long.decode(Functions.personaId);
					msg = setXmppSubject(msg);
					XmppSrv.sendMsg(personaIdLong, msg);
				}
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
			String THDate = fx.ReadText("www/soapbox/Engine.svc/serverSettings/THDate");
			if (THDate != LocalDate.now().toString()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ENGLISH);
				LocalDate lastCompletedTHDate = LocalDate.parse(THDate, formatter);
				LocalDate nowDate = LocalDate.now();
				long days = ChronoUnit.DAYS.between(lastCompletedTHDate, nowDate);

				Functions.log(" -->: Last TH completed was on " + lastCompletedTHDate.toString() + ".");
				if (days == 0) {
					Functions.log(" -->: Since that date is today, nothing will be done.");
				} else if (days == 1) {
					fx.StartNewTH(true);
				} else if (days >= 2) {
					fx.StartNewTH(false);
					Functions.log(" -->: Since that date, it's been " + String.valueOf(days)	+ " days. Your TH Streak is broken.");
				} else {
					Functions.log(" -->: Go back where you came from time traveller!");
				}
			}

			String[] settings = Files.readAllLines(Paths.get("www/soapbox/Engine.svc/serverSettings/settings"))
					.toArray(new String[] {});
			Functions.rewards = new int[] { Integer.parseInt(settings[1]), Integer.parseInt(settings[5]),
					Integer.parseInt(settings[6]), Integer.parseInt(settings[7]), Integer.parseInt(settings[8]) };
			Functions.multipliers = new double[] { Double.parseDouble(settings[2]), Double.parseDouble(settings[3]),
					Double.parseDouble(settings[4]) };
			Functions.rankDrop = new int[][] { new int[] {}, fx.StringArrayToIntArray(settings[10]),
					fx.StringArrayToIntArray(settings[11]), fx.StringArrayToIntArray(settings[12]),
					fx.StringArrayToIntArray(settings[13]) };
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
