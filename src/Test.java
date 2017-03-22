import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONObject;

public class Test {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "");
		Statement statement = connection.createStatement();

		/*
		 * String sql = "INSERT INTO test" + "VALUES (100, 'Zara', 'Ali', 18)";
		 * statement.executeUpdate(sql);
		 */

		int a = 0;
		// source attributes
		String sourceURL = "";
		String language = "";
		// entities attributes
		String[] entity = new String[7];
		// keywords attributes
		String[] keywords = new String[3];
		// concepts
		String[] concepts = new String[4];
		// source
		JSONObject json = new JSONObject(
				"{    \"status\": \"OK\",    \"warningMessage\": \"certain-operations-excluded-due-to-unsupported-text-language\",    \"usage\": \"By accessing AlchemyAPI or using information generated by AlchemyAPI, you are agreeing to be bound by the AlchemyAPI Terms of Use: http://www.alchemyapi.com/company/terms.html\",    \"url\": \"http://www.spiegel.de/panorama/\",    \"totalTransactions\": \"4\",    \"language\": \"german\",    \"concepts\": [ ],    \"entities\": [        {            \"count\": \"1\",            \"relevance\": \"0.950395\",            \"sentiment\": {                \"score\": \"0\",                \"type\": \"neutral\"            },            \"text\": \"Peru\",            \"type\": \"Country\"        },        {            \"count\": \"1\",            \"relevance\": \"0.878171\",            \"sentiment\": {                \"score\": \"0\",                \"type\": \"neutral\"            },            \"text\": \"Virginias\",            \"type\": \"StateOrCounty\"        },        {            \"count\": \"1\",            \"relevance\": \"0.827297\",            \"sentiment\": {                \"score\": \"0\",                \"type\": \"neutral\"            },            \"text\": \"\u00A0Forum\u00A0\",            \"type\": \"Person\"        }    ],    \"keywords\": [        {            \"relevance\": \"0.938722\",            \"sentiment\": {                \"score\": \"0.169978\",                \"type\": \"positive\"            },            \"text\": \"zur\u00FCckgegeben Virginias Regierungschef\"        },        {            \"relevance\": \"0.920203\",            \"sentiment\": {                \"type\": \"neutral\"            },            \"text\": \"H\u00E4fen vor\u00FCbergehend geschlossen\"        },        {            \"relevance\": \"0.898273\",            \"sentiment\": {                \"score\": \"0.314421\",\"type\": \"positive\"},\"text\": \"fr\u00FChere Seeleute begnadigt\"}]}");
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
						keywords = new String[keyArray.length * 3];
						for (int i = 0; i < keyArray.length; i++) {

							JSONObject keyword = new JSONObject(keyArray[i]);
							if (keyword.toString().contains("text")) {
								keywords[a] = keyword.getString("text");
							} else {
								keywords[a] = "";
							}
							a++;
							if (keyword.toString().contains("relevance")) {
								keywords[a] = keyword.getString("relevance");
							} else {
								keywords[a] = "";
							}
							JSONObject keySentiment = new JSONObject(keyword.get("sentiment").toString());
							a++;
							if (keySentiment.toString().contains("score")) {
								keywords[a] = keySentiment.getString("score");
							} else {
								keywords[a] = "0";
							}
							a++;
						}
					}
				} else {
					keywords[a] = "";
					a++;
					keywords[a] = "";
					a++;
					keywords[a] = "0";
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
								concepts[a] = "";
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
					concepts[a] = "";
					a++;
					concepts[a] = "";
					a++;
					concepts[a] = "";
					a++;
				}
				
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

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
