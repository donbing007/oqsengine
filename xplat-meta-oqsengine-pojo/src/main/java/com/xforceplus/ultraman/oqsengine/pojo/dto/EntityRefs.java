package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * entryRef的包装.
 *
 * @author dongbin
 * @version 0.1 2021/3/18 15:32
 * @since 1.8
 */
public class EntityRefs implements Serializable {

    private Collection<EntityRef> refs;
    private Page page;

    public EntityRefs(Collection<EntityRef> refs, Page page) {
        if (refs == null) {
            this.refs = Collections.emptyList();
        } else {
            this.refs = refs;
        }
        this.page = page;
    }

    public Collection<EntityRef> getRefs() {
        return refs;
    }

    public Page getPage() {
        return page;
    }

    public int size() {
        return this.refs.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityRefs)) return false;
        EntityRefs that = (EntityRefs) o;
        return Objects.equals(getRefs(), that.getRefs()) &&
            Objects.equals(getPage(), that.getPage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRefs(), getPage());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityRefs{");
        sb.append("refs=").append(refs);
        sb.append(", page=").append(page);
        sb.append('}');
        return sb.toString();
    }
}
