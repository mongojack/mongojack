package org.mongojack;

import org.bson.types.ObjectId;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    public static class LocalDateTimeContainer {
        public org.bson.types.ObjectId _id;
        public LocalDateTime localDateTime;
    }

    @Test
    public void testLocalDateTimeSavedAsTimestamps() {
        // create the object
        LocalDateTimeContainer object = new LocalDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalDateTime time = LocalDateTime.now();
        object.localDateTime = time;

        // get a container
        JacksonDBCollection<LocalDateTimeContainer, org.bson.types.ObjectId> coll = getCollection(LocalDateTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.localDateTime, equalTo(object.localDateTime));
    }

    @Test
    public void testLocalDateTimeSavedAsISO8601() {
        // create the object
        LocalDateTimeContainer object = new LocalDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalDateTime time = LocalDateTime.now();
        object.localDateTime = time;

        // get a container
        JacksonDBCollection<LocalDateTimeContainer, org.bson.types.ObjectId> coll = getCollection(LocalDateTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.localDateTime, equalTo(object.localDateTime));
    }

    public static class ZonedDateTimeContainer {
        public org.bson.types.ObjectId _id;
        public ZonedDateTime zonedDateTime;
    }

    /*
        NOTE: A ZonedDateTime as WRITE_DATES_AS_TIMESTAMPS wants to store as a BigDecimal, but MongoJack doesn't support
        serialization of a BigDecimal.

        So this test is disabled because it will fail.


    @Test
    public void testZonedDateTimeSavedAsTimestamps() {
        // create the object
        ZonedDateTimeContainer object = new ZonedDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalDateTime time = LocalDateTime.now();
        ZoneId paris = ZoneId.of("Europe/Paris");
        ZonedDateTime zoned = time.atZone(paris);
        object.zonedDateTime = zoned;

        // get a container
        JacksonDBCollection<ZonedDateTimeContainer, org.bson.types.ObjectId> coll = getCollection(ZonedDateTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        ZonedDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        boolean equals = result.zonedDateTime.isEqual(zoned);
        assertTrue("Zoned date times do not match.", equals);
    }
    */

    @Test
    public void testZonedDateTimeSavedAsISO8601() {
        // create the object
        ZonedDateTimeContainer object = new ZonedDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalDateTime time = LocalDateTime.now();
        ZoneId paris = ZoneId.of("Europe/Paris");
        ZonedDateTime zoned = time.atZone(paris);
        object.zonedDateTime = zoned;

        // get a container
        JacksonDBCollection<ZonedDateTimeContainer, org.bson.types.ObjectId> coll = getCollection(ZonedDateTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        ZonedDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertTrue(result.zonedDateTime.isEqual(object.zonedDateTime));
    }

    public static class YearContainer {
        public org.bson.types.ObjectId _id;
        public Year year;
    }

    @Test
    public void testYearSavedAsTimestamps() {
        // create the object
        YearContainer object = new YearContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.year = Year.now();

        // get a container
        JacksonDBCollection<YearContainer, org.bson.types.ObjectId> coll = getCollection(YearContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        YearContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.year, equalTo(object.year));
    }

    @Test
    public void testYearSavedAsISO8601() {
        // create the object
        YearContainer object = new YearContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.year = Year.now();

        // get a container
        JacksonDBCollection<YearContainer, org.bson.types.ObjectId> coll = getCollection(YearContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        YearContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.year, equalTo(object.year));
    }

    public static class YearMonthContainer {
        public org.bson.types.ObjectId _id;
        public YearMonth yearMonth;
    }

    @Test
    public void testYearMonthSavedAsTimestamps() {
        // create the object
        YearMonthContainer object = new YearMonthContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.yearMonth = YearMonth.now();

        // get a container
        JacksonDBCollection<YearMonthContainer, org.bson.types.ObjectId> coll = getCollection(YearMonthContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        YearMonthContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.yearMonth, equalTo(object.yearMonth));
    }

    @Test
    public void testYearMonthSavedAsISO8601() {
        // create the object
        YearMonthContainer object = new YearMonthContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.yearMonth = YearMonth.now();

        // get a container
        JacksonDBCollection<YearMonthContainer, org.bson.types.ObjectId> coll = getCollection(YearMonthContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        YearMonthContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.yearMonth, equalTo(object.yearMonth));
    }

    public static class MonthDayContainer {
        public org.bson.types.ObjectId _id;
        public MonthDay monthDay;
    }

    @Test
    public void testMonthDaySavedAsTimestamps() {
        // create the object
        MonthDayContainer object = new MonthDayContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.monthDay = MonthDay.now();

        // get a container
        JacksonDBCollection<MonthDayContainer, org.bson.types.ObjectId> coll = getCollection(MonthDayContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        MonthDayContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.monthDay, equalTo(object.monthDay));
    }

    @Test
    public void testMonthDaySavedAsISO8601() {
        // create the object
        MonthDayContainer object = new MonthDayContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.monthDay = MonthDay.now();

        // get a container
        JacksonDBCollection<MonthDayContainer, org.bson.types.ObjectId> coll = getCollection(MonthDayContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        MonthDayContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.monthDay, equalTo(object.monthDay));
    }

    public static class OffsetTimeContainer {
        public org.bson.types.ObjectId _id;
        public OffsetTime offsetTime;
    }

    @Test
    public void testOffsetTimeSavedAsTimestamps() {
        // create the object
        OffsetTimeContainer object = new OffsetTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalTime now = LocalTime.now();
        OffsetTime offsetTime = OffsetTime.of(now, ZoneOffset.UTC);

        object.offsetTime = offsetTime;

        // get a container
        JacksonDBCollection<OffsetTimeContainer, org.bson.types.ObjectId> coll = getCollection(OffsetTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.offsetTime, equalTo(object.offsetTime));
    }

    @Test
    public void testOffsetTimeSavedAsISO8601() {
        // create the object
        OffsetTimeContainer object = new OffsetTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalTime now = LocalTime.now();
        OffsetTime offsetTime = OffsetTime.of(now, ZoneOffset.UTC);

        object.offsetTime = offsetTime;

        // get a container
        JacksonDBCollection<OffsetTimeContainer, org.bson.types.ObjectId> coll = getCollection(OffsetTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.offsetTime, equalTo(object.offsetTime));
    }

    public static class OffsetDateTimeContainer {
        public org.bson.types.ObjectId _id;
        public OffsetDateTime offsetDateTime;
    }

    /*
        Like ZonedDateTime, OffsetDateTime serializes to a BigDecimal when writing dates as timestamps.
        Since MongoJack does not support BigDecimals, this type cannot be serialized.
        
    @Test
    public void testOffsetDateTimeSavedAsTimestamps() {
        // create the object
        OffsetDateTimeContainer object = new OffsetDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        OffsetDateTime offsetTime = OffsetDateTime.of(LocalDate.now(), LocalTime.now(), ZoneOffset.UTC);

        object.offsetDateTime = offsetTime;

        // get a container
        JacksonDBCollection<OffsetDateTimeContainer, org.bson.types.ObjectId> coll = getCollection(OffsetDateTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.enable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.offsetDateTime, equalTo(object.offsetDateTime));
    }
     */

    @Test
    public void testOffsetDateTimeSavedAsISO8601() {
        // create the object
        OffsetDateTimeContainer object = new OffsetDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        OffsetDateTime offsetTime = OffsetDateTime.of(LocalDate.now(), LocalTime.now(), ZoneOffset.UTC);

        object.offsetDateTime = offsetTime;

        // get a container
        JacksonDBCollection<OffsetDateTimeContainer, org.bson.types.ObjectId> coll = getCollection(OffsetDateTimeContainer.class,
                org.bson.types.ObjectId.class);

        // enable as timestamps.
        coll.disable(JacksonDBCollection.Feature.WRITE_DATES_AS_TIMESTAMPS);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id, equalTo(id));
        assertThat(result.offsetDateTime, equalTo(object.offsetDateTime));
    }
}
