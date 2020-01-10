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

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class TestJacksonMongoCollectionMultiple extends MongoDBTestBase {

    private JacksonMongoCollection.JacksonMongoCollectionBuilder builder;

    @Before
    public void setup() throws Exception {
        builder = JacksonMongoCollection.builder();
    }

    @Test
    public void testGetMultipleCollections() {
        JacksonMongoCollection<MockObject1> coll1 =
            builder.build(getMongoCollection("testJacksonMongoCollection1"), MockObject1.class);
        JacksonMongoCollection<MockObject2> coll2 =
            builder.build(getMongoCollection("testJacksonMongoCollection2"), MockObject2.class);

        MockObject1 o1 = new MockObject1();
        o1.num = 10;
        MockObject2 o2 = new MockObject2();
        o2.value = "test-2";
        coll1.insert(o1);
        coll2.insert(o2);

        List<MockObject1> results11 = coll1
            .find(new Document("num", 10)).into(new ArrayList<>());
        assertThat(results11, hasSize(1));

        List<MockObject1> results10 = coll1
            .find(new Document("value", "test-2")).into(new ArrayList<>());
        assertThat(results10, hasSize(0));

        List<MockObject2> results21 = coll2
            .find(new Document("value", "test-2")).into(new ArrayList<>());
        assertThat(results21, hasSize(1));

        List<MockObject2> results20 = coll2
            .find(new Document("num", 10)).into(new ArrayList<>());
        assertThat(results20, hasSize(0));
    }

    public static class MockObject1 {
        @Id
        @ObjectId
        public String id;
        public Integer num;
    }

    public static class MockObject2 {
        @Id
        @ObjectId
        public String id;
        public String value;
    }
}
