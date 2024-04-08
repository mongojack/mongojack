package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.*;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestBsonSerializers extends MongoDBTestBase {

    private final ObjectMapper bsonSerializingObjectMapper = ObjectMapperConfigurer.configureObjectMapper(
        new ObjectMapper(),
        new MongoJackModuleConfiguration().with(MongoJackModuleFeature.ENABLE_BSON_VALUE_SERIALIZATION)
    );

    @Test
    public void testCollectionOfDocuments() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c.insertOne(new Document("a", List.of(new Document("documentField", "documentValue"))));

        Document found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("documentField", "documentValue")), found.get("a"));
    }

    @Test
    public void testCollectionOfBsonDocuments() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c.insertOne(new Document("a", List.of(new BsonDocument("bsonDocumentField", new BsonString("bsonDocumentValue")))));

        Document found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("bsonDocumentField", "bsonDocumentValue")), found.get("a"));
    }

    @Test
    public void testCollectionOfFilters() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c.insertOne(new Document("a", List.of(Filters.eq("filterFieldName", "filterFieldValue"))));

        Document found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("filterFieldName", "filterFieldValue")), found.get("a"));
    }

    public static class BsonHoldingObject {

        public String _id;

        public List<Bson> bsonList = new ArrayList<>();

        public BsonHoldingObject() {
        }

        public BsonHoldingObject(Bson... bson) {
            bsonList.addAll(Arrays.asList(bson));
        }
    }

    @Test
    public void testPojoCollectionOfDocuments() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(new Document("documentField", "documentValue")));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("documentField", "documentValue")), found.bsonList);
    }

    @Test
    public void testPojoCollectionOfBsonDocuments() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(new BsonDocument("bsonDocumentField", new BsonString("bsonDocumentValue"))));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("bsonDocumentField", "bsonDocumentValue")), found.bsonList);
    }

    @Test
    public void testPojoCollectionOfFilters() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(Filters.eq("filterFieldName", "filterFieldValue")));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("filterFieldName", "filterFieldValue")), found.bsonList);
    }

    @Test
    public void testPojoCollectionWithArray() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(new BsonDocument("filterFieldName", new BsonArray(List.of(new BsonString("z"), new BsonString("x"))))));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(List.of(Map.of("filterFieldName", List.of("z", "x"))), found.bsonList);
    }

    @Test
    public void testAggregationWithFailingList() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        // we don't expect anything here, but it shouldn't throw an exception
        c
            .aggregate(
                List.of(
                    new Document(
                        "$unionWith",
                        new Document("coll", "otherCollection")
                            .append("pipeline",
                                List.of(
                                    Aggregates.match(
                                        Filters.and(
                                            Filters.eq("a", "a"),
                                            Filters.eq("a", "c")
                                        )
                                    )
                                )
                            )
                    )
                )
            )
            .into(new ArrayList<>());
    }

    @Test
    public void testAggregationWithNull() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c
            .aggregate(
                Arrays.asList(
                    new Document("$match",
                        new Document(
                            "$and",
                            Arrays.asList(
                                new Document("receiver",
                                    new Document("$exists", true)
                                        .append("$ne", new BsonNull())
                                )
                            )
                        )
                    )
                )
            )
            .into(new ArrayList<>());
    }

    @Test
    public void testAggregationWithFailingListWithoutCustomObjectMapper() {
        JacksonMongoCollection<Document> c = getCollection(Document.class);

        assertThrows(
            MongoJsonMappingException.class,
            () -> c
                .aggregate(
                    List.of(
                        new Document(
                            "$unionWith",
                            new Document("coll", "otherCollection")
                                .append("pipeline",
                                    List.of(
                                        Aggregates.match(
                                            Filters.and(
                                                Filters.eq("a", "a"),
                                                Filters.eq("a", "c")
                                            )
                                        )
                                    )
                                )
                        )
                    )
                )
                .into(new ArrayList<>()),
            "Expected aggregation to throw MongoJsonMappingException, but didn't"
        );
    }
}
