package org.mongodb.jackson.internal;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;

/**
 * The ObjectID serialising module
 */
public class ObjectIdModule extends Module {
    public static final Module INSTANCE = new ObjectIdModule();

    @Override
    public String getModuleName() {
        return "Object ID Module";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, null);
    }

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(new ObjectIdAnnotationIntrospector());
    }
}
