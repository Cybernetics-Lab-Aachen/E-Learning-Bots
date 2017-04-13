FROM openjdk:jdk

WORKDIR /usr/src/e-learning-bots

# Add ivy.xml before rest of source code as it doesn't change often and this allows to cache the retrieved dependencies
ADD ivy.xml .

# Get dependencies
RUN wget http://central.maven.org/maven2/org/apache/ivy/ivy/2.4.0/ivy-2.4.0.jar && \
	java -jar ivy-2.4.0.jar -retrieve "lib/[artifact]-[revision].[ext]" -confs compile

# Add souce code and run script
ADD ["./src", "./run.sh", "./"]

# Compile and mark script as executable
RUN \
	javac -sourcepath . -cp "lib/*" crawler/Controller.java && \
	chmod 0777 run.sh

VOLUME /usr/src/e-learning-bots/crawl-storage
CMD ["./run.sh"]