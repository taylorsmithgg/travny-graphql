package cz.atlascon.travny.graphql;

import cz.atlascon.travny.schemas.Field;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.shaded.com.google.common.collect.Lists;
import cz.atlascon.travny.types.Type;
import graphql.java.generator.BuildContext;
import graphql.java.generator.DefaultBuildContext;
import graphql.java.generator.type.ITypeGenerator;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.List;

/**
 * Created by tomas on 6.6.17.
 */
public class GraphQLGeneratorImpl implements GraphQLGenerator {

    private final ITypeGenerator generator = DefaultBuildContext.reflectionContext;

    public GraphQLSchema generateSchema(RecordSchema schema) {
        return GraphQLSchema.newSchema()
                .query(createType(schema))
                .build();
    }

    private GraphQLObjectType createType(RecordSchema schema) {
        return GraphQLObjectType.newObject()
                .fields(createFields(schema.getFields()))
                .name(schema.getName().replaceAll("\\.","_"))
                .description("Generated graphQL schema for class: " + schema.getName())
                .build();
    }

    private List<GraphQLFieldDefinition> createFields(List<Field> fields) {
        List<GraphQLFieldDefinition> qlFields = Lists.newArrayList();
        for (Field field : fields) {
            GraphQLFieldDefinition build = GraphQLFieldDefinition.newFieldDefinition()
                    .type(generator.getOutputType(field.getSchema().getType().getJavaClass()))
                    //TODO implement default values .staticValue()
                    .name(field.getName())
                    .build();
            qlFields.add(build);
        }

        return qlFields;
    }
}
