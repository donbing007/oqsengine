package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AbstractCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import io.vavr.control.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算字段初始化管理者.
 *
 * @version 0.1 2021/11/22 11:08
 * @Auther weikai
 * @since 1.8
 */
public class DefaultInitCalculationManager implements InitCalculationManager {
    private final Logger logger = LoggerFactory.getLogger(DefaultInitCalculationManager.class);

    @Resource
    private MetaManager metaManager;

    @Resource
    private KeyValueStorage kv;

    @Resource
    private SerializeStrategy serializeStrategy;

    @Resource
    private CalculationInitLogic calculationInitLogic;

    @Resource
    private ResourceLocker locker;

    private ExecutorService worker;

    private final List<CalculationType> participantTypes;

    private static final String SUCCESS = "success";

    private static final String FAILED = "failed";

    private static final String INITING = "-initing";

    private static final String INIT_FLAG = "calculationInitField-";

    /**
     * 参与者候选人年龄上限，超过阈值没被初始化认为初始化失败.
     */
    private static final int AGE_LIMIT = 1000;

    /**
     * 筛选公式、聚合、自增编号的字段类型.
     */
    public DefaultInitCalculationManager() {
        participantTypes = new ArrayList<>();
        participantTypes.add(CalculationType.FORMULA);
        participantTypes.add(CalculationType.AGGREGATION);
        participantTypes.add(CalculationType.AUTO_FILL);
    }

    public void setWorker(ExecutorService worker) {
        this.worker = worker;
    }

    @Override
    public Collection<AbstractParticipant> getParticipant(Collection<IEntityClass> entityClasses) {
        Set<AbstractParticipant> abstractParticipants = new HashSet<>();
        entityClasses.forEach(entityClass -> {
            entityClass.fields().forEach(entityField -> {
                if (participantTypes.contains(entityField.calculationType())) {
                    AbstractParticipant build = build(entityClass, entityField, entityField.calculationType());
                    if (build != null) {
                        abstractParticipants.add(build);
                    }
                }
            });
        });
        return abstractParticipants;
    }

    @Override
    public List<Infuence> generateInfluence(Collection<AbstractParticipant> abstractParticipants) {
        List<Infuence> infuences = new ArrayList<>();

        // 获取根节点
        abstractParticipants.forEach(participant -> {
            if (isRootNode(participant)) {
                infuences.add(new Infuence(null, participant, null));
            }
        });

        // 添加叶子节点
        infuences.forEach(infuence ->
                infuence.scan((parentParticipant, participant, infuenceInner) -> {

                    abstractParticipants.forEach(p -> {
                        InitCalculationParticipant pt = (InitCalculationParticipant) p;
                        if (pt.getSourceField().contains(participant.getField()) && canImpact(pt, participant)) {
                            boolean needImpact = true;
                            for (Infuence in : infuences) {
                                // 去除别的树中重复的参与者，根据当前层数大小决定
                                if (in.contains(pt)) {
                                    if (pt.getLevel() - ((InitCalculationParticipant) participant).getLevel() <= 1) {
                                        needImpact = false;
                                        in.remove(pt.getPre(), pt);
                                        infuenceInner.impact(participant, pt);
                                        break;
                                    }
                                }
                            }
                            if (needImpact) {
                                infuenceInner.impact(participant, pt);
                            }
                        }
                    });

                    return InfuenceConsumer.Action.CONTINUE;
                }));

        return infuences;
    }

    @Override
    public Set<AbstractParticipant> getNeedInitParticipant(Collection<AbstractParticipant> abstractParticipants) {
        Set<AbstractParticipant> needs = new HashSet<>();
        Map<String, byte[]> needsInitMap = new HashMap<>();
        abstractParticipants.forEach(participant -> {
            if (needInit(participant.getField().id())) {
                Queue<AbstractParticipant> queue = new LinkedList<>();
                queue.add(participant);
                while (!queue.isEmpty()) {
                    AbstractParticipant poll = queue.poll();
                    needs.add(poll);
                    needsInitMap.put(INIT_FLAG + poll.getField().id(), serializeStrategy.serialize(CalculationInitStatus.UN_INIT));
                    queue.addAll(poll.getNextParticipants());
                }
            }
        });
        kv.save(needsInitMap.entrySet());

        return needs;
    }

    @Override
    public InitCalculationInfo generateAppInfo(String code) {

        // TODO 需要获取redis中元数据配置.
        List<IEntityClass> entityClasses = new ArrayList<>();
        Optional<IEntityClass> load = metaManager.load(Long.MAX_VALUE - 4, null);
        if (load.isPresent()) {
            entityClasses.add(load.get());
        }
        Collection<AbstractParticipant> all = getParticipant(entityClasses);
        List<Infuence> infuences = generateInfluence(all);
        Set<AbstractParticipant> need = getNeedInitParticipant(all);
        return InitCalculationInfo.Builder.anEmptyBuilder()
                .withCode(code)
                .withAll(all)
                .withCandidate(new HashMap<>())
                .withNeed(need)
                .withInfuences(infuences).build();
    }

    @Override
    public boolean isComplete(InitCalculationInfo initCalculationInfo) {
        initCalculationInfo.growUp();
        if (initCalculationInfo.getAge() >= AGE_LIMIT) {
            throw new CalculationException(initCalculationInfo.getCode() + "init failed , cause of exceed age limit " + AGE_LIMIT + initCalculationInfo);
        }
        return initCalculationInfo.getNeed().isEmpty() && initCalculationInfo.getCandidate().isEmpty() && initCalculationInfo.getRun().isEmpty();
    }

    @Override
    public Map<IEntityClass, HashSet<AbstractParticipant>> voteCandidate(InitCalculationInfo initCalculationInfo) {
        Map<IEntityClass, HashSet<AbstractParticipant>> candidate = initCalculationInfo.getCandidate();
        if (initCalculationInfo.isInitFlag()) {
            initCalculationInfo.getInfuences().forEach(infuence ->
                    infuence.scan((parentParticipant, participant, infuenceInner) -> {
                        if (candidate.containsKey(participant.getEntityClass())) {
                            candidate.get(participant.getEntityClass()).add(participant);
                        } else {
                            candidate.put(participant.getEntityClass(), (HashSet<AbstractParticipant>) Stream.of(participant).collect(Collectors.toSet()));
                        }
                        initCalculationInfo.getNeed().remove(participant);
                        return InfuenceConsumer.Action.OVER;
                    }));
            initCalculationInfo.setInitFlag(false);
        } else {
            // 历史候选人年龄加一
            if (candidate.size() > 0) {
                candidate.values().forEach(participants ->
                        participants.forEach(participant -> ((InitCalculationParticipant) participant).growUp()));
            }

            // 候选池中计算字段参与者
            Collection<AbstractParticipant> historyAbstractParticipant = candidate.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            // 新增候选人
            historyAbstractParticipant.forEach(participant -> {
                participant.getNextParticipants().forEach(participant1 -> {
                    if (candidate.containsKey(participant1.getEntityClass())) {
                        candidate.get(participant1.getEntityClass()).add(participant1);

                    } else {
                        candidate.put(participant1.getEntityClass(), (HashSet<AbstractParticipant>) Stream.of(participant1).collect(Collectors.toSet()));
                    }
                    initCalculationInfo.getNeed().remove(participant1);
                });

            });
            // 移除上次run池中初始化完成的计算字段参与者
            initCalculationInfo.getRun().forEach(new Consumer<AbstractParticipant>() {
                @Override
                public void accept(AbstractParticipant abstractParticipant) {
                    candidate.get(abstractParticipant.getEntityClass()).remove(abstractParticipant);
                    if (candidate.get(abstractParticipant.getEntityClass()).isEmpty()) {
                        candidate.remove(abstractParticipant.getEntityClass());
                    }
                }
            });
            initCalculationInfo.getRun().clear();
        }
        return candidate;
    }

    @Override
    public Collection<AbstractParticipant> voteRun(InitCalculationInfo initCalculationInfo) {
        if (initCalculationInfo.getCandidate() == null || initCalculationInfo.getCandidate().isEmpty()) {
            return Collections.emptyList();
        }

        for (IEntityClass next : initCalculationInfo.getCandidate().keySet()) {
            if (canVoteRun(next, initCalculationInfo)) {
                initCalculationInfo.getRun().addAll(initCalculationInfo.getCandidate().get(next));
            }
        }

        return initCalculationInfo.getRun();
    }

    @Override
    public ArrayList<Map<IEntityClass, Collection<InitCalculationParticipant>>> sortRun(Collection<AbstractParticipant> abstractParticipants, InitCalculationInfo initCalculationInfo) {
        if (abstractParticipants.isEmpty()) {
            return new ArrayList<>();
        }
        Map<IEntityClass, Collection<InitCalculationParticipant>> map = new HashMap<>();
        // 相同entityClass的计算字段按照年龄降序排序.
        abstractParticipants.forEach(participant -> {
            if (map.containsKey(participant.getEntityClass())) {
                map.get(participant.getEntityClass()).add((InitCalculationParticipant) participant);
            } else {
                TreeSet<InitCalculationParticipant> set = new TreeSet<>();
                set.add((InitCalculationParticipant) participant);
                map.put(participant.getEntityClass(), set);
            }
        });

        ArrayList<HashSet<IEntityClass>> hashSets = individualClasses(null, map, new ArrayList<>());
        ArrayList<Map<IEntityClass, Collection<InitCalculationParticipant>>> run = new ArrayList<>(hashSets.size());
        // 转换成kv，participant按照entityClass分类，按照年龄排序.
        for (HashSet<IEntityClass> hashSet : hashSets) {
            Map<IEntityClass, Collection<InitCalculationParticipant>> hashMap = new HashMap<>();
            hashSet.forEach(entityClass -> {
                List<InitCalculationParticipant> collect = map.get(entityClass).stream().filter(initCalculationParticipant ->
                        !initCalculationInfo.getSkip().contains(initCalculationParticipant)).collect(Collectors.toList());
                if (collect.size() > 0) {
                    hashMap.put(entityClass, collect);
                }
            });
            if (hashMap.size() > 0) {
                run.add(hashMap);
            }
        }

        // 获取分层的entityClass
        return run;
    }

    @Override
    public Either<String, List<IEntityField>> initAppCalculations(String appCode) {
        try {
            locker.lock(appCode);
            if (kv.exist(appCode + INITING)) {
                return Either.left(String.format("curent app %s is initing now, please wait", appCode));
            } else {
                List<IEntityField> entityFields = generateAppInfo(appCode).getNeed().stream()
                        .map(AbstractParticipant::getField).collect(Collectors.toList());
                worker.submit(new Runner(appCode));
                kv.save(appCode + INITING, serializeStrategy.serialize(1));
                return Either.right(entityFields);
            }
        } finally {
            locker.unlock(appCode);
        }
    }


    /**
     * 解析entityClass依赖顺序.
     */
    private ArrayList<HashSet<IEntityClass>> individualClasses(Collection<IEntityClass> up,
                                                               Map<IEntityClass, Collection<InitCalculationParticipant>> map,
                                                               ArrayList<HashSet<IEntityClass>> individuals) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }

        Set<IEntityClass> all = map.keySet();

        HashSet<IEntityClass> temp = new LinkedHashSet<>();


        // 找出rootEntityClass，例如C依赖B，B依赖A，rootEntityClass为A.
        if (up == null) {
            for (IEntityClass entityClass : all) {
                if (map.get(entityClass).stream().noneMatch(initCalculationParticipant ->
                        map.containsKey(initCalculationParticipant.getSourceEntityClass())
                                && !initCalculationParticipant.getSourceEntityClass().equals(entityClass))) {
                    temp.add(entityClass);
                }
            }
            if (temp.isEmpty()) {
                logger.error("sortRun error");
                return new ArrayList<>();
            }
            individuals.add(temp);
            up = temp;
            // 递归寻找up中entityClass的引用链路
            return individualClasses(up, map, individuals);

        } else {
            // 寻找up的下一层
            Collection<IEntityClass> finalUp = up;
            for (IEntityClass entityClass : all) {
                if (map.get(entityClass).stream().anyMatch(initCalculationParticipant -> (finalUp.contains(initCalculationParticipant.getSourceEntityClass())
                        && !initCalculationParticipant.getEntityClass().equals(initCalculationParticipant.getSourceEntityClass())))) {
                    temp.add(entityClass);
                }
            }

            // 递归出口，up下层无依赖.
            if (temp.isEmpty()) {
                /*
                 * 倒叙去重
                 *      A1               B1
                 *  C1     A2          B2    D1
                 * D2 C2     A3      B3       E2
                 * entityClass执行组为[A,B],[C,D],[D,E]去重后为[A,B],[C],[D,E]，同组可并发执行
                 */
                if (individuals.size() >= 2) {
                    ArrayList<HashSet<IEntityClass>> clone = (ArrayList<HashSet<IEntityClass>>) individuals.clone();
                    for (int size = individuals.size() - 2; size > 0; size--) {
                        for (int j = individuals.size() - 1; j < size; j--) {
                            clone.get(size).removeAll(clone.get(j));
                        }
                    }
                    return clone;
                } else {
                    return individuals;
                }
            } else {
                // 添加下层引用链路的entityClass集合
                individuals.add(temp);
                // up指针指向下一层
                up = temp;
                return individualClasses(up, map, individuals);
            }
        }
    }

    // 判断是否可以加入到叶子节点中.
    private boolean canImpact(AbstractParticipant child, AbstractParticipant father) {
        if (!child.getField().calculationType().equals(CalculationType.FORMULA)
                && !child.getField().calculationType().equals(CalculationType.AUTO_FILL)) {
            return true;
        } else {
            InitCalculationParticipant initCalculationParticipant = (InitCalculationParticipant) child;
            HashSet<IEntityField> fields = new HashSet<>(initCalculationParticipant.getSourceField());
            fields.remove(father.getField());
            if (child.getField().calculationType().equals(CalculationType.FORMULA) || child.getField().calculationType().equals(CalculationType.AUTO_FILL)) {
                return !transitiveDependency(child.getEntityClass(), fields, father.getField());
            }
            return false;
        }
    }

    /**
     * 传递引用的entityClass中存在计算参与者还在needs列表中不添加初始化.
     * A1        B1
     * A2    B2
     * A3
     * 当候选池为 A1 B1 A2 B2 时，判定B暂时也不能选举到run池.
     */
    private boolean canVoteRun(IEntityClass entityClass, InitCalculationInfo initCalculationInfo) {
        Set<IEntityClass> set = transitiveEntityClass(entityClass, initCalculationInfo);

        for (AbstractParticipant abstractParticipant : initCalculationInfo.getNeed()) {
            if (set.contains(abstractParticipant.getEntityClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 得到传递向上依赖的entityClass集合.
     */
    private Set<IEntityClass> transitiveEntityClass(IEntityClass entityClass, InitCalculationInfo initCalculationInfo) {
        Set<IEntityClass> set = new HashSet<>();
        for (AbstractParticipant abstractParticipant : initCalculationInfo.getCandidate().get(entityClass)) {
            AbstractParticipant pre = abstractParticipant;
            while (pre != null) {
                set.add(pre.getEntityClass());
                pre = pre.getPre();
            }
        }
        return set;
    }


    /**
     * 递归查找公式或者自增编号中是否有含有传递依赖关系.
     * A2 = A1 + 普通字段.
     * A3 = A2 + A1.
     * A3应该在第三层中，A3在加入A1的叶子节点中判定为不能加入当前节点.
     *     A1
     *   A2  (A3)
     * A3
     */
    private boolean transitiveDependency(IEntityClass entityClass, Collection<IEntityField> fields, IEntityField source) {
        if (fields.size() == 0) {
            return false;
        } else {
            List<IEntityField> resFields = new ArrayList<>();

            for (IEntityField entityField : fields) {
                List<IEntityField> nextFields = nextFields(entityClass, entityField);
                if (nextFields.contains(source)) {
                    return true;
                } else {
                    resFields.addAll(nextFields);
                }
            }
            fields = null;

            return transitiveDependency(entityClass, resFields, source);
        }
    }


    /**
     * 获取计算字段引用字段列表.
     */
    private List<IEntityField> nextFields(IEntityClass entityClass, IEntityField field) {
        List<IEntityField> fields = new ArrayList<>();
        if (field.config().getCalculation().getCalculationType().equals(CalculationType.AUTO_FILL)) {
            AutoFill calculation = (AutoFill) field.config().getCalculation();
            if (calculation.getArgs() != null) {
                for (String arg : calculation.getArgs()) {
                    if (entityClass.field(arg).isPresent()) {
                        fields.add(entityClass.field(arg).get());
                    }
                }
            }
        }

        if (field.config().getCalculation().getCalculationType().equals(CalculationType.FORMULA)) {
            Formula calculation = (Formula) field.config().getCalculation();
            for (String arg : calculation.getArgs()) {
                if (entityClass.field(arg).isPresent()) {
                    fields.add(entityClass.field(arg).get());
                }
            }
        }

        return fields;
    }

    /**
     * 根节点判定.
     */
    private boolean isRootNode(AbstractParticipant abstractParticipant) {
        InitCalculationParticipant initCalculationParticipant = (InitCalculationParticipant) abstractParticipant;
        AbstractCalculation calculation = initCalculationParticipant.getField().config().getCalculation();

        if (calculation.getCalculationType().equals(CalculationType.AGGREGATION)) {
            Aggregation aggregation = (Aggregation) calculation;
            if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                return true;
            }
            return isStaticField(aggregation.getClassId(), aggregation.getFieldId());
        } else if (calculation.getCalculationType().equals(CalculationType.FORMULA)) {
            IEntityClass entityClass = initCalculationParticipant.getEntityClass();
            Formula formula = (Formula) calculation;
            if (formula.getArgs() == null || formula.getArgs().isEmpty()) {
                return true;
            }
            return formula.getArgs().stream().noneMatch(arg -> {
                if (entityClass.field(arg).isPresent()) {
                    return !isStaticField(entityClass.id(), entityClass.field(arg).get().id());
                }
                return false;
            });
        } else if (calculation.getCalculationType().equals(CalculationType.AUTO_FILL)) {
            IEntityClass entityClass = initCalculationParticipant.getEntityClass();
            AutoFill autoFill = (AutoFill) calculation;
            if (autoFill.getArgs() == null || autoFill.getArgs().isEmpty()) {
                return true;
            }
            return autoFill.getArgs().stream().noneMatch(arg -> {
                if (entityClass.field(arg).isPresent()) {
                    return !isStaticField(entityClass.id(), entityClass.field(arg).get().id());
                }
                return false;
            });
        } else if (calculation.getCalculationType().equals(CalculationType.LOOKUP)) {
            Lookup lookup = (Lookup) calculation;
            return isStaticField(lookup.getClassId(), lookup.getFieldId());
        }
        return false;
    }

    /**
     * 普通字段判定.
     */
    private boolean isStaticField(long entityClassId, long entityFieldId) {
        Optional<IEntityClass> entityClassOptional = metaManager.load(entityClassId, null);
        if (entityClassOptional.isPresent()) {
            if (entityClassOptional.get().field(entityFieldId).isPresent()) {
                return entityClassOptional.get().field(entityFieldId).get()
                        .calculationType().equals(CalculationType.STATIC);
            } else {
                logger.warn(String.format("can not find field by fieldId %s in entityClass %s",
                        entityFieldId, entityClassId));
            }
        }
        return false;
    }

    /**
     * 构建参与者.
     */
    private AbstractParticipant build(IEntityClass entityClass, IEntityField entityField, CalculationType calculationType) {
        InitCalculationParticipant.Builder builder = InitCalculationParticipant.Builder.anParticipant().withEntityClass(entityClass).withField(entityField);
        switch (calculationType) {
            case FORMULA:
                Formula formula = (Formula) entityField.config().getCalculation();
                if (formula.getArgs() == null || formula.getArgs().isEmpty()) {
                    return builder.build();
                }
                List<IEntityField> sourceFields = new ArrayList<>();
                formula.getArgs().forEach(s -> {
                    if (entityClass.field(s).isPresent()) {
                        sourceFields.add(entityClass.field(s).get());
                    }
                });
                return builder.withSourceField(sourceFields).withSourceEntityClass(entityClass).build();
            case AGGREGATION:
                Aggregation aggregation = (Aggregation) entityField.config().getCalculation();
                Optional<IEntityClass> entityClassOptional = metaManager.load(aggregation.getClassId(), null);
                if (entityClassOptional.isPresent()) {
                    if (entityClassOptional.get().field(aggregation.getFieldId()).isPresent()) {
                        ArrayList<IEntityField> fields = new ArrayList<>();
                        fields.add(entityClassOptional.get().field(aggregation.getFieldId()).get());
                        return builder.withSourceEntityClass(entityClassOptional.get()).withSourceField(fields).build();
                    } else if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                        return builder.withSourceEntityClass(entityClassOptional.get()).build();
                    } else {
                        logger.error(String.format("can not find entityField %s in entityClass %s", aggregation.getFieldId(), aggregation.getClassId()));
                        throw new CalculationException(String.format(
                                "init calculation error: can not find entityField %s in entityClass %s", aggregation.getFieldId(), aggregation.getClassId()));
                    }
                } else {
                    logger.error(String.format("can not find entityClass %s", aggregation.getClassId()));
                    throw new CalculationException(String.format(
                            "init calculation error: can not find entityClass %s", aggregation.getAggregationType()));
                }
            case AUTO_FILL:
                AutoFill autoFill = (AutoFill) entityField.config().getCalculation();
                if (autoFill.getArgs() == null || autoFill.getArgs().isEmpty()) {
                    return builder.build();
                }
                List<IEntityField> fields = new ArrayList<>();
                autoFill.getArgs().forEach(s -> {
                    if (entityClass.field(s).isPresent()) {
                        fields.add(entityClass.field(s).get());
                    }
                });
                return builder.withSourceField(fields).withSourceEntityClass(entityClass).build();
            default:
                throw new CalculationException(String.format(
                        "init calculation error: not support calculationType %s , can not transfer to InitCalculationParticipant", calculationType.name()));


        }
    }


    /**
     * kv存储中查看当前字段是否已经初始化.
     */
    private boolean needInit(long fieldID) {
        if (!kv.exist(INIT_FLAG + fieldID)) {
            return true;
        }

        if (kv.get(INIT_FLAG + fieldID).isPresent()) {
            byte[] bytes = kv.get(INIT_FLAG + fieldID).get();
            return serializeStrategy.unserialize(bytes, CalculationInitStatus.class).equals(CalculationInitStatus.UN_INIT);
        }

        return false;
    }

    /**
     * 应用初始化任务，每个应用占用一个线程.
     */
    public class Runner implements Callable<InitResultInfo> {
        private final String appCode;

        public Runner(String appCode) {
            this.appCode = appCode;
        }

        @Override
        public InitResultInfo call() throws Exception {
            long begin = System.currentTimeMillis();
            InitResultInfo initResultInfo = new InitResultInfo();
            InitCalculationInfo initCalculationInfo = generateAppInfo(appCode);
            while (true) {
                try {
                    if (isComplete(initCalculationInfo)) {
                        break;
                    }
                    // 选举候选计算参与者.
                    voteCandidate(initCalculationInfo);
                    // 选举本次可执行初始化计算字段参与者.
                    Collection<AbstractParticipant> abstractParticipants = voteRun(initCalculationInfo);
                    // 可执行计算字段参与者分类.
                    ArrayList<Map<IEntityClass, Collection<InitCalculationParticipant>>> run = sortRun(abstractParticipants, initCalculationInfo);

                    if (!run.isEmpty()) {
                        // 执行初始化
                        Map<String, List<InitCalculationParticipant>> accept = calculationInitLogic.accept(run);

                        Map<String, byte[]> done = new HashMap<>();

                        // 记录初始化成功的计算参与者
                        if (accept.get(SUCCESS) != null && !accept.get(SUCCESS).isEmpty()) {
                            List<InitCalculationParticipant> successRes = accept.get(SUCCESS);
                            for (InitCalculationParticipant success : successRes) {
                                if (initResultInfo.getFailedInfo().containsKey(success.getEntityClass().id())) {
                                    initResultInfo.getFailedInfo().get(success.getEntityClass().id()).add(success.getField().id());
                                } else {
                                    initResultInfo.getFailedInfo().put(success.getEntityClass().id(), Stream.of(success.getField().id()).collect(Collectors.toList()));
                                }

                                done.put(INIT_FLAG + success.getField().id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
                            }
                        }

                        // 更新完成初始化的计算字段.
                        kv.save(done.entrySet());

                        // 记录初始化失败的计算参与者
                        if (accept.get(FAILED) != null && !accept.get(FAILED).isEmpty()) {
                            List<InitCalculationParticipant> failedRes = accept.get(FAILED);
                            for (InitCalculationParticipant failedRe : failedRes) {
                                if (initResultInfo.getFailedInfo().containsKey(failedRe.getEntityClass().id())) {
                                    initResultInfo.getFailedInfo().get(failedRe.getEntityClass().id()).add(failedRe.getField().id());
                                } else {
                                    initResultInfo.getFailedInfo().put(failedRe.getEntityClass().id(), Stream.of(failedRe.getField().id()).collect(Collectors.toList()));
                                }
                            }
                            // 有失败的参与者后，终止后续初始化任务
                            throw new CalculationException("init failed when process app " + appCode + ": " + initResultInfo);
                        }
                    }


                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    // 处理因异常未被执行的计算字段参与者
                    for (AbstractParticipant abstractParticipant : initCalculationInfo.getNeed()) {
                        if (initResultInfo.getFailedInfo().containsKey(abstractParticipant.getEntityClass().id())) {
                            initResultInfo.getFailedInfo().get(abstractParticipant.getEntityClass().id()).add(abstractParticipant.getField().id());
                        } else {
                            initResultInfo.getFailedInfo().put(abstractParticipant.getEntityClass().id(), Stream.of(abstractParticipant.getField().id()).collect(Collectors.toList()));
                        }
                    }
                    // 失败后停止初始化，防止引用链路中引用错误数据.
                    break;
                }
            }
            // 记录初始化结果，key是时间，v是初始化结果
            HashMap<Long, InitResultInfo> map = new HashMap<>();
            if (kv.exist(appCode + "-initResLog")) {
                map = serializeStrategy.unserialize(kv.get(appCode + "-initResLog").get(), HashMap.class);
            }
            map.put(begin, initResultInfo);
            kv.save(appCode + "-initResLog", serializeStrategy.serialize(map));
            kv.delete(appCode + INITING);
            return initResultInfo;
        }
    }
}
