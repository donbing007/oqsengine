package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;

import java.util.Objects;

public class BoNode {

    private String code;

    private Long id;

    public BoNode(String code, Long id) {
        this.code = code;
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoNode boNode = (BoNode) o;
        return Objects.equals(code, boNode.code) &&
                Objects.equals(id, boNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, id);
    }
}
