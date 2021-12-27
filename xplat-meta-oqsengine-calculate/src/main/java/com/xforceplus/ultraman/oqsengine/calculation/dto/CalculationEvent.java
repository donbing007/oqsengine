package com.xforceplus.ultraman.oqsengine.calculation.dto;

import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculationEvent {
    private String appId;
    private int version;
    private Map<Long, List<CalculationField>> calculationFields;

    /**
     * construct fx.
     */
    public CalculationEvent(String appId, int version) {
        this.appId = appId;
        this.version = version;
        this.calculationFields = new HashMap<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public Map<Long, List<CalculationField>> getCalculationFields() {
        return calculationFields;
    }

    /**
     * CalculationField.
     */
    public static class CalculationField {
        /**
         * 操作类型, 新增/修改/删除.
         */
        private OperationType op;
        /**
         * 该field属于哪个profile.
         */
        private String profile;
        /**
         * 当前改变后的entityField,当op为delete时,该字段为null.
         */
        private IEntityField entityField;

        /**
         * construct.
         */
        public CalculationField(OperationType op, String profile,
                                IEntityField entityField) {
            this.op = op;
            this.profile = profile;
            this.entityField = entityField;
        }

        public OperationType getOp() {
            return op;
        }

        public void setOp(OperationType op) {
            this.op = op;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public IEntityField getEntityField() {
            return entityField;
        }

        public void setEntityField(IEntityField entityField) {
            this.entityField = entityField;
        }
    }
}
