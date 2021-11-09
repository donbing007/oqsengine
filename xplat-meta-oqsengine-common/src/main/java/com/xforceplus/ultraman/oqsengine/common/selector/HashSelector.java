package com.xforceplus.ultraman.oqsengine.common.selector;


import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基于 Hash 方法的 datasoruce 选择器.
 *
 * @param <V> 选择元素类型.
 * @author dongbin
 * @version 0.1 2020/2/16 19:12
 * @since 1.8
 */
public class HashSelector<V> implements Selector<V> {

    private List<V> targets;

    /**
     * 实例化.
     *
     * @param targets 目标元素列表明.
     */
    public HashSelector(Collection<V> targets) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("There are no optional elements.");
        }
        this.targets = new ArrayList(targets);
    }

    @Override
    public V select(String key) {
        int address = Math.abs(Time33Hash.getInstance().hash(key) % targets.size());

        return targets.get(address);
    }

    @Override
    public List<V> selects() {
        return targets.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
