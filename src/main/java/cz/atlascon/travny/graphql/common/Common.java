package cz.atlascon.travny.graphql.common;

import com.google.common.base.Splitter;
import cz.atlascon.travny.schemas.RecordSchema;

import java.util.List;

/**
 * Created by tomas on 13.6.17.
 */
public class Common {

    private Common(){

    }

    public static String convertToName(String name) {
        if (name.contains("id<")) {
            String s = name.replaceAll("id<|>", "");
            name = s + "_ID";
        }
        return name.replaceAll("\\.|<|>", "_");
    }

    public static String createRootFieldName(RecordSchema recordSchema){
        List<String> strings = Splitter.on('.').splitToList(recordSchema.getName());
        return strings.get(strings.size() - 1).toLowerCase();
    }
}
