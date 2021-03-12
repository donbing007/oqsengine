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
    private Long id;
    private Long commitId;
    private Integer status;
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

        //  add id
        if (null != id) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.ID).append("=").append("?");
            hasEnd = true;
        }

        //  add commitId
        if (null != commitId) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.COMMIT_ID).append("=").append("?");
            hasEnd = true;
        }

        //  add status
        if (null != status) {
            if (hasEnd) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(ErrorFieldDefine.STATUS).append("=").append("?");
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

    public void setSeqNo(Long seqNo) {
        this.seqNo = seqNo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCommitId(Long commitId) {
        this.commitId = commitId;
    }

    public void setRangeLEExecuteTime(Long rangeLEExecuteTime) {
        this.rangeLEExecuteTime = rangeLEExecuteTime;
    }

    public void setRangeGeExecuteTime(Long rangeGeExecuteTime) {
        this.rangeGeExecuteTime = rangeGeExecuteTime;
    }

    public void setRangeLEFixedTime(Long rangeLEFixedTime) {
        this.rangeLEFixedTime = rangeLEFixedTime;
    }

    public void setRangeGeFixedTime(Long rangeGeFixedTime) {
        this.rangeGeFixedTime = rangeGeFixedTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
