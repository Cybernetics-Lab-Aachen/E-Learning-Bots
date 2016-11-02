import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

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
      */
     @Override
     public void visit(Page page) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
         String url = page.getWebURL().getURL();
         System.out.println("URL: " + url);

         if (page.getParseData() instanceof HtmlParseData) {
             HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
             String text = htmlParseData.getText();
             String html = htmlParseData.getHtml();
             Set<WebURL> links = htmlParseData.getOutgoingUrls();
             
             URL urls = new URL("https://gateway-a.watsonplatform.net/calls/url/URLGetCombinedData?url=" + url + ".html&outputMode=json&extract=keywords,entities,concepts&sentiment=1&maxRetrieve=3&apikey=ddc06944c93c23c9cfd6e6bbbb6cd5c00e7bf18b");

             try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
                 for (String line; (line = reader.readLine()) != null;) {
                     System.out.println(line);
                 }
             }

             System.out.println("Text length: " + text.length());
             System.out.println("Html length: " + html.length());
             System.out.println("Number of outgoing links: " + links.size());
         }
    }
}