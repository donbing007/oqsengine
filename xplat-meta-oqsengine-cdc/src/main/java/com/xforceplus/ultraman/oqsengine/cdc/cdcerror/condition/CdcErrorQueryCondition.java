package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition;

import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;

/**
 * desc :
 * name : CdcErrorQueryCondition
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
public class CdcErrorQueryCondition {
    private Long seqNo;
    private Long batchId;
    private Long id;
    private Long entity;
    private Long commitId;
    private Integer status;
    private Boolean isEqualStatus;
    private Integer type;
    private Long rangeLEExecuteTime;
    private Long rangeGeExecuteTime;
    private Long rangeLEFixedTime;
    private Long rangeGeFixedTime;

    public String conditionToQuerySql() {
        boolean hasEnd = false;
        StringBuilder stringBuilder = new StringBuilder();

        //  add seqNo
        if (null != seqNo) {
            stringBuilder.append(ErrorFieldDefine.SEQ_NO).append("=").append("?");
            hasEnd = true;
        }

        //  add batchId
        if (null != batchId) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.BATCH_ID).append("=").append("?");
            hasEnd = true;
        }

        //  add id
        if (null != id) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.ID).append("=").append("?");
            hasEnd = true;
        }

        //  add entity
        if (null != entity) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.ENTITY).append("=").append("?");
        }

        //  add commitId
        if (null != commitId) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.COMMIT_ID).append("=").append("?");
            hasEnd = true;
        }

        //  add type
        if (null != type) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.TYPE).append("=").append("?");
            hasEnd = true;
        }

        //  add status
        if (null != status) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.STATUS);
            if (null != isEqualStatus && !isEqualStatus) {
                stringBuilder.append("!");

            }
            stringBuilder.append("=").append("?");
            hasEnd = true;
        }

        //  add rangeLEExecuteTime
        if (null != rangeLEExecuteTime) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.EXECUTE_TIME).append("<=").append("?");
            hasEnd = true;
        }

        //  add rangeGeExecuteTime
        if (null != rangeGeExecuteTime) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.EXECUTE_TIME).append(">=").append("?");
            hasEnd = true;
        }

        //  add rangeLEFixedTime
        if (null != rangeLEFixedTime) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.FIXED_TIME).append("<=").append("?");
            hasEnd = true;
        }

        //  add rangeLEFixedTime
        if (null != rangeGeFixedTime) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.FIXED_TIME).append(">=").append("?");
        }

        return stringBuilder.toString();
    }

    public Long getSeqNo() {
        return seqNo;
    }

    public Long getId() {
        return id;
    }

    public Long getCommitId() {
        return commitId;
    }

    public Integer getStatus() {
        return status;
    }

    public Long getRangeLEExecuteTime() {
        return rangeLEExecuteTime;
    }

    public Long getRangeGeExecuteTime() {
        return rangeGeExecuteTime;
    }

    public Long getRangeLEFixedTime() {
        return rangeLEFixedTime;
    }

    public Long getRangeGeFixedTime() {
        return rangeGeFixedTime;
    }

    public Long getBatchId() {
        return batchId;
    }

    public Integer getType() {
        return type;
    }

    public Long getEntity() {
        return entity;
    }

    public Boolean getEqualStatus() {
        return isEqualStatus;
    }

    public CdcErrorQueryCondition setSeqNo(Long seqNo) {
        this.seqNo = seqNo;
        return this;
    }

    public CdcErrorQueryCondition setId(Long id) {
        this.id = id;
        return this;
    }

    public CdcErrorQueryCondition setCommitId(Long commitId) {
        this.commitId = commitId;
        return this;
    }

    public CdcErrorQueryCondition setRangeLEExecuteTime(Long rangeLEExecuteTime) {
        this.rangeLEExecuteTime = rangeLEExecuteTime;
        return this;
    }

    public CdcErrorQueryCondition setRangeGeExecuteTime(Long rangeGeExecuteTime) {
        this.rangeGeExecuteTime = rangeGeExecuteTime;
        return this;
    }

    public CdcErrorQueryCondition setRangeLEFixedTime(Long rangeLEFixedTime) {
        this.rangeLEFixedTime = rangeLEFixedTime;
        return this;
    }

    public CdcErrorQueryCondition setRangeGeFixedTime(Long rangeGeFixedTime) {
        this.rangeGeFixedTime = rangeGeFixedTime;
        return this;
    }

    public CdcErrorQueryCondition setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public CdcErrorQueryCondition setBatchId(Long batchId) {
        this.batchId = batchId;
        return this;
    }

    public CdcErrorQueryCondition setType(Integer type) {
        this.type = type;
        return this;
    }

    public CdcErrorQueryCondition setEqualStatus(Boolean equalStatus) {
        isEqualStatus = equalStatus;
        return this;
    }

    public CdcErrorQueryCondition setEntity(Long entity) {
        this.entity = entity;
        return this;
    }
}
