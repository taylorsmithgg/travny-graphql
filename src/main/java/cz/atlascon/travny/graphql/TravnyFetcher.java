package cz.atlascon.travny.graphql;

import graphql.schema.DataFetcher;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public abstract class TravnyFetcher<T> implements DataFetcher<Collection<T>> {

    Class<T> getFetchedClass(){
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
