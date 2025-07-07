Changelog
=========

5.0.3
-----

_Released 2025.07.07_

* [PR #250](https://github.com/mongojack/mongojack/pull/250): fix kotlin id writing or id writing where constructor parameter ends up as @Id


5.0.2
-----

_Released 2024.10.05_

* [Issue #247](https://github.com/mongojack/mongojack/issues/247)

5.0.1
-----

_Released 2024.08.20_

* [Issue #246](https://github.com/mongojack/mongojack/issues/246)

5.0.0
-----
_Released 2024.04.09_

* Upgrade to 5.x mongo driver
* Remove deprecated constructs
* [Issue #245](https://github.com/mongojack/mongojack/issues/245)


4.11.1
-----
_Released 2024.04.09_

* [Issue #245](https://github.com/mongojack/mongojack/issues/245)

4.11.0
-----
_Released 2024.03.10_

* [Issue #243](https://github.com/mongojack/mongojack/issues/243): Add slf4j to remove `printStacktrace()`
* Update to 4.11.1 mongo driver

4.8.3
-----
_Released 2024.04.09_

* [Issue #245](https://github.com/mongojack/mongojack/issues/245)

4.8.2
-----
_Released 2024.01.06_

* [PR #2242](https://github.com/mongojack/mongojack/pull/242)

4.8.1
-----
_Released 2023.10.12_

* [Issue #232](https://github.com/mongojack/mongojack/issues/232)
* [Jackson 2.15](https://github.com/mongojack/mongojack/pull/241)
* [Minkey/Maxkey Serialization](https://github.com/mongojack/mongojack/pull/237)

4.7.0
-----
_Released 2022.07.22_

* Upgrade to mongo driver version 4.7.0

4.5.1
-----
_Released 2022.07.22_

* [Issue #223](https://github.com/mongojack/mongojack/issues/223)
* [Issue #224](https://github.com/mongojack/mongojack/issues/224)
* [Issue #226](https://github.com/mongojack/mongojack/issues/226)

4.5.0
-----
_Released 2022.04.21_

* Upgrade to mongo driver version 4.5.1


4.3.1
-----
_Released 2022.04.21_

* [PR #222](https://github.com/mongojack/mongojack/pull/222)

4.3.0
-----
_Released 2021.11.02_

* [Issue #220](https://github.com/mongojack/mongojack/issues/220)
* Make javax persistence optional
* Upgrade to mongo driver version 4.3.3

4.2.2
-----
_Released 2021.11.02_

* [Issue #220](https://github.com/mongojack/mongojack/issues/220)
* Make javax persistence optional

4.2.1
-----
_Released 2021.10.02_

* [Issue #213](https://github.com/mongojack/mongojack/issues/213)
* Upgrade to Jackson 2.12.x (while hopefully retaining compatibility with 2.11.x)

4.2.0
-----
_Released 2021.04.05_

* [Issue #210](https://github.com/mongojack/mongojack/issues/210)
* [Issue #211](https://github.com/mongojack/mongojack/issues/211)
* [Issue #212](https://github.com/mongojack/mongojack/issues/212)
* Upgraded to mongo driver version 4.2.2

4.0.3
-----
_Released 2021.04.05_

* [Issue #210](https://github.com/mongojack/mongojack/issues/210)
* [Issue #211](https://github.com/mongojack/mongojack/issues/211)
* Upgraded to mongo driver version 4.0.5


4.0.2
-----
_Released 2020.06.14_

* [Issue #204](https://github.com/mongojack/mongojack/issues/201)
* [Issue #205](https://github.com/mongojack/mongojack/issues/202)
* Upgraded to mongo driver version 4.0.4 

4.0.1
-----
_Released 2020.05.29_

* [Issue #201](https://github.com/mongojack/mongojack/issues/201)
* [Issue #202](https://github.com/mongojack/mongojack/issues/202) 

4.0.0
-----

_Released 2020.04.23_

* Upgrade mongo library to 4.0.1 and make any changes necessary to support

3.0.1
-----

_Released 2020.02.07_

* [Issue #195](https://github.com/mongojack/mongojack/issues/195)
* [Issue #197](https://github.com/mongojack/mongojack/issues/197) 
* [Issue #198](https://github.com/mongojack/mongojack/issues/198) 

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
