FROM openjdk:jdk

WORKDIR /usr/src/e-learning-bots

# Get dependencies
RUN \
	wget http://central.maven.org/maven2/edu/uci/ics/crawler4j/4.3/crawler4j-4.3.jar && \
	wget http://central.maven.org/maven2/org/json/json/20160810/json-20160810.jar && \
	wget http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar && \
	wget http://central.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/1.5.9/mariadb-java-client-1.5.9.jar && \
	wget http://central.maven.org/maven2/net/sf/jopt-simple/jopt-simple/6.0-alpha-1/jopt-simple-6.0-alpha-1.jar && \
	wget http://central.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.3/httpclient-4.5.3.jar

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