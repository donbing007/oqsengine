package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;


import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    private ParseTree parseTree;

    /**
     * 内存暂存解析树列表.
     */
    private ConcurrentHashMap<Long, ParseTree> parseTrees;

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
    }

    public MetaAggregationParse(ConcurrentHashMap<Long, ParseTree> parseTrees) {
        this.parseTrees = parseTrees;
    }

    @Override
    public ParseTree find(Long entityClassId, Long fieldId, String profileCode) {
        ParseTree parseTree = parseTrees.get(fieldId);
        if (parseTree == null) {
            logger.warn("parseTree is empty!");
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
}
