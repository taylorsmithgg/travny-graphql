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
    private TravnyFieldDataFetcherFactory dataFetcher;
    private volatile Function<String, RecordSchema> schemaSupplier = n -> null;

    public OutputGenerator(ClassConvertor classConvertor,
                           TravnyFieldDataFetcherFactory dataFetcher) {
        Preconditions.checkNotNull(classConvertor);
        Preconditions.checkNotNull(dataFetcher);
        this.convertor = classConvertor;
        this.dataFetcher = dataFetcher;
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

    private GraphQLFieldDefinition createField(String fieldName, GraphQLOutputType type, DataFetcher fieldFetcher) {
        Preconditions.checkNotNull(type, "Got null type");
        Preconditions.checkNotNull(fieldName, "Got null fieldName");
        GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                .type(type)
                .dataFetcher(fieldFetcher)
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
            RecordSchema recSchema = replaceIdForRecordIfId((RecordSchema) schema);
            GraphQLObjectType recordType = createRecordType(recSchema);
            return new GraphQLTypeReference(recordType.getName());
        } else if (Type.LIST == schema.getType()) {
            Schema valueSchema = ((ListSchema) schema).getValueSchema();
            return GraphQLList.list(createType(valueSchema));
        } else if (schema.getType() == Type.MAP) {
            MapSchema mapSchema = (MapSchema) schema;
            GraphQLOutputType key = createType(mapSchema.getKeySchema());
            GraphQLOutputType val = createType(mapSchema.getValueSchema());
            String entryName = "map_entry_" + key.getName() + "_" + val.getName();
            GraphQLOutputType entryType = typeMap.computeIfAbsent(entryName, n -> {
                GraphQLFieldDefinition keyField = createField("key", key, null);
                GraphQLFieldDefinition valField = createField("val", val, null);
                return GraphQLObjectType
                        .newObject()
                        .field(keyField)
                        .field(valField)
                        .name(entryName)
                        .build();
            });
            return GraphQLList.list(entryType);
        } else {
            return convertor.getOutputType(schema);
        }
    }

    private RecordSchema replaceIdForRecordIfId(RecordSchema recordSchema) {
        // type for ID schema = type for REC schemas
        if (SchemaNameUtils.isIdSchema(recordSchema.getName())) {
            String recSchemaName = SchemaNameUtils.getRecordForId(recordSchema.getName());
            recordSchema = schemaSupplier.apply(recSchemaName);
            if (recordSchema == null) {
                throw new NullPointerException("Schema " + recSchemaName + " not found!");
            }
        }
        return recordSchema;
    }

    private GraphQLObjectType createRecordType(RecordSchema recordSchema) {
        String className = convertToName(recordSchema.getName());
        GraphQLOutputType existing = typeMap.get(className);
        if (existing != null) {
            return (GraphQLObjectType) existing;
        } else {
            LOGGER.info("Creating type for {}", className);
            List<GraphQLFieldDefinition> fieldDefs = Lists.newArrayList();
            for (Field field : recordSchema.getFields()) {
                // skip removed fields
                if (field.isRemoved()) {
                    continue;
                }
                GraphQLFieldDefinition refDef = createFieldSelfRef(field, recordSchema);
                if (refDef != null) {
                    fieldDefs.add(refDef);
                    continue;
                }
                GraphQLFieldDefinition fieldDef = createField(field.getName(), createType(field.getSchema()),
                        dataFetcher.create(field));
                fieldDefs.add(fieldDef);
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

    private GraphQLFieldDefinition createFieldSelfRef(Field field, RecordSchema recordSchema) {
        // field is another record
        if (field.getSchema() instanceof RecordSchema) {
            String fieldSchemaName = ((RecordSchema) field.getSchema()).getName();
            // field is ID record
            if (SchemaNameUtils.isIdSchema(fieldSchemaName)) {
                // if field is ID self reference -> point to itself
                String recName = SchemaNameUtils.getRecordForId(fieldSchemaName);
                if (recName.equals(recordSchema.getName())) {
                    return createField(field.getName(), new GraphQLTypeReference(convertToName(recName)), null);
                }
            }
        }
        return null;
    }

}
