package cz.atlascon.travny.graphql.convertor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
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
import java.util.concurrent.ConcurrentMap;

import static cz.atlascon.travny.graphql.common.Common.convertToName;
import static graphql.Scalars.*;

/**
 * Created by tomas on 25.6.17.
 */
public class ClassConvertorImpl<E extends Enum> implements ClassConvertor {
    private final ConcurrentMap<String, GraphQLEnumType> enumMap = Maps.newConcurrentMap();

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
        }
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
            // TODO implement map
            return GraphQLByte;
        } else {
            return getByClass(schema.getType().getJavaClass());
        }
    }


    @Override
    public GraphQLInputType getInputType(Schema schema) {
        if (schema.getType() == Type.ENUM) {
            return createEnum(schema);
        }
        return (GraphQLInputType) createCommon(schema);
    }

    private GraphQLEnumType createEnum(Schema schema) {
        EnumSchema enumSchema = (EnumSchema) schema;
        Collection<EnumConstant> constants = enumSchema.getConstants();
        String eName = convertToName(((EnumSchema) schema).getName());
        GraphQLEnumType.Builder enumQL = GraphQLEnumType.newEnum().name(eName);

        Class<E> aClass = null;
        try {
            aClass = (Class<E>) Class.forName(((EnumSchema) schema).getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        E[] enumConstants = aClass.getEnumConstants();

        for (EnumConstant constant : constants) {
            String constantValue = constant.getConstant();
            enumQL.value(constant.getConstant(), findValue(constantValue, enumConstants), constant.getConstant());
        }
        enumMap.putIfAbsent(eName, enumQL.build());

        return enumMap.get(eName);
    }

    private Object findValue(String constant, E[] enumConstants) {
        for (int i = 0; i < enumConstants.length; i++) {
            if (enumConstants[i].name().equals(constant)) {
                return enumConstants[i];
            }
        }
        throw new AssertException("Value: " + constant + " not found in given constants!");
    }
}