package cz.atlascon.travny.graphql.input;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.graphql.convertor.ClassConvertorImpl;
import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.*;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by tomas on 25.6.17.
 */
public class InputGenerator {
    private final ConcurrentMap<String, GraphQLInputObjectType> inputMap = Maps.newConcurrentMap();
    private final ClassConvertor convertor;

    public InputGenerator(ClassConvertor classConvertor) {
        this.convertor = classConvertor;
    }

    private GraphQLInputType createType(GraphQLInputType inputType, String name) {
        if (inputType instanceof GraphQLScalarType) {
            return inputType;
        } else if (inputType instanceof GraphQLEnumType) {
            return inputType;
        } else {
            inputMap.putIfAbsent(name, GraphQLInputObjectType.newInputObject()
                    .name(name)
                    .description(((GraphQLInputObjectType) inputType).getDescription())
                    .fields(((GraphQLInputObjectType) inputType).getFields())
                    .build());
        }
        return inputMap.get(name);
    }

    public List<GraphQLArgument> createRootField(RecordSchema idSchema) {
        List<GraphQLArgument> arguments = Lists.newArrayList();
        if (idSchema == null) {
            return arguments;
        }
        for (Field field : idSchema.getFields()) {
            GraphQLInputType createdType = convertor.getInputType(field.getSchema());
            GraphQLInputType toArgument;
            String name = createdType.getName() + "_arg";
            toArgument = createType(createdType, name);

            GraphQLArgument argument = GraphQLArgument.newArgument()
                    .name(field.getName())
                    .type(toArgument)
                    .description("This is input argument for field name: " + field.getName())
                    .build();
            arguments.add(argument);
        }

        return arguments;
    }
}
