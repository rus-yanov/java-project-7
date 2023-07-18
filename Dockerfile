FROM gradle:8.2-jdk20

WORKDIR /app

COPY ./ .

RUN gradle installDist

CMD build/install/app/bin/app
