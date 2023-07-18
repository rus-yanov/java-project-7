FROM eclipse-temurin:20-jdk

WORKDIR /app

COPY ./ .

RUN ./gradlew installDist

CMD build/install/app/bin/app
