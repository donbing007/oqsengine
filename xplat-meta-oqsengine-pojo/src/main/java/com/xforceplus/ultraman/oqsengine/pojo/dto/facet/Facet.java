package com.xforceplus.ultraman.oqsengine.pojo.dto.facet;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 分面搜索.
 *
 * @author dongbin
 * @version 0.1 2021/03/31 15:41
 * @since 1.8
 */
public class Facet {

    private Set<IEntityField> fields;

    public static Facet build() {
        return new Facet();
    }

    public Facet() {
    }

    public Facet(Set<IEntityField> fields) {
        lazyInit();

        fields.addAll(fields);

    }

    public Set<IEntityField> getFields() {
        if (fields == null) {
            return Collections.emptySet();
        } else {
            return fields;
        }
    }

    public Facet addField(IEntityField field) {

        lazyInit();

        fields.add(field);

        return this;
    }

    private void lazyInit() {
        if (fields == null) {
            fields = new HashSet<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Facet facet = (Facet) o;
        return Objects.equals(fields, facet.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return "Facet{" +
                "fields=" + fields +
                '}';
    }
}
