package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.BoNode;
import io.vavr.Tuple2;
import org.apache.metamodel.UpdateableDataContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * helper service for MetadataRepository
 * version mapping
 */
public interface VersionService {

    UpdateableDataContext getVersionedDCForBoById(long id, String version);

    UpdateableDataContext getVersionedDCForBoByCode(String code, String version);

    UpdateableDataContext getCurrentVersionDCForBoById(Long id);

    UpdateableDataContext getCurrentVersionDCForBoByCode(String code);

    UpdateableDataContext getVersionedDCForModule(long id, String version);

    void saveModule(long id, String version, List<BoNode> boIds);

    void initVersionedDC(int versionSize, Supplier<UpdateableDataContext> dcSupplier);

    Map<Long, String> getCurrentVersion();

    Map<BoNode, LinkedList<Tuple2<Long, String>>> getBoModuleMapping();
}
