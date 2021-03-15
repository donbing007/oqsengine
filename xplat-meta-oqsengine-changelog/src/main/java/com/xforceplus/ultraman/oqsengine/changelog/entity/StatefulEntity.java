package com.xforceplus.ultraman.oqsengine.changelog.entity;

import java.util.List;
import java.util.Map;

public interface StatefulEntity<S, I, O> {

    List<O> receive(I input, Map<String, Object> context);
}
