package cz.atlascon.travny.graphql;

import com.sun.istack.internal.NotNull;
import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.shaded.com.google.common.collect.Lists;
import graphql.schema.GraphQLSchema;

import java.util.List;

/**
 * Created by tomas on 6.6.17.
 */
public interface GraphQLGenerator {

    default GraphQLSchema generateSchema(@NotNull Record item){
        return generateSchema(item.getSchema());
    }

    GraphQLSchema generateSchema(@NotNull RecordSchema schema);

    GraphQLSchema generateSchema(@NotNull List<RecordSchema> recordSchemas);

    default GraphQLSchema generateSchema(@NotNull RecordSchema... recordSchemas){
        return generateSchema(Lists.newArrayList(recordSchemas));
    }
}
