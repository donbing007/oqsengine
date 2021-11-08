package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;


import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task.AggregationTaskCoordinator;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.MetaParseTree;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 元数据聚合解析器.
 *
 * @className: MetaAggregationParse
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse
 * @author: wangzheng
 * @date: 2021/8/30 12:04
 */
public class MetaAggregationParse implements AggregationParse {

    final Logger logger = LoggerFactory.getLogger(MetaAggregationParse.class);

    @Resource
    private MetaManager metaManager;

    @Resource
    private AggregationTaskCoordinator aggregationTaskCoordinator;

    /**
     * 内存暂存解析树列表.
     */
    private ConcurrentHashMap<Long, ParseTree> parseTrees;


    /**
     *  内存暂存被聚合字段id集合.
     */
    private Set<Long> aggFieldIds;

    private List<IEntityClass> entityClasses;

    public List<IEntityClass> getEntityClasses() {
        return entityClasses;
    }

    public void setEntityClasses(List<IEntityClass> entityClasses) {
        this.entityClasses = entityClasses;
    }

    public ConcurrentHashMap<Long, ParseTree> getParseTrees() {
        return parseTrees;
    }

    public void setParseTrees(ConcurrentHashMap<Long, ParseTree> parseTrees) {
        this.parseTrees = parseTrees;
    }

    public MetaAggregationParse() {
        this.parseTrees = new ConcurrentHashMap<>();
        this.aggFieldIds = new HashSet<>();
    }

    public MetaAggregationParse(ConcurrentHashMap<Long, ParseTree> parseTrees) {
        this.parseTrees = parseTrees;
    }

    @Override
    public ParseTree find(Long entityClassId, Long fieldId, String profileCode) {
        ParseTree parseTree = parseTrees.get(fieldId);
        if (parseTree == null) {
            logger.warn("parseTree is empty!");
            /*parseTree = reBuild(entityClassId, fieldId, profileCode);*/
        }
        return parseTree;
    }

    @Override
    public List<ParseTree> find(Long entityClassId, String profileCode) {
        List<ParseTree> findTrees = new ArrayList<>();
        Optional<IEntityClass> entityClass = metaManager.load(entityClassId, profileCode);
        if (entityClass.isPresent()) {
            entityClass.get().fields().forEach(f -> {
                ParseTree parseTree = parseTrees.get(f.id());
                if (parseTree != null) {
                    findTrees.add(parseTree);
                }
            });
        }
        return findTrees;
    }

    @Override
    public void appendTree(ParseTree parseTree) {
        if (parseTrees.size() == 0) {
            parseTrees.put(parseTree.root().getEntityField().id(), parseTree);
        } else {
            if (parseTrees.containsKey(parseTree.root().getEntityField().id())) {
                int version = parseTrees.get(parseTree.root().getEntityField().id()).root().getVersion();
                if (version < parseTree.root().getVersion()) {
                    parseTrees.replace(parseTree.root().getEntityField().id(), parseTree);
                }
            } else {
                parseTrees.put(parseTree.root().getEntityField().id(), parseTree);
            }
        }

    }

    @Override
    public void builder(String appId, int version, List<IEntityClass> entityClasses) {
        MetaParseTree parseTree = new MetaParseTree(appId + "-" + version);
        this.entityClasses = entityClasses;
        entityClasses.forEach(entityClass -> {
            Collection<IEntityField> entityFields = entityClass.fields();
            entityFields.forEach(f -> {
                if (f.calculationType().equals(CalculationType.AGGREGATION)) {
                    Aggregation aggregation = (Aggregation) f.config().getCalculation();
                    long classId = aggregation.getClassId();
                    long fieldId = aggregation.getFieldId();
                    Optional<IEntityClass> entityClassOp = entityByField(classId, fieldId, entityClasses);
                    if (entityClassOp.isPresent()) {
                        IEntityClass aggEntityClass = entityClassOp.get();
                        Optional<IEntityField> entityFieldOp = aggEntityClass.field(fieldId);
                        if (entityFieldOp.isPresent()) {
                            ParseTree pt = parseTree.buildTree(entityClasses, entityClass, f, aggEntityClass, entityFieldOp.get());
                            appendTree(pt);
                        }
                    }
                }
            });
        });
    }

    @Override
    public void builderTrees(String appId, int version, List<IEntityClass> entityClasses) {
        List<PTNode> nodes = new ArrayList<>();
        entityClasses.forEach(entityClass -> {
            Collection<IEntityField> entityFields = entityClass.fields().stream()
                    .filter(f -> f.calculationType().equals(CalculationType.AGGREGATION)).collect(Collectors.toList());
            entityFields.forEach(f -> {
                if (f.calculationType().equals(CalculationType.AGGREGATION)) {
                    Aggregation aggregation = (Aggregation) f.config().getCalculation();
                    Optional<IEntityClass> entityClassOp = entityByField(aggregation.getClassId(), aggregation.getFieldId(), entityClasses);
                    if (entityClassOp.isPresent()) {
                        Optional<IEntityField> entityFieldOp = entityClassOp.get().field(aggregation.getFieldId());
                        if (entityFieldOp.isPresent()) {
                            if (entityFieldOp.get().calculationType().equals(CalculationType.AGGREGATION)) {
                                //放置其他节点
                                PTNode ptNode = new PTNode();
                                ptNode.setRootFlag(false);
                                ptNode.setEntityField(f);
                                ptNode.setEntityClass(entityClass);
                                ptNode.setConditions(aggregation.getConditions());
                                ptNode.setAggregationType(aggregation.getAggregationType());
                                ptNode.setAggEntityClass(entityClassOp.get());
                                ptNode.setAggEntityField(entityFieldOp.get());
                                nodes.add(ptNode);
                            } else {
                                //放置root节点
                                PTNode ptNode = new PTNode();
                                ptNode.setRootFlag(true);
                                ptNode.setEntityField(f);
                                ptNode.setEntityClass(entityClass);
                                ptNode.setConditions(aggregation.getConditions());
                                ptNode.setAggregationType(aggregation.getAggregationType());
                                ptNode.setAggEntityClass(entityClassOp.get());
                                ptNode.setAggEntityField(entityFieldOp.get());
                                nodes.add(ptNode);
                            }
                        }
                    }
                }
            });
        });
        List<ParseTree> parseTrees = new ArrayList<>();
        if (nodes.size() > 0) {
            parseTrees = MetaParseTree.generateMultipleTress(nodes);
            parseTrees.forEach(pt -> {
                appendTree(pt);
            });
            Optional<Set<Long>> longs = parseFieldIds(parseTrees);
            if (longs.isPresent()) {
                aggFieldIds.addAll(longs.get());
            }
        }
        parseTrees.forEach(ParseTree::toSimpleTree);
        aggregationTaskCoordinator.addInitAppInfo(appId + "-" + version, parseTrees);
    }

    /**
     * 重新构建字段解析树.
     *
     * @param entityClassId 应用id
     * @param fieldId 字段id
     * @return parseTree.
     */
    private ParseTree reBuild(Long entityClassId, Long fieldId, String profileCode) {
        Optional<IEntityClass> entityClass = metaManager.load(entityClassId, profileCode);
        if (entityClass.isPresent()) {
            Optional<IEntityField> entityField = entityClass.get().field(fieldId);
            /*if (entityField.isPresent()) {
                ParseTree pt = parseTree.buildTree(entityClasses, entityClass.get(), entityField.get(),
                        aggEntityClass, entityFieldOp.get());
            }*/
        }
        return null;
    }

    /**
     * 根据对象id和字段id找到匹配的EntityClass-支持租户.
     *
     * @param entityClassId 对象id.
     * @param fieldId 字段id.
     * @param entityList 元数据.
     */
    private Optional<IEntityClass> entityByField(long entityClassId, long fieldId, List<IEntityClass> entityList) {
        if (entityList != null && entityList.size() > 0) {
            List<IEntityClass> entityClasses = entityList.stream().filter(s -> s.id() == entityClassId)
                    .collect(Collectors.toList());
            if (entityClasses.size() >= 0) {
                if (entityClasses.size() == 1) {
                    return Optional.of(entityClasses.get(0));
                }
                for (IEntityClass entityClass : entityClasses) {
                    Collection<IEntityField> entityFields = entityClass.fields();
                    for (IEntityField entityField : entityFields) {
                        if (entityField.id() == fieldId) {
                            return Optional.of(entityClass);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }


    /**
     * 根据聚合树生成被聚合字段id集合.
     *
     * @param trees 聚合树集合.
     * @return 被聚合字段id集合.
     */
    private Optional<Set<Long>> parseFieldIds(List<ParseTree> trees) {
        return Optional.of(trees.stream().flatMap(l -> l.toList().stream().map(p -> p.getAggEntityField().id())).collect(Collectors.toSet()));
    }

    @Override
    public boolean checkIsAggField(Long id) {
        return aggFieldIds.contains(id);
    }
}
