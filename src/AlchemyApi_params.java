import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class AlchemyApi_params {
	public static final String OUTPUT_XML = "json";//change the format here
	private String outputMode = OUTPUT_XML;
	private String url;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getOutputMode() {
		return outputMode;
	}
	
	public static void main(String[] args)
			throws IOException, SAXException,
			ParserConfigurationException, XPathExpressionException
		{
			// Create an AlchemyAPI object.
			AlchemyAPI alchemyObj = new AlchemyAPI();
		
			String obj = alchemyObj.URLGetAuthor("http://www.politico.com/blogs/media/2012/02/detroit-news-ed-upset-over-romney-edit-115247.html");
			//Printed as a String, but can be converted to the reqd format using proper libraries
			System.out.println(obj);
		}
	
	public String getParameterString(){
		String retString = "";
		try{
			if(url!=null) retString+="&url="+URLEncoder.encode(url,"UTF-8");
			if(outputMode!=null) retString+="&outputMode="+outputMode;
		}
		catch(UnsupportedEncodingException e ){
			retString = "";
		}
			return retString;
	}

}
