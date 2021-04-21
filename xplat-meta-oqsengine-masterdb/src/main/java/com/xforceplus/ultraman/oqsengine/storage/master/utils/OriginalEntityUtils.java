package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * desc :
 * name : OriginalEntityUtils
 *
 * @author : xujia
 * date : 2021/3/15
 * @since : 1.8
 */
public class OriginalEntityUtils {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static List<Object> attributesToList(String attrStr) throws JsonProcessingException {
        List<Object> attributes = new ArrayList<>();
        Map<String, Object> keyValues = jsonMapper.readValue(attrStr, Map.class);
        keyValues.forEach(
                (k, v) -> {
                    attributes.add(k);
                    attributes.add(v);
                }
        );
        return attributes;
    }
}
