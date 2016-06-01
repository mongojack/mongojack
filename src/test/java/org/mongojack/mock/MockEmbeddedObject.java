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
package org.mongojack.mock;

import java.util.List;

/**
 * Embedded object
 */
public class MockEmbeddedObject {

    public static class MockEmbeddedListElement {
        public MockEmbeddedListElement() {}

        public MockEmbeddedListElement(int id) {
            this.id = id;
        }

        public Integer id;

        @Override
        public String toString() {
            return "MockEmbeddedListElement{" +
                    "id='" + id + '\'' +
                    '}';
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MockEmbeddedListElement that = (MockEmbeddedListElement) o;

            if (id != null ? !id.equals(that.id) : that.id != null) {
                return false;
            }

            return true;
        }

    }

    public String value;
    public List<String> list;
    public List<MockEmbeddedListElement> objectList;

    public MockEmbeddedObject() {
    }

    public MockEmbeddedObject(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MockEmbeddedObject{" +
                "value='" + value + '\'' +
                ", list=" + list +
                ", objectList=" + objectList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockEmbeddedObject that = (MockEmbeddedObject) o;

        if (list != null ? !list.equals(that.list) : that.list != null) {
            return false;
        }
        if (objectList != null ? !objectList.equals(that.objectList) : that.objectList != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (list != null ? list.hashCode() : 0);
        result = 31 * result + (objectList != null ? objectList.hashCode() : 0);
        return result;
    }
}
