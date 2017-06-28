import cz.atlascon.travny.schemas.EnumSchema;
import cz.atlascon.travny.types.EnumConstant;

/**
 * Created by tomas on 13.6.17.
 */
public enum SomeEnum implements EnumConstant {
    A(0, false),
    B(1, false),
    C(2, false);

    private final int ord;
    private final boolean deprecated;

    private SomeEnum(int ord, boolean deprecated) {
        this.ord = ord;
        this.deprecated = deprecated;
    }



    public static cz.atlascon.travny.schemas.EnumSchema getEnumSchema() {
        return EnumSchema.newBuilder(SomeEnum.class.getName())
                .addConstant(A.name())
                .addConstant(B.name())
                .addConstant(C.name())
                .build();
    }

    @Override
    public String getConstant() {
        return name();
    }

    @Override
    public int getOrd() {
        return ord;
    }

    @Override
    public boolean isDeprecated() {
        return deprecated;
    }
}
