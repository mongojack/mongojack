/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack;

import org.mongojack.mock.MockObject;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.JacksonDBCollection;
import org.mongojack.MapReduce;
import org.mongojack.MapReduceOutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestMapReduce extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testInlineMapReduce() throws Exception {
        simpleMapReduce(MapReduce.OutputType.INLINE, null);
    }

    @Test
    public void testReplaceMapReduce() throws Exception {
        JacksonDBCollection<Simple, String> resultColl = getCollection(Simple.class, String.class);
        simpleMapReduce(MapReduce.OutputType.REPLACE, resultColl.getName());
    }

    private void simpleMapReduce(MapReduce.OutputType outputType, String collection) {
        coll.insert(new MockObject("foo", 10));
        coll.insert(new MockObject("foo", 15));
        coll.insert(new MockObject("bar", 5));

        MapReduceOutput<Simple, String> output = coll.mapReduce(
                MapReduce.build("function() {emit(this.string, this.integer);}",
                        "function(k, vals) {var sum=0;for(var i in vals) sum += vals[i];return sum;}",
                        outputType, collection, Simple.class, String.class));

        Simple foo = null;
        Simple bar = null;

        for (Simple result : output.results()) {
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
    public void testComplexInlineMapReduce() throws Exception {
        complexMapReduce(MapReduce.OutputType.INLINE, null);
    }

    @Test
    public void testComplexReplaceMapReduce() throws Exception {
        JacksonDBCollection<Complex, String> resultColl = getCollection(Complex.class, String.class);
        complexMapReduce(MapReduce.OutputType.REPLACE, resultColl.getName());
    }

    private void complexMapReduce(MapReduce.OutputType outputType, String collection) {
        coll.insert(new MockObject("foo", 10));
        coll.insert(new MockObject("foo", 15));
        coll.insert(new MockObject("bar", 5));

        MapReduceOutput<Complex, String> output = coll.mapReduce(
                MapReduce.build("function() {emit(this.string, {sum: this.integer, product: this.integer});}",
                        "function(k, vals) {var sum=0,product=1;for(var i in vals){sum+=vals[i].sum;product*=vals[i].product;}return {sum:sum,product:product};}",
                        outputType, collection, Complex.class, String.class));

        Complex foo = null;
        Complex bar = null;

        for (Complex result : output.results()) {
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
