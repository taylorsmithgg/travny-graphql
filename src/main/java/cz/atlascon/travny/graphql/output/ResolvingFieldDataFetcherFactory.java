package cz.atlascon.travny.graphql.output;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import cz.atlascon.travny.graphql.domain.MapEntry;
import cz.atlascon.travny.parser.SchemaNameUtils;
import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.types.BytesArray;
import cz.atlascon.travny.types.EnumConstant;
import cz.atlascon.travny.types.EnumConstantImpl;
import cz.atlascon.travny.types.Type;
import graphql.schema.DataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by trehak on 1.7.17.
 */
public class ResolvingFieldDataFetcherFactory implements TravnyFieldDataFetcherFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvingFieldDataFetcherFactory.class);
    private final Function<Record, Record> retrieveFc;

    public ResolvingFieldDataFetcherFactory(Function<Record, Record> retrieveFc) {
        this.retrieveFc = retrieveFc;
    }

    @Override
    public DataFetcher create(Field field) {
        return environment -> {
            Object object = environment.getSource();
            if (object == null) return null;
            Record rec;
            if (object instanceof List) {
                rec = (Record) ((List) object).get(0);
            } else {
                rec = (Record) object;
            }

            Field requestedField = rec.getSchema().getField(field.getName());
            // requested field not found in object, object is ID and requested field is present in actual object
            // --> load object by ID
            if (requestedField == null && SchemaNameUtils.isIdSchema(rec.getSchema().getName())) {
                String recName = SchemaNameUtils.getRecordForId(rec.getSchema().getName());
                try {
                    Record fullRecord = retrieveFc.apply(rec);
                    if (fullRecord == null) {
                        return null;
                    }
                    Object val = fullRecord.get(field.getOrd());
                    if (val == null) {
                        return null;
                    }
                    return convertIfMap(field, val);
                } catch (Exception e) {
                    LOGGER.error("Exception getting " + recName, e);
                    return null;
                }
            } else {
                Object val = rec.get(field.getName());

                if (val != null && val instanceof Enum) {
                    val = new EnumConstantImpl(((EnumConstant) val).getOrd(), ((EnumConstant) val).getConstant());
                } else if (val instanceof List && !((List) val).isEmpty() && ((List) val).get(0) instanceof Enum) {
                    val = ((List) val).stream()
                            .map(v -> new EnumConstantImpl(((EnumConstant) v).getOrd(), ((EnumConstant) v).getConstant()))
                            .collect(Collectors.toList());
                }

                return convertIfMap(field, val);
            }
        };
    }

    private Object convertIfMap(Field field, Object val) {
        if (val != null) {
            if (field.getSchema().getType() == Type.MAP) {
                Set<Map.Entry> set = ((Map) val).entrySet();
                return convertToEntry(set);
            } else if (field.getSchema().getType() == Type.BYTES) {
                return BaseEncoding.base16().lowerCase().encode(((BytesArray) val).get());
            }
        }
        return val;
    }

    private List<MapEntry> convertToEntry(Set<Map.Entry> set) {
        List<MapEntry> mapEntryList = Lists.newArrayList();

        for (Map.Entry entry : set) {
            if (entry.getValue() instanceof Map) {
                List c = convertToEntry(((Map) entry.getValue()).entrySet());
                mapEntryList.add(new MapEntry(entry.getKey(), c));
            } else {
                mapEntryList.add(new MapEntry(entry.getKey(), entry.getValue()));
            }
        }
        return mapEntryList;
    }

}
