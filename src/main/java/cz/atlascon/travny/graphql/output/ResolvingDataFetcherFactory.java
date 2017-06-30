package cz.atlascon.travny.graphql.output;

import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.DataFetcher;

/**
 * Created by trehak on 29.6.17.
 */
public interface ResolvingDataFetcherFactory {

    DataFetcher create(RecordSchema parent, Field field);

}
