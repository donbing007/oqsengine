package com.xforceplus.ultraman.oqsengine.common.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 轮流依次选择.
 *
 * @param <V> 选择元素类型.
 * @author dongbin
 * @version 0.1 2020/2/25 18:10
 * @since 1.8
 */
public class TakeTurnsSelector<V> implements Selector<V> {

    private List<V> targets;
    private int point = 0;

    public TakeTurnsSelector(Collection<V> targets) {
        this.targets = new ArrayList<>(targets);
    }

    @Override
    public V select(String key) {
        return targets.get(point++ % targets.size());
    }
}
