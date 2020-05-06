FROM openjdk:14-jdk-alpine as builder
WORKDIR /workspace/app

COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle
COPY src src

RUN ./gradlew build
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM openjdk:14-jdk-alpine

RUN apk --no-cache add bash && \
    wget -O /usr/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && \
    chmod +x /usr/wait-for-it.sh

RUN addgroup -S docker_spring && adduser -S docker_spring -G docker_spring
USER docker_spring

ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["/usr/wait-for-it.sh", "-t", "120", "cms_db:27017", "--", \
            "java","-cp","app:app/lib/*", \
            "-noverify", "-XX:TieredStopAtLevel=1", \
            "pl.maciejkopec.cms.CmsApplication"]