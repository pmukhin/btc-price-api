# btc-price-api

### architecture
there's web API `btc-rates-api` built on top of `http4s`. it accepts http requests on two endpoints: `/rates/{from}/{to}` and `/rates/{for}`.
there's also a Spark app that downloads rates from the internet and process it. In the end the new rates go to kafka, `btc-rates-api` reads them and add to the db.

### e2e tests
in order to test the spark app there's a cucumber spec for that. there's also a mock test service to serve rates.

### unit tests
there's a funspec `RatesServiceSpec` as an example of unit testing of services. the `RateRepository` might have been mocked,
but fake implementation I found a bit more explicit.