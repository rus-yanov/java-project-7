setup:
	gradle wrapper --gradle-version 7.4

clean:
	gradle clean

build:
	gradle clean build

start:
	gradle bootRun --args='--spring.profiles.active=dev'

start-prod:
	gradle bootRun --args='--spring.profiles.active=prod'

install:
	gradle installDist

start-dist:
	./build/install/app/bin/app

lint:
	gradle checkstyleMain checkstyleTest

test:
	gradle test

report:
	gradle jacocoTestReport

check-updates:
	gradle dependencyUpdates

generate-migrations:
	gradle diffChangeLog

db-migrate:
	gradle update

.PHONY: build
