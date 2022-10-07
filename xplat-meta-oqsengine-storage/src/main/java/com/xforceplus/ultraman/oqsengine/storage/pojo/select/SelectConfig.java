package com.xforceplus.ultraman.oqsengine.storage.pojo.select;

import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.facet.Facet;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 查询配置.
 *
 * @author dongbin
 * @version 0.1 2021/3/1 17:06
 * @since 1.8
 */
public class SelectConfig implements Serializable {

    private long commitId;
    private Sort sort;
    private Sort secondarySort;
    private Sort thirdSort;
    private Page page;
    private Set<Long> excludedIds;
    private Conditions dataAccessFilterCondtitions;
    private Facet facet;

    private OperationType[] ignoredOperations;

    public SelectConfig() {
    }

    public long getCommitId() {
        return commitId;
    }

    public Sort getSort() {
        return this.sort == null ? Sort.buildOutOfSort() : this.sort;
    }

    public Sort getSecondarySort() {
        return this.secondarySort == null ? Sort.buildOutOfSort() : this.secondarySort;
    }

    public Sort getThirdSort() {
        return this.thirdSort == null ? Sort.buildOutOfSort() : this.thirdSort;
    }

    public Page getPage() {
        return this.page == null ? Page.newSinglePage(10) : this.page;
    }

    public Set<Long> getExcludedIds() {
        return this.excludedIds == null ? Collections.emptySet() : this.excludedIds;
    }

    public Facet getFacet() {
        return this.facet == null ? Facet.build() : this.facet;
    }

    public Conditions getDataAccessFilterCondtitions() {
        return this.dataAccessFilterCondtitions == null
            ? Conditions.buildEmtpyConditions() : this.dataAccessFilterCondtitions;
    }

    public OperationType[] getIgnoredOperations() {
        return ignoredOperations;
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
        return getCommitId() == that.getCommitId()
            && Objects.equals(getSort(), that.getSort())
            && Objects.equals(getSecondarySort(), that.getSecondarySort())
            && Objects.equals(getThirdSort(), that.getThirdSort())
            && Objects.equals(getPage(), that.getPage())
            && Objects.equals(getExcludedIds(), that.getExcludedIds())
            && Objects.equals(getDataAccessFilterCondtitions(), that.getDataAccessFilterCondtitions())
            && Objects.equals(getFacet(), that.getFacet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommitId(), getSort(), getSecondarySort(), getThirdSort(), getPage(), getExcludedIds(),
            getDataAccessFilterCondtitions(), getFacet());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SelectConfig{");
        sb.append("commitId=").append(commitId);
        sb.append(", sort=").append(sort);
        sb.append(", page=").append(page);
        sb.append(", excludedIds=").append(excludedIds);
        sb.append(", dataAccessFilterCondtitions=").append(dataAccessFilterCondtitions);
        sb.append(", facet=").append(facet);
        sb.append('}');
        return sb.toString();
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long commitId = 0;
        private Sort sort = Sort.buildOutOfSort();
        private Sort secondarySort = Sort.buildOutOfSort();
        private Sort thirdSort = Sort.buildOutOfSort();
        private Page page = Page.newSinglePage(10);
        private Set<Long> excludedIds = Collections.emptySet();
        private Facet facet = Facet.build();
        private Conditions dataAccessFilterConditions = Conditions.buildEmtpyConditions();

        private List<OperationType> ignoredOperations = Collections.emptyList();

        private Builder() {
        }

        public static Builder anSelectConfig() {
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

        public Builder withSecondarySort(Sort sort) {
            this.secondarySort = sort;
            return this;
        }

        public Builder withThirdSort(Sort sort) {
            this.thirdSort = sort;
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

        /**
         * 排除的ID.
         */
        public Builder withExcludedIds(Set<Long> excludedIds) {
            if (excludedIds == null) {
                return this;
            }

            if (Collections.emptySet().getClass().equals(this.excludedIds.getClass())) {
                this.excludedIds = new HashSet(excludedIds);
            } else {
                this.excludedIds.clear();
                this.excludedIds.addAll(excludedIds);
            }
            return this;
        }

        /**
         * 排除id.
         */
        public Builder withExcludeIds(long[] excludeIds) {
            for (long id : excludeIds) {
                withExcludeId(id);
            }

            return this;
        }

        /**
         * 排除id.
         */
        public Builder withExcludeId(long excludeId) {
            if (Collections.emptySet().getClass().equals(this.excludedIds.getClass())) {
                this.excludedIds = new HashSet<>();
            }
            this.excludedIds.add(excludeId);
            return this;
        }

        /**
         * 需要排除的操作.
         */
        public Builder withIgnoredOperation(OperationType operation) {
            if (Collections.emptyList().getClass().equals(this.ignoredOperations.getClass())) {
                this.ignoredOperations = new ArrayList<>();
            }
            this.ignoredOperations.add(operation);
            return this;
        }

        /**
         * 构造实例.
         */
        public SelectConfig build() {
            SelectConfig selectConfig = new SelectConfig();
            selectConfig.sort = this.sort;
            selectConfig.secondarySort = this.secondarySort;
            selectConfig.thirdSort = this.thirdSort;
            selectConfig.commitId = this.commitId;
            selectConfig.page = this.page;
            selectConfig.excludedIds = this.excludedIds;
            selectConfig.facet = this.facet;
            selectConfig.dataAccessFilterCondtitions = this.dataAccessFilterConditions;
            selectConfig.ignoredOperations = this.ignoredOperations.stream().toArray(OperationType[]::new);
            return selectConfig;
        }
    }
}
