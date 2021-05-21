package com.xforceplus.ultraman.oqsengine.changelog.storage.query;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * query-side service
 *
 */
public interface QueryStorage {

    /**
     * query changelog accumulate times
     * @param ids
     * @return
     */
    Map<Long, Long> changeCountMapping(List<Long> ids, boolean isSelf) throws SQLException;

    /**
     * query changelog version for one id
     * @param id
     * @param page
     * @param size
     * @return
     */
    List<ChangeVersion> queryChangelog(long id, boolean isSelf, int page, int size) throws SQLException;

    /**
     *
     */
    int saveChangeVersion(long id, List<ChangeVersion> changeVersionList) throws SQLException;
}
