Changelog
=========

1.2
---

_Released 2011.12.??_

* Added support for references
* Made the use of stream parser optional
* Allowed lists of `@ObjectId` annotated items to be serialised
* Fixed handling of error objects in stream parser
* Added DBQuery builder

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
