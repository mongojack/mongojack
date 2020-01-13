Changelog
=========

3.0.0
-----

_Released 2020_

* Modify the entire library to conform to the functionality of MongoCollection as provided in mongodb-driver-sync, and drop all support for legacy driver features.

2.1.0
-----

_Released 2014.07.29_

* Update dependency versions (by lukelukeluke)
* Extended Readme to explain Snapshot installations (by abc2mit)
* Updated mongo-java-driver to newer version (by benmccann)
* Upgrade Jackson and Bson4Jackson to prevent potential conflicts with Jackson 2.2.X (by yunspace)
* Implementation of the mongoDB Aggregation feature
* Added a how-to-release documentation

2.0.0
-----

_Released 2014.01.13_

* code cleanups & some new tests
* upgrade mongo-java-driver to 2.11.3 and jackson to 2.2.3 (by benmccann)
* adding parameterized findAndModify (by truthspirit)
* updated mongo-java-driver, jackson-databind, bson4jackson and junit to newer versions (by benmccann)
* Fixed bug in query serialization for DBQuery.all (by saadmufti)
* Added some notes about thread-safety (by benmccann)
* Updated pom.xml to build osgi bundle (by m-bs-jmeyer)
* Proper update and query serialization support.
* Renamed to MongoJack.
* Added a DBSort helper utility.
* Added a DBProjection helper utility.

1.4.2
-----

_Released 2012.04.20_

* Fixed bug when using DBRefs in update statements.

1.4.1
-----

_Released 2012.03.17_

* Added convenience method for configuring a custom object mapper
* Documented the necessary use of custom mongo configuration when supplying a custom object mapper

1.4.0
-----

_Released 2012.02.23_

* Added map/reduce deserialisation support
* Fixed bug in object deserialisation where ObjectIds were reported as string values
* Added support for stream serialisation
* Fixed bug in configuring jackson mappers with views

1.3
---

_Released 2012.01.22_

* Upgraded to bson4jackson 1.3.0 to fix bug
* Added elemMatch and where support to DBQuery

1.2.1
-----

_Released 2011.12.31_

* Upgraded to bson4jackson 1.2
* Ensured byte arrays could be handled correctly
* Fixed DBQuery collection method signature so its a collection of wildcards, not objects
* Fixed bug in date handling

1.2
---

_Released 2011.12.22_

* Added support for references
* Made the use of stream parser optional
* Allowed lists of `@ObjectId` annotated items to be serialised
* Fixed handling of error objects in stream parser
* Added DBQuery builder
* Ensured Dates get serialised/deserialised to/from BSON date types

1.1.3
-----

_Released 2011.12.09_

* Fixed serialisation bug where ObjectId’s were being serialised to object

1.1.2
-----

_Released 2011.12.09_

* Fixed bug where an object containing all null values threw an NPE
* Added parser using bson4jackson when deserialising from the DBCursor

1.1.1
-----

_Released 2011.12.07_

* Fixed bug in update so that multiple operations add with the same modifier don't replace each other

1.1
---

_Released 2011.12.06_

* Added builder for modifier based queries, with serialisation support

1.0
---

_Released 2011.11.30_

Initial release.
