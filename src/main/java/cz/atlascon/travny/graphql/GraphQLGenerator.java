package cz.atlascon.travny.graphql;

import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.GraphQLSchema;

/**
 * Created by tomas on 6.6.17.
 */
public interface GraphQLGenerator {

    default GraphQLSchema generateSchema(Record item){
        return generateSchema(item.getSchema());
    }

    GraphQLSchema generateSchema(RecordSchema schema);
}
