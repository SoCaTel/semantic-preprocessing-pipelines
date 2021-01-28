package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Can be used to load values into a properties of an object.
 *
 * <p>Known limitations:
 * - Does not support arrays.
 * - Does not support generics with more then one argument.
 * - Does not support nested collections.
 * - Does not support language tags.
 *
 * <p>This class can be extended to provide additional functionality.
 */
class FieldLoader {

    private static final Set<Class<?>> WRAP_TYPES;

    private static final Logger LOG =
            LoggerFactory.getLogger(FieldLoader.class);

    static {
        WRAP_TYPES = new HashSet<>();
        WRAP_TYPES.add(Boolean.class);
        WRAP_TYPES.add(Character.class);
        WRAP_TYPES.add(Byte.class);
        WRAP_TYPES.add(Short.class);
        WRAP_TYPES.add(Integer.class);
        WRAP_TYPES.add(Long.class);
        WRAP_TYPES.add(Float.class);
        WRAP_TYPES.add(Double.class);
        WRAP_TYPES.add(Void.class);
        WRAP_TYPES.add(String.class);
        WRAP_TYPES.add(Date.class);
    }

    /**
     * If the field represent a primitive type, then the value is converted
     * to given type and stored in the field.
     *
     * <p>If the field represent a complex object or a collection,
     * then the behaviour depends on extendExisting.
     *
     * <p>If the extendExisting is true, then new object is added to collection,
     * and new object is set as a complex type.
     *
     * <p>If the extendExisting is false, the collection is cleared before
     * adding any object and new complex objects are created.
     */
    public Object set(
            Object target, Field field, BackendRdfValue value,
            boolean extendExisting)
            throws LoaderException {
        Class<?> fieldType = field.getType();
        if (Collection.class.isAssignableFrom(fieldType)) {
            return setCollection(target, field, value, extendExisting);
        } else if (isPrimitive(fieldType)) {
            FieldUtils.setValue(target, field,
                    valueToPrimitive(fieldType, value));
        } else if (fieldType.isEnum()) {
            FieldUtils.setValue(target, field, valueToEnum(fieldType, value));
        } else if (fieldType.isArray()) {
            throw new LoaderException("Arrays are not supported.");
        } else {
            if (LangString.class.isAssignableFrom(fieldType)) {
                FieldUtils.setValue(target, field,
                        valueToStringLang(fieldType, value));
                return null;
            }
            // It's a regular object.
            if (extendExisting) {
                final Object currentObject = FieldUtils.getValue(target, field);
                if (currentObject != null) {
                    return currentObject;
                }
            }
            // Create and set new object.
            Object newObject = createInstance(fieldType);
            FieldUtils.setValue(target, field, newObject);
            return newObject;
        }
        return null;
    }

    private static Object setCollection(
            Object target, Field field, BackendRdfValue value,
            boolean extendExisting) throws LoaderException {
        Class<?> genericType = getCollectionType(field.getGenericType());
        if (Collection.class.isAssignableFrom(genericType)) {
            throw new LoaderException(
                    "Nested collection are not supported.");
        } else if (isPrimitive(genericType)) {
            addToCollection(target, field,
                    valueToPrimitive(genericType, value), extendExisting);
        } else if (genericType.isEnum()) {
            addToCollection(target, field, valueToEnum(genericType, value),
                    extendExisting);
        } else if (genericType.isArray()) {
            throw new LoaderException("Arrays are not supported.");
        } else {
            if (LangString.class.isAssignableFrom(genericType)) {
                addToCollection(target, field,
                        valueToStringLang(genericType, value),
                        extendExisting);
                return null;
            } else {
                final Object newObject = createInstance(genericType);
                addToCollection(target, field, newObject, extendExisting);
                return newObject;
            }
        }
        return null;
    }

    private static void addToCollection(
            Object object, Field field, Object value, boolean extend)
            throws LoaderException {
        Collection collection = (Collection) FieldUtils.getValue(object, field);
        if (collection == null) {
            throw new LoaderException(
                    "Collection must be initialized prior to loading."
                            + " Collection: '" + field.getName()
                            + "' on class: '"
                            + object.getClass().getCanonicalName() + "'");
        }
        if (!extend) {
            collection.clear();
        }
        collection.add(value);
    }

    private static Object createInstance(Class<?> type) throws LoaderException {
        try {
            return type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new LoaderException("Can't handle object instance.", ex);
        }
    }

    private static Object valueToEnum(Class<?> type, BackendRdfValue value) {
        return Enum.valueOf((Class<Enum>) type, value.asString());
    }

    private static Object valueToStringLang(
            Class<?> fieldType, BackendRdfValue value) throws LoaderException {
        LangString langString = (LangString) createInstance(fieldType);
        String language = value.getLanguage();
        langString.setValue(value.asString(), language);
        return langString;
    }

    private static Object valueToPrimitive(Class<?> type, BackendRdfValue value)
            throws LoaderException {
        try {
            if (type == String.class) {
                return value.asString();
            } else if (type == boolean.class || type == Boolean.class) {
                return value.asBoolean();
            } else if (type == byte.class || type == Byte.class) {
                return (byte) value.asLong();
            } else if (type == short.class || type == Short.class) {
                return (short) value.asLong();
            } else if (type == int.class || type == Integer.class) {
                return (int) value.asLong();
            } else if (type == long.class || type == Long.class) {
                return value.asLong();
            } else if (type == Date.class) {
                // We use GMT time zone as default, to have the same
                // settings on different systems.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                return format.parse(value.asString());
            } else {
                throw new LoaderException("Unknown type: {}", type.getName());
            }
        } catch (RdfUtilsException | ParseException ex) {
            throw new LoaderException("Can't convert to primitive.", ex);
        }
    }

    private static boolean isPrimitive(Class<?> fieldClass) {
        return fieldClass.isPrimitive() || WRAP_TYPES.contains(fieldClass);
    }

    private static Class<?> getCollectionType(Type type)
            throws LoaderException {
        if (!(type instanceof ParameterizedType)) {
            LOG.warn("Superclass it not instance of ParameterizedType");
            return null;
        }
        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
        // We know there should be just one for Collection.
        if (params.length != 1) {
            throw new LoaderException("Unexpected number of generic types: "
                    + params.length + " (1 expected)");
        }
        if (!(params[0] instanceof Class)) {
            throw new LoaderException(
                    "Unexpected type: " + params[0].toString());
        }
        return (Class<?>) params[0];
    }

}
