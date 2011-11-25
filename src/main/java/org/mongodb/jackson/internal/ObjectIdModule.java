package org.mongodb.jackson.internal;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
        // Only include non null properties, this makes it possible to use object templates for querying and
        // partial object retrieving
        context.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
}
