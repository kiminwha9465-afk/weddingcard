# 1단계: 빌드 환경 구성
FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean bootJar --no-daemon -x test

# 2단계: 실행 환경 구성
FROM eclipse-temurin:21-jre-jammy
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/weddingcard-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
