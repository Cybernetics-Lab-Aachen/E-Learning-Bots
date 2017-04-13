FROM openjdk:jdk

WORKDIR /usr/src/e-learning-bots

# Get dependencies
RUN \
	wget http://central.maven.org/maven2/edu/uci/ics/crawler4j/4.3/crawler4j-4.3.jar && \
	wget http://central.maven.org/maven2/org/json/json/20160810/json-20160810.jar && \
	wget http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar && \
	wget http://central.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/1.5.9/mariadb-java-client-1.5.9.jar && \
	wget http://central.maven.org/maven2/net/sf/jopt-simple/jopt-simple/6.0-alpha-1/jopt-simple-6.0-alpha-1.jar && \
	wget http://central.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.3/httpclient-4.5.3.jar && \
	wget http://central.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.6/httpcore-4.4.6.jar && \
	wget http://central.maven.org/maven2/com/google/guava/guava/21.0/guava-21.0.jar && \
	wget http://central.maven.org/maven2/org/apache/tika/tika-parsers/1.14/tika-parsers-1.14.jar && \
	wget http://central.maven.org/maven2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar && \
	wget http://central.maven.org/maven2/commons-codec/commons-codec/1.10/commons-codec-1.10.jar && \
	wget http://central.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar && \
	wget http://repo.boundlessgeo.com/main/com/sleepycat/je/5.0.84/je-5.0.84.jar && \
	wget http://central.maven.org/maven2/org/apache/tika/tika-core/1.14/tika-core-1.14.jar && \
	wget http://central.maven.org/maven2/org/ccil/cowan/tagsoup/tagsoup/1.2.1/tagsoup-1.2.1.jar

# Add souce code
ADD ./src .

# Add run script
ADD ./run.sh .

# Compile and mark script as executable
RUN \
	javac -sourcepath . -cp crawler4j-4.3.jar:json-20160810.jar:mysql-connector-java-5.1.6.jar:jopt-simple-6.0-alpha-1.jar crawler/Controller.java && \
	chmod 0777 run.sh

VOLUME /usr/src/e-learning-bots/crawl-storage
CMD ["./run.sh"]