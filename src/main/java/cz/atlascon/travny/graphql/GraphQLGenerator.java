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
                                 Function<String, RecordSchema> schemaSupplier,
                                 boolean withRootFieldPredicate);


    default GraphQLSchema generateSchema(List<RecordSchema> recordSchemas, boolean withRootFieldPredicate) {
        List<SchemaAddinfo> collect = recordSchemas.stream().map(SchemaAddinfo::new).collect(Collectors.toList());
        return generateSchema(collect, null, withRootFieldPredicate);
    }

    default GraphQLSchema generateSchema(RecordSchema recordSchema, boolean withRootFieldPredicate) {
        return generateSchemaWInfo(new SchemaAddinfo(recordSchema), withRootFieldPredicate);
    }

    default GraphQLSchema generateSchemaWInfo(SchemaAddinfo schemaAddinfo, boolean withRootFieldPredicate) {
        return generateSchema(Lists.newArrayList(schemaAddinfo), null, withRootFieldPredicate);
    }

}
