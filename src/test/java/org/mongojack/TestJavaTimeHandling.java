package org.mongojack;

import org.bson.types.ObjectId;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * class TestJavaTimeHandling: Tests the java.time.* handling in MongoJack.
 *
 * @author dnebinger
 */
public class TestJavaTimeHandling extends MongoDBTestBase {

    public static class LocalDateContainer {
        public org.bson.types.ObjectId _id;
        public LocalDate localDate;
    }

    @Test
    public void testLocalDateSavedAsTimestamps() {
        // create the object
        LocalDateContainer object = new LocalDateContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.localDate = LocalDate.now();

        // get a container
        JacksonDBCollection<LocalDateContainer, org.bson.types.ObjectId> coll = getCollection(LocalDateContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.localDate, equalTo(object.localDate));
    }

    @Test
    public void testLocalDateSavedAsISO8601() {
        // create the object
        LocalDateContainer object = new LocalDateContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.localDate = LocalDate.now();

        // get a container
        JacksonDBCollection<LocalDateContainer, org.bson.types.ObjectId> coll = getCollection(LocalDateContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.localDate, equalTo(object.localDate));
    }
}
