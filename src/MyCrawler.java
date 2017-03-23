import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONObject;
import org.xml.sax.SAXException;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {

	// source attributes
	String sourceURL = "";
	String language = "";
	// entities attributes
	String[] entity = new String[7];
	// keywords attributes
	String[] keywords = new String[3];
	// concepts
	String[] concepts = new String[4];

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
	 */
	@Override
	public void visit(Page page) throws XPathExpressionException, IOException, SAXException,
			ParserConfigurationException, SQLException, InterruptedException {
		String url = page.getWebURL().getURL();
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String date = "";
		String nextVisit = "";
		System.out.println("URL: " + url);

		String wlAnaPath = "C:\\Users\\ml538117\\Desktop\\Pfad.txt";
		String wlCrPath = "C:\\Users\\ml538117\\Desktop\\Pfad.txt";
		String[] wlAnalysis = readFile(wlAnaPath, StandardCharsets.UTF_8).split(",");
		String[] wlCrawler = readFile(wlCrPath, StandardCharsets.UTF_8).split(",");

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (page.getParseData() instanceof HtmlParseData) {
			boolean passed = false;
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			// check text with whitelistCrawler
			for (int i = 0; i < wlCrawler.length; i++) {
				if (text.toLowerCase().contains(wlCrawler[i].toLowerCase())) {
					passed = true;
					break;
				}
			}
			if (passed == false) {
				return;
			}
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			URL urls = new URL("https://gateway-a.watsonplatform.net/calls/url/URLGetCombinedData?url=" + url
					+ "&outputMode=json&extract=keywords,entities,concepts&sentiment=1&maxRetrieve=3&apikey=ddc06944c93c23c9cfd6e6bbbb6cd5c00e7bf18b");
			Controller.counter++;
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

			// add JsonParser here + fit gathered data in StringArray --> then
			// save in db

			// check analysis with whitelistAnalysis
			passed = false;
			for (int i = 0; i < wlAnalysis.length; i++) {
				if (text.toLowerCase().contains(wlAnalysis[i].toLowerCase())) {
					passed = true;
					break;
				}
			}
			if (passed == false) {
				return;
			}
			date = timeFormat.format(Calendar.getInstance().getTime());
			String c = date.substring(4, 6);
			int nextTime = Integer.parseInt(c) + 1;
			if (nextTime == 13) {
				c = date.substring(0, 4);
				nextTime = Integer.parseInt(c) + 1;
				nextVisit = date.substring(6, 8) + ".01." + nextTime;
			} else {
				nextVisit = date.substring(6, 8) + "." + nextTime + "." + date.substring(0, 4);
			}
			date = date.substring(6, 8) + "." + date.substring(4, 6) + "." + date.substring(0, 4);

			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://" + Controller.host + ":" + Controller.port + "/demo", "" + Controller.user,
					"" + Controller.password);
			Statement statement = connection.createStatement();
			String sql = "INSERT INTO demo" + "sources (" + sourceURL + ", " + language + ", " + date + ", " + nextVisit
					+ ")";
			statement.executeUpdate(sql);

			/*
			 * System.out.println("Text length: " + text.length());
			 * System.out.println("Html length: " + html.length());
			 * System.out.println("Number of outgoing links: " + links.size());
			 */

			System.out.println("Websites visited: " + Controller.counter);
			System.out.println(date);

			for (int i = 0; i < concepts.length; i++) {
				System.out.println(concepts[i]);
			}
			for (int i = 0; i < entity.length; i++) {
				System.out.println(entity[i]);
			}
			for (int i = 0; i < keywords.length; i++) {
				System.out.println(keywords[i]);
			}
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public void parseJson(String alchemy) {
		int a = 0;
		// source
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
				key = key.substring(1, key.length() - 1);
				String[] keyArray = key.split("(},\\{)");
				if (keyArray.length > 1) {
					keyArray[0] = keyArray[0] + "}";
					keyArray[keyArray.length - 1] = "{" + keyArray[keyArray.length - 1];
					for (int i = 1; i < keyArray.length - 1; i++) {
						keyArray[i] = "{" + keyArray[i] + "}";
					}
				}
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
				con = con.substring(1, con.length() - 1);
				String[] conArray = con.split("(},\\{)");
				if (conArray.length > 1) {
					conArray[0] = conArray[0] + "}";
					conArray[conArray.length - 1] = "{" + conArray[conArray.length - 1];
					for (int i = 1; i < conArray.length - 1; i++) {
						conArray[i] = "{" + conArray[i] + "}";
					}
				}
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
}