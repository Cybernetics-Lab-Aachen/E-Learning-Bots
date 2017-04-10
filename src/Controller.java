import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.PreparedStatement;

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
	static boolean restart = false;
	static int run = 0;
	static int restartEveryDays = 0;

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
		restartEveryDays = Integer.parseInt(args[11]);
		try {
			buildDB();
		} catch (Exception e) {

		}
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setIncludeHttpsPages(true);
		config.setResumableCrawling(true);
		String[] seeds = readFile(seedPath, StandardCharsets.UTF_8).split(",");
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
		for (int i = 0; i < seeds.length; i++) {
			controller.addSeed(seeds[i]);
		}
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(MyCrawler.class, numberOfCrawlers);
	}

	static void buildDB() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://" + Controller.host + ":" + Controller.port + "/demo", "" + Controller.user,
				"" + Controller.password);
		// create sources table
		PreparedStatement sql = (PreparedStatement) connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS `sources` (`sourceId` int(11) NOT NULL AUTO_INCREMENT, `url` text NOT NULL, `language` varchar(140) NOT NULL,  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  `data` text NOT NULL,  `country` varchar(4) NOT NULL, `run` int(11) NOT NULL,  PRIMARY KEY (`sourceId`),  KEY `sourceId` (`sourceId`),  KEY `country` (`country`),  KEY `language` (`language`),  KEY `run` (`run`),  KEY `time` (`time`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		sql.executeUpdate();
		// create vivits table
		sql = (PreparedStatement) connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS `visits` (  `visits` int(11) NOT NULL,  PRIMARY KEY (`visits`),  UNIQUE KEY `visits` (`visits`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		sql.executeUpdate();
		// create keywords table
		sql = (PreparedStatement) connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS `keywords` (  `keywordsId` int(11) NOT NULL AUTO_INCREMENT,  `relevance` double NOT NULL,  `sentiment` double NOT NULL,  `text` varchar(255) NOT NULL,  `anger` double NOT NULL,  `disgust` double NOT NULL,  `fear` double NOT NULL,  `joy` double NOT NULL,  `sadness` double NOT NULL,  `sourcesID` int(11) NOT NULL,  PRIMARY KEY (`keywordsId`),  UNIQUE KEY `number_2` (`keywordsId`),  KEY `number` (`keywordsId`),  KEY `keywordsId` (`keywordsId`),  KEY `sourcesID` (`sourcesID`),  KEY `sourcesID_2` (`sourcesID`),  KEY `sadness` (`sadness`),  KEY `joy` (`joy`),  KEY `fear` (`fear`),  KEY `disgust` (`disgust`),  KEY `anger` (`anger`),  KEY `sentiment` (`sentiment`),  KEY `relevance` (`relevance`),  KEY `text` (`text`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		sql.executeUpdate();
		// create entities table
		sql = (PreparedStatement) connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS `entities` (  `entitiesId` int(11) NOT NULL AUTO_INCREMENT,  `type` varchar(140) NOT NULL,  `relevance` double NOT NULL,  `sentiment` double NOT NULL,  `count` int(11) NOT NULL,  `text` varchar(255) NOT NULL,  `website_Link` text NOT NULL,  `dbpedia_Link` text NOT NULL,  `sourcesID` int(11) NOT NULL,  PRIMARY KEY (`entitiesId`),  UNIQUE KEY `entitiesId` (`entitiesId`),  KEY `entitiesId_2` (`entitiesId`),  KEY `sourcesID` (`sourcesID`),  KEY `type` (`type`),  KEY `relevance` (`relevance`),  KEY `sentiment` (`sentiment`),  KEY `text` (`text`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		sql.executeUpdate();
		// create concepts table
		sql = (PreparedStatement) connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS `concepts` (  `conceptsId` int(11) NOT NULL AUTO_INCREMENT,  `text` varchar(255) NOT NULL,  `relevance` double NOT NULL,  `website_Link` text NOT NULL,  `dbpedia_Link` text NOT NULL,  `sourcesID` int(11) NOT NULL,  PRIMARY KEY (`conceptsId`),  UNIQUE KEY `conceptsId` (`conceptsId`),  KEY `conceptsId_2` (`conceptsId`),  KEY `sourcesID` (`sourcesID`),  KEY `relevance` (`relevance`),  KEY `text` (`text`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		sql.executeUpdate();
		sql = (PreparedStatement) connection.prepareStatement("INSERT INTO `visits` (`visits`) VALUES(0);");
		sql.executeUpdate();
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
