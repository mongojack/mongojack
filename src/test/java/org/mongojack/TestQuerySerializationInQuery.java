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

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.BasicDBObject;

public class TestQuerySerializationInQuery extends MongoDBTestBase {

    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testInStringInCollection() {
        coll.save(new MockObject());
        List<String> values = new ArrayList<String>();
        Query q = DBQuery.in("string", values);
        List<MockObject> res = coll.find(q).toArray();
    }

    @Test
    public void testInCollectionInCollection() {
    	coll.save(new MockObject());
    	List<String> values = new ArrayList<String>();
    	Query q = DBQuery.in("strings", values);
    	List<MockObject> res = coll.find(q).toArray();
    }
    
    @Test(expected=ClassCastException.class)
    public void testInCollectionInString() {
    	coll.save(new MockObject());
    	Query q = DBQuery.in("strings", "");
    	List<MockObject> res = coll.find(q).toArray();
    }
    
    public static class MockObject {
        @ObjectId
        @Id
        private String id;
        
        private String string = "";
        private List<String> strings = new ArrayList<String>();

		public List<String> getStrings() {
			return strings;
		}

		public void setStrings(List<String> strings) {
			this.strings = strings;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
        
        

    }


}
