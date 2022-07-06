package com.xforceplus.ultraman.oqsengine.calculation.dto.agg;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper.AggregationAttachmentHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin.xu on 07/2022.
 *
 * @since 1.8
 */
public class CollectAttachment {

    private Map<String, Integer> collectElements;

    //  当前水位.
    private int used;

    public CollectAttachment(Map<String, Integer> collectElements) {

        this.collectElements = collectElements;
        this.used = collectElements.size();
    }

    /**
     * 对于本对象的cs操作 add/delete.
     *
     * @param element 字段值.
     * @param isAdd 是否新增,只存在新增和删除两种情况,当该字段值被修改时,需要先进行删除再新增的操作.
     */
    public void compareAndOperation(String element, boolean isAdd, int capacity) {
        collectElements.compute(element, (k, v) -> {
            if (v == null) {
                if (isAdd) {
                    if (used >= capacity) {
                        return null;
                    }
                    used++;
                }
                v = 0;
            }

            v = isAdd ? v + 1 : v - 1;

            //  统计的字段被删除
            if (!isAdd && v == 0) {
                used--;
                return null;
            }
            return v;
        });
    }

    /**
     * 产生IValue.
     *
     * @param entityField   字段类型.
     * @return              值对象.
     */
    public IValue toIValue(IEntityField entityField) {
        String attachment = toAttachment();
        String[] collect = toCollect();

        return new StringsValue(entityField, collect, attachment);
    }

    /**
     * 构建attachment存储值.
     *
     * @return 存储值.
     */
    public String toAttachment() {
        if (collectElements.isEmpty()) {
            return "";
        }

        return collectElements
                    .values()
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(AggregationAttachmentHelper.COLLECT_ATTACHMENT_DIVIDE));
    }

    /**
     * 构建attachment存储值.
     *
     * @return 存储值.
     */
    public String[] toCollect() {
        if (collectElements.isEmpty()) {
            return new String[0];
        }

        return collectElements.keySet().toArray(new String[0]);
    }
}
