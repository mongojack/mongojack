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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

public class TestDBProjection extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testIncludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(DBQuery.empty(),
                DBProjection.include("string", "integer"));
        assertThat(result.string, equalTo("string"));
        assertThat(result.integer, equalTo(10));
        assertNull(result.longs);
    }

    @Test
    public void testExcludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(DBQuery.empty(),
                DBProjection.exclude("string", "integer"));
        assertNull(result.string);
        assertNull(result.integer);
        assertThat(result.longs, equalTo(20l));
    }
}
