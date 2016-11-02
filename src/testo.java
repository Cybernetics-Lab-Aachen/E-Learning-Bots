import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class testo {
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		URL urls = new URL("https://gateway-a.watsonplatform.net/calls/url/URLGetCombinedData?url=http://www.cnbc.com/2016/05/16/buffetts-berkshire-hathaway-takes-new-stake-in-apple.html&outputMode=json&extract=keywords,entities,concepts&sentiment=1&maxRetrieve=3&apikey=ddc06944c93c23c9cfd6e6bbbb6cd5c00e7bf18b");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                System.out.println(line);
            }
        }
	}
}
