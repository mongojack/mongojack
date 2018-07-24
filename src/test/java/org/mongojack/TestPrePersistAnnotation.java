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

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * This test case tries out the various combinations of the @PrePersist annotation.
 *
 * @author dnebinger
 */
public class TestPrePersistAnnotation extends MongoDBTestBase {

    private <T, K> JacksonDBCollection<T, K> createCollFor(T object,
            Class<K> keyType) throws Exception {
        // Stupid generics...
        return (JacksonDBCollection) getCollection(object.getClass(), keyType);
    }

    @Test
    public void testPrePersistAnnotatedMethod() throws Exception {
        TestPrePersistAnnotation.PrePersistMethodAnnotated o = new PrePersistMethodAnnotated();
        o.id = "blahPre";
        JacksonDBCollection<TestPrePersistAnnotation.PrePersistMethodAnnotated, String> coll = createCollFor(o,
                String.class);
        coll.insert(o);
        assertThat(o.updated, equalTo("pre"));
        TestPrePersistAnnotation.PrePersistMethodAnnotated result = coll.findOneById("blahPre");
        assertThat(result, notNullValue());
        assertThat(result.updated, equalTo("pre"));
    }

    @Test
    public void testPrePersistAnnotatedSuperMethod() throws Exception {
        TestPrePersistAnnotation.PrePersistMethodAnnotatedSubclass o = new PrePersistMethodAnnotatedSubclass();
        o.id = "blahPre2";
        JacksonDBCollection<TestPrePersistAnnotation.PrePersistMethodAnnotatedSubclass, String> coll = createCollFor(o,
                String.class);
        coll.insert(o);
        assertThat(o.updated, equalTo("pre"));
        TestPrePersistAnnotation.PrePersistMethodAnnotatedSubclass result = coll.findOneById("blahPre2");
        assertThat(result, notNullValue());
        assertThat(result.updated, equalTo("pre"));
    }

    @Test
    public void testPrePersistAnnotatedMethods() throws Exception {
        TestPrePersistAnnotation.PrePersistMethodsAnnotated o = new PrePersistMethodsAnnotated();
        o.id = "blahPre3";
        JacksonDBCollection<TestPrePersistAnnotation.PrePersistMethodsAnnotated, String> coll = createCollFor(o,
                String.class);
        coll.insert(o);
        assertThat(o.updated1, equalTo("one"));
        assertThat(o.updated2, equalTo("two"));
        TestPrePersistAnnotation.PrePersistMethodsAnnotated result = coll.findOneById("blahPre3");
        assertThat(result, notNullValue());
        assertThat(result.updated1, equalTo("one"));
        assertThat(result.updated2, equalTo("two"));
    }

    @Test
    public void testPrePersistAnnotatedMethodOverride() throws Exception {
        TestPrePersistAnnotation.PrePersistMethodAnnotatedSubclassOverride o = new PrePersistMethodAnnotatedSubclassOverride();
        o.id = "blahPre4";
        JacksonDBCollection<TestPrePersistAnnotation.PrePersistMethodAnnotatedSubclassOverride, String> coll = createCollFor(o,
                String.class);
        coll.insert(o);
        assertThat(o.updated, equalTo("override"));
        TestPrePersistAnnotation.PrePersistMethodAnnotatedSubclassOverride result = coll.findOneById("blahPre4");
        assertThat(result, notNullValue());
        assertThat(result.updated, equalTo("override"));
    }

    @Test
    public void testUpdatePrePersistAnnotatedMethod() throws Exception {
        TestPrePersistAnnotation.UpdatePrePersistMethodAnnotated o = new UpdatePrePersistMethodAnnotated();
        o.id = "blahPre5";
        o.pass = "one";

        JacksonDBCollection<TestPrePersistAnnotation.UpdatePrePersistMethodAnnotated, String> coll = createCollFor(o,
                String.class);

        // the insert will call the PrePersist method with a value of false.
        coll.insert(o);
        assertThat(o.created, equalTo(o.lastModified));
        TestPrePersistAnnotation.UpdatePrePersistMethodAnnotated result = coll.findOneById("blahPre5");
        assertThat(result, notNullValue());
        assertThat(result.created, equalTo(result.lastModified));

        // wait 3 secs to ensure the update will work.
        Thread.sleep(3000);

        // now set up for the update
        result.pass = "two";
        DBQuery.is("pass", "one");
        coll.update(DBQuery.is("pass", "one"), result);

        TestPrePersistAnnotation.UpdatePrePersistMethodAnnotated result2 = coll.findOneById("blahPre5");
        assertThat(result2, notNullValue());
        assertThat(result2.created, not(equalTo(result.lastModified)));
    }

    public static class PrePersistMethodAnnotated {
        @Id
        public String id;
        public String updated = "n/a";

        @PrePersist
        void prePersist() {
            updated = "pre";
        }
    }

    public static class PrePersistMethodAnnotatedSubclass extends PrePersistMethodAnnotated {
        public String untouched;
    }

    public static class PrePersistMethodsAnnotated {
        @Id
        public String id;
        public String updated1 = "n/a";
        public String updated2 = "n/a";

        @PrePersist
        void prePersistOne() {
            updated1 = "one";
        }

        @PrePersist
        void prePersistTwo() {
            updated2 = "two";
        }
    }

    public static class PrePersistMethodAnnotatedSubclassOverride extends PrePersistMethodAnnotated {
        public String untouched;

        @PrePersist
        void overridePrePersist() {
            updated = "override";
        }
    }

    public static class UpdatePrePersistMethodAnnotated {
        @Id
        public String id;
        public String created = "n/a";
        public String lastModified = "n/a";
        public String pass = "zero";

        @PrePersist
        void prePersist(final boolean update) {
            String currentDate = String.valueOf(System.currentTimeMillis());

            lastModified = currentDate;

            if (! update) {
                // this is an insert/save, go ahead and set created date.
                created = currentDate;
            }
        }
    }


}
