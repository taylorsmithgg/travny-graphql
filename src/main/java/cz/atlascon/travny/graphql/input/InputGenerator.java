package cz.atlascon.travny.graphql.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.schemas.*;
import cz.atlascon.travny.types.Type;
import graphql.schema.*;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static cz.atlascon.travny.graphql.common.Common.convertToName;

/**
 * Created by tomas on 25.6.17.
 */
public class InputGenerator {
    private final ConcurrentMap<String, GraphQLInputObjectType> inputMap = Maps.newConcurrentMap();
    private final ClassConvertor convertor;

    public InputGenerator(ClassConvertor classConvertor) {
        this.convertor = classConvertor;
    }

    private GraphQLInputType createType(Schema schema) {
        Preconditions.checkNotNull(schema, "Got null schema");
        if (Type.ENUM == schema.getType()) {
            return convertor.getInputType(schema);
        } else if (Type.LIST == schema.getType()) {
            Schema valueSchema = ((ListSchema) schema).getValueSchema();
            return GraphQLList.list(createType(valueSchema));
        } else {
            return convertor.getInputType(schema);
        }
    }

    public List<GraphQLArgument> createRootField(RecordSchema idSchema) {
        List<GraphQLArgument> arguments = Lists.newArrayList();
        if (idSchema == null) {
            return arguments;
        }
        for (Field field : idSchema.getFields()) {
            if (!field.isRemoved()) {
                GraphQLInputType toArgument = createType(field.getSchema());

                GraphQLArgument argument = GraphQLArgument.newArgument()
                        .name(field.getName())
                        .type(toArgument)
                        .description("This is input argument for field name: " + field.getName())
                        .build();
                arguments.add(argument);
            }
        }

        return arguments;
    }
}
