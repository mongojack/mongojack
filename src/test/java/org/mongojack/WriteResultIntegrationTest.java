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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

@MongoTestParams(serializerType = MongoTestParams.SerializationType.OBJECT)
public class WriteResultIntegrationTest extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testGetSavedId() {
        assertThat(coll.insert(new MockObject("blah", "ten", 10)).getSavedId(),
                equalTo("blah"));
    }

    @Test
    public void testGetSavedObject() {
        MockObject o = new MockObject("blah", "ten", 10);
        assertThat(coll.insert(o).getSavedObject(), equalTo(o));
    }

    @Test
    public void testGetSavedIds() {
        final WriteResult<MockObject, String> result = coll.insert(
                new MockObject("A", "a", 1), new MockObject("B", "b", 2));
        assertThat(result.getSavedIds().get(0), equalTo("A"));
        assertThat(result.getSavedIds().get(1), equalTo("B"));
    }

    @Test
    public void testGetSavedObjects() {
        final MockObject a = new MockObject("A", "a", 1);
        final MockObject b = new MockObject("B", "b", 2);
        final WriteResult<MockObject, String> result = coll.insert(a, b);
        assertThat(result.getSavedObjects().get(0), equalTo(a));
        assertThat(result.getSavedObjects().get(1), equalTo(b));
    }
}
