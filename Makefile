SBT:=sbt
DOCKER:=docker
DB_CONTAINER:=mariadb

run-schema:
	$(DOCKER) exec -ti $(DB_CONTAINER) bash -c "mysql -u root -padmin1 btc_rates < /var/config/schema.sql"

build-app:
	$(SBT) $(DOCKER)

restart: build-app
	$(DOCKER)-compose down ; $(DOCKER)-compose up -d

sparkjars-clean:
	rm -f ./sparkjars/*.jar

sparkapp: sparkjars-clean
	$(SBT) "project sparkRatesDownloader" assembly ; cp spark-rates-downloader/target/scala-2.12/btc-price-api-assembly-*.jar ./sparkjars/btc-price-api-assembly.jar

sparkapp-submit:
	$(DOCKER) exec spark-master /spark/bin/spark-submit --verbose /lib/jars/btc-price-api-assembly.jar


all: build-app run-schema