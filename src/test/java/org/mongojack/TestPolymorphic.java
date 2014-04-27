/*
 * Copyright 2014 Luke Palmer
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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.mock.MockBaseObject;
import org.mongojack.mock.MockDerivedObject;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Test use of polymorphic types with mongojack
 * 
 * @author Luke Palmer
 */
public class TestPolymorphic extends MongoDBTestBase {
    private JacksonDBCollection<MockBaseObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockBaseObject.class, String.class);
    }

    String id;

    @Test
    public void testDerived() {
        MockDerivedObject obj = new MockDerivedObject();
        obj.baseField = 1;
        obj.derivedField = 2;
        coll.insert(obj);
        MockBaseObject foundObj = coll.findOne();
        assertEquals(foundObj.baseField, obj.baseField);
        assertEquals(((MockDerivedObject) foundObj).derivedField, obj.derivedField);
        id = foundObj._id;
        assertNotNull(id);
    }

    @Ignore("This is broken, checked in as a repro")
    @Test
    public void testFindId() {
        assertNotNull(coll.findOne(DBQuery.is("_id", id)));
        assertNotNull(coll.findOneById(id));
    }
}
