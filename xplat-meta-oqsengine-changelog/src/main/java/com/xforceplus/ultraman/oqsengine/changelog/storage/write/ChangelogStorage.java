package com.xforceplus.ultraman.oqsengine.changelog.storage.write;

import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import io.vavr.control.Either;

import java.sql.SQLException;
import java.util.List;

/**
 * desc :
 * name : TaskStorage
 *
 * @author : xujia
 * date : 2020/8/24
 * @since : 1.8
 */
public interface ChangelogStorage {
    /*
        保存
     */
    Either<SQLException, Integer> saveBatch(List<Changelog> changeLogs);


    /**
     * find changelog with startVersion and endVersion
     * @param id
     * @param endVersion    -1 mean +inf
     * @param startVersion  -1 means -inf
     * @return
     */
    List<Changelog> findById(long id, long endVersion, long startVersion);


}
