package com.xforceplus.ultraman.oqsengine.sdk.steady;

import java.util.List;
import java.util.Map;

/**
 * duck type querys like
 */
public interface QuerysLike {
    List<Map> querys(Map<String, Object> params);
}
