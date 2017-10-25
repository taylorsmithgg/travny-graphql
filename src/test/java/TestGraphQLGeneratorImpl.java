import com.google.common.collect.Lists;
import cz.atlascon.travny.graphql.GraphQLGenerator;
import cz.atlascon.travny.graphql.GraphQLGeneratorImpl;
import cz.atlascon.travny.parser.Parser;
import cz.atlascon.travny.schemas.*;
import cz.atlascon.travny.schemas.builders.RecordSchemaBuilder;
import cz.atlascon.travny.types.EnumConstantImpl;
import graphql.AssertException;
import graphql.Scalars;
import graphql.schema.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static graphql.Scalars.*;

/**
 * Created by tomas on 6.6.17.
 */
public class TestGraphQLGeneratorImpl {

    private final GraphQLGenerator generator = new GraphQLGeneratorImpl(new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            return null;
        }
    }, field -> null, null);

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
        GraphQLObjectType queryType = (GraphQLObjectType) ((GraphQLList) fieldDefinition.getType()).getWrappedType();
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

    @Ignore
    @Test
    public void convertingEnum() {
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, "someInt")
                .addField(new EnumSchema("someName", Lists.newArrayList(new EnumConstantImpl(0, "ano"), new EnumConstantImpl(0, "ne"))), "enum")
                .build();
        generator.generateSchema(schema2);
    }

    @Test
    public void shouldFailOnWrongNaming() {
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, "this.is.not.valid§§")
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .build();
        try {
            generator.generateSchema(schema2);
        } catch (AssertException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void shouldFailOnWrongNaming2() {
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, "field0")
                .addField(Schema.STRING, "field0")
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .build();
        try {
            generator.generateSchema(schema2);
        } catch (AssertException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void usingObjectAsFieldAndInput() throws IOException {
        Parser parser = new Parser();
        InputStream resourceAsStream = TestGraphQLGeneratorImpl.class.getResourceAsStream("failSchema2.txt");
        parser.parse(resourceAsStream);
        Set<String> schemaNames = parser.getSchemaNames();

        GraphQLSchema graphQLSchema = generator.generateSchema(Lists.newArrayList(
                (RecordSchema) parser.getSchema("cz.atlascon.app.LCRrez3"),
                (RecordSchema) parser.getSchema("cz.atlascon.app.LCRrez2"),
                (RecordSchema) parser.getSchema("cz.atlascon.app.LCRpredpis2"),
                (RecordSchema) parser.getSchema("cz.atlascon.app.LCRpredpis"))
        );
    }

    @Test
    public void shouldProduceValidSchemaWithIDarguments() {
        final String ID_INT = "idInt_arg";
        final String ID_SCHEMA = "idSchema";
        final String SCHEMA_W_ID = "schemaWithId";
        RecordSchema idSchema = RecordSchema.newBuilder(ID_SCHEMA)
                .addField(Schema.INT, ID_INT)
                .build();

        RecordSchema schemaWithId = RecordSchema.newBuilder(SCHEMA_W_ID)
                .addField(Schema.INT, INT_NUMBER_NAME)
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .setIdSchema(idSchema)
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(schemaWithId);

        GraphQLObjectType queryType = graphQLSchema.getQueryType();
        Assert.assertEquals(1, queryType.getFieldDefinitions().size());
        GraphQLFieldDefinition rootField = queryType.getFieldDefinition(SCHEMA_W_ID);

        Assert.assertEquals(1, rootField.getArguments().size());
        Assert.assertEquals(GraphQLInt, rootField.getArgument(ID_INT).getType());

        GraphQLObjectType schema = (GraphQLObjectType) ((GraphQLList) rootField.getType()).getWrappedType();

        Assert.assertEquals(2, schema.getFieldDefinitions().size());
        Assert.assertEquals(GraphQLInt, schema.getFieldDefinition(INT_NUMBER_NAME).getType());
        Assert.assertNotNull(schema.getFieldDefinition(LIST_NAME).getType());
    }

    @Test
    public void shouldProduceValidComplexListSchema() {
        final String NC = "NestedClass";
        RecordSchema nestedClass = RecordSchema.newBuilder(NC)
                .addField(RecordSchema.LONG, "field2")
                .addField(RecordSchema.BOOLEAN, "field3")
                .build();

        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, INT_NUMBER_NAME)
                .addField(new ListSchema(nestedClass), LIST_NAME)
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(Lists.newArrayList(createDummySchema(), schema2));
        List<GraphQLFieldDefinition> fieldDefinitions = graphQLSchema.getQueryType().getFieldDefinitions();
        Assert.assertEquals(2, fieldDefinitions.size());
        testDummyRoot(fieldDefinitions.get(0));

        GraphQLObjectType withList = (GraphQLObjectType) ((GraphQLList) fieldDefinitions.get(1).getType()).getWrappedType();

        GraphQLOutputType type = withList.getFieldDefinition(INT_NUMBER_NAME).getType();
        Assert.assertTrue(type instanceof GraphQLScalarType);

        GraphQLList list = (GraphQLList) withList.getFieldDefinition(LIST_NAME).getType();
        Assert.assertEquals(NC, list.getWrappedType().getName());
        GraphQLObjectType wrappedType = (GraphQLObjectType) list.getWrappedType();
        Assert.assertEquals(GraphQLLong, wrappedType.getFieldDefinitions().get(0).getType());
        Assert.assertEquals(GraphQLBoolean, wrappedType.getFieldDefinitions().get(1).getType());
    }

    @Test
    public void shouldProduceValidListSchema() {
        RecordSchema schema2 = RecordSchema.newBuilder("Schema2")
                .addField(Schema.INT, INT_NUMBER_NAME)
                .addField(new ListSchema(RecordSchema.INT), LIST_NAME)
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(Lists.newArrayList(createDummySchema(), schema2));
        List<GraphQLFieldDefinition> fieldDefinitions = graphQLSchema.getQueryType().getFieldDefinitions();
        Assert.assertEquals(2, fieldDefinitions.size());
        testDummyRoot(fieldDefinitions.get(0));

        GraphQLObjectType withList = (GraphQLObjectType) ((GraphQLList) fieldDefinitions.get(1).getType()).getWrappedType();

        GraphQLOutputType type = withList.getFieldDefinition(INT_NUMBER_NAME).getType();
        Assert.assertTrue(type instanceof GraphQLScalarType);

        GraphQLList list = (GraphQLList) withList.getFieldDefinition(LIST_NAME).getType();
        Assert.assertEquals(Scalars.GraphQLInt, list.getWrappedType());
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
        final String FIELD2 = "field2";
        RecordSchema subclass = RecordSchemaBuilder.newBuilder(SUBCLASS_FIELD)
                .addField(Schema.INT, INT_FIELD)
                .addField(Schema.BOOLEAN, BOOLEAN_FIELD)
                .build();
        RecordSchema build = RecordSchemaBuilder.newBuilder(FIRST_CLASS)
                .addField(subclass, FIELD)
                .addField(subclass, FIELD2)
                .addField(subclass, "field3")
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(build);
        Assert.assertNotNull(graphQLSchema);
    }

    @Test
    public void oneClassHasMultipleFieldWithSameSubclass() {
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


        GraphQLObjectType listType = (GraphQLObjectType) ((GraphQLList) subcl.getType()).getWrappedType();

        GraphQLObjectType subType = (GraphQLObjectType) listType.getFieldDefinition(FIELD).getType();
        Assert.assertEquals(2, subType.getFieldDefinitions().size());

        GraphQLFieldDefinition intField = subType.getFieldDefinition(INT_FIELD);
        Assert.assertNotNull(intField);
        Assert.assertTrue(intField.getType() instanceof GraphQLScalarType);

        GraphQLFieldDefinition booleanField = subType.getFieldDefinition(BOOLEAN_FIELD);
        Assert.assertNotNull(booleanField);
        Assert.assertTrue(booleanField.getType() instanceof GraphQLScalarType);
    }

    @Test
    public void shouldProduceValidComplexNestedClass() throws IOException {
        Parser parser = new Parser();
        InputStream resourceAsStream = TestGraphQLGeneratorImpl.class.getResourceAsStream("predpisSchema.txt");
        parser.parse(resourceAsStream);
        Set<String> schemaNames = parser.getSchemaNames();

        GraphQLSchema graphQLSchema = generator.generateSchema((RecordSchema) parser.getSchema("cz.atlascon.etic.Propertyvalue"));
    }

    @Test
    public void shouldProduceValidInputList() {
        String name = "name";
        RecordSchema idSchema = RecordSchemaBuilder.newBuilder("cz_atlascon_id")
                .addField(ListSchema.of(Schema.STRING), name).build();
        RecordSchema schema = RecordSchemaBuilder
                .newBuilder("cz_atlascon_record").addField(Schema.STRING, name)
                .setIdSchema(idSchema).build();
        GraphQLSchema graphQLSchema = generator.generateSchema(schema);

        GraphQLFieldDefinition record = graphQLSchema.getQueryType().getFieldDefinition("cz_atlascon_record");
        List<GraphQLArgument> arguments = record.getArguments();
        Assert.assertTrue(arguments.size() == 1);
        GraphQLArgument graphQLArgument = arguments.get(0);

        GraphQLInputType type = graphQLArgument.getType();
        Assert.assertTrue(type instanceof GraphQLList);
        GraphQLType wrappedType = ((GraphQLList) type).getWrappedType();
        Assert.assertEquals("String", wrappedType.getName());
    }

    @Test
    public void shouldProduceValidInputObject() {
        RecordSchema inputObject = RecordSchemaBuilder.newBuilder("object").addField(Schema.DOUBLE, "somefield").build();

        String name = "name";
        RecordSchema idSchema = RecordSchemaBuilder.newBuilder("cz.atlascon.id")
                .addField(ListSchema.of(inputObject), name).build();
        RecordSchema schema = RecordSchemaBuilder
                .newBuilder("cz.atlascon.record").addField(Schema.STRING, name)
                .setIdSchema(idSchema).build();
        GraphQLSchema graphQLSchema = generator.generateSchema(schema);
        //TODO checks
    }


    @Test
    public void shouldProduceValidInputMap() {
        RecordSchema keySchema = RecordSchemaBuilder.newBuilder("keyObject").addField(Schema.STRING, "stringField").build();
        RecordSchema valSchema = RecordSchemaBuilder.newBuilder("valObject").addField(Schema.DOUBLE, "doubleField").build();

        String name = "name";
        RecordSchema idSchema = RecordSchemaBuilder.newBuilder("cz.atlascon.id")
                .addField(MapSchema.of(keySchema, valSchema), name).build();
        RecordSchema schema = RecordSchemaBuilder
                .newBuilder("cz.atlascon.record").addField(Schema.STRING, name)
                .setIdSchema(idSchema).build();
        GraphQLSchema graphQLSchema = generator.generateSchema(schema);
        // TODO checks
    }


    @Test
    public void sameObjectInputOutput() {
        RecordSchema date = RecordSchemaBuilder
                .newBuilder("cz.atlascon.Date")
                .addField(Schema.INT, "day")
                .addField(Schema.INT, "month")
                .addField(Schema.INT, "year")
                .build();

        RecordSchema input = RecordSchemaBuilder
                .newBuilder("input")
                .addField(date, "inputDate")
                .build();
        RecordSchema build = RecordSchemaBuilder
                .newBuilder("together")
                .setIdSchema(input)
                .addField(ListSchema.of(date), "list")
                .build();

        GraphQLSchema graphQLSchema = generator.generateSchema(build);
    }

    @Test
    public void shouldProduceValidNEstedMaps(){
        // map<string, map<string, integer>>
        RecordSchema build = RecordSchemaBuilder.newBuilder("cz.SomeName")
                .addField(
                        MapSchema.of(Schema.STRING,
                                MapSchema.of(Schema.STRING, Schema.INT)), "fieldName").build();
        GraphQLSchema graphQLSchema = generator.generateSchema(build);

        GraphQLObjectType queryType = graphQLSchema.getQueryType();


    }
}
