package cz.atlascon.travny.graphql.input;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.domain.MapEntry;
import cz.atlascon.travny.records.CustomRecord;
import cz.atlascon.travny.schemas.*;
import cz.atlascon.travny.types.EnumConstantImpl;
import cz.atlascon.travny.types.Type;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class InputConvertor {
    private static final Predicate<Type> IS_SIMPLE = type -> type == Type.INT || type == Type.LONG
            || type == Type.DOUBLE || type == Type.FLOAT || type == Type.BOOLEAN || type == Type.STRING;


    public <R extends CustomRecord> R convert(RecordSchema idSchema, DataFetchingEnvironment environment) throws Exception {
        Map<String, Object> arguments = environment.getArguments();
        CustomRecord customRecord = convertRecord(idSchema, arguments);
        return (R) customRecord;
    }

    public <R extends CustomRecord> R convert(Class<R> aClass, DataFetchingEnvironment environment) throws Exception{
        RecordSchema schema = (RecordSchema) Schemas.getSchema(aClass);
        return (R) convert(schema, environment);
    }

    private <T extends CustomRecord> T convertRecord(RecordSchema schema, Map<String, Object> arguments) throws ClassNotFoundException {
        Class<T> aClass = (Class<T>) Class.forName(schema.getName());
        T record = CustomRecords.newInstance(aClass);

        for (Field field : schema.getFields()) {
            if (!field.isRemoved()) {
                Object o = convertField(field.getSchema(), arguments.get(field.getName()));
                record.set(field.getName(), o);
            }
        }

        return record;
    }

    private <E extends Enum> Object convertField(Schema schema, Object o) throws ClassNotFoundException {
        if (o == null || IS_SIMPLE.test(schema.getType())) {
            return o;
        }
        if (schema.getType() == Type.MAP) {
            MapSchema mapSchema = (MapSchema) schema;
            Schema keySchema = mapSchema.getKeySchema();
            Schema valueSchema = mapSchema.getValueSchema();

            List<Map<String, Object>> list = (List<Map<String, Object>>) o;
            Map<Object, Object> outputMap = Maps.newHashMap();

            for (Map<String, Object> objectMap : list) {
                Object key = convertField(keySchema, objectMap.get(MapEntry.KEY_NAME));
                Object val = convertField(valueSchema, objectMap.get(MapEntry.VALUE_NAME));
                outputMap.put(key, val);
            }

            return outputMap;
        }
        if (schema.getType() == Type.ENUM) {
            Class<E> enumClass = (Class<E>) Class.forName(((EnumSchema) schema).getName());
            List<E> enumConstants = Lists.newArrayList(enumClass.getEnumConstants());
            return enumConstants
                    .stream()
                    .filter(en -> en.name().equals(((EnumConstantImpl) o).getConstant()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Wrong enum constant " + o + " for enum " + enumClass.getName()));
        }
        if (schema.getType() == Type.LIST) {
            List list = (List) o;
            List<Object> objects = Lists.newArrayList();
            Schema valueSchema = ((ListSchema) schema).getValueSchema();
            for (Object o2 : list) {
                objects.add(convertField(valueSchema, o2));
            }
            return objects;
        }
        if (schema.getType() == Type.RECORD) {
            RecordSchema recordSchema = (RecordSchema) schema;
            return convertRecord(recordSchema, (Map<String, Object>) o);
        }

        return null;
    }
}
