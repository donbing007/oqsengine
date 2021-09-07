package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;


import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.MetaParseTree;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private CacheExecutor cacheExecutor;

    @Resource
    private MetaManager metaManager;

    @Resource
    private ParseTree parseTree;

    /**
     * 内存暂存解析树列表.
     */
    private ConcurrentHashMap<Long, ParseTree> parseTrees;

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
            parseTree = reBuild(entityClassId, fieldId, profileCode);
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
        if (parseTrees.size() > 0) {
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
            if (entityField.isPresent()) {
                ParseTree parseTree = new MetaParseTree();

            }
        }
        return null;
    }

}
