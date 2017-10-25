package cz.atlascon.travny.graphql.domain;

import com.google.common.base.Preconditions;
import cz.atlascon.travny.graphql.common.Common;
import cz.atlascon.travny.schemas.RecordSchema;

public class SchemaAddinfo {
    private final RecordSchema recordSchema;
    private final RecordSchema idSchema;
    private final String fieldName;

    public SchemaAddinfo(RecordSchema recordSchema, RecordSchema idSchema, String fieldName) {
        Preconditions.checkNotNull(recordSchema);
        Preconditions.checkNotNull(fieldName);
        this.recordSchema = recordSchema;
        this.idSchema = idSchema;
        this.fieldName = fieldName;
    }

    public SchemaAddinfo(RecordSchema recordSchema, String fieldName) {
        this(recordSchema, recordSchema.getIdSchema(), fieldName);
    }

    public SchemaAddinfo(RecordSchema recordSchema) {
        this(recordSchema, Common.convertToName(recordSchema.getName()));
    }

    public RecordSchema getRecordSchema() {
        return recordSchema;
    }

    public String getFieldName() {
        return fieldName;
    }

    public RecordSchema getIdSchema() {
        return idSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaAddinfo that = (SchemaAddinfo) o;

        if (recordSchema != null ? !recordSchema.equals(that.recordSchema) : that.recordSchema != null) return false;
        if (idSchema != null ? !idSchema.equals(that.idSchema) : that.idSchema != null)
            return false;
        return fieldName != null ? fieldName.equals(that.fieldName) : that.fieldName == null;
    }

    @Override
    public int hashCode() {
        int result = recordSchema != null ? recordSchema.hashCode() : 0;
        result = 31 * result + (idSchema != null ? idSchema.hashCode() : 0);
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SchemaAddinfo{" +
                "recordSchema=" + recordSchema +
                ", idSchema=" + idSchema +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
