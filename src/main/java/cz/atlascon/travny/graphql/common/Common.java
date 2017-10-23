package cz.atlascon.travny.graphql.common;

import com.google.common.base.Splitter;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;

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

    public static String getName(GraphQLOutputType outputType){
        if(outputType instanceof GraphQLList){
            return ((GraphQLList) outputType).getWrappedType().getName();
        }
        return outputType.getName();
    }

}
