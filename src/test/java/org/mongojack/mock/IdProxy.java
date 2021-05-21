package org.mongojack.mock;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Id
@ObjectId
public @interface IdProxy {
}
