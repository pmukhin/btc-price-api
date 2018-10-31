SBT:=sbt
DOCKER:=docker
DB_CONTAINER:=mariadb

run-schema:
	$(DOCKER) exec -ti $(DB_CONTAINER) bash -c "mysql -u root -padmin1 btc_rates < /var/config/schema.sql"

build-app:
	$(SBT) $(DOCKER)

restart: build-app
	$(DOCKER)-compose down ; $(DOCKER)-compose up -d

all: build-app run-schema