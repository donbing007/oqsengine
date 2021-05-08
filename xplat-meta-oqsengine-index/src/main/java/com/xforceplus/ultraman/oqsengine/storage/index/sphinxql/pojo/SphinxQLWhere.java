package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import java.util.LinkedList;
import java.util.List;

/**
 * sphinx ql where 查询条件.
 *
 * @author dongbin
 * @version 0.1 2021/04/09 15:21
 * @since 1.8
 */
public class SphinxQLWhere {
    private List<Long> filterIds;
    private long commitId;
    private List<String> match;
    private List<String> attrfilter;
    private IEntityClass entityClass;

    public SphinxQLWhere() {
        match = new LinkedList<>();
        attrfilter = new LinkedList<>();
    }

    /**
     * 过滤条件数量.
     *
     * @return 数量.
     */
    public int attrFilterSize() {
        if (attrfilter == null) {
            return 0;
        }
        return attrfilter.size();
    }

    /**
     * 全文匹配条件数量.
     *
     * @return 数量.
     */
    public int matchSize() {
        if (match == null) {
            return 0;
        }
        return match.size();
    }

    /**
     * 过滤id数量.
     *
     * @return 数量.
     */
    public int filterIdSize() {
        if (filterIds == null) {
            return 0;
        }
        return filterIds.size();
    }

    /**
     * 增加一个过滤id.
     *
     * @param id 对象标识.
     * @return 本身实例.
     */
    public SphinxQLWhere addFilterId(long id) {
        if (filterIds == null) {
            filterIds = new LinkedList<>();
        }
        filterIds.add(id);
        return this;
    }

    /**
     * 增加一个全文匹配条件.
     *
     * @param condition 条件.
     * @return 本身实例.
     */
    public SphinxQLWhere addMatch(String condition) {
        if (match == null) {
            match = new LinkedList<>();
        }
        match.add(condition);
        return this;
    }

    /**
     * 增加一个属性过滤条件.
     *
     * @param condition 条件.
     * @return 本身实例.
     */
    public SphinxQLWhere addAttrFilter(String condition) {
        if (attrfilter == null) {
            attrfilter = new LinkedList<>();
        }
        attrfilter.add(condition);
        return this;
    }

    /**
     * 增加附加的条件,只会处理条件过滤和全文匹配.
     *
     * @param where 条件.
     * @param and true 以 AND 连接, false 以 OR 连接.
     * @return 当前实例.
     */
    public SphinxQLWhere addWhere(SphinxQLWhere where, boolean and) {
        boolean attrClose = false;
        boolean matchClose = false;
        if (and) {
            if (this.attrfilter.size() > 0 && where.attrFilterSize() > 0) {
                this.attrfilter.add(0, "(");
                this.attrfilter.add(") ");
                this.attrfilter.add(SqlKeywordDefine.AND);
                this.attrfilter.add(" ");
                attrClose = true;
            }

            if (this.match.size() > 0 && where.matchSize() > 0) {
                this.match.add(0, "(");
                this.match.add(") ");
                matchClose = true;
            }

        } else {

            if (this.attrfilter.size() > 0 && where.attrFilterSize() > 0) {
                this.attrfilter.add(0, "(");
                this.attrfilter.add(") ");
                this.attrfilter.add(SqlKeywordDefine.OR);
                this.attrfilter.add(" ");
                attrClose = true;
            }

            if (this.match.size() > 0 && where.matchSize() > 0) {
                this.match.add(0, "(");
                this.match.add(") | ");
                matchClose = true;
            }

        }

        if (attrClose) {
            this.attrfilter.add("(");
        }
        this.attrfilter.addAll(where.attrfilter);
        if (attrClose) {
            this.attrfilter.add(")");
        }

        if (matchClose) {
            this.match.add("(");
        }
        this.match.addAll(where.match);
        if (matchClose) {
            this.match.add(")");
        }
        return this;
    }

    public SphinxQLWhere setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    public SphinxQLWhere setCommitId(long commitId) {
        this.commitId = commitId;
        return this;
    }

    /**
     * 生成SphinxQL格式的Where子句.
     *
     * @return where子句.
     */
    public String toString() {
        StringBuilder buff = new StringBuilder();
        // commitid <
        if (commitId > 0) {
            buff.append(FieldDefine.COMMITID).append(" < ").append(commitId);
        }

        // id not in()
        if (filterIds != null && !filterIds.isEmpty()) {
            if (buff.length() > 0) {
                buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
            }
            buff.append(FieldDefine.ID).append(" NOT IN (");
            filterIds.forEach(id -> {
                buff.append(id).append(',');
            });
            buff.deleteCharAt(buff.length() - 1).append(')');
        }

        if (attrfilter != null && !attrfilter.isEmpty()) {
            if (buff.length() > 0) {
                buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
            }

            buff.append('(');
            attrfilter.forEach(c -> buff.append(c));
            buff.append(')');
        }

        if (match != null && !match.isEmpty()) {
            if (buff.length() > 0) {
                buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
            }

            buff.append("MATCH('(");

            match.forEach(c -> {
                buff.append(c);
            });

            buff.append(")");

            if (entityClass != null) {
                buff.append(" (@").append(FieldDefine.ENTITYCLASSF).append(" =").append(entityClass.id()).append(')');
            }

            buff.append("')");

        } else {

            if (entityClass != null) {
                if (buff.length() > 0) {
                    buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
                }
                buff.append("MATCH('@").append(FieldDefine.ENTITYCLASSF).append(" =").append(entityClass.id())
                    .append("')");
            }

        }

        return buff.toString();
    }
}
