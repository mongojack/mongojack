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

import com.mongodb.client.model.Sorts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;

import static org.assertj.core.api.Assertions.assertThat;


public class TestDBSort extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setUp() {
        coll = getCollection(MockObject.class);
        coll.insert(new MockObject("1", "b", 10));
        coll.insert(new MockObject("2", "a", 30));
        coll.insert(new MockObject("3", "a", 20));
    }

    private void assertOrder(Iterable<MockObject> results, String... order) {
        int i = 0;
        for (MockObject o : results) {
            assertThat(o._id).as("Item " + i + " out of order").isEqualTo(order[i]);
            i++;
        }
    }

    @Test
    public void testSortsAsc() {
        assertOrder(coll.find().sort(Sorts.ascending("string", "integer")), "3",
            "2", "1");
    }

    @Test
    public void testSortsDesc() {
        assertOrder(coll.find().sort(Sorts.descending("string", "integer")),
            "1", "2", "3");
    }

    @Test
    public void testSortsAscDesc() {
        assertOrder(coll.find().sort(Sorts.orderBy(Sorts.ascending("string"), Sorts.descending("integer"))),
            "2", "3", "1");
    }

    @Test
    public void testSortsDescAsc() {
        assertOrder(coll.find().sort(Sorts.orderBy(Sorts.descending("string"), Sorts.ascending("integer"))),
            "1", "3", "2");
    }

}
