package cz.atlascon.travny.graphql;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cz.atlascon.travny.graphql.common.Common;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.graphql.convertor.ClassConvertorImpl;
import cz.atlascon.travny.graphql.domain.SchemaAddinfo;
import cz.atlascon.travny.graphql.input.InputGenerator;
import cz.atlascon.travny.graphql.output.OutputGenerator;
import cz.atlascon.travny.graphql.output.TravnyFieldDataFetcherFactory;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by tomas on 6.6.17.
 */
public class GraphQLGeneratorImpl implements GraphQLGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLGeneratorImpl.class);
    private final InputGenerator inputGenerator;
    private final OutputGenerator outputGenerator;
    private DataFetcher dataFetcher;
    private List<TravnyFetcher> dataFetchers;

    public GraphQLGeneratorImpl(DataFetcher defaultFetcher, TravnyFieldDataFetcherFactory dataFetcherFactory, List<TravnyFetcher> fetchers) {
        Preconditions.checkNotNull(defaultFetcher);
        this.dataFetcher = defaultFetcher;
        this.dataFetchers = fetchers;
        ClassConvertor classConvertor = new ClassConvertorImpl();
        inputGenerator = new InputGenerator(classConvertor);
        outputGenerator = new OutputGenerator(classConvertor, dataFetcherFactory);
    }

    @Override
    public GraphQLSchema generateSchema(List<SchemaAddinfo> addinfoList,
                                        Function<String, RecordSchema> schemaSupplier) {
        Preconditions.checkNotNull(addinfoList, "Not Valid use addinfoList cannot be null!");
        Preconditions.checkArgument(!addinfoList.isEmpty(), "Not valid Use, addinfoList cannot be empty!");
        List<RecordSchema> schemas = addinfoList.stream().map(schemaAddinfo -> schemaAddinfo.getRecordSchema()).collect(Collectors.toList());

        outputGenerator.setSchemaSupplier(createSupplier(schemaSupplier, schemas));
        LOGGER.info("Creating types schemas");
        List<GraphQLFieldDefinition> types = createRootFields(addinfoList, dataFetcher, dataFetchers);
        LOGGER.info("Types schemas created");
        LOGGER.info("Creating root schema");
        GraphQLSchema root = GraphQLSchema.newSchema()
                .query(createRootObject(types))
                .build(Sets.newHashSet(outputGenerator.getTypeMap().values()));
        LOGGER.info("Root schema created");
        return root;
    }

    private Function<String, RecordSchema> createSupplier(Function<String, RecordSchema> suppliedSupplier,
                                                          List<RecordSchema> recordSchemas) {
        Map<String, RecordSchema> m = Maps.newHashMap();
        recordSchemas.stream().forEach(rs -> m.put(rs.getName(), rs));
        return n -> {
            if (suppliedSupplier != null) {
                RecordSchema itm = suppliedSupplier.apply(n);
                if (itm != null) {
                    return itm;
                }
            }
            return m.get(n);
        };
    }

    private List<GraphQLFieldDefinition> createRootFields(List<SchemaAddinfo> recordSchemas, DataFetcher dataFetcher, List<TravnyFetcher> dataFetchers) {
        List<GraphQLFieldDefinition> fieldDefinitions = Lists.newArrayList();
        for (SchemaAddinfo addinfo : recordSchemas) {
            // get name of class without packages
            RecordSchema recordSchema = addinfo.getRecordSchema();
            fieldDefinitions.add(createRootField(recordSchema, chooseFetcher(dataFetcher, dataFetchers, recordSchema), addinfo.getFieldName()));
        }

        return fieldDefinitions;
    }

    private DataFetcher chooseFetcher(DataFetcher defaultFetcher, List<TravnyFetcher> dataFetchers, RecordSchema recordSchema) {
        if(dataFetchers == null){
            return defaultFetcher;
        }

        for (TravnyFetcher fetcher : dataFetchers) {
            String type = fetcher.getFetchedClass().getName();
            if(recordSchema.getName().equals(type)){
                return fetcher;
            }
        }

        return defaultFetcher;
    }

    private GraphQLFieldDefinition createRootField(RecordSchema schema, DataFetcher dataFetcher, String rootFieldName) {
        Preconditions.checkNotNull(schema, "schema cannot be null!");
        GraphQLOutputType outputType = outputGenerator.createRootField(schema);

        return GraphQLFieldDefinition.newFieldDefinition()
                .type(GraphQLList.list(outputType))
                .argument(argumentsPerRootField(schema.getIdSchema()))
                .name(rootFieldName)
                .dataFetcher(dataFetcher)
                .description("Generated graphQL schema for class: " + schema.getName())
                .build();
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
                .description("This is root Query object type. Which main purpose is to hold all Objects/Fields for which you can ask. Down format is (fieldName):(fieldType)")
                .build();
    }
}
