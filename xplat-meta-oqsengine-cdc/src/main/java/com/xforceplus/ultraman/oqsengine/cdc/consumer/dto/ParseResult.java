package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
     * 计数器.
     */
    int pos;

    /**
     * 需要最终操作manticore的对象结果集.
     */
    private Map<Long, OriginalEntity> finishEntries;

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

        //  pos重置为0
        pos = CDCConstant.START_POS;
    }

    public ParseResult() {
        this.pos = CDCConstant.START_POS;
        this.finishEntries = new HashMap<>();
        this.errors  = new LinkedHashMap<>();
        this.commitIds = new HashSet<>();
    }

    public Map<Long, OriginalEntity> getFinishEntries() {
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

    public void finishOne(long id) {
        pos++;
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
        addError(new Error(id, commitId, pos, message));
    }

    public static class Error {
        private long id;
        private long commitId;
        private int pos;
        private String message;

        public Error(long id, long commitId, int pos, String message) {
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
            return commitId + "_" + id + "_" + pos;
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
            return id == error.id && commitId == error.commitId && pos == error.pos &&
                Objects.equals(message, error.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, commitId, pos, message);
        }
    }


}
