package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ParseResult {

    /**
     * 起始ID.
     */
    private long startId;

    /**
     * 计数器.
     */
    private int pos;

    /**
     * 需要最终操作manticore的对象结果集.
     */
    private Map<Long, OqsEngineEntity> finishEntries;

    /**
     * operationEntries中每一条数据key为主键id, value由control信息及业务数据组成.
     * 每一条数据最后都必须检查Attribute是否为空，如果不为空可以转移到finishEntries中.
     */
    private Map<Long, OqsEngineEntity> operationEntries;

    /**
     * 处理过程中失败的记录.
     */
    private Map<String, Error> errors;

    /**
     * 需要checkReady的commitIds.
     */
    private Set<Long> commitIds;

    /**
     * 清除.
     */
    public void clean() {
        finishEntries.clear();
        errors.clear();
        commitIds.clear();

        //  pos重置为0
        pos = CDCConstant.START_POS;
        startId = CDCConstant.NOT_INIT_START_ID;
    }

    /**
     * 构造一个解析结果.
     */
    public ParseResult() {
        this.startId = CDCConstant.NOT_INIT_START_ID;
        this.pos = CDCConstant.START_POS;
        this.finishEntries = new LinkedHashMap<>();
        this.errors = new LinkedHashMap<>();
        this.commitIds = new HashSet<>();
    }

    public Map<Long, OqsEngineEntity> getFinishEntries() {
        return finishEntries;
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

    public void finishOne() {
        pos++;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    /**
     * 写入一条error.
     */
    public void addError(Error error) {
        errors.put(error.keyGenerate(), error);
    }

    /**
     * generate and addError.
     */
    public void addError(long id, long commitId, String message) {
        addError(new Error(id, commitId, pos, "", message));
    }

    /**
     * generate and addError.
     */
    public void addError(long id, long commitId, int pos, String operationObjectString, String message) {
        addError(new Error(id, commitId, pos, operationObjectString, message));
    }

    /**
     * 表示一个错误.
     */
    public static class Error {
        private long id;
        private long commitId;
        private int pos;
        private String message;
        private String operationObjectString;

        /**
         * 构造一个错误.
         *
         * @param id       错误标识.
         * @param commitId 提交号.
         * @param pos      当前读取位置号.
         * @param message  消息.
         */
        public Error(long id, long commitId, int pos, String operationObjectString, String message) {
            this.id = id;
            this.commitId = commitId;
            this.pos = pos;
            this.operationObjectString = operationObjectString;
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
            return commitId + "_" + id + "_" + pos;
        }

        public String getOperationObjectString() {
            return operationObjectString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Error error = (Error) o;
            return id == error.id
                && commitId == error.commitId
                && pos == error.pos
                && Objects.equals(message, error.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, commitId, pos, message);
        }
    }


}
