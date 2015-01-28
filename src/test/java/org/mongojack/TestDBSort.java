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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

public class TestDBSort extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
        coll.insert(new MockObject("1", "b", 10));
        coll.insert(new MockObject("2", "a", 30));
        coll.insert(new MockObject("3", "a", 20));
    }

    private void assertOrder(Iterable<MockObject> results, String... order) {
        int i = 0;
        for (MockObject o : results) {
            assertThat("Item " + i + " out of order", o._id, equalTo(order[i]));
            i++;
        }
    }

    @Test
    public void testAsc() {
        assertOrder(coll.find().sort(DBSort.asc("string").asc("integer")), "3",
                "2", "1");
    }

    @Test
    public void testDesc() {
        assertOrder(coll.find().sort(DBSort.desc("string").desc("integer")),
                "1", "2", "3");
    }

    @Test
    public void testAscDesc() {
        assertOrder(coll.find().sort(DBSort.asc("string").desc("integer")),
                "2", "3", "1");
    }

    @Test
    public void testDescAsc() {
        assertOrder(coll.find().sort(DBSort.desc("string").asc("integer")),
                "1", "3", "2");
    }

}
