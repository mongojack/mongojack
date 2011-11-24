package org.mongodb.jackson;

import java.util.List;

/**
 * Embedded object
 */
public class MockEmbeddedObject {
    public String value;
    public List<String> list;

    @Override
    public String toString() {
        return "MockEmbeddedObject{" +
                "value='" + value + '\'' +
                ", list=" + list +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockEmbeddedObject that = (MockEmbeddedObject) o;

        if (list != null ? !list.equals(that.list) : that.list != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }
}
