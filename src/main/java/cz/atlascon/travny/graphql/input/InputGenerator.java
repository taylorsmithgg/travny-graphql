package cz.atlascon.travny.graphql.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.atlascon.travny.graphql.convertor.ClassConvertor;
import cz.atlascon.travny.graphql.convertor.ClassConvertorImpl;
import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.schema.*;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static graphql.Scalars.GraphQLString;

/**
 * Created by tomas on 25.6.17.
 */
public class InputGenerator {
    private final ConcurrentMap<String, GraphQLInputObjectType> inputMap = Maps.newConcurrentMap();
    private final ClassConvertor convertor = new ClassConvertorImpl();

    public List<GraphQLArgument> createRootField(RecordSchema idSchema) {
        List<GraphQLArgument> arguments = Lists.newArrayList();
        if (idSchema == null) {
            return arguments;
        }
        for (Field field : idSchema.getFields()) {
            GraphQLInputType inputType = convertor.getInputType(field.getSchema());
            String name = inputType.getName() + "_arg";
            if (inputType instanceof GraphQLEnumType) {
                inputMap.putIfAbsent(name, GraphQLInputObjectType.newInputObject()
                        .name(name)
                        .description(((GraphQLEnumType) inputType).getDescription())
                        .fields(Lists.newArrayList(GraphQLInputObjectField
                                .newInputObjectField()
                                .name("enum_" + field.getName())
                                .description("String for enum")
                                .type(GraphQLString)
                                .build()))
                        .build());
                inputType = inputMap.get(name);
            } else if (inputType instanceof GraphQLScalarType == false) {
                inputMap.putIfAbsent(name, GraphQLInputObjectType.newInputObject()
                        .name(name)
                        .description(((GraphQLInputObjectType) inputType).getDescription())
                        .fields(((GraphQLInputObjectType) inputType).getFields())
                        .build());

                inputType = inputMap.get(name);
            }
            Preconditions.checkNotNull(inputType);

            GraphQLArgument build = GraphQLArgument.newArgument()
                    .name(field.getName())
                    .type(inputType)
                    .description("This is input argument for field name: " + field.getName())
                    .build();
            arguments.add(build);
        }

        return arguments;
    }
}
