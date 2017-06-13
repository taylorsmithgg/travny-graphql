package cz.atlascon.travny.graphql;

import com.google.common.base.Preconditions;
import cz.atlascon.travny.schemas.EnumSchema;
import cz.atlascon.travny.schemas.Schema;
import cz.atlascon.travny.types.EnumConstant;
import cz.atlascon.travny.types.Type;
import graphql.AssertException;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;

import java.util.Collection;

import static cz.atlascon.travny.graphql.Common.convertToName;
import static graphql.Scalars.*;

/**
 * Created by tomas on 13.6.17.
 */
public class JavaClassConvertor<E extends Enum<E>> implements ClassConvertor {

    @Override
    public GraphQLScalarType getByClass(Class aClass) {
        Preconditions.checkNotNull(aClass);

        if (aClass.equals(Integer.class)) {
            return GraphQLInt;
        } else if (aClass.equals(Long.class)) {
            return GraphQLLong;
        } else if (aClass.equals(Short.class)) {
            return GraphQLShort;
        } else if (aClass.equals(String.class)) {
            return GraphQLString;
        } else if (aClass.equals(Boolean.class)) {
            return GraphQLBoolean;
        } else if (aClass.equals(Character.class)) {
            return GraphQLChar;
        } else if (aClass.equals(Float.class)) {
            return GraphQLFloat;
        } else
//            if (aClass.equals(Enum.class)) {
//            Enum[] enumConstants = (Enum[]) aClass.getEnumConstants();
//            GraphQLEnumType.Builder qlEnum = GraphQLEnumType.newEnum()
//                    .name(convertToName(aClass.getName()));
//            for(Enum o : enumConstants){
//                qlEnum.value(o.name());
//            }
//            return qlEnum.build();
//        }

            throw new AssertException("Not a valid class type: " + aClass.getName());
    }


    @Override
    public GraphQLOutputType getOutputType(Schema schema) {
        return createCommon(schema);
    }

    private GraphQLOutputType createCommon(Schema schema) {
        if (schema.getType() == Type.ENUM) {
            return createEnum(schema);
        } else if (schema.getType() == Type.BYTES) {
            return GraphQLByte;
        } else if (schema.getType() == Type.MAP) {
            return GraphQLByte;
        } else {
            return getByClass(schema.getType().getJavaClass());
        }
    }


    @Override
    public GraphQLInputType getInputType(Schema schema) {
        return (GraphQLInputType) createCommon(schema);
    }

    private GraphQLEnumType createEnum(Schema schema) {
        EnumSchema enumSchema = (EnumSchema) schema;
        Collection<EnumConstant> constants = enumSchema.getConstants();
        GraphQLEnumType.Builder name = GraphQLEnumType.newEnum().name(convertToName(((EnumSchema) schema).getName()));

        for (EnumConstant constant : constants) {
            name.value(constant.getConstant());
        }
        return name.build();
    }
}
