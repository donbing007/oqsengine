package com.xforceplus.ultraman.oqsengine.cdc.testhelp;

import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToMap;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CanalEntryCase {
    private long id;
    private int levelOrdinal;
    private boolean deleted;
    private boolean replacement;
    private int version;
    private int oqsmajor;
    private long create;
    private long update;
    private long tx;
    private long commitId;
    private String attr;
    private long entityId;
    private String profile;

    private CanalEntryCase() {

    }

    public static CanalEntryCase anCase() {
        return new CanalEntryCase();
    }

    public CanalEntryCase withId(long id) {
        this.id = id;
        return this;
    }

    public CanalEntryCase withCommitId(long commitId) {
        this.commitId = commitId;
        return this;
    }

    public CanalEntryCase withReplacement(boolean replacement) {
        this.replacement = replacement;
        return this;
    }

    public CanalEntryCase withLevelOrdinal(int levelOrdinal) {
        this.levelOrdinal = levelOrdinal;
        return this;
    }

    public CanalEntryCase withDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public CanalEntryCase withVersion(int version) {
        this.version = version;
        return this;
    }

    public CanalEntryCase withQqsmajor(int oqsmajor) {
        this.oqsmajor = oqsmajor;
        return this;
    }

    public CanalEntryCase withCreate(long create) {
        this.create = create;
        return this;
    }

    public CanalEntryCase withUpdate(long update) {
        this.update = update;
        return this;
    }

    public CanalEntryCase withTx(long tx) {
        this.tx = tx;
        return this;
    }

    public CanalEntryCase withAttr(String attr) {
        this.attr = attr;
        return this;
    }

    public CanalEntryCase withProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public CanalEntryCase withEntityId(long entityId) {
        this.entityId = entityId;
        return this;
    }

    public long getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * 操作类型.
     *
     * @return 类型.
     */
    public int getOp() {
        int op = OperationType.DELETE.getValue();
        if (!isDeleted()) {
            if (isReplacement()) {
                op = OperationType.UPDATE.getValue();
            } else {
                op = OperationType.CREATE.getValue();
            }
        }
        return op;
    }

    public int getVersion() {
        return version;
    }

    public int getOqsmajor() {
        return oqsmajor;
    }

    public long getCreate() {
        return create;
    }

    public long getUpdate() {
        return update;
    }

    public long getTx() {
        return tx;
    }

    public long getCommitId() {
        return commitId;
    }

    public String getAttr() {
        return attr;
    }

    public long getEntityId() {
        return entityId;
    }

    public int getLevelOrdinal() {
        return levelOrdinal;
    }

    public boolean isReplacement() {
        return replacement;
    }

    public String getProfile() {
        return profile;
    }

    public void assertionOriginEntry(OqsEngineEntity oqsEngineEntity) throws JsonProcessingException {
        Assertions.assertEquals(this.id, oqsEngineEntity.getId());
        Assertions.assertEquals(this.entityId, oqsEngineEntity.getEntityClass().id());
        Assertions.assertEquals(this.tx, oqsEngineEntity.getTx());
        Assertions.assertEquals(this.commitId, oqsEngineEntity.getCommitid());
        Assertions.assertEquals(this.oqsmajor, oqsEngineEntity.getOqsMajor());
        Assertions.assertEquals(this.create, oqsEngineEntity.getCreateTime());
        Assertions.assertEquals(this.update, oqsEngineEntity.getUpdateTime());
        Assertions.assertEquals(this.version, oqsEngineEntity.getVersion());
        Assertions.assertEquals(this.deleted, oqsEngineEntity.isDeleted());
        Assertions.assertEquals(this.getOp(), oqsEngineEntity.getOp());

        Map<String, Object> r = attributesToMap(this.attr);
        Assertions.assertEquals(r.size(), oqsEngineEntity.attributeSize());
        r.forEach(
            (rk, rv) -> {
                Object ov = oqsEngineEntity.getAttributes().get(rk);
                Assertions.assertEquals(rv, ov);
            }
        );
    }

    public void assertionColumns(List<CanalEntry.Column> columns) {
        Assertions.assertEquals(this.id, BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ID));
        Assertions.assertEquals(this.tx, BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.TX));
        Assertions.assertEquals(this.commitId, BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.COMMITID));
        Assertions.assertEquals(this.oqsmajor, BinLogParseUtils.getIntegerFromColumn(columns, OqsBigEntityColumns.OQSMAJOR));
        Assertions.assertEquals(this.create, BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.CREATETIME));
        Assertions.assertEquals(this.update, BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.UPDATETIME));
        Assertions.assertEquals(this.version, BinLogParseUtils.getIntegerFromColumn(columns, OqsBigEntityColumns.VERSION));
        Assertions.assertEquals(this.deleted, BinLogParseUtils.getBooleanFromColumn(columns, OqsBigEntityColumns.DELETED));
        Assertions.assertEquals(this.getOp(), BinLogParseUtils.getIntegerFromColumn(columns, OqsBigEntityColumns.OP));
        Assertions.assertEquals(this.getAttr(), BinLogParseUtils.getStringFromColumn(columns, OqsBigEntityColumns.ATTRIBUTE));

        Optional<OqsBigEntityColumns> levelOp = OqsBigEntityColumns.getByOrdinal(levelOrdinal);
        Assertions.assertTrue(levelOp.isPresent());
        Assertions.assertEquals(this.entityId, BinLogParseUtils.getLongFromColumn(columns, levelOp.get()));

    }
}
