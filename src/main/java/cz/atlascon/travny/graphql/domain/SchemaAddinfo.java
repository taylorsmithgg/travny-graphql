package cz.atlascon.travny.graphql.domain;

import com.google.common.base.Preconditions;
import cz.atlascon.travny.graphql.common.Common;
import cz.atlascon.travny.schemas.RecordSchema;

public class SchemaAddinfo {
    private final RecordSchema recordSchema;
    private final String fieldName;

    public SchemaAddinfo(RecordSchema recordSchema, String fieldName) {
        Preconditions.checkNotNull(recordSchema);
        Preconditions.checkNotNull(fieldName);
        this.recordSchema = recordSchema;
        this.fieldName = fieldName;
    }

    public SchemaAddinfo(RecordSchema recordSchema) {
        Preconditions.checkNotNull(recordSchema);
        this.recordSchema = recordSchema;
        this.fieldName = Common.createRootFieldName(recordSchema);
    }

    public RecordSchema getRecordSchema() {
        return recordSchema;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaAddinfo schemaAddinfo = (SchemaAddinfo) o;

        if (recordSchema != null ? !recordSchema.equals(schemaAddinfo.recordSchema) : schemaAddinfo.recordSchema != null)
            return false;
        return fieldName != null ? fieldName.equals(schemaAddinfo.fieldName) : schemaAddinfo.fieldName == null;
    }

    @Override
    public int hashCode() {
        int result = recordSchema != null ? recordSchema.hashCode() : 0;
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SchemaAddinfo{" +
                "recordSchema=" + recordSchema +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
