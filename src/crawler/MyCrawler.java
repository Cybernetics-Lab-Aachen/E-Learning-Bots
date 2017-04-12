package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.mysql.jdbc.PreparedStatement;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	private static final String ALCHEMY_KEY = System.getenv("ALCHEMY_KEY");

	// source attributes
	String sourceURL = "";
	String language = "";
	// entities attributes
	String[] entity = new String[7];
	// keywords attributes
	String[] keywords = new String[8];
	// concepts attributes
	String[] concepts = new String[4];
	String text = "";
	String url = "";
	boolean passed = false;
	SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp3|zip|gz))$");

	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches();
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	@Override
	public void visit(Page page) {
		try {
			url = page.getWebURL().getURL();
			System.out.println("URL: " + url);
			String[] wlAnalysis = readFile(Controller.wlAnaPath, StandardCharsets.UTF_8).split(",");
			String[] wlCrawler = readFile(Controller.wlCrPath, StandardCharsets.UTF_8).split(",");

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://" + Controller.host + ":" + Controller.port + "/demo", "" + Controller.user,
					"" + Controller.password);
			Statement statement = connection.createStatement();
			ResultSet res = statement.executeQuery("SELECT * FROM  visits");
			res.next();
			Controller.counter = res.getInt("visits");
			Controller.counter++;
			PreparedStatement sql = (PreparedStatement) connection
					.prepareStatement("UPDATE visits SET visits =" + Controller.counter);
			sql.executeUpdate();

			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				text = htmlParseData.getText();
				// check text with whitelistCrawler
				checkWhiteList(wlCrawler);
				if (passed == false) {
					return;
				}
				if (Controller.enableAlchemy) {
					useAlchemy(wlAnalysis);
				}
				try {
					if (Controller.restart == false) {
						ResultSet result = statement.executeQuery("SELECT * FROM  sources WHERE url ='" + url + "'");
						result.next();
						Controller.run = result.getInt("run");
						Controller.restart = true;
					}
				} catch (Exception e) {
					Controller.restart = true;
					Controller.run = 1;
				}
				System.out.println("Websites visited: " + Controller.counter);
				accessDB(connection, statement);
			}
		} catch (IOException | SQLException | ParseException | InterruptedException ex) {
			onUnhandledException(page.getWebURL(), ex);
		}
	}

	public void useAlchemy(String[] whiteList) throws InterruptedException, UnsupportedEncodingException, IOException {
		// access Alchemy to receive JSon containing keywords, entities and
		// concepts
		URL urls = new URL("https://gateway-a.watsonplatform.net/calls/url/URLGetCombinedData?url=" + url
				+ "&outputMode=json&extract=keywords,entities,concepts&sentiment=1&maxRetrieve=3&apikey="
				+ ALCHEMY_KEY);

		String alchemy = "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
			for (String line; (line = reader.readLine()) != null;) {
				alchemy = alchemy + line;
				System.out.println(line);

			}
		}
		parseJson(alchemy);
		// pause till next day if daily limit(about 300 accesses) is
		// exceeded
		if (alchemy.contains("daily-transaction-limit-exceeded")) {
			String timeStamp = timeFormat.format(Calendar.getInstance().getTime());
			timeStamp = timeStamp.substring(9, 11);
			int waitingTime = (25 - Integer.parseInt(timeStamp)) * 3600000;
			System.out.println("Pause until next day");
			Thread.sleep(waitingTime);
		}

		// check analysis with whitelistAnalysis
		passed = false;
		checkWhiteList(whiteList);
		if (passed == false) {
			return;
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public void accessDB(Connection connection, Statement statement) throws SQLException, ParseException {
		// receiving current timestamp
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String lastVisit = "";
		lastVisit = timeFormat.format(Calendar.getInstance().getTime());
		java.util.Date time = timeFormat.parse(lastVisit);
		java.sql.Timestamp timestamp = new java.sql.Timestamp(time.getTime());

		try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO sources (url, language, time, data, country, run)" + " VALUES (?,?,?,?,?,?)");) {
			if (Controller.enableAlchemy) {
				sql.setString(1, sourceURL);
			} else {
				sql.setString(1, url);
			}
			sql.setString(2, language);
			sql.setTimestamp(3, timestamp);
			if (Controller.storeSources) {
				sql.setString(4, text);
			} else {
				sql.setString(4, "");
			}
			sql.setString(5, "");
			sql.setInt(6, Controller.run);
			sql.executeUpdate();
		}
		System.out.println("Updated sources");
		try {
			ResultSet res = statement.executeQuery(
					"SELECT * FROM  sources where url = '" + sourceURL + "' AND run ='" + Controller.run + "'");
			res.next();
			int id = res.getInt("sourceId");

			for (int i = 0; i < concepts.length;) {
				if (concepts[1] != "") {
					try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
							"INSERT INTO concepts (text, relevance, website_Link, dbpedia_Link, sourcesID)"
									+ " VALUES (?,?,?,?,?)");) {
						sql.setString(1, concepts[i]);
						i++;
						sql.setDouble(2, Double.parseDouble(concepts[i]));
						i++;
						sql.setString(3, concepts[i]);
						i++;
						sql.setString(4, concepts[i]);
						i++;
						sql.setInt(5, id);
						sql.executeUpdate();
						System.out.println("Updated concepts");
					}
				} else {
					i = 4;
				}
			}
			for (int i = 0; i < keywords.length;) {
				if (keywords[0] != "" && keywords[1] != "") {
					try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
							"INSERT INTO keywords (relevance, sentiment, text, anger, disgust, fear, joy, sadness, sourcesID)"
									+ " VALUES (?,?,?,?,?,?,?,?,?)");) {
						sql.setDouble(1, Double.parseDouble(keywords[i]));
						i++;
						sql.setDouble(2, Double.parseDouble(keywords[i]));
						i++;
						sql.setString(3, keywords[i]);
						i++;
						sql.setDouble(4, Double.parseDouble(keywords[i]));
						i++;
						sql.setDouble(5, Double.parseDouble(keywords[i]));
						i++;
						sql.setDouble(6, Double.parseDouble(keywords[i]));
						i++;
						sql.setDouble(7, Double.parseDouble(keywords[i]));
						i++;
						sql.setDouble(8, Double.parseDouble(keywords[i]));
						i++;
						sql.setInt(9, id);
						sql.executeUpdate();
						System.out.println("Updated keywords");
					}
				} else {
					i = 3;
				}
			}
			for (int i = 0; i < entity.length;) {
				if (entity[1] != "" && entity[2] != "" && entity[3] != "") {
					try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
							"INSERT INTO entities (type, relevance, sentiment, count, text, website_Link, dbpedia_Link, sourcesID)"
									+ " VALUES (?,?,?,?,?,?,?,?)");) {
						sql.setString(1, entity[i]);
						i++;
						sql.setDouble(2, Double.parseDouble(entity[i]));
						i++;
						sql.setDouble(3, Double.parseDouble(entity[i]));
						i++;
						sql.setInt(4, Integer.parseInt(entity[i]));
						i++;
						sql.setString(5, entity[i]);
						i++;
						sql.setString(6, entity[i]);
						i++;
						sql.setString(7, entity[i]);
						i++;
						sql.setInt(8, id);
						sql.executeUpdate();
						System.out.println("Updated entity");
					}
				} else {
					i = 7;
				}
			}
		} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
			System.err.println(e.getStackTrace());
		} catch (java.sql.SQLException e) {
			System.err.println(e.getStackTrace());
		}
	}

	public void parseJson(String alchemy) {
		int a = 0;
		// source
		/*
		 * going through JSon step by step saving necessary data in String
		 * Arrays. Each n fields are equal to one keyword/concept/entity. If
		 * parts are empty in the JSon the arrays are also filled up with empty
		 * Strings or fixed values (0 for integers and doubles except emotions
		 * from keywords) to be easy to identify later.
		 */
		JSONObject json = new JSONObject(alchemy);
		if (json.toString().contains("url")) {
			sourceURL = json.get("url").toString();
		} else {
			sourceURL = "";
		}
		if (json.toString().contains("language")) {
			language = json.get("language").toString();
		} else {
			language = "";
		}
		// entities
		if (json.toString().contains("entities")) {
			String ent = json.get("entities").toString();
			if (ent.contains("[]")) {
				entity[a] = "";
				a++;
				entity[a] = "";
				a++;
				entity[a] = "";
				a++;
				entity[a] = "";
				a++;
				entity[a] = "0";
				a++;
				entity[a] = "";
				a++;
				entity[a] = "0";
				a++;
			} else {
				// preparing String ent to be used for JSONObject
				if (ent.startsWith("[") && ent.endsWith("]")) {
					ent = ent.substring(1, ent.length() - 1);
				}
				String[] entArray = ent.split("(},\\{)");
				if (entArray.length > 1) {
					entArray[0] = entArray[0] + "}";
					entArray[entArray.length - 1] = "{" + entArray[entArray.length - 1];
					for (int i = 1; i < entArray.length - 1; i++) {
						entArray[i] = "{" + entArray[i] + "}";
					}
				}
				// setting 7 fields for each entity
				entity = new String[entArray.length * 7];
				for (int i = 0; i < entArray.length; i++) {
					JSONObject entities = new JSONObject(entArray[i]);
					if (entities.toString().contains("type")) {
						entity[a] = entities.getString("type");
					} else {
						entity[a] = "";
					}
					a++;

					if (entities.toString().contains("relevance")) {
						entity[a] = "" + entities.getDouble("relevance");
					} else {
						entity[a] = "0";
					}
					a++;
					if (entities.toString().contains("sentiment")) {
						String entSentiment = entities.get("sentiment").toString();
						JSONObject entitiesSentiment = new JSONObject(entSentiment);
						if (entitiesSentiment.toString().contains("type")) {
							String entSentType = entitiesSentiment.get("type").toString();
							if (entSentType != "neutral" && entitiesSentiment.toString().contains("score")) {
								entity[a] = "" + entitiesSentiment.getDouble("score");
							} else {
								entity[a] = "0";
							}
						} else {
							entity[a] = "0";
						}
						a++;
					} else {
						entity[a] = "0";
						a++;
					}
					if (entities.toString().contains("count")) {
						entity[a] = "" + entities.getInt("count");
					} else {
						entity[a] = "0";
					}
					a++;
					if (entities.toString().contains("text")) {
						entity[a] = entities.getString("text");
					} else {
						entity[a] = "";
					}
					a++;
					if (entities.toString().contains("disambiguated")) {
						String entLink = entities.get("disambiguated").toString();
						JSONObject entLinks = new JSONObject(entLink);
						if (entLinks.toString().contains("website")) {
							entity[a] = entLinks.getString("website");
						} else {
							entity[a] = "";
						}
						a++;
						if (entLinks.toString().contains("dbpedia")) {
							entity[a] = entLinks.getString("dbpedia");
						} else {
							entity[a] = "";
						}
						a++;
					} else {
						entity[a] = "";
						a++;
						entity[a] = "";
						a++;
					}
				}
			}
		} else {
			entity[a] = "";
			a++;
			entity[a] = "";
			a++;
			entity[a] = "";
			a++;
			entity[a] = "";
			a++;
			entity[a] = "0";
			a++;
			entity[a] = "";
			a++;
			entity[a] = "0";
			a++;
		}
		// keywords
		// missing emotions will be saved as -10 to be easy to identify later
		a = 0;
		if (json.toString().contains("keywords")) {
			String key = json.get("keywords").toString();
			if (key.contains("[]")) {
				keywords[a] = "";
				a++;
				keywords[a] = "";
				a++;
				keywords[a] = "0";
				a++;
				keywords[a] = "" + -10;
				a++;
				keywords[a] = "" + -10;
				a++;
				keywords[a] = "" + -10;
				a++;
				keywords[a] = "" + -10;
				a++;
				keywords[a] = "" + -10;
				a++;
			} else {
				// preparing String key to be used for JSONObject
				key = key.substring(1, key.length() - 1);
				String[] keyArray = key.split("(},\\{)");
				if (keyArray.length > 1) {
					keyArray[0] = keyArray[0] + "}";
					keyArray[keyArray.length - 1] = "{" + keyArray[keyArray.length - 1];
					for (int i = 1; i < keyArray.length - 1; i++) {
						keyArray[i] = "{" + keyArray[i] + "}";
					}
				}
				// setting 8 fields for each keyword
				keywords = new String[keyArray.length * 8];
				for (int i = 0; i < keyArray.length; i++) {
					JSONObject keyword = new JSONObject(keyArray[i]);
					if (keyword.toString().contains("relevance")) {
						keywords[a] = keyword.getString("relevance");
					} else {
						keywords[a] = "0";
					}
					JSONObject keySentiment = new JSONObject(keyword.get("sentiment").toString());
					a++;
					if (keySentiment.toString().contains("score")) {
						keywords[a] = keySentiment.getString("score");
					} else {
						keywords[a] = "0";
					}
					a++;
					if (keyword.toString().contains("text")) {
						keywords[a] = keyword.getString("text");
					} else {
						keywords[a] = "";
					}
					a++;
					if (keyword.toString().contains("emotions")) {
						String emotion = keyword.get("emotions").toString();
						JSONObject emotions = new JSONObject(emotion);
						if (emotion.contains("anger")) {
							keywords[a] = emotions.getString("anger");
						} else {
							keywords[a] = "" + -10;
						}
						a++;
						if (emotion.contains("disgust")) {
							keywords[a] = emotions.getString("disgust");
						} else {
							keywords[a] = "" + -10;
						}
						a++;
						if (emotion.contains("fear")) {
							keywords[a] = emotions.getString("fear");
						} else {
							keywords[a] = "" + -10;
						}
						a++;
						if (emotion.contains("joy")) {
							keywords[a] = emotions.getString("joy");
						} else {
							keywords[a] = "" + -10;
						}
						a++;
						if (emotion.contains("sadness")) {
							keywords[a] = emotions.getString("sadness");
						} else {
							keywords[a] = "" + -10;
						}
						a++;
					} else {
						keywords[a] = "" + -10;
						a++;
						keywords[a] = "" + -10;
						a++;
						keywords[a] = "" + -10;
						a++;
						keywords[a] = "" + -10;
						a++;
						keywords[a] = "" + -10;
						a++;
					}
				}
			}
		} else {
			keywords[a] = "0";
			a++;
			keywords[a] = "0";
			a++;
			keywords[a] = "";
			a++;
			keywords[a] = "" + -10;
			a++;
			keywords[a] = "" + -10;
			a++;
			keywords[a] = "" + -10;
			a++;
			keywords[a] = "" + -10;
			a++;
			keywords[a] = "" + -10;
			a++;
		}
		// concepts
		a = 0;
		if (json.toString().contains("concepts")) {
			String con = json.get("concepts").toString();
			if (con.contains("[]")) {
				concepts[a] = "";
				a++;
				concepts[a] = "";
				a++;
				concepts[a] = "";
				a++;
				concepts[a] = "";
				a++;
			} else {
				// preparing String con to be used for JSONObject
				con = con.substring(1, con.length() - 1);
				String[] conArray = con.split("(},\\{)");
				if (conArray.length > 1) {
					conArray[0] = conArray[0] + "}";
					conArray[conArray.length - 1] = "{" + conArray[conArray.length - 1];
					for (int i = 1; i < conArray.length - 1; i++) {
						conArray[i] = "{" + conArray[i] + "}";
					}
				}
				// setting for fields for each concept
				concepts = new String[conArray.length * 4];
				for (int i = 0; i < conArray.length; i++) {

					JSONObject concept = new JSONObject(conArray[i]);
					if (concept.toString().contains("text")) {
						concepts[a] = concept.getString("text");
					} else {
						concepts[a] = "";
					}
					a++;
					if (concept.toString().contains("relevance")) {
						concepts[a] = concept.getString("relevance");
					} else {
						concepts[a] = "0";
					}
					a++;
					if (concept.toString().contains("website")) {
						concepts[a] = concept.getString("website");
					} else {
						concepts[a] = "";
					}
					a++;
					if (concept.toString().contains("dbpedia")) {
						concepts[a] = concept.getString("dbpedia");
					} else {
						concepts[a] = "";
					}
					a++;
				}
			}
		} else {
			concepts[a] = "";
			a++;
			concepts[a] = "0";
			a++;
			concepts[a] = "";
			a++;
			concepts[a] = "";
			a++;
		}
	}

	public void checkWhiteList(String[] whiteList) {
		for (int i = 0; i < whiteList.length; i++) {
			if (text.toLowerCase().contains(whiteList[i].toLowerCase())) {
				passed = true;
				break;
			}
		}
	}
}