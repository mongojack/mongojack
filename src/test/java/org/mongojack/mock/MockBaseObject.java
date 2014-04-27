package org.mongojack.mock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class MockBaseObject {
    public String _id;
    public int baseField;
}
