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
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonView;

public class TestJsonViews extends MongoDBTestBase {

    private JacksonDBCollection<ObjectWithView, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(ObjectWithView.class, String.class,
                MockView1.class);
    }

    @Test
    public void testNormalPropertyWithView() {
        coll.save(new ObjectWithView("id", "normal", "view1", "view2"));
        assertThat(coll.findOneById("id").normal, equalTo("normal"));
    }

    @Test
    public void testEnabledPropertyWithView() {
        coll.save(new ObjectWithView("id", "normal", "view1", "view2"));
        assertThat(coll.findOneById("id").view1, equalTo("view1"));
    }

    @Test
    public void testDisabledPropertyWithView() {
        coll.save(new ObjectWithView("id", "normal", "view1", "view2"));
        assertThat(coll.findOneById("id").view2, nullValue());
    }

    @Test
    public void testDisabledPropertyWithViewAfterUpdate() {
        ObjectWithView obj = new ObjectWithView("id", "normal", "view1", "view2");
        coll.save(obj);
        coll.update(DBQuery.is("_id", "id"), obj);
        assertThat(coll.findOneById("id").view2, nullValue());
    }

    public static class ObjectWithView {
        public ObjectWithView() {
        }

        public ObjectWithView(String id, String normal, String view1,
                String view2) {
            _id = id;
            this.normal = normal;
            this.view1 = view1;
            this.view2 = view2;
        }

        public String _id;
        public String normal;
        @JsonView(MockView1.class)
        public String view1;
        @JsonView(MockView2.class)
        public String view2;
    }

    public class MockView1 {
    }

    public class MockView2 {
    }
}
