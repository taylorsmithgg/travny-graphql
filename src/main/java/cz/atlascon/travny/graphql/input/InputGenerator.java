package cz.atlascon.travny.graphql.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.schemas.*;
import cz.atlascon.travny.types.Type;
import graphql.schema.*;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static cz.atlascon.travny.graphql.common.Common.convertToName;
import static cz.atlascon.travny.graphql.common.Common.getName;
import static graphql.Scalars.GraphQLString;

/**
 * Created by tomas on 25.6.17.
 */
public class InputGenerator {
    private final ConcurrentMap<String, GraphQLInputObjectType> inputMap = Maps.newConcurrentMap();
    private final ClassConvertor convertor;
    private static final String SUFFIX = "_input";
    public static final String PREDICATE_NAME = "predicate";
    private static final String DESCRIPTION = "this is same as original but with suffix '_input'. Original schema: ";

    public InputGenerator(ClassConvertor classConvertor) {
        this.convertor = classConvertor;
    }

    private GraphQLInputType createType(Schema schema) {
        Preconditions.checkNotNull(schema, "Got null schema");
        if (Type.ENUM == schema.getType()) {
            return convertor.getInputType(schema);
        } else if (Type.RECORD == schema.getType()) {
            return createRecordType((RecordSchema) schema);
        } else if (Type.LIST == schema.getType()) {
            Schema valueSchema = ((ListSchema) schema).getValueSchema();
            return GraphQLList.list(createType(valueSchema));
        } else if (schema.getType() == Type.MAP) {
            MapSchema mapSchema = (MapSchema) schema;
            GraphQLInputType key = createType(mapSchema.getKeySchema());
            GraphQLInputType val = createType(mapSchema.getValueSchema());
            String entryName = "map_entry_" + key.getName() + "_" + val.getName();

            GraphQLInputType entryType = inputMap.computeIfAbsent(entryName, n -> {
                GraphQLInputObjectField keyField = createField("key", key);
                GraphQLInputObjectField valField = createField("val", val);
                return GraphQLInputObjectType.newInputObject()
                        .name(entryName)
                        .field(keyField)
                        .field(valField)
                        .build();
            });
            return GraphQLList.list(entryType);
        } else {
            return convertor.getInputType(schema);
        }
    }

    public List<GraphQLArgument> createRootField(RecordSchema idSchema, boolean withRootFieldPredicate) {
        List<GraphQLArgument> arguments = Lists.newArrayList();
        if (idSchema == null) {
            return arguments;
        }
        for (Field field : idSchema.getFields()) {
            if (!field.isRemoved()) {
                GraphQLInputType toArgument = createType(field.getSchema());

                GraphQLArgument argument = GraphQLArgument.newArgument()
                        .name(field.getName())
                        .type(toArgument)
                        .description("This is input argument for field name: " + field.getName())
                        .build();
                arguments.add(argument);
            }
        }
        if(withRootFieldPredicate){
            GraphQLArgument argument = GraphQLArgument.newArgument()
                    .name(PREDICATE_NAME)
                    .type(GraphQLString)
                    .description("This is input argument for predicate field, its purpose is to supply predice for backend: " + PREDICATE_NAME)
                    .build();
            arguments.add(argument);
        }

        return arguments;
    }

    private GraphQLInputObjectType createRecordType(RecordSchema recordSchema) {
        String className = convertToName(recordSchema.getName()) + SUFFIX;
        GraphQLInputObjectType existing = inputMap.get(className);
        if (existing != null) {
            return existing;
        } else {
            List<GraphQLInputObjectField> fieldDefs = Lists.newArrayList();
            for (Field field : recordSchema.getFields()) {
                // skip removed fields
                if (field.isRemoved()) {
                    continue;
                }
                GraphQLInputObjectField fieldDef = createField(field.getName(),
                        createType(field.getSchema()));
                fieldDefs.add(fieldDef);
            }

            GraphQLInputObjectType objectType = GraphQLInputObjectType
                    .newInputObject()
                    .fields(fieldDefs)
                    .name(className)
                    .description(DESCRIPTION + convertToName(recordSchema.getName()))
                    .build();
            inputMap.put(className, objectType);
            return objectType;
        }
    }

    private GraphQLInputObjectField createField(String name, GraphQLInputType type) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(name)
                .type(type)
                .build();
    }
}
