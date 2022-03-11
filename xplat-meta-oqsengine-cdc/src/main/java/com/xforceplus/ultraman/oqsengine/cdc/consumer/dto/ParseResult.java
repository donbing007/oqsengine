package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;

import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ParseResult {

    /**
     * 计数器.
     */
    int pos;

    /**
     * dynamic中最后一条数据的主键ID.
     */
    long lastId;

    /**
     * 当前记录的唯一前缀.
     */
    private String uniKeyPrefix;

    /**
     * 需要最终操作manticore的对象结果集.
     */
    private Map<Long, OriginalEntity> finishEntries;

    /**
     * operationEntries中每一条数据key为主键id, value由control信息及业务数据组成.
     * 每一条数据最后都必须检查Attribute是否为空，如果不为空可以转移到finishEntries中.
     */
    private Map<Long, OriginalEntity> operationEntries;

    /**
     * 处理过程中失败的记录.
     */
    private Map<String, Error> errors;

    /**
     * 需要checkReady的commitIds
     */
    private Set<Long> commitIds;

    /**
     * 清除.
     */
    public void clean() {
        finishEntries.clear();
        errors.clear();
        commitIds.clear();

        //  最后一条记录不是binLog中该批次的最后一条时将清除掉该集合.
        if (operationEntries.size() > 0) {
            OriginalEntity t = operationEntries.remove(lastId);

            operationEntries.clear();
            //  跨批次最后只有控制信息
            if (null != t) {
                operationEntries.put(lastId, t);
            }
        }

        //  pos重置为0
        pos = CDCConstant.START_POS;

        //  lastId重置
        lastId = INIT_ID;

        uniKeyPrefix = "";
    }

    public ParseResult() {
        this.pos = CDCConstant.START_POS;
        this.lastId = INIT_ID;
        this.uniKeyPrefix = "";
        this.finishEntries = new HashMap<>();
        this.operationEntries = new HashMap<>();
        this.errors  = new LinkedHashMap<>();
        this.commitIds = new HashSet<>();
    }

    public Map<Long, OriginalEntity> getFinishEntries() {
        return finishEntries;
    }

    public Map<Long, OriginalEntity> getOperationEntries() {
        return operationEntries;
    }

    public Map<String, Error> getErrors() {
        return errors;
    }

    public Set<Long> getCommitIds() {
        return commitIds;
    }

    public int getPos() {
        return pos;
    }

    public void finishOne(long id) {
        this.lastId = id;
        pos++;
    }

    public long getLastId() {
        return lastId;
    }


    public String getUniKeyPrefix() {
        return uniKeyPrefix;
    }

    public void setUniKeyPrefix(String uniKeyPrefix) {
        this.uniKeyPrefix = uniKeyPrefix;
    }

    /**
     * 写入一条error.
     * @param error
     */
    public void addError(Error error) {
        errors.put(error.keyGenerate(), error);
    }

    /**
     * generate and addError.
     */
    public void addError(long id, long commitId, String message) {
        addError(new Error(this.uniKeyPrefix, id, commitId, pos, message));
    }

    public static class Error {
        private String prefix;
        private long id;
        private long commitId;
        private int pos;
        private String message;

        public Error(String prefix, long id, long commitId, int pos, String message) {
            this.prefix = prefix;
            this.id = id;
            this.commitId = commitId;
            this.pos = pos;
            this.message = message;
        }

        public long getId() {
            return id;
        }

        public long getCommitId() {
            return commitId;
        }

        public String getMessage() {
            return message;
        }

        public String keyGenerate() {
            return prefix + "_" + id + "_" + commitId + "_" + pos;
        }
    }
}
