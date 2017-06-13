package cz.atlascon.travny.graphql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.ListSchema;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.schemas.Schema;
import cz.atlascon.travny.types.Type;
import graphql.java.generator.DefaultBuildContext;
import graphql.java.generator.type.ITypeGenerator;
import graphql.schema.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by tomas on 6.6.17.
 */
public class GraphQLGeneratorImpl implements GraphQLGenerator {

    private final ConcurrentMap<String, GraphQLObjectType> objectMap = Maps.newConcurrentMap();
    private final ConcurrentMap<String, GraphQLInputObjectType> inputMap = Maps.newConcurrentMap();
    private final ITypeGenerator generator = DefaultBuildContext.reflectionContext;

    @Override
    public GraphQLSchema generateSchemaWFetcher(List<RecordSchema> recordSchemas, DataFetcher<Collection<?>> dataFetcher) {
        Preconditions.checkNotNull(recordSchemas, "Not Valid use recordSchemas cannot be null!");
        Preconditions.checkArgument(!recordSchemas.isEmpty(), "Not valid Use, recordSchemas cannot be empty!");
        return GraphQLSchema.newSchema()
                .query(createRootObject(createTypes(recordSchemas, dataFetcher)))
                .build();
    }

    private List<GraphQLFieldDefinition> createTypes(List<RecordSchema> recordSchemas, DataFetcher dataFetcher) {
        List<GraphQLFieldDefinition> fieldDefinitions = Lists.newArrayList();
        for (RecordSchema recordSchema : recordSchemas) {
            fieldDefinitions.add(createRootField(recordSchema, dataFetcher));
        }

        return fieldDefinitions;
    }

    private GraphQLFieldDefinition createRootField(RecordSchema schema, DataFetcher dataFetcher) {
        Preconditions.checkNotNull(schema, "schema cannot be null!");
        String rootName = convertToName(schema.getName());
        objectMap.putIfAbsent(rootName, GraphQLObjectType.newObject()
                .fields(createFields(schema.getFields()))
                .name(rootName)
                .build());
        GraphQLObjectType rootType = objectMap.get(rootName);

        // get name of class without packages
        List<String> strings = Splitter.on('.').splitToList(schema.getName());
        String fieldName = strings.get(strings.size() - 1).toLowerCase();

        return GraphQLFieldDefinition.newFieldDefinition()
                .type(GraphQLList.list(rootType))
                .argument(createArgumentsFromIdSchema(schema.getIdSchema()))
                .name(fieldName)
                .dataFetcher(dataFetcher)
                .description("Generated graphQL schema for class: " + schema.getName())
                .build();
    }

    private List<GraphQLArgument> createArgumentsFromIdSchema(RecordSchema idSchema) {
        List<GraphQLArgument> arguments = Lists.newArrayList();
        if (idSchema == null) {
            return arguments;
        }
        for (Field field : idSchema.getFields()) {
            GraphQLInputType inputType = generator.getInputType(field.getSchema().getType().getJavaClass());
            if (inputType instanceof GraphQLScalarType == false) {
                String name = inputType.getName() + "_arg";
                inputMap.putIfAbsent(name, GraphQLInputObjectType.newInputObject()
                        .name(name)
                        .description(((GraphQLInputObjectType) inputType).getDescription())
                        .fields(((GraphQLInputObjectType) inputType).getFields())
                        .build());

                inputType = inputMap.get(name);
            }

            GraphQLArgument build = GraphQLArgument.newArgument()
                    .name(field.getName())
                    .type(inputType)
                    .description("This is input argument for field name: " + field.getName())
                    .build();
            arguments.add(build);
        }

        return arguments;
    }

    /**
     * We assume that each GraphQLFieldDefinition is separated class for root Object
     *
     * @param schema
     * @return
     */
    private GraphQLObjectType createRootObject(List<GraphQLFieldDefinition> schema) {
        return GraphQLObjectType.newObject()
                .fields(schema)
                .name("RootObject")
                .description("This is root object type. Which main purpose is to hold all Objects/Fields for which you can ask. Down format is (fieldName):(fieldType)")
                .build();
    }

    private List<GraphQLFieldDefinition> createFields(List<Field> fields) {
        List<GraphQLFieldDefinition> qlFields = Lists.newArrayList();
        Preconditions.checkNotNull(fields);
        Preconditions.checkArgument(!fields.isEmpty(), "There must exist some fields! Empty class is not allowed!");

        GraphQLOutputType outputType;
        for (Field field : fields) {
            if (Type.RECORD == field.getSchema().getType()) {
                String className = convertToName(((RecordSchema) field.getSchema()).getName());
                objectMap.putIfAbsent(className, GraphQLObjectType.newObject()
                        .fields(createFields(((RecordSchema) field.getSchema()).getFields()))
                        .name(className)
                        .build());
                outputType = objectMap.get(className);
            } else if(field.getSchema() instanceof ListSchema){
                GraphQLType graphQLType;
                Schema listSchema = ((ListSchema) field.getSchema()).getValueSchema();
                if(Type.RECORD == listSchema.getType()){
                    String className = convertToName(((RecordSchema) listSchema).getName());
                    objectMap.putIfAbsent(className, GraphQLObjectType.newObject()
                            .fields(createFields((((RecordSchema) listSchema).getFields())))
                            .name(className)
                            .build());
                    graphQLType = objectMap.get(className);
                } else {
                    Class javaClass = ((ListSchema) field.getSchema()).getValueSchema().getType().getJavaClass();
                    graphQLType = generator.getOutputType(javaClass);
                }

                outputType = GraphQLList.list(graphQLType);
            } else {
                outputType = generator.getOutputType(field.getSchema().getType().getJavaClass());
            }

            GraphQLFieldDefinition build = GraphQLFieldDefinition.newFieldDefinition()
                    .type(outputType)
                    //TODO implement default values .staticValue()
                    .name(field.getName())
                    .description("This is field for: " + field.getSchema().getType().getJavaClass().getName())
                    .build();
            qlFields.add(build);
        }

        return qlFields;
    }

    private GraphQLObjectType createListField(Schema schema) {
        if(Type.RECORD == schema.getType()){

            createListField(schema);
        }

        return null;
    }

    private String convertToName(String name) {
        if (name.contains("id<")) {
            String s = name.replaceAll("id<|>", "");
            name = s + "_ID";
        }
        return name.replaceAll("\\.|<|>", "_");
    }
}
