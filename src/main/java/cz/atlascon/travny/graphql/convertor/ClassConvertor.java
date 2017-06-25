package cz.atlascon.travny.graphql.convertor;

import com.google.common.base.Preconditions;
import cz.atlascon.travny.schemas.EnumSchema;
import cz.atlascon.travny.schemas.Schema;
import cz.atlascon.travny.types.EnumConstant;
import cz.atlascon.travny.types.Type;
import graphql.AssertException;
import graphql.schema.*;

import java.util.Collection;

import static cz.atlascon.travny.graphql.common.Common.convertToName;
import static graphql.Scalars.*;

/**
 * Created by tomas on 13.6.17.
 */
public interface ClassConvertor {

    GraphQLType getByClass(Class aClass) ;

    GraphQLOutputType getOutputType(Schema schema);

    GraphQLInputType getInputType(Schema schema);

}
