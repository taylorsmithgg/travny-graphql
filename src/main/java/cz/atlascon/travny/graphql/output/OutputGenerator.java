package cz.atlascon.travny.graphql.output;

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
public class OutputGenerator {
    private final ConcurrentMap<String, GraphQLOutputType> outputMap = Maps.newConcurrentMap();
    private final ClassConvertor convertor;

    public OutputGenerator(ClassConvertor classConvertor) {
        this.convertor = classConvertor;
    }

    public GraphQLOutputType createRootField(RecordSchema schema) {
        String rootName = convertToName(schema.getName());
        outputMap.putIfAbsent(rootName, GraphQLObjectType.newObject()
                .fields(createFields(schema.getFields()))
                .name(rootName)
                .build());
        return outputMap.get(rootName);
    }

    private GraphQLOutputType createType(List<GraphQLFieldDefinition> type, String name) {
        outputMap.putIfAbsent(name, GraphQLObjectType.newObject()
                .fields(type)
                .name(name)
                .build());
        return outputMap.get(name);
    }

    private List<GraphQLFieldDefinition> createFields(List<Field> fields) {
        List<GraphQLFieldDefinition> qlFields = Lists.newArrayList();
        Preconditions.checkNotNull(fields);
        Preconditions.checkArgument(!fields.isEmpty(), "There must exist some fields! Empty class is not allowed!");

        GraphQLOutputType outputType;
        for (Field field : fields) {
            if (Type.ENUM == field.getSchema().getType()) {
                String className = convertToName(((EnumSchema) field.getSchema()).getName());
                GraphQLOutputType qlOutputType = convertor.getOutputType(field.getSchema());
                outputMap.putIfAbsent(className, qlOutputType);
                outputType = outputMap.get(className);
            } else if (Type.RECORD == field.getSchema().getType()) {
                String className = convertToName(((RecordSchema) field.getSchema()).getName());
                outputType = createType(createFields(((RecordSchema) field.getSchema()).getFields()), className);
            } else if (field.getSchema() instanceof ListSchema) {
                GraphQLType graphQLType;
                Schema listSchema = ((ListSchema) field.getSchema()).getValueSchema();
                if (Type.RECORD == listSchema.getType()) {
                    String className = convertToName(((RecordSchema) listSchema).getName());
                    outputMap.putIfAbsent(className, GraphQLObjectType.newObject()
                            .fields(createFields((((RecordSchema) listSchema).getFields())))
                            .name(className)
                            .build());
                    graphQLType = outputMap.get(className);

                } else if (Type.ENUM == ((ListSchema) field.getSchema()).getValueSchema().getType()) {
                    String className = convertToName(((EnumSchema) listSchema).getName());
                    outputMap.putIfAbsent(className, convertor.getOutputType(((ListSchema) field.getSchema()).getValueSchema()));
                    graphQLType = outputMap.get(className);
                } else {
                    graphQLType = convertor.getOutputType(((ListSchema) field.getSchema()).getValueSchema());
                }
                outputType = GraphQLList.list(graphQLType);
//            } else if (field.getSchema() instanceof MapSchema) {
//                // TODO prepared for map implementation
//                GraphQLOutputType outputType1 = convertor.getOutputType(field.getSchema());
//                outputMap.putIfAbsent(convertToName(field.getName()), outputType1);
//                return outputMap.get()
            } else {
                outputType = convertor.getOutputType(field.getSchema());
                Preconditions.checkArgument(outputType instanceof GraphQLScalarType);
            }

            GraphQLFieldDefinition build = GraphQLFieldDefinition.newFieldDefinition()
                    .type(outputType)
                    //TODO implement default values .staticValue()
                    .name(field.getName())
                    .description("This is field for: " + field.getSchema().getType().getJavaClass().getName())
                    .build();
            qlFields.add(build);
        }

        return qlFields;
    }
}
