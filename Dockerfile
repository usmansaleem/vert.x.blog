FROM anapsix/alpine-java:8_server-jre_unlimited

RUN apk --no-cache upgrade
RUN apk add --no-cache tini paxctl && paxctl -c /opt/jdk/bin/* && paxctl -m /opt/jdk/bin/*

COPY target/vertx-blog-full.jar /usr/local/bin

# Tini is now available at /sbin/tini
ENTRYPOINT ["/sbin/tini", "-s", "-g", "--"]

CMD ["/opt/jdk/bin/java" , "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/usr/local/bin/vertx-blog-full.jar"]

EXPOSE 8080





