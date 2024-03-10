package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * class TestJavaTimeHandling: Tests the java.time.* handling in MongoJack.
 *
 * @author dnebinger
 */
public class TestJavaTimeHandling extends MongoDBTestBase {

    private ObjectMapper timestampWritingObjectMapper;
    private ObjectMapper millisWritingObjectMapper;

    public static class LocalDateContainer {
        public org.bson.types.ObjectId _id;
        public LocalDate localDate;
    }
    
    @BeforeEach
    public void setUp() {
        timestampWritingObjectMapper = ObjectMapperConfigurer.configureObjectMapper(new ObjectMapper());
        timestampWritingObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        millisWritingObjectMapper = ObjectMapperConfigurer.configureObjectMapper(new ObjectMapper(), new MongoJackModuleConfiguration().with(MongoJackModuleFeature.WRITE_INSTANT_AS_BSON_DATE));
        millisWritingObjectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    @Test
    public void testLocalDateSavedAsTimestamps() {
        // create the object
        LocalDateContainer object = new LocalDateContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.localDate = LocalDate.now();

        // get a container
        JacksonMongoCollection<LocalDateContainer> coll = getCollection(LocalDateContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.localDate).isEqualTo(object.localDate);
    }

    @Test
    public void testLocalDateSavedAsISO8601() {
        // create the object
        LocalDateContainer object = new LocalDateContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.localDate = LocalDate.now();

        // get a container
        JacksonMongoCollection<LocalDateContainer> coll = getCollection(LocalDateContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.localDate).isEqualTo(object.localDate);
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
        object.localTime = LocalTime.now();

        // get a container
        JacksonMongoCollection<LocalTimeContainer> coll = getCollection(LocalTimeContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.localTime).isEqualTo(object.localTime);
    }

    @Test
    public void testLocalTimeSavedAsISO8601() {
        // create the object
        LocalTimeContainer object = new LocalTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.localTime = LocalTime.now();

        // get a container
        JacksonMongoCollection<LocalTimeContainer> coll = getCollection(LocalTimeContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.localTime).isEqualTo(object.localTime);
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
        object.localDateTime = LocalDateTime.now();

        // get a container
        JacksonMongoCollection<LocalDateTimeContainer> coll = getCollection(LocalDateTimeContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.localDateTime).isEqualTo(object.localDateTime);
    }

    @Test
    public void testLocalDateTimeSavedAsISO8601() {
        // create the object
        LocalDateTimeContainer object = new LocalDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.localDateTime = LocalDateTime.now();

        // get a container
        JacksonMongoCollection<LocalDateTimeContainer> coll = getCollection(LocalDateTimeContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        LocalDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.localDateTime).isEqualTo(object.localDateTime);
    }

    public static class ZonedDateTimeContainer {
        public org.bson.types.ObjectId _id;
        public ZonedDateTime zonedDateTime;
    }

    @Test
    public void testZonedDateTimeSavedAsISO8601() {
        // create the object
        ZonedDateTimeContainer object = new ZonedDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalDateTime time = LocalDateTime.now();
        ZoneId paris = ZoneId.of("Europe/Paris");
        object.zonedDateTime = time.atZone(paris);

        // get a container
        JacksonMongoCollection<ZonedDateTimeContainer> coll = getCollection(ZonedDateTimeContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        ZonedDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
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
        JacksonMongoCollection<YearContainer> coll = getCollection(YearContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        YearContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.year).isEqualTo(object.year);
    }

    @Test
    public void testYearSavedAsISO8601() {
        // create the object
        YearContainer object = new YearContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.year = Year.now();

        // get a container
        JacksonMongoCollection<YearContainer> coll = getCollection(YearContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        YearContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.year).isEqualTo(object.year);
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
        JacksonMongoCollection<YearMonthContainer> coll = getCollection(YearMonthContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        YearMonthContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.yearMonth).isEqualTo(object.yearMonth);
    }

    @Test
    public void testYearMonthSavedAsISO8601() {
        // create the object
        YearMonthContainer object = new YearMonthContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.yearMonth = YearMonth.now();

        // get a container
        JacksonMongoCollection<YearMonthContainer> coll = getCollection(YearMonthContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        YearMonthContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.yearMonth).isEqualTo(object.yearMonth);
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
        JacksonMongoCollection<MonthDayContainer> coll = getCollection(MonthDayContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        MonthDayContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.monthDay).isEqualTo(object.monthDay);
    }

    @Test
    public void testMonthDaySavedAsISO8601() {
        // create the object
        MonthDayContainer object = new MonthDayContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.monthDay = MonthDay.now();

        // get a container
        JacksonMongoCollection<MonthDayContainer> coll = getCollection(MonthDayContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        MonthDayContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.monthDay).isEqualTo(object.monthDay);
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

        object.offsetTime = OffsetTime.of(now, ZoneOffset.UTC);

        // get a container
        JacksonMongoCollection<OffsetTimeContainer> coll = getCollection(OffsetTimeContainer.class, timestampWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.offsetTime).isEqualTo(object.offsetTime);
    }

    @Test
    public void testOffsetTimeSavedAsISO8601() {
        // create the object
        OffsetTimeContainer object = new OffsetTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        LocalTime now = LocalTime.now();

        object.offsetTime = OffsetTime.of(now, ZoneOffset.UTC);

        // get a container
        JacksonMongoCollection<OffsetTimeContainer> coll = getCollection(OffsetTimeContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.offsetTime).isEqualTo(object.offsetTime);
    }

    public static class OffsetDateTimeContainer {
        public org.bson.types.ObjectId _id;
        public OffsetDateTime offsetDateTime;
    }

    @Test
    public void testOffsetDateTimeSavedAsISO8601() {
        // create the object
        OffsetDateTimeContainer object = new OffsetDateTimeContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;

        object.offsetDateTime = OffsetDateTime.of(LocalDate.now(), LocalTime.now(), ZoneOffset.UTC);

        // get a container
        JacksonMongoCollection<OffsetDateTimeContainer> coll = getCollection(OffsetDateTimeContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        OffsetDateTimeContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.offsetDateTime).isEqualTo(object.offsetDateTime);
    }

    public static class DurationContainer {
        public org.bson.types.ObjectId _id;
        public Duration duration;
    }

    @Test
    public void testDurationSavedAsISO8601() {
        // create the object
        DurationContainer object = new DurationContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.duration = Duration.ofMinutes(2047);

        // get a container
        JacksonMongoCollection<DurationContainer> coll = getCollection(DurationContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        DurationContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.duration).isEqualTo(object.duration);
    }

    public static class InstantContainer {
        public org.bson.types.ObjectId _id;
        public Instant instant;
    }

    @Test
    public void testInstantSavedAsISO8601() {
        // create the object
        InstantContainer object = new InstantContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.instant = Instant.now().plus(Duration.ofHours(3).plusMinutes(8));

        // get a container
        JacksonMongoCollection<InstantContainer> coll = getCollection(InstantContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        InstantContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.instant).isEqualTo(object.instant);
    }

    @Test
    public void testInstantSavedAsNativeTimestamps() {
        // create the object
        InstantContainer object = new InstantContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;
        object.instant = Instant.now().plus(Duration.ofHours(3).plusMinutes(8));

        // get a container
        JacksonMongoCollection<InstantContainer> coll = getCollection(InstantContainer.class, millisWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        InstantContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.instant).isEqualTo(object.instant.truncatedTo(ChronoUnit.MILLIS));

        // retrieve raw bson
        BsonDocument bsonResult = coll.withDocumentClass(BsonDocument.class).findOneById(id);

        // verify it
        BsonDateTime expectedBsonDateTime = new BsonDateTime(result.instant.toEpochMilli());
        assertThat(bsonResult.getDateTime("instant")).isEqualTo(expectedBsonDateTime);
    }

    // not java.time-related exactly, but I deleted all the calendar tests, so
    public static class CalendarContainer {
        public org.bson.types.ObjectId _id;
        public java.util.Calendar calendar;
    }

    @Test
    public void testCalendarSavedAsISO8601() {
        // create the object
        CalendarContainer object = new CalendarContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.add(Calendar.MINUTE, 3);
        object._id = id;
        object.calendar = calendar;

        // get a container
        JacksonMongoCollection<CalendarContainer> coll = getCollection(CalendarContainer.class);

        // save the object
        coll.insert(object);

        // retrieve it
        CalendarContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.calendar.getTimeInMillis()).isEqualTo(object.calendar.getTimeInMillis());
    }

    @Test
    public void testCalendarSavedAsNativeTimestamps() {
        // create the object
        CalendarContainer object = new CalendarContainer();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.add(Calendar.MINUTE, 3);
        object._id = id;
        object.calendar = calendar;

        // get a container
        JacksonMongoCollection<CalendarContainer> coll = getCollection(CalendarContainer.class, millisWritingObjectMapper);

        // save the object
        coll.insert(object);

        // retrieve it
        CalendarContainer result = coll.findOneById(id);

        // verify it
        assertThat(result._id).isEqualTo(id);
        assertThat(result.calendar.getTimeInMillis()).isEqualTo(object.calendar.getTimeInMillis());

        // retrieve raw bson
        BsonDocument bsonResult = coll.withDocumentClass(BsonDocument.class).findOneById(id);

        // verify it
        BsonDateTime expectedBsonDateTime = new BsonDateTime(result.calendar.getTimeInMillis());
        assertThat(bsonResult.getDateTime("calendar")).isEqualTo(expectedBsonDateTime);
    }

}
