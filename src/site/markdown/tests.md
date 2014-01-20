Tests
=====

`mvn clean test` will run some few unittests

`mvn clean verify` will run integration tests

MongoJack uses the [embedmongo-maven-plugin](https://github.com/joelittlejohn/embedmongo-maven-plugin) to start an embedded MongoDB instance before running the integration tests. This instance will startup with port 37017.

If the process that runs the tests die, unexpectedly, please verify that there is no running embedded MongoDB instance at port 37017 anymore, before restarting the tests.

