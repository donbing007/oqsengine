package com.xforceplus.ultraman.oqsengine.storage.pojo.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.facet.Facet;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 搜索配置.
 *
 * @author dongbin
 * @version 0.1 2021/3/1 17:06
 * @since 1.8
 */
public class SelectConfig implements Serializable {

    private long commitId;
    private Sort sort;
    private Page page;
    private Set<Long> excludedIds;
    private Conditions dataAccessFilterCondtitions;
    private Facet facet;

    public SelectConfig() {
    }

    public long getCommitId() {
        return commitId;
    }

    public Sort getSort() {
        return sort;
    }

    public Page getPage() {
        return page;
    }

    public Set<Long> getExcludedIds() {
        return excludedIds;
    }

    public Facet getFacet() {
        return facet;
    }

    public Conditions getDataAccessFilterCondtitions() {
        return dataAccessFilterCondtitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelectConfig that = (SelectConfig) o;
        return commitId == that.commitId
            && Objects.equals(sort, that.sort)
            && Objects.equals(page, that.page)
            && Objects.equals(excludedIds, that.excludedIds)
            && Objects.equals(dataAccessFilterCondtitions, that.dataAccessFilterCondtitions)
            && Objects.equals(facet, that.facet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitId, sort, page, excludedIds, dataAccessFilterCondtitions, facet);
    }

//    @Override
//    public String toString() {
//        final StringBuffer sb = new StringBuffer("SelectConfig{");
//        sb.append("commitId=").append(commitId);
//        sb.append(", sort=").append(sort);
//        sb.append(", page=").append(page);
//        sb.append(", excludedIds=").append(excludedIds);
//        sb.append(", dataAccessFilterCondtitions=").append(dataAccessFilterCondtitions);
//        sb.append(", facet=").append(facet);
//        sb.append('}');
//        return sb.toString();
//    }

    /**
     * builder
     */
    public static final class Builder {
        private long commitId = 0;
        private Sort sort = Sort.buildOutOfSort();
        private Page page = Page.emptyPage();
        private Set<Long> excludedIds = Collections.emptySet();
        private Facet facet = Facet.build();
        private Conditions dataAccessFilterConditions = Conditions.buildEmtpyConditions();

        private Builder() {
        }

        public static Builder aSelectConfig() {
            return new Builder();
        }

        public Builder withCommitId(long commitId) {
            this.commitId = commitId;
            return this;
        }

        public Builder withSort(Sort sort) {
            this.sort = sort;
            return this;
        }

        public Builder withPage(Page page) {
            this.page = page;
            return this;
        }

        public Builder withFacet(Facet facet) {
            this.facet = facet;
            return this;
        }

        public Builder withDataAccessFitlerCondtitons(Conditions conditions) {
            this.dataAccessFilterConditions = conditions;
            return this;
        }

        public Builder withExcludedIds(Set<Long> excludedIds) {
            if (Collections.emptySet().getClass().equals(this.excludedIds.getClass())) {
                this.excludedIds = new HashSet(excludedIds);
            } else {
                this.excludedIds.clear();
                this.excludedIds.addAll(excludedIds);
            }
            return this;
        }

        public Builder withExcludeId(long excludeId) {
            if (Collections.emptySet().getClass().equals(this.excludedIds.getClass())) {
                this.excludedIds = new HashSet<>();
            }
            this.excludedIds.add(excludeId);
            return this;
        }

        public SelectConfig build() {
            SelectConfig selectConfig = new SelectConfig();
            selectConfig.sort = this.sort;
            selectConfig.commitId = this.commitId;
            selectConfig.page = this.page;
            selectConfig.excludedIds = this.excludedIds;
            selectConfig.facet = this.facet;
            selectConfig.dataAccessFilterCondtitions = this.dataAccessFilterConditions;
            return selectConfig;
        }
    }
}
