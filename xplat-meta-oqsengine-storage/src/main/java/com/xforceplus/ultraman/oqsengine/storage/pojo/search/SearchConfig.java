package com.xforceplus.ultraman.oqsengine.storage.pojo.search;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 跨元信息的搜索配置.
 *
 * @author dongbin
 * @version 0.1 2021/05/14 14:37
 * @since 1.8
 */
public class SearchConfig implements Serializable {
    private String code;
    private String value;
    private FieldConfig.FuzzyType fuzzyType;
    private Page page;

    public Page getPage() {
        return page;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public FieldConfig.FuzzyType getFuzzyType() {
        return fuzzyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchConfig that = (SearchConfig) o;
        return Objects.equals(code, that.code) && Objects.equals(value, that.value)
            && fuzzyType == that.fuzzyType && Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code, value, fuzzyType, page);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CrossSearchConfig{");
        sb.append("code='").append(code).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", fuzzyType=").append(fuzzyType);
        sb.append(", page=").append(page);
        sb.append('}');
        return sb.toString();
    }


    /**
     * 构造器.
     */
    public static final class Builder {
        private String code;
        private String value;
        private FieldConfig.FuzzyType fuzzyType = FieldConfig.FuzzyType.NOT;
        private Page page;

        private Builder() {
        }

        public static Builder anSearchConfig() {
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

        public Builder withFuzzyType(FieldConfig.FuzzyType fuzzyType) {
            this.fuzzyType = fuzzyType;
            return this;
        }

        public Builder withPage(Page page) {
            this.page = page;
            return this;
        }

        /**
         * builder.
         */
        public SearchConfig build() {
            SearchConfig searchConfig = new SearchConfig();
            searchConfig.fuzzyType = this.fuzzyType;
            searchConfig.code = this.code;
            searchConfig.value = this.value;
            if (this.page == null) {
                searchConfig.page = Page.newSinglePage(10);
            } else {
                searchConfig.page = this.page;
            }
            return searchConfig;
        }
    }
}
