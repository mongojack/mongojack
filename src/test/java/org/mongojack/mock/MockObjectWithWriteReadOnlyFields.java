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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to test READ_ONLY (only for serialization) and WRITE_ONLY (only for deserialization) fields
 *
 * @author Volodymyr Masliy
 */
public class MockObjectWithWriteReadOnlyFields {
    private String _id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String someReadOnlyField;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String someWriteOnlyField;

    public MockObjectWithWriteReadOnlyFields() {
    }

    public MockObjectWithWriteReadOnlyFields(String _id, String someReadOnlyField, String someWriteOnlyField) {
        this._id = _id;
        this.someReadOnlyField = someReadOnlyField;
        this.someWriteOnlyField = someWriteOnlyField;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSomeReadOnlyField() {
        return someReadOnlyField;
    }

    public void setSomeReadOnlyField(String someReadOnlyField) {
        this.someReadOnlyField = someReadOnlyField;
    }

    public String getSomeWriteOnlyField() {
        return someWriteOnlyField;
    }

    public void setSomeWriteOnlyField(String someWriteOnlyField) {
        this.someWriteOnlyField = someWriteOnlyField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MockObjectWithWriteReadOnlyFields that = (MockObjectWithWriteReadOnlyFields) o;
        return Objects.equals(_id, that._id) && Objects.equals(someReadOnlyField, that.someReadOnlyField) && Objects.equals(
            someWriteOnlyField, that.someWriteOnlyField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, someReadOnlyField, someWriteOnlyField);
    }

    @Override
    public String toString() {
        return "SampleWithWriteOnly{" +
            "_id='" + _id + '\'' +
            ", someReadOnlyField='" + someReadOnlyField + '\'' +
            ", someWriteOnlyField='" + someWriteOnlyField + '\'' +
            '}';
    }
}
