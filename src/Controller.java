import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	static int counter = 0;

	public static void main(String[] args) throws Exception {
		/*args = new String[20];

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the blacklist with each word substracted by ; : ");
		args[0] = sc.nextLine();
		System.out.println("Now enter your Maria DB host :");
		args[1] = sc.nextLine();
		System.out.println("Now enter your Maria DB port :");
		args[2] = sc.nextLine();
		System.out.println("Now enter your Maria DB username :");
		args[3] = sc.nextLine();
		System.out.println("Now enter your Maria DB password :");
		args[4] = sc.nextLine();
		System.out.println("Now enter your Maria DB table name :");
		args[5] = sc.nextLine();*/
		
		Class.forName("com.mysql.jdbc.Driver");

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "");
		Statement statement = connection.createStatement();
	      
	      String sql = "INSERT INTO test " +
	                   "VALUES (100, 'Zara', 'Ali', 18)";
	      statement.executeUpdate(sql);

		String crawlStorageFolder = "/data/crawl/root";
		int numberOfCrawlers = 7;

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		controller.addSeed(
				"http://www.spiegel.de/wirtschaft/soziales/ceta-ein-lob-den-feilschern-kommentar-a-1118289.html");
		controller.addSeed("http://www.ics.uci.edu/~lopes/");
		controller.addSeed("http://www.ics.uci.edu/~welling/");
		controller.addSeed("http://www.ics.uci.edu/");

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(MyCrawler.class, numberOfCrawlers);
	}
}
