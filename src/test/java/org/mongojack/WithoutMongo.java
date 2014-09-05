package org.mongojack;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * 
 * An annotation for test methods that do not require the {@link MongoRule} to
 * create and tear down the instance. Please refer to {@link MongoRule}
 * documentation.
 * 
 */
@Retention(RUNTIME)
@Documented
@Target(METHOD)
public @interface WithoutMongo {

}
