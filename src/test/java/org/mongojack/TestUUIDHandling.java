/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class TestUUIDHandling extends MongoDBTestBase {

    @Test
    public void testUnderlyingMongoFunctionality() {

        final MongoCollection<Document> collection = getMongoCollection(Document.class);

        final Document document = new Document("uuid", UUID.randomUUID());
        collection.insertOne(document);

        final Document found = collection.find(Filters.eq("uuid", document.get("uuid"))).first();

        assertThat(found.get("uuid"), equalTo(document.get("uuid")));
    }

    @Test
    public void testSavesAndRetrievesUuid() {
        ObjectIdId object = new ObjectIdId(UUID.randomUUID());

        JacksonMongoCollection<ObjectIdId> coll = getCollection(ObjectIdId.class);

        coll.insert(object);
        ObjectId id = coll.findOne()._id;
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
        assertThat(result.uuid, equalTo(object.uuid));
        assertThat(getUnderlyingCollection(coll).find().first().get("uuid"), equalTo(object.uuid));
    }

    public static class ObjectIdId {
        public ObjectId _id;
        public UUID uuid;

        public ObjectIdId() {
        }

        public ObjectIdId(final UUID uuid) {
            this.uuid = uuid;
        }

    }

}
