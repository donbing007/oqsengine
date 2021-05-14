package com.xforceplus.ultraman.oqsengine.storage.pojo.search;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * 跨元信息的搜索配置.
 *
 * @author dongbin
 * @version 0.1 2021/05/14 14:37
 * @since 1.8
 */
public class CrossSearchConfig implements Serializable {
    private Page page;
    private IEntityClass[] entityClasses;

    public Page getPage() {
        return page;
    }

    public IEntityClass[] getEntityClasses() {
        return entityClasses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrossSearchConfig that = (CrossSearchConfig) o;
        return Objects.equals(page, that.page) && Arrays.equals(entityClasses, that.entityClasses);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(page);
        result = 31 * result + Arrays.hashCode(entityClasses);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SearchConfig{");
        sb.append("page=").append(page);
        sb.append(", entityClasses=")
            .append(entityClasses == null ? "null" : Arrays.asList(entityClasses).toString());
        sb.append('}');
        return sb.toString();
    }


    public static final class Builder {
        private Page page = Page.newSinglePage(10);
        private Collection<IEntityClass> entityClasses;

        private Builder() {
        }

        public static Builder anSearchConfig() {
            return new Builder();
        }

        public Builder withPage(Page page) {
            this.page = page;
            return this;
        }

        /**
         * 增加相关元信息.
         */
        public Builder withEntityClasses(Collection<IEntityClass> entityClasses) {
            if (this.entityClasses == null) {
                this.entityClasses = new ArrayList<>(entityClasses.size());
            }
            this.entityClasses.addAll(entityClasses);
            return this;
        }

        /**
         * 增加相关元信息.
         */
        public Builder withEntityClass(IEntityClass entityClass) {
            if (this.entityClasses == null) {
                this.entityClasses = new ArrayList<>(entityClasses.size());
            }
            this.entityClasses.add(entityClass);
            return this;
        }

        /**
         * 构造新实例.
         */
        public CrossSearchConfig build() {
            CrossSearchConfig crossSearchConfig = new CrossSearchConfig();
            crossSearchConfig.page = this.page;
            crossSearchConfig.entityClasses =
                this.entityClasses == null ? new IEntityClass[0] : this.entityClasses.toArray(new IEntityClass[0]);
            return crossSearchConfig;
        }
    }
}
