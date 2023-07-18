FROM eclipse-temurin:20-jdk

WORKDIR /app

COPY ./ .

SHELL ["/bin/bash", "-c"]

RUN gradle installDist

CMD build/install/app/bin/app
