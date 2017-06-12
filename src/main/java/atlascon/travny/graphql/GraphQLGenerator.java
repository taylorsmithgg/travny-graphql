package atlascon.travny.graphql;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.shaded.com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;

import java.util.Collection;
import java.util.List;

/**
 * Created by tomas on 6.6.17.
 */
public interface GraphQLGenerator {

    default GraphQLSchema generateSchema(Record item) {
        return generateSchema(item.getSchema());
    }

    default GraphQLSchema generateSchema(RecordSchema schema){
        return generateSchemaWFetcher(Lists.newArrayList(schema), null);
    }

    default GraphQLSchema generateSchema(List<RecordSchema> recordSchemas){
        return generateSchemaWFetcher(recordSchemas, null);
    }

    default GraphQLSchema generateSchema(RecordSchema... recordSchemas) {
        return generateSchema(Lists.newArrayList(recordSchemas));
    }

    /**
     * To each root field add dataFetcher and use {@link #generateSchema(List)}
     * you should overide this
     * @param recordSchemas
     * @param dataFetcher
     * @return
     */
    GraphQLSchema generateSchemaWFetcher(@NotNull List<RecordSchema> recordSchemas, @Nullable DataFetcher<Collection<?>> dataFetcher);
}
