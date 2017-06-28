package cz.atlascon.travny.graphql.output;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.parser.SchemaNameUtils;
import cz.atlascon.travny.schemas.*;
import cz.atlascon.travny.types.Type;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static cz.atlascon.travny.graphql.common.Common.convertToName;

/**
 * Created by tomas on 25.6.17.
 */
public class OutputGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputGenerator.class);
    private final ConcurrentMap<String, GraphQLOutputType> typeMap = Maps.newConcurrentMap();
    private final ClassConvertor convertor;
    private volatile Function<String, RecordSchema> schemaSupplier = n -> null;

    public OutputGenerator(ClassConvertor classConvertor) {
        this.convertor = classConvertor;
    }

    public void setSchemaSupplier(Function<String, RecordSchema> schemaSupplier) {
        this.schemaSupplier = schemaSupplier;
    }

    public ConcurrentMap<String, GraphQLOutputType> getTypeMap() {
        return typeMap;
    }

    public GraphQLOutputType createRootField(RecordSchema schema) {
        LOGGER.info("Creating  GraphQL object type for {}", schema.getName());
        Preconditions.checkArgument(!schema.getFields().isEmpty());
        return createType(schema);
    }

    private GraphQLFieldDefinition createField(String fieldName, GraphQLOutputType type) {
        Preconditions.checkNotNull(type, "Got null type");
        Preconditions.checkNotNull(fieldName, "Got null fieldName");
        GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                .type(type)
                //TODO implement default values .staticValue()
                .name(fieldName)
                .build();
        return fieldDef;
    }

    private GraphQLOutputType createType(Schema schema) {
        Preconditions.checkNotNull(schema, "Got null schema");
        if (Type.ENUM == schema.getType()) {
            String className = convertToName(((EnumSchema) schema).getName());
            return typeMap.computeIfAbsent(className, enumName -> convertor.getOutputType(schema));
        } else if (Type.RECORD == schema.getType()) {
            GraphQLObjectType recordType = createRecordType((RecordSchema) schema);
            return new GraphQLTypeReference(recordType.getName());
        } else if (Type.LIST == schema.getType()) {
            Schema valueSchema = ((ListSchema) schema).getValueSchema();
            return GraphQLList.list(createType(valueSchema));
        } else if (schema.getType() == Type.MAP) {
            MapSchema mapSchema = (MapSchema) schema;
            GraphQLOutputType key = createType(mapSchema.getKeySchema());
            GraphQLOutputType val = createType(mapSchema.getValueSchema());
            GraphQLFieldDefinition keyField = createField("key", key);
            GraphQLFieldDefinition valField = createField("val", val);
            return GraphQLObjectType
                    .newObject()
                    .field(keyField)
                    .field(valField)
                    .name("map")
                    .build();
        } else {
            return convertor.getOutputType(schema);
        }
    }

    private GraphQLObjectType createRecordType(RecordSchema recordSchema) {
        String className = convertToName(recordSchema.getName());
        GraphQLOutputType existing = typeMap.get(className);
        if (existing != null) {
            return (GraphQLObjectType) existing;
        } else {
            LOGGER.info("Creating type for {}", className);
            if (!SchemaNameUtils.isIdSchema(recordSchema.getName())) {
                // if this is a record schema, create ID schema with reference to record first
                // rec schema for ID
                RecordSchema idSchema = recordSchema.getIdSchema();
                if (idSchema != null) {
                    GraphQLObjectType idType = createRecordType(idSchema);
                    // reference to object
                    GraphQLTypeReference typeRef = new GraphQLTypeReference(className);
                    List<GraphQLFieldDefinition> idFields = Lists.newArrayList(idType.getFieldDefinitions());
                    idFields.add(createField("_resolved", typeRef));
                    GraphQLObjectType idObjectType = new GraphQLObjectType.Builder()
                            .fields(idFields)
                            .name(idType.getName())
                            .description(idType.getDescription())
                            .build();
                    typeMap.put(idObjectType.getName(), idObjectType);
                }
            }
            List<GraphQLFieldDefinition> fieldDefs = Lists.newArrayList();
            for (Field field : recordSchema.getFields()) {
                if(!field.isRemoved()){
                    GraphQLFieldDefinition fieldDef = createField(field.getName(), createType(field.getSchema()));
                    fieldDefs.add(fieldDef);
                }
            }
            GraphQLObjectType objectType = GraphQLObjectType
                    .newObject()
                    .fields(fieldDefs)
                    .name(className)
                    .build();
            typeMap.put(className, objectType);
            return objectType;
        }
    }

}
