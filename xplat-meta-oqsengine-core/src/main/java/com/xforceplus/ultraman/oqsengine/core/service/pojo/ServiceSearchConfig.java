package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 服务搜索配置.
 *
 * @author dongbin
 * @version 0.1 2021/05/19 10:00
 * @since 1.8
 */
public class ServiceSearchConfig implements Serializable {
    private String code;
    private String value;
    private Page page;
    private FieldConfig.FuzzyType fuzzyType;
    private EntityClassRef[] entityClassRefs;

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public Page getPage() {
        return page;
    }

    public FieldConfig.FuzzyType getFuzzyType() {
        return fuzzyType;
    }

    public EntityClassRef[] getEntityClassRefs() {
        return entityClassRefs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceSearchConfig that = (ServiceSearchConfig) o;
        return Objects.equals(code, that.code) && Objects.equals(value, that.value) && Objects
            .equals(page, that.page) && fuzzyType == that.fuzzyType && Arrays
            .equals(entityClassRefs, that.entityClassRefs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code, value, page, fuzzyType);
        result = 31 * result + Arrays.hashCode(entityClassRefs);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ServiceSearchConfig{");
        sb.append("code='").append(code).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", page=").append(page);
        sb.append(", fuzzyType=").append(fuzzyType);
        sb.append(", entityClassRefs=")
            .append(entityClassRefs == null ? "null" : Arrays.asList(entityClassRefs).toString());
        sb.append('}');
        return sb.toString();
    }

    /**
     * builder.
     */
    public static final class Builder {
        private String code;
        private String value;
        private Page page;
        private FieldConfig.FuzzyType fuzzyType;
        private EntityClassRef[] entityClassRefs;

        private Builder() {
        }

        public static Builder anServiceSearchConfig() {
            return new Builder();
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withPage(Page page) {
            this.page = page;
            return this;
        }

        public Builder withFuzzyType(FieldConfig.FuzzyType fuzzyType) {
            this.fuzzyType = fuzzyType;
            return this;
        }

        public Builder withEntityClassRefs(EntityClassRef[] entityClassRefs) {
            this.entityClassRefs = entityClassRefs;
            return this;
        }

        /**
         * builder.
         */
        public ServiceSearchConfig build() {
            ServiceSearchConfig serviceSearchConfig = new ServiceSearchConfig();
            serviceSearchConfig.value = this.value;
            serviceSearchConfig.code = this.code;

            if (this.fuzzyType == null) {
                serviceSearchConfig.fuzzyType = FieldConfig.FuzzyType.NOT;
            } else {
                serviceSearchConfig.fuzzyType = this.fuzzyType;
            }

            if (this.page == null) {
                this.page = Page.newSinglePage(10);
            } else {
                serviceSearchConfig.page = this.page;
            }

            if (this.entityClassRefs == null) {
                this.entityClassRefs = new EntityClassRef[0];
            } else {
                serviceSearchConfig.entityClassRefs = this.entityClassRefs;
            }
            return serviceSearchConfig;
        }
    }
}
