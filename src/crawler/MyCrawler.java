package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
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
	private static final String ALCHEMY_KEY = System.getenv("CRAWLER_ALCHEMY_KEY");
	private static final String[] WHITELIST_CRAWLER = System.getenv("CRAWLER_WHITELIST_CRAWLER").split(";");
	private static final String[] WHITELIST_ANALYSIS = System.getenv("CRAWLER_WHITELIST_ANALYSIS").split(";");

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
			AlchemyResults gatheredData = new AlchemyResults();
			boolean passed = false;
			gatheredData.setUrl(page.getWebURL().getURL());
			System.out.println("URL: " + gatheredData.getUrl());
			if(gatheredData.getUrl().contains("twitter.com")||gatheredData.getUrl().contains("facebook.com")||gatheredData.getUrl().contains("youtube.com")){
				return;
			}
			try {
				String urlString = Controller.kpiManagerURL + "-visited";
				URL url = new URL(urlString);
				URLConnection conn = url.openConnection();
				InputStream is = conn.getInputStream();
			} catch (IOException e) {
			}
			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				gatheredData.setText(htmlParseData.getText());
				// check text with whitelistCrawler
				passed = checkWhiteList(WHITELIST_CRAWLER, passed, gatheredData.getText());
				if (passed == false) {
					return;
				}
				try {
					String urlString = Controller.kpiManagerURL + "-stored";
					URL url = new URL(urlString);
					URLConnection conn = url.openConnection();
					InputStream is = conn.getInputStream();
				} catch (IOException e) {
				}
				if (Controller.enableAlchemy) {
					useAlchemy(WHITELIST_ANALYSIS, gatheredData);
				}
				try (Connection connection = DriverManager.getConnection(
						"jdbc:mysql://" + Controller.host + ":" + Controller.port + "/" + Controller.name + "",
						"" + Controller.user, "" + Controller.password)) {
					Statement statement = connection.createStatement();

					try {
						if (Controller.restart == false) {

							ResultSet res2 = statement
									.executeQuery("SELECT * FROM  sources WHERE url ='" + gatheredData.getUrl() + "'");
							res2.next();
							Controller.run = res2.getInt("run");
							Controller.restart = true;
							res2.close();
						}
					} catch (Exception e) {
						Controller.restart = true;
						Controller.run = 1;
					}
					accessDB(connection, statement, gatheredData);
					connection.close();
					statement.close();
				}
			}
		} catch (IOException | SQLException | ParseException | InterruptedException ex) {
			onUnhandledException(page.getWebURL(), ex);
		}

	}

	public void useAlchemy(String[] whiteList, AlchemyResults gatheredData)
			throws InterruptedException, UnsupportedEncodingException, IOException {
		// access Alchemy to receive JSon containing keywords, entities and
		// concepts
		URL urls = new URL(
				"https://gateway-a.watsonplatform.net/calls/url/URLGetCombinedData?url=" + gatheredData.getUrl()
						+ "&outputMode=json&extract=keywords,entities,concepts&sentiment=1&maxRetrieve=3&apikey="
						+ ALCHEMY_KEY);

		String alchemy = "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
			for (String line; (line = reader.readLine()) != null;) {
				alchemy = alchemy + line;
				System.out.println(line);

			}
		}
		gatheredData = parseJson(alchemy, gatheredData.getText());
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
		boolean passed = false;
		passed = checkWhiteList(whiteList, passed, gatheredData.getText());
		if (passed == false) {
			return;
		}
	}

	public void accessDB(Connection connection, Statement statement, AlchemyResults gatheredData)
			throws SQLException, ParseException {
		// receiving current timestamp
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String lastVisit = "";
		lastVisit = timeFormat.format(Calendar.getInstance().getTime());
		java.util.Date time = timeFormat.parse(lastVisit);
		java.sql.Timestamp timestamp = new java.sql.Timestamp(time.getTime());

		try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO sources (url, language, time, data, country, run)" + " VALUES (?,?,?,?,?,?)");) {

			sql.setString(1, gatheredData.getUrl());
			sql.setString(2, gatheredData.getLanguage());
			sql.setTimestamp(3, timestamp);
			if (Controller.storeSources) {
				sql.setString(4, gatheredData.getText());
			} else {
				sql.setString(4, "");
			}
			sql.setString(5, "");
			sql.setInt(6, Controller.run);
			sql.executeUpdate();
		}
		System.out.println("Updated sources");
		try {
			ResultSet res = statement.executeQuery("SELECT * FROM  sources where url = '" + gatheredData.getUrl()
					+ "' AND run ='" + Controller.run + "'");
			res.next();
			int id = res.getInt("sourceId");
			res.close();

			for (int i = 0; i < gatheredData.getConcepts().length;) {
				if (gatheredData.getConcept(1) != "") {
					try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
							"INSERT INTO concepts (text, relevance, website_Link, dbpedia_Link, sourcesID)"
									+ " VALUES (?,?,?,?,?)");) {
						sql.setString(1, gatheredData.getConcept(i));
						i++;
						sql.setDouble(2, Double.parseDouble(gatheredData.getConcept(i)));
						i++;
						sql.setString(3, gatheredData.getConcept(i));
						i++;
						sql.setString(4, gatheredData.getConcept(i));
						i++;
						sql.setInt(5, id);
						sql.executeUpdate();
						System.out.println("Updated concepts");
					}
				} else {
					i = 4;
				}
			}
			for (int i = 0; i < gatheredData.getKeywords().length;) {
				if (gatheredData.getKeyword(0) != "" && gatheredData.getKeyword(1) != "") {
					try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
							"INSERT INTO keywords (relevance, sentiment, text, anger, disgust, fear, joy, sadness, sourcesID)"
									+ " VALUES (?,?,?,?,?,?,?,?,?)");) {
						sql.setDouble(1, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setDouble(2, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setString(3, gatheredData.getKeyword(i));
						i++;
						sql.setDouble(4, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setDouble(5, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setDouble(6, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setDouble(7, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setDouble(8, Double.parseDouble(gatheredData.getKeyword(i)));
						i++;
						sql.setInt(9, id);
						sql.executeUpdate();
						System.out.println("Updated keywords");
					}
				} else {
					i = 3;
				}
			}
			for (int i = 0; i < gatheredData.getEntities().length;) {
				if (gatheredData.getEntity(1) != "" && gatheredData.getEntity(2) != ""
						&& gatheredData.getEntity(3) != "") {
					try (PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
							"INSERT INTO entities (type, relevance, sentiment, count, text, website_Link, dbpedia_Link, sourcesID)"
									+ " VALUES (?,?,?,?,?,?,?,?)");) {
						sql.setString(1, gatheredData.getEntity(i));
						i++;
						sql.setDouble(2, Double.parseDouble(gatheredData.getEntity(i)));
						i++;
						sql.setDouble(3, Double.parseDouble(gatheredData.getEntity(i)));
						i++;
						sql.setInt(4, Integer.parseInt(gatheredData.getEntity(i)));
						i++;
						sql.setString(5, gatheredData.getEntity(i));
						i++;
						sql.setString(6, gatheredData.getEntity(i));
						i++;
						sql.setString(7, gatheredData.getEntity(i));
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
			System.out.println(e.getStackTrace());
		} catch (java.sql.SQLException e) {
			System.out.println(e.getStackTrace());
		}
		connection.close();
	}

	public AlchemyResults parseJson(String alchemy, String text) {
		AlchemyResults gatheredData = new AlchemyResults();
		gatheredData.setText(text);
		int arrayField = 0;
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
			gatheredData.setUrl(json.get("url").toString());
		} else {
			gatheredData.setUrl("");
		}
		if (json.toString().contains("language")) {
			gatheredData.setLanguage(json.get("language").toString());
		} else {
			gatheredData.setLanguage("");
		}
		// entities
		if (json.toString().contains("entities")) {
			String ent = json.get("entities").toString();
			if (ent.contains("[]")) {
				gatheredData.setEntity("", arrayField);
				arrayField++;
				gatheredData.setEntity("", arrayField);
				arrayField++;
				gatheredData.setEntity("", arrayField);
				arrayField++;
				gatheredData.setEntity("", arrayField);
				arrayField++;
				gatheredData.setEntity("0", arrayField);
				arrayField++;
				gatheredData.setEntity("", arrayField);
				arrayField++;
				gatheredData.setEntity("0", arrayField);
				arrayField++;
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
				gatheredData.setEntities(new String[entArray.length * 7]);
				for (int i = 0; i < entArray.length; i++) {
					JSONObject entities = new JSONObject(entArray[i]);
					if (entities.toString().contains("type")) {
						gatheredData.setEntity(entities.getString("type"), arrayField);
					} else {
						gatheredData.setEntity("", arrayField);
					}
					arrayField++;

					if (entities.toString().contains("relevance")) {
						gatheredData.setEntity("" + entities.getDouble("relevance"), arrayField);
					} else {
						gatheredData.setEntity("0", arrayField);
					}
					arrayField++;
					if (entities.toString().contains("sentiment")) {
						String entSentiment = entities.get("sentiment").toString();
						JSONObject entitiesSentiment = new JSONObject(entSentiment);
						if (entitiesSentiment.toString().contains("type")) {
							String entSentType = entitiesSentiment.get("type").toString();
							if (entSentType != "neutral" && entitiesSentiment.toString().contains("score")) {
								gatheredData.setEntity("" + entitiesSentiment.getDouble("score"), arrayField);
							} else {
								gatheredData.setEntity("0", arrayField);
							}
						} else {
							gatheredData.setEntity("0", arrayField);
						}
						arrayField++;
					} else {
						gatheredData.setEntity("0", arrayField);
						arrayField++;
					}
					if (entities.toString().contains("count")) {
						gatheredData.setEntity("" + entities.getInt("count"), arrayField);
					} else {
						gatheredData.setEntity("0", arrayField);
					}
					arrayField++;
					if (entities.toString().contains("text")) {
						gatheredData.setEntity(entities.getString("text"), arrayField);
					} else {
						gatheredData.setEntity("", arrayField);
					}
					arrayField++;
					if (entities.toString().contains("disambiguated")) {
						String entLink = entities.get("disambiguated").toString();
						JSONObject entLinks = new JSONObject(entLink);
						if (entLinks.toString().contains("website")) {
							gatheredData.setEntity(entLinks.getString("website"), arrayField);
						} else {
							gatheredData.setEntity("", arrayField);
						}
						arrayField++;
						if (entLinks.toString().contains("dbpedia")) {
							gatheredData.setEntity(entLinks.getString("dbpedia"), arrayField);
						} else {
							gatheredData.setEntity("", arrayField);
						}
						arrayField++;
					} else {
						gatheredData.setEntity("", arrayField);
						arrayField++;
						gatheredData.setEntity("", arrayField);
						arrayField++;
					}
				}
			}
		} else {
			gatheredData.setEntity("", arrayField);
			arrayField++;
			gatheredData.setEntity("", arrayField);
			arrayField++;
			gatheredData.setEntity("", arrayField);
			arrayField++;
			gatheredData.setEntity("", arrayField);
			arrayField++;
			gatheredData.setEntity("0", arrayField);
			arrayField++;
			gatheredData.setEntity("", arrayField);
			arrayField++;
			gatheredData.setEntity("0", arrayField);
			arrayField++;
		}
		// keywords
		// missing emotions will be saved as -10 to be easy to identify later
		arrayField = 0;
		if (json.toString().contains("keywords")) {
			String key = json.get("keywords").toString();
			if (key.contains("[]")) {
				gatheredData.setKeyword("", arrayField);
				arrayField++;
				gatheredData.setKeyword("", arrayField);
				arrayField++;
				gatheredData.setKeyword("0", arrayField);
				arrayField++;
				gatheredData.setKeyword("" + -10, arrayField);
				arrayField++;
				gatheredData.setKeyword("" + -10, arrayField);
				arrayField++;
				gatheredData.setKeyword("" + -10, arrayField);
				arrayField++;
				gatheredData.setKeyword("" + -10, arrayField);
				arrayField++;
				gatheredData.setKeyword("" + -10, arrayField);
				arrayField++;
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
				gatheredData.setKeywords(new String[keyArray.length * 8]);
				for (int i = 0; i < keyArray.length; i++) {
					JSONObject keyword = new JSONObject(keyArray[i]);
					if (keyword.toString().contains("relevance")) {
						gatheredData.setKeyword(keyword.getString("relevance"), arrayField);
					} else {
						gatheredData.setKeyword("0", arrayField);
					}
					JSONObject keySentiment = new JSONObject(keyword.get("sentiment").toString());
					arrayField++;
					if (keySentiment.toString().contains("score")) {
						gatheredData.setKeyword(keySentiment.getString("score"), arrayField);
					} else {
						gatheredData.setKeyword("0", arrayField);
					}
					arrayField++;
					if (keyword.toString().contains("text")) {
						gatheredData.setKeyword(keyword.getString("text"), arrayField);
					} else {
						gatheredData.setKeyword("", arrayField);
					}
					arrayField++;
					if (keyword.toString().contains("emotions")) {
						String emotion = keyword.get("emotions").toString();
						JSONObject emotions = new JSONObject(emotion);
						if (emotion.contains("anger")) {
							gatheredData.setKeyword(emotions.getString("anger"), arrayField);
						} else {
							gatheredData.setKeyword("" + -10, arrayField);
						}
						arrayField++;
						if (emotion.contains("disgust")) {
							gatheredData.setKeyword(emotions.getString("disgust"), arrayField);
						} else {
							gatheredData.setKeyword("" + -10, arrayField);
						}
						arrayField++;
						if (emotion.contains("fear")) {
							gatheredData.setKeyword(emotions.getString("fear"), arrayField);
						} else {
							gatheredData.setKeyword("" + -10, arrayField);
						}
						arrayField++;
						if (emotion.contains("joy")) {
							gatheredData.setKeyword(emotions.getString("joy"), arrayField);
						} else {
							gatheredData.setKeyword("" + -10, arrayField);
						}
						arrayField++;
						if (emotion.contains("sadness")) {
							gatheredData.setKeyword(emotions.getString("sadness"), arrayField);
						} else {
							gatheredData.setKeyword("" + -10, arrayField);
						}
						arrayField++;
					} else {
						gatheredData.setKeyword("" + -10, arrayField);
						arrayField++;
						gatheredData.setKeyword("" + -10, arrayField);
						arrayField++;
						gatheredData.setKeyword("" + -10, arrayField);
						arrayField++;
						gatheredData.setKeyword("" + -10, arrayField);
						arrayField++;
						gatheredData.setKeyword("" + -10, arrayField);
						arrayField++;
					}
				}
			}
		} else {
			gatheredData.setKeyword("", arrayField);
			arrayField++;
			gatheredData.setKeyword("", arrayField);
			arrayField++;
			gatheredData.setKeyword("0", arrayField);
			arrayField++;
			gatheredData.setKeyword("" + -10, arrayField);
			arrayField++;
			gatheredData.setKeyword("" + -10, arrayField);
			arrayField++;
			gatheredData.setKeyword("" + -10, arrayField);
			arrayField++;
			gatheredData.setKeyword("" + -10, arrayField);
			arrayField++;
			gatheredData.setKeyword("" + -10, arrayField);
			arrayField++;
		}
		// concepts
		arrayField = 0;
		if (json.toString().contains("concepts")) {
			String con = json.get("concepts").toString();
			if (con.contains("[]")) {
				gatheredData.setConcept("", arrayField);
				arrayField++;
				gatheredData.setConcept("0", arrayField);
				arrayField++;
				gatheredData.setConcept("", arrayField);
				arrayField++;
				gatheredData.setConcept("", arrayField);
				arrayField++;
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
				gatheredData.setConcepts(new String[conArray.length * 4]);
				for (int i = 0; i < conArray.length; i++) {

					JSONObject concept = new JSONObject(conArray[i]);
					if (concept.toString().contains("text")) {
						gatheredData.setConcept(concept.getString("text"), arrayField);
					} else {
						gatheredData.setConcept("", arrayField);
					}
					arrayField++;
					if (concept.toString().contains("relevance")) {
						gatheredData.setConcept(concept.getString("relevance"), arrayField);
					} else {
						gatheredData.setConcept("0", arrayField);
					}
					arrayField++;
					if (concept.toString().contains("website")) {
						gatheredData.setConcept(concept.getString("website"), arrayField);
					} else {
						gatheredData.setConcept("", arrayField);
					}
					arrayField++;
					if (concept.toString().contains("dbpedia")) {
						gatheredData.setConcept(concept.getString("dbpedia"), arrayField);
					} else {
						gatheredData.setConcept("", arrayField);
					}
					arrayField++;
				}
			}
		} else {
			gatheredData.setConcept("", arrayField);
			arrayField++;
			gatheredData.setConcept("0", arrayField);
			arrayField++;
			gatheredData.setConcept("", arrayField);
			arrayField++;
			gatheredData.setConcept("", arrayField);
			arrayField++;
		}
		return gatheredData;
	}

	public boolean checkWhiteList(String[] whiteList, boolean passed, String text) {
		for (int i = 0; i < whiteList.length; i++) {
			if (text.toLowerCase().contains(whiteList[i].toLowerCase())) {
				passed = true;
				return passed;
			}
		}
		return passed;
	}
}