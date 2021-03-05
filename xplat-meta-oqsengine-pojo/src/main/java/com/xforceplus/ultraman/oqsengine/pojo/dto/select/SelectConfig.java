package com.xforceplus.ultraman.oqsengine.pojo.dto.select;

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

    public SelectConfig() {
        if (commitId < 0) {
            commitId = 0;
        }

        if (sort == null) {
            sort = Sort.buildOutOfSort();
        }

        if (excludedIds == null) {
            excludedIds = Collections.emptySet();
        }

        if (page == null) {
            page = Page.emptyPage();
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectConfig)) {
            return false;
        }
        SelectConfig that = (SelectConfig) o;
        return getCommitId() == that.getCommitId() &&
            Objects.equals(getSort(), that.getSort()) &&
            Objects.equals(getPage(), that.getPage()) &&
            Objects.equals(getExcludedIds(), that.getExcludedIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommitId(), getSort(), getPage(), getExcludedIds());
    }


    public static final class Builder {
        private long commitId;
        private Sort sort;
        private Page page;
        private Set<Long> excludedIds;

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

        public Builder withExcludedIds(Set<Long> excludedIds) {
            if (Collections.emptySet().getClass().isInstance(this.excludedIds)) {
                this.excludedIds = new HashSet(excludedIds);
            } else {
                this.excludedIds.clear();
                this.excludedIds.addAll(excludedIds);
            }
            return this;
        }

        public Builder withExcludeId(long excludeId) {
            if (Collections.emptySet().getClass().isInstance(this.excludedIds)) {
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
            return selectConfig;
        }
    }
}
