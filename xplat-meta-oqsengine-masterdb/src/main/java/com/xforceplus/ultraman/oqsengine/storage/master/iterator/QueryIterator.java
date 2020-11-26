package com.xforceplus.ultraman.oqsengine.storage.master.iterator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * desc :
 * name : QueryIterator
 *
 * @author : xujia
 * date : 2020/9/11
 * @since : 1.8
 */
public interface QueryIterator {
    public int size();

    public boolean hasNext();

    public List<IEntity> next() throws SQLException;
}
