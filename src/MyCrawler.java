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

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
                                                           + "|png|mp3|mp3|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
     @Override
     public boolean shouldVisit(Page referringPage, WebURL url) {
         String href = url.getURL().toLowerCase();
         return !FILTERS.matcher(href).matches();
     }

     /**
      * This function is called when a page is fetched and ready
      * to be processed by your program.
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws IOException 
     * @throws XPathExpressionException 
     * @throws SQLException 
     * @throws InterruptedException 
      */
     @Override
     public void visit(Page page) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException, SQLException, InterruptedException {
         String url = page.getWebURL().getURL();
         System.out.println("URL: " + url);
         
         String wlAnaPath = "C:\\Users\\ml538117\\Desktop\\Pfad.txt";
         String wlCrPath = "C:\\Users\\ml538117\\Desktop\\Pfad.txt";
         String[] wlAnalysis = readFile(wlAnaPath,StandardCharsets.UTF_8).split(",");
         String[] wlCrawler = readFile(wlCrPath,StandardCharsets.UTF_8).split(",");
         
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
             //check text with whitelistCrawler
             for(int i = 0; i < wlCrawler.length ; i++){
            	 if(text.toLowerCase().contains(wlCrawler[i].toLowerCase())){
            		 passed = true;
            		 break;
            	 }
             }
             if(passed == false){
            	 return;
             }
             Set<WebURL> links = htmlParseData.getOutgoingUrls();
             URL urls = new URL("https://gateway-a.watsonplatform.net/calls/url/URLGetCombinedData?url=" + url + "&outputMode=json&extract=keywords,entities,concepts&sentiment=1&maxRetrieve=3&apikey=ddc06944c93c23c9cfd6e6bbbb6cd5c00e7bf18b");
             Controller.counter ++;
             String alchemy = "";
             try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
                 for (String line; (line = reader.readLine()) != null;) {
                     alchemy = alchemy +line;
                     System.out.println(line);
                     
                 }
             }
             //pause till next day if daily limit(about 300 accesses) is exceeded
             if(alchemy.contains("daily-transaction-limit-exceeded")){
                    	 SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    		timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    		String timeStamp = timeFormat.format(Calendar.getInstance().getTime());
                    		timeStamp = timeStamp.substring(9, 11);
                    		int waitingTime = (25 - Integer.parseInt(timeStamp)) * 3600000;
                    		System.out.println("Pause until next day");
                    		Thread.sleep(waitingTime);                		
                     }
             
             //add JsonParser here + fit gathered data in StringArray --> then save in db
             
             //check analysis with whitelistAnalysis
             passed = false;
             for(int i = 0; i < wlAnalysis.length ; i++){
            	 if(text.toLowerCase().contains(wlAnalysis[i].toLowerCase())){
            		 passed = true;
            		 break;
            	 }
             }
             if(passed == false){
            	 return;
             }
             
            /* JSONObject obj = new JSONObject(alchemy);
             try {
				Class.forName("com.mysql.jdbc.Driver");
				Connection connection = DriverManager.getConnection("jdbc:mysql://"+Controller.host+":"+Controller.port+"/demo", ""+Controller.user, ""+Controller.password);
				Statement statement = connection.createStatement();
     	      
     	      String sql = "INSERT INTO test"  +
     	                  "VALUES (100, 'Zara', 'Ali', 18)";
     	      statement.executeUpdate(sql);
             } catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}*/
             
             
             
             System.out.println("Text length: " + text.length());
             System.out.println("Html length: " + html.length());
             System.out.println("Number of outgoing links: " + links.size());
             System.out.println("Websites visited: " + Controller.counter);
             
         }
    }
     
     static String readFile(String path, Charset encoding) 
   		  throws IOException 
   		{
   		  byte[] encoded = Files.readAllBytes(Paths.get(path));
   		  return new String(encoded, encoding);
   		}
     
}