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

import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.model.MapReduceAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;

import static org.junit.jupiter.api.Assertions.*;


public class TestMapReduce extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setup() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testInlineMapReduce() {
        simpleMapReduce(true, MapReduceAction.REDUCE, null);
    }

    @Test
    public void testReplaceMapReduce() {
        JacksonMongoCollection<Simple> resultColl = getCollection(Simple.class);
        simpleMapReduce(false, MapReduceAction.REPLACE, resultColl.getName());
    }

    private void simpleMapReduce(
        Boolean inline,
        MapReduceAction action,
        String collection
    ) {
        coll.insert(new MockObject("foo", 10));
        coll.insert(new MockObject("foo", 15));
        coll.insert(new MockObject("bar", 5));

        final MapReduceIterable<Simple> mrIterable = coll.mapReduce(
            // language=JavaScript
            "function map() {emit(this.string, this.integer);}",
            // language=JavaScript
            "function reduce(k, vals) {var sum=0;for(var i in vals) sum += vals[i];return sum;}",
            Simple.class
        );

        if (!inline) {
            mrIterable.collectionName(collection);
            mrIterable.action(action);
            mrIterable.toCollection();
        }

        Simple foo = null;
        Simple bar = null;

        for (Simple result : mrIterable) {
            if (result._id.equals("foo")) {
                foo = result;
            } else if (result._id.equals("bar")) {
                bar = result;
            } else {
                fail("Unknown result: " + result._id);
            }
        }

        assertNotNull(foo);
        assertEquals(25, foo.value);

        assertNotNull(bar);
        assertEquals(5, bar.value);
    }

    public static class Simple {
        public String _id;
        public int value;
    }

    @Test
    public void testComplexInlineMapReduce() {
        complexMapReduce(true, null, null);
    }

    @Test
    public void testComplexReplaceMapReduce() {
        JacksonMongoCollection<Complex> resultColl = getCollection(Complex.class);
        complexMapReduce(false, MapReduceAction.REPLACE, resultColl.getName());
    }

    private void complexMapReduce(
        Boolean inline,
        MapReduceAction action,
        String collection
    ) {
        coll.insert(new MockObject("foo", 10));
        coll.insert(new MockObject("foo", 15));
        coll.insert(new MockObject("bar", 5));

        final MapReduceIterable<Complex> mrIterable = coll.mapReduce(
            // language=JavaScript
            "function map() {emit(this.string, {sum: this.integer, product: this.integer});}",
            // language=JavaScript
            "function reduce(k, vals) {var sum=0,product=1;for(var i in vals){sum+=vals[i].sum;product*=vals[i].product;}return {sum:sum,product:product};}",
            Complex.class
        );

        if (!inline) {
            mrIterable.collectionName(collection);
            mrIterable.action(action);
            mrIterable.toCollection();
        }

        Complex foo = null;
        Complex bar = null;

        for (Complex result : mrIterable) {
            if (result._id.equals("foo")) {
                foo = result;
            } else if (result._id.equals("bar")) {
                bar = result;
            } else {
                fail("Unknown result: " + result._id);
            }
        }

        assertNotNull(foo);
        assertEquals(25, foo.value.sum);
        assertEquals(150, foo.value.product);

        assertNotNull(bar);
        assertEquals(5, bar.value.sum);
        assertEquals(5, bar.value.product);
    }

    public static class Complex {
        public String _id;
        public Value value;
    }

    public static class Value {
        public int sum;
        public int product;
    }
}
