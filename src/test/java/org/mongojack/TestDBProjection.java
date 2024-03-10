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

import com.mongodb.client.model.Projections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;


public class TestDBProjection extends MongoDBTestBase {
    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setUp() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testIncludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(
            DBQuery.empty(),
            DBProjection.include("string", "integer")
        );
        assertThat(result.string).isEqualTo("string");
        assertThat(result.integer).isEqualTo(10);
        assertNull(result.longs);
    }

    @Test
    public void testExcludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(
            DBQuery.empty(),
            DBProjection.exclude("string", "integer")
        );
        assertNull(result.string);
        assertNull(result.integer);
        assertThat(result.longs).isEqualTo(20l);
    }

    @Test
    public void testProjectionsIncludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(
            DBQuery.empty(),
            Projections.include("string", "integer")
        );
        assertThat(result.string).isEqualTo("string");
        assertThat(result.integer).isEqualTo(10);
        assertNull(result.longs);
    }

    @Test
    public void testProjectionsExcludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(
            DBQuery.empty(),
            Projections.exclude("string", "integer")
        );
        assertNull(result.string);
        assertNull(result.integer);
        assertThat(result.longs).isEqualTo(20l);
    }

    @Test
    public void testProjectionsIterableIncludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.find().projection(Projections.include("string", "integer")).first();
        assertThat(result.string).isEqualTo("string");
        assertThat(result.integer).isEqualTo(10);
        assertNull(result.longs);
    }

    @Test
    public void testProjectionsIterableExcludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.find().projection(Projections.exclude("string", "integer")).first();
        assertNull(result.string);
        assertNull(result.integer);
        assertThat(result.longs).isEqualTo(20l);
    }

}
