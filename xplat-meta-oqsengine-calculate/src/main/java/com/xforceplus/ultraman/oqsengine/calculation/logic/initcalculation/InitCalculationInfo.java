package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
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
    private Collection<Participant> all;

    /**
     * 当前应用计算字段引用树.
     */
    private Collection<Infuence> infuences;

    /**
     * 当前应用未被初始化的计算字段参与者.
     */
    private Collection<Participant> need;

    /**
     * 当前应用无需初始化的计算字段参与者.
     */
    private Collection<Participant> skip;


    /**
     * 当前应用本次初始化的计算字段参与者.
     */
    private Collection<Participant> run;

    /**
     * 当前应用已经完成初始化的计算字段参与者.
     */
    private Collection<Participant> done;

    /**
     * 当前应用初始化失败的计算字段参与者.
     */
    private Collection<Participant> failed;

    /**
     * 当前应用计算字段参与者初始化候选人.
     */
    private Map<IEntityClass, HashSet<Participant>> candidate;


    public Collection<Participant> getDone() {
        return done;
    }

    public void setDone(Collection<Participant> done) {
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

    public void setAll(Collection<Participant> all) {
        this.all = all;
    }

    public void setInfuences(Collection<Infuence> infuences) {
        this.infuences = infuences;
    }

    public void setNeed(Collection<Participant> need) {
        this.need = need;
    }

    public void setCandidate(Map<IEntityClass, HashSet<Participant>> candidate) {
        this.candidate = candidate;
    }

    /**
     * 获取run.
     */
    public Collection<Participant> getRun() {
        if (run == null) {
            run = new ArrayList<>();
        }
        return run;
    }

    public Collection<Participant> getFailed() {
        return failed;
    }

    public void setFailed(Collection<Participant> failed) {
        this.failed = failed;
    }

    public void setRun(Collection<Participant> run) {
        this.run = run;
    }

    public void setInitFlag(boolean initFlag) {
        this.initFlag = initFlag;
    }

    public boolean isInitFlag() {
        return initFlag;
    }

    public Collection<Participant> getSkip() {
        return skip;
    }

    public String getCode() {
        return code;
    }

    /**
     * get all.
     */
    public Collection<Participant> getAll() {
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
    public Collection<Participant> getNeed() {
        if (need == null) {
            need = Collections.emptyList();
        }
        return need;
    }

    /**
     * 获取候选人.
     */
    public Map<IEntityClass, HashSet<Participant>> getCandidate() {
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
        private Collection<Participant> all;
        private Collection<Infuence> infuences;
        private Collection<Participant> need;
        private Map<IEntityClass, HashSet<Participant>> candidate;
        private Collection<Participant> done;
        private Collection<Participant> run;

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withAll(Collection<Participant> all) {
            this.all = all;
            return this;
        }

        public Builder withInfuences(Collection<Infuence> infuences) {
            this.infuences = infuences;
            return this;
        }

        public Builder withNeed(Collection<Participant> need) {
            this.need = need;
            return this;
        }

        public Builder withRun(Collection<Participant> run) {
            this.run = run;
            return this;
        }

        public Builder withDone(Collection<Participant> done) {
            this.done = done;
            return this;
        }

        public Builder withCandidate(Map<IEntityClass, HashSet<Participant>> candidate) {
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
