package cz.atlascon.travny.graphql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.graphql.convertor.ClassConvertorImpl;
import cz.atlascon.travny.graphql.input.InputGenerator;
import cz.atlascon.travny.graphql.output.OutputGenerator;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by tomas on 6.6.17.
 */
public class GraphQLGeneratorImpl implements GraphQLGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLGeneratorImpl.class);
    private final InputGenerator inputGenerator;
    private final OutputGenerator outputGenerator;

    public GraphQLGeneratorImpl() {
        ClassConvertor classConvertor = new ClassConvertorImpl();
        inputGenerator = new InputGenerator(classConvertor);
        outputGenerator = new OutputGenerator(classConvertor);
    }

    @Override
    public GraphQLSchema generateSchema(List<RecordSchema> recordSchemas, DataFetcher<Collection<?>> dataFetcher) {
        Preconditions.checkNotNull(recordSchemas, "Not Valid use recordSchemas cannot be null!");
        Preconditions.checkArgument(!recordSchemas.isEmpty(), "Not valid Use, recordSchemas cannot be empty!");
        outputGenerator.setSchemaSupplier(createSupplier(recordSchemas));
        LOGGER.info("Creating types schemas");
        List<GraphQLFieldDefinition> types = createTypes(recordSchemas, dataFetcher);
        LOGGER.info("Types schemas created");
        LOGGER.info("Creating root schema");
        GraphQLSchema root = GraphQLSchema.newSchema()
                .query(createRootObject(types))
                .build(Sets.newHashSet(outputGenerator.getTypeMap().values()));
        LOGGER.info("Root schema created");
        return root;
    }

    private Function<String, RecordSchema> createSupplier(List<RecordSchema> recordSchemas) {
        Map<String, RecordSchema> m = Maps.newHashMap();
        recordSchemas.stream().forEach(rs -> m.put(rs.getName(), rs));
        return n -> m.get(n);
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
