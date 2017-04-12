package crawler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.PreparedStatement;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class Controller {
	static int counter = 0;
	static String host = "";
	static int port;
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
		// CLI stuff
<<<<<<< HEAD:src/crawler/Controller.java
				final OptionParser parser = new OptionParser();
				final OptionSpec<String> hostOption = parser.accepts("db_host").withRequiredArg().ofType(String.class).required();
				final OptionSpec<Integer> portOption = parser.accepts("db_port").withRequiredArg().ofType(Integer.class).required();
				final OptionSpec<String> userOption = parser.accepts("db_user").withRequiredArg().ofType(String.class).required();
				final OptionSpec<String> passwordOption = parser.accepts("db_pass").withRequiredArg().ofType(String.class).required();
				parser.accepts("alchemy");
				parser.accepts("store_sources");
				final OptionSpec<String> wlAnaPathOption = parser.accepts("alchemy_whitelist").requiredIf("alchemy").withRequiredArg().ofType(String.class);
				final OptionSpec<String> wlCrPathOption = parser.accepts("crawler_whitelist").withRequiredArg().ofType(String.class).required();
				final OptionSpec<String> seedPathOption = parser.accepts("seed").withRequiredArg().ofType(String.class).required();
				final OptionSpec<String> crawlStorageFolderOption = parser.accepts("crawl_storage").withRequiredArg().ofType(String.class).required();
				final OptionSpec<Integer> numberOfCrawlersOption = parser.accepts("number_of_crawlers").withRequiredArg().ofType(Integer.class).required();
				final OptionSpec<Integer> restartEveryDaysOption = parser.accepts("restart_every_days").withRequiredArg().ofType(Integer.class).required();

				final OptionSet options = parser.parse(args);
				host = options.valueOf(hostOption);
				port = options.valueOf(portOption);
				user = options.valueOf(userOption);
				password = options.valueOf(passwordOption);
				enableAlchemy = options.has("alchemy");
				storeSources = options.has("store_sources");
				wlAnaPath = options.valueOf(wlAnaPathOption);
				wlCrPath =  options.valueOf(wlCrPathOption);
				String seedPath = options.valueOf(seedPathOption);
				String crawlStorageFolder = options.valueOf(crawlStorageFolderOption);
				int numberOfCrawlers = options.valueOf(numberOfCrawlersOption);
				restartEveryDays = options.valueOf(restartEveryDaysOption);
=======
		final OptionParser parser = new OptionParser();
		final OptionSpec<String> hostOption = parser.accepts("db_host").withRequiredArg().ofType(String.class).required();
		final OptionSpec<Integer> portOption = parser.accepts("db_port").withRequiredArg().ofType(Integer.class).required();
		final OptionSpec<String> userOption = parser.accepts("db_user").withRequiredArg().ofType(String.class).required();
		final OptionSpec<String> passwordOption = parser.accepts("db_pass").withRequiredArg().ofType(String.class).required();
		parser.accepts("alchemy");
		parser.accepts("store_sources");
		final OptionSpec<String> wlAnaPathOption = parser.accepts("alchemy_whitelist").requiredIf("alchemy").withRequiredArg().ofType(String.class);
		final OptionSpec<String> wlCrPathOption = parser.accepts("crawler_whitelist").withRequiredArg().ofType(String.class).required();
		final OptionSpec<String> seedPathOption = parser.accepts("seed").withRequiredArg().ofType(String.class).required();
		final OptionSpec<String> crawlStorageFolderOption = parser.accepts("crawl_storage").withRequiredArg().ofType(String.class).required();
		final OptionSpec<Integer> numberOfCrawlersOption = parser.accepts("number_of_crawlers").withRequiredArg().ofType(Integer.class).required();
		final OptionSpec<Integer> restartEveryDaysOption = parser.accepts("restart_every_days").withRequiredArg().ofType(Integer.class).required();

		final OptionSet options = parser.parse(args);
		host = options.valueOf(hostOption);
		port = options.valueOf(portOption);
		user = options.valueOf(userOption);
		password = options.valueOf(passwordOption);
		enableAlchemy = options.has("alchemy");
		storeSources = options.has("store_sources");
		wlAnaPath = options.valueOf(wlAnaPathOption);
		wlCrPath =  options.valueOf(wlCrPathOption);
		String seedPath = options.valueOf(seedPathOption);
		String crawlStorageFolder = options.valueOf(crawlStorageFolderOption);
		int numberOfCrawlers = options.valueOf(numberOfCrawlersOption);
		restartEveryDays = options.valueOf(restartEveryDaysOption);
>>>>>>> 4fda68aff46dc3f8f7d10c01f04e7554f732100f:src/Controller.java
		try {
			buildDB();
		} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
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
		//check if visits has already an entry
		try {
			java.sql.Statement statement = connection.createStatement();
			ResultSet res = statement.executeQuery("SELECT * FROM  visits");
			res.next();
			int test = res.getInt("visits");
		} catch (java.sql.SQLException e) {
			sql = (PreparedStatement) connection.prepareStatement("INSERT INTO `visits` (`visits`) VALUES(0);");
			sql.executeUpdate();
			System.err.println(e.getStackTrace());
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
