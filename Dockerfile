FROM gradle:8.2-jdk20

ARG GRADLE_VERSION=8.2

RUN apt-get update && apt-get install -yq make unzip

WORKDIR /app

COPY ./ .

RUN gradle installDist

CMD build/install/app/bin/app
