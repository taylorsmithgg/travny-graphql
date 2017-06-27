package cz.atlascon.travny.graphql;

import com.google.common.collect.Lists;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;

import java.util.Collection;
import java.util.List;

/**
 * Created by tomas on 6.6.17.
 */
public interface GraphQLGenerator {

    /**
     * To each root field add dataFetcher and use }
     * you should overide this
     *
     * @param recordSchemas
     * @param dataFetcher
     * @return
     */
    GraphQLSchema generateSchema(List<RecordSchema> recordSchemas, DataFetcher<Collection<?>> dataFetcher);

    default GraphQLSchema generateSchema(List<RecordSchema> recordSchemas) {
        return generateSchema(recordSchemas, null);
    }

    default GraphQLSchema generateSchema(RecordSchema recordSchemas) {
        return generateSchema(Lists.newArrayList(recordSchemas), null);
    }

}
