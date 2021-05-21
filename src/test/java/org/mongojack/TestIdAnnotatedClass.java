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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Test;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.mock.IdProxy;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestIdAnnotatedClass extends MongoDBTestBase {

    @SuppressWarnings("unchecked")
    private <T> JacksonMongoCollection<T> createCollFor(T object) {
        return getCollection((Class<T>) object.getClass());
    }

    @Test
    public void testIdFieldAnnotated() {
        IdFieldAnnotated o = new IdFieldAnnotated();
        o.id = "blah";
        JacksonMongoCollection<IdFieldAnnotated> coll = createCollFor(o);
        coll.insert(o);
        IdFieldAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo("blah"));
    }

    public static class IdFieldAnnotated {
        @Id
        public String id;
    }

    @Test
    public void testJpaIdFieldAnnotated() {
        JpaIdFieldAnnotated o = new JpaIdFieldAnnotated();
        o.id = "blah";
        JacksonMongoCollection<JpaIdFieldAnnotated> coll = createCollFor(o);
        coll.insert(o);
        JpaIdFieldAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo("blah"));
    }

    public static class JpaIdFieldAnnotated {
        @javax.persistence.Id
        public String id;
    }

    @Test
    public void testGetterSetterAnnotated() {
        GetterSetterAnnotated o = new GetterSetterAnnotated();
        o.setId("blah");
        JacksonMongoCollection<GetterSetterAnnotated> coll = createCollFor(o);
        coll.insert(o);
        GetterSetterAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo("blah"));
    }

    public static class GetterSetterAnnotated {
        private String id;

        @Id
        public String getId() {
            return id;
        }

        @Id
        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    public void testCreatorGetterAnnotated() {
        CreatorGetterAnnotated o = new CreatorGetterAnnotated("blah");
        JacksonMongoCollection<CreatorGetterAnnotated> coll = createCollFor(o);
        coll.insert(o);
        CreatorGetterAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo("blah"));
    }

    public static class CreatorGetterAnnotated {
        private final String id;

        @JsonCreator
        public CreatorGetterAnnotated(@Id String id) {
            this.id = id;
        }

        @Id
        public String getId() {
            return id;
        }
    }

    @Test
    public void testObjectIdFieldAnnotated() {
        ObjectIdFieldAnnotated o = new ObjectIdFieldAnnotated();
        o.id = new org.bson.types.ObjectId().toString();
        JacksonMongoCollection<ObjectIdFieldAnnotated> coll = createCollFor(o);
        coll.insert(o);
        ObjectIdFieldAnnotated result = coll.findOneById(o.id);
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo(o.id));
    }

    public static class ObjectIdFieldAnnotated {
        @Id
        @ObjectId
        public String id;
    }

    @Test
    public void testCreatorGetterObjectIdAnnotated() {
        CreatorGetterObjectIdAnnotated o = new CreatorGetterObjectIdAnnotated(
            new org.bson.types.ObjectId().toString());
        JacksonMongoCollection<CreatorGetterObjectIdAnnotated> coll = createCollFor(o);
        coll.insert(o);
        CreatorGetterObjectIdAnnotated result = coll.findOneById(o.id);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(o.id));
    }

    public static class CreatorGetterObjectIdAnnotated {
        private final String id;

        @JsonCreator
        public CreatorGetterObjectIdAnnotated(@Id @ObjectId String id) {
            this.id = id;
        }

        @Id
        @ObjectId
        public String getId() {
            return id;
        }
    }

    public static class IdFieldProxyAnnotated {
        @IdProxy
        public String id;
    }

    @Test
    public void testProxyFieldAnnotated() {
        IdFieldProxyAnnotated o = new IdFieldProxyAnnotated();
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId();
        final String objectIdAsHexString = objectId.toString();
        o.id = objectIdAsHexString;
        JacksonMongoCollection<IdFieldProxyAnnotated> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.id, notNullValue());
        IdFieldProxyAnnotated result = coll.findOneById(objectIdAsHexString);
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo(objectIdAsHexString));
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyFieldAnnotatedIsGeneratedByMongo() {
        IdFieldProxyAnnotated o = new IdFieldProxyAnnotated();
        JacksonMongoCollection<IdFieldProxyAnnotated> coll = createCollFor(o);
        coll.insert(o);
        IdFieldProxyAnnotated result = coll.find().first();
        assertThat(result, notNullValue());
        assertThat(result.id, notNullValue());
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(result.id);
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyFieldAnnotatedWithoutSettingIt() {
        ObjectMapper om = MongoJackModule.configure(new ObjectMapper());

        final SerializationConfig config = om.getSerializationConfig();
        final BeanDescription beanDescription = config.introspect(config.constructType(IdFieldProxyAnnotated.class));
        beanDescription.findProperties()
            .forEach(
                bpd -> {
                    if (bpd.getPrimaryMember().hasAnnotation(ObjectId.class)) {
                        System.out.println("Property " + bpd.getName() + " has ObjectId annotation");
                    }
                }
            );

        IdFieldProxyAnnotated o = new IdFieldProxyAnnotated();
        JacksonMongoCollection<IdFieldProxyAnnotated> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.id, notNullValue());
        IdFieldProxyAnnotated result = coll.findOneById(o.id);
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo(o.id));
    }

    public static class IdFieldProxyAnnotatedSubclass extends IdFieldProxyAnnotated {
    }

    @Test
    public void testProxyFieldAnnotatedSubclass() {
        IdFieldProxyAnnotatedSubclass o = new IdFieldProxyAnnotatedSubclass();
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId();
        final String objectIdAsHexString = objectId.toString();
        o.id = objectIdAsHexString;
        JacksonMongoCollection<IdFieldProxyAnnotatedSubclass> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.id, notNullValue());
        IdFieldProxyAnnotatedSubclass result = coll.findOneById(objectIdAsHexString);
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo(objectIdAsHexString));
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyFieldAnnotatedIsGeneratedByMongoSubclass() {
        IdFieldProxyAnnotatedSubclass o = new IdFieldProxyAnnotatedSubclass();
        JacksonMongoCollection<IdFieldProxyAnnotatedSubclass> coll = createCollFor(o);
        coll.insert(o);
        IdFieldProxyAnnotatedSubclass result = coll.find().first();
        assertThat(result, notNullValue());
        assertThat(result.id, notNullValue());
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(result.id);
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyFieldAnnotatedWithoutSettingItSubclass() {
        ObjectMapper om = MongoJackModule.configure(new ObjectMapper());

        final SerializationConfig config = om.getSerializationConfig();
        final BeanDescription beanDescription = config.introspect(config.constructType(IdFieldProxyAnnotatedSubclass.class));
        beanDescription.findProperties()
            .forEach(
                bpd -> {
                    if (bpd.getPrimaryMember().hasAnnotation(ObjectId.class)) {
                        System.out.println("Property " + bpd.getName() + " has ObjectId annotation");
                    }
                }
            );

        IdFieldProxyAnnotatedSubclass o = new IdFieldProxyAnnotatedSubclass();
        JacksonMongoCollection<IdFieldProxyAnnotatedSubclass> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.id, notNullValue());
        IdFieldProxyAnnotatedSubclass result = coll.findOneById(o.id);
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo(o.id));
    }

    public static class IdGetterSetterProxyAnnotated {
        private String notNamedId;

        @IdProxy
        public String getId() {
            return notNamedId;
        }

        @IdProxy
        public void setId(final String id) {
            this.notNamedId = id;
        }
    }

    @Test
    public void testProxyGetterSetterAnnotated() {
        IdGetterSetterProxyAnnotated o = new IdGetterSetterProxyAnnotated();
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId();
        final String objectIdAsHexString = objectId.toString();
        o.setId(objectIdAsHexString);
        JacksonMongoCollection<IdGetterSetterProxyAnnotated> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.getId(), notNullValue());
        IdGetterSetterProxyAnnotated result = coll.findOneById(objectIdAsHexString);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(objectIdAsHexString));
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyGetterSetterAnnotatedIsGeneratedByMongo() {
        IdGetterSetterProxyAnnotated o = new IdGetterSetterProxyAnnotated();
        JacksonMongoCollection<IdGetterSetterProxyAnnotated> coll = createCollFor(o);
        coll.insert(o);
        IdGetterSetterProxyAnnotated result = coll.find().first();
        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(result.getId());
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyGetterSetterAnnotatedWithoutSettingIt() {
        IdGetterSetterProxyAnnotated o = new IdGetterSetterProxyAnnotated();
        JacksonMongoCollection<IdGetterSetterProxyAnnotated> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.getId(), notNullValue());
        IdGetterSetterProxyAnnotated result = coll.findOneById(o.getId());
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(o.getId()));
    }

    public static class IdGetterSetterProxyAnnotatedSubclass extends IdGetterSetterProxyAnnotated {
    }

    @Test
    public void testProxyGetterSetterAnnotatedSubclass() {
        IdGetterSetterProxyAnnotatedSubclass o = new IdGetterSetterProxyAnnotatedSubclass();
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId();
        final String objectIdAsHexString = objectId.toString();
        o.setId(objectIdAsHexString);
        JacksonMongoCollection<IdGetterSetterProxyAnnotatedSubclass> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.getId(), notNullValue());
        IdGetterSetterProxyAnnotatedSubclass result = coll.findOneById(objectIdAsHexString);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(objectIdAsHexString));
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyGetterSetterAnnotatedIsGeneratedByMongoSubclass() {
        IdGetterSetterProxyAnnotatedSubclass o = new IdGetterSetterProxyAnnotatedSubclass();
        JacksonMongoCollection<IdGetterSetterProxyAnnotatedSubclass> coll = createCollFor(o);
        coll.insert(o);
        IdGetterSetterProxyAnnotatedSubclass result = coll.find().first();
        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        final org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(result.getId());
        Document doc = coll.getMongoCollection().withDocumentClass(Document.class).find(Filters.eq(objectId)).first();
        assertThat(doc, notNullValue());
        assertThat(doc.get("_id"), equalTo(objectId));
    }

    @Test
    public void testProxyGetterSetterAnnotatedWithoutSettingItSubclass() {
        IdGetterSetterProxyAnnotatedSubclass o = new IdGetterSetterProxyAnnotatedSubclass();
        JacksonMongoCollection<IdGetterSetterProxyAnnotatedSubclass> coll = createCollFor(o);
        coll.insert(o);
        assertThat(o.getId(), notNullValue());
        IdGetterSetterProxyAnnotatedSubclass result = coll.findOneById(o.getId());
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(o.getId()));
    }

}
