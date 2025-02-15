FROM bellsoft/liberica-openjdk-alpine:23 AS builder
WORKDIR /application
COPY . .
RUN --mount=type=cache,target=/root/.gradle  chmod +x gradlew && ./gradlew clean build -x test

FROM bellsoft/liberica-openjre-alpine:21 AS layers
WORKDIR /application
COPY --from=builder /application/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM bellsoft/liberica-openjre-alpine:23
VOLUME /tmp
RUN adduser -S spring-user
USER spring-user
WORKDIR /home/spring-user/app
COPY --from=layers /application/dependencies/ ./
COPY --from=layers /application/spring-boot-loader/ ./
COPY --from=layers /application/snapshot-dependencies/ ./
COPY --from=layers /application/application/ ./
RUN mkdir -p /home/spring-user/app/downloads && chmod 777 /home/spring-user/app/downloads

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]