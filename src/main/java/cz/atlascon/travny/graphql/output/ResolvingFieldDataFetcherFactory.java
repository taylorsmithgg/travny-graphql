package cz.atlascon.travny.graphql.output;

import cz.atlascon.travny.parser.SchemaNameUtils;
import cz.atlascon.travny.records.IdRecord;
import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.schemas.Schemas;
import graphql.schema.DataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Created by trehak on 1.7.17.
 */
public class ResolvingFieldDataFetcherFactory implements TravnyFieldDataFetcherFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvingFieldDataFetcherFactory.class);
    private final Function<IdRecord, Record> retrieveFc;

    public ResolvingFieldDataFetcherFactory(Function<IdRecord, Record> retrieveFc) {
        this.retrieveFc = retrieveFc;
    }

    @Override
    public DataFetcher create(Field field) {
        return environment -> {
            Object object = environment.getSource();
            if (object == null) return null;
            Record rec = (Record) object;
            Field requestedField = rec.getSchema().getField(field.getName());
            // requested field not found in object, object is ID and requested field is present in actual object
            // --> load object by ID
            if (requestedField == null && SchemaNameUtils.isIdSchema(rec.getSchema().getName())) {
                String recName = SchemaNameUtils.getRecordForId(rec.getSchema().getName());
                try {
                    Class<?> recClass = Class.forName(recName);
                    RecordSchema recSchema = (RecordSchema) Schemas.getSchema(recClass);
                    Field recField = recSchema.getField(field.getOrd());
                    if (recField != null) {
                        Record fullRecord = retrieveFc.apply((IdRecord) rec);
                        return fullRecord.get(field.getOrd());
                    } else {
                        LOGGER.warn("Field " + field.getName() + " not found in object schema " + recName);
                        return null;
                    }
                } catch (Exception e) {
                    LOGGER.error("Class " + recName + " not found", e);
                    return null;
                }
            } else {
                return rec.get(field.getName());
            }
        };
    }
}
