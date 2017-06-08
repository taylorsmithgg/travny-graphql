package cz.atlascon.travny.graphql;

import cz.atlascon.travny.schemas.ListSchema;
import cz.atlascon.travny.schemas.RecordSchema;
import cz.atlascon.travny.schemas.Schema;
import cz.atlascon.travny.schemas.builders.RecordSchemaBuilder;
import graphql.AssertException;
import graphql.schema.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
    private static final String LIST_NAME = "list";
    private static final String FLOAT_NAME = "floatNumber";

    private static final String FIRST_CLASS = "first";
    private static final String FIELD = "field";
    private static final String SUBCLASS_FIELD = "subclass";
    private static final String INT_FIELD = "intField";
    private static final String BOOLEAN_FIELD = "booleanField";

    @Test
    public void shouldProduceSomething() {
        RecordSchema recordSchema = createDummySchema();
        GraphQLSchema graphQLSchema = generator.generateSchema(recordSchema);
        Assert.assertNotNull(graphQLSchema);
    }

    private RecordSchema createDummySchema() {
        return RecordSchema.newBuilder("DummySchema")
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

        GraphQLObjectType rootType = graphQLSchema.getQueryType();
        List<GraphQLFieldDefinition> fieldDefinitions = rootType.getFieldDefinitions();
        Assert.assertEquals(1, fieldDefinitions.size());
        testDummyRoot(fieldDefinitions.get(0));
    }

    private void testDummyRoot(GraphQLFieldDefinition fieldDefinition) {
        GraphQLObjectType queryType = (GraphQLObjectType) fieldDefinition.getType();
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
    }

    @Test
    public void shouldFailOnWrongNaming() {
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, "this.is.not.valid§§")
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .build();
        try{
            generator.generateSchema(schema2);
        } catch (AssertException e){
            return;
        }
        Assert.fail();
    }

    @Test
    public void shouldFailOnWrongNaming2(){
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, "field0")
                .addField(Schema.STRING, "field0")
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .build();
        try{
            generator.generateSchema(schema2);
        } catch (AssertException e){
            return;
        }
        Assert.fail();
    }

    @Test
    public void shouldProduceValidSchemaWithIDarguments(){
        final String ID_INT = "idInt";
        final String ID_SCHEMA = "idSchema";
        RecordSchema idSchema = RecordSchema.newBuilder(ID_SCHEMA)
                .addField(Schema.INT, ID_INT)
                .build();

        RecordSchema schemaWithId = RecordSchema.newBuilder("schemaWithId")
                .addField(Schema.INT, INT_NUMBER_NAME)
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .setIdSchema(idSchema)
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(schemaWithId);

        GraphQLObjectType queryType = graphQLSchema.getQueryType();
        Assert.assertEquals(2, queryType.getFieldDefinitions().size());
        Assert.fail();
    }

    @Test
    public void shouldProduceValidListSchema() {
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, INT_NUMBER_NAME)
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(createDummySchema(), schema2);
        List<GraphQLFieldDefinition> fieldDefinitions = graphQLSchema.getQueryType().getFieldDefinitions();
        Assert.assertEquals(2, fieldDefinitions.size());
        testDummyRoot(fieldDefinitions.get(0));

        GraphQLObjectType withList = (GraphQLObjectType) fieldDefinitions.get(1).getType();

        GraphQLOutputType type = withList.getFieldDefinition(INT_NUMBER_NAME).getType();
        Assert.assertTrue(type instanceof GraphQLScalarType);

        GraphQLObjectType list = (GraphQLObjectType) withList.getFieldDefinition(LIST_NAME).getType();
        Assert.assertNotNull(list);
    }

    @Test
    public void shouldFailOnEmptySchema() {
        RecordSchema dummy = RecordSchema.newBuilder("dummy").build();
        try {
            generator.generateSchema(dummy);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void shouldProduceValidMultiClassSchema() {

    }

    @Test
    public void oneClassHasMultipleFieldWithSameSubclass(){
        final String FIELD2 = "field2";
        RecordSchema subclass = RecordSchemaBuilder.newBuilder(SUBCLASS_FIELD)
                .addField(Schema.INT, INT_FIELD)
                .addField(Schema.BOOLEAN, BOOLEAN_FIELD)
                .build();
        RecordSchema build = RecordSchemaBuilder.newBuilder(FIRST_CLASS)
                .addField(subclass, FIELD)
                .addField(subclass, FIELD2)
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(build);
        Assert.assertNotNull(graphQLSchema);
    }

    @Test
    public void shouldProduceValidNestedClass() {

        RecordSchema subclass = RecordSchemaBuilder.newBuilder(SUBCLASS_FIELD)
                .addField(Schema.INT, INT_FIELD)
                .addField(Schema.BOOLEAN, BOOLEAN_FIELD)
                .build();
        RecordSchema build = RecordSchemaBuilder.newBuilder(FIRST_CLASS).addField(subclass, FIELD).build();

        GraphQLSchema graphQLSchema = generator.generateSchema(build);
        List<GraphQLFieldDefinition> fieldDefinitions = graphQLSchema.getQueryType().getFieldDefinitions();
        Assert.assertEquals(1, fieldDefinitions.size());
        GraphQLFieldDefinition subcl = fieldDefinitions.get(0);
        Assert.assertEquals(FIRST_CLASS, subcl.getName());

        GraphQLObjectType subType = (GraphQLObjectType) ((GraphQLObjectType) subcl.getType()).getFieldDefinition(FIELD).getType();
        Assert.assertEquals(2, subType.getFieldDefinitions().size());

        GraphQLFieldDefinition intField = subType.getFieldDefinition(INT_FIELD);
        Assert.assertNotNull(intField);
        Assert.assertTrue(intField.getType() instanceof GraphQLScalarType);

        GraphQLFieldDefinition booleanField = subType.getFieldDefinition(BOOLEAN_FIELD);
        Assert.assertNotNull(booleanField);
        Assert.assertTrue(booleanField.getType() instanceof GraphQLScalarType);
    }

}
