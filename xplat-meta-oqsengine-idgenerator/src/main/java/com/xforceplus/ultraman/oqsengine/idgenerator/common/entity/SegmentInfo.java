package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;


import java.sql.Timestamp;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/7/21 6:25 PM
 */
public class SegmentInfo {

    private Long id;
    private String bizType;
    private Long beginId;
    private Long maxId;
    private Integer step;
    private Integer mode;
    private String pattern;
    private String patternKey;
    private Integer resetable;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Long getBeginId() {
        return beginId;
    }

    public void setBeginId(Long beginId) {
        this.beginId = beginId;
    }

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPatternKey() {
        return patternKey;
    }

    public void setPatternKey(String patternKey) {
        this.patternKey = patternKey;
    }

    public Integer getResetable() {
        return resetable;
    }

    public void setResetable(Integer resetable) {
        this.resetable = resetable;
    }

    public static SegmentBuilder builder() {
        return new SegmentBuilder();
    }


    public static class SegmentBuilder {
        private Long id;
        private String bizType;
        private Long beginId;
        private Long maxId;
        private Integer step;
        private Integer mode;
        private String pattern;
        private String patternKey;
        private Integer resetable;
        private Timestamp createTime;
        private Timestamp updateTime;
        private Long version;

        public SegmentBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public SegmentBuilder withBizType(String bizType) {
            this.bizType = bizType;
            return this;
        }

        public SegmentBuilder withBeginId(Long beginId) {
            this.beginId = beginId;
            return this;
        }

        public SegmentBuilder withMaxId(Long maxId) {
            this.maxId = maxId;
            return this;
        }

        public SegmentBuilder withStep(Integer step) {
            this.step = step;
            return this;
        }

        public SegmentBuilder withMode(Integer mode) {
            this.mode = mode;
            return this;
        }

        public SegmentBuilder withPatten(String patten) {
            this.pattern = patten;
            return  this;
        }

        public SegmentBuilder withVersion(Long version) {
            this.version = version;
            return this;
        }

        public SegmentBuilder withCreateTime(Timestamp createTime) {
            this.createTime = createTime;
            return this;
        }

        public SegmentBuilder withUpdateTime(Timestamp updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public SegmentBuilder withPatternKey(String patternKey) {
            this.patternKey = patternKey;
            return this;
        }

        public SegmentBuilder withResetable(Integer resetable) {
            this.resetable = resetable;
            return this;
        }


        public SegmentInfo build() {
            SegmentInfo segmentInfo = new SegmentInfo();
            segmentInfo.id = this.id;
            segmentInfo.bizType = this.bizType;
            segmentInfo.beginId = this.beginId;
            segmentInfo.maxId = this.maxId;
            segmentInfo.step = this.step;
            segmentInfo.mode = this.mode;
            segmentInfo.pattern = this.pattern;
            segmentInfo.version = this.version;
            segmentInfo.createTime = this.createTime;
            segmentInfo.updateTime = this.updateTime;
            segmentInfo.patternKey = this.patternKey;
            segmentInfo.resetable = this.resetable;
            return segmentInfo;
        }
    }

    @Override
    public String toString() {
        return "SegmentInfo{" +
            "id=" + id +
            ", bizType='" + bizType + '\'' +
            ", beginId=" + beginId +
            ", maxId=" + maxId +
            ", step=" + step +
            ", mode=" + mode +
            ", pattern='" + pattern + '\'' +
            ", patternKey='" + patternKey + '\'' +
            ", resetable=" + resetable +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", version=" + version +
            '}';
    }
}
