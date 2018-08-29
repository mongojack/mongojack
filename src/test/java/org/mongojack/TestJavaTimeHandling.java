package org.mongojack;

import org.bson.types.ObjectId;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

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

    public static class LocalTimeContainer {
        public org.bson.types.ObjectId _id;
        public LocalTime localTime;
    }

    @Test
    public void testLocalTimeSavedAsTimestamps() {
        // create the object
        LocalTimeContainer object = new LocalTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalTime time = LocalTime.now();
        object.localTime = time;

        // get a container
        JacksonDBCollection<LocalTimeContainer, org.bson.types.ObjectId> coll = getCollection(LocalTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.localTime, equalTo(object.localTime));
    }

    @Test
    public void testLocalTimeSavedAsISO8601() {
        // create the object
        LocalTimeContainer object = new LocalTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalTime time = LocalTime.now();
        object.localTime = time;

        // get a container
        JacksonDBCollection<LocalTimeContainer, org.bson.types.ObjectId> coll = getCollection(LocalTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.localTime, equalTo(object.localTime));
    }
}
