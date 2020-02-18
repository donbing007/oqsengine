package com.xforceplus.ultraman.oqsengine.core.metadata;

public interface ILink {
    /**
     * 拿到关联数据
     * @param entityClass
     * @param id
     * @param version
     * @return
     */
    public IEntityValue searchValue(IEntityClass entityClass,Long id,String version);
}
