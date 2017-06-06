package cz.atlascon.travny.graphql;

import cz.atlascon.travny.records.CustomRecord;
import cz.atlascon.travny.records.Record;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.schemas.Schema;

/**
 * Created by tomas on 6.6.17.
 */
public class DummyClass implements CustomRecord{

    public static final String INT_NUMBER_NAME = "intNumber";
    public static final String STRING_NAME = "string";
    public static final String LONG_NUMBER_NAME = "longNumber";

    private int intNumber;
    private String string;
    private long longNumber;

    public DummyClass(int intNumber, String string, long longNumber) {
        this.intNumber = intNumber;
        this.string = string;
        this.longNumber = longNumber;
    }

    public DummyClass() {
    }

    public int getIntNumber() {
        return intNumber;
    }

    public void setIntNumber(int intNumber) {
        this.intNumber = intNumber;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public long getLongNumber() {
        return longNumber;
    }

    public void setLongNumber(long longNumber) {
        this.longNumber = longNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyClass that = (DummyClass) o;

        if (intNumber != that.intNumber) return false;
        if (longNumber != that.longNumber) return false;
        return string != null ? string.equals(that.string) : that.string == null;
    }

    @Override
    public int hashCode() {
        int result = intNumber;
        result = 31 * result + (string != null ? string.hashCode() : 0);
        result = 31 * result + (int) (longNumber ^ (longNumber >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DummyClass{" +
                "intNumber=" + intNumber +
                ", string='" + string + '\'' +
                ", longNumber=" + longNumber +
                '}';
    }

    public RecordSchema getSchema() {
        return RecordSchema.newBuilder(this.getClass().getName())
                .addField(Schema.INT, "intNumber")
                .addField(Schema.STRING, "string")
                .addField(Schema.LONG, "longNumber")
                .build();
    }

    public Object get(int i) {
        return i;
    }

    public void set(int i, Object o) {

    }

    public boolean hasValue(int i) {
        return false;
    }
}
