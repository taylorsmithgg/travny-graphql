package cz.atlascon.travny.graphql;

import com.google.common.collect.Lists;
import cz.atlascon.travny.graphql.common.Common;
import cz.atlascon.travny.graphql.domain.SchemaAddinfo;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.GraphQLSchema;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by tomas on 6.6.17.
 */
public interface GraphQLGenerator {

    /**
     * To each root field add dataFetcher and use }
     * you should overide this
     *
     * @param recordSchemas
     * @return
     */
    GraphQLSchema generateSchema(List<SchemaAddinfo> recordSchemas,
                                 Function<String, RecordSchema> schemaSupplier);


    default GraphQLSchema generateSchema(List<RecordSchema> recordSchemas) {
        List<SchemaAddinfo> collect = recordSchemas.stream().map(recordSchema ->
                new SchemaAddinfo(recordSchema, Common.createRootFieldName(recordSchema))).collect(Collectors.toList());
        return generateSchema(collect, null);
    }

    default GraphQLSchema generateSchema(RecordSchema recordSchema) {
        return generateSchema(Lists.newArrayList(
                new SchemaAddinfo(recordSchema, Common.createRootFieldName(recordSchema))),
                null);
    }

    default GraphQLSchema generateSchemaWInfo(SchemaAddinfo schemaAddinfo) {
        return generateSchema(Lists.newArrayList(schemaAddinfo), null);
    }

}
