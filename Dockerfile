FROM eclipse-temurin:20-jdk

ARG GRADLE_VERSION=8.2

RUN apt-get update && apt-get install -yq make unzip

WORKDIR /app

COPY ./ .

RUN gradle installDist

CMD build/install/app/bin/app
