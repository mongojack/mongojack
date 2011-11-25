package org.mongodb.jackson.internal;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.deser.BeanDeserializer;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.map.type.SimpleType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Place that hacks (using reflection) exist for Jackson, until we get features into jackson
 */
public class IdHandlerFactory {

    private final static Map<Class, Class> PRIMITIVE_MAP = new HashMap<Class, Class>();
    static {
        PRIMITIVE_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_MAP.put(byte.class, Byte.class);
        PRIMITIVE_MAP.put(short.class, Short.class);
        PRIMITIVE_MAP.put(char.class, Character.class);
        PRIMITIVE_MAP.put(int.class, Integer.class);
        PRIMITIVE_MAP.put(long.class, Long.class);
        PRIMITIVE_MAP.put(float.class, Float.class);
        PRIMITIVE_MAP.put(double.class, Double.class);
    }


    public static <K> IdHandler<K, ?> getIdHandlerForProperty(ObjectMapper objectMapper, Class klass,
                                                              String property, Class<K> type) throws JsonMappingException {
        return IdHandler.create(getBeanProperty(objectMapper, klass, property, type), type);
    }

    private static <K> BeanProperty getBeanProperty(ObjectMapper objectMapper, Class klass,
                                                               String property, Class<K> type) throws JsonMappingException {

        JsonDeserializer deserializer = objectMapper.getDeserializerProvider().findTypedValueDeserializer(
                objectMapper.copyDeserializationConfig(), SimpleType.construct(klass), null);
        if (deserializer instanceof BeanDeserializer) {
            // First priority is a property creator, see if one exists
            Object propertyBasedCreator = privateGet(deserializer, "_propertyBasedCreator");
            if (propertyBasedCreator != null) {
                Map<String, SettableBeanProperty> properties = (Map) privateGet(propertyBasedCreator, "_properties");
                SettableBeanProperty beanProperty = properties.get("_id");
                if (beanProperty != null) {
                    checkType(type, beanProperty.getType().getRawClass());
                    return beanProperty;
                }
            }

            // Now try setters
            Iterator<SettableBeanProperty> iter = ((BeanDeserializer) deserializer).properties();
            while (iter.hasNext()) {
                SettableBeanProperty beanProperty = iter.next();
                if (beanProperty.getName().equals("_id")) {
                    checkType(type, beanProperty.getType().getRawClass());
                    return beanProperty;
                }
            }
        }
        return null;
    }

    private static void checkType(Class expected, Class toCheck) throws JsonMappingException {
        if (toCheck.isPrimitive()) {
            toCheck = PRIMITIVE_MAP.get(toCheck);
        }
        if (!expected.isAssignableFrom(toCheck)) {
            throw new JsonMappingException("Type " + expected + " doesn't match properties type: " + toCheck);
        }
    }

    private static Object privateGet(Object o, String property) {
        try {
            Field f = o.getClass().getDeclaredField(property);
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            return f.get(o);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
