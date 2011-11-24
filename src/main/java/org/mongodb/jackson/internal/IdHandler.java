package org.mongodb.jackson.internal;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.BeanProperty;

/**
 * Handler for ids.  Converts them between the objects type and the database type
 */
public abstract class IdHandler {
    private final String idProperty;

    protected IdHandler(String idProperty) {
        this.idProperty = idProperty;
    }

    public String getIdProperty() {
        return idProperty;
    }

    /**
     * Convert the given database id to the java objects id
     *
     * @param dbId The database id to convert from
     * @return The converted id
     */
    public abstract Object fromDbId(Object dbId);

    /**
     * Convert the given java object id to the databases id
     *
     * @param id The java object id to convert from
     * @return The converted database id
     */
    public abstract Object toDbId(Object id);

    public static IdHandler create(BeanProperty beanProperty) {
        if (beanProperty.getAnnotation(org.mongodb.jackson.ObjectId.class) != null) {
            if (beanProperty.getType().getRawClass() == String.class) {
                return new StringToObjectIdHandler(beanProperty.getName());
            } else if (beanProperty.getType().getRawClass() == byte[].class) {
                return new ByteArrayToObjectIdHandler(beanProperty.getName());
            }
        }
        return new NoopIdHandler(beanProperty.getName());
    }

    public static class NoopIdHandler extends IdHandler {
        public NoopIdHandler(String idProperty) {
            super(idProperty);
        }

        @Override
        public Object fromDbId(Object dbId) {
            return dbId;
        }

        @Override
        public Object toDbId(Object id) {
            return id;
        }
    }

    public static class StringToObjectIdHandler extends IdHandler {
        public StringToObjectIdHandler(String idProperty) {
            super(idProperty);
        }

        @Override
        public Object fromDbId(Object dbId) {
            if (dbId instanceof ObjectId) {
                return dbId.toString();
            }
            return dbId;
        }

        @Override
        public Object toDbId(Object id) {
            if (id instanceof String) {
                return new ObjectId((String) id);
            }
            return id;
        }
    }

    public static class ByteArrayToObjectIdHandler extends IdHandler {
        public ByteArrayToObjectIdHandler(String idProperty) {
            super(idProperty);
        }

        @Override
        public Object fromDbId(Object dbId) {
            if (dbId instanceof ObjectId) {
                return ((ObjectId) dbId).toByteArray();
            }
            return dbId;
        }

        @Override
        public Object toDbId(Object id) {
            if (id instanceof byte[]) {
                return new ObjectId((byte[]) id);
            }
            return id;
        }
    }
}
