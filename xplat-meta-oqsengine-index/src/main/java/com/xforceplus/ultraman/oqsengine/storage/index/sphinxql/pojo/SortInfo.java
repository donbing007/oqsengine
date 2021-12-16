package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 排序的中间结果.
 *
 * @author dongbin
 * @version 0.1 2021/09/08 15:13
 * @since 1.8
 */
public class SortInfo {

    // 字段名称.
    private String fieldName;
    // 是否为数字类型.
    private boolean number;
    // 是否为系统字段.
    private boolean system;
    // 是否为主键.
    private boolean identifie;
    // 是否为降序.
    private boolean desc;
    // 逻辑字段类型.
    private IEntityField field;

    public String getFieldName() {
        return fieldName;
    }

    public boolean isNumber() {
        return number;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isIdentifie() {
        return identifie;
    }

    public boolean isDesc() {
        return desc;
    }

    public IEntityField getField() {
        return field;
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        // 字段名称.
        private String fieldName;
        // 是否为数字类型.
        private boolean number = false;
        // 是否为系统字段.
        private boolean system = false;
        // 是否为主键.
        private boolean identifie = false;
        // 是否为降序.
        private boolean desc = false;
        // 逻辑字段类型.
        private IEntityField field;

        private Builder() {
        }

        public static Builder anSortField() {
            return new Builder();
        }

        public Builder withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder withNumber(boolean number) {
            this.number = number;
            return this;
        }

        public Builder withSystem(boolean system) {
            this.system = system;
            return this;
        }

        public Builder withIdentifie(boolean identifie) {
            this.identifie = identifie;
            return this;
        }

        public Builder withDesc(boolean desc) {
            this.desc = desc;
            return this;
        }

        public Builder withField(IEntityField field) {
            this.field = field;
            return this;
        }

        /**
         * 构造.
         */
        public SortInfo build() {
            SortInfo sortInfo = new SortInfo();
            sortInfo.system = this.system;
            sortInfo.number = this.number;
            sortInfo.identifie = this.identifie;
            sortInfo.desc = this.desc;
            sortInfo.field = this.field;
            sortInfo.fieldName = this.fieldName;
            return sortInfo;
        }
    }
}
