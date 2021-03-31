package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.facet.Facet;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.util.Objects;

/**
 * 搜索配置.
 *
 * @author dongbin
 * @version 0.1 2021/03/31 16:14
 * @since 1.8
 */
public class SearchConfig {
    private Page page;
    private Sort sort;
    private Facet facet;
    private Conditions filter;

    public SearchConfig() {
    }

    public Page getPage() {
        return page;
    }

    public Sort getSort() {
        return sort;
    }

    public Facet getFacet() {
        return facet;
    }

    public Conditions getFilter() {
        return filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchConfig that = (SearchConfig) o;
        return Objects.equals(page, that.page)
                && Objects.equals(sort, that.sort)
                && Objects.equals(facet, that.facet)
                && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, sort, facet, filter);
    }

    @Override
    public String toString() {
        return "SearchConfig{" +
                "page=" + page +
                ", sort=" + sort +
                ", facet=" + facet +
                ", secondFilter=" + filter +
                '}';
    }


    public static final class Builder {
        private Page page = Page.emptyPage();
        private Sort sort = Sort.buildOutOfSort();
        private Facet facet = Facet.build();
        private Conditions filter = Conditions.buildEmtpyConditions();

        private Builder() {
        }

        public static Builder aSearchConfig() {
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

        public Builder withFacet(Facet facet) {
            this.facet = facet;
            return this;
        }

        public Builder withFilter(Conditions filter) {
            this.filter = filter;
            return this;
        }

        public SearchConfig build() {
            SearchConfig searchConfig = new SearchConfig();
            searchConfig.filter = this.filter;
            searchConfig.sort = this.sort;
            searchConfig.page = this.page;
            searchConfig.facet = this.facet;
            return searchConfig;
        }
    }
}