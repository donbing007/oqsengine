package com.xforceplus.ultraman.oqsengine.storage.selector;


import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Hash 方法的 datasoruce 选择器.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 19:12
 * @since 1.8
 */
public class DataSourceHashSelector implements Selector<DataSource> {

    private List<DataSource> multipleDatasource;

    public DataSourceHashSelector(List<DataSource> multipleDatasource) {
        this.multipleDatasource = new ArrayList(multipleDatasource);
    }

    @Override
    public DataSource select(String key) {
        int address = Math.abs(Time33Hash.build().hash(key) % multipleDatasource.size());

        return multipleDatasource.get(address);
    }
}
