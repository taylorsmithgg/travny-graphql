package cz.atlascon.travny.graphql;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.atlascon.travny.schemas.RecordSchema;
import graphql.language.Type;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Assert;
import org.junit.Test;

import static cz.atlascon.travny.graphql.DummyClass.INT_NUMBER_NAME;

/**
 * Created by tomas on 6.6.17.
 */
public class TestGraphQLGeneratorImpl {
    private final GraphQLGenerator generator = new GraphQLGeneratorImpl();

    @Test
    public void shouldProduceSomething() {
        RecordSchema recordSchema = new DummyClass().getSchema();
        GraphQLSchema graphQLSchema = generator.generateSchema(recordSchema);
        Assert.assertNotNull(graphQLSchema);
    }

    @Test
    public void shouldProduceValid() {
        RecordSchema recordSchema = new DummyClass().getSchema();
        GraphQLSchema graphQLSchema = generator.generateSchema(recordSchema);

        GraphQLObjectType queryType = graphQLSchema.getQueryType();
        GraphQLFieldDefinition fieldDefinition = queryType.getFieldDefinition(INT_NUMBER_NAME);
        Type type = fieldDefinition.getDefinition().getType();
    }

    @Test
    public void shouldFail() {

    }

    @Test
    public void shouldProduceValidEmptyClassSchema() {

    }

}
