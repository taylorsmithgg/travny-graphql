package cz.atlascon.travny.graphql;

import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.schemas.Schema;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tomas on 6.6.17.
 */
public class TestGraphQLGeneratorImpl {
    private final GraphQLGenerator generator = new GraphQLGeneratorImpl();

    private static final String INT_NUMBER_NAME = "intNumber";
    private static final String STRING_NAME = "string";
    private static final String LONG_NUMBER_NAME = "longNumber";
    private static final String BYTES_NAME = "bytes";
    private static final String BOOLEAN_NAME = "aboolean";
    private static final String FLOAT_NAME = "floatNumber";

    @Test
    public void shouldProduceSomething() {
        RecordSchema recordSchema = createDummySchema();
        GraphQLSchema graphQLSchema = generator.generateSchema(recordSchema);
        Assert.assertNotNull(graphQLSchema);
    }

    private RecordSchema createDummySchema(){
        return RecordSchema.newBuilder(this.getClass().getName())
                .addField(Schema.INT, INT_NUMBER_NAME)
                .addField(Schema.STRING, STRING_NAME)
                .addField(Schema.LONG, LONG_NUMBER_NAME)
                .addField(Schema.BOOLEAN, BOOLEAN_NAME)
                .addField(Schema.FLOAT, FLOAT_NAME)
                //TODO proverit
//                .addField(Schema.BYTES, BYTES_NAME)
                .build();
    }

    @Test
    public void shouldProduceValid() {
        RecordSchema recordSchema = createDummySchema();
        GraphQLSchema graphQLSchema = generator.generateSchema(recordSchema);

        GraphQLObjectType queryType = graphQLSchema.getQueryType();
        GraphQLFieldDefinition intDefinition = queryType.getFieldDefinition(INT_NUMBER_NAME);
        Assert.assertTrue(intDefinition.getType() instanceof GraphQLScalarType);
        Assert.assertEquals("Int", intDefinition.getType().getName());

        GraphQLFieldDefinition stringDefinition = queryType.getFieldDefinition(STRING_NAME);
        Assert.assertTrue(stringDefinition.getType() instanceof GraphQLScalarType);
        Assert.assertEquals("String", stringDefinition.getType().getName());

        GraphQLFieldDefinition longDefinition = queryType.getFieldDefinition(LONG_NUMBER_NAME);
        Assert.assertTrue(longDefinition.getType() instanceof GraphQLScalarType);
        Assert.assertEquals("Long", longDefinition.getType().getName());

        GraphQLFieldDefinition booleanDefinition = queryType.getFieldDefinition(BOOLEAN_NAME);
        Assert.assertTrue(booleanDefinition.getType() instanceof GraphQLScalarType);
        Assert.assertEquals("Boolean", booleanDefinition.getType().getName());

        GraphQLFieldDefinition floatDefinition = queryType.getFieldDefinition(FLOAT_NAME);
        Assert.assertTrue(floatDefinition.getType() instanceof GraphQLScalarType);
        Assert.assertEquals("Float", floatDefinition.getType().getName());

        GraphQLFieldDefinition bytesDefinition = queryType.getFieldDefinition(BYTES_NAME);
        Assert.assertTrue(bytesDefinition.getType() instanceof GraphQLScalarType);
        Assert.assertEquals("Bytes", bytesDefinition.getType().getName());
    }

    @Test
    public void shouldFail() {

    }

    @Test
    public void shouldProduceValidEmptyClassSchema() {

    }

    @Test
    public void shouldProduceValidMultiClassSchema() {

    }

    @Test
    public void shouldProduceValidExtendedSchema() {

    }

}
