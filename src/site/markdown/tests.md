Tests
=====

`mvn clean test` will run some few unittests

`mvn clean integration-test` will run integration tests

MongoJack uses the [embedmongo-maven-plugin](https://github.com/joelittlejohn/embedmongo-maven-plugin) to start an embedded MongoDB instance before running the integration tests. This instance will startup with port `37037`.

If the process that runs the tests die, unexpectedly, please verify that there is no running embedded MongoDB instance at port `37037` anymore, before restarting the tests.

`mvn clean integration-test -Dembed.mongodb.port=47047` will run integration tests with MongoDB listening at port `47047`

`mvn clean integration-test -Dembed.mongodb.version=2.2.0` will run integration tests with MongoDB at version `2.2.0`
