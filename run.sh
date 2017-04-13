#!/bin/bash
java -cp crawler4j-4.3.jar:json-20160810.jar:mysql-connector-java-5.1.6.jar:jopt-simple-6.0-alpha-1.jar:httpclient-4.5.3.jar:httpcore-4.4.6.jar:guava-21.0.jar:tika-parsers-1.14.jar:slf4j-api-1.7.25.jar:commons-codec-1.10.jar:commons-logging-1.2.jar:. crawler.Controller --db_host=db --db_port=3306 --db_user=${MYSQL_USER} --db_pass=${MYSQL_PASSWORD} --db_name=${MYSQL_DATABASE} --crawl_storage="/usr/src/e-learning-bots/crawl-storage" --number_of_crawlers=${CRAWLER_NUM_CRAWLERS} --restart_every_days=${CRAWLER_RESTART_EVERY_DAYS} --kpi_manager_url=${CRAWLER_KPIManager_URL}