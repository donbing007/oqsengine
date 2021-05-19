package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.storage.Storage;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * UniqueMasterStorage.
 */
public interface UniqueMasterStorage extends Storage {


    /**
     * query by businessKey.
     *
     * @param businessKeys businessKeys
     * @param entityClass entityClass
     *
     * @return StorageUniqueEntity
     * @throws SQLException throw sqlException
     */
    Optional<StorageUniqueEntity> select(List<BusinessKey> businessKeys, IEntityClass entityClass) throws SQLException;


    /**
     * judge if contain unique config.
     *
     * @param businessKeys businessKey
     * @param entityClass entityClass
     *
     * @return boolean
     */
    boolean containUniqueConfig(List<BusinessKey> businessKeys, IEntityClass entityClass);


    /**
     * judge if contain unique config.
     *
     * @param entity entity
     * @param entityClass entityClass
     *
     * @return boolean
     */
    boolean containUniqueConfig(IEntity entity, IEntityClass entityClass);


}
