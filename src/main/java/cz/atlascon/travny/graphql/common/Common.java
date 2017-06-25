package cz.atlascon.travny.graphql.common;

/**
 * Created by tomas on 13.6.17.
 */
public class Common {
    public static String convertToName(String name) {
        if (name.contains("id<")) {
            String s = name.replaceAll("id<|>", "");
            name = s + "_ID";
        }
        return name.replaceAll("\\.|<|>", "_");
    }
}
