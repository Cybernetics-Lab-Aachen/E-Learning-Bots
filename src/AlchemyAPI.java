import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class AlchemyAPI {
	
	// If running locally complete the variables below with the information in VCAP_SERVICES
	private static String baseURL = "http://access.alchemyapi.com/calls/";
	private static String key = "";
	 
	public String URLGetAuthor(String url)
			throws IOException, SAXException,
			ParserConfigurationException, XPathExpressionException
		{
			return URLGetAuthor(url, new AlchemyApi_params());
		}
	
	public String URLGetAuthor(String url, AlchemyApi_params params)
			throws IOException, SAXException, ParserConfigurationException,
			XPathExpressionException
		{
			params.setUrl(url);
			//Change the first parameter for different API calls
			return GET("URLGetAuthors", "url", params);
		}
	
	private String GET(String callName, String callPrefix, AlchemyApi_params params)
			throws IOException, SAXException,
			ParserConfigurationException, XPathExpressionException
		{
			StringBuilder uri = new StringBuilder();
			uri.append(baseURL).append(callPrefix).append('/').append(callName)
			.append('?').append("apikey=").append(key);
			uri.append(params.getParameterString());
			URL url = new URL(uri.toString());
			HttpURLConnection handle = (HttpURLConnection) url.openConnection();
			handle.setDoOutput(true);
			return doRequest(handle, params.getOutputMode());
		}
	
	private String doRequest(HttpURLConnection handle, String outputMode)
			throws IOException
			{
				try{
				    int status = handle.getResponseCode();
				    switch (status) {
			            case 200:
			            case 201:
			                BufferedReader br = new BufferedReader(new InputStreamReader(handle.getInputStream()));
			                StringBuilder sb = new StringBuilder();
			                String line;
			                while ((line = br.readLine()) != null) {
			                    sb.append(line+"\n");
			                }
			                br.close();
			                return sb.toString();
			              
			        }
					}
				catch(IOException ex){
						System.out.println("IO Exception ");
					}
				return "";
			}
}
