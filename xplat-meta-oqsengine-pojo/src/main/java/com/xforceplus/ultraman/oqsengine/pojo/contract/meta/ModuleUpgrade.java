package com.xforceplus.ultraman.oqsengine.pojo.contract.meta;

import com.xforceplus.ultraman.oqsengine.pojo.dto.Bo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 模块同步对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class ModuleUpgrade implements Serializable {
    private Long id;
    private String version;
    private List<Bo> bos;

    public ModuleUpgrade() {
    }

    public ModuleUpgrade(Long id, String version, List<Bo> bos) {
        this.id = id;
        this.version = version;
        this.bos = bos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Bo> getBos() {
        return bos;
    }

    public void setBos(List<Bo> bos) {
        this.bos = bos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleUpgrade)) return false;
        ModuleUpgrade that = (ModuleUpgrade) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getBos(), that.getBos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion(), getBos());
    }

}