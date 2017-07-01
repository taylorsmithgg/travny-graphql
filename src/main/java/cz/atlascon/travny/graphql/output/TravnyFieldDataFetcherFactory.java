package cz.atlascon.travny.graphql.output;

import cz.atlascon.travny.schemas.Field;
import graphql.schema.DataFetcher;

/**
 * Create DataFetcher for given field and Travny source object
 */
public interface TravnyFieldDataFetcherFactory {

    DataFetcher create(Field field);

}
