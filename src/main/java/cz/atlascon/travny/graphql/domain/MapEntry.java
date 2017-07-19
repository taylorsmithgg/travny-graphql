package cz.atlascon.travny.graphql.domain;

/**
 * Created by tomas on 18.7.17.
 */
public final class MapEntry {
    private final Object key;
    private final Object val;

    public MapEntry(Object key, Object val) {
        this.key = key;
        this.val = val;
    }

    public Object getKey() {
        return key;
    }

    public Object getVal() {
        return val;
    }
}
