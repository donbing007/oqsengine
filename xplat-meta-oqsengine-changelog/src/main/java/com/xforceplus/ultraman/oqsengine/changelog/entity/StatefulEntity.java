package com.xforceplus.ultraman.oqsengine.changelog.entity;

import java.util.List;

public interface StatefulEntity<S, I, O> {

    List<O> receive(I input);
}
