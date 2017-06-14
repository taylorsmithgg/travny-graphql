package cz.atlascon.travny.graphql;

import cz.atlascon.travny.schemas.Schema;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

/**
 * Created by tomas on 13.6.17.
 */
public interface ClassConvertor {


    GraphQLType getByClass(Class aClass) ;

    GraphQLOutputType getOutputType(Schema schema);

    GraphQLInputType getInputType(Schema schema);
}
