package org.mongojack.mock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A base type for polymorphic tests.
 * @author Luke Palmer
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class MockBaseObject {
    public String _id;
    public int baseField;
}
