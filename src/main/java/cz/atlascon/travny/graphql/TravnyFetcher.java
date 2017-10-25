package cz.atlascon.travny.graphql;

import com.google.common.collect.Lists;
import cz.atlascon.travny.data.BinaryReader;
import cz.atlascon.travny.data.BinaryWriter;
import cz.atlascon.travny.records.CustomRecord;
import cz.atlascon.travny.records.Record;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

/**
 * It needs to be abstract class beacse when it is interface, method 'getFetchedClass' doesnt return concrete type of <T>
 * Collection is enforced because we add rootField always as list
 *
 * @param <T>
 */
public abstract class TravnyFetcher<T extends CustomRecord> implements DataFetcher<Collection<Record>> {

    Class<T> getFetchedClass() {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public T getData(DataFetchingEnvironment environment) throws Exception {
        throw new NotImplementedException();
    }

    public Collection<T> getCollectionData(DataFetchingEnvironment environment) throws Exception {
        List<T> dataList = Lists.newArrayList();
        dataList.add(getData(environment));
        return dataList;
    }

    @Override
    public final Collection<Record> get(DataFetchingEnvironment environment) {
        try {
            Collection<T> collectionData = getCollectionData(environment);
            if (collectionData == null || collectionData.isEmpty()) {
                return Lists.newArrayList();
            }

            List<Record> records = Lists.newArrayList();
            for (T data : collectionData) {
                byte[] bytes = BinaryWriter.toBytes(data);
                records.add(BinaryReader.fromBytes(bytes, data.getSchema()));
            }
            return records;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
