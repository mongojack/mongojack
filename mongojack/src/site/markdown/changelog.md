Changelog
=========

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
