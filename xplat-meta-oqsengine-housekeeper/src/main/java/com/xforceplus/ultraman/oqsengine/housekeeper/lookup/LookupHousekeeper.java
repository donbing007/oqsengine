package com.xforceplus.ultraman.oqsengine.housekeeper.lookup;

/**
 * lookup字段的追踪管家.
 *
 * @author dongbin
 * @version 0.1 2021/07/05 16:23
 * @since 1.8
 */
public interface LookupHousekeeper {

    /**
     * 记录lookup字段的追踪信息,用以追踪某个字段被lookup的信息.
     *
     * @param entityClassId
     * @param entityFieldId
     * @param targetEntity
     */
    public void recordTrackInfo(long lookupEntityClassId, long lookupEntityFieldId, long targetEntityId);
}