package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 应用初始化信息.
 *
 * @version 0.1 2021/11/22 16:12
 * @Auther weikai
 * @since 1.8
 */
public class InitCalculationInfo {
    /**
     * appId.
     */
    private String code;


    /**
     * 年龄.
     */
    private int age;

    /**
     * 首次候选辨识.
     */
    private boolean initFlag;

    /**
     * 当前应用全量计算字段参与者.
     */
    private Collection<AbstractParticipant> all;

    /**
     * 当前应用计算字段引用树.
     */
    private Collection<Infuence> infuences;

    /**
     * 当前应用未被初始化的计算字段参与者.
     */
    private Collection<AbstractParticipant> need;

    /**
     * 当前应用无需初始化的计算字段参与者.
     */
    private Collection<AbstractParticipant> skip;


    /**
     * 当前应用本次初始化的计算字段参与者.
     */
    private Collection<AbstractParticipant> run;

    /**
     * 当前应用已经完成初始化的计算字段参与者.
     */
    private Collection<AbstractParticipant> done;

    /**
     * 当前应用初始化失败的计算字段参与者.
     */
    private Collection<AbstractParticipant> failed;

    /**
     * 当前应用计算字段参与者初始化候选人.
     */
    private Map<IEntityClass, HashSet<AbstractParticipant>> candidate;


    public Collection<AbstractParticipant> getDone() {
        return done;
    }

    public void setDone(Collection<AbstractParticipant> done) {
        this.done = done;
    }

    public int getAge() {
        return age;
    }

    public void growUp() {
        this.age++;
    }


    public void setCode(String code) {
        this.code = code;
    }

    public void setAll(Collection<AbstractParticipant> all) {
        this.all = all;
    }

    public void setInfuences(Collection<Infuence> infuences) {
        this.infuences = infuences;
    }

    public void setNeed(Collection<AbstractParticipant> need) {
        this.need = need;
    }

    public void setCandidate(Map<IEntityClass, HashSet<AbstractParticipant>> candidate) {
        this.candidate = candidate;
    }

    /**
     * 获取run.
     */
    public Collection<AbstractParticipant> getRun() {
        if (run == null) {
            run = new ArrayList<>();
        }
        return run;
    }

    public Collection<AbstractParticipant> getFailed() {
        return failed;
    }

    public void setFailed(Collection<AbstractParticipant> failed) {
        this.failed = failed;
    }

    public void setRun(Collection<AbstractParticipant> run) {
        this.run = run;
    }

    public void setInitFlag(boolean initFlag) {
        this.initFlag = initFlag;
    }

    public boolean isInitFlag() {
        return initFlag;
    }

    public Collection<AbstractParticipant> getSkip() {
        return skip;
    }

    public String getCode() {
        return code;
    }

    /**
     * get all.
     */
    public Collection<AbstractParticipant> getAll() {
        if (all == null) {
            all = Collections.emptyList();
        }
        return all;
    }

    /**
     * 获取应用计算字段依赖树.
     */
    public Collection<Infuence> getInfuences() {
        if (infuences == null) {
            infuences = Collections.emptyList();
        }
        return infuences;
    }

    /**
     * 当前需要初始化的计算字段参与者集合.
     */
    public Collection<AbstractParticipant> getNeed() {
        if (need == null) {
            need = Collections.emptyList();
        }
        return need;
    }

    /**
     * 获取候选人.
     */
    public Map<IEntityClass, HashSet<AbstractParticipant>> getCandidate() {
        if (candidate == null) {
            candidate = new HashMap<>();
        }
        return candidate;
    }

    /**
     * 建造者.
     */
    public static class Builder {
        private String code;
        private Collection<AbstractParticipant> all;
        private Collection<Infuence> infuences;
        private Collection<AbstractParticipant> need;
        private Map<IEntityClass, HashSet<AbstractParticipant>> candidate;
        private Collection<AbstractParticipant> done;
        private Collection<AbstractParticipant> run;

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withAll(Collection<AbstractParticipant> all) {
            this.all = all;
            return this;
        }

        public Builder withInfuences(Collection<Infuence> infuences) {
            this.infuences = infuences;
            return this;
        }

        public Builder withNeed(Collection<AbstractParticipant> need) {
            this.need = need;
            return this;
        }

        public Builder withRun(Collection<AbstractParticipant> run) {
            this.run = run;
            return this;
        }

        public Builder withDone(Collection<AbstractParticipant> done) {
            this.done = done;
            return this;
        }

        public Builder withCandidate(Map<IEntityClass, HashSet<AbstractParticipant>> candidate) {
            this.candidate = candidate;
            return this;
        }

        public static Builder anEmptyBuilder() {
            return new Builder();
        }

        /**
         * 建造者.
         */
        public InitCalculationInfo build() {
            InitCalculationInfo initCalculationInfo = new InitCalculationInfo();
            initCalculationInfo.all = this.all;
            initCalculationInfo.code = this.code;
            initCalculationInfo.candidate = this.candidate;
            initCalculationInfo.infuences = this.infuences;
            initCalculationInfo.need = this.need;
            initCalculationInfo.run = this.run;
            initCalculationInfo.done = this.done;
            initCalculationInfo.initFlag = true;
            initCalculationInfo.skip = new HashSet<>(this.all);
            initCalculationInfo.skip.removeAll(this.need);
            return initCalculationInfo;
        }
    }


}
