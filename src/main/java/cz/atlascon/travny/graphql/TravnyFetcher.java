package cz.atlascon.travny.graphql;

import graphql.schema.DataFetcher;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * It needs to be abstract class beacse when it is interface, method 'getFetchedClass' doesnt return concrete type of <T>
 * @param <T>
 */
public abstract class TravnyFetcher<T> implements DataFetcher<Collection<T>> {

    Class<T> getFetchedClass(){
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
