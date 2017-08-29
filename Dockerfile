FROM anapsix/8_server-jre_unlimited

RUN apk --no-cache upgrade
RUN apk add --no-cache tini

COPY target/vertx-blog-1.1*fat.jar /usr/local/bin

# Tini is now available at /sbin/tini
ENTRYPOINT ["/sbin/tini", "-g", "--"]

CMD ["java" , "-jar", "/usr/local/bin/vertx-blog-1.1*.jar"]

EXPOSE 8080





