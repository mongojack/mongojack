package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TestBsonSerializers extends MongoDBTestBase {

    private final ObjectMapper bsonSerializingObjectMapper = ObjectMapperConfigurer.configureObjectMapper(
        new ObjectMapper(),
        new MongoJackModuleConfiguration().with(MongoJackModuleFeature.ENABLE_BSON_VALUE_SERIALIZATION)
    );

    @Test
    public void testCollectionOfDocuments() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c.insertOne(new Document("a", Arrays.asList(new Document("documentField", "documentValue"))));

        Document found = c.find().first();
        assertNotNull(found);
        assertEquals(Arrays.asList(mapOf("documentField", "documentValue")), found.get("a"));
    }

    @Test
    public void testCollectionOfBsonDocuments() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c.insertOne(new Document("a", Arrays.asList(new BsonDocument("bsonDocumentField", new BsonString("bsonDocumentValue")))));

        Document found = c.find().first();
        assertNotNull(found);
        assertEquals(Arrays.asList(mapOf("bsonDocumentField", "bsonDocumentValue")), found.get("a"));
    }

    @Test
    public void testCollectionOfFilters() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c.insertOne(new Document("a", Arrays.asList(Filters.eq("filterFieldName", "filterFieldValue"))));

        Document found = c.find().first();
        assertNotNull(found);
        assertEquals(Arrays.asList(mapOf("filterFieldName", "filterFieldValue")), found.get("a"));
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
        assertEquals(Arrays.asList(mapOf("documentField", "documentValue")), found.bsonList);
    }

    @Test
    public void testPojoCollectionOfBsonDocuments() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(new BsonDocument("bsonDocumentField", new BsonString("bsonDocumentValue"))));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(Arrays.asList(mapOf("bsonDocumentField", "bsonDocumentValue")), found.bsonList);
    }

    @Test
    public void testPojoCollectionOfFilters() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(Filters.eq("filterFieldName", "filterFieldValue")));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(Arrays.asList(mapOf("filterFieldName", "filterFieldValue")), found.bsonList);
    }

    @Test
    public void testPojoCollectionWithArray() {
        JacksonMongoCollection<BsonHoldingObject> c = getCollection(BsonHoldingObject.class, bsonSerializingObjectMapper);

        c.insertOne(new BsonHoldingObject(new BsonDocument("filterFieldName", new BsonArray(Arrays.asList(new BsonString("z"), new BsonString("x"))))));

        BsonHoldingObject found = c.find().first();
        assertNotNull(found);
        assertEquals(Arrays.asList(mapOf("filterFieldName", Arrays.asList("z", "x"))), found.bsonList);
    }

    @Test
    public void testAggregationWithFailingList() {
        JacksonMongoCollection<Document> c = getCollection(Document.class, bsonSerializingObjectMapper);

        c
            .aggregate(
                Arrays.asList(
                    new Document(
                        "$unionWith",
                        new Document("coll", "otherCollection")
                            .append("pipeline",
                                Arrays.asList(
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
    public void testAggregationWithFailingListWithoutCustomObjectMapper() {
        JacksonMongoCollection<Document> c = getCollection(Document.class);

        assertThrows(
            MongoJsonMappingException.class,
            () -> c
                .aggregate(
                    Arrays.asList(
                        new Document(
                            "$unionWith",
                            new Document("coll", "otherCollection")
                                .append("pipeline",
                                    Arrays.asList(
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
                .into(new ArrayList<>())
        );
    }
    
    private Map<String, Object> mapOf(String k, Object v) {
        Map<String, Object> map = new HashMap<>();
        map.put(k, v);
        return map;
    }
}
