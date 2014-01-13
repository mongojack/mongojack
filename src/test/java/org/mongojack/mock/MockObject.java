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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Test object with one of each field
 */
public class MockObject {
    public String _id;
    public String string;
    public Integer integer;
    public Long longs;
    public BigInteger bigInteger;
    public Float floats;
    public Double doubles;
    public BigDecimal bigDecimal;
    public Boolean booleans;
    public Date date;

    public List<String> simpleList;
    public List<MockEmbeddedObject> complexList;
    public MockEmbeddedObject object;

    public MockObject() {
    }

    public MockObject(String _id, String string, Integer integer) {
        this._id = _id;
        this.string = string;
        this.integer = integer;
    }

    public MockObject(String string, Integer integer) {
        this.string = string;
        this.integer = integer;
    }

    @Override
    public String toString() {
        return "MockObject{" +
                "_id='" + _id + '\'' +
                ", string='" + string + '\'' +
                ", integer=" + integer +
                ", longs=" + longs +
                ", bigInteger=" + bigInteger +
                ", floats=" + floats +
                ", doubles=" + doubles +
                ", bigDecimal=" + bigDecimal +
                ", booleans=" + booleans +
                ", date=" + date +
                ", simpleList=" + simpleList +
                ", complexList=" + complexList +
                ", object=" + object +
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

        MockObject that = (MockObject) o;

        if (_id != null ? !_id.equals(that._id) : that._id != null) {
            return false;
        }
        if (bigDecimal != null ? !bigDecimal.equals(that.bigDecimal) : that.bigDecimal != null) {
            return false;
        }
        if (bigInteger != null ? !bigInteger.equals(that.bigInteger) : that.bigInteger != null) {
            return false;
        }
        if (booleans != null ? !booleans.equals(that.booleans) : that.booleans != null) {
            return false;
        }
        if (complexList != null ? !complexList.equals(that.complexList) : that.complexList != null) {
            return false;
        }
        if (doubles != null ? !doubles.equals(that.doubles) : that.doubles != null) {
            return false;
        }
        if (floats != null ? !floats.equals(that.floats) : that.floats != null) {
            return false;
        }
        if (integer != null ? !integer.equals(that.integer) : that.integer != null) {
            return false;
        }
        if (longs != null ? !longs.equals(that.longs) : that.longs != null) {
            return false;
        }
        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }
        if (object != null ? !object.equals(that.object) : that.object != null) {
            return false;
        }
        if (simpleList != null ? !simpleList.equals(that.simpleList) : that.simpleList != null) {
            return false;
        }
        if (string != null ? !string.equals(that.string) : that.string != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (string != null ? string.hashCode() : 0);
        result = 31 * result + (integer != null ? integer.hashCode() : 0);
        result = 31 * result + (longs != null ? longs.hashCode() : 0);
        result = 31 * result + (bigInteger != null ? bigInteger.hashCode() : 0);
        result = 31 * result + (floats != null ? floats.hashCode() : 0);
        result = 31 * result + (doubles != null ? doubles.hashCode() : 0);
        result = 31 * result + (bigDecimal != null ? bigDecimal.hashCode() : 0);
        result = 31 * result + (booleans != null ? booleans.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (simpleList != null ? simpleList.hashCode() : 0);
        result = 31 * result + (complexList != null ? complexList.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
