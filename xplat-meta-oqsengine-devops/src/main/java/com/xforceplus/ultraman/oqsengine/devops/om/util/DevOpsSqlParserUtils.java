package com.xforceplus.ultraman.oqsengine.devops.om.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.redisson.transaction.operation.set.AddOperation;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @copyright： 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 6:20 PM
 * @description:
 * @history:
 */
public class DevOpsSqlParserUtils {


    public static Conditions parseWhereSql(String whereSql) throws JSQLParserException {
//        String fakeSql = "select 1 from temp where " + whereSql;

        String fakeSql = "select 1 from temp where ((f1 > 0 and f2 < 1) and (f3 > 1 and f4 < 5)) or (f5 > 1)";

        Select selectStatement = (Select) CCJSqlParserUtil.parse(fakeSql);
        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        Expression exprWhere = plainSelect.getWhere();
        GroupByElement groupBy = plainSelect.getGroupBy();
        List<OrderByElement> orderBy = plainSelect.getOrderByElements();

        exprWhere.accept(new ExpressionVisitorAdapter() {
            @Override
            protected void visitBinaryExpression(BinaryExpression expr) {
                if (expr instanceof ComparisonOperator) {
                    System.out.println("1 - " + expr.getLeftExpression().toString() + " === " + expr.getRightExpression().toString());
                } else if(expr instanceof OrExpression) {
                    System.out.println("2 - " + expr.getLeftExpression().toString() + " === " + expr.getRightExpression().toString());
                } else if(expr instanceof AndExpression) {
                    System.out.println("3 - " + expr.getLeftExpression().toString() + " === " + expr.getRightExpression().toString());
                }
                super.visitBinaryExpression(expr);
            }
        });

//        groupBy.accept((GroupByElement groupByElement) -> {
//            List<Expression> newExpr = groupByElement.getGroupByExpressions().stream().
//                    map(expr -> {
//                        Column column = null;
//                        String fieldCode = expr.toString();
//                        Optional<IEntityField> optional =
//                                fields.stream().filter(field -> field.name().equals(fieldCode)).findAny();
//                        if (optional.isPresent()) {
//                            String sn = String.format("jsonfields.%s", toStorageName(optional.get()));
//                            segmentSqls.add(sn);
//                            column = new Column(sn);
//                        } else {
//                            if ("rowid".equals(fieldCode)) {
//
//                            } else if ("id".equals(fieldCode)) {
//
//                            }
//                        }
//                        return column == null ? expr : column;
//                    }).collect(Collectors.toList());
//            groupByElement.setGroupByExpressions(newExpr);
//        });
//
//        List<Map> sortFields = Lists.newArrayList();
//        orderBy.stream().forEach(order ->
//                order.accept((OrderByElement orderByElement) -> {
//                            fields.stream()
//                                    .filter(field -> field.name().equals(orderByElement.getExpression().toString())).findAny().ifPresent(field -> {
//                                String sn = String.format("jsonfields.%s", toStorageName(field));
//                                if(segmentSqls.contains(sn)) {
//                                    segmentSqls.remove(sn);
//                                }
//                                segmentSqls.add(String.format("%s AS sort%s", sn, sortFields.size()));
//                                orderByElement.setExpression(new Column("sort" + sortFields.size()));
//                            });
//                        })
//        );
//
//        String whereSql = plainSelect.getWhere().toString() + " " + plainSelect.getGroupBy().toString() + " ORDER BY " + plainSelect.getOrderByElements().stream().map(oe -> oe.toString()).collect(Collectors.joining(","));
//        if (!StringUtils.isEmpty(whereSql)) {
//            whereSql = SqlKeywordDefine.AND + " " + whereSql;
//        }
//        String selectSegmentSql = segmentSqls.stream().distinct().collect(Collectors.joining(","));
//        if(!StringUtils.isEmpty(selectSegmentSql)) {
//            selectSegmentSql = " ," + selectSegmentSql;
//        }
//
//        Collection<IEntity> resultEntitys = entitySearchService
//                .selectBySql(selectSegmentSql, whereSql, entityClass,
//                        Page.newSinglePage(10));

        return null;
    }

    public static void main(String[] args) throws JSQLParserException {
        System.out.println(parseWhereSql(""));
    }
}
