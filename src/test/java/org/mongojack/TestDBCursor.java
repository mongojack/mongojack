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

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test the Json DB Cursor
 */
public class TestDBCursor extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setup() throws Exception {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testIterator() {
        MockObject o1 = new MockObject("id1", "blah1", 10);
        MockObject o2 = new MockObject("id2", "blah2", 20);
        MockObject o3 = new MockObject("id3", "blah3", 30);
        coll.insert(o1, o2, o3);
        final MongoCursor<MockObject> cursor = coll.find().sort(
            new BasicDBObject("integer", 1)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(o1);
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(o2);
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(o3);
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testArray() {
        MockObject o1 = new MockObject("id1", "blah1", 10);
        MockObject o2 = new MockObject("id2", "blah2", 20);
        MockObject o3 = new MockObject("id3", "blah3", 30);
        coll.insert(o1, o2, o3);
        List<MockObject> results = coll.find().sort(new BasicDBObject("integer", 1)).into(new ArrayList<>());
        assertThat(results).contains(o1, o2, o3);
        assertThat(results).hasSize(3);
    }

}
