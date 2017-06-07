package cz.atlascon.travny.graphql;

import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.shaded.com.google.common.collect.Lists;
import graphql.schema.GraphQLSchema;

import java.util.List;

/**
 * Created by tomas on 6.6.17.
 */
public interface GraphQLGenerator {

    default GraphQLSchema generateSchema(Record item){
        return generateSchema(item.getSchema());
    }

    GraphQLSchema generateSchema(RecordSchema schema);

    GraphQLSchema generateMultiSchema(List<RecordSchema> recordSchemas);

    default GraphQLSchema generateMultiSchema(RecordSchema... recordSchemas){
        return generateMultiSchema(Lists.newArrayList(recordSchemas));
    }
}
