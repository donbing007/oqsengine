package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SPLITTER;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SPLIT_META_LENGTH;
import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType.fromRawType;

/**
 * desc :
 * name : EntityFieldBuildUtils
 *
 * @author : xujia
 * date : 2020/11/19
 * @since : 1.8
 */
public class EntityFieldBuildUtils {

    //  meta -> Map<String, IEntityField>
    public static Map<String, IEntityField> metaToFieldTypeMap(String meta) throws SQLException {

        Map<String, IEntityField> results = new HashMap<>();
        List<String> metaList = null;
        try {
            metaList = JSON.parseArray(meta, String.class);
        } catch (Exception e) {
            throw new SQLException(
                    String.format("parse meta to array failed, [%s]", meta));
        }
        for (String metas : metaList) {
            String[] sMetas = metas.split(SPLITTER);
            if (sMetas.length != SPLIT_META_LENGTH) {
                throw new SQLException(
                        String.format("parse meta failed. meta value length error, should be [%d], actual [%d], meta [%s]",
                                SPLIT_META_LENGTH, sMetas.length, metas));
            }

            Long id = Long.parseLong(sMetas[0]);
            FieldType fieldType = fromRawType(sMetas[1]);

            results.put(sMetas[0], new EntityField(id, null, fieldType));
        }

        return results;
    }
}
