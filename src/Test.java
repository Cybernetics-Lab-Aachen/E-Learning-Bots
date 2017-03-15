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
	      
	    /*  String sql = "INSERT INTO test"  +
	                  "VALUES (100, 'Zara', 'Ali', 18)";
	      statement.executeUpdate(sql);*/
    
	//source attributes
	String sourceURL = "";
	String language = "";
	//entities attributes
	int entCount = 0;
	double entRelevance = 0;
	double entSentScore = 0;
	String entType = "";
	String entText = "";
	String entWeb = "";
	String entDB = "";
	//keywords attributes
	String [] keywords = new String [3];
	//source
	JSONObject json = new JSONObject("{\"status\": \"OK\",\"usage\": \"By accessing AlchemyAPI or using information generated by AlchemyAPI, you are agreeing to be bound by the AlchemyAPI Terms of Use: http://www.alchemyapi.com/company/terms.html\",\"url\": \"http://www.spiegel.de/international/germany/after-terror-attack-germany-examines-security-architecture-a-1128917.html\",\"totalTransactions\": \"5\",\"language\": \"english\",\"concepts\": [{\"text\": \"Germany\",\"relevance\": \"0.961389\",\"website\": \"http://www.deutschland.de/\",\"dbpedia\": \"http://dbpedia.org/resource/Germany\",\"ciaFactbook\": \"http://www4.wiwiss.fu-berlin.de/factbook/resource/Germany\",\"freebase\": \"http://rdf.freebase.com/ns/m.0cn004\",\"opencyc\": \"http://sw.opencyc.org/concept/Mx4rvVj4PJwpEbGdrcN5Y29ycA\",\"yago\": \"http://yago-knowledge.org/resource/Germany\"},{\"text\": \"Angela Merkel\",\"relevance\": \"0.85606\",\"dbpedia\": \"http://dbpedia.org/resource/Angela_Merkel\",\"freebase\": \"http://rdf.freebase.com/ns/m.0jl0g\",\"yago\": \"http://yago-knowledge.org/resource/Angela_Merkel\"},{\"text\": \"World War II\",\"relevance\": \"0.854558\",\"dbpedia\": \"http://dbpedia.org/resource/World_War_II\",\"freebase\": \"http://rdf.freebase.com/ns/m.081pw\",\"yago\": \"http://yago-knowledge.org/resource/World_War_II\"}],\"entities\": [{\"type\": \"Country\",\"relevance\": \"0.879944\",\"sentiment\": {\"type\": \"negative\",\"score\": \"-0.450984\"},\"count\": \"17\",\"text\": \"Germany\",\"disambiguated\": {\"subType\": [\"Location\",\"GovernmentalJurisdiction\"],\"name\": \"Germany\",\"website\": \"http://www.deutschland.de/\",\"dbpedia\": \"http://dbpedia.org/resource/Germany\",\"freebase\": \"http://rdf.freebase.com/ns/m.0cn004\",\"ciaFactbook\": \"http://www4.wiwiss.fu-berlin.de/factbook/resource/Germany\",\"opencyc\": \"http://sw.opencyc.org/concept/Mx4rvVj4PJwpEbGdrcN5Y29ycA\",\"yago\": \"http://yago-knowledge.org/resource/Germany\"}},{\"type\": \"Organization\",\"relevance\": \"0.554555\",\"sentiment\": {\"type\": \"negative\",\"score\": \"-0.405531\"},\"count\": \"4\",\"text\": \"federal government\",\"disambiguated\": {\"name\": \"Federal government of the United States\",\"website\": \"http://www.usa.gov/\",\"dbpedia\": \"http://dbpedia.org/resource/Federal_government_of_the_United_States\",\"freebase\": \"http://rdf.freebase.com/ns/m.01bqks\",\"opencyc\": \"http://sw.opencyc.org/concept/Mx4rwQBeUJwpEbGdrcN5Y29ycA\"}},{\"type\": \"Person\",\"relevance\": \"0.55388\",\"sentiment\": {\"type\": \"positive\",\"score\": \"0.382004\"},\"count\": \"7\",\"text\": \"Minister Thomas de Maizi\u00E8re\",\"disambiguated\": {\"subType\": [\"Politician\",\"OfficeHolder\"],\"name\": \"Thomas de Maizi\u00E8re\",\"dbpedia\": \"http://dbpedia.org/resource/Thomas_de_Maizi\u00E8re\",\"freebase\": \"http://rdf.freebase.com/ns/m.08dds4\",\"yago\": \"http://yago-knowledge.org/resource/Thomas_de_Maizi%C3%A8re\"}}],\"keywords\": [{\"relevance\": \"0.991991\",\"sentiment\": {\"score\": \"0.460425\",\"type\": \"positive\"},\"text\": \"federal interior minister\"},{\"relevance\": \"0.988167\",\"sentiment\": {\"score\": \"-0.468031\",\"type\": \"negative\"},\"text\": \"Criminal Police Office\"},{\"relevance\": \"0.953787\",\"sentiment\": {\"mixed\": \"1\",\"score\": \"-0.133709\",\"type\": \"negative\"},\"text\": \"security agencies\"}]}");
	sourceURL = json.get("url").toString();
	language = json.get("language").toString();
	//entities
	String ent = json.get("entities").toString();
	ent = ent.substring(1, ent.length()-1);
	JSONObject entities = new JSONObject(ent);
	entType = entities.getString("type");
	entRelevance = entities.getDouble("relevance");
	String entSentiment = entities.get("sentiment").toString();
	String entLink = entities.get("disambiguated").toString();
	JSONObject entLinks = new JSONObject(entLink);
	entWeb = entLinks.getString("website");
	entDB = entLinks.getString("dbpedia");
	JSONObject entitiesSentiment = new JSONObject(entSentiment);
	String entSentType = entitiesSentiment.get("type").toString();
	entCount = entities.getInt("count");
	entText = entities.getString("text");
	if(entSentType != "neutral"){
		entSentScore = entitiesSentiment.getDouble("score");
	}
	//keywords
	String key = json.get("keywords").toString();
	key = key.substring(1, key.length()-1);
	String[] keyArray = key.split("(},\\{)");
	if(keyArray.length > 1){
		keyArray[0] = keyArray[0] + "}";
		keyArray[keyArray.length - 1] = "{" + keyArray[keyArray.length - 1];
		for(int i = 1; i < keyArray.length - 1; i ++){
			keyArray[i] = "{" + keyArray[i] + "}";
		}
	}
	keywords = new String[keyArray.length*3];
	for(int i = 0; i < keyArray.length; i ++){
		int a = 0;
		JSONObject keyword = new JSONObject(keyArray[i]);
		keywords[a] = keyword.getString("text");
		a++;
		keywords[a] = keyword.getString("relevance");
		JSONObject keySentiment = new JSONObject(keyword.get("sentiment").toString());
		a++;
		keywords[a] = keySentiment.getString("score");
	}

	//JSONObject keywords = new JSONObject(keyArray[i]);
	String wlAnaPath = "C:\\Users\\ml538117\\Desktop\\Pfad.txt";
    String wlCrPath = "C:\\Users\\ml538117\\Desktop\\Pfad.txt";
    String[] wlAnalysis = readFile(wlAnaPath,StandardCharsets.UTF_8).split(",");
    String[] wlCrawler = readFile(wlCrPath,StandardCharsets.UTF_8).split(",");

}
static String readFile(String path, Charset encoding) 
 		  throws IOException 
 		{
 		  byte[] encoded = Files.readAllBytes(Paths.get(path));
 		  return new String(encoded, encoding);
 		}
}
