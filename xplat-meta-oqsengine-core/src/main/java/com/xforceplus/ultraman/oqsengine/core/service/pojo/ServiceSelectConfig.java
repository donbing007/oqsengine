package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.facet.Facet;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.util.Objects;
import java.util.Optional;

/**
 * 查询配置.
 *
 * @author dongbin
 * @version 0.1 2021/03/31 16:14
 * @since 1.8
 */
public class ServiceSelectConfig {
    private Page page;
    private Sort sort;
    private Sort secondarySort;
    private Sort thirdSort;
    private Facet facet;
    private Conditions filter;

    public ServiceSelectConfig() {
    }

    public Optional<Page> getPage() {
        return Optional.ofNullable(page);
    }

    public Optional<Sort> getSort() {
        return Optional.ofNullable(sort);
    }

    public Optional<Facet> getFacet() {
        return Optional.ofNullable(facet);
    }

    public Optional<Conditions> getFilter() {
        return Optional.ofNullable(filter);
    }

    public Optional<Sort> getSecondarySort() {
        return Optional.ofNullable(secondarySort);
    }

    public Optional<Sort> getThirdSort() {
        return Optional.ofNullable(thirdSort);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceSelectConfig that = (ServiceSelectConfig) o;
        return Objects.equals(page, that.page)
            && Objects.equals(sort, that.sort)
            && Objects.equals(secondarySort, that.secondarySort)
            && Objects.equals(thirdSort, that.thirdSort)
            && Objects.equals(facet, that.facet)
            && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPage(), getSort(), getSecondarySort(), getThirdSort(), getFacet(), getFilter());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ServiceSelectConfig{");
        sb.append("page=").append(page);
        sb.append(", sort=").append(sort);
        sb.append(", secondarySort=").append(secondarySort);
        sb.append(", thirdSort=").append(thirdSort);
        sb.append(", facet=").append(facet);
        sb.append(", filter=").append(filter);
        sb.append('}');
        return sb.toString();
    }

    /**
     * builder.
     */
    public static final class Builder {
        private Page page;
        private Sort sort;
        private Sort secondarySort;
        private Sort thirdSort;
        private Facet facet;
        private Conditions filter;

        private Builder() {
        }

        public static Builder anSearchConfig() {
            return new Builder();
        }

        public Builder withPage(Page page) {
            this.page = page;
            return this;
        }

        public Builder withSort(Sort sort) {
            this.sort = sort;
            return this;
        }

        public Builder withSecondarySort(Sort sort) {
            this.secondarySort = sort;
            return this;
        }

        public Builder withThridSort(Sort sort) {
            this.thirdSort = sort;
            return this;
        }

        public Builder withFacet(Facet facet) {
            this.facet = facet;
            return this;
        }

        public Builder withFilter(Conditions filter) {
            this.filter = filter;
            return this;
        }

        /**
         * 获得 SearchConfig 实例.
         *
         * @return 实例.
         */
        public ServiceSelectConfig build() {
            ServiceSelectConfig serviceSelectConfig = new ServiceSelectConfig();
            serviceSelectConfig.filter = this.filter;
            serviceSelectConfig.sort = this.sort;
            serviceSelectConfig.secondarySort = this.secondarySort;
            serviceSelectConfig.thirdSort = this.thirdSort;
            serviceSelectConfig.page = this.page;
            serviceSelectConfig.facet = this.facet;
            return serviceSelectConfig;
        }
    }
}
