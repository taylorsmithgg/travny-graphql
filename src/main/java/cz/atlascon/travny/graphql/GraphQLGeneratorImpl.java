package cz.atlascon.travny.graphql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.input.InputGenerator;
import cz.atlascon.travny.graphql.output.OutputGenerator;
import cz.atlascon.travny.schemas.*;
import cz.atlascon.travny.types.Type;
import graphql.schema.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static cz.atlascon.travny.graphql.common.Common.convertToName;
import static graphql.Scalars.GraphQLString;

/**
 * Created by tomas on 6.6.17.
 */
public class GraphQLGeneratorImpl implements GraphQLGenerator {

    private final InputGenerator inputGenerator = new InputGenerator();
    private final OutputGenerator outputGenerator = new OutputGenerator();

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
        // get name of class without packages
        List<String> strings = Splitter.on('.').splitToList(schema.getName());
        String fieldName = strings.get(strings.size() - 1).toLowerCase();

        return GraphQLFieldDefinition.newFieldDefinition()
                .type(GraphQLList.list(outputObjectPerField(schema)))
                .argument(argumentsPerRootField(schema.getIdSchema()))
                .name(fieldName)
                .dataFetcher(dataFetcher)
                .description("Generated graphQL schema for class: " + schema.getName())
                .build();
    }

    private GraphQLOutputType outputObjectPerField(RecordSchema schema) {
        return outputGenerator.createRootField(schema);
    }

    private List<GraphQLArgument> argumentsPerRootField(RecordSchema idSchema) {
        return inputGenerator.createRootField(idSchema);
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
}
