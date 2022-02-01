FROM openjdk:17-oracle

ADD ./build/libs/JavaDistFS-1.0-SNAPSHOT.jar JavaDistFS-1.0-SNAPSHOT.jar

ENV JAVA_OPTS ""
ENV APP_OPTS ""
ENTRYPOINT ["/bin/bash", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /JavaDistFS-1.0-SNAPSHOT.jar ${APP_OPTS}"]