package com.xforceplus.ultraman.oqsengine.metadata.mock.generator;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/14
 * @since 1.8
 */
public class GeneralConstant {
    public static final String LEVEL = "_LEVEL_";
    public static final int MOCK_FATHER_DISTANCE = 100;
    public static final int MOCK_ANC_DISTANCE = MOCK_FATHER_DISTANCE + 1000;
    public static final int DEFAULT_VERSION = 1001;
    public static final int DEFAULT_RELATION_TYPE = 1;
    public static final int MOCK_PROFILE_R_DISTANCE = 200101;
    public static final int MOCK_PROFILE_E_DISTANCE = 100101;
    public static final com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.MetaFieldSense MOCK_SYSTEM_FIELD_TYPE =
        FieldConfig.MetaFieldSense.NORMAL;

    public static final String MOCK_EXPRESSION = "return 1 + 1;";
    public static final String MOCK_EXPRESSION_SUB = "return 1 + 2;";
    public static final int MOCK_LEVEL = 1;

    public static final String MOCK_PATTEN = "{0000}";
    public static final String MOCK_PATTEN_SUB = "{0001}";
    public static final String MOCK_SENIOR_EXPRESSION = "getId(${0000}, ${A}${B})";
    public static final String MOCK_SENIOR_EXPRESSION_SUB = "getId(${00000}, ${A}${B})";
    public static final int MOCK_DOMAIN_NOT_TYPE = AutoFill.DomainNoType.SENIOR.getType();
    public static final List<String> MOCK_SENIOR_ARGS = Arrays.asList("A", "B");
    public static final String MOCK_MODEL = "model";
    public static final String MOCK_MIN = "1";
    public static final int MOCK_STEP = 1;


    //  relation
    public static final String R = "_R";
    //  entityField
    public static final String E = "_E";

    public static final String DICT_ID_SUFFIX = "_dictIdSuffix";
    public static final String CNAME_SUFFIX = "_cnameSuffix";
    public static final String NAME_SUFFIX = "_nameSuffix";
    public static final String CODE_SUFFIX = "_codeSuffix";


    public static final Long DEFAULT_LONG_VALUE = 0L;
    public static final String DEFAULT_STRING_VALUE = "default";

    public static final AbstractMap.SimpleEntry<String, Long> PROFILE_CODE_1 =
        new AbstractMap.SimpleEntry<>("PROFILE_CODE_1", 2L);
    public static final AbstractMap.SimpleEntry<String, Long> PROFILE_CODE_2 =
        new AbstractMap.SimpleEntry<>("PROFILE_CODE_2", 3L);

    /**
     * 提供一个4元组
     * @param <A>
     * @param <B>
     * @param <C>
     * @param <D>
     */
    public static class FourGeneric<A, B, C, D> {
        private final Object[] fourGeneric;

        public FourGeneric(A a, B b, C c, D d) {
            this.fourGeneric = new Object[4];
            this.fourGeneric[0] = a;
            this.fourGeneric[1] = b;
            this.fourGeneric[2] = c;
            this.fourGeneric[3] = d;
        }

        public A getA() {
            return (A) this.fourGeneric[0];
        }

        public B getB() {
            return (B) this.fourGeneric[1];
        }

        public C getC() {
            return (C) this.fourGeneric[2];
        }

        public D getD() {
            return (D) this.fourGeneric[3];
        }
    }

    public static Object defaultValue(FieldType fieldType) {
        switch (fieldType) {
            case LONG: {
                return DEFAULT_LONG_VALUE;
            }
            case STRING: {
                return DEFAULT_STRING_VALUE;
            }
            default: {
                throw new IllegalArgumentException(
                    String.format("un-support test fieldType [%s]", fieldType.getType()));
            }
        }
    }

    public static final List<String> DEFAULT_ARGS = Arrays.asList("code", "name", "paper");
}
