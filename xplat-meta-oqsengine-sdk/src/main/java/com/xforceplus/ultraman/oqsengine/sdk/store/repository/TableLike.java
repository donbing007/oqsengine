package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import java.util.List;
import java.util.Map;

/**
 * Table like
 */
public interface TableLike {

    String name();

    String[] columns();

    List<Map<String, ?>> getStore();

}
