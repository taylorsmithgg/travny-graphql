package cz.atlascon.travny.graphql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.types.Type;
import graphql.java.generator.DefaultBuildContext;
import graphql.java.generator.type.ITypeGenerator;
import graphql.schema.*;

import java.util.Collection;
import java.util.List;

/**
 * Created by tomas on 6.6.17.
 */
public class GraphQLGeneratorImpl implements GraphQLGenerator {

    private final ITypeGenerator generator = DefaultBuildContext.reflectionContext;

    @Override
    public GraphQLSchema generateSchema(RecordSchema schema) {
        return GraphQLSchema.newSchema()
                .query(createRootObject(Lists.newArrayList(createRootField(schema, null))))
                .build();
    }

    @Override
    public GraphQLSchema generateSchema(List<RecordSchema> recordSchemas) {
        return GraphQLSchema.newSchema()
                .query(createRootObject(createTypes(recordSchemas, null)))
                .build();
    }

    @Override
    public GraphQLSchema generateSchemaWFetcher(List<RecordSchema> recordSchemas, DataFetcher<Collection<?>> dataFetcher) {
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
        GraphQLObjectType rootType = GraphQLObjectType.newObject()
                .fields(createFields(schema.getFields()))
                .name(schema.getName().replaceAll("\\.", "_"))
                .build();

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
        Preconditions.checkArgument(!fields.isEmpty(), "There must exist some fields!");

        for (Field field : fields) {
            GraphQLOutputType outputType;
            if (Type.RECORD == field.getSchema().getType()) {
                outputType = GraphQLObjectType.newObject()
                        .fields(createFields(((RecordSchema) field.getSchema()).getFields()))
                        .name(field.getName())
                        .build();
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
}
