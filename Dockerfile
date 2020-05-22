# Base image
FROM tomcat:7.0.94-jre7-alpine

# Update default server.xml
RUN ["rm", "-f", "/usr/local/tomcat/conf/server.xml"]
ADD docker/server.xml /usr/local/tomcat/conf/server.xml

# Update default logging.properties
RUN ["rm", "-f", "/usr/local/tomcat/conf/logging.properties"]
ADD docker/logging.properties /usr/local/tomcat/conf/logging.properties

# Copy dist build
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD srv/target/qzui.war /usr/local/tomcat/webapps/ROOT.war

# Copy entry point & make it executable
COPY docker/docker-entrypoint.sh .
RUN chmod +x ./docker-entrypoint.sh

# Start
ENTRYPOINT ["./docker-entrypoint.sh"]
