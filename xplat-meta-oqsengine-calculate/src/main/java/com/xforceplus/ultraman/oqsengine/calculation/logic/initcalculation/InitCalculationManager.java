package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计算字段初始化管理者.
 *
 * @version 0.1 2021/11/19 11:11
 * @Auther weikai
 * @since 1.8
 */
public interface InitCalculationManager {
    /**
     * 将符合条件的entityClass转换成参与者.
     */
    public Collection<AbstractParticipant> getParticipant(Collection<IEntityClass> entityClasses);


    /**
     * 参与者构建出依赖树.
     */
    public List<Infuence> generateInfluence(Collection<AbstractParticipant> abstractParticipants);


    /**
     * 筛选本次需要初始化的参与者.
     */
    public Set<AbstractParticipant> getNeedInitParticipant(Collection<AbstractParticipant> abstractParticipants);

    /**
     * 通过appid-version生成Init信息.
     */
    public InitCalculationInfo generateAppInfo(String code);

    /**
     * 应用初始化是否完成.
     */
    public boolean isComplete(InitCalculationInfo initCalculationInfo);

    /**
     * 选举候选初始化节点.
     */
    public Map<IEntityClass, HashSet<AbstractParticipant>> voteCandidate(InitCalculationInfo initCalculationInfo);

    /**
     * 生成此次初始化节点.
     */
    public Collection<AbstractParticipant> voteRun(InitCalculationInfo initCalculationInfo);

    /**
     * 将run池中所有参与者转换成entityClass组.
     * 例如run池中含有A1,A2,B1,B2,C1,C2参与者，其中B依赖A，C独立，会分为两组可并发初始化.
     * [A,B], [C]
     * 同组内并发初始化，不同组需等前组完成.
     */
    public  ArrayList<Map<IEntityClass, Collection<InitCalculationParticipant>>> sortRun(Collection<AbstractParticipant> abstractParticipants, InitCalculationInfo initCalculationInfo);

    /**
     * 如果当前app已经在队列中，无需加入.否则返回一个需要初始化的字段列表.
     */
    public Either<String, List<IEntityField>> initAppCalculations(String appCode);
}
