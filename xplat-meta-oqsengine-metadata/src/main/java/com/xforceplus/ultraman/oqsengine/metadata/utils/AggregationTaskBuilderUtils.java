package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聚合字段初始化字段任务.
 *
 * @className: AggregationTaskBuilderUtils
 * @package: com.xforceplus.ultraman.oqsengine.metadata.utils
 * @author: wangzheng
 * @date: 2021/8/31 16:24
 */
public class AggregationTaskBuilderUtils {

    public static void buildTask(String appId, int version, List<EntityClassStorage> preStorageList, List<EntityClassStorage> storageList){
        if (storageList != null && storageList.size() > 0) {
            List<EntityField> entityFields = new ArrayList<>();
            storageList.stream().map(s -> entityFields.addAll(s.getFields().stream().filter(f ->
                    f.calculationType().equals(CalculationType.AGGREGATION)
            ).collect(Collectors.toList()))).collect(Collectors.toList());
            if (preStorageList != null && preStorageList.size() > 0) {
                List<EntityField> preEntityFields = new ArrayList<>();
                preStorageList.stream().map(s -> preEntityFields.addAll(s.getFields().stream().filter(f ->
                        f.calculationType().equals(CalculationType.AGGREGATION)
                ).collect(Collectors.toList()))).collect(Collectors.toList());


            } else {

            }
        }
    }
}
