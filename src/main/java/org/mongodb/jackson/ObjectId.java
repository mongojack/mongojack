package org.mongodb.jackson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the property should be converted to an ObjectId before being stored in the database, and back to
 * whatever type it is afterwards.  Use this if you want an ID to have a String or byte[] value, but you want it stored
 * in the database as an ObjectId.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface ObjectId {
}
