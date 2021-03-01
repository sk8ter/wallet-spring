# Wallet service

## Build and run
Since it is a maven project just run:
```bash
mvn clean install
```
then it can be run with:
```bash
java -jar target/service-0.0.1-SNAPSHOT.jar
```
It is using h2 in memory DB by default.
In case you need to save data between restarts modify application.properties.

## API
Take a look at API.http for API documentation.

## Before production:

### Security.
Who can access this service and what endpoints require permission? Authentication & Authorization?
Is it only available to other services? TLS?

### Pagination and Sorting.
Should be added to limit amount of retrieved data.

### Failed transactions.
It was unclear if failed transactions should be stored or not.
Since current implementation does not save them, it makes it possible to perform a retry with transactionId that failed before.
It could be a good idea to store cause of failure, to perform a retry if needed.

### Concurrency & Scalability.
Service is build on flux and r2dbc i.e. non-blocking.
It was build with Concurrency, Scalability, Atomicity and Idempotency in mind.
