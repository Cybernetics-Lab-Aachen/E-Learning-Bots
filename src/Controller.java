import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	static String host = "";
	static String port = "";
	static String user = "";
	static String password = "";
	static boolean enableAlchemy = false;
	static boolean storeSources = false;
	static String wlAnaPath = "";
	static String wlCrPath = "";

	public static void main(String[] args) throws Exception {
		host = args[0];
		port = args[1];
		user = args[2];
		password = args[3];
		if (args[4].contains("true")) {
			enableAlchemy = true;
		}
		if (args[5].contains("true")) {
			storeSources = true;
		}
		wlAnaPath = args[6];
		wlCrPath = args[7];
		String seedPath = args[8];

		String crawlStorageFolder = args[9];
		int numberOfCrawlers = Integer.parseInt(args[10]);

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		String[] seeds = readFile(seedPath, StandardCharsets.UTF_8).split(",");
		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		System.out.println(config+ " " +pageFetcher + " " +robotstxtServer );
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		for (int i = 0; i < seeds.length; i++) {
			controller.addSeed(seeds[i]);
		}
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(MyCrawler.class, numberOfCrawlers);
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
